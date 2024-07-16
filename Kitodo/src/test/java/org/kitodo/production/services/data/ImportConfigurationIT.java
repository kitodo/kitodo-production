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
import static org.junit.Assert.assertFalse;
import static org.kitodo.test.utils.TestConstants.GBV;
import static org.kitodo.test.utils.TestConstants.KALLIOPE;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ImportConfigurationIT {

    /**
     * Insert test mapping files and import configurations into database.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
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
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        List<ImportConfiguration> configs = ServiceManager.getImportConfigurationService().getAll();
        assertEquals("Wrong number of import configurations", Long.valueOf(3), Long.valueOf(configs.size()));
        assertEquals("Wrong first import configuration", GBV, configs.get(0).getTitle());
        assertEquals("Wrong last import configuration", KALLIOPE, configs.get(2).getTitle());
    }

    /**
     * Verifies that a user does not have access to import configurations assigned to clients to which the user is not
     * assigned.
     * @throws DAOException when loading of ImportConfigurations fails
     */
    @Test
    public void shouldNotGetImportConfigurationsOfUnassigedClients() throws DAOException {
        User userThree = ServiceManager.getUserService().getById(3);
        SecurityTestUtils.addUserDataToSecurityContext(userThree, 2);
        List<ImportConfiguration> configs = ServiceManager.getImportConfigurationService().getAll();
        assertEquals("Wrong number of import configurations", Long.valueOf(2), Long.valueOf(configs.size()));
        assertFalse("User should not have access to import configuration 'Kalliope' of unassigned client",
                configs.stream().anyMatch(config -> KALLIOPE.equals(config.getTitle())));
    }

    @After
    public void cleanupSecurityContext() {
        SecurityTestUtils.cleanSecurityContext();
    }

    /**
     * Clean up test database.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

}
