package com.vinayak.healing.verification;

import com.vinayak.healing.model.FailureContext;
import org.openqa.selenium.WebElement;

public class ExpectedElementVerifier {

    public boolean isSuspicious(
            FailureContext context,
            WebElement element) {

        if (context == null || element == null) {
            return false;
        }

        double score = 0;

        // ==========================
        // TAG
        // ==========================

        if (context.getExpectedTag() != null
                && !context.getExpectedTag().isBlank()) {

            if (context.getExpectedTag()
                    .equalsIgnoreCase(element.getTagName())) {

                score += 20;
            }
        }

        // ==========================
        // PLACEHOLDER
        // ==========================

        String expectedPlaceholder =
                context.getLocatorTextHint();

        String actualPlaceholder =
                element.getAttribute("placeholder");

        if (expectedPlaceholder != null
                && !expectedPlaceholder.isBlank()
                && actualPlaceholder != null
                && actualPlaceholder.toLowerCase()
                        .contains(expectedPlaceholder.toLowerCase())) {

            score += 20;
        }

        // ==========================
        // LABEL
        // ==========================

        String expectedLabel =
                context.getExpectedLabel();

        String aria =
                element.getAttribute("aria-label");

        if (expectedLabel != null
                && !expectedLabel.isBlank()
                && aria != null
                && aria.toLowerCase()
                        .contains(expectedLabel.toLowerCase())) {

            score += 20;
        }

        // ==========================
        // NAME
        // ==========================

        String variable =
                context.getVariableName();

        String name =
                element.getAttribute("name");

        if (variable != null
                && !variable.isBlank()
                && name != null
                && name.toLowerCase()
                        .contains(variable.toLowerCase())) {

            score += 20;
        }

        // ==========================
        // TEXT
        // ==========================

        String expectedText =
                context.getLocatorTextHint();

        String actualText =
                element.getText();

        if (expectedText != null
                && !expectedText.isBlank()
                && actualText != null
                && actualText.toLowerCase()
                        .contains(expectedText.toLowerCase())) {

            score += 20;
        }

        System.out.println();
        System.out.println("===== EXPECTED ELEMENT VERIFIER =====");
        System.out.println("Verification Score : " + score);

        return score < 40;
    }
}