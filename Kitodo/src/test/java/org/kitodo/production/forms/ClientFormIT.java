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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ClientFormIT {

    private final ClientForm clientForm = new ClientForm();

    /**
     * Setup Database and start elasticsearch.
     * @throws Exception If databaseConnection failed.
     */
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
    public void testRoleAdding() throws DAOException {
        clientForm.setClient(ServiceManager.getClientService().getById(1));
        int numberOfRolesForFirstClient = ServiceManager.getRoleService().getAllRolesByClientId(1).size();
        final int numberOfAuthoritiesToCopy = ServiceManager.getRoleService().getAllRolesByClientId(2).get(0).getAuthorities()
                .size();

        assertEquals(9, numberOfRolesForFirstClient, "Number of roles is incorrect");

        clientForm.getRolesForClient();
        clientForm.setClientToCopyRoles(ServiceManager.getClientService().getById(2));
        clientForm.copyRolesToClient();
        clientForm.save();

        numberOfRolesForFirstClient = ServiceManager.getRoleService().getAllRolesByClientId(1).size();
        int numberOfOldAuthorities = ServiceManager.getRoleService().getAllRolesByClientId(2).get(0).getAuthorities()
                .size();
        int numberOfNewAuthorities = ServiceManager.getRoleService().getAllRolesByClientId(1).get(9).getAuthorities()
                .size();
        assertEquals(11, numberOfRolesForFirstClient, "Role was not added");
        assertEquals(numberOfOldAuthorities, numberOfNewAuthorities, "Authorities were not added");
        assertEquals(numberOfAuthoritiesToCopy, numberOfOldAuthorities, "Authorities were removed from second client");
    }
}
