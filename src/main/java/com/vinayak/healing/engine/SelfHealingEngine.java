package com.vinayak.healing.engine;
import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.ai.AiModelClient;
import com.vinayak.healing.ai.AiResponseParser;
import com.vinayak.healing.ai.CandidatePromptBuilder;
import java.util.Comparator;
import java.util.List;
import com.vinayak.healing.decision.HealingDecision;
import com.vinayak.healing.decision.HealingDecisionEngine;
import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.builder.FailureContextFactory;
import com.vinayak.healing.cache.LocatorCache;
import com.vinayak.healing.repair.RepairReport;
import org.openqa.selenium.NoSuchElementException;
import com.vinayak.healing.validator.CachedLocatorValidator;
import com.vinayak.healing.learning.LearningKey;
import com.vinayak.healing.learning.LearningRecord;
import com.vinayak.healing.dom.XPathFallbackGenerator;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.filter.CandidateFilter;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.model.TargetCardinality;
import com.vinayak.healing.outcome.engine.ExpectedOutcomeEngine;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.pipeline.HealingPipeline;
import com.vinayak.healing.pipeline.PipelineResult;
import com.vinayak.healing.repair.SourceCodeRepairEngine;
import com.vinayak.healing.report.HealingReportManager;
import com.vinayak.healing.resolver.DuplicateResolver;

import com.vinayak.healing.validator.CandidateValidator;
import com.vinayak.healing.shadow.ShadowDomDetector;
import com.vinayak.healing.shadow.ShadowDomHealingEngine;
import com.vinayak.healing.iframe.IframeHealingEngine;
import com.vinayak.healing.learning.LearningEngine;
import com.vinayak.healing.learning.LearningKey;
import com.vinayak.healing.learning.LearningRecord;
import com.vinayak.healing.learning.LearningRecorder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;


public class SelfHealingEngine {

        private static final boolean DEBUG = false;
   

        private final HealingPipeline pipeline =
        new HealingPipeline();

        private final CandidateFilter candidateFilter =
        new CandidateFilter();


        private final ExpectedOutcomeEngine
        expectedOutcomeEngine =
        new ExpectedOutcomeEngine();

private final ShadowDomHealingEngine
        shadowDomHealingEngine =
        new ShadowDomHealingEngine();
private final FailureContextFactory
        failureContextFactory =
        new FailureContextFactory();
        private final IframeHealingEngine
        iframeHealingEngine =
        new IframeHealingEngine();


       


       private static final ThreadLocal<By>
        lastSuccessfulLocator =
                new ThreadLocal<>();

                private final ThreadLocal<LearningRecord> lastLearningRecord =
        new ThreadLocal<>();

                private final CachedLocatorValidator cachedLocatorValidator =
        new CachedLocatorValidator();

        private final SourceCodeRepairEngine sourceCodeRepairEngine =
        new SourceCodeRepairEngine();

        private final CandidateValidator candidateValidator =
        new CandidateValidator();
        private final DuplicateResolver duplicateResolver =
        new DuplicateResolver();

        private final HealingDecisionEngine healingDecisionEngine =
        new HealingDecisionEngine();

        private final LearningEngine learningEngine =
        new LearningEngine();

        private final LearningRecorder learningRecorder =
        new LearningRecorder(learningEngine);

        private final ThreadLocal<LearningRecord> lastUsedLearningRecord =
        new ThreadLocal<>();

        private final CollectionHealingEngine
        collectionHealingEngine =
        new CollectionHealingEngine(learningEngine);

 public static By getLastSuccessfulLocator() {

        return lastSuccessfulLocator.get();
    }
    public WebElement heal(
            WebDriver driver,
            By failedLocator)




