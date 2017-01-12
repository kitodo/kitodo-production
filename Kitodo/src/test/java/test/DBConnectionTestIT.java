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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.ProzessDAO;
import org.junit.Assert;
import org.junit.Test;

public class DBConnectionTestIT {

    @Test
    public void test() throws Exception {

        Prozess test = new Prozess();
        test.setTitel("TestTitle");
        ProzessDAO dao = new ProzessDAO();
        dao.save(test);

        long counted = dao.count("from Prozess");
        Assert.assertNotNull("No Prozess found",counted);
        Assert.assertEquals(1, counted);

        String title = dao.get(1).getTitel();
        Assert.assertEquals("TestTitle", title);

    }
}
