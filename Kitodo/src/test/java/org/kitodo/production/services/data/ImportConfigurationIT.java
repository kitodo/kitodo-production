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

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ImportConfigurationIT {

    /**
     * Insert test mapping files and import configurations into database.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.setUpAwaitility();
    }

    /**
     * Verifies that ImportConfigurations are returned in alphabetical order by ImportConfigurationService.
     * @throws DAOException when loading of ImportConfigurations fails
     */
    @Test
    public void shouldGetAllImportConfigurationsSortedAlphabetically() throws DAOException {
        List<ImportConfiguration> configs = ServiceManager.getImportConfigurationService().getAll();
        assertEquals(Long.valueOf(3), Long.valueOf(configs.size()), "Wrong number of import configurations");
        assertEquals("GBV", configs.get(0).getTitle(), "Wrong first import configuration");
        assertEquals("Kalliope", configs.get(2).getTitle(), "Wrong last import configuration");
    }

    /**
     * Clean up test database.
     */
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

}
