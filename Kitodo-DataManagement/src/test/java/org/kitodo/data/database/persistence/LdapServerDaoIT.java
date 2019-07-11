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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.exceptions.DAOException;

public class LdapServerDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<LdapServer> ldapServers = getAuthorities();

        LdapServerDAO ldapServerDAO = new LdapServerDAO();
        ldapServerDAO.save(ldapServers.get(0));
        ldapServerDAO.save(ldapServers.get(1));
        ldapServerDAO.save(ldapServers.get(2));

        assertEquals("Objects were not saved or not found!", 3, ldapServerDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, ldapServerDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_ldapServer", ldapServerDAO.getById(1).getTitle());

        ldapServerDAO.remove(1);
        ldapServerDAO.remove(ldapServers.get(1));
        assertEquals("Objects were not removed or not found!", 1, ldapServerDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        ldapServerDAO.getById(1);
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
