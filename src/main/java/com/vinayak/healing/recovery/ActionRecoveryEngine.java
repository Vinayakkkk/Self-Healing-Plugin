package com.vinayak.healing.recovery;

import com.vinayak.healing.capability.CapabilityValidator;
import com.vinayak.healing.capability.ElementCapability;
import com.vinayak.healing.execution.ExecutionStep;
import com.vinayak.healing.execution.ExecutionTracker;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ActionRecoveryEngine {

    private final CapabilityValidator capabilityValidator =
            new CapabilityValidator();

    private final DomTreeWalker domTreeWalker =
            new DomTreeWalker();

    public WebElement recover(
            WebElement currentElement,
            ElementCapability capability) {

        if (currentElement == null || capability == null) {
            return currentElement;
        }

       List<WebElement> elements =
        collectRecoveryContext(currentElement);

             ExecutionStep expected =
        ExecutionTracker.getContext()
                .getLastSuccessfulStep();

ExecutionStep latestAction =
        ExecutionTracker.getContext()
                .getLatestAction();

       WebElement bestElement = null;
int bestScore = Integer.MIN_VALUE;

        for (WebElement element : elements) {

            try {

                if (!capabilityValidator.supports(
                        element,
                        capability)) {

                    continue;
                }

           int score =
        calculateScore(
                element,
                expected,
                latestAction,
                capability);

                if (score > bestScore) {

                    bestScore = score;
                    bestElement = element;
                }

            } catch (Exception ignored) {
            }
        }

        if (bestElement != null) {

            System.out.println(
                    "ACTION RECOVERY FOUND : "
                            + bestElement.getTagName()
                            + " | class="
                            + bestElement.getAttribute("class")
                            + " | score="
                            + bestScore);

            return bestElement;
        }

      System.out.println(
        "ACTION RECOVERY : No matching element found.");

throw new IllegalStateException(
        "No valid element found for action capability: "
                + capability);
    }

private int calculateScore(
        WebElement element,
        ExecutionStep expected,
        ExecutionStep latestAction,
        ElementCapability capability) {

            if (capability == ElementCapability.TYPE) {

    String tag =
            element.getTagName();

    if (tag == null) {
        return Integer.MIN_VALUE;
    }

    tag = tag.trim().toLowerCase();

    if (!tag.equals("input")
            && !tag.equals("textarea")) {

        return Integer.MIN_VALUE;
    }
}

    if (expected == null) {
        return 0;
    }

    int score = 0;

    if (latestAction != null
        && latestAction.getAction() != null
        && capability == ElementCapability.TYPE) {

    if (capabilityValidator.supports(
            element,
            ElementCapability.TYPE)) {

        score += 200;
    }
}

    try {

        if (element.isDisplayed()) {
            score += 40;
        }

        if (element.isEnabled()) {
            score += 30;
        }

        String id =
                element.getAttribute("id");

        if (id != null && !id.isBlank()) {
            score += 10;
        }

        String name =
                element.getAttribute("name");

        if (name != null && !name.isBlank()) {
            score += 10;
        }

        String aria =
                element.getAttribute("aria-label");

        if (aria != null && !aria.isBlank()) {
            score += 5;
        }

        String placeholder =
                element.getAttribute("placeholder");

        if (placeholder != null && !placeholder.isBlank()) {
            score += 5;
        }

        // ======================================
        // NEW CONTEXT-AWARE SCORING STARTS HERE
        // ======================================

        String tag =
        element.getTagName();

if (expected.getTagName() != null
        && tag != null
        && expected.getTagName()
                .trim()
                .equalsIgnoreCase(
                        tag.trim())) {

    score += 40;
}

        if (expected.getPlaceholder() != null
                && placeholder != null
                && expected.getPlaceholder()
        .trim()
        .equalsIgnoreCase(
                placeholder.trim())) {

            score += 30;
        }
if (expected.getAriaLabel() != null
        && aria != null
        && expected.getAriaLabel()
                .trim()
                .equalsIgnoreCase(
                        aria.trim())) {

    score += 30;
}

String text =
        element.getText();

if (text == null || text.isBlank()) {

    text =
            element.getAttribute("value");
}

if (expected.getElementText() != null
        && text != null
        && expected.getElementText()
                .trim()
                .equalsIgnoreCase(
                        text.trim())) {

    score += 20;
}

        // ======================================
        // NEW CONTEXT-AWARE SCORING ENDS HERE
 
    } catch (Exception ignored) {
    }
    

    return score;
}
private List<WebElement> collectRecoveryContext(
        WebElement currentElement) {

    List<WebElement> elements =
            new ArrayList<>();

    if (currentElement == null) {
        return elements;
    }

    /*
     * Start from the current element.
     */
    elements.add(currentElement);

    /*
     * Walk upward through a few meaningful ancestors.
     *
     * This allows recovery when the healed element
     * is a label/span/div but the real action target
     * is a sibling or descendant of its parent.
     */
    WebElement ancestor =
            currentElement;

    for (int level = 0; level < 4; level++) {

        try {

            List<WebElement> parents =
                    ancestor.findElements(
                            By.xpath("./.."));

            if (parents == null
                    || parents.isEmpty()) {

                break;
            }

            ancestor =
                    parents.get(0);

            /*
             * Search the entire subtree of the
             * surrounding container.
             */
            List<WebElement> descendants =
                    domTreeWalker.walk(
                            ancestor);

            for (WebElement element :
                    descendants) {

                if (!elements.contains(element)) {
                    elements.add(element);
                }
            }

        } catch (Exception ignored) {
            break;
        }
    }

    return elements;
}
}