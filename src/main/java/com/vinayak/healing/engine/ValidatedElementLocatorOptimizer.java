package com.vinayak.healing.engine;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ValidatedElementLocatorOptimizer {

    public By chooseBestLocator(
            WebDriver driver,
            WebElement element,
            By fallbackLocator) {

        if (driver == null || element == null) {
            return fallbackLocator;
        }

        // Preserve already validated locator
    if (fallbackLocator instanceof By.ById
        || fallbackLocator instanceof By.ByName
        || fallbackLocator instanceof By.ByClassName
        || fallbackLocator.toString().contains("cssSelector")) {

    if (isUnique(driver, fallbackLocator, element)) {
        return fallbackLocator;
    }
}

        // First: optimize the validated element itself.
        By ownLocator =
                findBestDirectLocator(
                        driver,
                        element);

        if (ownLocator != null) {
            return ownLocator;
        }

        /*
         * Second: if the validated element is text/icon inside a clickable
         * parent, optimize the parent only when it is a real <a> or <button>
         * and its direct locator is unique.
         */
        WebElement clickableParent =
                findClickableParent(element);

        if (clickableParent != null) {

            By parentLocator =
                    findBestDirectLocator(
                            driver,
                            clickableParent);

            if (parentLocator != null) {

                System.out.println(
                        "PARENT CLICKABLE LOCATOR SELECTED : "
                                + parentLocator);

                return parentLocator;
            }
        }

        return fallbackLocator;
    }

    private By findBestDirectLocator(
            WebDriver driver,
            WebElement element) {

        By locator;

        locator = uniqueAttribute(
                driver,
                element,
                "data-testid");
        if (locator != null) return locator;

        locator = uniqueAttribute(
                driver,
                element,
                "data-test");
        if (locator != null) return locator;

        locator = uniqueAttribute(
                driver,
                element,
                "data-qa");
        if (locator != null) return locator;

        locator = uniqueAttribute(
                driver,
                element,
                "data-cy");
        if (locator != null) return locator;

        String id = element.getAttribute("id");

        if (isUsable(id)) {

            locator = By.id(id);

            if (isUnique(driver, locator, element)) {
                return locator;
            }
        }

        String name = element.getAttribute("name");

        if (isUsable(name)) {

            locator = By.name(name);

            if (isUnique(driver, locator, element)) {
                return locator;
            }
        }

        String className = element.getAttribute("class");

if (isUsable(className)
        && !className.contains(" ")) {

    locator = By.className(className);

    if (isUnique(driver, locator, element)) {
        return locator;
    }
}

        locator = uniqueAttribute(
                driver,
                element,
                "aria-label");
        if (locator != null) return locator;

        locator = uniqueAttribute(
                driver,
                element,
                "placeholder");
        if (locator != null) return locator;

        String href = element.getAttribute("href");

        if (isUsable(href)
                && element.getTagName()
                        .equalsIgnoreCase("a")) {

            locator = By.cssSelector(
                    "a[href="
                            + cssValue(href)
                            + "]");

            if (isUnique(driver, locator, element)) {
                return locator;
            }
        }

        return null;
    }

private WebElement findClickableParent(
        WebElement element) {

    WebElement current = element;

    for (int level = 0;
         level < 4 && current != null;
         level++) {

        try {

            String tag =
                    current.getTagName();

            if (("a".equalsIgnoreCase(tag)
        || "button".equalsIgnoreCase(tag))
        && isActionableParent(current)) {

    if (current.findElements(
            By.xpath(".//*"))
            .contains(element)) {

        return current;
    }
}

            current =
                    current.findElement(
                            By.xpath(".."));

        } catch (Exception exception) {

            return null;
        }
    }

    return null;
}

private boolean isActionableParent(
        WebElement element) {

    if (element == null) {
        return false;
    }

    try {

        String tag =
                element.getTagName();

        if ("button".equalsIgnoreCase(tag)) {
            return element.isDisplayed()
                    && element.isEnabled();
        }

        if ("a".equalsIgnoreCase(tag)) {

            String href =
                    element.getAttribute("href");

            String role =
                    element.getAttribute("role");

            return element.isDisplayed()
                    && (
                        isUsable(href)
                        || "button".equalsIgnoreCase(role)
                    );
        }

    } catch (Exception ignored) {
    }

    return false;
}

    private By uniqueAttribute(
            WebDriver driver,
            WebElement element,
            String attribute) {

        String value =
                element.getAttribute(attribute);

        if (!isUsable(value)) {
            return null;
        }

        By locator =
                By.cssSelector(
                        "[" + attribute
                                + "="
                                + cssValue(value)
                                + "]");

        return isUnique(driver, locator, element)
                ? locator
                : null;
    }

    private boolean isUnique(
            WebDriver driver,
            By locator,
            WebElement expectedElement) {

        try {

            List<WebElement> matches =
                    driver.findElements(locator);

            return matches.size() == 1
                    && matches.get(0).equals(
                            expectedElement);

        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isUsable(String value) {

        return value != null
                && !value.isBlank()
                && !value.matches(
                        ".*\\d{8,}.*");
    }

    private String cssValue(String value) {

        return "'"
                + value.replace("\\", "\\\\")
                        .replace("'", "\\'")
                + "'";
    }
}