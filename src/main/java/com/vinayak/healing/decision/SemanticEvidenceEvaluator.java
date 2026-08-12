package com.vinayak.healing.decision;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class SemanticEvidenceEvaluator {

    public SemanticEvidence evaluate(
            LocatorCandidate candidate,
            FailureContext context) {

        SemanticEvidence evidence =
                new SemanticEvidence();

        if (candidate == null || context == null) {
            return evidence;
        }

        /*
         * Variable
         */
        if (matches(context.getVariableName(),
                candidate.getLocatorValue())
                || matches(context.getVariableName(),
                candidate.getElementText())
                || matches(context.getVariableName(),
                candidate.getNearestLabel())
                || matches(context.getVariableName(),
                candidate.getId())
                || matches(context.getVariableName(),
                candidate.getName())) {

            evidence.setVariableMatched(true);
            evidence.incrementSignal();
        }

        /*
         * Broken locator text
         */
        /*
 * Broken locator text
 *
 * Example:
 *
 * Failed locator:
 *     By.className: shopping
 *
 * Candidate:
 *     class=shopping_cart_badge
 *
 * "shopping" is meaningful identity evidence.
 */
if (matches(
        context.getLocatorTextHint(),
        candidate.getLocatorValue())) {

    evidence.setLocatorMatched(true);
    evidence.incrementSignal();
}

/*
 * Failed locator itself
 *
 * Example:
 *
 * By.className: shopping
 *
 * must be compared using its actual semantic
 * locator value, not the complete "By.className:"
 * string.
 */
String failedLocatorValue =
        extractLocatorValue(
                context.getFailedLocator());

if (matches(
        failedLocatorValue,
        candidate.getLocatorValue())) {

    evidence.setLocatorMatched(true);
    evidence.incrementSignal();
}

        /*
         * Expected Label
         */
        if (matches(context.getExpectedLabel(),
                candidate.getNearestLabel())) {

            evidence.setLabelMatched(true);
            evidence.incrementSignal();
        }

        /*
         * ID
         */
        if (matches(context.getVariableName(),
                candidate.getId())) {

            evidence.setIdMatched(true);
            evidence.incrementSignal();
        }

        /*
         * Name
         */
        if (matches(context.getVariableName(),
                candidate.getName())) {

            evidence.setNameMatched(true);
            evidence.incrementSignal();
        }

        /*
         * Tag
         */
        if (context.getExpectedTag() != null
                && candidate.getTagName() != null
                && context.getExpectedTag()
                        .equalsIgnoreCase(
                                candidate.getTagName())) {

            evidence.setTagMatched(true);
            evidence.incrementSignal();
        }

        /*
         * Intent
         */
        if (context.getExpectedIntent() != null
                && context.getExpectedIntent()
                != ElementIntent.UNKNOWN
                && candidate.getIntent() != null
                && context.getExpectedIntent()
                == candidate.getIntent()) {

            evidence.setIntentMatched(true);
            evidence.incrementSignal();
        }

        return evidence;
    }

    private String extractLocatorValue(
        String failedLocator) {

    if (!hasText(failedLocator)) {
        return "";
    }

    String value =
            failedLocator.trim();

    /*
     * Selenium format:
     *
     * By.className: shopping
     * By.id: username
     * By.name: password
     */
    java.util.regex.Matcher matcher =
            java.util.regex.Pattern
                    .compile(
                            "(?i)^By\\.[a-zA-Z]+\\s*:\\s*(.+)$")
                    .matcher(value);

    if (matcher.find()) {

        return matcher.group(1)
                .trim()
                .replaceAll(
                        "^['\"]|['\"]$",
                        "");
    }

    return value;
}

    private boolean matches(
            String left,
            String right) {

        if (!hasText(left)
                || !hasText(right)) {

            return false;
        }

        left = normalize(left);
        right = normalize(right);

        return left.equals(right)
                || left.contains(right)
                || right.contains(left);
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }

    private String normalize(
            String value) {

        return value
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[^a-zA-Z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}