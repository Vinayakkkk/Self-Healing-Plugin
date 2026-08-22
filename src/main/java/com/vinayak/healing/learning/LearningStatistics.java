package com.vinayak.healing.learning;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LearningStatistics {

    /*
     * =====================================================
     * OVERALL CONTEXT STATISTICS
     * =====================================================
     */

    private int attemptCount;
    private int successCount;
    private int failureCount;

    private double totalScore;

    /*
     * =====================================================
     * LOCATOR-LEVEL STATISTICS
     * =====================================================
     *
     * Keeps historical statistics separately for each
     * locator type + locator value.
     *
     * Example:
     *
     * xpath|//input[@name='username']
     * id|username
     * css|[data-test='username']
     */
    private final Map<String, LocatorStatistics>
            locatorStatistics =
            new HashMap<>();

    public LearningStatistics() {
    }

    /**
     * Adds one learning record to the statistics.
     *
     * Existing overall statistics are preserved.
     *
     * The same record is also aggregated at the
     * locator level.
     */
    public void record(
            LearningRecord record) {

        if (record == null) {
            return;
        }

        /*
         * =================================================
         * OVERALL STATISTICS
         * =================================================
         */

        attemptCount++;

        if (record.isOutcomeSuccess()) {
            successCount++;
        } else {
            failureCount++;
        }

        totalScore += record.getCandidateScore();

        /*
         * =================================================
         * LOCATOR STATISTICS
         * =================================================
         */

        String locatorKey =
                buildLocatorKey(record);

        locatorStatistics
                .computeIfAbsent(
                        locatorKey,
                        key -> new LocatorStatistics(
                                record.getSelectedLocatorType(),
                                record.getSelectedLocatorValue()))
                .record(record);
    }

    // =====================================================
    // OVERALL GETTERS
    // =====================================================

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    /**
     * Returns historical success rate as a percentage.
     */
    public double getSuccessRate() {

        if (attemptCount == 0) {
            return 0.0;
        }

        return (successCount * 100.0)
                / attemptCount;
    }

    /**
     * Returns the average candidate score
     * across recorded healing attempts.
     */
    public double getAverageScore() {

        if (attemptCount == 0) {
            return 0.0;
        }

        return totalScore / attemptCount;
    }

    public double getTotalScore() {
        return totalScore;
    }

    // =====================================================
    // LOCATOR STATISTICS
    // =====================================================

    /**
     * Returns statistics for a specific locator.
     *
     * Example:
     *
     * getLocatorStatistics(
     *     "xpath",
     *     "//input[@name='username']");
     */
    public LocatorStatistics getLocatorStatistics(
            String locatorType,
            String locatorValue) {

        String key =
                buildLocatorKey(
                        locatorType,
                        locatorValue);

        return locatorStatistics.get(key);
    }

    /**
     * Returns all locator-level statistics.
     *
     * The returned map cannot be modified by the caller.
     */
    public Map<String, LocatorStatistics>
    getLocatorStatistics() {

        return Collections.unmodifiableMap(
                locatorStatistics);
    }

    /**
     * Returns the number of distinct locators
     * observed for this learning context.
     */
    public int getDistinctLocatorCount() {

        return locatorStatistics.size();
    }

    // =====================================================
    // LOCATOR KEY
    // =====================================================

    private String buildLocatorKey(
            LearningRecord record) {

        if (record == null) {
            return "UNKNOWN|UNKNOWN";
        }

        return buildLocatorKey(
                record.getSelectedLocatorType(),
                record.getSelectedLocatorValue());
    }

    private String buildLocatorKey(
            String locatorType,
            String locatorValue) {

        return normalize(locatorType)
                + "|"
                + normalize(locatorValue);
    }

    private String normalize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "UNKNOWN";
        }

        return value.trim()
                .toLowerCase();
    }

    // =====================================================
    // TO STRING
    // =====================================================

    @Override
    public String toString() {

        return "LearningStatistics{" +
                "attemptCount=" +
                attemptCount +
                ", successCount=" +
                successCount +
                ", failureCount=" +
                failureCount +
                ", successRate=" +
                getSuccessRate() +
                ", averageScore=" +
                getAverageScore() +
                ", distinctLocatorCount=" +
                getDistinctLocatorCount() +
                '}';
    }

    // =====================================================
