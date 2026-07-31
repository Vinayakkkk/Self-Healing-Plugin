package com.vinayak.healing.generator;

import com.vinayak.healing.model.LocatorCandidate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UniqueLocatorGenerator {

       private String refineCssSelector(
        Document document,
        Element target,
        String selector) {

    if (document == null || target == null || selector == null) {
        return selector;
    }

    // Already unique
    Elements matches = document.select(selector);

    if (matches.size() == 1
            && matches.first() == target) {
        return selector;
    }

    String refinedSelector = selector;

    Element current = target.parent();

    while (current != null) {

        String parentSelector = null;

        // Prefer unique parent id
        if (!current.id().isBlank()) {

            parentSelector = "#" + current.id();

        }
        // Then data-testid
        else if (current.hasAttr("data-testid")) {

            parentSelector =
                    "[data-testid='"
                    + current.attr("data-testid")
                    + "']";

        }
        // Then data-test
        else if (current.hasAttr("data-test")) {

            parentSelector =
                    "[data-test='"
                    + current.attr("data-test")
                    + "']";

        }
        // Then class
        else if (!current.className().isBlank()) {

            StringBuilder classes =
                    new StringBuilder(current.tagName());

            for (String cls : current.classNames()) {

                if (!cls.isBlank()) {
                    classes.append(".").append(cls);
                }
            }

            parentSelector = classes.toString();
        }

        if (parentSelector != null) {

           refinedSelector =
        parentSelector + " " + refinedSelector;

            matches =
                    document.select(refinedSelector);

            if (matches.size() == 1
                    && matches.first() == target) {

                return refinedSelector;
            }
        }

        current = current.parent();
    }

    return selector;
}

    public LocatorCandidate generate(
            Document document,
            LocatorCandidate candidate) {

        if (document == null || candidate == null) {
            return candidate;
        }

        // Already unique
        if (candidate.isUniqueLocator()) {
            return candidate;
        }

        System.out.println("\n===== UNIQUE LOCATOR GENERATOR =====");
        System.out.println("Duplicate locator detected");
        System.out.println("Type  : " + candidate.getLocatorType());
        System.out.println("Value : " + candidate.getLocatorValue());
        System.out.println("Tag   : " + candidate.getTagName());

        Elements matchingElements =
                findMatchingElements(document, candidate);

        System.out.println("\nMatching Elements : "
                + matchingElements.size());

        int index = 1;

        for (Element element : matchingElements) {

            System.out.println("\nElement " + index++);

            System.out.println("Tag         : " + element.tagName());
            System.out.println("Id          : " + element.id());
            System.out.println("Name        : " + element.attr("name"));
            System.out.println("Placeholder : "
                    + element.attr("placeholder"));
            System.out.println("Class       : "
                    + element.className());
            System.out.println("Text        : "
                    + element.ownText().trim());
        }

        // Next update:
        // Generate unique XPath/CSS from one of these elements.

        Element bestElement =
        findBestMatch(
                matchingElements,
                candidate);

if (bestElement != null) {

    System.out.println("\n===== SELECTED ELEMENT =====");
    System.out.println("Tag         : " + bestElement.tagName());
    System.out.println("Id          : " + bestElement.id());
    System.out.println("Name        : " + bestElement.attr("name"));
    System.out.println("Placeholder : " + bestElement.attr("placeholder"));
    System.out.println("Class       : " + bestElement.className());
    System.out.println("Text        : " + bestElement.ownText().trim());

    LocatorCandidate uniqueCandidate =
            generateUniqueCandidate(bestElement, candidate);

    if (uniqueCandidate != null) {

        System.out.println("\n===== GENERATED UNIQUE LOCATOR =====");
        System.out.println(uniqueCandidate.getLocatorType()
                + "="
                + uniqueCandidate.getLocatorValue());

        return uniqueCandidate;
    }
}

return candidate;
    }

    private Elements findMatchingElements(
            Document document,
            LocatorCandidate candidate) {

        String type =
                candidate.getLocatorType().toLowerCase();

        String value =
                candidate.getLocatorValue();

        switch (type) {

            case "id":
                return document.select(
                        "[id=\"" + escape(value) + "\"]");

            case "name":
                return document.select(
                        "[name=\"" + escape(value) + "\"]");

            case "placeholder":
                return document.select(
                        "[placeholder=\"" + escape(value) + "\"]");

            case "aria-label":
                return document.select(
                        "[aria-label=\"" + escape(value) + "\"]");

            case "data-test":
                return document.select(
                        "[data-test=\"" + escape(value) + "\"]");

            case "data-testid":
                return document.select(
                        "[data-testid=\"" + escape(value) + "\"]");

            case "data-qa":
                return document.select(
                        "[data-qa=\"" + escape(value) + "\"]");

            case "data-cy":
                return document.select(
                        "[data-cy=\"" + escape(value) + "\"]");

            case "class":

                StringBuilder selector =
                        new StringBuilder();

                for (String cls : value.split("\\s+")) {

                    if (!cls.isBlank()) {
                        selector.append(".").append(cls);
                    }
                }

                return document.select(selector.toString());

            default:
                return new Elements();
        }
    }

    private String escape(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
    private String escapeXpath(String value) {

    if (value == null) {
        return "";
    }

    return value.replace("'", "\\'");
}
    private Element findBestMatch(
        Elements matchingElements,
        LocatorCandidate candidate) {

    if (matchingElements == null || matchingElements.isEmpty()) {
        return null;
    }

    // 1. Prefer matching parent id
    if (candidate.getParentId() != null
            && !candidate.getParentId().isBlank()) {

        for (Element element : matchingElements) {

            Element parent = element.parent();

            if (parent != null
                    && candidate.getParentId()
                    .equalsIgnoreCase(parent.id())) {

                return element;
            }
        }
    }

    // 2. Prefer matching parent class
    if (candidate.getParentClass() != null
            && !candidate.getParentClass().isBlank()) {

        for (Element element : matchingElements) {

            Element parent = element.parent();

            if (parent != null
                    && parent.className()
                    .equalsIgnoreCase(candidate.getParentClass())) {

                return element;
            }
        }
    }

    // 3. Prefer nearest label match
    if (candidate.getNearestLabel() != null
            && !candidate.getNearestLabel().isBlank()) {

        for (Element element : matchingElements) {

            String label = findNearestLabel(element);

            if (candidate.getNearestLabel()
                    .equalsIgnoreCase(label)) {

                return element;
            }
        }
    }

    return matchingElements.first();
}
private String findNearestLabel(Element element) {

    if (element == null) {
        return "";
    }

    String id = element.id();

    if (!id.isBlank()) {

        Element label =
                element.ownerDocument()
                        .selectFirst(
                                "label[for='" + id + "']");

        if (label != null) {
            return label.text().trim();
        }
    }

    Element parent = element.parent();

    while (parent != null) {

        Element label = parent.selectFirst("label");

        if (label != null
                && !label.text().isBlank()) {

            return label.text().trim();
        }

        parent = parent.parent();
    }

    return "";
}
private LocatorCandidate generateUniqueCandidate(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return original;
    }

    // Priority 1 - Unique ID
    if (!element.id().isBlank()) {

        LocatorCandidate candidate =
                new LocatorCandidate(
                        "id",
                        element.id(),
                        original.getTagName(),
                        original.getInputType(),
                        original.getIntent(),
                        original.getScore(),
                        original.getParentTag(),
                        original.getParentClass(),
                        original.getParentId());

        candidate.setNearestLabel(original.getNearestLabel());

        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("ID");
candidate.setGenerationConfidence(100);

        return candidate;
    }

    // Priority 2 - data-testid
    if (element.hasAttr("data-testid")) {

        LocatorCandidate candidate =
                new LocatorCandidate(
                        "data-testid",
                        element.attr("data-testid"),
                        original.getTagName(),
                        original.getInputType(),
                        original.getIntent(),
                        original.getScore(),
                        original.getParentTag(),
                        original.getParentClass(),
                        original.getParentId());

        candidate.setNearestLabel(original.getNearestLabel());
        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("DATA_TESTID");
candidate.setGenerationConfidence(100);

        return candidate;
    }

    // Priority 3 - data-test
    if (element.hasAttr("data-test")) {

        LocatorCandidate candidate =
                new LocatorCandidate(
                        "data-test",
                        element.attr("data-test"),
                        original.getTagName(),
                        original.getInputType(),
                        original.getIntent(),
                        original.getScore(),
                        original.getParentTag(),
                        original.getParentClass(),
                        original.getParentId());

        candidate.setNearestLabel(original.getNearestLabel());
        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("DATA_TEST");
candidate.setGenerationConfidence(95);

        return candidate;
    }

    LocatorCandidate parentCandidate =
        tryParentUniqueId(element, original);

if (parentCandidate != null) {

    return parentCandidate;
}


LocatorCandidate parentDataCandidate =
        tryParentDataAttribute(element, original);

if (parentDataCandidate != null) {
    return parentDataCandidate;
}

LocatorCandidate semanticCandidate =
        trySemanticContainer(element, original);

if (semanticCandidate != null) {
    return semanticCandidate;
}

LocatorCandidate scopedCssCandidate =
        tryScopedCss(element, original);

if (scopedCssCandidate != null) {
    return scopedCssCandidate;
}

LocatorCandidate scopedXpathCandidate =
        tryScopedXpath(element, original);

if (scopedXpathCandidate != null) {
    return scopedXpathCandidate;
}

    // Priority 4 - Label XPath
    String label = findNearestLabel(element);

    if (!label.isBlank()) {

        String xpath =
                "//label[normalize-space()='"
                        + label
                        + "']/ancestor::*[count(.//"
                        + element.tagName()
                        + ")=1][1]//"
                        + element.tagName();

        LocatorCandidate candidate =
                new LocatorCandidate(
                        "xpath",
                        xpath,
                        original.getTagName(),
                        original.getInputType(),
                        original.getIntent(),
                        original.getScore(),
                        original.getParentTag(),
                        original.getParentClass(),
                        original.getParentId());

        candidate.setNearestLabel(label);

        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("LABEL_XPATH");
candidate.setGenerationConfidence(65);

        return candidate;
    }

    return original;
}
private LocatorCandidate tryParentUniqueId(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return null;
    }

    Element parent = element.parent();

    while (parent != null) {

        if (!parent.id().isBlank()) {

           String childSelector = buildChildSelector(element);

String css =
        "#" + parent.id()
        + " "
        + childSelector;

css =
        refineCssSelector(
                element.ownerDocument(),
                element,
                css);

Elements matches =
        element.ownerDocument().select(css);
if (matches.size() != 1) {
    parent = parent.parent();
    continue;
}


            LocatorCandidate candidate =
                    new LocatorCandidate(
                            "css",
                            css,
                            original.getTagName(),
                            original.getInputType(),
                            original.getIntent(),
                            original.getScore(),
                            original.getParentTag(),
                            original.getParentClass(),
                            original.getParentId());

                            candidate.setFinalScore(original.getFinalScore() + 300);

candidate.setOccurrenceCount(1);
candidate.setUniqueLocator(true);

candidate.setNearestLabel(original.getNearestLabel());

candidate.setElementText(original.getElementText());
candidate.setPlaceholder(original.getPlaceholder());
candidate.setAriaLabel(original.getAriaLabel());
candidate.setId(original.getId());
candidate.setName(original.getName());

            candidate.setNearestLabel(original.getNearestLabel());

            candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("PARENT_ID");
candidate.setGenerationConfidence(90);

            return candidate;
            
        }

        parent = parent.parent();
    }

    return null;
}
private LocatorCandidate tryParentDataAttribute(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return null;
    }

    Element parent = element.parent();

    while (parent != null) {

        String[][] attributes = {
                {"data-testid", "data-testid"},
                {"data-test", "data-test"},
                {"data-qa", "data-qa"},
                {"data-cy", "data-cy"}
        };

        for (String[] attribute : attributes) {

            String attributeName = attribute[0];

            if (parent.hasAttr(attributeName)) {

               String css =
        "[" + attributeName + "='"
        + parent.attr(attributeName)
        + "'] "
        + buildChildSelector(element);

css =
        refineCssSelector(
                element.ownerDocument(),
                element,
                css);

Elements matches =
        element.ownerDocument().select(css);

if (matches.size() != 1) {
    continue;
}

                LocatorCandidate candidate =
                        new LocatorCandidate(
                                "css",
                                css,
                                original.getTagName(),
                                original.getInputType(),
                                original.getIntent(),
                                original.getScore(),
                                original.getParentTag(),
                                original.getParentClass(),
                                original.getParentId());

                                candidate.setFinalScore(original.getFinalScore() + 300);

candidate.setOccurrenceCount(1);
candidate.setUniqueLocator(true);

candidate.setNearestLabel(original.getNearestLabel());

candidate.setElementText(original.getElementText());
candidate.setPlaceholder(original.getPlaceholder());
candidate.setAriaLabel(original.getAriaLabel());
candidate.setId(original.getId());
candidate.setName(original.getName());

                candidate.setNearestLabel(
                        original.getNearestLabel());
                        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("PARENT_DATA");
candidate.setGenerationConfidence(85);

                return candidate;
            }
        }

        parent = parent.parent();
    }

    return null;
}
private LocatorCandidate trySemanticContainer(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return null;
    }

    Element parent = element.parent();

    while (parent != null) {

        String tag = parent.tagName().toLowerCase();

        if (tag.equals("form")
                || tag.equals("section")
                || tag.equals("main")
                || tag.equals("article")
                || tag.equals("dialog")
                || tag.equals("aside")
                || tag.equals("nav")
                || tag.equals("header")
                || tag.equals("footer")
                || tag.equals("fieldset")
                || tag.equals("table")) {

            String containerSelector = null;

            // Highest priority - id
            if (!parent.id().isBlank()) {

                containerSelector = "#" + parent.id();

            }
            // data-testid
            else if (parent.hasAttr("data-testid")) {

                containerSelector =
                        "[data-testid='"
                                + parent.attr("data-testid")
                                + "']";

            }
            // data-test
            else if (parent.hasAttr("data-test")) {

                containerSelector =
                        "[data-test='"
                                + parent.attr("data-test")
                                + "']";

            }
            // aria-label
            else if (parent.hasAttr("aria-label")) {

                containerSelector =
                        "[aria-label='"
                                + parent.attr("aria-label")
                                + "']";

            }
            // role
            else if (parent.hasAttr("role")) {

                containerSelector =
                        tag
                                + "[role='"
                                + parent.attr("role")
                                + "']";
            }

            if (containerSelector != null) {
String childSelector =
        buildChildSelector(element);
                String css =
        containerSelector
        + " "
        + childSelector;

css =
        refineCssSelector(
                element.ownerDocument(),
                element,
                css);

Elements matches =
        element.ownerDocument().select(css);

if (matches.size() != 1) {
    parent = parent.parent();
    continue;
}

                LocatorCandidate candidate =
                        new LocatorCandidate(
                                "css",
                                css,
                                original.getTagName(),
                                original.getInputType(),
                                original.getIntent(),
                                original.getScore(),
                                original.getParentTag(),
                                original.getParentClass(),
                                original.getParentId());

                                candidate.setFinalScore(original.getFinalScore() + 300);

candidate.setOccurrenceCount(1);
candidate.setUniqueLocator(true);

candidate.setNearestLabel(original.getNearestLabel());

candidate.setElementText(original.getElementText());
candidate.setPlaceholder(original.getPlaceholder());
candidate.setAriaLabel(original.getAriaLabel());
candidate.setId(original.getId());
candidate.setName(original.getName());

                candidate.setNearestLabel(
                        original.getNearestLabel());

                        candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("SEMANTIC_CONTAINER");
candidate.setGenerationConfidence(80);

                return candidate;
            }
        }

        parent = parent.parent();
    }

    return null;
}