            throws Exception {



                long startTime = System.currentTimeMillis();

        HealingLogger.info(
                "Healing locator : "
                        + failedLocator);




        // ==========================
        // WAIT FOR PAGE + DOM
        // ==========================

        waitForDomReady(driver);

FailureContext context =
        failureContextFactory.build(
                driver,
                failedLocator,
                TargetCardinality.SINGLE);

String pageObjectPath =
        context.getPageObjectPath();

String variableName =
        context.getVariableName();





                String pageObjectClass =
        pageObjectPath == null
                ? "UNKNOWN"
                : pageObjectPath.substring(
                        pageObjectPath.lastIndexOf('/') + 1)
                        .replace(".java", "");

               String expectedIntentName =
        context.getExpectedIntent() == null
                ? "UNKNOWN"
                : context.getExpectedIntent().name();

String cacheKey =
        LocatorCache.buildKey(
                pageObjectClass,
                variableName,
                expectedIntentName,
                failedLocator.toString());





// =========================================================
// PERSISTENT CACHE LOOKUP
// =========================================================

LocatorSuggestion cached =
        LocatorCache.get(cacheKey);

if (cached != null) {

    try {

        By cachedLocator =
                LocatorBuilder.build(cached);

        boolean validCachedLocator =
        cachedLocatorValidator.validate(
                driver,
                cachedLocator,
                context);

              if (DEBUG){  System.out.println("\n===== CACHE CONTEXT =====");
System.out.println("Variable : " + context.getVariableName());
System.out.println("Tag      : " + context.getExpectedTag());
System.out.println("Intent   : " + context.getExpectedIntent());
System.out.println("Action   : " + context.getFailedAction());}

if (validCachedLocator) {

    WebElement cachedElement =
        findElementWithShadowSupport(
                driver,
                cachedLocator);

            HealingLogger.debug(
                    "CACHE HIT | "
                            + failedLocator
                            + " -> "
                            + cachedLocator);

            lastSuccessfulLocator.set(
                    cachedLocator);

            HealingAnalytics.cacheHit();

            HealingAnalytics.addHealingTime(
                    System.currentTimeMillis()
                            - startTime);

String reportAction =
        context.getFailedAction() == null
                ? "UNKNOWN"
                : context.getFailedAction().name();

System.out.println(
        "[HEALING REPORT DEBUG]"
                + " source=CACHE"
                + " | action=" + reportAction
                + " | variable=" + context.getVariableName()
                + " | failed=" + failedLocator
                + " | healed=" + cachedLocator);

HealingReportManager.logHealing(
        pageObjectClass,
        context.getVariableName(),
        reportAction,
        expectedIntentName,
        cacheKey,
        failedLocator.toString(),
        cachedLocator.toString(),
        0.0,
        "HIGH",
        true,
        true,
        "CACHE");

            return cachedElement;
        }

        /*
         * Cached locator no longer represents
         * exactly one usable element.
         */
       HealingLogger.debug(
        "STALE CACHE ENTRY | "
                + cachedLocator
                + " | semantic validation failed");

    } catch (Exception exception) {

        HealingLogger.debug(
                "CACHE VALIDATION FAILED : "
                        + exception.getMessage());
    }

    /*
     * Only remove the cache after validation fails.
     */
    LocatorCache.remove(cacheKey);

    HealingLogger.debug(
            "INVALID CACHE REMOVED : "
                    + cacheKey);
}


// =========================================================
// CACHE MISS
// =========================================================

if (cached == null) {

    HealingAnalytics.cacheMiss();

    HealingLogger.debug(
            "CACHE MISS : "
                    + cacheKey);
}

lastLearningRecord.remove();

WebElement learningElement =
        tryLearningHit(
                driver,
                context,
                pageObjectClass);

if (learningElement != null) {

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis()
                    - startTime);

    String reportAction =
            context.getFailedAction() == null
                    ? "UNKNOWN"
                    : context.getFailedAction().name();

    By learnedLocatorForReport =
            lastSuccessfulLocator.get();

    /*
     * IMPORTANT:
     *
     * tryLearningHit() has already selected the
     * historical LearningRecord that was actually
     * used for this healing.
     *
     * DO NOT call findLearningRecord() again here.
     *
     * A second lookup can return a different/older
     * record and corrupt the healing report.
     */

   LearningRecord learningRecord =
        findLearningRecord(
                context,
                pageObjectClass);

    double reportScore =
            learningRecord == null
                    ? 0.0
                    : learningRecord.getCandidateScore();

    String reportConfidence =
            learningRecord == null
                    ? "UNKNOWN"
                    : learningRecord.getConfidenceLevel();

    boolean reportHealingAllowed =
            learningRecord != null
                    && learningRecord.isHealingAllowed();

    boolean reportCacheAllowed =
            learningRecord != null
                    && learningRecord.isCacheAllowed();

    System.out.println(
            "[HEALING REPORT DEBUG]"
                    + " source=LEARNING"
                    + " | action=" + reportAction
                    + " | variable=" + context.getVariableName()
                    + " | failed=" + failedLocator
                    + " | healed=" + learnedLocatorForReport
                    + " | score=" + reportScore
                    + " | confidence=" + reportConfidence
                    + " | healingAllowed=" + reportHealingAllowed
                    + " | cacheAllowed=" + reportCacheAllowed);

    HealingReportManager.logHealing(
            pageObjectClass,
            context.getVariableName(),
            reportAction,
            expectedIntentName,
            cacheKey,
            failedLocator.toString(),
            learnedLocatorForReport == null
                    ? "UNKNOWN"
                    : learnedLocatorForReport.toString(),
            reportScore,
            reportConfidence,
            reportHealingAllowed,
            reportCacheAllowed,
            "LEARNING");

    return learningElement;
}


HealingLogger.debug(
        "LEARNING FALLBACK | "
                + "continuing normal candidate healing");
PipelineResult result =
        pipeline.execute(context);



List<LocatorCandidate> candidates =
        new ArrayList<>(result.getCandidates());

List<LocatorCandidate> shadowCandidates =
        shadowDomHealingEngine.findCandidates(
                driver,
                context);
if (DEBUG) {
                System.out.println("\n===== SHADOW CANDIDATES =====");
System.out.println("Count = " + shadowCandidates.size());

for (LocatorCandidate c : shadowCandidates) {
    System.out.println(
        c.getLocatorType()
        + "="
        + c.getLocatorValue()
        + " tag="
        + c.getTagName());
}}

if (!shadowCandidates.isEmpty()) {

    HealingLogger.debug(
            "Shadow candidates found : "
                    + shadowCandidates.size());

    candidates.addAll(shadowCandidates);
}

/*
 * Validate AFTER merging all candidates.
 */


LocatorCandidate validatedCandidate =
        candidateValidator.validate(
                driver,
                candidates,
                context);

if (validatedCandidate == null) {

   LocatorCandidate resolvedCandidate =
            duplicateResolver.resolve(
                    context,
                    candidates);


    if (resolvedCandidate != null) {

       validatedCandidate =
                candidateValidator.validate(
                        driver,
                        List.of(resolvedCandidate),
                        context);
    }
}

