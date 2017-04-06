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

package org.kitodo.production.flow.statistics.hibernate;

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.Test;
import org.kitodo.production.flow.statistics.enums.TimeUnit;

public class SQLStorageTest {

    @Test
    public final void testSQLStorage() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2009, 01, 01);
        cal2.set(2009, 03, 31);
        SQLStorage storage = new SQLStorage(cal1.getTime(), cal2.getTime(), TimeUnit.days, null);
        assertNotNull(storage);
    }

    @Test
    public final void testGetSQL() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2009, 01, 01);
        cal2.set(2009, 03, 31);
        SQLStorage storage = new SQLStorage(cal1.getTime(), cal2.getTime(), TimeUnit.days, null);
        String answer = storage.getSQL();
        assertNotNull(answer);

    }

}
