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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;

public class SQLStepRequestsTest {

    static SQLStepRequests request;
    static HistoryTypeEnum typeSelection;

    @BeforeClass
    public static void setUp() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2009, Calendar.FEBRUARY, 1);
        cal2.set(2009, Calendar.APRIL, 31);
        request = new SQLStepRequests(cal1.getTime(), cal2.getTime(), TimeUnit.days, null);
        typeSelection = HistoryTypeEnum.storageDifference;
    }

    @Test
    public final void testGetSQL() {

        String answer = request.getSQL(typeSelection, 1, true, true);
        assertNotNull(answer);

    }

    @Test
    public final void testGetSQLWithoutParam() {
        boolean exception = false;
        try {
            @SuppressWarnings("unused")
            String answer = request.getSQL();
            fail("Es wurde eine Exception erwartet.");
        } catch (UnsupportedOperationException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public final void testSQLMaxStepOrder() {
        String answer = request.getSQLMaxStepOrder(typeSelection);
        assertNotNull(answer);
    }

}
