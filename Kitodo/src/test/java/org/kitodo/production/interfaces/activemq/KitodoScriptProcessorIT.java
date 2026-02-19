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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KitodoScriptProcessorIT {

    @Captor
    private ArgumentCaptor<List<Process>> processCaptor;

    @Captor
    private ArgumentCaptor<String> scriptCaptor;

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

        // manipulate static field to insert a mocked service
        // using MockStatic or other options did not work or too less knowdlegde to manipulate a static field
        KitodoScriptService kitodoScriptService = mock(KitodoScriptService.class);
        Field field = ReflectionUtils
                .findFields(
                        KitodoScriptProcessor.class, f -> f.getName().equals("kitodoScriptService"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .getFirst();
        field.setAccessible(true);
        field.set(underTest, kitodoScriptService);

        // capture the method parameters of the execute method
        doNothing().when(kitodoScriptService).execute(processCaptor.capture(), scriptCaptor.capture());

        // carry out test
        underTest.process(mockedMappedMessageObjectReader);

        // check executed mocks
        verify(mockedMappedMessageObjectReader, times(1)).getMandatoryString("script");
        verify(mockedMappedMessageObjectReader, times(1)).getCollectionOfInteger("processes");
        verify(kitodoScriptService, times(1)).execute(anyList(), anyString());

        // check results
        assertEquals("action:test", scriptCaptor.getValue(), "should have passed the script to be executed");
        assertEquals(1, processCaptor.getAllValues().size(), "should have passed one process");
        assertEquals(1, processCaptor.getAllValues().getFirst().getFirst().getId(), "should have passed process 1");
    }

    @Test
    public void shouldNotExecuteKitodoScript() throws Exception {
        // test data
        MapMessageObjectReader mockedMessage = mock(MapMessageObjectReader.class);
        when(mockedMessage.getMandatoryString("script")).thenReturn("action:other");

        // the object to be tested
        KitodoScriptProcessor underTest = new KitodoScriptProcessor();

        // carry out test
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> underTest
                .process(mockedMessage));
        assertEquals("action:other is not allowed", illegalArgumentException.getMessage(),
            "should report that the action is not permitted");
    }
}
