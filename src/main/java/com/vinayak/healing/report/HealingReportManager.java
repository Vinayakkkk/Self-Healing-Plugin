package com.vinayak.healing.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinayak.healing.execution.ExecutionAction;
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
        String action,
        String expectedIntent,
        String cacheKey,
        String failedLocator,
        String healedLocator,
        double score,
        String confidenceLevel,
        boolean healingAllowed,
        boolean cacheAllowed,
        String source){

                System.out.println(
        "REPORT EVENT -> "
                + pageObjectClass
                + " | "
                + variableName
                + " | "
                + healedLocator);

                System.out.println(
        "[HEALING REPORT DEBUG]"
                + " | page=" + pageObjectClass
                + " | variable=" + variableName
                + " | action=" + action
                + " | intent=" + expectedIntent
                + " | source=" + source
                + " | failed=" + failedLocator
                + " | healed=" + healedLocator
                + " | score=" + score
                + " | confidence=" + confidenceLevel
                + " | healingAllowed=" + healingAllowed
                + " | cacheAllowed=" + cacheAllowed);

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
                action,
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

                System.out.println(
        "[HEALING REPORT DEBUG] Event created"
                + " | action=" + event.getAction()
                + " | source=" + event.getSource()
                + " | healedLocator=" + event.getHealedLocator());

            events.add(event);

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            reportFile,
                            events);

                            System.out.println(
        "REPORT SAVED : "
                + reportFile.getAbsolutePath());

System.out.println(
        "TOTAL EVENTS : "
                + events.size());

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static String getReportPath() {
    return REPORT_FILE;
}
}