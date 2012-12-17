package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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

//import java.lang.ref.WeakReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

/**
 * This class UserDefinedFilter implements the IEvaluateFilter interface It
 * takes care of unsolved hibernate issues surrounding Criteria and Projections
 * ... could be also due to lack of hibernate knowledge but offers a pragmatic
 * solution for the purpose of creating robust extended statistical functions in
 * goobi
 * 
 * It uses code formerly used in ProzessverwaltungForm.FilterAlleStart and it
 * creates a Criteria which can be thrown into the Page Object. At the same time
 * it can now be used in order to provide statistical evaluations on the
 * filtered dataset without destroying the criteria used in the Page object
 * 
 * The interface IEvaluable Filter was used so that other Implementations of a
 * filter could be used with the same interface.
 * 
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public class UserDefinedFilter implements IEvaluableFilter, Cloneable {
	private static final long serialVersionUID = 4715772407607416975L;
	private WeakReference<Criteria> myCriteria = null;
	private String myName = null;
	private String myFilterExpression = null;
	private List<Integer> myIds = null;
	private Dispatcher myObservable;
	private Parameters myParameter = new Parameters();

	/**
	 * Constructor using an Array of Integers representing the ids of the
	 * Objects that need to be selected
	 ****************************************************************************/
	public UserDefinedFilter(List<Integer> selectIDs) {
		myIds = new ArrayList<Integer>(selectIDs);
	}

	/**
	 * Constructor using the user generated search string
	 * 
	 ****************************************************************************/
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
		String message = FilterHelper.criteriaBuilder(session, inFilter, crit, null, myParameter, null, null, true);
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
	 * @param crit
	 ****************************************************************************/
	@SuppressWarnings("unchecked")
	private void createIDListFromCriteria(Criteria crit) {
		myIds = new ArrayList<Integer>();
		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
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
		throw new UnsupportedOperationException("The class " + this.getClass().getName() + " does not implement setSQL() ");
	}

	protected class Parameters {
		private Boolean flagCriticalQuery = false;
		private Integer exactStepDone = null;

		protected void setCriticalQuery() {
			flagCriticalQuery = true;
		}

		protected void setStepDone(Integer exactStepDone) {
			this.exactStepDone = exactStepDone;
		}

		@SuppressWarnings("unused")
		private Boolean getCriticalQuery() {
			return flagCriticalQuery;
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
