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
import org.kitodo.data.database.helper.HibernateHelper;

/**
 * Base class for DAOs.
 */
public abstract class BaseDAO<T extends BaseBean> implements Serializable {

    private static final long serialVersionUID = 4676125965631365912L;

    /**
     * Retrieves a BaseBean identified by the given id from the database.
     *
     * @param id
     *            of bean to load
     * @return persisted bean
     * @throws DAOException
     *             if a HibernateException is thrown
     */
    public abstract T getById(Integer id) throws DAOException;

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    public abstract List<T> getAll();

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     */
    public abstract List<T> getAll(int offset, int size) throws DAOException;

    /**
     * Saves a BaseBean object to the database.
     *
     * @param baseBean
     *            object to persist
     * @return stored object
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public abstract T save(T baseBean) throws DAOException;

    /**
     * Removes BaseBean object specified by the given id from the database.
     *
     * @param id
     *            of bean to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public abstract void remove(Integer id) throws DAOException;

    /**
     * Removes given BaseBean object from the database.
     *
     * @param baseBean
     *            bean to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public void remove(T baseBean) throws DAOException {
        if (baseBean.getId() != null) {
            Transaction transaction = null;
            try {
                Session session = HibernateHelper.getHibernateSession();
                transaction = session.beginTransaction();
                Object merged = session.merge(baseBean);
                session.delete(merged);
                session.flush();
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw new DAOException(e);
            }
        }
    }

    /**
     * Retrieves BaseBean objects from database by given query.
     *
     * @param query
     *            as String
     * @return list of beans objects
     */
    @SuppressWarnings("unchecked")
    public List<T> getByQuery(String query) {
        Session session = HibernateHelper.getHibernateSession();
        return (List<T>) session.createQuery(query).list();
    }

    /**
     * Count all rows in database.
     *
     * @param query
     *            for counting objects
     * @return amount of rows in database according to given query
     */
    public Long count(String query) throws DAOException {
        try {
            Session session = HibernateHelper.getHibernateSession();
            return (Long) session.createQuery("select count(*) " + query).uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
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
     *             if a HibernateException is thrown
     */
    @SuppressWarnings("unchecked")
    protected static void removeObject(Class cls, Integer id) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = HibernateHelper.getHibernateSession();
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
            Session session = HibernateHelper.getHibernateSession();
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
            Session session = HibernateHelper.getHibernateSession();
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
            Session session = HibernateHelper.getHibernateSession();
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
            Session session = HibernateHelper.getHibernateSession();
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
     * @param cls
     *            class
     * @return List of all objects
     */
    @SuppressWarnings("unchecked")
    protected List<T> retrieveAllObjects(Class cls) {
        Session session = HibernateHelper.getHibernateSession();
        Query query = session.createQuery("FROM " + cls.getSimpleName());
        return (List<T>) query.list();
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
            Session session = HibernateHelper.getHibernateSession();
            transaction = session.beginTransaction();
            if (object.getId() != null) {
                session.merge(object);
            } else {
                session.save(object);
            }
            session.flush();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(e);
        }
    }

    /**
     * Store given list of objects.
     *
     * @param list
     *            of objects
     */
    protected void storeList(List<T> list) throws DAOException {
        Transaction transaction = null;
        try {
            Session session = HibernateHelper.getHibernateSession();
            transaction = session.beginTransaction();
            for (Object obj : list) {
                session.saveOrUpdate(obj);
            }
            session.flush();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DAOException(e);
        }
    }

    /**
     * Refresh object associated with the session.
     *
     * @param object
     *            associated with the session
     */
    protected void refreshObject(T object) {
        Session session = HibernateHelper.getHibernateSession();
        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
            session.refresh(object);
            session.close();
        }
        session.refresh(object);
    }

    @SuppressWarnings("unchecked")
    protected T loadObjects(Class cls, Integer id) throws DAOException {
        try {
            Session session = HibernateHelper.getHibernateSession();
            if (session == null) {
                session = HibernateUtil.getSessionFactory().openSession();
                T object = (T) session.load(cls, id);
                session.close();
                return object;
            }
            return (T) session.load(cls, id);
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    /**
     * Update of the object.
     *
     * @param object
     *            to update
     */
    protected void updateObject(T object) {
        Session session = HibernateHelper.getHibernateSession();
        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
            session.update(object);
            session.close();
        }
        session.update(object);
    }
}
