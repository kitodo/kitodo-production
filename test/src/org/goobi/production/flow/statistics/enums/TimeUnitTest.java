package org.goobi.production.flow.statistics.enums;

import static org.junit.Assert.*;
import org.junit.Test;

public class TimeUnitTest{

	@Test
	public final void testGetId() {
		assertEquals("1",TimeUnit.days.getId());
		assertEquals("2",TimeUnit.weeks.getId());
		assertEquals("3",TimeUnit.months.getId());
		assertEquals("4",TimeUnit.quarters.getId());
		assertEquals("5",TimeUnit.years.getId());
	}

	@Test
	public final void testGetSqlKeyword() {
		assertEquals("day",TimeUnit.days.getSqlKeyword());
		assertEquals("week",TimeUnit.weeks.getSqlKeyword());
		assertEquals("month",TimeUnit.months.getSqlKeyword());
		assertEquals("quarter",TimeUnit.quarters.getSqlKeyword());
		assertEquals("year",TimeUnit.years.getSqlKeyword());
	}

	@Test
	public final void testGetSingularTitle() {
		assertEquals("day",TimeUnit.days.getSingularTitle());
		assertEquals("week",TimeUnit.weeks.getSingularTitle());
		assertEquals("month",TimeUnit.months.getSingularTitle());
		assertEquals("quarter",TimeUnit.quarters.getSingularTitle());
		assertEquals("year",TimeUnit.years.getSingularTitle());

	}

	@Test
	public final void testGetTitle() {
		assertEquals("days",TimeUnit.days.getTitle());
		assertEquals("weeks",TimeUnit.weeks.getTitle());
		assertEquals("months",TimeUnit.months.getTitle());
		assertEquals("quarters",TimeUnit.quarters.getTitle());
		assertEquals("years",TimeUnit.years.getTitle());
	}

	@Test
	public final void testGetById() {
		assertEquals(TimeUnit.days,TimeUnit.getById("1"));
		assertEquals(TimeUnit.weeks,TimeUnit.getById("2"));
		assertEquals(TimeUnit.months,TimeUnit.getById("3"));
		assertEquals(TimeUnit.quarters,TimeUnit.getById("4"));
		assertEquals(TimeUnit.years,TimeUnit.getById("5"));
	}

}
