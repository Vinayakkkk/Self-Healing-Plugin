package com.vinayak.healing.analyzer;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.VariableInfo;
import com.vinayak.healing.util.TokenParser;

import java.util.*;

public class VariableAnalyzer {

    /*
     * ---------------------------------------------------------
     * Semantic vocabulary
     * ---------------------------------------------------------
     *
     * These are semantic categories, NOT individual locator names.
     * The analyzer works from tokens extracted from variable names.
     */

    private static final Set<String> BUTTON_WORDS =
            Set.of(
                    "button",
                    "btn",
                    "submit",
                    "save",
                    "login",
                    "logout",
                    "signin",
                    "signout",
                    "register",
                    "signup",
                    "search",
                    "cancel",
                    "delete",
                    "remove",
                    "add",
                    "create",
                    "update",
                    "edit",
                    "apply",
                    "clear",
                    "reset",
                    "continue",
                    "next",
                    "previous",
                    "back",
                    "finish",
                    "confirm",
                    "checkout",
                    "proceed"
            );

    private static final Set<String> INPUT_WORDS =
            Set.of(
                    "input",
                    "field",
                    "textbox",
                    "text",
                    "username",
                    "userid",
                    "user",
                    "password",
                    "pass",
                    "email",
                    "phone",
                    "mobile",
                    "address",
                    "searchbox",
                    "query",
                    "keyword",
                    "value",
                    "date",
                    "time",
                    "amount",
                    "price"
            );

    private static final Set<String> LINK_WORDS =
        Set.of(
                "link",
                "anchor",
                "navigation",
                "nav"
        );

    private static final Set<String> DROPDOWN_WORDS =
            Set.of(
                    "dropdown",
                    "select",
                    "selector",
                    "combobox",
                    "listbox",
                    "option"
            );

    private static final Set<String> CHECKBOX_WORDS =
            Set.of(
                    "checkbox",
                    "check"
            );

    private static final Set<String> RADIO_WORDS =
            Set.of(
                    "radio",
                    "radiobutton"
            );

    private static final Set<String> TEXT_WORDS =
            Set.of(
                    "title",
                    "heading",
                    "header",
                    "label",
                    "caption",
                    "message",
                    "description",
                    "text",
                    "value",
                    "status",
                    "notification",
                    "badge"
            );

    private static final Set<String> CONTAINER_WORDS =
            Set.of(
                    "container",
                    "wrapper",
                    "panel",
                    "section",
                    "content",
                    "area",
                    "region",
                    "card",
                    "box",
                    "form",
                    "dialog",
                    "modal",
                    "table",
                    "row",
                    "column"
            );

    /*
     * Words which describe the UI control rather than its business meaning.
     */
    private static final Set<String> CONTROL_WORDS =
            Set.of(
                    "button",
                    "btn",
                    "link",
                    "icon",
                    "field",
                    "input",
                    "textbox",
                    "dropdown",
                    "select",
                    "checkbox",
                    "radio",
                    "menu",
                    "nav",
                    "badge",
                    "label",
                    "title",
                    "header",
                    "container",
                    "wrapper",
                    "panel",
                    "section"
            );

    /*
     * ---------------------------------------------------------
     * Main analysis
     * ---------------------------------------------------------
     */

    public VariableInfo analyze(String variableName) {

        VariableInfo info = new VariableInfo();

        if (variableName == null
                || variableName.isBlank()) {

            info.setVariableName("");
            info.setTokens(Collections.emptyList());
            info.setExpectedIntent(ElementIntent.UNKNOWN);
            info.setExpectedTag("");
            info.setSynonyms(Collections.emptyList());
            info.setConfidence(0);

            return info;
        }

        info.setVariableName(variableName);

        List<String> tokens =
                TokenParser.parse(variableName);

        info.setTokens(tokens);

        ElementIntent intent =
                detectIntent(tokens);

        info.setExpectedIntent(intent);

       

        info.setSynonyms(
                buildSynonyms(tokens));

        info.setConfidence(
                calculateConfidence(
                        tokens,
                        intent));

        return info;
    }

    /*
     * ---------------------------------------------------------
     * Intent detection
     * ---------------------------------------------------------
     */