        /*
 * Normal DOM healing failed.
 * Search inside Shadow DOM before
 * trying XPath fallback or AI.
 */


        if (validatedCandidate != null) {



    By candidateLocator =
            LocatorBuilder.build(validatedCandidate);

            if (DEBUG) {

            System.out.println("\n===== SAFETY CHECK =====");

System.out.println(
        "Locator = "
        + candidateLocator);

System.out.println(
        "Unsafe = "
        + isUnsafeGeneratedLocator(candidateLocator));}




    if (isUnsafeGeneratedLocator(candidateLocator)) {

        HealingLogger.debug(
                "REJECTED UNSAFE DIRECT HEAL : "
                        + candidateLocator);

        validatedCandidate = null;
    }
}

        if (validatedCandidate == null) {

    HealingLogger.debug(
            "DIRECT LOCATORS FAILED. "
                    + "TRYING XPATH FALLBACK.");

    String html =
            context.getPageSource();

    if (html == null || html.isBlank()) {
        html = driver.getPageSource();
    }

    List<LocatorCandidate> xpathFallbackCandidates =
            new XPathFallbackGenerator()
                    .generate(
                            html,
                            context);

    HealingLogger.debug(
            "XPATH FALLBACK CANDIDATES = "
                    + xpathFallbackCandidates.size());

    if (!xpathFallbackCandidates.isEmpty()) {

        LocatorCandidate xpathValidatedCandidate =
                candidateValidator.validate(
                        driver,
                        xpathFallbackCandidates,
                        context);

        if (xpathValidatedCandidate != null) {

    validatedCandidate =
            xpathValidatedCandidate;

    /*
     * The decision engine must evaluate the
     * candidate set from which this candidate
     * was actually selected.
     */
    candidates =
            xpathFallbackCandidates;

    HealingLogger.debug(
            "XPATH FALLBACK VALIDATED = "
                    + validatedCandidate.getLocatorType()
                    + " = "
                    + validatedCandidate.getLocatorValue());
}
    }
}
if (DEBUG) {
System.out.println("\n===== VALIDATION RESULT =====");

if (validatedCandidate == null) {

    System.out.println("Validated Candidate : NULL");

} else {

    System.out.println("Validated Candidate : "
            + validatedCandidate.getLocatorType()
            + "="
            + validatedCandidate.getLocatorValue());

    System.out.println("Score : "
            + validatedCandidate.getFinalScore());
}}

if (DEBUG) {
System.out.println("\n===== BEFORE DECISION ENGINE =====");

System.out.println(
        "validatedCandidate = "
        + (validatedCandidate == null
            ? "NULL"
            : validatedCandidate.getLocatorValue()));}

