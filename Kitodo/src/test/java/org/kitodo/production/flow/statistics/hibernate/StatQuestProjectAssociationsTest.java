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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataTable;

import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.production.flow.statistics.IDataSource;
import org.kitodo.production.flow.statistics.enums.CalculationUnit;
import org.kitodo.production.flow.statistics.enums.TimeUnit;

public class StatQuestProjectAssociationsTest {

    Locale locale = new Locale("GERMAN");
    IDataSource testFilter = new UserDefinedFilter("stepdone:5");
    StatQuestProjectAssociations test = new StatQuestProjectAssociations();

    @Ignore("Crashing")
    @Test
    public void testGetDataTables() {
        IDataSource testFilter = new UserDefinedFilter("stepdone:5");
        test.setTimeUnit(TimeUnit.days);
        List<DataTable> table = test.getDataTables(testFilter);
        assertNotNull(table);
    }

    @Test
    public void testIsRendererInverted() {
        IRenderer inRenderer = new ChartRenderer();
        IRenderer inRenderer2 = new HtmlTableRenderer();
        assertFalse(test.isRendererInverted(inRenderer));
        assertTrue(test.isRendererInverted(inRenderer2));
    }

    @Test
    public void testSetCalculationUnit() {
        test.setCalculationUnit(CalculationUnit.pages);
    }

    @Test
    public void testSetTimeUnit() {
        test.setTimeUnit(TimeUnit.days);
    }

    @Test
    public void testGetNumberFormatPattern() {
        String answer = null;
        answer = test.getNumberFormatPattern();
        assertNotNull(answer);
    }

}
