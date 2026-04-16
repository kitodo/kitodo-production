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

package org.kitodo.production.forms;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectFormIT {

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
    public void shouldDeleteFolderInSandbox() throws Exception {
        ProjectForm view = new ProjectForm();
        Project project = ServiceManager.getProjectService().getById(1);
        view.loadProject(project.getId());
        int initialSize = project.getFolders().size();
        assertTrue(initialSize > 0);
        Folder toDelete = view.getFolderList().get(0);
        view.setEditingFolder(toDelete);
        view.deleteFolder();
        assertEquals(initialSize - 1, view.getFolderList().size(), "Sandbox should reflect deletion");
        assertEquals(initialSize, project.getFolders().size(), "Entity should remain untouched until save");
        view.save();
        Project updated = ServiceManager.getProjectService().getById(1);
        assertEquals(initialSize - 1, updated.getFolders().size());
    }

    @Test
    public void shouldAddFolderInSandbox() throws Exception {
        ProjectForm view = new ProjectForm();
        Project project = ServiceManager.getProjectService().getById(1);
        view.loadProject(project.getId());
        int initialSize = project.getFolders().size();

        view.addFolder();
        Folder newFolder = view.getEditingFolder();
        newFolder.setPath("newFolder");
        newFolder.setFileGroup("NEW_GROUP");
        newFolder.setMimeType("image/jpeg");

        view.saveFolder();
        assertEquals(initialSize + 1, view.getFolderList().size(), "Sandbox should reflect addition");
        assertEquals(initialSize, project.getFolders().size(), "Entity should remain untouched until save");
        view.save();
        Project updated = ServiceManager.getProjectService().getById(1);
        assertEquals(initialSize + 1, updated.getFolders().size());
        assertTrue(updated.getFolders().stream()
                .anyMatch(f -> "NEW_GROUP".equals(f.getFileGroup())));
    }

    @Test
    public void shouldRollbackOnCancel() throws Exception {
        ProjectForm view = new ProjectForm();
        Project project = ServiceManager.getProjectService().getById(1);
        view.loadProject(project.getId());
        int initialSize = project.getFolders().size();

        view.addFolder();
        view.getEditingFolder().setFileGroup("TEMP");
        view.saveFolder();
        assertEquals(initialSize + 1, view.getFolderList().size());
        view.cancel();
        assertEquals(initialSize, view.getFolderList().size(), "Sandbox should have reset to original entity state");
    }
}
