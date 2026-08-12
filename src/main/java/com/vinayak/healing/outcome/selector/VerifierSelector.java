package com.vinayak.healing.outcome.selector;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.ExpectedOutcomeAction;
import com.vinayak.healing.outcome.verifier.ContextVerifier;
import com.vinayak.healing.outcome.verifier.DomVerifier;
import com.vinayak.healing.outcome.verifier.ExecutionFlowVerifier;
import com.vinayak.healing.outcome.verifier.OutcomeVerifier;
import com.vinayak.healing.outcome.verifier.UrlVerifier;

public class VerifierSelector {

    public List<OutcomeVerifier> select(
            FailureContext context) {

        List<OutcomeVerifier> verifiers =
                new ArrayList<>();

        if (context == null) {
            return verifiers;
        }

        ExpectedOutcomeAction action =
                context.getExpectedOutcomeAction();

        if (action == null) {
            action = ExpectedOutcomeAction.UNKNOWN;
        }

        switch (action) {

            case LOGIN:

                verifiers.add(new UrlVerifier());
                verifiers.add(new ExecutionFlowVerifier());
                verifiers.add(new ContextVerifier());
                break;

            case SEARCH:

                verifiers.add(new DomVerifier());
                verifiers.add(new ExecutionFlowVerifier());
                break;

            case SAVE:

            case UPDATE:

            case DELETE:

                verifiers.add(new DomVerifier());
                verifiers.add(new ContextVerifier());
                break;

            case NAVIGATE:

                verifiers.add(new UrlVerifier());
                verifiers.add(new DomVerifier());
                break;

            default:

                /*
                 * Safe default.
                 */

                verifiers.add(new UrlVerifier());
                verifiers.add(new DomVerifier());
                verifiers.add(new ExecutionFlowVerifier());
                verifiers.add(new ContextVerifier());

                break;
        }

        return verifiers;
    }
}