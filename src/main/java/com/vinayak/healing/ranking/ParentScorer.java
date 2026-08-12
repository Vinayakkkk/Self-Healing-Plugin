package com.vinayak.healing.ranking;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class ParentScorer {

    public double score(
            FailureContext context,
            LocatorCandidate candidate) {

        if (context == null
                || candidate == null) {

            return 0;
        }

        double score = 0;

        /*
         * Parent Tag
         */
        if (equals(
                context.getParentTag(),
                candidate.getParentTag())) {

            score += 20;
        }

        /*
         * Parent Id
         */
        if (equals(
                context.getParentId(),
                candidate.getParentId())) {

            score += 50;
        }

        /*
         * Parent Class
         */
        if (equals(
                context.getParentClass(),
                candidate.getParentClass())) {

            score += 30;
        }

        return score;
    }

    private boolean equals(
            String first,
            String second) {

        if (first == null
                || second == null) {

            return false;
        }

        return first.trim()
                .equalsIgnoreCase(
                        second.trim());
    }
}