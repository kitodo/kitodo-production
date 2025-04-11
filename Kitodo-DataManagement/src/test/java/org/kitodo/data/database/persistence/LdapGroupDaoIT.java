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
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;

public class LdapGroupDaoIT {

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
        List<LdapGroup> ldapGroups = getAuthorities();

        LdapGroupDAO ldapGroupDAO = new LdapGroupDAO();
        ldapGroupDAO.save(ldapGroups.get(0));
        ldapGroupDAO.save(ldapGroups.get(1));
        ldapGroupDAO.save(ldapGroups.get(2));

        assertEquals(3, ldapGroupDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, ldapGroupDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_ldapGroup", ldapGroupDAO.getById(1).getTitle(), "Object was not saved or not found!");

        ldapGroupDAO.remove(1);
        ldapGroupDAO.remove(ldapGroups.get(1));
        assertEquals(1, ldapGroupDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> ldapGroupDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<LdapGroup> getAuthorities() {
        LdapGroup firstLdapGroup = new LdapGroup();
        firstLdapGroup.setTitle("first_ldapGroup");

        LdapGroup secondLdapGroup = new LdapGroup();
        secondLdapGroup.setTitle("second_ldapGroup");

        LdapGroup thirdLdapGroup = new LdapGroup();
        thirdLdapGroup.setTitle("third_ldapGroup");

        List<LdapGroup> ldapGroups = new ArrayList<>();
        ldapGroups.add(firstLdapGroup);
        ldapGroups.add(secondLdapGroup);
        ldapGroups.add(thirdLdapGroup);
        return ldapGroups;
    }
}
