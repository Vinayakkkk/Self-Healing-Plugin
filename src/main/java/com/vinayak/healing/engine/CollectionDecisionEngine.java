package com.vinayak.healing.engine;

import com.vinayak.healing.model.LocatorCandidate;

public class CollectionDecisionEngine {

        private static final double STRONG_SCORE_GAP = 100.0;

    public enum Decision {
        HIGH,
        MEDIUM,
        LOW,
        REJECT
    }

    public record Result(
            Decision decision,
            double confidence,
            boolean shouldCache,
            boolean shouldRepair,
            String reason) {
    }

    public Result decide(
            LocatorCandidate bestCandidate,
            double collectionScore,
            int collectionSize,
            int displayedCount,
            boolean uniqueLocator,
            double scoreGap,
            int semanticSignals,
            double learningScore) {

        if (bestCandidate == null) {

            return new Result(
                    Decision.REJECT,
                    0,
                    false,
                    false,
                    "No validated collection candidate");
        }

        /*
         * ==================================================
         * LEARNING EVIDENCE
         * ==================================================
         *
         * A positive learning score means that this candidate
         * has historical healing evidence for the same
         * learning context.
         *
         * Do NOT treat learning alone as HIGH confidence.
         */
      boolean learnedCandidate =
        learningScore >= 200.0;

        /*
         * ==================================================
         * HIGH CONFIDENCE
         * ==================================================
         *
         * Existing strong deterministic evidence remains
         * the strongest signal.
         */
       if (uniqueLocator
        && semanticSignals >= 2) {

    if (scoreGap >= STRONG_SCORE_GAP) {

        return new Result(
                Decision.HIGH,
                100,
                true,
                true,
                "Unique collection with strong semantic evidence and clear score separation");
    }

    return new Result(
            Decision.MEDIUM,
            75,
            false,
            false,
            "Unique collection has strong semantic evidence but weak score separation"
                    + " | scoreGap="
                    + scoreGap
                    + " | semanticSignals="
                    + semanticSignals);
}

        /*
         * ==================================================
         * LEARNED COLLECTION
         * ==================================================
         *
         * A previously successful exact/semantic healing
         * experience is stronger than a completely unknown
         * collection candidate.
         *
         * However, we still require the browser to have
         * validated multiple elements.
         */
        if (learnedCandidate
        && collectionSize >= 2
        && displayedCount >= 2
        && uniqueLocator) {

    return new Result(
            Decision.MEDIUM,
            85,
            false,
            false,
            "Collection validated with strong historical learning evidence");
}

        /*
         * ==================================================
         * MEDIUM CONFIDENCE
         * ==================================================
         */
       if (semanticSignals >= 1
        && collectionSize >= 2
        && displayedCount >= 2) {

    return new Result(
            Decision.MEDIUM,
            75,
            false,
            false,
            "Collection validated with supporting semantic evidence"
                    + " | scoreGap="
                    + scoreGap
                    + " | semanticSignals="
                    + semanticSignals
                    + " | collectionSize="
                    + collectionSize
                    + " | displayedCount="
                    + displayedCount);
}

        /*
         * ==================================================
         * LOW CONFIDENCE
         * ==================================================
         */
        if (collectionSize >= 2) {

            return new Result(
                    Decision.LOW,
                    50,
                    false,
                    false,
                    "Collection found with weak semantic support");
        }

        /*
         * ==================================================
         * REJECT
         * ==================================================
         */
        return new Result(
                Decision.REJECT,
                0,
                false,
                false,
                "Collection is not safe to heal");
    }
}