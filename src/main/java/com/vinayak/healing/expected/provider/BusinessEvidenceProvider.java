package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.model.FailureContext;

public class BusinessEvidenceProvider {

    public List<ExpectedEvidence> collect(
            FailureContext context) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null) {
            return evidences;
        }

        /*
         * Business text
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.TEXT,
                context.getExpectedText(),
                88,
                "expectedText",
                "Derived from business text");

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
                        EvidenceSource.BUSINESS,
                        type,
                        value,
                        confidence,
                        description);

        evidence.setAttribute(attribute);
        evidence.setRawValue(value);

        evidences.add(evidence);
    }
}