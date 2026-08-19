package com.vinayak.healing.dom;

import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.util.SimilarityUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import com.vinayak.healing.generator.ContextAwareLocatorGenerator;
import com.vinayak.healing.generator.UniqueLocatorGenerator;
//import com.vinayak.healing.model.ContextInfo;
import com.vinayak.healing.model.FailureContext;
import java.util.Set;
import java.util.stream.Collectors;
import com.vinayak.healing.util.TokenParser;
import com.vinayak.healing.analyzer.VariableAnalyzer;
import com.vinayak.healing.dynamic.DynamicAttributeDetector;
import com.vinayak.healing.dynamic.DynamicAttributeResult;
import com.vinayak.healing.model.VariableInfo;
import com.vinayak.healing.extractor.ElementFeatureExtractor;
import com.vinayak.healing.model.ElementFeature;
// import com.vinayak.healing.dom.DomContextAnalyzer;
// import com.vinayak.healing.analyzer.CandidateRanker;
// import com.vinayak.healing.context.ContextBuilder;
import com.vinayak.healing.analyzer.LocatorAnalyzer;
//import com.vinayak.healing.model.ContextFeature;
import com.vinayak.healing.model.LocatorInfo;

public class DomCandidateFinder {

private final ContextAwareLocatorGenerator contextAwareLocatorGenerator =
        new ContextAwareLocatorGenerator();
        private final DomContextAnalyzer analyzer =
        new DomContextAnalyzer();
   private final LocatorAnalyzer locatorAnalyzer =
        new LocatorAnalyzer();
        private final UniqueLocatorGenerator uniqueLocatorGenerator =
        new UniqueLocatorGenerator();
       

private final VariableAnalyzer variableAnalyzer =
        new VariableAnalyzer();



private final DynamicAttributeDetector dynamicAttributeDetector =
        new DynamicAttributeDetector();

   

private final ElementFeatureExtractor extractor =
        new ElementFeatureExtractor();

