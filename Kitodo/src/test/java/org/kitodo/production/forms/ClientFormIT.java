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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ClientFormIT {

    private ClientForm clientForm = new ClientForm();

    /**
     * Setup Database and start elasticsearch.
     * @throws Exception If databaseConnection failed.
     */
    @BeforeClass
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
    @AfterClass
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

        Assert.assertEquals("Number of roles is incorrect", 8, numberOfRolesForFirstClient);

        clientForm.getRolesForClient();
        clientForm.setClientToCopyRoles(ServiceManager.getClientService().getById(2));
        clientForm.copyRolesToClient();
        clientForm.save();

        numberOfRolesForFirstClient = ServiceManager.getRoleService().getAllRolesByClientId(1).size();
        int numberOfOldAuthorities = ServiceManager.getRoleService().getAllRolesByClientId(2).get(0).getAuthorities()
                .size();
        int numberOfNewAuthorities = ServiceManager.getRoleService().getAllRolesByClientId(1).get(8).getAuthorities()
                .size();
        Assert.assertEquals("Role was not added", 9, numberOfRolesForFirstClient);
        Assert.assertEquals("Authorities were not added", numberOfOldAuthorities, numberOfNewAuthorities);
        Assert.assertEquals("Authorities were removed from second client", numberOfAuthoritiesToCopy,
                numberOfOldAuthorities);
    }
}
