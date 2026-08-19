package com.vinayak.healing.analytics;

public final class HealingAnalytics {

    private static final HealingMetrics METRICS =
            new HealingMetrics();

    private HealingAnalytics() {
    }

    // =====================================================
    // CACHE
    // =====================================================

    public static void cacheHit() {

        METRICS.setCacheHits(
                METRICS.getCacheHits() + 1);
    }

    public static void cacheMiss() {

        METRICS.setCacheMisses(
                METRICS.getCacheMisses() + 1);
    }

    // =====================================================
    // HEALING
    // =====================================================

    public static void deterministicHeal() {

        METRICS.setDeterministicHeals(
                METRICS.getDeterministicHeals() + 1);

        METRICS.setTotalHealingAttempts(
                METRICS.getTotalHealingAttempts() + 1);
    }

    public static void aiHeal() {

        METRICS.setAiHeals(
                METRICS.getAiHeals() + 1);

        METRICS.setTotalHealingAttempts(
                METRICS.getTotalHealingAttempts() + 1);
    }

    public static void validationFailure() {

        METRICS.setValidationFailures(
                METRICS.getValidationFailures() + 1);
    }

    public static void failure() {

        METRICS.setFailures(
                METRICS.getFailures() + 1);

        METRICS.setTotalHealingAttempts(
                METRICS.getTotalHealingAttempts() + 1);
    }

    public static void failure(String page) {

    failure();

    METRICS.recordPageFailure(page);
}

    // =====================================================
    // TIME
    // =====================================================

    public static void addHealingTime(
            long millis) {

        METRICS.setTotalHealingTime(
                METRICS.getTotalHealingTime() + millis);

        if (millis < METRICS.getMinimumHealingTime()) {

            METRICS.setMinimumHealingTime(
                    millis);
        }

        if (millis > METRICS.getMaximumHealingTime()) {

            METRICS.setMaximumHealingTime(
                    millis);
        }
    }
 public static void capabilityFailure() {

    METRICS.setCapabilityFailures(
            METRICS.getCapabilityFailures() + 1);
}
public static void duplicateResolution() {

    METRICS.setDuplicateResolutions(
            METRICS.getDuplicateResolutions() + 1);
}
public static void learningRecorded() {

    METRICS.setLearningRecords(
            METRICS.getLearningRecords() + 1);
}
public static void pageFailure(String page) {

    METRICS.recordPageFailure(page);
}
    // =====================================================
    // RESET
    // =====================================================

public static void reset() {

    METRICS.setCacheHits(0);
    METRICS.setCacheMisses(0);

    METRICS.setDeterministicHeals(0);
    METRICS.setAiHeals(0);

    METRICS.setValidationFailures(0);
    METRICS.setFailures(0);

    METRICS.setTotalHealingAttempts(0);

    METRICS.setTotalHealingTime(0);

    METRICS.setMinimumHealingTime(
            Long.MAX_VALUE);

    METRICS.setCapabilityFailures(0);

    METRICS.setDuplicateResolutions(0);

    METRICS.setLearningRecords(0);

    METRICS.clearPageFailures();

    METRICS.setMaximumHealingTime(0);
}

    // =====================================================
    // GET
    // =====================================================

    public static HealingMetrics getMetrics() {

        return METRICS;
    }

    // =====================================================
    // PRINT
    // =====================================================

    public static void printSummary() {

        HealingSummary.print(
                METRICS);
    }

}