  // ==========================
// DIRECT HEALING
// ==========================

HealingDecision healingDecision =
        healingDecisionEngine.decide(
                validatedCandidate,
                candidates,
                context);

String resolvedExpectedIntentName =
        expectedIntentName;

if ("UNKNOWN".equalsIgnoreCase(resolvedExpectedIntentName)
        && validatedCandidate != null
        && validatedCandidate.getIntent() != null) {

    resolvedExpectedIntentName =
            validatedCandidate.getIntent().name();

    HealingLogger.debug(
            "EXPECTED INTENT RESOLVED FROM CANDIDATE"
                    + " | initial=" + expectedIntentName
                    + " | candidate="
                    + validatedCandidate.getIntent()
                    + " | resolved="
                    + resolvedExpectedIntentName);
}

String resolvedCacheKey =
        LocatorCache.buildKey(
                pageObjectClass,
                variableName,
                resolvedExpectedIntentName,
                failedLocator.toString());

HealingLogger.debug(
        "RESOLVED EXPECTED INTENT"
                + " | initial=" + expectedIntentName
                + " | resolved=" + resolvedExpectedIntentName
                + " | candidate="
                + (validatedCandidate == null
                        ? "UNKNOWN"
                        : validatedCandidate.getIntent()));

HealingLogger.debug(
        "RESOLVED CACHE KEY"
                + " | old=" + cacheKey
                + " | new=" + resolvedCacheKey);

HealingLogger.debug(
        "\n===== HEALING DECISION =====");

HealingLogger.debug(
        "Confidence = "
                + healingDecision.getConfidence());

HealingLogger.debug(
        "Healing Allowed = "
                + healingDecision.isHealingAllowed());

HealingLogger.debug(
        "Cache Allowed = "
                + healingDecision.isCacheAllowed());

HealingLogger.debug(
        "Reason = "
                + healingDecision.getReason());


if (!healingDecision.isHealingAllowed()) {

    HealingLogger.debug(
            "DIRECT HEALING REJECTED BY DECISION ENGINE");

    validatedCandidate = null;
}

if (validatedCandidate != null) {

        HealingAnalytics.deterministicHeal();

HealingAnalytics.addHealingTime(
        System.currentTimeMillis() - startTime);

          HealingLogger.debug(
            "Validated Candidate : "
                    + validatedCandidate.getLocatorType()
                    + "="
                    + validatedCandidate.getLocatorValue());
    By candidateLocator =
        LocatorBuilder.build(
                validatedCandidate);

WebElement validatedElement =
        findElementWithShadowSupport(
                driver,
                candidateLocator);

By locator =
        new ValidatedElementLocatorOptimizer()
                .chooseBestLocator(
                        driver,
                        validatedElement,
                        candidateLocator);

HealingLogger.debug(
        "OPTIMIZED HEALED LOCATOR : "
                + locator);

RUNTIME_HEALED_LOCATORS.put(
        resolvedCacheKey,
        locator);

        RUNTIME_HEALED_LOCATORS.put(
        failedLocator.toString(),
        locator);

    lastSuccessfulLocator.set(locator);

   learningRecorder.record(
        context,
        pageObjectClass,
        validatedCandidate,
        locator,
        healingDecision.getConfidence().name(),
        healingDecision.isHealingAllowed(),
        healingDecision.isCacheAllowed(),
        "DIRECT",
        true,
        healingDecision.isCacheAllowed()
                ? 100.0
                : 80.0);

    LocatorSuggestion suggestion =
        CandidateConverter.convert(
                locator,
                validatedCandidate.getFinalScore());

if (suggestion != null
        && healingDecision.isCacheAllowed()) {

    LocatorCache.put(
        resolvedCacheKey,
        suggestion);

    HealingLogger.debug(
            "PERSISTENT CACHE STORED"
                    + " | confidence="
                    + healingDecision.getConfidence());

} else if (suggestion == null) {

    HealingLogger.debug(
            "PERSISTENT CACHE SKIPPED"
                    + " | optimized locator could not be converted");

} else {

    HealingLogger.debug(
            "PERSISTENT CACHE SKIPPED"
                    + " | confidence="
                    + healingDecision.getConfidence());
}


  String reportAction =
        context.getFailedAction() == null
                ? "UNKNOWN"
                : context.getFailedAction().name();

double reportScore =
        validatedCandidate.getFinalScore();

String reportConfidence =
        healingDecision.getConfidence().name();

boolean reportHealingAllowed =
        healingDecision.isHealingAllowed();

boolean reportCacheAllowed =
        healingDecision.isCacheAllowed();

System.out.println(
        "[HEALING REPORT DEBUG]"
                + " source=DIRECT"
                + " | action=" + reportAction
                + " | variable=" + context.getVariableName()
                + " | failed=" + failedLocator
                + " | healed=" + locator
                + " | score=" + reportScore
                + " | confidence=" + reportConfidence
                + " | healingAllowed=" + reportHealingAllowed
                + " | cacheAllowed=" + reportCacheAllowed);

HealingReportManager.logHealing(
        pageObjectClass,
        context.getVariableName(),
        reportAction,
        resolvedExpectedIntentName,
        resolvedCacheKey,
        failedLocator.toString(),
        locator.toString(),
        reportScore,
        reportConfidence,
        reportHealingAllowed,
        reportCacheAllowed,
        "DIRECT");

    HealingLogger.debug(
            "DIRECT HEAL SUCCESS : " + locator);

if (context.getPageObjectPath() != null
        && !context.getPageObjectPath().isBlank()) {


    try {

        RepairReport report =


                sourceCodeRepairEngine.repair(
                        context,
                        suggestion);

if (report.isRepairSuccessful()) {

    HealingLogger.info(
            "Page Object repaired : "
                    + report.getPageObjectFile());

} else {

    String message = report.getMessage();

    if (message != null
            && message.contains(
                    "parameterized dynamic locator method")) {

        HealingLogger.info(
                "Source repair skipped intentionally : "
                        + message);

    } else {

        HealingLogger.warn(
                "Source repair failed : "
                        + message);
    }
}

} catch (Exception e) {

    HealingLogger.warn(
            "Source repair skipped : "
                    + e.getMessage());
}

} else {

    HealingLogger.debug(
            "Skipping source repair: Page Object path not available.");
}

 return findElementWithShadowSupport(
        driver,
        locator);
}



if (candidates == null) {
    candidates = java.util.Collections.emptyList();
}


List<LocatorCandidate> filteredCandidates =
        candidateFilter.filter(
                context,
                candidates);



/*
 * AI must receive only candidates that passed
 * intent/action filtering.
 */
if (filteredCandidates.isEmpty()) {

    HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "No safe candidates available for AI healing.");
}


                    String prompt =
        CandidatePromptBuilder.build(
                context,
                filteredCandidates);


String aiResponse;

try {

    aiResponse =
            new AiModelClient()
                    .ask(prompt);

} catch (Exception e) {



    throw e;
}
if (DEBUG) {

    System.out.println(
            "\n===== AI PROMPT =====\n");

    System.out.println(prompt);

    System.out.println(
            "\n================ AI RESPONSE ================\n");
}

LocatorSuggestion suggestion =
        new AiResponseParser()
                .parse(aiResponse);

if (suggestion == null) {

    HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "AI failed to provide locator suggestion");
}

boolean valid = filteredCandidates.stream().anyMatch(c ->

        c.getLocatorType() != null
        && c.getLocatorValue() != null

        && c.getLocatorType()
                .equalsIgnoreCase(
                        suggestion.getLocatorType())

        && c.getLocatorValue()
                .equalsIgnoreCase(
                        suggestion.getLocatorValue()));

