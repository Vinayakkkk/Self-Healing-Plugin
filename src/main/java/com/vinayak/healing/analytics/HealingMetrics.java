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
// Intelligence Statistics
// ======================================================

private long capabilityFailures;

private long duplicateResolutions;

private long learningRecords;

private final java.util.Map<String, Long> pageFailures =
        new java.util.LinkedHashMap<>();

    // ======================================================
    // Timing Statistics
    // ======================================================

    private long totalHealingTime;

    private long minimumHealingTime = Long.MAX_VALUE;

    private long maximumHealingTime = 0;

    // ======================================================
    // GETTERS & SETTERS
    // ======================================================

    public long getCapabilityFailures() {
    return capabilityFailures;
}

public void setCapabilityFailures(long capabilityFailures) {
    this.capabilityFailures = capabilityFailures;
}

public long getDuplicateResolutions() {
    return duplicateResolutions;
}

public void setDuplicateResolutions(long duplicateResolutions) {
    this.duplicateResolutions = duplicateResolutions;
}

public long getLearningRecords() {
    return learningRecords;
}

public void setLearningRecords(long learningRecords) {
    this.learningRecords = learningRecords;
}

public java.util.Map<String, Long> getPageFailures() {
    return java.util.Collections.unmodifiableMap(pageFailures);
}

public void recordPageFailure(String page) {

    if (page == null || page.isBlank()) {
        page = "UNKNOWN";
    }

    pageFailures.merge(
            page.trim(),
            1L,
            Long::sum);
}

public void clearPageFailures() {
    pageFailures.clear();
}
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

    public double getAiHealingRate() {

    if (totalHealingAttempts == 0) {
        return 0;
    }

    return ((double) aiHeals
            / totalHealingAttempts) * 100;
}

public double getDuplicateRate() {

    if (totalHealingAttempts == 0) {
        return 0;
    }

    return ((double) duplicateResolutions
            / totalHealingAttempts) * 100;
}

public double getLearningRate() {

    if (totalHealingAttempts == 0) {
        return 0;
    }

    return ((double) learningRecords
            / totalHealingAttempts) * 100;
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
            ", capabilityFailures=" + capabilityFailures +
            ", duplicateResolutions=" + duplicateResolutions +
            ", learningRecords=" + learningRecords +
            ", pageFailures=" + pageFailures +
            ", totalHealingTime=" + totalHealingTime +
            ", minimumHealingTime=" + minimumHealingTime +
            ", maximumHealingTime=" + maximumHealingTime +
            '}';
}
}