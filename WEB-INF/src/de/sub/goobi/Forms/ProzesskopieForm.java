package de.sub.goobi.Forms;

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
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
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
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Werkstueckeigenschaft;
import de.sub.goobi.Import.ImportOpac;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.Persistence.HibernateUtil;
import de.sub.goobi.Persistence.ProzessDAO;
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

import org.goobi.production.flow.jobs.HistoryJob;

public class ProzesskopieForm {
	private static final Logger myLogger = Logger.getLogger(ProzesskopieForm.class);
	private Helper help = new Helper();
	UghHelper ughHelp = new UghHelper();
	private BeanHelper bhelp = new BeanHelper();
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
	
	public final static String DIRECTORY_SUFFIX = "_tif";

	/* =============================================================== */

	public String Prepare() {
		if (prozessVorlage.isContainsUnreachableSteps()) {
			for (Schritt s : prozessVorlage.getSchritteList()) {
				if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0)
					help.setFehlerMeldung("Kein Benutzer festgelegt für: ", s.getTitel());
			}
			return "";
		}

		clearValues();
		try {
			co = new ConfigOpac();
		} catch (IOException e) {
			myLogger.error("Error while reading von opac-config", e);
			help.setFehlerMeldung("Error while reading von opac-config", e);
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

		/* -------------------------------- 
		 * Kopie der Prozessvorlage anlegen
		 * -------------------------------- */
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
			help.setFehlerMeldung("IOException", e.getMessage());
			return;
		}

		docType = cp.getParamString("createNewProcess.defaultdoctype", co.getAllDoctypes().get(0).getTitle());
		useOpac = cp.getParamBoolean("createNewProcess.opac[@use]");
		useTemplates = cp.getParamBoolean("createNewProcess.templates[@use]");
		naviFirstPage = "ProzessverwaltungKopie1";
		if (opacKatalog.equals(""))
			opacKatalog = cp.getParamString("createNewProcess.opac.catalogue");

		/* -------------------------------- 
		 * die auszublendenden Standard-Felder ermitteln 
		 * --------------------------------*/
		for (String t : cp.getParamList("createNewProcess.itemlist.hide")) {
			standardFields.put(t, false);
		}

