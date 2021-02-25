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

package org.kitodo.data.database.persistence;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.exceptions.DAOException;


public class DataEditorSettingDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test saving and loading DataEditorSettings.
     * @throws DAOException when loading or saving fails
     */
    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<DataEditorSetting> dataEditorSettings = getDataEditorSettings();

        DataEditorSettingDAO dataEditorSettingDAO = new DataEditorSettingDAO();
        dataEditorSettingDAO.save(dataEditorSettings.get(0));
        dataEditorSettingDAO.save(dataEditorSettings.get(1));
        dataEditorSettingDAO.save(dataEditorSettings.get(2));

        assertEquals("Objects were not saved or not found!", 3, dataEditorSettingDAO.getAll().size());
        assertEquals(0.5f, dataEditorSettingDAO.getById(2).getGalleryWidth(), 0);
    }

    private List<DataEditorSetting> getDataEditorSettings() {
        DataEditorSetting firstSetting = new DataEditorSetting();
        firstSetting.setTaskId(1);
        firstSetting.setUserId(1);
        firstSetting.setStructureWidth(0.2f);
        firstSetting.setMetadataWidth(0.4f);
        firstSetting.setGalleryWidth(0.4f);

        DataEditorSetting secondSetting = new DataEditorSetting();
        secondSetting.setTaskId(2);
        secondSetting.setUserId(1);
        secondSetting.setStructureWidth(0.5f);
        secondSetting.setMetadataWidth(0f);
        secondSetting.setGalleryWidth(0.5f);

        DataEditorSetting thirdSetting = new DataEditorSetting();
        thirdSetting.setTaskId(3);
        thirdSetting.setUserId(1);
        thirdSetting.setStructureWidth(0f);
        thirdSetting.setMetadataWidth(0f);
        thirdSetting.setGalleryWidth(1f);

        List<DataEditorSetting> settings = new ArrayList<>();
        settings.add(firstSetting);
        settings.add(secondSetting);
        settings.add(thirdSetting);
        return settings;
    }
}
