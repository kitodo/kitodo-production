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

package org.kitodo.production.services.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;

public class KitodoScriptServiceIT {
    // private static final Logger logger =
    // LogManager.getLogger(KitodoScriptServiceIT.class);
    private static final String metadataWithDuplicatesTestFile = "testMetaWithDuplicateMetadata.xml";
    private static final String directoryForDerivateGeneration = "testFilesForDerivativeGeneration";
    private static int kitodoScriptTestProcessId = -1;
    private static final String testProcessTitle = "Second process";
    private static final int projectId = 1;
    private static final int templateId = 1;
    private static final int rulesetId = 1;
    private static final int userId = 1;
    private static final int clientId = 1;

    private static final File scriptCreateDirMeta = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        User userOne = ServiceManager.getUserService().getById(userId);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, clientId);
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Add metadata test process and metadata file for KitodoScriptService tests.
     */
    @BeforeEach
    public void prepareFileCopy() throws IOException, DAOException {
        kitodoScriptTestProcessId = MockDatabase.insertTestProcess(testProcessTitle, projectId, templateId, rulesetId);
        ProcessTestUtils.copyTestResources(kitodoScriptTestProcessId, directoryForDerivateGeneration);
        ProcessTestUtils.copyTestMetadataFile(kitodoScriptTestProcessId, metadataWithDuplicatesTestFile);
    }

    /**
     * Remove test process and metadata file for KitodoScriptService tests.
     */
    @AfterEach
    public void removeKitodoScriptServiceTestFile() throws IOException, DAOException {
        ProcessTestUtils.removeTestProcess(kitodoScriptTestProcessId);
        kitodoScriptTestProcessId = -1;
    }

    @Test
    public void shouldCreateProcessFolders() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
        }

        Process process = ServiceManager.getProcessService().getById(1);
        process.setTitle("FirstProcess");
        ServiceManager.getFileService().createProcessLocation(process);

        File processHome = new File(ConfigCore.getKitodoDataDirectory(), "1");
        File max = new File(processHome, "jpgs/max");
        max.delete();

        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:createFolders";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        kitodoScript.execute(processes, script);

        assertTrue(max.isDirectory(), max + ": There is no such directory!");

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
        }

        TreeDeleter.deltree(processHome);
    }

    @Test
    public void shouldExecuteAddRoleScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        Task task = ServiceManager.getTaskService().getById(8);
        int amountOfRoles = task.getRoles().size();

        String script = "action:addRole \"tasktitle:Progress\" role:General";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        task = ServiceManager.getTaskService().getById(8);
        assertEquals(amountOfRoles + 1, task.getRoles().size(), "Role was not correctly added to task!");
    }

    @Test
    public void shouldExecuteSetTaskStatusScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setStepStatus \"tasktitle:Progress\" status:3";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Processing status was not correctly changed!");
    }

    @Test
    public void shouldExecuteAddShellScriptToTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:addShellScriptToStep \"tasktitle:Progress\" \"label:script\" \"script:/some/new/path\"";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("script", task.getScriptName(), "Script was not added to task - incorrect name!");
        assertEquals("/some/new/path", task.getScriptPath(), "Script was not added to task - incorrect path!");
    }

    @Test
    public void shouldExecuteSetPropertyTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setTaskProperty \"tasktitle:Closed\" property:validate value:true";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertTrue(task.isTypeCloseVerify(), "Task property was not set!");
    }

    @Test
    public void shouldNotExecuteSetPropertyTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setTaskProperty \"tasktitle:Closed\" property:validate value:invalid";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertFalse(task.isTypeCloseVerify(), "Task property was set - default value is false!");
    }

    @Test
    public void shouldGenerateDerivativeImages() throws Exception {

        // Delete created and still running taskmanager tasks from other test suites because
        // this test is assuming that there are no other taskmanager tasks running!
        TaskManager.stopAndDeleteAllTasks();

        Folder generatorSource = new Folder();
        generatorSource.setMimeType("image/tiff");
        generatorSource.setPath("images/(processtitle)_media");
        Process processTwo = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        Project project = processTwo.getProject();
        generatorSource.setProject(project);
        ServiceManager.getFolderService().save(generatorSource);
        project.setGeneratorSource(generatorSource);
        ServiceManager.getProjectService().save(project);
        List<Process> processes = new ArrayList<>();
        processTwo.setTitle("SecondProcess");
        processes.add(processTwo);

        ServiceManager.getKitodoScriptService().execute(processes,
            "action:generateImages \"folders:jpgs/max,jpgs/thumbs\" images:all");
        EmptyTask taskImageGeneratorThread = TaskManager.getTaskList().getFirst();
        while (taskImageGeneratorThread.isStartable() || taskImageGeneratorThread.isStoppable()) {
            Thread.sleep(400);
        }
        TaskManager.stopAndDeleteAllTasks();
    }

    @Test
    public void shouldAddDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "PDM1.0");

        final List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " value:PDM1.0";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<Process> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadataAfter.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(7, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldAddDataWithType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "should not contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " value:PDM1.0" + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(2, metadataOfChapter.size(), "metadata should have been added");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(7, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldDeleteDataWithType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMain";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "should contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(0, metadataOfChapter.size(), "metadata should have been deleted");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(5, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldNotDeleteDataWithTypeAndWrongValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMain";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "should contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey + " value:test" + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "metadata should not have been deleted");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldNotDeleteDataWithWrongType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "should contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().getFirst().getMetadata();
        assertEquals(1, metadataOfChapter.size(), "metadata should not have been deleted");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldCopyMultipleDataToChildren() throws Exception {
        Map<String, Integer> testProcessIds = MockDatabase.insertProcessesForHierarchyTests();
        ProcessTestUtils.copyHierarchyTestFiles(testProcessIds);
        Thread.sleep(2000);
        String metadataKey = "DigitalCollection";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Kollektion2");

        final List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should not contain metadata beforehand");

        String script = "action:copyDataToChildren " + "key:" + metadataKey + " source:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(testProcessIds.get(MockDatabase.HIERARCHY_PARENT)));
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<Process> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap);
        assertEquals(3, processByMetadataAfter.size(), "does not contain metadata");
        ProcessTestUtils.removeTestProcess(testProcessIds.get(MockDatabase.HIERARCHY_PARENT));
    }

    @Test
    public void shouldAddDataWithWhitespace() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "legal note");

        final List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap, true);
        assertEquals(0, processByMetadata.size(), "should not contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " \"value:legal note\"";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<Process> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap, true);
        assertEquals(1, processByMetadataAfter.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(7, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldAddDataWithMultipleScripts() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "legal note");

        HashMap<String, String> secondMetadataSearchMap = new HashMap<>();
        secondMetadataSearchMap.put(metadataKey, "secondNote");

        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata beforehand");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(secondMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata beforehand");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " value:legal note;" + "key:" + metadataKey + " value:secondNote";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        List<Process> processByMetadataAfter = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadataAfter.size(), "does not contain metadata");
        processByMetadataAfter = ServiceManager.getProcessService().findByMetadata(secondMetadataSearchMap);
        assertEquals(1, processByMetadataAfter.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(8, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldCopyDataWithSource() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Proc");

        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " source:TSL_ATS";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "does not contain metadata");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(8, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldAddDataWithVariable() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, String.valueOf(kitodoScriptTestProcessId));

        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:addData " + "key:" + metadataKey + " variable:(processid)";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "does not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(7, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldDeleteData() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(5, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldDeleteDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey + " value:SecondMetaShort";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(5, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldDeleteAllDataWithSameKey() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TSL_ATS";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Proc");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(4, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldDeleteDataWithSource() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData " + "key:" + metadataKey + " source:TitleDocMainShort";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(5, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldNotDeleteDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:deleteData" + " key:" + metadataKey + " value:SecondMetaLong";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should still contain metadata");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldOverwriteDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, "Overwritten");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:overwriteData " + "key:" + metadataKey + " value:Overwritten";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata anymore");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldOverwriteDataWithSource() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, "Second process");

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:overwriteData " + "key:" + metadataKey + " source:TitleDocMain";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata anymore");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }

    @Test
    public void shouldOverwriteDataWithVariable() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        File processDir = new File(ConfigCore.getKitodoDataDirectory(), process.getId().toString());
        File mainMetsFile = new File(processDir, "meta.xml");
        File backupMetsFile = new File(processDir, "meta.xml.1");

        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, String.valueOf(kitodoScriptTestProcessId));

        Thread.sleep(2000);
        List<Process> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain metadata");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertFalse(backupMetsFile.exists(), "Backup file meta.xml.1 should not exist");

        // update database entries with init values
        ServiceManager.getProcessService().updateAmountOfInternalMetaInformation(process, true);
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());

        String script = "action:overwriteData " + "key:" + metadataKey + " variable:(processid)";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        assertEquals(0, processByMetadata.size(), "should not contain metadata anymore");

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        assertEquals(1, processByMetadata.size(), "should contain new metadata value");

        assertTrue(mainMetsFile.exists(), "File meta.xml should exist");
        assertTrue(backupMetsFile.exists(), "Backup file meta.xml.1 should exist");
        assertNotEquals(mainMetsFile.length(), backupMetsFile.length(), "meta.xml and meta.xml.1 should not have same size");

        // database based check
        assertEquals(0, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(2, process.getSortHelperDocstructs());
    }
}
