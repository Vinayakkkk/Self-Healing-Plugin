package com.vinayak.healing.outcome.verifier;

import com.vinayak.healing.engine.LocatorBuilder;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.ExpectedElement;
import com.vinayak.healing.outcome.model.OutcomeSignal;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;
import com.vinayak.healing.outcome.model.OutcomeWeights;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DomVerifier implements OutcomeVerifier {

    @Override
    public OutcomeVerificationResult verify(
            FailureContext context) {

        OutcomeVerificationResult result =
                new OutcomeVerificationResult();

        if (context == null) {

            result.setSuccess(false);

            result.addMessage(
                    "FailureContext is null.");

            return result;
        }

        WebDriver driver =
                context.getDriver();

        if (driver == null) {

            result.setSuccess(false);

            result.addMessage(
                    "WebDriver unavailable.");

            return result;
        }

        if (context.getExpectedElements() == null
        || context.getExpectedElements().isEmpty()) {

    result.setSuccess(false);

    result.setConfidence(0);

    result.addMessage(
            "No expected DOM elements configured.");

    return result;
}
int passed = 0;
        for (ExpectedElement expected :
                context.getExpectedElements()) {

            try {

                By locator =
                        LocatorBuilder.build(
                                expected.getLocatorType(),
                                expected.getLocatorValue());

               java.util.List<org.openqa.selenium.WebElement> elements =
        driver.findElements(locator);

if (!elements.isEmpty()
        && elements.get(0).isDisplayed()) {

    passed++;

    result.addPassedSignal(
            OutcomeSignal.EXPECTED_ELEMENT_FOUND);

} else {

    result.addFailedSignal(
            OutcomeSignal.EXPECTED_ELEMENT_NOT_FOUND);

    if (expected.isMandatory()) {

        result.setSuccess(false);

        result.setConfidence(0);

        result.addMessage(
                "Mandatory element missing : "
                        + expected.getDescription());

        return result;
    }
}

            } catch (Exception ex) {

                 result.addFailedSignal(
            OutcomeSignal.EXPECTED_ELEMENT_NOT_FOUND);

    result.addMessage(
            "DOM verification failed : "
                    + ex.getMessage());
}
        }

        if (passed > 0) {

    result.setSuccess(true);

    result.setConfidence(
            OutcomeWeights.EXPECTED_ELEMENT);

    result.addMessage(
            "DOM verification successful.");

} else {

    result.setSuccess(false);

    result.setConfidence(0);

    result.addMessage(
            "No expected DOM element verified.");
}

return result;
    }
}