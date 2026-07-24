package com.vinayak.healing.analyzer;

import java.util.Comparator;
import java.util.List;

import com.vinayak.healing.engine.DynamicLocatorAnalyzer;   // NEW
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class CandidateRanker {

    private final DynamicLocatorAnalyzer dynamicAnalyzer =
            new DynamicLocatorAnalyzer();   // NEW

    public List<LocatorCandidate> rank(
            FailureContext context,
            List<LocatorCandidate> candidates) {

        if (context == null || candidates == null) {
            return candidates;
        }

        String failedLocator =
                context.getFailedLocator();

        for (LocatorCandidate candidate : candidates) {

            double score = candidate.getFinalScore();

            // =====================================
            // TAG MATCH
            // =====================================

            if (context.getExpectedTag() != null
                    && candidate.getTagName() != null) {

                if (context.getExpectedTag()
                        .equalsIgnoreCase(candidate.getTagName())) {

                    score += 150;

                } else {

                    score -= 200;
                }
            }

            // =====================================
            // INTENT MATCH
            // =====================================

            if (context.getExpectedIntent() != null
                    && context.getExpectedIntent() != ElementIntent.UNKNOWN
                    && candidate.getIntent() != null) {

                if (context.getExpectedIntent()
                        == candidate.getIntent()) {

                    score += 120;

                } else {

                    score -= 250;
                }
            }

            // =====================================
            // PARENT TAG MATCH
            // =====================================

            if (candidate.getParentTag() != null) {

                if ("form".equalsIgnoreCase(candidate.getParentTag())) {
                    score += 20;
                }

                if ("table".equalsIgnoreCase(candidate.getParentTag())) {
                    score += 15;
                }
            }

            // =====================================
            // ID BONUS
            // =====================================

            if ("id".equalsIgnoreCase(candidate.getLocatorType())) {
                score += 40;
            }

            // =====================================
            // NAME BONUS
            // =====================================

            if ("name".equalsIgnoreCase(candidate.getLocatorType())) {
                score += 30;
            }

            // =====================================
            // DATA-* BONUS
            // =====================================

            if (candidate.getLocatorType() != null
                    && candidate.getLocatorType().startsWith("data")) {

                score += 35;
            }

            // =====================================
            // NEW : Dynamic locator bonus
            // =====================================

            if (failedLocator != null
                    && candidate.getLocatorValue() != null
                    && isDynamicLocator(failedLocator)) {

                score += dynamicAnalyzer.calculateDynamicScore(
                        failedLocator,
                        candidate.getLocatorValue());
            }

            candidate.setFinalScore(score);
        }

        candidates.sort(
                Comparator.comparingDouble(
                        LocatorCandidate::getFinalScore)
                        .reversed());

        System.out.println(
        "\n===== CANDIDATE RANKING =====");

System.out.println(
        "Total Candidates : "
                + candidates.size());

int limit =
        Math.min(
                10,
                candidates.size());

System.out.println(
        "\nTOP "
                + limit
                + " CANDIDATES:");

for (int i = 0; i < limit; i++) {

    LocatorCandidate candidate =
            candidates.get(i);

    System.out.println(
            (i + 1)
                    + ". "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue()
                    + " | Score="
                    + candidate.getFinalScore()
                    + " | Matches="
                    + candidate.getOccurrenceCount());
}

        return candidates;
    }

    // =====================================
    // NEW
    // =====================================

    private boolean isDynamicLocator(
            String locator) {

        String value =
                locator.toLowerCase();

        return value.contains("-")
                || value.matches(".*\\d+.*")
                || value.contains("_");
    }
}