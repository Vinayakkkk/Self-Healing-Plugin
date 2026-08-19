package com.vinayak.healing.builder;

import org.openqa.selenium.WebDriver;

import com.vinayak.healing.context.DomContextExtractor;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.expected.ExpectedContext;
import com.vinayak.healing.expected.ExpectedContextManager;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.TargetCardinality;
import com.vinayak.healing.outcome.model.ExpectedElement;
import com.vinayak.healing.outcome.model.ExpectedOutcomeAction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FailureContextBuilder {

        private final ExpectedContextManager expectedContextManager =
        new ExpectedContextManager();

       

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

String resolvedVariableName =
        resolveVariableName(
                variableName,
                failedLocator);

context.setVariableName(
        resolvedVariableName);

String normalizedVariableName =
        normalize(
                resolvedVariableName);

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
String expectedText =
        extractExpectedTextFromException(exception);

context.setExpectedText(expectedText);

context.setLocatorTextHint(
        extractExpectedText(locatorDeclaration));

      String locatorLabel =
        extractExpectedLabel(locatorDeclaration);

if (locatorLabel != null
        && !locatorLabel.isBlank()) {

    context.setLocatorTextHint(
            locatorLabel);
}

HealingLogger.debug(
        "LOCATOR TEXT HINT = "
                + context.getLocatorTextHint());

HealingLogger.debug(
        "EXPECTED TEXT = "
                + context.getExpectedText());







        // -------------------------
        // Browser Information
        // -------------------------

        if (driver != null) {

    try {

        String currentUrl = driver.getCurrentUrl();

        context.setPreviousUrl(currentUrl);

        context.setCurrentUrl(currentUrl);

    } catch (Exception ignored) {
    }

    try {

        context.setExpectedTitle(
                driver.getTitle());

    } catch (Exception ignored) {
    }

    try {

        context.setPageSource(
                buildCombinedPageSource(driver));

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

/*
 * Consume the action that caused this failure.
 *
 * This gives the healing pipeline the failed action
 * while preventing that action from leaking into
 * a later unrelated failure.
 */
ExecutionAction failedAction = null;

if (ExecutionTracker.getContext()
        .getLatestAction() != null) {

    failedAction =
            ExecutionTracker.getContext()
                    .getLatestAction()
                    .getAction();
}

if (failedAction != null
        && failedAction != ExecutionAction.UNKNOWN) {

    context.setFailedAction(
            failedAction);

    HealingLogger.debug(
            "FAILED ACTION = "
                    + context.getFailedAction());
}
context.setExpectedIntent(
        extractExpectedIntent(
                normalizedVariableName,
                locatorDeclaration,
                context.getExpectedTag(),
                context.getExpectedText(),
                context.getFailedAction()));

                context.setTargetCardinality(
        inferTargetCardinality(
                locatorDeclaration,
                executionContext));

HealingLogger.debug(
        "VARIABLE = "
                + normalizedVariableName);

HealingLogger.debug(
        "EXPECTED TAG = "
                + context.getExpectedTag());

HealingLogger.debug(
        "FAILED ACTION = "
                + context.getFailedAction());

HealingLogger.debug(
        "EXPECTED INTENT = "
                + context.getExpectedIntent());

/*
 * Enrich execution context.
 */
enrichFromExecutionContext(
        context,
        executionContext);


/*
 * Resolve expected context only after
 * failedAction has been established.
 */
ExpectedContext expectedContext =
        expectedContextManager.resolve(
                context,
                executionContext);

context.setExpectedContext(
        expectedContext);

if (expectedContext != null) {

    /*
     * Preserve expected text obtained directly from
     * the Selenium execution condition.
     *
     * Example:
     *
     * Selenium:
     * to have text "Products"
     *
     * This is stronger than locator-derived evidence.
     */
    if (isBlank(context.getExpectedText())
            && !isBlank(expectedContext.getExpectedText())) {

        context.setExpectedText(
                expectedContext.getExpectedText());
    }
}



HealingLogger.debug(
        "FINAL EXPECTED TAG = "
                + context.getExpectedTag());

HealingLogger.debug(
        "FINAL EXPECTED INTENT = "
                + context.getExpectedIntent());

                context.setExpectedUrl("");

context.setExpectedElements(
        new ArrayList<ExpectedElement>());

context.setExpectedOutcomeAction(
        ExpectedOutcomeAction.UNKNOWN);



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
        String expectedTag,
        String expectedText,
        ExecutionAction failedAction) {

    String variable =
            variableName == null
                    ? ""
                    : variableName.toLowerCase();

    String locator =
            declaration == null
                    ? ""
                    : declaration.toLowerCase();

    /*
     * =====================================================
     * 1. ACTION HAS HIGHEST PRIORITY
     * =====================================================
     *
     * The action tells us what the test was actually
     * trying to do.
     *
     * SEND_KEYS / CLEAR -> INPUT
     *
     * Exception text must NOT override this.
     */

    if (failedAction != null) {

        switch (failedAction) {

            case SEND_KEYS:
            case CLEAR:
                return ElementIntent.INPUT;

            case CHECKBOX:
                return ElementIntent.CHECKBOX;

            case RADIO:
                return ElementIntent.RADIO;

            case SELECT:
                return ElementIntent.DROPDOWN;

            default:
                break;
        }
    }

    /*
     * =====================================================
     * 2. VARIABLE NAME SEMANTICS
     * =====================================================
     */

    ElementIntent variableIntent =
            inferIntentFromVariable(variable);

    if (variableIntent != ElementIntent.UNKNOWN) {
        return variableIntent;
    }

    /*
     * =====================================================
     * 3. LOCATOR SEMANTICS
     * =====================================================
     */

    if (containsExplicitLocatorIntent(locator)) {

        ElementIntent locatorIntent =
                inferIntentFromLocator(locator);

        if (locatorIntent != ElementIntent.UNKNOWN) {
            return locatorIntent;
        }
    }

    /*
     * =====================================================
     * 4. EXPECTED TAG
     * =====================================================
     */

    if (expectedTag != null
            && !expectedTag.isBlank()) {

        String tag =
                expectedTag
                        .trim()
                        .toLowerCase();

        switch (tag) {

            case "input":
            case "textarea":
                return ElementIntent.INPUT;

            case "button":
                return ElementIntent.BUTTON;

            case "select":
                return ElementIntent.DROPDOWN;

            case "a":
                return ElementIntent.LINK;

            default:
                break;
        }
    }

    /*
     * =====================================================
     * 5. EXPECTED TEXT
     * =====================================================
     *
     * Text is only a fallback.
     *
     * It must NEVER override SEND_KEYS.
     */

    if (expectedText != null
            && !expectedText.isBlank()) {

        return ElementIntent.TEXT;
    }

    /*
     * =====================================================
     * 6. UNKNOWN
     * =====================================================
     */

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

private String resolveVariableName(
        String variableName,
        String failedLocator) {

    if (variableName != null
            && !variableName.isBlank()
            && !"DIRECT_LOCATOR".equalsIgnoreCase(variableName)) {

        return variableName;
    }

    /*
     * Never derive a Java variable name from
     * the failed locator.
     *
     * Example:
     *
     * By.className: oxd-buzz
     *
     * oxd-buzz is the locator value,
     * NOT the Page Object variable name.
     *
     * If source analysis could not resolve
     * the variable, mark it as DIRECT_LOCATOR.
     */

    return "DIRECT_LOCATOR";
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
private String extractExpectedLabel(
        String locatorDeclaration) {

    if (locatorDeclaration == null
            || locatorDeclaration.isBlank()) {

        return "";
    }

    /*
     * Extract label text from generic XPath structures.
     *
     * Examples:
     *
     * //label[text()='Username']/...
     * //label[normalize-space()='Employee Name']/...
     * //label[contains(text(),'Email')]/...
     * //label[contains(normalize-space(),'Phone')]/...
     *
     * Nothing here is application-specific.
     */

    Pattern pattern =
            Pattern.compile(
                    "//label\\s*\\[\\s*"
                    + "(?:text\\(\\)|normalize-space\\(\\))"
                    + "\\s*=\\s*['\"]([^'\"]+)['\"]"
                    + "\\s*\\]"
                    + "|"
                    + "//label\\s*\\[\\s*"
                    + "contains\\s*\\(\\s*"
                    + "(?:text\\(\\)|normalize-space\\(\\))"
                    + "\\s*,\\s*['\"]([^'\"]+)['\"]"
                    + "\\s*\\)"
                    + "\\s*\\]",
                    Pattern.CASE_INSENSITIVE);

    Matcher matcher =
            pattern.matcher(locatorDeclaration);

    if (!matcher.find()) {
        return "";
    }

    String exactLabel =
            matcher.group(1);

    if (exactLabel != null
            && !exactLabel.isBlank()) {

        return exactLabel.trim();
    }

    String containsLabel =
            matcher.group(2);

    if (containsLabel != null
            && !containsLabel.isBlank()) {

        return containsLabel.trim();
    }

    return "";
}
private String extractExpectedTextFromException(
        Throwable exception) {

    if (exception == null) {
        return "";
    }

    /*
     * Walk through the complete exception cause chain.
     *
     * Selenium/framework code may wrap the original
     * TimeoutException inside another exception.
     *
     * We must inspect every cause instead of relying
     * only on exception.getMessage().
     */
    Throwable current = exception;

    while (current != null) {

        String message = current.getMessage();

        if (message != null
                && !message.isBlank()) {

            /*
             * Generic support for Selenium conditions:
             *
             * to have text "..."
             * to contain text "..."
             *
             * The actual expected value is captured
             * dynamically. Nothing is hardcoded.
             */
            Pattern pattern =
                    Pattern.compile(
                            "(?:to\\s+(?:have|contain)\\s+text)"
                                    + "\\s+[\"']([^\"']+)[\"']",
                            Pattern.CASE_INSENSITIVE);

            Matcher matcher =
                    pattern.matcher(message);

            if (matcher.find()) {

                String expectedText =
                        matcher.group(1);

                if (expectedText != null
                        && !expectedText.isBlank()) {

                    return expectedText.trim();
                }
            }
        }

        /*
         * Move to the underlying exception.
         */
        current = current.getCause();
    }

    return "";
}
private void enrichFromExecutionContext(
        FailureContext context,
        ExecutionContext executionContext) {

                System.out.println("\n========== EXECUTION CONTEXT ==========");

System.out.println(
        "ExecutionContext = "
                + executionContext);

System.out.println(
        "LastSuccessful = "
                + (executionContext == null
                ? null
                : executionContext.getLastSuccessfulStep()));

    if (context == null
            || executionContext == null
            || executionContext.getLastSuccessfulStep() == null) {

        return;
    }

    var step =
            executionContext.getLastSuccessfulStep();

    if (isBlank(context.getParentTag())) {

        context.setParentTag(
                step.getParentTag());
    }

    if (isBlank(context.getParentId())) {

        context.setParentId(
                step.getParentId());
    }

    if (isBlank(context.getParentClass())) {

        context.setParentClass(
                step.getParentClass());
    }

    if (isBlank(context.getNearestLabel())) {

        context.setNearestLabel(
                step.getNearestLabel());
    }
   



    HealingLogger.debug(
            "===== EXECUTION CONTEXT ENRICHMENT =====");

    HealingLogger.debug(
            "ParentTag      : "
                    + context.getParentTag());

    HealingLogger.debug(
            "ParentId       : "
                    + context.getParentId());

    HealingLogger.debug(
            "ParentClass    : "
                    + context.getParentClass());

    HealingLogger.debug(
            "NearestLabel   : "
                    + context.getNearestLabel());

    HealingLogger.debug(
            "ExpectedText   : "
                    + context.getExpectedText());

    HealingLogger.debug(
            "========================================");
}
private String buildCombinedPageSource(WebDriver driver) {

    StringBuilder html = new StringBuilder();

    try {

        driver.switchTo().defaultContent();

        html.append(driver.getPageSource());

        List<org.openqa.selenium.WebElement> iframes =
                driver.findElements(
                        org.openqa.selenium.By.tagName("iframe"));

        for (int i = 0; i < iframes.size(); i++) {

            try {

                driver.switchTo().defaultContent();
                driver.switchTo().frame(i);

                html.append("\n<!-- IFRAME START -->\n");
                html.append(driver.getPageSource());
                html.append("\n<!-- IFRAME END -->\n");

            } catch (Exception ignored) {

            }
        }

    } finally {

        driver.switchTo().defaultContent();
    }

    return html.toString();
}
private boolean isBlank(String value) {

    return value == null
            || value.isBlank();
}
private ElementIntent inferIntentFromVariable(
        String variableName) {

    if (variableName == null
            || variableName.isBlank()) {

        return ElementIntent.UNKNOWN;
    }

    String normalized =
            normalize(variableName);

    String[] tokens =
            normalized.split("\\s+");

    /*
     * Prefer exact semantic tokens.
     *
     * Example:
     *
     * loginButton
     *     -> login + button
     *     -> BUTTON
     *
     * rememberCheckbox
     *     -> remember + checkbox
     *     -> CHECKBOX
     */
    for (String token : tokens) {

        ElementIntent intent =
                intentFromToken(token);

        if (intent != ElementIntent.UNKNOWN) {
            return intent;
        }
    }

    /*
     * Handle enum names that may be represented
     * as multiple words.
     *
     * Example:
     *
     * checkBox -> checkbox
     * radioButton -> radiobutton
     */
    String compactVariable =
            normalized.replace(" ", "");

    for (ElementIntent intent :
            ElementIntent.values()) {

        if (intent == ElementIntent.UNKNOWN) {
            continue;
        }

        String intentName =
                intent.name()
                        .toLowerCase();

        if (compactVariable.contains(intentName)) {
            return intent;
        }
    }

    return ElementIntent.UNKNOWN;
}
private ElementIntent intentFromToken(
        String token) {

    if (token == null
            || token.isBlank()) {

        return ElementIntent.UNKNOWN;
    }

    for (ElementIntent intent :
            ElementIntent.values()) {

        if (intent == ElementIntent.UNKNOWN) {
            continue;
        }

        if (intent.name()
                .equalsIgnoreCase(token)) {

            return intent;
        }
    }

    return ElementIntent.UNKNOWN;
}

private boolean containsExplicitLocatorIntent(
        String locator) {

    if (locator == null || locator.isBlank()) {
        return false;
    }

    String normalized =
            locator.toLowerCase().trim();

    return normalized.contains("//button")
            || normalized.contains("/button")
            || normalized.contains("//input")
            || normalized.contains("/input")
            || normalized.contains("//textarea")
            || normalized.contains("/textarea")
            || normalized.contains("//select")
            || normalized.contains("/select")
            || normalized.contains("//a")
            || normalized.contains("/a[")
            || normalized.contains("@type='checkbox'")
            || normalized.contains("@type=\"checkbox\"")
            || normalized.contains("@type='radio'")
            || normalized.contains("@type=\"radio\"")
            || normalized.contains("@type='submit'")
            || normalized.contains("@type=\"submit\"");
}
private ElementIntent inferIntentFromLocator(
        String locator) {

    if (locator == null || locator.isBlank()) {
        return ElementIntent.UNKNOWN;
    }

    String normalized =
            locator.toLowerCase().trim();

    /*
     * INPUT
     */
    if (normalized.contains("//input")
            || normalized.contains("/input")
            || normalized.contains("//textarea")
            || normalized.contains("/textarea")) {

        return ElementIntent.INPUT;
    }

    /*
     * BUTTON
     */
    if (normalized.contains("//button")
            || normalized.contains("/button")
            || normalized.contains("@type='submit'")
            || normalized.contains("@type=\"submit\"")) {

        return ElementIntent.BUTTON;
    }

    /*
     * DROPDOWN
     */
    if (normalized.contains("//select")
            || normalized.contains("/select")) {

        return ElementIntent.DROPDOWN;
    }

    /*
     * LINK
     */
    if (normalized.contains("//a")
            || normalized.contains("/a[")) {

        return ElementIntent.LINK;
    }

    /*
     * CHECKBOX
     */
    if (normalized.contains("@type='checkbox'")
            || normalized.contains("@type=\"checkbox\"")) {

        return ElementIntent.CHECKBOX;
    }

    /*
     * RADIO
     */
    if (normalized.contains("@type='radio'")
            || normalized.contains("@type=\"radio\"")) {

        return ElementIntent.RADIO;
    }

    return ElementIntent.UNKNOWN;
}
private TargetCardinality inferTargetCardinality(
        String locatorDeclaration,
        ExecutionContext executionContext) {

    /*
     * Execution/source evidence is stronger than
     * variable-name guessing.
     *
     * At this stage the framework does not yet have
     * direct access to the original Selenium API call,
     * so do not guess COLLECTION from the variable name.
     *
     * Keep the context UNKNOWN until reliable evidence
     * is available.
     */
    return TargetCardinality.UNKNOWN;
}
}