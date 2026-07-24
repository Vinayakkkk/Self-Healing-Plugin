package com.vinayak.healing.model;

public class ContextFeature {

    private VariableInfo variableInfo;
    private ElementFeature elementFeature;
    private LocatorInfo locatorInfo;


    public ElementFeature getElementFeature() {
    return elementFeature;
}

public void setElementFeature(ElementFeature elementFeature) {
    this.elementFeature = elementFeature;
}

    public VariableInfo getVariableInfo() {
        return variableInfo;
    }

    public void setVariableInfo(VariableInfo variableInfo) {
        this.variableInfo = variableInfo;
    }

    public LocatorInfo getLocatorInfo() {
        return locatorInfo;
    }

    public void setLocatorInfo(LocatorInfo locatorInfo) {
        this.locatorInfo = locatorInfo;
    }
}