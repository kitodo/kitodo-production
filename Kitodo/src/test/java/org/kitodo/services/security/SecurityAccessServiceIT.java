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

package org.kitodo.services.security;

import java.util.Collection;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessServiceIT {

    private static ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertForAuthenticationTesting();
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
    public void shouldGetAuthorities() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        Assert.assertEquals("Security context holder does not hold the corresponding authorities", 149,
            authorities.size());
    }

    @Test
    public void hasAuthorityTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertTrue("The authority \"editClient\" was not found for authenticated user",
            serviceManager.getSecurityAccessService().hasAuthorityGlobal("editClient"));
    }

    @Test
    public void isAdminTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertTrue("Checking if user is admin returned wrong value",
            serviceManager.getSecurityAccessService().isAdmin());
    }

    @Test
    public void hasAuthorityForClientTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        Assert.assertTrue("Checking if user is admin returned wrong value",
            serviceManager.getSecurityAccessService().hasAuthorityForClient("editProject", 1));
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
    public void getClientIdListForAuthorityTest() throws DAOException {
        User user = serviceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user);
        List<Integer> ids = serviceManager.getSecurityAccessService().getClientIdListForAuthority("viewClient");
        Assert.assertEquals("Getting for which clients user has the viewClient authority returned wrong value", 1,
            ids.get(0).intValue());
        Assert.assertEquals("Getting for which clients user has the viewClient authority returned wrong result count",
            1, ids.size());
    }
}
