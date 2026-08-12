package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionStep;
import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.model.FailureContext;

public class ExecutionEvidenceProvider {

    public List<ExpectedEvidence> collect(
            FailureContext context,
            ExecutionContext executionContext) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null
                || executionContext == null) {

            return evidences;
        }

        ExecutionStep step =
                executionContext.getLastSuccessfulStep();

        if (step == null) {
            return evidences;
        }

        /*
         * Execution evidence is contextual evidence only.
         *
         * IMPORTANT:
         * Do NOT use the previous element's TEXT,
         * LABEL, TAG, PLACEHOLDER or attributes as
         * expected information for the CURRENT failure.
         *
         * The previous step may belong to a completely
         * different element.
         */

        add(
                evidences,
                ExpectedEvidenceType.PAGE,
                step.getPageName(),
                "page",
                "Previous execution page");

        add(
                evidences,
                ExpectedEvidenceType.ACTION,
                step.getAction() == null
                        ? null
                        : step.getAction().name(),
                "action",
                "Previous execution action");

        return evidences;
    }

    private void add(
            List<ExpectedEvidence> evidences,
            ExpectedEvidenceType type,
            String value,
            String attribute,
            String description) {

        if (value == null
                || value.isBlank()) {

            return;
        }

        ExpectedEvidence evidence =
                new ExpectedEvidence(
                        EvidenceSource.EXECUTION,
                        type,
                        value,
                        40,
                        description);

        evidence.setAttribute(attribute);
        evidence.setRawValue(value);

        evidences.add(evidence);
    }
}