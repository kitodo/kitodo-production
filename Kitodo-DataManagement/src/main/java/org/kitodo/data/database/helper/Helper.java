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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.persistence.HibernateUtilOld;

// TODO: split this class! here should be only parts of Helper which are needed
// for Beans and Persistence
public class Helper implements Serializable {

    /**
     * Always treat de-serialization as a full-blown constructor, by validating
     * the final state of the de-serialized object.
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

    }

    /**
     * This is the default implementation of writeObject. Customise if
     * necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

    }

    private static final Logger logger = LogManager.getLogger(Helper.class);
    private static final long serialVersionUID = -7449236652821237059L;

    private String myMetadatenVerzeichnis;
    private String myConfigVerzeichnis;
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;

    public static Map<String, String> activeMQReporting = null;
    private static String compoundMessage;

    /**
     * Ermitteln eines bestimmten Parameters des Requests.
     *
     * @return parameter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String parameter) {
        /* einen bestimmten übergebenen parameter ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        Map requestParams = context.getExternalContext().getRequestParameterMap();
        String myParameter = (String) requestParams.get(parameter);
        if (myParameter == null) {
            myParameter = "";
        }
        return myParameter;
    }

    /**
     * Get stack trace as String.
     *
     * @param inException
     *            input exception
     * @return stack traces as string
     */
    public static String getStacktraceAsString(Exception inException) {
        StringWriter sw = new StringWriter();
        inException.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Get Date as formatted String.
     *
     * @param inDate
     *            input date
     * @return date as formatted string
     */
    public static String getDateAsFormattedString(Date inDate) {
        if (inDate == null) {
            return "-";
        } else {
            return DateFormat.getDateInstance().format(inDate) + " "
                    + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
        }
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
                    } catch (PropertyNotFoundException e) {
                        logger.error(e);
                    } catch (EvaluationException e) {
                        logger.error(e);
                    }
                }
            }
            return value;
        }
    }

    /**
     * The procedure removeManagedBean() removes a managed bean from the faces
     * context by name. If nothing such is available, nothing happens.
     *
     * @param name
     *            managed bean to remove
     */
    public static void removeManagedBean(String name) {
        try {
            @SuppressWarnings("rawtypes")
            Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            if (sessionMap.containsKey(name)) {
                sessionMap.remove(name);
            }
        } catch (Exception nothingToDo) {
        }
    }

    /**
     * Get Hibernate Session.
     *
     * @return Hibernate Session
     */
    public static Session getHibernateSession() {
        Session sess;
        try {
            sess = (Session) getManagedBeanValue("#{HibernateSessionLong.session}");
            if (sess == null) {
                sess = HibernateUtilOld.getSession();
            }
        } catch (Exception e) {
            sess = HibernateUtilOld.getSession();
        }
        if (!sess.isOpen()) {
            sess = HibernateUtilOld.getSession();
        }
        return sess;
    }

    /**
     * The function getLastMessage() returns the last message processed to be
     * shown to the user. This is a last resort only to show the user why
     * perhaps something didn’t work if no error message is available otherwise.
     *
     * @return the most recent message created to be shown to the user
     */
    public static String getLastMessage() {
        return compoundMessage;
    }
}
