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

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * Tests for WorkpieceService class.
 */
public class WorkpieceServiceIT {

    private static final WorkpieceService workpieceService = new ServiceManager().getWorkpieceService();

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
    public void shouldCountAllWorkpieces() throws Exception {
        Long amount = workpieceService.count();
        assertEquals("Workpieces were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForWorkpieces() throws Exception {
        Long amount = workpieceService.countDatabaseRows();
        assertEquals("Workpieces were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindWorkpiece() throws Exception {
        Workpiece workpiece = workpieceService.getById(1);
        boolean condition = workpiece.getProperties().size() == 2;
        assertTrue("Workpiece was not found in database!", condition);
    }

    @Test
    public void shouldGetAllWorkpiecesInGivenRange() throws Exception {
        List<Workpiece> workpieces = workpieceService.getAll(2,10);
        assertEquals("Not all workpieces were found in database!", 0, workpieces.size());
    }

    @Test
    public void shouldRemoveWorkpiece() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        Process process = processService.getById(1);

        Workpiece workpiece = new Workpiece();
        workpiece.setProcess(process);
        workpieceService.save(workpiece);
        Workpiece foundWorkpiece = workpieceService.getById(3);
        assertEquals("Additional workpiece was not inserted in database!", "First process",
                foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(workpiece);
        exception.expect(DAOException.class);
        workpieceService.getById(3);

        workpiece = new Workpiece();
        workpiece.setProcess(process);
        workpieceService.save(workpiece);
        foundWorkpiece = workpieceService.getById(4);
        assertEquals("Additional workpiece was not inserted in database!", "First process",
                foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(4);
        exception.expect(DAOException.class);
        workpieceService.getById(4);
    }

    @Test
    public void shouldFindById() throws Exception {
        Integer actual = workpieceService.findById(1).getId();
        Integer expected = 1;
        assertEquals("Workpiece was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        List<JSONObject> workpieces = workpieceService.findByProcessId(1);
        Integer actual = workpieces.size();
        Integer expected = 2;
        assertEquals("Workpiece were not found in index!", expected, actual);

        workpieces = workpieceService.findByProcessId(4);
        actual = workpieces.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        List<JSONObject> workpieces = workpieceService.findByProcessTitle("First process");
        Integer actual = workpieces.size();
        Integer expected = 2;
        assertEquals("Workpiece was not found in index!", expected, actual);

        workpieces = workpieceService.findByProcessTitle("DBConnectionTest");
        actual = workpieces.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProperty() throws Exception {
        List<JSONObject> workpieces = workpieceService.findByProperty("FirstWorkpiece Property", "first value", true);
        Integer actual = workpieces.size();
        Integer expected = 1;
        assertEquals("Workpiece was not found in index!", expected, actual);

        workpieces = workpieceService.findByProperty("FirstUserProperty", "first value", true);
        actual = workpieces.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        Workpiece workpiece = workpieceService.getById(1);
        int actual = workpieceService.getPropertiesSize(workpiece);
        assertEquals("Workpiece's properties size is not equal to given value!", 2, actual);
    }

    @Test
    public void shouldNotInsertWorkpieceWithoutProcess() throws Exception {
        Workpiece workpiece = new Workpiece();
        workpiece.setProperties(new ArrayList<>());
        exception.expect(DataException.class);
        workpieceService.save(workpiece);
    }
}
