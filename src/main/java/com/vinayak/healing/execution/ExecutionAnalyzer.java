package com.vinayak.healing.execution;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExecutionAnalyzer {

    public ExecutionContext analyze(
            ExecutionContext context) {

        if (context == null
                || context.getTimeline().isEmpty()) {

            return context;
        }

        ExecutionStep previous = null;

        Set<String> visitedPages =
                new LinkedHashSet<>();

        int navigationCount = 0;

        /*
         * ----------------------------------------------------
         * First pass:
         *
         * Build navigation information and identify the
         * actual failed step.
         * ----------------------------------------------------
         */
        for (ExecutionStep current :
                context.getTimeline()) {

            if (current == null) {
                continue;
            }

            /*
             * Visited pages
             */
            if (current.getPageName() != null
                    && !current.getPageName().isBlank()) {

                visitedPages.add(
                        current.getPageName());
            }

            /*
             * Failed step
             */
            if (current.getStatus()
                    == ExecutionStatus.FAILED) {

                context.setFailedStep(current);
            }

            /*
             * Navigation detection
             */
            if (previous != null) {

                String previousPage =
                        previous.getPageName();

                String currentPage =
                        current.getPageName();

                if (previousPage != null
                        && currentPage != null
                        && !previousPage.equals(
                                currentPage)) {

                    navigationCount++;

                    context.setPreviousPage(
                            previousPage);

                    context.setCurrentPage(
                            currentPage);

                    context.setCurrentUrl(
                            current.getCurrentUrl());
                }
            }

            previous = current;
        }

        /*
         * ----------------------------------------------------
         * Determine the page of the failed operation.
         * ----------------------------------------------------
         */
        ExecutionStep failedStep =
                context.getFailedStep();

        String failedPage = null;

        if (failedStep != null) {

            failedPage =
                    failedStep.getPageName();
        }

        /*
         * ----------------------------------------------------
         * Find the most recent SUCCESSFUL step on the
         * SAME PAGE as the failure.
         *
         * This is the important fix.
         *
         * We must NOT use the last successful step from
         * another page.
         * ----------------------------------------------------
         */
        ExecutionStep lastSuccessfulSamePage = null;

        for (int i =
                context.getTimeline().size() - 1;
                i >= 0;
                i--) {

            ExecutionStep step =
                    context.getTimeline().get(i);

            if (step == null) {
                continue;
            }

            /*
             * Do not use the failed step itself.
             */
            if (step.getStatus()
                    != ExecutionStatus.SUCCESS) {

                continue;
            }

            /*
             * If we know the failed page, only accept
             * successful steps from that page.
             */
            if (failedPage != null
                    && !failedPage.isBlank()) {

                String stepPage =
                        step.getPageName();

                if (stepPage == null
                        || !stepPage.equals(
                                failedPage)) {

                    continue;
                }
            }

            lastSuccessfulSamePage = step;
            break;
        }

        /*
         * ----------------------------------------------------
         * Store page-local successful context.
         *
         * If there is no successful step on the current
         * page, deliberately store null.
         *
         * This prevents stale LoginPage information from
         * becoming ProductsPage evidence.
         * ----------------------------------------------------
         */
        context.setLastSuccessfulStep(
                lastSuccessfulSamePage);

        /*
         * ----------------------------------------------------
         * If the failed page is known, explicitly make it
         * the current page.
         * ----------------------------------------------------
         */
        if (failedPage != null
                && !failedPage.isBlank()) {

            context.setCurrentPage(
                    failedPage);
        }

        context.setNavigationCount(
                navigationCount);

        context.setVisitedPages(
                visitedPages.stream().toList());

        return context;
    }
}