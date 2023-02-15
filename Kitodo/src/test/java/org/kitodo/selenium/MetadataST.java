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

package org.kitodo.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;

/**
 * Tests for functions in the metadata editor.
 */
public class MetadataST extends BaseTestSelenium {

    private static final String TEST_IMAGES_DIR = "images";
    private static final String TEST_MEDIA_REFERENCES_FILE = "testUpdatedMediaReferencesMeta.xml";
    private static final String TEST_METADATA_LOCK_FILE = "testMetadataLockMeta.xml";
    private static int mediaReferencesProcessId = -1;
    private static int metadataLockProcessId = -1;
    private static List<Integer> dummyProcessIds = new LinkedList<>();
    private static final String PARENT_PROCESS_TITLE = "Parent process";
    private static final String FIRST_CHILD_PROCESS_TITLE = "First child process";
    private static final String SECOND_CHILD_PROCESS_TITLE = "Second child process";
    private static final String DUMMY_PROCESS_TITLE = "Dummy process";
    private static final String TEST_PARENT_PROCESS_METADATA_FILE = "testParentProcessMeta.xml";
    private static final String FIRST_CHILD_ID = "FIRST_CHILD_ID";
    private static final String SECOND_CHILD_ID = "SECOND_CHILD_ID";
    private static final String META_XML = "/meta.xml";
    private static List<Integer> processHierarchyTestProcessIds = new LinkedList<>();
    private static int parentProcessId = -1;
    private static final int TEST_PROJECT_ID = 1;
    private static final int TEST_TEMPLATE_ID = 1;

    private static void prepareMediaReferenceProcesses() throws DAOException, DataException, IOException {
        insertTestProcessForMediaReferencesTest();
        copyTestFilesForMediaReferences();
    }

    private static void prepareMetadataLockProcess() throws DAOException, DataException, IOException {
        insertTestProcessForMetadataLockTest();
        copyTestMetadataFile(metadataLockProcessId, TEST_METADATA_LOCK_FILE);
    }

    /**
     * Prepare tests by inserting dummy processes into database and index for sub-folders of test metadata resources.
     * @throws DAOException when saving of dummy or test processes fails.
     * @throws DataException when retrieving test project for test processes fails.
     * @throws IOException when copying test metadata or image files fails.
     */
    @BeforeClass
    public static void prepare() throws DAOException, DataException, IOException {
        MockDatabase.insertFoldersForSecondProject();
        insertDummyProcesses();
        prepareMetadataLockProcess();
        insertDummyProcesses();
        prepareMediaReferenceProcesses();
    }

    /**
     * Tests whether structure tree is hidden when user lacks permission to see a process structure in metadata editor.
     * @throws Exception when page navigation fails
     */
    @Test
    public void hideStructureDataTest() throws Exception {
        login("verylast");
        Pages.getProcessesPage().goTo().editMetadata(MockDatabase.METADATA_LOCK_TEST_PROCESS_TITLE);
        Assert.assertFalse(Pages.getMetadataEditorPage().isStructureTreeFormVisible());
        Pages.getMetadataEditorPage().save();
    }

