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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdom.Element;

import de.sub.goobi.Persistence.HibernateUtil;
import de.sub.goobi.Persistence.HibernateUtilOld;
import de.sub.goobi.config.ConfigMain;

//TODO: Check if more method can be made static
public class Helper implements Serializable, Observer {

	// Pictures of the "Helper" also known as Tree-Man
	// From http://monsterbrains.blogspot.com/
	// http://i35.tinypic.com/20jmwes.jpg
	// http://i38.tinypic.com/9jezh5.jpg
	// Comic Reference
	// http://katzundgoldt.de/port_laestiges_serviceunt_1.htm Panel 5
	// References to the original
	// http://upload.wikimedia.org/wikipedia/commons/9/91/Bosch_Jardin_des_delices_detail.jpg
	// http://en.wikipedia.org/wiki/The_Garden_of_Earthly_Delights
	// http://www.abcgallery.com/B/bosch/bosch1.html
	// http://www.mesart.com/artworksps.jsp.que.artist.eq.678.amp.series.eq.4634.shtml
	
	private static final Logger myLogger = Logger.getLogger(Helper.class);
	private static final long serialVersionUID = -7449236652821237059L;

	private String myMetadatenVerzeichnis;
	private String myConfigVerzeichnis;
	static ResourceBundle bundle;
	
	

