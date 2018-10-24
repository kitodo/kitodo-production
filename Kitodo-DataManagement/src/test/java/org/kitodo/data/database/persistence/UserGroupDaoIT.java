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

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;

public class UserGroupDaoIT {

    @org.junit.Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Role> userGroups = getAuthorities();

        RoleDAO userGroupDAO = new RoleDAO();
        userGroupDAO.save(userGroups.get(0));
        userGroupDAO.save(userGroups.get(1));
        userGroupDAO.save(userGroups.get(2));

        assertEquals("Objects were not saved or not found!", 3, userGroupDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, userGroupDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_userGroup", userGroupDAO.getById(1).getTitle());

        userGroupDAO.remove(1);
        userGroupDAO.remove(userGroups.get(1));
        assertEquals("Objects were not removed or not found!", 1, userGroupDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object can not be found in database");
        userGroupDAO.getById(1);
    }

    private List<Role> getAuthorities() {
        Role firstUserGroup = new Role();
        firstUserGroup.setTitle("first_userGroup");
        firstUserGroup.setIndexAction(IndexAction.DONE);

        Role secondUserGroup = new Role();
        secondUserGroup.setTitle("second_userGroup");
        secondUserGroup.setIndexAction(IndexAction.INDEX);

        Role thirdUserGroup = new Role();
        thirdUserGroup.setTitle("third_userGroup");

        List<Role> userGroups = new ArrayList<>();
        userGroups.add(firstUserGroup);
        userGroups.add(secondUserGroup);
        userGroups.add(thirdUserGroup);
        return userGroups;
    }
}
