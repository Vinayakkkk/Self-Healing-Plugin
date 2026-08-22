package com.vinayak.healing.decision;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

import java.util.List;

public class HealingDecisionEngine {
         private static final double STRONG_SCORE_GAP = 100.0;

        private final SemanticEvidenceEvaluator
        semanticEvidenceEvaluator =
                new SemanticEvidenceEvaluator();

    public HealingDecision decide(
            LocatorCandidate validatedCandidate,
            List<LocatorCandidate> candidates,
            FailureContext context) {

        if (validatedCandidate == null) {

            return new HealingDecision(
                    null,
                    HealingConfidence.REJECT,
                    false,
                    false,
                    "No candidate passed browser validation");
        }

        double selectedScore =
                validatedCandidate.getFinalScore();

        double secondBestScore =
                findSecondBestScore(
                        validatedCandidate,
                        candidates);

        double scoreGap =
                secondBestScore == Double.NEGATIVE_INFINITY
                        ? Double.POSITIVE_INFINITY
                        : selectedScore - secondBestScore;

        SemanticEvidence semanticEvidence =
        semanticEvidenceEvaluator.evaluate(
                validatedCandidate,
                context);

int semanticSignals =
        semanticEvidence.getSignalCount();

        System.out.println();
System.out.println("===== SEMANTIC EVIDENCE =====");
System.out.println(semanticEvidence);
System.out.println("=============================");
System.out.println();

                        int occurrenceCount =
        validatedCandidate.getOccurrenceCount();

boolean uniqueLocator =
        occurrenceCount == 1;

        System.out.println();
System.out.println("===== HEALING DECISION INPUT =====");
System.out.println(
        "Candidate       : "
                + validatedCandidate.getLocatorType()
                + "="
                + validatedCandidate.getLocatorValue());

System.out.println(
        "Selected Score  : "
                + selectedScore);

System.out.println(
        "Second Score    : "
                + secondBestScore);

System.out.println(
        "Score Gap       : "
                + scoreGap);

System.out.println(
        "Occurrence      : "
                + occurrenceCount);

System.out.println(
        "Unique Locator  : "
                + uniqueLocator);

System.out.println(
        "Semantic Signals: "
                + semanticSignals);

System.out.println(
        "Expected Intent : "
                + (context == null
                        ? null
                        : context.getExpectedIntent()));

System.out.println(
        "Candidate Intent: "
                + validatedCandidate.getIntent());

System.out.println(
        "=================================");
System.out.println();


        /*
 * UNIQUENESS SAFETY GATE
 */
if (occurrenceCount > 1) {

    if (semanticSignals >= 1) {

        return new HealingDecision(
                validatedCandidate,
                HealingConfidence.MEDIUM,
                true,
                true,
                "Candidate has semantic evidence but locator is not unique");
    }

    return new HealingDecision(
            validatedCandidate,
            HealingConfidence.REJECT,
            false,
            false,
            "Candidate locator matches multiple elements");
}

        /*
         * HIGH:
         *
         * Candidate passed CandidateValidator
         * and has strong semantic identity.
         *
         * Safe to heal and persist.
         */
      if (uniqueLocator
        && semanticSignals >= 2) {

    /*
     * A unique candidate with strong semantic evidence
     * is normally highly trustworthy.
     *
     * However, if another candidate has a very similar
     * ranking score, the decision is ambiguous.
     */
    if (scoreGap >= STRONG_SCORE_GAP
            || secondBestScore == Double.NEGATIVE_INFINITY) {

        return new HealingDecision(
                validatedCandidate,
                HealingConfidence.HIGH,
                true,
                true,
                "Validated unique candidate with strong semantic evidence and clear score separation");
    }

    return new HealingDecision(
            validatedCandidate,
            HealingConfidence.MEDIUM,
            true,
            true,
            "Validated unique candidate with strong semantic evidence but weak score separation");
}

        /*
         * HIGH:
         *
         * Sometimes only one candidate survives filtering.
         * If it has several semantic signals, there is no
         * meaningful competing candidate.
         */

        /*
         * MEDIUM:
         *
         * Candidate is browser validated and has semantic
         * evidence, but ranking is relatively close.
         *
         * Allow runtime healing but do not permanently cache.
         */
        if (semanticSignals >= 1) {

    return new HealingDecision(
            validatedCandidate,
            HealingConfidence.MEDIUM,
            true,
            true,
            "Validated candidate has supporting evidence but insufficient decision certainty"
                    + " | scoreGap="
                    + scoreGap
                    + " | semanticSignals="
                    + semanticSignals);
}

        /*
         * LOW:
         *
         * Structurally valid but there is not enough
         * semantic identity to trust automatically.
         */
        return new HealingDecision(
                validatedCandidate,
                HealingConfidence.LOW,
                false,
                false,
                "Candidate passed structural validation but lacks semantic evidence");
    }



    private double findSecondBestScore(
            LocatorCandidate selected,
            List<LocatorCandidate> candidates) {

        if (candidates == null
                || candidates.isEmpty()) {

            return Double.NEGATIVE_INFINITY;
        }

        double secondBest =
                Double.NEGATIVE_INFINITY;

        for (LocatorCandidate candidate : candidates) {

            if (candidate == null
                    || candidate == selected) {
                continue;
            }

            if (sameCandidate(
                    selected,
                    candidate)) {
                continue;
            }

            secondBest =
                    Math.max(
                            secondBest,
                            candidate.getFinalScore());
        }

        return secondBest;
    }

    private boolean sameCandidate(
            LocatorCandidate first,
            LocatorCandidate second) {

        if (first.getLocatorType() == null
                || first.getLocatorValue() == null
                || second.getLocatorType() == null
                || second.getLocatorValue() == null) {

            return false;
        }

        return first.getLocatorType()
                .equalsIgnoreCase(
                        second.getLocatorType())

                && first.getLocatorValue()
                .equalsIgnoreCase(
                        second.getLocatorValue());
    }


}