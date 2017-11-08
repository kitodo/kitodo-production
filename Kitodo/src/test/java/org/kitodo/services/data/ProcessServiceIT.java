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

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.PropertyDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import ugh.dl.DigitalDocument;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    private static FileService fileService = new FileService();
    private static final ProcessService processService = new ServiceManager().getProcessService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        fileService.createDirectory(URI.create(""), "1");
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProcesses() throws Exception {
        Long amount = processService.count();
        assertEquals("Processes were not counted correctly!", Long.valueOf(5), amount);
    }

    @Test
    public void shouldCountProcessesAccordingToQuery() throws Exception {
        String query = matchQuery("title", "First Process").operator(Operator.AND).toString();
        Long amount = processService.count(query);
        assertEquals("Process was not found!", Long.valueOf(1), amount);

        amount = processService.findNumberOfProcessesWithTitle("First Process");
        assertEquals("Process was not found!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForProcesses() throws Exception {
        Long amount = processService.countDatabaseRows();
        assertEquals("Processes were not counted correctly!", Long.valueOf(5), amount);
    }

    @Test
    public void shouldFindProcess() throws Exception {
        Process process = processService.getById(1);
        boolean condition = process.getTitle().equals("First process") && process.getOutputName().equals("Test");
        assertTrue("Process was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProcesses() {
        List<Process> processes = processService.getAll();
        assertEquals("Not all processes were found in database!", 5, processes.size());
    }

    @Test
    public void shouldGetAllProcessesInGivenRange() throws Exception {
        List<Process> processes = processService.getAll(3,10);
        assertEquals("Not all processes were found in database!", 2, processes.size());
    }

    @Test
    public void shouldRemoveProcess() throws Exception {
        Process process = new Process();
        process.setTitle("To Remove");
        processService.save(process);
        Process foundProcess = processService.getById(6);
        assertEquals("Additional process was not inserted in database!", "To Remove", foundProcess.getTitle());

        processService.remove(foundProcess);
        exception.expect(DAOException.class);
        processService.getById(6);

        process = new Process();
        process.setTitle("To remove");
        processService.save(process);
        foundProcess = processService.getById(7);
        assertEquals("Additional process was not inserted in database!", "To remove", foundProcess.getTitle());

        processService.remove(7);
        exception.expect(DAOException.class);
        processService.getById(7);
    }

    @Test
    public void shouldFindById() throws Exception {
        ProcessDTO process = processService.findById(1);
        Integer actual = process.getId();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JSONObject> process = processService.findByTitle("First process", true);
        Integer actual = process.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByBatchId() throws Exception {
        List<JSONObject> processes = processService.findByBatchId(1);
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
        List<JSONObject> processes = processService.findByBatchTitle("First batch");
        Integer actual = processes.size();
        Integer expected = 1;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByBatchTitle("Some batch");
        actual = processes.size();
        expected = 0;
        assertEquals("Process was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProjectId() throws Exception {
        List<ProcessDTO> processes = processService.findByProjectId(1, true);
        Integer actual = processes.size();
        Integer expected = 3;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByProjectId(2, true);
        actual = processes.size();
        expected = 0;
        assertEquals("Some processes were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProjectTitle() throws Exception {
        List<JSONObject> processes = processService.findByProjectTitle("First project");
        Integer actual = processes.size();
        Integer expected = 3;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByProjectTitle("Some project");
        actual = processes.size();
        expected = 0;
        assertEquals("Process was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProperty() throws Exception {
        List<JSONObject> processes = processService.findByProperty("Korrektur notwendig", null, true);
        Integer actual = processes.size();
        Integer expected = 2;
        assertEquals("Processes were not found in index!", expected, actual);

        processes = processService.findByProperty("Process Property", "first value", true);
        actual = processes.size();
        expected = 1;
        assertEquals("Process was not found in index!", expected, actual);

        processes = processService.findByProperty("firstTemplate title", "first value", true);
        actual = processes.size();
        expected = 0;
        assertEquals("Process was not found in index!", expected, actual);
    }

    @Test
    public void shouldGetBatchesByType() throws Exception {
        Process process = processService.getById(1);
        boolean condition = processService.getBatchesByType(process, LOGISTIC).size() == 1;
        assertTrue("Table size is incorrect!", condition);
    }

    @Ignore("for second process is attached task which is processed by blocked user")
    @Test
    public void shouldGetBlockedUser() throws Exception {
        UserService userService = new ServiceManager().getUserService();

        ProcessDTO process = processService.findById(1);
        boolean condition = processService.getBlockedUser(process) == null;
        assertTrue("Process has blocked user but it shouldn't!", condition);

        process = processService.findById(2);
        condition = processService.getBlockedUser(process) == userService.findById(3);
        assertTrue("Blocked user doesn't match to given user!", condition);
    }

    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        Process process = processService.getById(1);
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
        fileService.createDirectory(URI.create("1"), "images");
        URI directory = fileService.createDirectory(URI.create("1/images"), "First__process_tif");
        fileService.createResource(directory, "test.jpg");
        boolean condition = processService.checkIfTifDirectoryExists(1, "First process", null);
        assertTrue("Images TIF directory doesn't exist!", condition);

        condition = processService.checkIfTifDirectoryExists(2, "Second process", null);
        assertTrue("Images TIF directory exists, but it shouldn't!", !condition);
    }

    @Test
    public void shouldGetImagesOrigDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = processService.getImagesOrigDirectory(false, process);
        boolean condition = directory.getRawPath().contains("orig_First__process_tif");
        assertTrue("Images orig directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImagesDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getImagesDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images");
        assertTrue("Images directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetSourceDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getSourceDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images/First__process_source");
        assertTrue("Source directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetProcessDataDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = processService.getProcessDataDirectory(process);
        boolean condition = directory.getRawPath().contains("1");
        assertTrue("Process data directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetOcrDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getOcrDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr");
        assertTrue("OCR directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetTxtDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getTxtDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_txt");
        assertTrue("TXT directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetWordDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getWordDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_wc");
        assertTrue("Word directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetPdfDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getPdfDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_pdf");
        assertTrue("PDF directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetAltoDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getAltoDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_alto");
        assertTrue("Alto directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImportDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getImportDirectory(process);
        boolean condition = directory.getRawPath().contains("1/import");
        assertTrue("Import directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessDTO process = processService.findById(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue("BatchId doesn't match to given plain text!", condition);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        Process process = processService.getById(1);
        int actual = processService.getTasksSize(process);
        assertEquals("Tasks' size is incorrect!", 1, actual);

        process = processService.getById(2);
        actual = processService.getTasksSize(process);
        assertEquals("Tasks' size is incorrect!", 3, actual);
    }

    @Test
    public void shouldGetHistorySize() throws Exception {
        Process process = processService.getById(1);
        int actual = processService.getHistorySize(process);
        assertEquals("History's size is incorrect!", 1, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        Process process = processService.getById(1);
        int actual = processService.getPropertiesSize(process);
        assertEquals("Properties' size is incorrect!", 3, actual);
    }

    @Test
    public void shouldGetWorkpiecesSize() throws Exception {
        Process process = processService.getById(1);
        int actual = processService.getWorkpiecesSize(process);
        assertEquals("Workpieces' size is incorrect!", 2, actual);
    }

    @Test
    public void shouldGetTemplatesSize() throws Exception {
        Process process = processService.getById(1);
        int actual = processService.getTemplatesSize(process);
        assertEquals("Templates' size is incorrect!", 2, actual);
    }

    @Test
    public void shouldGetCurrentTask() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        Process process = processService.getById(2);
        Task actual = processService.getCurrentTask(process);
        Task expected = taskService.getById(2);
        assertEquals("Task doesn't match to given task!", expected, actual);
    }

    @Test
    public void shouldGetCreationDateAsString() throws Exception {
        Process process = processService.getById(2);
        String expected = "2017-01-20 00:00:00";
        String actual = processService.getCreationDateAsString(process);
        assertEquals("Creation date doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetProgress() throws Exception {
        Process process = processService.getById(2);
        String progress = processService.getProgress(process, null);
        assertEquals("Progress doesn't match given plain text!", "000033033033", progress);
    }

    @Test
    public void shouldGetProgressClosed() throws Exception {
        Process process = processService.getById(2);
        int condition = processService.getProgressClosed(process, null);
        assertEquals("Progress doesn't match given plain text!", 0, condition);
    }

    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        Process process = processService.getById(2);
        int condition = processService.getProgressInProcessing(process, null);
        assertEquals("Progress doesn't match given plain text!", 33, condition);
    }

    @Test
    public void shouldGetProgressOpen() throws Exception {
        Process process = processService.getById(2);
        int condition = processService.getProgressOpen(process, null);
        assertEquals("Progress doesn't match given plain text!", 33, condition);
    }

    @Test
    public void shouldGetProgressLocked() throws Exception {
        Process process = processService.getById(2);
        int condition = processService.getProgressLocked(process, null);
        assertEquals("Progress doesn't match given plain text!", 33, condition);
    }

    @Test
    public void shouldGetMetadataFilePath() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getMetadataFilePath(process);
        boolean condition = directory.getRawPath().contains("1/meta.xml");
        assertTrue("Metadata file path doesn't match to given file path!", condition);
    }

    @Test
    public void shouldGetTemplateFilePath() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getTemplateFile(process);
        boolean condition = directory.getRawPath().contains("1/template.xml");
        assertTrue("Template file path doesn't match to given file path!", condition);
    }

    @Test
    public void shouldGetFulltextFilePath() throws Exception {
        Process process = processService.getById(1);
        String directory = processService.getFulltextFilePath(process);
        boolean condition = directory.contains("1/fulltext.xml");
        assertTrue("Fulltext file path doesn't match to given file path!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldReadMetadataFile() throws Exception {
        Process process = processService.getById(1);
        System.out.println(processService.readMetadataFile(process));
        boolean condition = processService.readMetadataFile(process).equals("");
        assertTrue("It was not possible to read metadata file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldWriteMetadataFile() throws Exception {
        Process process = processService.getById(1);
        // boolean condition =
        // processService.writeMetadataFile(process).equals("");
        // assertTrue("It was not possible to write metadata file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldReadMetadataAsTemplateFile() throws Exception {
        Process process = processService.getById(1);
        System.out.println(processService.readMetadataAsTemplateFile(process));
        boolean condition = processService.readMetadataAsTemplateFile(process).equals("");
        assertTrue("It was not possible to read metadata as template file!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldWriteMetadataAsTemplateFile() throws Exception {
        Process process = processService.getById(1);
        // should return true or false
        fileService.writeMetadataAsTemplateFile(null, process);
        boolean condition = false;
        assertTrue("It was not possible to write metadata as template file!", condition);
    }

    @Test
    public void shouldGetContainsUnreachableSteps() throws Exception {
        Process process = processService.getById(3);
        boolean condition = processService.getContainsUnreachableSteps(process);
        assertTrue("Process doesn't contain unreachable tasks!", condition);

        ProcessDTO processDTO = processService.findById(3);
        condition = processService.getContainsUnreachableSteps(processDTO);
        assertTrue("Process DTO doesn't contain unreachable tasks!", condition);
    }

    @Test
    public void shouldCheckIfIsImageFolderInUse() throws Exception {
        Process process = processService.getById(1);
        boolean condition = !processService.isImageFolderInUse(process);
        assertTrue("Image folder is in use but it shouldn't be!", condition);

        process = processService.getById(2);
        condition = processService.isImageFolderInUse(process);
        assertTrue("Image folder is not in use but it should be!", condition);
    }

    @Test
    public void shouldGetImageFolderInUseUser() throws Exception {
        UserService userService = new ServiceManager().getUserService();

        Process process = processService.getById(2);
        System.out.println(process.getTasks().get(2).getProcessingStatusEnum().getTitle());
        User expected = userService.getById(2);
        User actual = processService.getImageFolderInUseUser(process);
        assertEquals("Processing user doesn't match to the given user!", expected, actual);
    }

    @Test
    public void shouldGetFirstOpenStep() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        Process process = processService.getById(2);
        Task expected = taskService.getById(2);
        Task actual = processService.getFirstOpenStep(process);
        assertEquals("First open task doesn't match to the given task!", expected, actual);
    }

    @Ignore("Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1")
    @Test
    public void shouldAddToWikiField() throws Exception {
        Process process = processService.getById(2);
        process.setWikiField(process.getWikiField() + "<p>test</p>");
        Process actual = processService.addToWikiField("test", process);
        assertEquals("Processes have different wikiField values!", process, actual);
    }

    @Ignore("find out what exactly was created")
    @Test
    public void shouldCreateProcessDirs() throws Exception {
        Process process = processService.getById(2);
        processService.createProcessDirs(process);
        // assertEquals("Process directories are not created!", expected,
        // actual);
    }

    @Ignore("not sure how it should look")
    @Test
    public void shouldGetDigitalDocument() throws Exception {
        DigitalDocument expected = new DigitalDocument();
        DigitalDocument actual = processService.getDigitalDocument(processService.getById(2));
        // assertEquals("Digital documents are not equal!", expected, actual);
    }

    @Test
    public void shouldFilterForCorrectionSolutionMessages() throws Exception {
        ProcessDTO process = processService.findById(1);
        List<PropertyDTO> properties = processService.filterForCorrectionSolutionMessages(process.getProperties());

        Integer expected = 2;
        Integer actual = properties.get(0).getId();
        assertEquals("Process property id is not equal to given process property id!", expected, actual);

        expected = 3;
        actual = properties.get(1).getId();
        assertEquals("Process property id is not equal to given process property id!", expected, actual);
    }

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() throws Exception {
        ProcessDTO process = processService.findById(1);
        Integer expected = 2;
        Integer actual = processService.getSortedCorrectionSolutionMessages(process).size();
        assertEquals("Size of sorted correction messages is not equal to given size!", expected, actual);
    }

    @Test
    public void shouldFindNotArchivedProcesses() throws Exception {
        List<ProcessDTO> notArchivedProcesses = processService.findNotArchivedProcesses(null);
        assertTrue("Found " + notArchivedProcesses.size() + " processes, instead of 3",
                notArchivedProcesses.size() == 3);
    }

    @Test
    public void shouldFindNotClosedProcessesWithoutTemplates() throws Exception {
        List<ProcessDTO> notClosedProcesses = processService.findNotClosedProcessesWithoutTemplates(null);
        assertTrue("Found " + notClosedProcesses.size() + " processes, instead of 3", notClosedProcesses.size() == 3);
    }

    @Test
    public void shouldFindNotClosedAndNotArchivedProcessesWithoutTemplates() throws Exception {
        List<ProcessDTO> notClosedAndNotArchivedProcesses = processService.findNotClosedAndNotArchivedProcessesWithoutTemplates(null);
        assertTrue("Found " + notClosedAndNotArchivedProcesses.size() + " processes, instead of 2",
                notClosedAndNotArchivedProcesses.size() == 2);
    }

    @Test
    public void shouldFindNotArchivedTemplates() throws Exception {
        List<ProcessDTO> notArchivedTemplates = processService.findNotArchivedTemplates(null);
        assertTrue("Found " + notArchivedTemplates.size() + " processes, instead of 1",
                notArchivedTemplates.size() == 1);
    }

    @Test
    public void shouldFindAllTemplates() throws Exception {
        List<ProcessDTO> allTemplates = processService.findAllTemplates(null);
        assertTrue("Found " + allTemplates.size() + " processes, instead of 2", allTemplates.size() == 2);
    }

    @Test
    public void shouldFindAllWithoutTemplates() throws Exception {
        List<ProcessDTO> allWithoutTemplates = processService.findAllWithoutTemplates(null);
        assertTrue("Found " + allWithoutTemplates.size() + " processes, instead of 3", allWithoutTemplates.size() == 3);
    }

    @Test
    public void shouldFindAllNotArchivedWithoutTemplates() throws Exception {
        List<ProcessDTO> notArchivedProcessesWithoutTemplates = processService.findAllNotArchivedWithoutTemplates(null);
        assertTrue("Found " + notArchivedProcessesWithoutTemplates.size() + " processes, instead of 2",
                notArchivedProcessesWithoutTemplates.size() == 2);
    }

    @Test
    public void shouldFindAllNotClosedAndNotArchivedTemplates() throws Exception {
        List<ProcessDTO> notClosedAndNotArchivedTemplates = processService.findAllNotClosedAndNotArchivedTemplates(null);
        assertTrue("Found " + notClosedAndNotArchivedTemplates.size() + " processes, instead of 1",
                notClosedAndNotArchivedTemplates.size() == 1);
    }

    @Test
    public void shouldFindAllNotClosedTemplates() throws Exception {
        List<ProcessDTO> notClosedTemplates = processService.findAllNotClosedTemplates(null);
        assertTrue("Found " + notClosedTemplates.size() + " processes, instead of 2", notClosedTemplates.size() == 2);
    }

    @Test
    public void shouldGetTemplates() throws Exception {
        List<Process> templates = processService.getProcessTemplates();
        assertTrue("Found " + templates.size() + " processes, instead of 2", templates.size() == 2);
    }

    @Test
    public void shouldGetTemplatesWithTitle() throws Exception {
        List<Process> templates = processService.getProcessTemplatesWithTitle("First process");
        assertTrue("Found " + templates.size() + " processes, instead of 1", templates.size() == 1);
    }

    @Ignore("IN clause doesn't work correctly here - to deeper check out")
    @Test
    public void shouldGetTemplatesForUser() throws Exception {
        List<Integer> projects = new ArrayList<>();
        projects.add(1);
        List<Process> templates = processService.getProcessTemplatesForUser(projects);
        assertTrue("Found " + templates.size() + " processes, instead of 1", templates.size() == 1);
    }
}
