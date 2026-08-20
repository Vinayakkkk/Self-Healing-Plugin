package com.vinayak.healing.ranking;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import com.vinayak.healing.similarity.SimilarityUtil;
import com.vinayak.healing.analysis.DynamicPatternAnalyzer;
import com.vinayak.healing.analysis.LocatorQualityAnalyzer;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.learning.LearningEngine;
import com.vinayak.healing.learning.LearningKey;
import com.vinayak.healing.learning.LearningRecord;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class CandidateRanker {

        private static final double LOCATOR_QUALITY_WEIGHT = 1.5;

        private static final double LEARNING_BASE_SCORE = 1000.0;

private static final double LEARNING_MAX_SCORE = 1300.0;

private static final double LEARNING_RECENCY_WEIGHT = 100.0;

/*
 * Learning evidence loses influence over time.
 *
 * 0 days  -> 100%
 * 7 days  -> ~87%
 * 30 days -> ~50%
 * 60 days -> ~25%
 * 90 days -> ~12.5%
 */
private static final double LEARNING_HALF_LIFE_DAYS = 30.0;

    private final ParentScorer parentScorer =
        new ParentScorer();

        private final LearningEngine learningEngine =
        new LearningEngine();

    private final DynamicPatternAnalyzer dynamicAnalyzer =
            new DynamicPatternAnalyzer();

    private final LocatorQualityAnalyzer qualityAnalyzer =
            new LocatorQualityAnalyzer();

   public List<LocatorCandidate> rank(
        FailureContext context,
        List<LocatorCandidate> candidates,
        boolean collectionMode) {

        if (context == null
                || candidates == null
                || candidates.isEmpty()) {

            return candidates;
        }

   

        for (LocatorCandidate candidate : candidates) {

             System.out.println("\n===== CANDIDATE CONTEXT =====");

    System.out.println(
            "Locator      : "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue());

    System.out.println(
            "ParentTag    : "
                    + candidate.getParentTag());

    System.out.println(
            "ParentId     : "
                    + candidate.getParentId());

    System.out.println(
            "ParentClass  : "
                    + candidate.getParentClass());

    System.out.println(
            "NearestLabel : "
                    + candidate.getNearestLabel());

    System.out.println(
            "ElementText  : "
                    + candidate.getElementText());

    System.out.println(
            "ExpectedIntent : "
                    + context.getExpectedIntent());

    System.out.println(
            "CandidateIntent : "
                    + candidate.getIntent());

    System.out.println(
            "================================");

    /*
     * ==========================================
     * SEMANTIC COMPATIBILITY GATE
     * ==========================================
     *
     * Known intent mismatch means this candidate
     * cannot be used for healing.
     */
    if (!isSemanticallyCompatible(
            context,
            candidate)) {

        System.out.println(
                "REJECTED - Semantic incompatibility");

        System.out.println(
                "Expected Intent : "
                        + context.getExpectedIntent());

        System.out.println(
                "Candidate Intent : "
                        + candidate.getIntent());

        candidate.setFinalScore(
                Double.NEGATIVE_INFINITY);

        continue;
    }

           double score = candidate.getScore();

            score += calculateTagScore(
                    context,
                    candidate);

            score += calculateIntentScore(
                    context,
                    candidate);

            score += calculateIdentityScore(
                    context,
                    candidate);

                    score += calculateFailedLocatorSimilarityScore(
        context,
        candidate);

            score += calculateLocatorTypeScore(context, candidate);

            score += parentScorer.score(
        context,
        candidate);

                 score += calculateDomContextScore(
        context,
        candidate);

        double businessScore =
        calculateBusinessContextScore(
                context,
                candidate);

score += businessScore;

score += calculateExactElementTextScore(
        context,
        candidate);



            score += calculateUniquenessScore(
                    candidate);

            score += calculateSemanticSimilarityScore(
        context,
        candidate);

            score += calculateDynamicScore(
        context,
        candidate);

           score += calculateQualityScore(candidate);

          double learningScore =
        calculateLearningScore(
                context,
                candidate,
                collectionMode);
score += learningScore;

System.out.println(
        "LEARNING SCORE CONTRIBUTION"
        + " | candidate="
        + candidate.getLocatorType()
        + "="
        + candidate.getLocatorValue()
        + " | learningScore="
        + learningScore);

/*
 * ==========================================================
 * RANKING 2.0 — EVIDENCE AGREEMENT / CONFLICT
 * ==========================================================
 *
 * The existing Ranking 1.x score remains the baseline.
 * Ranking 2.0 does NOT replace or recalculate that score.
 *
 * It evaluates whether the existing evidence comes from
 * independent dimensions and whether those dimensions agree.
 *
 * IMPORTANT:
 * - No existing ranking signal is removed.
 * - No existing score weight is changed.
 * - CandidateFilter/CandidateValidator contracts remain intact.
 * - Adjustment is deliberately bounded.
 */
double ranking2Adjustment =
        calculateRanking2EvidenceAdjustment(
                context,
                candidate,
                collectionMode);

score += ranking2Adjustment;

System.out.println(
        "Ranking 2.0    : "
                + ranking2Adjustment);


            System.out.println("\n===== SCORE BREAKDOWN =====");

            System.out.println(
        "Learning       : "
                + learningScore);
System.out.println(candidate.getLocatorType() + "=" + candidate.getLocatorValue());

System.out.println(
        "Base Score      : "
                + candidate.getScore());
System.out.println("Tag            : " + calculateTagScore(context, candidate));
System.out.println("Intent         : " + calculateIntentScore(context, candidate));
System.out.println("Identity       : " + calculateIdentityScore(context, candidate));
System.out.println("Locator Type   : " + calculateLocatorTypeScore(context, candidate));
System.out.println(
        "Parent         : "
        + parentScorer.score(
                context,
                candidate));
System.out.println("DOM Context    : "
        + calculateDomContextScore(
                context,
                candidate));
                System.out.println("Business       : "
        + businessScore);

        System.out.println(
        "Exact Text     : "
                + calculateExactElementTextScore(
                        context,
                        candidate));
System.out.println("Unique         : " + calculateUniquenessScore(candidate));
System.out.println("Semantic       : " + calculateSemanticSimilarityScore(context, candidate));
System.out.println("Dynamic        : " + calculateDynamicScore(context, candidate));
System.out.println(
        "Quality        : "
        + calculateQualityScore(candidate));

candidate.setScore(score);
candidate.setFinalScore(score);
        }

        candidates.sort(
                Comparator.comparingDouble(
                        LocatorCandidate::getFinalScore)
                        .reversed());

        printRanking(candidates);

        return candidates;
    }
private double calculateTagScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (!hasText(context.getExpectedTag())
            || !hasText(candidate.getTagName())) {

        return 0;
    }

    /*
     * Matching tag is positive evidence.
     *
     * Mismatching tag is NOT a rejection.
     *
     * A validated element may legitimately heal
     * through a clickable parent, wrapper, or
     * semantically equivalent DOM element.
     */
    return context.getExpectedTag()
            .equalsIgnoreCase(candidate.getTagName())
            ? 150
            : 0;
}

