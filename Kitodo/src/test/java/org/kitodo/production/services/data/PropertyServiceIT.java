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
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for PropertyService class.
 */
public class PropertyServiceIT {

    private static final PropertyService propertyService = ServiceManager.getPropertyService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProperties() {
        await().untilAsserted(
            () -> assertEquals("Properties were not counted correctly!", Long.valueOf(8), propertyService.countDatabaseRows()));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProperties() throws Exception {
        Long amount = propertyService.countDatabaseRows();
        assertEquals("Properties were not counted correctly!", Long.valueOf(8), amount);
    }

    @Test
    public void shouldGetProcessProperty() throws Exception {
        Property processProperty = propertyService.getById(1);
        String actual = processProperty.getTitle();
        String expected = "Process Property";
        assertEquals("Process property was not found in database - title doesn't match!", expected, actual);

        actual = processProperty.getValue();
        expected = "first value";
        assertEquals("Process property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldGetTemplateProperty() throws Exception {
        Property templateProperty = propertyService.getById(7);
        String actual = templateProperty.getTitle();
        String expected = "firstTemplate title";
        assertEquals("Template property was not found in database - title doesn't match!", expected, actual);

        actual = templateProperty.getValue();
        expected = "first value";
        assertEquals("Template property was not found in database - value doesn't match!", expected, actual);
    }

    @Test
    public void shouldGetWorkpieceProperty() throws Exception {
        Property workpieceProperty = propertyService.getById(5);
        String actual = workpieceProperty.getTitle();
        String expected = "FirstWorkpiece Property";
        assertEquals("Workpiece property was not found in database - title doesn't match!", expected, actual);

        actual = workpieceProperty.getValue();
        expected = "first value";
        assertEquals("Workpiece property was not found in database - value doesn't match!", expected, actual);
    }

    /**
     * test distinct titles.
     */
    @Test
    public void shouldFindDistinctTitles() {
        assertEquals("Incorrect size of distinct titles for process properties!", 6,
            propertyService.findDistinctTitles().size());

        List<String> processPropertiesTitlesDistinct = propertyService.findDistinctTitles();

        String title = processPropertiesTitlesDistinct.get(0);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "FirstWorkpiece Property", title);

        title = processPropertiesTitlesDistinct.get(1);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "Korrektur notwendig", title);
    }

    @Test
    public void shouldGetAllProperties() throws Exception {
        List<Property> properties = propertyService.getAll();
        assertEquals("Not all properties were found in database!", 8, properties.size());
    }

    @Test
    public void shouldGetAllPropertiesInGivenRange() throws Exception {
        List<Property> properties = propertyService.getAll(2, 6);
        assertEquals("Not all properties were found in database!", 6, properties.size());
    }

    @Test
    public void shouldRemoveProperty() throws Exception {
        Property property = new Property();
        property.setTitle("To Remove");
        propertyService.saveToDatabase(property);
        Property foundProperty = propertyService.getById(9);
        assertEquals("Additional property was not inserted in database!", "To Remove", foundProperty.getTitle());

        propertyService.saveToDatabase(foundProperty);
        exception.expect(DAOException.class);
        propertyService.getById(9);

        property = new Property();
        property.setTitle("To remove");
        propertyService.saveToDatabase(property);
        foundProperty = propertyService.getById(10);
        assertEquals("Additional property was not inserted in database!", "To remove", foundProperty.getTitle());

        propertyService.removeFromDatabase(10);
        exception.expect(DAOException.class);
        propertyService.getById(10);
    }

    @Test
    public void shouldFindById() throws DAOException {
        Integer expected = 1;
        assertEquals("Property was not found in database!", expected, propertyService.getById(1).getId());
    }

    @Test
    public void shouldFindByValue() {
        assertEquals("Properties were not found in database!", 2,
            propertyService.findByValue("second").size());

        assertEquals("Property was not found in database!", 1,
            propertyService.findByValue("second value").size());
    }

    @Test
    public void shouldFindByTitleAndValue() {
        assertEquals("Property was not found in database!", 1,
            propertyService.findByTitleAndValue("Korrektur notwendig", "second value").size());
    }

    @Test
    public void shouldNotFindByTitleAndValue() {
        assertEquals("Property was found in database!", 0,
            propertyService.findByTitleAndValue("Korrektur notwendig", "third").size());
    }

}
