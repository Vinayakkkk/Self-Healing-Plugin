package com.vinayak.healing.capability;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Validates whether a WebElement supports
 * a required capability.
 */
public final class CapabilityValidator {

    public boolean supports(
            WebElement element,
            ElementCapability capability) {

        if (element == null || capability == null) {
            return false;
        }

        switch (capability) {

            case TYPE:
                return supportsTyping(element);

            case CLICK:
                return supportsClick(element);

            case CLEAR:
                return supportsClear(element);

            case SELECT:
                return supportsSelect(element);

            case SUBMIT:
                return supportsSubmit(element);

            case CHECK:
                return supportsCheckBox(element);

            case RADIO:
                return supportsRadio(element);

            case HOVER:
                return supportsHover(element);

            case DRAG_DROP:
                return supportsDragDrop(element);

            case UPLOAD:
                return supportsUpload(element);

            case FOCUS:
                return supportsFocus(element);

            case SCROLL:
                return supportsScroll(element);

            default:
                return false;
        }
    }

    /**
     * Supports keyboard input.
     */
    private boolean supportsTyping(
            WebElement element) {

        String tag =
                element.getTagName().toLowerCase().trim();

        String contentEditable =
                element.getAttribute("contenteditable");

        String role =
                element.getAttribute("role");

        String ariaMultiline =
                element.getAttribute("aria-multiline");

        return tag.equals("input")
                || tag.equals("textarea")
                || "true".equalsIgnoreCase(contentEditable)
                || "".equals(contentEditable)
                || "textbox".equalsIgnoreCase(role)
                || "true".equalsIgnoreCase(ariaMultiline);
    }

    /**
     * Supports click.
     */
private boolean supportsClick(
        WebElement element) {

    if (!element.isDisplayed()
            || !element.isEnabled()) {

        return false;
    }

    String tag =
            element.getTagName()
                    .toLowerCase()
                    .trim();

    String role =
            element.getAttribute("role");

    /*
     * Directly clickable elements
     */
    if (tag.equals("button")
            || tag.equals("a")
            || tag.equals("input")
            || tag.equals("label")
            || "button".equalsIgnoreCase(role)
            || supportsTyping(element)) {

        return true;
    }

    /*
     * The element itself may be a child of the
     * actual clickable control.
     *
     * Example:
     *
     * <a href="/directory">
     *     <span>Directory</span>
     * </a>
     *
     * The healed locator may identify the span,
     * while the clickable control is its ancestor.
     */
    try {

        List<WebElement> clickableAncestors =
                element.findElements(
                        By.xpath(
                                "./ancestor::*["
                                + "self::a"
                                + " or self::button"
                                + " or self::input"
                                + " or @role='button'"
                                + " or @onclick"
                                + "]"));

        for (WebElement ancestor :
                clickableAncestors) {

            if (ancestor.isDisplayed()
                    && ancestor.isEnabled()) {

                return true;
            }
        }

    } catch (Exception ignored) {
        // No clickable ancestor found.
    }

    return false;
}

    /**
     * Clear requires typing capability.
     */
    private boolean supportsClear(
            WebElement element) {

        return supportsTyping(element);
    }

    /**
     * Supports Selenium Select.
     */
    private boolean supportsSelect(
            WebElement element) {

        return "select".equalsIgnoreCase(
                element.getTagName());
    }

    /**
     * Supports submit.
     */
    private boolean supportsSubmit(
            WebElement element) {

        String tag =
                element.getTagName().toLowerCase();

        return tag.equals("form")
                || tag.equals("button")
                || tag.equals("input");
    }

    /**
     * Supports checkbox.
     */
    private boolean supportsCheckBox(
            WebElement element) {

        return "checkbox".equalsIgnoreCase(
                element.getAttribute("type"));
    }

    /**
     * Supports radio.
     */
    private boolean supportsRadio(
            WebElement element) {

        return "radio".equalsIgnoreCase(
                element.getAttribute("type"));
    }

    /**
     * Supports upload.
     */
    private boolean supportsUpload(
            WebElement element) {

        return "file".equalsIgnoreCase(
                element.getAttribute("type"));
    }

    /**
     * Supports focus.
     */
    private boolean supportsFocus(
            WebElement element) {

        return element.isDisplayed()
                && element.isEnabled();
    }

    /**
     * Supports scrolling.
     */
    private boolean supportsScroll(
            WebElement element) {

        return element.isDisplayed();
    }

    /**
     * Supports hover.
     */
    private boolean supportsHover(
            WebElement element) {

        return element.isDisplayed();
    }

    /**
     * Supports drag and drop.
     */
    private boolean supportsDragDrop(
            WebElement element) {

        return element.isDisplayed()
                && element.isEnabled();
    }
}