private double calculateIntentScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context.getExpectedIntent() == null
            || candidate.getIntent() == null
            || context.getExpectedIntent() == ElementIntent.UNKNOWN
            || candidate.getIntent() == ElementIntent.UNKNOWN) {

        return 0;
    }

    return context.getExpectedIntent()
            == candidate.getIntent()
            ? 120
            : -250;
}

private boolean isSemanticallyCompatible(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null) {

        return true;
    }

    ElementIntent expected =
            context.getExpectedIntent();

    ElementIntent actual =
            candidate.getIntent();

    /*
     * No reliable expected intent.
     * Do not reject the candidate.
     */
    if (expected == null
            || expected == ElementIntent.UNKNOWN) {

        return true;
    }

    /*
     * Candidate intent is unknown.
     * Allow other scoring/validation logic
     * to evaluate the candidate.
     */
    if (actual == null
            || actual == ElementIntent.UNKNOWN) {

        return true;
    }

    /*
     * Both intents are known.
     *
     * A mismatch is not allowed.
     */
    return expected == actual;
}

private double calculateLocatorTypeScore(
        FailureContext context,
        LocatorCandidate candidate) {

    String locatorType = candidate.getLocatorType();

    if (!hasText(locatorType)) {
        return 0;
    }

    locatorType = locatorType.toLowerCase();

    String failedLocator =
            context.getFailedLocator() == null
                    ? ""
                    : context.getFailedLocator().toLowerCase();

    // Strong bonus if candidate uses the same locator strategy
    if ((failedLocator.contains("by.classname")
        || failedLocator.contains("@class"))
        && locatorType.equals("class")) {

    return 250;
}

if ((failedLocator.contains("by.id")
        || failedLocator.contains("@id"))
        && locatorType.equals("id")) {

    return 250;
}

if ((failedLocator.contains("by.name")
        || failedLocator.contains("@name"))
        && locatorType.equals("name")) {

    return 250;
}

    switch (locatorType) {

        case "class":
            return 60;

        case "id":
            return 55;

        case "name":
            return 50;

        case "data-test":
        case "data-testid":
        case "data-qa":
        case "data-cy":
            return 45;

        case "aria-label":
            return 35;

        case "placeholder":
            return 30;

        case "text":
            return 25;

        case "xpath":
            return 15;

        case "css":
        case "cssselector":
            return 10;

        default:
            return 0;
    }
}

private double calculateUniquenessScore(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return 0;
    }

    int occurrence =
            candidate.getOccurrenceCount();

    if (occurrence <= 0) {
        return -250;
    }

    if (occurrence == 1) {
        return 150;
    }

    if (occurrence == 2) {
        return -100;
    }

    if (occurrence <= 4) {
        return -200;
    }

    return -300;
}

private double calculateSemanticSimilarityScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null) {

        return 0;
    }

    /*
     * --------------------------------------------------
     * Build all available semantic sources.
     *
     * We do NOT depend only on variableName.
     * Failed locator is an important semantic signal.
     * --------------------------------------------------
     */

    String variable =
            normalize(context.getVariableName());

    String failedLocatorValue =
            normalize(
                    extractSemanticValue(
                            context.getFailedLocator()));

    String locatorHint =
            normalize(
                    context.getLocatorTextHint());

    String expectedText =
            normalize(
                    context.getExpectedText());

    /*
     * --------------------------------------------------
     * Candidate semantic values
     * --------------------------------------------------
     */

    String locatorValue =
            normalize(
                    extractSemanticValue(
                            candidate.getLocatorValue()));

    String id =
            normalize(
                    candidate.getId());

    String name =
            normalize(
                    candidate.getName());

    String label =
            normalize(
                    candidate.getNearestLabel());

    String elementText =
            normalize(
                    candidate.getElementText());

    /*
     * --------------------------------------------------
     * Calculate semantic evidence from all available
     * context sources.
     * --------------------------------------------------
     */

    double best = 0;

    /*
     * Variable name
     */
    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    variable,
                    locatorValue));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    variable,
                    id));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    variable,
                    name));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    variable,
                    label));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    variable,
                    elementText));

    /*
     * Failed locator semantic value
     *
     * Example:
     *
     * By.className: shopping
     *
     * becomes:
     *
     * shopping
     */
    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    failedLocatorValue,
                    locatorValue));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    failedLocatorValue,
                    id));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    failedLocatorValue,
                    name));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    failedLocatorValue,
                    label));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    failedLocatorValue,
                    elementText));

    /*
     * Locator text hint
     */
    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    locatorHint,
                    locatorValue));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    locatorHint,
                    id));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    locatorHint,
                    name));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    locatorHint,
                    label));

    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    locatorHint,
                    elementText));

    /*
     * Expected visible text
     */
    best = Math.max(
            best,
            SimilarityUtil.calculateOverallSimilarity(
                    expectedText,
                    elementText));

    /*
     * --------------------------------------------------
     * Convert similarity into score.
     * --------------------------------------------------
     */

    if (best >= 0.90) {
        return 500;
    }

    if (best >= 0.75) {
        return 350;
    }

    if (best >= 0.60) {
        return 250;
    }

    if (best >= 0.45) {
        return 150;
    }

    if (best >= 0.30) {
        return 75;
    }

    return 0;
}
private double calculateIdentityScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null) {

        return 0;
    }

    /*
     * --------------------------------------------------
     * 1. Variable semantic identity
     * --------------------------------------------------
     */

    String variable =
            normalize(
                    context.getVariableName());

    /*
     * Generic / missing variable name.
     * Fall back to failed locator semantic value.
     */
    if (!hasText(variable)
            || "direct locator".equals(variable)
            || "direct_locator".equals(variable)
            || "directlocator".equals(variable)) {

        variable =
                normalize(
                        extractSemanticValue(
                                context.getFailedLocator()));
    }

    /*
     * --------------------------------------------------
     * 2. Failed locator semantic value
     *
     * Example:
     *
     * By.className: shopping
     *
     * -> shopping
     * --------------------------------------------------
     */

    String failedLocatorValue =
            normalize(
                    extractSemanticValue(
                            context.getFailedLocator()));

    /*
     * --------------------------------------------------
     * 3. Candidate values
     * --------------------------------------------------
     */

    String locatorValue =
            normalize(
                    extractSemanticValue(
                            candidate.getLocatorValue()));

    String label =
            normalize(
                    candidate.getNearestLabel());

    String id =
            normalize(
                    candidate.getId());

    String name =
            normalize(
                    candidate.getName());

    String elementText =
            normalize(
                    candidate.getElementText());

    /*
     * --------------------------------------------------
     * 4. Variable coverage
     * --------------------------------------------------
     */

    double variableLocatorCoverage =
            tokenCoverage(
                    variable,
                    locatorValue);

    double variableLabelCoverage =
            tokenCoverage(
                    variable,
                    label);

    double variableIdCoverage =
            tokenCoverage(
                    variable,
                    id);

    double variableNameCoverage =
            tokenCoverage(
                    variable,
                    name);

    /*
     * --------------------------------------------------
     * 5. Failed locator coverage
     *
     * This is the important fix.
     *
     * Example:
     *
     * failed = shopping
     *
     * candidate =
     * shopping_cart_badge
     *
     * coverage = 1.0
     * --------------------------------------------------
     */

    double failedLocatorCoverage =
            tokenCoverage(
                    failedLocatorValue,
                    locatorValue);

    double failedIdCoverage =
            tokenCoverage(
                    failedLocatorValue,
                    id);

    double failedNameCoverage =
            tokenCoverage(
                    failedLocatorValue,
                    name);

    double failedLabelCoverage =
            tokenCoverage(
                    failedLocatorValue,
                    label);

    /*
     * --------------------------------------------------
     * 6. Visible text coverage
     * --------------------------------------------------
     */

    double textCoverage =
            tokenCoverage(
                    context.getExpectedText(),
                    elementText);

    /*
     * --------------------------------------------------
     * 7. Failed locator is strong semantic evidence.
     *
     * We deliberately give it priority over generic
     * structural information.
     * --------------------------------------------------
     */

    double failedCoverage =
            Math.max(
                    Math.max(
                            failedLocatorCoverage,
                            failedIdCoverage),
                    Math.max(
                            failedNameCoverage,
                            failedLabelCoverage));

    if (failedCoverage >= 1.0) {

        System.out.println(
                "IDENTITY MATCH | Failed locator semantic match = 100%");

        return 700;
    }

    if (failedCoverage >= 0.75) {

        System.out.println(
                "IDENTITY MATCH | Failed locator semantic match >= 75%");

        return 500;
    }

    if (failedCoverage >= 0.50) {

        System.out.println(
                "IDENTITY MATCH | Failed locator semantic match >= 50%");

        return 300;
    }

    /*
     * --------------------------------------------------
     * 8. Exact business label
     * --------------------------------------------------
     */

    if (variableLabelCoverage >= 1.0) {
        return 600;
    }

    if (variableLabelCoverage >= 0.75) {
        return 450;
    }

    if (variableLabelCoverage >= 0.50) {
        return 250;
    }

    /*
     * --------------------------------------------------
     * 9. Strong locator identity
     * --------------------------------------------------
     */

    String locatorType =
            candidate.getLocatorType() == null
                    ? ""
                    : candidate.getLocatorType()
                            .toLowerCase();

    if (isStrongIdentityLocator(locatorType)) {

        if (variableLocatorCoverage >= 1.0
                || variableIdCoverage >= 1.0
                || variableNameCoverage >= 1.0) {

            return 500;
        }

        if (variableLocatorCoverage >= 0.75
                || variableIdCoverage >= 0.75
                || variableNameCoverage >= 0.75) {

            return 350;
        }

        if (variableLocatorCoverage >= 0.50
                || variableIdCoverage >= 0.50
                || variableNameCoverage >= 0.50) {

            return 200;
        }
    }

    /*
     * --------------------------------------------------
     * 10. Text / XPath identity
     * --------------------------------------------------
     */

    if ("text".equals(locatorType)
            || "xpath".equals(locatorType)) {

        if (variableLocatorCoverage >= 1.0) {
            return 400;
        }

        if (variableLocatorCoverage >= 0.75) {
            return 300;
        }

        if (variableLocatorCoverage >= 0.50) {
            return 150;
        }
    }

    /*
     * --------------------------------------------------
     * 11. CSS / class identity
     * --------------------------------------------------
     */

    if ("class".equals(locatorType)
            || "css".equals(locatorType)
            || "cssselector".equals(locatorType)) {

        if (variableLocatorCoverage >= 1.0) {
            return 450;
        }

        if (variableLocatorCoverage >= 0.75) {
            return 325;
        }

        if (variableLocatorCoverage >= 0.50) {
            return 200;
        }
    }

    /*
     * --------------------------------------------------
     * 12. Expected visible text
     * --------------------------------------------------
     */

    if (textCoverage >= 1.0) {
        return 300;
    }

    if (textCoverage >= 0.50) {
        return 150;
    }

    return 0;
}
private double calculateDynamicScore(
        FailureContext context,
        LocatorCandidate candidate) {

    return dynamicAnalyzer.calculateDynamicScore(
            context,
            candidate);
}

