package com.vinayak.healing.analysis;

import com.vinayak.healing.model.LocatorCandidate;

public class LocatorQualityAnalyzer {

    /*
     * =========================================================
     * PUBLIC API
     * =========================================================
     */

    /**
     * Returns the complete locator quality score.
     *
     * Kept for backward compatibility with existing framework
     * code.
     *
     * Range:
     * 0 - 390
     */
    public double calculateScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        return getLocatorTypeScore(candidate)
                + getStabilityScore(candidate)
                + getDescriptiveScore(candidate)
                + getUniquenessScore(candidate)
                + getLengthScore(candidate)
                + getGeneratedPatternScore(candidate);
    }

    /*
     * =========================================================
     * RANKING 2.0 COMPONENTS
     * =========================================================
     */

    /**
     * Locator type reliability.
     *
     * Range:
     * 0 - 100
     */
    public double getLocatorTypeScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        String type =
                candidate.getLocatorType();

        if (type == null
                || type.isBlank()) {

            return 50;
        }

        switch (type
                .trim()
                .toLowerCase()) {

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

            case "css":
            case "cssselector":
                return 55;

            case "tagname":
                return 40;

            case "linktext":
                return 50;

            case "partiallinktext":
                return 40;

            default:
                return 50;
        }
    }

    /**
     * Locator stability.
     *
     * Prefer the stability information already calculated
     * by DynamicAttributeDetector / LocatorCandidate.
     *
     * Range:
     * 0 - 100
     */
    public double getStabilityScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        double stability =
                candidate.getStabilityScore();

        return clamp(
                stability,
                0,
                100);
    }

    /**
     * Descriptiveness of locator value.
     *
     * Range:
     * 0 - 100
     */
    public double getDescriptiveScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        String value =
                candidate.getLocatorValue();

        if (value == null
                || value.isBlank()) {

            return 0;
        }

        String[] tokens =
                value
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
     * Locator uniqueness.
     *
     * Uses occurrence count when available.
     *
     * Range:
     * 0 - 100
     */
    public double getUniquenessScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        int occurrenceCount =
                candidate.getOccurrenceCount();

        if (occurrenceCount <= 1) {
            return 100;
        }

        if (occurrenceCount == 2) {
            return 50;
        }

        if (occurrenceCount <= 4) {
            return 25;
        }

        return 0;
    }

    /**
     * Locator value length.
     *
     * Range:
     * 0 - 100
     */
    public double getLengthScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        String value =
                candidate.getLocatorValue();

        if (value == null
                || value.isBlank()) {

            return 0;
        }

        int length =
                value.length();

        if (length > 25) {
            return 100;
        }

        if (length > 15) {
            return 75;
        }

        if (length > 8) {
            return 50;
        }

        return 25;
    }

    /**
     * Generated-pattern reliability.
     *
     * This method converts the existing generated-pattern
     * penalties into a normalized reliability signal.
     *
     * Range:
     * 0 - 100
     */
    public double getGeneratedPatternScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        String value =
                candidate.getLocatorValue();

        if (value == null
                || value.isBlank()) {

            return 0;
        }

        double score = 100;

        /*
         * Numeric suffix.
         */
        if (value.matches(
                ".*_[0-9]{4,}.*")) {

            score -= 40;
        }

        /*
         * Large numeric suffix.
         */
        if (value.matches(
                ".*-[0-9]{5,}.*")) {

            score -= 40;
        }

        /*
         * Generated framework pattern.
         */
        if (value.matches(
                ".*\\$\\$.*")) {

            score -= 30;
        }

        return clamp(
                score,
                0,
                100);
    }

    /*
     * =========================================================
     * RANKING 2.0 RELIABILITY SCORE
     * =========================================================
     */

    /**
     * Calculates the normalized reliability score used by
     * CandidateRanker 2.0.
     *
     * Formula:
     *
     * Uniqueness          30%
     * Stability            25%
     * Descriptiveness      20%
     * Generation           10%
     * Locator Type          5%
     * Length               10%
     *
     * Range:
     * 0 - 100
     */
    public double calculateReliabilityScore(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return 0;
        }

        double uniqueness =
                getUniquenessScore(candidate);

        double stability =
                getStabilityScore(candidate);

        double descriptive =
                getDescriptiveScore(candidate);

        double generation =
                clamp(
                        candidate.getGenerationConfidence(),
                        0,
                        100);

        double locatorType =
                getLocatorTypeScore(candidate);

        double length =
                getLengthScore(candidate);

        double score =
                (uniqueness * 0.30)
                + (stability * 0.25)
                + (descriptive * 0.20)
                + (generation * 0.10)
                + (locatorType * 0.05)
                + (length * 0.10);

        return clamp(
                score,
                0,
                100);
    }

    /*
     * =========================================================
     * UTILITY
     * =========================================================
     */

    private double clamp(
            double value,
            double min,
            double max) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value));
    }
}