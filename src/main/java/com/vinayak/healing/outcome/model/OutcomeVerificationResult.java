package com.vinayak.healing.outcome.model;

import java.util.ArrayList;
import java.util.List;

public class OutcomeVerificationResult {

    private boolean success;

    private double confidence;

    private final List<OutcomeSignal> passedSignals =
            new ArrayList<>();

    private final List<OutcomeSignal> failedSignals =
            new ArrayList<>();

    private final List<String> messages =
            new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<OutcomeSignal> getPassedSignals() {
        return passedSignals;
    }

    public List<OutcomeSignal> getFailedSignals() {
        return failedSignals;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addPassedSignal(
            OutcomeSignal signal) {

        if (signal != null) {
            passedSignals.add(signal);
        }
    }

    public void addFailedSignal(
            OutcomeSignal signal) {

        if (signal != null) {
            failedSignals.add(signal);
        }
    }

    public void addMessage(
            String message) {

        if (message != null
                && !message.isBlank()) {

            messages.add(message);
        }
    }

    @Override
    public String toString() {

        return "OutcomeVerificationResult{" +
                "success=" + success +
                ", confidence=" + confidence +
                ", passedSignals=" + passedSignals +
                ", failedSignals=" + failedSignals +
                ", messages=" + messages +
                '}';
    }
}