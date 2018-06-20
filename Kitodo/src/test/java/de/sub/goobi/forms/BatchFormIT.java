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

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

public class BatchFormIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(new ServiceManager().getUserService().getById(1));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldFilterProcesses() {
        BatchForm batchForm = new BatchForm();

        batchForm.setProcessfilter("\"id:2\"");
        batchForm.filterProcesses();
        List<Process> processes = batchForm.getCurrentProcesses();

        assertEquals("Size of filtered processes is incorrect!", 1, processes.size());

        batchForm.setProcessfilter("\"id:1 2 3\"");
        batchForm.filterProcesses();
        processes = batchForm.getCurrentProcesses();
        assertEquals("Size of filtered processes is incorrect!", 3, processes.size());

        assertEquals("First sorted date is incorrect!", new DateTime().toString("YYYY-MM-dd"), formatDate(processes.get(0).getCreationDate()));
        assertEquals("Second sorted date is incorrect!", "2017-02-10", formatDate(processes.get(1).getCreationDate()));
        assertEquals("Second sorted date is incorrect!", "2017-01-20", formatDate(processes.get(2).getCreationDate()));

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