private double calculateQualityScore(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return 0;
    }

    return qualityAnalyzer
            .calculateReliabilityScore(candidate)
            * LOCATOR_QUALITY_WEIGHT;
}

/*
 * ==========================================================
 * RANKING 2.0
 * ==========================================================
 *
 * Ranking 1.x already contains the detailed scoring logic.
 * These methods deliberately sit inside CandidateRanker so
 * the frozen architecture is preserved.
 *
 * Ranking 2.0 does not treat every existing score as an
 * independent reason. It evaluates evidence families:
 *
 *   1. Identity / semantic family
 *   2. Structural / business family
 *   3. Locator quality family
 *   4. Historical learning family
 *
 * The adjustment is bounded so the existing score remains
 * authoritative and current healing behaviour is protected.
 */

private double calculateRanking2EvidenceAdjustment(
        FailureContext context,
        LocatorCandidate candidate,
        boolean collectionMode) {

    if (context == null || candidate == null) {
        return 0.0;
    }

    /*
     * Existing evidence scores are intentionally reused.
     * We do NOT create a second scoring system.
     */
    double identity =
            calculateIdentityScore(context, candidate);

    double semantic =
            calculateSemanticSimilarityScore(context, candidate);

    double failedSimilarity =
            calculateFailedLocatorSimilarityScore(
                    context,
                    candidate);

    double exactText =
            calculateExactElementTextScore(
                    context,
                    candidate);

    double dom =
            calculateDomContextScore(
                    context,
                    candidate);

    double business =
            calculateBusinessContextScore(
                    context,
                    candidate);

    double uniqueness =
            calculateUniquenessScore(candidate);

    double dynamic =
            calculateDynamicScore(
                    context,
                    candidate);

    double quality =
            calculateQualityScore(candidate);

   double learning =
        calculateLearningScore(
                context,
                candidate,
                collectionMode);

    /*
     * ------------------------------------------------------
     * Evidence families
     * ------------------------------------------------------
     *
     * Identity, semantic similarity, failed-locator
     * similarity and exact text are partially correlated.
     *
     * Therefore they are NOT counted as four independent
     * evidence sources.
     */
    boolean strongIdentity =
            identity >= 300;

    boolean strongSemantic =
            semantic >= 350;

    boolean strongText =
            exactText >= 450;

    boolean supportingIdentity =
            identity >= 150
                    || failedSimilarity >= 75;

    /*
     * Structural evidence is a separate family, although
     * DOM and business context themselves partially overlap.
     */
    boolean strongStructure =
            dom >= 60
                    || business >= 300;

    boolean supportingStructure =
            dom >= 25
                    || business >= 150;

    /*
     * Locator quality is independent from element identity.
     */
    boolean strongQuality =
            uniqueness >= 150
                    && quality >= 100;

    boolean stableQuality =
            uniqueness >= 150
                    || quality >= 100
                    || dynamic > 0;

    /*
     * Historical learning is independent because it comes
     * from a previous successful execution rather than only
     * from the current DOM.
     */
   boolean strongLearning =
        learning >= 200;

boolean supportingLearning =
        learning >= 50;

    double adjustment = 0.0;

    /*
     * ------------------------------------------------------
     * AGREEMENT
     * ------------------------------------------------------
     */

    /*
     * Strong identity + structural confirmation.
     */
    if (strongIdentity && strongStructure) {
        adjustment += 20.0;
    }

    /*
     * Strong semantic/text evidence + structural confirmation.
     */
    if ((strongSemantic || strongText)
            && strongStructure) {
        adjustment += 15.0;
    }

    /*
     * Strong current identity + independent locator quality.
     */
    if (strongIdentity && strongQuality) {
        adjustment += 20.0;
    }

    /*
     * Historical success + current evidence is especially
     * valuable because the evidence sources are independent.
     */
    if (strongLearning
            && (strongIdentity
                || strongSemantic
                || strongText)) {
        adjustment += 25.0;
    } else if (supportingLearning
            && supportingIdentity) {
        adjustment += 10.0;
    }

    /*
     * ------------------------------------------------------
     * CORRELATED EVIDENCE
     * ------------------------------------------------------
     *
     * We deliberately DO NOT add another bonus merely because
     * identity + semantic + failed-locator similarity are all
     * high. The existing Ranking 1.x score already contains
     * those contributions.
     *
     * Ranking 2.0 only rewards independent agreement.
     */

    /*
     * ------------------------------------------------------
     * CONFLICT / WEAKNESS
     * ------------------------------------------------------
     */

    /*
     * Strong semantic identity but poor uniqueness means the
     * candidate may represent the right concept but not a
     * safely unique locator.
     */
    if ((strongIdentity || strongSemantic || strongText)
            && uniqueness <= -100) {
        adjustment -= 20.0;
    }

    /*
     * Strong identity with weak structural support is not a
     * rejection, but confidence should not receive an
     * additional Ranking 2.0 boost.
     */
    if (strongIdentity
            && !supportingStructure
            && !stableQuality) {
        adjustment -= 10.0;
    }

    /*
     * Strong semantic evidence with a weak locator-quality
     * profile is a softer warning, not a hard rejection.
     */
    if ((strongSemantic || strongText)
            && quality < 0
            && uniqueness < 1) {
        adjustment -= 10.0;
    }

    /*
     * ------------------------------------------------------
     * BOUNDED ADJUSTMENT
     * ------------------------------------------------------
     *
     * Ranking 2.0 must never overpower the existing ranking
     * system. This keeps Login healing and other existing
     * scenarios protected while we tune the new intelligence.
     */
    return Math.max(
            -40.0,
            Math.min(
                    60.0,
                    adjustment));
}

