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
    public static List<WebElement> findShadowHosts(
            WebDriver driver) {

        if (!(driver instanceof JavascriptExecutor)) {
            return Collections.emptyList();
        }

        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        /*
         * =========================================================
         * AUTH-LOGIN DIAGNOSTIC
         * =========================================================
         *
         * Temporary diagnostic only.
         *
         * This tells us whether auth-login actually exposes
         * an accessible ShadowRoot.
         */
        Object diagnostic =
                js.executeScript(
                        """
                        const element =
                            document.querySelector("auth-login");

                        if (!element) {
                            return "AUTH_LOGIN_NOT_FOUND";
                        }

                        return {
                            tag: element.tagName,

                            outerHTML:
                                element.outerHTML,

                            innerHTML:
                                element.innerHTML,

                            shadowRoot:
                                element.shadowRoot !== null,

                            shadowRootType:
                                element.shadowRoot
                                    ? element.shadowRoot.constructor.name
                                    : "NONE"
                        };
                        """);

        System.out.println(
                "[SHADOW DEBUG] auth-login diagnostic = "
                        + diagnostic);


        /*
         * =========================================================
         * LIVE FORM ELEMENT DIAGNOSTIC
         * =========================================================
         *
         * Important:
         *
         * We are checking the LIVE browser DOM directly.
         *
         * This tells us whether Selenium/JavaScript can actually
         * see input/textarea/select elements even though the
         * page-source based Jsoup parser cannot.
         */
        Object liveDomDiagnostic =
                js.executeScript(
                        """
                        const elements =
                            Array.from(
                                document.querySelectorAll(
                                    "input, textarea, select"
                                )
                            );

                        return elements.map(element => ({
                            tag: element.tagName,

                            type:
                                element.getAttribute("type"),

                            name:
                                element.getAttribute("name"),

                            id:
                                element.getAttribute("id"),

                            placeholder:
                                element.getAttribute(
                                    "placeholder"
                                ),

                            ariaLabel:
                                element.getAttribute(
                                    "aria-label"
                                ),

                            value:
                                element.getAttribute(
                                    "value"
                                ),

                            outerHTML:
                                element.outerHTML
                        }));
                        """);

        System.out.println(
                "[SHADOW DEBUG] LIVE FORM ELEMENTS = "
                        + liveDomDiagnostic);


        /*
         * =========================================================
         * GENERIC OPEN SHADOW DOM DISCOVERY
         * =========================================================
         *
         * This remains completely generic.
         *
         * It does NOT depend on auth-login.
         */
        String script =
                """
                const hosts = [];

                function scan(root) {

                    if (!root) {
                        return;
                    }

                    const elements =
                        root.querySelectorAll("*");

                    for (const element of elements) {

                        try {

                            if (element.shadowRoot) {

                                hosts.push(element);

                                /*
                                 * Recursively inspect nested
                                 * open Shadow DOM roots.
                                 */
                                scan(
                                    element.shadowRoot
                                );
                            }

                        } catch (e) {

                            /*
                             * Ignore inaccessible shadow roots.
                             */
                        }
                    }
                }

                /*
                 * Start from the document.
                 */
                scan(document);

                return hosts;
                """;

        Object result =
                js.executeScript(script);

        if (result == null) {

            System.out.println(
                    "[SHADOW DEBUG] JS shadow hosts result = 0");

            return Collections.emptyList();
        }

        List<?> rawResult =
                (List<?>) result;

        System.out.println(
                "[SHADOW DEBUG] JS shadow hosts result = "
                        + rawResult.size());

        if (rawResult.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(
                (List<WebElement>) rawResult);
    }


    /**
     * Checks whether the current page contains
     * at least one accessible open Shadow DOM host.
     */
    public static boolean hasShadowDom(
            WebDriver driver) {

        return !findShadowHosts(driver).isEmpty();
    }
}