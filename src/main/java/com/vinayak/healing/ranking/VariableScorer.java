package com.vinayak.healing.ranking;

import com.vinayak.healing.model.ContextFeature;
import com.vinayak.healing.model.ElementFeature;
import com.vinayak.healing.model.VariableInfo;

import java.util.HashSet;
import java.util.Set;

public class VariableScorer implements Scorer {

    private static final Set<String> GENERIC_TOKENS = Set.of(
            "button",
            "btn",
            "icon",
            "input",
            "field",
            "text",
            "label",
            "link",
            "menu",
            "item",
            "element",
            "container",
            "wrapper",
            "div",
            "span",
            "form",
            "oxd"
    );

    @Override
    public double score(ContextFeature context) {

        if (context == null) {
            return 0;
        }

        VariableInfo variable = context.getVariableInfo();
        ElementFeature feature = context.getElementFeature();

        if (variable == null || feature == null) {
            return 0;
        }

        Set<String> variableTokens =
                new HashSet<>(variable.getTokens());

        Set<String> candidateTokens =
                new HashSet<>(feature.getTokens());

        variableTokens.removeAll(GENERIC_TOKENS);
        candidateTokens.removeAll(GENERIC_TOKENS);

        if (variableTokens.isEmpty()
                || candidateTokens.isEmpty()) {
            return 0;
        }

        int matches = 0;

        for (String token : variableTokens) {
            if (candidateTokens.contains(token)) {
                matches++;
            }
        }

        double identity =
                (double) matches / variableTokens.size();

        if (identity == 1.0) {
            return 250;
        }

        if (identity >= 0.75) {
            return 180;
        }

        if (identity >= 0.50) {
            return 120;
        }

        if (identity >= 0.25) {
            return 60;
        }

        return 0;
    }
}