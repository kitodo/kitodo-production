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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.encryption.MD5;
import org.kitodo.data.database.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import org.goobi.io.SafeFile;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.jobs.JobManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.encryption.DesEncrypter;
import org.kitodo.services.ProcessService;
import org.kitodo.services.RulesetService;
import org.kitodo.services.TaskService;
import org.kitodo.services.UserService;
import org.kitodo.services.UserGroupService;

import org.quartz.SchedulerException;

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

public class AdministrationForm implements Serializable {
	private static final long serialVersionUID = 5648439270064158243L;
	private static final Logger myLogger = Logger.getLogger(AdministrationForm.class);
	private String passwort;
	private boolean istPasswortRichtig = false;
	private boolean rusFullExport = false;
	private ProcessService processService = new ProcessService();
	private RulesetService rulesetService = new RulesetService();
	private TaskService taskService = new TaskService();
	private UserService userService = new UserService();
	private UserGroupService userGroupService = new UserGroupService();

	public final static String DIRECTORY_SUFFIX = "_tif";

	/**
	 * Passwort eingeben.
	 */
	public String Weiter() {
		this.passwort = new MD5(this.passwort).getMD5();
		String adminMd5 = ConfigMain.getParameter("superadminpassword");
		this.istPasswortRichtig = (this.passwort.equals(adminMd5));
		if (!this.istPasswortRichtig) {
			Helper.setFehlerMeldung("wrong password", "");
		}
		return "";
	}

	public String getPasswort() {
		return this.passwort;
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
		return this.istPasswortRichtig;
	}
	
	public void createIndex () {
	}


	public void ProzesseDurchlaufen() throws DAOException, IOException {
		List<Process> auftraege = processService.search("from Process");
		for (Process auf : auftraege) {
			processService.save(auf);
		}
		Helper.setMeldung(null, "", "Elements successful counted");
	}

	public void AnzahlenErmitteln() throws DAOException, IOException, InterruptedException, SwapException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		List<Process> auftraege = processService.search("from Process");
		for (Process auf : auftraege) {

			try {
				auf.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(auf, CountType.DOCSTRUCT));
				auf.setSortHelperMetadata(zaehlen.getNumberOfUghElements(auf, CountType.METADATA));
				auf.setSortHelperImages(FileUtils.getNumberOfFiles(new SafeFile(
						processService.getImagesOrigDirectory(true, auf))));
				processService.save(auf);
			} catch (RuntimeException e) {
				myLogger.error("Fehler bei Band: " + auf.getTitle(), e);
			}

