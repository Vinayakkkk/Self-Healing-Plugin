package com.vinayak.healing.pipeline;
import java.util.List;
import com.vinayak.healing.validator.CandidateValidator;
import com.vinayak.healing.analyzer.LocatorAnalyzer;
import com.vinayak.healing.analyzer.VariableAnalyzer;
import com.vinayak.healing.dom.DomCandidateFinder;
import com.vinayak.healing.execution.ExecutionAnalyzer;
import com.vinayak.healing.execution.ExecutionContext;
import com.vinayak.healing.filter.CandidateFilter;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;
import com.vinayak.healing.model.LocatorInfo;
import com.vinayak.healing.model.VariableInfo;
import com.vinayak.healing.ranking.CandidateRanker;

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

    private final CandidateRanker candidateRanker =
            new CandidateRanker();
            private final CandidateFilter candidateFilter =
        new CandidateFilter();

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
    // STEP 4 - DOM CANDIDATE DISCOVERY
    // ======================================

    List<LocatorCandidate> candidates =
            domCandidateFinder.findCandidates(
                    failureContext.getPageSource(),
                    failureContext.getFailedLocator(),
                    failureContext);

    System.out.println(
            "\n===== CANDIDATE SUMMARY =====");

    System.out.println(
            "Candidates Found : "
                    + candidates.size());

    // ======================================
    // STEP 5 - CANDIDATE RANKING
    // ======================================

    candidates =
            candidateRanker.rank(
                    failureContext,
                    candidates);

    // CandidateRanker already prints Top 10.
    // Do not print all candidates again here.

    // ======================================
    // STEP 6 - CANDIDATE FILTERING
    // ======================================

   



    candidates =
            candidateFilter.filter(
                    failureContext,
                    candidates);


    result.setCandidates(
            candidates);

    // ======================================
    // STEP 7 - CANDIDATE VALIDATION
    // ======================================
System.out.println("\n===== FINAL CANDIDATES =====");

for (LocatorCandidate c : candidates) {

    System.out.println(
            c.getLocatorType()
            + "="
            + c.getLocatorValue()
            + " | score="
            + c.getFinalScore());
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
                        + validatedCandidate.getLocatorType()
                        + "="
                        + validatedCandidate.getLocatorValue());

        System.out.println(
                "Score               : "
                        + validatedCandidate.getFinalScore());

        System.out.println(
                "Matches             : "
                        + validatedCandidate.getOccurrenceCount());

        System.out.println(
                "Unique              : "
                        + validatedCandidate.isUniqueLocator());

    } else {

        System.out.println(
                "No candidate validated.");
    }

    return result;
}
}