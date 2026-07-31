package com.vinayak.healing.engine;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.dom.DomCandidateFinder;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.ranking.CandidateRanker;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CollectionHealingEngine {

    private static final int MIN_COLLECTION_SIZE = 2;

    private final DomCandidateFinder candidateFinder =
            new DomCandidateFinder();

    private final CandidateRanker candidateRanker =
            new CandidateRanker();

    public List<WebElement> heal(
            WebDriver driver,
            By failedLocator,
            FailureContext context) {

        if (driver == null
                || failedLocator == null
                || context == null) {

            return List.of();
        }

        long startTime = System.currentTimeMillis();

        System.out.println(
                "\n========== COLLECTION HEALING ==========");

        System.out.println(
                "FAILED COLLECTION LOCATOR = "
                        + failedLocator);

        // ==========================================
        // 1. GENERATE DOM CANDIDATES
        // ==========================================

        List<LocatorCandidate> candidates;

        try {

            candidates =
                    candidateFinder.findCandidates(
                            driver.getPageSource(),
                            failedLocator.toString(),
                            context);

        } catch (Exception exception) {

            System.out.println(
                    "Collection candidate generation failed : "
                            + exception.getMessage());

            return List.of();
        }

        if (candidates == null
                || candidates.isEmpty()) {

            System.out.println(
                    "No collection candidates generated");

            return List.of();
        }

        // ==========================================
        // 2. RANK CANDIDATES
        // ==========================================

        try {

            candidates =
                    candidateRanker.rank(
                            context,
                            candidates);

        } catch (Exception exception) {

            System.out.println(
                    "Collection candidate ranking failed : "
                            + exception.getMessage());

            return List.of();
        }

        // ==========================================
        // 3. TEST COLLECTION CANDIDATES
        // ==========================================

        CollectionMatch bestMatch = null;

        for (LocatorCandidate candidate : candidates) {

            if (candidate == null) {
                continue;
            }

            By candidateLocator;

            try {

                candidateLocator =
                        LocatorBuilder.build(
                                candidate);

                               

            } catch (Exception exception) {

                continue;
            }



            if (candidateLocator == null) {
                continue;
            }

            List<WebElement> matches;

            try {

                matches =
                        driver.findElements(
                                candidateLocator);

            } catch (Exception exception) {

                continue;
            }

            // Must actually represent a collection.
            if (matches == null
                    || matches.size()
                    < MIN_COLLECTION_SIZE) {

                continue;
            }

            long displayedCount =
                    matches.stream()
                            .filter(this::isDisplayed)
                            .count();

            if (displayedCount
                    < MIN_COLLECTION_SIZE) {

                continue;
            }

            if (!isCollectionCapableLocator(candidate)) {

    System.out.println(
            "COLLECTION REJECTED | "
                    + candidate.getLocatorType()
                    + "="
                    + candidate.getLocatorValue()
                    + " | reason=single-element locator type");

    continue;
}

            // ==========================================
            // COLLECTION SEMANTIC SCORE
            // ==========================================

            double collectionIdentityScore =
                    calculateCollectionIdentityScore(
                            context,
                            candidate);

            /*
             * Reject candidates with no relationship
             * to the Page Object collection variable.
             *
             * Example:
             *
             * checkoutItems
             *
             * cart_item               -> valid
             * inventory-item          -> possible
             * Checkout: Overview      -> reject
             */
            if (collectionIdentityScore <= 0) {

                System.out.println(
                        "COLLECTION REJECTED | "
                                + candidate.getLocatorType()
                                + "="
                                + candidate.getLocatorValue()
                                + " | reason=no collection identity");

                continue;
            }

            // ==========================================
            // STRUCTURAL SCORE
            // ==========================================

            double structuralScore =
                    calculateStructuralScore(
                            matches);

            double collectionScore =
                    candidate.getFinalScore()
                            + collectionIdentityScore
                            + structuralScore;

            System.out.println(
                    "COLLECTION CANDIDATE | "
                            + candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue()
                            + " | matches="
                            + matches.size()
                            + " | displayed="
                            + displayedCount
                            + " | rankScore="
                            + candidate.getFinalScore()
                            + " | identity="
                            + collectionIdentityScore
                            + " | structure="
                            + structuralScore
                            + " | collectionScore="
                            + collectionScore);

            if (bestMatch == null
                    || collectionScore
                    > bestMatch.collectionScore) {

                bestMatch =
                        new CollectionMatch(
                                candidate,
                                candidateLocator,
                                matches,
                                collectionScore);
            }
        }

        // ==========================================
        // 4. NO SAFE COLLECTION
        // ==========================================

        if (bestMatch == null) {

            System.out.println(
                    "No safe collection locator found");

            return List.of();
        }

        CollectionDecisionEngine decisionEngine =
        new CollectionDecisionEngine();

CollectionDecisionEngine.Result decision =
        decisionEngine.decide(
                bestMatch.candidate,
                bestMatch.collectionScore,
                bestMatch.elements.size(),
                (int) bestMatch.elements.stream()
                        .filter(this::isDisplayed)
                        .count(),
                true,
                calculateScoreGap(candidates, bestMatch.candidate),
                calculateSemanticSignals(
                        context,
                        bestMatch.candidate));

System.out.println(
        "COLLECTION DECISION = "
                + decision.decision());

System.out.println(
        "CONFIDENCE = "
                + decision.confidence());

if (decision.decision()
        == CollectionDecisionEngine.Decision.REJECT) {

    System.out.println(
            "Collection rejected by decision engine.");

    return List.of();
}

        // ==========================================
        // 5. RETURN BEST COLLECTION
        // ==========================================

        System.out.println(
                "\nCOLLECTION HEAL SUCCESS");

        System.out.println(
                "HEALED COLLECTION LOCATOR = "
                        + bestMatch.locator);

        System.out.println(
                "ELEMENT COUNT = "
                        + bestMatch.elements.size());

        System.out.println(
                "COLLECTION SCORE = "
                        + bestMatch.collectionScore);

                     

HealingAnalytics.deterministicHeal();

HealingAnalytics.addHealingTime(
        System.currentTimeMillis() - startTime);

        return bestMatch.elements;
    }

    // ==========================================
    // COLLECTION IDENTITY
    // ==========================================

    private double calculateCollectionIdentityScore(
            FailureContext context,
            LocatorCandidate candidate) {

        if (context.getVariableName() == null
                || context.getVariableName().isBlank()) {

            return 0;
        }

        String variable =
                normalize(
                        context.getVariableName());

        String locatorValue =
                normalize(
                        candidate.getLocatorValue());

        if (locatorValue.isBlank()) {
            return 0;
        }

        String[] variableTokens =
                variable.split("\\s+");

        double score = 0;

        for (String token : variableTokens) {

            if (isGenericCollectionToken(token)) {
                continue;
            }

            if (token.length() < 3) {
                continue;
            }

            if (locatorValue.contains(token)) {
                score += 150;
            }

            /*
             * Domain relationship:
             *
             * checkoutItems
             * cart_item
             *
             * "checkout" and "cart" represent
             * the same business collection context.
             */
            if (isRelatedCollectionToken(
                    token,
                    locatorValue)) {

                score += 120;
            }
        }

        /*
         * Prefer locator values that structurally
         * look like repeated item containers.
         */
        if (locatorValue.contains("item")
                || locatorValue.contains("row")
                || locatorValue.contains("card")
                || locatorValue.contains("product")) {

            score += 100;
        }

        return score;
    }

    private boolean isRelatedCollectionToken(
            String variableToken,
            String locatorValue) {

        if (variableToken.equals("checkout")) {

            return locatorValue.contains("cart")
                    || locatorValue.contains("inventory");
        }

        if (variableToken.equals("cart")) {

            return locatorValue.contains("checkout")
                    || locatorValue.contains("inventory");
        }

        if (variableToken.equals("product")) {

            return locatorValue.contains("inventory")
                    || locatorValue.contains("item");
        }

        return false;
    }

    private boolean isGenericCollectionToken(
            String token) {

        return token.equals("items")
                || token.equals("item")
                || token.equals("elements")
                || token.equals("element")
                || token.equals("list")
                || token.equals("rows")
                || token.equals("row")
                || token.equals("cards")
                || token.equals("card")
                || token.equals("collection");
    }

    // ==========================================
    // STRUCTURAL CONSISTENCY
    // ==========================================

    private double calculateStructuralScore(
            List<WebElement> elements) {

        if (elements == null
                || elements.size()
                < MIN_COLLECTION_SIZE) {

            return 0;
        }

        String firstTag =
                safeTagName(
                        elements.get(0));

        if (firstTag.isBlank()) {
            return 0;
        }

        long sameTagCount =
                elements.stream()
                        .filter(element ->
                                firstTag.equalsIgnoreCase(
                                        safeTagName(element)))
                        .count();

        double consistency =
                (double) sameTagCount
                        / elements.size();

        if (consistency == 1.0) {
            return 150;
        }

        if (consistency >= 0.75) {
            return 75;
        }

        return 0;
    }

    private String safeTagName(
            WebElement element) {

        try {

            return element == null
                    ? ""
                    : element.getTagName();

        } catch (Exception exception) {

            return "";
        }
    }

    private boolean isDisplayed(
            WebElement element) {

        try {

            return element != null
                    && element.isDisplayed();

        } catch (Exception exception) {

            return false;
        }
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2")
                .replaceAll(
                        "[^a-zA-Z0-9]+",
                        " ")
                .replaceAll(
                        "\\s+",
                        " ")
                .trim()
                .toLowerCase();
    }

    private static class CollectionMatch {

        private final LocatorCandidate candidate;

        private final By locator;

        private final List<WebElement> elements;

        private final double collectionScore;

        private CollectionMatch(
                LocatorCandidate candidate,
                By locator,
                List<WebElement> elements,
                double collectionScore) {

            this.candidate = candidate;

            this.locator = locator;

            this.elements =
                    new ArrayList<>(
                            elements);

            this.collectionScore =
                    collectionScore;
        }
    }
    private boolean isCollectionCapableLocator(
        LocatorCandidate candidate) {

    if (candidate == null
            || candidate.getLocatorType() == null) {

        return false;
    }

    String type =
            candidate.getLocatorType()
                    .trim()
                    .toLowerCase();

    String value =
            candidate.getLocatorValue() == null
                    ? ""
                    : candidate.getLocatorValue()
                            .trim()
                            .toLowerCase();

    /*
     * Exact text locators normally identify
     * semantic single elements such as:
     *
     * Checkout: Overview
     * Sauce Labs Backpack
     * Finish
     *
     * They must not heal a collection locator.
     */
    if (type.equals("text")) {
        return false;
    }

    /*
     * Exact-text XPath is also a single-element
     * semantic locator, even if Selenium happens
     * to return multiple matches.
     */
    if (type.equals("xpath")
            && (value.contains("normalize-space()")
                || value.contains("text()"))) {

        return false;
    }

    return true;
}
private double calculateScoreGap(
        List<LocatorCandidate> candidates,
        LocatorCandidate best) {

    if (best == null || candidates == null) {
        return 0;
    }

    double secondBest = Double.NEGATIVE_INFINITY;

    for (LocatorCandidate candidate : candidates) {

        if (candidate == null || candidate == best) {
            continue;
        }

        secondBest = Math.max(
                secondBest,
                candidate.getFinalScore());
    }

    if (secondBest == Double.NEGATIVE_INFINITY) {
        return best.getFinalScore();
    }

    return best.getFinalScore() - secondBest;
}

private int calculateSemanticSignals(
        FailureContext context,
        LocatorCandidate candidate) {

    int signals = 0;

    String variable =
            normalize(context.getVariableName());

    String locator =
            normalize(candidate.getLocatorValue());

    if (!variable.isBlank()
            && locator.contains(variable)) {

        signals++;
    }

    if (candidate.getIntent()
            == context.getExpectedIntent()) {

        signals++;
    }

    if (context.getExpectedTag() != null
            && context.getExpectedTag()
                    .equalsIgnoreCase(
                            candidate.getTagName())) {

        signals++;
    }

    return signals;
}
}