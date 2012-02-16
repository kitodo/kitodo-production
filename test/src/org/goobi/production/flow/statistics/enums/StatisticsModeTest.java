package org.goobi.production.flow.statistics.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatisticsModeTest {
	
	@Test
	public final void testGetIsSimple() {
		assertTrue(StatisticsMode.SIMPLE_RUNTIME_STEPS.getIsSimple());
		assertFalse(StatisticsMode.PRODUCTION.getIsSimple());
	}

	@Test
	public final void testGetTitle() {
		assertEquals("runtimeOfSteps", StatisticsMode.SIMPLE_RUNTIME_STEPS.getTitle());
		assertEquals("productionThroughput", StatisticsMode.THROUGHPUT.getTitle());
		assertEquals("errorTracking", StatisticsMode.CORRECTIONS.getTitle());
		assertEquals("storageCalculator", StatisticsMode.STORAGE.getTitle());
		assertEquals("productionStatistics", StatisticsMode.PRODUCTION.getTitle());
		assertEquals("projectAssociation", StatisticsMode.PROJECTS.getTitle());
	}

	@Test
	public final void testGetStatisticalQuestion() {
		assertNotNull(StatisticsMode.STORAGE.getStatisticalQuestion());
	}

	@Test
	public final void testGetByClassName() {
		assertNotNull(StatisticsMode.PROJECTS.getStatisticalQuestion().getClass().getName());
		
	}

}
