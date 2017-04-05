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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.WorkpieceProperty;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;

/**
 * Tests for WorkpiecePropertyService class.
 */
public class WorkpiecePropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, ResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProcessWorkpiece() throws Exception {
        WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();

        WorkpieceProperty workpieceProperty = workpiecePropertyService.find(1);
        boolean condition = workpieceProperty.getTitle().equals("First Property")
                && workpieceProperty.getValue().equals("first value");
        assertTrue("Workpiece property was not found in database!", condition);
    }

    @Test
    public void shouldFindAllWorkpieces() throws Exception {
        WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();

        List<WorkpieceProperty> workpieceProperties = workpiecePropertyService.findAll();
        assertEquals("Not all workpiece properties were found in database!", 2, workpieceProperties.size());
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();

        WorkpieceProperty workpieceProperty = workpiecePropertyService.find(1);
        String expected = "First_Property";
        String actual = workpiecePropertyService.getNormalizedTitle(workpieceProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();

        WorkpieceProperty workpieceProperty = workpiecePropertyService.find(1);
        String expected = "first_value";
        String actual = workpiecePropertyService.getNormalizedValue(workpieceProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
