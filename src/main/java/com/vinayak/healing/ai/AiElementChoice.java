package com.vinayak.healing.ai;

public class AiElementChoice {

    private final int index;
    private final String locatorType;
    private final String locatorValue;
    private final String tag;
    private final String text;
    private final String cssClass;
    private final String parentTag;
    private final String parentClass;
    private final String parentHref;

    public AiElementChoice(
            int index,
            String locatorType,
            String locatorValue,
            String tag,
            String text,
            String cssClass,
            String parentTag,
            String parentClass,
            String parentHref) {

        this.index = index;
        this.locatorType = locatorType;
        this.locatorValue = locatorValue;
        this.tag = tag;
        this.text = text;
        this.cssClass = cssClass;
        this.parentTag = parentTag;
        this.parentClass = parentClass;
        this.parentHref = parentHref;
    }

    public int getIndex() {
        return index;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    public String getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getParentTag() {
        return parentTag;
    }

    public String getParentClass() {
        return parentClass;
    }

    public String getParentHref() {
        return parentHref;
    }
}