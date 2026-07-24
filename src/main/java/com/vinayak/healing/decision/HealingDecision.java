package com.vinayak.healing.decision;

import com.vinayak.healing.model.LocatorCandidate;

public class HealingDecision {

    private final LocatorCandidate candidate;
    private final HealingConfidence confidence;
    private final boolean healingAllowed;
    private final boolean cacheAllowed;
    private final String reason;

    public HealingDecision(
            LocatorCandidate candidate,
            HealingConfidence confidence,
            boolean healingAllowed,
            boolean cacheAllowed,
            String reason) {

        this.candidate = candidate;
        this.confidence = confidence;
        this.healingAllowed = healingAllowed;
        this.cacheAllowed = cacheAllowed;
        this.reason = reason;
    }

    public LocatorCandidate getCandidate() {
        return candidate;
    }

    public HealingConfidence getConfidence() {
        return confidence;
    }

    public boolean isHealingAllowed() {
        return healingAllowed;
    }

    public boolean isCacheAllowed() {
        return cacheAllowed;
    }

    public String getReason() {
        return reason;
    }
}