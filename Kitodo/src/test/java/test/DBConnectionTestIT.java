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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.services.ServiceManager;

public class DBConnectionTestIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
    }

    @Test
    public void test() throws Exception {
        ServiceManager serviceManager = new ServiceManager();

        long counted = serviceManager.getProcessService().count();
        Assert.assertNotNull("No Process found", counted);
        Assert.assertEquals(5, counted);

        String title = serviceManager.getProcessService().find(4).getTitle();
        Assert.assertEquals("DBConnectionTest", title);
    }
}
