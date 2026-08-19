package com.vinayak.healing.learning;

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