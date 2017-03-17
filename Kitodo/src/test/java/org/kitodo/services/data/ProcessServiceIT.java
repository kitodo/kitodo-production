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

package org.kitodo.services.data;

import de.sub.goobi.helper.FilesystemHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

import ugh.dl.DigitalDocument;

import static org.junit.Assert.*;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProcess() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = process.getTitle().equals("First process") && process.getOutputName().equals("Test");
        assertTrue("Process was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProcesses() throws Exception {
        ProcessService processService = new ProcessService();

        List<Process> processes = processService.findAll();
        assertEquals("Not all processes were found in database!", 3, processes.size());
    }

    @Test
    public void shouldGetBatchesByType() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = processService.getBatchesByType(process, LOGISTIC).size() == 1;
        assertTrue("Table size is incorrect!", condition);
    }

    @Ignore("for second process is attached task which is processed by blocked user")
    @Test
    public void shouldGetBlockedUsers() throws Exception {
        ProcessService processService = new ProcessService();
        UserService userService = new UserService();

        Process process = processService.find(1);
        boolean condition = processService.getBlockedUsers(process) == null;
        assertTrue("Process has blocked user but it shouldn't!", condition);

        process = processService.find(2);
        condition = processService.getBlockedUsers(process) == userService.find(3);
        assertTrue("Blocked user doesn't match to given user!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesTifDirectory(true, process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);

        directory = processService.getImagesTifDirectory(false, process);
        condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);
        //I don't know what changes this useFallback so I'm testing for both cases
    }

    @Ignore("not sure how method works")
    @Test
    public void shouldCheckIfTifDirectoryExists() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        //it is weird but it says that it doesn't exist....
        FilesystemHelper.createDirectory("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        boolean condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory doesn't exist!", condition);

        process = processService.find(2);
        condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory exists, but it shouldn't!", !condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetImagesOrigDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesOrigDirectory(false, process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\master_First process_media\\");
        assertTrue("Images orig directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetImagesDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\");
        assertTrue("Images directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetSourceDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getSourceDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_source");
        assertTrue("Source directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetProcessDataDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getProcessDataDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\");
        assertTrue("Process data directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetOcrDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getOcrDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\");
        assertTrue("OCR directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetTxtDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getTxtDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_txt\\");
        assertTrue("TXT directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetWordDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getWordDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_wc\\");
        assertTrue("Word directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetPdfDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getPdfDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_pdf\\");
        assertTrue("PDF directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetAltoDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getAltoDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_alto\\");
        assertTrue("Alto directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetImportDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImportDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\import\\");
        assertTrue("Import directory doesn't match to given directory!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetProcessDataDirectoryIgnoreSwapping() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getProcessDataDirectoryIgnoreSwapping(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\");
        assertTrue("Process data directory ignore swapping doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue("BatchId doesn't match to given plain text!", condition);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        int actual = processService.getTasksSize(process);
        assertEquals("Tasks' size is incorrect!", 1, actual);

        process = processService.find(2);
        actual = processService.getTasksSize(process);
        assertEquals("Tasks' size is incorrect!", 3, actual);
    }

    @Test
    public void shouldGetHistorySize() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        int actual = processService.getHistorySize(process);
        assertEquals("History's size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        int actual = processService.getPropertiesSize(process);
        assertEquals("Properties' size is incorrect!", 2, actual);
    }

    @Test
    public void shouldGetWorkpiecesSize() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        int actual = processService.getWorkpiecesSize(process);
        assertEquals("Workpieces' size is incorrect!", 2, actual);
    }

    @Test
    public void shouldGetTemplatesSize() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        int actual = processService.getTemplatesSize(process);
        assertEquals("Templates' size is incorrect!", 2, actual);
    }

    @Test
    public void shouldGetCurrentTask() throws Exception {
        ProcessService processService = new ProcessService();
        TaskService taskService = new TaskService();

        Process process = processService.find(2);
        Task actual = processService.getCurrentTask(process);
        Task expected = taskService.find(2);
        assertEquals("Task doesn't match to given task!", expected, actual);
    }

    @Test
    public void shouldGetCreationDateAsString() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        String expected = "2017-01-20 00:00:00";
        String actual = processService.getCreationDateAsString(process);
        assertEquals("Creation date doesn't match to given plain text!", expected, actual);
    }

    @Ignore("progress contains only 000000")
    @Test
    public void shouldGetProgress() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //System.out.println("Progress: " + processService.getProgress(process));
        boolean condition = processService.getProgress(process).equals("");
        assertTrue("Progress doesn't match given plain text!", condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressOpen() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //System.out.println("Progress: " + processService.getProgressOpen(process));
        int condition = processService.getProgressOpen(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //System.out.println("Progress: " + processService.getProgressInProcessing(process));
        int condition = processService.getProgressInProcessing(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressClosed() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //System.out.println("Progress: " + processService.getProgressClosed(process));
        int condition = processService.getProgressClosed(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetMetadataFilePath() throws Exception {
        ProcessService processService = new ProcessService();

        //TODO: solve problem of paths - it will be done with Path class!
        Process process = processService.find(1);
        String directory = processService.getMetadataFilePath(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\meta.xml");
        assertTrue("Metadata file path doesn't match to given file path!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetTemplateFilePath() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getTemplateFilePath(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\template.xml");
        assertTrue("Template file path doesn't match to given file path!", condition);
    }

    @Ignore("travis doesn't have this folder")
    @Test
    public void shouldGetFulltextFilePath() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getFulltextFilePath(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\fulltext.xml");
        assertTrue("Fulltext file path doesn't match to given file path!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldReadMetadataFile() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println(processService.readMetadataFile(process));
        boolean condition = processService.readMetadataFile(process).equals("");
        assertTrue("It was not possible to read metadata file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldWriteMetadataFile() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        //boolean condition = processService.writeMetadataFile(process).equals("");
        //assertTrue("It was not possible to write metadata file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldReadMetadataAsTemplateFile() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println(processService.readMetadataAsTemplateFile(process));
        boolean condition = processService.readMetadataAsTemplateFile(process).equals("");
        assertTrue("It was not possible to read metadata as template file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldWriteMetadataAsTemplateFile() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        //should return true or false
        processService.writeMetadataAsTemplateFile(null, process);
        boolean condition = false;
        assertTrue("It was not possible to write metadata as template file!", condition);
    }

    @Test
    public void shouldGetContainsUnreachableSteps() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(3);
        boolean condition = processService.getContainsUnreachableSteps(process);
        assertTrue("Process doesn't contain unreachable tasks!", condition);
    }

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldCheckIfIsImageFolderInUse() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = !processService.isImageFolderInUse(process);
        assertTrue("Image folder is in use but it shouldn't be!", condition);

        process = processService.find(2);
        condition = processService.isImageFolderInUse(process);
        assertTrue("Image folder is not in use but it should be!", condition);
    }

    @Test
    public void shouldGetImageFolderInUseUser() throws Exception {
        ProcessService processService = new ProcessService();
        UserService userService = new UserService();

        Process process = processService.find(2);
        System.out.println(process.getTasks().get(2).getProcessingStatusEnum().getTitle());
        User expected = userService.find(2);
        User actual = processService.getImageFolderInUseUser(process);
        assertEquals("Processing user doesn't match to the given user!", expected, actual);
    }

    @Ignore(" java.lang.NullPointerException at org.kitodo.services.ProcessService.downloadDocket(ProcessService.java:984)")
    @Test
    public void shouldDownloadDocket() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //TODO: method downloadDocket should return boolean not empty string
        boolean condition = processService.downloadDocket(process).equals("");
        assertTrue("Processing user doesn't match to the given user!", condition);
    }

    @Test
    public void shouldGetFirstOpenStep() throws Exception {
        ProcessService processService = new ProcessService();
        TaskService taskService = new TaskService();

        Process process = processService.find(2);
        Task expected = taskService.find(2);
        Task actual = processService.getFirstOpenStep(process);
        assertEquals("First open task doesn't match to the given task!", expected, actual);
    }

    @Test
    public void shouldAddToWikiField() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        process.setWikiField(process.getWikiField() + "<p>test</p>");
        Process actual = processService.addToWikiField("test", process);
        assertEquals("Processes have different wikiField values!", process, actual);
    }

    @Ignore("find out what exactly was created")
    @Test
    public void shouldCreateProcessDirs() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        //assertEquals("Process directories are not created!", expected, actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldGetDigitalDocument() throws Exception {
        ProcessService processService = new ProcessService();

        DigitalDocument expected = new DigitalDocument();
        DigitalDocument actual = processService.getDigitalDocument(processService.find(2));
        //assertEquals("Digital documents are not equal!", expected, actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldFilterForCorrectionSolutionMessages() throws Exception {
        ProcessService processService = new ProcessService();

        List<ProcessProperty> expected = new ArrayList<>();
        List<ProcessProperty> actual = processService.filterForCorrectionSolutionMessages(new ArrayList<ProcessProperty>());
        assertEquals("Process properties are not equal to given process properties!", expected, actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldGetSortedCorrectionSolutionMessages() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        List<ProcessProperty> expected = new ArrayList<>();
        List<ProcessProperty> actual = processService.getSortedCorrectionSolutionMessages(process);
        assertEquals("Process properties are not equal to given process properties!", expected, actual);
    }
}
