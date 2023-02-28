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

package test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.converter.ProcessConverter;
import org.kitodo.production.services.data.ProcessService;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockitoTest {

    private static Process process1;
    private static Process process2;

    @Mock
    private ProcessService mockedProcessService;

    @Mock
    private ProcessConverter mockedProcessConverter;

    @BeforeClass
    public static void setUp() {
        process1 = new Process();
        process1.setTitle("testProcess1");

        process2 = new Process();
        process2.setTitle("testProcess2");
    }

    @Test
    public void testMock() throws Exception {
        when(mockedProcessService.getAll()).thenReturn(Arrays.asList(process1, process2));
        List<Process> allProcesses = mockedProcessService.getAll();
        Process testProcess = allProcesses.get(1);
        Assert.assertEquals("testProcess2", testProcess.getTitle());
    }

    @Test
    public void testGenericMock() {
        when(mockedProcessConverter.getAsObject(eq(null), eq(null), any(String.class))).thenReturn(process2);
        Object object = mockedProcessConverter.getAsObject(null, null, "1");
        Assert.assertEquals(process2, object);
    }
}
