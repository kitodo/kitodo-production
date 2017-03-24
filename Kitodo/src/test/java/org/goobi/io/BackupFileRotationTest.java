/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.io;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BackupFileRotationTest {

    public static final String BACKUP_FILE_NAME = "File-BackupFileRotationTest.xml";
    public static final String BACKUP_FILE_PATH = "./";

    @BeforeClass
    public static void oneTimeSetUp() {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        deleteFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        deleteFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
        deleteFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".2");
        deleteFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".3");
    }

    @Test
    public void shouldCreateSingleBackupFile() throws Exception {
        int numberOfBackups = 1;
        runBackup(numberOfBackups);
        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
    }

    @Test
    public void backupFileShouldContainSameContentAsOriginalFile() throws IOException {
        int numberOfBackups = 1;
        String content = "Test One.";
        writeFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME, content);
        runBackup(numberOfBackups);
        assertFileHasContent(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1", content);
    }

    @Test
    public void modifiedDateShouldNotChangedOnBackup() throws IOException {
        int numberOfBackups = 1;
        long originalModifiedDate = getLastModifiedFileDate(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);
        assertLastModifiedDate(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1", originalModifiedDate);
    }

    @Test
    public void shouldWriteTwoBackupFiles() throws IOException {
        int numberOfBackups = 2;

        runBackup(numberOfBackups);

        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);

        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".2");
    }

    @Test
    public void initialContentShouldEndUpInSecondBackupFileAfterTwoBackupRuns() throws IOException {
        String content1 = "Test One.";
        int numberOfBackups = 2;

        writeFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME, content1);
        runBackup(numberOfBackups);

        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);

        assertFileHasContent(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".2", content1);
    }

    @Test
    public void secondBackupFileCorrectModifiedDate() throws IOException {
        long expectedLastModifiedDate;
        int numberOfBackups = 2;
        expectedLastModifiedDate = getLastModifiedFileDate(BACKUP_FILE_PATH + BACKUP_FILE_NAME);

        runBackup(numberOfBackups);
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);

        assertLastModifiedDate(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".2", expectedLastModifiedDate);
    }

    @Test
    public void threeBackupRunsCreateThreeBackupFiles() throws IOException {
        int numberOfBackups = 3;

        runBackup(numberOfBackups);
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);

        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".2");
        assertFileExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".3");
    }

    @Test
    public void initialContentShouldEndUpInThirdBackupFileAfterThreeBackupRuns() throws IOException {
        int numberOfBackups = 3;
        String content1 = "Test One.";

        writeFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME, content1);
        runBackup(numberOfBackups);
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);
        createFile(BACKUP_FILE_PATH + BACKUP_FILE_NAME);
        runBackup(numberOfBackups);

        assertFileHasContent(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".3", content1);
    }

    @Test
    public void noBackupIsPerformedWithNumberOfBackupsSetToZero() throws Exception {
        int numberOfBackups = 0;
        runBackup(numberOfBackups);
        assertFileNotExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
    }

    @Test
    public void nothingHappensIfFilePatternDontMatch() throws Exception {
        int numberOfBackups = 1;
        runBackup(numberOfBackups, "");
        assertFileNotExists(BACKUP_FILE_PATH + BACKUP_FILE_NAME + ".1");
    }

    private void assertLastModifiedDate(String fileName, long expectedLastModifiedDate) {
        long currentLastModifiedDate = getLastModifiedFileDate(fileName);
        assertEquals("Last modified date of file " + fileName + " differ:", expectedLastModifiedDate,
                currentLastModifiedDate);
    }

    private long getLastModifiedFileDate(String fileName) {
        File testFile = new File(fileName);
        return testFile.lastModified();
    }

    private void runBackup(int numberOfBackups) throws IOException {
        runBackup(numberOfBackups, BACKUP_FILE_NAME);
    }

    private void runBackup(int numberOfBackups, String format) throws IOException {
        BackupFileRotation bfr = new BackupFileRotation();
        bfr.setNumberOfBackups(numberOfBackups);
        bfr.setProcessDataDirectory(BACKUP_FILE_PATH);
        bfr.setFormat(format);
        bfr.performBackup();
    }

    private void assertFileHasContent(String fileName, String expectedContent) throws IOException {
        File testFile = new File(fileName);
        try (FileReader reader = new FileReader(testFile); BufferedReader br = new BufferedReader(reader);) {
            String content = br.readLine();
            assertEquals("File " + fileName + " does not contain expected content:", expectedContent, content);
        }
    }

    private void assertFileExists(String fileName) {
        File newFile = new File(fileName);
        if (!newFile.exists()) {
            fail("File " + fileName + " does not exist.");
        }
    }

    private void assertFileNotExists(String fileName) {
        File newFile = new File(fileName);
        if (newFile.exists()) {
            fail("File " + fileName + " should not exist.");
        }
    }

    private void createFile(String fileName) throws IOException {
        File testFile = new File(fileName);
        FileWriter writer = new FileWriter(testFile);
        writer.close();
    }

    private void deleteFile(String fileName) {
        File testFile = new File(fileName);
        testFile.delete();
    }

    private void writeFile(String fileName, String content) throws IOException {
        File testFile = new File(fileName);
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(content);
        }
    }

}
