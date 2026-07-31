package com.vinayak.healing.iframe;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

public class IframeHealingEngine {

    public WebElement findElement(
        WebDriver driver,
        By locator) {

    if (driver == null || locator == null) {
        return null;
    }

    // Always start from the main document
    driver.switchTo().defaultContent();

    List<WebElement> iframes =
            driver.findElements(By.tagName("iframe"));

    System.out.println(
            "\n===== IFRAME SEARCH =====");

    System.out.println(
            "Total iframes found : "
                    + iframes.size());

    for (int i = 0; i < iframes.size(); i++) {

        try {

            driver.switchTo().defaultContent();

            driver.switchTo().frame(i);

            List<WebElement> matches =
                    driver.findElements(locator);

            System.out.println(
                    "Iframe "
                            + i
                            + " -> "
                            + matches.size()
                            + " match(es)");

            if (matches.size() == 1) {

                System.out.println(
                        "Element found inside iframe : "
                                + i);

                return matches.get(0);
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to search iframe "
                            + i
                            + " : "
                            + e.getMessage());
        }
    }

    driver.switchTo().defaultContent();

    return null;
}

    public List<WebElement> findElements(
            WebDriver driver,
            By locator) {

        return Collections.emptyList();
    }
}