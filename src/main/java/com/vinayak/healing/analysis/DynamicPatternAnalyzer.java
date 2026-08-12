package com.vinayak.healing.analysis;


import com.vinayak.healing.dynamic.DynamicAttributeDetector;
import com.vinayak.healing.dynamic.DynamicAttributeResult;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class DynamicPatternAnalyzer {

 private final DynamicAttributeDetector detector =
        new DynamicAttributeDetector();

    public double calculateDynamicScore(
            FailureContext context,
            LocatorCandidate candidate) {

        if (context == null || candidate == null) {
            return 0;
        }

        double score = 0;

        score += scoreLocatorValue(context, candidate);
        score += scoreId(context, candidate);
        score += scoreName(context, candidate);

        return score;
    }

    private double scoreLocatorValue(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getLocatorValue(),
                300);
    }

    private double scoreId(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getId(),
                250);
    }

    private double scoreName(
            FailureContext context,
            LocatorCandidate candidate) {

        return scorePattern(
                context.getFailedLocator(),
                candidate.getName(),
                220);
    }

private double scorePattern(
        String failedLocator,
        String candidateValue,
        double score) {

    if (failedLocator == null || candidateValue == null) {
        return 0;
    }

    failedLocator = extractLocatorValue(failedLocator);

    DynamicAttributeResult failed =
            detector.analyze("locator", failedLocator);

    DynamicAttributeResult candidate =
            detector.analyze("locator", candidateValue);

    /*
     * Exact match doesn't need dynamic bonus.
     */
    if (normalize(failed.getOriginalValue())
            .equals(normalize(candidate.getOriginalValue()))) {

        return 0;
    }

    /*
     * Compare normalized dynamic values.
     */
 if (failed.getPatternType() != null
        && failed.getPatternType() != com.vinayak.healing.dynamic.DynamicPatternType.NONE
        && failed.getPatternType() == candidate.getPatternType()
        && normalize(failed.getNormalizedValue())
                .equals(normalize(candidate.getNormalizedValue()))) {

    return score;
}

    return 0;
}



    private String extractLocatorValue(
            String locator) {

        int index = locator.lastIndexOf(":");

        if (index != -1) {
            locator = locator.substring(index + 1);
        }

        return locator.trim();
    }
private String normalize(String value) {

    if (value == null) {
        return "";
    }

    return value
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "");
}
}