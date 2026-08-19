package com.vinayak.healing.execution;

import com.vinayak.healing.logging.HealingLogger;

public class ExecutionTracker {

    private static final ExecutionContext context =
            new ExecutionContext();

    private ExecutionTracker() {
    }

    public static void record(ExecutionStep step) {

        if (step == null) {
            return;
        }

        context.addStep(step);
    }

 public static void recordAction(String action) {

    if (action == null || action.isBlank()) {
        return;
    }

    ExecutionAction executionAction;

    try {
        executionAction =
                ExecutionAction.valueOf(
                        action.trim().toUpperCase());

    } catch (IllegalArgumentException e) {

        // FIND_ELEMENT is not an actual user action.
        // Do not add it to execution history.
        return;
    }

    ExecutionStep step =
            new ExecutionStep();

    step.setAction(executionAction);

    // Important: action-only steps do not have pageName,
    // so do NOT add them into execution history.
    // They are only used as the current/latest action.
    context.setLatestAction(step);

    HealingLogger.debug(
            "EXECUTION ACTION RECORDED = "
                    + executionAction);
}

    public static ExecutionContext getContext() {
        return context;
    }

    public static void clearAction() {

    context.setLatestAction(null);

    HealingLogger.debug(
            "EXECUTION ACTION CLEARED");
}

    public static void clear() {
        context.clear();
    }
    public static ExecutionAction consumeLatestAction() {

    ExecutionStep step =
            context.getLatestAction();

    if (step == null
            || step.getAction() == null) {

        return null;
    }

    ExecutionAction action =
            step.getAction();

    context.setLatestAction(null);

    HealingLogger.debug(
            "EXECUTION ACTION CONSUMED = "
                    + action);

    return action;
}
}