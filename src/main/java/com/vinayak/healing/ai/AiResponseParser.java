package com.vinayak.healing.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AiResponseParser {

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    public LocatorSuggestion parse(
            String ollamaResponse)
            throws Exception {

        if (ollamaResponse == null
                || ollamaResponse.isBlank()) {

            return null;
        }

        JsonNode root =
                MAPPER.readTree(ollamaResponse);

        if (root == null) {

            return null;
        }

        if (!root.has("response")) {

            System.out.println(
                    "AI response does not contain 'response' field.");

            return null;
        }

        String response =
                root.get("response").asText();

        if (response == null
                || response.isBlank()) {

            return null;
        }

        int start =
                response.indexOf("{");

        int end =
                response.lastIndexOf("}");

        if (start == -1
                || end == -1
                || start >= end) {

            System.out.println(
                    "Unable to locate JSON in AI response.");

            return null;
        }

        String json =
                response.substring(
                        start,
                        end + 1);

        LocatorSuggestion suggestion;

        try {

            suggestion =
                    MAPPER.readValue(
                            json,
                            LocatorSuggestion.class);

        } catch (Exception e) {

            System.out.println(
                    "Unable to parse AI JSON.");

            return null;
        }

        if (suggestion == null) {

            return null;
        }

        // ----------------------------------
        // Validate locatorType
        // ----------------------------------

        if (suggestion.getLocatorType() == null
                || suggestion.getLocatorType().isBlank()) {

            System.out.println(
                    "AI returned empty locator type.");

            return null;
        }

        // ----------------------------------
        // Validate locatorValue
        // ----------------------------------

        if (suggestion.getLocatorValue() == null
                || suggestion.getLocatorValue().isBlank()) {

            System.out.println(
                    "AI returned empty locator value.");

            return null;
        }

        // ----------------------------------
        // Normalize locatorType
        // ----------------------------------

        String locatorType =
                suggestion.getLocatorType()
                        .trim()
                        .toLowerCase();

        locatorType =
                locatorType
                        .replace("by.", "")
                        .replace("cssselector", "css")
                        .replace("classname", "class")
                        .replace("linktext", "linkText")
                        .replace("partiallinktext", "partialLinkText");

        switch (locatorType) {

            case "id":
            case "name":
            case "class":
            case "css":
            case "xpath":
            case "tagname":
            case "linktext":
            case "partiallinktext":
            case "data-test":
            case "data-testid":
            case "data-qa":
            case "data-cy":
            case "aria-label":
            case "placeholder":
            case "text":
                break;

            default:

                System.out.println(
                        "Unsupported locator type : "
                                + locatorType);

                return null;
        }

        suggestion.setLocatorType(locatorType);

        // ----------------------------------
        // Normalize locator value
        // ----------------------------------

        suggestion.setLocatorValue(
                suggestion.getLocatorValue().trim());

        // ----------------------------------
        // Validate confidence
        // ----------------------------------

        double confidence =
                suggestion.getConfidence();

        if (confidence < 0) {

            confidence = 0;
        }

        if (confidence > 1000) {

            confidence = 1000;
        }

        suggestion.setConfidence(confidence);

        System.out.println(
                "AI Suggestion Parsed Successfully");

        System.out.println(
                "Locator Type : "
                        + suggestion.getLocatorType());

        System.out.println(
                "Locator Value : "
                        + suggestion.getLocatorValue());

        System.out.println(
                "Confidence : "
                        + suggestion.getConfidence());

        return suggestion;
    }

public Integer parseCandidateIndex(
        String aiResponse) {

    if (aiResponse == null
            || aiResponse.isBlank()) {
        return null;
    }

    try {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(
                        "\\\\\"candidateIndex\\\\\"\\s*:\\s*(\\d+)")
                        .matcher(aiResponse);

        if (matcher.find()) {
            return Integer.parseInt(
                    matcher.group(1));
        }

        matcher =
                java.util.regex.Pattern.compile(
                        "\"candidateIndex\"\\s*:\\s*(\\d+)")
                        .matcher(aiResponse);

        if (matcher.find()) {
            return Integer.parseInt(
                    matcher.group(1));
        }

    } catch (Exception e) {
        System.out.println(
                "CANDIDATE INDEX PARSE FAILED : "
                        + e.getMessage());
    }

    return null;
}

}