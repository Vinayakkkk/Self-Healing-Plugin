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