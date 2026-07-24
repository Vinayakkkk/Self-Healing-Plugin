package com.vinayak.healing.model;
import com.vinayak.healing.intent.ElementIntent;
public class LocatorCandidate {

    private String locatorType;
    private String locatorValue;
private String nearestLabel = "";
    private String tagName;
    private String inputType;
    private ElementIntent intent;
private String parentTag;
private String parentClass;
private String parentId;
private int occurrenceCount = 1;
private boolean uniqueLocator = true;

    private double score;
    private double finalScore;
    public LocatorCandidate(
        String locatorType,
        String locatorValue,
        String tagName,
        String inputType,
        ElementIntent intent,
        double score,
        String parentTag,
        String parentClass,
        String parentId) {

    this.locatorType = locatorType;
    this.locatorValue = locatorValue;
    this.tagName = tagName;
    this.inputType = inputType;
    this.intent = intent;
    this.score = score;

    this.parentTag = parentTag;
    this.parentClass = parentClass;
    this.parentId = parentId;

    this.finalScore = score;
}

public LocatorCandidate(
        String locatorType,
        String locatorValue) {

    this(
            locatorType,
            locatorValue,
            "",
            "",
            null,
            0,
            "",
            "",
            ""
    );
}
public int getOccurrenceCount() {
    return occurrenceCount;
}

public void setOccurrenceCount(int occurrenceCount) {
    this.occurrenceCount = Math.max(1, occurrenceCount);
    this.uniqueLocator = this.occurrenceCount == 1;
}

public boolean isUniqueLocator() {
    return uniqueLocator;
}

public void setUniqueLocator(boolean uniqueLocator) {
    this.uniqueLocator = uniqueLocator;
}

public String getNearestLabel() {
    return nearestLabel;
}

public void setNearestLabel(String nearestLabel) {
    this.nearestLabel =
            nearestLabel == null
                    ? ""
                    : nearestLabel;
}

    public String getParentTag() {
        return parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = parentTag;
    }

    public String getParentClass() {
        return parentClass;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public String getParentId() {
        return parentId;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    public String getTagName() {
        return tagName;
    }

    public String getInputType() {
        return inputType;
    }

    public ElementIntent getIntent() {
        return intent;
    }

    public double getScore() {
        return score;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(
            double finalScore) {

        this.finalScore = finalScore;
    }

    @Override
    public String toString() {

        return "LocatorCandidate{" +
                "locatorType='" + locatorType + '\'' +
                ", locatorValue='" + locatorValue + '\'' +
                ", parentTag='" + parentTag + '\'' +
                ", parentClass='" + parentClass + '\'' +
                ", parentId='" + parentId + '\'' +
                ", inputType='" + inputType + '\'' +
                ", intent=" + intent +
                ", score=" + score +
                ", finalScore=" + finalScore +
                ", occurrenceCount=" + occurrenceCount +
", uniqueLocator=" + uniqueLocator +
                '}';
    }
}