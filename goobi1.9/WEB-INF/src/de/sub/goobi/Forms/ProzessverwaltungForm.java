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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.goobi.production.export.ExportXmlLog;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.goobi.production.flow.statistics.hibernate.UserProcessesFilter;
import org.goobi.production.flow.statistics.hibernate.UserTemplatesFilter;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jdom.transform.XSLTransformException;
import org.jfree.chart.plot.PlotOrientation;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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
import de.sub.goobi.helper.PropertyListObject;
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
	private List<ProcessProperty> processPropertyList;
	private ProcessProperty processProperty;
	private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
	private Integer container;

	private boolean showStatistics = false;

	private static String DONEDIRECTORYNAME = "fertig/";

	public ProzessverwaltungForm() {
		this.anzeigeAnpassen = new HashMap<String, Boolean>();
		this.anzeigeAnpassen.put("lockings", false);
		this.anzeigeAnpassen.put("swappedOut", false);
		this.anzeigeAnpassen.put("selectionBoxes", false);
		this.anzeigeAnpassen.put("processId", false);
		this.anzeigeAnpassen.put("batchId", false);
		this.sortierung = "titelAsc";
		/*
		 * Vorgangsdatum generell anzeigen?
		 */
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		if (login.getMyBenutzer() != null) {
			this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfVorgangsdatumAnzeigen());
		} else {
			this.anzeigeAnpassen.put("processDate", false);
		}
		DONEDIRECTORYNAME = ConfigMain.getParameter("doneDirectoryName", "fertig/");

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
		this.myProzess = new Prozess();
		this.modusBearbeiten = "prozess";
		return "ProzessverwaltungBearbeiten";
	}

	public String NeuVorlage() {
		this.myProzess = new Prozess();
		this.myProzess.setIstTemplate(true);
		this.modusBearbeiten = "prozess";
		return "ProzessverwaltungBearbeiten";
	}

	public String Speichern() {
		/*
		 * wenn der Vorgangstitel geändert wurde, wird dieser geprüft und bei erfolgreicher Prüfung an allen relevanten Stellen mitgeändert
		 */
		if (this.myProzess != null && this.myProzess.getTitel() != null) {
			if (!this.myProzess.getTitel().equals(this.myNewProcessTitle)) {
				String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
				if (!this.myNewProcessTitle.matches(validateRegEx)) {
					this.modusBearbeiten = "prozess";
					Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
					return "";
				} else {
					/* Prozesseigenschaften */
					for (Prozesseigenschaft pe : this.myProzess.getEigenschaftenList()) {
						if (pe != null && pe.getWert() != null) {
							if (pe.getWert().contains(this.myProzess.getTitel())) {
								pe.setWert(pe.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
							}
						}
					}
					/* Scanvorlageneigenschaften */
					for (Vorlage vl : this.myProzess.getVorlagenList()) {
						for (Vorlageeigenschaft ve : vl.getEigenschaftenList()) {
							if (ve.getWert().contains(this.myProzess.getTitel())) {
								ve.setWert(ve.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
							}
						}
					}
					/* Werkstückeigenschaften */
					for (Werkstueck w : this.myProzess.getWerkstueckeList()) {
						for (Werkstueckeigenschaft we : w.getEigenschaftenList()) {
							if (we.getWert().contains(this.myProzess.getTitel())) {
								we.setWert(we.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
							}
						}
					}
					/* Vorgangstitel */
					this.myProzess.setTitel(this.myNewProcessTitle);

					/* Tiffwriter-Datei löschen */
					GoobiScript gs = new GoobiScript();
					ArrayList<Prozess> pro = new ArrayList<Prozess>();
					pro.add(this.myProzess);
					gs.deleteTiffHeaderFile(pro);
					gs.updateImagePath(pro);

				}
			}

			try {
				this.dao.save(this.myProzess);
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
			// Batch b = this.myProzess.getBatch();
			// if (b != null) {
			// b.removeProcessFromBatch(this.myProzess);
			// if (b.getBatchList().isEmpty()) {
			// new BatchDAO().remove(b);
			// }
			// }
			this.dao.remove(this.myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not delete ", e);
			return "";
		}
		if (this.modusAnzeige == "vorlagen") {
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
		for (Schritt step : this.myProzess.getSchritteList()) {
			this.mySchritt = step;
			deleteSymlinksFromUserHomes();
		}
		try {
			Helper.deleteDir(new File(this.myProzess.getProcessDataDirectory()));
			File ocr = new File(this.myProzess.getOcrDirectory());
			if (ocr.exists()) {
				Helper.deleteDir(ocr);
			}
		} catch (Exception e) {
			Helper.setFehlerMeldung("Can not delete metadata directory", e);
		}
	}

	/*
	 * Filter
	 */

	public String FilterAktuelleProzesse() {
		// Helper.createNewHibernateSession();
		this.statisticsManager = null;
		this.myAnzahlList = null;

		try {
			// if (filter.toLowerCase().startsWith("lucene")) {
			// myFilteredDataSource = new LuceneStepFilter();
			// myFilteredDataSource.setFilter(filter.substring("lucene".length()));
			// } else {
			// this filter was implemented in its own class now as
			// IEvaluableFilter

			this.myFilteredDataSource = new UserProcessesFilter();
			// }
			Criteria crit = this.myFilteredDataSource.getCriteria();
			if (!this.showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}
			if (!this.showArchivedProjects) {
				crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
			}
			sortList(crit, false);
			this.page = new Page(crit, 0);

		} catch (HibernateException he) {
			Helper.setFehlerMeldung("ProzessverwaltungForm.FilterAktuelleProzesse", he);
			return "";
		}
		this.modusAnzeige = "aktuell";
		return "ProzessverwaltungAlle";
	}

	public String FilterVorlagen() {
		this.statisticsManager = null;
		this.myAnzahlList = null;
		try {
			this.myFilteredDataSource = new UserTemplatesFilter();
			Criteria crit = this.myFilteredDataSource.getCriteria();
			if (!this.showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}

			if (!this.showArchivedProjects) {
				crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
			}
			sortList(crit, false);
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("ProzessverwaltungForm.FilterVorlagen", he);
			return "";
		}
		this.modusAnzeige = "vorlagen";
		return "ProzessverwaltungAlle";
	}

	public String NeuenVorgangAnlegen() {
		FilterVorlagen();
		if (this.page.getTotalResults() == 1) {
			Prozess einziger = (Prozess) this.page.getListReload().get(0);
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
		// Helper.createNewHibernateSession();
		this.statisticsManager = null;
		this.myAnzahlList = null;
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
			this.myFilteredDataSource = new UserDefinedFilter(this.filter);
			// myFilteredDataSource = new UserProjectFilter(14);
			// }

			// set observable to replace helper.setMessage
			this.myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());

			// // calling the criteria as the result of the filter
			Criteria crit = this.myFilteredDataSource.getCriteria();
			// myFilteredDataSource = new UserDefinedFilter(getFilter());
			// // set observable to replace helper.setMessage
			// myFilteredDataSource.getObservable().addObserver(
			// new Helper().createObserver());
			//
			// // calling the criteria as the result of the filter
			// Criteria crit = myFilteredDataSource.getCriteria();

			// first manipulation of the created criteria

			/* nur die Vorlagen oder alles */
			if (this.modusAnzeige.equals("vorlagen")) {
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(true)));
			} else {
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
			}
			/* alle Suchparameter miteinander kombinieren */
			if (!this.showClosedProcesses) {
				crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
			}

			if (!this.showArchivedProjects) {
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
			this.page = new Page(crit, 0);
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
		if (this.sortierung.equals("titelAsc")) {
			order = Order.asc("titel");
		}
		if (this.sortierung.equals("titelDesc")) {
			order = Order.desc("titel");
		}
		if (this.sortierung.equals("batchAsc")) {
			order = Order.asc("batchID");
		}
		if (this.sortierung.equals("batchDesc")) {
			order = Order.desc("batchID");
		}

		if (this.sortierung.equals("projektAsc")) {
			if (addCriteria) {
				inCrit.createCriteria("projekt", "proj");
			}
			order = Order.asc("proj.titel");
		}

		if (this.sortierung.equals("projektDesc")) {
			if (addCriteria) {
				inCrit.createCriteria("projekt", "proj");
			}
			order = Order.desc("proj.titel");
		}

		if (this.sortierung.equals("vorgangsdatumAsc")) {
			order = Order.asc("erstellungsdatum");
		}
		if (this.sortierung.equals("vorgangsdatumDesc")) {
			order = Order.desc("erstellungsdatum");
		}

		if (this.sortierung.equals("fortschrittAsc")) {
			order = Order.asc("sortHelperStatus");
		}
		if (this.sortierung.equals("fortschrittDesc")) {
			order = Order.desc("sortHelperStatus");
		}

		inCrit.addOrder(order);
	}

	/*
	 * Eigenschaften
	 */

	public String ProzessEigenschaftLoeschen() {
		try {
			this.myProzess.getEigenschaften().remove(this.myProzessEigenschaft);
			this.dao.save(this.myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		this.myProzess.refreshProperties();
		return "";
	}

	public String SchrittEigenschaftLoeschen() {
		try {
			this.mySchritt.getEigenschaften().remove(this.mySchrittEigenschaft);
			this.dao.save(this.myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		this.mySchritt.refreshProperties();
		return "";
	}

	public String VorlageEigenschaftLoeschen() {
		try {
			this.myVorlage.getEigenschaften().remove(this.myVorlageEigenschaft);
			this.dao.save(this.myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		this.myVorlage.refreshProperties();
		return "";
	}

	public String WerkstueckEigenschaftLoeschen() {
		try {
			this.myWerkstueck.getEigenschaften().remove(this.myWerkstueckEigenschaft);
			this.dao.save(this.myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
		}
		this.myWerkstueck.refreshProperties();
		return "";
	}

	public String ProzessEigenschaftUebernehmen() {
		this.myProzess.getEigenschaften().add((Prozesseigenschaft) this.myProzess.getDisplayProperties().getCurrentProperty().getProperty());
		((Prozesseigenschaft) this.myProzess.getDisplayProperties().getCurrentProperty().getProperty()).setProzess(this.myProzess);
		Speichern();
		this.myProzess.refreshProperties();
		return "";
	}

	public String SchrittEigenschaftUebernehmen() {
		this.mySchritt.getEigenschaften().add((Schritteigenschaft) this.mySchritt.getDisplayProperties().getCurrentProperty().getProperty());
		((Schritteigenschaft) this.mySchritt.getDisplayProperties().getCurrentProperty().getProperty()).setSchritt(this.mySchritt);
		Speichern();
		this.mySchritt.refreshProperties();
		return "";
	}

	public String VorlageEigenschaftUebernehmen() {
		this.myVorlage.getEigenschaften().add((Vorlageeigenschaft) this.myVorlage.getDisplayProperties().getCurrentProperty().getProperty());
		((Vorlageeigenschaft) this.myVorlage.getDisplayProperties().getCurrentProperty().getProperty()).setVorlage(this.myVorlage);
		Speichern();
		this.myVorlage.refreshProperties();
		return "";
	}

	public String WerkstueckEigenschaftUebernehmen() {
		this.myWerkstueck.getEigenschaften().add((Werkstueckeigenschaft) this.myWerkstueck.getDisplayProperties().getCurrentProperty().getProperty());
		((Werkstueckeigenschaft) this.myWerkstueck.getDisplayProperties().getCurrentProperty().getProperty()).setWerkstueck(this.myWerkstueck);
		Speichern();
		this.myWerkstueck.refreshProperties();
		return "";
	}

	/*
	 * Schritte
	 */

	public String SchrittNeu() {
		this.mySchritt = new Schritt();
		this.modusBearbeiten = "schritt";
		return "ProzessverwaltungBearbeitenSchritt";
	}

	public void SchrittUebernehmen() {
		this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
		HelperSchritte.updateEditing(this.mySchritt);
		this.myProzess.getSchritte().add(this.mySchritt);
		this.mySchritt.setProzess(this.myProzess);
		Speichern();
	}

	public String SchrittLoeschen() {
		this.myProzess.getSchritte().remove(this.mySchritt);
		Speichern();
		deleteSymlinksFromUserHomes();
		return "ProzessverwaltungBearbeiten";
	}

	private void deleteSymlinksFromUserHomes() {
		WebDav myDav = new WebDav();
		/* alle Benutzer */
		for (Benutzer b : this.mySchritt.getBenutzerList()) {
			try {
				myDav.UploadFromHome(b, this.mySchritt.getProzess());
			} catch (RuntimeException e) {
			}
		}
		/* alle Benutzergruppen mit ihren Benutzern */
		for (Benutzergruppe bg : this.mySchritt.getBenutzergruppenList()) {
			for (Benutzer b : bg.getBenutzerList()) {
				try {
					myDav.UploadFromHome(b, this.mySchritt.getProzess());
				} catch (RuntimeException e) {
				}
			}
		}
	}

	public String BenutzerLoeschen() {
		this.mySchritt.getBenutzer().remove(this.myBenutzer);
		Speichern();
		return "";
	}

	public String BenutzergruppeLoeschen() {
		this.mySchritt.getBenutzergruppen().remove(this.myBenutzergruppe);
		Speichern();
		return "";
	}

	public String BenutzergruppeHinzufuegen() {
		this.mySchritt.getBenutzergruppen().add(this.myBenutzergruppe);
		Speichern();
		return "";
	}

	public String BenutzerHinzufuegen() {
		this.mySchritt.getBenutzer().add(this.myBenutzer);
		Speichern();
		return "";
	}

	/*
	 * Vorlagen
	 */

	public String VorlageNeu() {
		this.myVorlage = new Vorlage();
		this.myProzess.getVorlagen().add(this.myVorlage);
		this.myVorlage.setProzess(this.myProzess);
		Speichern();
		return "ProzessverwaltungBearbeitenVorlage";
	}

	public String VorlageUebernehmen() {
		this.myProzess.getVorlagen().add(this.myVorlage);
		this.myVorlage.setProzess(this.myProzess);
		Speichern();
		return "";
	}

	public String VorlageLoeschen() {
		this.myProzess.getVorlagen().remove(this.myVorlage);
		Speichern();
		return "ProzessverwaltungBearbeiten";
	}

	/*
	 * werkstücke
	 */

	public String WerkstueckNeu() {
		this.myWerkstueck = new Werkstueck();
		this.myProzess.getWerkstuecke().add(this.myWerkstueck);
		this.myWerkstueck.setProzess(this.myProzess);
		Speichern();
		return "ProzessverwaltungBearbeitenWerkstueck";
	}

	public String WerkstueckUebernehmen() {
		this.myProzess.getWerkstuecke().add(this.myWerkstueck);
		this.myWerkstueck.setProzess(this.myProzess);
		Speichern();
		return "";
	}

	public String WerkstueckLoeschen() {
		this.myProzess.getWerkstuecke().remove(this.myWerkstueck);
		Speichern();
		return "ProzessverwaltungBearbeiten";
	}

	/*
	 * Aktionen
	 */

	public void ExportMets() {
		ExportMets export = new ExportMets();
		try {
			export.startExport(this.myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export METS file for: " + this.myProzess.getTitel(), e);
			// Helper.setFehlerMeldung(
			// "An error occured while trying to export METS file for: "
			// + myProzess.getTitel(), e);
			logger.error("ExportMETS error", e);
		}
	}

	public void ExportPdf() {
		ExportPdf export = new ExportPdf();
		try {
			export.startExport(this.myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export PDF file for: " + this.myProzess.getTitel(), e);
			logger.error("ExportPDF error", e);
		}
	}

	public void ExportDMS() {
		ExportDms export = new ExportDms();
		try {
			export.startExport(this.myProzess);
		} catch (Exception e) {
			Helper.setFehlerMeldung("An error occured while trying to export to DMS for: " + this.myProzess.getTitel(), e);
			logger.error("ExportDMS error", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void ExportDMSPage() {
		ExportDms export = new ExportDms();
		Boolean flagError = false;
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
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
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
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
		for (Prozess proz : (List<Prozess>) this.page.getCompleteList()) {
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
		List<String> folder = myDav.UploadFromHomeAlle(DONEDIRECTORYNAME);
		myDav.removeFromHomeAlle(folder, DONEDIRECTORYNAME);
		Helper.setMeldung(null, "directoryRemovedAll", DONEDIRECTORYNAME);
		return "";
	}

	public String UploadFromHome() {
		WebDav myDav = new WebDav();
		myDav.UploadFromHome(this.myProzess);
		Helper.setMeldung(null, "directoryRemoved", this.myProzess.getTitel());
		return "";
	}

	public void DownloadToHome() {
		/*
		 * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde, ansonsten
		 * Download
		 */
		if (!this.myProzess.isImageFolderInUse()) {
			WebDav myDav = new WebDav();
			myDav.DownloadToHome(this.myProzess, 0, false);
		} else {
			Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + this.myProzess.getTitel() + " " + Helper.getTranslation("isInUse"),
					this.myProzess.getImageFolderInUseUser().getNachVorname());
		}
	}

	@SuppressWarnings("unchecked")
	public void DownloadToHomePage() {
		WebDav myDav = new WebDav();
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
			/*
			 * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde,
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
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
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
		for (Prozess proz : (List<Prozess>) this.page.getCompleteList()) {
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
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
			stepStatusUp(proz);
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusHochsetzenSelection() throws DAOException {
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
			if (proz.isSelected()) {
				stepStatusUp(proz);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusHochsetzenHits() throws DAOException {
		for (Prozess proz : (List<Prozess>) this.page.getCompleteList()) {
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
		this.dao.save(proz);		
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
		this.dao.save(proz);
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenPage() throws DAOException {
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
			stepStatusDown(proz);
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenSelection() throws DAOException {
		for (Prozess proz : (List<Prozess>) this.page.getListReload()) {
			if (proz.isSelected()) {
				stepStatusDown(proz);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void BearbeitungsstatusRuntersetzenHits() throws DAOException {
		for (Prozess proz : (List<Prozess>) this.page.getCompleteList()) {
			stepStatusDown(proz);
		}
	}

	public void SchrittStatusUp() {
		if (this.mySchritt.getBearbeitungsstatusEnum() != StepStatus.DONE) {
			this.mySchritt.setBearbeitungsstatusUp();
			this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
			if (this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				new HelperSchritte().SchrittAbschliessen(this.mySchritt, false);
			} else {
				HelperSchritte.updateEditing(this.mySchritt);
			}
		}		
		Speichern();
		deleteSymlinksFromUserHomes();
	}

	public String SchrittStatusDown() {
		this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
		HelperSchritte.updateEditing(this.mySchritt);
		this.mySchritt.setBearbeitungsstatusDown();
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
		for (Prozess proz : (List<Prozess>) this.page.getList()) {
			proz.setSelected(true);
		}
	}

	@SuppressWarnings("unchecked")
	public void SelectionNone() {
		for (Prozess proz : (List<Prozess>) this.page.getList()) {
			proz.setSelected(false);
		}
	}

	/*
	 * Getter und Setter
	 */

	public Prozess getMyProzess() {
		return this.myProzess;
	}

	public void setMyProzess(Prozess myProzess) {
		this.myProzess = myProzess;
		this.myNewProcessTitle = myProzess.getTitel();
		loadProcessProperties();
	}

	public Prozesseigenschaft getMyProzessEigenschaft() {
		return this.myProzessEigenschaft;
	}

	public void setMyProzessEigenschaft(Prozesseigenschaft myProzessEigenschaft) {
		this.myProzessEigenschaft = myProzessEigenschaft;
	}

	public Schritt getMySchritt() {
		return this.mySchritt;
	}

	public void setMySchritt(Schritt mySchritt) {
		this.mySchritt = mySchritt;
	}

	public void setMySchrittReload(Schritt mySchritt) {
		// Helper.getHibernateSession();
		this.mySchritt = mySchritt;
	}

	public Schritteigenschaft getMySchrittEigenschaft() {
		return this.mySchrittEigenschaft;
	}

	public void setMySchrittEigenschaft(Schritteigenschaft mySchrittEigenschaft) {
		// Helper.getHibernateSession();
		this.mySchrittEigenschaft = mySchrittEigenschaft;
	}

	public Vorlage getMyVorlage() {
		return this.myVorlage;
	}

	public void setMyVorlage(Vorlage myVorlage) {
		this.myVorlage = myVorlage;
	}

	public void setMyVorlageReload(Vorlage myVorlage) {
		// Helper.getHibernateSession();
		this.myVorlage = myVorlage;
	}

	public Vorlageeigenschaft getMyVorlageEigenschaft() {
		return this.myVorlageEigenschaft;
	}

	public void setMyVorlageEigenschaft(Vorlageeigenschaft myVorlageEigenschaft) {
		// Helper.getHibernateSession();
		this.myVorlageEigenschaft = myVorlageEigenschaft;
	}

	public Werkstueck getMyWerkstueck() {
		return this.myWerkstueck;
	}

	public void setMyWerkstueck(Werkstueck myWerkstueck) {
		this.myWerkstueck = myWerkstueck;
	}

	public void setMyWerkstueckReload(Werkstueck myWerkstueck) {
		// Helper.getHibernateSession();
		this.myWerkstueck = myWerkstueck;
	}

	public Werkstueckeigenschaft getMyWerkstueckEigenschaft() {
		return this.myWerkstueckEigenschaft;
	}

	public void setMyWerkstueckEigenschaft(Werkstueckeigenschaft myWerkstueckEigenschaft) {
		// Helper.getHibernateSession();
		this.myWerkstueckEigenschaft = myWerkstueckEigenschaft;
	}

	public String getModusAnzeige() {
		return this.modusAnzeige;
	}

	public void setModusAnzeige(String modusAnzeige) {
		this.sortierung = "titelAsc";
		this.modusAnzeige = modusAnzeige;
	}

	public String getModusBearbeiten() {
		return this.modusBearbeiten;
	}

	public void setModusBearbeiten(String modusBearbeiten) {
		this.modusBearbeiten = modusBearbeiten;
	}

	public String reihenfolgeUp() {
		this.mySchritt.setReihenfolge(Integer.valueOf(this.mySchritt.getReihenfolge().intValue() - 1));
		Speichern();
		return Reload();
	}

	public String reihenfolgeDown() {
		this.mySchritt.setReihenfolge(Integer.valueOf(this.mySchritt.getReihenfolge().intValue() + 1));
		Speichern();
		return Reload();
	}

	public void setReload(String bla) {
		// Reload();
	}

	public String Reload() {
		// Helper.createNewHibernateSession();
		if (this.mySchritt != null && this.mySchritt.getId() != null) {
			Helper.getHibernateSession().refresh(this.mySchritt);
		}
		if (this.myProzess != null && this.myProzess.getId() != null) {
			Helper.getHibernateSession().refresh(this.myProzess);
		}
		return "";
	}

	public Benutzer getMyBenutzer() {
		return this.myBenutzer;
	}

	public void setMyBenutzer(Benutzer myBenutzer) {
		this.myBenutzer = myBenutzer;
	}

	public Benutzergruppe getMyBenutzergruppe() {
		return this.myBenutzergruppe;
	}

	public void setMyBenutzergruppe(Benutzergruppe myBenutzergruppe) {
		this.myBenutzergruppe = myBenutzergruppe;
	}

	/*
	 * Zuweisung der Projekte
	 */

	public Integer getProjektAuswahl() {
		if (this.myProzess.getProjekt() != null) {
			return this.myProzess.getProjekt().getId();
		} else {
			return Integer.valueOf(0);
		}
	}

	public void setProjektAuswahl(Integer inProjektAuswahl) {
		if (inProjektAuswahl.intValue() != 0) {
			try {
				this.myProzess.setProjekt(new ProjektDAO().get(inProjektAuswahl));
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
		CalcMetadataAndImages(this.page.getListReload());
	}

	@SuppressWarnings("unchecked")
	public void CalcMetadataAndImagesSelection() throws IOException, InterruptedException, SwapException, DAOException {
		ArrayList<Prozess> auswahl = new ArrayList<Prozess>();
		for (Prozess p : (List<Prozess>) this.page.getListReload()) {
			if (p.isSelected()) {
				auswahl.add(p);
			}
		}
		CalcMetadataAndImages(auswahl);
	}

	@SuppressWarnings("unchecked")
	public void CalcMetadataAndImagesHits() throws IOException, InterruptedException, SwapException, DAOException {
		CalcMetadataAndImages(this.page.getCompleteList());
	}

	private void CalcMetadataAndImages(List<Prozess> inListe) throws IOException, InterruptedException, SwapException, DAOException {
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
		this.myAnzahlList = new ArrayList<ProcessCounterObject>();
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
				this.dao.save(proz);
			}

			ProcessCounterObject pco = new ProcessCounterObject(proz.getTitel(), tempMetadata, tempDocstructs, tempImg);
			this.myAnzahlList.add(pco);

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
		for (ProcessCounterObject pco : this.myAnzahlList) {
			pco.setRelImages(pco.getImages() * 100 / maxImages);
			pco.setRelMetadata(pco.getMetadata() * 100 / maxMetadata);
			pco.setRelDocstructs(pco.getDocstructs() * 100 / maxDocstructs);
		}

		/* die Durchschnittsberechnung durchführen */
		int faktor = 1;
		if (this.myAnzahlList != null && this.myAnzahlList.size() > 0) {
			faktor = this.myAnzahlList.size();
		}
		this.myAnzahlSummary = new HashMap<String, Integer>();
		this.myAnzahlSummary.put("sumProcesses", faktor);
		this.myAnzahlSummary.put("sumMetadata", allMetadata);
		this.myAnzahlSummary.put("sumDocstructs", allDocstructs);
		this.myAnzahlSummary.put("sumImages", allImages);
		this.myAnzahlSummary.put("averageImages", allImages / faktor);
		this.myAnzahlSummary.put("averageMetadata", allMetadata / faktor);
		this.myAnzahlSummary.put("averageDocstructs", allDocstructs / faktor);
	}

	public HashMap<String, Integer> getMyAnzahlSummary() {
		return this.myAnzahlSummary;
	}

	public List<ProcessCounterObject> getMyAnzahlList() {
		return this.myAnzahlList;
	}

	/**
	 * Starte GoobiScript über alle Treffer
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptHits() {
		GoobiScript gs = new GoobiScript();
		gs.execute(this.page.getCompleteList(), this.goobiScript);
	}

	/**
	 * Starte GoobiScript über alle Treffer der Seite
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptPage() {
		GoobiScript gs = new GoobiScript();
		gs.execute(this.page.getListReload(), this.goobiScript);
	}

	/**
	 * Starte GoobiScript über alle selectierten Treffer
	 */
	@SuppressWarnings("unchecked")
	public void GoobiScriptSelection() {
		ArrayList<Prozess> auswahl = new ArrayList<Prozess>();
		for (Prozess p : (List<Prozess>) this.page.getListReload()) {
			if (p.isSelected()) {
				auswahl.add(p);
			}
		}
		GoobiScript gs = new GoobiScript();
		gs.execute(auswahl, this.goobiScript);
	}

	/*
	 * Statistische Auswertung
	 */

	public void StatisticsStatusVolumes() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.STATUS_VOLUMES, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
		this.statisticsManager.calculate();
	}

	public void StatisticsUsergroups() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.USERGROUPS, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
		this.statisticsManager.calculate();
	}

	public void StatisticsRuntimeSteps() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS, this.myFilteredDataSource, FacesContext
				.getCurrentInstance().getViewRoot().getLocale());
	}

	public void StatisticsProduction() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.PRODUCTION, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
	}

	public void StatisticsStorage() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.STORAGE, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
	}

	public void StatisticsCorrection() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.CORRECTIONS, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
	}

	public void StatisticsTroughput() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.THROUGHPUT, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
	}

	public void StatisticsProject() {
		this.statisticsManager = new StatisticsManager(StatisticsMode.PROJECTS, this.myFilteredDataSource, FacesContext.getCurrentInstance()
				.getViewRoot().getLocale());
		this.statisticsManager.calculate();
	}

	/**
	 * ist called via jsp at the end of building a chart in include file Prozesse_Liste_Statistik.jsp and resets the statistics so that with the next
	 * reload a chart is not shown anymore
	 * 
	 * @author Wulf
	 */
	public String getResetStatistic() {
		// if (!showStatistics) {
		// statisticsManager = null;
		// }
		this.showStatistics = false;
		return "";
	}

	public String getMyDatasetHoehe() {
		int bla = this.page.getCompleteList().size() * 20;
		return String.valueOf(bla);
	}

	public int getMyDatasetHoeheInt() {
		int bla = this.page.getCompleteList().size() * 20;
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
		TiffHeader tiff = new TiffHeader(this.myProzess);
		tiff.ExportStart();
	}

	public void DownloadMultiTiff() throws IOException, InterruptedException, SwapException, DAOException {
		Multipage mp = new Multipage();
		mp.ExportStart(this.myProzess);
	}

	public String getGoobiScript() {
		return this.goobiScript;
	}

	public void setGoobiScript(String goobiScript) {
		this.goobiScript = goobiScript;
	}

	public HashMap<String, Boolean> getAnzeigeAnpassen() {
		return this.anzeigeAnpassen;
	}

	public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
		this.anzeigeAnpassen = anzeigeAnpassen;
	}

	public String getMyNewProcessTitle() {
		return this.myNewProcessTitle;
	}

	public void setMyNewProcessTitle(String myNewProcessTitle) {
		this.myNewProcessTitle = myNewProcessTitle;
	}

	public StatisticsManager getStatisticsManager() {
		return this.statisticsManager;
	}

	/*************************************************************************************
	 * Getter for showStatistics
	 * 
	 * @return the showStatistics
	 *************************************************************************************/
	public boolean isShowStatistics() {
		return this.showStatistics;
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
			return this.images;
		}

		public int getMetadata() {
			return this.metadata;
		}

		public String getTitle() {
			return this.title;
		}

		public int getDocstructs() {
			return this.docstructs;
		}

		public int getRelDocstructs() {
			return this.relDocstructs;
		}

		public int getRelImages() {
			return this.relImages;
		}

		public int getRelMetadata() {
			return this.relMetadata;
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
			String ziel = login.getMyBenutzer().getHomeDir() + this.myProzess.getTitel() + "_log.xml";
			xmlExport.startExport(this.myProzess, ziel);
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
			 * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
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
				export.startTransformation(out, this.myProzess, this.selectedXslt);
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
		return String.valueOf(this.myProzess.getId());
	}

	public void setMyProcessId(String id) {
		try {
			int myid = new Integer(id);
			this.myProzess = this.dao.get(myid);
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
		this.selectedXslt = select;
	}

	public String getSelectedXslt() {
		return this.selectedXslt;
	}

	public String downloadDocket() {
		return this.myProzess.downloadDocket();
	}

	public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
		this.myCurrentTable = myCurrentTable;
	}

	public StatisticsRenderingElement getMyCurrentTable() {
		return this.myCurrentTable;
	}

	public void CreateExcel() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("export.xls");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
				ServletOutputStream out = response.getOutputStream();
				HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
				wb.write(out);
				out.flush();
				facesContext.responseComplete();

			} catch (IOException e) {

			}
		}
	}

	public void generateResultAsPdf() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("search.pdf");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"search.pdf\"");
				ServletOutputStream out = response.getOutputStream();

				SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses, this.showArchivedProjects);
				HSSFWorkbook wb = sr.getResult();
				List<List<HSSFCell>> rowList = new ArrayList<List<HSSFCell>>();
				HSSFSheet mySheet = wb.getSheetAt(0);
				Iterator<Row> rowIter = mySheet.rowIterator();
				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator<Cell> cellIter = myRow.cellIterator();
					List<HSSFCell> row = new ArrayList<HSSFCell>();
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						row.add(myCell);
					}
					rowList.add(row);
				}
				Document document = new Document();
				Rectangle a4quer = new Rectangle(PageSize.A3.getHeight(), PageSize.A3.getWidth());
				PdfWriter.getInstance(document, out);
				document.setPageSize(a4quer);
				document.open();
				if (rowList.size() > 0) {
					Paragraph p = new Paragraph(rowList.get(0).get(0).toString());
					
					document.add(p);
					PdfPTable table = new PdfPTable(9);
					table.setSpacingBefore(20);
					for (int i = 1; i < rowList.size(); i++) {

						// StringBuffer sb = new StringBuffer();
						List<HSSFCell> row = rowList.get(i);
						for (int j = 0; j < row.size(); j++) {
							HSSFCell myCell = row.get(j);
							// TODO aufhübschen und nicht toString() nutzen

							String stringCellValue = myCell.toString();
							table.addCell(stringCellValue);
							// sb.append(stringCellValue);
						}

					}
					document.add(table);
				}

				document.close();
				out.flush();
				facesContext.responseComplete();

			} catch (Exception e) {
			}
		}
	}

	public void generateResult() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("search.xls");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"search.xls\"");
				ServletOutputStream out = response.getOutputStream();
				SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses, this.showArchivedProjects);
				HSSFWorkbook wb = sr.getResult();
				wb.write(out);
				out.flush();
				facesContext.responseComplete();

			} catch (IOException e) {

			}
		}
	}

	public boolean isShowClosedProcesses() {
		return this.showClosedProcesses;
	}

	public void setShowClosedProcesses(boolean showClosedProcesses) {
		this.showClosedProcesses = showClosedProcesses;
	}

	public void setShowArchivedProjects(boolean showArchivedProjects) {
		this.showArchivedProjects = showArchivedProjects;
	}

	public boolean isShowArchivedProjects() {
		return this.showArchivedProjects;
	}

	// TODO property

	public ProcessProperty getProcessProperty() {
		return this.processProperty;
	}

	public void setProcessProperty(ProcessProperty processProperty) {
		this.processProperty = processProperty;
	}

	public List<ProcessProperty> getProcessProperties() {
		return this.processPropertyList;
	}

	private void loadProcessProperties() {
		this.dao.refresh(this.myProzess);
		this.containers = new TreeMap<Integer, PropertyListObject>();
		this.processPropertyList = PropertyParser.getPropertiesForProcess(this.myProzess);
		// for (ProcessProperty pt : this.processPropertyList) {
		// if (!this.containers.contains(pt.getContainer())) {
		// this.containers.add(pt.getContainer());
		// }
		// }
		// Collections.sort(this.containers);
		for (ProcessProperty pt : this.processPropertyList) {
			if (!this.containers.keySet().contains(pt.getContainer())) {
				PropertyListObject plo = new PropertyListObject(pt.getContainer());
				plo.addToList(pt);
				this.containers.put(pt.getContainer(), plo);
			} else {
				PropertyListObject plo = this.containers.get(pt.getContainer());
				plo.addToList(pt);
				this.containers.put(pt.getContainer(), plo);
			}
		}
	}

	// TODO validierung nur bei Schritt abgeben, nicht bei normalen speichern
	public void saveProcessProperties() {
		boolean valid = true;
		for (IProperty p : this.processPropertyList) {
			if (!p.isValid()) {
				Helper.setFehlerMeldung("Property " + p.getName() + " not valid");
				valid = false;
			}
		}

		if (valid) {
			for (ProcessProperty p : this.processPropertyList) {
				if (p.getProzesseigenschaft() == null) {
					Prozesseigenschaft pe = new Prozesseigenschaft();
					pe.setProzess(this.myProzess);
					p.setProzesseigenschaft(pe);
					this.myProzess.getEigenschaften().add(pe);
				}
				p.transfer();
				if (!this.myProzess.getEigenschaften().contains(p.getProzesseigenschaft())) {
					this.myProzess.getEigenschaften().add(p.getProzesseigenschaft());
				}
			}

			List<Prozesseigenschaft> props = this.myProzess.getEigenschaftenList();
			for (Prozesseigenschaft pe : props) {
				if (pe.getTitel() == null) {
					this.myProzess.getEigenschaften().remove(pe);
				}
			}

			try {
				this.dao.save(this.myProzess);
				Helper.setMeldung("Properties saved");
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("Properties could not be saved");
			}
		}
	}

	public void saveCurrentProperty() {
		List<ProcessProperty> ppList = getContainerProperties();
		for (ProcessProperty pp : ppList) {
			this.processProperty = pp;
			if (!this.processProperty.isValid()) {
				Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
				return;
			}
			if (this.processProperty.getProzesseigenschaft() == null) {
				Prozesseigenschaft pe = new Prozesseigenschaft();
				pe.setProzess(this.myProzess);
				this.processProperty.setProzesseigenschaft(pe);
				this.myProzess.getEigenschaften().add(pe);
			}
			this.processProperty.transfer();

			List<Prozesseigenschaft> props = this.myProzess.getEigenschaftenList();
			for (Prozesseigenschaft pe : props) {
				if (pe.getTitel() == null) {
					this.myProzess.getEigenschaften().remove(pe);
				}
			}
			if (!this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().contains(this.processProperty.getProzesseigenschaft())) {
				this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().add(this.processProperty.getProzesseigenschaft());
			}
			try {
				this.dao.save(this.myProzess);
				Helper.setMeldung("Property saved");
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("Properties could not be saved");
			}
		}
		loadProcessProperties();
	}

	public int getPropertyListSize() {
		if (this.processPropertyList == null) {
			return 0;
		}
		return this.processPropertyList.size();
	}

	public Map<Integer, PropertyListObject> getContainers() {
		return this.containers;
	}

	public List<Integer> getContainerList() {
		return new ArrayList<Integer>(this.containers.keySet());
	}

	public int getContainersSize() {
		if (this.containers == null) {
			return 0;
		}
		return this.containers.size();
	}

	public List<ProcessProperty> getSortedProperties() {
		Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
		Collections.sort(this.processPropertyList, comp);
		return this.processPropertyList;
	}

	public void deleteProperty() {
		List<ProcessProperty> ppList = getContainerProperties();
		for (ProcessProperty pp : ppList) {
			this.processPropertyList.remove(pp);
			this.myProzess.getEigenschaften().remove(pp.getProzesseigenschaft());

		}

		List<Prozesseigenschaft> props = this.myProzess.getEigenschaftenList();
		for (Prozesseigenschaft pe : props) {
			if (pe.getTitel() == null) {
				this.myProzess.getEigenschaften().remove(pe);
			}
		}
		try {
			this.dao.save(this.myProzess);
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("Properties could not be deleted");
		}
		// saveWithoutValidation();
		loadProcessProperties();
	}

	public void duplicateProperty() {
		ProcessProperty pt = this.processProperty.getClone(0);
		this.processPropertyList.add(pt);
		saveProcessProperties();
	}

	public Integer getContainer() {
		return this.container;
	}

	public void setContainer(Integer container) {
		if (container != null && container > 0) {
			this.processProperty = getContainerProperties().get(0);
		}
		this.container = container;
	}

	public List<ProcessProperty> getContainerProperties() {
		List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
		// int currentContainer = this.processProperty.getContainer();

		if (this.container != null && this.container > 0) {
			for (ProcessProperty pp : this.processPropertyList) {
				if (pp.getContainer() == this.container) {
					answer.add(pp);
				}
			}
		} else {
			answer.add(this.processProperty);
		}

		return answer;
	}

	public String duplicateContainer() {
		Integer currentContainer = this.processProperty.getContainer();
		List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
		// search for all properties in container
		for (ProcessProperty pt : this.processPropertyList) {
			if (pt.getContainer() == currentContainer) {
				plist.add(pt);
			}
		}
		int newContainerNumber = 0;
		if (currentContainer > 0) {
			newContainerNumber++;
			// find new unused container number
			boolean search = true;
			while (search) {
				if (!this.containers.containsKey(newContainerNumber)) {
					search = false;
				} else {
					newContainerNumber++;
				}
			}
		}
		// clone properties
		for (ProcessProperty pt : plist) {
			ProcessProperty newProp = pt.getClone(newContainerNumber);
			this.processPropertyList.add(newProp);
			this.processProperty = newProp;
			if (this.processProperty.getProzesseigenschaft() == null) {
				Prozesseigenschaft pe = new Prozesseigenschaft();
				pe.setProzess(this.myProzess);
				this.processProperty.setProzesseigenschaft(pe);
				this.myProzess.getEigenschaften().add(pe);
			}
			this.processProperty.transfer();

		}
		try {
			this.dao.save(this.myProzess);
			Helper.setMeldung("Property saved");
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("Properties could not be saved");
		}
		loadProcessProperties();

		return "";

		// Integer currentContainer = this.processProperty.getContainer();
		// List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
		// // search for all properties in container
		// if (currentContainer != 0) {
		// for (ProcessProperty pt : this.processPropertyList) {
		// if (pt.getContainer() == currentContainer) {
		// plist.add(pt);
		// }
		// }
		// } else {
		// plist.add(this.processProperty);
		// }
		// int newContainerNumber = 0;
		// if (currentContainer > 0) {
		// newContainerNumber++;
		// // find new unused container number
		// boolean search = true;
		// while (search) {
		// if (!this.containers.contains(newContainerNumber)) {
		// search = false;
		// } else {
		// newContainerNumber++;
		// }
		// }
		// }
		// // clone properties
		// for (ProcessProperty pt : plist) {
		// ProcessProperty newProp = pt.getClone(newContainerNumber);
		// if (newProp.getProzesseigenschaft() == null) {
		// Prozesseigenschaft pe = new Prozesseigenschaft();
		// pe.setProzess(this.myProzess);
		// newProp.setProzesseigenschaft(pe);
		// this.myProzess.getEigenschaften().add(pe);
		// }
		//
		// this.processPropertyList.add(newProp);
		// this.processProperty = newProp;
		// saveCurrentProperty();
		// }
		// loadProcessProperties();
		//
		// return "";
	}

	public List<ProcessProperty> getContainerlessProperties() {
		List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
		for (ProcessProperty pp : this.processPropertyList) {
			if (pp.getContainer() == 0) {
				answer.add(pp);
			}
		}
		return answer;
	}

	public void createNewProperty() {
		if (this.processPropertyList == null) {
			this.processPropertyList = new ArrayList<ProcessProperty>();
		}
		ProcessProperty pp = new ProcessProperty();
		pp.setType(Type.TEXT);
		pp.setContainer(0);
		// Prozesseigenschaft pe = new Prozesseigenschaft();
		// pe.setProzess(this.myProzess);
		// this.myProzess.getEigenschaften().add(pe);
		// pp.setProzesseigenschaft(pe);
		this.processPropertyList.add(pp);
		this.processProperty = pp;
		// saveProcessProperties();
	}

}