if (!valid) {

       HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "AI returned locator not present in candidate list.");
}
         if("tag".equalsIgnoreCase(
        suggestion.getLocatorType())) {

               HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "Unsupported AI locator type : tag");
}

        // ==========================
        // VALIDATION
        // ==========================

        if (suggestion.getLocatorValue() == null
                || suggestion.getLocatorValue().isBlank()) {

                       HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

            throw fail(
                    "AI returned empty locator");
        }

        if (suggestion.getLocatorValue()
                .equalsIgnoreCase("wrong-username")
                || suggestion.getLocatorValue()
                        .equalsIgnoreCase("wrong-password")) {

                                HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

            throw fail(
                    "AI returned original invalid locator");
        }


LocatorCandidate aiCandidate =
        filteredCandidates.stream()
                .filter(c ->
                        suggestion.getLocatorType()
                                .equalsIgnoreCase(c.getLocatorType())
                        &&
                        suggestion.getLocatorValue()
                                .equalsIgnoreCase(c.getLocatorValue()))
                .findFirst()
                .orElse(null);

if (aiCandidate == null) {
    throw fail("AI selected candidate not found.");
}

LocatorCandidate validatedAiCandidate =
        candidateValidator.validate(
                driver,
                List.of(aiCandidate),
                context);
if (validatedAiCandidate == null) {

       HealingAnalytics.failure(pageObjectClass);

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "AI locator validation failed");
}

By healedLocator =
        LocatorBuilder.build(validatedAiCandidate);
       RUNTIME_HEALED_LOCATORS.put(
        resolvedCacheKey,
        healedLocator);

lastSuccessfulLocator.set(healedLocator);

learningRecorder.record(
        context,
        pageObjectClass,
        validatedAiCandidate,
        healedLocator,
        "MEDIUM",
        true,
        false,
        "AI",
        true,
100.0);

WebElement element =
        findElementWithShadowSupport(
                driver,
                healedLocator);

suggestion.setConfidence(
        validatedAiCandidate.getFinalScore());

LocatorCache.put(
        resolvedCacheKey,
        suggestion);

String reportAction =
        context.getFailedAction() == null
                ? "UNKNOWN"
                : context.getFailedAction().name();

double reportScore =
        validatedAiCandidate.getFinalScore();

String reportConfidence =
        "MEDIUM";

System.out.println(
        "[HEALING REPORT DEBUG]"
                + " source=AI"
                + " | action=" + reportAction
                + " | variable=" + context.getVariableName()
                + " | failed=" + failedLocator
                + " | healed=" + healedLocator
                + " | score=" + reportScore
                + " | confidence=" + reportConfidence);

HealingReportManager.logHealing(
        pageObjectClass,
        context.getVariableName(),
        reportAction,
        resolvedExpectedIntentName,
        resolvedCacheKey,
        failedLocator.toString(),
        healedLocator.toString(),
        reportScore,
        reportConfidence,
        true,
        true,
        "AI");

HealingAnalytics.aiHeal();

HealingAnalytics.addHealingTime(
        System.currentTimeMillis() - startTime);

System.out.println(
        "Element healed successfully");


if (context.getPageObjectPath() != null
        && !context.getPageObjectPath().isBlank()) {

    try {

        RepairReport report =
                sourceCodeRepairEngine.repair(
                        context,
                        suggestion);

        if (report.isRepairSuccessful()) {

            HealingLogger.info(
                    "Page Object repaired : "
                            + report.getPageObjectFile());

        } else {

            HealingLogger.warn(
                    "Repair failed : "
                            + report.getMessage());
        }

    } catch (Exception e) {

        HealingLogger.warn(
                "Source repair skipped : "
                        + e.getMessage());
    }

} else {

    HealingLogger.debug(
            "Skipping source repair: Page Object path not available.");
}

return element;

    }
public WebElement heal(
        WebDriver driver,
        By failedLocator,
        ExecutionAction action)
        throws Exception {

    HealingLogger.debug(
            "ACTION-AWARE HEALING | locator="
                    + failedLocator
                    + " | action="
                    + action);

    /*
     * The action has already been recorded by
     * HealingWebElement before this method is called.
     *
     * Therefore the existing one-argument healing
     * pipeline can build FailureContext with the
     * correct failed action.
     */
    return heal(
            driver,
            failedLocator);
}
/**
 * Attempts to reuse a previously successful healing experience.
 *
 * The learned locator is never trusted blindly.
 * It must first pass the same semantic cached-locator
 * validation used by the persistent locator cache.
 */

