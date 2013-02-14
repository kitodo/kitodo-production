/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goobi.mq.WebServiceResult;
import org.hibernate.Session;
import org.jdom.Element;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.enums.ReportLevel;
import de.sub.goobi.persistence.HibernateUtilOld;

//TODO: Check if more method can be made static
public class Helper implements Serializable, Observer {

	private static final Logger myLogger = Logger.getLogger(Helper.class);
	private static final long serialVersionUID = -7449236652821237059L;

	private String myMetadatenVerzeichnis;
	private String myConfigVerzeichnis;
	public static Map<String, String> activeMQReporting = null;

	/**
	 * Ermitteln eines bestimmten Paramters des Requests
	 * 
	 * @return Paramter als String
	 */
	public static String getRequestParameter(String Parameter) {
		/* einen bestimmten übergebenen Parameter ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		// TODO: Use generics
		Map requestParams = context.getExternalContext().getRequestParameterMap();
		String myParameter = (String) requestParams.get(Parameter);
		if (myParameter == null) {
			myParameter = "";
		}
		return myParameter;
	}

	// TODO: Get rid of this - create a API for application properties
	public String getGoobiDataDirectory() {
		if (myMetadatenVerzeichnis == null) {
			myMetadatenVerzeichnis = ConfigMain.getParameter("MetadatenVerzeichnis");
		}
		return myMetadatenVerzeichnis;
	}

	public String getGoobiConfigDirectory() {
		if (myConfigVerzeichnis == null) {
			myConfigVerzeichnis = ConfigMain.getParameter("KonfigurationVerzeichnis");
		}
		return myConfigVerzeichnis;
	}

	public static String getStacktraceAsString(Exception inException) {
		StringWriter sw = new StringWriter();
		inException.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static void setFehlerMeldung(String meldung) {
		setMeldung(null, meldung, "", false);
	}

	public static void setFehlerMeldung(String meldung, String beschreibung) {
		setMeldung(null, meldung, beschreibung, false);
	}

	public static void setFehlerMeldung(String control, String meldung, String beschreibung) {
		setMeldung(control, meldung, beschreibung, false);
	}

	public static void setFehlerMeldung(Exception e) {
		setFehlerMeldung("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
	}

	public static void setFehlerMeldung(String meldung, Exception e) {
		setFehlerMeldung(meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
	}

	public static void setFehlerMeldung(String control, String meldung, Exception e) {
		setFehlerMeldung(control, meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
	}

	private static String getExceptionMessage(Throwable e) {
		String message = e.getMessage();
		if (message == null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			message = sw.toString();
		}
		return message;
	}

	public static void setMeldung(String meldung) {
		setMeldung(null, meldung, "", true);
	}

	public static void setMeldung(String meldung, String beschreibung) {
		setMeldung(null, meldung, beschreibung, true);
	}

	public static void setMeldung(String control, String meldung, String beschreibung) {
		setMeldung(control, meldung, beschreibung, true);
	}

	/**
	 * The method setMeldung() adds an error message for a given control to the
	 * current form.
	 * 
	 * @param control
	 *            Name of control that caused the error or “null” if the error
	 *            was not caused by a control
	 * @param messageKey
	 *            The key of the error message. The method will try to resolve
	 *            the key against its messages file.
	 * @param descriptionKey
	 *            The description key of the error. The method will try to
	 *            resolve the key against its messages file.
	 * @param infoOnly
	 *            Set to false for error messages. Set to true for info
	 *            messages.
	 */
	private static void setMeldung(String control, String messageKey,
			String descriptionKey, boolean infoOnly) {
		FacesContext context = FacesContext.getCurrentInstance();

		String message;
		String description;
		try {
			message = Messages.getString(messageKey);
		} catch (RuntimeException e) {
			message = messageKey;
		}
		try {
			description = Messages.getString(descriptionKey);
		} catch (RuntimeException e) {
			description = descriptionKey;
		}

		message = message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		description = description.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

		String compoundMessage = message.replaceFirst(":\\s*$", "") + ": "
				+ description;

		/* If the Active MQ service is at work, report errors there, too. */
		if (activeMQReporting != null) {
			new WebServiceResult(activeMQReporting.get("queueName"),
					activeMQReporting.get("id"), infoOnly ? ReportLevel.INFO
							: ReportLevel.ERROR, compoundMessage).send();
		}

		if (context != null) {
			context.addMessage(
					control,
					new FacesMessage(infoOnly ? FacesMessage.SEVERITY_INFO
							: FacesMessage.SEVERITY_ERROR, message, description));
		} else { // wenn kein Kontext da ist, dann die Meldungen in Log
			myLogger.log(infoOnly ? Level.INFO : Level.ERROR, compoundMessage);
		}
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
			return context.getApplication().createValueBinding(expr).getValue(context);
		}
	}

