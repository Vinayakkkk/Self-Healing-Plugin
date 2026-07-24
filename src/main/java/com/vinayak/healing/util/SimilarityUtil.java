package com.vinayak.healing.util;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SimilarityUtil {

    private SimilarityUtil() {
    }

    private static final Set<String> GENERIC_VALUES = Set.of(
            "html",
            "head",
            "body",
            "div",
            "span",
            "section",
            "article",
            "main",
            "form",
            "label",
            "button",
            "input",
            "textarea",
            "select",
            "svg",
            "path",
            "ul",
            "ol",
            "li",
            "table",
            "tbody",
            "tr",
            "td",
            "th",
            "p"
    );

    public static double score(
            String failedLocator,
            String candidate) {

        if (failedLocator == null
                || candidate == null
                || candidate.isBlank()) {

            return 0;
        }

        failedLocator = normalize(failedLocator);
        candidate = normalize(candidate);

        if (GENERIC_VALUES.contains(candidate)) {
            return 0;
        }

        // ---------------------------------------
        // Exact Match
        // ---------------------------------------

        if (failedLocator.equals(candidate)) {
            return 300;
        }

        Set<String> failedTokens =
                tokenize(failedLocator);

        Set<String> candidateTokens =
                tokenize(candidate);

        if (failedTokens.isEmpty()
                || candidateTokens.isEmpty()) {

            return 0;
        }

        int exactMatches = 0;
        int prefixMatches = 0;

        for (String f : failedTokens) {

            for (String c : candidateTokens) {

                if (f.equals(c)) {

                    exactMatches++;

                } else if (c.startsWith(f)
                        || f.startsWith(c)) {

                    prefixMatches++;
                }
            }
        }

        double identity =
                (double) exactMatches
                        / failedTokens.size();

        double score = 0;

        // ---------------------------------------
        // Identity Score
        // ---------------------------------------

        score += identity * 200;

        // ---------------------------------------
        // Prefix Score
        // ---------------------------------------

        score += prefixMatches * 20;

        // ---------------------------------------
        // Levenshtein (only if meaningful)
        // ---------------------------------------

        int distance =
                levenshtein(
                        failedLocator,
                        candidate);

        int max =
                Math.max(
                        failedLocator.length(),
                        candidate.length());

        double similarity =
                ((double) (max - distance)
                        / max) * 100;

        if (similarity > 40) {

            score += similarity * 0.5;
        }

        return score;
    }

    private static Set<String> tokenize(
            String value) {

        value = value.replaceAll(
                "([a-z])([A-Z])",
                "$1 $2");

        value = value
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('.', ' ')
                .replace('/', ' ')
                .replace(':', ' ')
                .replaceAll(
                        "[\\[\\](){}'\"`]",
                        " ")
                .replaceAll(
                        "[=@*,;><]",
                        " ")
                .replaceAll(
                        "\\s+",
                        " ")
                .trim()
                .toLowerCase();

        String[] parts =
                value.split("\\s+");

        Set<String> tokens =
                new LinkedHashSet<>();

        for (String part : parts) {

            if (part.length() < 3) {
                continue;
            }

            tokens.add(part);
        }

        return tokens;
    }

    private static String normalize(
            String value) {

        return value

                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2")

                .replaceAll(
                        "by\\.[a-z]+:",
                        "")

                .replaceAll(
                        "[\\[\\](){}'\"`]",
                        " ")

                .replaceAll(
                        "[=@*,;><]",
                        " ")

                .replaceAll(
                        "\\s+",
                        " ")

                .trim()

                .toLowerCase();
    }

    private static int levenshtein(
            String a,
            String b) {

        int[][] dp =
                new int[a.length() + 1]
                        [b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {

            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {

            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {

            for (int j = 1; j <= b.length(); j++) {

                int cost =
                        a.charAt(i - 1)
                                == b.charAt(j - 1)
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

        return dp[a.length()][b.length()];
    }

}