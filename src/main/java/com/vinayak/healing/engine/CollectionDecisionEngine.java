package com.vinayak.healing.engine;

import com.vinayak.healing.model.LocatorCandidate;

public class CollectionDecisionEngine {

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
            int semanticSignals) {

        if (bestCandidate == null) {
            return new Result(
                    Decision.REJECT,
                    0,
                    false,
                    false,
                    "No validated collection candidate");
        }

        /*
         * Highest confidence:
         * - unique locator
         * - enough semantic evidence
         * - clear winner
         */
        if (uniqueLocator
                && semanticSignals >= 2
                && scoreGap >= 100) {

            return new Result(
                    Decision.HIGH,
                    100,
                    true,
                    true,
                    "Unique collection with strong semantic evidence");
        }

        /*
         * Medium confidence:
         * good candidate but not dominant.
         */
        if (semanticSignals >= 1
                && collectionSize >= 2
                && displayedCount >= 2) {

            return new Result(
                    Decision.MEDIUM,
                    75,
                    false,
                    false,
                    "Collection validated but confidence is moderate");
        }

        /*
         * Low confidence:
         * browser found elements but
         * semantics are weak.
         */
        if (collectionSize >= 2) {

            return new Result(
                    Decision.LOW,
                    50,
                    false,
                    false,
                    "Collection found with weak semantic support");
        }

        return new Result(
                Decision.REJECT,
                0,
                false,
                false,
                "Collection is not safe to heal");
    }
}