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
//CHECKSTYLE:ON

package org.goobi.production.flow.statistics.hibernate;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

/**
 * This class UserDefinedFilter implements the IEvaluateFilter interface It takes care of unsolved hibernate issues
 * surrounding Criteria and Projections... could be also due to lack of hibernate knowledge but offers a pragmatic
 * solution for the purpose of creating robust extended statistical functions in goobi
 *
 * <p>It uses code formerly used in ProzessverwaltungForm.FilterAlleStart and it creates a Criteria which can be thrown
 * into the Page Object. At the same time it can now be used in order to provide statistical evaluations on the
 * filtered dataset without destroying the criteria used in the Page object.</p>
 *
 * <p>The interface IEvaluable Filter was used so that other Implementations of a filter could be used with the same
 * interface.</p>
 *
 * @author Wulf Riebensahm
 */
public class UserDefinedFilter implements IEvaluableFilter, Cloneable {
	private static final long serialVersionUID = 4715772407607416975L;
	private WeakReference<Criteria> myCriteria = null;
	private String myName = null;
	private String myFilterExpression = null;
	private List<Integer> myIds = null;
	private Dispatcher myObservable;
	private Parameters myParameter = new Parameters();

	/**
	 * Constructor using an Array of Integers representing the ids of the Objects that need to be selected
	 */
	public UserDefinedFilter(List<Integer> selectIDs) {
		myIds = new ArrayList<Integer>(selectIDs);
	}

	/**
	 * Constructor using the user generated search string
	 */
	public UserDefinedFilter(String filter) {
		myFilterExpression = filter;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getCriteria
	 * ()
	 */
	@Override
	public Criteria getCriteria() {

		if (myCriteria == null || myCriteria.get() == null) {
			if (myIds == null) {
				if (getFilter() != null) {
					myCriteria = new WeakReference<Criteria>(createCriteriaFromFilterString(getFilter()));
				}
			} else {
				myCriteria = new WeakReference<Criteria>(createCriteriaFromIDList());
			}
		}

		return myCriteria.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
	 */
	@Override
	public String getName() {
		return myName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter
	 * (java.lang.String)
	 */
	@Override
	public void setFilter(String filter) {
		// reset myCriteria because it is invalid, if filter expression changed
		myCriteria = null;
		myFilterExpression = filter;
	}

	/*
	 *
	 */
	public String getFilter() {
		return myFilterExpression;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setName
	 * (java.lang.String)
	 */
	@Override
	public void setName(String name) {
		myName = name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public UserDefinedFilter clone() {

		UserDefinedFilter udf = new UserDefinedFilter(myFilterExpression);
		udf.setObservable(myObservable);
		return udf;
	}

	/*
	 * needed to pass on Observable
	 */
	private void setObservable(Dispatcher observable) {
		myObservable = observable;
	}

	/**
	 * generates a hibernate criteria based on the string filter
	 ****************************************************************************/
	private Criteria createCriteriaFromFilterString(String inFilter) {
		Session session = Helper.getHibernateSession();

		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		/*
		 * -------------------------------- combine all parameters together this
		 * part was exported to FilterHelper so that other Filters could access
		 * it * --------------------------------
		 */
		String message = FilterHelper.criteriaBuilder(session, inFilter, crit, null, myParameter, false, null, true);
		if (message.length() > 0) {
			myObservable.setMessage(message);
		}

		/**
		 * used for step filter if query conditions include more than one step
		 * by using a range the Criteria produces more than one item per
		 * process, for each step it involves
		 **/

		createIDListFromCriteria(crit);
		crit = null;
		crit = createCriteriaFromIDList();

		return crit;
	}

	/**
	 * creates an ID list from the criteria in parameter
	 *
	 * @param crit add description
	 */
	@SuppressWarnings("unchecked")
	private void createIDListFromCriteria(Criteria crit) {
		myIds = new ArrayList<Integer>();
		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it
				.hasNext();) {
			Prozess p = (Prozess) it.next();
			myIds.add(p.getId());
			myCriteria = null;
		}
	}

	/**
	 * filter processes by id
	 ****************************************************************************/
	private PaginatingCriteria createCriteriaFromIDList() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		crit.add(Restrictions.in("id", myIds));
		return crit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Object> getSourceData() {
		return getCriteria().setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getIDList
	 * ()
	 */
	@Override
	public List<Integer> getIDList() {

		if (myIds == null) {
			// create ID list if not yet done
			createIDListFromCriteria(getCriteria());
		}
		return new ArrayList<Integer>(myIds);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getObservable
	 * ()
	 */
	@Override
	public Observable getObservable() {

		if (myObservable == null) {
			myObservable = new Dispatcher();
		}
		return myObservable;
	}

	/*
	 * this internal class is extending the Observable Class and dispatches a
	 * message to the Observers
	 */
	private static class Dispatcher extends Observable {

		private void setMessage(String message) {
			super.setChanged();
			super.notifyObservers(message);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#stepDone
	 * ()
	 */
	@Override
	public Integer stepDone() {
		return myParameter.getExactStepDone();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setSQL
	 * (java.lang.String)
	 */
	@Override
	public void setSQL(String sqlString) {
		throw new UnsupportedOperationException("The class " + this.getClass().getName()
				+ " does not implement setSQL() ");
	}

	protected static class Parameters {
		private Integer exactStepDone = null;

		protected void setStepDone(Integer exactStepDone) {
			this.exactStepDone = exactStepDone;
		}

		private Integer getExactStepDone() {
			return exactStepDone;
		}

	}

	@Override
	public String stepDoneName() {
		String tosearch = "stepdone:";
		if (myFilterExpression.contains(tosearch)) {
			String myStepname = myFilterExpression.substring(myFilterExpression.indexOf(tosearch));
			myStepname = myStepname.substring(tosearch.lastIndexOf(":") + 1, myStepname.length());
			if (myStepname.contains(" ")) {
				myStepname = myStepname.substring(0, myStepname.indexOf(" "));
			}
			return myStepname;
		}
		return null;
	}

}
