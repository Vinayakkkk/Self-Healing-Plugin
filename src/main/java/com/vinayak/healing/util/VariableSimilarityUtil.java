package com.vinayak.healing.util;

public class VariableSimilarityUtil {

    public static double score(
            String variableName,
            String candidateValue) {

        if(variableName == null
                || variableName.isBlank()) {

            return 0;
        }

        String normalizedVariable =
                normalize(variableName);

        String normalizedCandidate =
                normalize(candidateValue);

        String[] variableTokens =
                normalizedVariable.split("\\s+");

        String[] candidateTokens =
                normalizedCandidate.split("\\s+");

        int matches = 0;

        for(String variableToken : variableTokens) {

            for(String candidateToken : candidateTokens) {

                if(variableToken.equals(
                        candidateToken)) {

                    matches++;
                }
            }
        }

        return matches * 20;
    }

    private static String normalize(
            String value) {

        return value
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2")
                .replace("-", " ")
                .replace("_", " ")
                .toLowerCase()
                .trim();
    }
}
