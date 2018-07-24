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

package org.kitodo.services.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.services.ServiceManager;

public class UserGroupClientAuthorityRelationServiceIT {
    private static final UserGroupClientAuthorityRelationService userGroupClientAuthorityRelationService = new ServiceManager()
            .getUserGroupClientAuthorityRelationService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertForAuthenticationTesting();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetRelation() throws Exception {
        UserGroupClientAuthorityRelation relation = userGroupClientAuthorityRelationService.getById(1);
        assertEquals("Client name is not matching", "First client", relation.getClient().getName());
        assertEquals("Authority title is not matching", "viewAllClients", relation.getAuthority().getTitle());
        assertEquals("UserGroup title is not matching", "Admin", relation.getUserGroup().getTitle());

        relation = userGroupClientAuthorityRelationService.getById(7);
        assertEquals("Client title is not matching", "Second client", relation.getClient().getName());
        assertEquals("UserGroup title is not matching", "Without authorities", relation.getUserGroup().getTitle());
        assertEquals("Authority title is not matching", "viewAllClients", relation.getAuthority().getTitle());
    }

    @Test
    public void shouldCountDataBaseRows() throws Exception {
        long rows = userGroupClientAuthorityRelationService.countDatabaseRows();
        assertEquals("Client name is not matching", 7L, rows);
    }

    @Test
    public void shouldGetAuthorities() {
        List<Authority> authorities = userGroupClientAuthorityRelationService.getAuthoritiesByUserGroupAndClient(1, 1);
        assertEquals("Number of returned authorities is not matching", 3, authorities.size());

        authorities = userGroupClientAuthorityRelationService.getAuthoritiesByUserGroupAndClient(2, 1);
        assertEquals("Number of returned authorities is not matching", 2, authorities.size());

        authorities = userGroupClientAuthorityRelationService.getAuthoritiesByUserGroupAndClient(1, 2);
        assertEquals("Number of returned authorities is not matching", 1, authorities.size());

        authorities = userGroupClientAuthorityRelationService.getAuthoritiesByUserGroupAndClient(3, 2);
        assertEquals("Number of returned authorities is not matching", 1, authorities.size());

    }
}
