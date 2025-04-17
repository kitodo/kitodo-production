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

package org.kitodo.data.database.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockIndex;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.exceptions.DAOException;

public class LdapServerDaoIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockIndex.startNode();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockIndex.stopNode();
    }

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<LdapServer> ldapServers = getAuthorities();

        LdapServerDAO ldapServerDAO = new LdapServerDAO();
        ldapServerDAO.save(ldapServers.get(0));
        ldapServerDAO.save(ldapServers.get(1));
        ldapServerDAO.save(ldapServers.get(2));

        assertEquals(3, ldapServerDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, ldapServerDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_ldapServer", ldapServerDAO.getById(1).getTitle(), "Object was not saved or not found!");

        ldapServerDAO.remove(1);
        ldapServerDAO.remove(ldapServers.get(1));
        assertEquals(1, ldapServerDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> ldapServerDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<LdapServer> getAuthorities() {
        LdapServer firstLdapServer = new LdapServer();
        firstLdapServer.setTitle("first_ldapServer");

        LdapServer secondLdapServer = new LdapServer();
        secondLdapServer.setTitle("second_ldapServer");

        LdapServer thirdLdapServer = new LdapServer();
        thirdLdapServer.setTitle("third_ldapServer");

        List<LdapServer> ldapServers = new ArrayList<>();
        ldapServers.add(firstLdapServer);
        ldapServers.add(secondLdapServer);
        ldapServers.add(thirdLdapServer);
        return ldapServers;
    }
}
