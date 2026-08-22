package com.vinayak.healing.learning;

import java.util.Comparator;
import java.util.List;

public class LearningEngine {

    private final LearningRepository repository;

    public LearningEngine() {
        this.repository =
                LearningRepository.getInstance();
    }

    public LearningEngine(
            LearningRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException(
                    "LearningRepository cannot be null.");
        }

        this.repository = repository;
    }

    /**
     * Records one healing experience.
     */
    public void record(
            LearningRecord record) {

        if (record == null) {
            return;
        }

        repository.record(record);
    }

    /**
     * Returns historical healing records
     * for the supplied learning context.
     */
    public List<LearningRecord> findHistory(
            LearningKey key) {

        return repository.find(key);
    }

    /**
     * Returns the best historical learning record
     * for the supplied learning context.
     *
     * A learning record is eligible only when:
     *
     * - healing was successful
     * - healing was allowed
     * - confidence is MEDIUM or HIGH
     *
     * Among eligible records, the most reliable record
     * is selected using:
     *
     * 1. outcome confidence
     * 2. candidate score
     * 3. recency
     */
    public LearningRecord findBestLearning(
            LearningKey key) {

        if (key == null) {
            return null;
        }

        List<LearningRecord> history =
                repository.find(key);

        if (history == null
                || history.isEmpty()) {

            return null;
        }

        return history.stream()

                /*
                 * Only successful learning can
                 * participate in future reuse.
                 */
                .filter(LearningRecord::isOutcomeSuccess)

                /*
                 * Respect the original learning
                 * safety decision.
                 */
                .filter(LearningRecord::isHealingAllowed)

                /*
                 * LOW / UNKNOWN learning must not
                 * become trusted historical knowledge.
                 */
                .filter(record ->
                        isTrustedConfidence(
                                record.getConfidenceLevel()))

                /*
                 * Highest outcome confidence first.
                 */
                .max(
                        Comparator
                                .comparingDouble(
                                        LearningRecord::getOutcomeConfidence)

                                /*
                                 * If outcome confidence is equal,
                                 * prefer the stronger candidate.
                                 */
                                .thenComparingDouble(
                                        LearningRecord::getCandidateScore)

                                /*
                                 * If everything else is equal,
                                 * prefer the newest record.
                                 */
                                .thenComparing(
                                        LearningRecord::getTimestamp,
                                        Comparator.nullsFirst(
                                                Comparator.naturalOrder()))
                )

                .orElse(null);
    }

    /**
     * Determines whether a confidence level is
     * trusted for historical learning reuse.
     */
    private boolean isTrustedConfidence(
            String confidenceLevel) {

        if (confidenceLevel == null) {
            return false;
        }

        return "HIGH".equalsIgnoreCase(
                confidenceLevel.trim())

                || "MEDIUM".equalsIgnoreCase(
                confidenceLevel.trim());
    }

    /**
     * Returns aggregated statistics
     * for the supplied learning context.
     */
    public LearningStatistics getStatistics(
            LearningKey key) {

        return repository.getStatistics(key);
    }

    /**
     * Returns total number of learning records.
     */
    public int size() {

        return repository.size();
    }

    /**
     * Returns all learning records.
     */
    public List<LearningRecord> getAll() {

        return repository.getAll();
    }

    /**
     * Clears all learning history.
     */
    public void clear() {

        repository.clear();
    }

    /**
     * Returns the underlying repository.
     */
    public LearningRepository getRepository() {

        return repository;
    }
}