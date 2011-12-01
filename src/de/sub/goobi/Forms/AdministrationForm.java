package de.sub.goobi.Forms;

//TODO: Use generics.
//TODO: Dont use Iterators, use for loop instead.
//TODO: This need to be refactored into several classes, one for each distict functionality

//import groovy.lang.Binding;
//import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.jobs.JobManager;
import org.goobi.production.flow.jobs.LuceneIndexJob;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.quartz.SchedulerException;

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Regelsatz;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.Persistence.BenutzergruppenDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.Persistence.RegelsatzDAO;
import de.sub.goobi.Persistence.SchrittDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.encryption.DesEncrypter;
import de.sub.goobi.helper.encryption.MD5;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class AdministrationForm implements Serializable {
	private static final long serialVersionUID = 5648439270064158243L;
	private static final Logger myLogger = Logger.getLogger(AdministrationForm.class);
	private String passwort;
	private boolean istPasswortRichtig = false;
	private boolean rusFullExport = false;
	//TODO: Remove this
	//private boolean olmsZaehlung = false;
	//private String myPlugin;

	public final static String DIRECTORY_SUFFIX = "_tif";

	/* =============================================================== */

	/**
	 * Passwort eingeben
	 */
	public String Weiter() {
		passwort = new MD5(passwort).getMD5();
		String adminMd5 = ConfigMain.getParameter("superadminpassword");
		istPasswortRichtig = (passwort.equals(adminMd5));
		if (!istPasswortRichtig)
			Helper.setFehlerMeldung("wrong passworwd", "");
		return "";
	}

	/* =============================================================== */

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	/**
	 * restart quartz timer for scheduled storage calculation, so it notices
	 * chanced start time configuration from configuration
	 */
	public void restartStorageCalculationScheduler() {
		try {
			JobManager.restartTimedJobs();
			Helper.setMeldung("StorageHistoryManager scheduler restarted");
		} catch (SchedulerException e) {
			Helper.setFehlerMeldung("Error while restarting StorageHistoryManager scheduler", e);
		}
	}

	/**
	 * run storage calculation for all processes now
	 */
	public void startStorageCalculationForAllProcessesNow() {
		HistoryAnalyserJob job = new HistoryAnalyserJob();
		if (job.getIsRunning() == false) {
			job.execute();
			Helper.setMeldung("scheduler calculation executed");
		} else {
			Helper.setMeldung("Job is already running, try again in a few minutes");
		}
	}

	public boolean isIstPasswortRichtig() {
		return istPasswortRichtig;
	}
	
	public void createIndex () {
		LuceneIndexJob job = new LuceneIndexJob();
		if (job.getIsRunning() == false) {
			job.execute();
			Helper.setMeldung("lucene indexer executed");
		} else {
			Helper.setMeldung("lucene indexer is already running, try again in a few minutes");
		}
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Scripte über
	 * separaten Classloader laden ##
	 * #####################################################
	 * ####################################################
	 */

	/*
	public void startPlugin() {
		ConfigMain conf = new ConfigMain();
		File file = new File(conf.getParameter("pluginFolder", File.separator));
		try {
			URL url = file.toURL();
			URL[] urls = new URL[] { url };

			ClassLoader servletClassLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader cl = new URLClassLoader(urls, servletClassLoader);
			// cl.clearAssertionStatus();
			// cl.setClassAssertionStatus("de.sub.goobi.Plugins." + myPlugin,
			// true);
			Class cls = cl.loadClass("de.sub.goobi.Plugins." + myPlugin);

			// run it
			Object objectParameters[] = { new String[] {} };
			Class classParameters[] = { objectParameters[0].getClass() };
			try {
				Method theMethod = cls.getDeclaredMethod("main", classParameters);
				theMethod.invoke(null, objectParameters);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			// Static method, no instance needed
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			cl = null;
			// GoobiAdminScriptPlugin p = (GoobiAdminScriptPlugin)
			// cls.newInstance();
			// p.start();
			// cl.setClassAssertionStatus("de.sub.goobi.Plugins." + myPlugin,
			// true);
		} catch (MalformedURLException e) {
			Helper.setFehlerMeldung("MalformedURLException: ", e.getMessage());
		} catch (ClassNotFoundException e) {
			Helper.setFehlerMeldung("ClassNotFoundException: ", e.getMessage());
			// } catch (InstantiationException e) {
			// Helper.setFehlerMeldung("InstantiationException: ",
			// e.getMessage());
		} catch (IllegalAccessException e) {
			Helper.setFehlerMeldung("IllegalAccessException: ", e.getMessage());
		}
		Helper.setMeldung("------------------------------------------------------------------");
		Helper.setMeldung("Plugin ausgeführt");
	}
	*/

	/**
	 * Liste der Plugins aus
	 * ================================================================
	 */
	/*
	public List getMyPluginList() {
		ConfigMain conf = new ConfigMain();
		File dir = new File(conf.getParameter("pluginFolder", File.separator));
		// alle Plugins durchlaufen 
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".class"));
			}
		};
		String[] dateien = dir.list(filter);
		if (dateien == null || dateien.length == 0)
			return new ArrayList();

		// Klassen nach Namen sortieren und zurückgeben
		List<String> filesDirs = new ArrayList<String>();
		for (String string : dateien)
			filesDirs.add(string.substring(0, string.indexOf(".class")));
		Collections.sort(filesDirs);
		return filesDirs;
	}
	 */

	/**
	 * Getter und Setter für Plugin
	 * ================================================================
	 */
	/*
	public String getMyPlugin() {
		return myPlugin;
	}

	public void setMyPlugin(String myPlugin) {
		this.myPlugin = myPlugin;
	}
	*/

	/*
	public void GroovyTest() {
		Binding binding = new Binding();
		binding.setVariable("foo", new Integer(2));
		GroovyShell shell = new GroovyShell(binding);

		Object value = shell.evaluate("println 'Hello World!'; x = 123; return foo * 10");
		assert value.equals(new Integer(20));
		assert binding.getVariable("x").equals(new Integer(123));
	}
	*/

	/*
	 * #####################################################
	 * ##################################################### ## ##
	 * ProzesseDurchlaufen ##
	 * #####################################################
	 * ####################################################
	 */

	public void ProzesseDurchlaufen() throws DAOException {
		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		for (Prozess auf : auftraege) {
//			Prozess auf = (Prozess) iter.next();
			dao.save(auf);
		}
		Helper.setMeldung(null, "", "Elements successful counted");
	}

	public void AnzahlenErmitteln() throws DAOException, IOException, InterruptedException, SwapException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		for (Prozess auf : auftraege) {
//			Prozess auf = (Prozess) iter.next();

			try {
				auf.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(auf, CountType.DOCSTRUCT));
				auf.setSortHelperMetadata(zaehlen.getNumberOfUghElements(auf, CountType.METADATA));
				auf.setSortHelperImages(FileUtils.getNumberOfFiles(new File(auf.getImagesOrigDirectory())));
				dao.save(auf);
			} catch (RuntimeException e) {
				myLogger.error("Fehler bei Band: " + auf.getTitel(), e);
			}

			dao.save(auf);
		}
		Helper.setMeldung(null, "", "Elements successful counted");
	}

	//TODO: Remove this
	public void SiciKorr() throws DAOException {
		Benutzergruppe gruppe = new BenutzergruppenDAO().get(Integer.valueOf(15));
		Set<Benutzergruppe> neueGruppen = new HashSet<Benutzergruppe>();
		neueGruppen.add(gruppe);

		SchrittDAO dao = new SchrittDAO();
		//TODO: Try to avoid SQL
		List<Schritt> schritte = dao.search("from Schritt where titel='Automatische Generierung der SICI'");
		for (Schritt auf : schritte) {
//			Schritt auf = (Schritt) iter.next();
			auf.setBenutzergruppen(neueGruppen);
			dao.save(auf);
		}
		Helper.setMeldung(null, "", "Sici erfolgreich korrigiert");
	}

	public void StandardRegelsatzSetzen() throws DAOException {
		Regelsatz mk = new RegelsatzDAO().get(Integer.valueOf(1));

		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		int i = 0;
		for (Prozess auf : auftraege) {
			
			auf.setRegelsatz(mk);
			dao.save(auf);
			myLogger.debug(auf.getId() + " - " + i++ + "von" + auftraege.size());
		}
		Helper.setMeldung(null, "", "Standard-ruleset successful set");
	}

	//TODO: Remove this
	/*
	public void MesskatalogeOrigOrdnerErstellen() throws DAOException {
	//	HibernateUtil.clearSession();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);

		Disjunction dis = Expression.disjunction();
		dis.add(Expression.like("titel", "%PPN525606394%"));
		dis.add(Expression.like("titel", "%PPN525616772%"));
		dis.add(Expression.like("titel", "%PPN525614036%"));
		dis.add(Expression.like("titel", "%PPN525615075%"));
		dis.add(Expression.like("titel", "%PPN525616276%"));
		crit.add(dis);

		int i = 0;
		for (Iterator iter = crit.list().iterator(); iter.hasNext();) {
			Prozess auf = (Prozess) iter.next();

			// Aktion ausführen, bei Fehlern auf offen setzen
			try {
				File ausgang = new File(auf.getImagesTifDirectory());
				File ziel = new File(ausgang.getParent() + File.separator + "orig_" + ausgang.getName());
				CopyFile.copyDirectory(ausgang, ziel);
			} catch (Exception e) {
				Helper.setFehlerMeldung("Fehler bei den Messkatalogen", e);
				return;
			}

			Helper.setMeldung(auf.getId() + " - " + auf.getTitel() + " erledigt");
		}
		Helper.setMeldung(null, "", "MesskatalogeOrigOrdnerErstellen gesetzt");
	}
	*/

	/*
	 * #####################################################
	 * ##################################################### ## ## LDAP testen
	 * ## #####################################################
	 * ####################################################
	 */

