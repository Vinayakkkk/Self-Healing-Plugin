package com.vinayak.healing.analysis;

import com.vinayak.healing.model.LocatorCandidate;

public class LocatorQualityAnalyzer {

    public double calculateScore(LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        double score = 0;

        score += getLocatorTypeWeight(candidate);
        score += calculateStabilityScore(candidate);
        score += calculateDescriptiveScore(candidate);
        score += calculateUniquenessScore(candidate);
        score += calculateLengthScore(candidate);
        score += calculateGeneratedPatternPenalty(candidate);

        return score;
    }

    /**
     * Different locator types have different reliability.
     * Keep the gap small so this doesn't dominate ranking.
     */
    private double getLocatorTypeWeight(
            LocatorCandidate candidate) {

        String type = candidate.getLocatorType();

        if (type == null) {
            return 0;
        }

        switch (type.toLowerCase()) {

            case "data-testid":
                return 100;

            case "data-test":
                return 95;

            case "data-qa":
                return 95;

            case "data-cy":
                return 95;

            case "id":
                return 90;

            case "name":
                return 85;

            case "class":
                return 80;

            case "aria-label":
                return 75;

            case "placeholder":
                return 70;

            case "xpath":
                return 60;

            default:
                return 50;
        }
    }

    /**
     * Penalize obviously unstable/generated values.
     */
    private double calculateStabilityScore(
            LocatorCandidate candidate) {

        String value = candidate.getLocatorValue();

        if (value == null || value.isBlank()) {
            return 0;
        }

        double score = 100;

        // long numeric sequences
        if (value.matches(".*\\d{5,}.*")) {
            score -= 40;
        }

        // long hex/hash strings
        if (value.matches(".*[a-fA-F0-9]{8,}.*")) {
            score -= 40;
        }

        // Angular generated classes
        if (value.matches(".*ng-tns.*")) {
            score -= 30;
        }

        // CSS-in-JS generated classes
        if (value.matches(".*css-[a-zA-Z0-9]+.*")) {
            score -= 30;
        }

        // React/MUI style hashes
        if (value.matches(".*__[A-Za-z0-9]+.*")) {
            score -= 20;
        }

        return Math.max(score, 0);
    }

    /**
     * Reward descriptive locator values.
     */
    private double calculateDescriptiveScore(
            LocatorCandidate candidate) {

        String value = candidate.getLocatorValue();

        if (value == null || value.isBlank()) {
            return 0;
        }

        String[] tokens = value
                .replace('-', ' ')
                .replace('_', ' ')
                .trim()
                .split("\\s+");

        if (tokens.length >= 4) {
            return 100;
        }

        if (tokens.length == 3) {
            return 80;
        }

        if (tokens.length == 2) {
            return 60;
        }

        return 20;
    }

    /**
     * Unique locators are more reliable.
     */
    private double calculateUniquenessScore(
            LocatorCandidate candidate) {

        return candidate.isUniqueLocator()
                ? 50
                : 0;
    }

    /**
     * Longer locator values are usually more descriptive.
     */
    private double calculateLengthScore(
            LocatorCandidate candidate) {

        String value = candidate.getLocatorValue();

        if (value == null) {
            return 0;
        }

        int length = value.length();

        if (length > 25) {
            return 40;
        }

        if (length > 15) {
            return 30;
        }

        if (length > 8) {
            return 20;
        }

        return 10;
    }

    /**
     * Penalize generated patterns.
     */
    private double calculateGeneratedPatternPenalty(
            LocatorCandidate candidate) {

        String value = candidate.getLocatorValue();

        if (value == null) {
            return 0;
        }

        if (value.matches(".*_[0-9]{4,}.*")) {
            return -40;
        }

        if (value.matches(".*-[0-9]{5,}.*")) {
            return -40;
        }

        if (value.matches(".*\\$\\$.*")) {
            return -30;
        }

        return 0;
    }
}