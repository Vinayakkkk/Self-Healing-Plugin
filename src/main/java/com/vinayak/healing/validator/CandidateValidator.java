package com.vinayak.healing.validator;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.capability.ActionCapabilityResolver;
import com.vinayak.healing.capability.CapabilityValidator;
import com.vinayak.healing.capability.ElementCapability;
import com.vinayak.healing.context.DomContextExtractor;
import com.vinayak.healing.decision.SemanticEvidence;
import com.vinayak.healing.decision.SemanticEvidenceEvaluator;
import com.vinayak.healing.engine.LocatorBuilder;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.shadow.ShadowDomDetector;
import com.vinayak.healing.expected.ExpectedContext;


import org.openqa.selenium.*;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.iframe.IframeHealingEngine;
import com.vinayak.healing.expected.verifier.ExpectedElementVerifier;


import java.util.List;

public class CandidateValidator {

        private static final double MIN_HEAL_SCORE = 1.0;

        private final ExpectedElementVerifier expectedElementVerifier =
        new ExpectedElementVerifier();

        private final IframeHealingEngine
        iframeHealingEngine =
        new IframeHealingEngine();

        private final DomContextExtractor
        domContextExtractor =
        new DomContextExtractor();



        private final ActionCapabilityResolver
        actionCapabilityResolver =
        new ActionCapabilityResolver();

private final CapabilityValidator
        capabilityValidator =
        new CapabilityValidator();

        private final SemanticEvidenceEvaluator
        semanticEvidenceEvaluator =
        new SemanticEvidenceEvaluator();

