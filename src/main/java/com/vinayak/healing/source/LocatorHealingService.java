package com.vinayak.healing.source;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openqa.selenium.By;

public class LocatorHealingService {

    public boolean replaceLocator(
            String pageObjectPath,
            String oldDeclaration,
            By healedLocator) {

        try {

            if (pageObjectPath == null
                    || pageObjectPath.isBlank()) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "page object path is empty");

                return false;
            }

            if (oldDeclaration == null
                    || oldDeclaration.isBlank()) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "old locator declaration is empty");

                return false;
            }

            if (healedLocator == null) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "healed locator is null");

                return false;
            }

            Path path =
                    Path.of(pageObjectPath);

            if (!Files.exists(path)) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "file does not exist: "
                                + pageObjectPath);

                return false;
            }

            String source =
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8);

            String newDeclaration =
                    buildDeclaration(
                            oldDeclaration,
                            healedLocator);

            if (newDeclaration == null) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "unsupported healed locator: "
                                + healedLocator);

                return false;
            }

            if (!source.contains(oldDeclaration)) {

                System.out.println(
                        "SOURCE REPAIR FAILED: "
                                + "old declaration was not found");

                System.out.println(
                        "OLD DECLARATION: "
                                + oldDeclaration);

                return false;
            }

            String updatedSource =
                    source.replace(
                            oldDeclaration,
                            newDeclaration);

            Files.writeString(
                    path,
                    updatedSource,
                    StandardCharsets.UTF_8);

            System.out.println(
                    "SOURCE CODE UPDATED SUCCESSFULLY");

            System.out.println(
                    "FILE: "
                            + pageObjectPath);

            System.out.println(
                    "OLD: "
                            + oldDeclaration);

            System.out.println(
                    "NEW: "
                            + newDeclaration);

            return true;

        } catch (Exception exception) {

            System.out.println(
                    "SOURCE REPAIR ERROR: "
                            + exception.getMessage());

            exception.printStackTrace();

            return false;
        }
    }

    private String buildDeclaration(
            String oldDeclaration,
            By healedLocator) {

        int equalsIndex =
                oldDeclaration.indexOf("=");

        if (equalsIndex == -1) {
            return null;
        }

        String leftSide =
                oldDeclaration.substring(
                        0,
                        equalsIndex + 1);

        String locatorText =
                healedLocator.toString();

        if (locatorText.startsWith("By.id: ")) {

            String value =
                    locatorText.replace(
                            "By.id: ",
                            "");

            return leftSide
                    + "\n            By.id(\""
                    + escapeJava(value)
                    + "\");";
        }

        if (locatorText.startsWith("By.name: ")) {

            String value =
                    locatorText.replace(
                            "By.name: ",
                            "");

            return leftSide
                    + "\n            By.name(\""
                    + escapeJava(value)
                    + "\");";
        }

        if (locatorText.startsWith("By.cssSelector: ")) {

            String value =
                    locatorText.replace(
                            "By.cssSelector: ",
                            "");

            return leftSide
                    + "\n            By.cssSelector(\""
                    + escapeJava(value)
                    + "\");";
        }

        if (locatorText.startsWith("By.xpath: ")) {

            String value =
                    locatorText.replace(
                            "By.xpath: ",
                            "");

            return leftSide
                    + "\n            By.xpath(\""
                    + escapeJava(value)
                    + "\");";
        }

        return null;
    }

    private String escapeJava(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}