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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for TaskService class.
 */
public class FolderServiceIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindFolder() throws Exception {
        FolderService folderService = new FolderService();

        Folder folder = folderService.getById(1);
        boolean condition = folder.getFileGroup().equals("MAX") && folder.getMimeType().equals("image/jpeg");
        assertTrue(condition, "Folder was not found in database!");
    }

    @Test
    public void shouldGetAllFolders() throws Exception {
        FolderService folderService = new FolderService();

        List<Folder> folders = folderService.getAll();
        assertEquals(6, folders.size(), "Folder was not found in database!");
        for (Folder folder : folders) {
            assertNotEquals(null, folder.getProject(), "No project assigned");
        }

        Project project = ServiceManager.getProjectService().getById(1);
        assertEquals(6, project.getFolders().size(), "No project assigned");
        for (Folder folder : project.getFolders()) {
            assertNotEquals(null, folder.getProject(), "No project assigned");
            assertEquals(project.getTitle(), folder.getProject().getTitle(), "No project assigned");
        }
    }
}
