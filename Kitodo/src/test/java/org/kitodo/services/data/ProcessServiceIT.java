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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.services.file.FileService;

import ugh.dl.DigitalDocument;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    private static FileService fileService = new FileService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
        fileService.createDirectory(URI.create(""), "1");
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        // MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldFindProcess() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = process.getTitle().equals("First process") && process.getOutputName().equals("Test");
        assertTrue("Process was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProcesses() {
        ProcessService processService = new ProcessService();

        List<Process> processes = processService.findAll();
        assertEquals("Not all processes were found in database!", 4, processes.size());
    }

    @Test
    public void shouldRemoveProcess() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = new Process();
        process.setTitle("To Remove");
        processService.save(process);
        Process foundProcess = processService.convertSearchResultToObject(processService.findById(5));
        assertEquals("Additional process was not inserted in database!", "To Remove", foundProcess.getTitle());

        processService.remove(foundProcess);
        foundProcess = processService.convertSearchResultToObject(processService.findById(5));
        assertEquals("Additional process was not removed from database!", null, foundProcess);

        process = new Process();
        process.setTitle("To remove");
        processService.save(process);
        foundProcess = processService.convertSearchResultToObject(processService.findById(6));
        assertEquals("Additional process was not inserted in database!", "To remove", foundProcess.getTitle());

        processService.remove(6);
        foundProcess = processService.convertSearchResultToObject(processService.findById(6));
        assertEquals("Additional process was not removed from database!", null, foundProcess);
    }

    @Test
    public void shouldFindById() throws Exception {
        ProcessService processService = new ProcessService();

        SearchResult process = processService.findById(1);
        Integer actual = process.getId();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> process = processService.findByTitle("First process", true);
        Integer actual = process.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByOutputName() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> process = processService.findByOutputName("Test");
        Integer actual = process.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByWikiField() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> process = processService.findByWikiField("wiki");
        Integer actual = process.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByBatchId() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> processes = processService.findByBatchId(1);
        Integer actual = processes.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByBatchId(2);
        actual = processes.size();
        expected = 0;
        assertEquals("Some processes were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByBatchTitle() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> processes = processService.findByBatchTitle("First batch");
        Integer actual = processes.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByBatchTitle("Some batch");
        actual = processes.size();
        expected = 0;
        assertEquals("Process was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProperty() throws Exception {
        ProcessService processService = new ProcessService();

        List<SearchResult> processes = processService.findByProperty("Process Property", "first value");
        Integer actual = processes.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByProperty("firstTemplate title", "first value");
        actual = processes.size();
        expected = 0;
        assertEquals("Process was not found in index!", expected, actual);
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

    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = processService.getImagesTifDirectory(true, process);
        boolean condition = directory.getRawPath().contains("First__process_tif");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);

        directory = processService.getImagesTifDirectory(false, process);
        condition = directory.getRawPath().contains("First__process_tif");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);
        // I don't know what changes this useFallback so I'm testing for both
        // cases
    }

    @Test
    public void shouldCheckIfTifDirectoryExists() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        fileService.createDirectory(URI.create("1"), "images");
        URI directory = fileService.createDirectory(URI.create("1/images"), "First__process_tif");
        fileService.createResource(directory, "test.jpg");
        boolean condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory doesn't exist!", condition);

        process = processService.find(2);
        condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory exists, but it shouldn't!", !condition);
    }

    @Test
    public void shouldGetImagesOrigDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = processService.getImagesOrigDirectory(false, process);
        boolean condition = directory.getRawPath().contains("orig_First__process_tif");
        assertTrue("Images orig directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImagesDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getImagesDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images");
        assertTrue("Images directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetSourceDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getSourceDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images/First__process_source");
        assertTrue("Source directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetProcessDataDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = processService.getProcessDataDirectory(process);
        boolean condition = directory.getRawPath().contains("1");
        assertTrue("Process data directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetOcrDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getOcrDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr");
        assertTrue("OCR directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetTxtDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getTxtDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_txt");
        assertTrue("TXT directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetWordDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getWordDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_wc");
        assertTrue("Word directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetPdfDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getPdfDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_pdf");
        assertTrue("PDF directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetAltoDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getAltoDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_alto");
        assertTrue("Alto directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImportDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        URI directory = fileService.getImportDirectory(process);
        boolean condition = directory.getRawPath().contains("1/import");
        assertTrue("Import directory doesn't match to given directory!", condition);
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
        // System.out.println("Progress: " +
        // processService.getProgress(process));
        boolean condition = processService.getProgress(process).equals("");
        assertTrue("Progress doesn't match given plain text!", condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressOpen() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        // System.out.println("Progress: " +
        // processService.getProgressOpen(process));
        int condition = processService.getProgressOpen(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        // System.out.println("Progress: " +
        // processService.getProgressInProcessing(process));
        int condition = processService.getProgressInProcessing(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Ignore("progress is not calculated correctly - method needs to be fix")
    @Test
    public void shouldGetProgressClosed() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(2);
        // System.out.println("Progress: " +
        // processService.getProgressClosed(process));
        int condition = processService.getProgressClosed(process);
        assertEquals("Progress doesn't match given plain text!", 1, condition);
    }

    @Test
    public void shouldGetMetadataFilePath() throws Exception {
        ProcessService processService = new ProcessService();
        FileService fileService = new FileService();

        Process process = processService.find(1);
        URI directory = fileService.getMetadataFilePath(process);
        boolean condition = directory.getRawPath().contains("1/meta.xml");
        assertTrue("Metadata file path doesn't match to given file path!", condition);
    }

    @Test
    public void shouldGetTemplateFilePath() throws Exception {
        ProcessService processService = new ProcessService();
        FileService fileService = new FileService();

        Process process = processService.find(1);
        URI directory = fileService.getTemplateFile(process);
        boolean condition = directory.getRawPath().contains("1/template.xml");
        assertTrue("Template file path doesn't match to given file path!", condition);
    }

    @Test
    public void shouldGetFulltextFilePath() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getFulltextFilePath(process);
        boolean condition = directory.contains("1/fulltext.xml");
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
        // boolean condition =
        // processService.writeMetadataFile(process).equals("");
        // assertTrue("It was not possible to write metadata file!", condition);
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
        FileService fileService = new FileService();

        Process process = processService.find(1);
        // should return true or false
        fileService.writeMetadataAsTemplateFile(null, process);
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
        processService.createProcessDirs(process);
        // assertEquals("Process directories are not created!", expected,
        // actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldGetDigitalDocument() throws Exception {
        ProcessService processService = new ProcessService();

        DigitalDocument expected = new DigitalDocument();
        DigitalDocument actual = processService.getDigitalDocument(processService.find(2));
        // assertEquals("Digital documents are not equal!", expected, actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldFilterForCorrectionSolutionMessages() {
        ProcessService processService = new ProcessService();

        List<Property> expected = new ArrayList<>();
        List<Property> actual = processService.filterForCorrectionSolutionMessages(new ArrayList<>());
        assertEquals("Process properties are not equal to given process properties!", expected, actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldGetSortedCorrectionSolutionMessages() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        List<Property> expected = new ArrayList<>();
        List<Property> actual = processService.getSortedCorrectionSolutionMessages(process);
        assertEquals("Process properties are not equal to given process properties!", expected, actual);
    }
}
