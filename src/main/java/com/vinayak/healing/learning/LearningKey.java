package com.vinayak.healing.learning;

import java.util.Objects;

public final class LearningKey {

    private final String pageObjectClass;
    private final String variableName;
    private final String expectedIntent;
    private final String action;
    private final String failedLocator;

    public LearningKey(
            String pageObjectClass,
            String variableName,
            String expectedIntent,
            String action,
            String failedLocator) {

        this.pageObjectClass =
                normalize(pageObjectClass);

        this.variableName =
                normalize(variableName);

        this.expectedIntent =
                normalize(expectedIntent);

        this.action =
                normalize(action);

        this.failedLocator =
                normalize(failedLocator);
    }

    public String getPageObjectClass() {
        return pageObjectClass;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getExpectedIntent() {
        return expectedIntent;
    }

    public String getAction() {
        return action;
    }

    public String getFailedLocator() {
        return failedLocator;
    }

    private static String normalize(String value) {

        if (value == null
                || value.isBlank()) {

            return "UNKNOWN";
        }

        return value.trim();
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof LearningKey)) {
            return false;
        }

        LearningKey other =
                (LearningKey) object;

        return pageObjectClass.equalsIgnoreCase(
                other.pageObjectClass)

                && variableName.equalsIgnoreCase(
                other.variableName)

                && expectedIntent.equalsIgnoreCase(
                other.expectedIntent)

                && action.equalsIgnoreCase(
                other.action)

                && failedLocator.equalsIgnoreCase(
                other.failedLocator);
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                pageObjectClass.toLowerCase(),
                variableName.toLowerCase(),
                expectedIntent.toLowerCase(),
                action.toLowerCase(),
                failedLocator.toLowerCase());
    }

    @Override
    public String toString() {

        return "LearningKey{" +
                "pageObjectClass='" +
                pageObjectClass + '\'' +
                ", variableName='" +
                variableName + '\'' +
                ", expectedIntent='" +
                expectedIntent + '\'' +
                ", action='" +
                action + '\'' +
                ", failedLocator='" +
                failedLocator + '\'' +
                '}';
    }
}