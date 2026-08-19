package com.vinayak.healing.learning;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import org.openqa.selenium.By;

/**
 * Converts a successful healing experience into a LearningRecord
 * and stores it through LearningEngine.
 *
 * This class is intentionally kept separate from SelfHealingEngine
 * so that learning concerns do not become part of the healing logic.
 */
public class LearningRecorder {

    private final LearningEngine learningEngine;

    public LearningRecorder() {

        this.learningEngine =
                new LearningEngine();
    }

    public LearningRecorder(
            LearningEngine learningEngine) {

        if (learningEngine == null) {
            throw new IllegalArgumentException(
                    "LearningEngine cannot be null.");
        }

        this.learningEngine =
                learningEngine;
    }

    /**
     * Records one healing experience.
     *
     * The outcome values are supplied by the caller.
     * This method does NOT assume that a healing was successful
     * merely because a locator was found.
     */
    public boolean record(
            FailureContext context,
            String pageObjectClass,
            LocatorCandidate candidate,
            By healedLocator,
            String confidenceLevel,
            boolean healingAllowed,
            boolean cacheAllowed,
            String healingSource,
            boolean outcomeSuccess,
            double outcomeConfidence) {

        if (context == null) {
            return false;
        }

        if (candidate == null) {
            return false;
        }

        if (healedLocator == null) {
            return false;
        }

        /*
 * ==========================================
 * LEARNING SAFETY GATE
 * ==========================================
 *
 * Only trusted healing outcomes become
 * long-term learning evidence.
 *
 * Collection-level medium confidence healing
 * is intentionally excluded because it is not
 * cacheable/reliable enough for future ranking.
 */
if (!outcomeSuccess) {

    System.out.println(
            "LEARNING SKIPPED : outcome was unsuccessful.");

    return false;
}

if (!healingAllowed) {

    System.out.println(
            "LEARNING SKIPPED : healing was not allowed.");

    return false;
}


        /*
         * ==========================================
         * LEARNING KEY
         * ==========================================
         */

        String expectedIntent =
                context.getExpectedIntent() == null
                        ? "UNKNOWN"
                        : context.getExpectedIntent().name();

        String action =
                context.getFailedAction() == null
                        ? "UNKNOWN"
                        : context.getFailedAction().name();

        LearningKey learningKey =
                new LearningKey(
                        pageObjectClass,
                        context.getVariableName(),
                        expectedIntent,
                        action,
                        context.getFailedLocator());

        /*
         * ==========================================
         * ACTUAL SELECTED LOCATOR
         * ==========================================
         */

        LocatorParts locatorParts =
                parseLocator(healedLocator);

        /*
         * ==========================================
         * LEARNING RECORD
         * ==========================================
         */

        LearningRecord record =
                new LearningRecord(
                        learningKey,
                        healedLocator.toString(),
                        locatorParts.type,
                        locatorParts.value,
                        candidate.getFinalScore(),
                        normalize(healingSource),
                        normalize(confidenceLevel),
                        healingAllowed,
                        cacheAllowed,
                        outcomeSuccess,
                        outcomeConfidence);

        /*
         * ==========================================
         * STORE
         * ==========================================
         */

        learningEngine.record(record);

        HealingAnalytics.learningRecorded();

        return true;
    }

    /**
     * Parses Selenium By.toString() into the framework's
     * canonical locator type and locator value.
     *
     * Selenium examples:
     *
     * By.id: username
     * By.name: username
     * By.cssSelector: [data-test='login']
     * By.xpath: //input[@name='username']
     * By.className: cart_item
     *
     * Framework canonical forms:
     *
     * id
     * name
     * css
     * xpath
     * class
     */
    private LocatorParts parseLocator(
            By locator) {

        String text =
                locator.toString();

        if (text == null
                || text.isBlank()) {

            return new LocatorParts(
                    "UNKNOWN",
                    "UNKNOWN");
        }

        int separator =
                text.indexOf(": ");

        if (separator < 0) {

            return new LocatorParts(
                    "UNKNOWN",
                    normalize(text));
        }

        String type =
                text.substring(
                        0,
                        separator);

        String value =
                text.substring(
                        separator + 2);

        /*
         * Remove Selenium's "By." prefix.
         */
        if (type.startsWith("By.")) {

            type =
                    type.substring(3);
        }

        /*
         * ==========================================
         * CANONICALIZE LOCATOR TYPE
         * ==========================================
         *
         * Selenium and the framework can use different
         * names for the same locator strategy.
         *
         * Keep one canonical representation inside
         * LearningRecord so CandidateRanker can compare
         * candidates consistently.
         */
        type =
                canonicalLocatorType(type);

        return new LocatorParts(
                normalize(type),
                normalize(value));
    }

    /**
     * Converts Selenium locator names into the framework's
     * canonical locator vocabulary.
     */
    private String canonicalLocatorType(
            String type) {

        if (type == null
                || type.isBlank()) {

            return "UNKNOWN";
        }

        switch (type.trim().toLowerCase()) {

            case "id":
                return "id";

            case "name":
                return "name";

            case "classname":
                return "class";

            case "cssselector":
                return "css";

            case "xpath":
                return "xpath";

            case "tagname":
                return "tag";

            case "linktext":
                return "linktext";

            case "partiallinktext":
                return "partiallinktext";

            default:
                /*
                 * Preserve already-canonical framework
                 * locator types and unknown custom types.
                 */
                return type.trim();
        }
    }

    private String normalize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "UNKNOWN";
        }

        return value.trim();
    }

    /**
     * Small immutable holder for parsed locator data.
     */
    private static final class LocatorParts {

        private final String type;
        private final String value;

        private LocatorParts(
                String type,
                String value) {

            this.type = type;
            this.value = value;
        }
    }
}