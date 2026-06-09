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

package org.kitodo.production.interfaces.activemq;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import javax.jms.*;

import org.junit.jupiter.api.*;
import org.kitodo.*;
import org.kitodo.config.*;
import org.kitodo.config.enums.*;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.*;
import org.kitodo.production.services.data.*;

public class CreateNewProcessesProcessorIT {
    private final ProcessService processService = ServiceManager.getProcessService();


    private static final File scriptDeleteSymLink = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_DELETE_SYMLINK));
    private static final File scriptCreateDirMeta = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));

    @BeforeEach
    public void prepare() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
        ExecutionPermission.setExecutePermission(scriptDeleteSymLink);
    }

    @AfterEach
    public void clean() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
        ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
        ExecutionPermission.setNoExecutePermission(scriptDeleteSymLink);
    }

    @Test
    public void test() throws Exception {
        // test data
        final int projectId = 1;
        final int templateId = 1;
        final String processTitle = "TestAMQ";
        final Map<String, Object> metadata = new HashMap<>(); {
            metadata.put("document_type", "Monograph");
            metadata.put("singleDigCollection", Arrays.asList("Collection 1", "Collection 2"));
            Map<String, Object> author = new HashMap<>(); {
                author.put("Role", "Author");
                author.put("FirstName", "Max");
                author.put("LastName", "Mustermann");
            }
            metadata.put("Person", author);
        }

        // test
        MapMessage message = mock(MapMessage.class, new FakeMapMessage());
        message.setString("id", processTitle); // map message subject (used in logging)
        message.setInt("project", projectId);
        message.setInt("template", templateId);
        message.setString("title", processTitle);
        message.setObject("metadata", metadata);

        CreateNewProcessesProcessor underTest = new CreateNewProcessesProcessor();
        underTest.onMessage(message);
        
        // checks
        BeanQuery query = new BeanQuery(Process.class);
        query.addStringRestriction("title", processTitle);
        List<Process> found = processService.getByQuery(query.formQueryWithoutSelect(), query.getQueryParameters());
        assertThat("should have created the process", found.size(), is(equalTo(1)));

        // clean up
        ProcessService.deleteProcess(found.get(0));
    }
}
