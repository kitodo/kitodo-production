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

//TODO: Why doesn't this implemets Cloneable?
public class UserProjectFilter implements IEvaluableFilter{

	/**
	 * 
	 */
	private static final long serialVersionUID = 441692997066826360L;
	
	private Integer projectID;

	private WeakReference<Criteria> myCriteria = null;
	private List<Integer> myIds = null;
	private Dispatcher myObservable;
	private String step="";
	
	public UserProjectFilter(Integer projectID){
		this.projectID = projectID;
	}

	public Criteria getCriteria() {

		if (myCriteria == null || myCriteria.get() == null) {
			if (this.myIds == null) {
				if (this.projectID != null) {
					myCriteria = new WeakReference<Criteria>(
							createCriteriaFromProjectID());
				}
			} else {
				myCriteria = new WeakReference<Criteria>(
						createCriteriaFromIDList());
			}
		}

		return myCriteria.get();
	}

	private Criteria createCriteriaFromProjectID() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session); 
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.createCriteria("projekt", "proj");
		crit.add(Restrictions.eq("proj.id", this.projectID));
		return crit;
	}

	/**
	 * filter processes by id
	 ****************************************************************************/
	private PaginatingCriteria createCriteriaFromIDList() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		// crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.in("id", myIds));
		return crit;
	}
	
	
	@SuppressWarnings("unchecked")
	private void createIDListFromCriteria(Criteria crit) {
		myIds = new ArrayList<Integer>();
		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			Prozess p = (Prozess) it.next();
			myIds.add(p.getId());
			myCriteria = null;
		}
	}
	
	public List<Integer> getIDList() {
		createIDListFromCriteria(getCriteria());
		return myIds;
	}

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
	
	public void setFilter(String filter) {
		step = filter;
		
	}

	public void setName(String name) {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement setName() ");
	}

	public void setSQL(String sqlString) {
		// TODO Auto-generated method stub
		
	}

	public Integer stepDone() {
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement stepDone() ");
	}

	public List<Object> getSourceData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IEvaluableFilter clone(){
		throw new UnsupportedOperationException("The class "
				+ this.getClass().getName() + " does not implement clone() ");
	}
	
	public String stepDoneName() {
		return step;
	}
	
	/*
	 * this internal class is extending the Observable Class and dispatches a
	 * message to the Observers
	 */
	private static class Dispatcher extends Observable {

		@SuppressWarnings("unused")
		private void setMessage(String message) {
			super.setChanged();
			super.notifyObservers(message);
		}
	}
}
