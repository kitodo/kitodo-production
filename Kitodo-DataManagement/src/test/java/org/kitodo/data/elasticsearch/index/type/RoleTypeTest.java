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
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Test class for RoleType.
 */
public class RoleTypeTest {

    private static List<Role> prepareData() {
        List<User> users = new ArrayList<>();

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

        Client client = new Client();
        client.setId(1);
        client.setName("Client");

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

        Role firstRole = new Role();
        firstRole.setId(1);
        firstRole.setTitle("Administrator");
        firstRole.setClient(client);
        firstRole.setAuthorities(adminAuthorities);
        firstRole.setUsers(users);

        Role secondRole = new Role();
        secondRole.setId(2);
        secondRole.setTitle("Random");
        secondRole.setAuthorities(adminAuthorities);
        secondRole.setClient(client);

        List<Role> roles = new ArrayList<>();

        roles.add(firstRole);
        roles.add(secondRole);

        return roles;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        RoleType roleType = new RoleType();

        Role role = prepareData().get(0);
        HttpEntity document = roleType.createDocument(role);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Administrator",
            RoleTypeField.TITLE.getStringValue(actual));
        assertEquals("Key client.id doesn't match to given value!", 1,
                RoleTypeField.CLIENT_ID.getIntValue(actual));
        assertEquals("Key client.name doesn't match to given value!", "Client",
                RoleTypeField.CLIENT_NAME.getStringValue(actual));

        JsonArray authorities = RoleTypeField.AUTHORITIES.getJsonArray(actual);
        assertEquals("Size authorities doesn't match to given value!", 3, authorities.size());

        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Key authorities.id doesn't match to given value!", 1,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "admin",
            AuthorityTypeField.TITLE.getStringValue(authority));

        authority = authorities.getJsonObject(1);
        assertEquals("Key authorities.id doesn't match to given value!", 2,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "manager",
            AuthorityTypeField.TITLE.getStringValue(authority));

        authority = authorities.getJsonObject(2);
        assertEquals("Key authorities.id doesn't match to given value!", 3,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "user",
            AuthorityTypeField.TITLE.getStringValue(authority));

        JsonArray users = RoleTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 2, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Tic", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Tac",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "first",
            UserTypeField.LOGIN.getStringValue(user));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Ted", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Barney",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "second",
            UserTypeField.LOGIN.getStringValue(user));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        RoleType roleType = new RoleType();

        Role role = prepareData().get(1);
        HttpEntity document = roleType.createDocument(role);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Random",
            RoleTypeField.TITLE.getStringValue(actual));
        assertEquals("Key client.id doesn't match to given value!", 1,
                RoleTypeField.CLIENT_ID.getIntValue(actual));
        assertEquals("Key client.name doesn't match to given value!", "Client",
                RoleTypeField.CLIENT_NAME.getStringValue(actual));

        JsonArray users = RoleTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 0, users.size());

        JsonArray authorities = RoleTypeField.AUTHORITIES.getJsonArray(actual);
        assertEquals("Size authorities doesn't match to given value!", 3, authorities.size());

        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Key authorities.id doesn't match to given value!", 1,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "admin",
            AuthorityTypeField.TITLE.getStringValue(authority));

        authority = authorities.getJsonObject(1);
        assertEquals("Key authorities.id doesn't match to given value!", 2,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "manager",
            AuthorityTypeField.TITLE.getStringValue(authority));

        authority = authorities.getJsonObject(2);
        assertEquals("Key authorities.id doesn't match to given value!", 3,
            AuthorityTypeField.ID.getIntValue(authority));
        assertEquals("Key authorities.title doesn't match to given value!", "user",
            AuthorityTypeField.TITLE.getStringValue(authority));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        RoleType roleType = new RoleType();

        Role role = prepareData().get(0);
        HttpEntity document = roleType.createDocument(role);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 5, actual.keySet().size());

        JsonArray authorities = RoleTypeField.AUTHORITIES.getJsonArray(actual);
        JsonObject authority = authorities.getJsonObject(0);
        assertEquals("Amount of keys in authorities is incorrect!", 2, authority.keySet().size());

        JsonArray users = RoleTypeField.USERS.getJsonArray(actual);
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 4, user.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        RoleType roleType = new RoleType();

        List<Role> roles = prepareData();
        Map<Integer, HttpEntity> documents = roleType.createDocuments(roles);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
