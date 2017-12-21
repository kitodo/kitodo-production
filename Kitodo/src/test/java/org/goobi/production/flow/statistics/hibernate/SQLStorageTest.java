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
        Calendar calendarOne = Calendar.getInstance();
        Calendar calendarTwo = Calendar.getInstance();
        calendarOne.set(2009, 01, 01);
        calendarTwo.set(2009, 03, 31);
        SQLStorage storage = new SQLStorage(calendarOne.getTime(), calendarTwo.getTime(), TimeUnit.days, null);
        assertNotNull(storage);
    }

    @Test
    public final void testGetSQL() {
        Calendar calendarOne = Calendar.getInstance();
        Calendar calendarTwo = Calendar.getInstance();
        calendarOne.set(2009, 01, 01);
        calendarTwo.set(2009, 03, 31);
        SQLStorage storage = new SQLStorage(calendarOne.getTime(), calendarTwo.getTime(), TimeUnit.days, null);
        String answer = storage.getSQL();
        assertNotNull(answer);

    }

}
