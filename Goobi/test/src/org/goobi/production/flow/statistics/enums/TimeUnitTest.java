/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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

//	@Test
//	public final void testGetTitle() {
//		assertEquals("days",TimeUnit.days.getTitle());
//		assertEquals("weeks",TimeUnit.weeks.getTitle());
//		assertEquals("months",TimeUnit.months.getTitle());
//		assertEquals("quarters",TimeUnit.quarters.getTitle());
//		assertEquals("years",TimeUnit.years.getTitle());
//	}

	@Test
	public final void testGetById() {
		assertEquals(TimeUnit.days,TimeUnit.getById("1"));
		assertEquals(TimeUnit.weeks,TimeUnit.getById("2"));
		assertEquals(TimeUnit.months,TimeUnit.getById("3"));
		assertEquals(TimeUnit.quarters,TimeUnit.getById("4"));
		assertEquals(TimeUnit.years,TimeUnit.getById("5"));
	}

}
