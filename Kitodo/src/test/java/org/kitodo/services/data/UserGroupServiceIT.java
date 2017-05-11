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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for UserGroupService class.
 */
public class UserGroupServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, CustomResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabaseAndIndex() throws IOException, CustomResponseException {
        // MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = userGroupService.find(1);
        boolean condition = userGroup.getTitle().equals("Admin") && userGroup.getPermission().equals(1);
        assertTrue("User group was not found in database!", condition);
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldRemoveUserGroup() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("To Remove");
        userGroupService.save(userGroup);
        UserGroup foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(4));
        assertEquals("Additional user group was not inserted in database!", "To Remove", foundUserGroup.getTitle());

        userGroupService.remove(foundUserGroup);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(4));
        assertEquals("Additional user group was not removed from database!", null, foundUserGroup);

        userGroup = new UserGroup();
        userGroup.setTitle("To remove");
        userGroupService.save(userGroup);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(5));
        assertEquals("Additional user group was not inserted in database!", "To remove", foundUserGroup.getTitle());

        userGroupService.remove(5);
        foundUserGroup = userGroupService.convertSearchResultToObject(userGroupService.findById(5));
        assertEquals("Additional user group was not removed from database!", null, foundUserGroup);
    }

    @Test
    public void shouldFindById() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        SearchResult userGroup = userGroupService.findById(1);
        String actual = userGroup.getProperties().get("title");
        String expected = "Admin";
        assertEquals("User group was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<SearchResult> userGroups = userGroupService.findByTitle("Admin", true);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByTitle("none", true);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User group was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByPermission() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<SearchResult> userGroups = userGroupService.findByPermission(1);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByPermission(4);
        actual = userGroups.size();
        expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByPermission(5);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User group was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserId() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<SearchResult> userGroups = userGroupService.findByUserId(1);
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByUserId(2);
        actual = userGroups.size();
        expected = 0;
        assertEquals("User groups were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserLogin() throws Exception {
        UserGroupService userGroupService = new UserGroupService();

        List<SearchResult> userGroups = userGroupService.findByUserLogin("kowal");
        Integer actual = userGroups.size();
        Integer expected = 1;
        assertEquals("User group was not found in index!", expected, actual);

        userGroups = userGroupService.findByUserLogin("dora");
        actual = userGroups.size();
        expected = 0;
        assertEquals("User groups were found in index!", expected, actual);
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
