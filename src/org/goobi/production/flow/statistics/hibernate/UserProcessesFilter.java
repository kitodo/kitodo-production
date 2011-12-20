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

import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

/*****************************************************************************
 * Filter, that uses Criteria, changed by {@link FilterHelper}.
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public class UserProcessesFilter implements IEvaluableFilter {
	private static final long serialVersionUID = -7552065588001741347L;

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getCriteria()
	 */
	public Criteria getCriteria() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);

		FilterHelper.criteriaBuilder(null, crit, false, null, null, null);
		return crit;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
	 */
	public String getName() {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'getName()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter(java.lang.String)
	 */
	public void setFilter(String Filter) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setFilter()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setName(java.lang.String)
	 */
	public void setName(String name) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setName()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setSQL(java.lang.String)
	 */
	public void setSQL(String sqlString) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not implement the method 'setSQL()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public IEvaluableFilter clone() {
		return new UserProcessesFilter();
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getSourceData() {
		return getCriteria().setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getIDList()
	 */
	public List<Integer> getIDList() {
		throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " does not support getIDList()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getObservable()
	 */
	public Observable getObservable() {
		throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " is not observable at this point");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#stepDone()
	 */
	public Integer stepDone() {
		throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " does not support stepDone()");
	}

	public String stepDoneName() {
		return null;
	}
}
