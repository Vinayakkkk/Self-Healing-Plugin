package com.vinayak.healing.generator;

import com.vinayak.healing.dynamic.DynamicAttributeDetector;
import com.vinayak.healing.dynamic.DynamicAttributeResult;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

import java.util.ArrayList;
import java.util.List;

public class ContextAwareLocatorGenerator {

        private final DynamicAttributeDetector detector =
        new DynamicAttributeDetector();

public List<LocatorCandidate> generate(
        FailureContext context,
        LocatorCandidate candidate) {

    List<LocatorCandidate> generated =
            new ArrayList<>();

    if (candidate == null) {
        return generated;
    }

    generateIdLocator(
            candidate,
            generated);

    generateNameLocator(
            candidate,
            generated);

    generateLabelXpath(
            context,
            candidate,
            generated);

    return generated;
}

    /*
     * -------------------------------------------------------
     * ID Generator
     * -------------------------------------------------------
     */

    private void generateIdLocator(
            LocatorCandidate candidate,
            List<LocatorCandidate> generated) {

        if ("id".equalsIgnoreCase(
                candidate.getLocatorType())) {

            return;
        }

        if (!hasText(candidate.getId())) {
            return;
        }

        LocatorCandidate generatedCandidate =
                new LocatorCandidate(
                        "id",
                        candidate.getId());

        copyMetadata(
                candidate,
                generatedCandidate);
                applyDynamicAnalysis(
        generatedCandidate);

        generatedCandidate.setGeneratedLocator(true);

        generatedCandidate.setGenerationStrategy(
                "ID_GENERATOR");

        generatedCandidate.setGenerationConfidence(100);

        generated.add(
                generatedCandidate);
    }

    /*
     * -------------------------------------------------------
     * NAME Generator
     * -------------------------------------------------------
     */

    private void generateNameLocator(
            LocatorCandidate candidate,
            List<LocatorCandidate> generated) {

        if ("name".equalsIgnoreCase(
                candidate.getLocatorType())) {

            return;
        }

        if (!hasText(candidate.getName())) {
            return;
        }

        LocatorCandidate generatedCandidate =
                new LocatorCandidate(
                        "name",
                        candidate.getName());

        copyMetadata(
                candidate,
                generatedCandidate);

                applyDynamicAnalysis(
        generatedCandidate);

        generatedCandidate.setGeneratedLocator(true);

        generatedCandidate.setGenerationStrategy(
                "NAME_GENERATOR");

        generatedCandidate.setGenerationConfidence(95);

        generated.add(
                generatedCandidate);
    }
        /*
     * -------------------------------------------------------
     * LABEL BASED XPATH
     * -------------------------------------------------------
     */
private void generateLabelXpath(
        FailureContext context,
        LocatorCandidate candidate,
        List<LocatorCandidate> generated) {

    if (candidate == null) {
        return;
    }

    /*
     * Label XPath is only valid for real form controls.
     */
    String tagName =
            candidate.getTagName();

    if (!hasText(tagName)) {
        return;
    }

    tagName =
            tagName.trim()
                    .toLowerCase();

    if (!tagName.equals("input")
            && !tagName.equals("textarea")
            && !tagName.equals("select")) {

        return;
    }

    /*
     * IMPORTANT:
     *
     * Use the label belonging to THIS candidate.
     *
     * Do NOT use context.getNearestLabel().
     *
     * Example:
     *
     * employeeNameInput -> Employee Name
     * employeeIdInput   -> Employee Id
     * supervisorInput   -> Supervisor Name
     */
    String candidateLabel =
            candidate.getNearestLabel();

    if (!hasText(candidateLabel)) {
        return;
    }

    /*
     * If execution context contains a known label,
     * don't generate a locator from a different label.
     *
     * This prevents:
     *
     * Employee Name candidate
     *       ->
     * Password XPath
     */
    if (context != null
            && hasText(context.getNearestLabel())
            && !candidateLabel.trim()
                    .equalsIgnoreCase(
                            context.getNearestLabel().trim())) {

        return;
    }

    String xpath =
            "//label[normalize-space()="
                    + xpathLiteral(candidateLabel.trim())
                    + "]"
                    + "/ancestor::*[count(.//"
                    + tagName
                    + ")=1][1]"
                    + "//"
                    + tagName;

    LocatorCandidate generatedCandidate =
            new LocatorCandidate(
                    "xpath",
                    xpath);

    copyMetadata(
            candidate,
            generatedCandidate);

    applyDynamicAnalysis(
            generatedCandidate);

    generatedCandidate.setGeneratedLocator(true);

    generatedCandidate.setGenerationStrategy(
            "LABEL_XPATH_GENERATOR");

    generatedCandidate.setGenerationConfidence(92);

    generated.add(
            generatedCandidate);
}
    /*
     * -------------------------------------------------------
     * PARENT CSS
     * -------------------------------------------------------
     */
    private void generateParentCss(
            FailureContext context,
            LocatorCandidate candidate,
            List<LocatorCandidate> generated) {

                 if (context == null
            || candidate == null) {
        return;
    }

    if (context.getExpectedIntent() == ElementIntent.TEXT) {
        return;
    }

        if (context == null) {
            return;
        }

        if (hasText(context.getParentId())) {

            String css =
                    "#"
                            + context.getParentId()
                            + " "
                            + candidate.getTagName();

            LocatorCandidate generatedCandidate =
                    new LocatorCandidate(
                            "css",
                            css);

            copyMetadata(
                    candidate,
                    generatedCandidate);

                    applyDynamicAnalysis(
        generatedCandidate);

            generatedCandidate.setGeneratedLocator(true);

            generatedCandidate.setGenerationStrategy(
                    "PARENT_CSS_GENERATOR");

            generatedCandidate.setGenerationConfidence(90);

            generated.add(generatedCandidate);

            return;
        }


    }

