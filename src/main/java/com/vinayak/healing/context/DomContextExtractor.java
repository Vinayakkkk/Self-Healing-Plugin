package com.vinayak.healing.context;

import com.vinayak.healing.model.FailureContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DomContextExtractor {

    private static final int MAX_ANCESTOR_DEPTH = 8;

   public void populate(
        FailureContext context,
        WebDriver driver,
        WebElement element) {

        if (context == null || element == null) {
            return;
        }

        populateParentContext(
                context,
                element);

      context.setNearestLabel(
        resolveNearestLabel(
                driver,
                element));

        context.setNeighbourTexts(
                extractNeighbourTexts(
                        driver,
                        element));
    }

    /*
     * ------------------------------------
     * Parent Context
     * ------------------------------------
     */

    private void populateParentContext(
            FailureContext context,
            WebElement element) {

        try {

            WebElement parent =
                    element.findElement(By.xpath(".."));

            context.setParentTag(
                    safe(parent.getTagName()));

            context.setParentId(
                    safe(parent.getAttribute("id")));

            context.setParentClass(
                    safe(parent.getAttribute("class")));

        } catch (Exception ignored) {
        }
    }

    /*
     * ------------------------------------
     * Label Resolution
     * ------------------------------------
     */

   private String resolveNearestLabel(
        WebDriver driver,
        WebElement element) {

        String label;

        label =
        findAssociatedLabel(
                driver,
                element);

        if (!label.isBlank())
            return label;

        label = findSiblingLabel(element);

        if (!label.isBlank())
            return label;

        label = findAncestorLabel(element);

        if (!label.isBlank())
            return label;

        label = findAttributeLabel(element);

        return label;
    }

    /*
     * label[for=id]
     */

    private String findAssociatedLabel(
        WebDriver driver,
        WebElement element) {

        try {

            String id =
                    element.getAttribute("id");

            if (id == null || id.isBlank())
                return "";

           List<WebElement> labels =
        driver.findElements(
                By.xpath(
                        "//label[@for='"
                                + id
                                + "']"));

if (!labels.isEmpty()) {

    return normalize(
            labels.get(0).getText());
}

return "";

        } catch (Exception ignored) {

            return "";
        }
    }

    /*
     * Previous sibling label/text
     */

    private String findSiblingLabel(
            WebElement element) {

        try {

            List<WebElement> siblings =
                    element.findElements(
                            By.xpath("../*"));

            int index =
                    siblings.indexOf(element);

            if (index <= 0)
                return "";

            for (int i = index - 1; i >= 0; i--) {

                String text =
                        normalize(
                                siblings.get(i)
                                        .getText());

                if (!text.isBlank()) {
                    return text;
                }
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    /*
     * Ancestor Search
     */

    private String findAncestorLabel(
            WebElement element) {

        try {

            WebElement current =
                    element;

            for (int depth = 0;
                 depth < MAX_ANCESTOR_DEPTH;
                 depth++) {

                current =
                        current.findElement(
                                By.xpath(".."));

                List<WebElement> labels =
                        current.findElements(
                                By.tagName("label"));

                for (WebElement label : labels) {

                    String text =
                            normalize(
                                    label.getText());

                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    /*
     * Attribute fallback
     */

    private String findAttributeLabel(
            WebElement element) {

        String[] attributes = {

                "placeholder",

                "aria-label",

                "title",

                "name",

                "value"

        };

        for (String attribute :
                attributes) {

            try {

                String value =
                        normalize(
                                element.getAttribute(
                                        attribute));

                if (!value.isBlank()) {

                    return value;
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }
        /*
     * ------------------------------------
     * Neighbour Text Extraction
     * ------------------------------------
     */

   private List<String> extractNeighbourTexts(
        WebDriver driver,
        WebElement element) {

        Set<String> texts =
                new LinkedHashSet<>();

        try {

            List<WebElement> neighbours =
                    element.findElements(
                            By.xpath("../*"));

            for (WebElement neighbour :
                    neighbours) {

                String text =
                        normalize(
                                neighbour.getText());

                if (!text.isBlank()) {

                    texts.add(text);
                }
            }

        } catch (Exception ignored) {
        }

        try {

            List<WebElement> descendants =
                    element.findElements(
                            By.xpath(".//*"));

            for (WebElement child :
                    descendants) {

                String text =
                        normalize(
                                child.getText());

                if (!text.isBlank()) {

                    texts.add(text);
                }
            }

        } catch (Exception ignored) {
        }

        return new ArrayList<>(texts);
    }

    /*
     * ------------------------------------
     * Utility
     * ------------------------------------
     */

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value

                .replace('\n', ' ')

                .replace('\t', ' ')

                .replaceAll("\\s+", " ")

                .trim();
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }
}