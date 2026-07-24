package com.vinayak.healing.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.model.LocatorInfo;
import com.vinayak.healing.model.VariableInfo;

public class PipelineResult {

    private FailureContext failureContext;
private LocatorCandidate validatedCandidate;
    private VariableInfo variableInfo;

    private LocatorInfo locatorInfo;

    private ExecutionContext executionContext;

    private List<LocatorCandidate> candidates =
            new ArrayList<>();

    public FailureContext getFailureContext() {
        return failureContext;
    }

    public void setFailureContext(FailureContext failureContext) {
        this.failureContext = failureContext;
    }

    public VariableInfo getVariableInfo() {
        return variableInfo;
    }

    public void setVariableInfo(VariableInfo variableInfo) {
        this.variableInfo = variableInfo;
    }
    public LocatorCandidate getValidatedCandidate() {
    return validatedCandidate;
}

public void setValidatedCandidate(
        LocatorCandidate validatedCandidate) {
    this.validatedCandidate = validatedCandidate;
}

    public LocatorInfo getLocatorInfo() {
        return locatorInfo;
    }

    public void setLocatorInfo(LocatorInfo locatorInfo) {
        this.locatorInfo = locatorInfo;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public List<LocatorCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<LocatorCandidate> candidates) {
        this.candidates = candidates;
    }
}