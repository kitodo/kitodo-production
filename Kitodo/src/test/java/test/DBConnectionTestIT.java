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
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

public class DBConnectionTestIT {

    @Test
    public void test() throws Exception {

        Process test = new Process();
        test.setTitle("First process");
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.getProcessService().save(test);

        long counted = serviceManager.getProcessService().count("from Process");
        Assert.assertNotNull("No Process found", counted);
        Assert.assertEquals(4, counted);

        String title = serviceManager.getProcessService().find(1).getTitle();
        Assert.assertEquals("First process", title);

    }
}