private void printRanking(
        List<LocatorCandidate> candidates) {

    System.out.println(
            "\n========== Candidate Ranking ==========");

    int limit =
            Math.min(
                    candidates.size(),
                    10);

    for (int i = 0; i < limit; i++) {

        LocatorCandidate candidate =
                candidates.get(i);

        System.out.println(
                (i + 1)
                        + ". "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());

        System.out.println(
                "   Score      : "
                        + candidate.getFinalScore());

        System.out.println(
                "   Intent     : "
                        + candidate.getIntent());

        System.out.println(
                "   Tag        : "
                        + candidate.getTagName());

        System.out.println(
                "   Unique     : "
                        + candidate.isUniqueLocator());

        System.out.println(
                "   Occurrence : "
                        + candidate.getOccurrenceCount());

        System.out.println();
    }
}
private boolean hasText(
        String value) {

    return value != null
            && !value.isBlank();
}

private String normalize(
        String value) {

    if (value == null) {
        return "";
    }

    return value
            .toLowerCase()
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
}

private boolean isGenericToken(
        String token) {

    if (!hasText(token)) {
        return true;
    }

    switch (token.toLowerCase()) {

        case "button":
        case "btn":
        case "input":
        case "field":
        case "text":
        case "textbox":
        case "label":
        case "message":
        case "link":
        case "icon":
        case "image":
        case "img":
        case "element":
        case "locator":
        case "page":
        case "menu":
        case "container":
        case "wrapper":
        case "content":
        case "header":
        case "footer":
        case "section":
        case "div":
        case "span":
            return true;

        default:
            return false;
    }
}
private double tokenCoverage(
        String source,
        String target) {

    if (!hasText(source)
            || !hasText(target)) {

        return 0;
    }

    String normalizedSource =
            normalize(source);

    String normalizedTarget =
            normalize(target);

    if (!hasText(normalizedSource)
            || !hasText(normalizedTarget)) {

        return 0;
    }

    String[] sourceTokens =
            normalizedSource.split("\\s+");

    String[] targetTokens =
            normalizedTarget.split("\\s+");

    int meaningful = 0;
    int matched = 0;

    for (String sourceToken :
            sourceTokens) {

        if (!hasText(sourceToken)
                || sourceToken.length() < 2) {

            continue;
        }

        if (isGenericToken(sourceToken)) {
            continue;
        }

        meaningful++;

        for (String targetToken :
                targetTokens) {

            if (sourceToken.equals(targetToken)) {

                matched++;
                break;
            }
        }
    }

    if (meaningful == 0) {
        return 0;
    }

    return (double) matched / meaningful;
}

private boolean isStrongIdentityLocator(
        String locatorType) {

    if (!hasText(locatorType)) {
        return false;
    }

    switch (locatorType.toLowerCase()) {

        case "id":
        case "name":
        case "placeholder":
        case "aria-label":
        case "data-test":
        case "data-testid":
        case "data-qa":
        case "data-cy":
        case "href":
            return true;

        default:
            return false;
    }
}
private String extractSemanticValue(
        String value) {

    if (!hasText(value)) {
        return "";
    }

    String cleaned =
            value.trim();

    cleaned =
            cleaned.replaceFirst(
                    "(?i)^By\\.[a-zA-Z]+\\s*:\\s*",
                    "");

    java.util.regex.Matcher quoted =
            java.util.regex.Pattern
                    .compile("['\"]([^'\"]+)['\"]")
                    .matcher(cleaned);

    String best = "";

    while (quoted.find()) {

        String extracted =
                quoted.group(1);

        if (extracted.length() > best.length()) {
            best = extracted;
        }
    }

    if (hasText(best)) {
        return best;
    }

    java.util.regex.Matcher css =
            java.util.regex.Pattern
                    .compile("\\[[^=]+=['\"]?([^'\"\\]]+)['\"]?\\]")
                    .matcher(cleaned);

    if (css.find()) {
        return css.group(1);
    }

    java.util.regex.Matcher xpath =
        java.util.regex.Pattern
                .compile("@[a-zA-Z0-9_-]+=['\"]([^'\"]+)['\"]")
                .matcher(cleaned);

if (xpath.find()) {
    return xpath.group(1);
}

    return cleaned;
}

