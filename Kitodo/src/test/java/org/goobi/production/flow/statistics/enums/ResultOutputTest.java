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

package org.goobi.production.flow.statistics.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResultOutputTest {

    @Test
    public final void testGetId() {
        assertEquals("1", ResultOutput.chart.getId());
        assertEquals("2", ResultOutput.table.getId());
        assertEquals("3", ResultOutput.chartAndTable.getId());
    }

    // @Test
    // public final void testGetTitle() {
    // assertEquals("Chart",ResultOutput.chart.getTitle());
    // assertEquals("Table",ResultOutput.table.getTitle());
    // assertEquals("Chart & table",ResultOutput.chartAndTable.getTitle());
    // }

    @Test
    public final void testGetById() {
        assertEquals(ResultOutput.chart, ResultOutput.getById("1"));
        assertEquals(ResultOutput.table, ResultOutput.getById("2"));
        assertEquals(ResultOutput.chartAndTable, ResultOutput.getById("3"));
    }

}
