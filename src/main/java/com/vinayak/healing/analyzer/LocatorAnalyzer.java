package com.vinayak.healing.analyzer;

import com.vinayak.healing.model.LocatorInfo;
import com.vinayak.healing.util.TokenParser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocatorAnalyzer {

    // ==========================================
    // XPATH PATTERNS
    // ==========================================

    /*
     * Matches XPath element steps.
     *
     * //aside//ul/li[6]//span
     *
     * Extracts:
     * aside
     * ul
     * li
     * span
     */
    private static final Pattern XPATH_TAG_PATTERN =
            Pattern.compile(
                    "/+([a-zA-Z][a-zA-Z0-9_-]*)");

    /*
     * text()='My Info'
     * normalize-space()='My Info'
     */
    private static final Pattern XPATH_EXACT_TEXT_PATTERN =
            Pattern.compile(
                    "(?:text\\(\\)|normalize-space\\(\\))"
                            + "\\s*=\\s*"
                            + "(['\"])(.*?)\\1",
                    Pattern.CASE_INSENSITIVE);

    /*
     * contains(text(),'My Info')
     * contains(normalize-space(),'My Info')
     */
    private static final Pattern XPATH_CONTAINS_TEXT_PATTERN =
            Pattern.compile(
                    "contains\\s*\\(\\s*"
                            + "(?:text\\(\\)|normalize-space\\(\\))"
                            + "\\s*,\\s*"
                            + "(['\"])(.*?)\\1"
                            + "\\s*\\)",
                    Pattern.CASE_INSENSITIVE);

    /*
     * @name='username'
     * @placeholder="Employee Name"
     */
    private static final Pattern XPATH_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "@([a-zA-Z0-9_:-]+)"
                            + "\\s*=\\s*"
                            + "(['\"])(.*?)\\2",
                    Pattern.CASE_INSENSITIVE);

    /*
     * contains(@class,'menu-item')
     * contains(@id,'user')
     */
    private static final Pattern XPATH_CONTAINS_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "contains\\s*\\(\\s*"
                            + "@([a-zA-Z0-9_:-]+)"
                            + "\\s*,\\s*"
                            + "(['\"])(.*?)\\2"
                            + "\\s*\\)",
                    Pattern.CASE_INSENSITIVE);

    /*
     * starts-with(@id,'user')
     */
    private static final Pattern XPATH_STARTS_WITH_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "starts-with\\s*\\(\\s*"
                            + "@([a-zA-Z0-9_:-]+)"
                            + "\\s*,\\s*"
                            + "(['\"])(.*?)\\2"
                            + "\\s*\\)",
                    Pattern.CASE_INSENSITIVE);

    // ==========================================
    // CSS PATTERNS
    // ==========================================

    /*
     * input
     * button.primary
     * div#container
     */
    private static final Pattern CSS_TAG_PATTERN =
            Pattern.compile(
                    "^\\s*([a-zA-Z][a-zA-Z0-9_-]*)");

    /*
     * [name='username']
     * [data-test="login-button"]
     * [type=submit]
     *
     * Also handles operators:
     * =
     * *=
     * ^=
     * $=
     * ~=
     * |=
     */
    private static final Pattern CSS_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "\\[\\s*"
                            + "([a-zA-Z0-9_:-]+)"
                            + "\\s*"
                            + "(?:[~|^$*]?=)"
                            + "\\s*"
                            + "(?:"
                            + "(['\"])(.*?)\\2"
                            + "|"
                            + "([^\\]\\s]+)"
                            + ")"
                            + "\\s*\\]",
                    Pattern.CASE_INSENSITIVE);

    /*
     * #username
     */
    private static final Pattern CSS_ID_PATTERN =
            Pattern.compile(
                    "#([a-zA-Z0-9_-]+)");

    /*
     * .login-button
     */
    private static final Pattern CSS_CLASS_PATTERN =
            Pattern.compile(
                    "\\.([a-zA-Z0-9_-]+)");

    public LocatorInfo analyze(
            String locator) {

        LocatorInfo info =
                new LocatorInfo();

        if (!hasText(locator)) {
            return info;
        }

        info.setOriginalLocator(
                locator);

        String lower =
                locator.toLowerCase();

        // ==========================================
        // ID
        // ==========================================

        if (lower.startsWith(
                "by.id:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType("id");
            info.setAttribute("id");
            info.setAttributeValue(value);
            info.addAttribute(
                    "id",
                    value);

            addTokens(
                    info,
                    value);

            info.setConfidence(100);

            return info;
        }

        // ==========================================
        // NAME
        // ==========================================

        if (lower.startsWith(
                "by.name:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType("name");
            info.setAttribute("name");
            info.setAttributeValue(value);
            info.addAttribute(
                    "name",
                    value);

            addTokens(
                    info,
                    value);

            info.setConfidence(100);

            return info;
        }

        // ==========================================
        // CLASS NAME
        // ==========================================

        if (lower.startsWith(
                "by.classname:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType("class");
            info.setAttribute("class");
            info.setAttributeValue(value);
            info.addAttribute(
                    "class",
                    value);

            addTokens(
                    info,
                    value);

            info.setConfidence(90);

            return info;
        }

        // ==========================================
        // TAG NAME
        // ==========================================

        if (lower.startsWith(
                "by.tagname:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType("tag");
            info.setAttribute("tag");
            info.setAttributeValue(value);
            info.setTag(value);

            addTokens(
                    info,
                    value);

            /*
             * Tag-only locators are weak semantic evidence.
             */
            info.setConfidence(40);

            return info;
        }

        // ==========================================
        // LINK TEXT
        // ==========================================

        if (lower.startsWith(
                "by.linktext:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType(
                    "linkText");

            info.setAttribute(
                    "text");

            info.setAttributeValue(
                    value);

            info.setSemanticText(
                    value);

            info.setTag("a");

            addTokens(
                    info,
                    value);

            info.setConfidence(100);

            return info;
        }

        // ==========================================
        // PARTIAL LINK TEXT
        // ==========================================

        if (lower.startsWith(
                "by.partiallinktext:")) {

            String value =
                    extractValue(locator);

            info.setLocatorType(
                    "partialLinkText");

            info.setAttribute(
                    "text");

            info.setAttributeValue(
                    value);

            info.setSemanticText(
                    value);

            info.setTag("a");

            addTokens(
                    info,
                    value);

            info.setConfidence(90);

            return info;
        }

        // ==========================================
        // CSS SELECTOR
        // ==========================================

        if (lower.startsWith(
                "by.cssselector:")) {

            String css =
                    extractValue(locator);

            info.setLocatorType("css");

            analyzeCss(
                    css,
                    info);

            return info;
        }

        // ==========================================
        // XPATH
        // ==========================================

        if (lower.startsWith(
                "by.xpath:")) {

            String xpath =
                    extractValue(locator);

            info.setLocatorType(
                    "xpath");

            analyzeXpath(
                    xpath,
                    info);

            return info;
        }

        // ==========================================
        // UNKNOWN LOCATOR
        // ==========================================

        info.setLocatorType(
                "unknown");

        info.setAttributeValue(
                locator);

        addTokens(
                info,
                locator);

        info.setConfidence(0);

        return info;
    }

    // ==========================================
    // XPATH ANALYSIS
    // ==========================================

    private void analyzeXpath(
            String xpath,
            LocatorInfo info) {

        if (!hasText(xpath)) {
            return;
        }

        info.setAttribute(
                "xpath");

        info.setAttributeValue(
                xpath);

        // ------------------------------------------
        // TARGET TAG
        // ------------------------------------------

        Matcher tagMatcher =
                XPATH_TAG_PATTERN.matcher(
                        xpath);

        String lastTag =
                null;

        while (tagMatcher.find()) {

            lastTag =
                    tagMatcher.group(1);
        }

        if (hasText(lastTag)) {

            info.setTag(
                    lastTag.toLowerCase());
        }

        // ------------------------------------------
        // EXACT TEXT
        // ------------------------------------------

        Matcher exactTextMatcher =
                XPATH_EXACT_TEXT_PATTERN
                        .matcher(xpath);

        if (exactTextMatcher.find()) {

            String text =
                    exactTextMatcher
                            .group(2)
                            .trim();

            info.setSemanticText(
                    text);

            addTokens(
                    info,
                    text);
        }

        // ------------------------------------------
        // CONTAINS TEXT
        // ------------------------------------------

        if (!hasText(
                info.getSemanticText())) {

            Matcher containsTextMatcher =
                    XPATH_CONTAINS_TEXT_PATTERN
                            .matcher(xpath);

            if (containsTextMatcher.find()) {

                String text =
                        containsTextMatcher
                                .group(2)
                                .trim();

                info.setSemanticText(
                        text);

                addTokens(
                        info,
                        text);
            }
        }

        // ------------------------------------------
        // EXACT ATTRIBUTES
        // ------------------------------------------

        Matcher attributeMatcher =
                XPATH_ATTRIBUTE_PATTERN
                        .matcher(xpath);

        while (attributeMatcher.find()) {

            String attribute =
                    attributeMatcher
                            .group(1);

            String value =
                    attributeMatcher
                            .group(3);

            info.addAttribute(
                    attribute,
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // CONTAINS ATTRIBUTES
        // ------------------------------------------

        Matcher containsAttributeMatcher =
                XPATH_CONTAINS_ATTRIBUTE_PATTERN
                        .matcher(xpath);

        while (containsAttributeMatcher.find()) {

            String attribute =
                    containsAttributeMatcher
                            .group(1);

            String value =
                    containsAttributeMatcher
                            .group(3);

            info.addAttribute(
                    attribute,
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // STARTS-WITH ATTRIBUTES
        // ------------------------------------------

        Matcher startsWithMatcher =
                XPATH_STARTS_WITH_ATTRIBUTE_PATTERN
                        .matcher(xpath);

        while (startsWithMatcher.find()) {

            String attribute =
                    startsWithMatcher
                            .group(1);

            String value =
                    startsWithMatcher
                            .group(3);

            info.addAttribute(
                    attribute,
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // PRIMARY SEMANTIC ATTRIBUTE
        // ------------------------------------------

        assignPrimaryAttribute(
                info);

        /*
         * XPath with semantic text or attributes
         * has stronger evidence than structural-only XPath.
         */
        if (hasText(info.getSemanticText())) {

            info.setConfidence(100);

        } else if (!info.getAttributes()
                .isEmpty()) {

            info.setConfidence(90);

        } else if (hasText(info.getTag())) {

            info.setConfidence(40);

        } else {

            info.setConfidence(10);
        }
    }

    // ==========================================
    // CSS ANALYSIS
    // ==========================================

    private void analyzeCss(
            String css,
            LocatorInfo info) {

        if (!hasText(css)) {
            return;
        }

        info.setAttribute(
                "css");

        info.setAttributeValue(
                css);

        /*
         * For complex CSS selectors:
         *
         * aside ul li span.my-info
         *
         * analyze the final target segment.
         */
        String targetSegment =
                extractLastCssSegment(
                        css);

        // ------------------------------------------
        // TARGET TAG
        // ------------------------------------------

        Matcher tagMatcher =
                CSS_TAG_PATTERN.matcher(
                        targetSegment);

        if (tagMatcher.find()) {

            info.setTag(
                    tagMatcher.group(1)
                            .toLowerCase());
        }

        // ------------------------------------------
        // ID
        // ------------------------------------------

        Matcher idMatcher =
                CSS_ID_PATTERN.matcher(
                        targetSegment);

        while (idMatcher.find()) {

            String value =
                    idMatcher.group(1);

            info.addAttribute(
                    "id",
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // CLASSES
        // ------------------------------------------

        Matcher classMatcher =
                CSS_CLASS_PATTERN.matcher(
                        targetSegment);

        while (classMatcher.find()) {

            String value =
                    classMatcher.group(1);

            /*
             * Multiple classes are stored separately
             * by synthetic keys to avoid overwriting.
             */
            String key =
                    info.getAttributes()
                            .containsKey("class")
                            ? "class-"
                                    + info.getAttributes()
                                            .size()
                            : "class";

            info.addAttribute(
                    key,
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // CSS ATTRIBUTES
        // ------------------------------------------

        Matcher attributeMatcher =
                CSS_ATTRIBUTE_PATTERN.matcher(
                        targetSegment);

        while (attributeMatcher.find()) {

            String attribute =
                    attributeMatcher
                            .group(1);

            String quotedValue =
                    attributeMatcher
                            .group(3);

            String unquotedValue =
                    attributeMatcher
                            .group(4);

            String value =
                    hasText(quotedValue)
                            ? quotedValue
                            : unquotedValue;

            if (!hasText(value)) {
                continue;
            }

            info.addAttribute(
                    attribute,
                    value);

            addTokens(
                    info,
                    value);
        }

        // ------------------------------------------
        // PRIMARY SEMANTIC ATTRIBUTE
        // ------------------------------------------

        assignPrimaryAttribute(
                info);

        if (!info.getAttributes()
                .isEmpty()) {

            info.setConfidence(90);

        } else if (hasText(info.getTag())) {

            info.setConfidence(40);

        } else {

            info.setConfidence(20);
        }
    }

    // ==========================================
    // PRIMARY ATTRIBUTE SELECTION
    // ==========================================

    private void assignPrimaryAttribute(
            LocatorInfo info) {

        String[] priority = {

                "data-test",
                "data-testid",
                "data-qa",
                "data-cy",
                "id",
                "name",
                "placeholder",
                "aria-label",
                "title",
                "href",
                "value",
                "type",
                "class"
        };

        for (String preferred :
                priority) {

            for (var entry :
                    info.getAttributes()
                            .entrySet()) {

                String key =
                        entry.getKey();

                if (key.equalsIgnoreCase(
                        preferred)
                        || key.toLowerCase()
                                .startsWith(
                                        preferred + "-")) {

                    info.setAttribute(
                            preferred);

                    info.setAttributeValue(
                            entry.getValue());

                    return;
                }
            }
        }
    }

    // ==========================================
    // CSS TARGET SEGMENT
    // ==========================================

    private String extractLastCssSegment(
            String css) {

        if (!hasText(css)) {
            return "";
        }

        int bracketDepth = 0;
        int parenthesisDepth = 0;

        char quote = 0;

        int lastSeparator =
                -1;

        for (int i = 0;
                i < css.length();
                i++) {

            char current =
                    css.charAt(i);

            if (quote != 0) {

                if (current == quote) {
                    quote = 0;
                }

                continue;
            }

            if (current == '\''
                    || current == '"') {

                quote = current;
                continue;
            }

            if (current == '[') {

                bracketDepth++;
                continue;
            }

            if (current == ']') {

                bracketDepth =
                        Math.max(
                                0,
                                bracketDepth - 1);

                continue;
            }

            if (current == '(') {

                parenthesisDepth++;
                continue;
            }

            if (current == ')') {

                parenthesisDepth =
                        Math.max(
                                0,
                                parenthesisDepth - 1);

                continue;
            }

            if (bracketDepth == 0
                    && parenthesisDepth == 0) {

                if (Character
                        .isWhitespace(current)
                        || current == '>'
                        || current == '+'
                        || current == '~') {

                    lastSeparator =
                            i;
                }
            }
        }

        if (lastSeparator >= 0
                && lastSeparator
                        < css.length() - 1) {

            return css.substring(
                    lastSeparator + 1)
                    .trim();
        }

        return css.trim();
    }

    // ==========================================
    // TOKEN HANDLING
    // ==========================================

    private void addTokens(
            LocatorInfo info,
            String value) {

        if (!hasText(value)) {
            return;
        }

        List<String> parsed =
                TokenParser.parse(
                        value);

        if (parsed == null
                || parsed.isEmpty()) {

            return;
        }

        Set<String> unique =
                new LinkedHashSet<>(
                        info.getLocatorTokens());

        unique.addAll(
                parsed);

        info.setLocatorTokens(
                new ArrayList<>(
                        unique));
    }

    // ==========================================
    // UTILITIES
    // ==========================================

    private String extractValue(
            String locator) {

        int colon =
                locator.indexOf(':');

        if (colon < 0
                || colon >= locator.length() - 1) {

            return "";
        }

        return locator.substring(
                colon + 1)
                .trim();
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }
}