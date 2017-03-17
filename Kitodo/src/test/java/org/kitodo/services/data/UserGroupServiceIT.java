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

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for UserGroupService class.
 */
public class UserGroupServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        boolean condition = userGroup.getTitle().equals("Admin") && userGroup.getPermission().equals(1);
        assertTrue("User group was not found in database!", condition);
    }

    @Test
    public void shouldGetPermissionAsString() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        String actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "1", actual);

        userGroup = userGroupService.find(3);
        actual = userGroupService.getPermissionAsString(userGroup);
        assertEquals("Permission string doesn't match to given plain text!", "4", actual);
    }

    @Test
    public void shouldGetTasksSize() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        int actual = userGroupService.getTasksSize(userGroup);
        assertEquals("Tasks size is not equal to given value!", 2, actual);
    }
}
