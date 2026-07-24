package com.vinayak.healing.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.vinayak.healing.logging.HealingLogger;

public class VariableExtractor {

private static final Pattern BY_DECLARATION_PATTERN =
        Pattern.compile(
                "(?:private|protected|public)?\\s*"
                        + "(?:static\\s+)?"
                        + "(?:final\\s+)?"
                        + "By\\s+(\\w+)\\s*=\\s*"
                        + "By\\.[a-zA-Z]+\\s*\\((.*?)\\)\\s*;",
                Pattern.DOTALL);

public String extractDeclaration(
        String sourceCode,
        String locatorValue) {

    MatchResult result =
            findMatchingDeclaration(
                    sourceCode,
                    locatorValue);

    return result == null
            ? ""
            : result.declaration;
}

public String extract(
        String sourceCode,
        String locatorValue) {

    MatchResult result =
            findMatchingDeclaration(
                    sourceCode,
                    locatorValue);

    return result == null
            ? ""
            : result.variableName;
}

private MatchResult findMatchingDeclaration(
        String sourceCode,
        String locatorValue) {


                JavaLocatorExtractor.VariableMatch match =
        new JavaLocatorExtractor()
                .find(
                        sourceCode,
                        locatorValue);

if (match != null) {

    HealingLogger.debug(
            "VARIABLE FOUND BY JAVAPARSER : "
                    + match.getVariableName());

    return new MatchResult(
            match.getVariableName(),
            match.getDeclaration());
}

    if (sourceCode == null
            || sourceCode.isBlank()
            || locatorValue == null
            || locatorValue.isBlank()) {

        return null;
    }

    String actualLocator =
            extractLocatorValue(locatorValue);

    if (actualLocator.isBlank()) {
        return null;
    }

    Matcher matcher =
            BY_DECLARATION_PATTERN.matcher(
                    sourceCode);

    while (matcher.find()) {

        String variableName =
                matcher.group(1);

        String declaration =
                matcher.group();

        String locatorExpression =
                matcher.group(2);

        String joinedLocator =
                extractAndJoinQuotedParts(
                        locatorExpression);

        if (sameLocator(
                joinedLocator,
                actualLocator)) {

            HealingLogger.debug(
                    "VARIABLE FOUND : "
                            + variableName);

            HealingLogger.debug(
                    "DECLARATION FOUND : "
                            + declaration);

            return new MatchResult(
                    variableName,
                    declaration);
        }
    }

    HealingLogger.debug(
            "VARIABLE NOT FOUND FOR : "
                    + actualLocator);

    return null;
}

private String extractLocatorValue(
        String failedLocator) {

    if (failedLocator == null
            || failedLocator.isBlank()) {

        return "";
    }

    int colonIndex =
            failedLocator.indexOf(":");

    if (colonIndex == -1) {
        return failedLocator.trim();
    }

    return failedLocator
            .substring(colonIndex + 1)
            .trim();
}

private String extractAndJoinQuotedParts(
        String locatorExpression) {

    if (locatorExpression == null
            || locatorExpression.isBlank()) {

        return "";
    }

    Pattern quotePattern =
            Pattern.compile(
                    "\"([^\"]*)\"|'([^']*)'");

    Matcher matcher =
            quotePattern.matcher(
                    locatorExpression);

    StringBuilder joined =
            new StringBuilder();

    while (matcher.find()) {

        String doubleQuoted =
                matcher.group(1);

        String singleQuoted =
                matcher.group(2);

        if (doubleQuoted != null) {
            joined.append(doubleQuoted);
        } else if (singleQuoted != null) {
            joined.append(singleQuoted);
        }
    }

    return joined.toString();
}

private boolean sameLocator(
        String declarationLocator,
        String failedLocator) {

    String left =
            normalize(declarationLocator);

    String right =
            normalize(failedLocator);

    return !left.isBlank()
            && left.equals(right);
}

private String normalize(
        String value) {

    if (value == null) {
        return "";
    }

    return value
            .replaceAll("\\s+", "")
            .replace("\"", "")
            .replace("'", "")
            .trim()
            .toLowerCase();
}

private static class MatchResult {

    private final String variableName;
    private final String declaration;

    private MatchResult(
            String variableName,
            String declaration) {

        this.variableName = variableName;
        this.declaration = declaration;
    }
}


}
