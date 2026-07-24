package com.vinayak.healing.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutionContext {

    private String currentPage;

private String previousPage;

private String currentUrl;
private ExecutionStep latestAction;
private ExecutionStep lastSuccessfulStep;

private ExecutionStep failedStep;

private int navigationCount;

private List<String> visitedPages =
        new ArrayList<>();


        public ExecutionStep getLatestAction() {
    return latestAction;
}

public void setLatestAction(
        ExecutionStep latestAction) {

    this.latestAction = latestAction;
}
public void clearLatestAction() {

    this.latestAction = null;
}

        // Getter and Setter for currentPage
public String getCurrentPage() {
    return currentPage;
}

public void setCurrentPage(String currentPage) {
    this.currentPage = currentPage;
}

// Getter and Setter for previousPage
public String getPreviousPage() {
    return previousPage;
}

public void setPreviousPage(String previousPage) {
    this.previousPage = previousPage;
}

// Getter and Setter for currentUrl
public String getCurrentUrl() {
    return currentUrl;
}

public void setCurrentUrl(String currentUrl) {
    this.currentUrl = currentUrl;
}

// Getter and Setter for lastSuccessfulStep
public ExecutionStep getLastSuccessfulStep() {
    return lastSuccessfulStep;
}

public void setLastSuccessfulStep(ExecutionStep lastSuccessfulStep) {
    this.lastSuccessfulStep = lastSuccessfulStep;
}

// Getter and Setter for failedStep
public ExecutionStep getFailedStep() {
    return failedStep;
}

public void setFailedStep(ExecutionStep failedStep) {
    this.failedStep = failedStep;
}

// Getter and Setter for navigationCount
public int getNavigationCount() {
    return navigationCount;
}

public void setNavigationCount(int navigationCount) {
    this.navigationCount = navigationCount;
}

// Getter and Setter for visitedPages
public List<String> getVisitedPages() {
    return visitedPages;
}

public void setVisitedPages(List<String> visitedPages) {
    this.visitedPages = visitedPages;
}

    private final List<ExecutionStep> timeline =
            new ArrayList<>();

    public void addStep(ExecutionStep step) {

        timeline.add(step);
    }

    public List<ExecutionStep> getTimeline() {

        return Collections.unmodifiableList(timeline);
    }

    public ExecutionStep getCurrentStep() {

        if (timeline.isEmpty()) {
            return null;
        }

        return timeline.get(timeline.size() - 1);
    }

    public ExecutionStep getPreviousStep() {

        if (timeline.size() < 2) {
            return null;
        }

        return timeline.get(timeline.size() - 2);
    }

public void clear() {

    timeline.clear();

    latestAction = null;
    lastSuccessfulStep = null;
    failedStep = null;

    currentPage = null;
    previousPage = null;
    currentUrl = null;

    navigationCount = 0;
    visitedPages.clear();
}
}