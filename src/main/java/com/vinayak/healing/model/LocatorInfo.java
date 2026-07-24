package com.vinayak.healing.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocatorInfo {

    /*
     * Complete original Selenium locator.
     *
     * Example:
     * By.id: username
     * By.xpath: //span[text()='My Info']
     */
    private String originalLocator;

    /*
     * Selenium locator family.
     *
     * id
     * name
     * class
     * css
     * xpath
     * tag
     * linkText
     * partialLinkText
     */
    private String locatorType;

    /*
     * Primary semantic attribute.
     *
     * id
     * name
     * placeholder
     * data-test
     * text
     * etc.
     */
    private String attribute;

    /*
     * Primary semantic value extracted from
     * the failed locator.
     */
    private String attributeValue;

    /*
     * Actual target tag.
     *
     * For:
     *
     * //aside//ul/li[6]//span[text()='My Info']
     *
     * target tag = span
     */
    private String tag;

    /*
     * Human-readable semantic text extracted
     * from the locator.
     *
     * Examples:
     *
     * text()='My Info'
     * contains(text(),'My Inffo')
     * By.linkText: My Info
     */
    private String semanticText;

    /*
     * All useful attributes extracted from
     * XPath/CSS.
     */
    private Map<String, String> attributes =
            new LinkedHashMap<>();

    /*
     * Tokens extracted from all useful
     * locator evidence.
     */
    private List<String> locatorTokens =
            new ArrayList<>();

    /*
     * Parser confidence.
     *
     * This is evidence extraction confidence,
     * not healing confidence.
     */
    private double confidence;

    public String getOriginalLocator() {
        return originalLocator;
    }

    public void setOriginalLocator(
            String originalLocator) {

        this.originalLocator =
                originalLocator;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public void setLocatorType(
            String locatorType) {

        this.locatorType =
                locatorType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(
            String attribute) {

        this.attribute =
                attribute;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(
            String attributeValue) {

        this.attributeValue =
                attributeValue;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(
            String tag) {

        this.tag =
                tag;
    }

    public String getSemanticText() {
        return semanticText;
    }

    public void setSemanticText(
            String semanticText) {

        this.semanticText =
                semanticText;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(
            Map<String, String> attributes) {

        this.attributes =
                attributes == null
                        ? new LinkedHashMap<>()
                        : attributes;
    }

    public void addAttribute(
            String name,
            String value) {

        if (name == null
                || name.isBlank()
                || value == null
                || value.isBlank()) {

            return;
        }

        attributes.put(
                name,
                value);
    }

    public List<String> getLocatorTokens() {
        return locatorTokens;
    }

    public void setLocatorTokens(
            List<String> locatorTokens) {

        this.locatorTokens =
                locatorTokens == null
                        ? new ArrayList<>()
                        : locatorTokens;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(
            double confidence) {

        this.confidence =
                confidence;
    }

    @Override
    public String toString() {

        return "LocatorInfo{" +
                "originalLocator='" +
                originalLocator + '\'' +
                ", locatorType='" +
                locatorType + '\'' +
                ", attribute='" +
                attribute + '\'' +
                ", attributeValue='" +
                attributeValue + '\'' +
                ", tag='" +
                tag + '\'' +
                ", semanticText='" +
                semanticText + '\'' +
                ", attributes=" +
                attributes +
                ", locatorTokens=" +
                locatorTokens +
                ", confidence=" +
                confidence +
                '}';
    }
}