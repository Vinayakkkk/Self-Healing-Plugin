package com.vinayak.healing.test;

import java.util.ArrayList;
import java.util.List;

import com.vinayak.healing.filter.CandidateFilter;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

public class TestCandidateFilter {

    public static void main(String[] args) {

        List<LocatorCandidate> candidates =
                new ArrayList<>();

        candidates.add(create("name", "username", 1662));
        candidates.add(create("placeholder", "Username", 1342));
        candidates.add(create("class", "oxd-input", 1449));
        candidates.add(create("text", "Username", 612));
        candidates.add(create("text", "Username : Admin", 587));

        candidates.add(create("name", "password", -550));
        candidates.add(create("class", "login-button", -600));
        candidates.add(create("href", "facebook", -780));

        System.out.println("================================");
        System.out.println("BEFORE FILTER");
        System.out.println("================================");

        for (LocatorCandidate candidate : candidates) {

            System.out.println(
                    candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue()
                            + " Score="
                            + candidate.getFinalScore());
        }

     CandidateFilter filter =
        new CandidateFilter();

FailureContext context =
        new FailureContext();

List<LocatorCandidate> filtered =
        filter.filter(
                context,
                candidates);

        System.out.println();
        System.out.println("================================");
        System.out.println("AFTER FILTER");
        System.out.println("================================");

        for (LocatorCandidate candidate : filtered) {

            System.out.println(
                    candidate.getLocatorType()
                            + "="
                            + candidate.getLocatorValue()
                            + " Score="
                            + candidate.getFinalScore());
        }
    }

    private static LocatorCandidate create(
        String type,
        String value,
        double score) {

    LocatorCandidate candidate =
            new LocatorCandidate(
                    type,                   // locatorType
                    value,                  // locatorValue
                    "",                     // tagName
                    "",                     // inputType
                    null,                   // intent
                    score,                  // score
                    "",                     // parentTag
                    "",                     // parentClass
                    ""                      // parentId
            );

    candidate.setFinalScore(score);

    return candidate;
}
}