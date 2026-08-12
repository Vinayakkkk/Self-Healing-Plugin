package com.vinayak.healing.decision;

public class SemanticEvidence {

    private boolean variableMatched;

    private boolean locatorMatched;

    private boolean labelMatched;

    private boolean idMatched;

    private boolean nameMatched;

    private boolean tagMatched;

    private boolean intentMatched;

    private int signalCount;

    public boolean isVariableMatched() {
        return variableMatched;
    }

    public void setVariableMatched(
            boolean variableMatched) {

        this.variableMatched = variableMatched;
    }

    public boolean isLocatorMatched() {
        return locatorMatched;
    }

    public void setLocatorMatched(
            boolean locatorMatched) {

        this.locatorMatched = locatorMatched;
    }

    public boolean isLabelMatched() {
        return labelMatched;
    }

    public void setLabelMatched(
            boolean labelMatched) {

        this.labelMatched = labelMatched;
    }

    public boolean isIdMatched() {
        return idMatched;
    }

    public void setIdMatched(
            boolean idMatched) {

        this.idMatched = idMatched;
    }

    public boolean isNameMatched() {
        return nameMatched;
    }

    public void setNameMatched(
            boolean nameMatched) {

        this.nameMatched = nameMatched;
    }

    public boolean isTagMatched() {
        return tagMatched;
    }

    public void setTagMatched(
            boolean tagMatched) {

        this.tagMatched = tagMatched;
    }

    public boolean isIntentMatched() {
        return intentMatched;
    }

    public void setIntentMatched(
            boolean intentMatched) {

        this.intentMatched = intentMatched;
    }

    public int getSignalCount() {
        return signalCount;
    }

    public void incrementSignal() {
        signalCount++;
    }

    @Override
    public String toString() {

        return "SemanticEvidence{" +
                "variableMatched=" + variableMatched +
                ", locatorMatched=" + locatorMatched +
                ", labelMatched=" + labelMatched +
                ", idMatched=" + idMatched +
                ", nameMatched=" + nameMatched +
                ", tagMatched=" + tagMatched +
                ", intentMatched=" + intentMatched +
                ", signalCount=" + signalCount +
                '}';
    }
}