package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.model.FailureContext;

public class NavigationEvidenceProvider {

    public List<ExpectedEvidence> collect(
            FailureContext context,
            ExecutionContext executionContext) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null
                || executionContext == null) {

            return evidences;
        }

        /*
         * Current page
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.PAGE,
                executionContext.getCurrentPage(),
                75,
                "currentPage",
                "Derived from current page");

        /*
         * Previous page
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.PAGE,
                executionContext.getPreviousPage(),
                45,
                "previousPage",
                "Derived from previous page");

        return evidences;
    }

    private void addEvidence(
            List<ExpectedEvidence> evidences,
            ExpectedEvidenceType type,
            String value,
            double confidence,
            String attribute,
            String description) {

        if (value == null
                || value.isBlank()) {

            return;
        }

        ExpectedEvidence evidence =
                new ExpectedEvidence(
                        EvidenceSource.NAVIGATION,
                        type,
                        value,
                        confidence,
                        description);

        evidence.setAttribute(attribute);
        evidence.setRawValue(value);

        evidences.add(evidence);
    }
}