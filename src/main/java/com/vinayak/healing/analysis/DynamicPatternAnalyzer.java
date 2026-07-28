package com.vinayak.healing.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class DynamicPatternAnalyzer {

    private static final Pattern NUMERIC_PATTERN =
            Pattern.compile("^(.*?)(\\d+)(.*?)$");

    public double calculateDynamicScore(
            FailureContext context,
            LocatorCandidate candidate) {

        if (context == null || candidate == null) {
            return 0;
        }

        double score = 0;

        score += scoreLocatorValue(context, candidate);
        score += scoreId(context, candidate);
        score += scoreName(context, candidate);

        return score;
    }

    private double scoreLocatorValue(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getLocatorValue(),
                300);
    }

    private double scoreId(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getId(),
                250);
    }

    private double scoreName(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getName(),
                220);
    }

    private double scorePattern(
            String failedLocator,
            String candidateValue,
            double score) {

        if (failedLocator == null || candidateValue == null) {
            return 0;
        }

        String failed =
                normalize(
                        extractLocatorValue(
                                failedLocator));

        String candidate =
                normalize(candidateValue);

        if (failed.isBlank()
                || candidate.isBlank()) {
            return 0;
        }

        if (failed.equals(candidate)) {
            return 0;
        }

        if (isSameNumericPattern(failed, candidate)) {
            return score;
        }

        return 0;
    }

    private boolean isSameNumericPattern(
            String left,
            String right) {

        Matcher leftMatcher =
                NUMERIC_PATTERN.matcher(left);

        Matcher rightMatcher =
                NUMERIC_PATTERN.matcher(right);

        if (!leftMatcher.matches()
                || !rightMatcher.matches()) {
            return false;
        }

        String leftPrefix = leftMatcher.group(1);
        String leftSuffix = leftMatcher.group(3);

        String rightPrefix = rightMatcher.group(1);
        String rightSuffix = rightMatcher.group(3);

        return leftPrefix.equals(rightPrefix)
                && leftSuffix.equals(rightSuffix);
    }

    private String extractLocatorValue(
            String locator) {

        int index = locator.lastIndexOf(":");

        if (index != -1) {
            locator = locator.substring(index + 1);
        }

        return locator.trim();
    }

    private String normalize(String value) {

        return value
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}