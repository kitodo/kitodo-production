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

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.data.database.exceptions.DAOException;

public class StatQuestProductionTest {

    static StatQuestProduction test;
    Locale locale = new Locale("GERMAN");
    private List testFilter = new ArrayList();
    StatisticsManager testManager = new StatisticsManager(StatisticsMode.PRODUCTION, testFilter, locale);

    @BeforeClass
    public static void setUp() {
        // TODO: HIBERNATE fix
        // Configuration cfg = HibernateUtil.getConfiguration();
        // cfg.setProperty("hibernate.connection.url",
        // "jdbc:mysql://localhost/testgoobi");
        // HibernateUtil.rebuildSessionFactory();
        test = new StatQuestProduction();
    }

    @AfterClass
    public static void tearDown() {

    }

    @Ignore("Crashing")
    @Test
    public void testGetDataTables() throws DAOException {
        test.setTimeUnit(TimeUnit.days);
        List<DataTable> tables = test.getDataTables(testFilter);
        int countTableInTables = 0;
        while (countTableInTables < tables.size()) {
            DataTable table = tables.get(countTableInTables);
            int countRowsInTable = 0;
            while (countRowsInTable < table.getDataRowsSize()) {
                List<DataRow> rows = table.getDataRows();
                ListIterator<DataRow> countRowInRows = rows.listIterator();
                while (countRowInRows.hasNext()) {
                    DataRow row = countRowInRows.next();
                    int number = row.getNumberValues();
                    int countValuesInRow = 0;
                    while (countValuesInRow < number) {
                        countValuesInRow++;
                        assertNotNull(row);
                    }
                    countRowsInTable++;
                }
                countTableInTables++;
            }
        }
    }

    @Test
    public void testSetTimeFrame() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2008, Calendar.FEBRUARY, 1);
        cal2.set(2008, Calendar.APRIL, 31);
        test.setTimeFrame(cal1.getTime(), cal2.getTime());
    }

    @Test
    public void testSetTimeUnit() {
        test.setTimeUnit(TimeUnit.days);
    }

    @Test
    public void testSetCalculationUnit() {
        test.setCalculationUnit(CalculationUnit.pages);
    }

    @Test
    public void testIsRendererInverted() {
        IRenderer inRenderer = new ChartRenderer();
        assertTrue(test.isRendererInverted(inRenderer));
    }

    @Test
    public void testGetNumberFormatPattern() {
        String answer = null;
        answer = test.getNumberFormatPattern();
        assertNotNull(answer);
    }
}
