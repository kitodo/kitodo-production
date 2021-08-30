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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceIT {

    private static FileService fileService = new FileService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";
    private static final String processNotFound = "Process was not found in index!";
    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        fileService.createDirectory(URI.create(""), "1");
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProcesses() throws DataException {
        assertEquals("Processes were not counted correctly!", Long.valueOf(7), processService.count());
    }

    @Test
    public void shouldCountProcessesAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", firstProcess).operator(Operator.AND);
        assertEquals("Process was not found!", processService.count(query),
            processService.findNumberOfProcessesWithTitle(firstProcess));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProcesses() throws Exception {
        Long amount = processService.countDatabaseRows();
        assertEquals("Processes were not counted correctly!", Long.valueOf(7), amount);
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

        assertEquals("Child process was not found in database!", "Child", foundProcess.getTitle());
        assertEquals("Parent process was not assigned to child!", "Parent", foundParent.getTitle());

        foundParent.getChildren().clear();
        foundProcess.setParent(null);

        processService.remove(foundProcess);
        processService.remove(foundParent.getId());
    }

    @Test
    public void shouldGetProcess() throws Exception {
        Process process = processService.getById(1);
        boolean condition = process.getTitle().equals(firstProcess) && process.getWikiField().equals("field");
        assertTrue("Process was not found in database!", condition);

        assertEquals("Process was found but tasks were not inserted!", 5, process.getTasks().size());
    }

    @Test
    public void shouldGetAllProcesses() throws Exception {
        List<Process> processes = processService.getAll();
        assertEquals("Not all processes were found in database!", 7, processes.size());
    }

    @Test
    public void shouldGetAllProcessesInGivenRange() throws Exception {
        List<Process> processes = processService.getAll(1, 10);
        assertEquals("Not all processes were found in database!", 6, processes.size());
    }

    @Test
    public void shouldRemoveProcess() throws Exception {
        Process process = new Process();
        process.setTitle("To Remove");
        processService.save(process);
        Process foundProcess = processService.getById(8);
        assertEquals("Additional process was not inserted in database!", "To Remove", foundProcess.getTitle());

        processService.remove(foundProcess);
        exception.expect(DAOException.class);
        processService.getById(10);

        process = new Process();
        process.setTitle("To remove");
        processService.save(process);
        foundProcess = processService.getById(9);
        assertEquals("Additional process was not inserted in database!", "To remove", foundProcess.getTitle());

        processService.remove(9);
        exception.expect(DAOException.class);
        processService.getById(9);
    }

    @Test
    public void shouldFindById() throws DataException {
        Integer expected = 1;
        assertEquals(processNotFound, expected, processService.findById(1).getId());
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(processNotFound, 1, processService.findByTitle(firstProcess, true).size());
    }

    @Test
    public void shouldFindByMetadata() throws DataException {
        assertEquals(processNotFound, 4,
            processService.findByMetadata(Collections.singletonMap("TSL_ATS", "Proc")).size());
    }

    @Test
    public void shouldNotFindByMetadata() throws DataException {
        assertEquals("Process was found in index!", 0,
                processService.findByMetadata(Collections.singletonMap("TSL_ATS", "Nope")).size());
    }

    @Test
    public void shouldFindByMetadataContent() throws DataException {
        assertEquals(processNotFound, 1, processService.findByAnything("SecondMetaShort").size());
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
        assertFalse("nothing found", byAnything.isEmpty());
        assertEquals("wrong process found", processTitle, byAnything.get(0).getTitle());

        ServiceManager.getProcessService().remove(process.getId());
    }

    @Test
    public void shouldFindByProjectTitleWithWildcard() throws DataException {
        assertEquals(processNotFound, 6, processService.findByAnything("proj").size());
    }

    @Test
    public void shouldNotFindByAnything() throws DataException {
        assertEquals(processNotFound, 0, processService.findByAnything("Nope").size());
    }

    @Test
    public void shouldFindByMetadataGroupContent() throws DataException {
        processService.findByTitle("Second Process");
        assertEquals(processNotFound, 1, processService.findByAnything("August").size());
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
    public void shouldFindLinkableParentProcesses() throws DataException {
        assertEquals("Processes were not found in index!", 1,
            processService.findLinkableParentProcesses("HierarchyParent", 1, 1).size());
    }

    @Ignore("for second process is attached task which is processed by blocked user")
    @Test
    public void shouldGetBlockedUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        Process process = processService.getById(1);
        boolean condition = MetadataLock.getLockUser(process.getId()) == null;
        assertTrue("Process has blocked user but it shouldn't!", condition);

        process = processService.getById(2);
        condition = MetadataLock.getLockUser(process.getId()) == userService.getById(3);
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

    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessDTO process = processService.findById(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue("BatchId doesn't match to given plain text!", condition);
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

        double condition = processService.getProgressClosed(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 40, condition, 0);
    }

    @Test
    public void shouldGetProgressInProcessing() throws Exception {
        Process process = processService.getById(1);

        double condition = processService.getProgressInProcessing(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition, 0);
    }

    @Test
    public void testGetQueryForClosedProcesses() throws DataException, DAOException {
        ProcessService processService = ServiceManager.getProcessService();
        Process secondProcess = processService.getById(2);
        final String sortHelperStatusOld = secondProcess.getSortHelperStatus();
        secondProcess.setSortHelperStatus("100000000");
        processService.save(secondProcess);

        QueryBuilder querySortHelperStatusTrue = processService.getQueryForClosedProcesses();
        List<ProcessDTO> byQuery = processService.findByQuery(querySortHelperStatusTrue, true);

        Assert.assertEquals("Found the wrong amount of Processes", 1 ,byQuery.size());
        secondProcess.setSortHelperStatus(sortHelperStatusOld);
        processService.save(secondProcess);
    }

    @Test
    public void shouldGetProgressOpen() throws Exception {
        Process process = processService.getById(1);

        double condition = processService.getProgressOpen(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition, 0);
    }

    @Test
    public void shouldGetProgressLocked() throws Exception {
        Process process = processService.getById(1);

        double condition = processService.getProgressLocked(process.getTasks(), null);
        assertEquals("Progress doesn't match given plain text!", 20, condition, 0);
    }

    @Test
    public void shouldGetMetadataFilePath() throws Exception {
        Process process = processService.getById(2);
        URI directory = fileService.getMetadataFilePath(process);
        boolean condition = directory.getRawPath().contains("2/meta.xml");
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
    public void shouldReadMetadataFile() throws Exception {
        FileLoader.createMetadataFile();

        Process process = processService.getById(1);
        LegacyMetsModsDigitalDocumentHelper digitalDocument = processService.readMetadataFile(process)
                .getDigitalDocument();

        String processTitle = process.getTitle();
        String processTitleFromMetadata = digitalDocument.getLogicalDocStruct().getAllMetadata().get(0).getValue();
        assertEquals("It was not possible to read metadata file!", processTitle, processTitleFromMetadata);

        FileLoader.deleteMetadataFile();
    }

    @Test
    public void shouldReadMetadataAsTemplateFile() throws Exception {
        FileLoader.createMetadataTemplateFile();

        Process process = processService.getById(1);
        LegacyMetsModsDigitalDocumentHelper fileFormat = processService.readMetadataAsTemplateFile(process);
        assertNotNull("Read template file has incorrect file format!", fileFormat);
        int metadataSize = fileFormat.getDigitalDocument().getLogicalDocStruct().getAllMetadata().size();
        assertEquals("It was not possible to read metadata as template file!", 1, metadataSize);

        FileLoader.deleteMetadataTemplateFile();
    }

    @Ignore("PreferencesException: Can't obtain DigitalDocument! Maybe wrong preferences file? - METS node")
    @Test
    public void shouldWriteMetadataAsTemplateFile() throws Exception {
        Process process = processService.getById(1);
        LegacyPrefsHelper preferences = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        fileService.writeMetadataAsTemplateFile(
            new LegacyMetsModsDigitalDocumentHelper(preferences.getRuleset()), process);
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
    public void shouldGetDigitalDocument() throws Exception {
        FileLoader.createMetadataFile();

        LegacyMetsModsDigitalDocumentHelper actual = processService.getDigitalDocument(processService.getById(1));
        assertEquals("Metadata size in digital document is incorrect!", 1,
            actual.getLogicalDocStruct().getAllMetadata().size());

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
            assertTrue("Process should have child to keep and child to add as only children",
                Arrays.asList("HierarchChildToKeep", "HierarchChildToAdd").contains(child.getTitle()));
            assertEquals("Child should have parent as parent", process, child.getParent());
        }
        assertEquals("Process to remove should have no parent", null, processService.getById(6).getParent());
    }

    @Test
    public void testFindAllIDs() throws DataException {
        List<Integer> allIDs = ServiceManager.getProcessService().findAllIDs();
        Assert.assertEquals("Wrong amount of id's in index", 7, allIDs.size());
        Assert.assertTrue("id's contain wrong entries", allIDs.containsAll(Arrays.asList(5, 2, 6, 4, 1, 7, 3)));

        allIDs = ServiceManager.getProcessService().findAllIDs(0L, 5);
        Assert.assertEquals("Wrong amount of id's in index", 5, allIDs.size());
        Assert.assertTrue("id's contain wrong entries", allIDs.containsAll(Arrays.asList(5, 2, 4, 1, 6)));


        allIDs = ServiceManager.getProcessService().findAllIDs(5L, 10);
        Assert.assertEquals("Wrong amount of id's in index", 2, allIDs.size());
        Assert.assertTrue("id's contain wrong entries", allIDs.containsAll(Arrays.asList(7, 3)));

    }

    @Test
    public void testCountMetadata() throws DAOException, IOException {
        Process process = ServiceManager.getProcessService().getById(2);
        URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
        long logicalMetadata = MetsService.countLogicalMetadata(workpiece);
        Assert.assertEquals("Wrong amount of metadata found!", 4, logicalMetadata);
    }
}
