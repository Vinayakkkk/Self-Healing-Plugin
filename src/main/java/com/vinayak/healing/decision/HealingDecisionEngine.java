package com.vinayak.healing.decision;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

import java.util.List;

public class HealingDecisionEngine {

    public HealingDecision decide(
            LocatorCandidate validatedCandidate,
            List<LocatorCandidate> candidates,
            FailureContext context) {

        if (validatedCandidate == null) {

            return new HealingDecision(
                    null,
                    HealingConfidence.REJECT,
                    false,
                    false,
                    "No candidate passed browser validation");
        }

        double selectedScore =
                validatedCandidate.getFinalScore();

        double secondBestScore =
                findSecondBestScore(
                        validatedCandidate,
                        candidates);

        double scoreGap =
                secondBestScore == Double.NEGATIVE_INFINITY
                        ? Double.POSITIVE_INFINITY
                        : selectedScore - secondBestScore;

        int semanticSignals =
                countSemanticSignals(
                        validatedCandidate,
                        context);

                        int occurrenceCount =
        validatedCandidate.getOccurrenceCount();

boolean uniqueLocator =
        occurrenceCount == 1;


        /*
 * UNIQUENESS SAFETY GATE
 */
if (occurrenceCount > 1) {

    if (semanticSignals >= 1) {

        return new HealingDecision(
                validatedCandidate,
                HealingConfidence.MEDIUM,
                true,
                false,
                "Candidate has semantic evidence but locator is not unique");
    }

    return new HealingDecision(
            validatedCandidate,
            HealingConfidence.REJECT,
            false,
            false,
            "Candidate locator matches multiple elements");
}

        /*
         * HIGH:
         *
         * Candidate passed CandidateValidator
         * and has strong semantic identity.
         *
         * Safe to heal and persist.
         */
        if (uniqueLocator
        && semanticSignals >= 2
        && scoreGap >= 100) {

            return new HealingDecision(
                    validatedCandidate,
                    HealingConfidence.HIGH,
                    true,
                    true,
                    "Strong semantic evidence and clear ranking lead");
        }

        /*
         * HIGH:
         *
         * Sometimes only one candidate survives filtering.
         * If it has several semantic signals, there is no
         * meaningful competing candidate.
         */
       if (uniqueLocator
        && semanticSignals >= 3
        && Double.isInfinite(scoreGap)) {

            return new HealingDecision(
                    validatedCandidate,
                    HealingConfidence.HIGH,
                    true,
                    true,
                    "Validated candidate has strong semantic evidence");
        }

        /*
         * MEDIUM:
         *
         * Candidate is browser validated and has semantic
         * evidence, but ranking is relatively close.
         *
         * Allow runtime healing but do not permanently cache.
         */
        if (semanticSignals >= 1) {

            return new HealingDecision(
                    validatedCandidate,
                    HealingConfidence.MEDIUM,
                    true,
                    false,
                    "Validated candidate has supporting evidence but insufficient certainty for persistent cache");
        }

        /*
         * LOW:
         *
         * Structurally valid but there is not enough
         * semantic identity to trust automatically.
         */
        return new HealingDecision(
                validatedCandidate,
                HealingConfidence.LOW,
                false,
                false,
                "Candidate passed structural validation but lacks semantic evidence");
    }

    private int countSemanticSignals(
            LocatorCandidate candidate,
            FailureContext context) {

        if (context == null) {
            return 0;
        }

        int signals = 0;

        // Variable identity
        if (hasText(context.getVariableName())) {

            double similarity =
                    tokenSimilarity(
                            context.getVariableName(),
                            candidate.getLocatorValue());

            if (similarity >= 0.50) {
                signals++;
            }
        }

        // Broken locator text is supporting evidence
        if (hasText(context.getLocatorTextHint())) {

            double similarity =
                    textSimilarity(
                            context.getLocatorTextHint(),
                            candidate.getLocatorValue());

            if (similarity >= 0.60) {
                signals++;
            }
        }

        // Expected label
        if (hasText(context.getExpectedLabel())
                && hasText(candidate.getNearestLabel())) {

            double similarity =
                    textSimilarity(
                            context.getExpectedLabel(),
                            candidate.getNearestLabel());

            if (similarity >= 0.60) {
                signals++;
            }
        }

        // Expected tag
        if (hasText(context.getExpectedTag())
                && hasText(candidate.getTagName())
                && context.getExpectedTag()
                        .equalsIgnoreCase(
                                candidate.getTagName())) {

            signals++;
        }

        // Expected intent
        if (context.getExpectedIntent() != null
                && context.getExpectedIntent()
                        != ElementIntent.UNKNOWN
                && candidate.getIntent() != null
                && context.getExpectedIntent()
                        == candidate.getIntent()) {

            signals++;
        }

        return signals;
    }

    private double findSecondBestScore(
            LocatorCandidate selected,
            List<LocatorCandidate> candidates) {

        if (candidates == null
                || candidates.isEmpty()) {

            return Double.NEGATIVE_INFINITY;
        }

        double secondBest =
                Double.NEGATIVE_INFINITY;

        for (LocatorCandidate candidate : candidates) {

            if (candidate == null
                    || candidate == selected) {
                continue;
            }

            if (sameCandidate(
                    selected,
                    candidate)) {
                continue;
            }

            secondBest =
                    Math.max(
                            secondBest,
                            candidate.getFinalScore());
        }

        return secondBest;
    }

    private boolean sameCandidate(
            LocatorCandidate first,
            LocatorCandidate second) {

        if (first.getLocatorType() == null
                || first.getLocatorValue() == null
                || second.getLocatorType() == null
                || second.getLocatorValue() == null) {

            return false;
        }

        return first.getLocatorType()
                .equalsIgnoreCase(
                        second.getLocatorType())

                && first.getLocatorValue()
                .equalsIgnoreCase(
                        second.getLocatorValue());
    }

    private double tokenSimilarity(
            String first,
            String second) {

        String normalizedFirst =
                normalize(first);

        String normalizedSecond =
                normalize(second);

        if (normalizedFirst.isBlank()
                || normalizedSecond.isBlank()) {

            return 0;
        }

        String[] tokens =
                normalizedFirst.split("\\s+");

        int meaningful = 0;
        int matched = 0;

        for (String token : tokens) {

            if (token.length() < 3
                    || isGenericToken(token)) {
                continue;
            }

            meaningful++;

            if (normalizedSecond.contains(token)) {
                matched++;
            }
        }

        if (meaningful == 0) {
            return 0;
        }

        return (double) matched / meaningful;
    }

    private double textSimilarity(
            String first,
            String second) {

        String normalizedFirst =
                normalize(first);

        String normalizedSecond =
                normalize(second);

        if (normalizedFirst.isBlank()
                || normalizedSecond.isBlank()) {

            return 0;
        }

        if (normalizedFirst.equals(normalizedSecond)
                || normalizedFirst.contains(normalizedSecond)
                || normalizedSecond.contains(normalizedFirst)) {

            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        normalizedFirst,
                        normalizedSecond);

        int maxLength =
                Math.max(
                        normalizedFirst.length(),
                        normalizedSecond.length());

        return maxLength == 0
                ? 1.0
                : 1.0
                - ((double) distance / maxLength);
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

    private boolean isGenericToken(
            String token) {

        return token.equals("button")
                || token.equals("input")
                || token.equals("field")
                || token.equals("link")
                || token.equals("text")
                || token.equals("element")
                || token.equals("locator");
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
}