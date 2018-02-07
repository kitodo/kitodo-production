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

package de.sub.goobi.forms;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;

public class BatchFormIT {

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

    @Test
    public void shouldFilterProcesses() {
        BatchForm batchForm = new BatchForm();

        batchForm.setProcessfilter("\"id:2\"");
        batchForm.filterProcesses();
        List<Process> processes = batchForm.getCurrentProcesses();

        assertEquals("Size of filtered processes is incorrect!", 1, processes.size());

        batchForm.setProcessfilter("\"id:2 3 4\"");
        batchForm.filterProcesses();
        processes = batchForm.getCurrentProcesses();
        assertEquals("Size of filtered processes is incorrect!", 3, processes.size());

        assertEquals("First sorted date is incorrect!", formatDate(new Date()), formatDate(processes.get(0).getCreationDate()));
        assertEquals("Second sorted date is incorrect!", "2017-02-10", formatDate(processes.get(1).getCreationDate()));
        assertEquals("Third sorted date is incorrect!", "2017-01-20", formatDate(processes.get(2).getCreationDate()));

        batchForm.setProcessfilter(null);
        batchForm.filterProcesses();
        processes = batchForm.getCurrentProcesses();
        assertEquals("Size of filtered processes is incorrect!", 3, processes.size());
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
