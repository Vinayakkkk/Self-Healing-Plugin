package com.vinayak.healing.builder;

import org.openqa.selenium.WebDriver;

import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FailureContextBuilder {

    public FailureContext build(
            WebDriver driver,
            Throwable exception,
            String failedLocator,
            String variableName,
            String locatorDeclaration) {

        FailureContext context =
                new FailureContext();

        context.setDriver(driver);

        // -------------------------
        // Failure Information
        // -------------------------

        context.setException(exception);

        context.setFailureTime(
                System.currentTimeMillis());

        context.setFailedLocator(
                failedLocator);

      String normalizedVariableName =
        normalize(variableName);

context.setVariableName(
        normalizedVariableName);

context.setLocatorDeclaration(
        locatorDeclaration);

context.setExpectedTag(
        extractExpectedTag(
                locatorDeclaration));

/*
 * Text inside a failed XPath/CSS locator is not necessarily
 * the text expected by the test.
 *
 * Example:
 * failed locator: //div[normalize-space()='Cart Total']
 * real expected value during execution: "1"
 *
 * Until execution/wait-condition arguments are captured,
 * do not use locator text as a strict candidate filter.
 */
context.setExpectedText("");

String locatorTextHint =
        extractExpectedText(
                locatorDeclaration);

context.setLocatorTextHint(
        locatorTextHint);

HealingLogger.debug(
        "LOCATOR TEXT HINT = "
                + context.getLocatorTextHint());

HealingLogger.debug(
        "EXPECTED TEXT = "
                + context.getExpectedText());





context.setExpectedIntent(
        extractExpectedIntent(
                normalizedVariableName,
                locatorDeclaration,
                context.getExpectedTag()));

HealingLogger.debug(
        "VARIABLE = "
                + normalizedVariableName);

        HealingLogger.debug(
                "EXPECTED TAG    = "
                        + context.getExpectedTag());

        HealingLogger.debug(
                "EXPECTED INTENT = "
                        + context.getExpectedIntent());

        // -------------------------
        // Browser Information
        // -------------------------

        if (driver != null) {

            try {
                context.setCurrentUrl(
                        driver.getCurrentUrl());
            } catch (Exception ignored) {
            }

            try {
                context.setPageSource(
                        driver.getPageSource());
            } catch (Exception ignored) {
            }
        }

        // -------------------------
        // Execution Information
        // -------------------------
// -------------------------
// Execution Information
// -------------------------

ExecutionContext executionContext =
        ExecutionTracker.getContext();

context.setExecutionContext(
        executionContext);

if (executionContext != null
        && executionContext.getLatestAction() != null
        && executionContext
                .getLatestAction()
                .getAction() != null) {

    context.setFailedAction(
            executionContext
                    .getLatestAction()
                    .getAction());

    HealingLogger.debug(
            "FAILED ACTION = "
                    + context.getFailedAction());

    switch (context.getFailedAction()) {

        case SEND_KEYS:
        case CLEAR:

            /*
             * SEND_KEYS/CLEAR strongly proves that
             * the failed element must be editable.
             *
             * TEXT here means the semantic inference
             * is wrong for this runtime operation.
             */
            if (context.getExpectedIntent() == null
                    || context.getExpectedIntent()
                            == ElementIntent.UNKNOWN
                    || context.getExpectedIntent()
                            == ElementIntent.TEXT) {

                context.setExpectedIntent(
                        ElementIntent.INPUT);
            }

            break;

        case CLICK:

            /*
             * CLICK alone must not force BUTTON.
             *
             * Keep strong source-code semantics:
             * BUTTON, LINK, etc.
             *
             * But a stale TEXT classification should
             * not be used as a strict interactive
             * candidate filter for a failed click.
             */
            if (context.getExpectedIntent()
                    == ElementIntent.TEXT
                    && context.getExpectedTag() == null) {

                context.setExpectedIntent(
                        ElementIntent.UNKNOWN);
            }

            break;

        default:
            break;
    }
}

HealingLogger.debug(
        "FINAL EXPECTED TAG = "
                + context.getExpectedTag());

HealingLogger.debug(
        "FINAL EXPECTED INTENT = "
                + context.getExpectedIntent());

return context;
    }

    // =====================================================
    // EXPECTED TAG
    // =====================================================
private String extractExpectedTag(String declaration) {

    if (declaration == null || declaration.isBlank()) {
        return null;
    }

    String lower = declaration.toLowerCase();

    // XPath: find the final target tag in the XPath.
    if (lower.contains("by.xpath")) {

        String xpath = lower;

        int firstQuote = xpath.indexOf('"');
        int lastQuote = xpath.lastIndexOf('"');

        if (firstQuote >= 0 && lastQuote > firstQuote) {
            xpath = xpath.substring(firstQuote + 1, lastQuote);
        }

        String[] tags = {
                "input", "button", "textarea", "select",
                "a", "img", "span", "label", "div",
                "table", "tr", "td", "li",
                "h1", "h2", "h3", "h4", "h5", "h6"
        };

        int lastPosition = -1;
        String expectedTag = null;

        for (String tag : tags) {

            int position = xpath.lastIndexOf("/" + tag);

            if (position > lastPosition) {
                lastPosition = position;
                expectedTag = tag;
            }
        }

        return expectedTag;
    }

    // CSS selector: target tag is the first HTML tag in selector.
    // input[placeholder='...'] -> input
    // button.login -> button
    // div > input[name='email'] -> input
    if (lower.contains("by.cssselector")) {

        int firstQuote = lower.indexOf('"');
        int lastQuote = lower.lastIndexOf('"');

        if (firstQuote >= 0 && lastQuote > firstQuote) {

            String css =
                    lower.substring(
                            firstQuote + 1,
                            lastQuote);

            java.util.regex.Pattern pattern =
                    java.util.regex.Pattern.compile(
                            "(?:^|\\s|>|\\+|~)(input|button|textarea|select|a|img|span|label|div|table|tr|td|li|h1|h2|h3|h4|h5|h6)(?=[\\[.#:\\s>+~]|$)");

            java.util.regex.Matcher matcher =
                    pattern.matcher(css);

            String expectedTag = null;

            while (matcher.find()) {
                expectedTag = matcher.group(1);
            }

            return expectedTag;
        }
    }

    // By.id, By.name, By.className do not reveal an HTML tag.
    // Return null. Intent/action filtering will decide.
    return null;
}

    // =====================================================
    // EXPECTED INTENT
    // =====================================================

private ElementIntent extractExpectedIntent(
        String variableName,
        String declaration,
        String expectedTag) {

    String variable =
            variableName == null
                    ? ""
                    : variableName.toLowerCase();

    String locator =
            declaration == null
                    ? ""
                    : declaration.toLowerCase();

    String tag =
            expectedTag == null
                    ? ""
                    : expectedTag.toLowerCase();

    // ==========================================
    // 1. STRONG TAG EVIDENCE
    // ==========================================

    if ("input".equals(tag)
            || "textarea".equals(tag)) {

        return ElementIntent.INPUT;
    }

    if ("select".equals(tag)) {

        return ElementIntent.DROPDOWN;
    }

    if ("button".equals(tag)) {

        return ElementIntent.BUTTON;
    }

    if ("a".equals(tag)) {

        return ElementIntent.LINK;
    }

    /*
     * If source code explicitly targets a normal
     * text/container tag, generic words such as
     * "name" must NOT classify it as INPUT.
     */
    if ("div".equals(tag)
            || "span".equals(tag)
            || "label".equals(tag)
            || "h1".equals(tag)
            || "h2".equals(tag)
            || "h3".equals(tag)
            || "h4".equals(tag)
            || "h5".equals(tag)
            || "h6".equals(tag)
            || "td".equals(tag)) {

        return ElementIntent.TEXT;
    }

    // ==========================================
    // 2. LOCATOR STRUCTURE
    // ==========================================

    if (locator.contains("//input")
            || locator.contains("/input")
            || locator.contains("//textarea")
            || locator.contains("/textarea")) {

        return ElementIntent.INPUT;
    }

    if (locator.contains("//select")
            || locator.contains("/select")) {

        return ElementIntent.DROPDOWN;
    }

    if (locator.contains("//button")
            || locator.contains("/button")
            || locator.contains("@type='submit'")
            || locator.contains("@type=\"submit\"")
            || locator.contains("@type='button'")
            || locator.contains("@type=\"button\"")) {

        return ElementIntent.BUTTON;
    }

    if (locator.contains("//a")
            || locator.contains("/a[")) {

        return ElementIntent.LINK;
    }

    // ==========================================
    // 3. VARIABLE SEMANTICS
    // ==========================================

    if (variable.matches(
        ".*(input|text\\s*area|text\\s*box|text\\s*field|"
                + "username|password|email|search|"
                + "field|name|number|date|code|"
                + "identifier|postal|zip|phone|address).*")) {

    return ElementIntent.INPUT;
}

    if (variable.matches(
            ".*(dropdown|select|combo|option).*")) {

        return ElementIntent.DROPDOWN;
    }

    if (variable.matches(
            ".*(button|btn|submit|save|cancel|login|"
                    + "logout|apply|reset|continue).*")) {

        return ElementIntent.BUTTON;
    }

    if (variable.matches(
            ".*(link|menu|url|navigation|nav|icon).*")) {

        return ElementIntent.LINK;
    }

    if (variable.matches(
        ".*(title|header|heading|label|text|message|"
                + "badge|price|description).*")) {

    return ElementIntent.TEXT;
}

    return ElementIntent.UNKNOWN;
}

    // =====================================================
    // VARIABLE NORMALIZATION
    // =====================================================

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
                        "\\s+",
                        " ")

                .trim()

                .toLowerCase();
    }

private String extractExpectedText(
        String locatorDeclaration) {

    if (locatorDeclaration == null
            || locatorDeclaration.isBlank()) {
        return "";
    }

    Pattern pattern = Pattern.compile(
            "(?:text\\(\\)|\\.|normalize-space\\(\\))"
                    + "\\s*=\\s*['\"]([^'\"]+)['\"]"
                    + "|"
                    + "contains\\s*\\(\\s*"
                    + "(?:text\\(\\)|\\.|normalize-space\\(\\))"
                    + "\\s*,\\s*['\"]([^'\"]+)['\"]\\s*\\)");

    Matcher matcher =
            pattern.matcher(locatorDeclaration);

    if (!matcher.find()) {
        return "";
    }

    String exactText =
            matcher.group(1);

    if (exactText != null
            && !exactText.isBlank()) {
        return exactText.trim();
    }

    String containsText =
            matcher.group(2);

    if (containsText != null
            && !containsText.isBlank()) {
        return containsText.trim();
    }

    return "";
}

}