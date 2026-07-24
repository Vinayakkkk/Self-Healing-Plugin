package com.vinayak.healing.model;

import com.vinayak.healing.intent.ElementIntent;

import java.util.ArrayList;
import java.util.List;

public class VariableInfo {

    private String variableName;

    private List<String> tokens =
            new ArrayList<>();

    private ElementIntent expectedIntent;

    private String expectedTag;

    private double confidence;

    private List<String> synonyms =
            new ArrayList<>();

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public ElementIntent getExpectedIntent() {
        return expectedIntent;
    }

    public void setExpectedIntent(ElementIntent expectedIntent) {
        this.expectedIntent = expectedIntent;
    }

    public String getExpectedTag() {
        return expectedTag;
    }

    public void setExpectedTag(String expectedTag) {
        this.expectedTag = expectedTag;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    public String toString() {

        return "VariableInfo{" +
                "variableName='" + variableName + '\'' +
                ", tokens=" + tokens +
                ", expectedIntent=" + expectedIntent +
                ", expectedTag='" + expectedTag + '\'' +
                ", confidence=" + confidence +
                ", synonyms=" + synonyms +
                '}';
    }
}