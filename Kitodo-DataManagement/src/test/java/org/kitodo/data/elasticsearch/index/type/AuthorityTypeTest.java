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
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.elasticsearch.index.type.enums.AuthorityTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserGroupTypeField;

import static org.junit.Assert.assertEquals;

public class AuthorityTypeTest {

    private static List<Authority> prepareData() {

        List<Authority> authorities = new ArrayList<>();
        List<UserGroup> userGroups = new ArrayList<>();

        UserGroup userGroup = new UserGroup();
        userGroup.setId(1);
        userGroup.setTitle("First");
        userGroups.add(userGroup);

        Authority firstAuthority = new Authority();
        firstAuthority.setId(1);
        firstAuthority.setTitle("First");
        firstAuthority.setUserGroups(userGroups);
        authorities.add(firstAuthority);

        Authority secondAuthority = new Authority();
        secondAuthority.setId(2);
        secondAuthority.setTitle("Second");
        authorities.add(secondAuthority);

        return authorities;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        AuthorityType authorityType = new AuthorityType();

        Authority authority = prepareData().get(0);
        HttpEntity document = authorityType.createDocument(authority);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "First",
            AuthorityTypeField.TITLE.getStringValue(actual));

        JsonArray userGroups = actual.getJsonArray(AuthorityTypeField.USER_GROUPS.getName());
        assertEquals("Size userGroups doesn't match to given value!", 1, userGroups.size());

        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Key userGroups.id doesn't match to given value!", 1,
            UserGroupTypeField.ID.getIntValue(userGroup));
        assertEquals("Key userGroups.title doesn't match to given value!", "First",
            UserGroupTypeField.TITLE.getStringValue(userGroup));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        AuthorityType authorityType = new AuthorityType();

        Authority authority = prepareData().get(1);
        HttpEntity document = authorityType.createDocument(authority);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Second",
            AuthorityTypeField.TITLE.getStringValue(actual));

        JsonArray userGroups = AuthorityTypeField.USER_GROUPS.getJsonArray(actual);
        assertEquals("Size userGroups doesn't match to given value!", 0, userGroups.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        AuthorityType authorityType = new AuthorityType();

        Authority authority = prepareData().get(0);
        HttpEntity document = authorityType.createDocument(authority);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 2, actual.keySet().size());

        JsonArray userGroups = AuthorityTypeField.USER_GROUPS.getJsonArray(actual);
        JsonObject userGroup = userGroups.getJsonObject(0);
        assertEquals("Amount of keys in userGroups is incorrect!", 2, userGroup.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        AuthorityType authorityType = new AuthorityType();

        List<Authority> authorities = prepareData();
        Map<Integer, HttpEntity> documents = authorityType.createDocuments(authorities);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
