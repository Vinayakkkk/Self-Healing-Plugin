package com.vinayak.healing.ranking;

import java.util.Comparator;
import java.util.List;
import com.vinayak.healing.similarity.SimilarityUtil;
import com.vinayak.healing.analysis.DynamicPatternAnalyzer;
import com.vinayak.healing.analysis.LocatorQualityAnalyzer;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class CandidateRanker {

        private static final double LOCATOR_QUALITY_WEIGHT = 1.5;

    private final ParentScorer parentScorer =
        new ParentScorer();

    private final DynamicPatternAnalyzer dynamicAnalyzer =
            new DynamicPatternAnalyzer();

    private final LocatorQualityAnalyzer qualityAnalyzer =
            new LocatorQualityAnalyzer();

    public List<LocatorCandidate> rank(
            FailureContext context,
            List<LocatorCandidate> candidates) {

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

            System.out.println("\n===== SCORE BREAKDOWN =====");
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
            || !hasText(context.getExpectedText())
            || !hasText(candidate.getElementText())) {

        return 0;
    }

    String expected =
            normalize(context.getExpectedText());

    String actual =
            normalize(candidate.getElementText());

    if (expected.isBlank()
            || actual.isBlank()) {

        return 0;
    }

    /*
     * Exact visible text is extremely strong identity
     * evidence for a TEXT element.
     */
    if (expected.equals(actual)) {
        return 700;
    }

    /*
     * Partial business-text match.
     */
    if (actual.contains(expected)
            || expected.contains(actual)) {

        return 250;
    }

    return 0;
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
}