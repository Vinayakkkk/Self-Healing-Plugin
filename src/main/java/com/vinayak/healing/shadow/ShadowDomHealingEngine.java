package com.vinayak.healing.shadow;

import com.vinayak.healing.dom.DomCandidateFinder;
import com.vinayak.healing.logging.HealingLogger;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShadowDomHealingEngine {

    private final DomCandidateFinder domCandidateFinder =
            new DomCandidateFinder();

    public List<LocatorCandidate> findCandidates(
            WebDriver driver,
            FailureContext context) {

               if (HealingLogger.isDebugEnabled()) {
    System.out.println("===== ENTERED SHADOW DOM HEALING =====");
}
List<LocatorCandidate> candidates =
        new ArrayList<>();

JavascriptExecutor js =
        (JavascriptExecutor) driver;

List<WebElement> hosts =
        ShadowDomDetector.findShadowHosts(driver);

System.out.println(
        "[SHADOW DEBUG] Hosts found = "
        + hosts.size());

if (hosts.isEmpty()) {
    return Collections.emptyList();
}

System.out.println(
        "[SHADOW DEBUG] Hosts found = "
        + hosts.size());

for (WebElement host : hosts) {

    try {
        System.out.println(
                "[SHADOW DEBUG] Host = "
                + host.getTagName());
    } catch (Exception ignored) {
        System.out.println(
                "[SHADOW DEBUG] Host tag unavailable");
    }

    SearchContext shadowRoot =
            ShadowDomExplorer.getShadowRoot(host);

    if (shadowRoot == null) {

        System.out.println(
                "[SHADOW DEBUG] Shadow root = NULL");

        continue;
    }

    System.out.println(
            "[SHADOW DEBUG] Shadow root = FOUND");

            try {

                String shadowHtml =
                        (String) js.executeScript(
                                "return arguments[0].shadowRoot.innerHTML;",
                                host);
if (HealingLogger.isDebugEnabled()) {
                                System.out.println("\n===== SHADOW HTML =====");
}
System.out.println(shadowHtml);
System.out.println("=======================\n");

                if (shadowHtml == null
                        || shadowHtml.isBlank()) {

                    continue;
                }

                candidates.addAll(
                        domCandidateFinder.findCandidates(
                                shadowHtml,
                                context.getFailedLocator(),
                                context));

            } catch (Exception ignored) {
            }
        }

        return candidates;
    }
}