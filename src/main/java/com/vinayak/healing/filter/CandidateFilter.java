package com.vinayak.healing.filter;

import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class CandidateFilter {

    private static final int MAX_CANDIDATES = 10;

public List<LocatorCandidate> filter(
        FailureContext context,
        List<LocatorCandidate> candidates) {

    if (candidates == null || candidates.isEmpty()) {
        return List.of();
    }

    if (context == null) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        LocatorCandidate::getFinalScore)
                        .reversed())
                .limit(MAX_CANDIDATES)
                .toList();
    }

    int originalCount =
        candidates.size();

    List<LocatorCandidate> filtered =
            new ArrayList<>();

    for (LocatorCandidate candidate : candidates) {

        if (isUnsafeLocator(candidate)) {

   

    continue;
}

       // 1. Strongest signal: failed operation.
//
// For your framework flow, driver.type(...) records SEND_KEYS
// immediately before findElement(locator), so this action is reliable.
if (context.getFailedAction() == ExecutionAction.SEND_KEYS
        && (context.getExpectedIntent() == null
            || context.getExpectedIntent() == ElementIntent.UNKNOWN
            || context.getExpectedIntent() == ElementIntent.INPUT)){

    String tag =
            candidate.getTagName() == null
                    ? ""
                    : candidate.getTagName()
                            .trim()
                            .toLowerCase();

    boolean editable =
        tag.equals("input")
                || tag.equals("textarea");

    if (!editable) {

        

        continue;
    }
}

        // 2. Second signal: variable-name intent.
        if (!matchesExpectedIntent(
                candidate,
                context.getExpectedIntent())) {

            

            continue;
        }

        /*
         * 3. Weak signal: tag parsed from the locator that failed.
         *
         * Do not reject here. A deliberately broken XPath/CSS can contain
         * a wrong tag, ancestor tag, or unrelated path.
         */
       

    /*
 * Expected text comes from the FAILED locator.
 * Therefore it is supporting evidence, not absolute truth.
 *
 * Examples:
 * My Inf    -> My Info
 * Director  -> Directory
 * Leav      -> Leave
 *
 * Do not reject a candidate only because the text
 * is not an exact substring match.
 */
String expectedText =
        context.getLocatorTextHint();

if (context.getExpectedIntent() == ElementIntent.TEXT
        && expectedText != null
        && !expectedText.isBlank()) {

String candidateValue =
        candidate.getLocatorValue();

String semanticCandidateValue =
        extractSemanticValue(
                candidateValue);

double textSimilarity =
        calculateTextSimilarity(
                expectedText,
                semanticCandidateValue);

  

    /*
     * Do not reject here.
     *
     * CandidateRanker already uses variable identity,
     * intent, tag and other semantic signals.
     */
}

if (context.getExpectedIntent() == ElementIntent.INPUT) {

    String requiredLabel =
            extractLabelFromFailedLocator(
                    context.getLocatorDeclaration());

    if (requiredLabel != null
            && !requiredLabel.isBlank()) {

        String candidateLabel =
                candidate.getNearestLabel();

        if (candidateLabel == null
                || !candidateLabel.equalsIgnoreCase(
                        requiredLabel)) {

           

            continue;
        }
    }
}

filtered.add(candidate);
    }

    int afterActionIntentFilter =
        filtered.size();

    filtered.sort(
        Comparator.comparingDouble(
                LocatorCandidate::getFinalScore)
                .reversed());

                Map<String, LocatorCandidate> uniqueCandidates =
        new LinkedHashMap<>();

for (LocatorCandidate candidate : filtered) {

    String key =
            buildCandidateKey(candidate);

    uniqueCandidates.putIfAbsent(
            key,
            candidate);
}

filtered =
        new ArrayList<>(
                uniqueCandidates.values());

int afterDeduplication =
        filtered.size();

// ==========================================
// KEEP TOP DIVERSE CANDIDATES
// ==========================================

if (filtered.size() > MAX_CANDIDATES) {

    filtered =
            new ArrayList<>(
                    filtered.subList(
                            0,
                            MAX_CANDIDATES));
}

    System.out.println(
        "\n===== FILTER SUMMARY =====");

System.out.println(
        "Original Candidates        : "
                + originalCount);

System.out.println(
        "After Action/Intent Filter : "
                + afterActionIntentFilter);

System.out.println(
        "After Deduplication        : "
                + afterDeduplication);

System.out.println(
        "Final Candidates           : "
                + filtered.size());

    return filtered;
}