private LocatorCandidate tryScopedCss(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return null;
    }

    Element parent = element.parent();

    while (parent != null) {

        String parentSelector = null;

        // Priority 1 - Parent ID
        if (!parent.id().isBlank()) {

            parentSelector = "#" + parent.id();

        }

        // Priority 2 - Parent Class
        else if (!parent.className().isBlank()) {

            StringBuilder classes = new StringBuilder();

            for (String cls : parent.classNames()) {

                if (!cls.isBlank()) {
                    classes.append(".").append(cls);
                }
            }

            if (!classes.isEmpty()) {
                parentSelector =
                        parent.tagName() + classes;
            }
        }

        if (parentSelector != null) {

            String css =
        parentSelector
        + " "
        + buildChildSelector(element);

css =
        refineCssSelector(
                element.ownerDocument(),
                element,
                css);

Elements matches =
        element.ownerDocument().select(css);

if (matches.size() != 1) {
    parent = parent.parent();
    continue;
}

            LocatorCandidate candidate =
                    new LocatorCandidate(
                            "css",
                            css,
                            original.getTagName(),
                            original.getInputType(),
                            original.getIntent(),
                            original.getScore(),
                            original.getParentTag(),
                            original.getParentClass(),
                            original.getParentId());

                            candidate.setFinalScore(original.getFinalScore() + 300);

candidate.setOccurrenceCount(1);
candidate.setUniqueLocator(true);

candidate.setNearestLabel(original.getNearestLabel());

candidate.setElementText(original.getElementText());
candidate.setPlaceholder(original.getPlaceholder());
candidate.setAriaLabel(original.getAriaLabel());
candidate.setId(original.getId());
candidate.setName(original.getName());

            candidate.setNearestLabel(
                    original.getNearestLabel());

                    candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("SCOPED_CSS");
candidate.setGenerationConfidence(75);

            return candidate;
        }

        parent = parent.parent();
    }

    return null;
}
private LocatorCandidate tryScopedXpath(
        Element element,
        LocatorCandidate original) {

    if (element == null) {
        return null;
    }

    Element parent = element.parent();

    while (parent != null) {

        String ancestorXpath = null;

        // Highest priority - id
        if (!parent.id().isBlank()) {

            ancestorXpath =
                    "//"
                    + parent.tagName()
                    + "[@id='"
                    + escapeXpath(parent.id())
                    + "']";
        }

        // data-testid
        else if (parent.hasAttr("data-testid")) {

            ancestorXpath =
                    "//"
                    + parent.tagName()
                    + "[@data-testid='"
                    + escapeXpath(parent.attr("data-testid"))
                    + "']";
        }

        // data-test
        else if (parent.hasAttr("data-test")) {

            ancestorXpath =
                    "//"
                    + parent.tagName()
                    + "[@data-test='"
                    + escapeXpath(parent.attr("data-test"))
                    + "']";
        }

        // aria-label
        else if (parent.hasAttr("aria-label")) {

            ancestorXpath =
                    "//"
                    + parent.tagName()
                    + "[@aria-label='"
                    + escapeXpath(parent.attr("aria-label"))
                    + "']";
        }

        if (ancestorXpath != null) {

            String xpath =
                    ancestorXpath
                    + "//"
                    + buildXpathSelector(element);

                    Elements matches =
        element.ownerDocument().selectXpath(xpath);

if (matches.size() != 1
        || matches.first() != element) {

    parent = parent.parent();
    continue;
}

            LocatorCandidate candidate =
                    new LocatorCandidate(
                            "xpath",
                            xpath,
                            original.getTagName(),
                            original.getInputType(),
                            original.getIntent(),
                            original.getScore(),
                            original.getParentTag(),
                            original.getParentClass(),
                            original.getParentId());

                            candidate.setFinalScore(original.getFinalScore() + 300);

candidate.setOccurrenceCount(matches.size());
candidate.setUniqueLocator(matches.size() == 1);

candidate.setNearestLabel(original.getNearestLabel());

candidate.setElementText(original.getElementText());
candidate.setPlaceholder(original.getPlaceholder());
candidate.setAriaLabel(original.getAriaLabel());
candidate.setId(original.getId());
candidate.setName(original.getName());

            candidate.setNearestLabel(
                    original.getNearestLabel());

                    candidate.setGeneratedLocator(true);
candidate.setGenerationStrategy("SCOPED_XPATH");
candidate.setGenerationConfidence(70);

            return candidate;
        }

        parent = parent.parent();
    }

    return null;
}
private String buildChildSelector(Element element) {

    // Highest priority
    if (!element.id().isBlank()) {
        return "#" + element.id();
    }

    if (element.hasAttr("data-testid")) {
        return element.tagName()
                + "[data-testid='"
                + element.attr("data-testid")
                + "']";
    }

    if (element.hasAttr("data-test")) {
        return element.tagName()
                + "[data-test='"
                + element.attr("data-test")
                + "']";
    }

    if (element.hasAttr("data-qa")) {
        return element.tagName()
                + "[data-qa='"
                + element.attr("data-qa")
                + "']";
    }

    if (element.hasAttr("data-cy")) {
        return element.tagName()
                + "[data-cy='"
                + element.attr("data-cy")
                + "']";
    }

    if (element.hasAttr("name")) {
        return element.tagName()
                + "[name='"
                + element.attr("name")
                + "']";
    }

    if (element.hasAttr("placeholder")) {
        return element.tagName()
                + "[placeholder='"
                + element.attr("placeholder")
                + "']";
    }

    if (element.hasAttr("aria-label")) {
        return element.tagName()
                + "[aria-label='"
                + element.attr("aria-label")
                + "']";
    }

    // NEW: use class before type
    if (!element.className().isBlank()) {

        StringBuilder selector =
                new StringBuilder(element.tagName());

        for (String cls : element.classNames()) {

            if (!cls.isBlank()) {
                selector.append(".").append(cls);
            }
        }

        return selector.toString();
    }

    // Last option
    if (element.hasAttr("type")) {
        return element.tagName()
                + "[type='"
                + element.attr("type")
                + "']";
    }

    return element.tagName();
}
private String buildXpathSelector(Element element) {

    if (!element.id().isBlank()) {

        return element.tagName()
                + "[@id='"
                + escapeXpath(element.id())
                + "']";
    }

    if (element.hasAttr("data-testid")) {

        return element.tagName()
                + "[@data-testid='"
                + escapeXpath(element.attr("data-testid"))
                + "']";
    }

    if (element.hasAttr("data-test")) {

        return element.tagName()
                + "[@data-test='"
                + escapeXpath(element.attr("data-test"))
                + "']";
    }

    if (element.hasAttr("data-qa")) {

        return element.tagName()
                + "[@data-qa='"
                + escapeXpath(element.attr("data-qa"))
                + "']";
    }

    if (element.hasAttr("data-cy")) {

        return element.tagName()
                + "[@data-cy='"
                + escapeXpath(element.attr("data-cy"))
                + "']";
    }

    if (element.hasAttr("name")) {

        return element.tagName()
                + "[@name='"
                + escapeXpath(element.attr("name"))
                + "']";
    }

    if (element.hasAttr("placeholder")) {

        return element.tagName()
                + "[@placeholder='"
                + escapeXpath(element.attr("placeholder"))
                + "']";
    }

    if (element.hasAttr("type")) {

        return element.tagName()
                + "[@type='"
                + escapeXpath(element.attr("type"))
                + "']";
    }

    if (element.hasAttr("aria-label")) {

        return element.tagName()
                + "[@aria-label='"
                + escapeXpath(element.attr("aria-label"))
                + "']";
    }

    return element.tagName();
}
}