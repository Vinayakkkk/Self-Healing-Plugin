package com.vinayak.healing.engine;

import com.vinayak.healing.analytics.HealingAnalytics;
import com.vinayak.healing.decision.SemanticEvidence;
import com.vinayak.healing.decision.SemanticEvidenceEvaluator;
import com.vinayak.healing.dom.DomCandidateFinder;
import com.vinayak.healing.learning.LearningEngine;
import com.vinayak.healing.learning.LearningRecorder;
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

            private final SemanticEvidenceEvaluator
        semanticEvidenceEvaluator =
                new SemanticEvidenceEvaluator();

    /*
     * =====================================================
     * LEARNING
     * =====================================================
     *
     * Collection healing uses the same learning mechanism
     * as the rest of the framework.
     *
     * Collection healing is recorded as historical evidence,
     * but it is NOT eligible for the single-element cache.
     */
    private final LearningRecorder learningRecorder;

    public CollectionHealingEngine(
        LearningEngine learningEngine) {

    if (learningEngine == null) {
        throw new IllegalArgumentException(
                "LearningEngine cannot be null.");
    }

    this.learningRecorder =
            new LearningRecorder(learningEngine);
}

    public List<WebElement> heal(
            WebDriver driver,
            By failedLocator,
            FailureContext context) {

        if (driver == null
                || failedLocator == null
                || context == null) {

            return List.of();
        }

        long startTime =
                System.currentTimeMillis();

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
                candidates,
                true);

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

           double learningScore =
        candidateRanker.calculateLearningScore(
                context,
                candidate,
                true);

double collectionScore =
        candidate.getFinalScore()
                + collectionIdentityScore
                + structuralScore
                + learningScore;

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
                + " | learning="
                + learningScore
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

      double learningScore =
        candidateRanker.calculateLearningScore(
                context,
                bestMatch.candidate,
                true);



                int displayedCount =
        (int) bestMatch.elements.stream()
                .filter(WebElement::isDisplayed)
                .count();

                 SemanticEvidence semanticEvidence =
        semanticEvidenceEvaluator.evaluate(
                bestMatch.candidate,
                context);

                int semanticSignals =
        semanticEvidence.getSignalCount();

        System.out.println(
        "COLLECTION SEMANTIC EVIDENCE"
                + " | signals="
                + semanticSignals
                + " | "
                + semanticEvidence);

CollectionDecisionEngine.Result decision =
        decisionEngine.decide(
                bestMatch.candidate,
                bestMatch.collectionScore,
                bestMatch.elements.size(),
                displayedCount,
                true,
                calculateScoreGap(
                        candidates,
                        bestMatch.candidate),
                semanticSignals,
                learningScore);

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

        /*
         * =====================================================
         * LEARNING RECORD
         * =====================================================
         *
         * The collection healing has now been successfully
         * validated by the CollectionDecisionEngine.
         *
         * Record it as historical evidence.
         *
         * IMPORTANT:
         *
         * cacheAllowed = false
         *
         * because this is a collection locator and must not
         * enter the single-element locator cache path.
         */
        try {

            String pageObjectClass =
                    extractPageObjectClass(
                            context.getPageObjectPath());

         boolean learningRecorded =
        learningRecorder.record(
                context,
                pageObjectClass,
                bestMatch.candidate,
                bestMatch.locator,
                confidenceLevel(
                        decision.confidence()),
                true,
                false,
                "COLLECTION",
                true,
                decision.confidence());

if (learningRecorded) {

    System.out.println(
            "COLLECTION LEARNING RECORDED");

} else {

    System.out.println(
            "COLLECTION LEARNING SKIPPED");
}

        } catch (Exception exception) {

            /*
             * Learning must never break a successful healing.
             *
             * The collection has already been safely healed,
             * therefore a learning-recording problem is isolated.
             */
            System.out.println(
                    "COLLECTION LEARNING RECORDING FAILED | "
                            + exception.getMessage());
        }

        HealingAnalytics.deterministicHeal();

        HealingAnalytics.addHealingTime(
                System.currentTimeMillis()
                        - startTime);

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

    // ==========================================
    // LEARNING HELPERS
    // ==========================================

    /**
     * Converts the numeric decision confidence into
     * the confidence level expected by LearningRecorder.
     *
     * This does not affect the numeric confidence stored
     * in the LearningRecord.
     */
    private String confidenceLevel(
            double confidence) {

        if (confidence >= 80.0) {
            return "HIGH";
        }

        if (confidence >= 50.0) {
            return "MEDIUM";
        }

        if (confidence > 0.0) {
            return "LOW";
        }

        return "UNKNOWN";
    }

    /**
     * Extracts the Page Object class name from the
     * Page Object path stored in FailureContext.
     *
     * Examples:
     *
     * /src/test/java/pages/CheckoutPage.java
     *        -> CheckoutPage
     *
     * pages.CheckoutPage
     *        -> CheckoutPage
     */
    private String extractPageObjectClass(
            String pageObjectPath) {

        if (pageObjectPath == null
                || pageObjectPath.isBlank()) {

            return "UNKNOWN";
        }

        String normalized =
                pageObjectPath
                        .replace('\\', '/')
                        .trim();

        int slash =
                normalized.lastIndexOf('/');

        if (slash >= 0) {

            normalized =
                    normalized.substring(
                            slash + 1);
        }

        if (normalized.endsWith(".java")) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 5);
        }

        int dot =
                normalized.lastIndexOf('.');

        if (dot >= 0
                && dot < normalized.length() - 1) {

            normalized =
                    normalized.substring(
                            dot + 1);
        }

        return normalized.isBlank()
                ? "UNKNOWN"
                : normalized;
    }

    // ==========================================
    // COLLECTION CAPABILITY
    // ==========================================

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

    // ==========================================
    // SCORE GAP
    // ==========================================

    private double calculateScoreGap(
            List<LocatorCandidate> candidates,
            LocatorCandidate best) {

        if (best == null || candidates == null) {
            return 0;
        }

        double secondBest =
                Double.NEGATIVE_INFINITY;

        for (LocatorCandidate candidate :
                candidates) {

            if (candidate == null
                    || candidate == best) {

                continue;
            }

            secondBest =
                    Math.max(
                            secondBest,
                            candidate.getFinalScore());
        }

        if (secondBest
                == Double.NEGATIVE_INFINITY) {

            return best.getFinalScore();
        }

        return best.getFinalScore()
                - secondBest;
    }

    // ==========================================
    // SEMANTIC SIGNALS
    // ==========================================



    // ==========================================
    // COLLECTION MATCH
    // ==========================================

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
}