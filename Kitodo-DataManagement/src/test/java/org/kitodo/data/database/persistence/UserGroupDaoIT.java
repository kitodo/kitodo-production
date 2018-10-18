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
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;

public class UserGroupDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<UserGroup> userGroups = getAuthorities();

        UserGroupDAO userGroupDAO = new UserGroupDAO();
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

    private List<UserGroup> getAuthorities() {
        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setTitle("first_userGroup");
        firstUserGroup.setIndexAction(IndexAction.DONE);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setTitle("second_userGroup");
        secondUserGroup.setIndexAction(IndexAction.INDEX);

        UserGroup thirdUserGroup = new UserGroup();
        thirdUserGroup.setTitle("third_userGroup");

        List<UserGroup> userGroups = new ArrayList<>();
        userGroups.add(firstUserGroup);
        userGroups.add(secondUserGroup);
        userGroups.add(thirdUserGroup);
        return userGroups;
    }
}
