package com.vinayak.healing.test;

import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionStatus;
import com.vinayak.healing.execution.ExecutionStep;
import com.vinayak.healing.execution.ExecutionTracker;

public class TestExecutionTracker {

    public static void main(String[] args) {

        ExecutionTracker.clear();

        // ==========================
        // Step 1 - Enter Username
        // ==========================
        ExecutionStep usernameStep = new ExecutionStep();
        usernameStep.setPageName("Login");
        usernameStep.setCurrentUrl("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        usernameStep.setAction(ExecutionAction.SEND_KEYS);
        usernameStep.setVariableName("username");
        usernameStep.setLocator("By.name: username");
        usernameStep.setValue("Admin");
        usernameStep.setStatus(ExecutionStatus.SUCCESS);

        ExecutionTracker.record(usernameStep);

        // ==========================
        // Step 2 - Enter Password
        // ==========================
        ExecutionStep passwordStep = new ExecutionStep();
        passwordStep.setPageName("Login");
        passwordStep.setCurrentUrl("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        passwordStep.setAction(ExecutionAction.SEND_KEYS);
        passwordStep.setVariableName("password");
        passwordStep.setLocator("By.name: password");
        passwordStep.setValue("********");
        passwordStep.setStatus(ExecutionStatus.SUCCESS);

        ExecutionTracker.record(passwordStep);

        // ==========================
        // Step 3 - Click Login
        // ==========================
        ExecutionStep loginStep = new ExecutionStep();
        loginStep.setPageName("Login");
        loginStep.setCurrentUrl("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        loginStep.setAction(ExecutionAction.CLICK);
        loginStep.setVariableName("loginButton");
        loginStep.setLocator("By.xpath: //button[@type='submit']");
        loginStep.setStatus(ExecutionStatus.SUCCESS);

        ExecutionTracker.record(loginStep);

        // ==========================
        // Step 4 - Verify Dashboard
        // ==========================
        ExecutionStep verifyStep = new ExecutionStep();
        verifyStep.setPageName("Dashboard");
        verifyStep.setCurrentUrl("https://opensource-demo.orangehrmlive.com/web/index.php/dashboard/index");
        verifyStep.setAction(ExecutionAction.VERIFY);
        verifyStep.setVariableName("dashboardHeader");
        verifyStep.setLocator("By.xpath: //h6[text()='Dashboard']");
        verifyStep.setStatus(ExecutionStatus.FAILED);
        verifyStep.setExceptionMessage("NoSuchElementException");

        ExecutionTracker.record(verifyStep);

        // ==========================
        // Print Timeline
        // ==========================
        ExecutionContext context = ExecutionTracker.getContext();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("        EXECUTION TIMELINE");
        System.out.println("==============================================");

        int count = 1;

        for (ExecutionStep step : context.getTimeline()) {

            System.out.println("\nSTEP " + count++);
            System.out.println(step);
        }

        System.out.println("\n==============================================");
        System.out.println("Current Step");
        System.out.println("==============================================");
        System.out.println(context.getCurrentStep());

        System.out.println("\n==============================================");
        System.out.println("Previous Step");
        System.out.println("==============================================");
        System.out.println(context.getPreviousStep());
    }
}