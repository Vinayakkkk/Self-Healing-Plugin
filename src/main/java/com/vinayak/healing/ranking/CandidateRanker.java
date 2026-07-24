package com.vinayak.healing.ranking;

import java.util.Comparator;
import java.util.List;

import com.vinayak.healing.engine.DynamicLocatorAnalyzer;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class CandidateRanker {

    private final DynamicLocatorAnalyzer dynamicAnalyzer =
            new DynamicLocatorAnalyzer();

    public List<LocatorCandidate> rank(
            FailureContext context,
            List<LocatorCandidate> candidates) {

        if (context == null || candidates == null) {
            return candidates;
        }

        String failedLocator =
                context.getFailedLocator();

        for (LocatorCandidate candidate : candidates) {

            /*
             * Always start from the original DOM score.
             */
            double score =
                    candidate.getScore();

            // =====================================
            // TAG MATCH
            // =====================================

            if (hasText(context.getExpectedTag())
                    && hasText(candidate.getTagName())) {

                if (context.getExpectedTag()
                        .equalsIgnoreCase(
                                candidate.getTagName())) {

                    score += 150;

                } else {

                    score -= 200;
                }
            }

            // =====================================
            // INTENT MATCH
            // =====================================

            if (context.getExpectedIntent() != null
                    && context.getExpectedIntent()
                            != ElementIntent.UNKNOWN
                    && candidate.getIntent() != null
                    && candidate.getIntent()
                            != ElementIntent.UNKNOWN) {

                if (context.getExpectedIntent()
                        == candidate.getIntent()) {

                    score += 120;

                } else {

                    score -= 250;
                }
            }

            // =====================================
            // VARIABLE IDENTITY
            // =====================================

            double identityScore =
                    calculateIdentityScore(
                            context,
                            candidate);

            score += identityScore;

            // =====================================
            // PARENT CONTEXT
            // =====================================

            if ("form".equalsIgnoreCase(
                    candidate.getParentTag())) {

                score += 20;
            }

            if ("table".equalsIgnoreCase(
                    candidate.getParentTag())) {

                score += 15;
            }

            // =====================================
            // LOCATOR STABILITY
            // =====================================

            String locatorType =
                    candidate.getLocatorType();

            if ("id".equalsIgnoreCase(locatorType)) {

                score += 40;

            } else if ("name".equalsIgnoreCase(
                    locatorType)) {

                score += 30;

            } else if (locatorType != null
                    && locatorType.toLowerCase()
                            .startsWith("data")) {

                score += 35;
            }

            // =====================================
// LOCATOR UNIQUENESS
// =====================================

double uniquenessScore = 0;

if (candidate.getOccurrenceCount() == 1) {

    uniquenessScore = 100;

} else if (candidate.getOccurrenceCount() > 1) {

    /*
     * Duplicate locators are weaker,
     * but do not reject them here.
     *
     * A later scoped locator may still
     * uniquely identify the same element.
     */
    uniquenessScore = -100;
}

score += uniquenessScore;

            // =====================================
            // FAILED LOCATOR SIMILARITY
            // =====================================

double similarityScore = 0;

String failedSemanticValue =
        extractSemanticValue(
                failedLocator);

String candidateSemanticValue =
        extractSemanticValue(
                candidate.getLocatorValue());

if (hasText(failedSemanticValue)
        && hasText(candidateSemanticValue)) {

    double similarity =
            calculateSimilarity(
                    normalize(failedSemanticValue),
                    normalize(candidateSemanticValue));

    /*
     * Strong semantic similarity receives a ranking boost.
     *
     * Example:
     * My Inffo -> My Info
     */
    if (similarity >= 0.85) {

        similarityScore = 350;

    } else if (similarity >= 0.70) {

        similarityScore = 220;

    } else if (similarity >= 0.50) {

        similarityScore = 100;
    }

    score += similarityScore;
}



            candidate.setFinalScore(score);

        
        }

        candidates.sort(
                Comparator.comparingDouble(
                        LocatorCandidate::getFinalScore)
                        .reversed());

       System.out.println(
        "\n===== CANDIDATE RANKING =====");

System.out.println(
        "Total Candidates : "
                + candidates.size());

int limit =
        Math.min(
                10,
                candidates.size());

System.out.println(
        "Showing Top "
                + limit
                + " Candidates");

for (int i = 0; i < limit; i++) {

    LocatorCandidate candidate =
            candidates.get(i);

    System.out.println(
            (i + 1)
                    + ". "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue()
                    + " | Score="
                    + candidate.getFinalScore()
                    + " | Matches="
                    + candidate.getOccurrenceCount()
                    + " | Unique="
                    + candidate.isUniqueLocator());
}

        return candidates;
    }

    /*
     * Uses the Page Object variable name as semantic identity.
     *
     * Example:
     *
     * continueButton -> continue
     * usernameSearch -> username
     * errorMessage   -> error
     * postButton     -> post
     */
private double calculateIdentityScore(
        FailureContext context,
        LocatorCandidate candidate) {

    if (!hasText(context.getVariableName())
            || candidate == null) {

        return 0;
    }

    String variable =
            normalize(
                    context.getVariableName());

    String locatorValue =
            normalize(
                    extractSemanticValue(
                            candidate.getLocatorValue()));

    String nearestLabel =
            normalize(
                    candidate.getNearestLabel());

    double locatorCoverage =
            tokenCoverage(
                    variable,
                    locatorValue);

    double labelCoverage =
            tokenCoverage(
                    variable,
                    nearestLabel);

    String locatorType =
            candidate.getLocatorType() == null
                    ? ""
                    : candidate.getLocatorType()
                            .trim()
                            .toLowerCase();

    // ==========================================
    // STRONG SEMANTIC LOCATOR TYPES
    // ==========================================

    if (isStrongIdentityLocator(locatorType)) {

        if (locatorCoverage >= 1.0) {
            return 500;
        }

        if (locatorCoverage >= 0.75) {
            return 350;
        }

        if (locatorCoverage >= 0.50) {
            return 200;
        }
    }

    // ==========================================
    // TEXT / XPATH SEMANTIC IDENTITY
    // ==========================================

    if (locatorType.equals("text")
            || locatorType.equals("xpath")) {

        if (locatorCoverage >= 1.0) {
            return 400;
        }

        if (locatorCoverage >= 0.75) {
            return 300;
        }

        if (locatorCoverage >= 0.50) {
            return 150;
        }
    }

    // ==========================================
    // CLASS / CSS
    // ==========================================
    //
    // Shared technical classes are weak identity.
    // They must never receive the same +500 bonus
    // as ID, name, text, label, etc.
    // ==========================================

    if (locatorType.equals("class")
            || locatorType.equals("css")
            || locatorType.equals("cssselector")) {

        if (locatorCoverage >= 1.0) {
            return 75;
        }

        if (locatorCoverage >= 0.50) {
            return 30;
        }
    }

    // ==========================================
    // NEAREST LABEL
    // ==========================================

    if (labelCoverage >= 1.0) {
        return 300;
    }

    if (labelCoverage >= 0.50) {
        return 150;
    }

    return 0;
}

    private double tokenCoverage(
            String variable,
            String candidateValue) {

        if (!hasText(variable)
                || !hasText(candidateValue)) {

            return 0;
        }

        String[] tokens =
                variable.split("\\s+");

        int meaningfulTokens = 0;
        int matchedTokens = 0;

        for (String token : tokens) {

            /*
             * Ignore generic Page Object suffixes.
             *
             * They describe the element type,
             * not its business identity.
             */
            if (isGenericToken(token)) {
                continue;
            }

            if (token.length() < 3) {
                continue;
            }

            meaningfulTokens++;

            if (candidateValue.contains(token)) {
                matchedTokens++;
            }
        }

        if (meaningfulTokens == 0) {
            return 0;
        }

        return (double) matchedTokens
                / meaningfulTokens;
    }

    private boolean isGenericToken(
            String token) {

        return token.equals("button")
                || token.equals("input")
                || token.equals("field")
                || token.equals("link")
                || token.equals("text")
                || token.equals("label")
                || token.equals("message")
                || token.equals("element")
                || token.equals("locator");
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2")
                .replaceAll(
                        "[^a-zA-Z0-9]+",
                        " ")
                .replaceAll(
                        "\\s+",
                        " ")
                .trim()
                .toLowerCase();
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
    private boolean isDynamicLocator(
        String locator) {

    if (!hasText(locator)) {
        return false;
    }

    String value =
            locator.toLowerCase();

    return value.contains("-")
            || value.contains("_")
            || value.matches(".*\\d+.*");
}
private String extractSemanticValue(
        String value) {

    if (!hasText(value)) {
        return "";
    }

    String cleaned = value.trim();

    /*
     * Remove Selenium's By.xxx prefix.
     *
     * Example:
     * By.xpath: //span[text()='My Inffo']
     */
    cleaned =
            cleaned.replaceFirst(
                    "(?i)^By\\.[a-zA-Z]+\\s*:\\s*",
                    "");

    /*
     * XPath quoted values.
     *
     * Handles:
     * text()='My Info'
     * contains(text(), 'My Info')
     * normalize-space()='My Info'
     * @id='username'
     * @name='username'
     */
    java.util.regex.Matcher quotedMatcher =
            java.util.regex.Pattern
                    .compile(
                            "['\"]([^'\"]+)['\"]")
                    .matcher(cleaned);

    String bestQuotedValue = "";

    while (quotedMatcher.find()) {

        String extracted =
                quotedMatcher.group(1);

        /*
         * Prefer meaningful human-readable values.
         */
        if (extracted != null
                && extracted.length()
                > bestQuotedValue.length()) {

            bestQuotedValue = extracted;
        }
    }

    if (hasText(bestQuotedValue)) {
        return bestQuotedValue;
    }

    /*
     * CSS attribute selector.
     *
     * Example:
     * [placeholder='Search']
     */
    java.util.regex.Matcher cssMatcher =
            java.util.regex.Pattern
                    .compile(
                            "\\[[^=]+=['\"]?([^'\"\\]]+)['\"]?\\]")
                    .matcher(cleaned);

    if (cssMatcher.find()) {
        return cssMatcher.group(1);
    }

    /*
     * Simple locator values such as:
     * My Info
     * username
     * employee_name
     */
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

    int distance =
            levenshteinDistance(
                    first,
                    second);

    int maxLength =
            Math.max(
                    first.length(),
                    second.length());

    if (maxLength == 0) {
        return 1.0;
    }

    return 1.0
            - ((double) distance
            / maxLength);
}

private int levenshteinDistance(
        String first,
        String second) {

    int[][] dp =
            new int[first.length() + 1]
                    [second.length() + 1];

    for (int i = 0;
            i <= first.length();
            i++) {

        dp[i][0] = i;
    }

    for (int j = 0;
            j <= second.length();
            j++) {

        dp[0][j] = j;
    }

    for (int i = 1;
            i <= first.length();
            i++) {

        for (int j = 1;
                j <= second.length();
                j++) {

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
                            dp[i - 1][j - 1]
                                    + cost);
        }
    }

    return dp[first.length()]
            [second.length()];
}
private boolean isStrongIdentityLocator(
        String locatorType) {

    if (!hasText(locatorType)) {
        return false;
    }

    return locatorType.equals("id")
            || locatorType.equals("name")
            || locatorType.equals("placeholder")
            || locatorType.equals("aria-label")
            || locatorType.equals("data-test")
            || locatorType.equals("data-testid")
            || locatorType.equals("data-qa")
            || locatorType.equals("data-cy")
            || locatorType.equals("href");
}
}