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
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;

public class RoleDaoIT {

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
        List<Role> roles = getRoles();

        RoleDAO roleDAO = new RoleDAO();
        roleDAO.save(roles.get(0));
        roleDAO.save(roles.get(1));
        roleDAO.save(roles.get(2));

        assertEquals(3, roleDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, roleDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_role", roleDAO.getById(1).getTitle(), "Object was not saved or not found!");

        roleDAO.remove(1);
        roleDAO.remove(roles.get(1));
        assertEquals(1, roleDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> roleDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
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