   public List<LocatorCandidate> findCandidates(
        String html,
        String failedLocator,
        FailureContext context){

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        Document document =
                Jsoup.parse(html);

        Elements elements =
        document.getAllElements();
        addCollectionCandidates(
        candidates,
        elements,
        context);

        System.out.println("\n===== DOM ELEMENTS =====");
System.out.println("Element Count = " + elements.size());

for (Element e : elements) {
    System.out.println(
        e.tagName()
        + " id=" + e.id()
        + " name=" + e.attr("name")
        + " placeholder=" + e.attr("placeholder"));
}

       

               VariableInfo variableInfo =
        variableAnalyzer.analyze(
                context.getVariableName());

LocatorInfo locatorInfo =
        locatorAnalyzer.analyze(
                failedLocator);         
       

        for (Element element : elements) {

                ElementIntent expectedIntent =
        context == null
                ? ElementIntent.UNKNOWN
                : context.getExpectedIntent();

ElementIntent actualIntent =
        determineIntent(element);

        System.out.println(
        "PROCESS CHECK -> tag="
        + element.tagName()
        + " name="
        + element.attr("name")
        + " id="
        + element.id()
        + " placeholder="
        + element.attr("placeholder")
        + " expectedIntent="
        + expectedIntent
        + " actualIntent="
        + actualIntent
        + " process="
        + shouldProcessElement(
                expectedIntent,
                actualIntent,
                element));

if (!shouldProcessElement(
        expectedIntent,
        actualIntent,
        element)) {

    continue;
}


               
            ElementFeature feature =
        extractor.extract(element);


        String nearestLabel =
        findClosestSemanticLabel(element);

addLabelScopedInputCandidate(
        candidates,
        failedLocator,
        element,
        context,
        variableInfo,
        locatorInfo,
        nearestLabel,
        0);

           

          


            

                String tag =
        element.tagName();

        if(tag.equalsIgnoreCase("meta")
        || tag.equalsIgnoreCase("title")
        || tag.equalsIgnoreCase("link")
        || tag.equalsIgnoreCase("noscript")
        || tag.equalsIgnoreCase("svg")
        || tag.equalsIgnoreCase("path")) {

    continue;
}

if(tag.equalsIgnoreCase("html")
        || tag.equalsIgnoreCase("head")
        || tag.equalsIgnoreCase("body")
        || tag.equalsIgnoreCase("script")
        || tag.equalsIgnoreCase("style")) {

    continue;
}



            // Ignore hidden elements
            if ("hidden".equalsIgnoreCase(
                    element.attr("type"))) {

                continue;
            }

         if (element.tagName().equalsIgnoreCase("html")
        || element.tagName().equalsIgnoreCase("body")
        || element.tagName().equalsIgnoreCase("main")) {
    continue;
}


    addCandidate(
        candidates,
        failedLocator,
        "data-qa",
        element.attr("data-qa"),
        element,
        feature,
       
        context, variableInfo,
        locatorInfo);

addCandidate(
        candidates,
        failedLocator,
        "data-cy",
        element.attr("data-cy"),
        element,
        feature,
        
        context,variableInfo,
        locatorInfo);

            addCandidate(
                    candidates,
                    failedLocator,
                    "id",
                    element.id(),
                    element,
                    feature,
                    
                    context,variableInfo,
        locatorInfo);

        System.out.println(
        "NAME CANDIDATE CHECK -> "
        + element.attr("name"));

            addCandidate(
                    candidates,
                    failedLocator,
                    "name",
                    element.attr("name"),
                    element,
                    feature,
                    
                    context,variableInfo,
        locatorInfo);

            addCandidate(
                    candidates,
                    failedLocator,
                    "placeholder",
                    element.attr("placeholder"),
                    element,
                    feature,
                   
                    context, variableInfo,
        locatorInfo);

            addCandidate(
                    candidates,
                    failedLocator,
                    "type",
                    element.attr("type"),
                    element,
                    feature,
                    
                    context,variableInfo,
        locatorInfo);

                    

            addCandidate(
                    candidates,
                    failedLocator,
                    "class",
                    element.className(),
                    element,
                    feature,
                    
                    context,variableInfo,
        locatorInfo);

            

            addCandidate(
                    candidates,
                    failedLocator,
                    "aria-label",
                    element.attr("aria-label"),
                    element,
                    feature,
                    
                    context,variableInfo,
        locatorInfo);

            addCandidate(
                    candidates,
                    failedLocator,
                    "title",
                    element.attr("title"),
                    element,
                    feature,
                   
                    context, variableInfo,
        locatorInfo);
String visibleText =
        extractCandidateText(element);

if (!visibleText.isBlank()) {

    addCandidate(
            candidates,
            failedLocator,
            "text",
            visibleText,
            element,
            feature,
            context,
            variableInfo,
            locatorInfo);

    addScopedTextCandidate(
            candidates,
            element,
            visibleText,
            context,
            variableInfo);

    addScopedButtonTextCandidate(
            candidates,
            element,
            visibleText,
            context);

    addScopedLinkTextCandidate(
            candidates,
            element,
            visibleText,
            context);
}



                    addCandidate(
        candidates,
        failedLocator,
        "data-test",
        element.attr("data-test"),
        element,
        feature,
        context,
 variableInfo,
        locatorInfo);

        addCandidate(
        candidates,
        failedLocator,
        "href",
        element.attr("href"),
        element,
        feature,
        context,
variableInfo,
        locatorInfo);

        addCandidate(
        candidates,
        failedLocator,
        "role",
        element.attr("role"),
        element,
        feature,
        
        context,variableInfo,
        locatorInfo);

        addCandidate(
    candidates,
    failedLocator,
    "data-testid",
    element.attr("data-testid"),
    element,
    feature,
    
    context,variableInfo,
        locatorInfo);

           
        }

        System.out.println("\n===== RAW CANDIDATES =====");
System.out.println("Total : " + candidates.size());

for (LocatorCandidate c : candidates) {
    System.out.println(
        c.getLocatorType() + "=" +
        c.getLocatorValue() +
        " tag=" + c.getTagName() +
        " intent=" + c.getIntent());
}

        populateLocatorUniqueness(
        document,
        candidates);

System.out.println(
        "\n===== LOCATOR UNIQUENESS ANALYSIS =====");

for (int i = 0; i < candidates.size(); i++) {

    LocatorCandidate candidate =
            candidates.get(i);

    System.out.println(
            "LOCATOR CHECK -> "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue()
                    + " | occurrences="
                    + candidate.getOccurrenceCount()
                    + " | unique="
                    + candidate.isUniqueLocator());

    /*
     * Unique locator:
     * No refinement is required.
     */
    if (candidate.isUniqueLocator()) {

        System.out.println(
                "UNIQUE LOCATOR -> SKIP REFINEMENT | "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());

        continue;
    }

    /*
     * Duplicate locator:
     * Try to generate a more specific locator.
     */
    System.out.println(
            "DUPLICATE LOCATOR -> REFINING | "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue()
                    + " | occurrences="
                    + candidate.getOccurrenceCount());

    LocatorCandidate updated =
            uniqueLocatorGenerator.generate(
                    document,
                    candidate);

    if (updated != null) {

        System.out.println(
                "REFINED LOCATOR -> "
                        + updated.getLocatorType()
                        + "="
                        + updated.getLocatorValue());

        candidates.set(i, updated);

    } else {

        System.out.println(
                "REFINEMENT FAILED -> Keeping original locator | "
                        + candidate.getLocatorType()
                        + "="
                        + candidate.getLocatorValue());
    }
}

/*
 * Recalculate uniqueness because some
 * duplicate candidates may have been refined.
 */
populateLocatorUniqueness(
        document,
        candidates);

candidates = removeDuplicateCandidates(candidates);

/*
 * Generate additional context-aware locators.
 */
List<LocatorCandidate> generatedCandidates =
        new ArrayList<>();

for (LocatorCandidate candidate : candidates) {

    generatedCandidates.addAll(
            contextAwareLocatorGenerator.generate(
                    context,
                    candidate));
}

/*
 * Merge generated candidates.
 */
candidates.addAll(generatedCandidates);

/*
 * Remove duplicates again because the generated
 * locator may already exist.
 */
candidates = removeDuplicateCandidates(candidates);

populateLocatorUniqueness(
        document,
        candidates);

candidates.sort(
        Comparator.comparingDouble(
                LocatorCandidate::getFinalScore)
                .reversed());

     System.out.println("\n===== FINAL DOM CANDIDATES =====");
System.out.println("Total : " + candidates.size());

for (LocatorCandidate c : candidates) {
    System.out.println(
        c.getLocatorType() + "=" +
        c.getLocatorValue() +
        " score=" + c.getFinalScore());
}


        return candidates;
     
    }



private void addCandidate(

        List<LocatorCandidate> candidates,

        String failedLocator,

        String locatorType,

        String locatorValue,

        Element element,

        ElementFeature feature,

        FailureContext context,

        VariableInfo variableInfo,

        LocatorInfo locatorInfo) {

        if (locatorValue == null
                || locatorValue.isBlank()) {

            return;
        }

        if (locatorType.equalsIgnoreCase("class")) {

    String value = locatorValue.toLowerCase();

    /*
     * Reject structural / visual CSS classes.
     * They commonly match many unrelated elements and are not
     * meaningful locator identities.
     */
    if (value.contains("icon")
            || value.contains("container")
            || value.contains("wrapper")
            || value.contains("layout")
            || value.contains("grid")
            || value.contains("row")
            || value.contains("column")
            || value.contains("active")
            || value.contains("disabled")) {

        return;
    }
}


if(locatorType.equalsIgnoreCase("type")) {

    return;
}
        if (isUsuallyUnique(locatorType)
        && exists(
                candidates,
                locatorType,
                locatorValue,
                element)) {

    return;
}

        if(locatorType.equalsIgnoreCase("text")
        && locatorValue.length() > 30) {

    return;
}



      String comparisonText = failedLocator;

if(context != null
        && context.getVariableName() != null
        && !context.getVariableName().isBlank()) {

    comparisonText =
            context.getVariableName();
}

double score =
        SimilarityUtil.score(
                comparisonText,
                locatorValue);

               
                Element parent =
        element.parent();

String parentTag = "";
String parentClass = "";
String parentId = "";

if(parent != null) {

    parentTag = parent.tagName();
    parentClass = parent.className();
    parentId = parent.id();
}

LocatorCandidate candidate =
        new LocatorCandidate(
                locatorType,
                locatorValue,
                element.tagName(),
                element.attr("type"),
                determineIntent(element),
                score,
                parentTag,
                parentClass,
                parentId);
DynamicAttributeResult dynamicResult =
        dynamicAttributeDetector.analyze(
                locatorType,
                locatorValue);



candidate.setDynamicAttribute(
        dynamicResult.isDynamic());

candidate.setDynamicPatternType(
        dynamicResult.getPatternType());

candidate.setNormalizedLocatorValue(
        dynamicResult.getNormalizedValue());

candidate.setStabilityScore(
        dynamicResult.getStabilityScore());
               

double finalScore = score;
String nearestLabel = "";
if(context != null
        && context.getExpectedIntent()
        == ElementIntent.INPUT) {

    if(element.tagName().equalsIgnoreCase("input")
            || element.tagName().equalsIgnoreCase("textarea")
            || element.tagName().equalsIgnoreCase("select")) {

        finalScore += 50;

    } else {

        finalScore -= 50;
    }
}

if (context != null
        && context.getExpectedIntent() == ElementIntent.BUTTON) {

    if (determineIntent(element) == ElementIntent.BUTTON) {

        finalScore += 75;

    } else {

        finalScore -= 50;
    }
}


if(element.childrenSize() > 10) {

    finalScore -= 100;
}


if(element.text().length() > 50) {

    finalScore -= 80;
}

                String tag =
        element.tagName()
                .toLowerCase();

if (context != null
        && context.getExpectedTag() != null
        && !context.getExpectedTag().isBlank()
        && !tag.equalsIgnoreCase(
                context.getExpectedTag())) {

    finalScore -= 20;
}



               if(tag.equals("h1")
        || tag.equals("h2")
        || tag.equals("h3")
        || tag.equals("h4")
        || tag.equals("h5")
        || tag.equals("h6")) {

    finalScore += 30;
}


if(tag.equals("div")
        || tag.equals("section")
        || tag.equals("article")) {

    finalScore -= 50;
}
if(tag.equals("main")) {
    finalScore -= 20;
}

      if (context != null
        && variableInfo != null
        && context.getVariableName() != null
        && !context.getVariableName().isBlank()) {

String domContext =
        analyzer.getFullContext(
                element);

String searchableText =
        locatorValue + " "
        + element.tagName() + " "
        + element.id() + " "
        + element.className() + " "
        + element.attr("name") + " "
        + element.attr("placeholder") + " "
        + element.attr("aria-label") + " "
        + element.attr("data-test") + " "
        + element.attr("data-testid") + " "
        + element.attr("data-qa") + " "
        + element.attr("data-cy") + " "
        + domContext;
        searchableText += " "
        + element.text();

nearestLabel =
        findClosestSemanticLabel(element);

if (!nearestLabel.isBlank()) {

    searchableText += " " + nearestLabel;
}

String variableName =
        context.getVariableName();

      List<String> variableTokens =
        variableInfo == null
                ? List.of()
                : variableInfo.getTokens();

        List<String> labelTokens =
        TokenParser.parse(nearestLabel);

int nearestLabelMatches =
        countExactMatches(
                variableTokens,
                labelTokens);

                System.out.println("VariableTokens : " + variableTokens);
System.out.println("LabelTokens    : " + labelTokens);
System.out.println("Matches        : " + nearestLabelMatches);
System.out.println("TotalTokens    : " + variableTokens.size());
System.out.println("===============================");




if (!nearestLabel.isBlank()) {

    int totalVariableTokens =
            variableTokens.size();

    if (nearestLabelMatches == totalVariableTokens
            && totalVariableTokens > 0) {

        finalScore += 700;

    } else if (nearestLabelMatches > 0) {

        finalScore += nearestLabelMatches * 150;
    }
}

List<String> candidateTokens =
        TokenParser.parse(searchableText);

double variableScore =
        variableScore(
                variableTokens,
                candidateTokens);

finalScore += variableScore;

}


if(context != null
        && context.getLocatorDeclaration() != null
        && !context.getLocatorDeclaration().isBlank()) {

    String declarationText =
            context.getLocatorDeclaration()
                    .toLowerCase();

    String candidateText =
            (
                    locatorValue + " "
                    + element.id() + " "
                    + element.className() + " "
                    + element.attr("name") + " "
                    + element.attr("data-test")
            ).toLowerCase();

    int declarationMatches =
            tokenMatches(
                    declarationText,
                    candidateText);

    finalScore += declarationMatches * 25;
}


ElementIntent intent =
        candidate.getIntent();

if (context != null
        && context.getExpectedIntent() != null
        && context.getExpectedIntent() != ElementIntent.UNKNOWN) {

    if (intent == context.getExpectedIntent()) {

        finalScore += 150;

    } else if (intent != ElementIntent.UNKNOWN) {

        finalScore -= 150;
    }
}

if(locatorValue.length() < 3) {

    finalScore -= 50;
}

if(locatorValue.equalsIgnoreCase(
        element.tagName())) {

    finalScore -= 50;
}

if (locatorType.equalsIgnoreCase("text")
        && dynamicResult.isDynamic()) {

    String dynamicXpath =
            buildDynamicTextXpath(
                    element,
                    dynamicResult);

    if (dynamicXpath != null
            && !dynamicXpath.isBlank()
            && !exists(
                    candidates,
                    "xpath",
                    dynamicXpath,
                    element)) {

        LocatorCandidate dynamicCandidate =
                new LocatorCandidate(
                        "xpath",
                        dynamicXpath,
                        element.tagName(),
                        element.attr("type"),
                        determineIntent(element),
                        score,
                        parentTag,
                        parentClass,
                        parentId);

        dynamicCandidate.setGeneratedLocator(true);
        dynamicCandidate.setDynamicAttribute(true);

        dynamicCandidate.setDynamicPatternType(
                dynamicResult.getPatternType());

        dynamicCandidate.setNormalizedLocatorValue(
                dynamicResult.getNormalizedValue());

        dynamicCandidate.setStabilityScore(
                dynamicResult.getStabilityScore());

        dynamicCandidate.setElementText(
                element.text().trim());

        dynamicCandidate.setFinalScore(
                finalScore + 100);

        candidates.add(dynamicCandidate);
    }
}

candidate.setFinalScore(
        finalScore);

       candidate.setNearestLabel(
        nearestLabel);

        candidate.setElementText(
        element.text().trim());

candidate.setPlaceholder(
        element.attr("placeholder"));

candidate.setAriaLabel(
        element.attr("aria-label"));

        candidate.setParentTag(parentTag);

candidate.setParentId(parentId);

candidate.setParentClass(parentClass);


candidate.setId(
        element.id());

candidate.setName(
        element.attr("name"));

        System.out.println(
    "ADD -> "
    + locatorType
    + "="
    + locatorValue
    + " tag="
    + element.tagName());

candidates.add(
        candidate);
        addLabelScopedInputCandidate(
        candidates,
        failedLocator,
        element,
        context,
        variableInfo,
        locatorInfo,
        nearestLabel,
        finalScore);


}
private boolean isDuplicateAttributeCandidate(
        Element element,
        String locatorType,
        String locatorValue) {

    if (element == null
            || locatorType == null
            || locatorValue == null
            || locatorValue.isBlank()) {

        return false;
    }

    String escapedValue =
            locatorValue.replace("'", "\\'");

    String selector;

    switch (locatorType.toLowerCase()) {

        case "placeholder":
            selector =
                    "[placeholder='"
                            + escapedValue
                            + "']";
            break;

        case "name":
            selector =
                    "[name='"
                            + escapedValue
                            + "']";
            break;

        case "aria-label":
            selector =
                    "[aria-label='"
                            + escapedValue
                            + "']";
            break;

        default:
            return false;
    }

    return element.ownerDocument()
            .select(selector)
            .size() > 1;
}


private void addLabelScopedInputCandidate(
        List<LocatorCandidate> candidates,
        String failedLocator,
        Element element,
        FailureContext context,
        VariableInfo variableInfo,
        LocatorInfo locatorInfo,
        String nearestLabel,
        double baseScore) {

    if (context == null) {
        return;
    }

    String tag =
            element.tagName()
                    .toLowerCase();

    boolean isEditableField =
            tag.equals("input")
                    || tag.equals("textarea")
                    || tag.equals("select");

    if (!isEditableField) {
        return;
    }

    /*
     * Create this candidate when either:
     * 1. framework already knows it is INPUT
     * 2. failed action was SEND_KEYS or CLEAR
     * 3. original locator declaration expects an input tag
     */
    boolean expectedInput =
            context.getExpectedIntent()
                    == ElementIntent.INPUT;

    boolean actionNeedsInput =
            context.getFailedAction() != null
                    && (
                    context.getFailedAction()
                            .name()
                            .equalsIgnoreCase("SEND_KEYS")
                    || context.getFailedAction()
                            .name()
                            .equalsIgnoreCase("CLEAR")
            );

    boolean expectedTagIsInput =
            context.getExpectedTag() != null
                    && (
                    context.getExpectedTag()
                            .equalsIgnoreCase("input")
                    || context.getExpectedTag()
                            .equalsIgnoreCase("textarea")
                    || context.getExpectedTag()
                            .equalsIgnoreCase("select")
            );

    if (!expectedInput
            && !actionNeedsInput
            && !expectedTagIsInput) {

        return;
    }

String labelText = nearestLabel;

if (labelText == null
        || labelText.isBlank()) {

    labelText = findClosestSemanticLabel(element);
}

if (labelText == null
        || labelText.isBlank()) {

    return;
}

  String safeLabel =
        xpathLiteral(labelText);

  String xpath =
        "//label[normalize-space()="
                + safeLabel
                + "]"
                + "/ancestor::*[count(.//"
                + tag
                + ")=1][1]"
                + "//"
                + tag;

    if (exists(
            candidates,
            "xpath",
            xpath,
            element)) {

        return;
    }

    Element parent =
            element.parent();

    LocatorCandidate scopedCandidate =
            new LocatorCandidate(
                    "xpath",
                    xpath,
                    tag,
                    element.attr("type"),
                    determineIntent(element),
                    baseScore + 200,
                    parent == null
                            ? ""
                            : parent.tagName(),
                    parent == null
                            ? ""
                            : parent.className(),
                    parent == null
                            ? ""
                            : parent.id());

                            

    scopedCandidate.setNearestLabel(
            labelText);

    scopedCandidate.setFinalScore(
            baseScore + 200);

    candidates.add(scopedCandidate);



                    
}

private void addScopedTextCandidate(
        List<LocatorCandidate> candidates,
        Element element,
        String text,
        FailureContext context,
        VariableInfo variableInfo) {

    if (context == null
            || element == null
            || text == null
            || text.isBlank()) {

        return;
    }

    String normalizedText =
            text.trim();

            System.out.println(
        "TEXT CANDIDATE CHECK -> tag="
        + element.tagName()
        + " | text="
        + normalizedText
        + " | intent="
        + determineIntent(element));

    /*
     * Avoid extremely large text blocks.
     * This is structural protection, not
     * application-specific filtering.
     */
    if (normalizedText.length() > 80) {
        return;
    }

    String tag =
            element.tagName()
                    .toLowerCase();

    /*
     * Generate scoped text candidates only
     * when the DOM element itself represents
     * textual content.
     */
    if (determineIntent(element)
            != ElementIntent.TEXT) {

        return;
    }

    // ==========================================
    // BUILD SAFE UNIQUE TEXT XPATH
    // ==========================================

    String safeText =
            xpathLiteral(
                    normalizedText);

    String tagTextXpath =
            "//"
                    + tag
                    + "[normalize-space()="
                    + safeText
                    + "]";

    int matchCount =
            element.ownerDocument()
                    .selectXpath(
                            tagTextXpath)
                    .size();

    /*
     * A direct text XPath should only become
     * a candidate when it uniquely identifies
     * the current DOM element.
     */
    if (matchCount != 1) {
        return;
    }

    if (exists(
            candidates,
            "xpath",
            tagTextXpath,
            element)) {

        return;
    }

    // ==========================================
    // BUILD COMPLETE ELEMENT CONTEXT
    // ==========================================

    StringBuilder semanticContext =
            new StringBuilder();

    appendContext(
            semanticContext,
            normalizedText);

    appendContext(
            semanticContext,
            element.id());

    appendContext(
            semanticContext,
            element.className());

    appendContext(
            semanticContext,
            element.attr("name"));

    appendContext(
            semanticContext,
            element.attr("placeholder"));

    appendContext(
            semanticContext,
            element.attr("aria-label"));

    appendContext(
            semanticContext,
            element.attr("title"));

    appendContext(
            semanticContext,
            element.attr("role"));

    appendContext(
            semanticContext,
            element.attr("data-test"));

    appendContext(
            semanticContext,
            element.attr("data-testid"));

    appendContext(
            semanticContext,
            element.attr("data-qa"));

    appendContext(
            semanticContext,
            element.attr("data-cy"));

    /*
     * Parent context is useful when the element
     * itself has weak semantic information.
     */
    Element parent =
            element.parent();

    if (parent != null) {

        appendContext(
                semanticContext,
                parent.id());

        appendContext(
                semanticContext,
                parent.className());

        appendContext(
                semanticContext,
                parent.attr("role"));

        appendContext(
                semanticContext,
                parent.attr("aria-label"));

        appendContext(
                semanticContext,
                parent.attr("data-test"));

        appendContext(
                semanticContext,
                parent.attr("data-testid"));
    }

    // ==========================================
    // CALCULATE CONTEXT-DRIVEN SCORE
    // ==========================================

    double score = 0;

    /*
     * Signal 1:
     * Variable name versus visible text.
     *
     * Example:
     *
     * productTitle -> "Product Details"
     *
     * cartCount -> "3"
     *
     * Both are evaluated using exactly the
     * same generic similarity mechanism.
     */
    if (context.getVariableName() != null
            && !context.getVariableName()
                    .isBlank()) {

        score +=
                SimilarityUtil.score(
                        context.getVariableName(),
                        normalizedText);
    }

    /*
     * Signal 2:
     * Variable tokens versus complete DOM
     * semantic context.
     */
    if (variableInfo != null
            && variableInfo.getTokens() != null
            && !variableInfo.getTokens()
                    .isEmpty()) {

        List<String> candidateTokens =
                TokenParser.parse(
                        semanticContext.toString());

        score +=
                variableScore(
                        variableInfo.getTokens(),
                        candidateTokens);
    }

    /*
     * Signal 3:
     * Original failed locator versus the
     * element's complete semantic context.
     */
    if (context.getFailedLocator() != null
            && !context.getFailedLocator()
                    .isBlank()) {

        score +=
                SimilarityUtil.score(
                        context.getFailedLocator(),
                        semanticContext.toString());
    }

    /*
     * Signal 4:
     * Intent compatibility.
     *
     * This is generic because it comes entirely
     * from FailureContext and DOM analysis.
     */
    ElementIntent actualIntent =
            determineIntent(
                    element);

    if (context.getExpectedIntent() != null
            && context.getExpectedIntent()
                    != ElementIntent.UNKNOWN) {

        if (context.getExpectedIntent()
                == actualIntent) {

            score += 100;

        } else if (actualIntent
                != ElementIntent.UNKNOWN) {

            score -= 100;
        }
    }

    /*
     * Signal 5:
     * Expected tag compatibility.
     */
    if (context.getExpectedTag() != null
            && !context.getExpectedTag()
                    .isBlank()) {

        if (context.getExpectedTag()
                .equalsIgnoreCase(tag)) {

            score += 75;

        } else {

            score -= 25;
        }
    }

    // ==========================================
    // CREATE CANDIDATE
    // ==========================================

    LocatorCandidate scopedCandidate =
            new LocatorCandidate(
                    "xpath",
                    tagTextXpath,
                    tag,
                    element.attr("type"),
                    actualIntent,
                    score,
                    parent == null
                            ? ""
                            : parent.tagName(),
                    parent == null
                            ? ""
                            : parent.className(),
                    parent == null
                            ? ""
                            : parent.id());

    scopedCandidate.setFinalScore(
            score);

            scopedCandidate.setElementText(
        normalizedText);

    candidates.add(
            scopedCandidate);

}


private void addScopedButtonTextCandidate(
        List<LocatorCandidate> candidates,
        Element element,
        String text,
        FailureContext context) {

    if (context == null
            || context.getExpectedIntent()
                    != ElementIntent.BUTTON
            || text == null
            || text.isBlank()) {
        return;
    }

    String tag =
            element.tagName().toLowerCase();

    boolean isButton =
            tag.equals("button")
                    || "button".equalsIgnoreCase(
                            element.attr("type"))
                    || "submit".equalsIgnoreCase(
                            element.attr("type"));

    if (!isButton) {
        return;
    }

    String safeText =
            text.replace("'", "\\'");

    String buttonXpath =
            "//"
                    + tag
                    + "[normalize-space()='"
                    + safeText
                    + "']";

    int matchCount =
            element.ownerDocument()
                    .selectXpath(buttonXpath)
                    .size();

    if (matchCount != 1) {
        return;
    }

    if (exists(
            candidates,
            "xpath",
            buttonXpath,
            element)) {
        return;
    }

double score =
        SimilarityUtil.score(
                context.getVariableName(),
                text);

if (context.getExpectedIntent()
        == ElementIntent.BUTTON) {

    score += 200;
}

    Element parent =
            element.parent();

    LocatorCandidate buttonCandidate =
            new LocatorCandidate(
                    "xpath",
                    buttonXpath,
                    tag,
                    element.attr("type"),
                    ElementIntent.BUTTON,
                    score,
                    parent == null
                            ? ""
                            : parent.tagName(),
                    parent == null
                            ? ""
                            : parent.className(),
                    parent == null
                            ? ""
                            : parent.id());

    buttonCandidate.setFinalScore(score);

    candidates.add(buttonCandidate);


}


private void addScopedLinkTextCandidate(
        List<LocatorCandidate> candidates,
        Element element,
        String text,
        FailureContext context) {

    if (context == null
            || text == null
            || text.isBlank()) {
        return;
    }

    Element link = element;

    if (!link.tagName().equalsIgnoreCase("a")) {
        link = element.closest("a[href]");
    }

    if (link == null) {
        return;
    }

    String href = link.attr("href");

    if (href == null
            || href.isBlank()
            || href.equals("#")
            || href.startsWith("javascript:")) {
        return;
    }

    String childTag =
            element.tagName().toLowerCase();

    String safeHref =
            href.replace("'", "\\'");

    String safeText =
            text.replace("'", "\\'");

    String linkXpath =
            "//a[@href='"
                    + safeHref
                    + "']//"
                    + childTag
                    + "[normalize-space()='"
                    + safeText
                    + "']";

    int matchCount =
            element.ownerDocument()
                    .selectXpath(linkXpath)
                    .size();

    if (matchCount != 1) {
        return;
    }

    if (exists(
            candidates,
            "xpath",
            linkXpath,
            element)) {
        return;
    }

    double score =
        SimilarityUtil.score(
                context.getVariableName(),
                text);

if (context.getExpectedIntent()
        == ElementIntent.LINK) {

    score += 200;
}

    Element parent =
            element.parent();

    LocatorCandidate linkCandidate =
            new LocatorCandidate(
                    "xpath",
                    linkXpath,
                    childTag,
                    "",
                    ElementIntent.LINK,
                    score,
                    parent == null
                            ? ""
                            : parent.tagName(),
                    parent == null
                            ? ""
                            : parent.className(),
                    parent == null
                            ? ""
                            : parent.id());

    linkCandidate.setFinalScore(score);

    candidates.add(linkCandidate);


}
private String findClosestSemanticLabel(Element element) {

    if (element == null) {
        return "";
    }

    // 1. Explicit label[for=id]
    String id = element.id();

    if (id != null && !id.isBlank()) {

        Element linkedLabel =
                element.ownerDocument()
                        .selectFirst(
                                "label[for='"
                                        + id
                                        + "']");

        if (linkedLabel != null
                && !linkedLabel.text().isBlank()) {

            return linkedLabel.text().trim();
        }
    }

    // 2. Input wrapped by label
    Element wrappedLabel =
            element.closest("label");

    if (wrappedLabel != null
            && !wrappedLabel.text().isBlank()) {

        return wrappedLabel.text().trim();
    }

    // 3. Immediate previous sibling
    Element previous =
            element.previousElementSibling();

    if (previous != null) {

        if (previous.tagName()
                .equalsIgnoreCase("label")
                && !previous.text().isBlank()) {

            return previous.text().trim();
        }

        Element nestedLabel =
                previous.selectFirst("label");

        if (nestedLabel != null
                && !nestedLabel.text().isBlank()) {

            return nestedLabel.text().trim();
        }
    }

    // 4. Fieldset / legend
    Element fieldset =
            element.closest("fieldset");

    if (fieldset != null) {

        Element legend =
                fieldset.selectFirst("legend");

        if (legend != null
                && !legend.text().isBlank()) {

            return legend.text().trim();
        }
    }

    // 5. Controlled nearby-container relationship
    Element current =
            element.parent();

    for (int depth = 0;
         depth < 4 && current != null;
         depth++) {

        Element previousContainer =
                current.previousElementSibling();

        if (previousContainer != null) {

            Element label =
                    previousContainer
                            .tagName()
                            .equalsIgnoreCase("label")
                            ? previousContainer
                            : previousContainer
                                    .selectFirst("label");

            if (label != null
                    && !label.text().isBlank()) {

                return label.text().trim();
            }
        }

        current = current.parent();
    }

    return "";
}
private String extractCandidateText(Element element) {

    if (element == null) {
        return "";
    }

    /*
     * ==========================================================
     * 1. Prefer direct text owned by the element
     * ==========================================================
     *
     * Example:
     *
     * <span>
     *     <span>(29)</span>
     *     Records Found
     * </span>
     *
     * element.ownText() =>
     * "Records Found"
     *
     * This is preferred because nested values such as counts,
     * indexes, badges and dynamic numbers should not become part
     * of the semantic text identity.
     */
    String ownText =
            normalizeVisibleText(
                    element.ownText());

    if (!ownText.isBlank()
            && ownText.length() <= 80) {

        return ownText;
    }

    /*
     * ==========================================================
     * 2. Complete text fallback
     * ==========================================================
     *
     * Used when the element itself owns no direct text.
     */
    String fullText =
            normalizeVisibleText(
                    element.text());

    if (!fullText.isBlank()
            && fullText.length() <= 80) {

        String tag =
                element.tagName()
                        .toLowerCase();

        /*
         * Normal text-bearing elements.
         */
        if (isTextBearingTag(tag)) {
            return fullText;
        }

        /*
         * Small containers can represent one meaningful
         * textual UI block.
         */
        if (element.childrenSize() <= 3) {
            return fullText;
        }
    }

    /*
     * ==========================================================
     * 3. Descendant fallback
     * ==========================================================
     *
     * If this element itself has no usable text, inspect
     * descendants for a meaningful TEXT element.
     */
    for (Element descendant :
            element.select("*")) {

        String descendantOwnText =
                normalizeVisibleText(
                        descendant.ownText());

        if (descendantOwnText.isBlank()
                || descendantOwnText.length() > 80) {
            continue;
        }

        ElementIntent intent =
                determineIntent(descendant);

        if (intent == ElementIntent.TEXT) {
            return descendantOwnText;
        }
    }

    return "";
}
private ElementIntent determineIntent(
        Element element) {

    String tag =
            element.tagName();

    String type =
            element.attr("type");

    // BUTTON FIRST

    if(type.equalsIgnoreCase("submit")
            || type.equalsIgnoreCase("button")
            || tag.equalsIgnoreCase("button")) {

        return ElementIntent.BUTTON;
    }

    // INPUT

    if(tag.equalsIgnoreCase("input")
            || tag.equalsIgnoreCase("textarea")) {

        return ElementIntent.INPUT;
    }

    // LINK

    if(tag.equalsIgnoreCase("a")) {

        return ElementIntent.LINK;
    }

    // DROPDOWN

    if(tag.equalsIgnoreCase("select")) {

    return ElementIntent.DROPDOWN;
}

if(tag.equalsIgnoreCase("section")
        || tag.equalsIgnoreCase("article")) {

    return ElementIntent.CONTAINER;
}

if(tag.equalsIgnoreCase("div")) {

    if(!element.ownText().trim().isBlank()) {
        return ElementIntent.TEXT;
    }

    return ElementIntent.CONTAINER;
}

if(tag.equalsIgnoreCase("h1")
        || tag.equalsIgnoreCase("h2")
        || tag.equalsIgnoreCase("h3")
        || tag.equalsIgnoreCase("h4")
        || tag.equalsIgnoreCase("h5")
        || tag.equalsIgnoreCase("h6")) {

    return ElementIntent.TEXT;
}


if (tag.equalsIgnoreCase("span")
        || tag.equalsIgnoreCase("label")
        || tag.equalsIgnoreCase("p")
        || tag.equalsIgnoreCase("td")
        || tag.equalsIgnoreCase("th")
        || tag.equalsIgnoreCase("li")
        || tag.equalsIgnoreCase("option")) {

    return ElementIntent.TEXT;
}

return ElementIntent.UNKNOWN;
}
private String extractLocatorType(
        String locator) {

    locator =
            locator.toLowerCase();

    if(locator.contains("by.id")) {

        return "id";
    }

    if(locator.contains("by.name")) {

        return "name";
    }

    if(locator.contains("by.classname")) {

        return "class";
    }

    if(locator.contains("by.xpath")) {

        return "xpath";
    }

    if(locator.contains("by.cssselector")) {

        return "css";
    }

    return "";
}


private int countExactMatches(
        List<String> variableTokens,
        List<String> candidateTokens) {

   

    Set<String> candidateSet =
        candidateTokens.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

int matches = 0;

for(String variableToken : variableTokens) {

    if(candidateSet.contains(
            variableToken.toLowerCase())) {

        matches++;
    }
}

    return matches;
}

private int tokenMatches(
        String left,
        String right) {

    left =
            left.replaceAll(
                    "([a-z])([A-Z])",
                    "$1 $2");

    right =
            right.replaceAll(
                    "([a-z])([A-Z])",
                    "$1 $2");

    String[] leftTokens =
            left.toLowerCase()
                    .replaceAll("[^a-z0-9]", " ")
                    .split("\\s+");

    String[] rightTokens =
            right.toLowerCase()
                    .replaceAll("[^a-z0-9]", " ")
                    .split("\\s+");

    int matches = 0;

    for(String l : leftTokens) {

        if(l.length() < 3) {
            continue;
        }

        for(String r : rightTokens) {

            if(l.equals(r)) {

                matches++;
            }
        }
    }

    return matches;
}


private boolean exists(
        List<LocatorCandidate> candidates,
        String locatorType,
        String locatorValue,
        Element element) {

    return candidates.stream()
            .anyMatch(candidate ->
                    candidate.getLocatorType()
                            .equalsIgnoreCase(locatorType)
                    && candidate.getLocatorValue()
                            .equalsIgnoreCase(locatorValue)
                    && candidate.getTagName()
                            .equalsIgnoreCase(
                                    element.tagName()));
}

private double variableScore(
        List<String> variableTokens,
        List<String> candidateTokens) {

    double score = 0;

    for (String variable : variableTokens) {

    boolean matched = false;

    for (String candidate : candidateTokens) {

        if (variable.equalsIgnoreCase(candidate)) {

            score += 200;
            matched = true;
            break;

        } else if (!matched &&
        (candidate.contains(variable)
         || variable.contains(candidate))) {

            score += 75;
            matched = true;
            break;
        }
    }
}

    return score;
}
private boolean isUsuallyUnique(String locatorType) {

    return locatorType.equalsIgnoreCase("id")
            || locatorType.equalsIgnoreCase("data-test")
            || locatorType.equalsIgnoreCase("data-testid")
            || locatorType.equalsIgnoreCase("data-qa")
            || locatorType.equalsIgnoreCase("data-cy");
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

private void appendContext(
        StringBuilder builder,
        String value) {

    if (value == null
            || value.isBlank()) {

        return;
    }

    if (builder.length() > 0) {
        builder.append(" ");
    }

    builder.append(
            value.trim());
}
private void populateLocatorUniqueness(
        Document document,
        List<LocatorCandidate> candidates) {

    for (LocatorCandidate candidate : candidates) {

        int occurrenceCount =
                countLocatorOccurrences(
                        document,
                        candidate);

        candidate.setOccurrenceCount(
                occurrenceCount);

        candidate.setUniqueLocator(
                occurrenceCount == 1);

    }
}
private int countLocatorOccurrences(
        Document document,
        LocatorCandidate candidate) {

    if (document == null
            || candidate == null
            || candidate.getLocatorType() == null
            || candidate.getLocatorValue() == null
            || candidate.getLocatorValue().isBlank()) {

        return 0;
    }

    String type =
            candidate.getLocatorType()
                    .toLowerCase();

    String value =
            candidate.getLocatorValue();

    try {

        switch (type) {

                case "href":
    return document
            .select(
                    "[href="
                            + cssAttributeValue(value)
                            + "]")
            .size();

case "role":
    return document
            .select(
                    "[role="
                            + cssAttributeValue(value)
                            + "]")
            .size();

case "title":
    return document
            .select(
                    "[title="
                            + cssAttributeValue(value)
                            + "]")
            .size();

            case "id":
                return document
                        .select(
                                "[id="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "name":
                return document
                        .select(
                                "[name="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "placeholder":
                return document
                        .select(
                                "[placeholder="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "aria-label":
                return document
                        .select(
                                "[aria-label="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "data-test":
                return document
                        .select(
                                "[data-test="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "data-testid":
                return document
                        .select(
                                "[data-testid="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "data-qa":
                return document
                        .select(
                                "[data-qa="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "data-cy":
                return document
                        .select(
                                "[data-cy="
                                        + cssAttributeValue(value)
                                        + "]")
                        .size();

            case "class":

                String[] classes =
                        value.trim()
                                .split("\\s+");

                StringBuilder selector =
                        new StringBuilder();

                for (String className : classes) {

                    if (!className.isBlank()) {

                        selector.append(".")
                                .append(className);
                    }
                }

                if (selector.length() == 0) {
                    return 0;
                }

                return document
                        .select(selector.toString())
                        .size();

            case "text":

                return (int) document
                        .getAllElements()
                        .stream()
                        .filter(element ->
                                element.ownText()
                                        .trim()
                                        .equals(value.trim()))
                        .count();

            case "xpath":

                return document
                        .selectXpath(value)
                        .size();

            default:
                return 0;
        }

    } catch (Exception e) {

        System.out.println(
                "Unable to calculate locator uniqueness | "
                        + type
                        + "="
                        + value
                        + " | reason="
                        + e.getMessage());

        return 0;
    }
}
private String cssAttributeValue(
        String value) {

    return "\""
            + value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
            + "\"";
}
private boolean shouldProcessElement(
        ElementIntent expected,
        ElementIntent actual,
        Element element) {

    if (expected == null
            || expected == ElementIntent.UNKNOWN) {

        return true;
    }

    switch (expected) {

        case INPUT:

            return actual == ElementIntent.INPUT;

        case BUTTON:

            return actual == ElementIntent.BUTTON;

        case DROPDOWN:

            return actual == ElementIntent.DROPDOWN;

        case LINK:

            return actual == ElementIntent.LINK;

        case TEXT:

            /*
             * TEXT healing is different from INPUT/BUTTON.
             *
             * A textual value may live inside a container:
             *
             * <div>
             *     <span>Records Found</span>
             * </div>
             *
             * Therefore do not reject containers before
             * text extraction.
             */
            return actual == ElementIntent.TEXT
                    || actual == ElementIntent.CONTAINER;

        default:

            return true;
    }
}
private List<LocatorCandidate> removeDuplicateCandidates(
        List<LocatorCandidate> candidates) {

    java.util.Map<String, LocatorCandidate> bestCandidates =
            new java.util.LinkedHashMap<>();

    for (LocatorCandidate candidate : candidates) {

        String key =
                candidate.getLocatorType().toLowerCase()
                        + "|"
                        + candidate.getLocatorValue();

        LocatorCandidate existing =
                bestCandidates.get(key);

        if (existing == null
                || candidate.getFinalScore() > existing.getFinalScore()) {

            bestCandidates.put(key, candidate);
        }
    }

    return new ArrayList<>(bestCandidates.values());
}
private void addCollectionCandidates(
        List<LocatorCandidate> candidates,
        Elements elements,
        FailureContext context) {

    /*
     * Collection candidates are useful mainly for
     * collection/list/table structures.
     *
     * Do not generate them for INPUT/BUTTON/LINK/DROPDOWN
     * healing because broad containers such as #app > div
     * are not valid action targets.
     */
    if (context == null
            || context.getExpectedIntent() == null
            || context.getExpectedIntent()
                    == ElementIntent.UNKNOWN) {

        return;
    }

    if (context.getExpectedIntent()
            != ElementIntent.TEXT) {

        return;
    }

    for (Element element : elements) {

        Elements children =
                element.children();

        if (children.size() < 2) {
            continue;
        }

        String firstTag =
                children.first()
                        .tagName();

        boolean sameTag =
                children.stream()
                        .allMatch(e ->
                                e.tagName()
                                        .equalsIgnoreCase(
                                                firstTag));

        if (!sameTag) {
            continue;
        }

        if (element.id().isBlank()) {
            continue;
        }

        /*
         * Avoid page/root containers such as #app.
         */
        String id =
                element.id()
                        .trim();

        if (id.equalsIgnoreCase("app")
                || id.equalsIgnoreCase("root")
                || id.equalsIgnoreCase("body")
                || id.equalsIgnoreCase("main")) {

            continue;
        }

        LocatorCandidate candidate =
                new LocatorCandidate(
                        "css",
                        "#" + id + " > " + firstTag,
                        firstTag,
                        "",
                        ElementIntent.TEXT,
                        600,
                        element.tagName(),
                        element.className(),
                        id);

        candidate.setFinalScore(600);

        String nearestLabel =
                findClosestSemanticLabel(
                        element);

        candidate.setNearestLabel(
                nearestLabel);

        candidate.setElementText(
                element.text().trim());

        candidate.setPlaceholder(
                element.attr("placeholder"));

        candidate.setAriaLabel(
                element.attr("aria-label"));

        candidate.setId(id);

        candidate.setName(
                element.attr("name"));

        candidate.setParentTag(
                element.tagName());

        candidate.setParentClass(
                element.className());

        candidate.setParentId(id);

        candidates.add(candidate);
    }
}
private String findNearestHeading(Element element) {

    Element current = element.parent();

    while (current != null) {

        Element heading =
                current.selectFirst(
                        "h1,h2,h3,h4,h5,h6");

        if (heading != null
                && !heading.text().isBlank()) {

            return heading.text().trim();
        }

        current = current.parent();
    }

    return "";
}

private String normalizeVisibleText(String value) {

    if (value == null) {
        return "";
    }

    return value
            .replace('\u00A0', ' ')
            .replace('\n', ' ')
            .replace('\r', ' ')
            .replace('\t', ' ')
            .replaceAll("\\s+", " ")
            .trim();
}
private String buildDynamicTextXpath(
        Element element,
        DynamicAttributeResult dynamicResult) {

    if (element == null
            || dynamicResult == null
            || !dynamicResult.isDynamic()) {

        return "";
    }

    String normalizedText =
            dynamicResult.getNormalizedValue();

    if (normalizedText == null
            || normalizedText.isBlank()) {

        return "";
    }

    String tag =
            element.tagName()
                    .toLowerCase();

    String safeText =
            xpathLiteral(normalizedText);

    switch (dynamicResult.getPatternType()) {

        case NUMERIC_PREFIX:

            return "//"
                    + tag
                    + "[contains(normalize-space(), "
                    + safeText
                    + ")]";

        case NUMERIC_SUFFIX:

            return "//"
                    + tag
                    + "[contains(normalize-space(), "
                    + safeText
                    + ")]";

        default:

            return "";
    }
}
private boolean isTextBearingTag(String tag) {

    if (tag == null) {
        return false;
    }

    switch (tag.toLowerCase()) {

        case "span":
        case "label":
        case "p":
        case "td":
        case "th":
        case "li":
        case "option":
        case "h1":
        case "h2":
        case "h3":
        case "h4":
        case "h5":
        case "h6":
            return true;

        default:
            return false;
    }
}
}