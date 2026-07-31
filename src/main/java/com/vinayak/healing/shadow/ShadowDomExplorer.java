package com.vinayak.healing.shadow;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public final class ShadowDomExplorer {

    private ShadowDomExplorer() {
    }

    /**
     * Returns the SearchContext representing the open shadow root.
     */
    public static SearchContext getShadowRoot(WebElement shadowHost) {

        if (shadowHost == null) {
            return null;
        }

        try {
            return shadowHost.getShadowRoot();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks whether a shadow host has an accessible shadow root.
     */
    public static boolean hasAccessibleShadowRoot(WebElement shadowHost) {
        return getShadowRoot(shadowHost) != null;
    }
}