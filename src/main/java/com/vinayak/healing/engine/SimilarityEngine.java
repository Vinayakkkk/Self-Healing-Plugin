package com.vinayak.healing.engine;

import com.vinayak.healing.model.CandidateElement;

import java.util.List;

public class SimilarityEngine {

    public CandidateElement findBest(
            String failedLocator,
            List<CandidateElement> candidates) {

        CandidateElement best = null;

        double bestScore = 0;

        for (CandidateElement candidate :
                candidates) {

            double score =
                    calculateScore(
                            failedLocator,
                            candidate);

            if(score > bestScore) {

                bestScore = score;

                best = candidate;
            }
        }

        return best;
    }

    private double calculateScore(
            String locator,
            CandidateElement candidate) {

        double score = 0;

        if(candidate.getId() != null &&
                locator.contains(candidate.getId())) {

            score += 50;
        }

        if(candidate.getPlaceholder() != null &&
                locator.toLowerCase()
                        .contains(
                                candidate.getPlaceholder()
                                        .toLowerCase())) {

            score += 30;
        }

        return score;
    }
}