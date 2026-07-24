package com.vinayak.healing.analytics;

public class HealingMetrics {

    // ======================================================
    // Cache Statistics
    // ======================================================

    private long cacheHits;

    private long cacheMisses;

    // ======================================================
    // Healing Statistics
    // ======================================================

    private long deterministicHeals;

    private long aiHeals;

    private long validationFailures;

    private long failures;

    private long totalHealingAttempts;

    // ======================================================
    // Timing Statistics
    // ======================================================

    private long totalHealingTime;

    private long minimumHealingTime = Long.MAX_VALUE;

    private long maximumHealingTime = 0;

    // ======================================================
    // GETTERS & SETTERS
    // ======================================================

    public long getCacheHits() {
        return cacheHits;
    }

    public void setCacheHits(long cacheHits) {
        this.cacheHits = cacheHits;
    }

    public long getCacheMisses() {
        return cacheMisses;
    }

    public void setCacheMisses(long cacheMisses) {
        this.cacheMisses = cacheMisses;
    }

    public long getDeterministicHeals() {
        return deterministicHeals;
    }

    public void setDeterministicHeals(long deterministicHeals) {
        this.deterministicHeals = deterministicHeals;
    }

    public long getAiHeals() {
        return aiHeals;
    }

    public void setAiHeals(long aiHeals) {
        this.aiHeals = aiHeals;
    }

    public long getValidationFailures() {
        return validationFailures;
    }

    public void setValidationFailures(long validationFailures) {
        this.validationFailures = validationFailures;
    }

    public long getFailures() {
        return failures;
    }

    public void setFailures(long failures) {
        this.failures = failures;
    }

    public long getTotalHealingAttempts() {
        return totalHealingAttempts;
    }

    public void setTotalHealingAttempts(long totalHealingAttempts) {
        this.totalHealingAttempts = totalHealingAttempts;
    }

    public long getTotalHealingTime() {
        return totalHealingTime;
    }

    public void setTotalHealingTime(long totalHealingTime) {
        this.totalHealingTime = totalHealingTime;
    }

    public long getMinimumHealingTime() {
        return minimumHealingTime;
    }

    public void setMinimumHealingTime(long minimumHealingTime) {
        this.minimumHealingTime = minimumHealingTime;
    }

    public long getMaximumHealingTime() {
        return maximumHealingTime;
    }

    public void setMaximumHealingTime(long maximumHealingTime) {
        this.maximumHealingTime = maximumHealingTime;
    }

    // ======================================================
    // DERIVED METRICS
    // ======================================================

    public long getSuccessfulHeals() {

        return deterministicHeals + aiHeals;
    }

    public double getAverageHealingTime() {

        if (getSuccessfulHeals() == 0) {
            return 0;
        }

        return (double) totalHealingTime
                / getSuccessfulHeals();
    }

    public double getSuccessRate() {

        if (totalHealingAttempts == 0) {
            return 0;
        }

        return ((double) getSuccessfulHeals()
                / totalHealingAttempts) * 100;
    }

    public double getCacheHitRate() {

        long total = cacheHits + cacheMisses;

        if (total == 0) {
            return 0;
        }

        return ((double) cacheHits / total) * 100;
    }

    @Override
    public String toString() {

        return "HealingMetrics{" +
                "cacheHits=" + cacheHits +
                ", cacheMisses=" + cacheMisses +
                ", deterministicHeals=" + deterministicHeals +
                ", aiHeals=" + aiHeals +
                ", validationFailures=" + validationFailures +
                ", failures=" + failures +
                ", totalHealingAttempts=" + totalHealingAttempts +
                ", totalHealingTime=" + totalHealingTime +
                ", minimumHealingTime=" + minimumHealingTime +
                ", maximumHealingTime=" + maximumHealingTime +
                '}';
    }
}