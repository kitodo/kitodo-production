/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.database.persistence;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.Helper;

/**
 * Base class for DAOs.
 *
 * @author Beatrycze Kmiec &lt;beatrycze.kmiec@slub-dresden.de&gt;
 */
public abstract class BaseDAO<T extends BaseBean> implements Serializable {

    private static final long serialVersionUID = 4676125965631365912L;

    /**
     * Removes the object from the database.
     *
     * @param object
     *            the class to remove
     * @throws DAOException
     *             add description
     */
    protected void removeObject(T object) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = Helper.getHibernateSession();
            transaction = session.beginTransaction();
            synchronized (object) {
                session.evict(object);
                session.delete(object);
                session.flush();
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(e);
        }
    }

    /**
     * Removes the object from the database with with specified class type and
     * <code>id</code>.
     *
     * @param cls
     *            the class type to remove
     * @param id
     *            the id of the class type
     * @throws DAOException
     *             add description
     */
    @SuppressWarnings("rawtypes")
    protected static void removeObject(Class cls, Integer id) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = Helper.getHibernateSession();
            transaction = session.beginTransaction();
            synchronized (cls) {
                Object object = session.load(cls, id);
                session.delete(object);
                session.flush();
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(e);
        }
    }

    /**
     * Retrieves an object of the class type specified by <code>cls</code>, and
     * having the given <code>id</code>.
     *
     * @param cls
     *            the class to load
     * @param id
     *            object id
     * @return Object may be null if object with ID doesn't exist
     */
    @SuppressWarnings({"unchecked" })
    protected T retrieveObject(Class cls, Integer id) throws DAOException {
        try {
            Session session = Helper.getHibernateSession();
            if (session == null) {
                session = HibernateUtil.getSessionFactory().openSession();
                T object = (T) session.get(cls, id);
                session.close();
                return object;
            }
            return (T) session.get(cls, id);
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve objects by given query.
     *
     * @param query
     *            string
     * @return list of results
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveObjects(String query) {
        Session session = Helper.getHibernateSession();
        return (List<T>) session.createQuery(query).list();
    }

    /**
     * Retrieve objects by given query for maximum number of objects.
     *
     * @param query
     *            string
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveObjects(String query, int first, int max) throws DAOException {
        try {
            Session session = Helper.getHibernateSession();
            Query q = session.createQuery(query);
            q.setFirstResult(first);
            q.setMaxResults(max);
            return (List<T>) q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Standard design for returning the objects.
     *
     * @param query
     *            string
     * @param parameter
     *            string
     * @return list of results
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveObjects(String query, String parameter) throws DAOException {
        try {
            Session session = Helper.getHibernateSession();
            Query q = session.createQuery(query);
            q.setParameter(0, parameter);
            return (List<T>) q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve list of objects by query string and named parameter.
     *
     * @param query
     *            string
     * @param namedParameter
     *            name of named parameter
     * @param parameter
     *            parameter value
     * @return List of objects
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveObjects(String query, String namedParameter, String parameter) throws DAOException {
        try {
            Session session = Helper.getHibernateSession();
            Query q = session.createQuery(query);
            q.setParameter(namedParameter, parameter, StringType.INSTANCE);
            return (List<T>) q.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Retrieve all objects fro given class.
     *
     * @param cls class
     * @return List of all objects
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveAllObjects(Class cls) {
        Session session = Helper.getHibernateSession();
        Query query = session.createQuery("FROM " + cls.getSimpleName());
        return (List<T>) query.list();
    }

    /**
     * Own design one of previous authors for determining the number of objects.
     *
     * @param query
     *            string
     * @return amount of results
     */
    protected Long retrieveAmount(String query) throws DAOException {
        try {
            Session session = Helper.getHibernateSession();
            return (Long) session.createQuery("select count(*) " + query).uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Store given object.
     *
     * @param object
     *            to persist
     */
    protected void storeObject(T object) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = Helper.getHibernateSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(object);
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(he);
        }
    }

    /**
     * Store given list of objects.
     *
     * @param list of objects
     */
    protected void storeList(List<T> list) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = Helper.getHibernateSession();
            transaction = session.beginTransaction();
            for (Object obj : list) {
                session.saveOrUpdate(obj);
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(he);
        }
    }

    /**
     * Refresh object associated with the session.
     *
     * @param object
     *            associated with the session
     */
    protected void refreshObject(T object) {
        Session session = Helper.getHibernateSession();
        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
            session.refresh(object);
            session.close();
        }
        session.refresh(object);
    }

    /**
     * Update of the object.
     *
     * @param object
     *            to update
     */
    protected void updateObject(T object) {
        Session session = Helper.getHibernateSession();
        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
            session.update(object);
            session.close();
        }
        session.update(object);
    }
}
