package com.vinayak.healing.decision;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

import java.util.ArrayList;
import java.util.List;

public class SemanticEvidenceEvaluator {

    /*
     * ============================================================
     * FUZZY SEMANTIC MATCH CONFIGURATION
     * ============================================================
     *
     * Used only when normal exact/contains matching fails.
     *
     * Example:
     *
     * Failed text   : Record Xoun
     * Candidate text: (1) Record Found
     *
     * "Record" is an exact semantic token.
     * "Xoun" is very similar to "Found".
     *
     * This allows controlled healing without making the
     * identity gate completely permissive.
     */
    private static final double FUZZY_TOKEN_THRESHOLD = 0.70;

    private static final double FUZZY_PHRASE_THRESHOLD = 0.75;

    public SemanticEvidence evaluate(
            LocatorCandidate candidate,
            FailureContext context) {

        SemanticEvidence evidence =
                new SemanticEvidence();

        if (candidate == null || context == null) {
            return evidence;
        }

        /*
         * ============================================================
         * VARIABLE
         * ============================================================
         */
        if (matches(
                context.getVariableName(),
                candidate.getLocatorValue())
                || matches(
                context.getVariableName(),
                candidate.getElementText())
                || matches(
                context.getVariableName(),
                candidate.getNearestLabel())
                || matches(
                context.getVariableName(),
                candidate.getId())
                || matches(
                context.getVariableName(),
                candidate.getName())) {

            evidence.setVariableMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ============================================================
         * LOCATOR TEXT / IDENTITY EVIDENCE
         * ============================================================
         *
         * The failed locator may contain meaningful business text.
         *
         * Example:
         *
         * Failed:
         *     //*[contains(normalize-space(), 'Record Xoun')]
         *
         * Candidate:
         *     elementText = "(1) Record Found"
         *
         * We must compare the semantic text:
         *
         *     Record Xoun
         *
         * against:
         *
         *     (1) Record Found
         */

        String locatorText =
                context.getLocatorTextHint();

        String failedLocatorValue =
                extractLocatorValue(
                        context.getFailedLocator());

boolean locatorMatched =
        semanticLocatorMatch(
                locatorText,
                candidate)
        || semanticLocatorMatch(
                failedLocatorValue,
                candidate);

if (locatorMatched) {

    evidence.setLocatorMatched(true);
    evidence.incrementSignal();

    System.out.println(
            "LOCATOR SEMANTIC MATCH"
                    + " | locatorText="
                    + locatorText
                    + " | failedLocatorValue="
                    + failedLocatorValue
                    + " | candidateText="
                    + candidate.getElementText()
                    + " | candidate="
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue());
}

        /*
         * ============================================================
         * EXPECTED LABEL
         * ============================================================
         */
        if (matches(
                context.getExpectedLabel(),
                candidate.getNearestLabel())) {

            evidence.setLabelMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ============================================================
         * ID
         * ============================================================
         */
        if (matches(
                context.getVariableName(),
                candidate.getId())) {

            evidence.setIdMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ============================================================
         * NAME
         * ============================================================
         */
        if (matches(
                context.getVariableName(),
                candidate.getName())) {

            evidence.setNameMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ============================================================
         * TAG
         * ============================================================
         */
        if (context.getExpectedTag() != null
                && candidate.getTagName() != null
                && context.getExpectedTag()
                .equalsIgnoreCase(
                        candidate.getTagName())) {

            evidence.setTagMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ============================================================
         * INTENT
         * ============================================================
         */
        if (context.getExpectedIntent() != null
                && context.getExpectedIntent()
                != ElementIntent.UNKNOWN
                && candidate.getIntent() != null
                && context.getExpectedIntent()
                == candidate.getIntent()) {

            evidence.setIntentMatched(true);
            evidence.incrementSignal();

            System.out.println(
        "===== SEMANTIC EVIDENCE DEBUG =====");

System.out.println(
        "Candidate : "
                + candidate.getLocatorType()
                + "="
                + candidate.getLocatorValue());

System.out.println(
        "Variable matched : "
                + evidence.isVariableMatched());

System.out.println(
        "Locator matched  : "
                + evidence.isLocatorMatched());

System.out.println(
        "Label matched    : "
                + evidence.isLabelMatched());

System.out.println(
        "ID matched       : "
                + evidence.isIdMatched());

System.out.println(
        "NAME matched     : "
                + evidence.isNameMatched());

System.out.println(
        "Tag matched      : "
                + evidence.isTagMatched());

System.out.println(
        "Intent matched   : "
                + evidence.isIntentMatched());

System.out.println(
        "Signal count     : "
                + evidence.getSignalCount());

System.out.println(
        "===================================");
        }

        return evidence;
    }

    /*
     * ============================================================
     * EXTRACT LOCATOR VALUE
     * ============================================================
     *
     * Handles normal Selenium strings:
     *
     * By.id: username
     * By.name: password
     * By.className: shopping
     *
     * Also handles XPath semantic text:
     *
     * //*[contains(normalize-space(), 'Record Xoun')]
     *
     * //span[normalize-space()='Record Xoun']
     *
     * The important part is that we return:
     *
     *     Record Xoun
     *
     * instead of the complete XPath.
     */
    private boolean semanticLocatorMatch(
        String locatorText,
        LocatorCandidate candidate) {

    if (!hasText(locatorText)
            || candidate == null) {

        return false;
    }

    String candidateText =
            firstNonBlank(
                    candidate.getElementText(),
                    candidate.getNearestLabel(),
                    candidate.getAriaLabel(),
                    candidate.getPlaceholder());

    if (!hasText(candidateText)) {
        return false;
    }

    /*
     * Semantic locator matching is intended for
     * text-oriented candidates.
     */
    boolean textLike =
            candidate.getIntent() == ElementIntent.TEXT
                    || "span".equalsIgnoreCase(
                            candidate.getTagName())
                    || "label".equalsIgnoreCase(
                            candidate.getTagName())
                    || "p".equalsIgnoreCase(
                            candidate.getTagName())
                    || "div".equalsIgnoreCase(
                            candidate.getTagName())
                    || "td".equalsIgnoreCase(
                            candidate.getTagName())
                    || "li".equalsIgnoreCase(
                            candidate.getTagName());

    if (!textLike) {
        return false;
    }

    List<String> locatorTokens =
            meaningfulTokens(
                    locatorText);

    List<String> candidateTokens =
            meaningfulTokens(
                    candidateText);

    if (locatorTokens.isEmpty()
            || candidateTokens.isEmpty()) {

        return false;
    }

    /*
     * First preference:
     * normal semantic match.
     */
    if (matches(
            locatorText,
            candidateText)) {

        return true;
    }

    /*
     * Controlled semantic anchor matching.
     *
     * Example:
     *
     * Failed:
     *     Record Xoun
     *
     * Candidate:
     *     (1) Record Found
     *
     * "record" is a strong shared semantic anchor.
     */
    int exactAnchorCount = 0;

    for (String locatorToken :
            locatorTokens) {

        if (locatorToken.length() < 3) {
            continue;
        }

        for (String candidateToken :
                candidateTokens) {

            if (locatorToken.equals(
                    candidateToken)) {

                exactAnchorCount++;
                break;
            }
        }
    }

    /*
     * Require at least one meaningful shared
     * semantic token.
     */
    if (exactAnchorCount <= 0) {
        return false;
    }

    System.out.println(
            "SEMANTIC ANCHOR MATCH"
                    + " | locatorText="
                    + locatorText
                    + " | candidateText="
                    + candidateText
                    + " | anchors="
                    + exactAnchorCount);

    return true;
}
private String firstNonBlank(
        String... values) {

    if (values == null) {
        return "";
    }

    for (String value : values) {

        if (hasText(value)) {
            return value.trim();
        }
    }

    return "";
}
    private String extractLocatorValue(
            String failedLocator) {

        if (!hasText(failedLocator)) {
            return "";
        }

        String value =
                failedLocator.trim();

        /*
         * ------------------------------------------------------------
         * Selenium By format
         * ------------------------------------------------------------
         *
         * By.className: shopping
         * By.id: username
         * By.name: password
         */
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile(
                                "(?i)^By\\.[a-zA-Z]+\\s*:\\s*(.+)$")
                        .matcher(value);

        if (matcher.find()) {

            String extracted =
                    matcher.group(1)
                            .trim()
                            .replaceAll(
                                    "^['\"]|['\"]$",
                                    "");

            /*
             * If the By value itself contains semantic text,
             * try extracting that semantic text.
             */
            String semantic =
                    extractXPathText(extracted);

            if (hasText(semantic)) {
                return semantic;
            }

            return extracted;
        }

        /*
         * ------------------------------------------------------------
         * XPath semantic text
         * ------------------------------------------------------------
         *
         * Example:
         *
         * //*[contains(normalize-space(), 'Record Xoun')]
         *
         * returns:
         *
         * Record Xoun
         */
        String xpathText =
                extractXPathText(value);

        if (hasText(xpathText)) {
            return xpathText;
        }

        /*
         * No semantic text found.
         * Return original locator value.
         */
        return value;
    }

    /*
     * ============================================================
     * EXTRACT TEXT FROM XPATH
     * ============================================================
     *
     * Extracts quoted strings from XPath expressions.
     *
     * Examples:
     *
     * //*[contains(normalize-space(), 'Record Xoun')]
     *
     * ->
     *
     * Record Xoun
     *
     *
     * //span[normalize-space()='Record Found']
     *
     * ->
     *
     * Record Found
     */
    private String extractXPathText(
            String value) {

        if (!hasText(value)) {
            return "";
        }

        /*
         * contains(..., 'TEXT')
         */
        java.util.regex.Pattern containsPattern =
                java.util.regex.Pattern.compile(
                        "(?i)contains\\s*\\([^,]+,\\s*['\"]([^'\"]+)['\"]\\s*\\)");

        java.util.regex.Matcher containsMatcher =
                containsPattern.matcher(value);

        if (containsMatcher.find()) {

            return containsMatcher.group(1)
                    .trim();
        }

        /*
         * normalize-space()='TEXT'
         */
        java.util.regex.Pattern normalizePattern =
                java.util.regex.Pattern.compile(
                        "(?i)normalize-space\\s*\\(\\s*\\)\\s*=\\s*['\"]([^'\"]+)['\"]");

        java.util.regex.Matcher normalizeMatcher =
                normalizePattern.matcher(value);

        if (normalizeMatcher.find()) {

            return normalizeMatcher.group(1)
                    .trim();
        }

        /*
         * text()='TEXT'
         */
        java.util.regex.Pattern textPattern =
                java.util.regex.Pattern.compile(
                        "(?i)text\\s*\\(\\s*\\)\\s*=\\s*['\"]([^'\"]+)['\"]");

        java.util.regex.Matcher textMatcher =
                textPattern.matcher(value);

        if (textMatcher.find()) {

            return textMatcher.group(1)
                    .trim();
        }

        return "";
    }

    /*
     * ============================================================
     * SEMANTIC MATCH
     * ============================================================
     *
     * Matching order:
     *
     * 1. Exact match
     * 2. Contains match
     * 3. Controlled fuzzy semantic match
     */
    private boolean matches(
            String left,
            String right) {

        if (!hasText(left)
                || !hasText(right)) {

            return false;
        }

        String normalizedLeft =
                normalize(left);

        String normalizedRight =
                normalize(right);

        /*
         * ------------------------------------------------------------
         * LEVEL 1 - EXACT
         * ------------------------------------------------------------
         */
        if (normalizedLeft.equals(
                normalizedRight)) {

            return true;
        }

        /*
         * ------------------------------------------------------------
         * LEVEL 2 - CONTAINS
         * ------------------------------------------------------------
         */
        if (normalizedLeft.contains(
                normalizedRight)
                || normalizedRight.contains(
                normalizedLeft)) {

            return true;
        }

        /*
         * ------------------------------------------------------------
         * LEVEL 3 - CONTROLLED FUZZY MATCH
         * ------------------------------------------------------------
         */
        return fuzzySemanticMatch(
                normalizedLeft,
                normalizedRight);
    }

    /*
     * ============================================================
     * CONTROLLED FUZZY SEMANTIC MATCH
     * ============================================================
     *
     * Example:
     *
     * left:
     *     Record Xoun
     *
     * right:
     *     1 Record Found
     *
     * Tokens:
     *
     * left  -> record, xoun
     * right -> record, found
     *
     * record -> exact match
     * xoun   -> found similarity ~75%
     *
     * Result -> ACCEPT
     *
     *
     * Important:
     *
     * We do NOT accept fuzzy matching based on only one common
     * generic token.
     *
     * Example:
     *
     *     User
     *
     * and
     *
     *     User Role
     *
     * are not automatically accepted by fuzzy matching.
     */
    private boolean fuzzySemanticMatch(
            String left,
            String right) {

        List<String> leftTokens =
                meaningfulTokens(left);

        List<String> rightTokens =
                meaningfulTokens(right);

        /*
         * Fuzzy matching is intended for meaningful phrases.
         */
        if (leftTokens.size() < 2
                || rightTokens.size() < 2) {

            return false;
        }

        boolean exactTokenFound =
                false;

        double totalBestSimilarity = 0.0;

        int comparedTokens = 0;

        for (String leftToken : leftTokens) {

            double bestSimilarity = 0.0;

            for (String rightToken : rightTokens) {

                double similarity =
                        stringSimilarity(
                                leftToken,
                                rightToken);

                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                }

                /*
                 * Strong identity signal.
                 */
                if (leftToken.equals(
                        rightToken)) {

                    exactTokenFound = true;
                }
            }

            /*
             * A token that is completely unrelated should
             * prevent fuzzy phrase matching.
             */
            if (bestSimilarity
                    < FUZZY_TOKEN_THRESHOLD) {

                return false;
            }

            totalBestSimilarity +=
                    bestSimilarity;

            comparedTokens++;
        }

        if (comparedTokens == 0) {
            return false;
        }

        double phraseSimilarity =
                totalBestSimilarity
                        / comparedTokens;

        boolean accepted =
                exactTokenFound
                        && phraseSimilarity
                        >= FUZZY_PHRASE_THRESHOLD;

        if (accepted) {

            System.out.println(
                    "FUZZY SEMANTIC MATCH"
                            + " | left="
                            + left
                            + " | right="
                            + right
                            + " | similarity="
                            + phraseSimilarity);
        }

        return accepted;
    }

    /*
     * ============================================================
     * MEANINGFUL TOKENS
     * ============================================================
     *
     * Removes numeric-only tokens.
     *
     * Example:
     *
     * "(1) Record Found"
     *
     * becomes:
     *
     * record
     * found
     */
    private List<String> meaningfulTokens(
            String value) {

        List<String> tokens =
                new ArrayList<>();

        if (!hasText(value)) {
            return tokens;
        }

        String[] split =
                value.split("\\s+");

        for (String token : split) {

            String cleaned =
                    token
                            .replaceAll(
                                    "[^a-zA-Z0-9]",
                                    "")
                            .toLowerCase();

            if (cleaned.isBlank()) {
                continue;
            }

            /*
             * Ignore pure numeric tokens.
             */
            if (cleaned.matches("\\d+")) {
                continue;
            }

            /*
             * Ignore extremely short noise tokens.
             */
            if (cleaned.length() < 2) {
                continue;
            }

            tokens.add(cleaned);
        }

        return tokens;
    }

    /*
     * ============================================================
     * STRING SIMILARITY
     * ============================================================
     *
     * Returns value between 0.0 and 1.0.
     *
     * 1.0 = identical
     * 0.0 = completely different
     */
    private double stringSimilarity(
            String left,
            String right) {

        if (!hasText(left)
                || !hasText(right)) {

            return 0.0;
        }

        if (left.equals(right)) {
            return 1.0;
        }

        int maxLength =
                Math.max(
                        left.length(),
                        right.length());

        if (maxLength == 0) {
            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        left,
                        right);

        return 1.0
                - ((double) distance
                / maxLength);
    }

    /*
     * ============================================================
     * LEVENSHTEIN DISTANCE
     * ============================================================
     */
    private int levenshteinDistance(
            String left,
            String right) {

        int leftLength =
                left.length();

        int rightLength =
                right.length();

        int[][] matrix =
                new int[leftLength + 1]
                        [rightLength + 1];

        for (int i = 0;
             i <= leftLength;
             i++) {

            matrix[i][0] = i;
        }

        for (int j = 0;
             j <= rightLength;
             j++) {

            matrix[0][j] = j;
        }

        for (int i = 1;
             i <= leftLength;
             i++) {

            for (int j = 1;
                 j <= rightLength;
                 j++) {

                int substitutionCost =
                        left.charAt(i - 1)
                                == right.charAt(j - 1)
                                ? 0
                                : 1;

                matrix[i][j] =
                        Math.min(
                                Math.min(
                                        matrix[i - 1][j]
                                                + 1,
                                        matrix[i][j - 1]
                                                + 1),
                                matrix[i - 1][j - 1]
                                        + substitutionCost);
            }
        }

        return matrix[leftLength][rightLength];
    }

    /*
     * ============================================================
     * HAS TEXT
     * ============================================================
     */
    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }

    /*
     * ============================================================
     * NORMALIZE
     * ============================================================
     */
    private String normalize(
            String value) {

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
}