   public LocatorCandidate validate(
        WebDriver driver,
        List<LocatorCandidate> candidates,
        FailureContext context) {

        if (driver == null
                || candidates == null
                || candidates.isEmpty()) {

            return null;
        }

ExpectedContext expectedContext =
        context.getExpectedContext();

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

        /*
         * ==========================================
         * HARD ACTION CAPABILITY GATE
         * ==========================================
         */

       if (!validateActionCompatibility(
        element,
        candidate,
        context)) {

                HealingAnalytics.capabilityFailure();

            System.out.println(
                    "FAILED: HARD ACTION CAPABILITY GATE -> "
                            + candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue()
                            + " | tag="
                            + element.getTagName()
                            + " | action="
                            + context.getFailedAction());

            continue;
        }

        /*
         * ==========================================
         * DISPLAYED
         * ==========================================
         */

        if (!validateDisplayed(element)) {

            System.out.println(
                    "FAILED: validateDisplayed");

            continue;
        }

        /*
         * ==========================================
         * ENABLED
         * ==========================================
         */

        if (!validateEnabled(element)) {

            System.out.println(
                    "FAILED: validateEnabled");

            continue;
        }

        /*
         * ==========================================
         * INTENT
         * ==========================================
         */

        if (!validateIntent(
                element,
                candidate,
                expectedContext)) {

            System.out.println(
                    "FAILED: validateIntent");

            continue;
        }

        /*
         * ==========================================
         * FIELD LABEL
         * ==========================================
         */

        if (!validateFieldLabel(
                candidate,
                expectedContext)) {

            System.out.println(
                    "FAILED: validateFieldLabel");

            continue;
        }

        /*
         * ==========================================
         * EXPECTED TEXT
         * ==========================================
         */

       if (!validateExpectedText(
        element,
        context,
        expectedContext)) {

            System.out.println(
                    "FAILED: validateExpectedText");

            continue;
        }

        /*
         * ==========================================
         * TAG
         * ==========================================
         */

        if (!validateTag(
                element,
                candidate)) {

            System.out.println(
                    "FAILED: validateTag");

            continue;
        }

        /*
         * ==========================================
         * LOCATOR ATTRIBUTE
         * ==========================================
         */

        if (!validateLocatorAttribute(
                element,
                candidate)) {

            System.out.println(
                    "FAILED: validateLocatorAttribute");

            continue;
        }

        /*
         * ==========================================
         * EXPECTED ELEMENT VERIFIER
         * ==========================================
         */

        if (!expectedElementVerifier.verify(
                element,
                candidate,
                context)) {

            System.out.println(
                    "FAILED: ExpectedElementVerifier");

            continue;
        }

        /*
         * ==========================================
         * SEMANTIC SAFETY GATE
         * ==========================================
         */

       SemanticEvidence semanticEvidence =
        semanticEvidenceEvaluator.evaluate(
                candidate,
                context);

boolean strongIdentity =
        hasStrongSemanticEvidence(
                semanticEvidence);

System.out.println(
        "===== SEMANTIC EVIDENCE =====");

System.out.println(
        semanticEvidence);

System.out.println(
        "=============================");

  if (hasMeaningfulFailedLocatorIdentity(context)) {

   boolean locatorIdentityMatched =
        matchesFailedLocatorIdentity(
                candidate,
                context,
                expectedContext);

    /*
     * The failed locator may contain stale,
     * partially corrupted, or typo-like text.
     *
     * If the locator identity does not match exactly,
     * allow the semantic safety layer to make the decision.
     *
     * This is especially important for TEXT elements.
     *
     * Example:
     *
     * Failed locator:
     *     Record Xoun
     *
     * Actual element:
     *     Records Found
     *
     * Expected intent:
     *     TEXT
     */
    boolean semanticOverride =
            isSafeSemanticOverride(
                    candidate,
                    context,
                    semanticEvidence);

    if (!locatorIdentityMatched
            && !semanticOverride) {

        System.out.println(
                "FAILED: HARD FAILED-LOCATOR IDENTITY GATE -> "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());

        continue;
    }

    if (!locatorIdentityMatched
            && semanticOverride) {

        System.out.println(
                "SEMANTIC IDENTITY OVERRIDE ACCEPTED -> "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());
    }
}





        /*
         * ==========================================
         * FINAL SCORE
         * ==========================================
         */

/*
 * ==========================================
 * FINAL SCORE + SEMANTIC PRIORITY
 * ==========================================
 */

double finalScore =
        candidate.getFinalScore();

/*
 * Semantic evidence must influence the final
 * healing decision.
 *
 * Generic candidate quality must never be enough
 * to beat a candidate that has stronger identity
 * evidence.
 */

int semanticSignals =
        semanticEvidence.getSignalCount();


        if (!strongIdentity
        && semanticSignals == 0) {

    System.out.println(
            "FAILED: SEMANTIC IDENTITY SAFETY GATE -> "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue());

    continue;
}



/*
 * Strong semantic identity gets a major bonus.
 *
 * This makes:
 *
 * variable + locator + text + DOM identity
 *
 * more important than generic:
 *
 * unique + button + displayed + enabled.
 */
double originalScore =
        candidate.getFinalScore();


candidate.setFinalScore(finalScore);

System.out.println(
        "===== SEMANTIC SCORE =====");

System.out.println(
        "Candidate       : "
                + candidate.getLocatorType()
                + "="
                + candidate.getLocatorValue());
System.out.println(
        "Original Score  : "
                + originalScore);

System.out.println(
        "Semantic Signals: "
                + semanticSignals);

System.out.println(
        "Strong Identity : "
                + strongIdentity);

System.out.println(
        "Final Score     : "
                + finalScore);

System.out.println(
        "==========================");

if (finalScore > bestScore) {

    bestScore = finalScore;
    bestCandidate = candidate;
}

    } catch (Exception e) {

        System.out.println(
                "VALIDATOR EXCEPTION");

        e.printStackTrace();
    }
}



