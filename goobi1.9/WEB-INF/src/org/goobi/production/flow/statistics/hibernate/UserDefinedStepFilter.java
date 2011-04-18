/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
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
package org.goobi.production.flow.statistics.hibernate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Schritt;
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
		// crit = session.createCriteria(Prozess.class);
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
		
		// following was moved to Filter Helper
		// limitToUserAssignedSteps(crit);

				
		String message = FilterHelper.criteriaBuilder(session, myFilter, crit, null, null, stepOpenOnly, userAssignedStepsOnly);
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

/*	private void limitToUserAssignedSteps(Criteria inCrit) {
		 show only open Steps or those in use by current user 
		Session session = Helper.getHibernateSession();
		 identify current user 
		LoginForm login = (LoginForm) Helper
				.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() == null)
			return;
		 init id-list, preset with item 0 
		List<Integer> idList = new ArrayList<Integer>();
		idList.add(Integer.valueOf(0));

		
		 * -------------------------------- hits by user groups
		 * --------------------------------
		 
		Criteria critGroups = session.createCriteria(Schritt.class);

		if (stepOpenOnly)
			critGroups.add(Restrictions.eq("bearbeitungsstatus", Integer
					.valueOf(1)));
		else if (userAssignedStepsOnly) {
			critGroups.add(Restrictions.eq("bearbeitungsstatus", Integer
					.valueOf(2)));
			critGroups.add(Restrictions.eq("bearbeitungsbenutzer.id", login
					.getMyBenutzer().getId()));
		} else
			critGroups.add(Restrictions.or(Restrictions.eq(
					"bearbeitungsstatus", Integer.valueOf(1)), Restrictions
					.like("bearbeitungsstatus", Integer.valueOf(2))));

		 only processes which are not templates 
		Criteria temp = critGroups.createCriteria("prozess", "proz");
		critGroups.add(Restrictions.eq("proz.istTemplate", Boolean
				.valueOf(false)));

		 only assigned projects 
		temp.createCriteria("projekt", "proj").createCriteria("benutzer",
				"projektbenutzer");
		critGroups.add(Restrictions.eq("projektbenutzer.id", login
				.getMyBenutzer().getId()));

		
		 * only steps assigned to the user groups the current user is member of
		 
		critGroups.createCriteria("benutzergruppen", "gruppen")
				.createCriteria("benutzer", "gruppennutzer");
		critGroups.add(Restrictions.eq("gruppennutzer.id", login
				.getMyBenutzer().getId()));

		 collecting the hits 
		// TODO: Try to avoid Iterators, use for loops instead
		critGroups.setProjection(Projections.id());
		for (Iterator<Object> it = critGroups.setFirstResult(0).setMaxResults(
				Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			idList.add((Integer) it.next());
		}

		
		 * -------------------------------- Users only
		 * --------------------------------
		 
		Criteria critUser = session.createCriteria(Schritt.class);

		if (stepOpenOnly)
			critUser.add(Restrictions.eq("bearbeitungsstatus", Integer
					.valueOf(1)));
		else if (userAssignedStepsOnly) {
			critUser.add(Restrictions.eq("bearbeitungsstatus", Integer
					.valueOf(2)));
			critUser.add(Restrictions.eq("bearbeitungsbenutzer.id", login
					.getMyBenutzer().getId()));
		} else
			critUser.add(Restrictions.or(Restrictions.eq(
					"bearbeitungsstatus", Integer.valueOf(1)), Restrictions
					.like("bearbeitungsstatus", Integer.valueOf(2))));

		 exclude templates 
		Criteria temp2 = critUser.createCriteria("prozess", "proz");
		critUser.add(Restrictions.eq("proz.istTemplate", Boolean
				.valueOf(false)));

		 check project assignment 
		temp2.createCriteria("projekt", "proj").createCriteria("benutzer",
				"projektbenutzer");
		critUser.add(Restrictions.eq("projektbenutzer.id", login
				.getMyBenutzer().getId()));

		 only steps where the user is assigned to 
		critUser.createCriteria("benutzer", "nutzer");
		critUser.add(Restrictions.eq("nutzer.id", login.getMyBenutzer()
				.getId()));

		 collecting the hits 
		// TODO: Try to avoid Iterators, use for loops instead
		critUser.setProjection(Projections.id());
		for (Iterator<Object> it = critUser.setFirstResult(0)
				.setMaxResults(Integer.MAX_VALUE).list().iterator(); it
				.hasNext();) {
			idList.add((Integer) it.next());
		}

		
		 * -------------------------------- 
		 * only taking the hits by restricting
		 * to the ids --------------------------------
		 
		inCrit.add(Restrictions.in("id", idList));
		//setting list of class variable for availability
		myIds = (ArrayList<Integer>) idList;
	}*/

}
