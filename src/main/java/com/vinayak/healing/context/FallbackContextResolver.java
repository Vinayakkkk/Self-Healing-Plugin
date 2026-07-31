package com.vinayak.healing.context;

import com.vinayak.healing.analyzer.LocatorAnalyzer;
import com.vinayak.healing.execution.ExecutionAction;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.execution.ExecutionStep;
import com.vinayak.healing.execution.ExecutionTracker;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorInfo;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.util.Map;

public class FallbackContextResolver {

    /*
     * Single source of truth for failed locator parsing.
     *
     * Do not add XPath/CSS regex parsing here.
     * LocatorAnalyzer owns locator parsing.
     */
    private final LocatorAnalyzer locatorAnalyzer =
            new LocatorAnalyzer();

    public FailureContext enrich(
            FailureContext context,
            By failedLocator) {

        if (context == null
                || failedLocator == null) {

            return context;
        }

        String locator =
                failedLocator.toString();

        System.out.println(
                "\n========== FALLBACK CONTEXT RESOLUTION ==========");

        System.out.println(
                "FAILED LOCATOR = "
                        + locator);

        // ==========================================
        // 1. ANALYZE FAILED LOCATOR
        // ==========================================

        LocatorInfo locatorInfo =
                locatorAnalyzer.analyze(
                        locator);

  enrichFromDom(
        context,
        failedLocator,
        locatorInfo);

        System.out.println(
                "LOCATOR EVIDENCE = "
                        + locatorInfo);

        // ==========================================
        // 2. EXECUTION ACTION
        // ==========================================

        resolveExecutionAction(
                context);

        // ==========================================
        // 3. EXPECTED TARGET TAG
        // ==========================================

        if (hasText(
                locatorInfo.getTag())) {

            context.setExpectedTag(
                    locatorInfo.getTag());

            System.out.println(
                    "FALLBACK EXPECTED TAG = "
                            + locatorInfo.getTag());
        }

        // ==========================================
        // 4. EXPECTED TEXT
        // ==========================================

        if (hasText(
        locatorInfo.getSemanticText())) {

    context.setLocatorTextHint(
            locatorInfo.getSemanticText());

    System.out.println(
            "FALLBACK LOCATOR TEXT HINT = "
                    + locatorInfo.getSemanticText());
}

        // ==========================================
        // 5. SEMANTIC IDENTITY
        // ==========================================

        /*
         * Do not overwrite a Page Object variable
         * if one already exists.
         */
        if (!hasText(
                context.getVariableName())) {

            String semanticIdentity =
                    resolveSemanticIdentity(
                            locatorInfo);

            if (hasText(
                    semanticIdentity)) {

                if (!hasText(context.getLocatorTextHint())) {

    context.setLocatorTextHint(
            semanticIdentity);
}

                System.out.println(
        "FALLBACK LOCATOR HINT = "
                + semanticIdentity);
            }
        }

        // ==========================================
        // 6. EXPECTED LABEL
        // ==========================================

        /*
         * Placeholder and aria-label are strong
         * label-like evidence.
         */
        if (!hasText(
                context.getExpectedLabel())) {

            String expectedLabel =
                    resolveExpectedLabel(
                            locatorInfo);

            if (hasText(
                    expectedLabel)) {

                context.setExpectedLabel(
                        expectedLabel);

                System.out.println(
                        "FALLBACK EXPECTED LABEL = "
                                + expectedLabel);
            }
        }

        // ==========================================
        // 7. EXPECTED INTENT
        // ==========================================

        ElementIntent inferredIntent =
                inferIntent(
                        context,
                        locatorInfo);

        if (inferredIntent != null
                && inferredIntent
                        != ElementIntent.UNKNOWN) {

            context.setExpectedIntent(
                    inferredIntent);

            System.out.println(
                    "FALLBACK EXPECTED INTENT = "
                            + inferredIntent);
        }

        System.out.println(
                "==============================================");

        return context;
    }

    // ==========================================
    // EXECUTION ACTION
    // ==========================================

    private void resolveExecutionAction(
            FailureContext context) {

        try {

            ExecutionContext executionContext =
                    ExecutionTracker.getContext();

            if (executionContext == null) {
                return;
            }

            ExecutionStep latestAction =
                    executionContext
                            .getLatestAction();

            if (latestAction == null
                    || latestAction
                            .getAction() == null) {

                return;
            }

            context.setFailedAction(
                    latestAction.getAction());

            System.out.println(
                    "FALLBACK ACTION = "
                            + latestAction
                                    .getAction());

        } catch (Exception exception) {

            System.out.println(
                    "Fallback action resolution failed : "
                            + exception.getMessage());
        }
    }

    // ==========================================
    // SEMANTIC IDENTITY
    // ==========================================