	public static Session getHibernateSession() {
		// Fix for Hibernate-Session-Management, old version - START
		Session sess;
		try {
			sess = (Session) getManagedBeanValue("#{HibernateSessionLong.session}");
			if (sess == null) {
				sess = HibernateUtilOld.getSession();
			}
		} catch (Exception e) {
			sess = HibernateUtilOld.getSession();
		}
		return sess;
		// Fix for Hibernate-Session-Management, old version - END
	}

	/**
	 * for easy access of the implemented Interface Observer
	 * 
	 * @return Observer -> can be added to an Observable
	 */
	public Observer createObserver() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (!(arg instanceof String)) {
			Helper.setFehlerMeldung("Usernotification failed by object: '" + arg.toString()
					+ "' which isn't an expected String Object. This error is caused by an implementation of the Observer Interface in Helper");
		} else {
			Helper.setFehlerMeldung((String) arg);
		}
	}

	public static String getBaseUrl() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		String fullpath = req.getRequestURL().toString();
		String servletpath = context.getExternalContext().getRequestServletPath();
		return fullpath.substring(0, fullpath.indexOf(servletpath));
	}

	public static Benutzer getCurrentUser() {
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		return login.getMyBenutzer();
	}

	/**
	 * Copies src file to dst file. If the dst file does not exist, it is created
	 */
	public static void copyFile(File src, File dst) throws IOException {
		myLogger.debug("copy " + src.getCanonicalPath() + " to " + dst.getCanonicalPath());
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all deletions were successful. If a deletion fails, the method stops attempting
	 * to delete and returns false.
	 */
	public static boolean deleteDir(File dir) {
		if (!dir.exists())
			return true;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	/**
	 * Deletes all files and subdirectories under dir. But not the dir itself
	 */
	public static boolean deleteInDir(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
	 */
	// TODO: Replace this method with two, one for the file stuff and another one for the checksum
	public static void copyDirectoryWithCrc32Check(File srcDir, File dstDir, int goobipathlength, Element inRoot) throws IOException {
		if (srcDir.isDirectory()) {
			if (!dstDir.exists()) {
				dstDir.mkdir();
				dstDir.setLastModified(srcDir.lastModified());
			}
			String[] children = srcDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectoryWithCrc32Check(new File(srcDir, children[i]), new File(dstDir, children[i]), goobipathlength, inRoot);
			}
		} else {
			Long crc = CopyFile.start(srcDir, dstDir);
			Element file = new Element("file");
			file.setAttribute("path", srcDir.getAbsolutePath().substring(goobipathlength));
			file.setAttribute("crc32", String.valueOf(crc));
			inRoot.addContent(file);
		}
	}

	public FilenameFilter getFilter() {
		return imageNameFilter;
	}

	FilenameFilter imageNameFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			boolean fileOk = false;
			String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");

			if (name.matches(prefix + "\\.[Tt][Ii][Ff][Ff]?")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[jJ][pP][eE]?[gG]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[jJ][pP][2]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[pP][nN][gG]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[gG][iI][fF]")) {
				fileOk = true;
			}
			return fileOk;
		}
	};
}
