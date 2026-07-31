package com.vinayak.healing.ai;

public class LocatorSuggestion {

    private String locatorType;
    private String locatorValue;
    private double confidence;
    private boolean generatedLocator;
private String generationStrategy;
private double generationConfidence;

    // Required by Jackson
    public LocatorSuggestion() {
    }

    // Convenient constructor
    public LocatorSuggestion(
            String locatorType,
            String locatorValue,
            double confidence
        ) {

        this.locatorType = locatorType;
        this.locatorValue = locatorValue;
        this.confidence = confidence;
        
    }

    public LocatorSuggestion(
        String locatorType,
        String locatorValue,
        double confidence,
        boolean generatedLocator,
        String generationStrategy,
        double generationConfidence) {

    this.locatorType = locatorType;
    this.locatorValue = locatorValue;
    this.confidence = confidence;
    this.generatedLocator = generatedLocator;
    this.generationStrategy = generationStrategy;
    this.generationConfidence = generationConfidence;
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
    public String getLocatorType() {
        return locatorType;
    }

    public void setLocatorType(String locatorType) {
        this.locatorType = locatorType;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    public void setLocatorValue(String locatorValue) {
        this.locatorValue = locatorValue;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "LocatorSuggestion{" +
                "locatorType='" + locatorType + '\'' +
                ", locatorValue='" + locatorValue + '\'' +
                ", confidence=" + confidence +
                ", generatedLocator=" + generatedLocator +
            ", generationStrategy='" + generationStrategy + '\'' +
            ", generationConfidence=" + generationConfidence +
                '}';
    }
}