package com.vinayak.healing.execution;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeContext {

    private String pageObjectClass;

    private String methodName;

    private final Map<String, Object> arguments =
            new LinkedHashMap<>();

    private final Map<String, Object> variables =
            new LinkedHashMap<>();

    public String getPageObjectClass() {
        return pageObjectClass;
    }

    public void setPageObjectClass(
            String pageObjectClass) {

        this.pageObjectClass =
                pageObjectClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(
            String methodName) {

        this.methodName =
                methodName;
    }

    public void putArgument(
            String name,
            Object value) {

        if (name == null
                || name.isBlank()) {

            return;
        }

        arguments.put(
                name,
                value);
    }

    public void putVariable(
            String name,
            Object value) {

        if (name == null
                || name.isBlank()) {

            return;
        }

        variables.put(
                name,
                value);
    }

    public Map<String, Object> getArguments() {

        return Collections.unmodifiableMap(
                arguments);
    }

    public Map<String, Object> getVariables() {

        return Collections.unmodifiableMap(
                variables);
    }

    public void clear() {

        pageObjectClass = null;

        methodName = null;

        arguments.clear();

        variables.clear();
    }

    @Override
    public String toString() {

        return "RuntimeContext{" +
                "pageObjectClass='" + pageObjectClass + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + arguments +
                ", variables=" + variables +
                '}';
    }
}