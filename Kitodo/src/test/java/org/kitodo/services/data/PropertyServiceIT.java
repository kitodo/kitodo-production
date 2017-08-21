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

package org.kitodo.services.data;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldCountAllProperties() throws Exception {
        PropertyService propertyService = new PropertyService();

        Long amount = propertyService.count();
        assertEquals("Properties were not counted correctly!", Long.valueOf(6), amount);
    }

    @Test
    public void shouldCountAllPropertiesAccordingToQuery() throws Exception {
        PropertyService propertyService = new PropertyService();

        String query = matchQuery("type", "process").operator(Operator.AND).toString();
        Long amount = propertyService.count(query);
        assertEquals("Properties were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForProperties() throws Exception {
        PropertyService propertyService = new PropertyService();

        Long amount = propertyService.countDatabaseRows();
        assertEquals("Properties were not counted correctly!", Long.valueOf(6), amount);
    }

    @Test
    public void shouldFindProcessProperty() throws Exception {
        PropertyService processPropertyService = new PropertyService();

        Property processProperty = processPropertyService.find(1);
        String actual = processProperty.getTitle();
        String expected = "Process Property";
        assertEquals("Process property was not found in database - title doesn't match!", expected, actual);

        actual = processProperty.getValue();
        expected = "first value";
        assertEquals("Process property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldFindTemplateProperty() throws Exception {
        PropertyService propertyService = new PropertyService();

        Property templateProperty = propertyService.find(5);
        String actual = templateProperty.getTitle();
        String expected = "firstTemplate title";
        assertEquals("Template property was not found in database - title doesn't match!", expected, actual);

        actual = templateProperty.getValue();
        expected = "first value";
        assertEquals("Template property was not found in database - value doesn'T match!", expected, actual);
    }

    @Test
    public void shouldFindWorkpieceProperty() throws Exception {
        PropertyService propertyService = new PropertyService();

        Property workpieceProperty = propertyService.find(3);
        String actual = workpieceProperty.getTitle();
        String expected = "FirstWorkpiece Property";
        assertEquals("Workpiece property was not found in database - title doesn't match!", expected, actual);

        actual = workpieceProperty.getValue();
        expected = "first value";
        assertEquals("Workpiece property was not found in database - value doesn'T match!", expected, actual);
    }

    @Test
    public void shouldFindAllProperties() {
        PropertyService propertyService = new PropertyService();

        List<Property> properties = propertyService.findAll();
        assertEquals("Not all properties were found in database!", 6, properties.size());
    }

    @Test
    public void shouldRemoveProperty() throws Exception {
        PropertyService propertyService = new PropertyService();

        Property property = new Property();
        property.setTitle("To Remove");
        propertyService.save(property);
        Property foundProperty = propertyService.convertJSONObjectToObject(propertyService.findById(7));
        assertEquals("Additional property was not inserted in database!", "To Remove", foundProperty.getTitle());

        propertyService.remove(foundProperty);
        foundProperty = propertyService.convertJSONObjectToObject(propertyService.findById(7));
        assertEquals("Additional property was not removed from database!", null, foundProperty);

        property = new Property();
        property.setTitle("To remove");
        propertyService.save(property);
        foundProperty = propertyService.convertJSONObjectToObject(propertyService.findById(8));
        assertEquals("Additional property was not inserted in database!", "To remove", foundProperty.getTitle());

        propertyService.remove(8);
        foundProperty = propertyService.convertJSONObjectToObject(propertyService.findById(8));
        assertEquals("Additional property was not removed from database!", null, foundProperty);
    }

    @Test
    public void shouldFindById() throws Exception {
        PropertyService propertyService = new PropertyService();

        JSONObject property = propertyService.findById(1);
        Integer actual = propertyService.getIdFromJSONObject(property);
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByValue() throws Exception {
        PropertyService propertyService = new PropertyService();

        List<JSONObject> properties = propertyService.findByValue("second", true);
        Integer actual = properties.size();
        Integer expected = 3;
        assertEquals("Properties were not found in index!", expected, actual);

        properties = propertyService.findByValue("second value", true);
        actual = properties.size();
        expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndValue() throws Exception {
        PropertyService propertyService = new PropertyService();

        List<JSONObject> properties = propertyService.findByTitleAndValue("secondProcessProperty", "second");
        Integer actual = properties.size();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);

        properties = propertyService.findByTitleAndValue("secondProcessProperty", "third");
        actual = properties.size();
        expected = 0;
        assertEquals("Property was found in index!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        PropertyService processPropertyService = new PropertyService();

        Property processProperty = processPropertyService.find(1);
        String expected = "Process_Property";
        String actual = processPropertyService.getNormalizedTitle(processProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        PropertyService processPropertyService = new PropertyService();

        Property processProperty = processPropertyService.find(1);
        String expected = "first_value";
        String actual = processPropertyService.getNormalizedValue(processProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
