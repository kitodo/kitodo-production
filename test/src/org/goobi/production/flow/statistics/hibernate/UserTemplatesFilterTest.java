package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.junit.Ignore;
import org.junit.Test;

public class UserTemplatesFilterTest {

	UserTemplatesFilter test = new UserTemplatesFilter();

	@Ignore("Crashing") 
	@Test
	public void testGetCriteria() {
		Criteria crit = test.getCriteria();
		assertNotNull(crit);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetName() {
		test.getName();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetFilter() {
		test.setFilter("something");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetName() {
		test.setName("something");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetSQL() {
		test.setSQL("something");
	}

	@Test
	public void testClone() {
		IEvaluableFilter clone = test.clone();
		assertNotNull(clone);
	}

	@Ignore("Crashing") 
	@Test
	public void testGetSourceData() {
		List<Object> sourceList = test.getSourceData();
		assertNotNull(sourceList);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetIDList() {
		test.getIDList();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetObservable() {
		test.getObservable();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testStepDone() {
		test.stepDone();
	}

}
