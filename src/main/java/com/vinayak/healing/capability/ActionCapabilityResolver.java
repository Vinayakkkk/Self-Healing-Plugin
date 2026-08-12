package com.vinayak.healing.capability;

import com.vinayak.healing.execution.ExecutionAction;

/**
 * Resolves the capability required
 * for an execution action.
 */
public final class ActionCapabilityResolver {

    public ElementCapability resolve(
            ExecutionAction action) {

        if (action == null) {
            return null;
        }

        switch (action) {

            case SEND_KEYS:
                return ElementCapability.TYPE;

            case CLEAR:
                return ElementCapability.CLEAR;

            case CLICK:
                return ElementCapability.CLICK;

            case CHECKBOX:
                return ElementCapability.CHECK;

            case RADIO:
                return ElementCapability.RADIO;

            case SELECT:
                return ElementCapability.SELECT;

            default:
                return null;
        }
    }
}