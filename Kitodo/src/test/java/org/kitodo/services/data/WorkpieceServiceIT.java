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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * Tests for WorkpieceService class.
 */
public class WorkpieceServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindWorkpiece() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        boolean condition = workpiece.getProperties().size() == 2;
        assertTrue("Workpiece was not found in database!", condition);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        int actual = workpieceService.getPropertiesSize(workpiece);
        assertEquals("Workpiece's properties size is not equal to given value!", 2, actual);
    }
}
