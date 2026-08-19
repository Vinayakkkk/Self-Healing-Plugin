package com.vinayak.healing.model;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.expected.ExpectedContext;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.outcome.model.ExpectedElement;
import com.vinayak.healing.outcome.model.ExpectedOutcomeAction;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
import com.vinayak.healing.execution.ExecutionAction;

public class FailureContext {
private ExecutionContext executionContext;
    private String failedLocator;
    private String pageSource;
    private ExecutionAction failedAction;
    private ElementIntent expectedIntent;
    private TargetCardinality targetCardinality =
        TargetCardinality.UNKNOWN;
    private String variableName;
    private String expectedTag;
    private ExpectedContext expectedContext;
    private String pageObjectPath;
    private String locatorTextHint;
    private String locatorDeclaration;
    private WebDriver driver;
    private String currentUrl;
    private String previousUrl;
    private String parentTag;

private String parentId;

private String parentClass;

private String nearestLabel;

private List<String> neighbourTexts =
        new ArrayList<>();

private String expectedUrl;

private String expectedTitle;

private List<ExpectedElement> expectedElements =
        new ArrayList<>();

private ExpectedOutcomeAction expectedOutcomeAction =
        ExpectedOutcomeAction.UNKNOWN;
    private String expectedLabel;
    private String expectedText;
    
    private Throwable exception;
    private long failureTime;
    private List<CandidateElement> candidates =
        new ArrayList<>();
    
    // ======================================================
// DOM Context
// ======================================================
public ExpectedContext getExpectedContext() {
    return expectedContext;
}

public void setExpectedContext(
        ExpectedContext expectedContext) {
    this.expectedContext = expectedContext;
}
public String getParentTag() {
    return parentTag;
}

public void setParentTag(String parentTag) {
    this.parentTag = parentTag;
}

public String getParentId() {
    return parentId;
}

public void setParentId(String parentId) {
    this.parentId = parentId;
}

public String getParentClass() {
    return parentClass;
}

public void setParentClass(String parentClass) {
    this.parentClass = parentClass;
}

public String getNearestLabel() {
    return nearestLabel;
}

public void setNearestLabel(String nearestLabel) {
    this.nearestLabel = nearestLabel;
}

public List<String> getNeighbourTexts() {
    return neighbourTexts;
}

public void setNeighbourTexts(
        List<String> neighbourTexts) {

    this.neighbourTexts = neighbourTexts;
}
    
public ExecutionAction getFailedAction() {
    return failedAction;
}

public void setFailedAction(
        ExecutionAction failedAction) {
    this.failedAction = failedAction;
}
    // --- Getters and Setters ---
    public WebDriver getDriver() {
    return driver;
}

public void setDriver(WebDriver driver) {
    this.driver = driver;
}
public String getLocatorTextHint() {
    return locatorTextHint;
}
public String getPageObjectPath() {
    return pageObjectPath;
}

public void setPageObjectPath(String pageObjectPath) {
    this.pageObjectPath = pageObjectPath;
}

public void setLocatorTextHint(
        String locatorTextHint) {

    this.locatorTextHint =
            locatorTextHint;
}

public String getExpectedText() {
    return expectedText;
}

public void setExpectedText(String expectedText) {
    this.expectedText = expectedText;
}

    public List<CandidateElement> getCandidates() {
    return candidates;
}

public void setCandidates(List<CandidateElement> candidates) {
    this.candidates = candidates;
}

    public long getFailureTime() {
    return failureTime;
}

public void setFailureTime(long failureTime) {
    this.failureTime = failureTime;
}


 public Throwable getException() {
    return exception;
}

public void setException(Throwable exception) {
    this.exception = exception;
}
    public String getCurrentUrl() {
    return currentUrl;
}

public void setCurrentUrl(String currentUrl) {
    this.currentUrl = currentUrl;
}

public String getPreviousUrl() {
    return previousUrl;
}

public void setPreviousUrl(String previousUrl) {
    this.previousUrl = previousUrl;
}

public String getExpectedUrl() {
    return expectedUrl;
}

public void setExpectedUrl(String expectedUrl) {
    this.expectedUrl = expectedUrl;
}

public String getExpectedTitle() {
    return expectedTitle;
}

public void setExpectedTitle(String expectedTitle) {
    this.expectedTitle = expectedTitle;
}

public List<ExpectedElement> getExpectedElements()  {
    return expectedElements;
}

public void setExpectedElements(
        List<ExpectedElement> expectedElements) {

    this.expectedElements = expectedElements;
}

public ExpectedOutcomeAction getExpectedOutcomeAction() {
    return expectedOutcomeAction;
}

public void setExpectedOutcomeAction(
        ExpectedOutcomeAction expectedOutcomeAction) {

    this.expectedOutcomeAction =
            expectedOutcomeAction;
}

    public ExecutionContext getExecutionContext() {
    return executionContext;
}

public void setExecutionContext(ExecutionContext executionContext) {
    this.executionContext = executionContext;
}

    public String getExpectedLabel() {
    return expectedLabel;
}

public void setExpectedLabel(String expectedLabel) {
    this.expectedLabel = expectedLabel;
}

    public String getLocatorDeclaration() {
    return locatorDeclaration;
}

public void setLocatorDeclaration(
        String locatorDeclaration) {

    this.locatorDeclaration =
            locatorDeclaration;
}

    public String getFailedLocator() {
        return failedLocator;
    }

    public void setFailedLocator(String failedLocator) {
        this.failedLocator = failedLocator;
    }

    public String getPageSource() {
        return pageSource;
    }

    public void setPageSource(String pageSource) {
        this.pageSource = pageSource;
    }

    public ElementIntent getExpectedIntent() {
        return expectedIntent;
    }
    public TargetCardinality getTargetCardinality() {
    return targetCardinality;
}

public void setTargetCardinality(
        TargetCardinality targetCardinality) {

    this.targetCardinality =
            targetCardinality == null
                    ? TargetCardinality.UNKNOWN
                    : targetCardinality;
}

    public void setExpectedIntent(ElementIntent expectedIntent) {
        this.expectedIntent = expectedIntent;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getExpectedTag() {
        return expectedTag;
    }

    public void setExpectedTag(String expectedTag) {
        this.expectedTag = expectedTag;
    }

    
}