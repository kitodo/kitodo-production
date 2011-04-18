package org.goobi.production.search.lucene;

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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

/**
 * This class implements the IEvaluateFilter interface for Lucene.
 * 
 * The interface IEvaluable Filter was used so that other Implementations of a filter could be used with the same interface.
 * 
 * @author Robert Sehr
 * 
 */

public class LuceneFilter implements IEvaluableFilter, Cloneable {
	private static final long serialVersionUID = 8756951004361151313L;
	private String myName;
	private String myFilterExpression = null;
//	private Criteria myCriteria = null;
	private WeakReference<Criteria> myCriteria = null;
	private List<Integer> myIds;
	private Dispatcher myObservable;

	/**
	 * Constructor
	 */
	public LuceneFilter() {

	}

	/**
	 * Constructor using an Array of Integers representing the ids of the Objects that need to be selected
	 */
	public LuceneFilter(List<Integer> selectIDs) {
		myIds = new ArrayList<Integer>(selectIDs);
	}

	/**
	 * /** Constructor using the user generated search string
	 */
	public LuceneFilter(String filter) {
		myFilterExpression = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getCriteria ()
	 */
	public Criteria getCriteria() {
//		if (myCriteria == null) {
//			if (this.myIds == null) {
//				if (this.getFilter() != null) {
//					myCriteria =createCriteriaFromLucene(this.getFilter());
//				}
//			} else {
//				myCriteria = createCriteriaFromIDList();
//			}
//		}
//
//		return myCriteria;
		
		if (myCriteria == null || myCriteria.get() == null) {
			if (this.myIds == null) {
				if (this.getFilter() != null) {
					myCriteria = new WeakReference<Criteria>(createCriteriaFromLucene(this.getFilter()));
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
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getName()
	 */
	public String getName() {
		return myName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setFilter (java.lang.String)
	 */
	public void setFilter(String filter) {
		myCriteria = null;
		myFilterExpression = filter;
	}

	/**
	 * 
	 * @return filter expression as string
	 */
	public String getFilter() {
		return myFilterExpression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setName (java.lang.String)
	 */
	public void setName(String name) {
		myName = name;
	}

	/**
	 * filter processes by id
	 ****************************************************************************/
	private Criteria createCriteriaFromIDList() {
		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		crit.add(Restrictions.in("id", myIds));
		return crit;
	}

	/**
	 * filter processes by Lucene query
	 ****************************************************************************/
	private Criteria createCriteriaFromLucene(String filter) {

		Session session = Helper.getHibernateSession();
		PaginatingCriteria crit = new PaginatingCriteria(Prozess.class, session);
		crit.add(Restrictions.in("id", LuceneSearch.getSearchEngine().getSearchResults(filter)));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.IDataSource#getSourceData()
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getSourceData() {
		return getCriteria().setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getIDList ()
	 */
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
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#setSQL (java.lang.String)
	 */
	public void setSQL(String sqlString) {
		throw new UnsupportedOperationException("The class " + this.getClass().getName() + " does not implement setSQL() ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#stepDone ()
	 */
	public Integer stepDone() {
		throw new UnsupportedOperationException("The filter " + this.getClass().getName() + " does not support stepDone()");
	}

	/*
	 * needed to pass on Observable
	 */
	private void setObservable(Dispatcher observable) {
		myObservable = observable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.hibernate.IEvaluableFilter#getObservable ()
	 */
	public Observable getObservable() {
		if (myObservable == null) {
			myObservable = new Dispatcher();
		}
		return myObservable;
	}

	public IEvaluableFilter clone() {
		LuceneFilter udf = new LuceneFilter(myFilterExpression);
		udf.setObservable(myObservable);
		return udf;
	}
	
	public String stepDoneName() {
		return null;
	}

	/*
	 * this internal class is extending the Observable Class and dispatches a message to the Observers
	 */
	private static class Dispatcher extends Observable {
		@SuppressWarnings("unused")
		private void setMessage(String message) {
			super.setChanged();
			super.notifyObservers(message);
		}
	}

}
