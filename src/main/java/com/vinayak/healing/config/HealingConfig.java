package com.vinayak.healing.config;

public class HealingConfig {

    private boolean aiEnabled = true;

    private boolean cacheEnabled = true;

    private boolean reportEnabled = true;

    private String model = "llama3";

    private double confidenceThreshold = 70.0;

    private int waitTimeoutSeconds = 5;

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(
            double confidenceThreshold) {

        this.confidenceThreshold =
                confidenceThreshold;
    }

    public int getWaitTimeoutSeconds() {
    return waitTimeoutSeconds;
}

public void setWaitTimeoutSeconds(
        int waitTimeoutSeconds) {

    this.waitTimeoutSeconds =
            waitTimeoutSeconds;
}

    @Override
    public String toString() {

        return "HealingConfig{" +
                "aiEnabled=" + aiEnabled +
                ", cacheEnabled=" + cacheEnabled +
                ", reportEnabled=" + reportEnabled +
                ", model='" + model + '\'' +
                ", confidenceThreshold=" + confidenceThreshold +
", waitTimeoutSeconds=" + waitTimeoutSeconds +
'}';
    }
}