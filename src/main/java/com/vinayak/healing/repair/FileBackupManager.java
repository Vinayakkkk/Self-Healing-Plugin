package com.vinayak.healing.repair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileBackupManager {

    /**
     * Creates a backup of the given Java source file.
     *
     * Example:
     * LoginPage.java
     *      ↓
     * LoginPage.java.bak
     *
     * @param sourceFile Path of the Java source file
     * @return Path of the created backup file
     * @throws IOException if backup creation fails
     */
    public Path createBackup(Path sourceFile) throws IOException {

        if (sourceFile == null) {
            throw new IllegalArgumentException("Source file cannot be null.");
        }

        if (!Files.exists(sourceFile)) {
            throw new IOException("Source file does not exist: " + sourceFile);
        }

        Path backupFile = getBackupFile(sourceFile);

        Files.copy(
                sourceFile,
                backupFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );

        return backupFile;
    }

    /**
     * Checks whether a backup file already exists.
     *
     * @param sourceFile Original Java file
     * @return true if backup exists
     */
    public boolean backupExists(Path sourceFile) {

        if (sourceFile == null || !Files.exists(sourceFile)) {
    return false;
}

        Path backupFile = getBackupFile(sourceFile);

        return Files.exists(backupFile);
    }

    /**
     * Deletes an existing backup file.
     *
     * @param sourceFile Original Java file
     * @return true if deleted successfully
     * @throws IOException if deletion fails
     */
    public boolean deleteBackup(Path sourceFile) throws IOException {

if (sourceFile == null || !Files.exists(sourceFile)) {
    return false;
}

        Path backupFile = getBackupFile(sourceFile);

        return Files.deleteIfExists(backupFile);
    }

    /**
     * Restores the original Java file from the backup.
     *
     * @param sourceFile Original Java file
     * @throws IOException if restore fails
     */
    public void restoreBackup(Path sourceFile) throws IOException {

        if (sourceFile == null) {
            throw new IllegalArgumentException("Source file cannot be null.");
        }

        Path backupFile = getBackupFile(sourceFile);

       if (!Files.exists(backupFile)) {
    throw new IOException("Backup file not found: " + backupFile);
}

if (!Files.exists(sourceFile)) {
    throw new IOException("Original source file not found: " + sourceFile);
}

        Files.copy(
                backupFile,
                sourceFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );
    }

    private Path getBackupFile(Path sourceFile) {

    return sourceFile.resolveSibling(
            sourceFile.getFileName().toString() + ".bak");
}
}


