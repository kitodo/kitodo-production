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

package org.goobi.production.cli.helper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigOpac;
import de.sub.goobi.config.ConfigOpacDoctype;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;


public class CopyProcess extends ProzesskopieForm {

	private static final Logger myLogger = Logger.getLogger(ProzesskopieForm.class);
	UghHelper ughHelp = new UghHelper();
	private BeanHelper bhelp = new BeanHelper();
	private Fileformat myRdf;
	private String opacSuchfeld = "12";
	private String opacSuchbegriff;
	private String opacKatalog;
	private Prozess prozessVorlage = new Prozess();
	private Prozess prozessKopie = new Prozess();
	private ConfigOpac co;
	/* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
	private boolean useOpac;
	private boolean useTemplates;
	public String metadataFile;

	private HashMap<String, Boolean> standardFields;
	private List<AdditionalField> additionalFields;
	private List<String> digitalCollections;
	private String tifHeader_imagedescription = "";
	private String tifHeader_documentname = "";

	private String naviFirstPage;
	private Integer auswahl;
	private String docType;
	private String atstsl = "";
	private List<String> possibleDigitalCollection;

	public final static String DIRECTORY_SUFFIX = "_tif";

	/* =============================================================== */

	public String Prepare() {
		if (prozessVorlage.isContainsUnreachableSteps()) {
			for (Schritt s : prozessVorlage.getSchritteList()) {
				if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0)
					Helper.setFehlerMeldung("Kein Benutzer festgelegt für: ", s.getTitel());
			}
			return "";
		}

		clearValues();
		try {
			co = new ConfigOpac();
		} catch (IOException e) {
			myLogger.error("Error while reading von opac-config", e);
			Helper.setFehlerMeldung("Error while reading von opac-config", e);
			return null;
		}
		readProjectConfigs();
		Prefs myPrefs = prozessVorlage.getRegelsatz().getPreferences();
		try {
			myRdf = new MetsModsImportExport(myPrefs);
			myRdf.read(metadataFile);
		} catch (PreferencesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		;
		prozessKopie = new Prozess();
		prozessKopie.setTitel("");
		prozessKopie.setIstTemplate(false);
		prozessKopie.setInAuswahllisteAnzeigen(false);
		prozessKopie.setProjekt(prozessVorlage.getProjekt());
		prozessKopie.setRegelsatz(prozessVorlage.getRegelsatz());
		digitalCollections = new ArrayList<String>();

		/*
		 * -------------------------------- Kopie der Prozessvorlage anlegen --------------------------------
		 */
		bhelp.SchritteKopieren(prozessVorlage, prozessKopie);
		bhelp.ScanvorlagenKopieren(prozessVorlage, prozessKopie);
		bhelp.WerkstueckeKopieren(prozessVorlage, prozessKopie);
		bhelp.EigenschaftenKopieren(prozessVorlage, prozessKopie);

		initializePossibleDigitalCollections();

		return naviFirstPage;
	}

	/* =============================================================== */

	private void readProjectConfigs() {
		/*-------------------------------- 
		 * projektabhängig die richtigen Felder in der Gui anzeigen 
		 * --------------------------------*/
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(prozessVorlage.getProjekt());
		} catch (IOException e) {
			Helper.setFehlerMeldung("IOException", e.getMessage());
			return;
		}

		docType = cp.getParamString("createNewProcess.defaultdoctype", co.getAllDoctypes().get(0).getTitle());
		useOpac = cp.getParamBoolean("createNewProcess.opac[@use]");
		useTemplates = cp.getParamBoolean("createNewProcess.templates[@use]");
		naviFirstPage = "ProzessverwaltungKopie1";
		if (opacKatalog.equals(""))
			opacKatalog = cp.getParamString("createNewProcess.opac.catalogue");

		/*
		 * -------------------------------- die auszublendenden Standard-Felder ermitteln --------------------------------
		 */
		for (String t : cp.getParamList("createNewProcess.itemlist.hide")) {
			standardFields.put(t, false);
		}

