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
//import com.vinayak.healing.model.ContextInfo;
import com.vinayak.healing.model.FailureContext;
import java.util.Set;
import java.util.stream.Collectors;
import com.vinayak.healing.util.TokenParser;
import com.vinayak.healing.analyzer.VariableAnalyzer;
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


        private final DomContextAnalyzer analyzer =
        new DomContextAnalyzer();
   private final LocatorAnalyzer locatorAnalyzer =
        new LocatorAnalyzer();

// private final ContextBuilder contextBuilder =
//         new ContextBuilder();

// private final CandidateRanker candidateRanker =
//         new CandidateRanker();

                private final VariableAnalyzer variableAnalyzer =
        new VariableAnalyzer();

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

       

               VariableInfo variableInfo =
        variableAnalyzer.analyze(
                context.getVariableName());

LocatorInfo locatorInfo =
        locatorAnalyzer.analyze(
                failedLocator);         
       

        for (Element element : elements) {


               
            ElementFeature feature =
        extractor.extract(element);

           

          


            

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

String ownText =
        element.ownText().trim();
        if (!ownText.isBlank()) {

    addCandidate(
            candidates,
            failedLocator,
            "text",
            ownText,
            element,
            feature,
            context,
            variableInfo,
            locatorInfo);

    addScopedTextCandidate(
            candidates,
            element,
            ownText,
            context,
            variableInfo);
            
            addScopedButtonTextCandidate(
        candidates,
        element,
        ownText,
        context);

        addScopedLinkTextCandidate(
        candidates,
        element,
        ownText,
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

        populateLocatorUniqueness(
        document,
        candidates);

        candidates.sort(
                Comparator.comparingDouble(
        LocatorCandidate::getFinalScore)
                        .reversed());

     


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

if(context != null
        && context.getExpectedIntent()
        == ElementIntent.BUTTON) {

    if(element.tagName().equalsIgnoreCase("button")
            || "submit".equalsIgnoreCase(element.attr("type"))) {

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

       if(context != null
        && context.getVariableName() != null) {

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

nearestLabel = "";

if(element.tagName().equalsIgnoreCase("input")
        || element.tagName().equalsIgnoreCase("textarea")
        || element.tagName().equalsIgnoreCase("select")) {

   nearestLabel =
        findClosestLabel(element);

    searchableText += " " + nearestLabel;
}

String variableName =
        context.getVariableName();

       List<String> variableTokens =
        variableInfo.getTokens();

        List<String> labelTokens =
        TokenParser.parse(nearestLabel);

int nearestLabelMatches =
        countExactMatches(
                variableTokens,
                labelTokens);




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



candidate.setFinalScore(
        finalScore);

       candidate.setNearestLabel(
        nearestLabel);

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

    labelText = findClosestLabel(
            element);
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



private String findClosestLabel(Element element) {

    if (element == null) {
        return "";
    }

    // 1. Standard HTML relationship:
    // <label for="employee-name">Employee Name</label>
    // <input id="employee-name">
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

Element current = element.parent();

for (int level = 0;
        level < 5 && current != null;
        level++) {

    Element label =
            current.selectFirst("label");

    if (label != null
            && !label.text().isBlank()) {

        return label.text().trim();
    }

    current = current.parent();
}


    Element parent = element.parent();

    if (parent != null) {

        Element previous =
                parent.previousElementSibling();

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
                (candidate.startsWith(variable)
                 || variable.startsWith(candidate))) {

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
}