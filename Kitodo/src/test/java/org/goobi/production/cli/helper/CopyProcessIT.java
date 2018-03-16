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

package org.goobi.production.cli.helper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertTrue;

public class CopyProcessIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldPrepareFromTemplate() throws Exception {
        CopyProcess copyProcess = new CopyProcess();
        Process template = new ServiceManager().getProcessService().getById(1);
        copyProcess.setProzessVorlage(template);

        boolean result = copyProcess.prepare(null);

        Process copy = copyProcess.getProzessKopie();

        assertTrue("Copy process was not prepared!", result);

        assertTrue("Incorrect size of tasks in copied process!", template.getTasks().size() == copy.getTasks().size());
    }
}
