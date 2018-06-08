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
import org.kitodo.data.database.beans.UserGroupProjectAuthorityRelation;
import org.kitodo.services.ServiceManager;

public class UserGroupProjectAuthorityRelationServiceIT {
    private static final UserGroupProjectAuthorityRelationService userGroupProjectAuthorityRelationService = new ServiceManager()
            .getUserGroupProjectAuthorityRelationService();

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
        UserGroupProjectAuthorityRelation relation = userGroupProjectAuthorityRelationService.getById(1);
        assertEquals("Project title is not matching", "First project", relation.getProject().getTitle());
        assertEquals("Authority title is not matching", "viewAllClients", relation.getAuthority().getTitle());
        assertEquals("UserGroup title is not matching", "Admin", relation.getUserGroup().getTitle());

        relation = userGroupProjectAuthorityRelationService.getById(6);
        assertEquals("Project title is not matching", "Second project", relation.getProject().getTitle());
        assertEquals("UserGroup title is not matching", "Without authorities", relation.getUserGroup().getTitle());
        assertEquals("Authority title is not matching", "viewAllClients", relation.getAuthority().getTitle());
    }

    @Test
    public void shouldCountDataBaseRows() throws Exception {
        long rows = userGroupProjectAuthorityRelationService.countDatabaseRows();
        assertEquals("Number of database rows is not matching", 7L, rows);
    }

    @Test
    public void shouldGetAuthorities() throws Exception {
        List<Authority> authorities = userGroupProjectAuthorityRelationService.getAuthoritiesByUserGroupAndProjectId(1,
            1);
        assertEquals("Number of returned authorities is not matching", 3, authorities.size());

        authorities = userGroupProjectAuthorityRelationService.getAuthoritiesByUserGroupAndProjectId(2, 1);
        assertEquals("Number of returned authorities is not matching", 1, authorities.size());

        authorities = userGroupProjectAuthorityRelationService.getAuthoritiesByUserGroupAndProjectId(1, 2);
        assertEquals("Number of returned authorities is not matching", 0, authorities.size());

        authorities = userGroupProjectAuthorityRelationService.getAuthoritiesByUserGroupAndProjectId(3, 2);
        assertEquals("Number of returned authorities is not matching", 1, authorities.size());

        authorities = userGroupProjectAuthorityRelationService.getAuthoritiesByUserGroupAndProjectId(3, 3);
        assertEquals("Number of returned authorities is not matching", 1, authorities.size());

    }
}
