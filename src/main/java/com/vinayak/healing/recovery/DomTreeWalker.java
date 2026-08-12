package com.vinayak.healing.recovery;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class DomTreeWalker {

    public List<WebElement> walk(
            WebElement root) {

        List<WebElement> elements =
                new ArrayList<>();

        if (root == null) {
            return elements;
        }

        /*
         * 1. Current element + descendants
         */
        collect(root, elements);

        /*
         * 2. Parent + parent's descendants
         *
         * Important for cases such as:
         *
         * <div>
         *     <label>Employee Name</label>
         *     <input>
         * </div>
         *
         * If healing initially selects the label,
         * the actual input is a sibling.
         */
        try {

            WebElement parent =
                    root.findElement(
                            By.xpath(".."));

            collectUnique(
                    parent,
                    elements);

        } catch (Exception ignored) {
        }

        return elements;
    }

    private void collect(
            WebElement element,
            List<WebElement> elements) {

        if (element == null) {
            return;
        }

        collectUnique(
                element,
                elements);

        List<WebElement> children;

        try {

            children =
                    element.findElements(
                            By.xpath("./*"));

        } catch (Exception e) {

            return;
        }

        for (WebElement child : children) {

            collect(
                    child,
                    elements);
        }
    }

    private void collectUnique(
            WebElement element,
            List<WebElement> elements) {

        if (element == null) {
            return;
        }

        try {

            if (!containsElement(
                    elements,
                    element)) {

                elements.add(element);
            }

        } catch (Exception ignored) {
        }
    }

    private boolean containsElement(
            List<WebElement> elements,
            WebElement target) {

        for (WebElement element : elements) {

            try {

                if (element.equals(target)) {
                    return true;
                }

            } catch (Exception ignored) {
            }
        }

        return false;
    }
}