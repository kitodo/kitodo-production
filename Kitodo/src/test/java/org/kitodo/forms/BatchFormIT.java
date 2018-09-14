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

package org.kitodo.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.helper.Helper;
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

        assertTrue("First sorted date is incorrect!", Helper.getDateAsFormattedString(processes.get(0).getCreationDate()).contains(new DateTime().toString("YYYY-MM-dd")));
        assertEquals("Second sorted date is incorrect!", "2017-02-10 00:00:00", Helper.getDateAsFormattedString(processes.get(1).getCreationDate()));
        assertEquals("Second sorted date is incorrect!", "2017-01-20 00:00:00", Helper.getDateAsFormattedString(processes.get(2).getCreationDate()));

        batchForm.setProcessfilter(null);
        batchForm.filterProcesses();
        processes = batchForm.getCurrentProcesses();
        assertEquals("Size of filtered processes is incorrect!", 3, processes.size());
    }
}
