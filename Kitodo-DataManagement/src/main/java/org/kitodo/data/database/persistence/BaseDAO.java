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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * Base class for DAOs.
 */
public abstract class BaseDAO<T extends BaseBean> implements Serializable {

    private static final long serialVersionUID = 4676125965631365912L;
    private static final Object lockObject = new Object();

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
    public abstract List<T> getAll() throws DAOException;

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
            try (Session session = HibernateUtil.getSession()) {
                Transaction transaction = session.beginTransaction();
                synchronized (lockObject) {
                    Object merged = session.merge(baseBean);
                    session.delete(merged);
                    session.flush();
                    transaction.commit();
                }
            } catch (HibernateException e) {
                throw new DAOException(e);
            }
        }
    }

    /**
     * Refresh given bean object.
     * 
     * @param baseBean
     *            bean to refresh
     */
    public void refresh(T baseBean) {
        refreshObject(baseBean);
    }

    /**
     * Retrieves BaseBean objects from database by given query.
     *
     * @param query
     *            as String
     * @param parameters
     *            for query
     * @return list of beans objects
     */
    @SuppressWarnings("unchecked")
    public List<T> getByQuery(String query, Map<String, Object> parameters) {
        try (Session session = HibernateUtil.getSession()) {
            Query q = session.createQuery(query);
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                if (parameter.getValue() instanceof List) {
                    q.setParameterList(parameter.getKey(), (List) parameter.getValue());
                } else {
                    q.setParameter(parameter.getKey(), parameter.getValue());
                }
            }
            return (List<T>) q.list();
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
        try (Session session = HibernateUtil.getSession()) {
            List<T> result = session.createQuery(query).list();
            if (Objects.isNull(result)) {
                result = new ArrayList<>();
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    List<Double> getAverage(String query, Map<String, Object> parameters) {
        try (Session session = HibernateUtil.getSession()) {
            Query q = session.createQuery(query);
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                q.setParameter(parameter.getKey(), parameter.getValue());
            }
            List<Double> result = q.list();
            if (Objects.isNull(result)) {
                result = new ArrayList<>();
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    List<Long> getCount(String query, Map<String, Object> parameters) {
        return getLongList(query, parameters);
    }

    List<Long> getSum(String query, Map<String, Object> parameters) {
        return getLongList(query, parameters);
    }

    @SuppressWarnings("unchecked")
    private List<Long> getLongList(String query, Map<String, Object> parameters) {
        try (Session session = HibernateUtil.getSession()) {
            Query q = session.createQuery(query);
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                q.setParameter(parameter.getKey(), parameter.getValue());
            }
            List<Long> result = q.list();
            if (Objects.isNull(result)) {
                result = new ArrayList<>();
            }
            return result;
        }
    }

    /**
     * Count all rows in database.
     *
     * @param query
     *            for counting objects
     * @return amount of rows in database according to given query
     */
    public Long count(String query) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            return (Long) session.createQuery(query).uniqueResult();
        } catch (HibernateException e) {
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
     *             if a HibernateException is thrown
     */
    @SuppressWarnings("unchecked")
    static void removeObject(Class cls, Integer id) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Transaction transaction = session.beginTransaction();
            synchronized (lockObject) {
                Object object = session.load(cls, id);
                session.delete(object);
                session.flush();
                transaction.commit();
            }
        } catch (HibernateException e) {
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
    T retrieveObject(Class cls, Integer id) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            return (T) session.get(cls, id);
        } catch (HibernateException e) {
            throw new DAOException(e);
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
    List<T> retrieveObjects(String query, int first, int max) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Query q = session.createQuery(query);
            q.setFirstResult(first);
            q.setMaxResults(max);
            return (List<T>) q.list();
        } catch (HibernateException e) {
            throw new DAOException(e);
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
    List<T> retrieveObjects(String query, String parameter) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
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
    List<T> retrieveObjects(String query, String namedParameter, String parameter) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Query q = session.createQuery(query);
            q.setParameter(namedParameter, parameter, StringType.INSTANCE);
            return (List<T>) q.list();
        } catch (HibernateException e) {
            throw new DAOException(e);
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
    List<T> retrieveAllObjects(Class cls) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Query query = session.createQuery("FROM " + cls.getSimpleName());
            return (List<T>) query.list();
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Store given object.
     *
     * @param object
     *            to persist
     */
    void storeObject(T object) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Transaction transaction = session.beginTransaction();
            if (object.getId() != null) {
                session.merge(object);
            } else {
                session.save(object);
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Store given list of objects.
     *
     * @param list
     *            of objects
     */
    void storeList(List<T> list) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            Transaction transaction = session.beginTransaction();
            for (Object obj : list) {
                session.saveOrUpdate(obj);
            }
            session.flush();
            transaction.commit();
        } catch (RuntimeException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Refresh object associated with the session.
     *
     * @param object
     *            associated with the session
     */
    void refreshObject(T object) {
        try (Session session = HibernateUtil.getSession()) {
            session.refresh(object);
        }
    }

    @SuppressWarnings("unchecked")
    T loadObject(Class cls, Integer id) throws DAOException {
        try (Session session = HibernateUtil.getSession()) {
            return (T) session.load(cls, id);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Update of the object.
     *
     * @param object
     *            to update
     */
    void updateObject(T object) {
        try (Session session = HibernateUtil.getSession()) {
            session.update(object);
        }
    }
}
