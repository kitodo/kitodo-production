package org.goobi.production.flow.statistics.enums;

import de.sub.goobi.helper.Messages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculationUnitTest{

	@Test
	public final void testGetId() {
		assertEquals("1",CalculationUnit.volumes.getId());
		assertEquals("2",CalculationUnit.pages.getId());
		assertEquals("3",CalculationUnit.volumesAndPages.getId());
		
	}

	@Test
	public final void shouldReturnTranslatedTitle() {
		assertEquals(Messages.getString("volumes"), CalculationUnit.volumes.getTitle());
		assertEquals(Messages.getString("pages"), CalculationUnit.pages.getTitle());
		assertEquals(Messages.getString("volumesAndPages"), CalculationUnit.volumesAndPages.getTitle());
	}

	@Test
	public final void testGetById() {
		assertEquals(CalculationUnit.volumes,CalculationUnit.getById("1"));
		assertEquals(CalculationUnit.pages,CalculationUnit.getById("2"));
		assertEquals(CalculationUnit.volumesAndPages,CalculationUnit.getById("3"));
		
	
	}

}
