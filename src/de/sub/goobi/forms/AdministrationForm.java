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

package de.sub.goobi.forms;

//TODO: Use generics.
//TODO: Dont use Iterators, use for loop instead.
//TODO: This need to be refactored into several classes, one for each distict functionality

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import dubious.sub.goobi.helper.encryption.DesEncrypter;
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
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.RegelsatzDAO;
import de.sub.goobi.persistence.SchrittDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
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
	 * ##################################################### ## ##
	 * ProzesseDurchlaufen ##
	 * #####################################################
	 * ####################################################
	 */

	public void ProzesseDurchlaufen() throws DAOException {
		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		for (Prozess auf : auftraege) {
			dao.save(auf);
		}
		Helper.setMeldung(null, "", "Elements successful counted");
	}

	public void AnzahlenErmitteln() throws DAOException, IOException, InterruptedException, SwapException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		ProzessDAO dao = new ProzessDAO();
		List<Prozess> auftraege = dao.search("from Prozess");
		for (Prozess auf : auftraege) {

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

	/*
	 * #####################################################
	 * ##################################################### ## ## Passwörter
	 * verschlüsseln ## #####################################################
	 * ####################################################
	 */

	public void PasswoerterVerschluesseln() {
		try {
			DesEncrypter encrypter = new DesEncrypter();
			BenutzerDAO dao = new BenutzerDAO();
			List<Benutzer> myBenutzer = dao.search("from Benutzer");
			for (Benutzer ben : myBenutzer) {
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
	 * ##################################################### ## ## Imagepfad
	 * korrigieren ## #####################################################
	 * ####################################################
	 */

	@SuppressWarnings("unchecked")
	public void ImagepfadKorrigieren() throws DAOException {
		UghHelper ughhelp = new UghHelper();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);

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
