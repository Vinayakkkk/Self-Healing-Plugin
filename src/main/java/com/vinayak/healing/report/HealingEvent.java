package com.vinayak.healing.report;
import com.vinayak.healing.execution.ExecutionAction;
public class HealingEvent {

    private String pageObjectClass;
    private String variableName;
    private String action;
    private String expectedIntent;
    private String cacheKey;

    private String failedLocator;
    private String healedLocator;


    /*
     * Candidate ranking score.
     * Example: 1075.0
     */
    private double score;

    /*
     * Decision Engine classification.
     * HIGH / MEDIUM / LOW / REJECT
     */
    private String confidenceLevel;

    private boolean healingAllowed;
    private boolean cacheAllowed;

    private String timestamp;
    private String source;

    public HealingEvent() {
    }

    public HealingEvent(
            String pageObjectClass,
            String variableName,
             String action,
            String expectedIntent,
            String cacheKey,
            String failedLocator,
            String healedLocator,
            double score,
            String confidenceLevel,
            boolean healingAllowed,
            boolean cacheAllowed,
            String timestamp,
            String source) {

        this.pageObjectClass = pageObjectClass;
        this.variableName = variableName;
        this.action = action;
        this.expectedIntent = expectedIntent;
        this.cacheKey = cacheKey;
        this.failedLocator = failedLocator;
        this.healedLocator = healedLocator;
        this.score = score;
        this.confidenceLevel = confidenceLevel;
        this.healingAllowed = healingAllowed;
        this.cacheAllowed = cacheAllowed;
        this.timestamp = timestamp;
        this.source = source;
    }
    public String getPageObjectClass() {
        return pageObjectClass;
    }

    public void setPageObjectClass(
            String pageObjectClass) {

        this.pageObjectClass =
                pageObjectClass;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(
            String variableName) {

        this.variableName =
                variableName;
    }

    public String getAction() {
    return action;
}

public void setAction(String action) {
    this.action = action;
}

    public String getExpectedIntent() {
        return expectedIntent;
    }

    public void setExpectedIntent(
            String expectedIntent) {

        this.expectedIntent =
                expectedIntent;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(
            String cacheKey) {

        this.cacheKey =
                cacheKey;
    }

    public String getFailedLocator() {
        return failedLocator;
    }

    public void setFailedLocator(
            String failedLocator) {

        this.failedLocator =
                failedLocator;
    }

    public String getHealedLocator() {
        return healedLocator;
    }

    public void setHealedLocator(
            String healedLocator) {

        this.healedLocator =
                healedLocator;
    }

    public double getScore() {
        return score;
    }

    public void setScore(
            double score) {

        this.score =
                score;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(
            String confidenceLevel) {

        this.confidenceLevel =
                confidenceLevel;
    }

    public boolean isHealingAllowed() {
        return healingAllowed;
    }

    public void setHealingAllowed(
            boolean healingAllowed) {

        this.healingAllowed =
                healingAllowed;
    }

    public boolean isCacheAllowed() {
        return cacheAllowed;
    }

    public void setCacheAllowed(
            boolean cacheAllowed) {

        this.cacheAllowed =
                cacheAllowed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(
            String timestamp) {

        this.timestamp =
                timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(
            String source) {

        this.source =
                source;
    }

    @Override
    public String toString() {

        return "HealingEvent{"
                + "pageObjectClass='" + pageObjectClass + '\''
                + ", variableName='" + variableName + '\''
                + ", action='" + action + '\''
                + ", expectedIntent='" + expectedIntent + '\''
                + ", cacheKey='" + cacheKey + '\''
                + ", failedLocator='" + failedLocator + '\''
                + ", healedLocator='" + healedLocator + '\''
                + ", score=" + score
                + ", confidenceLevel='" + confidenceLevel + '\''
                + ", healingAllowed=" + healingAllowed
                + ", cacheAllowed=" + cacheAllowed
                + ", timestamp='" + timestamp + '\''
                + ", source='" + source + '\''
                + '}';
    }
}