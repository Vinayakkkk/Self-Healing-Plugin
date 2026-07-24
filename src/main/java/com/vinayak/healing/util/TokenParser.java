package com.vinayak.healing.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TokenParser {

    private TokenParser() {
        // Utility class
    }

    /**
     * Converts any identifier into normalized tokens.
     *
     * Examples:
     *
     * employeeName
     * -> employee, name
     *
     * shopping_cart_badge
     * -> shopping, cart, badge
     *
     * item-4-title-link
     * -> item, 4, title, link
     *
     * btn.inventory.primary
     * -> btn, inventory, primary
     *
     * ADD_TO_CART
     * -> add, to, cart
     */
    public static List<String> parse(String value) {

        List<String> tokens = new ArrayList<>();

        if (value == null || value.isBlank()) {
            return tokens;
        }

        // CamelCase → Camel Case
        value = value.replaceAll(
                "([a-z])([A-Z])",
                "$1 $2");

        // Replace separators with spaces
        value = value
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('.', ' ')
                .replace('/', ' ')
                .replace(':', ' ');

        // Remove brackets and quotes
value = value.replaceAll("[\\[\\](){}'\"`]", " ");

// Remove XPath/CSS operators
value = value.replaceAll("[=@*,;><]", " ");

value = value.replaceAll("\\s+", " ");
        // Remove duplicate spaces
        value = value.replaceAll("\\s+", " ");

        String[] parts =
                value.toLowerCase()
                        .trim()
                        .split("\\s+");

        Set<String> unique =
                new LinkedHashSet<>();

        for (String token : parts) {

    token = token.trim();

    if (token.isBlank()) {
        continue;
    }

    // Ignore single-character non-numeric tokens like = @ * > <
    if (token.length() == 1 &&
            !Character.isDigit(token.charAt(0))) {
        continue;
    }

    unique.add(token);
}

        tokens.addAll(unique);

        return tokens;
    }
}