	/**
	 * Ermitteln eines bestimmten Paramters des Requests
	 * 
	 * @return Paramter als String
	 */
	public String getRequestParameter(String Parameter) {
		/* einen bestimmten übergebenen Parameter ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		//TODO: Use generics
		Map requestParams = context.getExternalContext().getRequestParameterMap();
		String myParameter = (String) requestParams.get(Parameter);
		if (myParameter == null) {
			myParameter = "";
		}
		return myParameter;
	}

	

	public String getGoobiDataDirectory() {
		if (myMetadatenVerzeichnis == null)
			myMetadatenVerzeichnis = ConfigMain.getParameter("MetadatenVerzeichnis");
		return myMetadatenVerzeichnis;
	}

	

	public String getGoobiConfigDirectory() {
		if (myConfigVerzeichnis == null)
			myConfigVerzeichnis = ConfigMain.getParameter("KonfigurationVerzeichnis");
		return myConfigVerzeichnis;
	}


	public String getStacktraceAsString(Exception inException) {
		StringWriter sw = new StringWriter();
		inException.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public void setFehlerMeldung(String meldung) {
		setMeldung(null, meldung, "", false);
	}

	public void setFehlerMeldung(String meldung, String beschreibung) {
		setMeldung(null, meldung, beschreibung, false);
	}

	public void setFehlerMeldung(String control, String meldung, String beschreibung) {
		setMeldung(control, meldung, beschreibung, false);
	}

	public void setFehlerMeldung(Exception e) {
		setFehlerMeldung("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
	}

	public void setFehlerMeldung(String meldung, Exception e) {
		setFehlerMeldung(meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
	}

	public void setFehlerMeldung(String control, String meldung, Exception e) {
		setFehlerMeldung(control, meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
	}

	private String getExceptionMessage(Throwable e) {
		String message = e.getMessage();
		if (message == null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			message = sw.toString();
		}
		return message;
	}

	

	public void setMeldung(String meldung) {
		setMeldung(null, meldung, "", true);
	}

	public void setMeldung(String meldung, String beschreibung) {
		setMeldung(null, meldung, beschreibung, true);
	}

	public void setMeldung(String control, String meldung, String beschreibung) {
		setMeldung(control, meldung, beschreibung, true);
	}

	

	/**
	 * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control
	 * übergeben
	 */
	private void setMeldung(String control, String meldung, String beschreibung, boolean nurInfo) {
		FacesContext context = FacesContext.getCurrentInstance();
		
		if(meldung ==null){
			meldung = "";
		}
		if (beschreibung==null){
			beschreibung ="";
		}
		meldung.replaceAll("<", "&lt;");
		meldung.replaceAll(">", "&gt;");

		beschreibung.replaceAll("<", "&lt;");
		beschreibung.replaceAll(">", "&gt;");
		/* --------------------------------
		 * wenn kein Kontext da ist, dann die Meldungen in Log
		 * --------------------------------*/
		if (context == null) {
			if (nurInfo)
				myLogger.info(meldung + " " + beschreibung);
			else
				myLogger.error(meldung + " " + beschreibung);
			return;
		}
		ResourceBundle bundle = ResourceBundle.getBundle("Messages.messages", context.getViewRoot().getLocale());
		String msg = "";
		String beschr = "";
		try {
			msg = bundle.getString(meldung);
		} catch (RuntimeException e) {
			msg = meldung;
		}
		try {
			beschr = bundle.getString(beschreibung);
		} catch (RuntimeException e) {
			beschr = beschreibung;
		}
		

		
		
		if (nurInfo)
			context.addMessage(control, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, beschr));
		else
			context.addMessage(control, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, beschr));
	}

	

	public static String getDateAsFormattedString(Date inDate) {
		if (inDate == null) {
			return "-";
		} else {
			return SimpleDateFormat.getDateInstance().format(inDate) + " " + SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
		}
	}

	

	//  public static Object getManagedBean(String name) {
	//      FacesContext context = FacesContext.getCurrentInstance();
	//      Object obj = context.getApplication().getVariableResolver().resolveVariable(context, name);
	//      return obj;
	//  }
	public static Object getManagedBeanValue(String expr) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context == null)
			return null;
		else
			return context.getApplication().createValueBinding(expr).getValue(context);
	}

	public static Session getHibernateSession() {
		//TODO: Fix for Hibernate-Session-Management, replaced with older version here
//		Session s;
//		try {
//			s = HibernateUtil.getSessionFactory().getCurrentSession();
//		} catch (HibernateException e) {
//			myLogger.info("cannot get session from context, generate a new session");
////			s = HibernateUtil.getSessionFactory().openSession();
//			s=HibernateUtilOld.getSession();
//		}
//		return s;
		
		// Fix for Hibernate-Session-Management, old version - START
		Session sess;
		try {
			sess = (Session) getManagedBeanValue("#{HibernateSessionLong.session}");
			if (sess == null)
				sess = HibernateUtilOld.getSession();
		} catch (Exception e) {
			sess = HibernateUtilOld.getSession();
		}
		return sess;
		// Fix for Hibernate-Session-Management, old version - END
	}
	 	
	/*#####################################################
	 #####################################################
	 ##                                                                                                                            
	 ##   Helferklassen für kopieren von Verzeichnissen und Dateien                                 
	 ##                                                                                                                   
	 #####################################################
	 ####################################################*/

	/**
	 * simple call of console command without any feedback, error handling or
	 * return value
	 * ================================================================
	 */
	//TODO: Try to make this metod static.
	public void callShell(String command) throws IOException, InterruptedException {
		myLogger.debug("execute Shellcommand callShell: " + command);
		//TODO: Use a ProcessBuilder
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		
	}

	

	/**
	 * Call scripts from console and give back error messages and return value
	 * of the called script
	 * ================================================================
	 */
	//TODO: Try to make this metod static.
	public int callShell2(String command) throws IOException, InterruptedException {
		myLogger.debug("execute Shellcommand callShell2: " + command);
		boolean errorsExist = false;
		if (command == null || command.length() == 0)
			return 1;
		//TODO: Use a process builder
		Process process = Runtime.getRuntime().exec(command);
		Scanner scanner = new Scanner(process.getInputStream());
		while (scanner.hasNextLine()) {
			String myLine = scanner.nextLine();
			setMeldung(myLine);
		}
		//TODO: Will this block the thread if the the called command doesn't eexit
		scanner.close();
		scanner = new Scanner(process.getErrorStream());
		while (scanner.hasNextLine()) {
			errorsExist = true;
			setFehlerMeldung(scanner.nextLine());
		}
		scanner.close();
		int rueckgabe = process.waitFor();
		if (errorsExist)
			return 1;
		else
			return rueckgabe;
	}

	/**
	 * NOCH FEHLERHAFT enhanced call of script with error messages and return
	 * value of script, call it with special encoding
	 * ================================================================
	 */
	//TODO: Remove this method
	/*
	public int callShell3(String command) throws IOException, InterruptedException {
		myLogger.debug("execute Shellcommand callShell3: " + command);
		if (command == null || command.length() == 0)
			return 1;

		StringTokenizer strtok = new StringTokenizer(command, " ");
		Process process = Runtime.getRuntime().exec(strtok.nextToken());

		// set encoding 
		BufferedWriter outCommand = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
		outCommand.write(strtok.hasMoreTokens() ? strtok.nextToken() : "");
		outCommand.flush();

		Scanner scanner = new Scanner(process.getInputStream());
		while (scanner.hasNextLine()) {
			String myLine = scanner.nextLine();
			setMeldung(myLine);
		}
		scanner.close();
		scanner = new Scanner(process.getErrorStream());
		while (scanner.hasNextLine()) {
			setFehlerMeldung(scanner.nextLine());
		}
		scanner.close();
		int rueckgabe = process.waitFor();
		return rueckgabe;
	}
	*/


	//TODO: Move the Stuff below in a class for interaction with a local file system
	

	public void createUserDirectory(String inDirPath, String inUser) throws IOException, InterruptedException {
		/* --------------------------------
		 * Create directory with script 
		 * --------------------------------*/
		String command = ConfigMain.getParameter("script_createDirUserHome") + " ";
		command += inUser + " " + inDirPath;
		callShell(command);
	}

	

	public void createMetaDirectory(String inDirPath) throws IOException, InterruptedException {
		/* --------------------------------
		 * Create directory with script 
		 * --------------------------------*/
		String command = ConfigMain.getParameter("script_createDirMeta") + " ";
		command += inDirPath;
		callShell(command);
	}

	



	

	/**
	 * Copies src file to dst file. If the dst file does not exist, it is
	 * created
	 */
	public static void copyFile(File src, File dst) throws IOException {
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
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 */
	public static  boolean deleteDir(File dir) {
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
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it
	 * will be created.
	 */
	//TODO: Replace this method with two, one for the file stuff and another one for the checksum
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

	/*#####################################################
	 #####################################################
	 ##                                                                                                                            
	 ##                                       Helferklassen für übersetzungen                                 
	 ##                                                                                                                   
	 #####################################################
	 ####################################################*/

	public static void loadLanguageBundle() {
		bundle = ResourceBundle.getBundle("Messages.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
	}

	

	public static String getTranslation(String dbTitel) {
		//running instance of ResourceBundle doesn't respond on user language changes, workaround by instanciating it every time
		//		SprachbundleLaden();
		try {
			String msg = bundle.getString(dbTitel);
			return msg;
		} catch (RuntimeException e) {
			return dbTitel;
		}
	}

	/**
	 *  for easy access of the implemented Interface Observer
	 * @return Observer -> can be added to an Observable
	 */
	public Observer createObserver(){
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if(!(arg instanceof String)){
			this.setFehlerMeldung("Usernotification failed by object: '" + arg.toString() + "' which isn't an expected String Object. This error is caused by an implementation of the Observer Interface in Helper");
		} else{
			this.setFehlerMeldung((String) arg);
		}
	}
	
	public FilenameFilter getFilter() {
		return imageNameFilter;
	}
	
	FilenameFilter imageNameFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			boolean fileOk = false;
			String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");
//			String suffix = ConfigMin.getParameter("ImageSuffix", "\\.[Tt][Ii][Ff][Ff]?");
//			return name.matches(prefix + suffix);
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
