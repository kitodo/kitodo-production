package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.*;
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
