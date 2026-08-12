package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.analyzer.VariableAnalyzer;
import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.VariableInfo;

public class VariableEvidenceProvider {

    private final VariableAnalyzer variableAnalyzer =
            new VariableAnalyzer();

    public List<ExpectedEvidence> collect(
            FailureContext context) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null) {
            return evidences;
        }

        String variableName =
                context.getVariableName();

        if (variableName == null
                || variableName.isBlank()) {

            return evidences;
        }

        /*
         * Reuse the existing VariableAnalyzer.
         *
         * IMPORTANT:
         *
         * We do NOT duplicate semantic rules here.
         *
         * VariableAnalyzer already understands:
         *
         * button
         * input
         * link
         * dropdown
         * checkbox
         * radio
         * title
         * header
         * etc.
         */
        VariableInfo variableInfo =
                variableAnalyzer.analyze(
                        variableName);

        if (variableInfo == null) {
            return evidences;
        }

        ElementIntent intent =
                variableInfo.getExpectedIntent();

        /*
         * Only publish reliable semantic intent.
         *
         * UNKNOWN must not become evidence.
         */
        if (intent != null
                && intent != ElementIntent.UNKNOWN) {

            ExpectedEvidence evidence =
                    new ExpectedEvidence(
                            EvidenceSource.VARIABLE,
                            ExpectedEvidenceType.ROLE,
                            intent.name(),
                            variableInfo.getConfidence(),
                            "Semantic intent derived from variable name");

            evidence.setAttribute(
                    "variable");

            evidence.setRawValue(
                    variableName);

            evidences.add(evidence);
        }

        /*
         * Expected tag is also propagated when the
         * analyzer has one.
         */
        String expectedTag =
                variableInfo.getExpectedTag();

        if (expectedTag != null
                && !expectedTag.isBlank()) {

            ExpectedEvidence evidence =
                    new ExpectedEvidence(
                            EvidenceSource.VARIABLE,
                            ExpectedEvidenceType.TAG,
                            expectedTag,
                            variableInfo.getConfidence(),
                            "Expected tag derived from variable semantics");

            evidence.setAttribute(
                    "expectedTag");

            evidence.setRawValue(
                    variableName);

            evidences.add(evidence);
        }

        return evidences;
    }
}