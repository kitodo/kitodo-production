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
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;

public class LdapGroupDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<LdapGroup> ldapGroups = getAuthorities();

        LdapGroupDAO ldapGroupDAO = new LdapGroupDAO();
        ldapGroupDAO.save(ldapGroups.get(0));
        ldapGroupDAO.save(ldapGroups.get(1));
        ldapGroupDAO.save(ldapGroups.get(2));

        assertEquals("Objects were not saved or not found!", 3, ldapGroupDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, ldapGroupDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_ldapGroup", ldapGroupDAO.getById(1).getTitle());

        ldapGroupDAO.remove(1);
        ldapGroupDAO.remove(ldapGroups.get(1));
        assertEquals("Objects were not removed or not found!", 1, ldapGroupDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        ldapGroupDAO.getById(1);
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
