package com.vinayak.healing.ranking;

import java.util.Comparator;
import java.util.List;

import com.vinayak.healing.analysis.DynamicPatternAnalyzer;
import com.vinayak.healing.analysis.LocatorQualityAnalyzer;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class CandidateRanker {

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

        String failedLocator =
                context.getFailedLocator();

        for (LocatorCandidate candidate : candidates) {

           double score = candidate.getFinalScore();

            score += calculateTagScore(
                    context,
                    candidate);

            score += calculateIntentScore(
                    context,
                    candidate);

            score += calculateIdentityScore(
                    context,
                    candidate);

            score += calculateLocatorTypeScore(context, candidate);

            score += calculateParentScore(
                    candidate);

            score += calculateUniquenessScore(
                    candidate);

            score += calculateSemanticSimilarityScore(
                    failedLocator,
                    candidate);

            score += calculateDynamicScore(
        context,
        candidate);

            score += calculateQualityScore(
                    candidate);
                    score += calculateGenerationScore(candidate);

            System.out.println("\n===== SCORE BREAKDOWN =====");
System.out.println(candidate.getLocatorType() + "=" + candidate.getLocatorValue());

System.out.println("Base Score      : " + candidate.getFinalScore());
System.out.println("Tag            : " + calculateTagScore(context, candidate));
System.out.println("Intent         : " + calculateIntentScore(context, candidate));
System.out.println("Identity       : " + calculateIdentityScore(context, candidate));
System.out.println("Locator Type   : " + calculateLocatorTypeScore(context, candidate));
System.out.println("Parent         : " + calculateParentScore(candidate));
System.out.println("Unique         : " + calculateUniquenessScore(candidate));
System.out.println("Semantic       : " + calculateSemanticSimilarityScore(failedLocator, candidate));
System.out.println("Dynamic        : " + calculateDynamicScore(context, candidate));
System.out.println("Quality        : " + calculateQualityScore(candidate));
System.out.println("Generation     : " + calculateGenerationScore(candidate));

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

    return context.getExpectedTag()
            .equalsIgnoreCase(candidate.getTagName())
            ? 150
            : -200;
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

private double calculateParentScore(
        LocatorCandidate candidate) {

    if (!hasText(candidate.getParentTag())) {
        return 0;
    }

    switch (candidate.getParentTag().toLowerCase()) {

        case "form":
            return 20;

        case "table":
            return 15;

        case "dialog":
            return 15;

        case "nav":
            return 10;

        default:
            return 0;
    }
}
private double calculateUniquenessScore(
        LocatorCandidate candidate) {

    if (candidate.getOccurrenceCount() <= 0) {
        return 0;
    }

    if (candidate.getOccurrenceCount() == 1) {
        return 100;
    }

    if (candidate.isUniqueLocator()) {
        return 80;
    }

    return -100;
}

private double calculateSemanticSimilarityScore(
        String failedLocator,
        LocatorCandidate candidate) {

    String failedSemantic =
            extractSemanticValue(failedLocator);

    String candidateSemantic =
            extractSemanticValue(
                    candidate.getLocatorValue());

    if (!hasText(failedSemantic)
            || !hasText(candidateSemantic)) {

        return 0;
    }

    double similarity =
            calculateSimilarity(
                    normalize(failedSemantic),
                    normalize(candidateSemantic));

    if (similarity >= 0.90) {
        return 350;
    }

    if (similarity >= 0.75) {
        return 250;
    }

    if (similarity >= 0.60) {
        return 150;
    }

    if (similarity >= 0.40) {
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


String variable =
        normalize(context.getVariableName());

if (!hasText(variable)
        || "direct locator".equals(variable)
        || "direct_locator".equals(variable)
        || "directlocator".equals(variable)) {

    variable =
            normalize(
                    extractSemanticValue(
                            context.getFailedLocator()));
}

    String locatorValue =
            normalize(
                    extractSemanticValue(
                            candidate.getLocatorValue()));

    String label =
            normalize(
                    candidate.getNearestLabel());

    double locatorCoverage =
            tokenCoverage(
                    variable,
                    locatorValue);

    double labelCoverage =
            tokenCoverage(
                    variable,
                    label);
                    if (labelCoverage >= 1.0)
    return 600;

if (labelCoverage >= 0.75)
    return 450;

if (labelCoverage >= 0.50)
    return 250;

    String locatorType =
            candidate.getLocatorType() == null
                    ? ""
                    : candidate.getLocatorType()
                            .toLowerCase();

    if (isStrongIdentityLocator(locatorType)) {

        if (locatorCoverage >= 1.0)
            return 500;

        if (locatorCoverage >= 0.75)
            return 350;

        if (locatorCoverage >= 0.50)
            return 200;
    }

    if ("text".equals(locatorType)
            || "xpath".equals(locatorType)) {

        if (locatorCoverage >= 1.0)
            return 400;

        if (locatorCoverage >= 0.75)
            return 300;

        if (locatorCoverage >= 0.50)
            return 150;
    }

if ("class".equals(locatorType)
        || "css".equals(locatorType)
        || "cssselector".equals(locatorType)) {

    if (locatorCoverage >= 1.0)
        return 450;

    if (locatorCoverage >= 0.75)
        return 325;

    if (locatorCoverage >= 0.50)
        return 200;
}

    if (labelCoverage >= 1.0)
        return 300;

    if (labelCoverage >= 0.50)
        return 150;

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

    return qualityAnalyzer.calculateScore(
            candidate);
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
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replaceAll("[^a-zA-Z0-9]+", " ")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase();
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

    String[] tokens =
            source.split("\\s+");

    int meaningful = 0;
    int matched = 0;

    for (String token : tokens) {

        if (isGenericToken(token)) {
            continue;
        }

        if (token.length() < 2) {
            continue;
        }

        meaningful++;

        if (target.contains(token)) {
            matched++;
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


if (first.startsWith(second)
        || second.startsWith(first)) {

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
private double calculateGenerationScore(
        LocatorCandidate candidate) {

    if (!candidate.isGeneratedLocator()) {
        return 0;
    }

    double score = candidate.getGenerationConfidence();

    String strategy = candidate.getGenerationStrategy();

    if (strategy == null) {
        return score;
    }

    switch (strategy.toUpperCase()) {

        case "ID":
            return score + 100;

        case "DATA_TESTID":
        case "DATA_TEST":
            return score + 90;

        case "PARENT_ID":
            return score + 80;

        case "PARENT_DATA":
            return score + 75;

        case "SEMANTIC_CONTAINER":
            return score + 65;

        case "SCOPED_CSS":
            return score + 60;

        case "SCOPED_XPATH":
            return score + 55;

        case "LABEL_XPATH":
            return score + 45;

        case "POSITION_XPATH":
            return score + 10;

        case "AI":
            return score + 70;

        default:
            return score;
    }
}
}