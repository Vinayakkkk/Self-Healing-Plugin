package com.vinayak.healing.source;

import java.io.File;
import java.nio.file.Files;

import com.vinayak.healing.logging.HealingLogger;


public class SourceCodeAnalyzer {

    public String findDeclaration(
            String sourceFile,
            String locatorValue) {

        try {

            if(sourceFile == null
                    || sourceFile.isBlank()) {

                return "";
            }

            File file =
                    new File(sourceFile);


                    HealingLogger.debug(
        "ABSOLUTE PATH = "
                + file.getAbsolutePath());

HealingLogger.debug(
        "EXISTS = "
                + file.exists());

            if(!file.exists()) {

                HealingLogger.debug(
                        "DECLARATION FILE NOT FOUND : "
                                + sourceFile);

                return "";
            }

            String code =
                    Files.readString(
                            file.toPath());

            String declaration =
                    new VariableExtractor()
                            .extractDeclaration(
                                    code,
                                    locatorValue);

            HealingLogger.debug(
                    "DECLARATION FOUND = "
                            + declaration);

            return declaration;

        } catch(Exception e) {

            HealingLogger.error(
                    "DECLARATION LOOKUP FAILED : "
                            + sourceFile, e);

            

            return "";
        }
    }

    public String findVariableName(
            String filePath,
          String locatorValue) {

        try {

            if(filePath == null
                    || filePath.isBlank()) {

                return "";
            }

            File file =
                    new File(filePath);

            if(!file.exists()) {

                HealingLogger.debug(
                        "SOURCE FILE NOT FOUND : "
                                + filePath);

                return "";
            }

            HealingLogger.debug(
                    "SOURCE FILE = "
                            + filePath);

            HealingLogger.debug(
                    "LOCATOR VALUE = "
                            + locatorValue);

            String sourceCode =
                    Files.readString(
                            file.toPath());

            HealingLogger.debug(
                    "SOURCE FILE LOADED");

            VariableExtractor extractor =
                    new VariableExtractor();

            String variableName =
                    extractor.extract(
                            sourceCode,
                            locatorValue);

            HealingLogger.debug(
                    "VARIABLE FOUND = "
                            + variableName);

            return variableName;

        } catch(Exception e) {

            HealingLogger.error(
                    "VARIABLE LOOKUP FAILED",
                    e);

            

            return "";
        }
    }

   public String findSourceFile(String projectRoot,
                             String variableName) {

    try {

        if (projectRoot == null || projectRoot.isBlank()) {
            return "";
        }

        File root = new File(projectRoot);

        if (!root.exists()) {
            return "";
        }

       

        return searchDirectory(root, variableName);

    } catch (Exception e) {

        HealingLogger.error(
                "SOURCE FILE SEARCH FAILED",
                e);

        return "";
    }
}

private String searchDirectory(File directory,
                               String variableName) {

    File[] files = directory.listFiles();

    if (files == null) {
        return "";
    }

    for (File file : files) {

        if (file.isDirectory()) {

          String result =
        searchDirectory(
                file,
                variableName);

            if (!result.isBlank()) {
                return result;
            }

        } else if (file.getName().endsWith(".java")) {

            try {

                String source =
                        Files.readString(
                                file.toPath());

               if (variableName != null
        && !variableName.isBlank()
        && source.contains(variableName)) {

    HealingLogger.debug(
            "SOURCE FILE FOUND = "
                    + file.getAbsolutePath());

    return file.getAbsolutePath();
}

            } catch (Exception ignored) {
            }
        }
    }

    return "";
}
    private String normalizeLocator(String value) {

    if (value == null) {
        return "";
    }

    return value
            .replaceAll("\\s+", "")
            .replace("\"", "")
            .replace("'", "")
            .replace("By.xpath:", "")
            .replace("By.id:", "")
            .replace("By.name:", "")
            .replace("By.cssSelector:", "")
            .trim();
}
}