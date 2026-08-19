package com.vinayak.healing.resolver;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class DuplicateResolver {

    private static final double DUPLICATE_SCORE_GAP = 20.0;

    public LocatorCandidate resolve(
            FailureContext context,
            List<LocatorCandidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        /*
         * If the top candidate is clearly better than
         * the second candidate, duplicate resolution
         * is not required.
         */
        if (!requiresDuplicateResolution(candidates)) {
            return candidates.get(0);
        }

        /*
         * Get all candidates that are close enough
         * to compete for the top position.
         */
        List<LocatorCandidate> duplicateCandidates =
                getDuplicateCandidates(candidates);

        if (duplicateCandidates.isEmpty()) {
            return null;
        }

        HealingAnalytics.duplicateResolution();

        /*
         * Actual duplicate resolution
         * (label, variable name, parent context,
         * DOM distance, AI, etc.)
         * will be added in the next iteration.
         */
        return resolveSemantically(
        context,
        duplicateCandidates);
    }

    private boolean requiresDuplicateResolution(
            List<LocatorCandidate> candidates) {

        if (candidates == null || candidates.size() < 2) {
            return false;
        }

        double topScore =
                candidates.get(0).getFinalScore();

        double secondScore =
                candidates.get(1).getFinalScore();

        double scoreGap =
                topScore - secondScore;

        return scoreGap <= DUPLICATE_SCORE_GAP;
    }

    public List<LocatorCandidate> getDuplicateCandidates(
            List<LocatorCandidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        if (candidates.size() == 1) {
            return List.of(candidates.get(0));
        }

        List<LocatorCandidate> duplicates =
                new ArrayList<>();

        LocatorCandidate first =
                candidates.get(0);

        duplicates.add(first);

        double topScore =
                first.getFinalScore();

        for (int i = 1; i < candidates.size(); i++) {

            LocatorCandidate candidate =
                    candidates.get(i);

            double scoreGap =
                    topScore - candidate.getFinalScore();

            if (scoreGap <= DUPLICATE_SCORE_GAP) {
                duplicates.add(candidate);
            } else {
                /*
                 * Candidates are already sorted
                 * by score, so we can stop here.
                 */
                break;
            }
        }

        return duplicates;
    }
    private LocatorCandidate resolveSemantically(
        FailureContext context,
        List<LocatorCandidate> candidates) {

    if (context == null || candidates.isEmpty()) {
        return candidates.get(0);
    }

    LocatorCandidate bestCandidate = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (LocatorCandidate candidate : candidates) {

        double score = 0;

        score += similarity(
                context.getExpectedLabel(),
                candidate.getNearestLabel()) * 300;

        score += similarity(
                context.getExpectedText(),
                candidate.getElementText()) * 200;

        score += similarity(
                context.getVariableName(),
                candidate.getPlaceholder()) * 150;

        score += similarity(
                context.getVariableName(),
                candidate.getAriaLabel()) * 150;

        score += similarity(
                context.getVariableName(),
                candidate.getName()) * 100;

        score += similarity(
                context.getVariableName(),
                candidate.getId()) * 100;

        score += candidate.getFinalScore();

        if (score > bestScore) {

            bestScore = score;
            bestCandidate = candidate;
        }
    }

    return bestCandidate;
}
private double similarity(
        String expected,
        String actual) {

    if (expected == null
            || actual == null
            || expected.isBlank()
            || actual.isBlank()) {

        return 0;
    }

    expected = expected.toLowerCase().trim();
    actual = actual.toLowerCase().trim();

    if (expected.equals(actual)) {
        return 1.0;
    }

    if (actual.contains(expected)
            || expected.contains(actual)) {

        return 0.8;
    }

    return 0;
}
}