private WebElement tryLearningHit(
        WebDriver driver,
        FailureContext context,
        String pageObjectClass) {

                 lastUsedLearningRecord.remove();

    

    if (driver == null
            || context == null) {

        return null;
    }

    String expectedIntent =
            context.getExpectedIntent() == null
                    ? "UNKNOWN"
                    : context.getExpectedIntent().name();

    String action =
            context.getFailedAction() == null
                    ? "UNKNOWN"
                    : context.getFailedAction().name();

    LearningKey learningKey =
            new LearningKey(
                    pageObjectClass,
                    context.getVariableName(),
                    expectedIntent,
                    action,
                    context.getFailedLocator());

    HealingLogger.debug(
            "\n========== LEARNING LOOKUP ==========");

    HealingLogger.debug(
            "Learning Key : "
                    + learningKey);

    List<LearningRecord> history =
            learningEngine.findHistory(
                    learningKey);

    if (history == null
            || history.isEmpty()) {

        HealingLogger.debug(
                "LEARNING MISS | no history found");

        HealingLogger.debug(
                "====================================");

        return null;
    }

   /*
 * ==========================================
 * LEARNING SELECTION
 * ==========================================
 *
 * Learning has two levels:
 *
 * HIGH / cacheAllowed = true
 *     -> direct reusable learning
 *
 * MEDIUM / cacheAllowed = false
 *     -> intermediate learning
 *
 * Intermediate learning is allowed only when
 * the previous healing outcome reached the
 * minimum confidence threshold.
 *
 * The learned locator is still validated
 * against the current DOM before reuse.
 */

LearningRecord bestRecord =
        learningEngine.findBestLearning(
                learningKey);

    if (bestRecord == null) {

        HealingLogger.debug(
                "LEARNING MISS | no reusable successful record");

        HealingLogger.debug(
                "====================================");

        return null;
    }
    lastLearningRecord.set(bestRecord);

    HealingLogger.debug(
            "LEARNING RECORD FOUND");

            String learningLevel =
        bestRecord.isCacheAllowed()
                ? "HIGH"
                : "INTERMEDIATE";

HealingLogger.debug(
        "Learning Level    : "
                + learningLevel);

HealingLogger.debug(
        "Cache Allowed     : "
                + bestRecord.isCacheAllowed());

    HealingLogger.debug(
            "Selected Locator : "
                    + bestRecord.getSelectedLocator());

    HealingLogger.debug(
            "Locator Type     : "
                    + bestRecord.getSelectedLocatorType());

    HealingLogger.debug(
            "Locator Value    : "
                    + bestRecord.getSelectedLocatorValue());

    HealingLogger.debug(
            "Outcome Confidence : "
                    + bestRecord.getOutcomeConfidence());

    HealingLogger.debug(
            "Candidate Score    : "
                    + bestRecord.getCandidateScore());

    /*
     * ==========================================
     * BUILD LEARNED LOCATOR
     * ==========================================
     */

    By learnedLocator;

    try {

        learnedLocator =
                LocatorBuilder.build(
                        bestRecord.getSelectedLocatorType(),
                        bestRecord.getSelectedLocatorValue());

    } catch (Exception exception) {

        HealingLogger.debug(
                "LEARNING LOCATOR BUILD FAILED | "
                        + exception.getMessage());

        return null;
    }

    if (learnedLocator == null) {

        HealingLogger.debug(
                "LEARNING LOCATOR BUILD FAILED | null locator");

        return null;
    }

    /*
     * ==========================================
     * SAFETY CHECK
     * ==========================================
     */

    if (isUnsafeGeneratedLocator(learnedLocator)) {

        HealingLogger.debug(
                "LEARNING LOCATOR REJECTED | unsafe locator = "
                        + learnedLocator);

        return null;
    }

    /*
     * ==========================================
     * CURRENT DOM VALIDATION
     * ==========================================
     *
     * IMPORTANT:
     *
     * Learning is experience, NOT blind trust.
     *
     * The learned locator must still represent
     * the correct element on the current page.
     */

    boolean valid;

    try {

        HealingLogger.debug(
        "LEARNING VALIDATION START"
                + " | locator=" + learnedLocator
                + " | page=" + pageObjectClass
                + " | variable=" + context.getVariableName()
                + " | intent=" + expectedIntent
                + " | action=" + action);

        valid =
                cachedLocatorValidator.validate(
                        driver,
                        learnedLocator,
                        context);

                        HealingLogger.debug(
        "LEARNING VALIDATION RESULT"
                + " | locator=" + learnedLocator
                + " | valid=" + valid);

    } catch (Exception exception) {

        HealingLogger.debug(
                "LEARNING VALIDATION FAILED | "
                        + exception.getMessage());

        return null;
    }

    if (!valid) {

        HealingLogger.debug(
                "LEARNING STALE | learned locator failed validation");

        HealingLogger.debug(
                "Learned Locator : "
                        + learnedLocator);

        HealingLogger.debug(
                "====================================");

        return null;
    }

    /*
     * ==========================================
     * FINAL ELEMENT LOOKUP
     * ==========================================
     */

    try {

        WebElement element =
                findElementWithShadowSupport(
                        driver,
                        learnedLocator);

        if (element == null) {

            HealingLogger.debug(
                    "LEARNING HIT FAILED | element is null");

            return null;
        }

        /*
         * ==========================================
         * LEARNING HIT
         * ==========================================
         */

        HealingLogger.debug(
                "\n========== LEARNING HIT ==========");

        HealingLogger.debug(
                "Failed Locator : "
                        + context.getFailedLocator());

        HealingLogger.debug(
                "Learned Locator : "
                        + learnedLocator);

        HealingLogger.debug(
        "Learning Source : PREVIOUS_SUCCESS");

HealingLogger.debug(
        "Learning Level  : "
                + (bestRecord.isCacheAllowed()
                        ? "HIGH"
                        : "INTERMEDIATE"));

        HealingLogger.debug(
                "Outcome Confidence : "
                        + bestRecord.getOutcomeConfidence());

        HealingLogger.debug(
                "==================================");

        lastSuccessfulLocator.set(
                learnedLocator);

        HealingAnalytics.deterministicHeal();

       HealingLogger.debug(
        "LEARNING HEAL SUCCESS : "
                + learnedLocator
                + " | level="
                + (bestRecord.isCacheAllowed()
                        ? "HIGH"
                        : "INTERMEDIATE"));

        return element;

    } catch (Exception exception) {

        HealingLogger.debug(
                "LEARNING ELEMENT LOOKUP FAILED | "
                        + exception.getMessage());

        return null;
    }
}

