package com.vinayak.healing.expected.verifier;

import org.openqa.selenium.WebElement;

import com.vinayak.healing.expected.ExpectedContext;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class ExpectedElementVerifier {

public boolean verify(
        WebElement element,
        LocatorCandidate candidate,
        FailureContext context) {

    if (element == null
            || candidate == null
            || context == null) {

        return false;
    }

    ExpectedContext expectedContext =
            context.getExpectedContext();

    if (expectedContext == null) {
        return true;
    }

    /*
     * Text verification is only one signal.
     * Never reject immediately.
     */
    boolean textMatched =
            verifyExpectedText(
                    element,
                    expectedContext);

    /*
     * High-confidence candidates should not be
     * rejected only because text differs
     * (translations, localization, etc.).
     */
    if (!textMatched
            && candidate.getFinalScore() >= 900) {

        return true;
    }

    return textMatched;
}

    /*
     * ---------------------------------------------
     * Expected Text Verification
     * ---------------------------------------------
     */

private boolean verifyExpectedText(
        WebElement element,
        ExpectedContext context) {

    String expectedText =
            context.getExpectedText();

    /*
     * No expected text.
     */
    if (expectedText == null
            || expectedText.isBlank()) {

        return true;
    }

    String actualText =
            element.getText();

    if (actualText == null
            || actualText.isBlank()) {

        actualText =
                element.getAttribute("value");
    }

    if (actualText == null
            || actualText.isBlank()) {

        return true;
    }

    expectedText =
            normalize(expectedText);

    actualText =
            normalize(actualText);

    if (actualText.equals(expectedText)) {
        return true;
    }

    if (actualText.contains(expectedText)) {
        return true;
    }

    if (expectedText.contains(actualText)) {
        return true;
    }

    /*
     * Don't reject here.
     * Let other validators decide.
     */
    return false;
}

    /*
     * ---------------------------------------------
     * Normalization
     * ---------------------------------------------
     */

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}