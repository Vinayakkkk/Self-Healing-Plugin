package com.vinayak.healing.test;

import com.vinayak.healing.execution.ExecutionAnalyzer;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionStep;
import com.vinayak.healing.execution.ExecutionTracker;

public class TestExecutionAnalyzer {

    public static void main(String[] args) {

        // Use the timeline already recorded
        ExecutionContext context =
                ExecutionTracker.getContext();

        if (context.getTimeline().isEmpty()) {

            System.out.println("------------------------------------");
            System.out.println("No execution history found.");
            System.out.println("Run TestExecutionTracker first.");
            System.out.println("------------------------------------");

            return;
        }

        ExecutionAnalyzer analyzer =
                new ExecutionAnalyzer();

        context = analyzer.analyze(context);

        System.out.println("\n======================================");
        System.out.println("EXECUTION ANALYSIS");
        System.out.println("======================================");

        System.out.println("Current Page      : "
                + context.getCurrentPage());

        System.out.println("Previous Page     : "
                + context.getPreviousPage());

        System.out.println("Current URL       : "
                + context.getCurrentUrl());

        System.out.println("Navigation Count  : "
                + context.getNavigationCount());

        System.out.println();

        System.out.println("Visited Pages");

        for (String page : context.getVisitedPages()) {

            System.out.println(" - " + page);
        }

        System.out.println();

        System.out.println("Last Successful Step");

        ExecutionStep success =
                context.getLastSuccessfulStep();

        if (success != null) {

            System.out.println(success);
        }

        System.out.println();

        System.out.println("Failed Step");

        ExecutionStep failed =
                context.getFailedStep();

        if (failed != null) {

            System.out.println(failed);
        }
    }
}