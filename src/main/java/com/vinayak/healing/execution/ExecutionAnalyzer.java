package com.vinayak.healing.execution;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExecutionAnalyzer {

    public ExecutionContext analyze(ExecutionContext context) {

        if (context == null || context.getTimeline().isEmpty()) {
            return context;
        }

        ExecutionStep previous = null;

        Set<String> visitedPages = new LinkedHashSet<>();

        int navigationCount = 0;

        for (ExecutionStep current : context.getTimeline()) {

            // ----------------------------
            // Visited Pages
            // ----------------------------
            if (current.getPageName() != null &&
                    !current.getPageName().isBlank()) {

                visitedPages.add(current.getPageName());
            }

            // ----------------------------
            // Last Successful Step
            // ----------------------------
            if (current.getStatus() == ExecutionStatus.SUCCESS) {

                context.setLastSuccessfulStep(current);
            }

            // ----------------------------
            // Failed Step
            // ----------------------------
            if (current.getStatus() == ExecutionStatus.FAILED) {

                context.setFailedStep(current);
            }

            // ----------------------------
            // Navigation Detection
            // ----------------------------
            if (previous != null) {

                if (!previous.getPageName()
                        .equals(current.getPageName())) {

                    navigationCount++;

                    context.setPreviousPage(
                            previous.getPageName());

                    context.setCurrentPage(
                            current.getPageName());

                    context.setCurrentUrl(
                            current.getCurrentUrl());
                }
            }

            previous = current;
        }

        context.setNavigationCount(navigationCount);

        context.setVisitedPages(
                visitedPages.stream().toList());

        return context;
    }
}