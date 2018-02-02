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

package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.Test;

public class SQLStorageTest {

    @Test
    public final void testSQLStorage() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2009, Calendar.FEBRUARY, 1);
        cal2.set(2009, Calendar.APRIL, 31);
        SQLStorage storage = new SQLStorage(cal1.getTime(), cal2.getTime(), TimeUnit.days, null);
        assertNotNull(storage);
    }

    @Test
    public final void testGetSQL() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2009, Calendar.FEBRUARY, 1);
        cal2.set(2009, Calendar.APRIL, 31);
        SQLStorage storage = new SQLStorage(cal1.getTime(), cal2.getTime(), TimeUnit.days, null);
        String answer = storage.getSQL();
        assertNotNull(answer);

    }

}
