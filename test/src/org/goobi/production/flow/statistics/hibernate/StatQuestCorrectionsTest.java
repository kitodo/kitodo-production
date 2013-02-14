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
