package com.vinayak.healing.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HealingSuccessTracker {

    private static final Map<String, Integer>
            SUCCESS_COUNTS =
            new ConcurrentHashMap<>();

    public static int recordSuccess(
            String cacheKey) {

        return SUCCESS_COUNTS.merge(
                cacheKey,
                1,
                Integer::sum);
    }

    public static int getSuccessCount(
            String cacheKey) {

        return SUCCESS_COUNTS.getOrDefault(
                cacheKey,
                0);
    }

    public static void reset(
            String cacheKey) {

        SUCCESS_COUNTS.remove(cacheKey);
    }
}