if (bestCandidate != null
        && bestCandidate.getFinalScore() >= MIN_HEAL_SCORE) {

    try {

        By locator =
                LocatorBuilder.build(bestCandidate);

        WebElement validatedElement =
                findSingleElement(driver, locator);

        if (validatedElement != null) {

            domContextExtractor.populate(
                    context,
                    driver,
                    validatedElement);

            /*
             * The candidate has already been identified
             * from the actual DOM element.
             *
             * Propagate its semantic intent back into
             * FailureContext so subsequent healing,
             * caching and reporting know what this element is.
             */
            if (bestCandidate.getIntent() != null
                    && bestCandidate.getIntent()
                            != ElementIntent.UNKNOWN) {

                context.setExpectedIntent(
                        bestCandidate.getIntent());

                System.out.println(
                        "Resolved Expected Intent = "
                                + bestCandidate.getIntent());
            }
        }

    } catch (Exception ignored) {
    }

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

String locatorString = locator.toString();

if (locatorString.startsWith("By.xpath:")) {

    /*
     * XPath cannot be searched inside shadow roots.
     * Go directly to iframe search.
     */
    try {

        WebElement iframeElement =
                iframeHealingEngine.findElement(
                        driver,
                        locator);

        if (iframeElement != null) {
            return iframeElement;
        }

    } catch (NoSuchElementException ignored) {
    }

    return null;
}

    JavascriptExecutor js =
            (JavascriptExecutor) driver;

   List<WebElement> hosts =
        ShadowDomDetector.findShadowHosts(driver);

if (!hosts.isEmpty()) {

    for (WebElement host : hosts) {

        try {


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


            By shadowLocator = locator;



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



List<WebElement> shadowElements =
        shadowRoot.findElements(shadowLocator);


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
        ExpectedContext expectedContext) {

    try {

        ElementIntent expectedIntent =
                expectedContext == null
                        ? ElementIntent.UNKNOWN
                        : expectedContext.getExpectedIntent();

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

           String contentEditable =
        element.getAttribute("contenteditable");

String role =
        element.getAttribute("role");

String ariaMultiline =
        element.getAttribute("aria-multiline");

boolean validInput =
        tag.equals("input")
        || tag.equals("textarea")
        || "true".equalsIgnoreCase(contentEditable)
        || "".equals(contentEditable)
        || "textbox".equalsIgnoreCase(role)
        || "true".equalsIgnoreCase(ariaMultiline);

            if (!validInput) {



                return false;
            }

            String readonly =
                    element.getAttribute("readonly");

            if (readonly != null
                    && !readonly.isBlank()) {



                return false;
            }

            return element.isDisplayed()
        && element.isEnabled();
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
        FailureContext context,
        ExpectedContext expectedContext){

    if (element == null) {
        return false;
    }

    if (context == null) {
        return true;
    }

    String expected =
        expectedContext != null
                ? expectedContext.getExpectedText()
                : context.getExpectedText();

    /*
     * No expected text means text validation
     * cannot make a decision.
     *
     * Missing evidence must NOT reject a candidate.
     */
    if (expected == null || expected.isBlank()) {
        return true;
    }

    expected = normalizeText(expected);

    String actual = "";

    try {
        actual = element.getText();

        if (actual == null || actual.isBlank()) {

            String textContent =
                    element.getAttribute("textContent");

            if (textContent != null) {
                actual = textContent;
            }
        }

    } catch (Exception e) {
        return false;
    }

    actual = normalizeText(actual);

    /*
     * Expected text is identity evidence.
     *
     * For TEXT intent we require the actual DOM
     * element to contain the expected text.
     */
    if (actual.equals(expected)) {
        return true;
    }

    /*
     * Some elements contain surrounding whitespace
     * or nested text. Allow the expected text to be
     * contained in the normalized DOM text.
     */
    return actual.contains(expected);
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
        ExpectedContext expectedContext) {

    if (expectedContext == null) {
        return true;
    }

    if (expectedContext.getExpectedIntent()
            != ElementIntent.INPUT) {

        return true;
    }

    String expectedLabel =
            expectedContext.getExpectedLabel();

    if (expectedLabel == null
            || expectedLabel.isBlank()) {

        /*
         * No expected-label evidence.
         * Do not invent a rejection.
         */
        return true;
    }

    String nearestLabel =
            candidate.getNearestLabel();

    if (nearestLabel == null
            || nearestLabel.isBlank()) {

        /*
         * We know what the failed element represented,
         * but this candidate has no label evidence.
         *
         * Do not give it label-match credit.
         * Let the remaining safety gates decide.
         */
        return true;
    }

    String expected =
            normalizeText(expectedLabel);

    String actual =
            normalizeText(nearestLabel);

    if (expected.isBlank()
            || actual.isBlank()) {

        return true;
    }

    if (!expected.equals(actual)
            && !actual.contains(expected)
            && !expected.contains(actual)) {

        System.out.println(
                "FAILED: FIELD LABEL MISMATCH -> "
                        + "expected="
                        + expectedLabel
                        + " | actual="
                        + nearestLabel);

        return false;
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
     * SEND_KEYS and CLEAR are strict action requirements.
     *
     * The candidate itself must be an editable element.
     *
     * Never allow:
     * label
     * span
     * div
     * p
     * heading
     * button
     * link
     * or any other non-editable element.
     */

    if (!isEditableControl) {

        System.out.println(
                "FAILED: SEND_KEYS/CLEAR requires editable control -> "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue()
                        + " | tag="
                        + tag
                        + " | intent="
                        + candidateIntent);

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

    /*
     * TEXT + CLICK
     *
     * Do not reject the candidate using only the static
     * LocatorCandidate parent metadata.
     *
     * The actual WebElement must first be resolved.
     * validateActionCompatibility() performs the real
     * browser-side click capability check, including:
     *
     * - native clickable elements
     * - ARIA button/link
     * - clickable ancestors
     *
     * This prevents a valid <span> inside a clickable
     * control from being rejected prematurely.
     */
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
        LocatorCandidate candidate,
        FailureContext context){

    if (element == null || context == null) {
        return false;
    }

    ExecutionAction action =
            context.getFailedAction();

    /*
     * No action information is available.
     *
     * This is a locator validation/healing request,
     * not an action-specific validation.
     *
     * Do not reject the candidate only because
     * failedAction is null.
     */
    if (action == null) {
        return true;
    }

    String tag =
            element.getTagName()
                    .toLowerCase()
                    .trim();

    /*
     * =====================================================
     * SEND_KEYS / CLEAR
     * =====================================================
     *
     * These actions MUST target an editable element.
     *
     * label/span/div/button/a must never pass.
     */
    if (action == ExecutionAction.SEND_KEYS
            || action == ExecutionAction.CLEAR) {

        if (tag.equals("input")
                || tag.equals("textarea")) {

            String readonly =
                    element.getAttribute("readonly");

            if (readonly != null
                    && !readonly.isBlank()) {

                return false;
            }

            return element.isDisplayed()
                    && element.isEnabled();
        }

        String contentEditable =
                element.getAttribute("contenteditable");

        if ("true".equalsIgnoreCase(
                contentEditable)) {

            return element.isDisplayed()
                    && element.isEnabled();
        }

        String role =
                element.getAttribute("role");

        if ("textbox".equalsIgnoreCase(role)) {

            return element.isDisplayed()
                    && element.isEnabled();
        }

        String ariaMultiline =
                element.getAttribute("aria-multiline");

        if ("true".equalsIgnoreCase(
                ariaMultiline)) {

            return element.isDisplayed()
                    && element.isEnabled();
        }

        return false;
    }

  /*
 * =====================================================
 * CLICK
 * =====================================================
 */
if (action == ExecutionAction.CLICK) {

    if (!element.isDisplayed()
            || !element.isEnabled()) {
        return false;
    }

    /*
     * =====================================================
     * TEXT + CLICK
     * =====================================================
     *
     * TEXT is allowed to be clicked directly.
     *
     * This is important for elements such as:
     *
     * <span>(1) Record Found</span>
     *
     * The element does not need to be a button or link.
     */
    if (context.getExpectedIntent() == ElementIntent.TEXT
            && candidate.getIntent() == ElementIntent.TEXT) {

        String text = element.getText();

        if (text != null
                && !text.isBlank()) {

            return true;
        }
    }

    /*
     * The element itself is a native clickable control.
     */
    if (tag.equals("button")
            || tag.equals("a")
            || tag.equals("input")
            || tag.equals("label")) {
        return true;
    }

    /*
     * ARIA clickable controls.
     */
    String role =
            element.getAttribute("role");

    if ("button".equalsIgnoreCase(role)
            || "link".equalsIgnoreCase(role)) {
        return true;
    }

    /*
     * Semantic child of clickable control.
     */
    try {

        WebElement clickableAncestor =
                element.findElement(
                        By.xpath(
                                "./ancestor::*["
                                        + "self::a"
                                        + " or self::button"
                                        + " or self::input"
                                        + " or self::label"
                                        + " or @role='button'"
                                        + " or @role='link'"
                                        + "][1]"));

        return clickableAncestor.isDisplayed()
                && clickableAncestor.isEnabled();

    } catch (Exception ignored) {

        return false;
    }
}

    /*
     * =====================================================
     * Other actions
     * =====================================================
     */
    ElementCapability capability =
            actionCapabilityResolver.resolve(action);

    if (capability == null) {
        return false;
    }

    return capabilityValidator.supports(
            element,
            capability);
}

private boolean hasMeaningfulFailedLocatorIdentity(
        FailureContext context) {

    if (context == null) {
        return false;
    }

    String failedLocator =
            context.getFailedLocator();

    if (failedLocator == null
            || failedLocator.isBlank()) {

        return false;
    }

    String extracted =
            extractMeaningfulTextFromLocator(
                    failedLocator);

    if (extracted == null
            || extracted.isBlank()) {

        return false;
    }

    return !tokenizeMeaningful(extracted).isEmpty();
}

private boolean matchesFailedLocatorIdentity(
        LocatorCandidate candidate,
        FailureContext context,
        ExpectedContext expectedContext) {

    if (candidate == null || context == null) {
        return false;
    }

    String failedLocator =
            context.getFailedLocator();

    String failedIdentity =
            extractMeaningfulTextFromLocator(
                    failedLocator);

    if (failedIdentity == null
            || failedIdentity.isBlank()) {

        return true;
    }

    String candidateText =
            candidate.getElementText();

    String candidateValue =
            candidate.getLocatorValue();

    String nearestLabel =
            candidate.getNearestLabel();

    /*
     * =====================================================
     * EXTRACT BUSINESS TEXT FROM CANDIDATE LOCATOR
     * =====================================================
     *
     * Example:
     *
     * Candidate:
     * xpath=//span[normalize-space()='Admin']
     *
     * Extracted:
     * Admin
     *
     * This is important because candidateValue may contain
     * the whole XPath instead of only the element text.
     */
    String candidateLocatorText =
            extractMeaningfulTextFromLocator(
                    candidateValue);

    String failed =
            normalizeText(failedIdentity);

    String candidateTextNormalized =
            normalizeText(candidateText);

    String candidateValueNormalized =
            normalizeText(candidateValue);

    String candidateLocatorTextNormalized =
            normalizeText(candidateLocatorText);

    String labelNormalized =
            normalizeText(nearestLabel);

            /*
 * =====================================================
 * EXPECTED LABEL IDENTITY
 * =====================================================
 *
 * Example:
 *
 * Failed locator:
 * //label[text()='Username']/../following-sibling::div/input
 *
 * Expected Label:
 * Username
 *
 * Candidate:
 * placeholder=Search
 * tag=input
 *
 * The candidate does not need to contain "Username"
 * in its locator value because the expected label already
 * identifies the business field.
 */
ElementIntent expectedIntent =
        context.getExpectedIntent();

String expectedLabel = null;

if (context.getExpectedContext() != null) {

    expectedLabel =
            context.getExpectedContext()
                    .getExpectedLabel();
}

if (expectedLabel == null
        || expectedLabel.isBlank()) {

    expectedLabel =
            context.getExpectedLabel();
}

if (expectedIntent == ElementIntent.INPUT
        && expectedLabel != null
        && !expectedLabel.isBlank()) {

    String normalizedExpectedLabel =
            normalizeText(expectedLabel);

    String normalizedNearestLabel =
            normalizeText(candidate.getNearestLabel());

    if (!normalizedNearestLabel.isBlank()
            && (normalizedNearestLabel.equals(
                        normalizedExpectedLabel)
                || normalizedNearestLabel.contains(
                        normalizedExpectedLabel)
                || normalizedExpectedLabel.contains(
                        normalizedNearestLabel))) {

        String candidateTag =
                candidate.getTagName() == null
                        ? ""
                        : candidate.getTagName()
                                .trim()
                                .toLowerCase();

        if (candidateTag.equals("input")
                || candidateTag.equals("textarea")) {

            System.out.println(
                    "EXPECTED LABEL IDENTITY MATCH | "
                    + "expectedLabel="
                    + expectedLabel
                    + " | nearestLabel="
                    + candidate.getNearestLabel()
                    + " | candidate="
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue());

            return true;
        }
    }
}

    /*
     * =====================================================
     * EXACT IDENTITY
     * =====================================================
     */
    if (candidateTextNormalized.equals(failed)
            || candidateValueNormalized.equals(failed)
            || candidateLocatorTextNormalized.equals(failed)
            || labelNormalized.equals(failed)) {

        return true;
    }

    /*
     * =====================================================
     * PREFIX IDENTITY
     * =====================================================
     *
     * Example:
     *
     * Failed locator text : Adm
     * Candidate text       : Admin
     *
     * Adm -> Admin
     */
    if (failed.length() >= 3) {

        if (candidateTextNormalized.startsWith(failed)
                || candidateValueNormalized.startsWith(failed)
                || candidateLocatorTextNormalized.startsWith(failed)
                || labelNormalized.startsWith(failed)) {

            return true;
        }
    }

    /*
     * =====================================================
     * MEANINGFUL TOKEN MATCH
     * =====================================================
     */
    return hasMeaningfulTokenMatch(
            failedIdentity,
            candidateText,
            candidateValue,
            candidateLocatorText,
            nearestLabel);
}

private boolean hasStrongSemanticEvidence(
        SemanticEvidence semanticEvidence) {

    if (semanticEvidence == null) {

        return false;
    }

    int signals =
            semanticEvidence.getSignalCount();

    boolean strong =
            signals >= 2;

    System.out.println(
            "SEMANTIC SAFETY CHECK"
                    + " | signals="
                    + signals
                    + " | variable="
                    + semanticEvidence.isVariableMatched()
                    + " | locator="
                    + semanticEvidence.isLocatorMatched()
                    + " | label="
                    + semanticEvidence.isLabelMatched()
                    + " | id="
                    + semanticEvidence.isIdMatched()
                    + " | name="
                    + semanticEvidence.isNameMatched()
                    + " | tag="
                    + semanticEvidence.isTagMatched()
                    + " | intent="
                    + semanticEvidence.isIntentMatched()
                    + " | strong="
                    + strong);

    return strong;
}


private String extractMeaningfulTextFromLocator(
        String failedLocator) {

    if (failedLocator == null
            || failedLocator.isBlank()) {

        return "";
    }

    String locator = failedLocator.trim();

    /*
     * XPath text:
     *
     * //div[normalize-space()='Sauce Labs Backpack']
     *
     * or
     *
     * //div[text()='Sauce Labs Backpack']
     */
    java.util.regex.Pattern xpathTextPattern =
            java.util.regex.Pattern.compile(
                    "(?:normalize-space\\(\\)|text\\(\\))\\s*=\\s*['\"]([^'\"]+)['\"]",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    java.util.regex.Matcher xpathMatcher =
            xpathTextPattern.matcher(locator);

    if (xpathMatcher.find()) {
        return xpathMatcher.group(1);
    }

    /*
     * XPath contains:
     *
     * contains(text(),'Sauce Labs Backpack')
     */
    java.util.regex.Pattern containsPattern =
            java.util.regex.Pattern.compile(
                    "contains\\s*\\([^,]+,\\s*['\"]([^'\"]+)['\"]\\s*\\)",
                    java.util.regex.Pattern.CASE_INSENSITIVE);

    java.util.regex.Matcher containsMatcher =
            containsPattern.matcher(locator);

    if (containsMatcher.find()) {
        return containsMatcher.group(1);
    }

    /*
     * Common locator values:
     *
     * By.id: username
     * By.name: username
     * By.cssSelector: [placeholder='Search']
     */
    java.util.regex.Pattern attributePattern =
            java.util.regex.Pattern.compile(
                    "['\"]([^'\"]+)['\"]");

    java.util.regex.Matcher attributeMatcher =
            attributePattern.matcher(locator);

    if (attributeMatcher.find()) {
        return attributeMatcher.group(1);
    }

    return "";
}

private boolean hasAnyMeaningfulTokenMatch(
        String expected,
        String... actualValues) {

    if (expected == null
            || expected.isBlank()
            || actualValues == null) {

        return false;
    }

    List<String> expectedTokens =
            tokenizeMeaningful(expected);

    if (expectedTokens.isEmpty()) {
        return false;
    }

    for (String actual : actualValues) {

        if (actual == null
                || actual.isBlank()) {
            continue;
        }

        String normalizedActual =
                normalizeText(actual);

        if (normalizedActual.isBlank()) {
            continue;
        }

        List<String> actualTokens =
                java.util.Arrays.asList(
                        normalizedActual.split("\\s+"));

        for (String expectedToken : expectedTokens) {

            for (String actualToken : actualTokens) {

                if (tokensAreSemanticallyEqual(
                        expectedToken,
                        actualToken)) {

                    return true;
                }
            }
        }
    }

    return false;
}
private boolean hasMeaningfulTokenMatch(
        String expected,
        String... actualValues) {

    if (expected == null
            || expected.isBlank()
            || actualValues == null) {

        return false;
    }

    List<String> expectedTokens =
            tokenizeMeaningful(expected);

    if (expectedTokens.isEmpty()) {
        return false;
    }

    for (String actual : actualValues) {

        if (actual == null
                || actual.isBlank()) {
            continue;
        }

        String normalizedActual =
                normalizeText(actual);

        if (normalizedActual.isBlank()) {
            continue;
        }

        List<String> actualTokens =
                java.util.Arrays.asList(
                        normalizedActual.split("\\s+"));

        int matched = 0;

        for (String expectedToken : expectedTokens) {

            boolean tokenMatched = false;

            for (String actualToken : actualTokens) {

                if (tokensAreSemanticallyEqual(
                        expectedToken,
                        actualToken)) {

                    tokenMatched = true;
                    break;
                }
            }

            if (tokenMatched) {
                matched++;
            }
        }

        /*
         * Require majority of meaningful tokens.
         *
         * Example:
         *
         * Failed:
         *     Record Found
         *
         * Actual:
         *     Records Found
         *
         * record  <-> records
         * found   <-> found
         *
         * 2 / 2 = 100%
         */
        double matchRatio =
                (double) matched
                        / expectedTokens.size();

        if (matchRatio >= 0.75) {
            return true;
        }
    }

    return false;
}

private List<String> tokenizeMeaningful(
        String value) {

    List<String> tokens =
            new java.util.ArrayList<>();

    if (value == null
            || value.isBlank()) {

        return tokens;
    }

    String normalized =
            normalizeText(value);

    for (String token :
            normalized.split("\\s+")) {

        if (token.length() < 3) {
            continue;
        }

        if (isGenericToken(token)) {
            continue;
        }

        tokens.add(token);
    }

    return tokens;
}
private boolean isStrongIdentityLocator(
        String locatorType) {

    if (locatorType == null
            || locatorType.isBlank()) {

        return false;
    }

    switch (locatorType.toLowerCase()) {

        case "id":
        case "name":
        case "data-test":
        case "data-testid":
        case "data-qa":
        case "data-cy":
        case "aria-label":
        case "placeholder":
            return true;

        default:
            return false;
    }
}

private String extractLocatorIdentityValue(
        String failedLocator) {

    if (failedLocator == null
            || failedLocator.isBlank()) {

        return "";
    }

    String locator =
            failedLocator.trim();

    /*
     * Selenium locator forms:
     *
     * By.id: username
     * By.name: username
     * By.className: login
     * By.cssSelector: ...
     */
    String[] prefixes = {
            "By.id: ",
            "By.name: ",
            "By.className: ",
            "By.cssSelector: ",
            "By.xpath: "
    };

    for (String prefix : prefixes) {

        if (locator.startsWith(prefix)) {

            return locator
                    .substring(prefix.length())
                    .trim();
        }
    }

    return locator;
}


private boolean isStrongIdentityMatch(
        String failedIdentity,
        String candidateValue) {

    if (failedIdentity == null
            || candidateValue == null
            || failedIdentity.isBlank()
            || candidateValue.isBlank()) {

        return false;
    }

    String failed =
            normalizeText(failedIdentity);

    String candidate =
            normalizeText(candidateValue);

    if (failed.equals(candidate)) {
        return true;
    }

    /*
     * Broken locator may contain a suffix such as:
     *
     * -old
     * -broken
     * -invalid
     * -changed
     *
     * Candidate must represent the meaningful
     * identity before that suffix.
     */
    String[] suffixes = {
            " old",
            " broken",
            " invalid",
            " changed",
            " wrong",
            " invalidlocator"
    };

    for (String suffix : suffixes) {

        if (failed.endsWith(suffix)) {

            String base =
                    failed.substring(
                            0,
                            failed.length()
                                    - suffix.length())
                            .trim();

            if (base.equals(candidate)) {
                return true;
            }
        }
    }

    return false;
}

private boolean isClickableTextCandidate(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return false;
    }

    String parentTag =
            candidate.getParentTag() == null
                    ? ""
                    : candidate.getParentTag()
                            .trim()
                            .toLowerCase();

    return parentTag.equals("a")
            || parentTag.equals("button")
            || parentTag.equals("input")
            || parentTag.equals("label");
}
private boolean isSafeSemanticOverride(
        LocatorCandidate candidate,
        FailureContext context,
        SemanticEvidence semanticEvidence) {

    if (candidate == null
            || context == null
            || semanticEvidence == null) {

        return false;
    }

    ElementIntent expectedIntent =
            context.getExpectedIntent();

    /*
     * Semantic override is allowed only for
     * meaningful semantic element types.
     */
    if (expectedIntent != ElementIntent.TEXT
        && expectedIntent != ElementIntent.INPUT
        && expectedIntent != ElementIntent.BUTTON) {

    return false;
}

    String candidateText =
            candidate.getElementText();

    String failedLocator =
            context.getFailedLocator();

    /*
     * ======================================================
     * TEXT
     * ======================================================
     *
     * Keep the existing strict TEXT behavior.
     */
    if (expectedIntent == ElementIntent.TEXT) {

        if (candidateText == null
                || candidateText.isBlank()) {

            return false;
        }

        String failedIdentity =
                extractMeaningfulTextFromLocator(
                        failedLocator);

        if (failedIdentity == null
                || failedIdentity.isBlank()) {

            return false;
        }

        boolean tokenEvidence =
                hasAnyMeaningfulTokenMatch(
                        failedIdentity,
                        candidateText);

        boolean semanticIntent =
                semanticEvidence.isIntentMatched();

        boolean strongSemanticEvidence =
                semanticEvidence.getSignalCount() >= 2;

        return tokenEvidence
                && semanticIntent
                && strongSemanticEvidence;
    }

    /*
     * ======================================================
     * INPUT
     * ======================================================
     *
     * For INPUT elements, the variable + intent
     * relationship is stronger evidence than the
     * failed locator text.
     *
     * Example:
     *
     * employeeNameInput
     *       ↓
     * Employee Name input
     *
     * The failed locator may be completely corrupted:
     *
     * [name='wrongEmployeeName']
     *
     * Therefore we do NOT require failed-locator
     * token matching here.
     */
    if (expectedIntent == ElementIntent.INPUT) {

        boolean semanticIntent =
                semanticEvidence.isIntentMatched();

        boolean strongSemanticEvidence =
                semanticEvidence.getSignalCount() >= 2;

        boolean variableMatched =
                semanticEvidence.isVariableMatched();

        boolean hasValidInputTag =
                candidate.getTagName() != null
                        && (
                            candidate.getTagName()
                                    .equalsIgnoreCase("input")
                            ||
                            candidate.getTagName()
                                    .equalsIgnoreCase("textarea")
                        );

        return variableMatched
                && semanticIntent
                && strongSemanticEvidence
                && hasValidInputTag;
    }

    /*
 * ======================================================
 * BUTTON
 * ======================================================
 *
 * For BUTTON elements, the failed locator may be completely
 * stale or corrupted.
 *
 * Example:
 *
 * Page Object:
 *     loginButton
 *
 * Failed locator:
 *     [data-test='wrong-login-button']
 *
 * Actual DOM:
 *     id=login-button
 *     name=login-button
 *     data-test=login-button
 *
 * The variable identity + expected BUTTON intent +
 * multiple semantic signals are sufficient to safely
 * override the stale locator identity.
 */
if (expectedIntent == ElementIntent.BUTTON) {

    boolean semanticIntent =
            semanticEvidence.isIntentMatched();

    boolean strongSemanticEvidence =
            semanticEvidence.getSignalCount() >= 2;

    boolean variableMatched =
            semanticEvidence.isVariableMatched();

    String tag =
            candidate.getTagName() == null
                    ? ""
                    : candidate.getTagName()
                            .trim()
                            .toLowerCase();

    String inputType =
            candidate.getInputType() == null
                    ? ""
                    : candidate.getInputType()
                            .trim()
                            .toLowerCase();

    boolean validButton =
            tag.equals("button")
                    || (
                        tag.equals("input")
                        && (
                            inputType.equals("submit")
                            || inputType.equals("button")
                            || inputType.equals("reset")
                        )
                    );

    return variableMatched
            && semanticIntent
            && strongSemanticEvidence
            && validButton;
}

    return false;
}
private boolean tokensAreSemanticallyEqual(
        String expectedToken,
        String actualToken) {

    if (expectedToken == null
            || actualToken == null
            || expectedToken.isBlank()
            || actualToken.isBlank()) {

        return false;
    }

    expectedToken =
            expectedToken.trim()
                    .toLowerCase();

    actualToken =
            actualToken.trim()
                    .toLowerCase();

    /*
     * Exact match
     */
    if (expectedToken.equals(actualToken)) {
        return true;
    }

    /*
     * Basic singular/plural equivalence.
     *
     * record  <-> records
     * user    <-> users
     * item    <-> items
     * employee <-> employees
     */
    if (expectedToken.length() > 3
            && expectedToken.endsWith("s")) {

        String singular =
                expectedToken.substring(
                        0,
                        expectedToken.length() - 1);

        if (singular.equals(actualToken)) {
            return true;
        }
    }

    if (actualToken.length() > 3
            && actualToken.endsWith("s")) {

        String singular =
                actualToken.substring(
                        0,
                        actualToken.length() - 1);

        if (singular.equals(expectedToken)) {
            return true;
        }
    }



    return false;
}
}