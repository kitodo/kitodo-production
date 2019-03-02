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
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
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
            () -> assertEquals("Properties were not counted correctly!", Long.valueOf(8), propertyService.count()));
    }

    @Test
    public void shouldCountAllPropertiesAccordingToQuery() {
        QueryBuilder query = matchQuery("type", "process").operator(Operator.AND);
        await().untilAsserted(() -> assertEquals("Properties were not counted correctly!", Long.valueOf(4),
            propertyService.count(query)));
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

    @Test
    public void shouldFindDistinctTitles() throws Exception {
        await().untilAsserted(() -> assertEquals("Incorrect size of distinct titles for process properties!", 2,
            propertyService.findProcessPropertiesTitlesDistinct().size()));

        List<String> processPropertiesTitlesDistinct = propertyService.findProcessPropertiesTitlesDistinct();

        String title = processPropertiesTitlesDistinct.get(0);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "Korrektur notwendig", title);

        title = processPropertiesTitlesDistinct.get(1);
        assertEquals("Incorrect sorting of distinct titles for process properties!", "Process Property", title);
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
    public void shouldFindById() {
        Integer expected = 1;
        await().untilAsserted(
            () -> assertEquals("Property was not found in index!", expected, propertyService.findById(1).getId()));
    }

    @Test
    public void shouldFindByValue() {
        await().untilAsserted(() -> assertEquals("Properties were not found in index!", 3,
            propertyService.findByValue("second", null, true).size()));

        await().untilAsserted(() -> assertEquals("Property was not found in index!", 1,
            propertyService.findByValue("second value", null, true).size()));
    }

    @Test
    public void shouldFindManyByValueForExactType() {
        await().untilAsserted(() -> assertEquals("Properties were not found in index!", 2,
            propertyService.findByValue("third", "workpiece", false).size()));
    }

    @Test
    public void shouldFindOneByValueForExactType() {
        await().untilAsserted(() -> assertEquals("Property was not found in index!", 1,
            propertyService.findByValue("second", "process", true).size()));
    }

    @Test
    public void shouldNotFindByValueForExactType() {
        await().untilAsserted(() -> assertEquals("Property was found in index!", 0,
            propertyService.findByValue("third", "workpiece", true).size()));
    }

    @Test
    public void shouldFindByTitleAndValue() {
        await().untilAsserted(() -> assertEquals("Property was not found in index!", 1,
            propertyService.findByTitleAndValue("Korrektur notwendig", "second", null, true).size()));
    }

    @Test
    public void shouldNotFindByTitleAndValue() {
        await().untilAsserted(() -> assertEquals("Property was found in index!", 0,
            propertyService.findByTitleAndValue("Korrektur notwendig", "third", null, true).size()));
    }

    @Test
    public void shouldFindManyByTitleAndValueForExactType() {
        await().untilAsserted(() -> assertEquals("Properties were not found in index!", 2,
            propertyService.findByTitleAndValue("Korrektur notwendig", "third", "workpiece", false).size()));
    }

    @Test
    public void shouldFindOneByTitleAndValueForExactType() {
        await().untilAsserted(() -> assertEquals("Property was not found in index!", 1,
            propertyService.findByTitleAndValue("Korrektur notwendig", "second", "process", true).size()));
    }

    @Test
    public void shouldNotFindByTitleAndValueForExactType() {
        await().untilAsserted(() -> assertEquals("Property was found in index!", 0,
            propertyService.findByTitleAndValue("Korrektur notwendig", "second", "workpiece", true).size()));
    }
}
