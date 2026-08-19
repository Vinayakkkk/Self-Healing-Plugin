package com.vinayak.healing.expected;

import java.util.List;

import com.vinayak.healing.intent.ElementIntent;

public class ExpectedContextResolver {

    private final EvidenceAggregator aggregator =
            new EvidenceAggregator();

    /**
     * Resolves the final ExpectedContext from all
     * collected evidence.
     *
     * The expected intent comes from the failure context
     * and is established before evidence is resolved.
     */
public ExpectedContext resolve(
        List<ExpectedEvidence> evidences,
        ElementIntent expectedIntent) {

    ExpectedContext context =
            new ExpectedContext();

   if (expectedIntent != null
        && expectedIntent != ElementIntent.UNKNOWN) {

    context.setExpectedIntent(
            expectedIntent);
}

    if (evidences == null
            || evidences.isEmpty()) {

        return context;
    }

    /*
     * Store every evidence.
     */
    evidences.forEach(context::addEvidence);

    /*
     * Aggregate evidence.
     */
    List<EvidenceScore> scores =
            aggregator.aggregate(evidences);

            /*
 * If the FailureContext did not provide a reliable
 * semantic intent, allow explicit expected-text
 * evidence to establish TEXT intent.
 *
 * This is generic. The actual text value comes from
 * the failure condition and is never hardcoded.
 */
if (context.getExpectedIntent() == null
        || context.getExpectedIntent()
                == ElementIntent.UNKNOWN) {

    ElementIntent resolvedIntent =
            resolveIntentFromEvidence(evidences);

    if (resolvedIntent != null
            && resolvedIntent != ElementIntent.UNKNOWN) {

        context.setExpectedIntent(
                resolvedIntent);
    }
}

    /*
     * Store calculated evidence scores.
     */
    for (EvidenceScore score : scores) {

        context.addEvidenceScore(score);
    }

    /*
     * Populate semantic context.
     */
    for (EvidenceScore score : scores) {

        populateContext(
                context,
                score);
    }

    /*
     * Highest score represents the
     * resolved confidence.
     */
    if (!scores.isEmpty()) {

        context.setConfidence(
                scores.get(0).getTotalScore());
    }

    return context;
}

