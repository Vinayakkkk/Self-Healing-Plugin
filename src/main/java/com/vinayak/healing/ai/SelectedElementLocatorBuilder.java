package com.vinayak.healing.ai;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SelectedElementLocatorBuilder {

    public By build(
            WebDriver driver,
            AiElementChoice choice) {

        if (choice == null) {
            return null;
        }

        String tag = clean(choice.getTag());
        String text = clean(choice.getText());
        String parentHref = clean(choice.getParentHref());

        if (tag.isBlank()) {
            return null;
        }

        /*
         * 1. For navigation/menu elements:
         * <a href="/..."><span>PIM</span></a>
         */
        if (!parentHref.isBlank() && !text.isBlank()) {

            String relativeHref =
                    parentHref.replaceFirst(
                            "^https?://[^/]+",
                            "");

            By locator =
                    By.xpath(
                            "//a[@href="
                                    + xpathLiteral(relativeHref)
                                    + "]//"
                                    + tag
                                    + "[normalize-space()="
                                    + xpathLiteral(text)
                                    + "]");

            if (isUniqueUsable(driver, locator)) {
                return locator;
            }
        }

        /*
         * 2. Text-based locator.
         * Works for buttons, spans, headings, labels, messages.
         */
        if (!text.isBlank()) {

            By locator =
                    By.xpath(
                            "//"
                                    + tag
                                    + "[normalize-space()="
                                    + xpathLiteral(text)
                                    + "]");

            if (isUniqueUsable(driver, locator)) {
                return locator;
            }
        }

        return null;
    }

    private boolean isUniqueUsable(
            WebDriver driver,
            By locator) {

        try {

            List<WebElement> matches =
                    driver.findElements(locator);

            return matches.size() == 1
                    && matches.get(0).isDisplayed()
                    && matches.get(0).isEnabled();

        } catch (Exception exception) {
            return false;
        }
    }

    private String clean(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String xpathLiteral(String value) {

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        StringBuilder result =
                new StringBuilder("concat(");

        String[] parts =
                value.split("'");

        for (int i = 0; i < parts.length; i++) {

            if (i > 0) {
                result.append(", \"'\", ");
            }

            result.append("'")
                    .append(parts[i])
                    .append("'");
        }

        result.append(")");

        return result.toString();
    }
}