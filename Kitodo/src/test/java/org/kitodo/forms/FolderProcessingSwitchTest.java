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

package org.kitodo.forms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
        assertThat(folderProcessingSwitch.getLabel(), is(equalTo(("folderToBeGenerated"))));
    }

    @Test
    public void isValueTest() {
        Folder folder = new Folder();
        List<Folder> activatedFolders = new ArrayList<>();
        FolderProcessingSwitch folderProcessingSwitch = new FolderProcessingSwitch(folder, activatedFolders);
        assertThat(folderProcessingSwitch.isValue(), is(equalTo((false))));
        activatedFolders.add(folder);
        assertThat(folderProcessingSwitch.isValue(), is(equalTo((true))));
        activatedFolders.remove(folder);
        assertThat(folderProcessingSwitch.isValue(), is(equalTo((false))));
    }

    @Test
    public void setValueTest() {
        Folder folder = new Folder();
        List<Folder> activatedFolders = new ArrayList<>();
        FolderProcessingSwitch folderProcessingSwitch = new FolderProcessingSwitch(folder, activatedFolders);
        assertThat(activatedFolders, not(contains(folder)));
        folderProcessingSwitch.setValue(true);
        assertThat(activatedFolders, contains(folder));
        folderProcessingSwitch.setValue(false);
        assertThat(activatedFolders, not(contains(folder)));
    }
}
