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
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.elasticsearch.index.type.enums.ClientTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientTypeTest {

    private static List<Client> prepareData() {
        List<Client> clients = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<Role> roles = new ArrayList<>();

        Role firstRole = new Role();
        firstRole.setId(1);
        firstRole.setTitle("Administrator");
        roles.add(firstRole);

        Role secondRole = new Role();
        secondRole.setId(2);
        secondRole.setTitle("Basic");
        roles.add(secondRole);

        User firstUser = new User();
        firstUser.setId(1);
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("jkowalski");
        firstUser.setActive(true);
        firstUser.setLocation("Dresden");
        firstUser.getRoles().add(firstRole);
        users.add(firstUser);

        User secondUser = new User();
        secondUser.setId(2);
        secondUser.setName("Anna");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("anowak");
        secondUser.setActive(true);
        secondUser.setLocation("Berlin");
        secondUser.setRoles(roles);
        users.add(secondUser);

        User thirdUser = new User();
        thirdUser.setId(3);
        thirdUser.setName("Peter");
        thirdUser.setSurname("Müller");
        thirdUser.setLogin("pmueller");
        users.add(thirdUser);

        Project project = new Project();
        project.setId(1);
        project.setTitle("Project");
        project.setActive(true);

        Client firstClient = new Client();
        firstClient.setId(1);
        firstClient.setName("First client");
        firstClient.getProjects().add(project);
        firstClient.setRoles(roles);
        firstClient.setUsers(users);

        Client secondClient = new Client();
        secondClient.setId(2);
        secondClient.setName("New client");

        clients.add(firstClient);
        clients.add(secondClient);

        return clients;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(0);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key name doesn't match to given value!", "First client",
            ClientTypeField.NAME.getStringValue(actual));

        JsonArray projects = ClientTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 1, projects.size());

        JsonObject project = projects.getJsonObject(0);
        assertEquals("Key projects.id doesn't match to given value!", 1, project.getInt("id"));
        assertEquals("Key projects.title doesn't match to given value!", "Project", project.getString("title"));
        assertTrue("Key projects.active doesn't match to given value!", project.getBoolean("active"));

        JsonArray users = ClientTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 3, users.size());

        JsonObject user = users.getJsonObject(0);
        assertEquals("Key users.id doesn't match to given value!", 1, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Jan", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Kowalski",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "jkowalski",
            UserTypeField.LOGIN.getStringValue(user));

        user = users.getJsonObject(1);
        assertEquals("Key users.id doesn't match to given value!", 2, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Anna", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Nowak",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "anowak",
            UserTypeField.LOGIN.getStringValue(user));

        user = users.getJsonObject(2);
        assertEquals("Key users.id doesn't match to given value!", 3, UserTypeField.ID.getIntValue(user));
        assertEquals("Key users.name doesn't match to given value!", "Peter", UserTypeField.NAME.getStringValue(user));
        assertEquals("Key users.surname doesn't match to given value!", "Müller",
            UserTypeField.SURNAME.getStringValue(user));
        assertEquals("Key users.login doesn't match to given value!", "pmueller",
            UserTypeField.LOGIN.getStringValue(user));

        JsonArray roles = ClientTypeField.ROLES.getJsonArray(actual);
        assertEquals("Size roles doesn't match to given value!", 2, roles.size());

        JsonObject role = roles.getJsonObject(0);
        assertEquals("Key roles.id doesn't match to given value!", 1, role.getInt("id"));
        assertEquals("Key roles.title doesn't match to given value!", "Administrator",
            role.getString("title"));

        role = roles.getJsonObject(1);
        assertEquals("Key roles.id doesn't match to given value!", 2, role.getInt("id"));
        assertEquals("Key roles.title doesn't match to given value!", "Basic", role.getString("title"));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(1);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key name doesn't match to given value!", "New client",
            ClientTypeField.NAME.getStringValue(actual));

        JsonArray projects = ClientTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        JsonArray users = ClientTypeField.USERS.getJsonArray(actual);
        assertEquals("Size users doesn't match to given value!", 0, users.size());

        JsonArray roles = ClientTypeField.ROLES.getJsonArray(actual);
        assertEquals("Size roles doesn't match to given value!", 0, roles.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ClientType clientType = new ClientType();

        Client client = prepareData().get(0);
        HttpEntity document = clientType.createDocument(client);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 4, actual.keySet().size());

        JsonArray projects = ClientTypeField.PROJECTS.getJsonArray(actual);
        JsonObject project = projects.getJsonObject(0);
        assertEquals("Amount of keys in projects is incorrect!", 3, project.keySet().size());

        JsonArray users = ClientTypeField.USERS.getJsonArray(actual);
        JsonObject user = users.getJsonObject(0);
        assertEquals("Amount of keys in users is incorrect!", 4, user.keySet().size());

        JsonArray roles = ClientTypeField.ROLES.getJsonArray(actual);
        JsonObject role = roles.getJsonObject(0);
        assertEquals("Amount of keys in roles is incorrect!", 2, role.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        ClientType clientType = new ClientType();

        List<Client> clients = prepareData();
        Map<Integer, HttpEntity> documents = clientType.createDocuments(clients);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
