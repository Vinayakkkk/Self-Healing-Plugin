package com.vinayak.healing.outcome.verifier;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.OutcomeSignal;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.outcome.model.OutcomeWeights;

public class ExecutionFlowVerifier
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

        ExecutionContext executionContext =
                context.getExecutionContext();

        if (executionContext == null) {

            result.setSuccess(false);

            result.addMessage(
                    "ExecutionContext unavailable.");

            return result;
        }

        if (context.getFailedAction() == null) {

            result.setSuccess(false);

            result.addMessage(
                    "Failed action unavailable.");

            return result;
        }

        if (context.getVariableName() == null
                || context.getVariableName().isBlank()) {

            result.setSuccess(false);

            result.addMessage(
                    "Variable name unavailable.");

            return result;
        }

        result.setSuccess(true);

        result.setConfidence(
                OutcomeWeights.EXECUTION_FLOW);

        result.addPassedSignal(
                OutcomeSignal.EXECUTION_FLOW_MATCH);

        result.addMessage(
                "Execution flow verified.");

        return result;
    }
}