    /*
     * -------------------------------------------------------
     * PARENT XPATH
     * -------------------------------------------------------
     */
    private void generateParentXpath(
            FailureContext context,
            LocatorCandidate candidate,
            List<LocatorCandidate> generated) {

        if (context == null) {
            return;
        }

        if (context.getExpectedIntent() == ElementIntent.TEXT) {
    return;
}

        if (!hasText(context.getParentId())) {
            return;
        }

        String xpath =
                "//*[@id='"
                        + context.getParentId()
                        + "']//"
                        + candidate.getTagName();

        LocatorCandidate generatedCandidate =
                new LocatorCandidate(
                        "xpath",
                        xpath);

        copyMetadata(
                candidate,
                generatedCandidate);
                applyDynamicAnalysis(
        generatedCandidate);

        generatedCandidate.setGeneratedLocator(true);

        generatedCandidate.setGenerationStrategy(
                "PARENT_XPATH_GENERATOR");

        generatedCandidate.setGenerationConfidence(88);

        generated.add(generatedCandidate);
    }
        /*
     * -------------------------------------------------------
     * COPY METADATA
     * -------------------------------------------------------
     */
    private void copyMetadata(
            LocatorCandidate source,
            LocatorCandidate target) {

        target.setTagName(
                source.getTagName());

        target.setIntent(
                source.getIntent());

        target.setNearestLabel(
                source.getNearestLabel());

        target.setElementText(
                source.getElementText());

        target.setPlaceholder(
                source.getPlaceholder());

        target.setId(
                source.getId());

        target.setName(
                source.getName());

        target.setAriaLabel(
                source.getAriaLabel());

        target.setParentTag(
                source.getParentTag());

        target.setParentId(
                source.getParentId());

        target.setParentClass(
                source.getParentClass());

        target.setOccurrenceCount(
                source.getOccurrenceCount());

        target.setUniqueLocator(
                source.isUniqueLocator());

                target.setScore(
        source.getScore());

target.setFinalScore(
        source.getFinalScore());

       target.setDynamicAttribute(
        source.isDynamicAttribute());

target.setDynamicPatternType(
        source.getDynamicPatternType());

target.setNormalizedLocatorValue(
        source.getNormalizedLocatorValue());

target.setStabilityScore(
        source.getStabilityScore());
    }

    private void applyDynamicAnalysis(
        LocatorCandidate candidate) {

    if (candidate == null) {
        return;
    }

    DynamicAttributeResult result =
            detector.analyze(
                    candidate.getLocatorType(),
                    candidate.getLocatorValue());

    candidate.setDynamicAttribute(
            result.isDynamic());

    candidate.setDynamicPatternType(
            result.getPatternType());

    candidate.setNormalizedLocatorValue(
            result.getNormalizedValue());

    candidate.setStabilityScore(
            result.getStabilityScore());
}

    /*
     * -------------------------------------------------------
     * ESCAPE XPATH
     * -------------------------------------------------------
     */
    private String escapeXpath(
            String value) {

        if (value == null) {
            return "";
        }

        return value.replace("'", "\\'");
    }

    /*
     * -------------------------------------------------------
     * HAS TEXT
     * -------------------------------------------------------
     */
    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }

    private String xpathLiteral(String value) {

    if (value == null) {
        return "''";
    }

    if (!value.contains("'")) {
        return "'" + value + "'";
    }

    if (!value.contains("\"")) {
        return "\"" + value + "\"";
    }

    String[] parts =
            value.split("'");

    StringBuilder xpath =
            new StringBuilder("concat(");

    for (int i = 0; i < parts.length; i++) {

        if (i > 0) {
            xpath.append(", \"'\", ");
        }

        xpath.append("'")
                .append(parts[i])
                .append("'");
    }

    xpath.append(")");

    return xpath.toString();
}

}