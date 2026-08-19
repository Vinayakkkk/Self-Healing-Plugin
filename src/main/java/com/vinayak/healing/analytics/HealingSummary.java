package com.vinayak.healing.analytics;

import com.vinayak.healing.cache.LocatorCache;

public final class HealingSummary {

    private HealingSummary() {
    }

    public static void print(
            HealingMetrics metrics) {

        if (metrics == null) {

            System.out.println(
                    "No healing metrics available.");

            return;
        }

        System.out.println();

        System.out.println(
                "======================================================");

        System.out.println(
                "             SELF HEALING ANALYTICS");

        System.out.println(
                "======================================================");

        System.out.printf(
                "%-35s %d%n",
                "Total Healing Attempts :",
                metrics.getTotalHealingAttempts());

        System.out.printf(
                "%-35s %d%n",
                "Successful Heals :",
                metrics.getSuccessfulHeals());

        System.out.printf(
                "%-35s %d%n",
                "Deterministic Heals :",
                metrics.getDeterministicHeals());

        System.out.printf(
                "%-35s %d%n",
                "AI Heals :",
                metrics.getAiHeals());

        System.out.printf(
                "%-35s %d%n",
                "Cache Hits :",
                metrics.getCacheHits());

        System.out.printf(
                "%-35s %d%n",
                "Cache Misses :",
                metrics.getCacheMisses());

        System.out.printf(
                "%-35s %d%n",
                "Validation Failures :",
                metrics.getValidationFailures());

        System.out.printf(
                "%-35s %d%n",
                "Healing Failures :",
                metrics.getFailures());

                System.out.printf(
        "%-35s %d%n",
        "Capability Failures :",
        metrics.getCapabilityFailures());

System.out.printf(
        "%-35s %d%n",
        "Duplicate Resolutions :",
        metrics.getDuplicateResolutions());

System.out.printf(
        "%-35s %d%n",
        "Learning Records :",
        metrics.getLearningRecords());

        System.out.printf(
                "%-35s %.2f %% %n",
                "Success Rate :",
                metrics.getSuccessRate());

        System.out.printf(
                "%-35s %.2f %% %n",
                "Cache Hit Rate :",
                metrics.getCacheHitRate());

                System.out.printf(
        "%-35s %.2f %% %n",
        "AI Healing Rate :",
        metrics.getAiHealingRate());

System.out.printf(
        "%-35s %.2f %% %n",
        "Duplicate Rate :",
        metrics.getDuplicateRate());

System.out.printf(
        "%-35s %.2f %% %n",
        "Learning Rate :",
        metrics.getLearningRate());

        System.out.printf(
                "%-35s %.2f ms%n",
                "Average Healing Time :",
                metrics.getAverageHealingTime());

        if (metrics.getMaximumHealingTime() > 0) {

            System.out.printf(
                    "%-35s %d ms%n",
                    "Maximum Healing Time :",
                    metrics.getMaximumHealingTime());
        }

        if (metrics.getMinimumHealingTime()
                != Long.MAX_VALUE) {

            System.out.printf(
                    "%-35s %d ms%n",
                    "Minimum Healing Time :",
                    metrics.getMinimumHealingTime());
        }
System.out.println(
        "Top Broken Pages :");

metrics.getPageFailures()
        .entrySet()
        .stream()
        .sorted(
                java.util.Map.Entry
                        .<String, Long>comparingByValue()
                        .reversed())
        .limit(5)
        .forEach(entry ->
                System.out.printf(
                        "  %-33s %d%n",
                        entry.getKey(),
                        entry.getValue()));
        System.out.printf(
                "%-35s %d%n",
                "Current Cache Size :",
                LocatorCache.size());

        System.out.println(
                "======================================================");

        System.out.println();
    }
}