		/*
		 * -------------------------------- 
		 * die einzublendenen (zusätzlichen) Eigenschaften ermitteln 
		 * --------------------------------*/
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
			 * -------------------------------- 
			 * Bindung an ein Metadatum eines Docstructs 
			 * -------------------------------- */
			if (cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@ughbinding]")) {
				fa.setUghbinding(true);
				fa.setDocstruct(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@docstruct]"));
				fa.setMetadata(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@metadata]"));
			}

			/* -------------------------------- 
			 * prüfen, ob das aktuelle Item
			 * eine Auswahlliste werden soll 
			 * -------------------------------- */
			int selectItemCount = cp.getParamList("createNewProcess.itemlist.item(" + i + ").select").size();
			/* Children durchlaufen und SelectItems erzeugen */
			if (selectItemCount > 0)
				fa.setSelectList(new ArrayList<SelectItem>());
			for (int j = 0; j < selectItemCount; j++) {
				String svalue = cp
					.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")[@label]");
				String sid = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")");
				fa.getSelectList().add(new SelectItem(sid, svalue, null));
			}
			additionalFields.add(fa);
		}
	}

	/* =============================================================== */

	public List<SelectItem> getProzessTemplates() throws DAOException {
		List<SelectItem> myProzessTemplates = new ArrayList<SelectItem>();
	//	HibernateUtil.clearSession();
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Expression.eq("istTemplate", Boolean.valueOf(false)));
		crit.add(Expression.eq("inAuswahllisteAnzeigen", Boolean.valueOf(true)));
		crit.addOrder(Order.asc("titel"));

		/* Einschränkung auf bestimmte Projekte, wenn kein Admin */
		LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		Benutzer aktuellerNutzer = loginForm.getMyBenutzer();
		try {
			aktuellerNutzer = new BenutzerDAO().get(loginForm.getMyBenutzer().getId());
		} catch (DAOException e) {
			e.printStackTrace();
		}
		if (aktuellerNutzer != null) {
			/*
			 * wenn die maximale Berechtigung nicht Admin ist, dann nur
			 * bestimmte
			 */
			if (loginForm.getMaximaleBerechtigung() > 1) {
				Hibernate.initialize(aktuellerNutzer);
				Disjunction dis = Expression.disjunction();
				for (Projekt proj : aktuellerNutzer.getProjekteList()) {
					dis.add(Expression.eq("projekt", proj));
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
			myRdf = myImportOpac.OpacToDocStruct(opacSuchfeld, opacSuchbegriff, opacKatalog, prozessKopie
				.getRegelsatz().getPreferences());
			if (myImportOpac.getOpacDocType() != null)
				docType = myImportOpac.getOpacDocType().getTitle();

			// if (myImportOpac.isMonograph())
			// docType = "monograph";
			// if (myImportOpac.isContainedWork())
			// docType = "containedwork";
			// if (myImportOpac.isPeriodical())
			// docType = "periodical";
			// if (myImportOpac.isMultivolume())
			// docType = "multivolume";

			atstsl = myImportOpac.getAtstsl();
			fillFieldsFromMetadataFile();

			/* über die Treffer informieren */
			if (myImportOpac.getHitcount() == 0)
				help.setFehlerMeldung("keine Treffer gefunden", "");
			if (myImportOpac.getHitcount() > 1)
				help.setMeldung(null, "mehrere Treffer gefunden", " - Daten des ersten Treffers übernommen");
		} catch (Exception e) {
			help.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
			e.printStackTrace();
		}
		return "";
	}

	/* =============================================================== */

	/**
	 * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei
	 * füllen
	 * 
	 * @throws PreferencesException
	 */
	private void fillFieldsFromMetadataFile() throws PreferencesException {
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
								for(Person p: myTempStruct.getAllPersons()){
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
							MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(),
								field.getMetadata());
							Metadata md = ughHelp.getMetadata(myTempStruct, mdt);
							field.setWert(md.getValue());
						}
					} catch (UghHelperException e) {
						help.setFehlerMeldung(e.getMessage(), "");
					}
				} // end if ughbinding
			}// end for
		} // end if myrdf==null
	}

	/**
	 * alle Konfigurationseigenschaften und Felder zurücksetzen
	 * ================================================================
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
	 *             ==============================================================
	 *             ==
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
			for (Vorlageeigenschaft eig: vor.getEigenschaften()) {
				for (AdditionalField field : additionalFields) {
					if (field.getTitel().equals(eig.getTitel()))
						field.setWert(eig.getWert());
				}
			}
		}

		try {
			myRdf = tempProzess.readMetadataAsTemplateFile();
		} catch (Exception e) {
			help.setFehlerMeldung("Fehler beim Einlesen der Template-Metadaten ", e);
		}

		/* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
		try {
			DocStruct colStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
			removeCollections(colStruct);
			colStruct = colStruct.getAllChildren().get(0);
			removeCollections(colStruct);
		} catch (PreferencesException e) {
			help.setFehlerMeldung("Fehler beim Anlegen des Vorgangs", e);
			myLogger.error("Fehler beim Anlegen des Vorgangs", e);
		} catch (RuntimeException e) {
			/*
			 * das Firstchild unterhalb des Topstructs konnte nicht ermittelt
			 * werden
			 */
		}

		return "";
	}

	/**
	 * Validierung der Eingaben
	 * 
	 * @return sind Fehler bei den Eingaben vorhanden?
	 *         ================================================================
	 */
	private boolean isContentValid() {
		/* -------------------------------- 
		 * Vorbedingungen prüfen
		 * -------------------------------- */
		boolean valide = true;

		/* -------------------------------- 
		 * grundsätzlich den Vorgangstitel prüfen 
		 * --------------------------------*/
		/* kein Titel */
		if (prozessKopie.getTitel() == null || prozessKopie.getTitel().equals("")) {
			valide = false;
			help.setFehlerMeldung("UnvollstaendigeDaten: ", "kein Vorgangstitel angegeben");
		}

		// if (!prozessKopie.getTitel().matches("[\\w-]*")) {
		String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
		if (!prozessKopie.getTitel().matches(validateRegEx)) {
			valide = false;
			help.setFehlerMeldung("UngueltigerTitelFuerVorgang");
		}

		/* prüfen, ob der Prozesstitel schon verwendet wurde */
		if (prozessKopie.getTitel() != null) {
			long anzahl = 0;
			try {
				anzahl = new ProzessDAO().count("from Prozess where titel='" + prozessKopie.getTitel() + "'");
			} catch (DAOException e) {
				new Helper().setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
				valide = false;
			}
			if (anzahl > 0) {
				valide = false;
				help.setFehlerMeldung("UngueltigeDaten: ", "der Vorgangstitel wird bereits verwendet");
			}
		}

		/* -------------------------------- 
		 * Prüfung der standard-Eingaben, die angegeben werden müssen 
		 * -------------------------------- */
		/* keine Collektion ausgewählt */
		if (standardFields.get("collections") && getDigitalCollections().size() == 0) {
			valide = false;
			help.setFehlerMeldung("UnvollstaendigeDaten: ", "keine Kollektion angegeben");
		}

		/*
		 * -------------------------------- 
		 * Prüfung der additional-Eingaben, die angegeben werden müssen 
		 * --------------------------------*/
		for (AdditionalField field : additionalFields) {
			if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype()
					&& (StringUtils.isBlank(field.getWert()))) {
				valide = false;
				help.setFehlerMeldung("UnvollstaendigeDaten: ", field.getTitel() + " nicht angegeben");
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
	 * Anlegen des Prozesses und Speichern der Metadaten
	 * ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 * @throws WriteException
	 */
	public String NeuenProzessAnlegen() throws ReadException, IOException, InterruptedException, PreferencesException,
	SwapException, DAOException, WriteException {
		Helper.getHibernateSession().evict(prozessKopie);
		
		prozessKopie.setId(null);
		if (!isContentValid())
			return naviFirstPage;
		EigenschaftenHinzufuegen();
		
		/*
		 * -------------------------------- 
		 * jetzt in der Prozesskopie für alle bereits abgeschlossenen Schritte ein Bearbeitungsdatum und einen
		 * Benutzer eintragen 
		 * --------------------------------*/
		// for (Iterator iter = prozessKopie.getSchritteList().iterator();
		// iter.hasNext();) {
		// Schritt step = (Schritt) iter.next();
		// if (step.getBearbeitungsstatus().intValue() == 3) {
		// step.setBearbeitungsbeginn(new Date());
		// step.setBearbeitungszeitpunkt(new Date());
		// step.setBearbeitungsende(new Date());
		// LoginForm loginForm = (LoginForm)
		// Helper.getManagedBeanValue("#{LoginForm}");
		// step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
		// }
		// }
		for (Schritt step : prozessKopie.getSchritteList()) {
			/* --------------------------------
			 * always save date and user for each step
			 * --------------------------------*/
			step.setBearbeitungszeitpunkt(prozessKopie.getErstellungsdatum());
			step.setEditTypeEnum(StepEditType.AUTOMATIC);
			LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
			if (loginForm != null)
				step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
			
			/* --------------------------------
			 * only if its done, set edit start and end date
			 * --------------------------------*/
			if (step.getBearbeitungsstatusEnum()==StepStatus.DONE) {
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
			return "";
		}
		
		/*
		 * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage
		 * stattfand, dann jetzt eine anlegen
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
							 * das Firstchild unterhalb des Topstructs konnte
							 * nicht ermittelt werden
							 */
						}
					}
					/*
					 * falls topstruct und firstchild das Metadatum bekommen
					 * sollen
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
						 * bis auf die Autoren alle additionals in die Metadaten
						 * übernehmen
						 */
						if (!field.getMetadata().equals("ListOfCreators")) {
							MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(),
								field.getMetadata());
							Metadata md = ughHelp.getMetadata(myTempStruct, mdt);
							/*
							 * wenn das Metadatum null ist, dann jetzt
							 * initialisieren
							 */
//							if (md.getType() == null) {
//								md = new Metadata(mdt);
//								md.setDocStruct(myTempStruct);
//								myTempStruct.addMetadata(md);
//							}
							md.setValue(field.getWert());
							/*
							 * wenn dem Topstruct und dem Firstchild der Wert
							 * gegeben werden soll
							 */
							if (myTempChild != null) {
								md = ughHelp.getMetadata(myTempChild, mdt);
//								if (md.getType() == null) {
//									md = new Metadata(mdt);
//									md.setDocStruct(myTempChild);
//									myTempChild.addMetadata(md);
//								}
								md.setValue(field.getWert());
							}
						}
					} catch (Exception e) {
						help.setFehlerMeldung(e);
						e.printStackTrace();
					}
				} // end if ughbinding
			}// end for
		
			/* -------------------------------- 
			 * Collectionen hinzufügen
			 *  -------------------------------- */
			DocStruct colStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
			try {
				addCollections(colStruct);
				/* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
				colStruct = colStruct.getAllChildren().get(0);
				addCollections(colStruct);
			} catch (RuntimeException e) {
				/* das Firstchild unterhalb des Topstructs konnte nicht
				 * ermittelt werden */
			}
		
			/* -------------------------------- 
			 * Imagepfad hinzufügen (evtl. vorhandene zunächst löschen) 
			 * -------------------------------- */
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
		
				/* -------------------------------- 
				 * soll der Prozess als Vorlage verwendet werden? 
				 * -------------------------------- */
				if (useTemplates && prozessKopie.isInAuswahllisteAnzeigen())
					prozessKopie.writeMetadataAsTemplateFile(myRdf);
		
				
			} catch (ugh.exceptions.DocStructHasNoTypeException e) {
				help.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			} catch (UghHelperException e) {
				help.setFehlerMeldung("UghHelperException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			} catch (MetadataTypeNotAllowedException e) {
				help.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());
				myLogger.error("creation of new process throws an error: ", e);
			}
		
		}

		//Adding process to history
		if(!HistoryJob.updateHistoryForProcess(prozessKopie)){
			help.setFehlerMeldung("historyNotUpdated");
		}else{
			try {
				new ProzessDAO().save(prozessKopie);
			} catch (DAOException e) {
				e.printStackTrace();
				myLogger.error("error on save: ", e);
				return "";
			}
		}
		
		prozessKopie.readMetadataFile();
		
		/* damit die Sortierung stimmt nochmal einlesen */
		Helper.getHibernateSession().refresh(prozessKopie);
		return "ProzessverwaltungAlle";
		
	}


	/* =============================================================== */

	private void addCollections(DocStruct colStruct) {
		for (String s: digitalCollections) {
			try {
				Metadata md = new Metadata(ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection"));
				md.setValue(s);
				md.setDocStruct(colStruct);
				colStruct.addMetadata(md);
			} catch (UghHelperException e) {
				help.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			} catch (DocStructHasNoTypeException e) {
				help.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			} catch (MetadataTypeNotAllowedException e) {
				help.setFehlerMeldung(e.getMessage(), "");
				e.printStackTrace();
			}
		}
	}

	/**
	 * alle Kollektionen eines übergebenen DocStructs entfernen
	 * ================================================================
	 */
	private void removeCollections(DocStruct colStruct) {
		try {
			MetadataType mdt = ughHelp.getMetadataType(prozessKopie.getRegelsatz().getPreferences(),
				"singleDigCollection");
			ArrayList<Metadata> myCollections = new ArrayList<Metadata>(colStruct.getAllMetadataByType(mdt));
			if (myCollections != null && myCollections.size() > 0) {
				for (Metadata md : myCollections) {
					colStruct.removeMetadata(md);
				}
			}
		} catch (UghHelperException e) {
			help.setFehlerMeldung(e.getMessage(), "");
			e.printStackTrace();
		} catch (DocStructHasNoTypeException e) {
			help.setFehlerMeldung(e.getMessage(), "");
			e.printStackTrace();
		}
	}

	/* =============================================================== */

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
			if (co.getDoctypeByName(docType).isPeriodical()) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("Periodical");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("PeriodicalVolume");
				DocStruct dsvolume = dd.createDocStruct(dstyvolume);
				ds.addChild(dsvolume);
				myRdf = ff;
			}

			/* MultivolumeBand */
			if (co.getDoctypeByName(docType).isMultiVolume()) {
				DocStructType dsty = myPrefs.getDocStrctTypeByName("MultiVolumeWork");
				DocStruct ds = dd.createDocStruct(dsty);
				dd.setLogicalDocStruct(ds);

				DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Volume");
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
		/* -------------------------------- 
		 * Vorlageneigenschaften initialisieren
		 * -------------------------------- */
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

		/* -------------------------------- 
		 * Werkstückeigenschaften initialisieren 
		 * --------------------------------*/
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

		/* -------------------------------- 
		 * jetzt alle zusätzlichen Felder durchlaufen und die Werte hinzufügen 
		 * --------------------------------*/
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

	/* this is needed for GUI, render multiple select only if this is false
	 * if this is true use the only choice
	 * @author Wulf
	 */
	public boolean isSingleChoiceCollection() {
		return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

	}

	/* this is needed for GUI, render multiple select only if this is false
	 * if isSingleChoiceCollection is true use this choice
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
		String filename = new Helper().getGoobiConfigDirectory() + "digitalCollections.xml";
		if (!(new File(filename).exists())) {
			help.setFehlerMeldung("File not found: ", filename);
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
					// System.out.println(projektname.getText() +
					// " - soll sein: " + prozessKopie.getProjekt().getTitel());

					/*
					 * wenn der Projektname aufgeführt wird, dann alle Digitalen
					 * Collectionen in die Liste
					 */
					if (projektname.getText().equalsIgnoreCase(prozessKopie.getProjekt().getTitel())) {
						List myCols = projekt.getChildren("DigitalCollection");
						for (Iterator it2 = myCols.iterator(); it2.hasNext();) {
							Element col = (Element) it2.next();
							// System.out.println(col.getText());
							possibleDigitalCollection.add(col.getText());
						}
					}
				}
			}
		} catch (JDOMException e1) {
			myLogger.error("error while parsing digital collections", e1);
			help.setFehlerMeldung("Error while parsing digital collections", e1);
		} catch (IOException e1) {
			myLogger.error("error while parsing digital collections", e1);
			help.setFehlerMeldung("Error while parsing digital collections", e1);
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
			help.setFehlerMeldung("Error while reading von opac-config", e);
			return new ArrayList<String>();
		}
	}

	public List<ConfigOpacDoctype> getAllDoctypes() {
		try {
			return new ConfigOpac().getAllDoctypes();
		} catch (IOException e) {
			myLogger.error("Error while reading von opac-config", e);
			help.setFehlerMeldung("Error while reading von opac-config", e);
			return new ArrayList<ConfigOpacDoctype>();
		}
	}

	/*
	 * changed, so that on first request list gets set
	 * if there is only one choice
	 */
	public List getDigitalCollections() {
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
	 * #####################################################
	 * ##################################################### ## ## Helper ##
	 * #####################################################
	 * ####################################################
	 */

	/**
	 * Prozesstitel und andere Details generieren
	 * ================================================================
	 */
	public void CalcProzesstitel() {
		String newTitle = "";
		String titeldefinition = "";
		ConfigProjects cp = null;
		try {
			cp = new ConfigProjects(prozessVorlage.getProjekt());
		} catch (IOException e) {
			help.setFehlerMeldung("IOException", e.getMessage());
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
			if (!isdoctype.equals("") && !isnotdoctype.equals("") && isdoctype.contains(docType)
					&& !isnotdoctype.contains(docType)) {
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
			// System.out.println(myString);
			/*
			 * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
			 * übernehmen
			 */
			if (myString.startsWith("'") && myString.endsWith("'"))
				newTitle += myString.substring(1, myString.length() - 1);
			else {
				/* andernfalls den string als Feldnamen auswerten */
				for (Iterator it2 = additionalFields.iterator(); it2.hasNext();) {
					AdditionalField myField = (AdditionalField) it2.next();

					/*
					 * wenn es das ATS oder TSL-Feld ist, dann den berechneten
					 * atstsl einsetzen, sofern noch nicht vorhanden
					 */
					if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL"))
							&& myField.getShowDependingOnDoctype()
							&& (myField.getWert() == null || myField.getWert().equals(""))) {
						myField.setWert(atstsl);
					}

					/* den Inhalt zum Titel hinzufügen */
					if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype()
							&& myField.getWert() != null)
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
		 * -------------------------------- Bandnummer
		 * --------------------------------
		 */
		if (inFeldName.equals("Bandnummer")) {
			try {
				int bandint = Integer.parseInt(inFeldWert);
				java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
				rueckgabe = df.format(bandint);
			} catch (NumberFormatException e) {
				help.setFehlerMeldung("Ungültige Daten: ", "Bandnummer ist keine gültige Zahl");
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
			help.setFehlerMeldung("IOException", e.getMessage());
			return;
		}
		// if (docType.equals("monograph"))
		// tif_definition = cp.getParamString("tifheader.monograph");
		// if (docType.equals("containedwork"))
		// tif_definition = cp.getParamString("tifheader.containedwork");
		// if (docType.equals("multivolume"))
		// tif_definition = cp.getParamString("tifheader.multivolume");
		// if (docType.equals("periodical"))
		// tif_definition = cp.getParamString("tifheader.periodical");
		// if (docType.equals("volume"))
		// tif_definition = cp.getParamString("tifheader.volume");
		tif_definition = cp.getParamString("tifheader." + docType, "blabla");

		/*
		 * -------------------------------- evtuelle Ersetzungen
		 * --------------------------------
		 */
		tif_definition = tif_definition.replaceAll("\\[\\[", "<");
		tif_definition = tif_definition.replaceAll("\\]\\]", ">");

		/*
		 * -------------------------------- Documentname ist im allgemeinen =
		 * Prozesstitel --------------------------------
		 */
		// if (tifHeader_documentname.equals(""))
		tifHeader_documentname = prozessKopie.getTitel();
		tifHeader_imagedescription = "";
		/*
		 * -------------------------------- Imagedescription
		 * --------------------------------
		 */
		// if (tifHeader_imagedescription.equals("")) {
		StringTokenizer tokenizer = new StringTokenizer(tif_definition, "+");
		/* jetzt den Tiffheader parsen */
		while (tokenizer.hasMoreTokens()) {
			String myString = tokenizer.nextToken();
			// System.out.println(myString);
			/*
			 * wenn der String mit ' anf�ngt und mit ' endet, dann den Inhalt so
			 * übernehmen
			 */
			if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2)
				tifHeader_imagedescription += myString.substring(1, myString.length() - 1);
			else if (myString.equals("$Doctype")) {
				/* wenn der Doctype angegeben werden soll */
				// if (docType.equals("monograph"))
				// tifHeader_imagedescription += "Monographie";
				// if (docType.equals("volume"))
				// tifHeader_imagedescription += "Volume";
				// if (docType.equals("containedwork"))
				// tifHeader_imagedescription += "ContainedWork";
				// if (docType.equals("multivolume"))
				// tifHeader_imagedescription += "Band_MultivolumeWork";
				// if (docType.equals("periodical"))
				// tifHeader_imagedescription += "Band_Zeitschrift";
				tifHeader_imagedescription += co.getDoctypeByName(docType).getTifHeaderType();
			} else {
				/* andernfalls den string als Feldnamen auswerten */
				for (Iterator it2 = additionalFields.iterator(); it2.hasNext();) {
					AdditionalField myField = (AdditionalField) it2.next();

					/*
					 * wenn es das ATS oder TSL-Feld ist, dann den berechneten
					 * atstsl einsetzen, sofern noch nicht vorhanden
					 */
					if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL"))
							&& myField.getShowDependingOnDoctype()
							&& (myField.getWert() == null || myField.getWert().equals(""))) {
						myField.setWert(atstsl);
					}

					/* den Inhalt zum Titel hinzufügen */
					if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype()
							&& myField.getWert() != null)
						tifHeader_imagedescription += CalcProzesstitelCheck(myField.getTitel(), myField.getWert());
				}
			}
			// }
		}
	}
}
