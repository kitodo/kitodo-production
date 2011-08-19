package de.sub.goobi.Forms;

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
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.goobi.production.export.ExportXmlLog;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.goobi.production.flow.statistics.hibernate.UserProcessesFilter;
import org.goobi.production.flow.statistics.hibernate.UserTemplatesFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jdom.transform.XSLTransformException;
import org.jfree.chart.plot.PlotOrientation;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Werkstueckeigenschaft;
import de.sub.goobi.Export.dms.ExportDms;
import de.sub.goobi.Export.download.ExportMets;
import de.sub.goobi.Export.download.ExportPdf;
import de.sub.goobi.Export.download.Multipage;
import de.sub.goobi.Export.download.TiffHeader;
import de.sub.goobi.Persistence.ProjektDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class ProzessverwaltungForm extends BasisForm {
	private static final long serialVersionUID = 2838270843176821134L;
	private static final Logger logger = Logger.getLogger(ProzessverwaltungForm.class);
	private Prozess myProzess = new Prozess();
	private Schritt mySchritt = new Schritt();
	private StatisticsManager statisticsManager;
	private IEvaluableFilter myFilteredDataSource;
	private List<ProcessCounterObject> myAnzahlList;
	private HashMap<String, Integer> myAnzahlSummary;
	private Prozesseigenschaft myProzessEigenschaft;
	private Schritteigenschaft mySchrittEigenschaft;
	private Benutzer myBenutzer;
	private Vorlage myVorlage;
	private Vorlageeigenschaft myVorlageEigenschaft;
	private Werkstueck myWerkstueck;
	private Werkstueckeigenschaft myWerkstueckEigenschaft;
	private Benutzergruppe myBenutzergruppe;
	private ProzessDAO dao = new ProzessDAO();
	private String modusAnzeige = "aktuell";
	private String modusBearbeiten = "";
	private String goobiScript;
	private HashMap<String, Boolean> anzeigeAnpassen;
	private String myNewProcessTitle;
	private String selectedXslt = "";
	private StatisticsRenderingElement myCurrentTable;
	private boolean showClosedProcesses = false;
	private boolean showArchivedProjects = false;

	private boolean showStatistics = false;

	public ProzessverwaltungForm() {
		anzeigeAnpassen = new HashMap<String, Boolean>();
		anzeigeAnpassen.put("lockings", false);
		anzeigeAnpassen.put("swappedOut", false);
		anzeigeAnpassen.put("selectionBoxes", false);
		anzeigeAnpassen.put("processId", false);
		sortierung = "titelAsc";
		/*
		 * Vorgangsdatum generell anzeigen?
		 */
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() != null) {
			anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfVorgangsdatumAnzeigen());
		} else {
			anzeigeAnpassen.put("processDate", false);
		}
	}

	/**
	 * needed for ExtendedSearch
	 * 
	 * @return
	 */
	public boolean getInitialize() {
		return true;
	}

	public String Neu() {
		myProzess = new Prozess();
		return "ProzessverwaltungBearbeiten";
	}

	public String Speichern() {
		/*
		 * wenn der Vorgangstitel geändert wurde, wird dieser geprüft und bei
		 * erfolgreicher Prüfung an allen relevanten Stellen mitgeändert
		 */
		if (myProzess != null && myProzess.getTitel() != null) {
			if (!myProzess.getTitel().equals(myNewProcessTitle)) {
				String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
				if (!myNewProcessTitle.matches(validateRegEx)) {
					modusBearbeiten = "prozess";
					Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
					return "";
				} else {
					/* Prozesseigenschaften */
					for (Prozesseigenschaft pe : myProzess.getEigenschaftenList()) {
						if (pe.getWert().contains(myProzess.getTitel())) {
							pe.setWert(pe.getWert().replaceAll(myProzess.getTitel(), myNewProcessTitle));
						}
					}
					/* Scanvorlageneigenschaften */
					for (Vorlage vl : myProzess.getVorlagenList()) {
						for (Vorlageeigenschaft ve : vl.getEigenschaftenList()) {
							if (ve.getWert().contains(myProzess.getTitel())) {
								ve.setWert(ve.getWert().replaceAll(myProzess.getTitel(), myNewProcessTitle));
							}
						}
					}
					/* Werkstückeigenschaften */
					for (Werkstueck w : myProzess.getWerkstueckeList()) {
						for (Werkstueckeigenschaft we : w.getEigenschaftenList()) {
							if (we.getWert().contains(myProzess.getTitel())) {
								we.setWert(we.getWert().replaceAll(myProzess.getTitel(), myNewProcessTitle));
							}
						}
					}
					/* Vorgangstitel */
					myProzess.setTitel(myNewProcessTitle);

					/* Tiffwriter-Datei löschen */
					GoobiScript gs = new GoobiScript();
					ArrayList<Prozess> pro = new ArrayList<Prozess>();
					pro.add(myProzess);
					gs.deleteTiffHeaderFile(pro);
					gs.updateImagePath(pro);

				}
			}

			try {
				dao.save(myProzess);
			} catch (DAOException e) {
				Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			}
		} else {
			Helper.setFehlerMeldung("titleEmpty");
		}
		return "";
	}

	public String Loeschen() {
		deleteMetadataDirectory();
		try {
			dao.remove(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not delete ", e);
			return "";
		}
		if (modusAnzeige == "vorlagen") {
			return FilterVorlagen();
		} else {
			return FilterAlleStart();
		}
	}

	public String ContentLoeschen() {
		deleteMetadataDirectory();
		Helper.setMeldung("Content deleted");
		return "";
	}

	private void deleteMetadataDirectory() {
		for (Schritt step : myProzess.getSchritteList()) {
			mySchritt = step;
			deleteSymlinksFromUserHomes();
		}
		try {
			Helper.deleteDir(new File(myProzess.getProcessDataDirectory()));
		} catch (Exception e) {
			Helper.setFehlerMeldung("Can not delete metadata directory", e);
		}
	}

	/*
	 * Filter
	 */

	public String FilterAktuelleProzesse() {
		statisticsManager = null;
		myAnzahlList = null;

		try {
			// if (filter.toLowerCase().startsWith("lucene")) {
			// myFilteredDataSource = new LuceneStepFilter();
			// myFilteredDataSource.setFilter(filter.substring("lucene".length()));
			// } else {
			// this filter was implemented in its own class now as
			// IEvaluableFilter

			myFilteredDataSource = new UserProcessesFilter();
			// }
			Criteria crit = myFilteredDataSource.getCriteria();
			if (!showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}
			if (!showArchivedProjects) {
				crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
			}
			sortList(crit, false);
			page = new Page(crit, 0);

		} catch (HibernateException he) {
			Helper.setFehlerMeldung("ProzessverwaltungForm.FilterAktuelleProzesse", he);
			return "";
		}
		modusAnzeige = "aktuell";
		return "ProzessverwaltungAlle";
	}

	public String FilterVorlagen() {
		statisticsManager = null;
		myAnzahlList = null;
		try {
			myFilteredDataSource = new UserTemplatesFilter();
			Criteria crit = myFilteredDataSource.getCriteria();
			if (!showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}

			if (!showArchivedProjects) {
				crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
			}
			sortList(crit, false);
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("ProzessverwaltungForm.FilterVorlagen", he);
			return "";
		}
		modusAnzeige = "vorlagen";
		return "ProzessverwaltungAlle";
	}

	public String NeuenVorgangAnlegen() {
		FilterVorlagen();
		if (page.getTotalResults() == 1) {
			Prozess einziger = (Prozess) page.getListReload().get(0);
			ProzesskopieForm pkf = (ProzesskopieForm) Helper.getManagedBeanValue("#{ProzesskopieForm}");
			pkf.setProzessVorlage(einziger);
			return pkf.Prepare();
		} else {
			return "ProzessverwaltungAlle";
		}
	}

	/**
	 * Anzeige der Sammelbände filtern
	 */
	public String FilterAlleStart() {
		statisticsManager = null;
		myAnzahlList = null;
		/*
		 * Filter für die Auflistung anwenden
		 */
		try {

			// ... Criteria will persist, because it gets passed on to the
			// PageObject
			// but in order to use the extended functions of the
			// UserDefinedFilter
			// for statistics, we will have to hold a reference to the instance
			// of
			// UserDefinedFilter
			// if (filter.toLowerCase().startsWith("lucene")) {
			// myFilteredDataSource = new
			// LuceneFilter(filter.substring("lucene".length()));
			// } else {
			myFilteredDataSource = new UserDefinedFilter(filter);
			// myFilteredDataSource = new UserProjectFilter(14);
			// }

			// set observable to replace helper.setMessage
			myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());

			// // calling the criteria as the result of the filter
			Criteria crit = myFilteredDataSource.getCriteria();
			// myFilteredDataSource = new UserDefinedFilter(getFilter());
			// // set observable to replace helper.setMessage
			// myFilteredDataSource.getObservable().addObserver(
			// new Helper().createObserver());
			//
			// // calling the criteria as the result of the filter
			// Criteria crit = myFilteredDataSource.getCriteria();

			// first manipulation of the created criteria

			/* nur die Vorlagen oder alles */
			if (modusAnzeige.equals("vorlagen")) {
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(true)));
			} else {
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
			}
			/* alle Suchparameter miteinander kombinieren */
			if (!showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}

			if (!showArchivedProjects) {
				crit.createCriteria("projekt", "proj");
				crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
				sortList(crit, false);
			} else {
				/* noch sortieren */
				sortList(crit, true);
			}
			/* Debugging */
			// for (Iterator it = crit.list().iterator(); it.hasNext();) {
			// Prozess p = (Prozess) it.next();
			// }
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return "";
		} catch (NumberFormatException ne) {
			Helper.setFehlerMeldung("Falsche Suchparameter angegeben", ne.getMessage());
			return "";
		} catch (UnsupportedOperationException e) {
			logger.error(e);
		}

		return "ProzessverwaltungAlle";
	}

	private void sortList(Criteria inCrit, boolean addCriteria) {
		Order order = Order.asc("titel");
		if (sortierung.equals("titelAsc")) {
			order = Order.asc("titel");
		}
		if (sortierung.equals("titelDesc")) {
			order = Order.desc("titel");
		}

		if (sortierung.equals("projektAsc")) {
			if (addCriteria) {
				inCrit.createCriteria("projekt", "proj");
			}
			order = Order.asc("proj.titel");
		}

		if (sortierung.equals("projektDesc")) {
			if (addCriteria) {
				inCrit.createCriteria("projekt", "proj");
			}
			order = Order.desc("proj.titel");
		}

		if (sortierung.equals("vorgangsdatumAsc")) {
			order = Order.asc("erstellungsdatum");
		}
		if (sortierung.equals("vorgangsdatumDesc")) {
			order = Order.desc("erstellungsdatum");
		}

		if (sortierung.equals("fortschrittAsc")) {
			order = Order.asc("sortHelperStatus");
		}
		if (sortierung.equals("fortschrittDesc")) {
			order = Order.desc("sortHelperStatus");
		}

		inCrit.addOrder(order);
	}

	/*
	 * Eigenschaften
	 */

	public String ProzessEigenschaftLoeschen() {
		try {
			myProzess.getEigenschaften().remove(myProzessEigenschaft);
			dao.save(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		myProzess.refreshProperties();
		return "";
	}

	public String SchrittEigenschaftLoeschen() {
		try {
			mySchritt.getEigenschaften().remove(mySchrittEigenschaft);
			dao.save(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		mySchritt.refreshProperties();
		return "";
	}

	public String VorlageEigenschaftLoeschen() {
		try {
			myVorlage.getEigenschaften().remove(myVorlageEigenschaft);
			dao.save(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		myVorlage.refreshProperties();
		return "";
	}

	public String WerkstueckEigenschaftLoeschen() {
		try {
			myWerkstueck.getEigenschaften().remove(myWerkstueckEigenschaft);
			dao.save(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		myWerkstueck.refreshProperties();
		return "";
	}

	public String ProzessEigenschaftUebernehmen() {
		myProzess.getEigenschaften().add((Prozesseigenschaft) myProzess.getDisplayProperties().getCurrentProperty().getProperty());
		((Prozesseigenschaft) myProzess.getDisplayProperties().getCurrentProperty().getProperty()).setProzess(myProzess);
		Speichern();
		myProzess.refreshProperties();
		return "";
	}

	public String SchrittEigenschaftUebernehmen() {
		mySchritt.getEigenschaften().add((Schritteigenschaft) mySchritt.getDisplayProperties().getCurrentProperty().getProperty());
		((Schritteigenschaft) mySchritt.getDisplayProperties().getCurrentProperty().getProperty()).setSchritt(mySchritt);
		Speichern();
		mySchritt.refreshProperties();
		return "";
	}

	public String VorlageEigenschaftUebernehmen() {
		myVorlage.getEigenschaften().add((Vorlageeigenschaft) myVorlage.getDisplayProperties().getCurrentProperty().getProperty());
		((Vorlageeigenschaft) myVorlage.getDisplayProperties().getCurrentProperty().getProperty()).setVorlage(myVorlage);
		Speichern();
		myVorlage.refreshProperties();
		return "";
	}

	public String WerkstueckEigenschaftUebernehmen() {
		myWerkstueck.getEigenschaften().add((Werkstueckeigenschaft) myWerkstueck.getDisplayProperties().getCurrentProperty().getProperty());
		((Werkstueckeigenschaft) myWerkstueck.getDisplayProperties().getCurrentProperty().getProperty()).setWerkstueck(myWerkstueck);
		Speichern();
		myWerkstueck.refreshProperties();
		return "";
	}

	/*
	 * Schritte
	 */

	public String SchrittNeu() {
		mySchritt = new Schritt();
		return "ProzessverwaltungBearbeitenSchritt";
	}

	public void SchrittUebernehmen() {
		mySchritt.setEditTypeEnum(StepEditType.ADMIN);
		HelperSchritte.updateEditing(mySchritt);
		myProzess.getSchritte().add(mySchritt);
		mySchritt.setProzess(myProzess);
		Speichern();
	}

	public String SchrittLoeschen() {
		myProzess.getSchritte().remove(mySchritt);
		Speichern();
		deleteSymlinksFromUserHomes();
		return "ProzessverwaltungBearbeiten";
	}

	private void deleteSymlinksFromUserHomes() {
		WebDav myDav = new WebDav();
		/* alle Benutzer */
		for (Benutzer b : mySchritt.getBenutzerList()) {
			try {
				myDav.UploadFromHome(b, mySchritt.getProzess());
			} catch (RuntimeException e) {
			}
		}
		/* alle Benutzergruppen mit ihren Benutzern */
		for (Benutzergruppe bg : mySchritt.getBenutzergruppenList()) {
			for (Benutzer b : bg.getBenutzerList()) {
				try {
					myDav.UploadFromHome(b, mySchritt.getProzess());
				} catch (RuntimeException e) {
				}
			}
		}
	}

	public String BenutzerLoeschen() {
		mySchritt.getBenutzer().remove(myBenutzer);
		Speichern();
		return "";
	}

	public String BenutzergruppeLoeschen() {
		mySchritt.getBenutzergruppen().remove(myBenutzergruppe);
		Speichern();
		return "";
	}

	public String BenutzergruppeHinzufuegen() {
		mySchritt.getBenutzergruppen().add(myBenutzergruppe);
		Speichern();
		return "";
	}

	public String BenutzerHinzufuegen() {
		mySchritt.getBenutzer().add(myBenutzer);
		Speichern();
		return "";
	}

	/*
	 * Vorlagen
	 */

	public String VorlageNeu() {
		myVorlage = new Vorlage();
		myProzess.getVorlagen().add(myVorlage);
		myVorlage.setProzess(myProzess);
		Speichern();
		return "ProzessverwaltungBearbeitenVorlage";
	}

	public String VorlageUebernehmen() {
		myProzess.getVorlagen().add(myVorlage);
		myVorlage.setProzess(myProzess);
		Speichern();
		return "";
	}

	public String VorlageLoeschen() {
		myProzess.getVorlagen().remove(myVorlage);
		Speichern();
		return "ProzessverwaltungBearbeiten";
	}

	/*
	 * werkstücke
	 */

	public String WerkstueckNeu() {
		myWerkstueck = new Werkstueck();
		myProzess.getWerkstuecke().add(myWerkstueck);
		myWerkstueck.setProzess(myProzess);
		Speichern();
		return "ProzessverwaltungBearbeitenWerkstueck";
	}

	public String WerkstueckUebernehmen() {
		myProzess.getWerkstuecke().add(myWerkstueck);
		myWerkstueck.setProzess(myProzess);
		Speichern();
		return "";
	}

	public String WerkstueckLoeschen() {
		myProzess.getWerkstuecke().remove(myWerkstueck);
		Speichern();
		return "ProzessverwaltungBearbeiten";
	}

	/*
	 * Aktionen
	 */

	public void ExportMets() {
		ExportMets export = new ExportMets();
		try {
			export.startExport(myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export METS file for: " + myProzess.getTitel(), e);
			// Helper.setFehlerMeldung(
			// "An error occured while trying to export METS file for: "
			// + myProzess.getTitel(), e);
			logger.error("ExportMETS error", e);
		}
	}

	public void ExportPdf() {
		ExportPdf export = new ExportPdf();
		try {
			export.startExport(myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export PDF file for: " + myProzess.getTitel(), e);
			logger.error("ExportPDF error", e);
		}
	}

	public void ExportDMS() {
		ExportDms export = new ExportDms();
		try {
			export.startExport(myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export to DMS for: " + myProzess.getTitel(), e);
			logger.error("ExportDMS error", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void ExportDMSPage() {
		ExportDms export = new ExportDms();
		Boolean flagError = false;
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			try {
				export.startExport(proz);
			} catch (Exception e) {
				// without this a new exception is thrown, if an exception
				// caught here doesn't have an
				// errorMessage
				String errorMessage;

				if (e.getMessage() != null) {
					errorMessage = e.getMessage();
				} else {
					errorMessage = e.toString();
				}
				Helper.setFehlerMeldung("ExportErrorID" + proz.getId() + ":", errorMessage);
				logger.error(e);
				flagError = true;
			}
		}
		if (flagError) {
			Helper.setFehlerMeldung("ExportFinishedWithErrors");
		} else {
			Helper.setMeldung(null, "ExportFinished", "");
		}
	}

	@SuppressWarnings("unchecked")
	public void ExportDMSSelection() {
		ExportDms export = new ExportDms();
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			if (proz.isSelected()) {
				try {
					export.startExport(proz);
				} catch (Exception e) {
					Helper.setFehlerMeldung("ExportError", e.getMessage());
					logger.error(e);
				}
			}
		}
		Helper.setMeldung(null, "ExportFinished", "");
	}

	@SuppressWarnings("unchecked")
	public void ExportDMSHits() {
		ExportDms export = new ExportDms();
		for (Prozess proz : (List<Prozess>) page.getCompleteList()) {
			try {
				export.startExport(proz);
			} catch (Exception e) {
				Helper.setFehlerMeldung("ExportError", e.getMessage());
				logger.error(e);
			}
		}
		Helper.setMeldung(null, "ExportFinished", "");
	}

	public String UploadFromHomeAlle() {
		WebDav myDav = new WebDav();
		List<String> folder = myDav.UploadFromHomeAlle("fertig/");
		myDav.removeFromHomeAlle(folder, "fertig/");
		Helper.setMeldung(null, "directoryRemovedAll", "fertig/");
		return "";
	}

	public String UploadFromHome() {
		WebDav myDav = new WebDav();
		myDav.UploadFromHome(myProzess);
		Helper.setMeldung(null, "directoryRemoved", myProzess.getTitel());
		return "";
	}

	public void DownloadToHome() {
		/*
		 * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in
		 * Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde,
		 * ansonsten Download
		 */
		if (!myProzess.isImageFolderInUse()) {
			WebDav myDav = new WebDav();
			myDav.DownloadToHome(myProzess, 0, false);
		} else {
			Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + myProzess.getTitel() + " " + Helper.getTranslation("isInUse"),
					myProzess.getImageFolderInUseUser().getNachVorname());
		}
	}

	@SuppressWarnings("unchecked")
	public void DownloadToHomePage() {
		WebDav myDav = new WebDav();
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			/*
			 * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer
			 * in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde,
			 * ansonsten Download
			 */
			if (!proz.isImageFolderInUse()) {
				myDav.DownloadToHome(proz, 0, false);
			} else {
				Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"), proz
						.getImageFolderInUseUser().getNachVorname());
			}
		}
		Helper.setMeldung(null, "createdInUserHome", "");
	}

	@SuppressWarnings("unchecked")
	public void DownloadToHomeSelection() {
		WebDav myDav = new WebDav();
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			if (proz.isSelected()) {
				if (!proz.isImageFolderInUse()) {
					myDav.DownloadToHome(proz, 0, false);
				} else {
					Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"),
							proz.getImageFolderInUseUser().getNachVorname());
				}
			}
		}
		Helper.setMeldung(null, "createdInUserHomeAll", "");
	}

	@SuppressWarnings("unchecked")
	public void DownloadToHomeHits() {
		WebDav myDav = new WebDav();
		for (Prozess proz : (List<Prozess>) page.getCompleteList()) {
			if (!proz.isImageFolderInUse()) {
				myDav.DownloadToHome(proz, 0, false);
			} else {
				Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"), proz
						.getImageFolderInUseUser().getNachVorname());
			}
		}
		Helper.setMeldung(null, "createdInUserHomeAll", "");
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusHochsetzenPage() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			stepStatusUp(proz);
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusHochsetzenSelection() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			if (proz.isSelected()) {
				stepStatusUp(proz);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusHochsetzenHits() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getCompleteList()) {
			stepStatusUp(proz);
		}
	}

	private void stepStatusUp(Prozess proz) throws DAOException {
		for (Schritt step : proz.getSchritteList()) {
			if (step.getBearbeitungsstatusEnum() != StepStatus.DONE) {
				step.setBearbeitungsstatusUp();
				step.setEditTypeEnum(StepEditType.ADMIN);
				if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
					new HelperSchritte().SchrittAbschliessen(step, false);
				} else {
					HelperSchritte.updateEditing(step);
				}
				break;
			}
		}
		dao.save(proz);
	}

	private void debug(String message, List<Schritt> bla) {
		for (Schritt s : bla) {
			logger.warn(message + " " + s.getTitel() + "   " + s.getReihenfolge());
		}
	}

	private void stepStatusDown(Prozess proz) throws DAOException {
		// debug("proz.getSchritteList: ", proz.getSchritteList());

		List<Schritt> tempList = new ArrayList<Schritt>(proz.getSchritteList());
		debug("templist: ", tempList);

		Collections.reverse(tempList);
		debug("reverse: ", tempList);

		for (Schritt step : tempList) {
			// logger.warn(step.getTitel());
			if (proz.getSchritteList().get(0) != step && step.getBearbeitungsstatusEnum() != StepStatus.LOCKED) {
				// logger.error("passt: " + step.getTitel() + "   " +
				// step.getReihenfolge());
				step.setEditTypeEnum(StepEditType.ADMIN);
				HelperSchritte.updateEditing(step);
				step.setBearbeitungsstatusDown();
				break;
			}
		}
		dao.save(proz);
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenPage() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			stepStatusDown(proz);
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenSelection() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getListReload()) {
			if (proz.isSelected()) {
				stepStatusDown(proz);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenHits() throws DAOException {
		for (Prozess proz : (List<Prozess>) page.getCompleteList()) {
			stepStatusDown(proz);
		}
	}

	public void SchrittStatusUp() {
		if (mySchritt.getBearbeitungsstatusEnum() != StepStatus.DONE) {
			mySchritt.setBearbeitungsstatusUp();
			mySchritt.setEditTypeEnum(StepEditType.ADMIN);
			if (mySchritt.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				new HelperSchritte().SchrittAbschliessen(mySchritt, false);
			} else {
				HelperSchritte.updateEditing(mySchritt);
			}
		}
		Speichern();
		deleteSymlinksFromUserHomes();
	}

	public String SchrittStatusDown() {
		mySchritt.setEditTypeEnum(StepEditType.ADMIN);
		HelperSchritte.updateEditing(mySchritt);
		mySchritt.setBearbeitungsstatusDown();
		Speichern();
		deleteSymlinksFromUserHomes();
		return "";
	}

	/*
	 * =======================================================
	 * 
	 * Auswahl mittels Selectboxen
	 * 
	 * ========================================================
	 */

	@SuppressWarnings("unchecked")
	public void SelectionAll() {
		for (Prozess proz : (List<Prozess>) page.getList()) {
			proz.setSelected(true);
		}
	}

	@SuppressWarnings("unchecked")
	public void SelectionNone() {
		for (Prozess proz : (List<Prozess>) page.getList()) {
			proz.setSelected(false);
		}
	}

	/*
	 * Getter und Setter
	 */

	public Prozess getMyProzess() {
		return myProzess;
	}

	public void setMyProzess(Prozess myProzess) {
		this.myProzess = myProzess;
		myNewProcessTitle = myProzess.getTitel();
	}

	public Prozesseigenschaft getMyProzessEigenschaft() {
		return myProzessEigenschaft;
	}

	public void setMyProzessEigenschaft(Prozesseigenschaft myProzessEigenschaft) {
		this.myProzessEigenschaft = myProzessEigenschaft;
	}

	public Schritt getMySchritt() {
		return mySchritt;
	}

	public void setMySchritt(Schritt mySchritt) {
		this.mySchritt = mySchritt;
	}

	public void setMySchrittReload(Schritt mySchritt) {
		// Helper.getHibernateSession();
		this.mySchritt = mySchritt;
	}

	public Schritteigenschaft getMySchrittEigenschaft() {
		return mySchrittEigenschaft;
	}

	public void setMySchrittEigenschaft(Schritteigenschaft mySchrittEigenschaft) {
		// Helper.getHibernateSession();
		this.mySchrittEigenschaft = mySchrittEigenschaft;
	}

	public Vorlage getMyVorlage() {
		return myVorlage;
	}

	public void setMyVorlage(Vorlage myVorlage) {
		this.myVorlage = myVorlage;
	}

	public void setMyVorlageReload(Vorlage myVorlage) {
		// Helper.getHibernateSession();
		this.myVorlage = myVorlage;
	}

	public Vorlageeigenschaft getMyVorlageEigenschaft() {
		return myVorlageEigenschaft;
	}

	public void setMyVorlageEigenschaft(Vorlageeigenschaft myVorlageEigenschaft) {
		// Helper.getHibernateSession();
		this.myVorlageEigenschaft = myVorlageEigenschaft;
	}

	public Werkstueck getMyWerkstueck() {
		return myWerkstueck;
	}

	public void setMyWerkstueck(Werkstueck myWerkstueck) {
		this.myWerkstueck = myWerkstueck;
	}

	public void setMyWerkstueckReload(Werkstueck myWerkstueck) {
		// Helper.getHibernateSession();
		this.myWerkstueck = myWerkstueck;
	}

	public Werkstueckeigenschaft getMyWerkstueckEigenschaft() {
		return myWerkstueckEigenschaft;
	}

	public void setMyWerkstueckEigenschaft(Werkstueckeigenschaft myWerkstueckEigenschaft) {
		// Helper.getHibernateSession();
		this.myWerkstueckEigenschaft = myWerkstueckEigenschaft;
	}

	public String getModusAnzeige() {
		return modusAnzeige;
	}

	public void setModusAnzeige(String modusAnzeige) {
		sortierung = "titelAsc";
		this.modusAnzeige = modusAnzeige;
	}

	public String getModusBearbeiten() {
		return modusBearbeiten;
	}

	public void setModusBearbeiten(String modusBearbeiten) {
		this.modusBearbeiten = modusBearbeiten;
	}

	public String reihenfolgeUp() {
		mySchritt.setReihenfolge(Integer.valueOf(mySchritt.getReihenfolge().intValue() - 1));
		Speichern();
		return Reload();
	}

	public String reihenfolgeDown() {
		mySchritt.setReihenfolge(Integer.valueOf(mySchritt.getReihenfolge().intValue() + 1));
		Speichern();
		return Reload();
	}

	public void setReload(String bla) {
		Reload();
	}

	public String Reload() {
		Helper.getHibernateSession().clear();
		if (mySchritt != null && mySchritt.getId() != null) {
			Helper.getHibernateSession().refresh(mySchritt);
		}
		if (myProzess != null && myProzess.getId() != null) {
			Helper.getHibernateSession().refresh(myProzess);
		}
		return "";
	}

	public Benutzer getMyBenutzer() {
		return myBenutzer;
	}

	public void setMyBenutzer(Benutzer myBenutzer) {
		this.myBenutzer = myBenutzer;
	}

	public Benutzergruppe getMyBenutzergruppe() {
		return myBenutzergruppe;
	}

	public void setMyBenutzergruppe(Benutzergruppe myBenutzergruppe) {
		this.myBenutzergruppe = myBenutzergruppe;
	}

	/*
	 * Zuweisung der Projekte
	 */

	public Integer getProjektAuswahl() {
		if (myProzess.getProjekt() != null) {
			return myProzess.getProjekt().getId();
		} else {
			return Integer.valueOf(0);
		}
	}

	public void setProjektAuswahl(Integer inProjektAuswahl) {
		if (inProjektAuswahl.intValue() != 0) {
			try {
				myProzess.setProjekt(new ProjektDAO().get(inProjektAuswahl));
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Projekt kann nicht zugewiesen werden", "");
				logger.error(e);
			}
		}
	}

	public List<SelectItem> getProjektAuswahlListe() throws DAOException {
		List<SelectItem> myProjekte = new ArrayList<SelectItem>();
		List<Projekt> temp = new ProjektDAO().search("from Projekt ORDER BY titel");
		for (Projekt proj : temp) {
			myProjekte.add(new SelectItem(proj.getId(), proj.getTitel(), null));
		}
		return myProjekte;
	}

	/*
	 * Anzahlen der Artikel und Images
	 */

	@SuppressWarnings("unchecked")
	public void CalcMetadataAndImagesPage() throws IOException, InterruptedException, SwapException, DAOException {
		CalcMetadataAndImages(page.getListReload());
	}

	@SuppressWarnings("unchecked")
	public void CalcMetadataAndImagesSelection() throws IOException, InterruptedException, SwapException, DAOException {
		ArrayList<Prozess> auswahl = new ArrayList<Prozess>();
		for (Prozess p : (List<Prozess>) page.getListReload()) {
			if (p.isSelected()) {
				auswahl.add(p);
			}
		}
		CalcMetadataAndImages(auswahl);
	}

	@SuppressWarnings("unchecked")
	public void CalcMetadataAndImagesHits() throws IOException, InterruptedException, SwapException, DAOException {
		CalcMetadataAndImages(page.getCompleteList());
	}

	private void CalcMetadataAndImages(List<Prozess> inListe) throws IOException, InterruptedException, SwapException, DAOException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		myAnzahlList = new ArrayList<ProcessCounterObject>();
		int allMetadata = 0;
		int allDocstructs = 0;
		int allImages = 0;

		int maxImages = 1;
		int maxDocstructs = 1;
		int maxMetadata = 1;

		for (Prozess proz : inListe) {
			int tempImg = proz.getSortHelperImages();
			int tempMetadata = proz.getSortHelperMetadata();
			int tempDocstructs = proz.getSortHelperDocstructs();

			boolean changed = false;

			if (tempMetadata == 0) {
				tempMetadata = zaehlen.getNumberOfUghElements(proz, CountType.METADATA);
				if (tempMetadata > 0) {
					changed = true;
				}
			}
			if (tempDocstructs == 0) {
				tempDocstructs = zaehlen.getNumberOfUghElements(proz, CountType.DOCSTRUCT);
				if (tempDocstructs > 0) {
					changed = true;
				}
			}
			if (tempImg == 0) {
				tempImg = FileUtils.getNumberOfFiles(new File(proz.getImagesOrigDirectory()));
				if (tempImg > 0) {
					changed = true;
				}
			}

			// if there are new values, write them to database
			if (changed) {
				proz.setSortHelperDocstructs(tempDocstructs);
				proz.setSortHelperMetadata(tempMetadata);
				proz.setSortHelperImages(tempImg);
				dao.save(proz);
			}

			ProcessCounterObject pco = new ProcessCounterObject(proz.getTitel(), tempMetadata, tempDocstructs, tempImg);
			myAnzahlList.add(pco);

			if (tempImg > maxImages) {
				maxImages = tempImg;
			}
			if (tempMetadata > maxMetadata) {
				maxMetadata = tempMetadata;
			}
			if (tempDocstructs > maxDocstructs) {
				maxDocstructs = tempDocstructs;
			}

			/* Werte für die Gesamt- und Durchschnittsberechnung festhalten */
			allImages += tempImg;
			allMetadata += tempMetadata;
			allDocstructs += tempDocstructs;
		}

		/* die prozentualen Werte anhand der Maximumwerte ergänzen */
		for (ProcessCounterObject pco : myAnzahlList) {
			pco.setRelImages(pco.getImages() * 100 / maxImages);
			pco.setRelMetadata(pco.getMetadata() * 100 / maxMetadata);
			pco.setRelDocstructs(pco.getDocstructs() * 100 / maxDocstructs);
		}

		/* die Durchschnittsberechnung durchführen */
		int faktor = 1;
		if (myAnzahlList != null && myAnzahlList.size() > 0) {
			faktor = myAnzahlList.size();
		}
		myAnzahlSummary = new HashMap<String, Integer>();
		myAnzahlSummary.put("sumProcesses", faktor);
		myAnzahlSummary.put("sumMetadata", allMetadata);
		myAnzahlSummary.put("sumDocstructs", allDocstructs);
		myAnzahlSummary.put("sumImages", allImages);
		myAnzahlSummary.put("averageImages", allImages / faktor);
		myAnzahlSummary.put("averageMetadata", allMetadata / faktor);
		myAnzahlSummary.put("averageDocstructs", allDocstructs / faktor);
	}

	public HashMap<String, Integer> getMyAnzahlSummary() {
		return myAnzahlSummary;
	}

	public List<ProcessCounterObject> getMyAnzahlList() {
		return myAnzahlList;
	}

	/**
	 * Starte GoobiScript über alle Treffer
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptHits() {
		GoobiScript gs = new GoobiScript();
		gs.execute(page.getCompleteList(), goobiScript);
	}

	/**
	 * Starte GoobiScript über alle Treffer der Seite
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptPage() {
		GoobiScript gs = new GoobiScript();
		gs.execute(page.getListReload(), goobiScript);
	}

	/**
	 * Starte GoobiScript über alle selectierten Treffer
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptSelection() {
		ArrayList<Prozess> auswahl = new ArrayList<Prozess>();
		for (Prozess p : (List<Prozess>) page.getListReload()) {
			if (p.isSelected()) {
				auswahl.add(p);
			}
		}
		GoobiScript gs = new GoobiScript();
		gs.execute(auswahl, goobiScript);
	}

	/*
	 * Statistische Auswertung
	 */

	public void StatisticsStatusVolumes() {
		statisticsManager = new StatisticsManager(StatisticsMode.STATUS_VOLUMES, myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
		statisticsManager.calculate();
	}

	public void StatisticsUsergroups() {
		statisticsManager = new StatisticsManager(StatisticsMode.USERGROUPS, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
		statisticsManager.calculate();
	}

	public void StatisticsRuntimeSteps() {
		statisticsManager = new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS, myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
	}

	public void StatisticsProduction() {
		statisticsManager = new StatisticsManager(StatisticsMode.PRODUCTION, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
	}

	public void StatisticsStorage() {
		statisticsManager = new StatisticsManager(StatisticsMode.STORAGE, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
	}

	public void StatisticsCorrection() {
		statisticsManager = new StatisticsManager(StatisticsMode.CORRECTIONS, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
	}

	public void StatisticsTroughput() {
		statisticsManager = new StatisticsManager(StatisticsMode.THROUGHPUT, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
	}

	public void StatisticsProject() {
		statisticsManager = new StatisticsManager(StatisticsMode.PROJECTS, myFilteredDataSource, FacesContext.getCurrentInstance().getViewRoot()
				.getLocale());
		statisticsManager.calculate();
	}

	/**
	 * ist called via jsp at the end of building a chart in include file
	 * Prozesse_Liste_Statistik.jsp and resets the statistics so that with the
	 * next reload a chart is not shown anymore
	 * 
	 * @author Wulf
	 */
	public String getResetStatistic() {
		// if (!showStatistics) {
		// statisticsManager = null;
		// }
		showStatistics = false;
		return "";
	}

	public String getMyDatasetHoehe() {
		int bla = page.getCompleteList().size() * 20;
		return String.valueOf(bla);
	}

	public int getMyDatasetHoeheInt() {
		int bla = page.getCompleteList().size() * 20;
		return bla;
	}

	public NumberFormat getMyFormatter() {
		return new DecimalFormat("#,##0");
	}

	public PlotOrientation getMyOrientation() {
		return PlotOrientation.HORIZONTAL;
	}

	/*
	 * Downloads
	 */

	public void DownloadTiffHeader() throws IOException {
		TiffHeader tiff = new TiffHeader(myProzess);
		tiff.ExportStart();
	}

	public void DownloadMultiTiff() throws IOException, InterruptedException, SwapException, DAOException {
		Multipage mp = new Multipage();
		mp.ExportStart(myProzess);
	}

	public String getGoobiScript() {
		return goobiScript;
	}

	public void setGoobiScript(String goobiScript) {
		this.goobiScript = goobiScript;
	}

	public HashMap<String, Boolean> getAnzeigeAnpassen() {
		return anzeigeAnpassen;
	}

	public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
		this.anzeigeAnpassen = anzeigeAnpassen;
	}

	public String getMyNewProcessTitle() {
		return myNewProcessTitle;
	}

	public void setMyNewProcessTitle(String myNewProcessTitle) {
		this.myNewProcessTitle = myNewProcessTitle;
	}

	public StatisticsManager getStatisticsManager() {
		return statisticsManager;
	}

	/*************************************************************************************
	 * Getter for showStatistics
	 * 
	 * @return the showStatistics
	 *************************************************************************************/
	public boolean isShowStatistics() {
		return showStatistics;
	}

	/**************************************************************************************
	 * Setter for showStatistics
	 * 
	 * @param showStatistics
	 *            the showStatistics to set
	 **************************************************************************************/
	public void setShowStatistics(boolean showStatistics) {
		this.showStatistics = showStatistics;
	}

	public static class ProcessCounterObject {
		private String title;
		private int metadata;
		private int docstructs;
		private int images;
		private int relImages;
		private int relDocstructs;
		private int relMetadata;

		public ProcessCounterObject(String title, int metadata, int docstructs, int images) {
			super();
			this.title = title;
			this.metadata = metadata;
			this.docstructs = docstructs;
			this.images = images;
		}

		public int getImages() {
			return images;
		}

		public int getMetadata() {
			return metadata;
		}

		public String getTitle() {
			return title;
		}

		public int getDocstructs() {
			return docstructs;
		}

		public int getRelDocstructs() {
			return relDocstructs;
		}

		public int getRelImages() {
			return relImages;
		}

		public int getRelMetadata() {
			return relMetadata;
		}

		public void setRelDocstructs(int relDocstructs) {
			this.relDocstructs = relDocstructs;
		}

		public void setRelImages(int relImages) {
			this.relImages = relImages;
		}

		public void setRelMetadata(int relMetadata) {
			this.relMetadata = relMetadata;
		}
	}

	/**
	 * starts generation of xml logfile for current process
	 */

	public void CreateXML() {
		ExportXmlLog xmlExport = new ExportXmlLog();
		try {
			LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
			String ziel = login.getMyBenutzer().getHomeDir() + myProzess.getTitel() + "_log.xml";
			xmlExport.startExport(myProzess, ziel);
		} catch (IOException e) {
			Helper.setFehlerMeldung("could not write logfile to home directory: ", e);
		} catch (InterruptedException e) {
			Helper.setFehlerMeldung("could not execute command to write logfile to home directory", e);
		}
	}

	/**
	 * transforms xml logfile with given xslt and provides download
	 */
	public void TransformXml() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			String OutputFileName = "export.xml";
			/*
			 * -------------------------------- Vorbereiten der
			 * Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

			ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
			String contentType = servletContext.getMimeType(OutputFileName);
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment;filename=\"" + OutputFileName + "\"");

			response.setContentType("text/xml");

			try {
				ServletOutputStream out = response.getOutputStream();
				ExportXmlLog export = new ExportXmlLog();
				export.startTransformation(out, myProzess, selectedXslt);
				out.flush();
			} catch (ConfigurationException e) {
				Helper.setFehlerMeldung("could not create logfile: ", e);
			} catch (XSLTransformException e) {
				Helper.setFehlerMeldung("could not create transformation: ", e);
			} catch (IOException e) {
				Helper.setFehlerMeldung("could not create transformation: ", e);
			}
			facesContext.responseComplete();
		}
	}

	public String getMyProcessId() {
		return String.valueOf(myProzess.getId());
	}

	public void setMyProcessId(String id) {
		try {
			int myid = new Integer(id);
			myProzess = dao.get(myid);
		} catch (DAOException e) {
			logger.error(e);
		} catch (NumberFormatException e) {
			logger.warn(e);
		}
	}

	public List<String> getXsltList() {
		List<String> answer = new ArrayList<String>();
		File folder = new File("xsltFolder");
		if (folder.isDirectory() && folder.exists()) {
			String[] files = folder.list();

			for (String file : files) {
				if (file.endsWith(".xslt") || file.endsWith(".xsl")) {
					answer.add(file);
				}
			}
		}
		return answer;
	}

	public void setSelectedXslt(String select) {
		selectedXslt = select;
	}

	public String getSelectedXslt() {
		return selectedXslt;
	}

	public String downloadDocket() {
		return myProzess.downloadDocket();
	}

	public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
		this.myCurrentTable = myCurrentTable;
	}

	public StatisticsRenderingElement getMyCurrentTable() {
		return myCurrentTable;
	}

	public void CreateExcel() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 * -------------------------------- Vorbereiten der
			 * Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("export.xls");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
				ServletOutputStream out = response.getOutputStream();
				HSSFWorkbook wb = (HSSFWorkbook) myCurrentTable.getExcelRenderer().getRendering();
				wb.write(out);
				out.flush();
				facesContext.responseComplete();

			} catch (IOException e) {

			}
		}
	}

	public void generateResult() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 * -------------------------------- Vorbereiten der
			 * Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("search.xls");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"search.xls\"");
				ServletOutputStream out = response.getOutputStream();
				SearchResultGeneration sr = new SearchResultGeneration(filter, showClosedProcesses, showArchivedProjects);
				HSSFWorkbook wb = sr.getResult();
				wb.write(out);
				out.flush();
				facesContext.responseComplete();

			} catch (IOException e) {

			}
		}
	}

	public boolean isShowClosedProcesses() {
		return showClosedProcesses;
	}

	public void setShowClosedProcesses(boolean showClosedProcesses) {
		this.showClosedProcesses = showClosedProcesses;
	}

	public void setShowArchivedProjects(boolean showArchivedProjects) {
		this.showArchivedProjects = showArchivedProjects;
	}

	public boolean isShowArchivedProjects() {
		return showArchivedProjects;
	}

}
