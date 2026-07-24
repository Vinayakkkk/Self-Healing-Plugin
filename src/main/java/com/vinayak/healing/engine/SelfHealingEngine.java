package com.vinayak.healing.engine;
import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.ai.AiModelClient;
import com.vinayak.healing.ai.AiResponseParser;
import com.vinayak.healing.ai.CandidatePromptBuilder;
import com.vinayak.healing.decision.HealingDecision;
import com.vinayak.healing.decision.HealingDecisionEngine;
import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.builder.FailureContextBuilder;
import com.vinayak.healing.cache.LocatorCache;
import com.vinayak.healing.validator.CachedLocatorValidator;
import com.vinayak.healing.context.FallbackContextResolver;
import com.vinayak.healing.dom.XPathFallbackGenerator;
import com.vinayak.healing.filter.CandidateFilter;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.pipeline.HealingPipeline;
import com.vinayak.healing.pipeline.PipelineResult;
import com.vinayak.healing.report.HealingReportManager;
import com.vinayak.healing.source.SourceCodeAnalyzer;
import com.vinayak.healing.validator.CandidateValidator;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.JavascriptExecutor;


public class SelfHealingEngine {

        private final CollectionHealingEngine
        collectionHealingEngine =
        new CollectionHealingEngine();

//         private final SourceRepairPolicy sourceRepairPolicy =
//         new SourceRepairPolicy();

// private final LocatorHealingService locatorHealingService =
//         new LocatorHealingService();

private final FallbackContextResolver
        fallbackContextResolver =
        new FallbackContextResolver();
        private static final boolean DEBUG = false;

        private final SourceCodeAnalyzer analyzer =
        new SourceCodeAnalyzer();
       private static final ThreadLocal<By>
        lastSuccessfulLocator =
                new ThreadLocal<>();

                private final CachedLocatorValidator cachedLocatorValidator =
        new CachedLocatorValidator();

        private final CandidateValidator candidateValidator =
        new CandidateValidator();

        private final HealingDecisionEngine healingDecisionEngine =
        new HealingDecisionEngine();
       
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

       

        // ==========================
        // DOM ANALYSIS
        // ==========================

   



       

        String pageObjectPath =
        findPageObjectFromStackTrace();


String variableName = "";
String declaration = "";


if(pageObjectPath != null) {

    variableName =
            analyzer.findVariableName(
                    pageObjectPath,
                    failedLocator.toString());

                

    declaration =
            analyzer.findDeclaration(
                    pageObjectPath,
                    failedLocator.toString());

                    
}

FailureContextBuilder builder =
        new FailureContextBuilder();

FailureContext context =
        builder.build(
                driver,
                null,
                failedLocator.toString(),
                variableName,
                declaration);

// ==========================================
// FALLBACK CONTEXT RESOLUTION
// ==========================================

if (variableName == null
        || variableName.isBlank()) {

    HealingLogger.debug(
            "PAGE OBJECT VARIABLE NOT AVAILABLE");

    context =
            fallbackContextResolver.enrich(
                    context,
                    failedLocator);

    variableName =
            context.getVariableName();
}

// ==========================================
// FINAL CONTEXT SAFETY GATE
// ==========================================

boolean hasVariable =
        context.getVariableName() != null
                && !context.getVariableName().isBlank();

boolean hasLocatorTextHint =
        context.getLocatorTextHint() != null
                && !context.getLocatorTextHint().isBlank();

boolean hasExpectedLabel =
        context.getExpectedLabel() != null
                && !context.getExpectedLabel().isBlank();

/*
 * Healing is allowed when we have at least one
 * meaningful semantic identity source.
 *
 * Page Object variable:
 *     myInfo
 *
 * Locator text:
 *     My Inffo
 *
 * Label:
 *     Employee Name
 */
if (!hasVariable
        && !hasLocatorTextHint
        && !hasExpectedLabel) {

   HealingLogger.warn(
        "Healing skipped | "
                + "Insufficient semantic identity for : "
                + failedLocator);

    return null;
}

if (variableName == null
        || variableName.isBlank()) {

   if (hasLocatorTextHint) {
    variableName = context.getLocatorTextHint();

    } else if (hasExpectedLabel) {

        variableName =
                context.getExpectedLabel();

    } else {

        variableName =
                "DIRECT_LOCATOR";
    }
}

             

