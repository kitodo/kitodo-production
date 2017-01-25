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

import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ProcessService;
import org.junit.Assert;
import org.junit.Test;

public class DBConnectionTestIT {

    @Test
    public void test() throws Exception {

        Process test = new Process();
        test.setTitle("TestTitle");
        ProcessService processService = new ProcessService();
        processService.save(test);

        long counted = processService.count("from Process");
        Assert.assertNotNull("No Process found",counted);
        Assert.assertEquals(1, counted);

        String title = processService.find(1).getTitle();
        Assert.assertEquals("TestTitle", title);

    }
}
