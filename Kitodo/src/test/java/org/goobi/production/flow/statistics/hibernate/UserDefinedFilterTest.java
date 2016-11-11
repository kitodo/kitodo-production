/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("All tests crashing")
public class UserDefinedFilterTest {

	UserDefinedFilter filter = new UserDefinedFilter("stepdone:5");
	List<Integer> globalId = filter.getIDList();
	UserDefinedFilter filter2 = new UserDefinedFilter(globalId);

	@Test
	public void testUserDefinedFilterListOfInteger() {
		List<Integer> id = filter.getIDList();
		UserDefinedFilter filterWithIds = new UserDefinedFilter(id);
		assertNotNull(filterWithIds);
	}

	@Test
	public void testUserDefinedFilterString() {
		UserDefinedFilter filterWithString = new UserDefinedFilter("stepdone:5");
		assertNotNull(filterWithString);
	}

	@Test
	public void testGetCriteria() {
		Criteria crit1, crit2;
		crit1 = filter.getCriteria();
		crit2 = filter2.getCriteria();
		assertEquals(
			"CriteriaImpl(de.sub.goobi.Beans.Prozess:this[Subcriteria(projekt:proj), Subcriteria(schritte:steps)][steps.reihenfolge=5 and steps.bearbeitungsstatus=3, ()])",
			crit1.toString());
		assertNotNull(crit2);
	}

	@Test
	public void testGetName() {
		filter.setName("test");
		assertEquals("test", filter.getName());
	}

	@Test
	public void testSetName() {
		filter.setName("test");
		assertEquals("test", filter.getName());
	}

	@Test
	public void testSetFilter() {
		filter.setFilter("stepdone:5");
		assertEquals("stepdone:5", filter.getFilter());
	}

	@Test
	public void testGetFilter() {
		filter.setFilter("stepdone:5");
		assertEquals("stepdone:5", filter.getFilter());
	}

	@Test
	public void testClone() {
		UserDefinedFilter clone = filter.clone();
		assertNotNull(clone);
	}

	@Test
	public void testGetSourceData() {
		List<Object> sourceList = filter.getSourceData();
		assertNotNull(sourceList);
	}

	@Test
	public void testGetIDList() {
		List<Integer> idList = filter.getIDList();
		assertNotNull(idList);
	}

	@Test
	public void testGetObservable() {
		Observable obs = filter.getObservable();
		assertNotNull(obs);
	}

	@Test
	public void testStepDone() {
		int stepdone = filter.stepDone();
		assertNotNull(stepdone);
		assertEquals(5, stepdone);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetSQL() {
		filter.setSQL("someString");
	}

}