private double calculateSimilarity(
        String first,
        String second) {

    if (!hasText(first)
            || !hasText(second)) {

        return 0;
    }

if (first.equals(second)) {
    return 1.0;
}


if (first.contains(second)
        || second.contains(first)) {

    return 0.90;
}

    int distance =
            levenshteinDistance(
                    first,
                    second);

    int max =
            Math.max(
                    first.length(),
                    second.length());

    if (max == 0) {
        return 1.0;
    }

    return 1.0
            - ((double) distance / max);
}
private int levenshteinDistance(
        String first,
        String second) {

    int[][] dp =
            new int[first.length() + 1]
                   [second.length() + 1];

    for (int i = 0; i <= first.length(); i++) {
        dp[i][0] = i;
    }

    for (int j = 0; j <= second.length(); j++) {
        dp[0][j] = j;
    }

    for (int i = 1; i <= first.length(); i++) {

        for (int j = 1; j <= second.length(); j++) {

            int cost =
                    first.charAt(i - 1)
                            == second.charAt(j - 1)
                            ? 0
                            : 1;

            dp[i][j] =
                    Math.min(
                            Math.min(
                                    dp[i - 1][j] + 1,
                                    dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost);
        }
    }

    return dp[first.length()][second.length()];
}

private double calculateDomContextScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null) {

        return 0;
    }

    double score = 0;

    /*
     * Parent Tag
     */
if (hasText(context.getParentTag())
        && hasText(candidate.getParentTag())) {

    double coverage =
            tokenCoverage(
                    normalize(context.getParentTag()),
                    normalize(candidate.getParentTag()));

    if (coverage >= 1.0) {

        score += 20;

    } else if (coverage >= 0.75) {

        score += 15;

    } else if (coverage >= 0.50) {

        score += 10;
    }
}

    /*
     * Parent Id
     */
if (hasText(context.getParentId())
        && hasText(candidate.getParentId())) {

    double coverage =
            tokenCoverage(
                    normalize(context.getParentId()),
                    normalize(candidate.getParentId()));

    if (coverage >= 1.0) {

        score += 40;

    } else if (coverage >= 0.75) {

        score += 30;

    } else if (coverage >= 0.50) {

        score += 20;
    }
}

    /*
     * Parent Class
     */
if (hasText(context.getParentClass())
        && hasText(candidate.getParentClass())) {

    double coverage =
            tokenCoverage(
                    normalize(context.getParentClass()),
                    normalize(candidate.getParentClass()));

    if (coverage >= 1.0) {

        score += 30;

    } else if (coverage >= 0.75) {

        score += 20;

    } else if (coverage >= 0.50) {

        score += 10;
    }
}

    /*
     * Nearest Label
     */
    if (hasText(context.getNearestLabel())
            && hasText(candidate.getNearestLabel())) {

        double coverage =
                tokenCoverage(
                        normalize(context.getNearestLabel()),
                        normalize(candidate.getNearestLabel()));

        if (coverage >= 1.0) {

            score += 60;

        } else if (coverage >= 0.75) {

            score += 45;

        } else if (coverage >= 0.50) {

            score += 30;
        }
    }

    /*
     * Visible Element Text
     */
    if (hasText(context.getExpectedText())
            && hasText(candidate.getElementText())) {

        double coverage =
                tokenCoverage(
                        normalize(context.getExpectedText()),
                        normalize(candidate.getElementText()));

        if (coverage >= 1.0) {

            score += 50;

        } else if (coverage >= 0.75) {

            score += 35;

        } else if (coverage >= 0.50) {

            score += 20;
        }
    }

    return score;
}
private double calculateExactElementTextScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null
            || !hasText(candidate.getElementText())) {

        return 0;
    }

    String actual =
            normalizeTextForComparison(
                    candidate.getElementText());

    if (!hasText(actual)) {
        return 0;
    }

    /*
     * =====================================================
     * EXPECTED TEXT
     * =====================================================
     *
     * First use explicitly resolved expected text.
     *
     * If unavailable, recover semantic text alternatives
     * directly from the failed locator.
     */
    String expected =
            normalize(context.getExpectedText());

    if (hasText(expected)) {

        double score =
                calculateTextMatchScore(
                        expected,
                        actual);

        if (score > 0) {
            return score;
        }
    }

    /*
     * =====================================================
     * FAILED LOCATOR TEXT
     * =====================================================
     *
     * A locator may contain multiple alternatives:
     *
     * contains(..., 'Record Found')
     * OR
     * contains(..., 'Records Found')
     *
     * Each extracted value must be evaluated
     * independently.
     */
    List<String> locatorTexts =
            extractTextHintsFromFailedLocator(
                    context.getFailedLocator());

    double bestScore = 0;

    for (String locatorText :
            locatorTexts) {

        if (!hasText(locatorText)) {
            continue;
        }

        double score =
                calculateTextMatchScore(
                        locatorText,
                        actual);

        bestScore =
                Math.max(
                        bestScore,
                        score);
    }

    return bestScore;
}
private double calculateBusinessContextScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null || candidate == null) {
        return 0;
    }

    double score = 0;

    score += compare(
            context.getNearestLabel(),
            candidate.getNearestLabel(),
            500);

    score += compare(
            context.getExpectedText(),
            candidate.getElementText(),
            300);

    score += compare(
            context.getParentId(),
            candidate.getParentId(),
            250);

    score += compare(
            context.getParentClass(),
            candidate.getParentClass(),
            200);

    /*
     * Cross comparison
     * Label often appears inside parent class/id.
     */

    score += compare(
            context.getNearestLabel(),
            candidate.getParentId(),
            250);

    score += compare(
            context.getNearestLabel(),
            candidate.getParentClass(),
            200);

    score += compare(
            context.getNearestLabel(),
            candidate.getId(),
            200);

    score += compare(
            context.getNearestLabel(),
            candidate.getName(),
            150);

    return score;
}
private double compare(
        String expected,
        String actual,
        double weight) {

    if (!hasText(expected)
            || !hasText(actual)) {

        return 0;
    }

    expected = normalize(expected);
    actual = normalize(actual);

    if (expected.equals(actual)) {
        return weight;
    }

    if (actual.contains(expected)
            || expected.contains(actual)) {

        return weight * 0.75;
    }

    String[] expectedTokens =
            expected.split("\\s+");

    String[] actualTokens =
            actual.split("\\s+");

    int meaningfulTokens = 0;
    int matchedTokens = 0;

    for (String token : expectedTokens) {

        if (isGenericToken(token)
                || token.length() < 2) {
            continue;
        }

        meaningfulTokens++;

        for (String actualToken : actualTokens) {

            if (actualToken.equals(token)) {
                matchedTokens++;
                break;
            }
        }
    }

    if (meaningfulTokens == 0
            || matchedTokens == 0) {

        return 0;
    }

    return weight
            * matchedTokens
            / meaningfulTokens;
}


private double calculateFailedLocatorSimilarityScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (context == null
            || candidate == null
            || !hasText(context.getFailedLocator())
            || !hasText(candidate.getLocatorValue())) {

        return 0;
    }

    String failedValue =
            normalize(
                    extractSemanticValue(
                            context.getFailedLocator()));

    String candidateValue =
            normalize(
                    extractSemanticValue(
                            candidate.getLocatorValue()));

    if (!hasText(failedValue)
            || !hasText(candidateValue)) {

        return 0;
    }

    double similarity =
            calculateSimilarity(
                    failedValue,
                    candidateValue);

    System.out.println(
            "Failed Locator Similarity : "
                    + similarity);

    /*
     * This score is only a supporting signal.
     *
     * Actual semantic identity is handled by
     * calculateIdentityScore().
     */

    if (similarity >= 0.95) {
        return 150;
    }

    if (similarity >= 0.80) {
        return 100;
    }

    if (similarity >= 0.65) {
        return 75;
    }

    if (similarity >= 0.50) {
        return 40;
    }

    return 0;
}
public double calculateLearningScore(
        FailureContext context,
        LocatorCandidate candidate,
        boolean collectionMode) {

    if (context == null || candidate == null) {
        return 0.0;
    }

    String pageObjectClass =
            extractPageObjectClass(
                    context.getPageObjectPath());

    String variableName =
            safe(context.getVariableName());

    String expectedIntent =
            context.getExpectedIntent() == null
                    ? "UNKNOWN"
                    : context.getExpectedIntent().name();

    String action =
            context.getFailedAction() == null
                    ? "UNKNOWN"
                    : context.getFailedAction().name();

    String failedLocator =
            safe(context.getFailedLocator());

    LearningKey key =
            new LearningKey(
                    pageObjectClass,
                    variableName,
                    expectedIntent,
                    action,
                    failedLocator);

    List<LearningRecord> history =
            learningEngine
                    .getRepository()
                    .find(key);

    if (history == null || history.isEmpty()) {
        return 0.0;
    }

    /*
     * ==================================================
     * CURRENT CANDIDATE IDENTITY
     * ==================================================
     */

    String candidateType =
            safe(candidate.getLocatorType());

    String candidateValue =
            safe(candidate.getLocatorValue());

    /*
     * ==================================================
     * HISTORICAL AGGREGATION
     * ==================================================
     *
     * We intentionally aggregate historical evidence
     * instead of using Math.max().
     *
     * This allows repeated successful healing to
     * strengthen future ranking.
     */

   int matchingAttempts = 0;
int matchingSuccesses = 0;

double totalOutcomeConfidence = 0.0;





double successfulRecencyWeight = 0.0;

    for (LearningRecord record : history) {

        if (record == null) {
            continue;
        }

        /*
         * COLLECTION ranking must only learn from
         * previous COLLECTION healing.
         *
         * Normal ranking can use all learning sources.
         */
        if (collectionMode) {

            if (!"COLLECTION".equalsIgnoreCase(
                    safe(record.getHealingSource()))) {

                continue;
            }
        }

       String learnedType =
        safe(record.getSelectedLocatorType());

String learnedValue =
        safe(record.getSelectedLocatorValue());

/*
 * ==================================================
 * HISTORICAL LOCATOR MATCH
 * ==================================================
 *
 * Do not require only raw locator equality.
 *
 * Reuse the existing learned-locator similarity
 * logic so historical learning can recognize:
 *
 * 1. Exact locator match
 * 2. Same semantic text
 * 3. Same XPath text
 * 4. Same stable attribute
 */
double learnedLocatorSimilarity =
        calculateLearnedLocatorSimilarity(
                candidate,
                record);

System.out.println(
        "LEARNING MATCH CHECK"
                + " | candidate="
                + candidateType
                + "="
                + candidateValue
                + " | learned="
                + learnedType
                + "="
                + learnedValue
                + " | similarity="
                + learnedLocatorSimilarity
                + " | success="
                + record.isOutcomeSuccess()
                + " | healingAllowed="
                + record.isHealingAllowed()
                + " | outcomeConfidence="
                + record.getOutcomeConfidence()
                + " | source="
                + record.getHealingSource());

if (learnedLocatorSimilarity <= 0.0) {
    continue;
}

        /*
         * Count every matching historical attempt.
         *
         * This allows failures to reduce reliability.
         */
        matchingAttempts++;

        double recencyWeight =
        calculateLearningRecencyWeight(
                record);



        /*
         * Only successful + allowed healing is allowed
         * to contribute positive learning.
         */
        if (!record.isOutcomeSuccess()) {
    continue;
}

if (!record.isHealingAllowed()) {
    continue;
}

successfulRecencyWeight +=
        recencyWeight;

double outcomeConfidence =
        normalizeOutcomeConfidence(
                record.getOutcomeConfidence());

/*
 * MEDIUM and HIGH historical healing are valid
 * learning evidence as long as the healing was
 * successful and allowed.
 *
 * LOW / REJECT should never reach this point because
 * healingAllowed is checked above.
 */
if (outcomeConfidence <= 0) {
    continue;
}

        matchingSuccesses++;

        totalOutcomeConfidence +=
                outcomeConfidence;


    }

    /*
     * No successful historical evidence
     * for this exact locator.
     */
    if (matchingSuccesses == 0) {
        return 0.0;
    }

    /*
     * ==================================================
     * HISTORICAL RELIABILITY
     * ==================================================
     *
     * Example:
     *
     * 1 success / 1 attempt = 1.00
     * 2 success / 2 attempts = 1.00
     * 2 success / 3 attempts = 0.67
     */

    double reliability =
            matchingSuccesses
                    / (double) matchingAttempts;

    /*
     * ==================================================
     * AVERAGE OUTCOME CONFIDENCE
     * ==================================================
     */

    double averageOutcomeConfidence =
            totalOutcomeConfidence
                    / matchingSuccesses;

                    double recencyConfidence =
        successfulRecencyWeight
                / matchingSuccesses;

    /*
     * ==================================================
     * REPEATED SUCCESS BONUS
     * ==================================================
     *
     * First successful learning:
     *      +0
     *
     * Second:
     *      +50
     *
     * Third:
     *      +100
     *
     * ...
     *
     * Maximum repeated-success bonus:
     *      +250
     *
     * This prevents unlimited score inflation.
     */

    int repeatedSuccesses =
            Math.max(
                    0,
                    matchingSuccesses - 1);



    /*
     * ==================================================
     * RELIABILITY ADJUSTMENT
     * ==================================================
     *
     * Historical learning becomes weaker when the
     * same locator has failed in previous executions.
     */

   /*
 * ==================================================
 * RELIABILITY + RECENCY
 * ==================================================
 *
 * Historical learning is useful only when:
 *
 * 1. The locator has succeeded historically.
 * 2. The historical success was allowed.
 * 3. The historical result is reasonably recent.
 *
 * Reliability reduces learning when previous
 * attempts have failed.
 */


double recencyBonus =
        LEARNING_RECENCY_WEIGHT
                * recencyConfidence
                * reliability;

/*
 * ==================================================
 * CONFIDENCE WEIGHTING
 * ==================================================
 *
 * Historical confidence is intentionally preserved.
 *
 * HIGH confidence:
 *      1.00
 *
 * MEDIUM confidence:
 *      0.60
 *
 * LOW confidence:
 *      0.00
 *
 * This means Medium healing can be learned,
 * but High healing remains stronger evidence.
 */
double confidenceWeight;

if (averageOutcomeConfidence >= 80.0) {

    confidenceWeight = 1.00;

} else if (averageOutcomeConfidence >= 50.0) {

    confidenceWeight = 0.60;

} else {

    confidenceWeight = 0.00;
}

/*
 * ==================================================
 * CONFIDENCE + RELIABILITY + RECENCY
 * ==================================================
 */
double effectiveHistoricalConfidence =
        confidenceWeight
                * reliability
                * recencyConfidence;

/*
 * ==================================================
 * BASE LEARNING CONTRIBUTION
 * ==================================================
 *
 * Maximum base contribution:
 *
 * HIGH:
 *      100
 *
 * MEDIUM:
 *      60
 *
 * before reliability/recency adjustment.
 */
double baseLearningScore =
        100.0
                * effectiveHistoricalConfidence;

/*
 * ==================================================
 * REPEATED SUCCESS
 * ==================================================
 *
 * Repeated successful healing strengthens
 * historical confidence.
 *
 * Maximum repeated-success contribution:
 *      +150
 */
double repeatedLearningBonus =
        Math.min(
                repeatedSuccesses
                        * 50.0
                        * reliability
                        * confidenceWeight
                        * recencyConfidence,
                150.0);

/*
 * ==================================================
 * FINAL LEARNING SCORE
 * ==================================================
 */
double exactLearningScore =
        baseLearningScore
                + repeatedLearningBonus
                + recencyBonus;

/*
 * Learning must remain bounded.
 *
 * Learning is supporting evidence.
 * It can reach a maximum of 300 points.
 */
exactLearningScore =
        Math.max(
                0.0,
                Math.min(
                        exactLearningScore,
                        300.0));
   System.out.println(
        "LEARNING AGGREGATED MATCH | "
                + candidateType
                + "="
                + candidateValue
                + " | attempts="
                + matchingAttempts
                + " | successes="
                + matchingSuccesses
                + " | reliability="
                + reliability
                + " | avgConfidence="
                + averageOutcomeConfidence
                + " | recencyConfidence="
                + recencyConfidence
                + " | recencyBonus="
                + recencyBonus
                + " | learningScore="
                + exactLearningScore
                + " | source="
                + (collectionMode
                        ? "COLLECTION"
                        : "ALL"));

    return exactLearningScore;
}

