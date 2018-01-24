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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;

public class LdapServerServiceIT {

    private static final LdapServerService ldapServerService = new ServiceManager().getLdapServerService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertLdapGroups();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindLdapGroup() throws Exception {
        LdapServer ldapServer = ldapServerService.getById(1);
        assertEquals("LpadServer title is not matching","FirstLdapServer",ldapServer.getTitle());
    }
}
