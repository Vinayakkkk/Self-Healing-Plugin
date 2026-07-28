package com.vinayak.healing.model;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.intent.ElementIntent;
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
    private String variableName;
    private String expectedTag;
    private String pageObjectPath;
    private String locatorTextHint;
    private String locatorDeclaration;
    private WebDriver driver;
    private String currentUrl;
    private String expectedLabel;
    private String expectedText;
    
    private Throwable exception;
    private long failureTime;
    private List<CandidateElement> candidates =
        new ArrayList<>();
    
    
    
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