		/*
		 * -------------------------------- die einzublendenen (zusätzlichen) Eigenschaften ermitteln --------------------------------
		 */
		int count = cp.getParamList("createNewProcess.itemlist.item").size();
		for (int i = 0; i < count; i++) {
			AdditionalField fa = new AdditionalField(this);
			fa.setFrom(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@from]"));
			fa.setTitel(cp.getParamString("createNewProcess.itemlist.item(" + i + ")"));
			fa.setRequired(cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@required]"));
			fa.setIsdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isdoctype]"));
			fa.setIsnotdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isnotdoctype]"));

			// attributes added 30.3.09
			String test = (cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initStart]"));
			fa.setInitStart(test);

			fa.setInitEnd(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initEnd]"));

			/*
			 * -------------------------------- Bindung an ein Metadatum eines Docstructs --------------------------------
			 */
			if (cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@ughbinding]")) {
				fa.setUghbinding(true);
				fa.setDocstruct(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@docstruct]"));
				fa.setMetadata(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@metadata]"));
			}

			/*
			 * -------------------------------- prüfen, ob das aktuelle Item eine Auswahlliste werden soll --------------------------------
			 */
			int selectItemCount = cp.getParamList("createNewProcess.itemlist.item(" + i + ").select").size();
			/* Children durchlaufen und SelectItems erzeugen */
			if (selectItemCount > 0)
				fa.setSelectList(new ArrayList<SelectItem>());
			for (int j = 0; j < selectItemCount; j++) {
				String svalue = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")[@label]");
				String sid = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")");
				fa.getSelectList().add(new SelectItem(sid, svalue, null));
			}
			additionalFields.add(fa);
		}
	}

	/* =============================================================== */

	/**
	 * OpacAnfrage
	 */
	public String OpacAuswerten() {
		clearValues();
		readProjectConfigs();
		try {
			Prefs myPrefs = prozessVorlage.getRegelsatz().getPreferences();
			/* den Opac abfragen und ein RDF draus bauen lassen */
			myRdf = new MetsMods(myPrefs);
			myRdf.read(metadataFile);

			fillFieldsFromMetadataFile(myRdf);

		} catch (Exception e) {
			Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
			e.printStackTrace();
		}
		return "";
	}

	/* =============================================================== */

	/**
	 * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei füllen
	 * 
	 * @throws PreferencesException
	 */
	private void fillFieldsFromMetadataFile(Fileformat myRdf) throws PreferencesException {
		if (myRdf != null) {
			UghHelper ughHelp = new UghHelper();

			for (AdditionalField field : additionalFields) {
				if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
					/* welches Docstruct */

					DocStruct myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
					if (field.getDocstruct().equals("firstchild"))
						try {
							myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
						} catch (RuntimeException e) {
						}
					if (field.getDocstruct().equals("boundbook"))
						myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
					/* welches Metadatum */
					try {
						if (field.getMetadata().equals("ListOfCreators")) {
							/* bei Autoren die Namen zusammenstellen */
							String myautoren = "";
							if (myTempStruct.getAllPersons() != null) {
								for (Person p : myTempStruct.getAllPersons()) {
									myautoren += p.getLastname();
									if (StringUtils.isNotBlank(p.getFirstname())) {
										myautoren += ", " + p.getFirstname();
									}
									myautoren += "; ";
								}
								if (myautoren.endsWith("; "))
									myautoren = myautoren.substring(0, myautoren.length() - 2);
							}
							field.setWert(myautoren);
						} else {
							/* bei normalen Feldern die Inhalte auswerten */
							MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
							Metadata md = ughHelp.getMetadata(myTempStruct, mdt);
							if (md != null) {
								field.setWert(md.getValue());
							}
						}
					} catch (UghHelperException e) {
						Helper.setFehlerMeldung(e.getMessage(), "");
					}
				} // end if ughbinding
			}// end for
		} // end if myrdf==null
	}

	/**
	 * alle Konfigurationseigenschaften und Felder zurücksetzen ================================================================
	 */
	private void clearValues() {
		if (opacKatalog == null)
			opacKatalog = "";
		standardFields = new HashMap<String, Boolean>();
		standardFields.put("collections", true);
		standardFields.put("doctype", true);
		standardFields.put("regelsatz", true);
		additionalFields = new ArrayList<AdditionalField>();
		tifHeader_documentname = "";
		tifHeader_imagedescription = "";
	}

	/**
	 * Auswahl des Prozesses auswerten
	 * 
	 * @throws DAOException
	 * @throws NamingException
	 * @throws SQLException
	 *             ============================================================== ==
	 */
	public String TemplateAuswahlAuswerten() throws DAOException {
		/* den ausgewählten Prozess laden */
		Prozess tempProzess = new ProzessDAO().get(auswahl);
		if (tempProzess.getWerkstueckeSize() > 0) {
			/* erstes Werkstück durchlaufen */
			Werkstueck werk = tempProzess.getWerkstueckeList().get(0);
			for (Werkstueckeigenschaft eig : werk.getEigenschaften()) {
				for (AdditionalField field : additionalFields) {
					if (field.getTitel().equals(eig.getTitel()))
						field.setWert(eig.getWert());
				}
			}
		}

		if (tempProzess.getVorlagenSize() > 0) {
			/* erste Vorlage durchlaufen */
			Vorlage vor = tempProzess.getVorlagenList().get(0);
			for (Vorlageeigenschaft eig : vor.getEigenschaften()) {
				for (AdditionalField field : additionalFields) {
					if (field.getTitel().equals(eig.getTitel()))
						field.setWert(eig.getWert());
				}
			}
		}

		try {
			myRdf = tempProzess.readMetadataAsTemplateFile();
		} catch (Exception e) {
			Helper.setFehlerMeldung("Fehler beim Einlesen der Template-Metadaten ", e);
		}

		/* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
		try {
			DocStruct colStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
			removeCollections(colStruct);
			colStruct = colStruct.getAllChildren().get(0);
			removeCollections(colStruct);
		} catch (PreferencesException e) {
			Helper.setFehlerMeldung("Fehler beim Anlegen des Vorgangs", e);
			myLogger.error("Fehler beim Anlegen des Vorgangs", e);
		} catch (RuntimeException e) {
			/*
			 * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
			 */
		}

		return "";
	}

	/**
	 * Validierung der Eingaben
	 * 
	 * @return sind Fehler bei den Eingaben vorhanden? ================================================================
	 */
	private boolean isContentValid() {
		/*
		 * -------------------------------- Vorbedingungen prüfen --------------------------------
		 */
		boolean valide = true;

		/*
		 * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
		 */
		/* kein Titel */
		if (prozessKopie.getTitel() == null || prozessKopie.getTitel().equals("")) {
			valide = false;
			Helper.setFehlerMeldung("UnvollstaendigeDaten: ", "kein Vorgangstitel angegeben");
		}

		// if (!prozessKopie.getTitel().matches("[\\w-]*")) {
		String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
		if (!prozessKopie.getTitel().matches(validateRegEx)) {
			valide = false;
			Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
		}

		/* prüfen, ob der Prozesstitel schon verwendet wurde */
		if (prozessKopie.getTitel() != null) {
			long anzahl = 0;
			try {
				anzahl = new ProzessDAO().count("from Prozess where titel='" + prozessKopie.getTitel() + "'");
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
				valide = false;
			}
			if (anzahl > 0) {
				valide = false;
				Helper.setFehlerMeldung("UngueltigeDaten: ", "der Vorgangstitel wird bereits verwendet");
			}
		}

		/*
		 * -------------------------------- Prüfung der standard-Eingaben, die angegeben werden müssen --------------------------------
		 */
		/* keine Collektion ausgewählt */
		if (standardFields.get("collections") && getDigitalCollections().size() == 0) {
			valide = false;
			Helper.setFehlerMeldung("UnvollstaendigeDaten: ", "keine Kollektion angegeben");
		}

		/*
		 * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
		 */
		for (AdditionalField field : additionalFields) {
			if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype() && (StringUtils.isBlank(field.getWert()))) {
				valide = false;
				Helper.setFehlerMeldung("UnvollstaendigeDaten: ", field.getTitel() + " nicht angegeben");
			}
		}
		return valide;
	}

	/* =============================================================== */

	public String GoToSeite1() {
		return naviFirstPage;
	}

	/* =============================================================== */

	public String GoToSeite2() {
		if (!isContentValid())
			return naviFirstPage;
		else
			return "ProzessverwaltungKopie2";
	}

	/**
	 * Anlegen des Prozesses und Speichern der Metadaten ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 * @throws WriteException
	 */
	public Prozess NeuenProzessAnlegen2() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
			WriteException {
		Helper.getHibernateSession().evict(prozessKopie);

		prozessKopie.setId(null);
		if (!isContentValid())
			return null;
		EigenschaftenHinzufuegen();

		/*
		 * -------------------------------- jetzt in der Prozesskopie für alle bereits abgeschlossenen Schritte ein Bearbeitungsdatum und einen
		 * Benutzer eintragen --------------------------------
		 */
		for (Schritt step : prozessKopie.getSchritteList()) {
			/*
			 * -------------------------------- always save date and user for each step --------------------------------
			 */
			step.setBearbeitungszeitpunkt(prozessKopie.getErstellungsdatum());
			step.setEditTypeEnum(StepEditType.AUTOMATIC);
			LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
			if (loginForm != null)
				step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());

			/*
			 * -------------------------------- only if its done, set edit start and end date --------------------------------
			 */
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				step.setBearbeitungsbeginn(prozessKopie.getErstellungsdatum());
				// this concerns steps, which are set as done right on creation
				// bearbeitungsbeginn is set to creation timestamp of process
				// because the creation of it is basically begin of work
				Date myDate = new Date();
				step.setBearbeitungszeitpunkt(myDate);
				step.setBearbeitungsende(myDate);
			}

		}

		try {
			ProzessDAO dao = new ProzessDAO();
			dao.save(prozessKopie);
			dao.refresh(prozessKopie);
		} catch (DAOException e) {
			e.printStackTrace();
			myLogger.error("error on save: ", e);
			return prozessKopie;
		}

		/*
		 * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
		 */
		createNewFileformat();

		// /*--------------------------------
		// * wenn eine RDF-Konfiguration
		// * vorhanden ist (z.B. aus dem Opac-Import, oder frisch angelegt), dann
		// * diese ergänzen
		// * --------------------------------*/
		if (myRdf != null) {
			for (AdditionalField field : additionalFields) {
				if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
					/* welches Docstruct */
					DocStruct myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
					DocStruct myTempChild = null;
					if (field.getDocstruct().equals("firstchild")) {
						try {
							myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
						} catch (RuntimeException e) {
							/*
							 * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
							 */
						}
					}
					/*
					 * falls topstruct und firstchild das Metadatum bekommen sollen
					 */
					if (!field.getDocstruct().equals("firstchild") && field.getDocstruct().contains("firstchild"))
						try {
							myTempChild = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
						} catch (RuntimeException e) {
						}
					if (field.getDocstruct().equals("boundbook"))
						myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
					/* welches Metadatum */
					try {
						/*
						 * bis auf die Autoren alle additionals in die Metadaten übernehmen
						 */
						if (!field.getMetadata().equals("ListOfCreators")) {
							MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
							Metadata md = ughHelp.getMetadata(myTempStruct, mdt);
							/*
							 * wenn das Metadatum null ist, dann jetzt initialisieren
							 */
							if (md == null) {
								md = new Metadata(mdt);
								md.setDocStruct(myTempStruct);
								myTempStruct.addMetadata(md);
							}
							md.setValue(field.getWert());
							/*
							 * wenn dem Topstruct und dem Firstchild der Wert gegeben werden soll
							 */
							if (myTempChild != null) {
								md = ughHelp.getMetadata(myTempChild, mdt);
								md.setValue(field.getWert());
							}
						}
					} catch (Exception e) {
						Helper.setFehlerMeldung(e);
						e.printStackTrace();
					}
				} // end if ughbinding
			}// end for

			/*
			 * -------------------------------- Collectionen hinzufügen --------------------------------
			 */
			DocStruct colStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
			try {
				addCollections(colStruct);
				/* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
				colStruct = colStruct.getAllChildren().get(0);
				addCollections(colStruct);
			} catch (RuntimeException e) {
				/*
				 * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
				 */
			}

			/*
			 * -------------------------------- Imagepfad hinzufügen (evtl. vorhandene zunächst löschen) --------------------------------
			 */
			try {
				UghHelper ughhelp = new UghHelper();
				MetadataType mdt = ughhelp.getMetadataType(prozessKopie, "pathimagefiles");
				List<? extends Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
				if (alleImagepfade != null && alleImagepfade.size() > 0) {
					for (Metadata md : alleImagepfade) {
						myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
					}
				}
				Metadata newmd = new Metadata(mdt);
				newmd.setValue("./" + prozessKopie.getTitel() + DIRECTORY_SUFFIX);
				myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);

				/* Rdf-File schreiben */
				prozessKopie.writeMetadataFile(myRdf);

				/*
				 * -------------------------------- soll der Prozess als Vorlage verwendet werden? --------------------------------
				 */
				if (useTemplates && prozessKopie.isInAuswahllisteAnzeigen())
					prozessKopie.writeMetadataAsTemplateFile(myRdf);

			} catch (ugh.exceptions.DocStructHasNoTypeException e) {
				Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung("UghHelperException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			} catch (MetadataTypeNotAllowedException e) {
				Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			}

		}

		// Adding process to history
		if (!HistoryAnalyserJob.updateHistoryForProcess(prozessKopie)) {
			Helper.setFehlerMeldung("historyNotUpdated");
		} else {
			try {
				new ProzessDAO().save(prozessKopie);
			} catch (DAOException e) {
				e.printStackTrace();
				myLogger.error("error on save: ", e);
				return prozessKopie;
			}
		}

		prozessKopie.readMetadataFile();

		/* damit die Sortierung stimmt nochmal einlesen */
		Helper.getHibernateSession().refresh(prozessKopie);
		return prozessKopie;

	}

	/* =============================================================== */

	private void addCollections(DocStruct colStruct) {
		for (String s : digitalCollections) {
			try {
				Metadata md = new Metadata(ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection"));
				md.setValue(s);
				md.setDocStruct(colStruct);
				colStruct.addMetadata(md);
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			} catch (DocStructHasNoTypeException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			} catch (MetadataTypeNotAllowedException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			}
		}
	}

	/**
	 * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
	 */
	private void removeCollections(DocStruct colStruct) {
		try {
			MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection");
			ArrayList<Metadata> myCollections = new ArrayList<Metadata>(colStruct.getAllMetadataByType(mdt));
			if (myCollections != null && myCollections.size() > 0) {
				for (Metadata md : myCollections) {
					colStruct.removeMetadata(md);
				}
			}
		} catch (UghHelperException e) {
			Helper.setFehlerMeldung(e.getMessage(), "");
			e.printStackTrace();
		} catch (DocStructHasNoTypeException e) {
			Helper.setFehlerMeldung(e.getMessage(), "");
			e.printStackTrace();
		}
	}

	/* =============================================================== */

	private void createNewFileformat() {

		Prefs myPrefs = prozessKopie.getRegelsatz().getPreferences();

		Fileformat ff;
		try {
			ff = new MetsModsImportExport(myPrefs);
			ff.read(metadataFile);
		} catch (PreferencesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void EigenschaftenHinzufuegen() {
		/*
		 * -------------------------------- Vorlageneigenschaften initialisieren --------------------------------
		 */
		Vorlage vor;
		if (prozessKopie.getVorlagenSize() > 0) {
			vor = (Vorlage) prozessKopie.getVorlagenList().get(0);
		} else {
			vor = new Vorlage();
			vor.setProzess(prozessKopie);
			Set<Vorlage> vorlagen = new HashSet<Vorlage>();
			vorlagen.add(vor);
			prozessKopie.setVorlagen(vorlagen);
		}

		/*
		 * -------------------------------- Werkstückeigenschaften initialisieren --------------------------------
		 */
		Werkstueck werk;
		if (prozessKopie.getWerkstueckeSize() > 0) {
			werk = (Werkstueck) prozessKopie.getWerkstueckeList().get(0);
		} else {
			werk = new Werkstueck();
			werk.setProzess(prozessKopie);
			Set<Werkstueck> werkstuecke = new HashSet<Werkstueck>();
			werkstuecke.add(werk);
			prozessKopie.setWerkstuecke(werkstuecke);
		}

		/*
		 * -------------------------------- jetzt alle zusätzlichen Felder durchlaufen und die Werte hinzufügen --------------------------------
		 */
		BeanHelper bh = new BeanHelper();
		for (AdditionalField field : additionalFields) {
			if (field.getShowDependingOnDoctype()) {
				if (field.getFrom().equals("werk"))
					bh.EigenschaftHinzufuegen(werk, field.getTitel(), field.getWert());
				if (field.getFrom().equals("vorlage"))
					bh.EigenschaftHinzufuegen(vor, field.getTitel(), field.getWert());
				if (field.getFrom().equals("prozess"))
					bh.EigenschaftHinzufuegen(prozessKopie, field.getTitel(), field.getWert());
			}
		}
		/* Doctype */
		bh.EigenschaftHinzufuegen(werk, "DocType", docType);
		/* Tiffheader */
		bh.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", tifHeader_imagedescription);
		bh.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", tifHeader_documentname);
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public Collection<SelectItem> getArtists() {
		ArrayList<SelectItem> artisten = new ArrayList<SelectItem>();
		StringTokenizer tokenizer = new StringTokenizer(ConfigMain.getParameter("TiffHeaderArtists"), "|");
		boolean tempBol = true;
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
			if (tempBol)
				artisten.add(new SelectItem(tok));
			tempBol = !tempBol;
		}
		return artisten;
	}

	public Prozess getProzessVorlage() {
		return prozessVorlage;
	}

	public void setProzessVorlage(Prozess prozessVorlage) {
		this.prozessVorlage = prozessVorlage;
	}

	public Integer getAuswahl() {
		return auswahl;
	}

	public void setAuswahl(Integer auswahl) {
		this.auswahl = auswahl;
	}

	public List<AdditionalField> getAdditionalFields() {
		return additionalFields;
	}

	/*
	 * this is needed for GUI, render multiple select only if this is false if this is true use the only choice
	 * 
	 * @author Wulf
	 */
	public boolean isSingleChoiceCollection() {
		return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

	}

	/*
	 * this is needed for GUI, render multiple select only if this is false if isSingleChoiceCollection is true use this choice
	 * 
	 * @author Wulf
	 */
	public String getDigitalCollectionIfSingleChoice() {
		List<String> pdc = getPossibleDigitalCollections();
		if (pdc.size() == 1) {
			return (String) pdc.get(0);
		} else {
			return null;
		}
	}

	public List<String> getPossibleDigitalCollections() {
		return possibleDigitalCollection;
	}

	@SuppressWarnings("unchecked")
	private void initializePossibleDigitalCollections() {
		possibleDigitalCollection = new ArrayList<String>();
		String filename = new Helper().getGoobiConfigDirectory() + "digitalCollections.xml";
		if (!(new File(filename).exists())) {
			Helper.setFehlerMeldung("File not found: ", filename);
			return;
		}

		try {
			/* Datei einlesen und Root ermitteln */
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(filename));
			Element root = doc.getRootElement();
			/* alle Projekte durchlaufen */
			List projekte = root.getChildren();
			for (Iterator iter = projekte.iterator(); iter.hasNext();) {
				Element projekt = (Element) iter.next();
				List projektnamen = projekt.getChildren("name");
				for (Iterator iterator = projektnamen.iterator(); iterator.hasNext();) {
					Element projektname = (Element) iterator.next();

					/*
					 * wenn der Projektname aufgeführt wird, dann alle Digitalen Collectionen in die Liste
					 */
					if (projektname.getText().equalsIgnoreCase(prozessKopie.getProjekt().getTitel())) {
						List myCols = projekt.getChildren("DigitalCollection");
						for (Iterator it2 = myCols.iterator(); it2.hasNext();) {
							Element col = (Element) it2.next();
							possibleDigitalCollection.add(col.getText());
						}
					}
				}
			}
		} catch (JDOMException e1) {
			myLogger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		} catch (IOException e1) {
			myLogger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		}

		// if only one collection is possible take it directly
		digitalCollections = new ArrayList<String>();
		if (isSingleChoiceCollection()) {
			digitalCollections.add(getDigitalCollectionIfSingleChoice());
		}
	}

	public List<String> getAllOpacCatalogues() {
		try {
			return new ConfigOpac().getAllCatalogueTitles();
		} catch (IOException e) {
			myLogger.error("Error while reading von opac-config", e);
			Helper.setFehlerMeldung("Error while reading von opac-config", e);
			return new ArrayList<String>();
		}
	}

	public List<ConfigOpacDoctype> getAllDoctypes() {
		try {
			return new ConfigOpac().getAllDoctypes();
		} catch (IOException e) {
			myLogger.error("Error while reading von opac-config", e);
			Helper.setFehlerMeldung("Error while reading von opac-config", e);
			return new ArrayList<ConfigOpacDoctype>();
		}
	}

	/*
	 * changed, so that on first request list gets set if there is only one choice
	 */
	public List<String> getDigitalCollections() {
		return digitalCollections;
	}

	public void setDigitalCollections(List<String> digitalCollections) {
		this.digitalCollections = digitalCollections;
	}

	public HashMap<String, Boolean> getStandardFields() {
		return standardFields;
	}

	public boolean isUseOpac() {
		return useOpac;
	}

	public boolean isUseTemplates() {
		return useTemplates;
	}

	public String getTifHeader_documentname() {
		return tifHeader_documentname;
	}

	public void setTifHeader_documentname(String tifHeader_documentname) {
		this.tifHeader_documentname = tifHeader_documentname;
	}

	public String getTifHeader_imagedescription() {
		return tifHeader_imagedescription;
	}

	public void setTifHeader_imagedescription(String tifHeader_imagedescription) {
		this.tifHeader_imagedescription = tifHeader_imagedescription;
	}

	public Prozess getProzessKopie() {
		return prozessKopie;
	}

	public void setProzessKopie(Prozess prozessKopie) {
		this.prozessKopie = prozessKopie;
	}

	public String getOpacSuchfeld() {
		return opacSuchfeld;
	}

	public void setOpacSuchfeld(String opacSuchfeld) {
		this.opacSuchfeld = opacSuchfeld;
	}

	public String getOpacKatalog() {
		return opacKatalog;
	}

	public void setOpacKatalog(String opacKatalog) {
		this.opacKatalog = opacKatalog;
	}

	public String getOpacSuchbegriff() {
		return opacSuchbegriff;
	}

	public void setOpacSuchbegriff(String opacSuchbegriff) {
		this.opacSuchbegriff = opacSuchbegriff;
	}

	/*
	 * ##################################################### ##################################################### ## ## Helper ##
	 * ##################################################### ####################################################
	 */

	/**
	 * Prozesstitel und andere Details generieren ================================================================
	 */
	@SuppressWarnings("unchecked")
	public void CalcProzesstitel() {
		String newTitle = "";
		String titeldefinition = "";
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(prozessVorlage.getProjekt());
		} catch (IOException e) {
			Helper.setFehlerMeldung("IOException", e.getMessage());
			return;
		}

		int count = cp.getParamList("createNewProcess.itemlist.processtitle").size();
		for (int i = 0; i < count; i++) {
			String titel = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")");
			String isdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isdoctype]");
			String isnotdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isnotdoctype]");

			if (titel == null)
				titel = "";
			if (isdoctype == null)
				isdoctype = "";
			if (isnotdoctype == null)
				isnotdoctype = "";

			/* wenn nix angegeben wurde, dann anzeigen */
			if (isdoctype.equals("") && isnotdoctype.equals("")) {
				titeldefinition = titel;
				break;
			}

			/* wenn beides angegeben wurde */
			if (!isdoctype.equals("") && !isnotdoctype.equals("") && isdoctype.contains(docType) && !isnotdoctype.contains(docType)) {
				titeldefinition = titel;
				break;
			}

			/* wenn nur pflicht angegeben wurde */
			if (isnotdoctype.equals("") && isdoctype.contains(docType)) {
				titeldefinition = titel;
				break;
			}
			/* wenn nur "darf nicht" angegeben wurde */
			if (isdoctype.equals("") && !isnotdoctype.contains(docType)) {
				titeldefinition = titel;
				break;
			}
		}

		StringTokenizer tokenizer = new StringTokenizer(titeldefinition, "+");
		/* jetzt den Bandtitel parsen */
		while (tokenizer.hasMoreTokens()) {
			String myString = tokenizer.nextToken();

			/*
			 * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
			 */
			if (myString.startsWith("'") && myString.endsWith("'"))
				newTitle += myString.substring(1, myString.length() - 1);
			else {
				/* andernfalls den string als Feldnamen auswerten */
				for (Iterator it2 = additionalFields.iterator(); it2.hasNext();) {
					AdditionalField myField = (AdditionalField) it2.next();

					/*
					 * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
					 */
					if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype()
							&& (myField.getWert() == null || myField.getWert().equals(""))) {
						myField.setWert(atstsl);
					}

					/* den Inhalt zum Titel hinzufügen */
					if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype() && myField.getWert() != null)
						newTitle += CalcProzesstitelCheck(myField.getTitel(), myField.getWert());
				}
			}
		}

		if (newTitle.endsWith("_"))
			newTitle = newTitle.substring(0, newTitle.length() - 1);
		prozessKopie.setTitel(newTitle);
		CalcTiffheader();
	}

	/* =============================================================== */

	private String CalcProzesstitelCheck(String inFeldName, String inFeldWert) {
		String rueckgabe = inFeldWert;

		/*
		 * -------------------------------- Bandnummer --------------------------------
		 */
		if (inFeldName.equals("Bandnummer")) {
			try {
				int bandint = Integer.parseInt(inFeldWert);
				java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
				rueckgabe = df.format(bandint);
			} catch (NumberFormatException e) {
				Helper.setFehlerMeldung("Ungültige Daten: ", "Bandnummer ist keine gültige Zahl");
			}
			if (rueckgabe != null && rueckgabe.length() < 4)
				rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
		}

		return rueckgabe;
	}

	/* =============================================================== */

	@SuppressWarnings("unchecked")
	public void CalcTiffheader() {
		String tif_definition = "";
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(prozessVorlage.getProjekt());
		} catch (IOException e) {
			Helper.setFehlerMeldung("IOException", e.getMessage());
			return;
		}

		tif_definition = cp.getParamString("tifheader." + docType, "blabla");

		/*
		 * -------------------------------- evtuelle Ersetzungen --------------------------------
		 */
		tif_definition = tif_definition.replaceAll("\\[\\[", "<");
		tif_definition = tif_definition.replaceAll("\\]\\]", ">");

		/*
		 * -------------------------------- Documentname ist im allgemeinen = Prozesstitel --------------------------------
		 */

		tifHeader_documentname = prozessKopie.getTitel();
		tifHeader_imagedescription = "";

		/*
		 * -------------------------------- Imagedescription --------------------------------
		 */
		StringTokenizer tokenizer = new StringTokenizer(tif_definition, "+");

		/* jetzt den Tiffheader parsen */
		while (tokenizer.hasMoreTokens()) {
			String myString = tokenizer.nextToken();
			// System.out.println(myString);
			/*
			 * wenn der String mit ' anf�ngt und mit ' endet, dann den Inhalt so übernehmen
			 */
			if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2)
				tifHeader_imagedescription += myString.substring(1, myString.length() - 1);
			else if (myString.equals("$Doctype")) {
				tifHeader_imagedescription += co.getDoctypeByName(docType).getTifHeaderType();
			} else {
				/* andernfalls den string als Feldnamen auswerten */
				for (Iterator it2 = additionalFields.iterator(); it2.hasNext();) {
					AdditionalField myField = (AdditionalField) it2.next();

					/*
					 * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
					 */
					if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype()
							&& (myField.getWert() == null || myField.getWert().equals(""))) {
						myField.setWert(atstsl);
					}

					/* den Inhalt zum Titel hinzufügen */
					if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype() && myField.getWert() != null)
						tifHeader_imagedescription += CalcProzesstitelCheck(myField.getTitel(), myField.getWert());
				}
			}
		}
	}
}
