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

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for ProcessPropertyService class.
 */
public class ProcessPropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProcessProperty() throws Exception {
        ProcessPropertyService processPropertyService = new ProcessPropertyService();

        ProcessProperty processProperty = processPropertyService.find(1);
        boolean condition = processProperty.getTitle().equals("First Property") && processProperty.getValue().equals("first value");
        assertTrue("Process property was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProcesses() throws Exception {
        ProcessPropertyService processPropertyService = new ProcessPropertyService();

        List<ProcessProperty> processProperties = processPropertyService.findAll();
        assertEquals("Not all process properties were found in database!", 2, processProperties.size());
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        ProcessPropertyService processPropertyService = new ProcessPropertyService();

        ProcessProperty processProperty = processPropertyService.find(1);
        String expected = "First_Property";
        String actual = processPropertyService.getNormalizedTitle(processProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        ProcessPropertyService processPropertyService = new ProcessPropertyService();

        ProcessProperty processProperty = processPropertyService.find(1);
        String expected = "first_value";
        String actual = processPropertyService.getNormalizedValue(processProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