private double calculateLearningRecencyWeight(
        LearningRecord record) {

    if (record == null
            || record.getTimestamp() == null) {

        return 0.0;
    }

    LocalDateTime timestamp =
            record.getTimestamp();

    long ageDays =
            Math.max(
                    0,
                    Duration.between(
                            timestamp,
                            LocalDateTime.now())
                            .toDays());

    /*
     * Exponential decay.
     *
     * At the configured half-life,
     * the learning evidence has 50%
     * of its original strength.
     */
    double weight =
            Math.pow(
                    0.5,
                    ageDays
                            / LEARNING_HALF_LIFE_DAYS);

    return Math.max(
            0.0,
            Math.min(
                    1.0,
                    weight));
}
private String extractPageObjectClass(
        String pageObjectPath) {

    if (pageObjectPath == null
            || pageObjectPath.isBlank()) {

        return "UNKNOWN";
    }

    String normalized =
            pageObjectPath
                    .replace("\\", "/");

    int slash =
            normalized.lastIndexOf('/');

    String fileName =
            slash >= 0
                    ? normalized.substring(
                            slash + 1)
                    : normalized;

    if (fileName.endsWith(".java")) {

        fileName =
                fileName.substring(
                        0,
                        fileName.length() - 5);
    }

    return normalize(fileName);
}
private double calculateLearnedLocatorSimilarity(
        LocatorCandidate candidate,
        LearningRecord record) {

    if (candidate == null || record == null) {
        return 0.0;
    }

    String candidateType =
            canonicalLocatorType(
                    safe(candidate.getLocatorType()));

    String candidateValue =
            safe(candidate.getLocatorValue());

    String learnedType =
            canonicalLocatorType(
                    safe(record.getSelectedLocatorType()));

    String learnedValue =
            safe(record.getSelectedLocatorValue());

            System.out.println(
        "LEARNING RAW VALUES"
        + " | candidateType=[" + candidateType + "]"
        + " | candidateValue=[" + candidateValue + "]"
        + " | learnedType=[" + learnedType + "]"
        + " | learnedValue=[" + learnedValue + "]"
        + " | candidateTypeLength=" + candidateType.length()
        + " | candidateValueLength=" + candidateValue.length()
        + " | learnedTypeLength=" + learnedType.length()
        + " | learnedValueLength=" + learnedValue.length());

    if ("UNKNOWN".equals(candidateType)
            || "UNKNOWN".equals(candidateValue)
            || "UNKNOWN".equals(learnedType)
            || "UNKNOWN".equals(learnedValue)) {

        return 0.0;
    }

    /*
     * EXACT HISTORICAL LOCATOR
     */
   /*
 * EXACT HISTORICAL LOCATOR
 */
boolean typeMatch =
        candidateType.equalsIgnoreCase(learnedType);

boolean valueMatch =
        candidateValue.equalsIgnoreCase(learnedValue);

System.out.println(
        "LEARNING EXACT CHECK"
        + " | typeMatch=" + typeMatch
        + " | valueMatch=" + valueMatch);

if (typeMatch && valueMatch) {

    System.out.println(
            "LEARNING LOCATOR EXACT MATCH | "
                    + candidateType
                    + "="
                    + candidateValue);

    return 200.0;
}

    /*
     * SEMANTIC TEXT MATCH
     */
    String learnedSemanticText =
            extractSemanticText(
                    learnedType,
                    learnedValue);

    String candidateSemanticText =
            extractSemanticText(
                    candidateType,
                    candidateValue);

    if (!isBlank(learnedSemanticText)
            && !isBlank(candidateSemanticText)
            && learnedSemanticText.equalsIgnoreCase(
                    candidateSemanticText)) {

        return 150.0;
    }

    /*
     * XPATH TEXT MATCH
     */
    if (isXPath(learnedType)
            && isXPath(candidateType)) {

        String learnedText =
                extractXPathText(learnedValue);

        String candidateText =
                extractXPathText(candidateValue);

        if (!isBlank(learnedText)
                && !isBlank(candidateText)
                && learnedText.equalsIgnoreCase(candidateText)) {

            return 150.0;
        }
    }

    /*
     * STABLE ATTRIBUTE MATCH
     */
    if (isStableLocatorType(learnedType)
            && isStableLocatorType(candidateType)
            && learnedValue.equalsIgnoreCase(candidateValue)) {

        return 100.0;
    }

    return 0.0;
}
private String extractSemanticText(
        String locatorType,
        String locatorValue) {

    if (isBlank(locatorValue)) {
        return null;
    }

    /*
     * Direct text locator.
     */
    if ("text".equalsIgnoreCase(locatorType)
            || "linktext".equalsIgnoreCase(locatorType)
            || "partiallinktext".equalsIgnoreCase(locatorType)) {

        return normalizeSemanticText(
                locatorValue);
    }

    /*
     * XPath containing:
     *
     * normalize-space()='Search'
     *
     * or
     *
     * text()='Search'
     */
    if (isXPath(locatorType)) {

        return extractXPathText(
                locatorValue);
    }

    return null;
}
private String extractXPathText(
        String xpath) {

    if (isBlank(xpath)) {
        return null;
    }

    /*
     * normalize-space()='Search'
     */
    java.util.regex.Pattern normalizePattern =
            java.util.regex.Pattern.compile(
                    "normalize-space\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    java.util.regex.Matcher normalizeMatcher =
            normalizePattern.matcher(xpath);

    if (normalizeMatcher.find()) {

        return normalizeSemanticText(
                normalizeMatcher.group(1));
    }

    /*
     * text()='Search'
     */
    java.util.regex.Pattern textPattern =
            java.util.regex.Pattern.compile(
                    "text\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    java.util.regex.Matcher textMatcher =
            textPattern.matcher(xpath);

    if (textMatcher.find()) {

        return normalizeSemanticText(
                textMatcher.group(1));
    }

    return null;
}
private String normalizeSemanticText(
        String value) {

    if (isBlank(value)) {
        return null;
    }

    return value
            .replaceAll("\\s+", " ")
            .trim();
}
private boolean isXPath(
        String locatorType) {

    return "xpath".equalsIgnoreCase(
            locatorType);
}

private String canonicalLocatorType(String locatorType) {

    if (locatorType == null
            || locatorType.isBlank()) {

        return "UNKNOWN";
    }

    String type =
            locatorType
                    .trim()
                    .toLowerCase();

    type = type.replace("by.", "");

    switch (type) {

        case "classname":
        case "class_name":
        case "class":
            return "class";

        case "cssselector":
        case "css_selector":
        case "css":
            return "css";

        case "id":
            return "id";

        case "name":
            return "name";

        case "xpath":
            return "xpath";

        case "linktext":
        case "link_text":
            return "linktext";

        case "partiallinktext":
        case "partial_link_text":
            return "partiallinktext";

        case "data-testid":
        case "data_testid":
            return "data-testid";

        case "data-test":
        case "data_test":
            return "data-test";

        case "data-qa":
        case "data_qa":
            return "data-qa";

        case "data-cy":
        case "data_cy":
            return "data-cy";

        case "aria-label":
        case "arialabel":
            return "aria-label";

        case "placeholder":
            return "placeholder";

        case "text":
            return "text";

        default:
            return type;
    }
}
private boolean isStableLocatorType(
        String locatorType) {

    if (isBlank(locatorType)) {
        return false;
    }

    switch (locatorType
            .trim()
            .toLowerCase()) {

        case "id":
        case "name":
        case "css":
        case "cssselector":
        case "data-test":
        case "data-testid":
        case "data-qa":
        case "data-cy":
        case "aria-label":
            return true;

        default:
            return false;
    }
}
private boolean isBlank(
        String value) {

    return value == null
            || value.isBlank();
}
private String safe(
        String value) {

    if (value == null
            || value.isBlank()) {

        return "UNKNOWN";
    }

    return value.trim();
}

private String normalizeTextForComparison(
        String value) {

    if (!hasText(value)) {
        return "";
    }

    return value
            .replaceAll(
                    "^\\(\\s*\\d+\\s*\\)\\s*",
                    "")
            .replaceAll(
                    "^\\d+\\s*",
                    "")
            .trim()
            .replaceAll(
                    "\\s+",
                    " ")
            .toLowerCase();
}
private double calculateTextMatchScore(
        String expected,
        String actual) {

    if (!hasText(expected)
            || !hasText(actual)) {

        return 0;
    }

    expected =
            normalizeTextForComparison(
                    expected);

    actual =
            normalizeTextForComparison(
                    actual);

    /*
     * Exact visible text.
     */
    if (expected.equals(actual)) {
        return 700;
    }

    /*
     * Dynamic numeric prefix.
     *
     * Example:
     *
     * expected = "Records Found"
     * actual   = "(29) Records Found"
     */
    if (actual.matches(
            "^\\(?\\d+\\)?\\s+"
                    + java.util.regex.Pattern.quote(expected)
                    + "$")) {

        return 650;
    }

    /*
     * Expected text contained in candidate text.
     */
    if (actual.contains(expected)) {
        return 500;
    }

    /*
     * Reverse containment.
     */
    if (expected.contains(actual)) {
        return 250;
    }

    /*
     * Meaningful token coverage.
     *
     * This handles small wording variations while
     * avoiding an exact-string requirement.
     */
    double coverage =
            tokenCoverage(
                    expected,
                    actual);

    if (coverage >= 1.0) {
        return 450;
    }

    if (coverage >= 0.75) {
        return 350;
    }

    if (coverage >= 0.50) {
        return 200;
    }

    return 0;
}
private List<String> extractTextHintsFromFailedLocator(
        String failedLocator) {

    List<String> values =
            new java.util.ArrayList<>();

    if (!hasText(failedLocator)) {
        return values;
    }

    String locator =
            failedLocator.trim();

    /*
     * =====================================================
     * normalize-space()='...'
     * =====================================================
     */
    java.util.regex.Pattern normalizeSpacePattern =
            java.util.regex.Pattern.compile(
                    "normalize-space\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    java.util.regex.Matcher matcher =
            normalizeSpacePattern.matcher(locator);

    while (matcher.find()) {

        String value =
                normalize(matcher.group(1));

        if (hasText(value)
                && !values.contains(value)) {

            values.add(value);
        }
    }

    /*
     * =====================================================
     * text()='...'
     * =====================================================
     */
    java.util.regex.Pattern textPattern =
            java.util.regex.Pattern.compile(
                    "text\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    matcher =
            textPattern.matcher(locator);

    while (matcher.find()) {

        String value =
                normalize(matcher.group(1));

        if (hasText(value)
                && !values.contains(value)) {

            values.add(value);
        }
    }

    /*
     * =====================================================
     * contains(normalize-space(), '...')
     * =====================================================
     */
    java.util.regex.Pattern containsPattern =
            java.util.regex.Pattern.compile(
                    "contains\\s*\\("
                            + "\\s*normalize-space\\s*\\(\\s*\\)"
                            + "\\s*,\\s*['\"]([^'\"]+)['\"]"
                            + "\\s*\\)",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    matcher =
            containsPattern.matcher(locator);

    while (matcher.find()) {

        String value =
                normalize(matcher.group(1));

        if (hasText(value)
                && !values.contains(value)) {

            values.add(value);
        }
    }

    /*
     * =====================================================
     * contains(text(), '...')
     * =====================================================
     */
    java.util.regex.Pattern containsTextPattern =
            java.util.regex.Pattern.compile(
                    "contains\\s*\\("
                            + "\\s*text\\s*\\(\\s*\\)"
                            + "\\s*,\\s*['\"]([^'\"]+)['\"]"
                            + "\\s*\\)",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    matcher =
            containsTextPattern.matcher(locator);

    while (matcher.find()) {

        String value =
                normalize(matcher.group(1));

        if (hasText(value)
                && !values.contains(value)) {

            values.add(value);
        }
    }

    return values;
}
private double normalizeOutcomeConfidence(
        double outcomeConfidence) {

    if (outcomeConfidence <= 0.0) {
        return 0.0;
    }

    /*
     * LearningRecord currently stores confidence
     * as a percentage:
     *
     * HIGH   = 100
     * MEDIUM = 50
     *
     * Ranking internally works with:
     *
     * HIGH   = 1.0
     * MEDIUM = 0.5
     */
    if (outcomeConfidence > 1.0) {

        outcomeConfidence =
                outcomeConfidence / 100.0;
    }

    return Math.max(
            0.0,
            Math.min(
                    1.0,
                    outcomeConfidence));
}
}