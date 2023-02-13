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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for TaskService class.
 */
public class FolderServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindFolder() throws Exception {
        FolderService folderService = new FolderService();

        Folder folder = folderService.getById(1);
        boolean condition = folder.getFileGroup().equals("MAX") && folder.getMimeType().equals("image/jpeg");
        assertTrue("Folder was not found in database!", condition);
    }

    @Test
    public void shouldGetAllFolders() throws Exception {
        FolderService folderService = new FolderService();

        List<Folder> folders = folderService.getAll();
        assertEquals("Folder was not found in database!", 6, folders.size());
        for (Folder folder : folders) {
            assertNotEquals("No project assigned", null, folder.getProject());
        }

        Project project = ServiceManager.getProjectService().getById(1);
        assertEquals("No project assigned", 6, project.getFolders().size());
        for (Folder folder : project.getFolders()) {
            assertNotEquals("No project assigned", null, folder.getProject());
            assertEquals("No project assigned", project.getTitle(), folder.getProject().getTitle());
        }
    }
}