                String pageObjectClass =
        pageObjectPath == null
                ? "UNKNOWN"
                : pageObjectPath.substring(
                        pageObjectPath.lastIndexOf('/') + 1)
                        .replace(".java", "");

                        String expectedIntentName =
        context.getExpectedIntent() == null
                ? "UNKNOWN"
                : context.getExpectedIntent()
                        .name();

String cacheKey =
        LocatorCache.buildKey(
                pageObjectClass,
                variableName,
                expectedIntentName,
                failedLocator.toString());

//                 By runtimeLocator =
// RUNTIME_HEALED_LOCATORS.get(cacheKey);

// if (runtimeLocator != null) {

// List<WebElement> runtimeMatches =
//         driver.findElements(runtimeLocator);

// boolean validRuntimeLocator =
//         runtimeMatches.size() == 1
//                 && runtimeMatches.get(0).isDisplayed()
//                 && runtimeMatches.get(0).isEnabled();

// if (validRuntimeLocator) {

//     System.out.println(
//             "RUNTIME CACHE HIT");

//     System.out.println(
//             "Runtime locator : "
//                     + runtimeLocator);

//     lastSuccessfulLocator.set(
//             runtimeLocator);

//     HealingAnalytics.cacheHit();

// //     int successfulHealCount =
// //         HealingSuccessTracker.recordSuccess(
// //                 cacheKey);

// // System.out.println(
// //         "Successful cached heal count : "
// //                 + successfulHealCount);

// // if (sourceRepairPolicy.canRepair(
// //         successfulHealCount)) {

// //     System.out.println(
// //             "SOURCE REPAIR APPROVED FROM CACHE");

    
// //             repairSourceIfAllowed(
// //         pageObjectPath,
// //         declaration,
// //         runtimeLocator);
// // }

//     HealingAnalytics.addHealingTime(
//             System.currentTimeMillis()
//                     - startTime);

//     return runtimeMatches.get(0);
// }

// System.out.println(
//         "STALE RUNTIME CACHE REMOVED : "
//                 + cacheKey);

// RUNTIME_HEALED_LOCATORS.remove(
//         cacheKey);

// }

// =========================================================
// PERSISTENT CACHE LOOKUP
// =========================================================

//               LocatorSuggestion cached =
//         LocatorCache.get(cacheKey);

// if (cached != null) {

// By cachedLocator = null;
// List<WebElement> cachedMatches =
//         java.util.Collections.emptyList();

// try {

//     cachedLocator =
//             LocatorBuilder.build(cached);

//     cachedMatches =
//             driver.findElements(cachedLocator);

//     boolean validCachedLocator =
//             cachedMatches.size() == 1
//                     && cachedMatches.get(0).isDisplayed()
//                     && cachedMatches.get(0).isEnabled();

//     if (validCachedLocator) {

//         WebElement element =
//                 cachedMatches.get(0);

//         lastSuccessfulLocator.set(
//                 cachedLocator);

//         HealingReportManager.logHealing(
//                 pageObjectClass,
//                 variableName,
//                 context.getExpectedIntent().name(),
//                 cacheKey,
//                 failedLocator.toString(),
//                 cachedLocator.toString(),
//                 cached.getConfidence(),
//                 "CACHE");

//         System.out.println(
//                 "CACHE HIT");

//         System.out.println(
//                 "Cache Key : "
//                         + cacheKey);

//         System.out.println(
//                 "Cached locator : "
//                         + cachedLocator);

//         HealingAnalytics.cacheHit();

// //         int successfulHealCount =
// //         HealingSuccessTracker.recordSuccess(
// //                 cacheKey);

// // System.out.println(
// //         "Successful cached heal count : "
// //                 + successfulHealCount);

// // if (sourceRepairPolicy.canRepair(
// //         successfulHealCount)) {

// //     System.out.println(
// //             "SOURCE REPAIR APPROVED FROM PERSISTENT CACHE");

// //     repairSourceIfAllowed(
// //         pageObjectPath,
// //         declaration,
// //         cachedLocator);
// // }

//         HealingAnalytics.addHealingTime(
//                 System.currentTimeMillis()
//                         - startTime);

//                         RUNTIME_HEALED_LOCATORS.put(
//         cacheKey,
//         cachedLocator);

//         return element;
//     }

//     System.out.println(
//             "STALE CACHE ENTRY DETECTED");

//     System.out.println(
//             "Cached locator : "
//                     + cachedLocator);

//     System.out.println(
//             "Matched elements : "
//                     + cachedMatches.size());

//     if (cachedMatches.isEmpty()) {

//         System.out.println(
//                 "Reason : cached locator no longer exists");

//     } else if (cachedMatches.size() > 1) {

//         System.out.println(
//                 "Reason : cached locator is not unique");

//     } else {

//         System.out.println(
//                 "Reason : cached element is hidden or disabled");
//     }

// } catch (Exception exception) {

//     System.out.println(
//             "CACHE VALIDATION ERROR : "
//                     + exception.getMessage());

//     System.out.println(
//             "Reason : cached locator could not be built or checked");
// }

// LocatorCache.remove(cacheKey);

// RUNTIME_HEALED_LOCATORS.remove(
//         cacheKey);

// System.out.println(
//         "STALE CACHE ENTRY REMOVED : "
//                 + cacheKey);

// }

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

if (validCachedLocator) {

    WebElement cachedElement =
            driver.findElement(
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

            HealingReportManager.logHealing(
        pageObjectClass,
        variableName,
        expectedIntentName,
        cacheKey,
        failedLocator.toString(),
        cachedLocator.toString(),
        cached.getConfidence(),
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

HealingPipeline pipeline =
        new HealingPipeline();

PipelineResult result =
        pipeline.execute(context);

        

List<LocatorCandidate> candidates =
        result.getCandidates();

LocatorCandidate validatedCandidate =
        result.getValidatedCandidate();

        if (validatedCandidate != null) {

    By candidateLocator =
            LocatorBuilder.build(validatedCandidate);

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


                                

  // ==========================
// DIRECT HEALING
// ==========================

HealingDecision healingDecision =
        healingDecisionEngine.decide(
                validatedCandidate,
                candidates,
                context);

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

/*
 * LOW or REJECT candidates must not be
 * automatically healed.
 *
 * Setting validatedCandidate to null allows
 * the existing AI flow below to execute.
 */
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
        driver.findElement(
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
        cacheKey,
        locator);

    lastSuccessfulLocator.set(locator);

    LocatorSuggestion suggestion =
        CandidateConverter.convert(
                locator,
                validatedCandidate.getFinalScore());

if (suggestion != null
        && healingDecision.isCacheAllowed()) {

    LocatorCache.put(
            cacheKey,
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



//         int successfulHealCount =
//         HealingSuccessTracker.recordSuccess(
//                 cacheKey);

// System.out.println(
//         "Successful heal count : "
//                 + successfulHealCount);

// if (sourceRepairPolicy.canRepair(
//         successfulHealCount)) {

//     System.out.println(
//             "SOURCE REPAIR APPROVED");

//     repairSourceIfAllowed(
//         pageObjectPath,
//         declaration,
//         locator);
// }

  HealingReportManager.logHealing(
        pageObjectClass,
        variableName,
        expectedIntentName,
        cacheKey,
        failedLocator.toString(),
        locator.toString(),
        validatedCandidate.getFinalScore(),
        healingDecision.getConfidence().name(),
        healingDecision.isHealingAllowed(),
        healingDecision.isCacheAllowed(),
        "DIRECT");

    HealingLogger.debug(
            "DIRECT HEAL SUCCESS : " + locator);

 return driver.findElement(locator);
}

        // ==========================
        // AI HEALING
// ==========================
// AI HEALING
// ==========================

if (candidates == null) {
    candidates = java.util.Collections.emptyList();
}



CandidateFilter candidateFilter =
        new CandidateFilter();

List<LocatorCandidate> filteredCandidates =
        candidateFilter.filter(
                context,
                candidates);



/*
 * AI must receive only candidates that passed
 * intent/action filtering.
 */
if (filteredCandidates.isEmpty()) {

    HealingAnalytics.failure();

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "No safe candidates available for AI healing.");
}


                    String prompt =
        CandidatePromptBuilder.build(
                context,
                filteredCandidates);
 /* 
             List<AiElementChoice> choices =
        new AiElementChoiceFinder()
                .findChoices(
                        driver,
                        filteredCandidates);

if (!choices.isEmpty()) {

    String choicePrompt =
            CandidatePromptBuilder.buildChoicePrompt(
                    context,
                    choices);

    System.out.println(
            "\n===== AI DUPLICATE CHOICE PROMPT =====\n"
                    + choicePrompt);

    String choiceResponse =
            new AiModelClient()
                    .ask(choicePrompt);

    System.out.println(
            "\n===== AI DUPLICATE CHOICE RESPONSE =====\n"
                    + choiceResponse);

    Integer selectedIndex =
            new AiResponseParser()
                    .parseCandidateIndex(
                            choiceResponse);

                            System.out.println(
        "AI SELECTED INDEX = "
                + selectedIndex);

    if (selectedIndex != null) {

        AiElementChoice selectedChoice =
                choices.stream()
                        .filter(choice ->
                                choice.getIndex()
                                        == selectedIndex)
                        .findFirst()
                        .orElse(null);

                        System.out.println(
        "AI SELECTED CHOICE = "
                + (selectedChoice == null
                ? "NULL"
                : selectedChoice.getTag()
                        + " | "
                        + selectedChoice.getText()
                        + " | "
                        + selectedChoice.getParentHref()));

        if (selectedChoice != null) {

SelectedElementLocatorBuilder locatorBuilder =
        new SelectedElementLocatorBuilder();

By uniqueLocator =
        locatorBuilder.build(
                driver,
                selectedChoice);

if (uniqueLocator != null) {

    System.out.println(
            "AI UNIQUE LOCATOR = "
                    + uniqueLocator);

    List<WebElement> matched =
            driver.findElements(uniqueLocator);

    System.out.println(
            "AI UNIQUE LOCATOR MATCH COUNT = "
                    + matched.size());

if (matched.size() == 1
        && matched.get(0).isDisplayed()
        && matched.get(0).isEnabled()) {

    String uniqueLocatorValue =
            uniqueLocator.toString()
                    .replace("By.xpath: ", "");

   if (isDynamicTextLocator(
        "xpath",
        uniqueLocatorValue)) {

    System.out.println(
            "AI duplicate-choice locator rejected: "
                    + uniqueLocatorValue);

    LocatorCandidate stableCandidate =
            findBestStableCandidate(
                    filteredCandidates);

    if (stableCandidate != null) {

        By stableLocator =
                LocatorBuilder.build(
                        stableCandidate);

        List<WebElement> stableMatches =
                driver.findElements(
                        stableLocator);

        if (stableMatches.size() == 1
                && stableMatches.get(0).isDisplayed()
                && stableMatches.get(0).isEnabled()) {

            System.out.println(
                    "STABLE CANDIDATE SELECTED AFTER "
                            + "DYNAMIC AI REJECTION: "
                            + stableLocator);

            lastSuccessfulLocator.set(
                    stableLocator);

            RUNTIME_HEALED_LOCATORS.put(
                    cacheKey,
                    stableLocator);

            LocatorSuggestion stableSuggestion =
                    CandidateConverter.convert(
                            stableCandidate);

            stableSuggestion.setConfidence(
                    stableCandidate.getFinalScore());

            LocatorCache.put(
                    cacheKey,
                    stableSuggestion);

            HealingReportManager.logHealing(
                    pageObjectClass,
                    variableName,
                    context.getExpectedIntent().name(),
                    cacheKey,
                    failedLocator.toString(),
                    stableLocator.toString(),
                    stableCandidate.getFinalScore(),
                    "STABLE_AFTER_DYNAMIC_AI_REJECTION");

            HealingAnalytics.deterministicHeal();

            HealingAnalytics.addHealingTime(
                    System.currentTimeMillis()
                            - startTime);

            return stableMatches.get(0);
        }
    }

} else {

        WebElement selectedElement =
        matched.get(0);

By optimizedLocator =
        new ValidatedElementLocatorOptimizer()
                .chooseBestLocator(
                        driver,
                        selectedElement,
                        uniqueLocator);

System.out.println(
        "AI OPTIMIZED LOCATOR = "
                + optimizedLocator);

lastSuccessfulLocator.set(
        optimizedLocator);

RUNTIME_HEALED_LOCATORS.put(
        cacheKey,
        optimizedLocator);

LocatorSuggestion suggestion =
        buildSuggestionFromBy(
                optimizedLocator);

suggestion.setConfidence(95.0);

LocatorCache.put(
        cacheKey,
        suggestion);

HealingReportManager.logHealing(
        pageObjectClass,
        variableName,
        context.getExpectedIntent().name(),
        cacheKey,
        failedLocator.toString(),
        optimizedLocator.toString(),
        95.0,
        "AI_DUPLICATE_CHOICE");

HealingAnalytics.aiHeal();

HealingAnalytics.addHealingTime(
        System.currentTimeMillis()
                - startTime);

System.out.println(
        "AI selected-element healing successful");

return selectedElement;
    }
}
}    }
    }
}*/

String aiResponse;

try {

    aiResponse =
            new AiModelClient()
                    .ask(prompt);

} catch (Exception e) {

   

    throw e;
}

                        System.out.println(
        "\n===== AI PROMPT =====\n");

        if (DEBUG) {

System.out.println(prompt);

        }

        System.out.println(
                "\n================ AI RESPONSE ================\n");

LocatorSuggestion suggestion =
        new AiResponseParser()
                .parse(aiResponse);

if (suggestion == null) {

    HealingAnalytics.failure();

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

        HealingAnalytics.failure();

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "AI returned locator not present in candidate list.");
}
         if("tag".equalsIgnoreCase(
        suggestion.getLocatorType())) {

                HealingAnalytics.failure();

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

                        HealingAnalytics.failure();

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

            throw fail(
                    "AI returned empty locator");
        }

        if (suggestion.getLocatorValue()
                .equalsIgnoreCase("wrong-username")
                || suggestion.getLocatorValue()
                        .equalsIgnoreCase("wrong-password")) {

                                HealingAnalytics.failure();

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

            throw fail(
                    "AI returned original invalid locator");
        }


        LocatorCandidate aiCandidate =
        CandidateConverter.convert(
                suggestion);
        // ==========================
        // CACHE STORE
        // ==========================


        System.out.println(
                "\nAI Suggested : "
                        + suggestion.getLocatorType()
                        + "="
                        + suggestion.getLocatorValue());

LocatorCandidate validatedAiCandidate =
        candidateValidator.validate(
                driver,
                List.of(aiCandidate),
                context);
if (validatedAiCandidate == null) {

       HealingAnalytics.failure();

    HealingAnalytics.addHealingTime(
            System.currentTimeMillis() - startTime);

    throw fail(
            "AI locator validation failed");
}

By healedLocator =
        LocatorBuilder.build(validatedAiCandidate);
        RUNTIME_HEALED_LOCATORS.put(
        cacheKey,
        healedLocator);

lastSuccessfulLocator.set(healedLocator);

WebElement element =
        driver.findElement(healedLocator);

suggestion.setConfidence(
        validatedAiCandidate.getFinalScore());

LocatorCache.put(
        cacheKey,
        suggestion);

HealingReportManager.logHealing(
        pageObjectClass,
        variableName,
        expectedIntentName,
        cacheKey,
        failedLocator.toString(),
        healedLocator.toString(),
        suggestion.getConfidence(),
        "MEDIUM",
        true,
        true,
        "AI");

HealingAnalytics.aiHeal();

HealingAnalytics.addHealingTime(
        System.currentTimeMillis() - startTime);

System.out.println(
        "Element healed successfully");

return element;
        
    }

    private void waitForDomReady(
            WebDriver driver) {

        WebDriverWait wait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(20));

        // Page load complete
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript(
                                "return document.readyState")
                        .equals("complete"));

        // Wait until Angular/React/Vue renders controls
       wait.until(d ->
        !d.findElements(
                By.cssSelector(
                        "input,button,select,textarea,a"))
                .isEmpty());
    }

private String findPageObjectFromStackTrace() {

StackTraceElement[] stack =
        Thread.currentThread().getStackTrace();



    String projectRoot =
            System.getProperty("user.dir");

            

    String latestPageObject = null;

    for (StackTraceElement element : stack) {

        String className = element.getClassName();

        

        if (!(className.startsWith("pages.")
        || className.contains(".pages."))) {
    continue;
}
        
        String fileName =
                className.substring(
                        className.lastIndexOf('.') + 1)
                        + ".java";

        try {

            java.nio.file.Path root =
                    java.nio.file.Paths.get(projectRoot);

            java.util.Optional<java.nio.file.Path> match =
                    java.nio.file.Files.walk(root)
                            .filter(path ->
                                    path.getFileName()
                                            .toString()
                                            .equals(fileName))
                            .findFirst();

            if (match.isPresent()) {

                latestPageObject =
                        match.get()
                                .toAbsolutePath()
                                .toString();

                
            }

       } catch (Exception e) {

    HealingLogger.error(
            "Page Object lookup failed",
            e);
}
    }

    if (latestPageObject == null) {

        HealingLogger.debug(
        "PAGE OBJECT NOT FOUND");
    }

    return latestPageObject;
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
// private void repairSourceIfAllowed(
//         String pageObjectPath,
//         String declaration,
//         By healedLocator) {

//     if (pageObjectPath == null
//             || pageObjectPath.isBlank()) {

//         System.out.println(
//                 "SOURCE REPAIR SKIPPED : Page object not found");

//         return;
//     }

//     if (declaration == null
//             || declaration.isBlank()) {

//         System.out.println(
//                 "SOURCE REPAIR SKIPPED : Dynamic locator");

//         return;
//     }

//     locatorHealingService.replaceLocator(
//             pageObjectPath,
//             declaration,
//             healedLocator);
// }
public String resolveVariableName(
        By locator) {

    try {

        String pageObjectPath =
                findPageObjectFromStackTrace();

        if (pageObjectPath == null) {
            return "";
        }

        return analyzer.findVariableName(
                pageObjectPath,
                locator.toString());

    } catch (Exception e) {

       HealingLogger.warn(
        "Variable resolution failed : "
                + e.getMessage());

        return "";
    }
}
public List<WebElement> healCollection(
        WebDriver driver,
        By failedLocator) {

    if (driver == null || failedLocator == null) {
        return List.of();
    }

    System.out.println(
            "\n========== PREPARING COLLECTION HEALING ==========");

    // Same source-code analysis used by normal healing
    String pageObjectPath =
            findPageObjectFromStackTrace();

    String variableName = "";
    String declaration = "";

    if (pageObjectPath != null) {

        variableName =
                analyzer.findVariableName(
                        pageObjectPath,
                        failedLocator.toString());

        declaration =
                analyzer.findDeclaration(
                        pageObjectPath,
                        failedLocator.toString());
    }

  HealingLogger.debug(
            "COLLECTION PAGE OBJECT = "
                    + pageObjectPath);

    HealingLogger.debug(
            "COLLECTION VARIABLE = "
                    + variableName);

    HealingLogger.debug(
            "COLLECTION DECLARATION = "
                    + declaration);

    FailureContextBuilder builder =
            new FailureContextBuilder();

    FailureContext context =
            builder.build(
                    driver,
                    null,
                    failedLocator.toString(),
                    variableName,
                    declaration);

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

}