    private void populateContext(
            ExpectedContext context,
            EvidenceScore score) {

        if (context == null
                || score == null) {

            return;
        }

        if (score.getSupportingEvidence()
                .isEmpty()) {

            return;
        }

        switch (score.getType()) {

case TEXT:

    /*
     * IMPORTANT:
     *
     * Locator values are NOT expected text.
     *
     * Example:
     *
     * failed locator:
     * [data-test='ti']
     *
     * "ti" is locator identity.
     *
     * It must NEVER become:
     *
     * expectedText = "ti"
     *
     * Expected text must come from actual
     * execution/wait/assertion evidence.
     */

    if (context.getExpectedIntent()
            == ElementIntent.TEXT
            && context.getExpectedText() == null) {

        boolean hasNonLocatorEvidence = false;

        for (ExpectedEvidence evidence :
                score.getSupportingEvidence()) {

            if (evidence == null) {
                continue;
            }

            if (evidence.getSource()
                    != EvidenceSource.LOCATOR) {

                hasNonLocatorEvidence = true;
                break;
            }
        }

        if (hasNonLocatorEvidence) {

            context.setExpectedText(
                    score.getValue());
        }
    }

    break;

            case TAG:

                if (context.getExpectedTag()
                        == null) {

                    context.setExpectedTag(
                            score.getValue());
                }

                break;

case LABEL:

    /*
     * ========================================================
     * LABEL EVIDENCE
     * ========================================================
     *
     * A DOM label describes the candidate element currently
     * present in the page.
     *
     * It is NOT automatically proof of what the failed test
     * expected.
     *
     * Example:
     *
     * Failed element:
     *     employeeNameInput
     *
     * Current DOM:
     *     Employee Name
     *
     * A DOM label such as "Employee Name" is useful for
     * candidate ranking, but it must not become the
     * authoritative expectedLabel unless an explicit semantic
     * provider supplied it.
     *
     * This prevents stale/wrong DOM context such as:
     *
     *     expectedLabel = Password
     *
     * from blocking otherwise valid candidates.
     */

    if (context.getExpectedLabel() == null
            || context.getExpectedLabel().isBlank()) {

        boolean authoritativeLabel =
                false;

        for (ExpectedEvidence evidence :
                score.getSupportingEvidence()) {

            if (evidence == null) {
                continue;
            }

            /*
             * DOM labels are contextual evidence only.
             */
            if (evidence.getSource()
                    == EvidenceSource.DOM) {

                continue;
            }

            /*
             * Locator labels are also not authoritative.
             */
            if (evidence.getSource()
                    == EvidenceSource.LOCATOR) {

                continue;
            }

            /*
             * Only explicit semantic/business/navigation
             * evidence may establish the expected label.
             */
            authoritativeLabel = true;
            break;
        }

        if (authoritativeLabel) {

            context.setExpectedLabel(
                    score.getValue());
        }
    }

    break;

            case PAGE:

                if (context.getExpectedPage()
                        == null) {

                    context.setExpectedPage(
                            score.getValue());
                }

                break;

            case ROLE:

                if (context.getExpectedRole()
                        == null) {

                    try {

                        context.setExpectedRole(
                                ExpectedRole.valueOf(
                                        score.getValue()
                                                .toUpperCase()));

                    } catch (Exception ignored) {
                    }
                }

                break;

            default:
                break;
        }
    }
private ElementIntent resolveIntentFromEvidence(
        List<ExpectedEvidence> evidences) {

    if (evidences == null
            || evidences.isEmpty()) {

        return ElementIntent.UNKNOWN;
    }

    System.out.println(
            "\n===== INTENT EVIDENCE =====");

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null) {
            continue;
        }

        System.out.println(
                "SOURCE="
                        + evidence.getSource()
                        + " | TYPE="
                        + evidence.getType()
                        + " | VALUE="
                        + evidence.getValue()
                        + " | ATTRIBUTE="
                        + evidence.getAttribute()
                        + " | CONFIDENCE="
                        + evidence.getConfidence());
    }

    System.out.println(
            "===========================\n");


    /*
     * ========================================================
     * PRIORITY 1
     * Explicit ROLE evidence
     * ========================================================
     */

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null
                || evidence.getType()
                        != ExpectedEvidenceType.ROLE) {

            continue;
        }

        ElementIntent intent =
                intentFromRole(
                        evidence.getValue());

        if (intent != ElementIntent.UNKNOWN) {

            return intent;
        }
    }


    /*
     * ========================================================
     * PRIORITY 2
     * Explicit DOM TAG evidence
     * ========================================================
     */

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null
                || evidence.getType()
                        != ExpectedEvidenceType.TAG) {

            continue;
        }

        ElementIntent intent =
                intentFromTag(
                        evidence.getValue());

        if (intent != ElementIntent.UNKNOWN) {

            return intent;
        }
    }


    /*
     * ========================================================
     * PRIORITY 3
     * Execution ACTION evidence
     *
     * Only unambiguous actions are allowed.
     * CLICK remains UNKNOWN.
     * ========================================================
     */

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null
                || evidence.getType()
                        != ExpectedEvidenceType.ACTION) {

            continue;
        }

        ElementIntent intent =
                intentFromAction(
                        evidence.getValue());

        if (intent != ElementIntent.UNKNOWN) {

            return intent;
        }
    }


    /*
     * ========================================================
     * PRIORITY 4
     * Locator semantic evidence
     *
     * IMPORTANT:
     *
     * We are NOT using the locator value as expected TEXT.
     *
     * We are only allowing an evidence provider that already
     * classified the locator semantically to contribute intent.
     *
     * Example:
     *
     * locator evidence:
     * TYPE = TEXT
     *
     * means the locator analyzer classified the target as
     * text-like.
     *
     * The actual locator value "ti" is NEVER copied into
     * expectedText.
     * ========================================================
     */

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null
                || evidence.getSource()
                        != EvidenceSource.LOCATOR) {

            continue;
        }

        ExpectedEvidenceType type =
                evidence.getType();

        if (type == null) {
            continue;
        }

        switch (type) {

            case TEXT:
                return ElementIntent.TEXT;

            default:
                break;
        }
    }


    /*
     * ========================================================
     * PRIORITY 5
     * Actual non-locator TEXT evidence
     *
     * This can establish TEXT intent because it represents
     * actual expected content rather than locator identity.
     * ========================================================
     */

    for (ExpectedEvidence evidence : evidences) {

        if (evidence == null
                || evidence.getType()
                        != ExpectedEvidenceType.TEXT) {

            continue;
        }

        if (evidence.getSource()
                == EvidenceSource.LOCATOR) {

            continue;
        }

        String value =
                evidence.getValue();

        if (value != null
                && !value.isBlank()) {

            return ElementIntent.TEXT;
        }
    }


    return ElementIntent.UNKNOWN;
}

