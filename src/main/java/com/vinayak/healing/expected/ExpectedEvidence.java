package com.vinayak.healing.expected;

public class ExpectedEvidence {

    /*
     * Which provider generated this evidence.
     */
    private EvidenceSource source;

    /*
     * What kind of evidence this is.
     */
    private ExpectedEvidenceType type;

    /*
     * Actual evidence value.
     *
     * Examples:
     * TEXT_INPUT
     * BUTTON
     * Dashboard
     * username
     * input
     */
    private String value;

    /*
     * Confidence score.
     */
    private double confidence;

    /*
     * Human-readable explanation.
     */
    private String description;

    /*
     * Optional supporting attribute.
     *
     * Examples:
     * placeholder
     * aria-label
     * class
     * id
     * variable
     * action
     */
    private String attribute;

    /*
     * Original raw value before normalization.
     *
     * Example:
     * postTextArea
     * sendKeys
     * oxd-buzz-post-input
     */
    private String rawValue;

    public ExpectedEvidence() {
    }

    public ExpectedEvidence(
            EvidenceSource source,
            ExpectedEvidenceType type,
            String value,
            double confidence,
            String description) {

        this.source = source;
        this.type = type;
        this.value = value;
        this.confidence = confidence;
        this.description = description;
    }

    public EvidenceSource getSource() {
        return source;
    }

    public void setSource(
            EvidenceSource source) {

        this.source = source;
    }

    public ExpectedEvidenceType getType() {
        return type;
    }

    public void setType(
            ExpectedEvidenceType type) {

        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(
            String value) {

        this.value = value;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(
            double confidence) {

        this.confidence = confidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(
            String attribute) {

        this.attribute = attribute;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(
            String rawValue) {

        this.rawValue = rawValue;
    }

    @Override
    public String toString() {

        return "ExpectedEvidence{" +
                "source=" + source +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", confidence=" + confidence +
                ", description='" + description + '\'' +
                ", attribute='" + attribute + '\'' +
                ", rawValue='" + rawValue + '\'' +
                '}';
    }
}