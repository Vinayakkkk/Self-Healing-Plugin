package com.vinayak.healing.verification;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import org.openqa.selenium.WebElement;

public class ExpectedElementVerifier {

    public boolean verify(
            WebElement element,
            LocatorCandidate candidate,
            FailureContext context) {

        if (element == null) {
            return false;
        }

        if (context == null) {
            return true;
        }

        int score = 0;
        int availableSignals = 0;

        // ==========================
        // TAG
        // ==========================

        String expectedTag =
                context.getExpectedTag();

        if (hasText(expectedTag)) {

            availableSignals++;

            if (expectedTag.equalsIgnoreCase(
                    element.getTagName())) {

                score += 20;
            }
        }

        // ==========================
        // TEXT
        // ==========================

        String expectedText =
                context.getExpectedText();

        if (hasText(expectedText)) {

            availableSignals++;

            String actualText =
                    element.getText();

            if (matches(
                    expectedText,
                    actualText)) {

                score += 40;
            }
        }

        // ==========================
        // LOCATOR TEXT HINT
        // ==========================

        String locatorHint =
                context.getLocatorTextHint();

        if (hasText(locatorHint)) {

            availableSignals++;

            String actualText =
                    element.getText();

            String placeholder =
                    element.getAttribute(
                            "placeholder");

            String ariaLabel =
                    element.getAttribute(
                            "aria-label");

            if (matches(locatorHint, actualText)
                    || matches(locatorHint, placeholder)
                    || matches(locatorHint, ariaLabel)) {

                score += 30;
            }
        }

        // ==========================
        // LABEL
        // ==========================

        String expectedLabel =
                context.getExpectedLabel();

        if (hasText(expectedLabel)) {

            availableSignals++;

            String ariaLabel =
                    element.getAttribute(
                            "aria-label");

            if (matches(
                    expectedLabel,
                    ariaLabel)) {

                score += 20;
            }
        }

        // ==========================
        // VARIABLE / NAME
        // ==========================

        String variable =
                context.getVariableName();

        String name =
                element.getAttribute("name");

        if (hasText(variable)
                && hasText(name)) {

            availableSignals++;

            if (name.toLowerCase()
                    .contains(
                            variable.toLowerCase())) {

                score += 20;
            }
        }

        // ==========================
        // DEBUG
        // ==========================

        System.out.println();
        System.out.println(
                "===== EXPECTED ELEMENT VERIFIER =====");

        System.out.println(
                "Candidate : "
                        + (candidate == null
                        ? "null"
                        : candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue()));

        System.out.println(
                "Score     : "
                        + score);

        System.out.println(
                "Signals   : "
                        + availableSignals);

        System.out.println(
                "======================================");

        /*
         * No evidence available:
         * do not reject the candidate.
         */
        if (availableSignals == 0) {
            return true;
        }

        /*
         * Strong evidence is enough.
         *
         * We are verifying an already browser-validated
         * candidate, not trying to rediscover the element.
         */
        return score >= 20;
    }

    private boolean matches(
            String expected,
            String actual) {

        if (!hasText(expected)
                || !hasText(actual)) {

            return false;
        }

        String normalizedExpected =
                normalize(expected);

        String normalizedActual =
                normalize(actual);

        if (normalizedActual.equals(
                normalizedExpected)) {

            return true;
        }

        return normalizedActual.contains(
                normalizedExpected)
                || normalizedExpected.contains(
                normalizedActual);
    }

    private String normalize(
            String value) {

        return value
                .replaceAll(
                        "\\s+",
                        " ")
                .trim()
                .toLowerCase();
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
}