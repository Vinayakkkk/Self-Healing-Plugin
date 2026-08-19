package com.vinayak.healing.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.execution.ExecutionAnalyzer;
import com.vinayak.healing.analyzer.LocatorAnalyzer;
import com.vinayak.healing.analyzer.VariableAnalyzer;
import com.vinayak.healing.dom.DomCandidateFinder;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.expected.ExpectedContext;
import com.vinayak.healing.expected.ExpectedContextManager;
import com.vinayak.healing.filter.CandidateFilter;
import com.vinayak.healing.generator.ContextAwareLocatorGenerator;
import com.vinayak.healing.intent.ElementIntent;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.model.LocatorInfo;
import com.vinayak.healing.model.VariableInfo;
import com.vinayak.healing.ranking.CandidateRanker;
import com.vinayak.healing.validator.CandidateValidator;


public class HealingPipeline {


    private final VariableAnalyzer variableAnalyzer =
            new VariableAnalyzer();


    private final CandidateValidator candidateValidator =
            new CandidateValidator();


    private final LocatorAnalyzer locatorAnalyzer =
            new LocatorAnalyzer();


    private final ExecutionAnalyzer executionAnalyzer =
            new ExecutionAnalyzer();


    private final DomCandidateFinder domCandidateFinder =
            new DomCandidateFinder();


    private final ContextAwareLocatorGenerator
            contextAwareLocatorGenerator =
            new ContextAwareLocatorGenerator();


    private final CandidateRanker candidateRanker =
            new CandidateRanker();


    private final CandidateFilter candidateFilter =
            new CandidateFilter();


    private final ExpectedContextManager expectedContextManager =
            new ExpectedContextManager();


