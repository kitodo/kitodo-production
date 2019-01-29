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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.FileLoader;
import org.kitodo.production.MockDatabase;
import org.kitodo.production.config.ConfigCore;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.legacy.UghImplementation;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    private static FileService fileService = new FileService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        fileService.createDirectory(URI.create(""), "1");
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProcesses() {
        await().untilAsserted(
            () -> assertEquals("Processes were not counted correctly!", Long.valueOf(3), processService.count()));
    }

    @Test
    public void shouldCountProcessesAccordingToQuery() {
        String query = matchQuery("title", "First Process").operator(Operator.AND).toString();
        await().untilAsserted(() -> assertEquals("Process was not found!", processService.count(query),
            processService.findNumberOfProcessesWithTitle("First Process")));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProcesses() throws Exception {
        Long amount = processService.countDatabaseRows();
        assertEquals("Processes were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldGetProcess() throws Exception {
        Process process = processService.getById(1);
        boolean condition = process.getTitle().equals("First process") && process.getWikiField().equals("field");
        assertTrue("Process was not found in database!", condition);

        assertEquals("Process was found but tasks were not inserted!", 5, process.getTasks().size());
    }

    @Test
    public void shouldGetAllProcesses() throws Exception {
        List<Process> processes = processService.getAll();
        assertEquals("Not all processes were found in database!", 3, processes.size());
    }

    @Test
    public void shouldGetAllProcessesInGivenRange() throws Exception {
        List<Process> processes = processService.getAll(1, 10);
        assertEquals("Not all processes were found in database!", 2, processes.size());
    }

    @Test
    public void shouldRemoveProcess() throws Exception {
        Process process = new Process();
        process.setTitle("To Remove");
        processService.save(process);
        Process foundProcess = processService.getById(4);
        assertEquals("Additional process was not inserted in database!", "To Remove", foundProcess.getTitle());

        processService.remove(foundProcess);
        exception.expect(DAOException.class);
        processService.getById(6);

        process = new Process();
        process.setTitle("To remove");
        processService.save(process);
        foundProcess = processService.getById(5);
        assertEquals("Additional process was not inserted in database!", "To remove", foundProcess.getTitle());

        processService.remove(5);
        exception.expect(DAOException.class);
        processService.getById(5);
    }

    @Test
    public void shouldFindById() {
        Integer expected = 1;
        await().untilAsserted(
            () -> assertEquals("Process was not found in index!", expected, processService.findById(1).getId()));
    }

    @Test
    public void shouldFindByTitle() {
        await().untilAsserted(() -> assertEquals("Process was not found in index!", 1,
            processService.findByTitle("First process", true).size()));
    }

    @Test
    public void shouldFindByBatchId() {
        await().untilAsserted(
            () -> assertEquals("Process was not found in index!", 1, processService.findByBatchId(1).size()));
    }

    @Test
    public void shouldNotFindByBatchId() {
        await().untilAsserted(
            () -> assertEquals("Some processes were found in index!", 0, processService.findByBatchId(2).size()));
    }

    @Test
    public void shouldFindByBatchTitle() {
        await().untilAsserted(() -> assertEquals("Process was not found in index!", 1,
            processService.findByBatchTitle("First batch").size()));
    }

    @Test
    public void shouldNotFindByBatchTitle() {
        await().untilAsserted(
            () -> assertEquals("Process was found in index!", 0, processService.findByBatchTitle("Some batch").size()));
    }

    @Test
    public void shouldFindByProjectId() {
        await().untilAsserted(
            () -> assertEquals("Process was not found in index!", 2, processService.findByProjectId(1, true).size()));
    }

    @Test
    public void shouldNotFindByProjectId() {
        await().untilAsserted(() -> assertEquals("Some processes were found in index!", 0,
            processService.findByProjectId(2, true).size()));
    }

    @Test
    public void shouldFindByProjectTitle() {
        await().untilAsserted(() -> assertEquals("Process was not found in index!", 2,
            processService.findByProjectTitle("First project").size()));
    }

    @Test
    public void shouldNotFindByProjectTitle() {
        await().untilAsserted(() -> assertEquals("Process was found in index!", 0,
            processService.findByProjectTitle("Some project").size()));
    }

    @Test
    public void shouldFindManyByProperty() {
        await().untilAsserted(() -> assertEquals("Processes were not found in index!", 2,
            processService.findByProcessProperty("Korrektur notwendig", null, true).size()));
    }

    @Test
    public void shouldFindOneByProperty() {
        await().untilAsserted(() -> assertEquals("Process was not found in index!", 1,
            processService.findByProcessProperty("Process Property", "first value", true).size()));
    }

    @Test
    public void shouldNotFindByProperty() {
        await().untilAsserted(() -> assertEquals("Process was not found in index!", 0,
            processService.findByProcessProperty("firstTemplate title", "first value", true).size()));
    }

    @Test
    public void shouldGetBatchesByType() throws Exception {
        Process process = processService.getById(1);
        List<Batch> batches = processService.getBatchesByType(process, LOGISTIC);
        assertEquals("Table size is incorrect!", 1, batches.size());
    }

    @Ignore("for second process is attached task which is processed by blocked user")
    @Test
    public void shouldGetBlockedUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

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
        URI directory = processService.getImagesTifDirectory(true, process.getId(), process.getTitle(),
            process.getProcessBaseUri());
        boolean condition = directory.getRawPath().contains("First__process_tif");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);

        directory = processService.getImagesTifDirectory(false, process.getId(), process.getTitle(),
            process.getProcessBaseUri());
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
        URI directory = processService.getImagesOriginDirectory(false, process);
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
        boolean condition = directory.getRawPath().contains("1/images/First__process_tif");
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

    @Ignore("batches are right now excluded from DTO list of processes")
    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessDTO process = processService.findById(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue("BatchId doesn't match to given plain text!", condition);
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
        Process process = processService.getById(1);
        Task actual = processService.getCurrentTask(process);
        assertEquals("Task doesn't match to given task!", 8, actual.getId().intValue());
    }

    @Test
    public void shouldGetProgress() throws Exception {
        Process process = processService.getById(1);

        String progress = processService.getProgress(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", "040020020020", progress);
    }

    @Test
    public void shouldGetProgressClosed() throws Exception {
        Process process = processService.getById(1);

        int condition = processService.getProgressClosed(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 40, condition);
    }

    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        Process process = processService.getById(1);

        int condition = processService.getProgressInProcessing(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition);
    }

    @Test
    public void shouldGetProgressOpen() throws Exception {
        Process process = processService.getById(1);

        int condition = processService.getProgressOpen(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition);
    }

    @Test
    public void shouldGetProgressLocked() throws Exception {
        Process process = processService.getById(1);

        int condition = processService.getProgressLocked(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition);
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

    @Test
    public void shouldReadMetadataFile() throws Exception {
        FileLoader.createMetadataFile();

        Process process = processService.getById(1);
        DigitalDocumentInterface digitalDocument = processService.readMetadataFile(process).getDigitalDocument();

        String processTitle = process.getTitle();
        String processTitleFromMetadata = digitalDocument.getLogicalDocStruct().getAllMetadata().get(0).getValue();
        assertEquals("It was not possible to read metadata file!", processTitle, processTitleFromMetadata);

        FileLoader.deleteMetadataFile();
    }

    @Test
    public void shouldReadMetadataAsTemplateFile() throws Exception {
        FileLoader.createMetadataTemplateFile();

        Process process = processService.getById(1);
        FileformatInterface fileFormat = processService.readMetadataAsTemplateFile(process);
        assertTrue("Read template file has incorrect file format!", fileFormat instanceof MetsModsInterface);
        int metadataSize = fileFormat.getDigitalDocument().getLogicalDocStruct().getAllMetadata().size();
        assertEquals("It was not possible to read metadata as template file!", 1, metadataSize);

        FileLoader.deleteMetadataTemplateFile();
    }

    @Ignore("PreferencesException: Can't obtain DigitalDocument! Maybe wrong preferences file? - METS node")
    @Test
    public void shouldWriteMetadataAsTemplateFile() throws Exception {
        Process process = processService.getById(1);
        PrefsInterface preferences = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        fileService.writeMetadataAsTemplateFile(UghImplementation.INSTANCE.createMetsMods(preferences), process);
        boolean condition = fileService.fileExist(URI.create("1/template.xml"));
        assertTrue("It was not possible to write metadata as template file!", condition);

        Files.deleteIfExists(Paths.get(ConfigCore.getKitodoDataDirectory() + "1/template.xml"));
    }

    @Test
    public void shouldCheckIfIsImageFolderInUse() throws Exception {
        Process process = processService.getById(2);
        boolean condition = !processService.isImageFolderInUse(process);
        assertTrue("Image folder is in use but it shouldn't be!", condition);

        process = processService.getById(1);
        condition = processService.isImageFolderInUse(process);
        assertTrue("Image folder is not in use but it should be!", condition);
    }

    @Test
    public void shouldGetImageFolderInUseUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        Process process = processService.getById(1);
        User expected = userService.getById(2);
        User actual = processService.getImageFolderInUseUser(process);
        assertEquals("Processing user doesn't match to the given user!", expected, actual);
    }

    @Test
    public void shouldAddToWikiField() throws Exception {
        Process process = processService.getById(1);
        process.setWikiField(process.getWikiField() + "<p>test</p>");
        Process actual = processService.addToWikiField("test", process);
        assertEquals("Processes have different wikiField values!", process, actual);
    }

    @Ignore("find out what exactly was created")
    @Test
    public void shouldCreateProcessDirs() throws Exception {
        Process process = processService.getById(2);
        processService.createProcessDirs(process);
        // assertEquals("Process directories are not created!", expected, actual);
    }

    @Test
    public void shouldGetDigitalDocument() throws Exception {
        FileLoader.createMetadataFile();

        DigitalDocumentInterface actual = processService.getDigitalDocument(processService.getById(1));
        assertEquals("Metadata size in digital document is incorrect!", 1,
            actual.getLogicalDocStruct().getAllMetadata().size());

        FileLoader.deleteMetadataFile();
    }

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() throws Exception {
        ProcessDTO process = processService.findById(1);
        List<PropertyDTO> properties = processService.getSortedCorrectionSolutionMessages(process);

        int actual = properties.size();
        assertEquals("Size of sorted correction messages is not equal to given size!", 2, actual);

        actual = properties.get(0).getId();
        assertEquals("Process property id is not equal to given process property id!", 2, actual);

        actual = properties.get(1).getId();
        assertEquals("Process property id is not equal to given process property id!", 3, actual);

        process = processService.findById(3);
        properties = processService.getSortedCorrectionSolutionMessages(process);

        actual = properties.size();
        assertEquals("Size of sorted correction messages is not equal to given size!", 0, actual);
    }

    @Test
    public void shouldBeProcessAssignedToOnlyOneLogisticBatch() throws Exception {
        ProcessDTO processDTO = processService.findById(1);
        assertTrue(processService.isProcessAssignedToOnlyOneLogisticBatch(processDTO.getBatches()));
    }

    @Test
    public void shouldNotBeProcessAssignedToOnlyOneLogisticBatch() throws Exception {
        ProcessDTO processDTO = processService.findById(2);
        assertFalse(processService.isProcessAssignedToOnlyOneLogisticBatch(processDTO.getBatches()));
    }
}
