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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for WorkpieceService class.
 */
public class WorkpieceServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldCountAllWorkpieces() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Long amount = workpieceService.count();
        assertEquals("Workpieces were not counted correctly!", Long.valueOf(2), amount);
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
        assertEquals("Additional workpiece was not inserted in database!", "First process",
                foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(workpiece);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(3));
        assertEquals("Additional workpiece was not removed from database!", null, foundWorkpiece);

        workpiece = new Workpiece();
        workpiece.setProcess(process);
        workpieceService.save(workpiece);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(4));
        assertEquals("Additional workpiece was not inserted in database!", "First process",
                foundWorkpiece.getProcess().getTitle());

        workpieceService.remove(4);
        foundWorkpiece = workpieceService.convertSearchResultToObject(workpieceService.findById(4));
        assertEquals("Additional workpiece was not removed from database!", null, foundWorkpiece);
    }

    @Test
    public void shouldFindById() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        SearchResult workpiece = workpieceService.findById(1);
        Integer actual = workpiece.getId();
        Integer expected = 1;
        assertEquals("Workpiece was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        List<SearchResult> workpieces = workpieceService.findByProcessId(1);
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
        WorkpieceService workpieceService = new WorkpieceService();

        List<SearchResult> workpieces = workpieceService.findByProcessTitle("First process");
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
        WorkpieceService workpieceService = new WorkpieceService();

        List<SearchResult> workpieces = workpieceService.findByProperty("FirstWorkpiece Property", "first value");
        Integer actual = workpieces.size();
        Integer expected = 1;
        assertEquals("Workpiece was not found in index!", expected, actual);

        workpieces = workpieceService.findByProperty("FirstUserProperty", "first value");
        actual = workpieces.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        WorkpieceService workpieceService = new WorkpieceService();

        Workpiece workpiece = workpieceService.find(1);
        int actual = workpieceService.getPropertiesSize(workpiece);
        assertEquals("Workpiece's properties size is not equal to given value!", 2, actual);
    }
}
