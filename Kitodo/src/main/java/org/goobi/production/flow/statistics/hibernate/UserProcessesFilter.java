package org.goobi.production.flow.statistics.hibernate;

//CHECKSTYLE:OFF
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
//CHECKSTYLE:ON

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

import java.util.List;
import java.util.Observable;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Filter, that uses Criteria, changed by {@link FilterHelper}.
 *
 * @author Wulf Riebensahm
 */
public class UserProcessesFilter implements IEvaluableFilter, Cloneable {
	private static final long serialVersionUID = -7552065588001741347L;
	private boolean clearSession = false;

	public UserProcessesFilter(boolean clearSession) {
		this.clearSession = clearSession;
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
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);

		FilterHelper.criteriaBuilder(session, null, crit, false, null, false, null, clearSession);
		return crit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
	 */
	@Override
	public String getName() {
		throw new UnsupportedOperationException(this.getClass().getName()
				+ " does not implement the method 'getName()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter
	 * (java.lang.String)
	 */
	@Override
	public void setFilter(String Filter) {
		throw new UnsupportedOperationException(this.getClass().getName()
				+ " does not implement the method 'setFilter()'. This Filter is static");
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
		throw new UnsupportedOperationException(this.getClass().getName()
				+ " does not implement the method 'setName()'. This Filter is static");
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
		throw new UnsupportedOperationException(this.getClass().getName()
				+ " does not implement the method 'setSQL()'. This Filter is static");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IEvaluableFilter clone() {
		return new UserProcessesFilter(clearSession);
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
		throw new UnsupportedOperationException("The filter " + this.getClass().getName()
				+ " does not support getIDList()");
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
		throw new UnsupportedOperationException("The filter " + this.getClass().getName()
				+ " is not observable at this point");
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
		throw new UnsupportedOperationException("The filter " + this.getClass().getName()
				+ " does not support stepDone()");
	}

	@Override
	public String stepDoneName() {
		return null;
	}
}
