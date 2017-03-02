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

package org.kitodo.services;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for TemplatePropertyService class.
 */
public class TemplatePropertyServiceTestIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProcessTemplate() throws Exception {
        TemplatePropertyService templatePropertyService = new TemplatePropertyService();

        TemplateProperty templateProperty = templatePropertyService.find(1);
        boolean condition = templateProperty.getTitle().equals("first title") && templateProperty.getValue().equals("first value");
        assertTrue("Process property was not found in database!", condition);
    }

    @Test
    public void shouldFindAllTemplates() throws Exception {
        TemplatePropertyService processPropertyService = new TemplatePropertyService();

        List<TemplateProperty> processProperties = processPropertyService.findAll();
        assertEquals("Not all templates properties were found in database!", 2, processProperties.size());
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        TemplatePropertyService templatePropertyService = new TemplatePropertyService();

        TemplateProperty templateProperty = templatePropertyService.find(1);
        String expected = "first_title";
        String actual = templatePropertyService.getNormalizedTitle(templateProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        TemplatePropertyService processPropertyService = new TemplatePropertyService();

        TemplateProperty processProperty = processPropertyService.find(1);
        String expected = "first_value";
        String actual = processPropertyService.getNormalizedValue(processProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
