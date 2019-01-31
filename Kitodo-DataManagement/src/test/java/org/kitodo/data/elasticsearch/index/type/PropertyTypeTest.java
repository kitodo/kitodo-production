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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> actual = propertyType.createDocument(property);

        assertEquals("Key title doesn't match to given value!", "Property1",
            PropertyTypeField.TITLE.getStringValue(actual));
        assertEquals("Key value doesn't match to given value!", "processes",
            PropertyTypeField.VALUE.getStringValue(actual));
        assertEquals("Key type doesn't match to given value!", "process",
            PropertyTypeField.TYPE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(property.getCreationDate()),
            PropertyTypeField.CREATION_DATE.getStringValue(actual));

        List<Map<String, Object>> processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 2, processes.size());

        Map<String, Object> process = processes.get(0);
        assertEquals("Key processes.id doesn't match to given value!", 2, ProcessTypeField.ID.getIntValue(process));

        process = processes.get(1);
        assertEquals("Key processes.id doesn't match to given value!", 3, ProcessTypeField.ID.getIntValue(process));

        List<Map<String, Object>> workpieces = PropertyTypeField.WORKPIECES.getJsonArray(actual);
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        List<Map<String, Object>> templates = PropertyTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 0, templates.size());
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        PropertyType propertyType = new PropertyType();

        Property property = prepareData().get(1);
        Map<String, Object> actual = propertyType.createDocument(property);

        assertEquals("Key title doesn't match to given value!", "Property2",
            PropertyTypeField.TITLE.getStringValue(actual));
        assertEquals("Key value doesn't match to given value!", "templates",
            PropertyTypeField.VALUE.getStringValue(actual));
        assertEquals("Key type doesn't match to given value!", "template",
            PropertyTypeField.TYPE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(property.getCreationDate()),
            PropertyTypeField.CREATION_DATE.getStringValue(actual));

        List<Map<String, Object>> processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        assertEquals("Size processes doesn't match to given value!", 0, processes.size());

        List<Map<String, Object>> workpieces = PropertyTypeField.WORKPIECES.getJsonArray(actual);
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        List<Map<String, Object>> templates = PropertyTypeField.TEMPLATES.getJsonArray(actual);
        assertEquals("Size templates doesn't match to given value!", 1, templates.size());

        Map<String, Object> template = templates.get(0);
        assertEquals("Key templates.id doesn't match to given value!", 1,
            PropertyTypeField.ID.getIntValue(template));
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        PropertyType propertyType = new PropertyType();

        Property property = prepareData().get(0);
        Map<String, Object> actual = propertyType.createDocument(property);

        assertEquals("Amount of keys is incorrect!", 7, actual.keySet().size());

        List<Map<String, Object>> processes = PropertyTypeField.PROCESSES.getJsonArray(actual);
        Map<String, Object> process = processes.get(0);
        assertEquals("Amount of keys in processes is incorrect!", 1, process.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        PropertyType propertyType = new PropertyType();

        List<Property> properties = prepareData();
        Map<Integer, Map<String, Object>> documents = propertyType.createDocuments(properties);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}
