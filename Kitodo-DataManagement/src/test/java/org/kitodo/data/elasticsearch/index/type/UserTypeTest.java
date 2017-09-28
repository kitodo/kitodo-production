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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
    public void shouldCreateDocument() throws Exception {
        UserType userType = new UserType();
        JSONParser parser = new JSONParser();

        User user = prepareData().get(0);
        HttpEntity document = userType.createDocument(user);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser.parse("{\"ldapLogin\":null,\"userGroups\":[],\"projects\":[],"
                + "\"surname\":\"Kowalski\",\"name\":\"Jan\",\"metadataLanguage\":null,\"login\":\"jkowalski\","
                + "\"active\":\"true\",\"location\":\"Dresden\",\"filters\":[],\"tasks\":[],\"processingTasks\":[]}");
        assertEquals("User JSONObject doesn't match to given JSONObject!", expected, actual);

        user = prepareData().get(1);
        document = userType.createDocument(user);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"ldapLogin\":null,\"userGroups\":[{\"id\":1,\"title\":\"Administrator\"}"
                + ",{\"id\":2,\"title\":\"Basic\"}],\"surname\":\"Nowak\",\"name\":\"Anna\",\"metadataLanguage\":null,"
                + "\"active\":\"true\", \"location\":\"Berlin\",\"login\":\"anowak\",\"filters\":[{\"id\":1,"
                + "\"value\":\"\\\"id:1\\\"\"},{\"id\":2,\"value\":\"\\\"id:2\\\"\"}],\"tasks\":[],\"processingTasks\":[],"
                + "\"projects\":[]}");
        assertEquals("User JSONObject doesn't match to given JSONObject!", expected, actual);

        user = prepareData().get(2);
        document = userType.createDocument(user);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"login\":\"pmueller\",\"ldapLogin\":null,\"userGroups\":[],"
                + "\"surname\":\"Müller\",\"name\":\"Peter\",\"metadataLanguage\":null,\"active\":\"true\","
                + "\"location\":null,\"filters\":[{\"id\":1,\"value\":\"\\\"id:1\\\"\"},{\"id\":2,"
                + "\"value\":\"\\\"id:2\\\"\"}],\"projects\":[],\"tasks\":[],\"processingTasks\":[]}");
        assertEquals("User JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        UserType userType = new UserType();

        List<User> users = prepareData();
        HashMap<Integer, HttpEntity> documents = userType.createDocuments(users);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
