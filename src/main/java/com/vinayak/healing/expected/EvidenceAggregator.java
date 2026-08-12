package com.vinayak.healing.expected;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EvidenceAggregator {

    /**
     * Groups evidence by value and
     * accumulates confidence scores.
     */
    public List<EvidenceScore> aggregate(
            List<ExpectedEvidence> evidences) {

        List<EvidenceScore> result =
                new ArrayList<>();

        if (evidences == null
                || evidences.isEmpty()) {

            return result;
        }

        Map<String, EvidenceScore> scoreMap =
                new LinkedHashMap<>();

        for (ExpectedEvidence evidence : evidences) {

            if (evidence == null) {
                continue;
            }

            String key =
        evidence.getType()
        + "::"
        + normalize(evidence.getValue());

            if (key.isBlank()) {
                continue;
            }

            EvidenceScore score =
        scoreMap.computeIfAbsent(
                key,
                k -> new EvidenceScore(
                        evidence.getType(),
                        evidence.getValue()));

            score.addEvidence(evidence);
        }

        result.addAll(scoreMap.values());

        result.sort((a, b) ->
                Double.compare(
                        b.getTotalScore(),
                        a.getTotalScore()));

        return result;
    }

    /**
     * Returns the highest scored evidence.
     */
    public EvidenceScore getBestEvidence(
            List<EvidenceScore> scores) {

        if (scores == null
                || scores.isEmpty()) {

            return null;
        }

        return scores.get(0);
    }

    /**
     * Finds all evidence supporting
     * a specific value.
     */
    public List<ExpectedEvidence> findEvidence(
            List<EvidenceScore> scores,
            String value) {

        if (scores == null
                || value == null) {

            return List.of();
        }

        String normalized =
                normalize(value);

        for (EvidenceScore score : scores) {

            if (normalize(score.getValue())
                    .equals(normalized)) {

                return score.getSupportingEvidence();
            }
        }

        return List.of();
    }

    /**
     * Normalizes evidence values.
     */
    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase();
    }
}