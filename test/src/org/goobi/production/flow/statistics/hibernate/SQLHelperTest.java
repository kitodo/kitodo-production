package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;

public class SQLHelperTest {

	@Test
	public void testGetWhereClauseForTimeFrame() {
		String testString = "date_format(Field1";
		assertTrue(SQLGenerator.getWhereClauseForTimeFrame(new Date(), new Date(768000232), new String("Field1")).contains(testString));
	}

}