//	//TODO: Remove this
//	public void LDAPtest() {
//		Ldap myldap = new Ldap();
//		Helper.setMeldung(null, "", "LDAP-Zugriff erfolgreich: " + myldap.isUserAlreadyExists("enders"));
//	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Passw�rter
	 * verschl�sseln ## #####################################################
	 * ####################################################
	 */

	public void PasswoerterVerschluesseln() {
		try {
			DesEncrypter encrypter = new DesEncrypter();
			BenutzerDAO dao = new BenutzerDAO();
			List<Benutzer> myBenutzer = dao.search("from Benutzer");
			for (Benutzer ben : myBenutzer) {
//				Benutzer ben = (Benutzer) iter.next();
				String passencrypted = encrypter.encrypt(ben.getPasswort());
				ben.setPasswort(passencrypted);
				dao.save(ben);
			}
			Helper.setMeldung(null, "", "passwords successful ciphered");
		} catch (Exception e) {
			Helper.setFehlerMeldung("could not cipher passwords: ", e.getMessage());
		}
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## das
	 * Erstellungsdatum der Prozesse auf den ersten Schritt setzen ##
	 * #####################################################
	 * ####################################################
	 */

	public void ProzesseDatumSetzen() throws DAOException {
		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		for (Prozess auf : auftraege) {
			

			for (Schritt s  : auf.getSchritteList()) {
			
				if (s.getBearbeitungsbeginn() != null) {
					auf.setErstellungsdatum(s.getBearbeitungsbeginn());
					break;
				}
			}
			dao.save(auf);
		}
		Helper.setMeldung(null, "", "created date");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Olms-Online -
	 * Bände anlegen ## #####################################################
	 * ####################################################
	 */
	/*
		public void OlmsOnlineBaendeAnlegen() {
			OlmsOnlineProzesseAnlegen oop = new OlmsOnlineProzesseAnlegen();
			try {
				oop.startAnlegen();
			} catch (Exception e) {
				Helper.setFehlerMeldung(null, "Fehler aufgetreten: ", e.getMessage());
			}
			Helper.setMeldung(null, "", "Baende angelegt");
		}
	*/
	/*
	 * #####################################################
	 * ##################################################### ## ## Imagepfad
	 * korrigieren ## #####################################################
	 * ####################################################
	 */

	@SuppressWarnings("unchecked")
	public void ImagepfadKorrigieren() throws DAOException {
		UghHelper ughhelp = new UghHelper();
		//	HibernateUtil.clearSession();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);

		// crit.createCriteria("projekt", "proj");
		// crit.add(Expression.like("proj.titel", "DigiWunschBuch"));
		//
		// crit.createCriteria("schritte", "steps");
		// crit.add(Expression.and(Expression.eq("steps.reihenfolge", 6),
		// Expression.eq(
		// "steps.bearbeitungsstatus", 1)));
		List<Prozess> auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Prozess p : auftraege) {
			

			if (p.getBenutzerGesperrt() != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
			} else {
				myLogger.debug("Prozess: " + p.getTitel());
				Prefs myPrefs = p.getRegelsatz().getPreferences();
				Fileformat gdzfile;
				try {
					gdzfile = p.readMetadataFile();

					MetadataType mdt = ughhelp.getMetadataType(myPrefs, "pathimagefiles");
					List alleMetadaten = gdzfile.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
					if (alleMetadaten != null && alleMetadaten.size() > 0) {
						Metadata md = (Metadata) alleMetadaten.get(0);
						myLogger.debug(md.getValue());

						// /* --------------------------------
						// * Prozesseigenschaften ermitteln und daraus den
						// Imagepfad errechnen
						// * --------------------------------*/
						// String atsPpnBand =
						// bHelper.WerkstueckEigenschaftErmitteln(p, "ATS")
						// + bHelper.WerkstueckEigenschaftErmitteln(p, "TSL") +
						// "_";
						// String ppn = bHelper.WerkstueckEigenschaftErmitteln(p,
						// "PPN digital");
						// if (!ppn.startsWith("PPN"))
						// ppn = "PPN" + ppn;
						// atsPpnBand += ppn;
						// String bandnummer =
						// bHelper.WerkstueckEigenschaftErmitteln(p, "Band");
						// if (bandnummer != null && bandnummer.length() > 0)
						// atsPpnBand += "_" +
						// bHelper.WerkstueckEigenschaftErmitteln(p, "Band");
						// md.setValue("./" + atsPpnBand + "_tif");
						if (SystemUtils.IS_OS_WINDOWS) {
							md.setValue("file:/" + p.getImagesDirectory() + p.getTitel().trim() + DIRECTORY_SUFFIX);
						} else {
							md.setValue("file://" + p.getImagesDirectory() + p.getTitel().trim() + DIRECTORY_SUFFIX);
						}
						p.writeMetadataFile(gdzfile);
						Helper.setMeldung(null, "", "Image path set: " + p.getTitel() + ": ./" + p.getTitel() + DIRECTORY_SUFFIX);
					} else
						Helper.setMeldung(null, "", "No Image path available: " + p.getTitel());
				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "Image paths set");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## PPNs in
	 * Metadaten durchlaufen und die Bandnummer bei Digizeit ergänzen ##
	 * #####################################################
	 * ####################################################
	 */

	@SuppressWarnings("unchecked")
	public void PPNsKorrigieren() throws DAOException {
		UghHelper ughhelp = new UghHelper();

		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.createCriteria("projekt", "proj");
		crit.add(Restrictions.like("proj.titel", "DigiZeitschriften"));
		// crit.createCriteria("schritte", "steps");
		// crit.add(Expression.and(Expression.eq("steps.reihenfolge", 6),
		// Expression.eq(
		// "steps.bearbeitungsstatus", 1)));
		List<Prozess> auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Prozess p : auftraege) {
		
			if (p.getBenutzerGesperrt() != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
			} else {
				String myBandnr = p.getTitel();
				StringTokenizer tokenizer = new StringTokenizer(p.getTitel(), "_");
				while (tokenizer.hasMoreTokens()) {
					myBandnr = "_" + tokenizer.nextToken();
				}
				Prefs myPrefs = p.getRegelsatz().getPreferences();
				try {
					Fileformat gdzfile = p.readMetadataFile();
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					DocStruct dsFirst = null;
					if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0)
						dsFirst = (DocStruct) dsTop.getAllChildren().get(0);

					MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDDigital");
					MetadataType mdtPpnAnalog = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");
					List<? extends Metadata> alleMetadaten;

					/* digitale PPN korrigieren */
					if (dsFirst != null) {
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md = (Metadata) alleMetadaten.get(0);
							myLogger.debug(md.getValue());
							if (!md.getValue().endsWith(myBandnr)) {
								md.setValue(md.getValue() + myBandnr);
								Helper.setMeldung(null, "PPN digital adjusted: ", p.getTitel());
							}
						}

						/* analoge PPN korrigieren */
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnAnalog);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md1 = (Metadata) alleMetadaten.get(0);
							myLogger.debug(md1.getValue());
							if (!md1.getValue().endsWith(myBandnr)) {
								md1.setValue(md1.getValue() + myBandnr);
								Helper.setMeldung(null, "PPN analog adjusted: ", p.getTitel());
							}
						}
					}

					/* Collections korrigieren */
					List<String> myKollektionenTitel = new ArrayList<String>();
					MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList<Metadata> myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null && dsTop.getAllMetadataByType(coltype).size() != 0) {
						myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Metadata md : myCollections) {
							
								if (myKollektionenTitel.contains(md.getValue()))
									dsTop.removeMetadata(md);
								else
									myKollektionenTitel.add(md.getValue());
							}
						}
					}
					if (dsFirst != null && dsFirst.getAllMetadataByType(coltype) != null) {
						myKollektionenTitel = new ArrayList<String>();
						myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Metadata md : myCollections) {
//								Metadata md = (Metadata) it.next();
								if (myKollektionenTitel.contains(md.getValue()))
									dsFirst.removeMetadata(md);
								else
									myKollektionenTitel.add(md.getValue());
							}
						}
					}

					p.writeMetadataFile(gdzfile);

				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "PPNs adjusted");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## PPNs in
	 * Metadaten durchlaufen und die Bandnummer bei Digizeit ergänzen ##
	 * #####################################################
	 * ####################################################
	 */

	//TODO: Remove this
	@SuppressWarnings("unchecked")
	public static void PPNsFuerStatistischesJahrbuchKorrigieren2() {
		UghHelper ughhelp = new UghHelper();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.add(Restrictions.like("titel", "statjafud%"));
		// crit.add(Expression.like("titel", "statjafud_PPN514401303_1880"));
		/* alle Prozesse durchlaufen */
		List<Prozess> pl = crit.list();
		for (Prozess p : pl) {
		
			if (p.getBenutzerGesperrt() != null) {
				Helper.setFehlerMeldung("metadata locked: " + p.getTitel());
			} else {
				Prefs myPrefs = p.getRegelsatz().getPreferences();
				try {
					Fileformat gdzfile = p.readMetadataFile();
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");

					/* analoge PPN korrigieren */
					if (dsTop != null) {
						List<? extends Metadata> alleMetadaten = dsTop.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							for (Iterator<? extends Metadata> it = alleMetadaten.iterator(); it.hasNext();) {
								Metadata md = (Metadata) it.next();
								if (!md.getValue().startsWith("PPN")) {
									md.setValue("PPN" + md.getValue());
									p.writeMetadataFile(gdzfile);
								}
							}
						}
					}
				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		Helper.setMeldung("------------------------------------------------------------------");
		Helper.setMeldung("PPNs adjusted");
	}

	@SuppressWarnings("unchecked")
	public void PPNsFuerStatistischesJahrbuchKorrigieren() throws DAOException {
		UghHelper ughhelp = new UghHelper();
		BeanHelper bhelp = new BeanHelper();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.createCriteria("projekt", "proj");
		crit.add(Restrictions.like("proj.titel", "UB-MannheimDigizeit"));

		/* alle Prozesse durchlaufen */
		for (Iterator<Prozess> iter = crit.list().iterator(); iter.hasNext();) {
			Prozess p = (Prozess) iter.next();
			if (p.getBenutzerGesperrt() != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
			} else {
				String ppn = bhelp.WerkstueckEigenschaftErmitteln(p, "PPN digital").replace("PPN ", "").replace("PPN", "");
				String jahr = bhelp.ScanvorlagenEigenschaftErmitteln(p, "Bandnummer");
				String ppnAufBandebene = "PPN" + ppn + "_" + jahr;

				Prefs myPrefs = p.getRegelsatz().getPreferences();
				try {
					Fileformat gdzfile = p.readMetadataFile();
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					DocStruct dsFirst = null;
					if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0)
						dsFirst = (DocStruct) dsTop.getAllChildren().get(0);

					MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDDigital");

					/* digitale PPN korrigieren */
					if (dsFirst != null) {
						List<? extends Metadata> alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten == null || alleMetadaten.size() == 0) {
							Metadata md = new Metadata(mdtPpnDigital);
							md.setValue(ppnAufBandebene);
							dsFirst.addMetadata(md);
						}
					}

					/* Collections korrigieren */
					List<String> myKollektionenTitel = new ArrayList<String>();
					MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList<Metadata> myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null) {
						myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
								Metadata md = (Metadata) it.next();
								if (myKollektionenTitel.contains(md.getValue()))
									dsTop.removeMetadata(md);
								else
									myKollektionenTitel.add(md.getValue());
							}
						}
					}
					if (dsFirst != null && dsFirst.getAllMetadataByType(coltype).size() > 0) {
						myKollektionenTitel = new ArrayList<String>();
						myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
								Metadata md = (Metadata) it.next();
								if (myKollektionenTitel.contains(md.getValue()))
									dsFirst.removeMetadata(md);
								else
									myKollektionenTitel.add(md.getValue());
							}
						}
					}

					p.writeMetadataFile(gdzfile);

				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel() + " - " + e.getMessage());
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "PPNs adjusted");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ##
	 * Tiffheader-Feld aus Tifs auslesen (ob mit PixEdit gearbeitet wurde) ##
	 * #####################################################
	 * ####################################################
	 */

	// public void RusDmlBaendeTiffPruefen() throws DAOException, IOException,
	// InterruptedException, SwapException {
	// int counter = 0;
	// /* --------------------------------
	// * erstmal alle Filter
	// * --------------------------------*/
	// FilenameFilter filterTifDateien = new FilenameFilter() {
	// public boolean accept(File dir, String name) {
	// return name.endsWith(".tif");
	// }
	// };
	//
	// Session session = Helper.getHibernateSession();
	// Criteria crit = session.createCriteria(Prozess.class);
	// // crit.add(Expression.like("titel", "%algeilo%"));
	// crit.createCriteria("projekt", "proj");
	// crit.add(Expression.like("proj.titel", "%rusdml%"));
	// List myProzesse = crit.list();
	// for (Iterator iter = myProzesse.iterator(); iter.hasNext();) {
	// Prozess proz = (Prozess) iter.next();
	// File tifOrdner = new File(proz.getImagesTifDirectory());
	// /* --------------------------------
	// * jetzt die Images durchlaufen
	// * --------------------------------*/
	// File[] dateien = tifOrdner.listFiles(filterTifDateien);
	// if (dateien.length != 0) {
	// for (int i = 0; i < 1; i++) {
	// counter++;
	// TiffConverter tiffConv = new TiffConverter(proz.getImagesTifDirectory() +
	// dateien[i].getName());
	// tiffConv.getTiffField(305));
	// String tiffField = tiffConv.getTiffField(305).toLowerCase();
	// if (!tiffField.contains("pixedit") && !tiffField.contains("photoshop"))
	// Helper.setFehlerMeldung("nicht nachbearbeitet: " + proz.getTitel() + " - ",
	// proz.getImagesTifDirectory()
	// + dateien[i].getName());
	// }
	// }
	// }
	// Helper.setMeldung(null, "", counter + " Images durchlaufen");
	// }
	/*
	 * #####################################################
	 * ##################################################### ## ## Getter und
	 * Setter ## #####################################################
	 * ####################################################
	 */

	//TODO: Remove this
	public boolean isRusFullExport() {
		return rusFullExport;
	}

	public void setRusFullExport(boolean rusFullExport) {
		this.rusFullExport = rusFullExport;
	}

}
