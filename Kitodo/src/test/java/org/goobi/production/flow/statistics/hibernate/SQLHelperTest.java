/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;

public class SQLHelperTest {

	@Test
	public void testGetWhereClauseForTimeFrame() {
		//String testString = "intervall>=5 AND intervall<=1";
		String testString = "date_format(Field1";
		assertTrue(SQLGenerator.getWhereClauseForTimeFrame(new Date(), new Date(768000232), new String("Field1")).contains(testString));
	}

}