    public PipelineResult execute(
            FailureContext failureContext) {


        PipelineResult result =
                new PipelineResult();


        result.setFailureContext(
                failureContext);


        // ======================================
        // STEP 1 - VARIABLE ANALYSIS
        // ======================================

        VariableInfo variableInfo =
                variableAnalyzer.analyze(
                        failureContext.getVariableName());


        result.setVariableInfo(
                variableInfo);


        // ======================================
        // STEP 2 - LOCATOR ANALYSIS
        // ======================================

        LocatorInfo locatorInfo =
                locatorAnalyzer.analyze(
                        failureContext.getFailedLocator());


        result.setLocatorInfo(
                locatorInfo);


        // ======================================
        // STEP 3 - EXECUTION ANALYSIS
        // ======================================

        ExecutionContext executionContext =
                executionAnalyzer.analyze(
                        failureContext.getExecutionContext());


        result.setExecutionContext(
                executionContext);


        // ======================================
        // STEP 4 - EXPECTED CONTEXT RESOLUTION
        // ======================================

        ExpectedContext expectedContext =
                expectedContextManager.resolve(
                        failureContext,
                        executionContext);

                        failureContext.setExpectedContext(
        expectedContext);


        if (expectedContext != null) {

                if (expectedContext.getExpectedLabel() != null
        && !expectedContext
                .getExpectedLabel()
                .isBlank()) {

    failureContext.setExpectedLabel(
            expectedContext.getExpectedLabel());
}


            System.out.println(
                    "\n===== EXPECTED CONTEXT =====");


            System.out.println(
                    "Expected Text   : "
                            + expectedContext.getExpectedText());


            System.out.println(
                    "Expected Tag    : "
                            + expectedContext.getExpectedTag());


            System.out.println(
                    "Expected Intent : "
                            + expectedContext.getExpectedIntent());


            System.out.println(
                    "Expected Label  : "
                            + expectedContext.getExpectedLabel());


            System.out.println(
                    "Expected Role   : "
                            + expectedContext.getExpectedRole());


            System.out.println(
                    "Expected Page   : "
                            + expectedContext.getExpectedPage());


            System.out.println(
                    "Confidence      : "
                            + expectedContext.getConfidence());


            /*
             * ======================================
             * EXPECTED INTENT PROPAGATION
             * ======================================
             *
             * ExpectedContext is the resolved semantic
             * context.
             *
             * CandidateRanker and CandidateFilter
             * operate using FailureContext.
             *
             * Therefore the resolved intent MUST be
             * copied back into FailureContext.
             */

            ElementIntent resolvedIntent =
                    expectedContext.getExpectedIntent();


            if (resolvedIntent != null
                    && resolvedIntent
                            != ElementIntent.UNKNOWN) {


                failureContext.setExpectedIntent(
                        resolvedIntent);


                System.out.println(
                        "PROPAGATED ExpectedIntent : "
                                + failureContext
                                        .getExpectedIntent());
            }


            /*
             * ======================================
             * EXPECTED TEXT PROPAGATION
             * ======================================
             */

            if (expectedContext.getExpectedText() != null
                    && !expectedContext
                            .getExpectedText()
                            .isBlank()) {


                failureContext.setExpectedText(
                        expectedContext.getExpectedText());
            }


            /*
             * ======================================
             * EXPECTED TAG PROPAGATION
             * ======================================
             */

            if (expectedContext.getExpectedTag() != null
                    && !expectedContext
                            .getExpectedTag()
                            .isBlank()) {


                failureContext.setExpectedTag(
                        expectedContext.getExpectedTag());
            }


            System.out.println(
                    "FailureContext ExpectedIntent : "
                            + failureContext
                                    .getExpectedIntent());


            System.out.println(
                    "============================");
        }


        // ======================================
        // STEP 5 - DOM CANDIDATE DISCOVERY
        // ======================================

        List<LocatorCandidate> candidates =
                domCandidateFinder.findCandidates(
                        failureContext.getPageSource(),
                        failureContext.getFailedLocator(),
                        failureContext);
// ======================================
// EXPECTED INTENT FROM DOM CANDIDATES
// ======================================

if (failureContext.getExpectedIntent() == null
        || failureContext.getExpectedIntent()
                == ElementIntent.UNKNOWN) {

    ElementIntent candidateIntent =
            resolveIntentFromCandidates(
                    candidates);

    if (candidateIntent != null
            && candidateIntent != ElementIntent.UNKNOWN) {

        failureContext.setExpectedIntent(
                candidateIntent);

        System.out.println(
                "[HEALING DEBUG] EXPECTED INTENT DERIVED FROM DOM = "
                        + candidateIntent);
    }
}

        /*
         * ======================================
         * CONTEXT-AWARE LOCATOR GENERATION
         * ======================================
         */

        List<LocatorCandidate> generated =
                new ArrayList<>();


        for (LocatorCandidate candidate :
                candidates) {


            generated.addAll(
                    contextAwareLocatorGenerator.generate(
                            failureContext,
                            candidate));
        }


        candidates.addAll(
                generated);


        System.out.println(
                "\n===== GENERATED LOCATORS =====");


        for (LocatorCandidate candidate :
                generated) {


            System.out.println(
                    candidate.getGenerationStrategy()
                            + " -> "
                            + candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue());
        }


        System.out.println(
                "\n===== CANDIDATE SUMMARY =====");


        System.out.println(
                "Candidates Found : "
                        + candidates.size());


        // ======================================
        // STEP 6 - CANDIDATE RANKING
        // ======================================

        candidates = candidateRanker.rank(
        failureContext,
        candidates,
        false);


        // ======================================
        // STEP 7 - CANDIDATE FILTERING
        // ======================================

        candidates =
                candidateFilter.filter(
                        failureContext,
                        candidates);


        result.setCandidates(
                candidates);


        // ======================================
        // STEP 8 - CANDIDATE VALIDATION
        // ======================================

        System.out.println(
                "\n===== FINAL CANDIDATES =====");


        for (LocatorCandidate candidate :
                candidates) {


            System.out.println(
                    candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue()
                            + " | score="
                            + candidate.getFinalScore());
        }


        LocatorCandidate validatedCandidate =
                candidateValidator.validate(
                        failureContext.getDriver(),
                        candidates,
                        failureContext);


        result.setValidatedCandidate(
                validatedCandidate);


        System.out.println(
                "\n===== VALIDATION RESULT =====");


        if (validatedCandidate != null) {


            System.out.println(
                    "Validated Candidate : "
                            + validatedCandidate
                                    .getLocatorType()
                            + "="
                            + validatedCandidate
                                    .getLocatorValue());


            System.out.println(
                    "Score               : "
                            + validatedCandidate
                                    .getFinalScore());


            System.out.println(
                    "Matches             : "
                            + validatedCandidate
                                    .getOccurrenceCount());


            System.out.println(
                    "Unique              : "
                            + validatedCandidate
                                    .isUniqueLocator());


        } else {


            System.out.println(
                    "No candidate validated.");
        }


        return result;
    }
private ElementIntent resolveIntentFromCandidates(
        List<LocatorCandidate> candidates) {

    if (candidates == null
            || candidates.isEmpty()) {

        return ElementIntent.UNKNOWN;
    }

    /*
     * Candidates have already been analyzed from
     * the actual DOM by DomCandidateFinder.
     *
     * Only use a candidate whose intent is known.
     */
    for (LocatorCandidate candidate : candidates) {

        if (candidate == null) {
            continue;
        }

        ElementIntent intent =
                candidate.getIntent();

        if (intent == null
                || intent == ElementIntent.UNKNOWN) {

            continue;
        }

        return intent;
    }

    return ElementIntent.UNKNOWN;
}
}