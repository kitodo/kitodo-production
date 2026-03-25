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
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.forms.dto.FolderDTO;
import org.kitodo.production.services.ServiceManager;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectEditViewIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }
    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception
     *             if elasticsearch could not been stopped.
     */
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldDeleteFolderViaDTO() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService().getProjectWithFolders(1).orElseThrow();
        view.setProject(project);
        int initialEntitySize = project.getFolders().size();
        assertTrue(initialEntitySize > 0);
        int initialDtoSize = view.getFolderList().size();
        assertEquals(initialEntitySize, initialDtoSize);

        view.getFolderList().removeFirst();
        assertEquals(initialDtoSize - 1, view.getFolderList().size());
        assertEquals(initialEntitySize, project.getFolders().size());

        view.save();
        Project updated = ServiceManager.getProjectService().getProjectWithFolders(project.getId()).orElseThrow();
        assertEquals(initialEntitySize - 1, updated.getFolders().size());
    }

    @Test
    public void shouldAddFolderViaDTO() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService()
                .getProjectWithFolders(1).orElseThrow();

        view.setProject(project);
        int initialEntitySize = project.getFolders().size();
        int initialDtoSize = view.getFolderList().size();

        assertEquals(initialEntitySize, initialDtoSize);

        FolderDTO newFolder = new FolderDTO();
        newFolder.setPath("newFolder");
        newFolder.setFileGroup("NEW");
        newFolder.setMimeType("image/jpeg");
        view.getFolderList().add(newFolder);

        assertEquals(initialDtoSize + 1, view.getFolderList().size());
        assertEquals(initialEntitySize, project.getFolders().size());

        view.save();
        Project updated = ServiceManager.getProjectService()
                .getProjectWithFolders(project.getId()).orElseThrow();
        assertEquals(initialEntitySize + 1, updated.getFolders().size());
        assertTrue(updated.getFolders().stream()
                .anyMatch(f -> "newFolder".equals(f.getPath())));
    }
}