    private String resolveSemanticIdentity(
            LocatorInfo info) {

        if (info == null) {
            return "";
        }

        /*
         * Human-readable text is strongest for
         * links, menu items, headings and buttons.
         */
        if (hasText(
                info.getSemanticText())) {

            return info.getSemanticText();
        }

        /*
         * Then use meaningful attributes.
         */
        String[] priority = {

                "data-test",
                "data-testid",
                "data-qa",
                "data-cy",
                "aria-label",
                "placeholder",
                "id",
                "name",
                "title",
                "href",
                "value",
                "class"
        };

        Map<String, String> attributes =
                info.getAttributes();

        if (attributes != null) {

            for (String preferred :
                    priority) {

                for (Map.Entry<String, String> entry :
                        attributes.entrySet()) {

                    if (entry.getKey()
                            .equalsIgnoreCase(
                                    preferred)
                            && hasText(
                                    entry.getValue())) {

                        return entry.getValue();
                    }
                }
            }
        }

        /*
         * Simple locators already expose their
         * semantic value directly.
         */
        String locatorType =
                info.getLocatorType();

        if ("id".equalsIgnoreCase(
                locatorType)
                || "name".equalsIgnoreCase(
                        locatorType)
                || "class".equalsIgnoreCase(
                        locatorType)
                || "linkText".equalsIgnoreCase(
                        locatorType)
                || "partialLinkText"
                        .equalsIgnoreCase(
                                locatorType)) {

            return info.getAttributeValue();
        }

        /*
         * Do NOT use tag-only locators as semantic
         * identity.
         *
         * //input
         * button
         * div
         *
         * are too ambiguous by themselves.
         */
        return "";
    }

    // ==========================================
    // EXPECTED LABEL
    // ==========================================

    private String resolveExpectedLabel(
            LocatorInfo info) {

        if (info == null
                || info.getAttributes() == null) {

            return "";
        }

        String ariaLabel =
                findAttribute(
                        info,
                        "aria-label");

        if (hasText(ariaLabel)) {
            return ariaLabel;
        }

        String placeholder =
                findAttribute(
                        info,
                        "placeholder");

        if (hasText(placeholder)) {
            return placeholder;
        }

        String title =
        findAttribute(
                info,
                "title");

if (hasText(title)) {
    return title;
}

String value =
        findAttribute(
                info,
                "value");

if (hasText(value)) {
    return value;
}

return "";
    }

    // ==========================================
    // INTENT INFERENCE
    // ==========================================

    private ElementIntent inferIntent(
            FailureContext context,
            LocatorInfo info) {

        ExecutionAction action =
                context.getFailedAction();

        String tag =
                info == null
                        ? ""
                        : info.getTag();

        String type =
                info == null
                        ? ""
                        : findAttribute(
                                info,
                                "type");

        // ------------------------------------------
        // ACTION IS STRONGEST
        // ------------------------------------------

        if (action
                == ExecutionAction.SEND_KEYS
                || action
                == ExecutionAction.CLEAR) {

            return ElementIntent.INPUT;
        }

        // ------------------------------------------
        // TAG + TYPE
        // ------------------------------------------

        if ("input".equalsIgnoreCase(tag)) {

            if ("submit".equalsIgnoreCase(type)
                    || "button"
                            .equalsIgnoreCase(type)
                    || "reset"
                            .equalsIgnoreCase(type)) {

                return ElementIntent.BUTTON;
            }

            return ElementIntent.INPUT;
        }

        if ("textarea"
                .equalsIgnoreCase(tag)
                || "select"
                        .equalsIgnoreCase(tag)) {

            return ElementIntent.INPUT;
        }

        if ("button"
                .equalsIgnoreCase(tag)) {

            return ElementIntent.BUTTON;
        }

        // ------------------------------------------
        // CLICK ACTION
        // ------------------------------------------

        if (action
                == ExecutionAction.CLICK) {

            /*
             * Anchors are clickable navigation
             * elements but your current intent model
             * appears to use TEXT rather than LINK.
             */
            if ("a".equalsIgnoreCase(tag)) {

                return ElementIntent.TEXT;
            }

            /*
             * span/div menu items are represented
             * as TEXT in your current framework.
             */
            return ElementIntent.TEXT;
        }

        // ------------------------------------------
        // TEXT EVIDENCE
        // ------------------------------------------

        if (info != null
        && hasText(info.getSemanticText())) {

    return ElementIntent.TEXT;
}

/*
 * Variable recovered during fallback.
 */
String variable =
        context.getVariableName() == null
                ? ""
                : context.getVariableName().toLowerCase();

if (variable.matches(
        ".*(input|textbox|textfield|textarea|username|password|email|search|field|name|number|date|phone|address).*")) {

    return ElementIntent.INPUT;
}

if (variable.matches(
        ".*(button|btn|submit|save|cancel|reset|search).*")) {

    return ElementIntent.BUTTON;
}

if (variable.matches(
        ".*(dropdown|select|option).*")) {

    return ElementIntent.DROPDOWN;
}

if (variable.matches(
        ".*(link|menu|navigation|nav).*")) {

    return ElementIntent.LINK;
}

if (variable.matches(
        ".*(header|title|text|message|label).*")) {

    return ElementIntent.TEXT;
}

return ElementIntent.UNKNOWN;
    }

    // ==========================================
    // ATTRIBUTE LOOKUP
    // ==========================================

