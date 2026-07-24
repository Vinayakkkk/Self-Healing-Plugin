package com.vinayak.healing.report;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HealingReportManager {

   private static final String REPORT_FOLDER =
        "reports";

private static final String RUN_ID =
        LocalDateTime.now()
                .toString()
                .replace(":", "-")
                .replace(".", "-");

private static final String REPORT_FILE =
        REPORT_FOLDER
                + "/healing-report-"
                + RUN_ID
                + ".json";

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public static void logHealing(
        String pageObjectClass,
        String variableName,
        String expectedIntent,
        String cacheKey,
        String failedLocator,
        String healedLocator,
        double score,
        String confidenceLevel,
        boolean healingAllowed,
        boolean cacheAllowed,
        String source){

        try {

            File reportFile =
                    new File(REPORT_FILE);

            reportFile.getParentFile().mkdirs();

            List<HealingEvent> events =
                    new ArrayList<>();

            if (reportFile.exists()
                    && reportFile.length() > 0) {

                try {

    HealingEvent[] existing =
            mapper.readValue(
                    reportFile,
                    HealingEvent[].class);

    events.addAll(
            List.of(existing));

} catch(Exception e) {

    System.out.println(
            "Invalid report file detected. Creating new report.");

    events.clear();
}
            }

           HealingEvent event =
        new HealingEvent(
                pageObjectClass,
                variableName,
                expectedIntent,
                cacheKey,
                failedLocator,
                healedLocator,
                score,
                confidenceLevel,
                healingAllowed,
                cacheAllowed,
                LocalDateTime.now().toString(),
                source);

            events.add(event);

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            reportFile,
                            events);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static String getReportPath() {
    return REPORT_FILE;
}
}