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

import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Current version of HibernateUtil.
 */
public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private static final ThreadLocal<Session> threadSession = new ThreadLocal<>();

    /**
     * Private constructor to hide the implicit public one.
     */
    private HibernateUtil() {

    }

    /**
     * Retrieves the current Session.
     *
     * @return Session
     */
    public static Session getSession() {
        Session session = threadSession.get();

        if (Objects.isNull(session)) {
            SessionFactory sessionFactory = getSessionFactory();
            if (Objects.nonNull(sessionFactory)) {
                session = sessionFactory.openSession();
            }
        } else {
            if (!session.isOpen()) {
                session = sessionFactory.openSession();
            }
        }
        threadSession.set(session);

        return session;
    }

    /**
     * Retrieve current SessionFactory.
     *
     * @return SessionFactory
     */
    private static SessionFactory getSessionFactory() {
        if (Objects.isNull(sessionFactory)) {
            try {
                registry = new StandardServiceRegistryBuilder().configure().build();
                MetadataSources sources = new MetadataSources(registry);
                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (RuntimeException e) {
                shutdown();
                throw new HibernateException(e.getMessage(), e);
            }
        }
        return sessionFactory;
    }

    /**
     * Destroy session.
     */
    private static void shutdown() {
        if (Objects.nonNull(registry)) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
