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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class DataEditorSettingServiceIT {

    private static final DataEditorSettingService dataEditorSettingService = ServiceManager.getDataEditorSettingService();
    private static final int EXPECTED_DATAEDITORSETTINGS_COUNT = 3;

    /**
     * Prepare database for tests.
     * @throws Exception when preparation fails
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertForDataEditorTesting();
        MockDatabase.setUpAwaitility();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllDatabaseRowsForDataEditorSettings() throws DAOException {
        Long amount = dataEditorSettingService.countDatabaseRows();
        assertEquals(Long.valueOf(3), amount, "DataEditorSettings were not counted correctly!");
    }

    @Test
    public void shouldGetAllDataEditorSettings() throws DAOException {
        List<DataEditorSetting> settings = dataEditorSettingService.getAll();
        assertEquals(EXPECTED_DATAEDITORSETTINGS_COUNT, settings.size(), "DataEditorSettings were not found in database!");
    }

    @Test
    public void shouldGetById() {
        DataEditorSetting setting = dataEditorSettingService.loadDataEditorSetting(1, 4);
        assertEquals(3, setting.getId().intValue(), "DataEditorSetting could not be found in database!");
    }

    @Test
    public void shouldNotGetById() {
        DataEditorSetting setting = dataEditorSettingService.loadDataEditorSetting(1, 5);
        assertNull(setting, "No setting should be found for these ids!");
    }
}
