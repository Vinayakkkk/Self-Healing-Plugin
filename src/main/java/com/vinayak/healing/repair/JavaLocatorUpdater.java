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
            LocatorSuggestion suggestion)
            throws IOException {

        validate(javaFile, variableName, suggestion);

        String source =
                Files.readString(
                        javaFile,
                        StandardCharsets.UTF_8);

        int variableIndex =
                findVariableDeclaration(
                        source,
                        variableName);

        if (variableIndex == -1) {
            return false;
        }

        int equalIndex =
                findAssignmentOperator(
                        source,
                        variableIndex);

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
                source.substring(0, locatorStart)
                        + newLocator
                        + source.substring(locatorEnd);

        if (updatedSource.equals(source)) {
            return false;
        }

        Files.writeString(
                javaFile,
                updatedSource,
                StandardCharsets.UTF_8);

        return true;
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

    /**
     * Finds:
     *
     * private By username =
     *
     * returns index of username.
     */
    private int findVariableDeclaration(
            String source,
            String variableName) {

        int searchIndex = 0;

        while (true) {

            searchIndex =
                    source.indexOf(
                            variableName,
                            searchIndex);

            if (searchIndex == -1) {
                return -1;
            }

            boolean leftOk =
                    searchIndex == 0
                            || !Character.isJavaIdentifierPart(
                                    source.charAt(searchIndex - 1));

            int end =
                    searchIndex
                            + variableName.length();

            boolean rightOk =
                    end >= source.length()
                            || !Character.isJavaIdentifierPart(
                                    source.charAt(end));

            if (leftOk && rightOk) {
                return searchIndex;
            }

            searchIndex =
                    end;
        }
    }

    /**
     * Finds '=' after variable.
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

    /**
     * Finds start of By...
     */
    private int findLocatorStart(
            String source,
            int assignmentIndex) {

        int byIndex =
                source.indexOf(
                        "By.",
                        assignmentIndex);

        return byIndex;
    }
        /**
     * Finds the end of the locator expression.
     *
     * Supports:
     *
     * By.id(...)
     * By.name(...)
     * By.xpath(...)
     * By.cssSelector(...)
     * By.className(...)
     * By.tagName(...)
     * By.linkText(...)
     * By.partialLinkText(...)
     *
     * Works for:
     *
     * By.id("user");
     *
     * By.xpath(
     *      "//input[@id='user']");
     *
     * By.cssSelector(
     *      "[placeholder='Search']");
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

            /*
             * Handle quoted strings.
             */
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

            /*
             * Count parentheses.
             */
            if (c == '(') {
                parentheses++;
            }

            if (c == ')') {

                parentheses--;

                /*
                 * End of By(...) expression.
                 */
                if (parentheses == 0) {

                    int index = i + 1;

                    /*
                     * Skip whitespace.
                     */
                    while (index < source.length()
                            && Character.isWhitespace(
                                    source.charAt(index))) {

                        index++;
                    }

                    /*
                     * Skip semicolon.
                     */
                   /*
 * Do NOT consume the semicolon.
 * Return the position immediately after ')'.
 * The existing ';' remains in the source.
 */
return i + 1;
                }
            }
        }

        return -1;
    }

    /**
     * Builds a Selenium locator expression.
     *
     * Example:
     *
     * By.id("username")
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

    /**
     * Escapes Java string literals.
     */
    private String escapeJavaString(
            String value) {

        return value
                .replace("\"", "\\\"");
    }
}