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
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.User;

/**
 * Test class for FilterType.
 */
public class FilterTypeTest {

    private static List<Filter> prepareData() {
        List<Filter> filters = new ArrayList<>();

        User firstUser = new User();
        firstUser.setId(1);

        Filter firstFilter = new Filter();
        firstFilter.setId(1);
        firstFilter.setValue("\"id:1\"");
        firstFilter.setUser(firstUser);

        Filter secondFilter = new Filter();
        firstFilter.setId(2);
        secondFilter.setValue("\"id:2\"");
        secondFilter.setUser(firstUser);

        filters.add(firstFilter);
        filters.add(secondFilter);

        return filters;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        FilterType propertyType = new FilterType();

        Filter property = prepareData().get(0);
        HttpEntity document = propertyType.createDocument(property);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        JsonObject expected = Json.createReader(new StringReader("{\"value\":\"\\\"id:1\\\"\",\"user\":1}")).readObject();
        assertEquals("Filter JSONObject doesn't match to given JSONObject!", expected, actual);

        property = prepareData().get(1);
        document = propertyType.createDocument(property);

        actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        expected = Json.createReader(new StringReader("{\"value\":\"\\\"id:2\\\"\",\"user\":1}")).readObject();
        assertEquals("Filter JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        FilterType filterType = new FilterType();

        List<Filter> filters = prepareData();
        HashMap<Integer, HttpEntity> documents = filterType.createDocuments(filters);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
