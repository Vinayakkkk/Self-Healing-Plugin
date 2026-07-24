package com.vinayak.healing.execution;

import com.vinayak.healing.logging.HealingLogger;

public final class RuntimeContextTracker {

    private RuntimeContextTracker() {
    }

    private static final ThreadLocal<RuntimeContext>
            CONTEXT =
            ThreadLocal.withInitial(
                    RuntimeContext::new);

    public static RuntimeContext getContext() {

        return CONTEXT.get();
    }

    public static void setPageObject(

            String pageObjectClass) {

        CONTEXT.get()
                .setPageObjectClass(
                        pageObjectClass);
    }

    public static void setMethod(

            String methodName) {

        CONTEXT.get()
                .setMethodName(
                        methodName);
    }

    public static void recordArgument(

            String name,
            Object value) {

        CONTEXT.get()
                .putArgument(
                        name,
                        value);
    }

    public static void recordVariable(

            String name,
            Object value) {

        CONTEXT.get()
                .putVariable(
                        name,
                        value);
    }

    public static void clear() {

        CONTEXT.get()
                .clear();
    }

    public static void print() {

        HealingLogger.debug(
                "\n========== RUNTIME CONTEXT ==========");

        HealingLogger.debug(
                CONTEXT.get().toString());

        HealingLogger.debug(
                "=====================================\n");
    }
}