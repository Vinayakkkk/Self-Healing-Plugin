package com.vinayak.healing.dom;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XPathFallbackGenerator {

    public List<LocatorCandidate> generate(
            String html,
            FailureContext context) {

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        if (html == null
                || html.isBlank()
                || context == null) {

            return candidates;
        }

        String variableName =
                context.getVariableName();

        if (variableName == null
                || variableName.isBlank()) {

            return candidates;
        }

        Document document =
                Jsoup.parse(html);

        // This fallback is for fields whose related label
        // identifies the correct control.
        Elements labels =
                document.select("label");

        for (Element label : labels) {

            String labelText =
                    label.text().trim();

            if (labelText.isBlank()) {
                continue;
            }

            if (!labelMatchesVariable(
                    labelText,
                    variableName)) {

                continue;
            }

            Element control =
                    findRelatedControl(label);

            if (control == null) {
                continue;
            }

            if (!isExpectedControl(
                    control,
                    context)) {

                continue;
            }

            String xpath =
                    buildLabelControlXpath(
                            labelText,
                            control.tagName());

            if (xpath.isBlank()) {
                continue;
            }

            if (alreadyExists(
                    candidates,
                    xpath)) {

                continue;
            }

            LocatorCandidate candidate =
                    new LocatorCandidate(
                            "label-input",
                            xpath,
                            control.tagName(),
                            control.attr("type"),
                            ElementIntent.INPUT,
                            0.0,
                            getParentTag(control),
                            getParentClass(control),
                            getParentId(control));

                            candidate.setNearestLabel(
        labelText);

            candidate.setFinalScore(
                    2000.0);

            candidates.add(candidate);

            System.out.println(
                    "XPATH FALLBACK CREATED");

            System.out.println(
                    "MATCHED LABEL = "
                            + labelText);

            System.out.println(
                    "RELATED CONTROL = "
                            + control.tagName());

            System.out.println(
                    "XPATH = "
                            + xpath);
        }

        return candidates;
    }

private boolean labelMatchesVariable(
        String labelText,
        String variableName) {

    Set<String> labelTokens =
            tokens(labelText);

    Set<String> variableTokens =
            tokens(variableName);

    if (labelTokens.isEmpty()
            || variableTokens.isEmpty()) {
        return false;
    }

    for (String variableToken : variableTokens) {

        if (!labelTokens.contains(variableToken)) {
            return false;
        }
    }

    return true;
}

    private Set<String> tokens(
            String value) {

        Set<String> result =
                new HashSet<>();

        if (value == null) {
            return result;
        }

        String normalized =
                value.replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2");

        String[] parts =
                normalized
                        .toLowerCase()
                        .split("[^a-z0-9]+");

        for (String part : parts) {

            if (part.length() >= 3) {
                result.add(part);
            }
        }

        return result;
    }

    private Element findRelatedControl(
            Element label) {

        // Case 1: <label for="employee"> ...
        String forValue =
                label.attr("for").trim();

        if (!forValue.isBlank()) {

            Document document =
                    label.ownerDocument();

            if (document != null) {

                Element byId =
                        document.getElementById(
                                forValue);

                if (isControl(byId)) {
                    return byId;
                }
            }
        }

        // Case 2: control is inside label
        Element insideLabel =
                label.selectFirst(
                        "input, textarea, select");

        if (insideLabel != null) {
            return insideLabel;
        }

        // Case 3: nearest ancestor that contains
        // this label and a form control.
        Element current =
                label.parent();

        for (int i = 0;
                current != null && i < 6;
                i++) {

            Element control =
                    current.selectFirst(
                            "input, textarea, select");

            if (control != null) {
                return control;
            }

            current = current.parent();
        }

        return null;
    }

    private boolean isExpectedControl(
            Element control,
            FailureContext context) {

        if (control == null) {
            return false;
        }

        String tag =
                control.tagName()
                        .toLowerCase();

        // If context knows expected tag, respect it.
        String expectedTag =
                context.getExpectedTag();

        if (expectedTag != null
                && !expectedTag.isBlank()) {

            return expectedTag.equalsIgnoreCase(tag);
        }

        // For label-related field fallback,
        // only editable form controls are valid.
        return tag.equals("input")
                || tag.equals("textarea")
                || tag.equals("select");
    }

    private boolean isControl(
            Element element) {

        if (element == null) {
            return false;
        }

        String tag =
                element.tagName()
                        .toLowerCase();

        return tag.equals("input")
                || tag.equals("textarea")
                || tag.equals("select");
    }

    private String buildLabelControlXpath(
            String labelText,
            String controlTag) {

        return "//label[normalize-space()="
                + xpathLiteral(labelText)
                + "]"
                + "/ancestor::*[.//"
                + controlTag
                + "][1]//"
                + controlTag;
    }

    private String xpathLiteral(
            String value) {

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts =
                value.split("'");

        StringBuilder result =
                new StringBuilder("concat(");

        for (int i = 0;
                i < parts.length;
                i++) {

            if (i > 0) {
                result.append(", \"'\", ");
            }

            result.append("'")
                    .append(parts[i])
                    .append("'");
        }

        result.append(")");

        return result.toString();
    }

    private boolean alreadyExists(
            List<LocatorCandidate> candidates,
            String xpath) {

        for (LocatorCandidate candidate : candidates) {

            if (xpath.equals(
                    candidate.getLocatorValue())) {

                return true;
            }
        }

        return false;
    }

    private String getParentTag(
            Element element) {

        return element.parent() == null
                ? ""
                : element.parent().tagName();
    }

    private String getParentClass(
            Element element) {

        return element.parent() == null
                ? ""
                : element.parent().className();
    }

    private String getParentId(
            Element element) {

        return element.parent() == null
                ? ""
                : element.parent().id();
    }
}