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

    /**
     * Performs computationally expensive setup shared several tests. This
     * compromises the independence of the tests, bit is a necessary
     * optimization here.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @BeforeClass
    public static void setUp() {
        Calendar one = Calendar.getInstance();
        Calendar other = Calendar.getInstance();
        one.set(2009, 01, 01);
        other.set(2009, 03, 31);
        request = new SQLStepRequests(one.getTime(), other.getTime(), TimeUnit.days, null);
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
