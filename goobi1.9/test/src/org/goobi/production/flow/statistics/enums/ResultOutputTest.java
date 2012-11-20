package org.goobi.production.flow.statistics.enums;

import static org.junit.Assert.*;


import org.junit.Test;

public class ResultOutputTest{

	@Test
	public final void testGetId() {
		assertEquals("1",ResultOutput.chart.getId());
		assertEquals("2",ResultOutput.table.getId());
		assertEquals("3",ResultOutput.chartAndTable.getId());
	}

	@Test
	public final void testGetTitle() {
		assertEquals("chart",ResultOutput.chart.getTitle());
		assertEquals("table",ResultOutput.table.getTitle());
		assertEquals("chartAndTable",ResultOutput.chartAndTable.getTitle());
	}

	@Test
	public final void testGetById() {
		assertEquals(ResultOutput.chart,ResultOutput.getById("1"));
		assertEquals(ResultOutput.table,ResultOutput.getById("2"));
		assertEquals(ResultOutput.chartAndTable,ResultOutput.getById("3"));
	}

}
