package com.vinayak.healing.cache;

import com.vinayak.healing.ai.LocatorSuggestion;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LocatorCache {

    private static final double MIN_CONFIDENCE_IMPROVEMENT = 100.0;

    private LocatorCache() {
    }

    private static final Map<String, LocatorSuggestion> CACHE =
            new ConcurrentHashMap<>(
                    CachePersistence.load());

    /**
     * Indicates whether cache has changed
     * and needs persistence.
     */
    private static volatile boolean dirty = false;


    public static String buildKey(
        String pageObjectClass,
        String variableName,
        String expectedIntent,
        String failedLocator) {

    return safe(pageObjectClass)
            + "|"
            + safe(variableName)
            + "|"
            + safe(expectedIntent)
            + "|"
            + safe(failedLocator);
}

private static String safe(String value) {

    if (value == null || value.isBlank()) {
        return "UNKNOWN";
    }

    return value.trim();
}

    // =====================================
    // GET
    // =====================================

    public static LocatorSuggestion get(
        String cacheKey) {

    System.out.println(
            "\n========== CACHE LOOKUP ==========");

    System.out.println(
            "Requested Cache Key : "
                    + cacheKey);

    if (cacheKey == null
            || cacheKey.isBlank()) {

        System.out.println(
                "CACHE LOOKUP SKIPPED : Empty key");

        System.out.println(
                "=================================");

        return null;
    }

    LocatorSuggestion suggestion =
            CACHE.get(cacheKey);

    if (suggestion == null) {

        System.out.println(
                "CACHE MISS");

        System.out.println(
                "Current Cache Size : "
                        + CACHE.size());

        System.out.println(
                "Available Cache Keys:");

        for (String key : CACHE.keySet()) {

            System.out.println(
                    "  -> " + key);
        }

    } else {

        System.out.println(
                "CACHE HIT");

        System.out.println(
                "Cached Locator : "
                        + suggestion.getLocatorType()
                        + "="
                        + suggestion.getLocatorValue());

        System.out.println(
                "Confidence : "
                        + suggestion.getConfidence());
    }

    System.out.println(
            "=================================");

    return suggestion;
}

    // =====================================
    // PUT
    // =====================================

    public static void put(
            String cacheKey,
            LocatorSuggestion suggestion) {

        if (cacheKey == null
                || cacheKey.isBlank()
                || suggestion == null) {

            return;
        }

LocatorSuggestion existing =
        CACHE.get(cacheKey);

if (existing != null) {

    if (equals(existing, suggestion)) {

        System.out.println(
                "Cache already contains identical locator.");

        return;
    }

    if (!shouldReplace(existing, suggestion)) {

        System.out.println(
                "Keeping existing cache locator.");

        System.out.println(
                "Existing : "
                        + existing.getLocatorType()
                        + "="
                        + existing.getLocatorValue()
                        + " | confidence = "
                        + existing.getConfidence());

        System.out.println(
                "Rejected new : "
                        + suggestion.getLocatorType()
                        + "="
                        + suggestion.getLocatorValue()
                        + " | confidence = "
                        + suggestion.getConfidence());

        return;
    }

    System.out.println(
            "Replacing cache locator with stronger locator.");

    System.out.println(
            "Old : "
                    + existing.getLocatorType()
                    + "="
                    + existing.getLocatorValue()
                    + " | confidence = "
                    + existing.getConfidence());

    System.out.println(
            "New : "
                    + suggestion.getLocatorType()
                    + "="
                    + suggestion.getLocatorValue()
                    + " | confidence = "
                    + suggestion.getConfidence());
}

        CACHE.put(
                cacheKey,
                suggestion);

        dirty = true;

        System.out.println(
                "\n========== CACHE UPDATED ==========");

        System.out.println(
                "Failed Locator : "
                        + cacheKey);

        System.out.println(
                "Recovered By : "
                        + suggestion.getLocatorType()
                        + "="
                        + suggestion.getLocatorValue());

        System.out.println(
                "Confidence : "
                        + suggestion.getConfidence());

        System.out.println(
                "Current Cache Size : "
                        + CACHE.size());
    }

    // =====================================
    // FLUSH
    // =====================================

    /**
     * Writes cache to disk only if modified.
     */
    public static synchronized void flush() {

        if (!dirty) {

            return;
        }

        CachePersistence.save(CACHE);

        dirty = false;

        System.out.println(
                "\nCache flushed successfully.");
    }

    // =====================================
    // CLEAR
    // =====================================

    public static synchronized void clear() {

        CACHE.clear();

        dirty = true;

        flush();
    }

    // =====================================
    // SIZE
    // =====================================

    public static int size() {

        return CACHE.size();
    }

    // =====================================
    // GET ALL
    // =====================================

    public static Map<String, LocatorSuggestion> getAll() {

        return Collections.unmodifiableMap(CACHE);
    }

    // =====================================
    // CONTAINS
    // =====================================

    public static boolean contains(
            String cacheKey) {

        return CACHE.containsKey(cacheKey);
    }

    // =====================================
    // REMOVE
    // =====================================

    public static void remove(
            String cacheKey) {

        if (CACHE.remove(cacheKey) != null) {

            dirty = true;
        }
    }

    // =====================================
    // Helper
    // =====================================

    private static boolean equals(
            LocatorSuggestion left,
            LocatorSuggestion right) {

        return left.getLocatorType()
                .equalsIgnoreCase(
                        right.getLocatorType())

                &&

                left.getLocatorValue()
                        .equalsIgnoreCase(
                                right.getLocatorValue());
    }
    private static boolean shouldReplace(
        LocatorSuggestion existing,
        LocatorSuggestion incoming) {

    int existingPriority =
            locatorPriority(
                    existing.getLocatorType());

    int incomingPriority =
            locatorPriority(
                    incoming.getLocatorType());

    // A more stable locator type always replaces a weaker one.
    if (incomingPriority > existingPriority) {
        return true;
    }

    // A weaker locator type must never replace a stronger one.
    if (incomingPriority < existingPriority) {
        return false;
    }

    // Same locator quality: replace only with meaningful confidence gain.
    return incoming.getConfidence()
            >= existing.getConfidence()
            + MIN_CONFIDENCE_IMPROVEMENT;
}

private static int locatorPriority(
        String locatorType) {

    if (locatorType == null) {
        return 0;
    }

    switch (locatorType.trim().toLowerCase()) {

        case "data-testid":
        case "data-test":
        case "data-qa":
        case "data-cy":
            return 100;

        case "id":
            return 90;

        case "name":
            return 80;

        case "aria-label":
            return 70;

        case "css":
        case "cssselector":
            return 60;

        case "href":
            return 55;

        case "xpath":
            return 40;

        case "class":
            return 20;

        default:
            return 10;
    }
}

}