package com.vinayak.healing.validator;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.engine.LocatorBuilder;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import org.openqa.selenium.*;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.iframe.IframeHealingEngine;


import java.util.List;

public class CandidateValidator {

        private static final double MIN_HEAL_SCORE = 1.0;

        private final IframeHealingEngine
        iframeHealingEngine =
        new IframeHealingEngine();

   public LocatorCandidate validate(
        WebDriver driver,
        List<LocatorCandidate> candidates,
        FailureContext context) {

        if (driver == null
                || candidates == null
                || candidates.isEmpty()) {

            return null;
        }

        LocatorCandidate bestCandidate = null;

double bestScore = Double.NEGATIVE_INFINITY;

for (LocatorCandidate candidate : candidates) {



if (!isCompatibleWithExpectedIntent(candidate, context)) {

    System.out.println(
            "FAILED: isCompatibleWithExpectedIntent -> "
            + candidate.getLocatorType()
            + "="
            + candidate.getLocatorValue());

    continue;
}

try {

    By locator =
            LocatorBuilder.build(candidate);

    WebElement element =
            findSingleElement(
                    driver,
                    locator);

    if (element == null) {

         System.out.println(
            "FAILED: element == null -> "
            + candidate.getLocatorType()
            + "="
            + candidate.getLocatorValue());
        continue;
    }

if (!validateDisplayed(element)) {
    System.out.println("FAILED: validateDisplayed");
    continue;
}

if (!validateEnabled(element)) {
    System.out.println("FAILED: validateEnabled");
    continue;
}

if (!validateActionCompatibility(element, context)) {
    System.out.println("FAILED: validateActionCompatibility");
    continue;
}

if (!validateIntent(element, candidate, context)) {
    System.out.println("FAILED: validateIntent");
    continue;
}

if (!validateFieldLabel(candidate, context)) {
    System.out.println("FAILED: validateFieldLabel");
    continue;
}

if (!validateTag(element, candidate)) {
    System.out.println("FAILED: validateTag");
    continue;
}

if (!validateLocatorAttribute(element, candidate)) {
    System.out.println("FAILED: validateLocatorAttribute");
    continue;
}

if (!validateExpectedText(element, context)) {
    System.out.println("FAILED: validateExpectedText");
    continue;
}

    if (candidate.getFinalScore() > bestScore) {
        bestScore = candidate.getFinalScore();
        bestCandidate = candidate;
    }

}catch (Exception e) {

    System.out.println("VALIDATOR EXCEPTION");
    e.printStackTrace();
}


}



if (bestCandidate != null
        && bestCandidate.getFinalScore() >= MIN_HEAL_SCORE) {

    return bestCandidate;
}

HealingAnalytics.validationFailure();

return null;
    }