			processService.save(auf);
		}
		Helper.setMeldung(null, "", "Elements successful counted");
	}

	//TODO: Remove this
	public void SiciKorr() throws DAOException {
		UserGroup gruppe = userGroupService.find(15);
		List<UserGroup> neueGruppen = new ArrayList<>();
		neueGruppen.add(gruppe);

		//TODO: Try to avoid SQL
		List<Task> schritte = taskService.search("from Task where title='Automatische Generierung der SICI'");
		for (Task auf : schritte) {
			auf.setUserGroups(neueGruppen);
			taskService.save(auf);
		}
		Helper.setMeldung(null, "", "Sici erfolgreich korrigiert");
	}

	public void StandardRegelsatzSetzen() throws DAOException, IOException {
		Ruleset mk = rulesetService.find(1);

		List<Process> auftraege = processService.search("from Process");
		int i = 0;
		for (Process auf : auftraege) {
			
			auf.setRuleset(mk);
			processService.save(auf);
			myLogger.debug(auf.getId() + " - " + i++ + "von" + auftraege.size());
		}
		Helper.setMeldung(null, "", "Standard-ruleset successful set");
	}


	public void PasswoerterVerschluesseln() {
		try {
			DesEncrypter encrypter = new DesEncrypter();
			List<User> myBenutzer = userService.search("from User");
			for (User ben : myBenutzer) {
				String passencrypted = encrypter.encrypt(ben.getPassword());
				ben.setPassword(passencrypted);
				userService.save(ben);
			}
			Helper.setMeldung(null, "", "passwords successful ciphered");
		} catch (Exception e) {
			Helper.setFehlerMeldung("could not cipher passwords: ", e.getMessage());
		}
	}

	public void ProzesseDatumSetzen() throws DAOException, IOException {
		List<Process> auftraege = processService.search("from Process");
		for (Process auf : auftraege) {

			for (Task s  : auf.getTasks()) {
				if (s.getProcessingBegin() != null) {
					auf.setCreationDate(s.getProcessingBegin());
					break;
				}
			}
			processService.save(auf);
		}
		Helper.setMeldung(null, "", "created date");
	}

	@SuppressWarnings("unchecked")
	public void ImagepfadKorrigieren() throws DAOException {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Process.class);

		List<Process> auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Process p : auftraege) {
			if (processService.getBlockedUsers(p) != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitle());
			} else {
				if(myLogger.isDebugEnabled()) {
					myLogger.debug("Process: " + p.getTitle());
				}
				Prefs myPrefs = rulesetService.getPreferences(p.getRuleset());
				Fileformat gdzfile;
				try {
					gdzfile = processService.readMetadataFile(p);

					MetadataType mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
					List<? extends Metadata> alleMetadaten = gdzfile.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
					if (alleMetadaten != null && alleMetadaten.size() > 0) {
						Metadata md = alleMetadaten.get(0);
						myLogger.debug(md.getValue());

						if (SystemUtils.IS_OS_WINDOWS) {
							md.setValue("file:/" + processService.getImagesDirectory(p) + p.getTitle().trim()
									+ DIRECTORY_SUFFIX);
						} else {
							md.setValue("file://" + processService.getImagesDirectory(p) + p.getTitle().trim()
									+ DIRECTORY_SUFFIX);
						}
						processService.writeMetadataFile(gdzfile, p);
						Helper.setMeldung(null, "", "Image path set: " + p.getTitle()
								+ ": ./" + p.getTitle() + DIRECTORY_SUFFIX);
					} else {
						Helper.setMeldung(null, "", "No Image path available: " + p.getTitle());
					}
				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("ReadException: " + p.getTitle(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("IOException: " + p.getTitle(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitle(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitle(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitle(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("Exception: " + p.getTitle(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "Image paths set");
	}

	@SuppressWarnings("unchecked")
	public void PPNsKorrigieren() throws DAOException {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Process.class);
		crit.add(Restrictions.eq("template", Boolean.FALSE));
		crit.createCriteria("project", "proj");
		crit.add(Restrictions.like("proj.titel", "DigiZeitschriften"));

		List<Process> auftraege = crit.list();

		/* alle Prozesse durchlaufen */
		for (Process p : auftraege) {
			if (processService.getBlockedUsers(p) != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitle());
			} else {
				String myBandnr = p.getTitle();
				StringTokenizer tokenizer = new StringTokenizer(p.getTitle(), "_");
				while (tokenizer.hasMoreTokens()) {
					myBandnr = "_" + tokenizer.nextToken();
				}
				Prefs myPrefs = rulesetService.getPreferences(p.getRuleset());
				try {
					Fileformat gdzfile = processService.readMetadataFile(p);
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					DocStruct dsFirst = null;
					if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0) {
						dsFirst = dsTop.getAllChildren().get(0);
					}

					MetadataType mdtPpnDigital = UghHelper.getMetadataType(myPrefs, "CatalogIDDigital");
					MetadataType mdtPpnAnalog = UghHelper.getMetadataType(myPrefs, "CatalogIDSource");
					List<? extends Metadata> alleMetadaten;

					/* digitale PPN korrigieren */
					if (dsFirst != null) {
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md = alleMetadaten.get(0);
							myLogger.debug(md.getValue());
							if (!md.getValue().endsWith(myBandnr)) {
								md.setValue(md.getValue() + myBandnr);
								Helper.setMeldung(null, "PPN digital adjusted: ",
										p.getTitle());
							}
						}

						/* analoge PPN korrigieren */
						alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnAnalog);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							Metadata md1 = alleMetadaten.get(0);
							myLogger.debug(md1.getValue());
							if (!md1.getValue().endsWith(myBandnr)) {
								md1.setValue(md1.getValue() + myBandnr);
								Helper.setMeldung(null, "PPN analog adjusted: ",
										p.getTitle());
							}
						}
					}

					/* Collections korrigieren */
					List<String> myKollektionenTitel = new ArrayList<String>();
					MetadataType coltype = UghHelper.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList<Metadata> myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null && dsTop.getAllMetadataByType(coltype).size() != 0) {
						myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
						if (myCollections.size() > 0) {
							for (Metadata md : myCollections) {
							
								if (myKollektionenTitel.contains(md.getValue())) {
									dsTop.removeMetadata(md);
								} else {
									myKollektionenTitel.add(md.getValue());
								}
							}
						}
					}
					if (dsFirst != null && dsFirst.getAllMetadataByType(coltype) != null) {
						myKollektionenTitel = new ArrayList<String>();
						myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
						if (myCollections.size() > 0) {
							for (Metadata md : myCollections) {
//								Metadata md = (Metadata) it.next();
								if (myKollektionenTitel.contains(md.getValue())) {
									dsFirst.removeMetadata(md);
								} else {
									myKollektionenTitel.add(md.getValue());
								}
							}
						}
					}

					processService.writeMetadataFile(gdzfile, p);

				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("ReadException: " + p.getTitle(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("IOException: " + p.getTitle(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitle(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitle(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitle(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("Exception: " + p.getTitle(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "PPNs adjusted");
	}

	//TODO: Remove this
	@SuppressWarnings("unchecked")
	public static void PPNsFuerStatistischesJahrbuchKorrigieren2() {
		ProcessService processService = new ProcessService();
		RulesetService rulesetService = new RulesetService();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Process.class);
		crit.add(Restrictions.eq("template", Boolean.FALSE));
		crit.add(Restrictions.like("title", "statjafud%"));
		/* alle Prozesse durchlaufen */
		List<Process> pl = crit.list();
		for (Process p : pl) {
			if (processService.getBlockedUsers(p) != null) {
				Helper.setFehlerMeldung("metadata locked: " + p.getTitle());
			} else {
				Prefs myPrefs = rulesetService.getPreferences(p.getRuleset());
				try {
					Fileformat gdzfile = processService.readMetadataFile(p);
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					MetadataType mdtPpnDigital = UghHelper.getMetadataType(myPrefs, "CatalogIDSource");

					/* analoge PPN korrigieren */
					if (dsTop != null) {
						List<? extends Metadata> alleMetadaten = dsTop.getAllMetadataByType(mdtPpnDigital);
						if (alleMetadaten != null && alleMetadaten.size() > 0) {
							for (Iterator<? extends Metadata> it = alleMetadaten.iterator(); it.hasNext();) {
								Metadata md = it.next();
								if (!md.getValue().startsWith("PPN")) {
									md.setValue("PPN" + md.getValue());
									processService.writeMetadataFile(gdzfile, p);
								}
							}
						}
					}
				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("ReadException: " + p.getTitle(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("IOException: " + p.getTitle(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitle(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitle(), e);
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitle(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("Exception: " + p.getTitle(), e);
				}
			}
		}
		Helper.setMeldung("------------------------------------------------------------------");
		Helper.setMeldung("PPNs adjusted");
	}

	@SuppressWarnings("unchecked")
	public void PPNsFuerStatistischesJahrbuchKorrigieren() throws DAOException {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Process.class);
		crit.add(Restrictions.eq("template", Boolean.FALSE));
		crit.createCriteria("project", "proj");
		crit.add(Restrictions.like("proj.titel", "UB-MannheimDigizeit"));

		/* alle Prozesse durchlaufen */
		for (Iterator<Process> iter = crit.list().iterator(); iter.hasNext();) {
			Process p = iter.next();
			if (processService.getBlockedUsers(p) != null) {
				Helper.setFehlerMeldung("metadata locked: ", p.getTitle());
			} else {
				String ppn = BeanHelper.determineWorkpieceProperty(p, "PPN digital").replace("PPN ", "")
						.replace("PPN", "");
				String jahr = BeanHelper.determineScanTemplateProperty(p, "Bandnummer");
				String ppnAufBandebene = "PPN" + ppn + "_" + jahr;

				Prefs myPrefs = rulesetService.getPreferences(p.getRuleset());
				try {
					Fileformat gdzfile = processService.readMetadataFile(p);
					DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
					DocStruct dsFirst = null;
					if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0) {
						dsFirst = dsTop.getAllChildren().get(0);
					}

					MetadataType mdtPpnDigital = UghHelper.getMetadataType(myPrefs, "CatalogIDDigital");

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
					MetadataType coltype = UghHelper.getMetadataType(myPrefs, "singleDigCollection");
					ArrayList<Metadata> myCollections;
					if (dsTop.getAllMetadataByType(coltype) != null) {
						myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
						if (myCollections.size() > 0) {
							for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
								Metadata md = it.next();
								if (myKollektionenTitel.contains(md.getValue())) {
									dsTop.removeMetadata(md);
								} else {
									myKollektionenTitel.add(md.getValue());
								}
							}
						}
					}
					if (dsFirst != null && dsFirst.getAllMetadataByType(coltype).size() > 0) {
						myKollektionenTitel = new ArrayList<String>();
						myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
						if (myCollections.size() > 0) {
							for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
								Metadata md = it.next();
								if (myKollektionenTitel.contains(md.getValue())) {
									dsFirst.removeMetadata(md);
								} else {
									myKollektionenTitel.add(md.getValue());
								}
							}
						}
					}

					processService.writeMetadataFile(gdzfile, p);

				} catch (ReadException e) {
					Helper.setFehlerMeldung("", "ReadException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("ReadException: " + p.getTitle(), e);
				} catch (IOException e) {
					Helper.setFehlerMeldung("", "IOException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("IOException: " + p.getTitle(), e);
				} catch (InterruptedException e) {
					Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("InterruptedException: " + p.getTitle(), e);
				} catch (PreferencesException e) {
					Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("PreferencesException: " + p.getTitle() + " - "
							+ e.getMessage());
				} catch (UghHelperException e) {
					Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("UghHelperException: " + p.getTitle(), e);
				} catch (Exception e) {
					Helper.setFehlerMeldung("", "Exception: " + p.getTitle() + " - "
							+ e.getMessage());
					myLogger.error("Exception: " + p.getTitle(), e);
				}
			}
		}
		Helper.setMeldung(null, "", "------------------------------------------------------------------");
		Helper.setMeldung(null, "", "PPNs adjusted");
	}

	public boolean isRusFullExport() {
		return this.rusFullExport;
	}

	public void setRusFullExport(boolean rusFullExport) {
		this.rusFullExport = rusFullExport;
	}

}
