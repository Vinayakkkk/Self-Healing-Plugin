package com.vinayak.healing.learning;

import java.time.LocalDateTime;

public class LearningRecord {

    private final LearningKey learningKey;

    private final String selectedLocator;
    private final String selectedLocatorType;
    private final String selectedLocatorValue;

    private final double candidateScore;

    private final String healingSource;
    private final String confidenceLevel;

    private final boolean healingAllowed;
    private final boolean cacheAllowed;

    private final boolean outcomeSuccess;
    private final double outcomeConfidence;

    private final LocalDateTime timestamp;

    /**
     * Creates a new learning record from the current healing execution.
     *
     * The timestamp is automatically generated for new records.
     */
    public LearningRecord(
            LearningKey learningKey,
            String selectedLocator,
            String selectedLocatorType,
            String selectedLocatorValue,
            double candidateScore,
            String healingSource,
            String confidenceLevel,
            boolean healingAllowed,
            boolean cacheAllowed,
            boolean outcomeSuccess,
            double outcomeConfidence) {

        this(
                learningKey,
                selectedLocator,
                selectedLocatorType,
                selectedLocatorValue,
                candidateScore,
                healingSource,
                confidenceLevel,
                healingAllowed,
                cacheAllowed,
                outcomeSuccess,
                outcomeConfidence,
                LocalDateTime.now()
        );
    }

    /**
     * Creates a learning record with an explicit timestamp.
     *
     * Used when restoring historical learning records
     * from persistent storage.
     */
    public LearningRecord(
            LearningKey learningKey,
            String selectedLocator,
            String selectedLocatorType,
            String selectedLocatorValue,
            double candidateScore,
            String healingSource,
            String confidenceLevel,
            boolean healingAllowed,
            boolean cacheAllowed,
            boolean outcomeSuccess,
            double outcomeConfidence,
            LocalDateTime timestamp) {

        this.learningKey = learningKey;

        this.selectedLocator = selectedLocator;
        this.selectedLocatorType = selectedLocatorType;
        this.selectedLocatorValue = selectedLocatorValue;

        this.candidateScore = candidateScore;

        this.healingSource = healingSource;
        this.confidenceLevel = confidenceLevel;

        this.healingAllowed = healingAllowed;
        this.cacheAllowed = cacheAllowed;

        this.outcomeSuccess = outcomeSuccess;
        this.outcomeConfidence = outcomeConfidence;

        /*
         * Preserve the supplied timestamp when loading
         * historical records.
         *
         * For safety, if no timestamp is supplied,
         * create one for the current execution.
         */
        this.timestamp =
                timestamp == null
                        ? LocalDateTime.now()
                        : timestamp;
    }

    public LearningKey getLearningKey() {
        return learningKey;
    }

    public String getSelectedLocator() {
        return selectedLocator;
    }

    public String getSelectedLocatorType() {
        return selectedLocatorType;
    }

    public String getSelectedLocatorValue() {
        return selectedLocatorValue;
    }

    public double getCandidateScore() {
        return candidateScore;
    }

    public String getHealingSource() {
        return healingSource;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public boolean isHealingAllowed() {
        return healingAllowed;
    }

    public boolean isCacheAllowed() {
        return cacheAllowed;
    }

    public boolean isOutcomeSuccess() {
        return outcomeSuccess;
    }

    public double getOutcomeConfidence() {
        return outcomeConfidence;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {

        return "LearningRecord{" +
                "learningKey=" + learningKey +
                ", selectedLocator='" +
                selectedLocator + '\'' +
                ", selectedLocatorType='" +
                selectedLocatorType + '\'' +
                ", selectedLocatorValue='" +
                selectedLocatorValue + '\'' +
                ", candidateScore=" +
                candidateScore +
                ", healingSource='" +
                healingSource + '\'' +
                ", confidenceLevel='" +
                confidenceLevel + '\'' +
                ", healingAllowed=" +
                healingAllowed +
                ", cacheAllowed=" +
                cacheAllowed +
                ", outcomeSuccess=" +
                outcomeSuccess +
                ", outcomeConfidence=" +
                outcomeConfidence +
                ", timestamp=" +
                timestamp +
                '}';
    }
}