    // =====================================================
    // FIND SINGLE ELEMENT
    // =====================================================

private WebElement findSingleElement(
        WebDriver driver,
        By locator) {

  List<WebElement> elements = List.of();

try {

    elements = driver.findElements(locator);

    System.out.println("\n===== NORMAL DOM SEARCH =====");
System.out.println("Locator : " + locator);
System.out.println("Matches : " + elements.size());

if (locator.toString().startsWith("By.cssSelector:")) {
    System.out.println(
            "duplicate-card exists : "
                    + driver.findElements(By.id("duplicate-card")).size());
}

System.out.println("=============================\n");

} catch (Exception e) {

    System.out.println(
            "Driver search failed for: "
                    + locator);

    e.printStackTrace();
}

if (elements.size() == 1) {
    return elements.get(0);
}

if (elements.size() > 1) {
    return null;
}

    JavascriptExecutor js =
            (JavascriptExecutor) driver;

    List<WebElement> hosts =
            driver.findElements(By.cssSelector("*"));

    for (WebElement host : hosts) {

        try {
            System.out.println(
                "Checking host : "
                        + host.getTagName()
                        + " id="
                        + host.getAttribute("id"));

            Object hasShadow =
                    js.executeScript(
                            "return arguments[0].shadowRoot != null;",
                            host);

            if (!(hasShadow instanceof Boolean)
                    || !((Boolean) hasShadow)) {
                continue;
            }

            SearchContext shadowRoot =
                    host.getShadowRoot();
                    System.out.println(
        "Shadow root found on : "
                + host.getTagName()
                + " id="
                + host.getAttribute("id"));

            By shadowLocator = locator;

String locatorString = locator.toString();

if (locatorString.startsWith("By.id: ")) {

    shadowLocator = By.cssSelector(
            "#" + locatorString.replace("By.id: ", ""));

} else if (locatorString.startsWith("By.name: ")) {

    String value =
            locatorString.replace("By.name: ", "");

    shadowLocator =
            By.cssSelector(
                    "[name='" + value + "']");

} else if (locatorString.startsWith("By.className: ")) {

    shadowLocator =
            By.cssSelector(
                    "." + locatorString.replace("By.className: ", ""));
}

if (locatorString.startsWith("By.xpath:")) {
    continue;
}

List<WebElement> shadowElements =
        shadowRoot.findElements(shadowLocator);
                    System.out.println(
        "Searching in shadow root using:"
                + locator
                + " inside shadow root -> "
                + shadowElements.size());

            if (shadowElements.size() == 1) {
                return shadowElements.get(0);
            }

            if (shadowElements.size() > 1) {
                return null;
            }

        } 
        catch (Exception e) {

    System.out.println(
            "Shadow search exception for locator: "
                    + locator);

    e.printStackTrace();
}
    }

   /*
 * Normal DOM and Shadow DOM search failed.
 * Try iframe search before giving up.
 */
try {

    WebElement iframeElement =
            iframeHealingEngine.findElement(
                    driver,
                    locator);

    if (iframeElement != null) {

        System.out.println(
                "Element validated inside iframe.");

        return iframeElement;
    }

} catch (NoSuchElementException ignored) {
    // Element not found in any iframe.
}

return null;
}

    // =====================================================
    // DISPLAY VALIDATION
    // =====================================================

    private boolean validateDisplayed(
            WebElement element) {

        if (!element.isDisplayed()) {

         

            return false;
        }

        return true;
    }

    // =====================================================
    // ENABLED VALIDATION
    // =====================================================

    private boolean validateEnabled(
            WebElement element) {

        if (!element.isEnabled()) {

           

            return false;
        }

        return true;
    }

    // =====================================================
    // INTENT VALIDATION
    // =====================================================



private boolean validateIntent(
        WebElement element,
        LocatorCandidate candidate,
        FailureContext context) {

    try {

        ElementIntent expectedIntent =
                context == null
                        ? ElementIntent.UNKNOWN
                        : context.getExpectedIntent();

        if (expectedIntent == null
                || expectedIntent == ElementIntent.UNKNOWN) {

            return true;
        }

        String tag =
                element.getTagName()
                        .toLowerCase();

        String type =
                element.getAttribute("type");

        // ==========================================
        // INPUT: sendKeys / clear must use a real input
        // ==========================================

        if (expectedIntent == ElementIntent.INPUT) {

            boolean validInput =
                    tag.equals("input")
                            || tag.equals("textarea")
                            || "true".equalsIgnoreCase(
                                    element.getAttribute(
                                            "contenteditable"));

            if (!validInput) {

               

                return false;
            }

            String readonly =
                    element.getAttribute("readonly");

            if (readonly != null
                    && !readonly.isBlank()) {

                

                return false;
            }

            return element.isEnabled();
        }

        // ==========================================
        // BUTTON: click action
        // ==========================================

        if (expectedIntent == ElementIntent.BUTTON) {

            boolean validButton =
                    tag.equals("button")
                            || (tag.equals("input")
                            && ("submit".equalsIgnoreCase(type)
                            || "button".equalsIgnoreCase(type)
                            || "reset".equalsIgnoreCase(type)));

            if (!validButton) {

                

                return false;
            }

            return element.isEnabled();
        }

        // ==========================================
        // LINK: navigation/click target
        // ==========================================

        if (expectedIntent == ElementIntent.LINK) {

            boolean validLink =
                    tag.equals("a")
                            || tag.equals("button");

            if (!validLink) {

               

                return false;
            }

            return element.isEnabled();
        }

        // ==========================================
        // DROPDOWN
        // ==========================================

        if (expectedIntent == ElementIntent.DROPDOWN) {

            boolean validDropdown =
                    tag.equals("select")
                            || (tag.equals("input")
                            && "combobox".equalsIgnoreCase(
                                    element.getAttribute("role")));

            if (!validDropdown) {

              

                return false;
            }

            return element.isEnabled();
        }

        return true;

    } catch (Exception e) {

        

        return false;
    }
}
        // =====================================================
    // TAG VALIDATION
    // =====================================================

