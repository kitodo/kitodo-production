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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.kitodo.data.database.exceptions.InfrastructureException;

// TODO: Fix for Hibernate-Session-Management, replaced with older version,
// the newer version follows on bottom of this class

/**
 * Basic Hibernate helper class, handles SessionFactory, Session and
 * Transaction.
 *
 * <p>
 * Uses a static initializer for the initial SessionFactory creation and holds
 * Session and Transactions in thread local variables. All exceptions are
 * wrapped in an unchecked InfrastructureException.
 *
 * @author christian@hibernate.org
 */
@SuppressWarnings("deprecation")
public class HibernateUtilOld {

    private static Log log = LogFactory.getLog(HibernateUtilOld.class);

    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static final ThreadLocal<Session> threadSession = new ThreadLocal<>();
    private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<>();
    private static final ThreadLocal<Interceptor> threadInterceptor = new ThreadLocal<>();
    private static final Object sessionFactoryRebuildLock = new Object();

    // Create the initial SessionFactory from the default configuration files
    static {
        try {
            configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
            // We could also let Hibernate bind it to JNDI:
            // configuration.configure().buildSessionFactory()
        } catch (Throwable ex) {
            // We have to catch Throwable, otherwise we will miss
            // NoClassDefFoundError and other subclasses of Error
            log.error("Building SessionFactory failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns the SessionFactory used for this static class.
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Returns the original Hibernate configuration.
     *
     * @return Configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     *
     */
    public static void rebuildSessionFactory() throws InfrastructureException {
        synchronized (sessionFactoryRebuildLock) {
            try {
                sessionFactory = getConfiguration().buildSessionFactory();
            } catch (Exception ex) {
                throw new InfrastructureException(ex);
            }
        }
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     *
     * @param cfg
     *            the configuration
     */
    public static void rebuildSessionFactory(Configuration cfg) throws InfrastructureException {
        synchronized (sessionFactoryRebuildLock) {
            try {
                sessionFactory = cfg.buildSessionFactory();
                configuration = cfg;
            } catch (Exception ex) {
                throw new InfrastructureException(ex);
            }
        }
    }

    /**
     * Retrieves the current Session local to the thread.
     *
     * <p/>
     * If no Session is open, opens a new Session for the running thread.
     *
     * @return Session
     */
    public static Session getSession() throws InfrastructureException {
        Session s = threadSession.get();
        try {
            if (s == null) {
                if (getInterceptor() != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Using interceptor: " + getInterceptor().getClass());
                    }
                    s = getSessionFactory().openSession();
                } else {
                    s = getSessionFactory().openSession();
                }
                threadSession.set(s);
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
        return s;
    }

    /**
     * Closes the Session local to the thread.
     */
    public static void closeSession() throws InfrastructureException {
        try {
            Session s = threadSession.get();
            threadSession.set(null);
            if (s != null && s.isOpen()) {
                s.close();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Start a new database transaction.
     */
    public static void beginTransaction() throws InfrastructureException {
        Transaction tx = threadTransaction.get();
        try {
            if (tx == null) {
                log.debug("Starting new database transaction in this thread.");
                tx = getSession().beginTransaction();
                threadTransaction.set(tx);
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Commit the database transaction.
     */
    public static void commitTransaction() throws InfrastructureException {
        Transaction tx = threadTransaction.get();
        try {
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log.debug("Committing database transaction of this thread.");
                tx.commit();
            }
            threadTransaction.set(null);
        } catch (HibernateException ex) {
            rollbackTransaction();
            throw new InfrastructureException(ex);
        }
    }

    /**
     * Commit the database transaction.
     */
    public static void rollbackTransaction() throws InfrastructureException {
        Transaction tx = threadTransaction.get();
        try {
            threadTransaction.set(null);
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log.debug("Tyring to rollback database transaction of this thread.");
                tx.rollback();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        } finally {
            closeSession();
        }
    }

    /**
     * Disconnect and return Session from current Thread.
     *
     * @return Session the disconnected Session
     */
    public static Session disconnectSession() throws InfrastructureException {

        Session session = getSession();
        try {
            threadSession.set(null);
            if (session.isConnected() && session.isOpen()) {
                session.disconnect();
            }
        } catch (HibernateException ex) {
            throw new InfrastructureException(ex);
        }
        return session;
    }

    /**
     * Register a Hibernate interceptor with the current thread.
     *
     * <p>
     * Every Session opened is opened with this interceptor after registration.
     * Has no effect if the current Session of the thread is already open,
     * effective on next close()/getSession().
     */
    public static void registerInterceptor(Interceptor interceptor) {
        threadInterceptor.set(interceptor);
    }

    private static Interceptor getInterceptor() {
        Interceptor interceptor = threadInterceptor.get();
        return interceptor;
    }

    /**
     * nicht sicher ob so korrekt implementiert.
     *
     * @return open session
     */
    public static boolean hasOpenSession() {
        Session session = threadSession.get();

        return session != null;
    }
}