    private ElementIntent detectIntent(
            List<String> tokens) {

        if (tokens == null
                || tokens.isEmpty()) {

            return ElementIntent.UNKNOWN;
        }

        Map<ElementIntent, Double> scores =
                new EnumMap<>(ElementIntent.class);

        scores.put(ElementIntent.BUTTON, 0.0);
        scores.put(ElementIntent.INPUT, 0.0);
        scores.put(ElementIntent.LINK, 0.0);
        scores.put(ElementIntent.DROPDOWN, 0.0);
        scores.put(ElementIntent.CHECKBOX, 0.0);
        scores.put(ElementIntent.RADIO, 0.0);
        scores.put(ElementIntent.TEXT, 0.0);
        scores.put(ElementIntent.CONTAINER, 0.0);

        for (String token : tokens) {

            if (token == null
                    || token.isBlank()) {
                continue;
            }

            String normalized =
                    token.toLowerCase().trim();

            /*
             * Explicit control words receive the strongest weight.
             */
            if (BUTTON_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.BUTTON,
                        100);
            }

            if (INPUT_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.INPUT,
                        100);
            }

            if (LINK_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.LINK,
                        70);
            }

            if (DROPDOWN_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.DROPDOWN,
                        100);
            }

            if (CHECKBOX_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.CHECKBOX,
                        100);
            }

            if (RADIO_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.RADIO,
                        100);
            }

            if (TEXT_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.TEXT,
                        70);
            }

            if (CONTAINER_WORDS.contains(normalized)) {
                addScore(
                        scores,
                        ElementIntent.CONTAINER,
                        100);
            }

            /*
             * "icon" by itself is ambiguous.
             *
             * We intentionally DO NOT classify every icon as IMAGE.
             * An icon can represent:
             *
             *   cart
             *   menu
             *   search
             *   profile
             *   settings
             *   close
             *
             * The surrounding tokens determine its role.
             */
            if (normalized.equals("icon")) {

                /*
                 * An icon combined with navigation/business
                 * vocabulary is generally a clickable link/control.
                 */
                if (containsAny(
                        tokens,
                        LINK_WORDS)) {

                    addScore(
                            scores,
                            ElementIntent.LINK,
                            45);
                }

                /*
                 * Search icon normally represents an action.
                 */
                if (containsToken(
                        tokens,
                        "search")) {

                    addScore(
                            scores,
                            ElementIntent.BUTTON,
                            45);
                }

                /*
                 * Menu icon normally represents navigation.
                 */
                if (containsAny(
                        tokens,
                        Set.of(
                                "menu",
                                "navigation",
                                "nav"))) {

                    addScore(
                            scores,
                            ElementIntent.LINK,
                            45);
                }
            }
        }

        /*
         * -----------------------------------------------------
         * Contextual adjustments
         * -----------------------------------------------------
         */

        /*
         * Input-related words must beat generic "text".
         *
         * Example:
         *
         * searchInput
         * userName
         * passwordField
         */
        if (containsAny(
                tokens,
                INPUT_WORDS)) {

            scores.merge(
                    ElementIntent.INPUT,
                    25.0,
                    Double::sum);
        }

        /*
         * Explicit button/control suffixes are stronger
         * than generic business words.
         *
         * Example:
         *
         * checkoutButton
         * deleteBtn
         * loginButton
         */
        if (containsAny(
                tokens,
                Set.of(
                        "button",
                        "btn"))) {

            scores.merge(
                    ElementIntent.BUTTON,
                    50.0,
                    Double::sum);
        }

        /*
         * Explicit link suffix.
         */
        if (containsAny(
                tokens,
                Set.of("link", "anchor"))) {

            scores.merge(
                    ElementIntent.LINK,
                    50.0,
                    Double::sum);
        }

        /*
         * Explicit dropdown/select suffix.
         */
        if (containsAny(
                tokens,
                Set.of(
                        "dropdown",
                        "select",
                        "selector",
                        "combobox",
                        "listbox"))) {

            scores.merge(
                    ElementIntent.DROPDOWN,
                    50.0,
                    Double::sum);
        }

        /*
         * -----------------------------------------------------
         * Find strongest intent
         * -----------------------------------------------------
         */

        ElementIntent bestIntent =
                ElementIntent.UNKNOWN;

        double bestScore = 0;

        /*
         * Explicit control intent wins over generic
         * semantic nouns.
         */
        List<ElementIntent> priority =
                List.of(
                        ElementIntent.BUTTON,
                        ElementIntent.INPUT,
                        ElementIntent.DROPDOWN,
                        ElementIntent.CHECKBOX,
                        ElementIntent.RADIO,
                        ElementIntent.LINK,
                        ElementIntent.TEXT,
                        ElementIntent.CONTAINER
                );

        for (ElementIntent intent : priority) {

            double score =
                    scores.getOrDefault(
                            intent,
                            0.0);

            if (score > bestScore) {

                bestScore = score;
                bestIntent = intent;
            }
        }

        return bestIntent;
    }

    private void addScore(
            Map<ElementIntent, Double> scores,
            ElementIntent intent,
            double amount) {

        scores.merge(
                intent,
                amount,
                Double::sum);
    }

    /*
     * ---------------------------------------------------------
     * Expected DOM tag
     * ---------------------------------------------------------
     */

  
 

    /*
     * ---------------------------------------------------------
     * Synonyms
     * ---------------------------------------------------------
     */

    private List<String> buildSynonyms(
            List<String> tokens) {

        LinkedHashSet<String> words =
                new LinkedHashSet<>();

        if (tokens == null) {
            return new ArrayList<>();
        }

        words.addAll(tokens);

        for (String token : tokens) {

            if (token == null
                    || token.isBlank()) {
                continue;
            }

            switch (token.toLowerCase()) {

                case "username" -> {
                    words.add("user");
                    words.add("userid");
                    words.add("login");
                    words.add("account");
                }

                case "password" -> {
                    words.add("pwd");
                    words.add("pass");
                    words.add("credential");
                }

                case "search" -> {
                    words.add("find");
                    words.add("lookup");
                    words.add("query");
                }

                case "cart" -> {
                    words.add("shopping");
                    words.add("basket");
                    words.add("checkout");
                }

                case "badge" -> {
                    words.add("count");
                    words.add("counter");
                    words.add("notification");
                }

                case "menu" -> {
                    words.add("navigation");
                    words.add("nav");
                    words.add("drawer");
                }

                case "settings" -> {
                    words.add("configuration");
                    words.add("preferences");
                    words.add("options");
                }

                case "employee" -> {
                    words.add("staff");
                    words.add("person");
                    words.add("worker");
                }

                case "product" -> {
                    words.add("item");
                    words.add("inventory");
                    words.add("goods");
                }

                case "title" -> {
                    words.add("heading");
                    words.add("header");
                    words.add("caption");
                }

                case "delete" -> {
                    words.add("remove");
                    words.add("discard");
                }

                case "add" -> {
                    words.add("create");
                    words.add("insert");
                }

                case "edit" -> {
                    words.add("update");
                    words.add("modify");
                }
            }
        }

        return new ArrayList<>(words);
    }

    /*
     * ---------------------------------------------------------
     * Confidence
     * ---------------------------------------------------------
     */

    private double calculateConfidence(
            List<String> tokens,
            ElementIntent intent) {

        if (tokens == null
                || tokens.isEmpty()) {

            return 0;
        }

        if (intent == null
                || intent == ElementIntent.UNKNOWN) {

            return 20;
        }

        double confidence = 50;

        /*
         * More meaningful tokens provide more context.
         */
        confidence +=
                Math.min(
                        tokens.size() * 8,
                        24);

        /*
         * Explicit control words provide strong confidence.
         */
        if (containsAny(
                tokens,
                CONTROL_WORDS)) {

            confidence += 20;
        }

        /*
         * Multiple semantic tokens increase confidence.
         *
         * Example:
         *
         * cartIcon
         * checkoutButton
         * passwordField
         */
        if (tokens.size() >= 2) {
            confidence += 10;
        }

        return Math.min(
                confidence,
                100);
    }

    /*
     * ---------------------------------------------------------
     * Utility methods
     * ---------------------------------------------------------
     */

    private boolean containsToken(
            List<String> tokens,
            String expected) {

        if (tokens == null
                || expected == null) {
            return false;
        }

        for (String token : tokens) {

            if (expected.equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsAny(
            List<String> tokens,
            Set<String> expectedWords) {

        if (tokens == null
                || expectedWords == null
                || expectedWords.isEmpty()) {

            return false;
        }

        for (String token : tokens) {

            if (token == null) {
                continue;
            }

            if (expectedWords.contains(
                    token.toLowerCase())) {

                return true;
            }
        }

        return false;
    }
}