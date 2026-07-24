package com.vinayak.healing.ai;

import com.vinayak.healing.engine.LocatorBuilder;
import com.vinayak.healing.model.LocatorCandidate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class AiElementChoiceFinder {

    private static final int MAX_MATCHES = 5;

    public List<AiElementChoice> findChoices(
            WebDriver driver,
            List<LocatorCandidate> candidates) {

        List<AiElementChoice> choices =
                new ArrayList<>();

        if (driver == null
                || candidates == null) {
            return choices;
        }

        int index = 1;

        for (LocatorCandidate candidate : candidates) {

            By locator;

            try {
                locator = LocatorBuilder.build(candidate);
            } catch (Exception ignored) {
                continue;
            }

            if (locator == null) {
                continue;
            }

            List<WebElement> matches;

            try {
                matches = driver.findElements(locator);
            } catch (Exception ignored) {
                continue;
            }

            // We need only ambiguous candidates.
            if (matches.size() < 2
                    || matches.size() > MAX_MATCHES) {
                continue;
            }

            for (WebElement element : matches) {

                try {
                    WebElement parent =
                            element.findElement(
                                    By.xpath(".."));

                    choices.add(
                            new AiElementChoice(
                                    index++,
                                    candidate.getLocatorType(),
                                    candidate.getLocatorValue(),
                                    safe(element.getTagName()),
                                    safe(element.getText()),
                                    safe(element.getAttribute("class")),
                                    safe(parent.getTagName()),
                                    safe(parent.getAttribute("class")),
                                    safe(parent.getAttribute("href"))));

                } catch (Exception ignored) {
                    // Ignore one unusable runtime element.
                }
            }

            // Send one ambiguous candidate group at a time.
            if (!choices.isEmpty()) {
                return choices;
            }
        }

        return choices;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}