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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import org.kitodo.data.database.persistence.HibernateUtilOld;

//TODO: split this class! here should be only parts of Helper which are needed for Beans and Persistence
public class Helper implements Serializable {

	/**
	 * Always treat de-serialization as a full-blown constructor, by validating the final state of the de-serialized object.
	 */
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

	}

	/**
	 * This is the default implementation of writeObject. Customise if necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

	}

	private static final Logger myLogger = Logger.getLogger(Helper.class);
	private static final long serialVersionUID = -7449236652821237059L;

	private String myMetadatenVerzeichnis;
	private String myConfigVerzeichnis;
	private static Map<Locale, ResourceBundle> commonMessages = null;
	private static Map<Locale, ResourceBundle> localMessages = null;

	public static Map<String, String> activeMQReporting = null;
	private static String compoundMessage;

	/**
	 * Ermitteln eines bestimmten Parameters des Requests
	 *
	 * @return Parameter als String
	 */
	@SuppressWarnings("rawtypes")
	public static String getRequestParameter(String Parameter) {
		/* einen bestimmten übergebenen Parameter ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		Map requestParams = context.getExternalContext().getRequestParameterMap();
		String myParameter = (String) requestParams.get(Parameter);
		if (myParameter == null) {
			myParameter = "";
		}
		return myParameter;
	}

	public static String getStacktraceAsString(Exception inException) {
		StringWriter sw = new StringWriter();
		inException.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static String getDateAsFormattedString(Date inDate) {
		if (inDate == null) {
			return "-";
		} else {
			return DateFormat.getDateInstance().format(inDate) + " " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
		}
	}

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
						myLogger.error(e);
					} catch (EvaluationException e) {
						myLogger.error(e);
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
