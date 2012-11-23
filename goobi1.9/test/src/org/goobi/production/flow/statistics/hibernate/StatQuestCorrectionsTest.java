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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;

public class StatQuestCorrectionsTest {
	static StatQuestCorrections test;

	@BeforeClass
	public static void setUp() {
		//TODO: HIBERNATE fix
//		Configuration cfg = HibernateUtil.getConfiguration();
//		cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost/testgoobi");
//		HibernateUtil.rebuildSessionFactory();
		test = new StatQuestCorrections();
	}
	
	@Test
	public final void testSetTimeUnit() {
		test.setTimeUnit(TimeUnit.days);
	}

	@Ignore("Crashing") 
	@Test
	public final void testGetDataTables() {
		IDataSource testFilter = new UserDefinedFilter("stepdone:5");
		test.setTimeUnit(TimeUnit.days);
		List<DataTable> tables = test.getDataTables(testFilter);
		java.util.Iterator<DataTable> tablesIterator = tables.iterator();
		int counter = 0;
		DataTable table = null;
		DataRow row = null;
		while (tablesIterator.hasNext()){
			table = tablesIterator.next();
			List<DataRow> rows = table.getDataRows();
			java.util.Iterator<DataRow> rowsIterator = rows.iterator();
			while (rowsIterator.hasNext()){
				row = rowsIterator.next();
			counter += row.getMaxValue();
			}
		}
		// count on max value of each row on test database should be 13
		assertEquals(counter, 13);
	}

	@Test
	public final void testSetCalculationUnit() {
		test.setCalculationUnit(CalculationUnit.pages);
	}

	@Test
	public final void testSetTimeFrame() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.set(2009, 01, 01);
		cal2.set(2009, 03, 31);
		test.setTimeFrame(cal1.getTime(), cal2.getTime());
	}

	@Test
	public final void testIsRendererInverted() {
		IRenderer inRenderer = new ChartRenderer();
		assertTrue(test.isRendererInverted(inRenderer));
	}

	@Test
	public final void testGetNumberFormatPattern() {
		String answer = null;
		answer = test.getNumberFormatPattern();
		assertNotNull(answer);
	}

}
