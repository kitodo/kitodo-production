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

package de.sub.goobi.persistence;

//TODO: What's the licence of this file?
//TODO: Use Generics
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Util;
import de.sub.goobi.helper.exceptions.DAOException;

/**
 * Base class for DAOs. This class defines common CRUD methods.
 */

public abstract class BaseDAO implements Serializable{
	private static final long serialVersionUID = 4676125965631365912L;

	/**
	 * Removes the object from the database.
	 * 
	 * @param obj
	 *            the class to remove
	 * @throws DAOException
	 */
	protected void removeObj(Object obj) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			synchronized (obj) {
				session.evict(obj);
				session.delete(obj);
				session.flush();
				session.connection().commit();		
			}
		} catch (Exception e) {
			rollback();
			throw new DAOException(e);
		}
	}

	/**
	 * Removes the object from the database with with specified class type and
	 * <code>id</code>.
	 * 
	 * @param c
	 *            the class type to remove
	 * @param id
	 *            the id of the class type
	 * @throws DAOException
	 */
	protected void removeObj(Class c, Integer id) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			// first load the object with the current session.
			// the object must be loaded in this session before it
			//   is deleted.
			synchronized (c) {
				Object obj = session.load(c, id);
				session.delete(obj);
				session.flush();
				session.connection().commit();		
			}
		} catch (Exception e) {
			rollback();
			throw new DAOException(e);
		}
	}

	/**
	 * Retrieves and <code>Object</code> of the class type specified by
	 * <code>c</code>, and having the given <code>id</code>.
	 * 
	 * @param c
	 *            the class to load
	 * @param id
	 * @return Object may be null if object with ID doesn't exist
	 * @throws DAOException
	 */
	protected Object retrieveObj(Class c, Integer id) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			if (session == null) {
				session = HibernateUtil.getSessionFactory().openSession();
				Object o = session.get(c, id);
				session.close();
				return o;
			}
			return session.get(c, id);
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	/**
	 * Mein eigener Entwurf für Rückgabe der Objekte
	 */
	protected List retrieveObjs(String query) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			return session.createQuery(query).list();
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	protected List retrieveObjs(String queryString, String parameter) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();

			Query q = session.createQuery(queryString);
			q.setParameter(0, parameter);

			return q.list();
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	/**
	 * Mein eigener Entwurf für maximale Anzahl bei Rückgabe der Objekte
	 */
	protected List retrieveObjs(String query, int first, int max) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			Query q = session.createQuery(query);
			q.setFirstResult(first);
			q.setMaxResults(max);
			return q.list();
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	/**
	 * Mein eigener Entwurf für Ermittlung der Anzahl der Objekte
	 */
	protected Long retrieveAnzahl(String query) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			return (Long) session.createQuery("select count(*) " + query).uniqueResult();
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	/**
	 * Stores <code>obj</code>, making it persistent.
	 * 
	 * @param obj
	 * @throws DAOException
	 */
	protected void storeObj(Object obj) throws DAOException {
		try {
			//TODO: I think this synchronized() may not do at all what's intended 
			//if - let's say - two users access the "same" step they still have different
			//instances of that same step both threads will be able to execute this method 
			//simultaneausly on each of these instances (WR)
			
			synchronized (obj) {
				Session session = Helper.getHibernateSession();
				session.evict(obj);
				session.saveOrUpdate(obj);
				session.flush();
				session.connection().commit();	
			}
		} catch (HibernateException he) {
			rollback();
			throw new DAOException(he);
		} catch (SQLException sqle) {
			rollback();
			throw new DAOException(sqle);
		}
	}

	/**
	 * Performs a rollback on the current session. Exceptions are logged.
	 * 
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback.
	 */
	protected void rollback() throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			if (session != null) {
				session.connection().rollback();
			}
		} catch (HibernateException he) {
			throw new DAOException(he);
		} catch (SQLException sqle) {
			throw new DAOException(sqle);
		}
	}

	/**
	 * Retrieves the HQL query from the resource bundle.
	 * 
	 * @param key
	 *            the HQL query to lookup
	 */
	protected String getQuery(String key) {
		return Util.getQuery(key);
	}
	
	protected void refresh(Object o) {
		Session session = Helper.getHibernateSession();
		session.refresh(o);
	}

}
