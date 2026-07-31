package com.vinayak.healing.source;

import com.vinayak.healing.logging.HealingLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PageObjectResolver {

    public String findPageObjectFromStackTrace() {

        StackTraceElement[] stack =
                Thread.currentThread().getStackTrace();

        String projectRoot =
                System.getProperty("user.dir");

        String latestPageObject = null;

        for (StackTraceElement element : stack) {

            String className = element.getClassName();

            if (!(className.startsWith("pages.")
                    || className.contains(".pages."))) {
                continue;
            }

            String fileName =
                    className.substring(
                            className.lastIndexOf('.') + 1)
                            + ".java";

            try {

                Path root = Paths.get(projectRoot);

                Optional<Path> match =
                        Files.walk(root)
                                .filter(path ->
                                        path.getFileName()
                                                .toString()
                                                .equals(fileName))
                                .findFirst();

                if (match.isPresent()) {

                    latestPageObject =
                            match.get()
                                    .toAbsolutePath()
                                    .toString();
                }

            } catch (Exception e) {

                HealingLogger.error(
                        "Page Object lookup failed",
                        e);
            }
        }

        if (latestPageObject == null) {

            HealingLogger.debug(
                    "PAGE OBJECT NOT FOUND");
        }

        return latestPageObject;
    }
}