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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.index.query.QueryBuilders.matchQuery;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.converter.ProcessConverter;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.enums.ProcessState;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.test.utils.ProcessTestUtils;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    private static final FileService fileService = new FileService();
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final String TEST_PROCESS_TITLE = "Test process";
    private static final String TEST_METADATA_FILE = "testMetadataFileServiceTest.xml";
    private static final String firstProcess = "First process";
    private static final String processNotFound = "Process was not found in index!";
    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
    private static Map<String, Integer> testProcessIds;

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        testProcessIds = MockDatabase.insertProcessesForHierarchyTests();
        ProcessTestUtils.copyHierarchyTestFiles(testProcessIds);
        MockDatabase.setUpAwaitility();
        fileService.createDirectory(URI.create(""), "1");
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        ProcessTestUtils.removeTestProcess(testProcessIds.get(MockDatabase.HIERARCHY_PARENT));
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
    }

    @Test
    public void shouldCountAllProcesses() throws DataException {
        assertEquals(Long.valueOf(7), processService.count(), "Processes were not counted correctly!");
    }

    @Test
    public void shouldCountProcessesAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", firstProcess).operator(Operator.AND);
        assertEquals(processService.count(query), processService.findNumberOfProcessesWithTitle(firstProcess), "Process was not found!");
    }

    @Test
    public void shouldFindProcessesWithExactTitleMatch() throws DataException {
        // Mock database contains process with title "First process", which should _not_ be found when searching for processes with title "First"
        assertEquals(Long.valueOf(0), processService.findNumberOfProcessesWithTitle("First"), "Found wrong process!");
        // Process with title "First process" should only be found when searching for that exact title
        assertEquals(Long.valueOf(1), processService.findNumberOfProcessesWithTitle("First process"), "Did not find correct process!");
    }

    @Test
    public void shouldCountAllDatabaseRowsForProcesses() throws Exception {
        Long amount = processService.countDatabaseRows();
        assertEquals(Long.valueOf(7), amount, "Processes were not counted correctly!");
    }

    @Test
    public void shouldFindByInChoiceListShown() throws DataException, DAOException {
        List<Process> byInChoiceListShown = ServiceManager.getProcessService().getTemplateProcesses();
        assertEquals(1, byInChoiceListShown.size(), "wrong amount of processes found");
    }

    @Test
    public void shouldSaveProcess() throws Exception {
        Process parent = new Process();
        parent.setTitle("Parent");

        Process process = new Process();
        process.setTitle("Child");
        process.setParent(parent);
        parent.getChildren().add(process);

        processService.save(process);

        Process foundProcess = processService.getById(process.getId());
        Process foundParent = foundProcess.getParent();

        assertEquals("Child", foundProcess.getTitle(), "Child process was not found in database!");
        assertEquals("Parent", foundParent.getTitle(), "Parent process was not assigned to child!");

        foundParent.getChildren().clear();
        foundProcess.setParent(null);

        processService.remove(foundProcess);
        processService.remove(foundParent.getId());
    }

    @Test
    public void shouldGetProcess() throws Exception {
        Process process = processService.getById(1);
        boolean condition = process.getTitle().equals(firstProcess) && process.getWikiField().equals("field");
        assertTrue(condition, "Process was not found in database!");

        assertEquals(5, process.getTasks().size(), "Process was found but tasks were not inserted!");
    }

    @Test
    public void shouldGetAllProcesses() throws Exception {
        List<Process> processes = processService.getAll();
        assertEquals(7, processes.size(), "Not all processes were found in database!");
    }

    @Test
    public void shouldGetAllProcessesInGivenRange() throws Exception {
        List<Process> processes = processService.getAll(1, 10);
        assertEquals(6, processes.size(), "Not all processes were found in database!");
    }

    @Test
    public void shouldRemoveProcess() throws Exception {
        Process process = new Process();
        process.setTitle("To Remove");
        processService.save(process);
        Process foundProcess = processService.getById(process.getId());
        assertEquals("To Remove", foundProcess.getTitle(), "Additional process was not inserted in database!");

        processService.remove(foundProcess);
        assertThrows(DAOException.class, () -> processService.getById(10));

        process = new Process();
        process.setTitle("To remove");
        processService.save(process);
        int processId = process.getId();
        foundProcess = processService.getById(processId);
        assertEquals("To remove", foundProcess.getTitle(), "Additional process was not inserted in database!");

        processService.remove(processId);
        assertThrows(DAOException.class, () -> processService.getById(processId));
    }

    @Test
    public void shouldFindById() throws DataException {
        Integer expected = 1;
        assertEquals(expected, processService.findById(1).getId(), processNotFound);
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(1, processService.findByTitle(firstProcess, true).size(), processNotFound);
    }

    @Test
    public void shouldFindByMetadata() throws DataException {
        assertEquals(3, processService.findByMetadata(Collections.singletonMap("TSL_ATS", "Proc")).size(), processNotFound);
    }

    @Test
    public void shouldNotFindByMetadata() throws DataException {
        assertEquals(0, processService.findByMetadata(Collections.singletonMap("TSL_ATS", "Nope")).size(), "Process was found in index!");
    }

    @Test
    public void shouldFindByMetadataContent() throws DataException, DAOException, IOException {
        int testProcessId = MockDatabase.insertTestProcess(TEST_PROCESS_TITLE, 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        assertEquals(1, processService.findByAnything("SecondMetaShort").size(), processNotFound);
        ProcessTestUtils.removeTestProcess(testProcessId);
    }

    @Test
    public void shouldFindByLongNumberInMetadata() throws DataException, DAOException, IOException {
        int processId = MockDatabase.insertTestProcess("Test process", 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(processId, ProcessTestUtils.testFileForLongNumbers);
        assertEquals(1, processService
                .findByMetadata(Collections.singletonMap("CatalogIDDigital", "999999999999999991")).size(), processNotFound);
        assertEquals(1, processService
                .findByMetadata(Collections.singletonMap("CatalogIDDigital", "991022551489706476")).size(), processNotFound);
        assertEquals(1, processService
                .findByMetadata(Collections.singletonMap("CatalogIDDigital", "999999999999999999999999991")).size(), processNotFound);
        ProcessTestUtils.removeTestProcess(processId);
    }

    @Test
    public void shouldFindProcessWithUnderscore() throws DataException, DAOException {
        Project project = ServiceManager.getProjectService().getById(1);
        Process process = new Process();
        process.setProject(project);
        String processTitle = "Title-with-hyphen_and_underscore";
        process.setTitle(processTitle);
        ServiceManager.getProcessService().save(process);

        List<ProcessDTO> byAnything = processService.findByAnything("ith-hyphen_an");
        assertFalse(byAnything.isEmpty(), "nothing found");
        assertEquals(processTitle, byAnything.get(0).getTitle(), "wrong process found");

        ServiceManager.getProcessService().remove(process.getId());
    }

    @Test
    public void shouldFindByProjectTitleWithWildcard() throws DataException {
        assertEquals(6, processService.findByAnything("proj").size(), processNotFound);
    }

    @Test
    public void shouldNotFindByAnything() throws DataException {
        assertEquals(0, processService.findByAnything("Nope").size(), processNotFound);
    }

    @Test
    public void shouldFindByMetadataGroupContent() throws DataException, DAOException, IOException {
        int testProcessId = MockDatabase.insertTestProcess("Test process", 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        assertEquals(1, processService.findByAnything("August").size(), processNotFound);
        ProcessTestUtils.removeTestProcess(testProcessId);
    }

    @Test
    public void shouldFindByProperty() throws DataException {
        List<ProcessDTO> processByProperty = processService.findByProperty("Process Property", "first value");
        assertEquals(1, processByProperty.size());
    }

    @Test
    public void shouldNotFindByWrongPropertyTitle() throws DataException {
        List<ProcessDTO> processByProperty = processService.findByProperty("test Property", "first value");
        assertTrue(processByProperty.isEmpty());
    }

    @Test
    public void shouldNotFindByWrongPropertyTitleAndValue() throws DataException {
        List<ProcessDTO> processByProperty = processService.findByProperty("test Property", "test value");
        assertTrue(processByProperty.isEmpty());
    }

    @Test
    public void shouldNotFindByTokenizedPropertyTitle() throws DataException {
        List<ProcessDTO> processByProperty = processService.findByProperty("Property", "first value");
        assertTrue(processByProperty.isEmpty());
    }

    @Test
    public void shouldNotFindByTokenizedPropertyTitleAndWrongValue() throws DataException {
        List<ProcessDTO> processByProperty = processService.findByProperty("Property", "test value");
        assertTrue(processByProperty.isEmpty());
    }

    @Test
    public void shouldFindLinkableParentProcesses() throws DataException, DAOException, IOException {
        assertEquals(1, processService.findLinkableParentProcesses(MockDatabase.HIERARCHY_PARENT, 1).size(), "Processes were not found in index!");
    }

    @Disabled("for second process is attached task which is processed by blocked user")
    @Test
    public void shouldGetBlockedUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        Process process = processService.getById(1);
        boolean condition = MetadataLock.getLockUser(process.getId()) == null;
        assertTrue(condition, "Process has blocked user but it shouldn't!");

        process = processService.getById(2);
        condition = MetadataLock.getLockUser(process.getId()) == userService.getById(3);
        assertTrue(condition, "Blocked user doesn't match to given user!");
    }

    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = processService.getImagesTifDirectory(true, process.getId(), process.getTitle(),
            process.getProcessBaseUri());
        boolean condition = directory.getRawPath().contains("First__process_tif");
        assertTrue(condition, "Images TIF directory doesn't match to given directory!");

        directory = processService.getImagesTifDirectory(false, process.getId(), process.getTitle(),
            process.getProcessBaseUri());
        condition = directory.getRawPath().contains("First__process_tif");
        assertTrue(condition, "Images TIF directory doesn't match to given directory!");
        // I don't know what changes this useFallback so I'm testing for both
        // cases
    }

    @Test
    public void shouldGetImagesOrigDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = processService.getImagesOriginDirectory(false, process);
        boolean condition = directory.getRawPath().contains("orig_First__process_tif");
        assertTrue(condition, "Images orig directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetImagesDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getImagesDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images");
        assertTrue(condition, "Images directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetSourceDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getSourceDirectory(process);
        boolean condition = directory.getRawPath().contains("1/images/First__process_tif");
        assertTrue(condition, "Source directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetProcessDataDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = processService.getProcessDataDirectory(process);
        boolean condition = directory.getRawPath().contains("1");
        assertTrue(condition, "Process data directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetOcrDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getOcrDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr");
        assertTrue(condition, "OCR directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetTxtDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getTxtDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_txt");
        assertTrue(condition, "TXT directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetWordDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getWordDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_wc");
        assertTrue(condition, "Word directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetPdfDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getPdfDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_pdf");
        assertTrue(condition, "PDF directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetAltoDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getAltoDirectory(process);
        boolean condition = directory.getRawPath().contains("1/ocr/First__process_alto");
        assertTrue(condition, "Alto directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetImportDirectory() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getImportDirectory(process);
        boolean condition = directory.getRawPath().contains("1/import");
        assertTrue(condition, "Import directory doesn't match to given directory!");
    }

    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessDTO process = processService.findById(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue(condition, "BatchId doesn't match to given plain text!");
    }

    @Test
    public void shouldGetCurrentTask() throws Exception {
        Process process = processService.getById(1);
        Task actual = processService.getCurrentTask(process);
        assertEquals(8, actual.getId().intValue(), "Task doesn't match to given task!");
    }

    @Test
    public void shouldGetProgress() throws Exception {
        Process process = processService.getById(1);

        String progress = ProcessConverter.getCombinedProgressAsString(process, true);
        assertEquals("040020020020", progress, "Progress doesn't match given plain text!");
    }

    @Test
    public void shouldGetProgressClosed() throws Exception {
        Process process = processService.getById(1);

        double condition = ProcessConverter.getTaskProgressPercentageOfProcess(process, true).get(TaskStatus.DONE);
        assertEquals(40, condition, 0, "Progress doesn't match given plain text!");
    }

    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        Process process = processService.getById(1);

        double condition = ProcessConverter.getTaskProgressPercentageOfProcess(process, true).get(TaskStatus.INWORK);
        assertEquals(20, condition, 0, "Progress doesn't match given plain text!");
    }

    @Test
    public void testGetQueryForClosedProcesses() throws DataException, DAOException {
        ProcessService processService = ServiceManager.getProcessService();
        Process secondProcess = processService.getById(2);
        final String sortHelperStatusOld = secondProcess.getSortHelperStatus();
        secondProcess.setSortHelperStatus(ProcessState.COMPLETED.getValue());
        processService.save(secondProcess);

        QueryBuilder querySortHelperStatusTrue = processService.getQueryForClosedProcesses();
        List<ProcessDTO> byQuery = processService.findByQuery(querySortHelperStatusTrue, true);

        assertEquals(1, byQuery.size(), "Found the wrong amount of Processes");
        secondProcess.setSortHelperStatus(sortHelperStatusOld);
        processService.save(secondProcess);
    }

    @Test
    public void shouldGetProgressOpen() throws Exception {
        Process process = processService.getById(1);

        double condition = ProcessConverter.getTaskProgressPercentageOfProcess(process, true).get(TaskStatus.OPEN);
        assertEquals(20, condition, 0, "Progress doesn't match given plain text!");
    }

    @Test
    public void shouldGetProgressLocked() throws Exception {
        Process process = processService.getById(1);

        double condition = ProcessConverter.getTaskProgressPercentageOfProcess(process, true).get(TaskStatus.LOCKED);
        assertEquals(20, condition, 0, "Progress doesn't match given plain text!");
    }

    @Test
    public void shouldGetMetadataFilePath() throws Exception {
        int testProcessId = MockDatabase.insertTestProcess(TEST_PROCESS_TITLE, 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        Process process = processService.getById(testProcessId);
        URI directory = fileService.getMetadataFilePath(process);
        boolean condition = directory.getRawPath().contains(testProcessId + "/meta.xml");
        assertTrue(condition, "Metadata file path doesn't match to given file path!");
        ProcessTestUtils.removeTestProcess(testProcessId);
    }

    @Test
    public void shouldGetTemplateFilePath() throws Exception {
        Process process = processService.getById(1);
        URI directory = fileService.getTemplateFile(process);
        boolean condition = directory.getRawPath().contains("1/template.xml");
        assertTrue(condition, "Template file path doesn't match to given file path!");
    }

    @Test
    public void shouldReadMetadataFile() throws Exception {
        FileLoader.createMetadataFile();

        Process process = processService.getById(1);
        LegacyMetsModsDigitalDocumentHelper digitalDocument = processService.readMetadataFile(process)
                .getDigitalDocument();

        String processTitle = process.getTitle();
        String processTitleFromMetadata = digitalDocument.getLogicalDocStruct().getAllMetadata().get(0).getValue();
        assertEquals(processTitle, processTitleFromMetadata, "It was not possible to read metadata file!");

        FileLoader.deleteMetadataFile();
    }

    @Test
    public void shouldReadMetadataAsTemplateFile() throws Exception {
        FileLoader.createMetadataTemplateFile();

        Process process = processService.getById(1);
        LegacyMetsModsDigitalDocumentHelper fileFormat = processService.readMetadataAsTemplateFile(process);
        assertNotNull(fileFormat, "Read template file has incorrect file format!");
        int metadataSize = fileFormat.getDigitalDocument().getLogicalDocStruct().getAllMetadata().size();
        assertEquals(1, metadataSize, "It was not possible to read metadata as template file!");

        FileLoader.deleteMetadataTemplateFile();
    }

    @Disabled("PreferencesException: Can't obtain DigitalDocument! Maybe wrong preferences file? - METS node")
    @Test
    public void shouldWriteMetadataAsTemplateFile() throws Exception {
        Process process = processService.getById(1);
        LegacyPrefsHelper preferences = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        fileService.writeMetadataAsTemplateFile(
            new LegacyMetsModsDigitalDocumentHelper(preferences.getRuleset()), process);
        boolean condition = fileService.fileExist(URI.create("1/template.xml"));
        assertTrue(condition, "It was not possible to write metadata as template file!");

        Files.deleteIfExists(Paths.get(ConfigCore.getKitodoDataDirectory() + "1/template.xml"));
    }

    @Test
    public void shouldCheckIfIsImageFolderInUse() throws Exception {
        Process process = processService.getById(2);
        boolean condition = !processService.isImageFolderInUse(process);
        assertTrue(condition, "Image folder is in use but it shouldn't be!");

        process = processService.getById(1);
        condition = processService.isImageFolderInUse(process);
        assertTrue(condition, "Image folder is not in use but it should be!");
    }

    @Test
    public void shouldGetImageFolderInUseUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        Process process = processService.getById(1);
        User expected = userService.getById(2);
        User actual = processService.getImageFolderInUseUser(process);
        assertEquals(expected, actual, "Processing user doesn't match to the given user!");
    }

    @Test
    public void shouldGetDigitalDocument() throws Exception {
        FileLoader.createMetadataFile();

        LegacyMetsModsDigitalDocumentHelper actual = processService.getDigitalDocument(processService.getById(1));
        assertEquals(1, actual.getLogicalDocStruct().getAllMetadata().size(), "Metadata size in digital document is incorrect!");

        FileLoader.deleteMetadataFile();
    }

    @Test
    public void shouldBeProcessAssignedToOnlyOneBatch() throws Exception {
        ProcessDTO processDTO = processService.findById(2);
        assertTrue(processService.isProcessAssignedToOnlyOneBatch(processDTO.getBatches()));
    }

    @Test
    public void shouldNotBeProcessAssignedToOnlyOneBatch() throws Exception {
        ProcessDTO processDTO = processService.findById(1);
        assertFalse(processService.isProcessAssignedToOnlyOneBatch(processDTO.getBatches()));
    }

    @Test
    public void shouldUpdateChildrenFromLogicalStructure() throws Exception {
        LinkedMetsResource childToKeepLink = new LinkedMetsResource();
        childToKeepLink.setUri(processService.getProcessURI(processService.getById(5)));
        LogicalDivision childToKeepLogicalDivision = new LogicalDivision();
        childToKeepLogicalDivision.setLink(childToKeepLink);
        LogicalDivision logicalStructure = new LogicalDivision();
        logicalStructure.getChildren().add(childToKeepLogicalDivision);
        LinkedMetsResource childToAddLink = new LinkedMetsResource();
        childToAddLink.setUri(processService.getProcessURI(processService.getById(7)));
        LogicalDivision childToAddLogicalDivision = new LogicalDivision();
        childToAddLogicalDivision.setLink(childToAddLink);
        logicalStructure.getChildren().add(childToAddLogicalDivision);

        Process process = processService.getById(4);

        processService.updateChildrenFromLogicalStructure(process, logicalStructure);

        for (Process child : process.getChildren()) {
            assertTrue(Arrays.asList("HierarchyChildToKeep", "HierarchyChildToAdd").contains(child.getTitle()), "Process should have child to keep and child to add as only children");
            assertEquals(process, child.getParent(), "Child should have parent as parent");
        }
        assertNull(processService.getById(6).getParent(), "Process to remove should have no parent");
    }

    @Test
    public void testFindAllIDs() throws DataException {
        List<Integer> allIDs = ServiceManager.getProcessService().findAllIDs();
        assertEquals(7, allIDs.size(), "Wrong amount of id's in index");
        assertTrue(allIDs.containsAll(Arrays.asList(5, 2, 6, 4, 1, 7, 3)), "id's contain wrong entries");

        allIDs = ServiceManager.getProcessService().findAllIDs(0L, 5);
        assertEquals(5, allIDs.size(), "Wrong amount of id's in index");
        assertEquals(allIDs.size(), new HashSet<>(allIDs).size(), "Duplicate ids in index");
        OptionalInt maxStream = allIDs.stream().mapToInt(Integer::intValue).max();
        assertTrue(maxStream.isPresent(), "Unable to find largest ID in stream of all process IDs!");
        int maxId = maxStream.getAsInt();
        int minId = allIDs.stream().mapToInt(Integer::intValue).min().getAsInt();
        assertTrue(maxId < 8, "Ids should all be smaller than 8");
        assertTrue(minId > 0, "Ids should all be larger than 0");
        
        allIDs = ServiceManager.getProcessService().findAllIDs(5L, 10);
        assertEquals(2, allIDs.size(), "Wrong amount of id's in index");
    }

    @Test
    public void testCountMetadata() throws DAOException, IOException, DataException {
        int testProcessId = MockDatabase.insertTestProcess(TEST_PROCESS_TITLE, 1, 1, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        Process process = ServiceManager.getProcessService().getById(testProcessId);
        URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
        long logicalMetadata = MetsService.countLogicalMetadata(workpiece);
        assertEquals(4, logicalMetadata, "Wrong amount of metadata found!");
        ProcessTestUtils.removeTestProcess(testProcessId);
    }

    @Test
    public void updateInternalMetaInformation() throws DAOException, IOException, DataException {
        int testProcessId = MockDatabase.insertTestProcess(TEST_PROCESS_TITLE, 3, 1, 1);
        Process process = processService.getById(testProcessId);

        // pre database checks
        assertEquals(0, process.getSortHelperImages());
        assertEquals(0, process.getSortHelperMetadata());
        assertEquals(0, process.getSortHelperDocstructs());

        // provide some meta data
        ProcessTestUtils.copyTestMetadataFile(testProcessId, TEST_METADATA_FILE);
        // copyTestMetadataFile is already updating process information
        // but in case that this is removed update it again.
        processService.updateAmountOfInternalMetaInformation(process, true);

        // after database checks
        assertEquals(1, process.getSortHelperImages());
        assertEquals(4, process.getSortHelperMetadata());
        assertEquals(1, process.getSortHelperDocstructs());
    }
}
