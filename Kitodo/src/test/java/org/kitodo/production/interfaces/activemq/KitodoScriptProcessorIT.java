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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;

public class KitodoScriptProcessorIT {
    @BeforeEach
    public void prepare() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterEach
    public void clean() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldExecuteKitodoScript() throws Exception {

        // define test data
        MapMessageObjectReader mockedMappedMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mockedMappedMessageObjectReader.getMandatoryString("script")).thenReturn("action:test");
        when(mockedMappedMessageObjectReader.getCollectionOfInteger("processes")).thenReturn(Collections.singletonList(1));

        // the object to be tested
        KitodoScriptProcessor underTest = new KitodoScriptProcessor();

        // organize return of results
        List<String> scriptResult = new ArrayList<>();
        List<Process> processesResult = new ArrayList<>();
        Field serviceField = KitodoScriptProcessor.class.getDeclaredField("kitodoScriptService");
        serviceField.setAccessible(true);
        serviceField.set(underTest, new KitodoScriptService() {
            @Override
            public void execute(List<Process> processes, String script) {
                scriptResult.add(script);
                processesResult.addAll(processes);
            }
        });

        // carry out test
        underTest.process(mockedMappedMessageObjectReader);

        // check results
        assertEquals(scriptResult.get(0), "action:test", "should have passed the script to be executed");
        assertEquals(processesResult.size(), 1, "should have passed one process");
        assertEquals(processesResult.get(0).getId(), 1, "should have passed process 1");
    }
}
