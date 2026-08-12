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

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        evidences.addAll(
                variableProvider.collect(failureContext));

        evidences.addAll(
                locatorProvider.collect(failureContext));

        evidences.addAll(
                executionProvider.collect(
                        failureContext,
                        executionContext));

        evidences.addAll(
                domProvider.collect(failureContext));

        evidences.addAll(
                navigationProvider.collect(
                        failureContext,
                        executionContext));

        evidences.addAll(
                businessProvider.collect(failureContext));

        return resolver.resolve(
        evidences,
        failureContext.getExpectedIntent());
    }
}