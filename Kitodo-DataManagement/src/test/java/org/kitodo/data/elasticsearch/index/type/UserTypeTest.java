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
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;

/**
 * Test class for UserType.
 */
public class UserTypeTest {

    private static List<User> prepareData() {

        List<User> users = new ArrayList<>();
        List<UserGroup> userGroups = new ArrayList<>();
        List<Filter> filters = new ArrayList<>();

        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setId(1);
        firstUserGroup.setTitle("Administrator");
        userGroups.add(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setId(2);
        secondUserGroup.setTitle("Basic");
        userGroups.add(secondUserGroup);

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
        secondUser.setUserGroups(userGroups);
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

        assertEquals("Key login doesn't match to given value!", "jkowalski", actual.getString("login"));
        assertEquals("Key ldapLogin doesn't match to given value!", "", actual.getString("ldapLogin"));
        assertEquals("Key name doesn't match to given value!", "Jan", actual.getString("name"));
        assertEquals("Key surname doesn't match to given value!", "Kowalski", actual.getString("surname"));
        assertEquals("Key location doesn't match to given value!", "Dresden", actual.getString("location"));
        assertEquals("Key metadataLanguage doesn't match to given value!", "", actual.getString("metadataLanguage"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));

        JsonArray filters = actual.getJsonArray("filters");
        assertEquals("Size filters doesn't match to given value!", 0, filters.size());

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size userGroups doesn't match to given value!", 0, userGroups.size());

        JsonArray projects = actual.getJsonArray("projects");
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray tasks = actual.getJsonArray("tasks");
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());

        JsonArray processingTasks = actual.getJsonArray("processingTasks");
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(1);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key login doesn't match to given value!", "anowak", actual.getString("login"));
        assertEquals("Key ldapLogin doesn't match to given value!", "", actual.getString("ldapLogin"));
        assertEquals("Key name doesn't match to given value!", "Anna", actual.getString("name"));
        assertEquals("Key surname doesn't match to given value!", "Nowak", actual.getString("surname"));
        assertEquals("Key location doesn't match to given value!", "Berlin", actual.getString("location"));
        assertEquals("Key metadataLanguage doesn't match to given value!", "", actual.getString("metadataLanguage"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));

        JsonArray filters = actual.getJsonArray("filters");
        assertEquals("Size filters doesn't match to given value!", 2, filters.size());

        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Key filters.id doesn't match to given value!", 1, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:1\"", filter.getString("value"));

        filter = filters.getJsonObject(1);
        assertEquals("Key filters.id doesn't match to given value!", 2, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:2\"", filter.getString("value"));

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size userGroups doesn't match to given value!", 2, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key userGroups.id doesn't match to given value!", 1, userGroup.getInt("id"));
        assertEquals("Key userGroups.title doesn't match to given value!", "Administrator", userGroup.getString("title"));

        userGroup = userGroups.getJsonObject(1);
        assertEquals("Key userGroups.id doesn't match to given value!", 2, userGroup.getInt("id"));
        assertEquals("Key userGroups.title doesn't match to given value!", "Basic", userGroup.getString("title"));

        JsonArray projects = actual.getJsonArray("projects");
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray tasks = actual.getJsonArray("tasks");
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());

        JsonArray processingTasks = actual.getJsonArray("processingTasks");
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(2);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key login doesn't match to given value!", "pmueller", actual.getString("login"));
        assertEquals("Key ldapLogin doesn't match to given value!", "", actual.getString("ldapLogin"));
        assertEquals("Key name doesn't match to given value!", "Peter", actual.getString("name"));
        assertEquals("Key surname doesn't match to given value!", "Müller", actual.getString("surname"));
        assertEquals("Key location doesn't match to given value!", "", actual.getString("location"));
        assertEquals("Key metadataLanguage doesn't match to given value!", "", actual.getString("metadataLanguage"));
        assertEquals("Key active doesn't match to given value!", true, actual.getBoolean("active"));

        JsonArray filters = actual.getJsonArray("filters");
        assertEquals("Size filters doesn't match to given value!", 2, filters.size());

        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Key filters.id doesn't match to given value!", 1, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:1\"", filter.getString("value"));

        filter = filters.getJsonObject(1);
        assertEquals("Key filters.id doesn't match to given value!", 2, filter.getInt("id"));
        assertEquals("Key filters.value doesn't match to given value!", "\"id:2\"", filter.getString("value"));

        JsonArray userGroups = actual.getJsonArray("userGroups");
        assertEquals("Size userGroups doesn't match to given value!", 0, userGroups.size());

        JsonArray projects = actual.getJsonArray("projects");
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray tasks = actual.getJsonArray("tasks");
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());

        JsonArray processingTasks = actual.getJsonArray("processingTasks");
        assertEquals("Size processingTasks doesn't match to given value!", 0, processingTasks.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        UserType userType = new UserType();

        User user = prepareData().get(1);
        HttpEntity document = userType.createDocument(user);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 12, actual.keySet().size());

        JsonArray filters = actual.getJsonArray("filters");
        JsonObject filter = filters.getJsonObject(0);
        assertEquals("Amount of keys in filters is incorrect!", 2, filter.keySet().size());

        JsonArray userGroups = actual.getJsonArray("userGroups");
        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Amount of keys in filters is incorrect!", 2, userGroup.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        UserType userType = new UserType();

        List<User> users = prepareData();
        HashMap<Integer, HttpEntity> documents = userType.createDocuments(users);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
