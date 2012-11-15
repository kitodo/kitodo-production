package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jdom.Element;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.persistence.HibernateSessionLong;
import de.sub.goobi.persistence.HibernateUtilOld;

public class Helper implements Serializable, Observer {

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

	/**
	 * Ermitteln eines bestimmten Paramters des Requests
	 * 
	 * @return Paramter als String
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

	public String getGoobiDataDirectory() {
		if (this.myMetadatenVerzeichnis == null) {
			this.myMetadatenVerzeichnis = ConfigMain.getParameter("MetadatenVerzeichnis");
		}
		return this.myMetadatenVerzeichnis;
	}

	public String getGoobiConfigDirectory() {
		if (this.myConfigVerzeichnis == null) {
			this.myConfigVerzeichnis = ConfigMain.getParameter("KonfigurationVerzeichnis");
		}
		return this.myConfigVerzeichnis;
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
	 * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control übergeben
	 */
	private static void setMeldung(String control, String meldung, String beschreibung, boolean nurInfo) {
		FacesContext context = FacesContext.getCurrentInstance();

		// Never forget: Strings are immutable
		meldung = meldung.replaceAll("<", "&lt;");
		meldung = meldung.replaceAll(">", "&gt;");
		beschreibung = beschreibung.replaceAll("<", "&lt;");
		beschreibung = beschreibung.replaceAll(">", "&gt;");
		/* wenn kein Kontext da ist, dann die Meldungen in Log */
		if (context == null) {
			if (nurInfo) {
				myLogger.info(meldung + " " + beschreibung);
			} else {
				// myLogger.error(meldung + " " + beschreibung);
			}
			return;
		}
		// ResourceBundle bundle = ResourceBundle.getBundle("Messages.messages",
		// context.getViewRoot().getLocale());
		String msg = "";
		String beschr = "";
		Locale language = Locale.ENGLISH;
		SpracheForm sf = (SpracheForm) Helper.getManagedBeanValue("#{SpracheForm}");
		if (sf != null) {
			language = sf.getLocale();
		}

		try {
			msg = getString(language, meldung);
		} catch (RuntimeException e) {
			msg = meldung;
		}
		try {
			beschr = getString(language, beschreibung);
		} catch (RuntimeException e) {
			beschr = beschreibung;
		}

		if (nurInfo) {
			context.addMessage(control, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, beschr));
		} else {
			context.addMessage(control, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, beschr));
		}
	}

	public static String getString(Locale language, String key) {
		if (commonMessages == null || commonMessages.size() <= 1) {
			loadMsgs();
		}

		if (localMessages.containsKey(language)) {
			ResourceBundle languageLocal = localMessages.get(language);
			if (languageLocal.containsKey(key))
				return languageLocal.getString(key);
			String lowKey = key.toLowerCase();
			if (languageLocal.containsKey(lowKey))
				return languageLocal.getString(lowKey);
		}
		try {

			return commonMessages.get(language).getString(key);
		} catch (RuntimeException irrelevant) {
			return key;
		}
	}

	public static String getDateAsFormattedString(Date inDate) {
		if (inDate == null) {
			return "-";
		} else {
			return DateFormat.getDateInstance().format(inDate) + " " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
		}
	}

	// public static Object getManagedBean(String name) {
	// FacesContext context = FacesContext.getCurrentInstance();
	// Object obj =
	// context.getApplication().getVariableResolver().resolveVariable(context,
	// name);
	// return obj;
	// }
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
					value = vb.getValue(context);
				}
			}
			return value;
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

	public static void createNewHibernateSession() {
		HibernateSessionLong hsl = (HibernateSessionLong) getManagedBeanValue("#{HibernateSessionLong}");
		hsl.getNewSession();
	}

	/**
	 * simple call of console command without any feedback, error handling or return value
	 * ================================================================
	 */
	public static void callShell(String command) throws IOException, InterruptedException {
		myLogger.debug("execute Shellcommand callShell: " + command);
		InputStream is = null;
		InputStream es = null;
		OutputStream out = null;

		try {
			myLogger.debug("execute Shellcommand callShell2: " + command);
			if (command == null || command.length() == 0) {
				return;
			}
			Process process = Runtime.getRuntime().exec(command);
			is = process.getInputStream();
			es = process.getErrorStream();
			out = process.getOutputStream();

			process.waitFor();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					is = null;
				}
			}
			if (es != null) {
				try {
					es.close();
				} catch (IOException e) {
					es = null;
				}

			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					out = null;
				}
			}
		}
	}

	/**
	 * Call scripts from console and give back error messages and return value of the called script
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public static Integer callShell2(String command) throws IOException, InterruptedException {
		InputStream is = null;
		InputStream es = null;
		OutputStream out = null;

		try {
			myLogger.debug("execute Shellcommand callShell2: " + command);
			boolean errorsExist = false;
			if (command == null || command.length() == 0) {
				return 1;
			}
			Process process = Runtime.getRuntime().exec(command);
			is = process.getInputStream();
			es = process.getErrorStream();
			out = process.getOutputStream();
			Scanner scanner = new Scanner(is);
			while (scanner.hasNextLine()) {
				String myLine = scanner.nextLine();
				setMeldung(myLine);
			}

			scanner.close();
			scanner = new Scanner(es);
			while (scanner.hasNextLine()) {
				errorsExist = true;
				setFehlerMeldung(scanner.nextLine());
			}
			scanner.close();
			int rueckgabe = process.waitFor();
			if (errorsExist) {
				return 1;
			} else {
				return rueckgabe;
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					is = null;
				}
			}
			if (es != null) {
				try {
					es.close();
				} catch (IOException e) {
					es = null;
				}

			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					out = null;
				}
			}
		}
	}

	public void createUserDirectory(String inDirPath, String inUser) throws IOException, InterruptedException {
		/*
		 * -------------------------------- Create directory with script --------------------------------
		 */
		String command = ConfigMain.getParameter("script_createDirUserHome") + " ";
		command += inUser + " " + inDirPath;
		callShell(command);
	}

	public void createMetaDirectory(String inDirPath) throws IOException, InterruptedException {
		/*
		 * -------------------------------- Create directory with script --------------------------------
		 */
		String command = ConfigMain.getParameter("script_createDirMeta") + " ";
		command += inDirPath;
		callShell(command);
	}

	private static void loadMsgs() {
		commonMessages = new HashMap<Locale, ResourceBundle>();
		localMessages = new HashMap<Locale, ResourceBundle>();
		if (FacesContext.getCurrentInstance() != null) {
			Iterator<Locale> polyglot = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
			while (polyglot.hasNext()) {
				Locale language = polyglot.next();
				commonMessages.put(language, ResourceBundle.getBundle("Messages.messages", language));
				File file = new File(ConfigMain.getParameter("localMessages", "/opt/digiverso/goobi/messages/"));
				if (file.exists()) {
					// Load local message bundle from file system only if file exists;
					// if value not exists in bundle, use default bundle from classpath

					try {
						final URL resourceURL = file.toURI().toURL();
						URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
							@Override
							public URLClassLoader run() {
								return new URLClassLoader(new URL[] { resourceURL });
							}
						});
						ResourceBundle localBundle = ResourceBundle.getBundle("messages", language, urlLoader);
						if (localBundle != null) {
							localMessages.put(language, localBundle);
						}

					} catch (Exception e) {
					}
				}
			}
		} else {
			Locale defaullLocale = new Locale("EN");
			commonMessages.put(defaullLocale, ResourceBundle.getBundle("Messages.messages", defaullLocale));
		}
	}

	public static String getTranslation(String dbTitel) {
		// running instance of ResourceBundle doesn't respond on user language
		// changes, workaround by instanciating it every time
		// SprachbundleLaden();

		Locale desiredLanguage = null;
		try {
			desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		} catch (NullPointerException skip) {
		}
		if (desiredLanguage != null) {
			return getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
		} else {
			return getString(Locale.ENGLISH, dbTitel);
		}
	}

	public static String getTranslation(String dbTitel, List<String> parameterList) {
		String value = "";
		Locale desiredLanguage = null;
		try {
			desiredLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		} catch (NullPointerException skip) {
		}
		if (desiredLanguage != null) {
			value = getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
		} else {
			value = getString(Locale.ENGLISH, dbTitel);
		}
		if (parameterList != null && parameterList.size() > 0) {
			int parameterCount = 0;
			for (String parameter : parameterList) {
				value = value.replace("{" + parameterCount + "}", parameter);
				parameterCount++;
			}
		}

		return value;
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
	@Override
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
		if (!dir.exists()) {
			return true;
		}
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

	// WELLCOME
	/**
	 * Deletes all files and subdirectories under dir. But not the dir itself and no metadata files
	 */
	public static boolean deleteDataInDir(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				if (!children[i].endsWith(".xml")) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
	 */

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

	public static FilenameFilter imageNameFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			boolean fileOk = false;
			String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");
			// String suffix = ConfigMin.getParameter("ImageSuffix",
			// "\\.[Tt][Ii][Ff][Ff]?");
			// return name.matches(prefix + suffix);
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
				// } else if (name.matches(prefix + "\\.[pP][dD][fF]")) {
				// fileOk = true;
			}
			return fileOk;
		}
	};

	public static FilenameFilter dataFilter = new FilenameFilter() {

		@Override
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
			} else if (name.matches(prefix + "\\.[pP][dD][fF]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[aA][vV][iI]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[mM][pP][gG]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[mM][pP]4")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[mM][pP]3")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[wW][aA][vV]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[wW][mM][vV]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[fF][lL][vV]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.[oO][gG][gG]")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.docx")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.doc")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.xls")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.xlsx")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.pptx")) {
				fileOk = true;
			} else if (name.matches(prefix + "\\.ppt")) {
				fileOk = true;
			}
			return fileOk;
		}
	};
}
