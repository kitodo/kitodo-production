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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws CustomResponseException, DAOException, IOException {
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
    public void shouldRemoveProperty() throws Exception {
        PropertyService propertyService = new PropertyService();

        Property property = new Property();
        property.setTitle("To Remove");
        propertyService.save(property);
        Property foundProperty = propertyService.convertSearchResultToObject(propertyService.findById(9));
        assertEquals("Additional property was not inserted in database!", "To Remove", foundProperty.getTitle());

        propertyService.remove(foundProperty);
        foundProperty = propertyService.convertSearchResultToObject(propertyService.findById(9));
        assertEquals("Additional property was not removed from database!", null, foundProperty);

        property = new Property();
        property.setTitle("To remove");
        propertyService.save(property);
        foundProperty = propertyService.convertSearchResultToObject(propertyService.findById(10));
        assertEquals("Additional property was not inserted in database!", "To remove", foundProperty.getTitle());

        propertyService.remove(10);
        foundProperty = propertyService.convertSearchResultToObject(propertyService.findById(10));
        assertEquals("Additional property was not removed from database!", null, foundProperty);
    }

    @Test
    public void shouldFindById() throws Exception {
        PropertyService propertyService = new PropertyService();

        SearchResult property = propertyService.findById(1);
        Integer actual = property.getId();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByValue() throws Exception {
        PropertyService propertyService = new PropertyService();

        List<SearchResult> properties = propertyService.findByValue("second", true);
        Integer actual = properties.size();
        Integer expected = 4;
        assertEquals("Properties were not found in index!", expected, actual);

        properties = propertyService.findByValue("second value", true);
        actual = properties.size();
        expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndValue() throws Exception {
        PropertyService propertyService = new PropertyService();

        List<SearchResult> properties = propertyService.findByTitleAndValue("secondProcessProperty", "second");
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
