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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Template;

/**
 * Test class for PropertyType.
 */
public class PropertyTypeTest {

    private static List<Property> prepareData() {

        List<Property> properties = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<Template> templates = new ArrayList<>();

        Process firstProcess = new Process();
        firstProcess.setId(1);
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(2);
        processes.add(secondProcess);

        User user = new User();
        user.setId(1);
        users.add(user);

        Template template = new Template();
        template.setId(1);
        templates.add(template);

        Property firstProperty = new Property();
        firstProperty.setId(1);
        firstProperty.setTitle("Property1");
        firstProperty.setValue("processes");
        firstProperty.setProcesses(processes);
        properties.add(firstProperty);

        Property secondProperty = new Property();
        secondProperty.setId(2);
        secondProperty.setTitle("Property2");
        secondProperty.setValue("users");
        secondProperty.setUsers(users);
        properties.add(secondProperty);

        Property thirdProperty = new Property();
        thirdProperty.setId(3);
        thirdProperty.setTitle("Property3");
        thirdProperty.setValue("templates");
        thirdProperty.setTemplates(templates);
        properties.add(thirdProperty);

        return properties;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        PropertyType propertyType = new PropertyType();
        JSONParser parser = new JSONParser();

        Property property = prepareData().get(0);
        HttpEntity document = propertyType.createDocument(property);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser.parse(
                "{\"title\":\"Property1\",\"value\":\"processes\",\"workpieces\":[],\"processes\":[{\"id\":1},"
                        + "{\"id\":2}],\"templates\":[],\"users\":[]}");
        assertEquals("Batch JSONObject doesn't match to given JSONObject!", expected, actual);

        property = prepareData().get(1);
        document = propertyType.createDocument(property);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Property2\",\"value\":\"users\",\"workpieces\":[],"
                + "\"processes\":[],\"templates\":[],\"users\":[{\"id\":1}]}");
        assertEquals("Batch JSONObject doesn't match to given JSONObject!", expected, actual);

        property = prepareData().get(2);
        document = propertyType.createDocument(property);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Property3\",\"value\":\"templates\",\"workpieces\":[],"
                + "\"processes\":[],\"templates\":[{\"id\":1}],\"users\":[]}");
        assertEquals("Batch JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        PropertyType propertyType = new PropertyType();

        List<Property> properties = prepareData();
        HashMap<Integer, HttpEntity> documents = propertyType.createDocuments(properties);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
