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

        if (!ShadowDomDetector.hasShadowDom(driver)) {
            return Collections.emptyList();
        }

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        for (WebElement host :
                ShadowDomDetector.findShadowHosts(driver)) {

            SearchContext shadowRoot =
                    ShadowDomExplorer.getShadowRoot(host);

            if (shadowRoot == null) {
                continue;
            }

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