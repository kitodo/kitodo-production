/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.goobi.production.flow.statistics.hibernate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

/**
 * This filter replaces the filter, which was integrated in class
 * AktuelleSchritteForm ... the purpose of refactoring was the goal to access
 * filter functions on the level of processes, which were already implemented in
 * UserDefinedFilter and combine them for the step filter.
 * 
 * 
 * @author Wulf Riebensahm
 * 
 */
public class UserDefinedStepFilter implements IEvaluableFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7134772860962768932L;
	private String myFilter = null;
	private WeakReference<Criteria> myCriteria = null;
	private ArrayList<Integer> myIds = null;
	private Dispatcher myObservable;
	private Boolean stepOpenOnly = false;
	private boolean userAssignedStepsOnly = false;

	/*
	 * setting basic filter modes
	 */
	public void setFilterModes(Boolean stepOpenOnly,
			boolean userAssignedStepsOnly) {
		myCriteria = null;
		this.stepOpenOnly = stepOpenOnly;
		this.userAssignedStepsOnly = userAssignedStepsOnly;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getCriteria
	 * ()
	 */
	public Criteria getCriteria() {
		// myCriteria is a WeakReference ... both cases needs to be evaluated,
		// after gc the WeakReference
		// object is still referenced but not the object referenced by it
		if (myCriteria == null || myCriteria.get() == null) {
			if (this.myIds == null) {
				if (this.myFilter != null) {
					myCriteria = new WeakReference<Criteria>(
							createCriteriaFromFilterString(this.myFilter));
				}
			} else {
				myCriteria = new WeakReference<Criteria>(
						createCriteriaFromIDList());
			}
		}

		return myCriteria.get();
	}

	private Criteria createCriteriaFromIDList() {
		Session session = Helper.getHibernateSession();
		Criteria crit = new PaginatingCriteria(Schritt.class, session);
		crit.add(Restrictions.in("id", myIds));
		return crit;
	}

	private Criteria createCriteriaFromFilterString(String filter) {
		Session session = Helper.getHibernateSession();

		PaginatingCriteria crit = new PaginatingCriteria(Schritt.class, session);		

		/*
		 * -------------------------------- 
		 * combine all parameters together this
		 * part was exported to FilterHelper so 
		 * that other Filters could access it 
		 * --------------------------------
		 */
		
		String message = FilterHelper.criteriaBuilder(myFilter, crit, null, null, stepOpenOnly, userAssignedStepsOnly);
		if (message.length() > 0) {
			myObservable.setMessage(message);
		}

		return crit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getIDList
	 * ()
	 */
	public List<Integer> getIDList() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement getIDList() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
	 */
	public String getName() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement getName() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getObservable
	 * ()
	 */
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
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter
	 * (java.lang.String)
	 */
	public void setFilter(String filter) {
		myCriteria = null;
		this.myFilter = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setName
	 * (java.lang.String)
	 */
	public void setName(String name) {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement setName() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setSQL
	 * (java.lang.String)
	 */
	public void setSQL(String sqlString) {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement setSQL() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#stepDone
	 * ()
	 */
	public Integer stepDone() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement stepDone() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
	 */
	public List<Object> getSourceData() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName()
				+ " does not implement getSourceData() ");
	}

	public UserDefinedStepFilter clone() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement clone() ");

	}
	
	public String stepDoneName() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement stepDoneName() ");
	}

}
