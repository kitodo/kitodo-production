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
import java.util.HashMap;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.UserGroup;

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
    public void shouldCreateDocument() throws Exception {
        AuthorityType authorityType = new AuthorityType();

        Authority authority = prepareData().get(0);
        HttpEntity document = authorityType.createDocument(authority);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        JsonObject expected = Json.createReader(new StringReader("{\"title\":\"First\","
                + "\"userGroups\":[{\"id\":1,\"title\":\"First\"}]}")).readObject();
        assertEquals("Authority JSONObject doesn't match to given JSONObject!", expected, actual);

        authority = prepareData().get(1);
        document = authorityType.createDocument(authority);

        actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        expected = Json.createReader(new StringReader("{\"title\":\"Second\",\"userGroups\":[]}")).readObject();
        assertEquals("Authority JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        AuthorityType authorityType = new AuthorityType();

        List<Authority> authorities = prepareData();
        HashMap<Integer, HttpEntity> documents = authorityType.createDocuments(authorities);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
