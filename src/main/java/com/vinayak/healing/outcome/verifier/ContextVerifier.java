package com.vinayak.healing.outcome.verifier;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.OutcomeSignal;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.outcome.model.OutcomeWeights;

public class ContextVerifier
        implements OutcomeVerifier {

    @Override
    public OutcomeVerificationResult verify(
            FailureContext context) {

        OutcomeVerificationResult result =
                new OutcomeVerificationResult();

        if (context == null) {

            result.setSuccess(false);

            result.addMessage(
                    "FailureContext unavailable.");

            return result;
        }

        boolean valid = true;

        if (context.getVariableName() == null
                || context.getVariableName().isBlank()) {

            valid = false;

            result.addMessage(
                    "Variable name unavailable.");
        }

        if (context.getExpectedIntent() == null) {

            valid = false;

            result.addMessage(
                    "Expected intent unavailable.");
        }

        if (context.getExpectedTag() == null
                || context.getExpectedTag().isBlank()) {

            valid = false;

            result.addMessage(
                    "Expected tag unavailable.");
        }

        if (valid) {

            result.setSuccess(true);

            result.setConfidence(
                    OutcomeWeights.CONTEXT);

            result.addPassedSignal(
                    OutcomeSignal.CONTEXT_MATCH);

            result.addMessage(
                    "Context verification successful.");

        } else {

            result.setSuccess(false);

            result.setConfidence(0);

            result.addFailedSignal(
                    OutcomeSignal.CONTEXT_MISMATCH);

            result.addMessage(
                    "Context verification failed.");
        }

        return result;
    }
}