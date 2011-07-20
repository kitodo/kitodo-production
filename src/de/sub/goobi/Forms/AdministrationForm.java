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

import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryJob;
import org.goobi.production.flow.jobs.JobManager;
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
import de.sub.goobi.helper.ldap.Ldap;

public class AdministrationForm implements Serializable {
	private static final long serialVersionUID = 5648439270064158243L;
	private static final Logger myLogger = Logger.getLogger(AdministrationForm.class);
	private String passwort;
	private boolean istPasswortRichtig = false;
	private boolean rusFullExport = false;
	//TODO: Remove this
	//private boolean olmsZaehlung = false;
	private Helper help = new Helper();
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
			help.setFehlerMeldung("Falsches Passwort", "");
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
			help.setMeldung("StorageHistoryManager scheduler restarted");
		} catch (SchedulerException e) {
			help.setFehlerMeldung("Error while restarting StorageHistoryManager scheduler", e);
		}
	}

	/**
	 * run storage calculation for all processes now
	 */
	public void startStorageCalculationForAllProcessesNow() {
		HistoryJob job = HistoryJob.getInstance();
		if (job.getIsRunning() == false) {
			job.updateHistoryForAllProcesses();
			help.setMeldung("scheduler calculation executed");
		} else {
			help.setMeldung("Job is already running, try again in a few minutes");
		}
	}

	public boolean isIstPasswortRichtig() {
		return istPasswortRichtig;
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
			help.setFehlerMeldung("MalformedURLException: ", e.getMessage());
		} catch (ClassNotFoundException e) {
			help.setFehlerMeldung("ClassNotFoundException: ", e.getMessage());
			// } catch (InstantiationException e) {
			// help.setFehlerMeldung("InstantiationException: ",
			// e.getMessage());
		} catch (IllegalAccessException e) {
			help.setFehlerMeldung("IllegalAccessException: ", e.getMessage());
		}
		help.setMeldung("------------------------------------------------------------------");
		help.setMeldung("Plugin ausgeführt");
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
		List auftraege = dao.search("from Prozess");
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess auf = (Prozess) iter.next();
			dao.save(auf);
		}
		help.setMeldung(null, "", "Artikel erfolgreich gezählt");
	}

	public void AnzahlenErmitteln() throws DAOException, IOException, InterruptedException, SwapException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		ProzessDAO dao = new ProzessDAO();
		List auftraege = dao.search("from Prozess");
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess auf = (Prozess) iter.next();

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
		help.setMeldung(null, "", "Artikel erfolgreich gezählt");
	}

	//TODO: Remove this
	public void SiciKorr() throws DAOException {
		Benutzergruppe gruppe = new BenutzergruppenDAO().get(Integer.valueOf(15));
		Set<Benutzergruppe> neueGruppen = new HashSet<Benutzergruppe>();
		neueGruppen.add(gruppe);

		SchrittDAO dao = new SchrittDAO();
		//TODO: Try to avoid SQL
		List schritte = dao.search("from Schritt where titel='Automatische Generierung der SICI'");
		for (Iterator iter = schritte.iterator(); iter.hasNext();) {
			Schritt auf = (Schritt) iter.next();
			auf.setBenutzergruppen(neueGruppen);
			dao.save(auf);
		}
		help.setMeldung(null, "", "Sici erfolgreich korrigiert");
	}

	public void StandardRegelsatzSetzen() throws DAOException {
		Regelsatz mk = new RegelsatzDAO().get(Integer.valueOf(1));

		ProzessDAO dao = new ProzessDAO();
		List auftraege = dao.search("from Prozess");
		int i = 0;
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess auf = (Prozess) iter.next();
			auf.setRegelsatz(mk);
			dao.save(auf);
			myLogger.debug(auf.getId() + " - " + i++ + "von" + auftraege.size());
		}
		help.setMeldung(null, "", "Standard-Regelsatz erfolgreich gesetzt");
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
				help.setFehlerMeldung("Fehler bei den Messkatalogen", e);
				return;
			}

			help.setMeldung(auf.getId() + " - " + auf.getTitel() + " erledigt");
		}
		help.setMeldung(null, "", "MesskatalogeOrigOrdnerErstellen gesetzt");
	}
	*/

	/*
	 * #####################################################
	 * ##################################################### ## ## LDAP testen
	 * ## #####################################################
	 * ####################################################
	 */

	//TODO: Remove this
	public void LDAPtest() {
		Ldap myldap = new Ldap();
		help.setMeldung(null, "", "LDAP-Zugriff erfolgreich: " + myldap.isUserAlreadyExists("enders"));
	}

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
			List myBenutzer = dao.search("from Benutzer");
			for (Iterator iter = myBenutzer.iterator(); iter.hasNext();) {
				Benutzer ben = (Benutzer) iter.next();
				String passencrypted = encrypter.encrypt(ben.getPasswort());
				ben.setPasswort(passencrypted);
				dao.save(ben);
			}
			help.setMeldung(null, "", "die Passw�rter wurden erfolgreich verschl�sselt");
		} catch (Exception e) {
			help.setFehlerMeldung("die Passw�rter konnten nicht verschl�sselt werden: ", e.getMessage());
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
		List auftraege = dao.search("from Prozess");
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess auf = (Prozess) iter.next();

			for (Iterator iterator = auf.getSchritteList().iterator(); iterator.hasNext();) {
				Schritt s = (Schritt) iterator.next();
				if (s.getBearbeitungsbeginn() != null) {
					auf.setErstellungsdatum(s.getBearbeitungsbeginn());
					break;
				}
			}
			dao.save(auf);
		}
		help.setMeldung(null, "", "Datum erfolgreich gesetzt");
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
				help.setFehlerMeldung(null, "Fehler aufgetreten: ", e.getMessage());
			}
			help.setMeldung(null, "", "Baende angelegt");
		}
	*/
	/*
	 * #####################################################
	 * ##################################################### ## ## Imagepfad
	 * korrigieren ## #####################################################
	 * ####################################################
	 */

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
		List auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess p = (Prozess) iter.next();

			if (p.getBenutzerGesperrt() != null) {
				help.setFehlerMeldung("Metadaten gesperrt: ", p.getTitel());
			} else {
				help.setMeldung(null, "Metadaten werden verarbeitet: ", p.getTitel());
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
						// bhelp.WerkstueckEigenschaftErmitteln(p, "ATS")
						// + bhelp.WerkstueckEigenschaftErmitteln(p, "TSL") +
						// "_";
						// String ppn = bhelp.WerkstueckEigenschaftErmitteln(p,
						// "PPN digital");
						// if (!ppn.startsWith("PPN"))
						// ppn = "PPN" + ppn;
						// atsPpnBand += ppn;
						// String bandnummer =
						// bhelp.WerkstueckEigenschaftErmitteln(p, "Band");
						// if (bandnummer != null && bandnummer.length() > 0)
						// atsPpnBand += "_" +
						// bhelp.WerkstueckEigenschaftErmitteln(p, "Band");
						// md.setValue("./" + atsPpnBand + "_tif");
						md.setValue("./" + p.getTitel().trim() + DIRECTORY_SUFFIX);
						p.writeMetadataFile(gdzfile);
						help.setMeldung(null, "", "Imagepfad erfolgreich gesetzt: " + p.getTitel() + ": ./" + p.getTitel() + DIRECTORY_SUFFIX);
					} else
						help.setMeldung(null, "", "KEIN Imagepfad vorhanden: " + p.getTitel());
				} catch (ReadException e) {
					help.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					help.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					help.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					help.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					help.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					help.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		help.setMeldung(null, "", "------------------------------------------------------------------");
		help.setMeldung(null, "", "Imagepfade gesetzt");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## PPNs in
	 * Metadaten durchlaufen und die Bandnummer bei Digizeit ergänzen ##
	 * #####################################################
	 * ####################################################
	 */

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
		List auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Iterator iter = auftraege.iterator(); iter.hasNext();) {
			Prozess p = (Prozess) iter.next();
			if (p.getBenutzerGesperrt() != null) {
				help.setFehlerMeldung("Metadaten gesperrt: ", p.getTitel());
			} else {
				String myBandnr = p.getTitel();
				StringTokenizer tokenizer = new StringTokenizer(p.getTitel(), "_");
				while (tokenizer.hasMoreTokens()) {
					myBandnr = "_" + tokenizer.nextToken();
				}
				help.setMeldung(null, "Metadaten werden verarbeitet: ", p.getTitel() + " mit: " + myBandnr);
				Prefs myPrefs = p.getRegelsatz().getPreferences();
				try {
					Fileformat gdzfile = p.readMetadataFile();
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					DocStruct dsFirst = null;
					if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0)
						dsFirst = (DocStruct) dsTop.getAllChildren().get(0);

					MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDDigital");
					MetadataType mdtPpnAnalog = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");
					List alleMetadaten;

					/* digitale PPN korrigieren */
					if (dsFirst != null) {
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md = (Metadata) alleMetadaten.get(0);
							myLogger.debug(md.getValue());
							if (!md.getValue().endsWith(myBandnr)) {
								md.setValue(md.getValue() + myBandnr);
								help.setMeldung(null, "PPN digital korrigiert: ", p.getTitel());
							}
						}

						/* analoge PPN korrigieren */
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnAnalog);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md1 = (Metadata) alleMetadaten.get(0);
							myLogger.debug(md1.getValue());
							if (!md1.getValue().endsWith(myBandnr)) {
								md1.setValue(md1.getValue() + myBandnr);
								help.setMeldung(null, "PPN analog korrigiert: ", p.getTitel());
							}
						}
					}

					/* Collections korrigieren */
					List<String> myKollektionenTitel = new ArrayList<String>();
					MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null && dsTop.getAllMetadataByType(coltype).size() != 0) {
						myCollections = new ArrayList(dsTop.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator it = myCollections.iterator(); it.hasNext();) {
								Metadata md = (Metadata) it.next();
								if (myKollektionenTitel.contains(md.getValue()))
									dsTop.removeMetadata(md);
								else
									myKollektionenTitel.add(md.getValue());
							}
						}
					}
					if (dsFirst != null && dsFirst.getAllMetadataByType(coltype) != null) {
						myKollektionenTitel = new ArrayList<String>();
						myCollections = new ArrayList(dsFirst.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator it = myCollections.iterator(); it.hasNext();) {
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
					help.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					help.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					help.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					help.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					help.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					help.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		help.setMeldung(null, "", "------------------------------------------------------------------");
		help.setMeldung(null, "", "PPNs korrigiert");
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## PPNs in
	 * Metadaten durchlaufen und die Bandnummer bei Digizeit ergänzen ##
	 * #####################################################
	 * ####################################################
	 */

	//TODO: Remove this
	public static void PPNsFuerStatistischesJahrbuchKorrigieren2() {
		Helper help = new Helper();
		UghHelper ughhelp = new UghHelper();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.add(Restrictions.like("titel", "statjafud%"));
		// crit.add(Expression.like("titel", "statjafud_PPN514401303_1880"));
		/* alle Prozesse durchlaufen */
		for (Iterator iter = crit.list().iterator(); iter.hasNext();) {
			Prozess p = (Prozess) iter.next();
			if (p.getBenutzerGesperrt() != null) {
				help.setFehlerMeldung("Metadaten gesperrt: " + p.getTitel());
			} else {
				help.setMeldung("Metadaten werden verarbeitet: " + p.getTitel());
				Prefs myPrefs = p.getRegelsatz().getPreferences();
				try {
					Fileformat gdzfile = p.readMetadataFile();
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");

					/* analoge PPN korrigieren */
					if (dsTop != null) {
						List alleMetadaten = dsTop.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							for (Iterator it = alleMetadaten.iterator(); it.hasNext();) {
								Metadata md = (Metadata) it.next();
								if (!md.getValue().startsWith("PPN")) {
									md.setValue("PPN" + md.getValue());
									p.writeMetadataFile(gdzfile);
								}
							}
						}
					}
				} catch (ReadException e) {
					help.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					help.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					help.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					help.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel(), e);
				} catch (UghHelperException e) {
					help.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					help.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		help.setMeldung("------------------------------------------------------------------");
		help.setMeldung("PPNs korrigiert");
	}

	public void PPNsFuerStatistischesJahrbuchKorrigieren() throws DAOException {
		UghHelper ughhelp = new UghHelper();
		BeanHelper bhelp = new BeanHelper();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.createCriteria("projekt", "proj");
		crit.add(Restrictions.like("proj.titel", "UB-MannheimDigizeit"));

		/* alle Prozesse durchlaufen */
		for (Iterator iter = crit.list().iterator(); iter.hasNext();) {
			Prozess p = (Prozess) iter.next();
			if (p.getBenutzerGesperrt() != null) {
				help.setFehlerMeldung("Metadaten gesperrt: ", p.getTitel());
			} else {
				String ppn = bhelp.WerkstueckEigenschaftErmitteln(p, "PPN digital").replace("PPN ", "").replace("PPN", "");
				String jahr = bhelp.ScanvorlagenEigenschaftErmitteln(p, "Bandnummer");
				String ppnAufBandebene = "PPN" + ppn + "_" + jahr;

				help.setMeldung(null, "Metadaten werden verarbeitet: ", p.getTitel() + " mit: " + ppnAufBandebene);
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
						List alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten == null || alleMetadaten.size() == 0) {
							Metadata md = new Metadata(mdtPpnDigital);
							md.setValue(ppnAufBandebene);
							dsFirst.addMetadata(md);
						}
					}

					/* Collections korrigieren */
					List<String> myKollektionenTitel = new ArrayList<String>();
					MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null) {
						myCollections = new ArrayList(dsTop.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator it = myCollections.iterator(); it.hasNext();) {
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
						myCollections = new ArrayList(dsFirst.getAllMetadataByType(coltype));
						if (myCollections != null && myCollections.size() > 0) {
							for (Iterator it = myCollections.iterator(); it.hasNext();) {
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
					help.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("ReadException: " + p.getTitel(), e);
				} catch (IOException e) {
					help.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("IOException: " + p.getTitel(), e);
				} catch (InterruptedException e) {
					help.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitel(), e);
				} catch (PreferencesException e) {
					help.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitel() + " - " + e.getMessage());
				} catch (UghHelperException e) {
					help.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitel(), e);
				} catch (Exception e) {
					help.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
					myLogger.error("Exception: " + p.getTitel(), e);
				}
			}
		}
		help.setMeldung(null, "", "------------------------------------------------------------------");
		help.setMeldung(null, "", "PPNs korrigiert");
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
	// // System.out.println("-------------------------- " + proz.getTifPfad());
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
	// // System.out.println(dateien[i].getName() + " - " +
	// tiffConv.getTiffField(305));
	// String tiffField = tiffConv.getTiffField(305).toLowerCase();
	// // System.out.println(tiffField);
	// if (!tiffField.contains("pixedit") && !tiffField.contains("photoshop"))
	// help.setFehlerMeldung("nicht nachbearbeitet: " + proz.getTitel() + " - ",
	// proz.getImagesTifDirectory()
	// + dateien[i].getName());
	// }
	// }
	// }
	// help.setMeldung(null, "", counter + " Images durchlaufen");
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
