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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    private static final PropertyService propertyService = new ServiceManager().getPropertyService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProperties() throws Exception {
        Long amount = propertyService.count();
        assertEquals("Properties were not counted correctly!", Long.valueOf(8), amount);
    }

    @Test
    public void shouldCountAllPropertiesAccordingToQuery() throws Exception {
        String query = matchQuery("type", "process").operator(Operator.AND).toString();
        Long amount = propertyService.count(query);
        assertEquals("Properties were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForProperties() throws Exception {
        Long amount = propertyService.countDatabaseRows();
        assertEquals("Properties were not counted correctly!", Long.valueOf(8), amount);
    }

    @Test
    public void shouldFindProcessProperty() throws Exception {
        Property processProperty = propertyService.getById(1);
        String actual = processProperty.getTitle();
        String expected = "Process Property";
        assertEquals("Process property was not found in database - title doesn't match!", expected, actual);

        actual = processProperty.getValue();
        expected = "first value";
        assertEquals("Process property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldFindTemplateProperty() throws Exception {
        Property templateProperty = propertyService.getById(7);
        String actual = templateProperty.getTitle();
        String expected = "firstTemplate title";
        assertEquals("Template property was not found in database - title doesn't match!", expected, actual);

        actual = templateProperty.getValue();
        expected = "first value";
        assertEquals("Template property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldFindWorkpieceProperty() throws Exception {
        Property workpieceProperty = propertyService.getById(5);
        String actual = workpieceProperty.getTitle();
        String expected = "FirstWorkpiece Property";
        assertEquals("Workpiece property was not found in database - title doesn't match!", expected, actual);

        actual = workpieceProperty.getValue();
        expected = "first value";
        assertEquals("Workpiece property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldFindDistinctTitles() throws Exception {
        List<String> processPropertiesTitlesDistinct = propertyService.findProcessPropertiesTitlesDistinct();
        int size = processPropertiesTitlesDistinct.size();
        assertEquals("Incorrect size of distinct titles for process properties!", 2, size);

        String title = processPropertiesTitlesDistinct.get(0);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "Korrektur notwendig", title);

        title = processPropertiesTitlesDistinct.get(1);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "Process Property", title);
    }

    @Test
    public void shouldFindAllProperties() {
        List<Property> properties = propertyService.getAll();
        assertEquals("Not all properties were found in database!", 8, properties.size());
    }

    @Test
    public void shouldGetAllPropertiesInGivenRange() throws Exception {
        List<Property> properties = propertyService.getAll(2,6);
        assertEquals("Not all properties were found in database!", 6, properties.size());
    }

    @Test
    public void shouldRemoveProperty() throws Exception {
        Property property = new Property();
        property.setTitle("To Remove");
        propertyService.save(property);
        Property foundProperty = propertyService.getById(9);
        assertEquals("Additional property was not inserted in database!", "To Remove", foundProperty.getTitle());

        propertyService.remove(foundProperty);
        exception.expect(DAOException.class);
        propertyService.getById(9);

        property = new Property();
        property.setTitle("To remove");
        propertyService.save(property);
        foundProperty = propertyService.getById(10);
        assertEquals("Additional property was not inserted in database!", "To remove", foundProperty.getTitle());

        propertyService.remove(10);
        exception.expect(DAOException.class);
        propertyService.getById(10);
    }

    @Test
    public void shouldFindById() throws Exception {
        Integer actual = propertyService.findById(1).getId();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByValue() throws Exception {
        List<JSONObject> properties = propertyService.findByValue("second", null, true);
        Integer actual = properties.size();
        Integer expected = 3;
        assertEquals("Properties were not found in index!", expected, actual);

        properties = propertyService.findByValue("second value", null, true);
        actual = properties.size();
        expected = 1;
        assertEquals("Property was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByValueForExactType() throws Exception {
        List<JSONObject> properties = propertyService.findByValue("second", "process", true);
        Integer actual = properties.size();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);

        properties = propertyService.findByValue("third", "workpiece", false);
        actual = properties.size();
        expected = 2;
        assertEquals("Properties were not found in index!", expected, actual);

        properties = propertyService.findByValue("third", "workpiece", true);
        actual = properties.size();
        expected = 0;
        assertEquals("Property was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndValue() throws Exception {
        List<JSONObject> properties = propertyService.findByTitleAndValue("Korrektur notwendig", "second", null, true);
        Integer actual = properties.size();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);

        properties = propertyService.findByTitleAndValue("Korrektur notwendig", "third", null, true);
        actual = properties.size();
        expected = 0;
        assertEquals("Property was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitleAndValueForExactType() throws Exception {
        List<JSONObject> properties = propertyService.findByTitleAndValue("Korrektur notwendig", "second", "process", true);
        Integer actual = properties.size();
        Integer expected = 1;
        assertEquals("Property was not found in index!", expected, actual);

        properties = propertyService.findByTitleAndValue("Korrektur notwendig", "third", "workpiece", false);
        actual = properties.size();
        expected = 2;
        assertEquals("Properties were not found in index!", expected, actual);

        properties = propertyService.findByTitleAndValue("Korrektur notwendig", "second", "workpiece", true);
        actual = properties.size();
        expected = 0;
        assertEquals("Property was found in index!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        Property processProperty = propertyService.getById(1);
        String expected = "Process_Property";
        String actual = propertyService.getNormalizedTitle(processProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        Property processProperty = propertyService.getById(1);
        String expected = "first_value";
        String actual = propertyService.getNormalizedValue(processProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
