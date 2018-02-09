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
    public void shouldCreateDocument() throws Exception {
        UserGroupType userGroupType = new UserGroupType();
        JSONParser parser = new JSONParser();

        UserGroup userGroup = prepareData().get(0);
        HttpEntity document = userGroupType.createDocument(userGroup);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser
                .parse("{\"authorities\":[{\"id\":1,\"title\":\"admin\"},{\"id\":2,\"title\":\"manager\"},"
                        + "{\"id\":3,\"title\":\"user\"}],\"title\":\"Administrator\",\"users\":[{\"surname\":"
                        + "\"Tac\",\"name\":\"Tic\",\"id\":1,\"login\":\"first\"},{\"surname\":\"Barney\","
                        + "\"name\":\"Ted\",\"id\":2,\"login\":\"second\"}]}");
        assertEquals("UserGroup JSONObject doesn't match to given JSONObject!", expected, actual);

        userGroup = prepareData().get(1);
        document = userGroupType.createDocument(userGroup);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser
                .parse("{\"authorities\":[{\"id\":1,\"title\":\"admin\"},{\"id\":2,\"title\":\"manager\"},"
                    + "{\"id\":3,\"title\":\"user\"}],\"title\":\"Random\",\"users\":[]}");
        assertEquals("UserGroup JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        UserGroupType UserGroupType = new UserGroupType();

        List<UserGroup> batches = prepareData();
        HashMap<Integer, HttpEntity> documents = UserGroupType.createDocuments(batches);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
