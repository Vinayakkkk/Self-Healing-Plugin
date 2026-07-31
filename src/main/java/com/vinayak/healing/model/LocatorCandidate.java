package com.vinayak.healing.model;
import com.vinayak.healing.intent.ElementIntent;
public class LocatorCandidate {

    private String locatorType;
    private String locatorValue;
private String nearestLabel = "";
private String elementText = "";
private String placeholder = "";
private String id = "";
private String name = "";
private String ariaLabel = "";
    private String tagName;
    private String inputType;
    private boolean generatedLocator = false;
    private String generationStrategy = "";
    private double generationConfidence = 0.0;
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

public boolean isGeneratedLocator() {
    return generatedLocator;
}

public void setGeneratedLocator(boolean generatedLocator) {
    this.generatedLocator = generatedLocator;
}

public String getGenerationStrategy() {
    return generationStrategy;
}

public void setGenerationStrategy(String generationStrategy) {
    this.generationStrategy = generationStrategy;
}

public double getGenerationConfidence() {
    return generationConfidence;
}

public void setGenerationConfidence(double generationConfidence) {
    this.generationConfidence = generationConfidence;
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
    public String getElementText() {
    return elementText;
}

public void setElementText(String elementText) {
    this.elementText = elementText == null ? "" : elementText;
}

public String getPlaceholder() {
    return placeholder;
}

public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder == null ? "" : placeholder;
}

public String getId() {
    return id;
}

public void setId(String id) {
    this.id = id == null ? "" : id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name == null ? "" : name;
}

public String getAriaLabel() {
    return ariaLabel;
}

public void setAriaLabel(String ariaLabel) {
    this.ariaLabel = ariaLabel == null ? "" : ariaLabel;
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
                ", generatedLocator=" + generatedLocator +
", generationStrategy='" + generationStrategy + '\'' +
", generationConfidence=" + generationConfidence +
                ", occurrenceCount=" + occurrenceCount +
", uniqueLocator=" + uniqueLocator +
                '}';
    }
}