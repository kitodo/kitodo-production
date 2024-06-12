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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    private static final PropertyService propertyService = ServiceManager.getPropertyService();

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllProperties() {
        await().untilAsserted(
            () -> assertEquals(Long.valueOf(8), propertyService.countDatabaseRows(), "Properties were not counted correctly!"));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProperties() throws Exception {
        Long amount = propertyService.countDatabaseRows();
        assertEquals(Long.valueOf(8), amount, "Properties were not counted correctly!");
    }

    @Test
    public void shouldGetProcessProperty() throws Exception {
        Property processProperty = propertyService.getById(1);
        String actual = processProperty.getTitle();
        String expected = "Process Property";
        assertEquals(expected, actual, "Process property was not found in database - title doesn't match!");

        actual = processProperty.getValue();
        expected = "first value";
        assertEquals(expected, actual, "Process property was not found in database - value doesn't match!");
    }

    @Test
    public void shouldGetTemplateProperty() throws Exception {
        Property templateProperty = propertyService.getById(7);
        String actual = templateProperty.getTitle();
        String expected = "firstTemplate title";
        assertEquals(expected, actual, "Template property was not found in database - title doesn't match!");

        actual = templateProperty.getValue();
        expected = "first value";
        assertEquals(expected, actual, "Template property was not found in database - value doesn't match!");
    }

    @Test
    public void shouldGetWorkpieceProperty() throws Exception {
        Property workpieceProperty = propertyService.getById(5);
        String actual = workpieceProperty.getTitle();
        String expected = "FirstWorkpiece Property";
        assertEquals(expected, actual, "Workpiece property was not found in database - title doesn't match!");

        actual = workpieceProperty.getValue();
        expected = "first value";
        assertEquals(expected, actual, "Workpiece property was not found in database - value doesn't match!");
    }

    /**
     * test distinct titles.
     */
    @Test
    public void shouldFindDistinctTitles() {
        assertEquals(6, propertyService.findDistinctTitles().size(), "Incorrect size of distinct titles for process properties!");

        List<String> processPropertiesTitlesDistinct = propertyService.findDistinctTitles();

        String title = processPropertiesTitlesDistinct.get(0);
        assertEquals("FirstWorkpiece Property", title, "Incorrect sorting of distinct titles for process properties!");

        title = processPropertiesTitlesDistinct.get(1);
        assertEquals("Korrektur notwendig", title, "Incorrect sorting of distinct titles for process properties!");
    }

    @Test
    public void shouldGetAllProperties() throws Exception {
        List<Property> properties = propertyService.getAll();
        assertEquals(8, properties.size(), "Not all properties were found in database!");
    }

    @Test
    public void shouldGetAllPropertiesInGivenRange() throws Exception {
        List<Property> properties = propertyService.getAll(2, 6);
        assertEquals(6, properties.size(), "Not all properties were found in database!");
    }

    @Test
    public void shouldRemoveProperty() throws Exception {
        Property property = new Property();
        property.setTitle("To Remove");
        propertyService.saveToDatabase(property);
        Property foundProperty = propertyService.getById(property.getId());
        assertEquals("To Remove", foundProperty.getTitle(), "Additional property was not inserted in database!");

        property = new Property();
        property.setTitle("To remove");
        propertyService.saveToDatabase(property);
        foundProperty = propertyService.getById(10);
        assertEquals("To remove", foundProperty.getTitle(), "Additional property was not inserted in database!");

        propertyService.removeFromDatabase(10);
        assertThrows(DAOException.class, () -> propertyService.getById(10));
    }

    @Test
    public void shouldFindById() throws DAOException {
        Integer expected = 1;
        assertEquals(expected, propertyService.getById(1).getId(), "Property was not found in database!");
    }

    @Test
    public void shouldFindByValue() {
        assertEquals(2, propertyService.findByValue("second").size(), "Properties were not found in database!");

        assertEquals(1, propertyService.findByValue("second value").size(), "Property was not found in database!");
    }

    @Test
    public void shouldFindByTitleAndValue() {
        assertEquals(1, propertyService.findByTitleAndValue("Korrektur notwendig", "second value").size(), "Property was not found in database!");
    }

    @Test
    public void shouldNotFindByTitleAndValue() {
        assertEquals(0, propertyService.findByTitleAndValue("Korrektur notwendig", "third").size(), "Property was found in database!");
    }

}
