package com.vinayak.healing.builder;

import com.vinayak.healing.model.FailureContext;

public class ExpectedOutcomeContextBuilder {

    public void enrich(FailureContext context) {

        if (context == null) {
            return;
        }

        inferExpectedOutcomeAction(context);

        inferExpectedUrl(context);

        inferExpectedElements(context);

        inferExpectedText(context);

        inferExpectedLabel(context);
    }

    private void inferExpectedOutcomeAction(
            FailureContext context) {

    }

    private void inferExpectedUrl(
            FailureContext context) {

    }

    private void inferExpectedElements(
            FailureContext context) {

    }

    private void inferExpectedText(
            FailureContext context) {

    }

    private void inferExpectedLabel(
            FailureContext context) {

    }
}