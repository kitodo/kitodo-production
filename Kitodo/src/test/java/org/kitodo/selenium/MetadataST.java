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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.AfterClass;
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

public class MetadataST extends BaseTestSelenium {

    private static final String TEST_IMAGES_DIR = "images";
    private static final String TEST_METADATA_FILE = "testUpdatedMediaReferencesMeta.xml";
    private static int mediaReferencesProcessId = -1;
    private static List<Integer> dummyProcessIds = new LinkedList<>();

    /**
     * Prepare file system, database and search index for tests.
     * @throws DAOException when inserting processes into database fails
     * @throws DataException when inserting processes into index fails
     * @throws IOException when copying test files fails
     */
    @BeforeClass
    public static void prepare() throws DAOException, DataException, IOException {
        MockDatabase.insertFoldersForSecondProject();
        insertTestProcessForUpdatedMediaReferences();
        copyTestFiles();
    }

    /**
     * Tests whether structure tree is hidden when user lacks permission to see a process structure in metadata editor.
     * @throws Exception when page navigation fails
     */
    @Test
    public void hideStructureDataTest() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("verylast");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        Pages.getProcessesPage().goTo().editMetadata();
        Assert.assertFalse(Pages.getMetadataEditorPage().isStructureTreeFormVisible());
    }

    /**
     * Tests if process metadata lock is being removed when the user leaves the metadata editor
     * without clicking the close button.
     */
    @Test
    public void removeMetadataLockTest() throws Exception {
        // Open process in metadata editor by default user to set metadata lock for this process and user
        Pages.getProcessesPage().goTo().editMetadata();
        // Leave metadata editor without explicitly clicking the 'close' button
        Pages.getMetadataEditorPage().clickPortalLogo();
        // Try to open metadata editor with separate user to check whether metadata lock is still in place
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getByLogin("kowal"));
        Pages.getProcessesPage().goTo().editMetadata();
        Assert.assertEquals("Unable to open metadata editor that was not closed by 'close' button",
                "http://localhost:8080/kitodo/pages/metadataEditor.jsf?referer=processes&id=2",
                Browser.getCurrentUrl());
    }

    /**
     * Tests total number of scans.
     */
    @Test
    public void totalNumberOfScansTest() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        Pages.getProcessesPage().goTo().editMetadata();
        assertEquals("Total number of scans is not correct", "(Anzahl von Scans: 1)",
                Pages.getMetadataEditorPage().getNumberOfScans());
    }

    /**
     * Verifies that turning the "pagination panel switch" on in the user settings
     * results in pagination panel being displayed by default in the metadata editor.
     */
    @Test
    public void showPaginationByDefaultTest() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        Pages.getProcessesPage().goTo().editMetadata();
        assertFalse(Pages.getMetadataEditorPage().isPaginationPanelVisible());
        Pages.getUserEditPage().setPaginationToShowByDefault();
        Pages.getProcessesPage().goTo().editMetadata();
        assertTrue(Pages.getMetadataEditorPage().isPaginationPanelVisible());
    }

    /**
     * Verifies that changing process images on file system triggers information/warning dialog.
     * @throws Exception when page navigation fails
     */
    @Test
    public void updateMediaReferencesTest() throws Exception {
        User metadataUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(metadataUser);
        Pages.getProcessesPage().goTo().editMetadata(MockDatabase.MEDIA_REFERENCES_TEST_PROCESS_TITLE);
        assertTrue("Media references updated dialog not visible", Pages.getMetadataEditorPage()
                .isFileReferencesUpdatedDialogVisible());
        Pages.getMetadataEditorPage().acknowledgeFileReferenceChanges();
    }

    @After
    public void closeEditorAndLogout() throws Exception {
        Pages.getMetadataEditorPage().closeEditor();
        Pages.getTopNavigation().logout();
    }

    /**
     * Cleanup file system, database and index after tests.
     * @throws DAOException when dummy processes cannot be removed from database
     * @throws DataException when dummy processes cannot be removed from index
     * @throws IOException when media references test process cannot be deleted from file system
     * @throws CustomResponseException when dummy processes cannot be removed from index
     */
    @AfterClass
    public static void cleanup() throws DAOException, DataException, IOException, CustomResponseException {
        ProcessService.deleteProcess(mediaReferencesProcessId);
        for (int dummyProcessId : dummyProcessIds) {
            ServiceManager.getProcessService().removeFromDatabase(dummyProcessId);
            ServiceManager.getProcessService().removeFromIndex(dummyProcessId, false);
        }
    }

    private static void insertTestProcessForUpdatedMediaReferences() throws DAOException, DataException {
        dummyProcessIds = new LinkedList<>();
        List<Integer> processIds = ServiceManager.getProcessService().getAll().stream().map(Process::getId)
                .collect(Collectors.toList());
        int id = Collections.max(processIds) + 1;
        while (processDirExists(id)) {
            dummyProcessIds.add(MockDatabase.insertDummyProcess(id));
            id++;
        }
        mediaReferencesProcessId = MockDatabase.insertTestProcessIntoSecondProject();
    }

    private static boolean processDirExists(int processId) {
        URI uri = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(processId)).toUri();
        return ServiceManager.getFileService().isDirectory(uri);
    }

    private static void copyTestFiles() throws IOException {
        // copy test meta xml
        URI processDir = Paths.get(ConfigCore.getKitodoDataDirectory(), String.valueOf(mediaReferencesProcessId))
                .toUri();
        URI processDirTargetFile = Paths.get(ConfigCore.getKitodoDataDirectory(), mediaReferencesProcessId
                + "/meta.xml").toUri();
        URI metaFileUri = Paths.get(ConfigCore.getKitodoDataDirectory(), TEST_METADATA_FILE).toUri();
        if (!ServiceManager.getFileService().isDirectory(processDir)) {
            ServiceManager.getFileService().createDirectory(Paths.get(ConfigCore.getKitodoDataDirectory()).toUri(),
                    String.valueOf(mediaReferencesProcessId));
        }
        ServiceManager.getFileService().copyFile(metaFileUri, processDirTargetFile);

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
}
