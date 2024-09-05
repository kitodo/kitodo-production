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

package org.kitodo.production.forms;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;

import java.util.Objects;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;

public class IndexingFormIT {

    private IndexingForm indexingForm = new IndexingForm();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        Client client = new Client();
        ServiceManager.getClientService().save(client);
        User user = new User();
        ServiceManager.getUserService().save(user);
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(user, 1);
            return Objects.nonNull(ServiceManager.getUserService().getAuthenticatedUser());
        });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
    }

    @Test
    @Ignore("Not working due to CDI injection problems")
    public void indexingAll() throws Exception {
        Project project = new Project();
        project.setTitle("TestProject");
        project.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getProjectService().save(project);
        Process process = new Process();
        process.setTitle("testIndex");
        process.setProject(project);
        ServiceManager.getProcessService().save(process);

        indexingForm.countDatabaseObjects();

        Process processOne = ServiceManager.getProcessService().getById(1);
        Assert.assertNull("process should not be found in index", processOne.getTitle());
        indexingForm.startAllIndexing();
        given().ignoreExceptions().await()
                .until(() -> Objects.nonNull(ServiceManager.getProcessService().getById(1).getTitle()));
        processOne = ServiceManager.getProcessService().getById(1);
        Assert.assertEquals("process should be found", "testIndex",processOne.getTitle());
    }
}