private ElementIntent intentFromRole(String value) {

    if (value == null || value.isBlank()) {
        return ElementIntent.UNKNOWN;
    }

    String normalized =
            value.trim()
                    .toUpperCase();

    /*
     * First handle values that directly represent
     * ElementIntent.
     *
     * Examples:
     *
     * BUTTON
     * INPUT
     * LINK
     * DROPDOWN
     * CHECKBOX
     * RADIO
     * TABLE
     * CONTAINER
     * TEXT
     */
    try {

        return ElementIntent.valueOf(normalized);

    } catch (IllegalArgumentException ignored) {
        // Continue with semantic role mappings.
    }

    /*
     * Standard accessibility / semantic roles.
     */
    switch (normalized) {

        case "BUTTON":
            return ElementIntent.BUTTON;

        case "LINK":
            return ElementIntent.LINK;

        case "TEXTBOX":
        case "SEARCHBOX":
            return ElementIntent.INPUT;

        case "COMBOBOX":
        case "LISTBOX":
            return ElementIntent.DROPDOWN;

        case "CHECKBOX":
            return ElementIntent.CHECKBOX;

        case "RADIO":
        case "RADIOBUTTON":
            return ElementIntent.RADIO;

        case "TABLE":
            return ElementIntent.TABLE;

        default:
            return ElementIntent.UNKNOWN;
    }
}


private ElementIntent intentFromTag(String value) {

    if (value == null || value.isBlank()) {
        return ElementIntent.UNKNOWN;
    }

    String tag =
            value.trim()
                    .toLowerCase();

    switch (tag) {

        case "input":
            return ElementIntent.INPUT;

        case "button":
            return ElementIntent.BUTTON;

        case "a":
            return ElementIntent.LINK;

        case "select":
            return ElementIntent.DROPDOWN;

        case "textarea":
            return ElementIntent.INPUT;

        case "table":
            return ElementIntent.TABLE;

        default:
            return ElementIntent.UNKNOWN;
    }
}


private ElementIntent intentFromAction(String value) {

    if (value == null || value.isBlank()) {
        return ElementIntent.UNKNOWN;
    }

    String action =
            value.trim()
                    .toLowerCase();

    switch (action) {

        case "send_keys":
        case "sendkeys":
        case "type":
        case "input":
        case "clear":
            return ElementIntent.INPUT;

        /*
         * CLICK is intentionally ambiguous.
         *
         * It can be BUTTON, LINK, CHECKBOX,
         * RADIO, etc.
         */
        case "click":
            return ElementIntent.UNKNOWN;

        default:
            return ElementIntent.UNKNOWN;
    }
}

}