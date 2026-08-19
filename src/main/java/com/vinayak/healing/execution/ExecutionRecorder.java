package com.vinayak.healing.execution;

import com.vinayak.healing.context.DomContextExtractor;
import com.vinayak.healing.core.HealingWebDriver;
import com.vinayak.healing.model.FailureContext;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;

public final class ExecutionRecorder {

    private static final DomContextExtractor domContextExtractor =
            new DomContextExtractor();

    private ExecutionRecorder() {
    }

    public static void record(
            WebElement element,
            By locator,
            HealingWebDriver driver) {

        System.out.println(
                ">>> RECORD SUCCESSFUL INTERACTION");

        try {

            if (element == null) {
                System.out.println(
                        "[RECORD SKIPPED] Element is null");
                return;
            }

            ExecutionStep step =
                    new ExecutionStep();

            /*
             * ==========================================
             * PAGE INFORMATION
             * ==========================================
             */

            try {

                step.setPageName(
                        driver.getWrappedDriver()
                                .getTitle());

                step.setCurrentUrl(
                        driver.getWrappedDriver()
                                .getCurrentUrl());

            } catch (Exception ignored) {
            }

            /*
             * ==========================================
             * ACTION
             * ==========================================
             */

           ExecutionAction action =
        ExecutionTracker.consumeLatestAction();

if (action != null) {

    step.setAction(action);

    System.out.println(
            "[RECORD DEBUG] ACTION CONSUMED = "
                    + action);
}

            /*
             * ==========================================
             * LOCATOR
             * ==========================================
             */

            if (locator != null) {

                step.setLocator(
                        locator.toString());
            }

            /*
             * ==========================================
             * BASIC ELEMENT INFORMATION
             * ==========================================
             */

            step.setTagName(
                    element.getTagName());

            String elementText =
                    element.getText();

            if (elementText == null
                    || elementText.isBlank()) {

                elementText =
                        element.getAttribute("value");
            }

            step.setElementText(elementText);

            step.setPlaceholder(
                    element.getAttribute(
                            "placeholder"));

            step.setAriaLabel(
                    element.getAttribute(
                            "aria-label"));

            /*
             * ==========================================
             * DOM CONTEXT
             * ==========================================
             */

            FailureContext context =
                    new FailureContext();

            domContextExtractor.populate(
                    context,
                    driver.getWrappedDriver(),
                    element);

            step.setNearestLabel(
                    context.getNearestLabel());

            /*
             * ==========================================
             * PARENT INFORMATION
             * ==========================================
             */

            step.setParentTag(
                    context.getParentTag());

            step.setParentId(
                    context.getParentId());

            step.setParentClass(
                    context.getParentClass());

            /*
             * ==========================================
             * STORE EXECUTION STEP
             * ==========================================
             */

            ExecutionTracker.getContext()
                    .setLastSuccessfulStep(step);

            ExecutionTracker.record(step);

            /*
             * ==========================================
             * LOG
             * ==========================================
             */

            System.out.println(
                    "===== EXECUTION STEP =====");

            System.out.println(
                    "Locator      : "
                            + step.getLocator());

            System.out.println(
                    "Tag          : "
                            + step.getTagName());

            System.out.println(
                    "ParentTag    : "
                            + step.getParentTag());

            System.out.println(
                    "ParentId     : "
                            + step.getParentId());

            System.out.println(
                    "ParentClass  : "
                            + step.getParentClass());

            System.out.println(
                    "NearestLabel : "
                            + step.getNearestLabel());

            System.out.println(
                    "ElementText  : "
                            + step.getElementText());

            System.out.println(
                    "==========================");

        } catch (StaleElementReferenceException stale) {

            /*
             * The interaction itself already succeeded.
             *
             * The DOM changed before the recorder could
             * inspect the element.
             *
             * Recording must NOT fail the test.
             */

            System.out.println(
                    "[RECORD SKIPPED] Element became stale "
                            + "after successful interaction.");

            System.out.println(
                    "Locator : "
                            + locator);

        } catch (Exception exception) {

            /*
             * Recording is auxiliary functionality.
             * It must never break the actual test.
             */

            System.out.println(
                    "========== RECORD FAILED ==========");

            System.out.println(
                    "Locator : "
                            + locator);

            exception.printStackTrace();

            System.out.println(
                    "===================================");
        }
    }
}