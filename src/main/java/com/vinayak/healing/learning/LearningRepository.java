package com.vinayak.healing.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LearningRepository {

    private static final String LEARNING_FOLDER =
            "cache";

    private static final String LEARNING_FILE =
            LEARNING_FOLDER + "/learning-history.json";

    private final ObjectMapper mapper;

    private final Map<LearningKey, List<LearningRecord>> records =
            new LinkedHashMap<>();

            private static final LearningRepository INSTANCE =
        new LearningRepository(true);

    private LearningRepository(boolean loadHistory) {

    mapper = new ObjectMapper();

    mapper.findAndRegisterModules();

    mapper.enable(
            SerializationFeature.INDENT_OUTPUT);

    if (loadHistory) {
        load();
    }
}

public LearningRepository() {
    this(true);
}

public static LearningRepository getInstance() {
    return INSTANCE;
}

    /**
     * Stores one learning record.
     */
    public synchronized void record(
        LearningRecord record) {

   if (record == null
        || record.getLearningKey() == null) {

    return;
}

/*
 * Only reusable learning experiences belong
 * in persistent learning history.
 *
 * Collection healing explicitly sets cacheAllowed=false
 * because collection locators must not be reused as
 * single-element learning.
 */
/*
 * ==========================================
 * TRUSTED LEARNING GATE
 * ==========================================
 *
 * Persistent learning must represent only a
 * successful and reusable healing experience.
 *
 * A record is persisted only when:
 *
 * outcomeSuccess  = true
 * healingAllowed  = true
 * cacheAllowed    = true
 *
 * This keeps failed, unsafe, or non-reusable
 * healing decisions out of historical learning.
 */
if (!record.isOutcomeSuccess()
        || !record.isHealingAllowed()) {

    System.out.println(
            "Learning record skipped from persistence."
                    + " | source="
                    + record.getHealingSource()
                    + " | outcomeSuccess="
                    + record.isOutcomeSuccess()
                    + " | healingAllowed="
                    + record.isHealingAllowed()
                    + " | cacheAllowed="
                    + record.isCacheAllowed()
                    + " | locator="
                    + record.getSelectedLocator());

    return;
}

    System.out.println(
            "\n========== LEARNING RECORD ==========");

    System.out.println(
            "BEFORE ADD - Total Records : "
                    + size());

    System.out.println(
            "Adding Key : "
                    + record.getLearningKey());

    List<LearningRecord> history =
        records.computeIfAbsent(
                record.getLearningKey(),
                key -> new ArrayList<>());

for (LearningRecord existing : history) {

    if (sameLearningOutcome(existing, record)) {

        System.out.println(
                "Duplicate learning record ignored.");

        System.out.println(
                "Key : "
                        + record.getLearningKey());

        System.out.println(
                "Locator : "
                        + record.getSelectedLocator());

        return;
    }
}

history.add(record);

    System.out.println(
            "AFTER ADD - Total Records : "
                    + size());

    System.out.println(
            "Total Keys : "
                    + records.size());

    System.out.println(
            "======================================");

    save();
}

    /**
     * Returns all learning records for a context.
     */
    public synchronized List<LearningRecord> find(
            LearningKey key) {

        if (key == null) {
            return Collections.emptyList();
        }

        System.out.println(
                "\n========== LEARNING LOOKUP ==========");

        System.out.println(
                "Requested Key : " + key);

        System.out.println(
                "Stored Keys   :");

        for (LearningKey storedKey :
                records.keySet()) {

            System.out.println(
                    "  " + storedKey);
        }

        List<LearningRecord> history =
                records.get(key);

        System.out.println(
                "History Found : "
                        + (history == null
                        ? 0
                        : history.size()));

        System.out.println(
                "====================================\n");

        if (history == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(history));
    }

    /**
     * Returns aggregated statistics
     * for a learning context.
     */
    public synchronized LearningStatistics getStatistics(
            LearningKey key) {

        LearningStatistics statistics =
                new LearningStatistics();

        List<LearningRecord> history =
                records.get(key);

        if (history == null) {
            return statistics;
        }

        for (LearningRecord record :
                history) {

            statistics.record(record);
        }

        return statistics;
    }

    /**
     * Returns total number of learning records.
     */
    public synchronized int size() {

        int total = 0;

        for (List<LearningRecord> history :
                records.values()) {

            total += history.size();
        }

        return total;
    }

    /**
     * Returns all stored learning records.
     */
    public synchronized List<LearningRecord> getAll() {

        List<LearningRecord> all =
                new ArrayList<>();

        for (List<LearningRecord> history :
                records.values()) {

            all.addAll(history);
        }

        return Collections.unmodifiableList(all);
    }

    /**
     * Clears all learning history.
     */
    public synchronized void clear() {

        records.clear();

        File file =
                new File(LEARNING_FILE);

        if (file.exists()
                && !file.delete()) {

            System.out.println(
                    "Unable to delete learning history: "
                            + file.getAbsolutePath());
        }
    }

    /**
     * Loads learning history from disk.
     *
     * JSON is read manually because LearningKey
     * and LearningRecord are immutable objects.
     */
    private synchronized void load() {

        File file =
                new File(LEARNING_FILE);

        if (!file.exists()
                || file.length() == 0) {

            System.out.println(
                    "No learning history found.");

            return;
        }

        try {

            JsonNode root =
                    mapper.readTree(file);

            if (root == null
                    || !root.isArray()) {

                System.out.println(
                        "Learning history JSON is not an array.");

                return;
            }

            records.clear();

            int loadedCount = 0;

            for (JsonNode node : root) {

                if (node == null
                        || !node.isObject()) {

                    continue;
                }

                JsonNode keyNode =
                        node.get("learningKey");

                if (keyNode == null
                        || keyNode.isNull()) {

                    continue;
                }

                LearningKey learningKey =
                        readLearningKey(keyNode);

                if (learningKey == null) {
                    continue;
                }

                LearningRecord learningRecord =
                        readLearningRecord(
                                node,
                                learningKey);

                if (learningRecord == null) {
                    continue;
                }

                records.computeIfAbsent(
                        learningKey,
                        key -> new ArrayList<>())
                        .add(learningRecord);

                loadedCount++;
            }

            System.out.println(
                    "\n========== LEARNING HISTORY LOAD ==========");

            System.out.println(
                    "Location : "
                            + file.getAbsolutePath());

            System.out.println(
                    "Loaded Records : "
                            + loadedCount);

            System.out.println(
                    "Stored Keys : "
                            + records.size());

            System.out.println(
                    "===========================================\n");

        } catch (Exception exception) {

            System.out.println(
                    "Unable to load learning history.");

            System.out.println(
                    "Learning history file: "
                            + file.getAbsolutePath());

            exception.printStackTrace();
        }
    }

    /**
     * Builds LearningKey from JSON.
     */
    private LearningKey readLearningKey(
            JsonNode keyNode) {

        try {

            String pageObjectClass =
                    text(
                            keyNode,
                            "pageObjectClass");

            String variableName =
                    text(
                            keyNode,
                            "variableName");

            String expectedIntent =
                    text(
                            keyNode,
                            "expectedIntent");

            String action =
                    text(
                            keyNode,
                            "action");

            String failedLocator =
                    text(
                            keyNode,
                            "failedLocator");

            return new LearningKey(
                    pageObjectClass,
                    variableName,
                    expectedIntent,
                    action,
                    failedLocator);

        } catch (Exception exception) {

            System.out.println(
                    "Unable to reconstruct LearningKey.");

            exception.printStackTrace();

            return null;
        }
    }

    /**
     * Builds LearningRecord from JSON.
     *
     * The original timestamp is restored when present.
     *
     * Older learning records that do not contain a timestamp
     * are still supported. In that case LearningRecord will
     * create a current timestamp as a safe fallback.
     */
    private LearningRecord readLearningRecord(
            JsonNode node,
            LearningKey learningKey) {

        try {

            String selectedLocator =
                    text(
                            node,
                            "selectedLocator");

            String selectedLocatorType =
                    text(
                            node,
                            "selectedLocatorType");

            String selectedLocatorValue =
                    text(
                            node,
                            "selectedLocatorValue");

            double candidateScore =
                    number(
                            node,
                            "candidateScore");

            String healingSource =
                    text(
                            node,
                            "healingSource");

            String confidenceLevel =
                    text(
                            node,
                            "confidenceLevel");

            boolean healingAllowed =
                    booleanValue(
                            node,
                            "healingAllowed");

            boolean cacheAllowed =
                    booleanValue(
                            node,
                            "cacheAllowed");

            boolean outcomeSuccess =
                    booleanValue(
                            node,
                            "outcomeSuccess");

            double outcomeConfidence =
                    number(
                            node,
                            "outcomeConfidence");

            /*
             * ==========================================
             * TIMESTAMP
             * ==========================================
             *
             * New learning records contain the timestamp.
             *
             * Older records may not contain it, so we
             * safely fall back to null.
             */
            String timestampText =
                    text(
                            node,
                            "timestamp");

            LocalDateTime timestamp = null;

            if (timestampText != null
                    && !timestampText.isBlank()) {

                try {

                    timestamp =
                            LocalDateTime.parse(
                                    timestampText);

                } catch (Exception timestampException) {

                    System.out.println(
                            "Unable to parse learning timestamp: "
                                    + timestampText);

                    /*
                     * Keep timestamp null.
                     *
                     * LearningRecord will safely fall back
                     * to the current time.
                     */
                    timestamp = null;
                }
            }

            /*
             * ==========================================
             * RESTORE LEARNING RECORD
             * ==========================================
             */
            return new LearningRecord(
                    learningKey,
                    selectedLocator,
                    selectedLocatorType,
                    selectedLocatorValue,
                    candidateScore,
                    healingSource,
                    confidenceLevel,
                    healingAllowed,
                    cacheAllowed,
                    outcomeSuccess,
                    outcomeConfidence,
                    timestamp);

        } catch (Exception exception) {

            System.out.println(
                    "Unable to reconstruct LearningRecord.");

            exception.printStackTrace();

            return null;
        }
    }

    /**
     * Reads a String field safely.
     */
    private String text(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return null;
        }

        return value.asText();
    }

    /**
     * Reads a numeric field safely.
     */
    private double number(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return 0.0;
        }

        return value.asDouble();
    }

    /**
     * Reads a boolean field safely.
     */
    private boolean booleanValue(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return false;
        }

        return value.asBoolean();
    }

    /**
     * Persists learning history to disk.
     */
    private synchronized void save() {

    try {

        File file =
                new File(LEARNING_FILE);

        File parent =
                file.getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        List<LearningRecord> allRecords =
                getAll();

        System.out.println(
                "\n========== LEARNING HISTORY SAVE ==========");

        System.out.println(
                "File : "
                        + file.getAbsolutePath());

        System.out.println(
                "Records Being Saved : "
                        + allRecords.size());

        System.out.println(
                "Keys Being Saved : "
                        + records.size());

        for (LearningRecord record :
                allRecords) {

            System.out.println(
                    "  "
                            + record.getLearningKey()
                            + " -> "
                            + record.getSelectedLocator());
        }

        mapper.writeValue(
                file,
                allRecords);

        System.out.println(
                "Learning history saved successfully.");

        System.out.println(
                "============================================");

    } catch (Exception exception) {

        System.out.println(
                "Unable to save learning history.");

        exception.printStackTrace();
    }
}

    public static String getLearningFilePath() {

        return LEARNING_FILE;
    }
    private boolean sameLearningOutcome(
        LearningRecord existing,
        LearningRecord incoming) {

    if (existing == null
            || incoming == null) {

        return false;
    }

    return safeEquals(
            existing.getSelectedLocator(),
            incoming.getSelectedLocator())

            && safeEquals(
                    existing.getSelectedLocatorType(),
                    incoming.getSelectedLocatorType())

            && safeEquals(
                    existing.getSelectedLocatorValue(),
                    incoming.getSelectedLocatorValue())

            && existing.isOutcomeSuccess()
            == incoming.isOutcomeSuccess();
}
private boolean safeEquals(
        String left,
        String right) {

    if (left == null && right == null) {
        return true;
    }

    if (left == null || right == null) {
        return false;
    }

    return left.equalsIgnoreCase(right);
}
}