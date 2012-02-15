package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataTable;

public class StatQuestStorageTest {

	StatQuestStorage test = new StatQuestStorage();
	Locale locale = new Locale("GERMAN");
	IDataSource testFilter = new UserDefinedFilter("stepdone:5");

	@Test
	public void testSetTimeUnit() {
		test.setTimeUnit(TimeUnit.days);
	}

	@Ignore("Crashing") 
	@Test
	public void testGetDataTables() {
		IDataSource testFilter = new UserDefinedFilter("stepdone:5");
		test.setTimeUnit(TimeUnit.days);
		List<DataTable> table = test.getDataTables(testFilter);
		assertNotNull(table);
	}

	@Test
	public void testSetCalculationUnit() {
		test.setCalculationUnit(CalculationUnit.pages);
	}

	@Test
	public void testSetTimeFrame() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.set(2009, 01, 01);
		cal2.set(2009, 03, 31);
		test.setTimeFrame(cal1.getTime(), cal2.getTime());
	}

	@Test
	public void testIsRendererInverted() {
		IRenderer inRenderer = new ChartRenderer();
		IRenderer inRenderer2 = new HtmlTableRenderer();
		assertFalse(test.isRendererInverted(inRenderer2));
		assertTrue(test.isRendererInverted(inRenderer));
	}

	@Test
	public void testGetNumberFormatPattern() {
		String answer = null;
		answer = test.getNumberFormatPattern();
		assertNotNull(answer);
	}

}
