package com.vinayak.healing.execution;

import java.time.LocalDateTime;

public class ExecutionStep {

    private LocalDateTime timestamp;

    private String pageName;

    private String currentUrl;

    private ExecutionAction action;

    private String variableName;

    private String locator;

    private String value;
    private String tagName;

private String parentTag;
private String parentId;
private String parentClass;

private String nearestLabel;

private String elementText;

private String placeholder;

private String ariaLabel;

private String heading;

    private ExecutionStatus status;

    private String exceptionMessage;

    public ExecutionStep() {

        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPageName() {
        return pageName;
    }
    public String getTagName() {
    return tagName;
}

public void setTagName(String tagName) {
    this.tagName = tagName;
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

public String getElementText() {
    return elementText;
}

public void setElementText(String elementText) {
    this.elementText = elementText;
}

public String getPlaceholder() {
    return placeholder;
}

public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
}

public String getAriaLabel() {
    return ariaLabel;
}

public void setAriaLabel(String ariaLabel) {
    this.ariaLabel = ariaLabel;
}

public String getHeading() {
    return heading;
}

public void setHeading(String heading) {
    this.heading = heading;
}

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public ExecutionAction getAction() {
        return action;
    }

    public void setAction(ExecutionAction action) {
        this.action = action;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public String toString() {

        return "\n==============================" +
                "\nTime      : " + timestamp +
                "\nPage      : " + pageName +
                "\nURL       : " + currentUrl +
                "\nAction    : " + action +
                "\nVariable  : " + variableName +
                "\nLocator   : " + locator +
                "\nValue     : " + value +
                "\nStatus    : " + status +
                "\nException : " + exceptionMessage;
    }
}