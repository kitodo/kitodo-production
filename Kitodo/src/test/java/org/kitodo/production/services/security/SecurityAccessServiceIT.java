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

package org.kitodo.production.services.security;

import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessServiceIT {

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
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        Assert.assertEquals("Security context holder does not hold the corresponding authorities", 171,
            authorities.size());
    }

    @Test
    public void hasAuthorityTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        Assert.assertTrue("The authority \"editClient\" was not found for authenticated user",
            ServiceManager.getSecurityAccessService().hasAuthorityGlobal("editClient"));
    }

    @Test
    public void hasAuthorityForClientTest() throws DAOException {
        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user,1);
        Assert.assertTrue("Checking if user has edit project authority for first client returned wrong value",
            ServiceManager.getSecurityAccessService().hasAuthorityForClient("editProject"));
    }
}
