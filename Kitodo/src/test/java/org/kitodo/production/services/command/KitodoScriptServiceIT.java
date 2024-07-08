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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
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
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;

public class KitodoScriptServiceIT {

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

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        User userOne = ServiceManager.getUserService().getById(userId);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, clientId);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Add metadata test process and metadata file for KitodoScriptService tests.
     */
    @Before
    public void prepareFileCopy() throws IOException, DAOException, DataException {
        kitodoScriptTestProcessId = MockDatabase.insertTestProcess(testProcessTitle, projectId, templateId, rulesetId);
        ProcessTestUtils.copyTestResources(kitodoScriptTestProcessId, directoryForDerivateGeneration);
        ProcessTestUtils.copyTestMetadataFile(kitodoScriptTestProcessId, metadataWithDuplicatesTestFile);
    }

    /**
     * Remove test process and metadata file for KitodoScriptService tests.
     */
    @After
    public void removeKitodoScriptServiceTestFile() throws IOException, DataException, DAOException {
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

        assertTrue(max + ": There is no such directory!", max.isDirectory());

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
        assertEquals("Role was not correctly added to task!", amountOfRoles + 1, task.getRoles().size());
    }

    @Test
    public void shouldExecuteSetTaskStatusScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setStepStatus \"tasktitle:Progress\" status:3";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("Processing status was not correctly changed!", TaskStatus.DONE, task.getProcessingStatus());
    }

    @Test
    public void shouldExecuteAddShellScriptToTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:addShellScriptToStep \"tasktitle:Progress\" \"label:script\" \"script:/some/new/path\"";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(8);
        assertEquals("Script was not added to task - incorrect name!", "script", task.getScriptName());
        assertEquals("Script was not added to task - incorrect path!", "/some/new/path", task.getScriptPath());
    }

    @Test
    public void shouldExecuteSetPropertyTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setTaskProperty \"tasktitle:Closed\" property:validate value:true";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertTrue("Task property was not set!", task.isTypeCloseVerify());
    }

    @Test
    public void shouldNotExecuteSetPropertyTaskScript() throws Exception {
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();

        String script = "action:setTaskProperty \"tasktitle:Closed\" property:validate value:invalid";
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(1));
        kitodoScript.execute(processes, script);

        Task task = ServiceManager.getTaskService().getById(7);
        assertFalse("Task property was set - default value is false!", task.isTypeCloseVerify());
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
        ServiceManager.getFolderService().saveToDatabase(generatorSource);
        project.setGeneratorSource(generatorSource);
        ServiceManager.getProjectService().save(project);
        List<Process> processes = new ArrayList<>();
        processTwo.setTitle("SecondProcess");
        processes.add(processTwo);

        ServiceManager.getKitodoScriptService().execute(processes,
            "action:generateImages \"folders:jpgs/max,jpgs/thumbs\" images:all");
        EmptyTask taskImageGeneratorThread = TaskManager.getTaskList().get(0);
        while (taskImageGeneratorThread.isStartable() || taskImageGeneratorThread.isStoppable()) {
            Thread.sleep(400);
        }
        TaskManager.stopAndDeleteAllTasks();
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldAddDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "PDM1.0");

        final List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata beforehand", 0, processByMetadata.size() );

        String script = "action:addData " + "key:" + metadataKey + " value:PDM1.0";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<ProcessInterface> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 1, processByMetadataAfter.size() );
    }

    @Test
    public void shouldAddDataWithType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("should not contain metadata beforehand", 1, metadataOfChapter.size() );

        String script = "action:addData " + "key:" + metadataKey + " value:PDM1.0" + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("metadata should have been added", 2, metadataOfChapter.size() );
    }

    @Test
    public void shouldDeleteDataWithType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMain";

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("should contain metadata beforehand", 1, metadataOfChapter.size() );

        String script = "action:deleteData " + "key:" + metadataKey + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("metadata should have been deleted", 0, metadataOfChapter.size() );
    }

    @Test
    public void shouldNotDeleteDataWithTypeAndWrongValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMain";

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("should contain metadata beforehand", 1, metadataOfChapter.size() );

        String script = "action:deleteData " + "key:" + metadataKey + " value:test" + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("metadata should not have been deleted", 1, metadataOfChapter.size() );
    }

    @Test
    public void shouldNotDeleteDataWithWrongType() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";

        LegacyMetsModsDigitalDocumentHelper metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        Workpiece workpiece = metadataFile.getWorkpiece();
        Collection<Metadata> metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("should contain metadata beforehand", 1, metadataOfChapter.size() );

        String script = "action:deleteData " + "key:" + metadataKey + " type:Chapter";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        metadataFile = ServiceManager.getProcessService()
                .readMetadataFile(process);
        workpiece = metadataFile.getWorkpiece();
        metadataOfChapter = workpiece.getLogicalStructure().getChildren().get(0).getMetadata();
        Assert.assertEquals("metadata should not have been deleted", 1, metadataOfChapter.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldCopyMultipleDataToChildren() throws Exception {
        Map<String, Integer> testProcessIds = MockDatabase.insertProcessesForHierarchyTests();
        ProcessTestUtils.copyHierarchyTestFiles(testProcessIds);
        String metadataKey = "DigitalCollection";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Kollektion2");

        final List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata beforehand", 1, processByMetadata.size() );

        String script = "action:copyDataToChildren " + "key:" + metadataKey + " source:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(ServiceManager.getProcessService().getById(testProcessIds.get(MockDatabase.HIERARCHY_PARENT)));
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<ProcessInterface> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 3, processByMetadataAfter.size() );
        ProcessTestUtils.removeTestProcess(testProcessIds.get(MockDatabase.HIERARCHY_PARENT));
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldAddDataWithWhitespace() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "legal note");

        final List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap, true);
        Assert.assertEquals("should not contain metadata beforehand", 0, processByMetadata.size() );

        String script = "action:addData " + "key:" + metadataKey + " \"value:legal note\"";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        final List<ProcessInterface> processByMetadataAfter = ServiceManager.getProcessService()
                .findByMetadata(metadataSearchMap, true);
        Assert.assertEquals("does not contain metadata", 1, processByMetadataAfter.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldAddDataWithMultipleScripts() throws Exception {
        String metadataKey = "LegalNoteAndTermsOfUse";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "legal note");

        HashMap<String, String> secondMetadataSearchMap = new HashMap<>();
        secondMetadataSearchMap.put(metadataKey, "secondNote");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata beforehand", 0, processByMetadata.size() );

        processByMetadata = ServiceManager.getProcessService().findByMetadata(secondMetadataSearchMap);
        Assert.assertEquals("should not contain metadata beforehand", 0, processByMetadata.size() );

        String script = "action:addData " + "key:" + metadataKey + " value:legal note;" + "key:" + metadataKey + " value:secondNote";
        List<Process> processes = new ArrayList<>();
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);
        Thread.sleep(2000);
        List<ProcessInterface> processByMetadataAfter = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 1, processByMetadataAfter.size());
        processByMetadataAfter = ServiceManager.getProcessService().findByMetadata(secondMetadataSearchMap);
        Assert.assertEquals("does not contain metadata", 1, processByMetadataAfter.size());
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldCopyDataWithSource() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Proc");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 0, processByMetadata.size() );

        String script = "action:addData " + "key:" + metadataKey + " source:TSL_ATS";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 1, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldAddDataWithVariable() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "LegalNoteAndTermsOfUse";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, String.valueOf(kitodoScriptTestProcessId));

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 0, processByMetadata.size() );

        String script = "action:addData " + "key:" + metadataKey + " variable:(processid)";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("does not contain metadata", 1, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldDeleteData() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        String script = "action:deleteData " + "key:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata", 0, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldDeleteDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        String script = "action:deleteData " + "key:" + metadataKey + " value:SecondMetaShort";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata", 0, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldDeleteAllDataWithSameKey() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TSL_ATS";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "Proc");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        String script = "action:deleteData " + "key:" + metadataKey;
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata", 0, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldDeleteDataWithSource() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        String script = "action:deleteData " + "key:" + metadataKey + " source:TitleDocMainShort";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should not contain metadata", 0, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldNotDeleteDataWithValue() throws Exception {
        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> metadataSearchMap = new HashMap<>();
        metadataSearchMap.put(metadataKey, "SecondMetaShort");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        String script = "action:deleteData" + " key:" + metadataKey + " value:SecondMetaLong";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(metadataSearchMap);
        Assert.assertEquals("should still contain metadata", 1, processByMetadata.size() );
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldOverwriteDataWithValue() throws Exception {
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, "Overwritten");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 0, processByMetadata.size());

        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String script = "action:overwriteData " + "key:" + metadataKey + " value:Overwritten";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should not contain metadata anymore", 0, processByMetadata.size());

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 1, processByMetadata.size());
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldOverwriteDataWithSource() throws Exception {
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, "Second process");

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 0, processByMetadata.size());

        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String script = "action:overwriteData " + "key:" + metadataKey + " source:TitleDocMain";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should not contain metadata anymore", 0, processByMetadata.size());

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 1, processByMetadata.size());
    }

    @Test
    @Disabled("Data index currently not available")
    public void shouldOverwriteDataWithVariable() throws Exception {
        String metadataKey = "TitleDocMainShort";
        HashMap<String, String> oldMetadataSearchMap = new HashMap<>();
        oldMetadataSearchMap.put(metadataKey, "SecondMetaShort");

        HashMap<String, String> newMetadataSearchMap = new HashMap<>();
        newMetadataSearchMap.put(metadataKey, String.valueOf(kitodoScriptTestProcessId));

        List<ProcessInterface> processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should contain metadata", 1, processByMetadata.size() );

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 0, processByMetadata.size());

        Process process = ServiceManager.getProcessService().getById(kitodoScriptTestProcessId);
        String script = "action:overwriteData " + "key:" + metadataKey + " variable:(processid)";
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        KitodoScriptService kitodoScript = ServiceManager.getKitodoScriptService();
        kitodoScript.execute(processes, script);

        Thread.sleep(2000);
        processByMetadata = ServiceManager.getProcessService().findByMetadata(oldMetadataSearchMap);
        Assert.assertEquals("should not contain metadata anymore", 0, processByMetadata.size());

        processByMetadata = ServiceManager.getProcessService().findByMetadata(newMetadataSearchMap);
        Assert.assertEquals("should contain new metadata value", 1, processByMetadata.size());
    }
}
