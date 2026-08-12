package com.vinayak.healing.repair;

import com.vinayak.healing.ai.LocatorSuggestion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaLocatorUpdater {

    public boolean updateLocator(
            Path javaFile,
            String variableName,
            String locatorDeclaration,
            LocatorSuggestion suggestion)
            throws IOException {

        validate(
                javaFile,
                variableName,
                suggestion);

        String source =
                Files.readString(
                        javaFile,
                        StandardCharsets.UTF_8);

        /*
         * =====================================================
         * STEP 1
         * Try normal/static locator field repair.
         * =====================================================
         */

        int declarationIndex =
                source.indexOf(variableName);

        if (declarationIndex == -1) {
            return false;
        }

        /*
         * =====================================================
         * STEP 2
         * Detect whether the locator is a method.
         *
         * Example:
         *
         * private By productNameLocator(String productName)
         * =====================================================
         */

        if (isMethodDeclaration(
                source,
                declarationIndex)) {

            return updateDynamicLocatorMethod(
                    source,
                    javaFile,
                    declarationIndex,
                    variableName,
                    locatorDeclaration,
                    suggestion);
        }

        /*
         * =====================================================
         * STEP 3
         * Existing static locator repair.
         * =====================================================
         */

        int equalIndex =
                findAssignmentOperator(
                        source,
                        declarationIndex);

        if (equalIndex == -1) {
            return false;
        }

        int locatorStart =
                findLocatorStart(
                        source,
                        equalIndex);

        if (locatorStart == -1) {
            return false;
        }

        int locatorEnd =
                findLocatorEnd(
                        source,
                        locatorStart);

        if (locatorEnd == -1) {
            return false;
        }

        String newLocator =
                buildLocatorExpression(
                        suggestion);

        String updatedSource =
                source.substring(
                        0,
                        locatorStart)
                + newLocator
                + source.substring(
                        locatorEnd);

        if (updatedSource.equals(source)) {
            return false;
        }

        Files.writeString(
                javaFile,
                updatedSource,
                StandardCharsets.UTF_8);

        return true;
    }

    /*
     * =========================================================
     * Detect dynamic locator method.
     * =========================================================
     */
    private boolean isMethodDeclaration(
            String source,
            int variableIndex) {

        int lineEnd =
                source.indexOf(
                        '{',
                        variableIndex);

        int semicolon =
                source.indexOf(
                        ';',
                        variableIndex);

        /*
         * If '(' appears before '=' or ';',
         * this is most likely a method declaration.
         */
        int openingParenthesis =
                source.indexOf(
                        '(',
                        variableIndex);

        if (openingParenthesis == -1) {
            return false;
        }

        if (semicolon != -1
                && semicolon < openingParenthesis) {

            return false;
        }

        if (lineEnd != -1
                && lineEnd < openingParenthesis) {

            return false;
        }

        return true;
    }

    /*
     * =========================================================
     * Dynamic locator method handling.
     * =========================================================
     */
    private boolean updateDynamicLocatorMethod(
            String source,
            Path javaFile,
            int declarationIndex,
            String variableName,
            String locatorDeclaration,
            LocatorSuggestion suggestion)
            throws IOException {

        System.out.println(
                ">>> DYNAMIC LOCATOR METHOD DETECTED");

        System.out.println(
                "Method : "
                        + variableName);

        /*
         * A dynamic method may depend on parameters.
         *
         * Example:
         *
         * productNameLocator(String productName)
         *
         * We must NOT replace its body with a locator
         * that is specific to only the current element.
         */

        if (containsMethodParameter(
                source,
                declarationIndex)) {

            System.out.println(
                    "[HEALING INFO] Dynamic locator "
                    + "method contains parameters.");

            System.out.println(
                    "[HEALING INFO] Source repair skipped "
                    + "to preserve dynamic behavior.");

            return false;
        }

        /*
         * Parameterless method returning By can safely
         * be treated as a locator method.
         */

        int returnIndex =
                findReturnStatement(
                        source,
                        declarationIndex);

        if (returnIndex == -1) {
            return false;
        }

        int locatorStart =
                findLocatorStart(
                        source,
                        returnIndex);

        if (locatorStart == -1) {
            return false;
        }

        int locatorEnd =
                findLocatorEnd(
                        source,
                        locatorStart);

        if (locatorEnd == -1) {
            return false;
        }

        String newLocator =
                buildLocatorExpression(
                        suggestion);

        String updatedSource =
                source.substring(
                        0,
                        locatorStart)
                + newLocator
                + source.substring(
                        locatorEnd);

        if (updatedSource.equals(source)) {
            return false;
        }

        Files.writeString(
                javaFile,
                updatedSource,
                StandardCharsets.UTF_8);

        return true;
    }

    /*
     * =========================================================
     * Detect method parameters.
     *
     * Example:
     *
     * productNameLocator(String productName)
     *
     * =========================================================
     */
    private boolean containsMethodParameter(
            String source,
            int declarationIndex) {

        int openingParenthesis =
                source.indexOf(
                        '(',
                        declarationIndex);

        if (openingParenthesis == -1) {
            return false;
        }

        int closingParenthesis =
                source.indexOf(
                        ')',
                        openingParenthesis);

        if (closingParenthesis == -1) {
            return false;
        }

        String parameters =
                source.substring(
                        openingParenthesis + 1,
                        closingParenthesis)
                        .trim();

        return !parameters.isBlank();
    }

    /*
     * =========================================================
     * Find return statement inside dynamic method.
     * =========================================================
     */
    private int findReturnStatement(
            String source,
            int declarationIndex) {

        int methodStart =
                source.indexOf(
                        '{',
                        declarationIndex);

        if (methodStart == -1) {
            return -1;
        }

        int returnIndex =
                source.indexOf(
                        "return",
                        methodStart);

        return returnIndex;
    }

    private void validate(
            Path javaFile,
            String variableName,
            LocatorSuggestion suggestion)
            throws IOException {

        if (javaFile == null) {
            throw new IllegalArgumentException(
                    "Java file cannot be null.");
        }

        if (!Files.exists(javaFile)) {
            throw new IOException(
                    "Java file not found : "
                            + javaFile);
        }

        if (variableName == null
                || variableName.isBlank()) {

            throw new IllegalArgumentException(
                    "Variable name is missing.");
        }

        if (suggestion == null) {
            throw new IllegalArgumentException(
                    "LocatorSuggestion cannot be null.");
        }
    }

    /*
     * =========================================================
     * Finds '=' after variable.
     * =========================================================
     */
    private int findAssignmentOperator(
            String source,
            int variableIndex) {

        for (int i = variableIndex;
             i < source.length();
             i++) {

            if (source.charAt(i) == '=') {
                return i;
            }

            if (source.charAt(i) == ';') {
                return -1;
            }
        }

        return -1;
    }

    /*
     * =========================================================
     * Finds By... after assignment.
     * =========================================================
     */
    private int findLocatorStart(
            String source,
            int assignmentIndex) {

        return source.indexOf(
                "By.",
                assignmentIndex);
    }

    /*
     * =========================================================
     * Finds end of By(...) expression.
     * =========================================================
     */
    private int findLocatorEnd(
            String source,
            int locatorStart) {

        boolean insideQuotes = false;
        char quoteChar = 0;

        int parentheses = 0;

        for (int i = locatorStart;
             i < source.length();
             i++) {

            char c = source.charAt(i);

            if (insideQuotes) {

                if (c == quoteChar
                        && source.charAt(i - 1) != '\\') {

                    insideQuotes = false;
                }

                continue;
            }

            if (c == '"' || c == '\'') {

                insideQuotes = true;
                quoteChar = c;

                continue;
            }

            if (c == '(') {
                parentheses++;
            }

            if (c == ')') {

                parentheses--;

                if (parentheses == 0) {

                    return i + 1;
                }
            }
        }

        return -1;
    }

    /*
     * =========================================================
     * Builds Selenium locator expression.
     * =========================================================
     */
    private String buildLocatorExpression(
            LocatorSuggestion suggestion) {

        if (suggestion.getLocatorType() == null
                || suggestion.getLocatorType().isBlank()) {

            throw new IllegalArgumentException(
                    "Locator type is missing.");
        }

        if (suggestion.getLocatorValue() == null
                || suggestion.getLocatorValue().isBlank()) {

            throw new IllegalArgumentException(
                    "Locator value is missing.");
        }

        String type =
                suggestion.getLocatorType()
                        .toLowerCase()
                        .replace("by.", "")
                        .trim();

        String value =
                escapeJavaString(
                        suggestion.getLocatorValue());

        switch (type) {

            case "id":
                return "By.id(\"" + value + "\")";

            case "name":
                return "By.name(\"" + value + "\")";

            case "xpath":
                return "By.xpath(\"" + value + "\")";

            case "css":
            case "cssselector":
                return "By.cssSelector(\"" + value + "\")";

            case "classname":
            case "class":
                return "By.className(\"" + value + "\")";

            case "tag":
            case "tagname":
                return "By.tagName(\"" + value + "\")";

            case "linktext":
                return "By.linkText(\"" + value + "\")";

            case "partiallinktext":
                return "By.partialLinkText(\"" + value + "\")";

            default:
                throw new IllegalArgumentException(
                        "Unsupported locator type : "
                                + suggestion.getLocatorType());
        }
    }

    /*
     * =========================================================
     * Escape Java string.
     * =========================================================
     */
    private String escapeJavaString(
            String value) {

        return value.replace(
                "\"",
                "\\\"");
    }
}