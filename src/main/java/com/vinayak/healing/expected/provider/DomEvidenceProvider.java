package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.model.FailureContext;

public class DomEvidenceProvider {

    public List<ExpectedEvidence> collect(
            FailureContext context) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null) {
            return evidences;
        }

        /*
         * Nearest label
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.LABEL,
                context.getNearestLabel(),
                90,
                "nearestLabel",
                "Derived from nearest label");

        /*
         * Expected text
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.TEXT,
                context.getExpectedText(),
                85,
                "expectedText",
                "Derived from expected text");

        /*
         * Expected tag
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.TAG,
                context.getExpectedTag(),
                80,
                "tag",
                "Expected HTML tag");

        /*
         * Failed locator
         */
        addEvidence(
                evidences,
                ExpectedEvidenceType.VALUE,
                context.getFailedLocator(),
                50,
                "locator",
                "Original failed locator");

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
                        EvidenceSource.DOM,
                        type,
                        value,
                        confidence,
                        description);

        evidence.setAttribute(attribute);
        evidence.setRawValue(value);

        evidences.add(evidence);
    }
}