private boolean matchesExpectedIntent(
        LocatorCandidate candidate,
        ElementIntent expectedIntent) {

    if (expectedIntent == null
            || expectedIntent == ElementIntent.UNKNOWN) {
        return true;
    }

    String tag = candidate.getTagName();

    if (tag == null || tag.isBlank()) {
        return false;
    }

    tag = tag.toLowerCase();

    // INPUT is strict because sendKeys must go only to editable elements.
   if (expectedIntent == ElementIntent.INPUT) {
    return tag.equals("input")
            || tag.equals("textarea");
}

    if (expectedIntent == ElementIntent.BUTTON) {
        return tag.equals("button")
                || (tag.equals("input")
                    && "submit".equalsIgnoreCase(
                            candidate.getInputType()))
                || candidate.getIntent()
                        == ElementIntent.BUTTON;
    }

    if (expectedIntent == ElementIntent.DROPDOWN) {
        return tag.equals("select")
                || candidate.getIntent()
                        == ElementIntent.DROPDOWN;
    }

    if (expectedIntent == ElementIntent.LINK) {
        return tag.equals("a")
                || candidate.getIntent()
                        == ElementIntent.LINK;
    }

    if (expectedIntent == ElementIntent.TEXT) {
        return candidate.getIntent()
                == ElementIntent.TEXT;
    }

    return candidate.getIntent()
            == expectedIntent;
}



private boolean isUnsafeLocator(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return true;
    }

    String type =
            candidate.getLocatorType();

    String value =
            candidate.getLocatorValue();

    if (type == null || value == null) {
        return false;
    }

    if (!"xpath".equalsIgnoreCase(type)) {
        return false;
    }

    String xpath =
            value.trim()
                    .toLowerCase();

    return xpath.startsWith("/html")
            || xpath.startsWith("/body");
}

private String extractLabelFromFailedLocator(
        String declaration) {

    if (declaration == null
            || declaration.isBlank()) {
        return "";
    }

    Pattern pattern =
            Pattern.compile(
                    "label\\s*\\[\\s*"
                            + "normalize-space\\(\\)\\s*=\\s*"
                            + "['\"]([^'\"]+)['\"]\\s*\\]");

    Matcher matcher =
            pattern.matcher(declaration);

    if (matcher.find()) {
        return matcher.group(1).trim();
    }

    return "";
}

private String extractSemanticValue(
        String locatorValue) {

    if (locatorValue == null
            || locatorValue.isBlank()) {

        return "";
    }

    String value =
            locatorValue.trim();

    // Example:
    // //span[normalize-space()='My Info']
    Pattern normalizeSpacePattern =
            Pattern.compile(
                    "normalize-space\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    Pattern.CASE_INSENSITIVE);

    Matcher normalizeMatcher =
            normalizeSpacePattern.matcher(value);

    if (normalizeMatcher.find()) {
        return normalizeMatcher.group(1).trim();
    }

    // Example:
    // //span[contains(text(), 'My Info')]
    Pattern containsTextPattern =
            Pattern.compile(
                    "contains\\s*\\(\\s*text\\(\\)\\s*,\\s*['\"]([^'\"]+)['\"]\\s*\\)",
                    Pattern.CASE_INSENSITIVE);

    Matcher containsMatcher =
            containsTextPattern.matcher(value);

    if (containsMatcher.find()) {
        return containsMatcher.group(1).trim();
    }

    // Example:
    // //span[text()='My Info']
    Pattern textPattern =
            Pattern.compile(
                    "text\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]",
                    Pattern.CASE_INSENSITIVE);

    Matcher textMatcher =
            textPattern.matcher(value);

    if (textMatcher.find()) {
        return textMatcher.group(1).trim();
    }

    // Normal text candidate:
    // text=My Info -> locator value is already My Info
    return value;
}
private double calculateTextSimilarity(
        String expected,
        String candidateValue) {

    if (expected == null
            || expected.isBlank()
            || candidateValue == null
            || candidateValue.isBlank()) {

        return 0;
    }

    String expectedNormalized =
            normalizeText(expected);

    String candidateNormalized =
            normalizeText(candidateValue);

    /*
     * Candidate locator can contain XPath syntax.
     *
     * Example:
     * //span[normalize-space()='My Info']
     *
     * So containment is useful here.
     */
    if (candidateNormalized.contains(
            expectedNormalized)
            || expectedNormalized.contains(
                    candidateNormalized)) {

        return 1.0;
    }

    int distance =
            levenshteinDistance(
                    expectedNormalized,
                    candidateNormalized);

    int maxLength =
            Math.max(
                    expectedNormalized.length(),
                    candidateNormalized.length());

    if (maxLength == 0) {
        return 1.0;
    }

    return 1.0
            - ((double) distance / maxLength);
}

private String normalizeText(String value) {

    return value
            .toLowerCase()
            .replaceAll(
                    "[^a-z0-9]+",
                    " ")
            .replaceAll(
                    "\\s+",
                    " ")
            .trim();
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

private String buildCandidateKey(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return "NULL";
    }

    String type =
            candidate.getLocatorType() == null
                    ? ""
                    : candidate.getLocatorType()
                            .trim()
                            .toLowerCase();

    String value =
            candidate.getLocatorValue() == null
                    ? ""
                    : candidate.getLocatorValue()
                            .trim()
                            .toLowerCase();

    return type + "|" + value;
}

}