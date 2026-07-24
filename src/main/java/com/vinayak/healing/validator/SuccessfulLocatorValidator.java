package com.vinayak.healing.validator;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class SuccessfulLocatorValidator {

    public boolean isSuspicious(
            String variableName,
            WebElement element) {

        if (variableName == null
                || variableName.isBlank()
                || element == null) {

            return false;
        }

        List<String> variableTokens =
                tokenize(variableName);

        if (variableTokens.isEmpty()) {
            return false;
        }

        String elementContext =
                buildElementContext(element);

        if (elementContext.isBlank()) {
            return false;
        }

        int meaningfulMatches = 0;

        for (String token : variableTokens) {

            if (elementContext.contains(token)) {
                meaningfulMatches++;
            }
        }

        /*
         * Do not reject an element only because there
         * are zero matches. Many valid variable names
         * do not directly match DOM attributes.
         *
         * Detect only strong contradictions.
         */

        boolean variableExpectsCartBadge =
                variableTokens.contains("cart")
                        && variableTokens.contains("badge");

        boolean actualElementIsTitle =
                elementContext.contains("title");

        if (variableExpectsCartBadge
                && actualElementIsTitle
                && meaningfulMatches == 0) {

            System.out.println(
                    "SUCCESSFUL LOCATOR SUSPICIOUS");

            System.out.println(
                    "Variable = "
                            + variableName);

            System.out.println(
                    "Element context = "
                            + elementContext);

            return true;
        }

        return false;
    }

    private String buildElementContext(
            WebElement element) {

        StringBuilder context =
                new StringBuilder();

        append(
                context,
                element.getTagName());

        append(
                context,
                element.getAttribute("id"));

        append(
                context,
                element.getAttribute("name"));

        append(
                context,
                element.getAttribute("class"));

        append(
                context,
                element.getAttribute("data-test"));

        append(
                context,
                element.getAttribute("data-testid"));

        append(
                context,
                element.getAttribute("aria-label"));

        append(
                context,
                element.getAttribute("placeholder"));

        append(
                context,
                element.getText());

        return context
                .toString()
                .toLowerCase();
    }

    private void append(
            StringBuilder builder,
            String value) {

        if (value != null
                && !value.isBlank()) {

            builder.append(" ")
                    .append(value);
        }
    }

    private List<String> tokenize(
            String value) {

        String normalized =
                value.replaceAll(
                                "([a-z])([A-Z])",
                                "$1 $2")
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .toLowerCase();

        String[] parts =
                normalized.split("\\s+");

        List<String> tokens =
                new ArrayList<>();

        for (String part : parts) {

            if (part.length() >= 3) {
                tokens.add(part);
            }
        }

        return tokens;
    }
}