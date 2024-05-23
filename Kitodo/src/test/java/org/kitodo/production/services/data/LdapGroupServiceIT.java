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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.LdapGroup;

/**
 * Tests for LdapGroupService class.
 */
public class LdapGroupServiceIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertLdapGroups();
    }

    @AfterAll
    public static void cleanDatabase() {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindLdapGroup() throws Exception {
        LdapGroupService ldapGroupService = new LdapGroupService();

        LdapGroup ldapGroup = ldapGroupService.getById(1);
        boolean condition = ldapGroup.getTitle().equals("LG") && ldapGroup.getDisplayName().equals("Name");
        assertTrue(condition, "LDAP group was not found in database!");

        assertEquals("FirstLdapServer", ldapGroup.getLdapServer().getTitle(), "Title of Ldap server is not matching");
    }
}
