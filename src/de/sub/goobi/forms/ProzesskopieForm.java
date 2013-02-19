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

import java.io.File;
import java.io.FileNotFoundException;
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
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.XStream;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.importer.ImportOpac;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigOpac;
import de.sub.goobi.config.ConfigOpacDoctype;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Messages;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class ProzesskopieForm {
	private static final Logger myLogger = Logger.getLogger(ProzesskopieForm.class);
	private Helper help = new Helper();
	UghHelper ughHelper = new UghHelper();
	private BeanHelper bHelper = new BeanHelper();
	private Fileformat myRdf;
	private String opacSuchfeld = "12";
	private String opacSuchbegriff;
	private String opacKatalog;
	private Prozess prozessVorlage = new Prozess();
	private Prozess prozessKopie = new Prozess();
	private ImportOpac myImportOpac = new ImportOpac();
	private ConfigOpac co;
	/* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
	private boolean useOpac;
	private boolean useTemplates;

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
	private Integer guessedImages = 0;

	public final static String DIRECTORY_SUFFIX = "_tif";

	public String Prepare() {
		if (prozessVorlage.isContainsUnreachableSteps()) {
			for (Schritt s : prozessVorlage.getSchritteList()) {
				if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0)
					Helper.setFehlerMeldung("No user associated for: ", s.getTitel());
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
		myRdf = null;
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
		bHelper.SchritteKopieren(prozessVorlage, prozessKopie);
		bHelper.ScanvorlagenKopieren(prozessVorlage, prozessKopie);
		bHelper.WerkstueckeKopieren(prozessVorlage, prozessKopie);
		bHelper.EigenschaftenKopieren(prozessVorlage, prozessKopie);

		initializePossibleDigitalCollections();

		return naviFirstPage;
	}

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
			if (cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@autogenerated]")) {
				fa.setAutogenerated(true);
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

	public List<SelectItem> getProzessTemplates() throws DAOException {
		List<SelectItem> myProzessTemplates = new ArrayList<SelectItem>();

		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.add(Restrictions.eq("inAuswahllisteAnzeigen", Boolean.valueOf(true)));
		crit.addOrder(Order.asc("titel"));

		/* Einschränkung auf bestimmte Projekte, wenn kein Admin */
		LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		Benutzer aktuellerNutzer = loginForm.getMyBenutzer();
		try {
			aktuellerNutzer = new BenutzerDAO().get(loginForm.getMyBenutzer().getId());
		} catch (DAOException e) {
			myLogger.error(e);
		}
		if (aktuellerNutzer != null) {
			/*
			 * wenn die maximale Berechtigung nicht Admin ist, dann nur bestimmte
			 */
			if (loginForm.getMaximaleBerechtigung() > 1) {
				Hibernate.initialize(aktuellerNutzer);
				Disjunction dis = Restrictions.disjunction();
				for (Projekt proj : aktuellerNutzer.getProjekteList()) {
					dis.add(Restrictions.eq("projekt", proj));
				}
				crit.add(dis);
			}
		}

		for (Object proz : crit.list()) {
			myProzessTemplates.add(new SelectItem(((Prozess) proz).getId(), ((Prozess) proz).getTitel(), null));
		}
		return myProzessTemplates;
	}

	/* =============================================================== */

	/**
	 * OpacAnfrage
	 */
	public String OpacAuswerten() {
		clearValues();
		readProjectConfigs();
		try {
			/* den Opac abfragen und ein RDF draus bauen lassen */
			myRdf = myImportOpac.OpacToDocStruct(opacSuchfeld, opacSuchbegriff, opacKatalog, prozessKopie.getRegelsatz().getPreferences());
			if (myImportOpac.getOpacDocType() != null)
				docType = myImportOpac.getOpacDocType().getTitle();
			atstsl = myImportOpac.getAtstsl();
			fillFieldsFromMetadataFile();
			/* über die Treffer informieren */
			if (myImportOpac.getHitcount() == 0)
				Helper.setFehlerMeldung("No hit found", "");
			if (myImportOpac.getHitcount() > 1)
				Helper.setMeldung(null, "Found more then one hit", " - use first hit");
		} catch (Exception e) {
			Helper.setFehlerMeldung("Error on reading opac ", e);
		}
		return "";
	}

	/* =============================================================== */

	/**
	 * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei füllen
	 * 
	 * @throws PreferencesException
	 */
	private void fillFieldsFromMetadataFile() throws PreferencesException {
		if (myRdf != null) {

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
							MetadataType mdt = ughHelper.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
							Metadata md = ughHelper.getMetadata(myTempStruct, mdt);
							if (md != null) {
								field.setWert(md.getValue());
							}
						}
					} catch (UghHelperException e) {
						myLogger.error(e);
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
		standardFields.put("images", true);
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
			Helper.setFehlerMeldung("Error on reading template-metadata ", e);
		}

		/* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
		try {
			DocStruct colStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
			removeCollections(colStruct);
			colStruct = colStruct.getAllChildren().get(0);
			removeCollections(colStruct);
		} catch (PreferencesException e) {
			Helper.setFehlerMeldung("Error on creating process", e);
			myLogger.error("Error on creating process", e);
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
			Helper.setFehlerMeldung(Messages.getString("UnvollstaendigeDaten") + " Process title is empty");
		}

		String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
		if (!prozessKopie.getTitel().matches(validateRegEx)) {
			valide = false;
			Helper.setFehlerMeldung(Messages.getString("UngueltigerTitelFuerVorgang"));
		}

		/* prüfen, ob der Prozesstitel schon verwendet wurde */
		if (prozessKopie.getTitel() != null) {
			long anzahl = 0;
			try {
				anzahl = new ProzessDAO().count("from Prozess where titel='" + prozessKopie.getTitel() + "'");
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Error on reading process information", e.getMessage());
				valide = false;
			}
			if (anzahl > 0) {
				valide = false;
				Helper.setFehlerMeldung(Messages.getString("UngueltigeDaten: ") + " the process title is allready used");
			}
		}

		/*
		 * -------------------------------- Prüfung der standard-Eingaben, die angegeben werden müssen --------------------------------
		 */
		/* keine Collektion ausgewählt */
		if (standardFields.get("collections") && getDigitalCollections().size() == 0) {
			valide = false;
			Helper.setFehlerMeldung(Messages.getString("UnvollstaendigeDaten") + " digital collection is empty");
		}

		/*
		 * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
		 */
		for (AdditionalField field : additionalFields) {
			if ((field.getWert() == null || field.getWert().equals("")) && field.isRequired() && field.getShowDependingOnDoctype() && (StringUtils.isBlank(field.getWert()))) {
				valide = false;
				Helper.setFehlerMeldung(Messages.getString("UnvollstaendigeDaten") + " " + field.getTitel() + " is empty");
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
	public String NeuenProzessAnlegen() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
			WriteException {
		Helper.getHibernateSession().evict(prozessKopie);

		prozessKopie.setId(null);
		if (!isContentValid())
			return naviFirstPage;
		EigenschaftenHinzufuegen();
		prozessKopie.setWikifield(prozessVorlage.getWikifield());


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
			prozessKopie.setSortHelperImages(guessedImages);
			ProzessDAO dao = new ProzessDAO();
			dao.save(prozessKopie);
			dao.refresh(prozessKopie);
		} catch (DAOException e) {
			myLogger.error(e);
			myLogger.error("error on save: ", e);
			return "";
		}

		/*
		 * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
		 */
		if (myRdf == null)
			createNewFileformat();

		/*-------------------------------- 
		 * wenn eine RDF-Konfiguration
		 * vorhanden ist (z.B. aus dem Opac-Import, oder frisch angelegt), dann
		 * diese ergänzen 
		 * --------------------------------*/
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
							MetadataType mdt = ughHelper.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
							Metadata md = ughHelper.getMetadata(myTempStruct, mdt);
							if (md != null) {
								md.setValue(field.getWert());
							}
							/*
							 * wenn dem Topstruct und dem Firstchild der Wert gegeben werden soll
							 */
							if (myTempChild != null) {
								md = ughHelper.getMetadata(myTempChild, mdt);
								if (md != null) {
									md.setValue(field.getWert());
								}
							}
						}
					} catch (Exception e) {
						Helper.setFehlerMeldung(e);

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
				// UghHelper ughhelp = new UghHelper();
				MetadataType mdt = ughHelper.getMetadataType(prozessKopie, "pathimagefiles");
				List<? extends Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
				if (alleImagepfade != null && alleImagepfade.size() > 0) {
					for (Metadata md : alleImagepfade) {
						myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
					}
				}
				Metadata newmd = new Metadata(mdt);
				if (SystemUtils.IS_OS_WINDOWS) {
					newmd.setValue("file:/" + prozessKopie.getImagesDirectory() + prozessKopie.getTitel().trim() + DIRECTORY_SUFFIX);
				} else {
					newmd.setValue("file://" + prozessKopie.getImagesDirectory() + prozessKopie.getTitel().trim() + DIRECTORY_SUFFIX);
				}
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
			return "";
		} else {
			try {
				new ProzessDAO().save(prozessKopie);
			} catch (DAOException e) {
				myLogger.error(e);
				myLogger.error("error on save: ", e);
				return "";
			}
		}

		prozessKopie.readMetadataFile();

		/* damit die Sortierung stimmt nochmal einlesen */
		Helper.getHibernateSession().refresh(prozessKopie);
		return "ProzessverwaltungKopie3";

	}

	/* =============================================================== */

	private void addCollections(DocStruct colStruct) {
		for (String s : digitalCollections) {
			try {
				Metadata md = new Metadata(ughHelper.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection"));
				md.setValue(s);
				md.setDocStruct(colStruct);
				colStruct.addMetadata(md);
			} catch (UghHelperException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");

			} catch (DocStructHasNoTypeException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");

			} catch (MetadataTypeNotAllowedException e) {
				Helper.setFehlerMeldung(e.getMessage(), "");

			}
		}
	}

	/**
	 * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
	 */
	private void removeCollections(DocStruct colStruct) {
		try {
			MetadataType mdt = ughHelper.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection");
			ArrayList<Metadata> myCollections = new ArrayList<Metadata>(colStruct.getAllMetadataByType(mdt));
			if (myCollections != null && myCollections.size() > 0) {
				for (Metadata md : myCollections) {
					colStruct.removeMetadata(md);
				}
			}
		} catch (UghHelperException e) {
			Helper.setFehlerMeldung(e.getMessage(), "");
			myLogger.error(e);
		} catch (DocStructHasNoTypeException e) {
			Helper.setFehlerMeldung(e.getMessage(), "");
			myLogger.error(e);
		}
	}

	/* =============================================================== */

	/**
	*
	* @return returns created internal used fiel format object
	*/
	public Fileformat createAndReturnNewFileformatObject() {
		createNewFileformat();
		return myRdf;
	}

	private void createNewFileformat() {
		Prefs myPrefs = prozessKopie.getRegelsatz().getPreferences();
		try {
			DigitalDocument dd = new DigitalDocument();
			Fileformat ff = new XStream(myPrefs);
			ff.setDigitalDocument(dd);
			/* BoundBook hinzufügen */
			DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
			DocStruct dsBoundBook = dd.createDocStruct(dst);
			dd.setPhysicalDocStruct(dsBoundBook);

			/* Monographie */
			if (!co.getDoctypeByName(docType).isPeriodical() && !co.getDoctypeByName(docType).isMultiVolume()) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName(co.getDoctypeByName(docType).getRulesetType());
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);
				myRdf = ff;
			}

			/* Zeitschrift */
			else if (co.getDoctypeByName(docType).isPeriodical()) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("Periodical");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("PeriodicalVolume");
				DocStruct dsvolume = dd.createDocStruct(dstyvolume);
				ds.addChild(dsvolume);
				myRdf = ff;
			}

			/* MultivolumeBand */
			else if (co.getDoctypeByName(docType).isMultiVolume()) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("MultiVolumeWork");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Volume");
				DocStruct dsvolume = dd.createDocStruct(dstyvolume);
				ds.addChild(dsvolume);
				myRdf = ff;
			}
			if (docType.equals("volumerun")) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("VolumeRun");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Record");
				DocStruct dsvolume = dd.createDocStruct(dstyvolume);
				ds.addChild(dsvolume);
				myRdf = ff;
			}
			if (docType.equals("bundleOfPapers")) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("BundleOfPapers");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("PortFolio");
				DocStruct dsvolume = dd.createDocStruct(dstyvolume);
				ds.addChild(dsvolume);
				myRdf = ff;
			}

		} catch (TypeNotAllowedForParentException e) {
			myLogger.error(e);
		} catch (TypeNotAllowedAsChildException e) {
			myLogger.error(e);
		} catch (PreferencesException e) {
			myLogger.error(e);
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

	private void initializePossibleDigitalCollections() {
		possibleDigitalCollection = new ArrayList<String>();
		try{
			possibleDigitalCollection = DigitalCollections.possibleDigitalCollectionsForProcess(prozessKopie);
		} catch (FileNotFoundException e1) {
			myLogger.error("File not found: ", e1);
			Helper.setFehlerMeldung("File not found: ", e1);
		} catch (JDOMException e1) {
			myLogger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		} catch (IOException e1) {
			myLogger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		} finally {
			if(possibleDigitalCollection == null)
				possibleDigitalCollection = new ArrayList<String>();
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
	public void CalcProzesstitel() {
		int counter = 0;
		for (AdditionalField field : additionalFields) {
			if (field.getAutogenerated() && field.getWert().isEmpty()) {
				field.setWert(String.valueOf(System.currentTimeMillis() + counter));
				counter++;
			}
		}
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
				for (Iterator<AdditionalField> it2 = additionalFields.iterator(); it2.hasNext();) {
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

		// remove non-ascii characters for the sake of TIFF header limits
		String filteredTitle = newTitle.replaceAll("[^\\p{ASCII}]", "");
		prozessKopie.setTitel(filteredTitle);

		CalcTiffheader();
	}

	/* =============================================================== */

	private String CalcProzesstitelCheck(String inFeldName, String inFeldWert) {
		String rueckgabe = inFeldWert;

		/*
		 * -------------------------------- Bandnummer --------------------------------
		 */
		if (inFeldName.equals("Bandnummer") || inFeldName.equals("Volume number")) {
			try {
				int bandint = Integer.parseInt(inFeldWert);
				java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
				rueckgabe = df.format(bandint);
			} catch (NumberFormatException e) {
				if (inFeldName.equals("Bandnummer")) {
					Helper.setFehlerMeldung(Messages.getString("UngueltigeDaten: ") + "Bandnummer ist keine gültige Zahl");
				} else {
					Helper.setFehlerMeldung(Messages.getString("UngueltigeDaten: ") + "Volume number is not a valid number");
				}
			}
			if (rueckgabe != null && rueckgabe.length() < 4)
				rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
		}

		return rueckgabe;
	}

	/* =============================================================== */

	public void CalcTiffheader() {
		String tif_definition = "";
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(prozessVorlage.getProjekt());
		} catch (IOException e) {
			Helper.setFehlerMeldung("IOException", e.getMessage());
			return;
		}

		tif_definition = cp.getParamString("tifheader." + docType, "intranda");

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
		String title = "";
		while (tokenizer.hasMoreTokens()) {
			String myString = tokenizer.nextToken();
			/*
			 * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
			 */
			if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2)
				tifHeader_imagedescription += myString.substring(1, myString.length() - 1);
			else if (myString.equals("$Doctype")) {
				tifHeader_imagedescription += co.getDoctypeByName(docType).getTifHeaderType();
			} else {
				/* andernfalls den string als Feldnamen auswerten */
				for (Iterator<AdditionalField> it2 = additionalFields.iterator(); it2.hasNext();) {
					AdditionalField myField = (AdditionalField) it2.next();
					if ((myField.getTitel().equals("Titel") || myField.getTitel().equals("Title")) && myField.getWert()!=null && !myField.getWert().equals("") ) {
						title = myField.getWert();
					}
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
			// reduce to 255 character
		}
		int length = tifHeader_imagedescription.length();
		if (length > 255) {
			try {
				int toCut = length - 255;
				String newTitle = title.substring(0, title.length() - toCut);
				tifHeader_imagedescription = tifHeader_imagedescription.replace(title, newTitle);
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
			}
		}
	}

	public String downloadDocket() {
		return	prozessKopie.downloadDocket();
	}

	/**
	 * @param imagesGuessed
	 *            the imagesGuessed to set
	 */
	public void setImagesGuessed(Integer imagesGuessed) {
		if (imagesGuessed == null) {
			imagesGuessed = 0;
		}
		this.guessedImages = imagesGuessed;
	}

	/**
	 * @return the imagesGuessed
	 */
	public Integer getImagesGuessed() {
		return guessedImages;
	}
}
