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
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;

public class RoleDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Role> roles = getRoles();

        RoleDAO roleDAO = new RoleDAO();
        roleDAO.save(roles.get(0));
        roleDAO.save(roles.get(1));
        roleDAO.save(roles.get(2));

        assertEquals("Objects were not saved or not found!", 3, roleDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, roleDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_role", roleDAO.getById(1).getTitle());

        roleDAO.remove(1);
        roleDAO.remove(roles.get(1));
        assertEquals("Objects were not removed or not found!", 1, roleDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        roleDAO.getById(1);
    }

    private List<Role> getRoles() {
        Role firstRole = new Role();
        firstRole.setTitle("first_role");

        Role secondRole = new Role();
        secondRole.setTitle("second_role");

        Role thirdRole = new Role();
        thirdRole.setTitle("third_role");

        List<Role> roles = new ArrayList<>();
        roles.add(firstRole);
        roles.add(secondRole);
        roles.add(thirdRole);
        return roles;
    }
}
