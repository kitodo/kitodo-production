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


import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectEditViewIT {

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
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService().getProjectWithFolders(1).orElseThrow();
        view.loadProject(project.getId(), false);
        int initialSize = project.getFolders().size();
        assertTrue(initialSize > 0);
        Folder toDelete = view.getFolderList().getFirst();
        view.setEditingFolder(toDelete);
        view.deleteFolder();
        assertEquals(initialSize - 1, view.getFolderList().size(), "Sandbox should reflect deletion");
        assertEquals(initialSize, project.getFolders().size(), "Entity should remain untouched until save");
        view.save();
        Project updated = ServiceManager.getProjectService().getProjectWithFolders(project.getId()).orElseThrow();
        assertEquals(initialSize - 1, updated.getFolders().size());
    }

    @Test
    public void shouldAddFolderInSandbox() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService().getProjectWithFolders(1).orElseThrow();
        view.loadProject(project.getId(), false);
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
        Project updated = ServiceManager.getProjectService().getProjectWithFolders(project.getId()).orElseThrow();
        assertEquals(initialSize + 1, updated.getFolders().size());
        assertTrue(updated.getFolders().stream()
                .anyMatch(f -> "NEW_GROUP".equals(f.getFileGroup())));
    }

    @Test
    public void shouldRollbackOnCancel() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService().getProjectWithFolders(1).orElseThrow();
        view.loadProject(project.getId(), false);
        int initialSize = project.getFolders().size();

        view.addFolder();
        view.getEditingFolder().setFileGroup("TEMP");
        view.saveFolder();
        assertEquals(initialSize + 1, view.getFolderList().size());
        view.cancel();
        assertEquals(initialSize, view.getFolderList().size(), "Sandbox should have reset to original entity state");
    }

    @Test
    public void shouldRejectDuplicateTransientFileGroup() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService()
                .getProjectWithFolders(1)
                .orElseThrow();

        view.loadProject(project.getId(), false);

        view.addFolder();
        Folder first = view.getEditingFolder();
        first.setFileGroup("DUPLICATE");
        first.setMimeType("image/jpeg");
        view.saveFolder();

        assertEquals(1,
                view.getFolderList().stream()
                        .filter(f -> "DUPLICATE".equals(f.getFileGroup()))
                        .count());

        view.addFolder();
        Folder second = view.getEditingFolder();
        second.setFileGroup("DUPLICATE");
        second.setMimeType("image/jpeg");

        view.saveFolder();

        assertEquals(1,
                view.getFolderList().stream()
                        .filter(f -> "DUPLICATE".equals(f.getFileGroup()))
                        .count());
    }

    @Test
    public void shouldRejectDuplicateEmptyFileGroup() throws Exception {
        ProjectEditView view = new ProjectEditView();
        Project project = ServiceManager.getProjectService()
                .getProjectWithFolders(1)
                .orElseThrow();

        view.loadProject(project.getId(), false);

        view.addFolder();
        Folder first = view.getEditingFolder();
        first.setFileGroup("");
        first.setMimeType("image/jpeg");
        view.saveFolder();

        view.addFolder();
        Folder second = view.getEditingFolder();
        second.setFileGroup("");
        second.setMimeType("image/jpeg");
        view.saveFolder();

        long emptyCount = view.getFolderList().stream()
                .filter(f -> Objects.nonNull(f.getFileGroup())
                        && f.getFileGroup().isEmpty())
                .count();

        assertEquals(1, emptyCount);
    }

    @Test
    public void shouldHandleFolderLifecycleAcrossMultipleSaves() throws Exception {
        /*
         * STEP 1
         * Load + add folders
         */
        ProjectEditView view = new ProjectEditView();

        Project project = ServiceManager.getProjectService()
                .getProjectWithFolders(1)
                .orElseThrow();

        view.loadProject(project.getId(), false);

        view.addFolder();
        Folder folderA = view.getEditingFolder();
        folderA.setFileGroup("GROUP_A");
        folderA.setMimeType("image/jpeg");
        folderA.setPath("folderA");
        view.saveFolder();

        view.addFolder();
        Folder folderB = view.getEditingFolder();
        folderB.setFileGroup("GROUP_B");
        folderB.setMimeType("image/jpeg");
        folderB.setPath("folderB");
        view.saveFolder();

        view.save();

        /*
         * STEP 2
         * Reload + assign usages
         */
        view = new ProjectEditView();

        project = ServiceManager.getProjectService()
                .getProjectWithFolders(project.getId())
                .orElseThrow();

        view.loadProject(project.getId(), false);

        view.setPreview("GROUP_A");
        view.setMediaView("GROUP_A");
        view.setGeneratorSource("GROUP_A");

        view.save();

        /*
         * STEP 3
         * Reload + delete referenced folder
         */
        view = new ProjectEditView();

        project = ServiceManager.getProjectService()
                .getProjectWithFolders(project.getId())
                .orElseThrow();

        view.loadProject(project.getId(), false);

        Folder folderAFromReload = view.getFolderList().stream()
                .filter(folder -> "GROUP_A".equals(folder.getFileGroup()))
                .findFirst()
                .orElseThrow();
       /*
         * Reassign usages
         */
        view.setPreview("GROUP_B");
        view.setMediaView("GROUP_B");
        view.setGeneratorSource("GROUP_B");

        /*
         * Delete old folder
         */
        view.setEditingFolder(folderAFromReload);
        view.deleteFolder();

        view.save();

        /*
         * STEP 4
         * Verify persisted state
         */
        Project updated = ServiceManager.getProjectService()
                .getProjectWithFolders(project.getId())
                .orElseThrow();

        assertTrue(updated.getFolders().stream()
                .noneMatch(folder -> "GROUP_A".equals(folder.getFileGroup())));

        assertEquals("GROUP_B",
                updated.getPreview().getFileGroup());

        assertEquals("GROUP_B",
                updated.getMediaView().getFileGroup());

        assertEquals("GROUP_B",
                updated.getGeneratorSource().getFileGroup());
    }
}
