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

package org.kitodo.data.elasticsearch.index.type;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;

/**
 * Test class for UserGroupType.
 */
public class UserGroupTypeTest {

    private static List<UserGroup> prepareData() {

        List<User> users = new ArrayList<>();
        List<UserGroup> userGroups = new ArrayList<>();

        User firstUser = new User();
        firstUser.setId(1);
        firstUser.setLogin("first");
        firstUser.setName("Tic");
        firstUser.setSurname("Tac");
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        secondUser.setLogin("second");
        secondUser.setName("Ted");
        secondUser.setSurname("Barney");
        users.add(secondUser);

        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setId(1);
        firstUserGroup.setTitle("Administrator");

        List<Authority> adminAuthorities = new ArrayList<>();
        Authority adminAuthority = new Authority();
        adminAuthority.setTitle("admin");
        adminAuthority.setId(1);

        Authority managerAuthority = new Authority();
        managerAuthority.setTitle("manager");
        managerAuthority.setId(2);

        Authority userAuthority = new Authority();
        userAuthority.setTitle("user");
        userAuthority.setId(3);

        adminAuthorities.add(adminAuthority);
        adminAuthorities.add(managerAuthority);
        adminAuthorities.add(userAuthority);

        firstUserGroup.setGlobalAuthorities(adminAuthorities);

        firstUserGroup.setUsers(users);
        userGroups.add(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setId(2);
        secondUserGroup.setTitle("Random");
        secondUserGroup.setGlobalAuthorities(adminAuthorities);
        userGroups.add(secondUserGroup);

        return userGroups;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        UserGroupType userGroupType = new UserGroupType();

        UserGroup userGroup = prepareData().get(0);
        HttpEntity document = userGroupType.createDocument(userGroup);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Administrator", actual.getString("title"));

        JsonArray authorities = actual.getJsonArray("authorities");
        assertEquals("Size authorities doesn't match to given value!", 3, authorities.size());

        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Key authorities.id doesn't match to given value!", 1, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "admin", authority.getString("title"));

        authority = authorities.getJsonObject(1);
        assertEquals("Key authorities.id doesn't match to given value!", 2, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "manager", authority.getString("title"));

        authority = authorities.getJsonObject(2);
        assertEquals("Key authorities.id doesn't match to given value!", 3, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "user", authority.getString("title"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Tic", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Tac", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "first", user.getString("login"));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, user.getInt("id"));
        assertEquals("Key users.name doesn't match to given value!", "Ted", user.getString("name"));
        assertEquals("Key users.surname doesn't match to given value!", "Barney", user.getString("surname"));
        assertEquals("Key users.login doesn't match to given value!", "second", user.getString("login"));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        UserGroupType userGroupType = new UserGroupType();

        UserGroup userGroup = prepareData().get(1);
        HttpEntity document = userGroupType.createDocument(userGroup);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Random", actual.getString("title"));

        JsonArray users = actual.getJsonArray("users");
        assertEquals("Size users doesn't match to given value!", 0, users.size());

        JsonArray authorities = actual.getJsonArray("authorities");
        assertEquals("Size authorities doesn't match to given value!", 3, authorities.size());

        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Key authorities.id doesn't match to given value!", 1, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "admin", authority.getString("title"));

        authority = authorities.getJsonObject(1);
        assertEquals("Key authorities.id doesn't match to given value!", 2, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "manager", authority.getString("title"));

        authority = authorities.getJsonObject(2);
        assertEquals("Key authorities.id doesn't match to given value!", 3, authority.getInt("id"));
        assertEquals("Key authorities.title doesn't match to given value!", "user", authority.getString("title"));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        UserGroupType userGroupType = new UserGroupType();

        UserGroup userGroup = prepareData().get(0);
        HttpEntity document = userGroupType.createDocument(userGroup);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 3, actual.keySet().size());

        JsonArray authorities = actual.getJsonArray("authorities");
        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Amount of keys in authorities is incorrect!", 2, authority.keySet().size());

        JsonArray users = actual.getJsonArray("users");
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 4, user.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        UserGroupType UserGroupType = new UserGroupType();

        List<UserGroup> batches = prepareData();
        HashMap<Integer, HttpEntity> documents = UserGroupType.createDocuments(batches);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
