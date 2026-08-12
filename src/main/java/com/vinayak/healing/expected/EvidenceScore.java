package com.vinayak.healing.expected;

import java.util.ArrayList;
import java.util.List;

public class EvidenceScore {

    /*
     * Common value shared by multiple evidence sources.
     *
     * Example:
     * Dashboard
     * Username
     * TEXT_INPUT
     */
    private String value;

    private ExpectedEvidenceType type;

    /*
     * Sum of all confidence scores.
     */
    private double totalScore;

    /*
     * All evidence supporting this value.
     */
    private final List<ExpectedEvidence> supportingEvidence =
            new ArrayList<>();

    public EvidenceScore() {
    }

    public EvidenceScore(
        ExpectedEvidenceType type,
        String value) {

    this.type = type;
    this.value = value;
}

    public String getValue() {
        return value;
    }

    public void setValue(
            String value) {

        this.value = value;
    }

    public ExpectedEvidenceType getType() {
        return type;
    }

    public void setType(
            ExpectedEvidenceType type) {

        this.type = type;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(
            double totalScore) {

        this.totalScore = totalScore;
    }

    public List<ExpectedEvidence> getSupportingEvidence() {
        return supportingEvidence;
    }

    /**
     * Adds one piece of evidence and
     * automatically updates the score.
     */
    public void addEvidence(
            ExpectedEvidence evidence) {

        if (evidence == null) {
            return;
        }

        supportingEvidence.add(evidence);

        totalScore += evidence.getConfidence();
    }

    /**
     * Number of evidence sources.
     */
    public int getEvidenceCount() {

        return supportingEvidence.size();
    }

    @Override
    public String toString() {

        return "\nEvidenceScore{" +
                "\nvalue='" + value + '\'' +
                ",\ntotalScore=" + totalScore +
                ",\nevidenceCount=" + getEvidenceCount() +
                ",\nsupportingEvidence=" + supportingEvidence +
                "\n}";
    }
}