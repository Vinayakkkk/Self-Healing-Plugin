package com.vinayak.healing.outcome.engine;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.decision.OutcomeDecisionEngine;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.outcome.selector.VerifierSelector;
import com.vinayak.healing.outcome.verifier.OutcomeVerifier;

public class ExpectedOutcomeEngine {

    private final OutcomeDecisionEngine decisionEngine =
            new OutcomeDecisionEngine();

    private final VerifierSelector selector =
            new VerifierSelector();

    /**
     * Executes only the verifiers relevant
     * to the current expected action.
     */
    public OutcomeVerificationResult verify(
            FailureContext context) {

        List<OutcomeVerificationResult> results =
                new ArrayList<>();

        List<OutcomeVerifier> verifiers =
                selector.select(context);

        for (OutcomeVerifier verifier : verifiers) {

            results.add(
                    verifier.verify(context));
        }

        return decisionEngine.decide(results);
    }
}