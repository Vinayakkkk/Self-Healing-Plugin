package com.vinayak.healing.validator;

import org.openqa.selenium.WebElement;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;

import java.util.ArrayList;
import java.util.List;

public class SuccessfulLocatorValidator {

    public boolean isSuspicious(
            FailureContext context,
            WebElement element) {

        System.out.println(
                ">>> ENTERED SuccessfulLocatorValidator");

        if (context == null
                || element == null) {

            return false;
        }

        String variableName =
                context.getVariableName();

        String expectedTag =
                context.getExpectedTag();

        ElementIntent expectedIntent =
                context.getExpectedIntent();

        String actualTag =
                safeLower(element.getTagName());

        System.out.println(
                "Variable      : "
                        + variableName);

        System.out.println(
                "ExpectedTag   : "
                        + expectedTag);

        System.out.println(
                "ExpectedIntent: "
                        + expectedIntent);

        System.out.println(
                "ActualTag     : "
                        + actualTag);

        System.out.println(
                "ExpectedLabel : "
                        + context.getExpectedLabel());

        System.out.println(
                "ExpectedText  : "
                        + context.getExpectedText());

        System.out.println(
                "LocatorHint   : "
                        + context.getLocatorTextHint());

        /*
         * =====================================================
         * 1. STRONG TAG MISMATCH
         * =====================================================
         *
         * This is the most important check.
         *
         * Example:
         *
         * variable       = loginButton
         * expectedIntent = BUTTON
         * expectedTag    = button
         * actualTag      = input
         *
         * The locator exists, but it found the wrong element.
         *
         * Therefore healing MUST start.
         */
        if (hasText(expectedTag)
                && hasText(actualTag)
                && !expectedTag.equalsIgnoreCase(actualTag)) {

            System.out.println(
                    "SUSPICIOUS = TRUE");

            System.out.println(
                    "Reason = Expected tag '"
                            + expectedTag
                            + "' but actual tag is '"
                            + actualTag
                            + "'");

            return true;
        }

        /*
         * =====================================================
         * 2. STRONG INTENT MISMATCH
         * =====================================================
         *
         * This protects us even when expectedTag is unavailable.
         */
        if (isKnownIntent(expectedIntent)) {

            ElementIntent actualIntent =
                    detectElementIntent(element);

            System.out.println(
                    "ActualIntent  : "
                            + actualIntent);

            if (actualIntent != null
                    && actualIntent != ElementIntent.UNKNOWN
                    && actualIntent != expectedIntent) {

                System.out.println(
                        "SUSPICIOUS = TRUE");

                System.out.println(
                        "Reason = Expected intent '"
                                + expectedIntent
                                + "' but actual intent is '"
                                + actualIntent
                                + "'");

                return true;
            }
        }

        /*
         * =====================================================
         * 3. TEXT / LABEL / VARIABLE CHECK
         * =====================================================
         *
         * Only use semantic text as a secondary signal.
         *
         * We deliberately do NOT reject an element simply
         * because its variable name does not occur in the DOM.
         */
        int suspicion = 0;

        String elementContext =
                buildElementContext(element);

        List<String> variableTokens =
                tokenize(variableName);

        int meaningfulMatches = 0;

        for (String token : variableTokens) {

            if (elementContext.contains(token)) {
                meaningfulMatches++;
            }
        }

        /*
         * Expected label mismatch.
         */
        if (hasText(context.getExpectedLabel())) {

            List<String> labelTokens =
                    tokenize(
                            context.getExpectedLabel());

            if (!containsAnyMeaningfulToken(
                    elementContext,
                    labelTokens)) {

                suspicion += 10;
            }
        }

        /*
         * Expected text mismatch.
         */
        if (hasText(context.getExpectedText())) {

            List<String> textTokens =
                    tokenize(
                            context.getExpectedText());

            if (!containsAnyMeaningfulToken(
                    elementContext,
                    textTokens)) {

                suspicion += 10;
            }
        }

        /*
         * Locator hint mismatch.
         */
        if (hasText(context.getLocatorTextHint())) {

            List<String> hintTokens =
                    tokenize(
                            context.getLocatorTextHint());

            if (!containsAnyMeaningfulToken(
                    elementContext,
                    hintTokens)) {

                suspicion += 10;
            }
        }

        /*
         * Variable-name mismatch is only weak evidence.
         *
         * DO NOT make this alone sufficient for healing.
         */
        if (!variableTokens.isEmpty()
                && meaningfulMatches == 0) {

            suspicion += 10;
        }

        System.out.println(
                "Variable Matches = "
                        + meaningfulMatches);

        System.out.println(
                "Element Context = "
                        + elementContext);

        System.out.println(
                "Secondary Suspicion Score = "
                        + suspicion);

        /*
         * Secondary semantic evidence should not override
         * a valid locator by itself.
         */
        return suspicion >= 40;
    }

