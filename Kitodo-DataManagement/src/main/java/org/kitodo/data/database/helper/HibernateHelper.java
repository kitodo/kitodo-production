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

package org.kitodo.data.database.helper;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.ValueBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.persistence.HibernateUtil;

/**
 * Class contains methods needed for beans and persistence.
 */
public class HibernateHelper implements Serializable {

    private static final Logger logger = LogManager.getLogger(HibernateHelper.class);
    private static final long serialVersionUID = -7449236652821237059L;

    /**
     * Always treat de-serialization as a full-blown constructor, by validating
     * the final state of the de-serialized object.
     */
    private void readObject(ObjectInputStream aInputStream) {

    }

    /**
     * This is the default implementation of writeObject. Customise if
     * necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) {

    }

    /**
     * Get Hibernate Session.
     *
     * @return Hibernate Session
     */
    public static Session getHibernateSession() {
        Session session;
        try {
            session = (Session) getManagedBeanValue("#{HibernateSessionLong.session}");
            if (session == null) {
                session = HibernateUtil.getSession();
            }
        } catch (RuntimeException e) {
            session = HibernateUtil.getSession();
        }
        if (!session.isOpen()) {
            session = HibernateUtil.getSession();
        }
        return session;
    }

    /**
     * Get managed bean value.
     *
     * @param expr
     *            String
     * @return managed bean
     */
    public static Object getManagedBeanValue(String expr) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        } else {
            Object value = null;
            Application application = context.getApplication();
            if (application != null) {
                ValueBinding vb = application.createValueBinding(expr);
                if (vb != null) {
                    try {
                        value = vb.getValue(context);
                    } catch (EvaluationException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            return value;
        }
    }
}
