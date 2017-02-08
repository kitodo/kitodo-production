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

package org.kitodo.services;

import org.junit.Test;

import org.kitodo.data.database.beans.LdapGroup;

import static org.junit.Assert.*;

/**
 * Tests for LdapGroupService class.
 */
public class LdapGroupServiceTest {

    @Test
    public void shouldFindLdapGroup() throws Exception {
        LdapGroupService ldapGroupService = new LdapGroupService();

        LdapGroup ldapGroup = ldapGroupService.find(1);
        boolean condition = ldapGroup.getTitle().equals("LG") && ldapGroup.getDisplayName().equals("Name");
        assertTrue("LDAP group was not found in database!", condition);
    }
}
