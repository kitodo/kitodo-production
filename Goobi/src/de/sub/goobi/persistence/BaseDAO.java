package de.sub.goobi.persistence;

/**
 * Copyright 2005 Nick Heudecker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * 
 * Changes have been made by Steffen Hankiewicz.
 * 
 * @author Nick Heudecker <nick@systemmobile.com>
 * @author Steffen Hankiewicz <steffen.hankiewicz@intranda.com>
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
	@SuppressWarnings("rawtypes")
	protected static void removeObj(Class c, Integer id) throws DAOException {
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
	protected static Object retrieveObj(Class c, Integer id) throws DAOException {
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
	protected static void storeObj(Object obj) throws DAOException {
		try {

			Session session = Helper.getHibernateSession();
			session.saveOrUpdate(obj);
			session.flush();
			session.beginTransaction().commit();
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
	protected static void rollback() throws DAOException {
		try {
			Session session = Helper.getHibernateSession();
			if (session != null) {
				session.beginTransaction().rollback();
			}
		} catch (HibernateException he) {
			throw new DAOException(he);
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
