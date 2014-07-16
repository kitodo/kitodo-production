/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

public class StatQuestProductionTest {

	Helper help = new Helper();
	static StatQuestProduction test;
	Locale locale = new Locale("GERMAN");
	IDataSource testFilter = new UserDefinedFilter("stepdone:5");
	StatisticsManager testManager = new StatisticsManager(StatisticsMode.PRODUCTION, testFilter, locale);

	@BeforeClass
	public static void setUp() {
	//TODO: HIBERNATE fix
//		Configuration cfg = HibernateUtil.getConfiguration();
//		cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost/testgoobi");
//		HibernateUtil.rebuildSessionFactory();
		test = new StatQuestProduction();
	}

	@AfterClass
	public static void tearDown() {
		
	}

	@Ignore("Crashing") 
	@Test
	public void testGetDataTables() throws DAOException {
		IDataSource testFilter = new UserDefinedFilter("stepdone:5");
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
		cal1.set(2008, 01, 01);
		cal2.set(2008, 03, 31);
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
