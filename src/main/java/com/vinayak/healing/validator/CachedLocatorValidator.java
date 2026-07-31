package com.vinayak.healing.validator;

import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CachedLocatorValidator {

    private static final double MIN_TEXT_SIMILARITY = 0.75;

    public boolean validate(
            WebDriver driver,
            By cachedLocator,
            FailureContext context) {

        if (driver == null
                || cachedLocator == null
                || context == null) {

            return false;
        }

        System.out.println(
                "\n========== SEMANTIC CACHE VALIDATION ==========");

        System.out.println(
                "Cached Locator : "
                        + cachedLocator);

        List<WebElement> elements;

        try {

            elements =
                    driver.findElements(
                            cachedLocator);

        } catch (Exception exception) {

            System.out.println(
                    "CACHE REJECTED : locator execution failed");

            return false;
        }

        // ==========================================
        // 1. UNIQUENESS
        // ==========================================

        if (elements.size() != 1) {

            System.out.println(
                    "CACHE REJECTED : expected 1 element but found "
                            + elements.size());

            return false;
        }

        WebElement element =
                elements.get(0);

        // ==========================================
        // 2. VISIBILITY
        // ==========================================

        try {

            if (!element.isDisplayed()) {

                System.out.println(
                        "CACHE REJECTED : element is not displayed");

                return false;
            }

        } catch (Exception exception) {

            return false;
        }

        // ==========================================
        // 3. ENABLED
        // ==========================================

        try {

            if (!element.isEnabled()) {

                System.out.println(
                        "CACHE REJECTED : element is disabled");

                return false;
            }

        } catch (Exception exception) {

            return false;
        }

        // ==========================================
        // 4. EXPECTED TAG
        // ==========================================

        if (!validateExpectedTag(
                element,
                context)) {

            return false;
        }

        // ==========================================
        // 5. EXPECTED INTENT
        // ==========================================

        if (!validateExpectedIntent(
                element,
                context)) {

            return false;
        }

        // ==========================================
        // 6. ACTION COMPATIBILITY
        // ==========================================

        if (!validateActionCompatibility(
                element,
                context)) {

            return false;
        }

        // ==========================================
        // 7. TEXT IDENTITY
        // ==========================================

        if (!validateTextIdentity(
                element,
                context)) {

            return false;
        }

        // ==========================================
        // 8. LABEL IDENTITY
        // ==========================================

if (!validateLabelIdentity(
        driver,
        element,
        context)) {

    return false;
}

        System.out.println(
                "SEMANTIC CACHE VALIDATION PASSED");

        return true;
    }

    // =====================================================
    // TAG VALIDATION
    // =====================================================

    private boolean validateExpectedTag(
            WebElement element,
            FailureContext context) {

        String expectedTag =
                context.getExpectedTag();

        if (!hasText(expectedTag)) {
            return true;
        }

        String actualTag =
                element.getTagName();

        if (actualTag == null) {
            return false;
        }

        expectedTag =
                expectedTag.trim()
                        .toLowerCase();

        actualTag =
                actualTag.trim()
                        .toLowerCase();

        if (expectedTag.equals(actualTag)) {
            return true;
        }

        /*
         * Input-family compatibility.
         */
        if (expectedTag.equals("input")) {

            return actualTag.equals("input")
                    || actualTag.equals("textarea")
                    || actualTag.equals("select");
        }

        /*
         * Heading-family compatibility.
         */
        if (expectedTag.matches("h[1-6]")
                && actualTag.matches("h[1-6]")) {

            return true;
        }

        System.out.println(
                "CACHE REJECTED : tag mismatch"
                        + " | expected="
                        + expectedTag
                        + " | actual="
                        + actualTag);

        return false;
    }

    // =====================================================
    // INTENT VALIDATION
    // =====================================================

    private boolean validateExpectedIntent(
            WebElement element,
            FailureContext context) {

        ElementIntent expectedIntent =
                context.getExpectedIntent();

        if (expectedIntent == null
                || expectedIntent
                == ElementIntent.UNKNOWN) {

            return true;
        }

        String tag =
                normalize(
                        element.getTagName());

        String type =
                normalize(
                        element.getAttribute("type"));

        String role =
                normalize(
                        element.getAttribute("role"));

        if (expectedIntent
                == ElementIntent.INPUT) {

            boolean valid =
                    tag.equals("input")
                            || tag.equals("textarea")
                            || "true".equalsIgnoreCase(
                                    element.getAttribute(
                                            "contenteditable"));

            if (!valid) {

                System.out.println(
                        "CACHE REJECTED : expected INPUT"
                                + " but found "
                                + tag);
            }

            return valid;
        }

        if (expectedIntent
                == ElementIntent.BUTTON) {

            boolean valid =
                    tag.equals("button")
                            || (tag.equals("input")
                            && (type.equals("submit")
                            || type.equals("button")
                            || type.equals("reset")));

            if (!valid) {

                System.out.println(
                        "CACHE REJECTED : expected BUTTON"
                                + " but found "
                                + tag);
            }

            return valid;
        }

        if (expectedIntent
                == ElementIntent.LINK) {

            boolean valid =
                    tag.equals("a")
                            || tag.equals("button");

            if (!valid) {

                System.out.println(
                        "CACHE REJECTED : expected LINK"
                                + " but found "
                                + tag);
            }

            return valid;
        }

        if (expectedIntent
                == ElementIntent.DROPDOWN) {

            boolean valid =
                    tag.equals("select")
                            || (tag.equals("input")
                            && role.equals("combobox"));

            if (!valid) {

                System.out.println(
                        "CACHE REJECTED : expected DROPDOWN"
                                + " but found "
                                + tag);
            }

            return valid;
        }

        /*
         * TEXT can be represented by many HTML tags.
         */
        return true;
    }

    // =====================================================
    // ACTION VALIDATION
    // =====================================================

    private boolean validateActionCompatibility(
            WebElement element,
            FailureContext context) {

        ExecutionAction action =
                context.getFailedAction();

        if (action == null) {
            return true;
        }

        String tag =
                normalize(
                        element.getTagName());

        if (action == ExecutionAction.SEND_KEYS
                || action == ExecutionAction.CLEAR) {

            boolean editable =
                    tag.equals("input")
                            || tag.equals("textarea")
                            || "true".equalsIgnoreCase(
                                    element.getAttribute(
                                            "contenteditable"));

            if (!editable) {

                System.out.println(
                        "CACHE REJECTED : "
                                + action
                                + " requires editable element");

                return false;
            }
        }

        /*
         * CLICK is intentionally not restricted to
         * button/a because many frameworks attach click
         * handlers to span/div/menu elements.
         */

        return true;
    }

    // =====================================================
    // TEXT IDENTITY
    // =====================================================

    private boolean validateTextIdentity(
            WebElement element,
            FailureContext context) {

        String expectedText =
                firstMeaningfulText(
                        context.getExpectedText(),
                        context.getLocatorTextHint());

        if (!hasText(expectedText)) {
            return true;
        }

        String actualText =
                element.getText();

        if (!hasText(actualText)) {

            /*
             * Input elements often carry semantic identity
             * through attributes instead of visible text.
             */
            actualText =
                    firstMeaningfulText(
                            element.getAttribute(
                                    "placeholder"),
                            element.getAttribute(
                                    "aria-label"),
                            element.getAttribute(
                                    "value"));
        }

        if (!hasText(actualText)) {

            System.out.println(
                    "CACHE REJECTED : expected text identity"
                            + " but cached element has no text evidence");

            return false;
        }

        String expected =
                normalize(expectedText);

        String actual =
                normalize(actualText);

        if (actual.equals(expected)
                || actual.contains(expected)
                || expected.contains(actual)) {

            return true;
        }

        double similarity =
                calculateSimilarity(
                        expected,
                        actual);

        System.out.println(
                "CACHE TEXT SIMILARITY"
                        + " | expected=["
                        + expectedText
                        + "]"
                        + " | actual=["
                        + actualText
                        + "]"
                        + " | similarity="
                        + similarity);

        if (similarity >= MIN_TEXT_SIMILARITY) {

            return true;
        }

        System.out.println(
                "CACHE REJECTED : text identity changed");

        return false;
    }

    // =====================================================
    // LABEL IDENTITY
    // =====================================================

   private boolean validateLabelIdentity(
        WebDriver driver,
        WebElement element,
        FailureContext context) {

        String expectedLabel =
                context.getExpectedLabel();

        if (!hasText(expectedLabel)) {
            return true;
        }

        /*
         * Label validation is most useful for form controls.
         */
        String tag =
                normalize(
                        element.getTagName());

        if (!tag.equals("input")
                && !tag.equals("textarea")
                && !tag.equals("select")) {

            return true;
        }

        String elementId =
                element.getAttribute("id");

        String actualLabel = "";

        try {

            if (hasText(elementId)) {

                List<WebElement> labels =
                        driver.findElements(
                                By.xpath(
                                        "//label[@for="
                                                + xpathLiteral(
                                                        elementId)
                                                + "]"));

                if (!labels.isEmpty()) {

                    actualLabel =
                            labels.get(0)
                                    .getText();
                }
            }

            /*
             * Fallback for structures where label and
             * control are grouped inside the same container.
             */
            if (!hasText(actualLabel)) {

                actualLabel =
                        element.findElement(
                                By.xpath(
                                        "./ancestor::*[self::div or self::label][1]"))
                                .getText();
            }

        } catch (Exception ignored) {
            // No reliable label available.
        }

        /*
         * Missing DOM label is not enough to invalidate
         * an otherwise semantically valid cached locator.
         */
        if (!hasText(actualLabel)) {
            return true;
        }

        String expected =
                normalize(expectedLabel);

        String actual =
                normalize(actualLabel);

        if (actual.contains(expected)
                || expected.contains(actual)) {

            return true;
        }

        double similarity =
                calculateSimilarity(
                        expected,
                        actual);

        if (similarity >= MIN_TEXT_SIMILARITY) {
            return true;
        }

        System.out.println(
                "CACHE REJECTED : label identity mismatch"
                        + " | expected=["
                        + expectedLabel
                        + "]"
                        + " | actual=["
                        + actualLabel
                        + "]");

        return false;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String firstMeaningfulText(
            String... values) {

        if (values == null) {
            return "";
        }

        for (String value : values) {

            if (hasText(value)) {
                return value;
            }
        }

        return "";
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2")
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll(
                        "[^a-zA-Z0-9 ]",
                        " ")
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

    private double calculateSimilarity(
            String first,
            String second) {

        if (!hasText(first)
                || !hasText(second)) {

            return 0;
        }

        if (first.equals(second)) {
            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        first,
                        second);

        int maxLength =
                Math.max(
                        first.length(),
                        second.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0
                - ((double) distance
                / maxLength);
    }

    private int levenshteinDistance(
            String first,
            String second) {

        int[][] dp =
                new int[first.length() + 1]
                        [second.length() + 1];

        for (int i = 0;
                i <= first.length();
                i++) {

            dp[i][0] = i;
        }

        for (int j = 0;
                j <= second.length();
                j++) {

            dp[0][j] = j;
        }

        for (int i = 1;
                i <= first.length();
                i++) {

            for (int j = 1;
                    j <= second.length();
                    j++) {

                int cost =
                        first.charAt(i - 1)
                                == second.charAt(j - 1)
                                ? 0
                                : 1;

                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1),
                                dp[i - 1][j - 1]
                                        + cost);
            }
        }

        return dp[first.length()]
                [second.length()];
    }

    private String xpathLiteral(
            String value) {

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        StringBuilder result =
                new StringBuilder(
                        "concat(");

        String[] parts =
                value.split("'");

        for (int i = 0;
                i < parts.length;
                i++) {

            if (i > 0) {

                result.append(
                        ", \"'\", ");
            }

            result.append("'")
                    .append(parts[i])
                    .append("'");
        }

        result.append(")");

        return result.toString();
    }
}