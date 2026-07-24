package com.vinayak.healing.engine;

import org.openqa.selenium.By;

import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.LocatorCandidate;

public final class CandidateConverter {

    private CandidateConverter() {
        // Utility class
    }

    /**
     * Converts a validated LocatorCandidate into a LocatorSuggestion.
     * Used for:
     *  - Cache
     *  - Runtime healing
     */
    public static LocatorSuggestion convert(
            LocatorCandidate candidate) {

        if (candidate == null) {
            return null;
        }

        LocatorSuggestion suggestion =
                new LocatorSuggestion();

        String locatorType =
                normalizeLocatorType(
                        candidate.getLocatorType());

        suggestion.setLocatorType(locatorType);

        switch (locatorType) {

            case "id":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
                break;

            case "name":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
                break;

            case "class":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
                break;

            case "css":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
                break;

            case "xpath":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
                break;

            case "placeholder":

                suggestion.setLocatorType("css");

                suggestion.setLocatorValue(
                        "[placeholder='"
                                + candidate.getLocatorValue()
                                + "']");
                break;

            case "text":

                suggestion.setLocatorType("xpath");

                suggestion.setLocatorValue(
                        "//*[normalize-space()='"
                                + candidate.getLocatorValue()
                                + "']");
                break;

            case "data-test":

            case "data-testid":

            case "data-qa":

            case "data-cy":

            case "aria-label":

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());

                break;

            default:

                suggestion.setLocatorValue(
                        candidate.getLocatorValue());
        }

        suggestion.setConfidence(
                candidate.getFinalScore());

        return suggestion;
    }

    /**
     * Converts an AI suggestion into a LocatorCandidate.
     * CandidateValidator will verify whether it is actually correct.
     */
    public static LocatorCandidate convert(
            LocatorSuggestion suggestion) {

        if (suggestion == null) {
            return null;
        }

        String locatorType =
                normalizeLocatorType(
                        suggestion.getLocatorType());

        LocatorCandidate candidate =
                new LocatorCandidate(

                        locatorType,

                        suggestion.getLocatorValue(),

                        null,           // tag

                        null,           // input type

                        ElementIntent.UNKNOWN,

                        suggestion.getConfidence(),

                        null,           // parent tag

                        null,           // parent class

                        null            // parent id
                );

        candidate.setFinalScore(
                suggestion.getConfidence());

        return candidate;
    }

    /**
 * Converts the final optimized Selenium By locator
 * into a LocatorSuggestion.
 *
 * This ensures the persistent cache stores the locator
 * actually used at runtime after LocatorOptimizer.
 */
public static LocatorSuggestion convert(
        By locator,
        double confidence) {

    if (locator == null) {
        return null;
    }

    String rawLocator =
            locator.toString();

    String locatorType;
    String locatorValue;

    if (rawLocator.startsWith("By.id: ")) {

        locatorType = "id";
        locatorValue =
                rawLocator.substring(
                        "By.id: ".length());

    } else if (rawLocator.startsWith("By.name: ")) {

        locatorType = "name";
        locatorValue =
                rawLocator.substring(
                        "By.name: ".length());

    } else if (rawLocator.startsWith(
            "By.className: ")) {

        locatorType = "class";
        locatorValue =
                rawLocator.substring(
                        "By.className: ".length());

    } else if (rawLocator.startsWith(
            "By.cssSelector: ")) {

        locatorType = "css";
        locatorValue =
                rawLocator.substring(
                        "By.cssSelector: ".length());

    } else if (rawLocator.startsWith(
            "By.xpath: ")) {

        locatorType = "xpath";
        locatorValue =
                rawLocator.substring(
                        "By.xpath: ".length());

    } else if (rawLocator.startsWith(
            "By.linkText: ")) {

        locatorType = "linkText";
        locatorValue =
                rawLocator.substring(
                        "By.linkText: ".length());

    } else if (rawLocator.startsWith(
            "By.partialLinkText: ")) {

        locatorType = "partialLinkText";
        locatorValue =
                rawLocator.substring(
                        "By.partialLinkText: ".length());

    } else if (rawLocator.startsWith(
            "By.tagName: ")) {

        locatorType = "tag";
        locatorValue =
                rawLocator.substring(
                        "By.tagName: ".length());

    } else {

        System.out.println(
                "Unable to convert optimized locator : "
                        + rawLocator);

        return null;
    }

    LocatorSuggestion suggestion =
            new LocatorSuggestion();

    suggestion.setLocatorType(
            locatorType);

    suggestion.setLocatorValue(
            locatorValue);

    suggestion.setConfidence(
            confidence);

    return suggestion;
}

    /**
     * Normalizes locator types coming from
     * AI / Cache / Candidate list.
     */
    private static String normalizeLocatorType(
            String locatorType) {

        if (locatorType == null) {
            return "";
        }

        locatorType =
                locatorType
                        .trim()
                        .toLowerCase();

        switch (locatorType) {

            case "cssselector":
                return "css";

            case "classname":
                return "class";

            case "by.id":
                return "id";

            case "by.name":
                return "name";

            case "by.class":
            case "by.classname":
                return "class";

            case "by.cssselector":
                return "css";

            case "by.xpath":
                return "xpath";

            default:
                return locatorType;
        }
    }

}