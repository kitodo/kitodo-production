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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import de.sub.goobi.converter.ProcessConverter;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.data.ProcessService;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockitoTest {

    private static Process process1;
    private static Process process2;

    @Mock
    private ProcessService mockedProcessService;

    @Mock
    private ProcessConverter mockedProcessConverter;

    /**
     * Performs computationally expensive setup shared several tests. This
     * compromises the independence of the tests, bit is a necessary
     * optimization here.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @BeforeClass
    public static void setUp() {
        process1 = new Process();
        process1.setTitle("testProcess1");

        process2 = new Process();
        process2.setTitle("testProcess2");
    }

    @Test
    public void testMock() {
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
