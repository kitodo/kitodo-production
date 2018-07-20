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

package org.kitodo.security;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class SecurityAccessServiceIT {

    ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @After
    public void cleanContext() {
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void isAdminTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertTrue("Checking if user is admin returned wrong value",
            serviceManager.getSecurityAccessService().isAdmin());
    }

    @Test
    public void hasAnyAuthorityGlobalTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertTrue("Checking if user kowal has any global authority returned wrong value",
            serviceManager.getSecurityAccessService().hasAnyAuthorityGlobal());
    }

    @Test
    public void hasNotAnyAuthorityGlobalTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("mmustermann");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertFalse("Checking if user mmustermann has any global authority returned wrong value",
            serviceManager.getSecurityAccessService().hasAnyAuthorityGlobal());
    }

    @Test
    public void getClientIdListForAnyAuthorityTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        List<Integer> ids = serviceManager.getSecurityAccessService().getClientIdListForAnyAuthority();
        Assert.assertEquals("Getting for which clients user has authorities returned wrong first value", 1,
            ids.get(0).intValue());
        Assert.assertEquals("Getting for which clients user has authorities returned wrong second value", 2,
            ids.get(1).intValue());
        Assert.assertEquals("Getting for which clients user has authorities returned wrong result count", 2,
            ids.size());
    }

    @Test
    public void getClientIdListForAuthorityTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        List<Integer> ids = serviceManager.getSecurityAccessService().getClientIdListForAuthority("viewClient");
        Assert.assertEquals("Getting for which clients user has the viewClient authority returned wrong value", 1,
            ids.get(0).intValue());
        Assert.assertEquals("Getting for which clients user has the viewClient authority returned wrong result count",
            1, ids.size());
    }

    @Test
    public void getProjectIdListForAnyAuthorityTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        List<Integer> ids = serviceManager.getSecurityAccessService().getProjectIdListForAnyAuthority();
        Assert.assertEquals("Getting for which projects user has authorities returned wrong value", 1,
            ids.get(0).intValue());
        Assert.assertEquals("Getting for which projects user has authorities returned wrong result count", 1,
            ids.size());
    }
}
