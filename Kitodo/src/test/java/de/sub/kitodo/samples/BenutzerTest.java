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

package de.sub.kitodo.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.data.database.beans.User;

@Ignore("Crashing")
public class BenutzerTest {

    @Test
    public void testLogin1() {
        User b = new User();
        b.setLogin("ein Name");
        System.out.println(b.getPasswordDecrypted());
    }

    @Test
    @Ignore("hallo")
    public void testLogin2() {
        User b = new User();
        b.setLogin("ein Name");
        // b.setMitMassendownload(true);
        assertTrue("wert falsch", b.isWithMassDownload());
        assertEquals("ein Name", b.getLogin());
    }
}
