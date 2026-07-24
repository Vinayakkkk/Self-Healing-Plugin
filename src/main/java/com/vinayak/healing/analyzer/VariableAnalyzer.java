package com.vinayak.healing.analyzer;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.VariableInfo;
import com.vinayak.healing.util.TokenParser;
import java.util.*;


public class VariableAnalyzer {

  

    private static final Set<String> BUTTON_WORDS =
            Set.of(
                    "button",
                    "btn",
                    "submit",
                    "save",
                    "login",
                    "logout",
                    "search",
                    "cancel",
                    "delete",
                    "remove",
                    "add",
                    "create",
                    "update");

    private static final Set<String> INPUT_WORDS =
            Set.of(
                    "name",
                    "username",
                    "password",
                    "email",
                    "phone",
                    "mobile",
                    "search",
                    "text",
                    "value",
                    "input");

    private static final Set<String> LINK_WORDS =
            Set.of(
                    "link",
                    
                    "home",
                    
                    "settings",
                    "navigation");

    private static final Set<String> IMAGE_WORDS =
            Set.of(
                    "image",
                    "icon",
                    "logo",
                    "photo",
                    "avatar");

    private static final Set<String> LABEL_WORDS =
            Set.of(
                    "title",
                    "header",
                    "heading",
                    "label",
                    "caption");

    public VariableInfo analyze(String variableName) {

        VariableInfo info =
                new VariableInfo();

        info.setVariableName(variableName);

        List<String> tokens =
        TokenParser.parse(variableName);

        info.setTokens(tokens);

        info.setExpectedIntent(
                detectIntent(tokens));

        info.setExpectedTag(
                detectTag(info.getExpectedIntent()));

        info.setSynonyms(
                buildSynonyms(tokens));

        info.setConfidence(
                calculateConfidence(tokens));

        return info;
    }

   

private ElementIntent detectIntent(List<String> tokens) {

    int buttonScore = 0;
    int inputScore = 0;
    int linkScore = 0;
    int textScore = 0;
    int imageScore = 0;

    for (String token : tokens) {

        if (BUTTON_WORDS.contains(token)) {
            buttonScore++;
        }

        if (INPUT_WORDS.contains(token)) {
            inputScore++;
        }

        if (LINK_WORDS.contains(token)) {
            linkScore++;
        }

        if (LABEL_WORDS.contains(token)) {
            textScore++;
        }

        if (IMAGE_WORDS.contains(token)) {
            imageScore++;
        }
    }

    int max = Math.max(
            Math.max(buttonScore, inputScore),
            Math.max(linkScore,
                    Math.max(textScore, imageScore)));

    if (max == 0) {
        return ElementIntent.UNKNOWN;
    }

    // INPUT gets highest priority because variables like
    // searchInput, userName, employeeName should never become BUTTON.
    if (inputScore == max) {
        return ElementIntent.INPUT;
    }

    if (buttonScore == max) {
        return ElementIntent.BUTTON;
    }

    if (linkScore == max) {
        return ElementIntent.LINK;
    }

    if (textScore == max) {
        return ElementIntent.TEXT;
    }

    // Your current ElementIntent doesn't have IMAGE yet.
    // We'll introduce IMAGE in Sprint 2.
    return ElementIntent.UNKNOWN;
}

    private String detectTag(
            ElementIntent intent) {

        return switch (intent) {

            case BUTTON -> "button";

            case INPUT -> "input";

            case LINK -> "a";

            case UNKNOWN -> "";

            case TEXT -> "span";

            default -> "";
        };
    }

    private List<String> buildSynonyms(
            List<String> tokens) {

        LinkedHashSet<String> words =
                new LinkedHashSet<>(tokens);

        for (String token : tokens) {

            switch (token) {

                case "username" -> {

                    words.add("user");
                    words.add("login");
                }

                case "password" -> {

                    words.add("pwd");
                    words.add("pass");
                }

                case "search" -> {

                    words.add("find");
                    words.add("lookup");
                }

                case "cart" -> {

                    words.add("shopping");
                    words.add("basket");
                }

                case "badge" -> {

                    words.add("count");
                    words.add("notification");
                }

                case "employee" -> {

                    words.add("staff");
                    words.add("person");
                }

                case "title" -> {

                    words.add("heading");
                    words.add("header");
                }
            }
        }

        return new ArrayList<>(words);
    }

    private double calculateConfidence(
            List<String> tokens) {

        if (tokens.isEmpty()) {

            return 0;
        }

        double confidence = 50;

        confidence +=
                tokens.size() * 10;

        if (tokens.size() >= 2) {

            confidence += 20;
        }

        return Math.min(
                confidence,
                100);
    }
}