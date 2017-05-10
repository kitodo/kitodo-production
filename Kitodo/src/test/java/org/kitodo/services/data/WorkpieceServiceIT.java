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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;

/**
 * Tests for WorkpieceService class.
 */
public class WorkpieceServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, CustomResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindWorkpiece() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        boolean condition = workpiece.getProperties().size() == 2;
        assertTrue("Workpiece was not found in database!", condition);
    }

    @Test
    public void shouldRemoveWorkpiece() throws Exception {
        ProcessService processService = new ProcessService();
        WorkpieceService workpieceService = new WorkpieceService();

        Process process = processService.find(1);

        Workpiece workpiece = new Workpiece();
        workpiece.setProcess(process);
        workpieceService.save(workpiece);
        Workpiece foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(3));
        assertEquals("Additional workpiece was not inserted in database!", "First process", foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(workpiece);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(3));
        assertEquals("Additional workpiece was not removed from database!", null, foundWorkpiece);

        workpiece = new Workpiece();
        workpiece.setProcess(process);
        workpieceService.save(workpiece);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(4));
        assertEquals("Additional workpiece was not inserted in database!", "First process", foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(4);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(4));
        assertEquals("Additional workpiece was not removed from database!", null, foundWorkpiece);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        int actual = workpieceService.getPropertiesSize(workpiece);
        assertEquals("Workpiece's properties size is not equal to given value!", 2, actual);
    }
}
