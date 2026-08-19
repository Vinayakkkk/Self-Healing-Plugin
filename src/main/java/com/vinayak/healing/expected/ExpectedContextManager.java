package com.vinayak.healing.expected;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.expected.provider.BusinessEvidenceProvider;
import com.vinayak.healing.expected.provider.DomEvidenceProvider;
import com.vinayak.healing.expected.provider.ExecutionEvidenceProvider;
import com.vinayak.healing.expected.provider.LocatorEvidenceProvider;
import com.vinayak.healing.expected.provider.NavigationEvidenceProvider;
import com.vinayak.healing.expected.provider.VariableEvidenceProvider;
import com.vinayak.healing.model.FailureContext;

public class ExpectedContextManager {

    private final VariableEvidenceProvider variableProvider =
            new VariableEvidenceProvider();

    private final LocatorEvidenceProvider locatorProvider =
            new LocatorEvidenceProvider();

    private final ExecutionEvidenceProvider executionProvider =
            new ExecutionEvidenceProvider();

    private final DomEvidenceProvider domProvider =
            new DomEvidenceProvider();

    private final NavigationEvidenceProvider navigationProvider =
            new NavigationEvidenceProvider();

    private final BusinessEvidenceProvider businessProvider =
            new BusinessEvidenceProvider();

    private final ExpectedContextResolver resolver =
            new ExpectedContextResolver();

public ExpectedContext resolve(
        FailureContext failureContext,
        ExecutionContext executionContext) {

    System.out.println(
            "===== EXPECTED CONTEXT INPUT =====");

    if (failureContext == null) {

        System.out.println(
                "FailureContext : NULL");

        System.out.println(
                "==================================");

        return new ExpectedContext();
    }

    System.out.println(
            "VariableName   : "
                    + failureContext.getVariableName());

    System.out.println(
            "FailedAction   : "
                    + failureContext.getFailedAction());

    System.out.println(
            "NearestLabel   : "
                    + failureContext.getNearestLabel());

    System.out.println(
            "ExpectedText   : "
                    + failureContext.getExpectedText());

    System.out.println(
            "ExpectedTag    : "
                    + failureContext.getExpectedTag());

    System.out.println(
            "ExpectedIntent : "
                    + failureContext.getExpectedIntent());

    System.out.println(
            "LocatorHint    : "
                    + failureContext.getLocatorTextHint());

    System.out.println(
            "==================================");

    List<ExpectedEvidence> evidences =
            new ArrayList<>();

    evidences.addAll(
            variableProvider.collect(
                    failureContext));

    evidences.addAll(
            locatorProvider.collect(
                    failureContext));

    evidences.addAll(
            executionProvider.collect(
                    failureContext,
                    executionContext));

    evidences.addAll(
            domProvider.collect(
                    failureContext));

    evidences.addAll(
            navigationProvider.collect(
                    failureContext,
                    executionContext));

    evidences.addAll(
            businessProvider.collect(
                    failureContext));

    /*
     * DEBUG:
     *
     * Show exactly which provider is producing
     * LABEL / TEXT evidence and what value it contains.
     */
    System.out.println(
            "===== EXPECTED EVIDENCE =====");

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null) {
            continue;
        }

        System.out.println(
                "SOURCE="
                        + evidence.getSource()
                        + " | TYPE="
                        + evidence.getType()
                        + " | VALUE="
                        + evidence.getValue()
                        + " | CONFIDENCE="
                        + evidence.getConfidence()
                        + " | ATTRIBUTE="
                        + evidence.getAttribute()
                        + " | DESCRIPTION="
                        + evidence.getDescription());
    }

    System.out.println(
            "==============================");

    ExpectedContext resolved =
            resolver.resolve(
                    evidences,
                    failureContext.getExpectedIntent());

    /*
     * DEBUG:
     *
     * Show what the resolver finally decided.
     */
    System.out.println(
            "===== RESOLVED EXPECTED CONTEXT =====");

    System.out.println(
            "ExpectedLabel  : "
                    + resolved.getExpectedLabel());

    System.out.println(
            "ExpectedText   : "
                    + resolved.getExpectedText());

    System.out.println(
            "ExpectedTag    : "
                    + resolved.getExpectedTag());

    System.out.println(
            "ExpectedIntent : "
                    + resolved.getExpectedIntent());

    System.out.println(
            "Confidence     : "
                    + resolved.getConfidence());

    System.out.println(
            "======================================");

    return resolved;
}
}