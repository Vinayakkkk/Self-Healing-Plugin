package com.vinayak.healing.logging;

public final class HealingLogger {

    /*
     * Log levels:
     *
     * OFF   -> No framework logs
     * ERROR -> Errors only
     * WARN  -> Warnings + Errors
     * INFO  -> Important healing flow
     * DEBUG -> Everything
     */
    public enum Level {
        OFF,
        ERROR,
        WARN,
        INFO,
        DEBUG
    }

    /*
     * Default level for normal framework execution.
     *
     * INFO keeps important healing information visible
     * without printing internal debugging details.
     */
    private static volatile Level currentLevel =
            Level.INFO;

    private HealingLogger() {
        // Utility class
    }

    // =====================================
    // CONFIGURATION
    // =====================================

    public static void setLevel(
            Level level) {

        if (level == null) {
            return;
        }

        currentLevel = level;
    }

    public static Level getLevel() {
        return currentLevel;
    }

    public static boolean isDebugEnabled() {

        return currentLevel == Level.DEBUG;
    }

    // =====================================
    // DEBUG
    // =====================================

    public static void debug(
            String message) {

        if (isEnabled(Level.DEBUG)) {

            System.out.println(
                    "[HEALING DEBUG] "
                            + safe(message));
        }
    }

    // =====================================
    // INFO
    // =====================================

    public static void info(
            String message) {

        if (isEnabled(Level.INFO)) {

            System.out.println(
                    "[HEALING INFO] "
                            + safe(message));
        }
    }

    // =====================================
    // WARN
    // =====================================

    public static void warn(
            String message) {

        if (isEnabled(Level.WARN)) {

            System.out.println(
                    "[HEALING WARN] "
                            + safe(message));
        }
    }

    // =====================================
    // ERROR
    // =====================================

    public static void error(
            String message) {

        if (isEnabled(Level.ERROR)) {

            System.err.println(
                    "[HEALING ERROR] "
                            + safe(message));
        }
    }

    public static void error(
            String message,
            Throwable throwable) {

        if (!isEnabled(Level.ERROR)) {
            return;
        }

        System.err.println(
                "[HEALING ERROR] "
                        + safe(message));

        if (throwable != null
                && currentLevel == Level.DEBUG) {

            throwable.printStackTrace();
        }
    }

    // =====================================
    // HEALING RESULT
    // =====================================

    public static void healingSuccess(
            String failedLocator,
            String healedLocator) {

        info(
                "Healing successful"
                        + " | failed="
                        + safe(failedLocator)
                        + " | healed="
                        + safe(healedLocator));
    }

    public static void cacheHit(
            String failedLocator,
            String cachedLocator) {

        info(
                "Cache hit"
                        + " | failed="
                        + safe(failedLocator)
                        + " | cached="
                        + safe(cachedLocator));
    }

    // =====================================
    // INTERNAL
    // =====================================

    private static boolean isEnabled(
            Level requiredLevel) {

        if (currentLevel == Level.OFF) {
            return false;
        }

        return currentLevel.ordinal()
                >= requiredLevel.ordinal();
    }

    private static String safe(
            String value) {

        return value == null
                ? "UNKNOWN"
                : value;
    }
}