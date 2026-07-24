package com.vinayak.healing.context;

import com.vinayak.healing.model.ContextFeature;
import com.vinayak.healing.model.ElementFeature;
import com.vinayak.healing.model.LocatorInfo;
import com.vinayak.healing.model.VariableInfo;

public class ContextBuilder {

    public ContextFeature build(
            VariableInfo variableInfo,
            LocatorInfo locatorInfo,
            ElementFeature elementFeature) {

        ContextFeature context =
                new ContextFeature();

        context.setVariableInfo(variableInfo);

        context.setLocatorInfo(locatorInfo);

        context.setElementFeature(elementFeature);

        return context;
    }
}