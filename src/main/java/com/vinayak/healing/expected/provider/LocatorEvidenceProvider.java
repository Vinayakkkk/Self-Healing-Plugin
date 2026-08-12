package com.vinayak.healing.expected.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vinayak.healing.expected.EvidenceSource;
import com.vinayak.healing.expected.ExpectedEvidence;
import com.vinayak.healing.expected.ExpectedEvidenceType;
import com.vinayak.healing.model.FailureContext;

public class LocatorEvidenceProvider {

    private static final Pattern TEXT_PATTERN =
            Pattern.compile(
                    "text\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]");

    private static final Pattern NORMALIZE_PATTERN =
            Pattern.compile(
                    "normalize-space\\(\\)\\s*=\\s*['\"]([^'\"]+)['\"]");

    /*
     * Selenium locator:
     *
     * By.id: username
     * By.name: username
     * By.className: shopping
     * By.tagName: button
     * By.linkText: Cart
     * By.partialLinkText: Shopping
     * By.cssSelector: [data-test='login-button']
     * By.xpath: //button[@id='login-button']
     */
    private static final Pattern SELENIUM_LOCATOR_PATTERN =
            Pattern.compile(
                    "(?i)^By\\.([a-zA-Z]+)\\s*:\\s*(.+)$");

    /*
     * CSS attribute value:
     *
     * [data-test='shopping-cart-link']
     * [id='login-button']
     * [name='username']
     */
    private static final Pattern CSS_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "\\[[^=\\]]+\\s*=\\s*['\"]?([^'\"\\]]+)['\"]?\\]");

    /*
     * XPath attribute:
     *
     * @id='login-button'
     * @name='username'
     * @class='shopping'
     */
    private static final Pattern XPATH_ATTRIBUTE_PATTERN =
            Pattern.compile(
                    "@[a-zA-Z0-9_-]+\\s*=\\s*['\"]([^'\"]+)['\"]");

    public List<ExpectedEvidence> collect(
            FailureContext context) {

        List<ExpectedEvidence> evidences =
                new ArrayList<>();

        if (context == null) {
            return evidences;
        }

        String locator =
                context.getLocatorDeclaration();

        if (locator == null
                || locator.isBlank()) {

            locator =
                    context.getFailedLocator();
        }

        if (locator == null
                || locator.isBlank()) {

            return evidences;
        }

        /*
         * ==========================================
         * 1. Existing XPath text extraction
         * ==========================================
         */

        extract(
                locator,
                TEXT_PATTERN,
                evidences);

        extract(
                locator,
                NORMALIZE_PATTERN,
                evidences);

        /*
         * ==========================================
         * 2. Selenium locator extraction
         * ==========================================
         */

        extractSeleniumLocator(
                locator,
                evidences);

        /*
         * ==========================================
         * 3. CSS attribute extraction
         * ==========================================
         */

        extractAttribute(
        locator,
        CSS_ATTRIBUTE_PATTERN,
        evidences,
        "CSS attribute value");

        /*
         * ==========================================
         * 4. XPath attribute extraction
         * ==========================================
         */

        extractAttribute(
        locator,
        XPATH_ATTRIBUTE_PATTERN,
        evidences,
        "XPath attribute value");

        return evidences;
    }

    /*
     * =========================================================
     * SELENIUM LOCATOR
     * =========================================================
     */

    private void extractSeleniumLocator(
            String locator,
            List<ExpectedEvidence> evidences) {

        Matcher matcher =
                SELENIUM_LOCATOR_PATTERN
                        .matcher(locator.trim());

        if (!matcher.find()) {
            return;
        }

        String locatorType =
                matcher.group(1)
                        .trim()
                        .toLowerCase();

        String locatorValue =
                matcher.group(2)
                        .trim();

        if (locatorValue.isBlank()) {
            return;
        }

        /*
         * Remove surrounding quotes if present.
         */
        locatorValue =
                removeQuotes(locatorValue);

        /*
         * =====================================================
         * Strong semantic locator values
         *
         * Example:
         *
         * By.className: shopping
         *
         * becomes:
         *
         * TEXT = shopping
         * =====================================================
         */

        switch (locatorType) {

            case "id":
            case "name":
            case "classname":
            case "tagname":
            case "linktext":
            case "partiallinktext":
            case "xpath":
            case "cssselector":
            case "css":

                addLocatorEvidence(
                        evidences,
                        locatorType,
                        locatorValue,
                        locator);

                break;

            default:

                /*
                 * Unknown Selenium locator types should
                 * still provide useful evidence rather
                 * than being silently ignored.
                 */
                addLocatorEvidence(
                        evidences,
                        locatorType,
                        locatorValue,
                        locator);

                break;
        }
    }

    /*
     * =========================================================
     * ADD SELENIUM LOCATOR EVIDENCE
     * =========================================================
     */

    private void addLocatorEvidence(
            List<ExpectedEvidence> evidences,
            String locatorType,
            String locatorValue,
            String rawLocator) {

        if (locatorValue == null
                || locatorValue.isBlank()) {

            return;
        }

        /*
         * Locator value is semantic evidence.
         *
         * Example:
         *
         * By.className: shopping
         *
         * Expected text becomes:
         *
         * shopping
         */
        ExpectedEvidence evidence =
                new ExpectedEvidence(
                        EvidenceSource.LOCATOR,
                        ExpectedEvidenceType.VALUE,
                        locatorValue,
                        90,
                        "Extracted from Selenium "
                                + locatorType
                                + " locator");

        evidence.setAttribute(
                locatorType);

        evidence.setRawValue(
                rawLocator);

        evidences.add(evidence);

        System.out.println(
                "LOCATOR EVIDENCE | type="
                        + locatorType
                        + " | value="
                        + locatorValue);
    }

    /*
     * =========================================================
     * GENERIC PATTERN EXTRACTION
     * =========================================================
     */

    private void extract(
            String locator,
            Pattern pattern,
            List<ExpectedEvidence> evidences) {

        extract(
                locator,
                pattern,
                evidences,
                "Extracted from locator");
    }

    private void extract(
            String locator,
            Pattern pattern,
            List<ExpectedEvidence> evidences,
            String description) {

        Matcher matcher =
                pattern.matcher(locator);

        while (matcher.find()) {

            String value =
                    matcher.group(1);

            if (value == null
                    || value.isBlank()) {

                continue;
            }

            ExpectedEvidence evidence =
                    new ExpectedEvidence(
                            EvidenceSource.LOCATOR,
                            ExpectedEvidenceType.TEXT,
                            value.trim(),
                            95,
                            description);

            evidence.setAttribute(
                    "locator");

            evidence.setRawValue(
                    locator);

            evidences.add(evidence);
        }
    }

    /*
     * =========================================================
     * REMOVE QUOTES
     * =========================================================
     */

    private String removeQuotes(
            String value) {

        if (value == null) {
            return "";
        }

        String result =
                value.trim();

        if (result.length() >= 2) {

            char first =
                    result.charAt(0);

            char last =
                    result.charAt(
                            result.length() - 1);

            if ((first == '\'' && last == '\'')
                    || (first == '"' && last == '"')) {

                result =
                        result.substring(
                                1,
                                result.length() - 1);
            }
        }

        return result.trim();
    }
    private void extractAttribute(
        String locator,
        Pattern pattern,
        List<ExpectedEvidence> evidences,
        String description) {

    Matcher matcher =
            pattern.matcher(locator);

    while (matcher.find()) {

        String value =
                matcher.group(1);

        if (value == null
                || value.isBlank()) {

            continue;
        }

        ExpectedEvidence evidence =
                new ExpectedEvidence(
                        EvidenceSource.LOCATOR,
                        ExpectedEvidenceType.ATTRIBUTE,
                        value.trim(),
                        90,
                        description);

        evidence.setAttribute(
                "locator");

        evidence.setRawValue(
                locator);

        evidences.add(evidence);
    }
}
}