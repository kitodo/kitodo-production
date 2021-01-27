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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertForDataEditorTesting();
        MockDatabase.setUpAwaitility();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllDatabaseRowsForDataEditorSettings() throws DAOException {
        Long amount = dataEditorSettingService.countDatabaseRows();
        assertEquals("DataEditorSettings were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldGetAllDataEditorSettings() throws DAOException {
        List<DataEditorSetting> settings = dataEditorSettingService.getAll();
        assertEquals("DataEditorSettings were not found in database!", EXPECTED_DATAEDITORSETTINGS_COUNT, settings.size());
    }

    @Test
    public void shouldGetById() {
        DataEditorSetting setting = dataEditorSettingService.loadDataEditorSetting(1, 4);
        assertEquals("DataEditorSetting could not be found in database!", 3, setting.getId().intValue());
    }

    @Test
    public void shouldNotGetById() {
        DataEditorSetting setting = dataEditorSettingService.loadDataEditorSetting(1, 5);
        assertNull("No setting should be found for these ids!", setting);
    }
}
