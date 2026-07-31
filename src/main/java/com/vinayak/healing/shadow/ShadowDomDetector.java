package com.vinayak.healing.shadow;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShadowDomDetector {

    private ShadowDomDetector() {
    }

    @SuppressWarnings("unchecked")
    public static List<WebElement> findShadowHosts(WebDriver driver) {

        if (!(driver instanceof JavascriptExecutor)) {
            return Collections.emptyList();
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script = """
                const hosts = [];

                function scan(root) {
                    const elements = root.querySelectorAll("*");

                    for (const element of elements) {

                        if (element.shadowRoot) {
                            hosts.push(element);
                            scan(element.shadowRoot);
                        }
                    }
                }

                scan(document);

                return hosts;
                """;

        Object result = js.executeScript(script);

        if (result == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>((List<WebElement>) result);
    }

    public static boolean hasShadowDom(WebDriver driver) {
        return !findShadowHosts(driver).isEmpty();
    }
}