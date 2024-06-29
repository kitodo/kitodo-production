/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.services.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.production.services.ServiceManager;

public class LdapServerServiceIT {

    private static final LdapServerService ldapServerService = ServiceManager.getLdapServerService();

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertLdapGroups();
    }

    @AfterAll
    public static void cleanDatabase() {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindLdapServer() throws Exception {
        LdapServer ldapServer = ldapServerService.getById(1);
        assertEquals("FirstLdapServer", ldapServer.getTitle(), "LpadServer title is not matching");
        assertFalse(ldapServer.isUseSsl(), "LpadServer useSsl is not matching");
        assertEquals("SHA", ldapServer.getPasswordEncryption().getTitle(), "LdapServer password encoding is not matching");
    }
}
