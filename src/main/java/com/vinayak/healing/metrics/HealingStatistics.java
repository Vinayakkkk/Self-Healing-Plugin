package com.vinayak.healing.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HealingStatistics {

    private static final AtomicInteger cacheHits =
            new AtomicInteger();

    private static final AtomicInteger deterministicHeals =
            new AtomicInteger();

    private static final AtomicInteger aiHeals =
            new AtomicInteger();

    private static final AtomicInteger failures =
            new AtomicInteger();

    private static final AtomicLong totalHealingTime =
            new AtomicLong();

    private HealingStatistics() {
    }

    public static void cacheHit() {
        cacheHits.incrementAndGet();
    }

    public static void deterministicHeal() {
        deterministicHeals.incrementAndGet();
    }

    public static void aiHeal() {
        aiHeals.incrementAndGet();
    }

    public static void failure() {
        failures.incrementAndGet();
    }

    public static void addHealingTime(long millis) {
        totalHealingTime.addAndGet(millis);
    }

    public static void printSummary() {

        int totalHeals =
                cacheHits.get()
                        + deterministicHeals.get()
                        + aiHeals.get();

        long average =
                totalHeals == 0
                        ? 0
                        : totalHealingTime.get() / totalHeals;

        System.out.println("\n================================");
        System.out.println("      HEALING SUMMARY");
        System.out.println("================================");
        System.out.println("Cache Hits          : " + cacheHits.get());
        System.out.println("Deterministic Heals : " + deterministicHeals.get());
        System.out.println("AI Heals            : " + aiHeals.get());
        System.out.println("Failures            : " + failures.get());
        System.out.println("Average Heal Time   : " + average + " ms");
        System.out.println("================================");
    }
}