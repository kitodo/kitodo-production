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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.PropertyTypeField;

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
    public void shouldCreateFirstDocument() throws Exception {
        PropertyType propertyType = new PropertyType();

        Property property = prepareData().get(0);
        HttpEntity document = propertyType.createDocument(property);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Property1",
            PropertyTypeField.TITLE.getStringValue(actual));
        assertEquals("Key value doesn't match to given value!", "processes",
            PropertyTypeField.VALUE.getStringValue(actual));
        assertEquals("Key type doesn't match to given value!", "process",
            PropertyTypeField.TYPE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(property.getCreationDate()),
            PropertyTypeField.CREATION_DATE.getStringValue(actual));

        JsonArray processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 2, processes.size());

        JsonObject process = processes.getJsonObject(0);
        assertEquals("Key processes.id doesn't match to given value!", 2, ProcessTypeField.ID.getIntValue(process));

        process = processes.getJsonObject(1);
        assertEquals("Key processes.id doesn't match to given value!", 3, ProcessTypeField.ID.getIntValue(process));

        JsonArray workpieces = PropertyTypeField.WORKPIECES.getJsonArray(actual);
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        JsonArray templates = PropertyTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 0, templates.size());
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        PropertyType propertyType = new PropertyType();

        Property property = prepareData().get(1);
        HttpEntity document = propertyType.createDocument(property);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Property2",
            PropertyTypeField.TITLE.getStringValue(actual));
        assertEquals("Key value doesn't match to given value!", "templates",
            PropertyTypeField.VALUE.getStringValue(actual));
        assertEquals("Key type doesn't match to given value!", "template",
            PropertyTypeField.TYPE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(property.getCreationDate()),
            PropertyTypeField.CREATION_DATE.getStringValue(actual));

        JsonArray processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 0, processes.size());

        JsonArray workpieces = PropertyTypeField.WORKPIECES.getJsonArray(actual);
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        JsonArray templates = PropertyTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 1, templates.size());

        JsonObject template = templates.getJsonObject(0);
        assertEquals("Key templates.id doesn't match to given value!", 1,
            PropertyTypeField.ID.getIntValue(template));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        PropertyType propertyType = new PropertyType();

        Property property = prepareData().get(0);
        HttpEntity document = propertyType.createDocument(property);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 7, actual.keySet().size());

        JsonArray processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        JsonObject process = processes.getJsonObject(0);
        assertEquals("Amount of keys in processes is incorrect!", 1, process.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        PropertyType propertyType = new PropertyType();

        List<Property> properties = prepareData();
        Map<Integer, HttpEntity> documents = propertyType.createDocuments(properties);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}