    /*
     * =========================================================
     * ELEMENT INTENT DETECTION
     * =========================================================
     *
     * This is generic DOM-based detection.
     */
    private ElementIntent detectElementIntent(
            WebElement element) {

        if (element == null) {
            return ElementIntent.UNKNOWN;
        }

        String tag =
                safeLower(
                        element.getTagName());

        String type =
                safeLower(
                        element.getAttribute("type"));

        /*
         * BUTTON
         */
        if ("button".equals(tag)) {
            return ElementIntent.BUTTON;
        }

        if ("input".equals(tag)
                && ("button".equals(type)
                || "submit".equals(type)
                || "reset".equals(type))) {

            return ElementIntent.BUTTON;
        }

        /*
         * INPUT
         */
        if ("input".equals(tag)
                || "textarea".equals(tag)
                || "select".equals(tag)) {

            return ElementIntent.INPUT;
        }

        /*
         * LINK
         */
        if ("a".equals(tag)) {
            return ElementIntent.LINK;
        }

        /*
         * TEXT
         */
        if ("label".equals(tag)
                || "span".equals(tag)
                || "p".equals(tag)
                || "h1".equals(tag)
                || "h2".equals(tag)
                || "h3".equals(tag)
                || "h4".equals(tag)
                || "h5".equals(tag)
                || "h6".equals(tag)) {

            return ElementIntent.TEXT;
        }

        return ElementIntent.UNKNOWN;
    }

    private boolean isKnownIntent(
            ElementIntent intent) {

        return intent != null
                && intent != ElementIntent.UNKNOWN;
    }

    /*
     * =========================================================
     * BUILD DOM CONTEXT
     * =========================================================
     */
    private String buildElementContext(
            WebElement element) {

        StringBuilder context =
                new StringBuilder();

        append(
                context,
                element.getTagName());

        append(
                context,
                element.getAttribute("id"));

        append(
                context,
                element.getAttribute("name"));

        append(
                context,
                element.getAttribute("class"));

        append(
                context,
                element.getAttribute("data-test"));

        append(
                context,
                element.getAttribute("data-testid"));

        append(
                context,
                element.getAttribute("data-qa"));

        append(
                context,
                element.getAttribute("data-cy"));

        append(
                context,
                element.getAttribute("aria-label"));

        append(
                context,
                element.getAttribute("placeholder"));

        append(
                context,
                element.getAttribute("type"));

        append(
                context,
                element.getAttribute("value"));

        append(
                context,
                element.getAccessibleName());

        append(
                context,
                element.getText());

        return context
                .toString()
                .toLowerCase();
    }

    private void append(
            StringBuilder builder,
            String value) {

        if (value != null
                && !value.isBlank()) {

            builder.append(" ")
                    .append(value);
        }
    }

    /*
     * =========================================================
     * TOKENIZATION
     * =========================================================
     */
    private List<String> tokenize(
            String value) {

        List<String> tokens =
                new ArrayList<>();

        if (value == null
                || value.isBlank()) {

            return tokens;
        }

        String normalized =
                value
                        .replaceAll(
                                "([a-z])([A-Z])",
                                "$1 $2")
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .toLowerCase();

        String[] parts =
                normalized.split("\\s+");

        for (String part : parts) {

            if (part.length() >= 3) {
                tokens.add(part);
            }
        }

        return tokens;
    }

    private boolean containsAnyMeaningfulToken(
            String elementContext,
            List<String> tokens) {

        if (elementContext == null
                || tokens == null
                || tokens.isEmpty()) {

            return false;
        }

        for (String token : tokens) {

            if (token.length() < 3) {
                continue;
            }

            if (elementContext.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private String safeLower(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase();
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
}