package com.vinayak.healing.outcome.verifier;

import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.outcome.model.OutcomeVerificationResult;

/**
 * Generic contract for every outcome verifier.
 *
 * Every verifier validates one aspect of the healed action
 * and returns an OutcomeVerificationResult.
 */
public interface OutcomeVerifier {

    OutcomeVerificationResult verify(
            FailureContext context);
}