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

package de.sub.kitodo.helper.archive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.sub.kitodo.helper.tasks.ProcessSwapInTask;
import de.sub.kitodo.helper.tasks.ProcessSwapOutTask;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

@Ignore("Crashing")
public class ProcessSwapOutTaskTest {
    static Process proz = null;
    static ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        proz = serviceManager.getProcessService().find(119);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void swapTest() {
        proz.setSwappedOutGui(false);
        swapOut();
    }

    private void swapOut() {
        ProcessSwapOutTask psot = new ProcessSwapOutTask();
        psot.initialize(proz);
        psot.run();
        assertTrue(proz.isSwappedOutGui());
    }

    @SuppressWarnings("unused")
    private void swapIn() {
        ProcessSwapInTask psot = new ProcessSwapInTask();
        psot.initialize(proz);
        psot.run();
        assertFalse(proz.isSwappedOutGui());
    }

}