private void waitForDomReady(
        WebDriver driver) {

    WebDriverWait wait =
            new WebDriverWait(
                    driver,
                    Duration.ofSeconds(20));

    /*
     * ==========================================
     * PHASE 1 - DOCUMENT READY
     * ==========================================
     */

    wait.until(webDriver -> {

        Object readyState =
                ((JavascriptExecutor) webDriver)
                        .executeScript(
                                "return document.readyState");

        return "complete".equals(readyState);
    });


    /*
     * ==========================================
     * PHASE 2 - DOM STABILITY
     * ==========================================
     *
     * document.readyState == complete does NOT
     * mean that a SPA has finished rendering.
     *
     * React / Angular / Vue applications may
     * continue modifying the DOM after the
     * document becomes complete.
     *
     * We therefore wait until the DOM fingerprint
     * remains unchanged for a short period.
     */

    final String[] previousFingerprint = { null };
    final long[] stableSince = { 0L };

    wait.until(webDriver -> {

        JavascriptExecutor js =
                (JavascriptExecutor) webDriver;

        Object fingerprintObject =
                js.executeScript(
                        """
                        const root =
                            document.documentElement;

                        if (!root) {
                            return null;
                        }

                        return [
                            root.outerHTML.length,
                            document.body
                                ? document.body.innerText.length
                                : 0,
                            document.querySelectorAll("*").length
                        ].join("|");
                        """);

        if (fingerprintObject == null) {
            return false;
        }

        String fingerprint =
                fingerprintObject.toString();

        long now =
                System.currentTimeMillis();

        /*
         * First observation.
         */
        if (previousFingerprint[0] == null) {

            previousFingerprint[0] =
                    fingerprint;

            stableSince[0] =
                    now;

            return false;
        }

        /*
         * DOM changed.
         *
         * Restart stability timer.
         */
        if (!fingerprint.equals(
                previousFingerprint[0])) {

            previousFingerprint[0] =
                    fingerprint;

            stableSince[0] =
                    now;

            return false;
        }

        /*
         * DOM has remained unchanged.
         */
        return now - stableSince[0] >= 500;
    });
}



private String xpathLiteral(String value) {

    if (!value.contains("'")) {
        return "'" + value + "'";
    }

    if (!value.contains("\"")) {
        return "\"" + value + "\"";
    }

    StringBuilder result =
            new StringBuilder("concat(");

    String[] parts =
            value.split("'");

    for (int i = 0; i < parts.length; i++) {

        if (i > 0) {
            result.append(", \"'\", ");
        }

        result.append("'")
                .append(parts[i])
                .append("'");
    }

    result.append(")");

    return result.toString();
}

private RuntimeException fail(String message) {



    return new RuntimeException(message);
}
private static final Map<String, By> RUNTIME_HEALED_LOCATORS =
        new ConcurrentHashMap<>();


public static By getRuntimeHealedLocator(By failedLocator) {

    if (failedLocator == null) {
        return null;
    }

    return RUNTIME_HEALED_LOCATORS.get(
            failedLocator.toString());
}

private boolean isUnsafeGeneratedLocator(
        By locator) {

    if (locator == null) {
        return true;
    }

    String value =
            locator.toString()
                    .toLowerCase();

    /*
     * Absolute DOM paths are positional and unstable:
     * /html[1]/body[1]/div[1]/...
     *
     * They must never be persisted as healed locators.
     */
    return value.contains("/html[")
            || value.contains("/body[");
}

private LocatorCandidate findBestStableCandidate(
        List<LocatorCandidate> candidates) {

    if (candidates == null) {
        return null;
    }

    for (LocatorCandidate candidate : candidates) {

        if (candidate == null
                || candidate.getLocatorType() == null
                || candidate.getLocatorValue() == null) {
            continue;
        }

        String type =
                candidate.getLocatorType();

        String value =
                candidate.getLocatorValue();

        /*
         * Never select dynamic text candidates such as:
         * text=1
         * xpath=//span[normalize-space()='1']
         */
        if ("text".equalsIgnoreCase(type)) {
            continue;
        }

        if (isDynamicTextLocator(type, value)) {
            continue;
        }

        /*
         * Direct stable attributes are preferred.
         * Your ranked first candidate is:
         * data-test=shopping-cart-badge
         */
        if (type.equalsIgnoreCase("data-test")
                || type.equalsIgnoreCase("data-testid")
                || type.equalsIgnoreCase("data-qa")
                || type.equalsIgnoreCase("data-cy")
                || type.equalsIgnoreCase("id")
                || type.equalsIgnoreCase("name")
                || type.equalsIgnoreCase("css")
                || type.equalsIgnoreCase("class")) {

            return candidate;
        }
    }

    return null;
}
private boolean isDynamicTextLocator(
        String locatorType,
        String locatorValue) {

    if (locatorType == null
            || locatorValue == null) {
        return false;
    }

    if (!locatorType.equalsIgnoreCase("xpath")) {
        return false;
    }

    String value =
            locatorValue.toLowerCase();

    return value.matches(
            ".*(text\\(\\)|normalize-space\\(\\))"
                    + "\\s*=\\s*['\"]\\d+['\"].*");
}
private LocatorSuggestion buildSuggestionFromBy(
        By locator) {

    LocatorSuggestion suggestion =
            new LocatorSuggestion();

    String text =
            locator.toString();

    if (text.startsWith("By.id: ")) {

        suggestion.setLocatorType("id");
        suggestion.setLocatorValue(
                text.replace("By.id: ", ""));

    } else if (text.startsWith("By.name: ")) {

        suggestion.setLocatorType("name");
        suggestion.setLocatorValue(
                text.replace("By.name: ", ""));

    } else if (text.startsWith("By.cssSelector: ")) {

        suggestion.setLocatorType("css");
        suggestion.setLocatorValue(
                text.replace("By.cssSelector: ", ""));

    } else if (text.startsWith("By.xpath: ")) {

        suggestion.setLocatorType("xpath");
        suggestion.setLocatorValue(
                text.replace("By.xpath: ", ""));
    }

    return suggestion;
}


