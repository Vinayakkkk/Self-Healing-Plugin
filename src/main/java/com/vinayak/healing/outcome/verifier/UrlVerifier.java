package com.vinayak.healing.outcome.verifier;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.OutcomeSignal;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.outcome.model.OutcomeWeights;

public class UrlVerifier implements OutcomeVerifier {

    @Override
    public OutcomeVerificationResult verify(
            FailureContext context) {

        OutcomeVerificationResult result =
                new OutcomeVerificationResult();

        if (context == null) {

            result.setSuccess(false);

            result.addMessage(
                    "FailureContext is null.");

            return result;
        }

        

        String currentUrl =
                context.getCurrentUrl();

        String expectedUrl =
                context.getExpectedUrl();

        /*
         * If no expectation exists yet,
         * skip URL verification.
         */
        if (expectedUrl == null
                || expectedUrl.isBlank()) {

            result.setSuccess(true);

            result.setConfidence(0);

            result.addMessage(
                    "No expected URL available.");

            return result;
        }

        if (currentUrl != null
                && currentUrl.equals(expectedUrl)) {

            result.setSuccess(true);

            result.setConfidence(
        OutcomeWeights.URL);

            result.addPassedSignal(
                    OutcomeSignal.EXPECTED_URL_REACHED);

            result.addMessage(
                    "Expected URL reached.");

            return result;
        }

        result.setSuccess(false);

        result.setConfidence(0);

        result.addFailedSignal(
                OutcomeSignal.ACTION_FAILED);

        result.addMessage(
                "Expected URL not reached.");

        return result;
    }
}