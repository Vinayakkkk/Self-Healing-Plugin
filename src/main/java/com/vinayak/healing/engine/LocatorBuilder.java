package com.vinayak.healing.engine;

import org.openqa.selenium.By;

import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.model.LocatorCandidate;

public class LocatorBuilder {

    /**
     * Builds a Selenium By from an AI LocatorSuggestion.
     */
    public static By build(LocatorSuggestion suggestion) {

        if (suggestion == null) {
            return null;
        }

        return build(
                suggestion.getLocatorType(),
                suggestion.getLocatorValue());
    }

    /**
     * Builds a Selenium By from a deterministic LocatorCandidate.
     */
    public static By build(LocatorCandidate candidate) {

        if (candidate == null) {
            return null;
        }

        return build(
                candidate.getLocatorType(),
                candidate.getLocatorValue());
    }

    /**
     * Common locator builder used by both AI and deterministic flows.
     */
    private static By build(
            String locatorType,
            String locatorValue) {

        if (locatorType == null || locatorValue == null) {
            throw new IllegalArgumentException(
                    "Locator type or locator value cannot be null.");
        }

        locatorType = locatorType
                .toLowerCase()
                .trim()
                .replace("by.", "");

        locatorValue = locatorValue.trim();

        switch (locatorType) {

            

            case "id":
                return By.id(locatorValue);

            case "name":
                return By.name(locatorValue);

            case "xpath":
                return By.xpath(locatorValue);

            case "css":
            case "cssselector":
                return By.cssSelector(locatorValue);

            case "class":
            case "classname":

                // Multiple classes cannot be used with By.className()
                if (locatorValue.contains(" ")) {
                    return By.cssSelector(
                            "." + locatorValue.replaceAll("\\s+", "."));
                }

                return By.className(locatorValue);

            case "placeholder":
                return By.cssSelector(
                        "[placeholder='" + locatorValue + "']");

            case "type":
                return By.cssSelector(
                        "[type='" + locatorValue + "']");

            case "data-test":
                return By.cssSelector(
                        "[data-test='" + locatorValue + "']");

            case "data-testid":
                return By.cssSelector(
                        "[data-testid='" + locatorValue + "']");

            case "data-qa":
                return By.cssSelector(
                        "[data-qa='" + locatorValue + "']");

            case "data-cy":
                return By.cssSelector(
                        "[data-cy='" + locatorValue + "']");

      case "text":
    return By.xpath(
            "//*[normalize-space()="
                    + toXpathLiteral(locatorValue)
                    + "]");

            case "label-input":
    return By.xpath(locatorValue);               

            default:
                throw new IllegalArgumentException(
                        "Unsupported locator type: "
                                + locatorType);
        }
    }

    private static String escapeXpath(String value) {

    if (!value.contains("'")) {
        return value;
    }

    return value.replace(
            "'",
            "\", '\"', \"");
}

private static String toXpathLiteral(String value) {

    if (!value.contains("'")) {
        return "'" + value + "'";
    }

    if (!value.contains("\"")) {
        return "\"" + value + "\"";
    }

    StringBuilder xpath = new StringBuilder("concat(");

    String[] parts = value.split("'");

    for (int i = 0; i < parts.length; i++) {

        if (i > 0) {
            xpath.append(", \"'\", ");
        }

        xpath.append("'")
                .append(parts[i])
                .append("'");
    }

    xpath.append(")");

    return xpath.toString();
}
}