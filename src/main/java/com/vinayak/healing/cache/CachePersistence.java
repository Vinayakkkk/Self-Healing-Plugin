package com.vinayak.healing.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinayak.healing.ai.LocatorSuggestion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class CachePersistence {

    private CachePersistence() {
    }

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    /**
     * Cache directory
     *
     * Windows :
     * C:\Users\<User>\.self-healing
     *
     * Mac/Linux :
     * ~/.self-healing
     */
    private static final File CACHE_DIRECTORY =
            new File(
                    System.getProperty("user.home"),
                    ".self-healing");

    /**
     * Cache file
     */
    private static final File CACHE_FILE =
            new File(
                    CACHE_DIRECTORY,
                    "healing-cache.json");

    // ====================================================
    // LOAD
    // ====================================================

    public static Map<String, LocatorSuggestion> load() {

        try {

            createDirectory();

            System.out.println(
                    "\n========== CACHE LOAD ==========");

            System.out.println(
                    "Location : "
                            + CACHE_FILE.getAbsolutePath());

            if (!CACHE_FILE.exists()) {

                System.out.println(
                        "Cache file not found.");

                return new HashMap<>();
            }

            Map<String, LocatorSuggestion> cache =
                    MAPPER.readValue(
                            CACHE_FILE,
                            new TypeReference<Map<String, LocatorSuggestion>>() {
                            });

            System.out.println(
                    "Loaded "
                            + cache.size()
                            + " cached locators.");

            return cache;

        } catch (Exception e) {

            System.out.println(
                    "Cache file is corrupted.");

            System.out.println(
                    "Creating a new cache.");

            try {

                CACHE_FILE.delete();

            } catch (Exception ignored) {
            }

            return new HashMap<>();
        }
    }

    // ====================================================
    // SAVE
    // ====================================================

    public static void save(
            Map<String, LocatorSuggestion> cache) {

        if (cache == null) {

            return;
        }

        try {

            createDirectory();

            MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(
                            CACHE_FILE,
                            cache);

            System.out.println(
                    "\n========== CACHE SAVE ==========");

            System.out.println(
                    "Saved "
                            + cache.size()
                            + " locator(s).");

            System.out.println(
                    "Location : "
                            + CACHE_FILE.getAbsolutePath());

        } catch (Exception e) {

            System.out.println(
                    "Unable to save cache.");

            System.out.println(
                    e.getMessage());
        }
    }

    // ====================================================
    // DELETE
    // ====================================================

    public static void delete() {

        try {

            if (CACHE_FILE.exists()) {

                CACHE_FILE.delete();

                System.out.println(
                        "Cache deleted.");
            }

        } catch (Exception ignored) {
        }
    }

    // ====================================================
    // EXISTS
    // ====================================================

    public static boolean exists() {

        return CACHE_FILE.exists();
    }

    // ====================================================
    // LOCATION
    // ====================================================

    public static String getCacheLocation() {

        return CACHE_FILE.getAbsolutePath();
    }

    // ====================================================
    // DIRECTORY
    // ====================================================

    private static void createDirectory() {

        if (!CACHE_DIRECTORY.exists()) {

            CACHE_DIRECTORY.mkdirs();
        }
    }

}