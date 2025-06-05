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

package org.kitodo.production.services.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class LongTermPreservationValidationServiceIT {

    /**
     * Insert test mapping files and import configurations into database.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    /**
     * Verifies that all LtpValidationConfigurations stored in the MockDatabase are returned by the service class.
     * 
     * @throws DAOException when loading of LtpValidationConfigurations fails
     */
    @Test
    public void shouldGetAllLtpValidationConfigurations() throws DAOException {
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        List<LtpValidationConfiguration> configs = ServiceManager.getLtpValidationConfigurationService().getAll();
        assertEquals(2, configs.size(), "Wrong number of ltp validation configurations");
        assertEquals("Valid Tif", configs.get(0).getTitle(), "Wrong title for tif ltp validation configuration");
        assertEquals(2, configs.get(0).getValidationConditions().size(), 
            "Wrong number of validation conditions for first ltp validation configuration");
        assertEquals("Wellformed Jpeg", configs.get(1).getTitle(), "Wrong title for second ltp validation configuration");
        assertEquals(1, configs.get(1).getValidationConditions().size(), 
            "Wrong number of validation conditions for second ltp validation configuration");
    }

    @AfterEach
    public void cleanupSecurityContext() {
        SecurityTestUtils.cleanSecurityContext();
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
