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

package org.kitodo.data.index.elasticsearch.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;

import static org.junit.Assert.*;

/**
 * Test class for UserGroupType.
 */
public class UserGroupTypeTest {

    private static List<UserGroup> prepareData() {

        List<User> users = new ArrayList<>();
        List<UserGroup> userGroups = new ArrayList<>();

        User firstUser = new User();
        firstUser.setId(1);
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        users.add(secondUser);

        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setId(1);
        firstUserGroup.setTitle("Administrator");
        firstUserGroup.setPermission(1);
        firstUserGroup.setUsers(users);
        userGroups.add(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setId(2);
        secondUserGroup.setTitle("Random");
        userGroups.add(secondUserGroup);

        return userGroups;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        UserGroupType userGroupType = new UserGroupType();
        JSONParser parser = new JSONParser();

        UserGroup userGroup = prepareData().get(0);
        HttpEntity document = userGroupType.createDocument(userGroup);
        JSONObject userGroupObject = (JSONObject) parser.parse(EntityUtils.toString(document));

        String actual = userGroupObject.get("title").toString();
        String excepted = "Administrator";
        assertEquals("UserGroup value for title key doesn't match to given plain text!", excepted, actual);

        actual = userGroupObject.get("permission").toString();
        excepted = "1";
        assertEquals("UserGroup value for permission key doesn't match to given plain text!", excepted, actual);

        actual = userGroupObject.get("users").toString();
        excepted = "[{\"id\":\"1\"},{\"id\":\"2\"}]";
        assertEquals("UserGroup value for users key doesn't match to given plain text!", excepted, actual);

        userGroup = prepareData().get(1);
        document = userGroupType.createDocument(userGroup);
        userGroupObject = (JSONObject) parser.parse(EntityUtils.toString(document));

        actual = String.valueOf(userGroupObject.get("title"));
        excepted = "Random";
        assertEquals("UserGroup value for title key doesn't match to given plain text!", excepted, actual);

        actual = String.valueOf(userGroupObject.get("permission"));
        excepted = "4";
        assertEquals("UserGroup value for permission key doesn't match to given plain text!", excepted, actual);

        actual = String.valueOf(userGroupObject.get("users"));
        excepted = "[]";
        assertEquals("UserGroup value for users key doesn't match to given plain text!", excepted, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        UserGroupType UserGroupType = new UserGroupType();

        List<UserGroup> batches = prepareData();
        HashMap<Integer, HttpEntity> documents = UserGroupType.createDocuments(batches);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
