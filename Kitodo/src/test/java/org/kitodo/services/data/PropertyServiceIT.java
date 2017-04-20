package org.kitodo.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, ResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
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
        assertEquals("Process property was not found in database - value doesn'T match!", expected, actual);
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
    public void shouldFindUserProperty() throws Exception {
        PropertyService propertyService = new PropertyService();

        Property userProperty = propertyService.find(7);
        String actual = userProperty.getTitle();
        String expected = "FirstUserProperty";
        assertEquals("User property was not found in database - title doesn't match!", expected, actual);

        actual = userProperty.getValue();
        expected = "first value";
        assertEquals("User property was not found in database - value doesn'T match!", expected, actual);
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
    public void shouldFindAllProperties() throws Exception {
        PropertyService propertyService = new PropertyService();

        List<Property> properties = propertyService.findAll();
        assertEquals("Not all properties were found in database!", 8, properties.size());
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
