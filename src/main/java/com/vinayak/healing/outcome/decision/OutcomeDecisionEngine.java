package com.vinayak.healing.outcome.decision;

import com.vinayak.healing.outcome.model.OutcomeVerificationResult;

import java.util.List;

public class OutcomeDecisionEngine {

    /**
     * Combines all verification results into
     * one final verification result.
     */
    public OutcomeVerificationResult decide(
            List<OutcomeVerificationResult> results) {

        OutcomeVerificationResult finalResult =
                new OutcomeVerificationResult();

        if (results == null || results.isEmpty()) {

            finalResult.setSuccess(false);
            finalResult.setConfidence(0);

            finalResult.addMessage(
                    "No verification results available.");

            return finalResult;
        }

        double confidence = 0;

        for (OutcomeVerificationResult result : results) {

            confidence += result.getConfidence();

            finalResult.getPassedSignals()
                    .addAll(result.getPassedSignals());

            finalResult.getFailedSignals()
                    .addAll(result.getFailedSignals());

            finalResult.getMessages()
                    .addAll(result.getMessages());
        }

        confidence = Math.min(confidence, 100);

        finalResult.setConfidence(confidence);

        finalResult.setSuccess(confidence >= 70);

        return finalResult;
    }
}