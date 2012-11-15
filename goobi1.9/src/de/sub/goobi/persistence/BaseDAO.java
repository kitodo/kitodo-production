package de.sub.goobi.persistence;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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

import java.io.Serializable;
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

public abstract class BaseDAO implements Serializable {
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
				session.beginTransaction().commit();
			}
		} catch (Exception e) {
			rollback();
			throw new DAOException(e);
		}
	}

	/**
	 * Removes the object from the database with with specified class type and <code>id</code>.
	 * 
	 * @param c
	 *            the class type to remove
	 * @param id
	 *            the id of the class type
	 * @throws DAOException
	 */
	@SuppressWarnings( "rawtypes" )
	protected void removeObj(Class c, Integer id) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			// first load the object with the current session.
			// the object must be loaded in this session before it
			// is deleted.
			synchronized (c) {
				Object obj = session.load(c, id);
				session.delete(obj);
				session.flush();
				session.beginTransaction().commit();
			}
		} catch (Exception e) {
			rollback();
			throw new DAOException(e);
		}
	}

	/**
	 * Retrieves and <code>Object</code> of the class type specified by <code>c</code>, and having the given <code>id</code>.
	 * 
	 * @param c
	 *            the class to load
	 * @param id
	 * @return Object may be null if object with ID doesn't exist
	 * @throws DAOException
	 */
	@SuppressWarnings({ "rawtypes" })
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
	@SuppressWarnings("rawtypes")
	protected List retrieveObjs(String query) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			return session.createQuery(query).list();
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	/**
	 * Mein eigener Entwurf für maximale Anzahl bei Rückgabe der Objekte
	 */
	@SuppressWarnings("rawtypes")
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

			Session session = Helper.getHibernateSession();
			// session.evict(obj);
			session.saveOrUpdate(obj);
			session.flush();
			session.beginTransaction().commit();
			// session.update(obj);
		} catch (HibernateException he) {
			rollback();
			throw new DAOException(he);
		}
		
	}

	protected void storeList(List<Object> list) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			for (Object obj : list) {
				session.saveOrUpdate(obj);
			}
			session.flush();
			session.beginTransaction().commit();
		} catch (HibernateException he) {
			rollback();
			throw new DAOException(he);
	
		}
	}

	/**
	 * Performs a rollback on the current session. Exceptions are logged.
	 * 
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is thrown while performing the rollback.
	 */
	protected void rollback() throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			if (session != null) {
				session.beginTransaction().rollback();
			}
		} catch (HibernateException he) {
			throw new DAOException(he);
//		} catch (SQLException sqle) {
//			throw new DAOException(sqle);
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
		if (session == null) {
			session = HibernateUtil.getSessionFactory().openSession();
			session.refresh(o);
			session.close();
		}
		session.refresh(o);
	}

	@SuppressWarnings("rawtypes")
	protected Object loadObj(Class c, Integer id) throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			if (session == null) {
				session = HibernateUtil.getSessionFactory().openSession();
				Object o = session.load(c, id);
				session.close();
				return o;
			}
			return session.load(c, id);
		} catch (HibernateException he) {
			throw new DAOException(he);
		}
	}

	
	protected void updateObj(Object o) {
		Session session = Helper.getHibernateSession();
		if (session == null) {
			session = HibernateUtil.getSessionFactory().openSession();
			session.update(o);
			session.close();
		}
		session.update(o);
	}

	@SuppressWarnings("rawtypes")
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

}
