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
import org.hibernate.Transaction;

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
        Session session = null;
        Transaction tx = null;
        try {
            session = Helper.getHibernateSession();
            synchronized (obj) {
                tx = session.beginTransaction();
                session.evict(obj);
                session.delete(obj);
                session.flush();
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new DAOException(e);
        } finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        Transaction tx = null;
        try {
            session = Helper.getHibernateSession();
            // first load the object with the current session.
            // the object must be loaded in this session before it
            // is deleted.
            synchronized (c) {
                tx = session.beginTransaction();
                Object obj = session.load(c, id);
                session.delete(obj);
                session.flush();
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new DAOException(e);
        } finally {
            if (session != null) {
                session.close();
            }
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
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            if (session == null) {
                session = HibernateUtil.getSessionFactory().openSession();
            }
            return session.get(c, id);
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Mein eigener Entwurf für Rückgabe der Objekte
     */
    @SuppressWarnings("rawtypes")
    protected List retrieveObjs(String query) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            return session.createQuery(query).list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Mein eigener Entwurf für maximale Anzahl bei Rückgabe der Objekte
     */
    @SuppressWarnings("rawtypes")
    protected List retrieveObjs(String query, int first, int max) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            Query q = session.createQuery(query);
            q.setFirstResult(first);
            q.setMaxResults(max);
            return q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Mein eigener Entwurf für Ermittlung der Anzahl der Objekte
     */
    protected Long retrieveAnzahl(String query) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            return (Long) session.createQuery("select count(*) " + query).uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Stores <code>obj</code>, making it persistent.
     *
     * @param obj
     * @throws DAOException
     */
    protected static void storeObj(Object obj) throws DAOException {
        Session session = null;
        Transaction tx = null;
        try {
            session = Helper.getHibernateSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(obj);
            session.flush();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    protected void storeList(List<Object> list) throws DAOException {
        Session session = null;
        Transaction tx = null;
        try {
            session = Helper.getHibernateSession();
            tx = session.beginTransaction();
            for (Object obj : list) {
                session.saveOrUpdate(obj);
            }
            session.flush();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
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
        }
        session.refresh(o);
        session.close();
    }

    @SuppressWarnings("rawtypes")
    protected Object loadObj(Class c, Integer id) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            if (session == null) {
                session = HibernateUtil.getSessionFactory().openSession();
            }
            return session.load(c, id);
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    protected void updateObj(Object o) {
        Session session = Helper.getHibernateSession();
        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
        }
        session.update(o);
        session.close();
    }

    @SuppressWarnings("rawtypes")
    protected List retrieveObjs(String queryString, String parameter) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            Query q = session.createQuery(queryString);
            q.setParameter(0, parameter);
            return q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Retrieve list of objects by query string and namend parameter.
     *
     * @param queryString Query string
     * @param namedParameter Name of named parameter
     * @param parameter Parameter value
     * @return List
     * @throws DAOException
     */
    protected List retrieveObjs(String queryString, String namedParameter, String parameter) throws DAOException {
        Session session = null;
        try {
            session = Helper.getHibernateSession();
            Query q = session.createQuery(queryString);
            q.setString(namedParameter, parameter);
            return q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
