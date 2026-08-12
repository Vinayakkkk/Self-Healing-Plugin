package com.vinayak.healing.expected;

import java.util.ArrayList;
import java.util.List;
import com.vinayak.healing.intent.ElementIntent;

public class ExpectedContext {

    private String expectedText;

    private String expectedTag;

    private ElementIntent expectedIntent;

    private String expectedLabel;

  private ExpectedRole expectedRole;

    private String expectedParent;

    private String expectedPage;

    private double confidence;

    private final List<ExpectedEvidence> evidences =
            new ArrayList<>();

            private final List<EvidenceScore> evidenceScores =
        new ArrayList<>();

            private final List<ExpectedEvidence> acceptedEvidence =
        new ArrayList<>();

        public List<EvidenceScore> getEvidenceScores() {
    return evidenceScores;
}

public void addEvidenceScore(
        EvidenceScore score) {

    if (score != null) {
        evidenceScores.add(score);
    }
}

private final List<ExpectedEvidence> rejectedEvidence =
        new ArrayList<>();

        public List<ExpectedEvidence> getAcceptedEvidence() {

    return acceptedEvidence;
}

public List<ExpectedEvidence> getRejectedEvidence() {

    return rejectedEvidence;
}

    public String getExpectedText() {
        return expectedText;
    }

    public void setExpectedText(String expectedText) {
        this.expectedText = expectedText;
    }

    public String getExpectedTag() {
        return expectedTag;
    }

    public void setExpectedTag(String expectedTag) {
        this.expectedTag = expectedTag;
    }

    public ElementIntent getExpectedIntent() {
        return expectedIntent;
    }

    public void setExpectedIntent(
            ElementIntent expectedIntent) {

        this.expectedIntent = expectedIntent;
    }

    public String getExpectedLabel() {
        return expectedLabel;
    }

    public void setExpectedLabel(
            String expectedLabel) {

        this.expectedLabel = expectedLabel;
    }

    public ExpectedRole getExpectedRole() {
        return expectedRole;
    }

    public void setExpectedRole(
        ExpectedRole expectedRole) {

        this.expectedRole = expectedRole;
    }

    public String getExpectedParent() {
        return expectedParent;
    }

    public void setExpectedParent(
            String expectedParent) {

        this.expectedParent = expectedParent;
    }

    public String getExpectedPage() {
    return expectedPage;
}

public void setExpectedPage(
        String expectedPage) {

    this.expectedPage = expectedPage;
}

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(
            double confidence) {

        this.confidence = confidence;
    }

    public List<ExpectedEvidence> getEvidences() {
        return evidences;
    }

    public void addEvidence(
            ExpectedEvidence evidence) {

        if (evidence != null) {
            evidences.add(evidence);
        }
    }

    public void addAcceptedEvidence(
        ExpectedEvidence evidence) {

    if (evidence != null) {
        acceptedEvidence.add(evidence);
    }
}

public void addRejectedEvidence(
        ExpectedEvidence evidence) {

    if (evidence != null) {
        rejectedEvidence.add(evidence);
    }
}

    @Override
    public String toString() {

        return "ExpectedContext{" +
                "expectedText='" + expectedText + '\'' +
                ", expectedTag='" + expectedTag + '\'' +
                ", expectedIntent=" + expectedIntent +
                ", expectedLabel='" + expectedLabel + '\'' +
                ", expectedRole='" + expectedRole + '\'' +
                ", expectedParent='" + expectedParent + '\'' +
                ", expectedPage='" + expectedPage + '\'' +
                ", confidence=" + confidence +
                ", evidenceScores=" + evidenceScores +
                ", evidences=" + evidences +
                ", acceptedEvidence=" + acceptedEvidence
+ ", rejectedEvidence=" + rejectedEvidence+
                '}';
    }
}