    private String findAttribute(
            LocatorInfo info,
            String attributeName) {

        if (info == null
                || info.getAttributes() == null
                || attributeName == null) {

            return "";
        }

        for (Map.Entry<String, String> entry :
                info.getAttributes()
                        .entrySet()) {

            if (entry.getKey()
                    .equalsIgnoreCase(
                            attributeName)) {

                return entry.getValue();
            }
        }

        return "";
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
private void enrichFromDom(
        FailureContext context,
        By failedLocator,
        LocatorInfo locatorInfo) {

    if (context == null
            || context.getDriver() == null
            || failedLocator == null) {
        return;
    }

    try {

        List<WebElement> elements =
                context.getDriver()
                        .findElements(failedLocator);

       if (elements.isEmpty()) {
    return;
}

        WebElement element =
                elements.get(0);

                System.out.println("\n===== DOM ENRICHMENT =====");
System.out.println("Locator      : " + failedLocator);
System.out.println("Matches      : " + elements.size());

for (int i = 0; i < elements.size(); i++) {

    WebElement e = elements.get(i);

    System.out.println(
            "Element " + (i + 1)
                    + " value="
                    + e.getAttribute("value"));
}

System.out.println(
        "Selected="
                + element.getAttribute("value"));

System.out.println("==========================");

        // -----------------------------
        // TAG
        // -----------------------------

        String tag =
                element.getTagName();

        if (hasText(tag)) {
            locatorInfo.setTag(tag);
        }

        // -----------------------------
        // PLACEHOLDER
        // -----------------------------

        String placeholder =
                element.getAttribute("placeholder");

        if (hasText(placeholder)) {

            locatorInfo.getAttributes()
                    .put("placeholder",
                            placeholder);

            if (!hasText(
                    context.getExpectedLabel())) {

                context.setExpectedLabel(
                        placeholder);
            }
        }

        // -----------------------------
        // ARIA LABEL
        // -----------------------------

        String ariaLabel =
                element.getAttribute("aria-label");

        if (hasText(ariaLabel)) {

            locatorInfo.getAttributes()
                    .put("aria-label",
                            ariaLabel);

            if (!hasText(
                    context.getExpectedLabel())) {

                context.setExpectedLabel(
                        ariaLabel);
            }
        }

        // -----------------------------
        // TITLE
        // -----------------------------

        String title =
                element.getAttribute("title");

        if (hasText(title)) {

            locatorInfo.getAttributes()
                    .put("title",
                            title);

            if (!hasText(
                    context.getExpectedLabel())) {

                context.setExpectedLabel(
                        title);
            }
        }
        // -----------------------------
// VALUE
// -----------------------------

String value =
        element.getAttribute("value");

if (hasText(value)) {

    locatorInfo.getAttributes()
            .put("value", value);

    // Do NOT use input value as expected text.
    // It changes during execution and is not a stable identifier.

    if (!hasText(context.getLocatorTextHint())) {
        context.setLocatorTextHint(value);
    }

    if (!hasText(context.getExpectedLabel())) {
        context.setExpectedLabel(value);
    }
}

        // -----------------------------
        // LABEL
        // -----------------------------

       String label =
        resolveLabel(
                context,
                element);

        if (hasText(label)) {

            context.setExpectedLabel(label);

            if (!hasText(
                    context.getLocatorTextHint())) {

                context.setLocatorTextHint(label);
            }
        }

        // -----------------------------
        // TEXT
        // -----------------------------

        String text =
                element.getText();

        if (hasText(text)) {

            if (!hasText(context.getExpectedText())) {
    context.setExpectedText(text);
}

            if (!hasText(
                    context.getLocatorTextHint())) {

                context.setLocatorTextHint(text);
            }
        }

        System.out.println(
                "DOM EXPECTED LABEL = "
                        + context.getExpectedLabel());

        System.out.println(
                "DOM EXPECTED TEXT = "
                        + context.getExpectedText());

        System.out.println(
                "DOM LOCATOR HINT = "
                        + context.getLocatorTextHint());

    } catch (Exception ignored) {

    }
}
private String resolveLabel(
        FailureContext context,
        WebElement element){

    if (element == null) {
        return "";
    }

    try {

        // ==========================
        // Strategy 1 : parent <label>
        // ==========================

       WebElement parent =
        element.findElement(
                By.xpath("./parent::*"));

if ("label".equalsIgnoreCase(parent.getTagName())) {

    String text = parent.getText();

            if (hasText(text)) {
                return text.trim();
            }
        }

    } catch (Exception ignored) {
    }
   

    try {

        // ==========================
        // Strategy 2 : previous sibling label
        // ==========================

        WebElement label =
                element.findElement(
                        By.xpath("./preceding-sibling::label[1]"));

        String text = label.getText();

        if (hasText(text)) {
            return text.trim();
        }

    } catch (Exception ignored) {
    }

    try {

        // ==========================
        // Strategy 3 : label using for=""
        // ==========================

        String id =
                element.getAttribute("id");

        if (hasText(id)) {

WebElement label =
        context.getDriver().findElement(
                By.xpath("//label[@for='" + id + "']"));

            String text =
                    label.getText();

            if (hasText(text)) {
                return text.trim();
            }
        }

    } catch (Exception ignored) {
    }

    return "";
}
}