    private boolean validateTag(
            WebElement element,
            LocatorCandidate candidate) {

        if (candidate.getTagName() == null
                || candidate.getTagName().isBlank()) {

            return true;
        }

        String actualTag =
                element.getTagName();

        if (!tagMatches(
                candidate.getTagName(),
                actualTag)) {

           

           

            return false;
        }

        return true;
    }

    // =====================================================
    // LOCATOR ATTRIBUTE VALIDATION
    // =====================================================

    private boolean validateLocatorAttribute(
            WebElement element,
            LocatorCandidate candidate) {

        String locatorType =
                candidate.getLocatorType();

        String locatorValue =
                candidate.getLocatorValue();

        if(locatorType == null
                || locatorValue == null){

            return true;
        }

        switch(locatorType.toLowerCase()){

            case "id":

                return attributeEquals(
                        element.getAttribute("id"),
                        locatorValue);

            case "name":

                return attributeEquals(
                        element.getAttribute("name"),
                        locatorValue);

            case "class":

                String className =
                        element.getAttribute("class");

                return className != null
                        && className.contains(locatorValue);

            case "placeholder":

                return attributeEquals(
                        element.getAttribute("placeholder"),
                        locatorValue);

            case "aria-label":

                return attributeEquals(
                        element.getAttribute("aria-label"),
                        locatorValue);

            case "data-test":

                return attributeEquals(
                        element.getAttribute("data-test"),
                        locatorValue);

            case "data-testid":

                return attributeEquals(
                        element.getAttribute("data-testid"),
                        locatorValue);

            case "data-qa":

                return attributeEquals(
                        element.getAttribute("data-qa"),
                        locatorValue);

            case "data-cy":

                return attributeEquals(
                        element.getAttribute("data-cy"),
                        locatorValue);

            case "href":

    String href =
            element.getAttribute("href");

    return href != null
            && href.endsWith(locatorValue);

            default:

                return true;
        }
    }

    // =====================================================
    // ATTRIBUTE COMPARISON
    // =====================================================

    private boolean attributeEquals(
            String actual,
            String expected){

        if(actual == null
                || expected == null){

            return false;
        }

        return actual.trim()
                .equalsIgnoreCase(
                        expected.trim());
    }

    // =====================================================
    // GENERIC TAG MATCHING
    // =====================================================