    /**
     * Tests if process metadata lock is being removed when the user leaves the metadata editor
     * without clicking the close button.
     */
    @Test
    public void removeMetadataLockTest() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("verylast");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        // Open process in metadata editor by default user to set metadata lock for this process and user
        login("kowal");
        Pages.getProcessesPage().goTo().editMetadata(MockDatabase.METADATA_LOCK_TEST_PROCESS_TITLE);
        Pages.getMetadataEditorPage().save();
        // Leave metadata editor without explicitly clicking the 'close' button
        Pages.getMetadataEditorPage().clickPortalLogo();
        // Try to open metadata editor with separate user to check whether metadata lock is still in place
        Pages.getTopNavigation().logout();
        login("verylast");
        Pages.getProcessesPage().goTo().editMetadata(MockDatabase.METADATA_LOCK_TEST_PROCESS_TITLE);
        Assert.assertTrue("Unable to open metadata editor that was not closed by 'close' button",
                Browser.getCurrentUrl().contains("metadataEditor.jsf"));
    }

    /**
     * Verifies that linked child processes can be reordered via drag'n'drop in the metadata editor.
     * @throws Exception if processes cannot be saved or loaded
     */
    @Test
    public void changeProcessLinkOrderTest() throws Exception {
        processHierarchyTestProcessIds = linkProcesses();
        copyTestParentProcessMetadataFile();
        updateChildProcessIdsInParentProcessMetadataFile();
        User metadataUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        Pages.getProcessesPage().goTo().editThirdProcessMetadata();
        Assert.assertTrue("Wrong initial order of linked child processes",
                Pages.getMetadataEditorPage().getNameOfFirstLinkedChildProcess().endsWith(FIRST_CHILD_PROCESS_TITLE));
        Assert.assertTrue("Wrong initial order of linked child processes",
                Pages.getMetadataEditorPage().getNameOfSecondLinkedChildProcess().endsWith(SECOND_CHILD_PROCESS_TITLE));
        Pages.getMetadataEditorPage().changeOrderOfLinkedChildProcesses();
        Pages.getMetadataEditorPage().saveAndExit();
        Pages.getProcessesPage().goTo().editThirdProcessMetadata();
        Assert.assertTrue("Wrong resulting order of linked child processes",
                Pages.getMetadataEditorPage().getNameOfFirstLinkedChildProcess().endsWith(SECOND_CHILD_PROCESS_TITLE));
        Assert.assertTrue("Wrong resulting order of linked child processes",
                Pages.getMetadataEditorPage().getNameOfSecondLinkedChildProcess().endsWith(FIRST_CHILD_PROCESS_TITLE));
        Pages.getMetadataEditorPage().closeEditor();
        cleanup();
    }

    /**
     * Tests total number of scans.
     */
    @Test
    public void totalNumberOfScansTest() throws Exception {
        login("kowal");
        Pages.getProcessesPage().goTo().editMetadata();
        assertEquals("Total number of scans is not correct", "(Anzahl von Scans: 1)",
                Pages.getMetadataEditorPage().getNumberOfScans());
        Pages.getMetadataEditorPage().closeEditor();
    }

    /**
     * Verifies that turning the "pagination panel switch" on in the user settings
     * results in pagination panel being displayed by default in the metadata editor.
     */
    @Test
    public void showPaginationByDefaultTest() throws Exception {
        login("kowal");
        Pages.getProcessesPage().goTo().editMetadata();
        assertFalse(Pages.getMetadataEditorPage().isPaginationPanelVisible());
        Pages.getMetadataEditorPage().closeEditor();
        Pages.getUserEditPage().setPaginationToShowByDefault();
        Pages.getProcessesPage().goTo().editMetadata();
        assertTrue(Pages.getMetadataEditorPage().isPaginationPanelVisible());
        Pages.getMetadataEditorPage().closeEditor();
    }

    /**
     * Verifies that changing process images on file system triggers information/warning dialog.
     * @throws Exception when page navigation fails
     */
    @Test
    public void updateMediaReferencesTest() throws Exception {
        login("kowal");
        Pages.getProcessesPage().goTo().editMetadata(MockDatabase.MEDIA_REFERENCES_TEST_PROCESS_TITLE);
        assertTrue("Media references updated dialog not visible", Pages.getMetadataEditorPage()
                .isFileReferencesUpdatedDialogVisible());
        Pages.getMetadataEditorPage().acknowledgeFileReferenceChanges();
    }

    /**
     * Close metadata editor and logout after every test.
     * @throws Exception when page navigation fails
     */
    @After
    public void closeEditorAndLogout() throws Exception {
        Pages.getMetadataEditorPage().closeEditor();
        Pages.getTopNavigation().logout();
    }

    /**
     * Cleanup test environment by removing temporal dummy processes from database and index.
     * @throws DAOException when dummy process cannot be removed from database
     * @throws CustomResponseException when dummy process cannot be removed from index
     * @throws DataException when dummy process cannot be removed from index
     * @throws IOException when deleting test files fails.
     */
    @AfterClass
    public static void cleanup() throws DAOException, CustomResponseException, DataException, IOException {
        for (int processId : processHierarchyTestProcessIds) {
            ProcessService.deleteProcess(processId);
        }
        for (int dummyProcessId : dummyProcessIds) {
            ServiceManager.getProcessService().removeFromDatabase(dummyProcessId);
            ServiceManager.getProcessService().removeFromIndex(dummyProcessId, false);
        }
        ProcessService.deleteProcess(mediaReferencesProcessId);
        ProcessService.deleteProcess(metadataLockProcessId);
    }

    private void login(String username) throws InstantiationException, IllegalAccessException, InterruptedException {
        User metadataUser = ServiceManager.getUserService().getByLogin(username);
        Pages.getLoginPage().goTo().performLogin(metadataUser);
    }

    private static void insertTestProcessForMediaReferencesTest() throws DAOException, DataException {
        mediaReferencesProcessId = MockDatabase.insertTestProcessForMediaReferencesTestIntoSecondProject();
    }

    private static void insertTestProcessForMetadataLockTest() throws DAOException, DataException {
        metadataLockProcessId = MockDatabase.insertTestProcessForMetadataLockTestIntoSecondProject();
    }

    /**
     * Creates dummy parent process and links first two test processes as child processes to parent process.
     * @throws DAOException if loading of processes fails
     * @throws DataException if saving of processes fails
     */
    private static List<Integer> linkProcesses() throws DAOException, DataException {
        List<Integer> processIds = new LinkedList<>();
        List<Process> childProcesses = new LinkedList<>();
        childProcesses.add(addProcess(FIRST_CHILD_PROCESS_TITLE));
        childProcesses.add(addProcess(SECOND_CHILD_PROCESS_TITLE));
        Process parentProcess = addProcess(PARENT_PROCESS_TITLE);
        parentProcess.getChildren().addAll(childProcesses);
        ServiceManager.getProcessService().save(parentProcess);
        parentProcessId = parentProcess.getId();
        for (Process childProcess : childProcesses) {
            childProcess.setParent(parentProcess);
            ServiceManager.getProcessService().save(childProcess);
            processIds.add(childProcess.getId());
        }
        processIds.add(parentProcess.getId());
        return processIds;
    }

    private static Process addProcess(String processTitle) throws DAOException, DataException {
        insertDummyProcesses();
        return MockDatabase.addProcess(processTitle, TEST_PROJECT_ID, TEST_TEMPLATE_ID);
    }

    private static void insertDummyProcesses() throws DAOException, DataException {
        dummyProcessIds = new LinkedList<>();
        List<Integer> processIds = ServiceManager.getProcessService().getAll().stream().map(Process::getId)
                .collect(Collectors.toList());
        int id = Collections.max(processIds) + 1;
        while (processDirExists(id)) {
            dummyProcessIds.add(MockDatabase.insertDummyProcess(id));
            id++;
        }
    }

    private static boolean processDirExists(int processId) {
        URI uri = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId)).toUri();
        return ServiceManager.getFileService().isDirectory(uri);
    }

    private static void copyTestFilesForMediaReferences() throws IOException {
        // copy test meta xml
        copyTestMetadataFile(mediaReferencesProcessId, TEST_MEDIA_REFERENCES_FILE);
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(mediaReferencesProcessId))
                .toUri();
        // copy test images
        URI testImagesUri = Paths.get(ConfigCore.getKitodoDataDirectory(), TEST_IMAGES_DIR).toUri();
        URI targetImages = Paths.get(ConfigCore.getKitodoDataDirectory(), mediaReferencesProcessId
                + "/images/").toUri();
        try {
            if (!ServiceManager.getFileService().isDirectory(targetImages)) {
                ServiceManager.getFileService().createDirectory(processDir, "images");
            }
            ServiceManager.getFileService().copyDirectory(testImagesUri, targetImages);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void copyTestMetadataFile(int processId, String filename) throws IOException {
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId))
                .toUri();
        URI processDirTargetFile = Paths.get(ConfigCore.getKitodoDataDirectory(), processId
                + "/meta.xml").toUri();
        URI metaFileUri = Paths.get(ConfigCore.getKitodoDataDirectory(), filename).toUri();
        if (!ServiceManager.getFileService().isDirectory(processDir)) {
            ServiceManager.getFileService().createDirectory(Paths.get(ConfigCore.getKitodoDataDirectory()).toUri(),
                    String.valueOf(processId));
        }
        ServiceManager.getFileService().copyFile(metaFileUri, processDirTargetFile);
    }

    private static void copyTestParentProcessMetadataFile() throws IOException {
        copyTestMetadataFile(parentProcessId, TEST_PARENT_PROCESS_METADATA_FILE);
    }

    private static void updateChildProcessIdsInParentProcessMetadataFile() throws IOException, DAOException {
        Process parentProcess = ServiceManager.getProcessService().getById(parentProcessId);
        Path metaXml = Paths.get(ConfigCore.getKitodoDataDirectory(), parentProcessId + META_XML);
        String xmlContent = Files.readString(metaXml);
        String firstChildId = String.valueOf(parentProcess.getChildren().get(0).getId());
        String secondChildId = String.valueOf(parentProcess.getChildren().get(1).getId());
        xmlContent = xmlContent.replaceAll(FIRST_CHILD_ID, String.valueOf(firstChildId));
        xmlContent = xmlContent.replaceAll(SECOND_CHILD_ID, String.valueOf(secondChildId));
        Files.write(metaXml, xmlContent.getBytes());
    }
}
