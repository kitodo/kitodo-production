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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kitodo.data.database.beans.Folder;

public class FolderProcessingSwitchTest {
    @Test
    public void getLabelTest() {
        Folder folderToBeGenerated = new Folder();
        folderToBeGenerated.setPath("folderToBeGenerated");
        FolderProcessingSwitch folderProcessingSwitch = new FolderProcessingSwitch(folderToBeGenerated,
                new ArrayList<>());
        assertEquals("folderToBeGenerated", folderProcessingSwitch.getLabel());
    }

    @Test
    public void isValueTest() {
        Folder folder = new Folder();
        List<Folder> activatedFolders = new ArrayList<>();
        FolderProcessingSwitch folderProcessingSwitch = new FolderProcessingSwitch(folder, activatedFolders);
        assertFalse(folderProcessingSwitch.isValue());
        activatedFolders.add(folder);
        assertTrue(folderProcessingSwitch.isValue());
        activatedFolders.remove(folder);
        assertFalse(folderProcessingSwitch.isValue());
    }

    @Test
    public void setValueTest() {
        Folder folder = new Folder();
        List<Folder> activatedFolders = new ArrayList<>();
        FolderProcessingSwitch folderProcessingSwitch = new FolderProcessingSwitch(folder, activatedFolders);
        assertFalse(activatedFolders.contains(folder));
        folderProcessingSwitch.setValue(true);
        assertTrue(activatedFolders.contains(folder));
        folderProcessingSwitch.setValue(false);
        assertFalse(activatedFolders.contains(folder));
    }
}
