package org.goobi.production.flow.statistics.hibernate;

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

public class UserProjectFilter implements IEvaluableFilter, Cloneable {

	/**
	 *
	 */
	private static final long serialVersionUID = 441692997066826360L;

	private Integer projectID;
	// private Criteria myCriteria = null;
	private WeakReference<Criteria> myCriteria = null;
	private List<Integer> myIds = null;
	private Dispatcher myObservable;
	private String step = "";

	public UserProjectFilter(Integer projectID) {
		this.projectID = projectID;
	}

	@Override
	public Criteria getCriteria() {

		if (myCriteria == null || myCriteria.get() == null) {
			if (myIds == null) {
				if (projectID != null) {
					myCriteria = new WeakReference<Criteria>(createCriteriaFromProjectID());
				}
			} else {
				myCriteria = new WeakReference<Criteria>(createCriteriaFromIDList());
			}
		}

		return myCriteria.get();
	}

	private Criteria createCriteriaFromProjectID() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.createCriteria("projekt", "proj");
		crit.add(Restrictions.eq("proj.id", projectID));
		return crit;
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

	@Override
	public List<Integer> getIDList() {
		createIDListFromCriteria(getCriteria());
		return myIds;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("The class " + this.getClass().getName()
				+ " does not implement getName() ");
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

	@Override
	public void setFilter(String filter) {
		step = filter;

	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("The class " + this.getClass().getName()
				+ " does not implement setName() ");
	}

	@Override
	public void setSQL(String sqlString) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer stepDone() {
		throw new UnsupportedOperationException("The class " + this.getClass().getName()
				+ " does not implement stepDone() ");
	}

	@Override
	public List<Object> getSourceData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEvaluableFilter clone() {
		throw new UnsupportedOperationException("The class " + this.getClass().getName()
				+ " does not implement clone() ");
	}

	@Override
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