    private boolean tagMatches(
            String expected,
            String actual){

        if(expected == null
                || actual == null){

            return true;
        }

        expected =
                expected.toLowerCase().trim();

        actual =
                actual.toLowerCase().trim();

        if(expected.equals(actual)){
            return true;
        }

        // Input family

        if(expected.equals("input")){

            return actual.equals("input")
                    || actual.equals("textarea")
                    || actual.equals("select");
        }

        // Heading family

        if(expected.matches("h[1-6]")){

            return actual.matches("h[1-6]");
        }

        return false;
    }

private boolean validateExpectedText(
        WebElement element,
        FailureContext context) {

    if (context == null) {
        return true;
    }

    String expectedText =
            context.getExpectedText();

    if (expectedText == null
            || expectedText.isBlank()) {

        return true;
    }

String actualText =
        element.getText();

/*
 * Input/textarea values are stored in the value attribute,
 * not as visible text.
 */
if (actualText == null || actualText.isBlank()) {

    actualText =
            element.getAttribute("value");
}

System.out.println("\n===== TEXT VALIDATION =====");
System.out.println("Expected : " + expectedText);
System.out.println("Actual   : " + actualText);
System.out.println("Tag      : " + element.getTagName());
System.out.println("Value    : " + element.getAttribute("value"));
System.out.println("===========================\n");

if (actualText == null
        || actualText.isBlank()) {

    return false;
}

    String expected =
            normalizeText(expectedText);

    String actual =
            normalizeText(actualText);

    // Exact or containment match
    if (actual.equals(expected)
            || actual.contains(expected)
            || expected.contains(actual)) {

        return true;
    }

    // Typo-tolerant comparison
    double similarity =
            calculateTextSimilarity(
                    expected,
                    actual);

   

    /*
     * Failed locator text may itself contain a typo.
     *
     * Example:
     * My Inffo -> My Info
     *
     * We accept a strong fuzzy match instead of
     * requiring the broken locator text to match exactly.
     */
    if (similarity >= 0.75) {

        

        return true;
    }

  

    return false;
}

private double calculateTextSimilarity(
        String first,
        String second) {

    if (first == null || second == null) {
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
            - ((double) distance / maxLength);
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

private boolean validateFieldLabel(
        LocatorCandidate candidate,
        FailureContext context) {

    if (context == null) {
        return true;
    }

    if (context.getExpectedIntent() != ElementIntent.INPUT) {
        return true;
    }

    String variableName = context.getVariableName();
    String nearestLabel = candidate.getNearestLabel();

    /*
     * Missing information should never reject a candidate.
     */
    if (variableName == null
            || variableName.isBlank()
            || nearestLabel == null
            || nearestLabel.isBlank()) {

        return true;
    }

    String normalizedVariable =
            normalizeText(variableName);

    String normalizedLabel =
            normalizeText(nearestLabel);

    String[] variableTokens =
            normalizedVariable.split("\\s+");

    int meaningfulTokens = 0;
    int matchedTokens = 0;

    for (String token : variableTokens) {

        if (token.length() < 3) {
            continue;
        }

        if (isGenericToken(token)) {
            continue;
        }

        meaningfulTokens++;

        if (normalizedLabel.contains(token)) {
            matchedTokens++;
        }
    }

    /*
     * Label mismatch is only informational.
     * Do not reject an otherwise valid candidate.
     */
    if (meaningfulTokens > 0 && matchedTokens == 0) {

        System.out.println(
                "Label mismatch ignored for: "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());
    }

    return true;
}

private String normalizeText(String value) {

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

private boolean isGenericToken(String token) {

    return token.equals("input")
            || token.equals("field")
            || token.equals("textbox")
            || token.equals("text")
            || token.equals("box")
            || token.equals("value")
            || token.equals("element")
            || token.equals("control");
}

private boolean isCompatibleWithExpectedIntent(
        LocatorCandidate candidate,
        FailureContext context) {

    if (context == null) {
        return true;
    }

    ElementIntent expectedIntent =
            context.getExpectedIntent();

    ExecutionAction failedAction =
            context.getFailedAction();

    String tag =
            candidate.getTagName() == null
                    ? ""
                    : candidate.getTagName()
                            .trim()
                            .toLowerCase();

    ElementIntent candidateIntent =
            candidate.getIntent() == null
                    ? ElementIntent.UNKNOWN
                    : candidate.getIntent();

   boolean isEditableControl =
        tag.equals("input")
                || tag.equals("textarea")
                || tag.equals("select");

    boolean isClickableControl =
            tag.equals("button")
                    || tag.equals("a")
                    || candidateIntent == ElementIntent.BUTTON
                    || candidateIntent == ElementIntent.LINK;

    /*
     * These actions are strict evidence.
     *
     * clear() and sendKeys() can only happen on an editable field.
     * A menu item, span, button, link, heading, or div must never pass.
     */
if (failedAction == ExecutionAction.CLEAR
        || failedAction == ExecutionAction.SEND_KEYS) {

    /*
     * Action can be stale from the previous operation.
     *
     * If source-code/semantic analysis already identified
     * the failed locator as something other than INPUT,
     * trust the expected intent instead of stale action history.
     */
    if (expectedIntent == ElementIntent.BUTTON
            || expectedIntent == ElementIntent.LINK) {

        return isClickableControl;
    }

    if (expectedIntent == ElementIntent.TEXT) {
        return true;
    }

    if (expectedIntent == ElementIntent.DROPDOWN) {
        return tag.equals("select")
                || candidateIntent == ElementIntent.DROPDOWN;
    }

    /*
     * SEND_KEYS / CLEAR is strict only when the failed
     * element is actually expected to be an input,
     * or when semantic intent is unknown.
     */
    if (!isEditableControl) {

        
        return false;
    }

    return true;
}

    /*
     * click() must not heal to plain text such as span, label,
     * heading, div, paragraph, etc.
     */
if (failedAction == ExecutionAction.CLICK) {

    /*
     * CLICK may be stale.
     * If semantic analysis already identified the element type,
     * trust the semantic intent.
     */
    if (expectedIntent == ElementIntent.INPUT) {
        return isEditableControl;
    }

    if (expectedIntent == ElementIntent.TEXT) {
        return true;
    }

    if (expectedIntent == ElementIntent.BUTTON
            || expectedIntent == ElementIntent.LINK) {
        return isClickableControl;
    }

    if (!isClickableControl) {


        return false;
    }

    return true;
}

    /*
     * Use expected intent when action information is unavailable.
     */
    if (expectedIntent == ElementIntent.INPUT) {
        return isEditableControl;
    }

    if (expectedIntent == ElementIntent.BUTTON
            || expectedIntent == ElementIntent.LINK) {
        return isClickableControl;
    }

    if (expectedIntent == ElementIntent.DROPDOWN) {
        return tag.equals("select")
                || candidateIntent == ElementIntent.DROPDOWN;
    }

    return true;
}
private boolean validateActionCompatibility(
        WebElement element,
        FailureContext context) {

    if (context == null
            || context.getFailedAction() == null) {
        return true;
    }

    ExecutionAction action =
            context.getFailedAction();

    ElementIntent expectedIntent =
            context.getExpectedIntent();

    String tag =
            element.getTagName()
                    .toLowerCase()
                    .trim();

    /*
     * Semantic intent is stronger than a stale
     * previously recorded execution action.
     */
    if (expectedIntent != null
            && expectedIntent != ElementIntent.UNKNOWN) {

        if (expectedIntent == ElementIntent.BUTTON) {

            return tag.equals("button")
                    || (tag.equals("input")
                    && ("submit".equalsIgnoreCase(
                            element.getAttribute("type"))
                    || "button".equalsIgnoreCase(
                            element.getAttribute("type"))));
        }

        if (expectedIntent == ElementIntent.LINK) {

            return tag.equals("a")
                    || tag.equals("button");
        }

        if (expectedIntent == ElementIntent.INPUT) {

            return tag.equals("input")
                    || tag.equals("textarea")
                    || "true".equalsIgnoreCase(
                            element.getAttribute(
                                    "contenteditable"));
        }

        if (expectedIntent == ElementIntent.DROPDOWN) {

            return tag.equals("select")
                    || (tag.equals("input")
                    && "combobox".equalsIgnoreCase(
                            element.getAttribute("role")));
        }

        if (expectedIntent == ElementIntent.TEXT) {
            return true;
        }
    }

    /*
     * Fall back to execution action only when
     * semantic intent is unavailable.
     */
    if (action == ExecutionAction.CLEAR
            || action == ExecutionAction.SEND_KEYS) {

        boolean editableField =
                tag.equals("input")
                        || tag.equals("textarea")
                        || "true".equalsIgnoreCase(
                                element.getAttribute(
                                        "contenteditable"));

        if (!editableField) {

           

            return false;
        }
    }

    return true;
}
}