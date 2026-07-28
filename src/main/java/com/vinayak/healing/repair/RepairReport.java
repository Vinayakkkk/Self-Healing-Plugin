package com.vinayak.healing.repair;

import java.time.LocalDateTime;

public class RepairReport {

    private String pageObjectFile;
    private String variableName;

    private String oldLocator;
    private String newLocator;

    private boolean backupCreated;
    private boolean repairSuccessful;

    private LocalDateTime timestamp;

    private String message;

    public RepairReport() {
        this.timestamp = LocalDateTime.now();
    }

    public String getPageObjectFile() {
        return pageObjectFile;
    }

    public void setPageObjectFile(String pageObjectFile) {
        this.pageObjectFile = pageObjectFile;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getOldLocator() {
        return oldLocator;
    }

    public void setOldLocator(String oldLocator) {
        this.oldLocator = oldLocator;
    }

    public String getNewLocator() {
        return newLocator;
    }

    public void setNewLocator(String newLocator) {
        this.newLocator = newLocator;
    }

    public boolean isBackupCreated() {
        return backupCreated;
    }

    public void setBackupCreated(boolean backupCreated) {
        this.backupCreated = backupCreated;
    }

    public boolean isRepairSuccessful() {
        return repairSuccessful;
    }

    public void setRepairSuccessful(boolean repairSuccessful) {
        this.repairSuccessful = repairSuccessful;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RepairReport{" +
                "pageObjectFile='" + pageObjectFile + '\'' +
                ", variableName='" + variableName + '\'' +
                ", oldLocator='" + oldLocator + '\'' +
                ", newLocator='" + newLocator + '\'' +
                ", backupCreated=" + backupCreated +
                ", repairSuccessful=" + repairSuccessful +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}