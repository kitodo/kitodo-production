package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ConverterTest {

	@Test
	public void testGetInteger() {
		Integer testInt = 21;
		assertTrue("False Integer Value", new Converter(testInt).getInteger() == testInt);
	}

	@Test
	public void testGetDouble() {
		double testDouble = 3.14;
		assertTrue("False double Value", Math.abs(new Converter(testDouble).getDouble() - testDouble) < .0000001);
	}

	@Test
	public void testGetString() {
		String testString = "test";
		assertTrue("False String value", new Converter(testString).getString() == testString);
	}

}