// LOCATOR STATISTICS MODEL
// =====================================================

public enum LearningMaturity {
    NEW,
    OBSERVED,
    REPEATED,
    STABLE
}

    // =====================================================
    // LOCATOR STATISTICS MODEL
    // =====================================================

    /**
     * Historical statistics for one specific locator.
     *
     * This is intentionally kept inside LearningStatistics
     * so we do not introduce another framework layer/class.
     */
    public static final class LocatorStatistics {

        private final String locatorType;
        private final String locatorValue;

        private int attemptCount;
        private int successCount;
        private int failureCount;

        private double totalScore;
        private double totalOutcomeConfidence;

        private LocatorStatistics(
                String locatorType,
                String locatorValue) {

            this.locatorType =
                    normalizeValue(locatorType);

            this.locatorValue =
                    normalizeValue(locatorValue);
        }

        /**
         * Adds one historical record.
         */
        private void record(
                LearningRecord record) {

            if (record == null) {
                return;
            }

            attemptCount++;

            if (record.isOutcomeSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }

            totalScore +=
                    record.getCandidateScore();

            totalOutcomeConfidence +=
                    record.getOutcomeConfidence();
        }

        public String getLocatorType() {
            return locatorType;
        }

        public String getLocatorValue() {
            return locatorValue;
        }

        public int getAttemptCount() {
            return attemptCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        /**
         * Returns historical success rate
         * for this specific locator.
         */
        public double getSuccessRate() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return (successCount * 100.0)
                    / attemptCount;
        }

        /**
         * Returns average candidate score
         * for this locator.
         */
        public double getAverageScore() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return totalScore
                    / attemptCount;
        }

        /**
         * Returns average outcome confidence
         * for this locator.
         */
        public double getAverageOutcomeConfidence() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return totalOutcomeConfidence
                    / attemptCount;
        }

        /**
         * Returns a bounded reliability value
         * between 0.0 and 1.0.
         */
        public double getReliability() {

            if (attemptCount == 0) {
                return 0.0;
            }

            return successCount
                    / (double) attemptCount;
        }

        /**
 * Returns the current learning maturity of this locator.
 *
 * Maturity is based on successful historical outcomes.
 *
 * 0 successes  -> NEW
 * 1 success    -> OBSERVED
 * 2 successes  -> REPEATED
 * 3+ successes -> STABLE
 */
public LearningMaturity getLearningMaturity() {

    if (successCount == 0) {
        return LearningMaturity.NEW;
    }

    if (successCount == 1) {
        return LearningMaturity.OBSERVED;
    }

    if (successCount == 2) {
        return LearningMaturity.REPEATED;
    }

    return LearningMaturity.STABLE;
}

        /**
         * Returns total candidate score accumulated
         * for this locator.
         */
        public double getTotalScore() {
            return totalScore;
        }

        /**
         * Returns total historical outcome confidence.
         */
        public double getTotalOutcomeConfidence() {
            return totalOutcomeConfidence;
        }

        @Override
        public String toString() {

            return "LocatorStatistics{" +
                    "locatorType='" +
                    locatorType + '\'' +
                    ", locatorValue='" +
                    locatorValue + '\'' +
                    ", attemptCount=" +
                    attemptCount +
                    ", successCount=" +
                    successCount +
                    ", failureCount=" +
                    failureCount +
                    ", successRate=" +
                    getSuccessRate() +
                    ", reliability=" +
                    getReliability() +
                    ", learningMaturity=" +
getLearningMaturity() +
                    ", averageScore=" +
                    getAverageScore() +
                    ", averageOutcomeConfidence=" +
                    getAverageOutcomeConfidence() +
                    '}';
        }

        private static String normalizeValue(
                String value) {

            if (value == null
                    || value.isBlank()) {

                return "UNKNOWN";
            }

            return value.trim();
        }
    }
}