public List<WebElement> healCollection(
        WebDriver driver,
        By failedLocator) {

    if (driver == null || failedLocator == null) {
        return List.of();
    }
HealingLogger.debug(
        "BEFORE FAILURE CONTEXT | action="
                + ExecutionTracker.getContext()
                        .getLatestAction());
FailureContext context =
        failureContextFactory.build(
                driver,
                failedLocator,
                TargetCardinality.COLLECTION);



String variableName =
        context.getVariableName();


                    if (variableName == null
        || variableName.isBlank()) {

    HealingLogger.debug(
            "COLLECTION HEALING SKIPPED | "
                    + "No Page Object variable resolved for : "
                    + failedLocator);

    return List.of();
}

    return collectionHealingEngine.heal(
            driver,
            failedLocator,
            context);
}
private WebElement findElementWithShadowSupport(
        WebDriver driver,
        By locator) {

    List<WebElement> elements =
            driver.findElements(locator);

    if (elements.size() == 1) {
        return elements.get(0);
    }



   List<WebElement> hosts =
        ShadowDomDetector.findShadowHosts(driver);

    for (WebElement host : hosts) {

        try {


            SearchContext shadowRoot =
        host.getShadowRoot();

        if (shadowRoot == null) {
            continue;
        }

By shadowLocator = locator;

String locatorString = locator.toString();

if (locatorString.startsWith("By.id: ")) {

    shadowLocator = By.cssSelector(
            "#" + locatorString.replace("By.id: ", ""));

} else if (locatorString.startsWith("By.name: ")) {

    String value =
            locatorString.replace("By.name: ", "");

    shadowLocator =
            By.cssSelector(
                    "[name='" + value + "']");

} else if (locatorString.startsWith("By.className: ")) {

    shadowLocator =
            By.cssSelector(
                    "." + locatorString.replace("By.className: ", ""));
}

/*
 * ChromeDriver does not support XPath on ShadowRoot.
 */
if (locatorString.startsWith("By.xpath:")) {
    continue;
}

List<WebElement> shadowElements =
        shadowRoot.findElements(shadowLocator);

if (shadowElements.size() == 1) {
    return shadowElements.get(0);
}

        } catch (Exception ignored) {
        }
    }

WebElement iframeElement =
        iframeHealingEngine.findElement(
                driver,
                locator);

if (iframeElement != null) {

    HealingLogger.debug(
            "Element found inside iframe.");

    return iframeElement;
}

throw new NoSuchElementException(
        "Unable to locate " + locator);
}
private double confidenceToOutcomeScore(
        CollectionDecisionEngine.Decision confidence) {

    if (confidence == null) {
        return 0.0;
    }

    switch (confidence) {

        case HIGH:
            return 100.0;

        case MEDIUM:
            return 60.0;

        case LOW:
        case REJECT:
        default:
            return 0.0;
    }
}
private LearningRecord findLearningRecord(
        FailureContext context,
        String pageObjectClass) {

    if (context == null) {
        return null;
    }

    String expectedIntent =
            context.getExpectedIntent() == null
                    ? "UNKNOWN"
                    : context.getExpectedIntent().name();

    String action =
            context.getFailedAction() == null
                    ? "UNKNOWN"
                    : context.getFailedAction().name();

    LearningKey key =
            new LearningKey(
                    pageObjectClass,
                    context.getVariableName(),
                    expectedIntent,
                    action,
                    context.getFailedLocator());

    List<LearningRecord> history =
            learningEngine.findHistory(key);

    if (history == null || history.isEmpty()) {
        return null;
    }

    return history.stream()

            // Never reuse an unsuccessful experience
            .filter(LearningRecord::isOutcomeSuccess)

            // Only trusted learning is eligible
            .filter(LearningRecord::isHealingAllowed)

            // HIGH before MEDIUM
            .sorted(
                    Comparator
                            .comparingInt(
                                    (LearningRecord record) ->
                                            confidenceWeight(
                                                    record.getConfidenceLevel()))
                            .reversed()

                            // Higher candidate score first
                            .thenComparing(
                                    LearningRecord::getCandidateScore,
                                    Comparator.reverseOrder())

                            // More recent experience first
                            .thenComparing(
                                    LearningRecord::getTimestamp,
                                    Comparator.reverseOrder())
            )

            .findFirst()
            .orElse(null);
}
private int confidenceWeight(
        String confidence) {

    if (confidence == null) {
        return 0;
    }

    switch (confidence.trim().toUpperCase()) {

        case "HIGH":
            return 3;

        case "MEDIUM":
            return 2;

        case "LOW":
            return 1;

        case "REJECT":
        default:
            return 0;
    }
}
}