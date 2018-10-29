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
import static org.junit.Assert.assertTrue;

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
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.type.enums.FilterTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

/**
 * Test class for UserType.
 */
public class UserTypeTest {

    private static List<User> prepareData() {

        List<User> users = new ArrayList<>();
        List<Role> roles = new ArrayList<>();
        List<Filter> filters = new ArrayList<>();

        Role firstRole = new Role();
        firstRole.setId(1);
        firstRole.setTitle("Administrator");
        roles.add(firstRole);

        Role secondRole = new Role();
        secondRole.setId(2);
        secondRole.setTitle("Basic");
        roles.add(secondRole);

        Filter firstFilter = new Filter();
        firstFilter.setId(1);
        firstFilter.setValue("\"id:1\"");
        filters.add(firstFilter);

        Filter secondFilter = new Filter();
        secondFilter.setId(2);
        secondFilter.setValue("\"id:2\"");
        filters.add(secondFilter);

        User firstUser = new User();
        firstUser.setId(1);
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("jkowalski");
        firstUser.setActive(true);
        firstUser.setLocation("Dresden");
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        secondUser.setName("Anna");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("anowak");
        secondUser.setActive(true);
        secondUser.setLocation("Berlin");
        secondUser.setRoles(roles);
        secondUser.setFilters(filters);
        users.add(secondUser);

        User thirdUser = new User();
        thirdUser.setId(3);
        thirdUser.setName("Peter");
        thirdUser.setSurname("Müller");
        thirdUser.setLogin("pmueller");
        thirdUser.setFilters(filters);
        users.add(thirdUser);

        return users;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(0);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key login doesn't match to given value!", "jkowalski",
            UserTypeField.LOGIN.getStringValue(actual));
        assertEquals("Key ldapLogin doesn't match to given value!", "",
            UserTypeField.LDAP_LOGIN.getStringValue(actual));
        assertEquals("Key name doesn't match to given value!", "Jan", UserTypeField.NAME.getStringValue(actual));
        assertEquals("Key surname doesn't match to given value!", "Kowalski",
            UserTypeField.SURNAME.getStringValue(actual));
        assertEquals("Key location doesn't match to given value!", "Dresden",
            UserTypeField.LOCATION.getStringValue(actual));
        assertEquals("Key metadataLanguage doesn't match to given value!", "",
            UserTypeField.METADATA_LANGUAGE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", UserTypeField.ACTIVE.getBooleanValue(actual));

        JsonArray filters = UserTypeField.FILTERS.getJsonArray(actual);
        assertEquals("Size filters doesn't match to given value!", 0, filters.size());

        JsonArray roles = UserTypeField.ROLES.getJsonArray(actual);
        assertEquals("Size roles doesn't match to given value!", 0, roles.size());

        JsonArray projects = UserTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray processingTasks = UserTypeField.PROCESSING_TASKS.getJsonArray(actual);
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(1);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key login doesn't match to given value!", "anowak", UserTypeField.LOGIN.getStringValue(actual));
        assertEquals("Key ldapLogin doesn't match to given value!", "",
            UserTypeField.LDAP_LOGIN.getStringValue(actual));
        assertEquals("Key name doesn't match to given value!", "Anna", UserTypeField.NAME.getStringValue(actual));
        assertEquals("Key surname doesn't match to given value!", "Nowak",
            UserTypeField.SURNAME.getStringValue(actual));
        assertEquals("Key location doesn't match to given value!", "Berlin",
            UserTypeField.LOCATION.getStringValue(actual));
        assertEquals("Key metadataLanguage doesn't match to given value!", "",
            UserTypeField.METADATA_LANGUAGE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", UserTypeField.ACTIVE.getBooleanValue(actual));

        JsonArray filters = UserTypeField.FILTERS.getJsonArray(actual);
        assertEquals("Size filters doesn't match to given value!", 2, filters.size());

        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Key filters.id doesn't match to given value!", 1, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:1\"", filter.getString("value"));

        filter = filters.getJsonObject(1);
        assertEquals("Key filters.id doesn't match to given value!", 2, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:2\"", filter.getString("value"));

        JsonArray roles = UserTypeField.ROLES.getJsonArray(actual);
        assertEquals("Size roles doesn't match to given value!", 2, roles.size());

        JsonObject role = roles.getJsonObject(0);
        assertEquals("Key roles.id doesn't match to given value!", 1, role.getInt("id"));
        assertEquals("Key roles.title doesn't match to given value!", "Administrator",
                role.getString("title"));

        role = roles.getJsonObject(1);
        assertEquals("Key roles.id doesn't match to given value!", 2, role.getInt("id"));
        assertEquals("Key roles.title doesn't match to given value!", "Basic", role.getString("title"));

        JsonArray projects = UserTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray processingTasks = UserTypeField.PROCESSING_TASKS.getJsonArray(actual);
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(2);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key login doesn't match to given value!", "pmueller", UserTypeField.LOGIN.getStringValue(actual));
        assertEquals("Key ldapLogin doesn't match to given value!", "",
            UserTypeField.LDAP_LOGIN.getStringValue(actual));
        assertEquals("Key name doesn't match to given value!", "Peter", UserTypeField.NAME.getStringValue(actual));
        assertEquals("Key surname doesn't match to given value!", "Müller",
            UserTypeField.SURNAME.getStringValue(actual));
        assertEquals("Key location doesn't match to given value!", "", UserTypeField.LOCATION.getStringValue(actual));
        assertEquals("Key metadataLanguage doesn't match to given value!", "",
            UserTypeField.METADATA_LANGUAGE.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", UserTypeField.ACTIVE.getBooleanValue(actual));

        JsonArray filters = UserTypeField.FILTERS.getJsonArray(actual);
        assertEquals("Size filters doesn't match to given value!", 2, filters.size());

        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Key filters.id doesn't match to given value!", 1, FilterTypeField.ID.getIntValue(filter));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:1\"",
            FilterTypeField.VALUE.getStringValue(filter));

        filter = filters.getJsonObject(1);
        assertEquals("Key filters.id doesn't match to given value!", 2, FilterTypeField.ID.getIntValue(filter));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:2\"",
            FilterTypeField.VALUE.getStringValue(filter));

        JsonArray roles = UserTypeField.ROLES.getJsonArray(actual);
        assertEquals("Size roles doesn't match to given value!", 0, roles.size());

        JsonArray projects = UserTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray processingTasks = UserTypeField.PROCESSING_TASKS.getJsonArray(actual);
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(1);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 12, actual.keySet().size());

        JsonArray filters = UserTypeField.FILTERS.getJsonArray(actual);
        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Amount of keys in filters is incorrect!", 2, filter.keySet().size());

        JsonArray roles = UserTypeField.ROLES.getJsonArray(actual);
        JsonObject role = roles.getJsonObject(0);
        assertEquals("Amount of keys in filters is incorrect!", 2, role.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        UserType userType = new UserType();

        List<User> users = prepareData();
        Map<Integer, HttpEntity> documents = userType.createDocuments(users);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
