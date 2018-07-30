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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.services.ServiceManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAccessServiceIT {

    private static ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertUserGroupsFull();
        SecurityTestUtils.addUserDataToSecurityContext(serviceManager.getUserService().getById(1));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        Assert.assertEquals("Security context holder does not hold the corresponding authorities", 44,
            authorities.size());
    }

    @Test
    public void isAdminTest() {
        Assert.assertTrue("Authenticated user was not identified as admin",
            serviceManager.getSecurityAccessService().isAdmin());
    }

    @Test
    public void hasAuthorityTest() {
        Assert.assertTrue("The authority \"editClient\" was not found for authenticated user",
            serviceManager.getSecurityAccessService().hasAuthorityGlobal("editClient"));
    }
}
