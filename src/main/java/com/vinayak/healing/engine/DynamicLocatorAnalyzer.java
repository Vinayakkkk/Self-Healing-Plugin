package com.vinayak.healing.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DynamicLocatorAnalyzer {

    private static final Set<String> STOP_WORDS =
            Set.of(
                    "by",
                    "id",
                    "name",
                    "css",
                    "cssselector",
                    "xpath",
                    "class",
                    "classname",

                    "add",
                    "remove",
                    "click",
                    "button",
                    "btn",

                    "old",
                    "new",

                    "data",
                    "test",
                    "testid",
                    "qa",
                    "cy"
            );

    private static final Pattern SPLIT_PATTERN =
            Pattern.compile("[^a-zA-Z0-9]+");

    public List<String> extractTokens(
            String locatorValue) {

        if (locatorValue == null
                || locatorValue.isBlank()) {

            return Collections.emptyList();
        }

        List<String> tokens =
                new ArrayList<>();

        String[] parts =
                SPLIT_PATTERN.split(
                        locatorValue.toLowerCase());

        for (String part : parts) {

            if (part == null
                    || part.isBlank()) {

                continue;
            }

            if (part.length() <= 2) {

                continue;
            }

            if (STOP_WORDS.contains(part)) {

                continue;
            }

            tokens.add(part);
        }

        return tokens;
    }

    public double calculateDynamicScore(
            String failedLocator,
            String candidateLocator) {

        Set<String> failedTokens =
                new HashSet<>(
                        extractTokens(
                                failedLocator));

        Set<String> candidateTokens =
                new HashSet<>(
                        extractTokens(
                                candidateLocator));

        if (failedTokens.isEmpty()
                || candidateTokens.isEmpty()) {

            return 0;
        }

        int exactMatches = 0;

        for (String failedToken
                : failedTokens) {

            if (candidateTokens.contains(
                    failedToken)) {

                exactMatches++;
            }
        }

        double score = 0;

        // =====================================
        // EXACT TOKEN MATCH
        // =====================================

        score += exactMatches * 250;

        // =====================================
        // IDENTITY COVERAGE
        // =====================================

        double coverage =
                (double) exactMatches
                        / failedTokens.size();

        if (coverage >= 1.0) {

            score += 1200;

        } else if (coverage >= 0.75) {

            score += 600;

        } else if (coverage >= 0.50) {

            score += 200;
        }

        // =====================================
        // MISSING IDENTITY PENALTY
        // =====================================

        int missingTokens =
                failedTokens.size()
                        - exactMatches;

        /*
         * This prevents candidates that share only generic
         * surrounding words from beating the real target.
         */
        score -= missingTokens * 150;

        // =====================================
        // FUZZY MATCH
        // =====================================

        for (String failedToken
                : failedTokens) {

            if (candidateTokens.contains(
                    failedToken)) {

                continue;
            }

            double bestSimilarity = 0;

            for (String candidateToken
                    : candidateTokens) {

                double similarity =
                        similarity(
                                failedToken,
                                candidateToken);

                bestSimilarity =
                        Math.max(
                                bestSimilarity,
                                similarity);
            }

            if (bestSimilarity >= 0.85) {

                score += 50;

            } else if (bestSimilarity >= 0.70) {

                score += 20;
            }
        }

        return score;
    }

    private double similarity(
            String left,
            String right) {

        int distance =
                levenshtein(
                        left,
                        right);

        int max =
                Math.max(
                        left.length(),
                        right.length());

        if (max == 0) {
            return 1.0;
        }

        return 1.0
                - ((double) distance / max);
    }

    private int levenshtein(
            String left,
            String right) {

        int[][] dp =
                new int[left.length() + 1]
                        [right.length() + 1];

        for (int i = 0;
             i <= left.length();
             i++) {

            dp[i][0] = i;
        }

        for (int j = 0;
             j <= right.length();
             j++) {

            dp[0][j] = j;
        }

        for (int i = 1;
             i <= left.length();
             i++) {

            for (int j = 1;
                 j <= right.length();
                 j++) {

                int cost =
                        left.charAt(i - 1)
                                == right.charAt(j - 1)
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

        return dp[left.length()]
                [right.length()];
    }
}