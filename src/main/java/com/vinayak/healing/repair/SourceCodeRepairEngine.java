package com.vinayak.healing.repair;

import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.source.SourceCodeAnalyzer;
import java.io.IOException;
import java.nio.file.Path;

public class SourceCodeRepairEngine {

    private final FileBackupManager backupManager;
    private final JavaLocatorUpdater locatorUpdater;

    public SourceCodeRepairEngine() {
        this.backupManager = new FileBackupManager();
        this.locatorUpdater = new JavaLocatorUpdater();
    }

    /**
     * Repairs the Page Object source code using the validated locator.
     *
     * @param context FailureContext
     * @param suggestion Validated locator suggestion
     * @return RepairReport
     */
    public RepairReport repair(FailureContext context,
                               LocatorSuggestion suggestion) {

        RepairReport report = new RepairReport();

        try {

            if (context == null) {
                throw new IllegalArgumentException("FailureContext cannot be null.");
            }

            if (suggestion == null) {
                throw new IllegalArgumentException("LocatorSuggestion cannot be null.");
            }

if (context.getPageObjectPath() == null
        || context.getPageObjectPath().isBlank()) {

    throw new IllegalStateException(
            "Page Object path is missing.");
}

Path javaFile =
        Path.of(context.getPageObjectPath());

            report.setPageObjectFile(javaFile.getFileName().toString());
            report.setVariableName(context.getVariableName());
            report.setOldLocator(context.getLocatorDeclaration());

            String newLocator = buildLocatorString(suggestion);
            report.setNewLocator(newLocator);

            /*
             * STEP 1
             * Create Backup
             */
            backupManager.createBackup(javaFile);
            report.setBackupCreated(true);

            /*
             * STEP 2
             * Update Java source
             */
if (context.getVariableName() == null
        || context.getVariableName().isBlank()) {

    throw new IllegalStateException(
            "Variable name is missing.");
}

System.out.println("=================================");
System.out.println("JAVA FILE : " + javaFile);

System.out.println("VARIABLE : "
        + context.getVariableName());

System.out.println("DECLARATION :");
System.out.println(context.getLocatorDeclaration());

System.out.println("NEW LOCATOR :");
System.out.println(buildLocatorString(suggestion));
System.out.println("=================================");
            boolean updated = locatorUpdater.updateLocator(
        javaFile,
        context.getVariableName(),
        suggestion
);

            report.setRepairSuccessful(updated);

            if (updated) {
                report.setMessage("Locator repaired successfully.");
            } else {
                report.setMessage(
        "Unable to locate variable in Page Object.");
            }

        } catch (IOException e) {

            report.setRepairSuccessful(false);
            report.setMessage("IO Error : " + e.getMessage());

        } catch (Exception e) {

            report.setRepairSuccessful(false);
            report.setMessage(e.getMessage());

        }

        return report;
    }

    /**
     * Builds:
     * By.id("username")
     */
    private String buildLocatorString(LocatorSuggestion suggestion) {

        String type = suggestion.getLocatorType()
                .toLowerCase()
                .replace("by.", "")
                .trim();

        String value = suggestion.getLocatorValue();

        switch (type) {

            case "id":
                return "By.id(\"" + value + "\")";

            case "name":
                return "By.name(\"" + value + "\")";

            case "xpath":
                return "By.xpath(\"" + value + "\")";

            case "css":
            case "cssselector":
                return "By.cssSelector(\"" + value + "\")";

            case "classname":
            case "class":
                return "By.className(\"" + value + "\")";

            case "tag":
            case "tagname":
                return "By.tagName(\"" + value + "\")";

            case "linktext":
                return "By.linkText(\"" + value + "\")";

            case "partiallinktext":
                return "By.partialLinkText(\"" + value + "\")";

            default:
                return "By." + type + "(\"" + value + "\")";
        }
    }
}