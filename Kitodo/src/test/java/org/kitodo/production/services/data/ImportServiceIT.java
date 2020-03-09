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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

import java.net.URI;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImportServiceIT {

    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final ImportService importService = ServiceManager.getImportService();

    private static final String firstProcess = "First process";
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        await().untilTrue(new AtomicBoolean(Objects.nonNull(processService.findByTitle(firstProcess))));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testImportProcess() throws Exception {
        Assert.assertEquals("Not the correct amount of processes found",(long) 7, (long) processService.count());
        Process importedProcess = importService.importProcess("1443484881", 1, 1, "K10Plus");

        Assert.assertEquals("WrongProcessTitle", "KlAiSoP_1443484881", importedProcess.getTitle());
        Assert.assertEquals("Wrong project used", 1, (long) importedProcess.getProject().getId());
        Assert.assertEquals("Wrong template used", 1, (long) importedProcess.getTemplate().getId());
        Assert.assertEquals("Not the correct amount of processes found",(long) 8, (long) processService.count());
    }
}
