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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;

/**
 * Test class for PropertyType.
 */
public class PropertyTypeTest {

    private static List<Property> prepareData() {

        List<Property> properties = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        List<Process> templates = new ArrayList<>();

        Process template = new Process();
        template.setId(1);
        templates.add(template);

        Process firstProcess = new Process();
        firstProcess.setId(2);
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(3);
        processes.add(secondProcess);

        Property firstProperty = new Property();
        firstProperty.setId(1);
        firstProperty.setTitle("Property1");
        firstProperty.setValue("processes");
        firstProperty.setProcesses(processes);
        properties.add(firstProperty);

        Property secondProperty = new Property();
        secondProperty.setId(2);
        secondProperty.setTitle("Property2");
        secondProperty.setValue("templates");
        secondProperty.setTemplates(templates);
        properties.add(secondProperty);

        return properties;
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        PropertyType propertyType = new PropertyType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Property property = prepareData().get(0);
        HttpEntity document = propertyType.createDocument(property);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        JsonObject expected = Json.createReader(new StringReader("{\"title\":\"Property1\",\"value\":\"processes\",\"workpieces\":[],\"processes\":[{\"id\":2},"
                + "{\"id\":3}],\"type\":\"process\",\"templates\":[],\"creationDate\":\""
                + dateFormat.format(property.getCreationDate()) + "\"}")).readObject();
        assertEquals("Property JSONObject doesn't match to given JSONObject!", expected, actual);

        property = prepareData().get(1);
        document = propertyType.createDocument(property);

        actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        expected = Json.createReader(new StringReader("{\"title\":\"Property2\",\"value\":\"templates\",\"workpieces\":[],"
                + "\"processes\":[],\"type\":\"template\",\"templates\":[{\"id\":1}],\"creationDate\":\""
                + dateFormat.format(property.getCreationDate()) + "\"}")).readObject();
        assertEquals("Property JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        PropertyType propertyType = new PropertyType();

        List<Property> properties = prepareData();
        HashMap<Integer, HttpEntity> documents = propertyType.createDocuments(properties);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
