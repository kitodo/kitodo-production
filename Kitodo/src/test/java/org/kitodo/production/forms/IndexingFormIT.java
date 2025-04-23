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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.services.ServiceManager;

public class IndexingFormIT {

    private IndexingForm indexingForm = new IndexingForm();

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNodeWithoutMapping();
        Client client = new Client();
        ServiceManager.getClientService().saveToDatabase(client);
        User user = new User();
        ServiceManager.getUserService().saveToDatabase(user);
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(user, 1);
            return Objects.nonNull(ServiceManager.getUserService().getAuthenticatedUser());
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCreateMapping() {
        assertFalse(indexingForm.indexExists());
        indexingForm.createMapping(false);
        assertTrue(indexingForm.indexExists());
    }

    @Test
    @Disabled("Not working due to CDI injection problems")
    public void indexingAll() throws Exception {
        indexingForm.createMapping(false);
        Project project = new Project();
        project.setTitle("TestProject");
        project.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getProjectService().saveToDatabase(project);
        Process process = new Process();
        process.setTitle("testIndex");
        process.setProject(project);
        process.setIndexAction(IndexAction.INDEX);
        ServiceManager.getProcessService().saveToDatabase(process);

        indexingForm.countDatabaseObjects();

        ProcessDTO processOne = ServiceManager.getProcessService().findById(1);
        assertNull(processOne.getTitle(), "process should not be found in index");
        IndexAction indexAction = ServiceManager.getProcessService().getById(1).getIndexAction();
        assertEquals(IndexAction.INDEX, indexAction, "Index Action should be Index");
        indexingForm.startAllIndexing();
        given().ignoreExceptions().await()
                .until(() -> Objects.nonNull(ServiceManager.getProcessService().findById(1).getTitle()));
        processOne = ServiceManager.getProcessService().findById(1);
        assertEquals("testIndex", processOne.getTitle(), "process should be found");
        indexAction = ServiceManager.getProcessService().getById(1).getIndexAction();
        assertEquals(IndexAction.INDEX, indexAction, "Index Action should be Index");
    }
}
