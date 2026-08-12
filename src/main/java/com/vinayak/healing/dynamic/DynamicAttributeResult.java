package com.vinayak.healing.dynamic;

public class DynamicAttributeResult {

    private String attributeName;

    private String originalValue;

    private String normalizedValue;

    private DynamicPatternType patternType;

    private boolean dynamic;

    private double stabilityScore;

    public DynamicAttributeResult() {
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(String normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public DynamicPatternType getPatternType() {
        return patternType;
    }

    public void setPatternType(DynamicPatternType patternType) {
        this.patternType = patternType;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public double getStabilityScore() {
        return stabilityScore;
    }

    public void setStabilityScore(double stabilityScore) {
        this.stabilityScore = stabilityScore;
    }

    @Override
    public String toString() {
        return "DynamicAttributeResult{" +
                "attributeName='" + attributeName + '\'' +
                ", originalValue='" + originalValue + '\'' +
                ", normalizedValue='" + normalizedValue + '\'' +
                ", patternType=" + patternType +
                ", dynamic=" + dynamic +
                ", stabilityScore=" + stabilityScore +
                '}';
    }

}