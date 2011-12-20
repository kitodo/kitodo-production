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

package de.sub.goobi.metadaten;

//TODO: Use generics
//TODO: Don't use Iterators
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.goobi.api.display.Modes;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.importer.ImportOpac;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import de.sub.goobi.helper.Transliteration;
import de.sub.goobi.helper.TreeNode;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
public class Metadaten {
	private static final Logger myLogger = Logger.getLogger(Metadaten.class);
	MetadatenImagesHelper imagehelper;
	MetadatenHelper metahelper;
	private boolean treeReloaden = false;
	String ocrResult = "";
	private Fileformat gdzfile;
	private DocStruct myDocStruct;
	private DocStruct tempStrukturelement;
	private List<Metadatum> myMetadaten = new LinkedList<Metadatum>();
	private List<MetaPerson> myPersonen = new LinkedList<MetaPerson>();
	private Metadatum curMetadatum;
	private MetaPerson curPerson;
	private DigitalDocument mydocument;
	private Prozess myProzess;
	private Prefs myPrefs;
	// private String myProzesseID;
	private String myBenutzerID;
	private String tempTyp;
	private String tempWert;
	private String tempPersonVorname;
	private String tempPersonNachname;
	private String tempPersonRolle;
	// private String myProzessTitel;
	private String currentTifFolder;
	private List<String> allTifFolders;
	/* Variablen für die Zuweisung der Seiten zu Strukturelementen */
	private String alleSeitenAuswahl_ersteSeite;
	private String alleSeitenAuswahl_letzteSeite;
	private String[] alleSeitenAuswahl;
	private String[] structSeitenAuswahl;
	private SelectItem alleSeiten[];
	private Metadatum alleSeitenNeu[];
	private ArrayList<Metadatum> tempMetadatumList = new ArrayList<Metadatum>();
	private Metadatum selectedMetadatum;

	private String paginierungWert;
	private int paginierungAbSeiteOderMarkierung;
	private String paginierungArt;
	private int paginierungSeitenProImage = 1; // 1=normale Paginierung, 2=zwei
	// Spalten auf einem Image,
	// 3=nur jede zweite Seite hat
	// Seitennummer

	private SelectItem structSeiten[];
	private Metadatum structSeitenNeu[];
	private DocStruct logicalTopstruct;

	private boolean modusHinzufuegen = false;
	private boolean modusHinzufuegenPerson = false;
	private String modusAnsicht = "Metadaten";
	private TreeNodeStruct3 tree3;
	private String myBild;

	private int myBildNummer = 0;
	private int myBildLetztes = 0;
	private int myBildCounter = 0;
	private int myBildGroesse = 30;
	private int myImageRotation = 0; // entspricht myBildRotation

	private boolean bildAnzeigen = true;
	private boolean bildZuStrukturelement = false;
	private String bildNummerGeheZu = "";
	private String addDocStructType1;
	private String addDocStructType2;
	private String zurueck = "Main";
	private MetadatenSperrung sperrung = new MetadatenSperrung();
	private Helper help = new Helper();
	private boolean nurLesenModus;
	private String neuesElementWohin = "1";
	private boolean modusStrukturelementVerschieben = false;
	private String additionalOpacPpns;
	private String opacSuchfeld = "12";
	private String opacKatalog;

	private String ajaxSeiteStart = "";
	private String ajaxSeiteEnde = "";
	private String pagesStart ="";
	private String pagesEnd="";
	private HashMap<String, Boolean> treeProperties;

	/**
	 * Konstruktor ================================================================
	 */
	public Metadaten() {
		treeProperties = new HashMap<String, Boolean>();
		treeProperties.put("showtreelevel", Boolean.valueOf(false));
		treeProperties.put("showtitle", Boolean.valueOf(false));
		treeProperties.put("fullexpanded", Boolean.valueOf(true));
		treeProperties.put("showfirstpagenumber", Boolean.valueOf(false));
		treeProperties.put("showpagesasajax", Boolean.valueOf(true));
	}

	/**
	 * die Anzeige der Details ändern (z.B. nur die Metadaten anzeigen, oder nur die Paginierungssequenzen)
	 * 
	 * @return Navigationsanweisung "null" als String (also gleiche Seite reloaden)
	 */
	public String AnsichtAendern() {
		modusAnsicht = Helper.getRequestParameter("Ansicht");
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String Hinzufuegen() {
		modusHinzufuegen = true;
		Modes.setBindState(BindState.create);
		getMetadatum().setValue("");
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String HinzufuegenPerson() {
		modusHinzufuegenPerson = true;
		tempPersonNachname = "";
		tempPersonVorname = "";
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String Abbrechen() {
		modusHinzufuegen = false;
		modusHinzufuegenPerson = false;
		Modes.setBindState(BindState.edit);
		getMetadatum().setValue("");
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String Reload() {
		// MetadatenDebuggen(gdzfile.getDigitalDocument().getLogicalDocStruct());
		if (!SperrungAktualisieren()) {
			return "SperrungAbgelaufen";
		} else {
			try {
				myProzess.writeMetadataFile(gdzfile);
			} catch (Exception e) {
				Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
				myLogger.error(e);
			}
			return "";
		}
	}

	public String Kopieren() {
		Metadata md;
		try {
			md = new Metadata(curMetadatum.getMd().getType());

			md.setValue(curMetadatum.getMd().getValue());
			myDocStruct.addMetadata(md);
		} catch (MetadataTypeNotAllowedException e) {
			myLogger.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e.getMessage());
		}
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String KopierenPerson() {
		Person per;
		try {
			per = new Person(myPrefs.getMetadataTypeByName(curPerson.getP().getRole()));
			per.setFirstname(curPerson.getP().getFirstname());
			per.setLastname(curPerson.getP().getLastname());
			per.setRole(curPerson.getP().getRole());

			myDocStruct.addPerson(per);
		} catch (IncompletePersonObjectException e) {
			myLogger.error("Fehler beim Kopieren von Personen (IncompletePersonObjectException): " + e.getMessage());
		} catch (MetadataTypeNotAllowedException e) {
			myLogger.error("Fehler beim Kopieren von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
		}
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String ChangeCurrentDocstructType() {
		// myStrukturelement.getType().getName()
		// + " soll werden zu " + tempWert);

		if (myDocStruct != null && tempWert != null) {
			try {
				DocStruct rueckgabe = metahelper.ChangeCurrentDocstructType(myDocStruct, tempWert);
				MetadatenalsBeanSpeichern(rueckgabe);
				MetadatenalsTree3Einlesen1();
			} catch (DocStructHasNoTypeException e) {
				Helper.setFehlerMeldung("Error while changing DocStructTypes (DocStructHasNoTypeException): ", e.getMessage());
				myLogger.error("Error while changing DocStructTypes (DocStructHasNoTypeException): " + e.getMessage());
			} catch (MetadataTypeNotAllowedException e) {
				Helper.setFehlerMeldung("Error while changing DocStructTypes (MetadataTypeNotAllowedException): ", e.getMessage());
				myLogger.error("Error while changing DocStructTypes (MetadataTypeNotAllowedException): " + e.getMessage());
			} catch (TypeNotAllowedAsChildException e) {
				Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedAsChildException): ", e.getMessage());
				myLogger.error("Error while changing DocStructTypes (TypeNotAllowedAsChildException): " + e.getMessage());
			} catch (TypeNotAllowedForParentException e) {
				Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedForParentException): ", e.getMessage());
				myLogger.error("Error while changing DocStructTypes (TypeNotAllowedForParentException): " + e.getMessage());
			}
		}
		return "Metadaten3links";
	}

	public String Speichern() {
		try {
			Metadata md = new Metadata(myPrefs.getMetadataTypeByName(tempTyp));
			md.setValue(selectedMetadatum.getValue());

			myDocStruct.addMetadata(md);
		} catch (MetadataTypeNotAllowedException e) {
			myLogger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
		}

		/*
		 * wenn TitleDocMain, dann gleich Sortiertitel mit gleichem Inhalt anlegen
		 */
		if (tempTyp.equals("TitleDocMain") && myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
			try {
				Metadata md2 = new Metadata(myPrefs.getMetadataTypeByName("TitleDocMainShort"));
				md2.setValue(selectedMetadatum.getValue());
				myDocStruct.addMetadata(md2);
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
			}
		}

		modusHinzufuegen = false;
		Modes.setBindState(BindState.edit);
		selectedMetadatum.setValue("");
		tempWert = "";
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String loadRightFrame() {
		modusHinzufuegen = false;
		modusHinzufuegenPerson = false;
		Modes.setBindState(BindState.edit);
		return "Metadaten2rechts";
	}

	public String SpeichernPerson() {
		try {
			Person per = new Person(myPrefs.getMetadataTypeByName(tempPersonRolle));
			per.setFirstname(tempPersonVorname);
			per.setLastname(tempPersonNachname);
			per.setRole(tempPersonRolle);

			// MetadataType mdt =
			// myPrefs.getMetadataTypeByName(tempPersonRolle);
			// per.setType(mdt);

			myDocStruct.addPerson(per);
		} catch (IncompletePersonObjectException e) {
			Helper.setFehlerMeldung("Incomplete data for person", "");
			// myLogger.error("Fehler beim Hinzufügen von Personen (IncompletePersonObjectException): "
			// + e.getMessage());
			return "";
			// e.printStackTrace();
		} catch (MetadataTypeNotAllowedException e) {
			Helper.setFehlerMeldung("Person is for this structure not allowed", "");
			// myLogger.error("Fehler beim Hinzufügen von Personen(MetadataTypeNotAllowedException): + e.getMessage()");
			return "";
		}
		modusHinzufuegenPerson = false;
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String Loeschen() {
		myDocStruct.removeMetadata(curMetadatum.getMd());
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String LoeschenPerson() {
		myDocStruct.removePerson(curPerson.getP());
		MetadatenalsBeanSpeichern(myDocStruct);
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	/**
	 * die noch erlaubten Rollen zurückgeben ================================================================
	 */
	public ArrayList<SelectItem> getAddableRollen() {
		return metahelper.getAddablePersonRoles(myDocStruct, "");
	}

	public int getSizeOfRoles() {
		try {
			return getAddableRollen().size();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public void setSizeOfRoles(int i) {
		// do nothing, needed for jsp only
	}

	public int getSizeOfMetadata() {
		try {
			return getAddableMetadataTypes().size();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public void setSizeOfMetadata(int i) {
		// do nothing, needed for jsp only
	}

	/**
	 * die noch erlaubten Metadaten zurückgeben ================================================================
	 */
	public ArrayList<SelectItem> getAddableMetadataTypes() {
		ArrayList<SelectItem> myList = new ArrayList<SelectItem>();
		/*
		 * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
		 */
		List<MetadataType> types = myDocStruct.getAddableMetadataTypes();
		if (types == null)
			return myList;

		/*
		 * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
		 */
		for (MetadataType mdt : new ArrayList<MetadataType>(types)) {
			if (mdt.getIsPerson())
				types.remove(mdt);
		}

		/*
		 * -------------------------------- die Metadatentypen sortieren --------------------------------
		 */
		HelperComparator c = new HelperComparator();
		c.setSortierart("MetadatenTypen");
		Collections.sort(types, c);

		int counter = types.size();

		for (MetadataType mdt : types) {
			myList.add(new SelectItem(mdt.getName(), metahelper.getMetadatatypeLanguage(mdt)));
			try {
				Metadata md = new Metadata(mdt);
				Metadatum mdum = new Metadatum(md, counter, myPrefs, myProzess);
				counter++;
				tempMetadatumList.add(mdum);

			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
			}
		}
		return myList;
	}

	public ArrayList<Metadatum> getTempMetadatumList() {
		return tempMetadatumList;
	}

	public void setTempMetadatumList(ArrayList<Metadatum> tempMetadatumList) {
		this.tempMetadatumList = tempMetadatumList;
	}

	/**
	 * die MetadatenTypen zurückgeben ================================================================
	 */
	public SelectItem[] getMetadatenTypen() {
		/*
		 * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
		 */
		List<MetadataType> types = myDocStruct.getAddableMetadataTypes();

		if (types == null) {
			// SelectItem myTypen[] = new SelectItem[0];
			return new SelectItem[0];
		}

		/*
		 * -------------------------------- die Metadatentypen sortieren --------------------------------
		 */
		HelperComparator c = new HelperComparator();
		c.setSortierart("MetadatenTypen");
		Collections.sort(types, c);

		/*
		 * -------------------------------- nun ein Array mit der richtigen Größe anlegen --------------------------------
		 */
		int zaehler = types.size();
		SelectItem myTypen[] = new SelectItem[zaehler];

		/*
		 * -------------------------------- und anschliessend alle Elemente in das Array packen --------------------------------
		 */
		zaehler = 0;
		for (MetadataType mdt : types) {

			// myTypen[zaehler] = new SelectItem(mdt.getName(),
			// mdt.getLanguage("rusdml"));
			myTypen[zaehler] = new SelectItem(mdt.getName(), metahelper.getMetadatatypeLanguage(mdt));
			zaehler++;

		}

		/*
		 * -------------------------------- alle Typen, die einen Unterstrich haben nochmal rausschmeissen --------------------------------
		 */
		SelectItem myTypenOhneUnterstrich[] = new SelectItem[zaehler];
		for (int i = 0; i < zaehler; i++) {
			myTypenOhneUnterstrich[i] = myTypen[i];
		}
		return myTypenOhneUnterstrich;
	}

	/*
	 * ##################################################### ##################################################### ## ## Metadaten lesen und schreiben
	 * ## ##################################################### ####################################################
	 */

	/**
	 * Metadaten Einlesen
	 * 
	 */
	public String XMLlesen() {

		// myProzesseID = Helper.getRequestParameter("ProzesseID");

		/*
		 * re-reading the ruleset.xml file
		 */
		ConfigDispayRules.getInstance().refresh();

		Modes.setBindState(BindState.edit);
		try {
			myProzess = new ProzessDAO().get(new Integer(Helper.getRequestParameter("ProzesseID")));
		} catch (NumberFormatException e1) {
			Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (DAOException e1) {
			Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
			return Helper.getRequestParameter("zurueck");
		}
		myBenutzerID = Helper.getRequestParameter("BenutzerID");
		alleSeitenAuswahl_ersteSeite = "";
		alleSeitenAuswahl_letzteSeite = "";
		zurueck = Helper.getRequestParameter("zurueck");
		nurLesenModus = Helper.getRequestParameter("nurLesen").equals("true") ? true : false;
		neuesElementWohin = "1";
		tree3 = null;
		try {
			XMLlesenStart();
		} catch (SwapException e) {
			Helper.setFehlerMeldung(e);
			return Helper.getRequestParameter("zurueck");
		} catch (ReadException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (PreferencesException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (WriteException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (IOException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (InterruptedException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		} catch (DAOException e) {
			Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
			return Helper.getRequestParameter("zurueck");
		}

		TreeExpand();
		sperrung.setLocked(myProzess.getId().intValue(), myBenutzerID);
		return "Metadaten";
	}

	/**
	 * Metadaten Einlesen
	 * 
	 * @throws ReadException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws PreferencesException
	 *             ============================================================== ==
	 * @throws DAOException
	 * @throws SwapException
	 * @throws WriteException
	 */
	public String XMLlesenStart() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
			WriteException {
		myPrefs = myProzess.getRegelsatz().getPreferences();
		modusAnsicht = "Metadaten";
		modusHinzufuegen = false;
		modusHinzufuegenPerson = false;
		modusStrukturelementVerschieben = false;
		// TODO: Make file pattern configurable
		myBild = null;
		myBildNummer = 1;
		myImageRotation = 0;
		readAllTifFolders();

		/*
		 * -------------------------------- Dokument einlesen --------------------------------
		 */
		gdzfile = myProzess.readMetadataFile();
		mydocument = gdzfile.getDigitalDocument();
		mydocument.addAllContentFiles();
		metahelper = new MetadatenHelper(myPrefs, mydocument);
		imagehelper = new MetadatenImagesHelper(myPrefs, mydocument);

		/*
		 * -------------------------------- Das Hauptelement ermitteln --------------------------------
		 */

		// TODO: think something up, how to handle a not matching ruleset
		// causing logicalDocstruct to be null
		logicalTopstruct = mydocument.getLogicalDocStruct();

		// this exception needs some serious feedback because data is corrupted
		if (logicalTopstruct == null) {
			throw new ReadException(Helper.getTranslation("metaDataError"));
		}

		BildErmitteln(0);
		retrieveAllImages();
		// MetadatenImLogAusgeben(logicalTopstruct);
		MetadatenalsBeanSpeichern(logicalTopstruct);
		MetadatenalsTree3Einlesen1();

		// inserted to make Paginierung the starting view
		modusAnsicht = "Paginierung";
		return "Metadaten";
	}

	/**
	 * Metadaten Einlesen
	 * 
	 * @throws ReadException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws PreferencesException
	 *             ============================================================== ==
	 * @throws DAOException
	 * @throws SwapException
	 * @throws WriteException
	 */

	/**
	 * Metadaten Schreiben
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 *             ============================================================== ==
	 * @throws DAOException
	 * @throws SwapException
	 * @throws WriteException
	 * @throws PreferencesException
	 */
	public String XMLschreiben() {
		/*
		 * für den Prozess nochmal die Metadaten durchlaufen und die Daten speichern
		 */
		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

		myProzess.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(logicalTopstruct, CountType.DOCSTRUCT));
		myProzess.setSortHelperMetadata(zaehlen.getNumberOfUghElements(logicalTopstruct, CountType.METADATA));
		try {
			myProzess.setSortHelperImages(FileUtils.getNumberOfFiles(new File(myProzess.getImagesOrigDirectory())));
			new ProzessDAO().save(myProzess);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
			myLogger.error(e);
		} catch (Exception e) {
			Helper.setFehlerMeldung("error while counting current images", e);
			myLogger.error(e);
		}
		/* xml-Datei speichern */
		// MetadatenDebuggen(gdzfile.getDigitalDocument().getLogicalDocStruct());
		/*
		 * --------------------- vor dem Speichern alle ungenutzen Docstructs rauswerfen -------------------
		 */
		metahelper.deleteAllUnusedElements(mydocument.getLogicalDocStruct());

		try {
			myProzess.writeMetadataFile(gdzfile);
		} catch (Exception e) {
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
			myLogger.error(e);
			return "Metadaten";
		}
		SperrungAufheben();
		return zurueck;
	}

	/**
	 * vom aktuellen Strukturelement alle Metadaten einlesen
	 * 
	 * @param inStrukturelement
	 *            ================================================================
	 */

	private void MetadatenalsBeanSpeichern(DocStruct inStrukturelement) {
		myDocStruct = inStrukturelement;
		LinkedList<Metadatum> lsMeta = new LinkedList<Metadatum>();
		LinkedList<MetaPerson> lsPers = new LinkedList<MetaPerson>();

		/*
		 * -------------------------------- alle Metadaten ermitteln --------------------------------
		 */
		// if (inStrukturelement != null &&
		// inStrukturelement.getAllVisibleMetadata() != null
		// && inStrukturelement.getAllMetadata().size() > 0) {
		// for (Iterator iter = inStrukturelement.getAllMetadata().iterator();
		// iter.hasNext();)
		// lsMeta.add(new Metadatum((Metadata) iter.next(), 0, myPrefs));
		// }
		/*
		 * -------------------------------- alle Metadaten und die DefaultDisplay-Werte anzeigen --------------------------------
		 */
		List<? extends Metadata> myTempMetadata = metahelper.getMetadataInclDefaultDisplay(inStrukturelement, (String) Helper
				.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"), false, myProzess);
		if (myTempMetadata != null)
			for (Metadata metadata : myTempMetadata)
				lsMeta.add(new Metadatum(metadata, 0, myPrefs, myProzess));

		/*
		 * -------------------------------- alle Personen ermitteln --------------------------------
		 */
		// if (inStrukturelement != null && inStrukturelement.getAllPersons() !=
		// null
		// && inStrukturelement.getAllPersons().size() > 0) {
		// for (Iterator iter = inStrukturelement.getAllPersons().iterator();
		// iter.hasNext();)
		// lsPers.add(new MetaPerson((Person) iter.next(), 0, myPrefs));
		// }
		/*
		 * -------------------------------- alle Personen und die DefaultDisplay-Werte ermitteln --------------------------------
		 */
		myTempMetadata = metahelper.getMetadataInclDefaultDisplay(inStrukturelement, (String) Helper
				.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"), true, myProzess);
		if (myTempMetadata != null)
			for (Metadata metadata : myTempMetadata)
				lsPers.add(new MetaPerson((Person) metadata, 0, myPrefs, inStrukturelement));

		myMetadaten = lsMeta;
		myPersonen = lsPers;

		/*
		 * -------------------------------- die zugehörigen Seiten ermitteln --------------------------------
		 */
		StructSeitenErmitteln(myDocStruct);
	}

	/*
	 * ##################################################### ##################################################### ## ## Treeview ##
	 * ##################################################### ####################################################
	 */

	@SuppressWarnings("unchecked")
	private String MetadatenalsTree3Einlesen1() {
		HashMap map;
		TreeNodeStruct3 knoten;
		List<DocStruct> status = new ArrayList<DocStruct>();

		/*
		 * -------------------------------- den Ausklapp-Zustand aller Knoten erfassen --------------------------------
		 */
		if (tree3 != null) {
			for (Iterator iter = tree3.getChildrenAsList().iterator(); iter.hasNext();) {
				map = (HashMap) iter.next();
				knoten = (TreeNodeStruct3) map.get("node");
				if (knoten.isExpanded())
					status.add(knoten.getStruct());
			}
		}

		if (logicalTopstruct == null)
			return "Metadaten3links";
		/*
		 * -------------------------------- Die Struktur als Tree3 aufbereiten --------------------------------
		 */
		String label = logicalTopstruct.getType().getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		if (label == null)
			label = logicalTopstruct.getType().getName();

		tree3 = new TreeNodeStruct3(label, logicalTopstruct);
		MetadatenalsTree3Einlesen2(logicalTopstruct, (TreeNodeStruct3) tree3);

		/*
		 * -------------------------------- den Ausklappzustand nach dem neu-Einlesen wieder herstellen --------------------------------
		 */
		for (Iterator iter = tree3.getChildrenAsListAlle().iterator(); iter.hasNext();) {
			map = (HashMap) iter.next();
			knoten = (TreeNodeStruct3) map.get("node");
			// Ausklappstatus wiederherstellen
			if (status.contains(knoten.getStruct()))
				knoten.setExpanded(true);
			// Selection wiederherstellen
			if (myDocStruct == knoten.getStruct())
				knoten.setSelected(true);
		}

		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "Metadaten3links";
	}

	/**
	 * Metadaten in Tree3 ausgeben
	 * 
	 * @param inStrukturelement
	 *            ================================================================
	 */
	private void MetadatenalsTree3Einlesen2(DocStruct inStrukturelement, TreeNodeStruct3 OberKnoten) {
		OberKnoten.setMainTitle(MetadatenErmitteln(inStrukturelement, "TitleDocMain"));
		OberKnoten.setZblNummer(MetadatenErmitteln(inStrukturelement, "ZBLIdentifier"));
		OberKnoten.setZblSeiten(MetadatenErmitteln(inStrukturelement, "ZBLPageNumber"));
		OberKnoten.setPpnDigital(MetadatenErmitteln(inStrukturelement, "IdentifierDigital"));
		OberKnoten.setFirstImage(metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_FIRST));
		OberKnoten.setLastImage(metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_LAST));
		// wenn es ein Heft ist, die Issue-Number mit anzeigen
		if (inStrukturelement.getType().getName().equals("PeriodicalIssue"))
			OberKnoten.setDescription(OberKnoten.getDescription() + " " + MetadatenErmitteln(inStrukturelement, "CurrentNo"));

		// wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
		if (inStrukturelement.getType().getName().equals("Periodical") || inStrukturelement.getType().getName().equals("PeriodicalVolume"))
			OberKnoten.setExpanded(true);

		int zaehler = 0;
		/*
		 * -------------------------------- vom aktuellen Strukturelement alle Kinder in den Tree packen --------------------------------
		 */
		List<DocStruct> meineListe = inStrukturelement.getAllChildren();
		if (meineListe != null) {
			/* es gibt Kinder-Strukturelemente */
			for (DocStruct kind : meineListe) {
				String label = kind.getType().getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
				if (label == null)
					label = kind.getType().getName();
				TreeNodeStruct3 tns = new TreeNodeStruct3(label, kind);
				zaehler++;
				OberKnoten.addChild(tns);
				// MetadatenDebuggen(kind);
				MetadatenalsTree3Einlesen2(kind, tns);
			}
		}
	}

	/**
	 * Metadaten gezielt zurückgeben
	 * 
	 * @param inStrukturelement
	 *            ================================================================
	 */
	private String MetadatenErmitteln(DocStruct inStrukturelement, String inTyp) {
		String rueckgabe = "";
		List<Metadata> allMDs = inStrukturelement.getAllMetadata();
		if (allMDs != null) {
			for (Metadata md : allMDs) {
				if (md.getType().getName().equals(inTyp))
					rueckgabe += (md.getValue() == null ? "" : md.getValue()) + " ";
			}
		}
		return rueckgabe.trim();
	}

	// /**
	// * Metadaten debuggen
	// *
	// * @param inStrukturelement
	// * ================================================================
	// */
	// private void MetadatenDebuggen(DocStruct inStrukturelement) {
	// myLogger.info("--------------------------- " + inStrukturelement.getType().getName() + " -------------------------------------------------");
	// List<Metadata> allMDs = inStrukturelement.getAllMetadata();
	// if (allMDs != null)
	// for (Metadata md : allMDs)
	// myLogger.info(md.getType().getName() + ": " + md.getValue());
	// }

	@SuppressWarnings("unchecked")
	public void setMyStrukturelement(DocStruct inStruct) {
		modusHinzufuegen = false;
		modusHinzufuegenPerson = false;
		Modes.setBindState(BindState.edit);
		MetadatenalsBeanSpeichern(inStruct);

		/*
		 * -------------------------------- die Selektion kenntlich machen --------------------------------
		 */
		for (Iterator iter = tree3.getChildrenAsListAlle().iterator(); iter.hasNext();) {

			HashMap map = (HashMap) iter.next();
			TreeNodeStruct3 knoten = (TreeNodeStruct3) map.get("node");
			// Selection wiederherstellen
			if (myDocStruct == knoten.getStruct())
				knoten.setSelected(true);
			else
				knoten.setSelected(false);
		}

		SperrungAktualisieren();
	}

	/**
	 * Knoten nach oben schieben ================================================================
	 */
	public String KnotenUp() {
		try {
			metahelper.KnotenUp(myDocStruct);
		} catch (TypeNotAllowedAsChildException e) {
			myLogger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
		}
		return MetadatenalsTree3Einlesen1();
	}

	/**
	 * Knoten nach unten schieben ================================================================
	 */
	public String KnotenDown() {
		try {
			metahelper.KnotenDown(myDocStruct);
		} catch (TypeNotAllowedAsChildException e) {
			myLogger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
		}
		return MetadatenalsTree3Einlesen1();
	}

	/**
	 * Knoten zu einer anderen Stelle
	 * 
	 * @throws TypeNotAllowedAsChildException
	 *             ============================================================== ==
	 */
	public String KnotenVerschieben() throws TypeNotAllowedAsChildException {
		myDocStruct.getParent().removeChild(myDocStruct);
		tempStrukturelement.addChild(myDocStruct);
		MetadatenalsTree3Einlesen1();
		myLogger.debug(modusStrukturelementVerschieben);
		neuesElementWohin = "1";
		return "Metadaten3links";
	}

	/**
	 * Knoten nach oben schieben
	 * 
	 * @throws IOException
	 *             ============================================================== ==
	 */
	public String KnotenDelete() throws IOException {
		if (myDocStruct != null && myDocStruct.getParent() != null) {
			DocStruct tempParent = myDocStruct.getParent();
			myDocStruct.getParent().removeChild(myDocStruct);
			myDocStruct = tempParent;
		}
		// den Tree neu einlesen
		return MetadatenalsTree3Einlesen1();
	}

	/**
	 * Knoten hinzufügen
	 * 
	 * @throws TypeNotAllowedForParentException
	 * @throws IOException
	 * @throws TypeNotAllowedForParentException
	 * @throws TypeNotAllowedAsChildException
	 * @throws TypeNotAllowedAsChildException
	 *             ============================================================== ==
	 */
	@SuppressWarnings("unchecked")
	public String KnotenAdd() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
		// myLogger.warn("eingefügt wird: " + neuesElementWohin);
		// myLogger.warn("eingefügt wird was: " + addDocStructType1 + " - " +
		// addDocStructType2);

		/*
		 * -------------------------------- prüfen, wohin das Strukturelement gepackt werden soll, anschliessend entscheiden, welches Strukturelement
		 * gewählt wird und abschliessend richtig einfügen --------------------------------
		 */

		
		DocStruct ds = null;
		/*
		 * -------------------------------- vor das aktuelle Element --------------------------------
		 */
		if (neuesElementWohin.equals("1")) {
			if (addDocStructType1 == null || addDocStructType1.equals("")) {
				return "Metadaten3links";
			}
			DocStructType dst = myPrefs.getDocStrctTypeByName(addDocStructType1);
			 ds = mydocument.createDocStruct(dst);
			if (myDocStruct == null) {
				return "Metadaten3links";
			}
			DocStruct parent = myDocStruct.getParent();
			if (parent == null) {
				myLogger.debug("das gewählte Element kann den Vater nicht ermitteln");
				return "Metadaten3links";
			}
			List alleDS = new ArrayList();

			/* alle Elemente des Parents durchlaufen */
			for (Iterator iter = parent.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct tempDS = (DocStruct) iter.next();

				/* wenn das aktuelle Element das gesuchte ist */
				if (tempDS == myDocStruct) {
					alleDS.add(ds);
				}
				alleDS.add(tempDS);
			}

			/* anschliessend alle Childs entfernen */
			for (Iterator iter = alleDS.iterator(); iter.hasNext();) {
				parent.removeChild((DocStruct) iter.next());
			}

			/* anschliessend die neue Childliste anlegen */
			for (Iterator iter = alleDS.iterator(); iter.hasNext();) {
				parent.addChild((DocStruct) iter.next());
			}
			// myStrukturelement =ds;
		}

		/*
		 * -------------------------------- hinter das aktuelle Element --------------------------------
		 */
		if (neuesElementWohin.equals("2")) {
			DocStructType dst = myPrefs.getDocStrctTypeByName(addDocStructType1);
			 ds = mydocument.createDocStruct(dst);
			DocStruct parent = myDocStruct.getParent();
			if (parent == null) {
				myLogger.debug("das gewählte Element kann den Vater nicht ermitteln");
				return "Metadaten3links";
			}
			List alleDS = new ArrayList();

			/* alle Elemente des Parents durchlaufen */
			for (Iterator iter = parent.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct tempDS = (DocStruct) iter.next();
				alleDS.add(tempDS);
				/* wenn das aktuelle Element das gesuchte ist */
				if (tempDS == myDocStruct)
					alleDS.add(ds);
			}

			/* anschliessend alle Childs entfernen */
			for (Iterator iter = alleDS.iterator(); iter.hasNext();) {
				parent.removeChild((DocStruct) iter.next());
			}

			/* anschliessend die neue Childliste anlegen */
			for (Iterator iter = alleDS.iterator(); iter.hasNext();) {
				parent.addChild((DocStruct) iter.next());
			}
			// myStrukturelement =ds;
		}

		/*
		 * -------------------------------- als erstes Child --------------------------------
		 */
		if (neuesElementWohin.equals("3")) {
			DocStructType dst = myPrefs.getDocStrctTypeByName(addDocStructType2);
			 ds = mydocument.createDocStruct(dst);
			DocStruct parent = myDocStruct;
			if (parent == null) {
				myLogger.debug("das gewählte Element kann den Vater nicht ermitteln");
				return "Metadaten3links";
			}
			List alleDS = new ArrayList();
			alleDS.add(ds);

			if (parent.getAllChildren() != null && parent.getAllChildren().size() != 0) {
				alleDS.addAll(parent.getAllChildren());
				parent.getAllChildren().retainAll(new ArrayList());
			}

			/* anschliessend die neue Childliste anlegen */
			for (Iterator iter = alleDS.iterator(); iter.hasNext();) {
				parent.addChild((DocStruct) iter.next());
			}
			// myStrukturelement =ds;
		}

		/*
		 * -------------------------------- als letztes Child --------------------------------
		 */
		if (neuesElementWohin.equals("4")) {
			DocStructType dst = myPrefs.getDocStrctTypeByName(addDocStructType2);
			 ds = mydocument.createDocStruct(dst);
			myDocStruct.addChild(ds);
			// myStrukturelement =ds;
		}
		
		if (!pagesStart.equals("") && !pagesEnd.equals("")) {
			DocStruct temp = myDocStruct;
			myDocStruct = ds;		
			ajaxSeiteStart = pagesStart;
			ajaxSeiteEnde = pagesEnd;
			AjaxSeitenStartUndEndeSetzen();
			myDocStruct = temp;
//			pagesStart = pagesEnd;
//			pagesEnd = "";
		}

		return MetadatenalsTree3Einlesen1();
	}

	/**
	 * mögliche Docstructs als Kind zurückgeben ================================================================
	 */
	public SelectItem[] getAddableDocStructTypenAlsKind() {
		return metahelper.getAddableDocStructTypen(myDocStruct, false);
	}

	/**
	 * mögliche Docstructs als Nachbar zurückgeben ================================================================
	 */
	public SelectItem[] getAddableDocStructTypenAlsNachbar() {
		return metahelper.getAddableDocStructTypen(myDocStruct, true);
	}

	/*
	 * ##################################################### ##################################################### ## ## Strukturdaten: Seiten ##
	 * ##################################################### ####################################################
	 */

	/**
	 * Markus baut eine Seitenstruktur aus den vorhandenen Images ================================================================
	 * 
	 * @throws DAOException
	 * @throws SwapException
	 */
	public String createPagination() throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException, DAOException {
		imagehelper.createPagination(myProzess);
		retrieveAllImages();
		return "";
	}

	/**
	 * alle Seiten ermitteln ================================================================
	 */
	private void retrieveAllImages() {
		DigitalDocument mydocument = null;
		try {
			mydocument = gdzfile.getDigitalDocument();
		} catch (PreferencesException e) {
			Helper.setMeldung(null, "Can not get DigitalDocument: ", e.getMessage());
		}

		List<DocStruct> meineListe = mydocument.getPhysicalDocStruct().getAllChildren();
		if (meineListe == null) {
			alleSeiten = null;
			return;
		}
		int zaehler = meineListe.size();
		alleSeiten = new SelectItem[zaehler];
		alleSeitenNeu = new Metadatum[zaehler];
		zaehler = 0;
		MetadataType mdt = myPrefs.getMetadataTypeByName("logicalPageNumber");
		if (meineListe != null && meineListe.size() > 0) {
			for (DocStruct mySeitenDocStruct : meineListe) {
				List<? extends Metadata> mySeitenDocStructMetadaten = mySeitenDocStruct.getAllMetadataByType(mdt);
				for (Metadata meineSeite : mySeitenDocStructMetadaten) {
					alleSeitenNeu[zaehler] = new Metadatum(meineSeite, zaehler, myPrefs, myProzess);
					alleSeiten[zaehler] = new SelectItem(String.valueOf(zaehler), MetadatenErmitteln(meineSeite.getDocStruct(), "physPageNumber")
							.trim()
							+ ": " + meineSeite.getValue());
				}
				zaehler++;
			}
		}
	}

	/**
	 * alle Seiten des aktuellen Strukturelements ermitteln ================================================================
	 */
	@SuppressWarnings("unchecked")
	private void StructSeitenErmitteln(DocStruct inStrukturelement) {
		if (inStrukturelement == null)
			return;
		List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
		int zaehler = 0;
		int imageNr = 0;
		if (listReferenzen != null) {
			/*
			 * -------------------------------- Referenzen sortieren --------------------------------
			 */
			Collections.sort(listReferenzen, new Comparator() {
				public int compare(final Object o1, final Object o2) {
					final Reference r1 = (Reference) o1;
					final Reference r2 = (Reference) o2;
					Integer page1 = 0;
					Integer page2 = 0;

					MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
					List<? extends Metadata> listMetadaten = r1.getTarget().getAllMetadataByType(mdt);
					if (listMetadaten != null && listMetadaten.size() > 0) {
						Metadata meineSeite = listMetadaten.get(0);
						page1 = Integer.parseInt(meineSeite.getValue());
					}
					listMetadaten = r2.getTarget().getAllMetadataByType(mdt);
					if (listMetadaten != null && listMetadaten.size() > 0) {
						Metadata meineSeite = listMetadaten.get(0);
						page2 = Integer.parseInt(meineSeite.getValue());
					}
					return page1.compareTo(page2);
				}
			});

			/* die Größe der Arrays festlegen */
			structSeiten = new SelectItem[listReferenzen.size()];
			structSeitenNeu = new Metadatum[listReferenzen.size()];

			/* alle Referenzen durchlaufen und deren Metadaten ermitteln */
			for (Reference ref : listReferenzen) {
				DocStruct target = ref.getTarget();
				StructSeitenErmitteln2(target, zaehler);
				if (imageNr == 0)
					imageNr = StructSeitenErmitteln3(target);
				zaehler++;
			}

		}

		/*
		 * Wenn eine Verkn�pfung zwischen Strukturelement und Bildern sein soll, das richtige Bild anzeigen
		 */
		// myLogger.info("erste Seite ist Image " + imageNr);
		if (bildZuStrukturelement)
			BildErmitteln(imageNr - myBildNummer);
	}

	/**
	 * alle Seiten des aktuellen Strukturelements ermitteln 2 ================================================================
	 */
	private void StructSeitenErmitteln2(DocStruct inStrukturelement, int inZaehler) {
		MetadataType mdt = myPrefs.getMetadataTypeByName("logicalPageNumber");
		List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
		if (listMetadaten == null || listMetadaten.size() == 0)
			return;
		for (Metadata meineSeite : listMetadaten) {
			structSeitenNeu[inZaehler] = new Metadatum(meineSeite, inZaehler, myPrefs, myProzess);
			structSeiten[inZaehler] = new SelectItem(String.valueOf(inZaehler), MetadatenErmitteln(meineSeite.getDocStruct(), "physPageNumber")
					.trim()
					+ ": " + meineSeite.getValue());
		}
	}

	/**
	 * noch für Testzweck zum direkten öffnen der richtigen Startseite 3 ================================================================
	 */
	private int StructSeitenErmitteln3(DocStruct inStrukturelement) {
		MetadataType mdt = myPrefs.getMetadataTypeByName("physPageNumber");
		List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
		if (listMetadaten == null || listMetadaten.size() == 0)
			return 0;
		int rueckgabe = 0;
		for (Metadata meineSeite : listMetadaten)
			rueckgabe = Integer.parseInt(meineSeite.getValue());
		return rueckgabe;
	}

	/**
	 * die Paginierung ändern
	 */

	public String Paginierung() {
		Pagination p = new Pagination(alleSeitenAuswahl, alleSeitenNeu, paginierungAbSeiteOderMarkierung, paginierungArt, paginierungSeitenProImage,
				paginierungWert);
		String result = p.doPagination();
		/*
		 * zum Schluss nochmal alle Seiten neu einlesen
		 */
		alleSeitenAuswahl = null;
		retrieveAllImages();
		if (!SperrungAktualisieren()) {
			return "SperrungAbgelaufen";
		}
		return result;
	}

	/**
	 * alle Knoten des Baums expanden oder collapsen ================================================================
	 */
	public String TreeExpand() {
		tree3.expandNodes((Boolean) treeProperties.get("fullexpanded"));
		return "Metadaten3links";
	}

	/*
	 * ##################################################### ##################################################### ## ## Bilder-Anzeige ##
	 * ##################################################### ####################################################
	 */

	public String BildBlaetternVor() {
		BildErmitteln(1);
		return "";
	}

	public String BildBlaetternZurueck() {
		BildErmitteln(-1);
		return "";
	}

	public String BildBlaettern() {
		String parameter = Helper.getRequestParameter("Anzahl");
		if (parameter.equals(""))
			parameter = "0";
		int tempint = Integer.parseInt(parameter);
		BildErmitteln(tempint);
		return "";
	}

	public void rotateLeft() {
		if (myImageRotation < 90) {
			myImageRotation = 360;
		}
		myImageRotation = (myImageRotation - 90) % 360;
		BildErmitteln(0);
	}

	public void rotateRight() {
		myImageRotation = (myImageRotation + 90) % 360;
		BildErmitteln(0);
	}

	public String BildGeheZu() {
		int eingabe;
		try {
			eingabe = Integer.parseInt(bildNummerGeheZu);
		} catch (Exception e) {
			eingabe = myBildNummer;
		}

		BildErmitteln(eingabe - myBildNummer);
		return "";
	}

	public String BildZoomPlus() {
		myBildGroesse += 10;
		BildErmitteln(0);
		return "";
	}

	public String BildZoomMinus() {
		if (myBildGroesse > 10)
			myBildGroesse -= 10;
		BildErmitteln(0);
		return "";
	}

	public String getBild() {
		BildPruefen();
		/* Session ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		return ConfigMain.getTempImagesPath() + session.getId() + "_" + myBildCounter + ".png";
		// return myBildCounter + ".png";
	}

	public List<String> getAllTifFolders() throws IOException, InterruptedException {
		return allTifFolders;
	}

	public void readAllTifFolders() throws IOException, InterruptedException, SwapException, DAOException {
		allTifFolders = new ArrayList<String>();
		File dir = new File(myProzess.getImagesDirectory());

		/* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
		// TODO: Remove this, we have several implementions of this, use an
		// existing one.
		FilenameFilter filterVerz = new FilenameFilter() {
			public boolean accept(File indir, String name) {
				return (new File(indir + File.separator + name).isDirectory());
			}
		};

		// String tifOrdner = "";
		String[] verzeichnisse = dir.list(filterVerz);
		for (int i = 0; i < verzeichnisse.length; i++) {
			allTifFolders.add(verzeichnisse[i]);
		}

		if (!allTifFolders.contains(currentTifFolder))
			currentTifFolder = new File(myProzess.getImagesTifDirectory()).getName();
	}

	private void BildErmitteln(int welches) {
		/*
		 * wenn die Bilder nicht angezeigt werden, brauchen wir auch das Bild nicht neu umrechnen
		 */
		if (!bildAnzeigen) {
			return;
		}
		ocrResult = "";

		ArrayList<String> dataList = new ArrayList<String>();
		try {
			dataList = imagehelper.getImageFiles(myProzess);
		} catch (InvalidImagesException e) {
			myLogger.error("Images could not be read", e);
			Helper.setFehlerMeldung("images could not be read", e);
		}
		if (dataList != null && dataList.size() > 0) {
			myBildLetztes = dataList.size();
			for (int i = 0; i < dataList.size(); i++) {
				if (myBild == null) {
					myBild = dataList.get(0);
				}
				String index = dataList.get(i).substring(0, dataList.get(i).lastIndexOf("."));
				String myPicture = myBild.substring(0, myBild.lastIndexOf("."));
				/* wenn das aktuelle Bild gefunden ist, das neue ermitteln */
				if (index.equals(myPicture)) {
					int pos = i + welches;
					/* aber keine Indexes ausserhalb des Array erlauben */
					if (pos < 0)
						pos = 0;
					if (pos > dataList.size() - 1) {
						pos = dataList.size() - 1;
					}
					if (currentTifFolder != null) {
						try {
							dataList = imagehelper.getImageFiles(myProzess, currentTifFolder);
						} catch (InvalidImagesException e1) {
							myLogger.error("Images could not be read", e1);
							Helper.setFehlerMeldung("images could not be read", e1);
						}
					}
					if (dataList == null) {
						return;
					}
					/* das aktuelle tif erfassen */
					if (dataList.size() > pos) {
						myBild = dataList.get(pos);
					} else {
						myBild = dataList.get(dataList.size() - 1);
					}
					/* die korrekte Seitenzahl anzeigen */
					myBildNummer = pos + 1;

					/* Pages-Verzeichnis ermitteln */
					String myPfad = ConfigMain.getTempImagesPathAsCompleteDirectory();

					/*
					 * den Counter für die Bild-ID auf einen neuen Wert setzen, damit nichts gecacht wird
					 */
					myBildCounter++;
					// if (myBildCounter > 2) myBildCounter = 0;

					/* Session ermitteln */
					FacesContext context = FacesContext.getCurrentInstance();
					HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
					String mySession = session.getId() + "_" + myBildCounter + ".png";
					// String mySession = myBildCounter + ".png";

					/* das neue Bild zuweisen */
					try {
						String tiffconverterpfad = myProzess.getImagesDirectory() + currentTifFolder + File.separator + myBild;
						if (!new File(tiffconverterpfad).exists()) {
							tiffconverterpfad = myProzess.getImagesTifDirectory() + myBild;
							Helper.setFehlerMeldung("formularOrdner:TifFolders", "", "image " + myBild + " does not exist in folder "
									+ currentTifFolder + ", using image from " + new File(myProzess.getImagesTifDirectory()).getName());
						}
						imagehelper.scaleFile(tiffconverterpfad, myPfad + mySession, myBildGroesse, myImageRotation);
					} catch (Exception e) {
						Helper.setFehlerMeldung("could not found image folder", e);
						// TODO: Log this.
						myLogger.error(e);
					}
					break;
				}
			}
		}
		BildPruefen();
	}

	private void BildPruefen() {
		/* wenn bisher noch kein Bild da ist, das erste nehmen */
		boolean exists = false;
		try {
			exists = (new File(myProzess.getImagesDirectory() + currentTifFolder + File.separator + myBild)).exists();
		} catch (Exception e) {
			myBildNummer = -1;
			myLogger.error(e);
		}
		/* wenn das Bild nicht existiert, den Status ändern */
		if (!exists) {
			myBildNummer = -1;
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Sperrung der Metadaten
	 * aktualisieren oder prüfen ## ##################################################### ####################################################
	 */

	private boolean SperrungAktualisieren() {
		/*
		 * wenn die Sperrung noch aktiv ist und auch für den aktuellen Nutzer gilt, Sperrung aktualisieren
		 */
		if (MetadatenSperrung.isLocked(myProzess.getId().intValue()) && sperrung.getLockBenutzer(myProzess.getId().intValue()).equals(myBenutzerID)) {
			sperrung.setLocked(myProzess.getId().intValue(), myBenutzerID);
			return true;
		} else
			return false;
	}

	private void SperrungAufheben() {
		if (MetadatenSperrung.isLocked(myProzess.getId().intValue()) && sperrung.getLockBenutzer(myProzess.getId().intValue()).equals(myBenutzerID)) {
			sperrung.setFree(myProzess.getId().intValue());
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Navigationsanweisungen ##
	 * ##################################################### ####################################################
	 */

	/**
	 * zurück zur Startseite, Metadaten vorher freigeben
	 */
	public String goMain() {
		SperrungAufheben();
		return "newMain";
	}

	/**
	 * zurück gehen
	 */
	public String goZurueck() {
		SperrungAufheben();
		return zurueck;
	}

	/*
	 * ##################################################### ##################################################### ## ## Transliteration bestimmter
	 * Felder ## ##################################################### ####################################################
	 */

	public String Transliterieren() {
		Metadata md = curMetadatum.getMd();

		/*
		 * -------------------------------- wenn es ein russischer Titel ist, dessen Transliterierungen anzeigen --------------------------------
		 */
		if (md.getType().getName().equals("RUSMainTitle")) {
			Transliteration trans = new Transliteration();

			try {
				MetadataType mdt = myPrefs.getMetadataTypeByName("MainTitleTransliterated");
				Metadata mdDin = new Metadata(mdt);
				Metadata mdIso = new Metadata(mdt);
				mdDin.setValue(trans.transliterate_din(md.getValue()));
				mdIso.setValue(trans.transliterate_iso(md.getValue()));

				myDocStruct.addMetadata(mdDin);
				myDocStruct.addMetadata(mdIso);
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
			}
		}
		MetadatenalsBeanSpeichern(myDocStruct);

		/* zum Schluss die Sperrung aktualisieren */
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	public String TransliterierenPerson() {
		Person md = curPerson.getP();

		/*
		 * -------------------------------- wenn es ein russischer Autor ist, dessen Transliterierungen anlegen --------------------------------
		 */
		if (md.getRole().equals("Author")) {
			Transliteration trans = new Transliteration();
			try {
				MetadataType mdtDin = myPrefs.getMetadataTypeByName("AuthorTransliteratedDIN");
				MetadataType mdtIso = myPrefs.getMetadataTypeByName("AuthorTransliteratedISO");
				Person mdDin = new Person(mdtDin);
				Person mdIso = new Person(mdtIso);
				// mdDin.setType(mdtDin);
				// mdIso.setType(mdtIso);
				mdDin.setFirstname(trans.transliterate_din(md.getFirstname()));
				mdDin.setLastname(trans.transliterate_din(md.getLastname()));
				mdIso.setFirstname(trans.transliterate_iso(md.getFirstname()));
				mdIso.setLastname(trans.transliterate_iso(md.getLastname()));
				mdDin.setRole("AuthorTransliteratedDIN");
				mdIso.setRole("AuthorTransliteratedISO");

				myDocStruct.addPerson(mdDin);
				myDocStruct.addPerson(mdIso);
			} catch (MetadataTypeNotAllowedException e) {
				myLogger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
			}
		}
		MetadatenalsBeanSpeichern(myDocStruct);

		/* zum Schluss die Sperrung aktualisieren */
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return "";
	}

	/*
	 * ##################################################### ##################################################### ## ## aus einer Liste von PPNs
	 * Strukturelemente aus dem Opac ## holen und dem aktuellen Strukturelement unterordnen ## #####################################################
	 * ####################################################
	 */

	/**
	 * mehrere PPNs aus dem Opac abfragen und dem aktuellen Strukturelement unterordnen
	 * ================================================================
	 */
	public String AddAdditionalOpacPpns() {
		ImportOpac iopac = new ImportOpac();
		// Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
		StringTokenizer tokenizer = new StringTokenizer(additionalOpacPpns, "\r\n");
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
			try {
				Fileformat addrdf = iopac.OpacToDocStruct(opacSuchfeld, tok, opacKatalog, myPrefs);
				if (addrdf != null) {
					myDocStruct.addChild(addrdf.getDigitalDocument().getLogicalDocStruct());
					MetadatenalsTree3Einlesen1();
				} else
					Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
			} catch (Exception e) {
				Helper.setMeldung(null, "Opac-Fehler: ", e.getMessage());
			}
		}
		return "Metadaten3links";
	}

	/**
	 * eine PPN aus dem Opac abfragen und dessen Metadaten dem aktuellen Strukturelement zuweisen
	 * ================================================================
	 */
	public String AddMetadaFromOpacPpn() {
		ImportOpac iopac = new ImportOpac();
		StringTokenizer tokenizer = new StringTokenizer(additionalOpacPpns, "\r\n");
		while (tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
			try {
				Fileformat addrdf = iopac.OpacToDocStruct(opacSuchfeld, tok, opacKatalog, myPrefs);
				if (addrdf != null) {

					/* die Liste aller erlaubten Metadatenelemente erstellen */
					List<String> erlaubte = new ArrayList<String>();
					for (Iterator<MetadataType> it = myDocStruct.getAddableMetadataTypes().iterator(); it.hasNext();) {
						MetadataType mt = (MetadataType) it.next();
						erlaubte.add(mt.getName());
					}

					/*
					 * wenn der Metadatentyp in der Liste der erlaubten Typen, dann hinzufügen
					 */
					for (Iterator<Metadata> it = addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadata().iterator(); it.hasNext();) {
						Metadata m = (Metadata) it.next();
						if (erlaubte.contains(m.getType().getName()))
							myDocStruct.addMetadata(m);
					}

					MetadatenalsTree3Einlesen1();
				} else
					Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
			} catch (Exception e) {
				myLogger.error(e);
				Helper.setMeldung(null, "Opac-Fehler: ", e.getMessage());
			}
		}
		MetadatenalsBeanSpeichern(myDocStruct);
		modusAnsicht = "Metadaten";
		return "";
	}

	/*
	 * ##################################################### ##################################################### ## ## Metadatenvalidierung ##
	 * ##################################################### ####################################################
	 */

	public void Validate() {
		MetadatenVerifizierung mv = new MetadatenVerifizierung();
		mv.validate(gdzfile, myPrefs, myProzess);
		MetadatenalsBeanSpeichern(myDocStruct);
	}

	/*
	 * ##################################################### ##################################################### ## ## Auswahl der Seiten über Ajax
	 * ## ##################################################### ####################################################
	 */

	public String getAjaxSeiteStart() {
		return ajaxSeiteStart;
	}

	public void setAjaxSeiteStart(String ajaxSeiteStart) {
		this.ajaxSeiteStart = ajaxSeiteStart;
	}

	public String getAjaxSeiteEnde() {
		return ajaxSeiteEnde;
	}

	public void setAjaxSeiteEnde(String ajaxSeiteEnde) {
		this.ajaxSeiteEnde = ajaxSeiteEnde;
	}
	
	public String getPagesEnd() {
		return pagesEnd;
	}
	
	public String getPagesStart() {
		return pagesStart;
	}
	public void setPagesEnd(String pagesEnd) {
		this.pagesEnd = pagesEnd;
	}
	
	public void setPagesStart(String pagesStart) {
		this.pagesStart = pagesStart;
	}

	
	
	public void CurrentStartpage() {
		for (int i = 0; i < alleSeiten.length; i++) {
			SelectItem si = alleSeiten[i];
			if (si.getValue().equals(String.valueOf(pageNumber))) {
				pagesStart = si.getLabel();		
			}
		}
	}
	
	public void CurrentEndpage() {
		for (int i = 0; i < alleSeiten.length; i++) {
			SelectItem si = alleSeiten[i];
			if (si.getValue().equals(String.valueOf(pageNumber))) {
				pagesEnd = si.getLabel();		
			}
		}
	}
	
	
	private int pageNumber =0;
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber -1;
		
	}
	
	
	public List<String> getAjaxAlleSeiten(String prefix) {
		myLogger.debug("Ajax-Liste abgefragt");
		List<String> li = new ArrayList<String>();
		if (alleSeiten != null && alleSeiten.length > 0) {
			for (int i = 0; i < alleSeiten.length; i++) {
				SelectItem si = alleSeiten[i];
				if (si.getLabel().contains(prefix))
					li.add(si.getLabel());
			}
		}
		return li;
	}

	/**
	 * die Seiten über die Ajax-Felder festlegen ================================================================
	 */
	public void AjaxSeitenStartUndEndeSetzen() {
		boolean startseiteOk = false;
		boolean endseiteOk = false;

		/*
		 * alle Seiten durchlaufen und prüfen, ob die eingestellte Seite überhaupt existiert
		 */
		for (int i = 0; i < alleSeiten.length; i++) {
			SelectItem si = alleSeiten[i];
			if (si.getLabel().equals(ajaxSeiteStart)) {
				startseiteOk = true;
				alleSeitenAuswahl_ersteSeite = (String) si.getValue();
			}
			if (si.getLabel().equals(ajaxSeiteEnde)) {
				endseiteOk = true;
				alleSeitenAuswahl_letzteSeite = (String) si.getValue();
			}
		}

		/* wenn die Seiten ok sind */
		if (startseiteOk && endseiteOk) {
			SeitenStartUndEndeSetzen();
		} else {
			Helper.setFehlerMeldung("Selected image(s) unavailable");
			// Helper.setFehlerMeldung("suggestFehler","",
			// "Selected image(s) unavailable");
		}
	}

	/**
	 * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
	 */
	public String SeitenStartUndEndeSetzen() {
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		int anzahlAuswahl = Integer.parseInt(alleSeitenAuswahl_letzteSeite) - Integer.parseInt(alleSeitenAuswahl_ersteSeite) + 1;
		// myLogger.debug("Anzahl der gewählten Seiten: " + anzahlAuswahl);
		if (anzahlAuswahl > 0) {
			/* alle bisher zugewiesenen Seiten entfernen */
			myDocStruct.getAllToReferences().clear();
			int zaehler = 0;
			while (zaehler < anzahlAuswahl) {
				// alleSeitenAuswahl[zaehler]=
				// String.valueOf(Integer.parseInt(alleSeitenAuswahl_ersteSeite)
				// + zaehler);
				myDocStruct.addReferenceTo(alleSeitenNeu[Integer.parseInt(alleSeitenAuswahl_ersteSeite) + zaehler].getMd().getDocStruct(),
						"logical_physical");
				zaehler++;
			}
		}
		StructSeitenErmitteln(myDocStruct);
		return null;
	}

	/**
	 * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
	 */
	@SuppressWarnings("unchecked")
	public String SeitenVonChildrenUebernehmen() {
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";

		/* alle Kinder des aktuellen DocStructs durchlaufen */
		myDocStruct.getAllReferences("to").removeAll(myDocStruct.getAllReferences("to"));
		if (myDocStruct.getAllChildren() != null) {
			for (Iterator iter = myDocStruct.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct child = (DocStruct) iter.next();
				myDocStruct.getAllReferences("to").addAll(child.getAllReferences("to"));
			}
		}
		StructSeitenErmitteln(myDocStruct);
		return null;
	}

	/**
	 * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
	 */
	public String BildErsteSeiteAnzeigen() {
		bildAnzeigen = true;
		if ((Boolean) treeProperties.get("showpagesasajax")) {
			for (int i = 0; i < alleSeiten.length; i++) {
				SelectItem si = alleSeiten[i];
				if (si.getLabel().equals(ajaxSeiteStart)) {
					alleSeitenAuswahl_ersteSeite = (String) si.getValue();
					break;
				}
			}
		}
		BildErmitteln(Integer.parseInt(alleSeitenAuswahl_ersteSeite) - myBildNummer + 1);
		return "";
	}

	/**
	 * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
	 */
	public String BildLetzteSeiteAnzeigen() {
		bildAnzeigen = true;
		if ((Boolean) treeProperties.get("showpagesasajax")) {
			for (int i = 0; i < alleSeiten.length; i++) {
				SelectItem si = alleSeiten[i];
				if (si.getLabel().equals(ajaxSeiteEnde)) {
					alleSeitenAuswahl_letzteSeite = (String) si.getValue();
					break;
				}
			}
		}
		BildErmitteln(Integer.parseInt(alleSeitenAuswahl_letzteSeite) - myBildNummer + 1);
		return "";
	}

	/**
	 * ausgewählte Seiten dem aktuellen Strukturelement hinzufügen ================================================================
	 */
	public String SeitenHinzu() {
		/* alle markierten Seiten durchlaufen */
		for (int i = 0; i < alleSeitenAuswahl.length; i++) {
			int aktuelleID = Integer.parseInt(alleSeitenAuswahl[i]);
			// myLogger.info(alleSeitenNeu[aktuelleID].getWert());

			boolean schonEnthalten = false;

			/*
			 * wenn schon References vorhanden, prüfen, ob schon enthalten, erst dann zuweisen
			 */
			if (myDocStruct.getAllToReferences("logical_physical") != null) {
				for (Iterator<Reference> iter = myDocStruct.getAllToReferences("logical_physical").iterator(); iter.hasNext();) {
					Reference obj = (Reference) iter.next();
					if (obj.getTarget() == alleSeitenNeu[aktuelleID].getMd().getDocStruct()) {
						schonEnthalten = true;
						break;
					}
				}
			}

			if (!schonEnthalten)
				myDocStruct.addReferenceTo(alleSeitenNeu[aktuelleID].getMd().getDocStruct(), "logical_physical");
		}
		StructSeitenErmitteln(myDocStruct);
		alleSeitenAuswahl = null;
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return null;
	}

	/**
	 * ausgewählte Seiten aus dem Strukturelement entfernen ================================================================
	 */
	public String SeitenWeg() {
		for (int i = 0; i < structSeitenAuswahl.length; i++) {
			// myLogger.info(structSeitenAuswahl[i]);
			int aktuelleID = Integer.parseInt(structSeitenAuswahl[i]);
			// myLogger.info(structSeitenNeu[aktuelleID].getWert());
			myDocStruct.removeReferenceTo(structSeitenNeu[aktuelleID].getMd().getDocStruct());
		}
		StructSeitenErmitteln(myDocStruct);
		structSeitenAuswahl = null;
		if (!SperrungAktualisieren())
			return "SperrungAbgelaufen";
		return null;
	}

	/*
	 * ##################################################### ##################################################### ## ## OCR ##
	 * ##################################################### ####################################################
	 */

	public boolean isShowOcrButton() {
		return ConfigMain.getBooleanParameter("showOcrButton");
	}

	public void showOcrResult() {
		String myOcrUrl = getOcrBasisUrl(myBildNummer);
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(myOcrUrl);
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				ocrResult = "HttpStatus nicht ok";
				return;
			}
			ocrResult = method.getResponseBodyAsString();
		} catch (HttpException e) {
			ocrResult = "Fatal protocol violation: " + e.getMessage();
		} catch (IOException e) {
			ocrResult = "Fatal transport error: " + e.getMessage();
		} finally {
			method.releaseConnection();
		}
	}

	public String getOcrResult() {
		return ocrResult;
	}

	public String getOcrAcdress() {
		int startseite = -1;
		int endseite = -1;
		if (structSeiten != null) {
			for (int i = 0; i < structSeiten.length; i++) {
				SelectItem si = structSeiten[i];
				int temp = Integer.parseInt((String) si.getLabel().substring(0, si.getLabel().indexOf(":")));
				if (startseite == -1 || startseite > temp)
					startseite = temp;
				if (endseite == -1 || endseite < temp)
					endseite = temp;
			}
		}
		if (endseite - startseite > 5)
			endseite = startseite + 5;
		return getOcrBasisUrl(startseite, endseite);
	}

	private String getOcrBasisUrl(int... seiten) {
		String url = ConfigMain.getParameter("ocrUrl") + "?path=/";
		try {
			url += myProzess.getImagesDirectory().substring(help.getGoobiDataDirectory().length()) + currentTifFolder;
		} catch (Exception e) {
			myLogger.error(e);
		}
		url += "/&imgrange=" + seiten[0];
		if (seiten.length > 1)
			url += "-" + seiten[1];
		return url;
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
	 */

	public int getBildNummer() {
		return myBildNummer;
	}

	public void setBildNummer(int inBild) {
	}

	public int getBildLetztes() {
		return myBildLetztes;
	}

	public int getBildGroesse() {
		return myBildGroesse;
	}

	public void setBildGroesse(int myBildGroesse) {
		this.myBildGroesse = myBildGroesse;
	}

	public String getTempTyp() {
		if (selectedMetadatum == null) {
			getAddableMetadataTypes();
			selectedMetadatum = tempMetadatumList.get(0);
		}
		return selectedMetadatum.getMd().getType().getName();
	}

	public Metadatum getSelectedMetadatum() {
		return selectedMetadatum;
	}

	public void setSelectedMetadatum(Metadatum newMeta) {
		selectedMetadatum = newMeta;
	}

	public void setTempTyp(String tempTyp) {
		MetadataType mdt = myPrefs.getMetadataTypeByName(tempTyp);
		try {
			Metadata md = new Metadata(mdt);
			selectedMetadatum = new Metadatum(md, myMetadaten.size() + 1, myPrefs, myProzess);
		} catch (MetadataTypeNotAllowedException e) {
			myLogger.error(e.getMessage());
		}
		this.tempTyp = tempTyp;
	}

	public Metadatum getMetadatum() {

		if (selectedMetadatum == null) {
			getAddableMetadataTypes();
			selectedMetadatum = tempMetadatumList.get(0);
		}
		return selectedMetadatum;
	}

	public void setMetadatum(Metadatum meta) {
		selectedMetadatum = meta;
	}

	public String getOutputType() {
		return selectedMetadatum.getOutputType();
	}

	public String getTempWert() {
		return tempWert;
	}

	public void setTempWert(String tempWert) {
		this.tempWert = tempWert;
	}

	public boolean isModusHinzufuegen() {
		return modusHinzufuegen;
	}

	public void setModusHinzufuegen(boolean modusHinzufuegen) {
		this.modusHinzufuegen = modusHinzufuegen;
	}

	public boolean isModusHinzufuegenPerson() {
		return modusHinzufuegenPerson;
	}

	public void setModusHinzufuegenPerson(boolean modusHinzufuegenPerson) {
		this.modusHinzufuegenPerson = modusHinzufuegenPerson;
	}

	public String getTempPersonNachname() {
		return tempPersonNachname;
	}

	public void setTempPersonNachname(String tempPersonNachname) {
		this.tempPersonNachname = tempPersonNachname;
	}

	public String getTempPersonRolle() {
		return tempPersonRolle;
	}

	public void setTempPersonRolle(String tempPersonRolle) {
		this.tempPersonRolle = tempPersonRolle;
	}

	public String getTempPersonVorname() {
		return tempPersonVorname;
	}

	public void setTempPersonVorname(String tempPersonVorname) {
		this.tempPersonVorname = tempPersonVorname;
	}

	public String[] getAlleSeitenAuswahl() {
		return alleSeitenAuswahl;
	}

	public void setAlleSeitenAuswahl(String[] alleSeitenAuswahl) {
		this.alleSeitenAuswahl = alleSeitenAuswahl;
	}

	public SelectItem[] getAlleSeiten() {
		// TODO fix me
		return alleSeiten;
	}

	public SelectItem[] getStructSeiten() {
		// TODO wieder einkommentieren
		if (structSeiten.length > 0 && structSeiten[0] == null) {
			return new SelectItem[0];
		} else
			return structSeiten;
	}

	public String[] getStructSeitenAuswahl() {
		return structSeitenAuswahl;
	}

	public void setStructSeitenAuswahl(String[] structSeitenAuswahl) {
		this.structSeitenAuswahl = structSeitenAuswahl;
	}

	public Prozess getMyProzess() {
		return myProzess;
	}

	public String getModusAnsicht() {
		return modusAnsicht;
	}

	public void setModusAnsicht(String modusAnsicht) {
		this.modusAnsicht = modusAnsicht;
	}

	public String getPaginierungWert() {
		return paginierungWert;
	}

	public void setPaginierungWert(String paginierungWert) {
		this.paginierungWert = paginierungWert;
	}

	public int getPaginierungAbSeiteOderMarkierung() {
		return paginierungAbSeiteOderMarkierung;
	}

	public void setPaginierungAbSeiteOderMarkierung(int paginierungAbSeiteOderMarkierung) {
		this.paginierungAbSeiteOderMarkierung = paginierungAbSeiteOderMarkierung;
	}

	public String getPaginierungArt() {
		return paginierungArt;
	}

	public void setPaginierungArt(String paginierungArt) {
		this.paginierungArt = paginierungArt;
	}

	public boolean isBildAnzeigen() {
		return bildAnzeigen;
	}

	public void BildAnzeigen() {
		bildAnzeigen = !bildAnzeigen;
		if (bildAnzeigen) {
			try {
				BildErmitteln(0);
			} catch (Exception e) {
				Helper.setFehlerMeldung("Error while generating image", e.getMessage());
				myLogger.error(e);
			}
		}
	}

	public String getAddDocStructType1() {
		return addDocStructType1;
	}

	public void setAddDocStructType1(String addDocStructType1) {
		this.addDocStructType1 = addDocStructType1;
	}

	public String getAddDocStructType2() {
		return addDocStructType2;
	}

	public void setAddDocStructType2(String addDocStructType2) {
		this.addDocStructType2 = addDocStructType2;
	}

	public String getBildNummerGeheZu() {
		return "";
	}

	public void setBildNummerGeheZu(String bildNummerGeheZu) {
		this.bildNummerGeheZu = bildNummerGeheZu;
	}

	public boolean isNurLesenModus() {
		return nurLesenModus;
	}

	public void setNurLesenModus(boolean nurLesenModus) {
		this.nurLesenModus = nurLesenModus;
	}

	public boolean isBildZuStrukturelement() {
		return bildZuStrukturelement;
	}

	public void setBildZuStrukturelement(boolean bildZuStrukturelement) {
		this.bildZuStrukturelement = bildZuStrukturelement;
	}

	public String getNeuesElementWohin() {
		if (neuesElementWohin == null || neuesElementWohin == "")
			neuesElementWohin = "1";
		return neuesElementWohin;
	}

	public void setNeuesElementWohin(String inNeuesElementWohin) {
		if (inNeuesElementWohin == null || inNeuesElementWohin.equals(""))
			neuesElementWohin = "1";
		else
			neuesElementWohin = inNeuesElementWohin;
	}

	public String getAlleSeitenAuswahl_ersteSeite() {
		return alleSeitenAuswahl_ersteSeite;
	}

	public void setAlleSeitenAuswahl_ersteSeite(String alleSeitenAuswahl_ersteSeite) {
		this.alleSeitenAuswahl_ersteSeite = alleSeitenAuswahl_ersteSeite;
	}

	public String getAlleSeitenAuswahl_letzteSeite() {
		return alleSeitenAuswahl_letzteSeite;
	}

	public void setAlleSeitenAuswahl_letzteSeite(String alleSeitenAuswahl_letzteSeite) {
		this.alleSeitenAuswahl_letzteSeite = alleSeitenAuswahl_letzteSeite;
	}

	public List<TreeNode> getStrukturBaum3() {
		if (tree3 != null)
			return tree3.getChildrenAsList();
		else
			return new ArrayList<TreeNode>();
	}

	public List<TreeNode> getStrukturBaum3Alle() {
		if (tree3 != null)
			return tree3.getChildrenAsListAlle();
		else
			return new ArrayList<TreeNode>();
	}

	public boolean isModusStrukturelementVerschieben() {
		return modusStrukturelementVerschieben;
	}

	public void setModusStrukturelementVerschieben(boolean modusStrukturelementVerschieben) {
		this.modusStrukturelementVerschieben = modusStrukturelementVerschieben;

		// wenn der Verschiebevorgang gestartet werden soll, dann in allen
		// DocStructs prüfen
		// ob das aktuelle Strukturelement dort eingefügt werden darf
		if (this.modusStrukturelementVerschieben)
			TreeDurchlaufen(tree3);
	}

	@SuppressWarnings("unchecked")
	private void TreeDurchlaufen(TreeNodeStruct3 inTreeStruct) {
		DocStruct temp = inTreeStruct.getStruct();
		if (inTreeStruct.getStruct() == myDocStruct)
			inTreeStruct.setSelected(true);
		else
			inTreeStruct.setSelected(false);

		// alle erlaubten Typen durchlaufen
		for (Iterator<String> iter = temp.getType().getAllAllowedDocStructTypes().iterator(); iter.hasNext();) {
			String dst = (String) iter.next();
			if (myDocStruct.getType().getName().equals(dst)) {
				inTreeStruct.setEinfuegenErlaubt(true);
				break;
			}
		}

		for (Iterator iter = inTreeStruct.getChildren().iterator(); iter.hasNext();) {
			TreeNodeStruct3 kind = (TreeNodeStruct3) iter.next();
			TreeDurchlaufen(kind);
		}
	}

	public void setTempStrukturelement(DocStruct tempStrukturelement) {
		this.tempStrukturelement = tempStrukturelement;
	}

	public List<Metadatum> getMyMetadaten() {
		return myMetadaten;
	}

	public void setMyMetadaten(List<Metadatum> myMetadaten) {
		this.myMetadaten = myMetadaten;
	}

	public List<MetaPerson> getMyPersonen() {
		return myPersonen;
	}

	public void setMyPersonen(List<MetaPerson> myPersonen) {
		this.myPersonen = myPersonen;
	}

	public Metadatum getCurMetadatum() {
		return curMetadatum;
	}

	public void setCurMetadatum(Metadatum curMetadatum) {
		this.curMetadatum = curMetadatum;
	}

	public MetaPerson getCurPerson() {
		return curPerson;
	}

	public void setCurPerson(MetaPerson curPerson) {
		this.curPerson = curPerson;
	}

	public String getAdditionalOpacPpns() {
		return additionalOpacPpns;
	}

	public void setAdditionalOpacPpns(String additionalOpacPpns) {
		this.additionalOpacPpns = additionalOpacPpns;
	}

	public boolean isTreeReloaden() {
		return treeReloaden;
	}

	public void setTreeReloaden(boolean treeReloaden) {
		this.treeReloaden = treeReloaden;
	}

	public HashMap<String, Boolean> getTreeProperties() {
		return treeProperties;
	}

	public void setTreeProperties(HashMap<String, Boolean> treeProperties) {
		this.treeProperties = treeProperties;
	}

	public String getOpacKatalog() {
		return opacKatalog;
	}

	public void setOpacKatalog(String opacKatalog) {
		this.opacKatalog = opacKatalog;
	}

	public String getOpacSuchfeld() {
		return opacSuchfeld;
	}

	public void setOpacSuchfeld(String opacSuchfeld) {
		this.opacSuchfeld = opacSuchfeld;
	}

	public int getPaginierungSeitenProImage() {
		return paginierungSeitenProImage;
	}

	public void setPaginierungSeitenProImage(int paginierungSeitenProImage) {
		this.paginierungSeitenProImage = paginierungSeitenProImage;
	}

	public String getCurrentTifFolder() {
		return currentTifFolder;
	}

	public void setCurrentTifFolder(String currentTifFolder) {
		this.currentTifFolder = currentTifFolder;
	}

	public List<String> autocomplete(Object suggest) {
		String pref = (String) suggest;
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> alle = new ArrayList<String>();
		for (SelectItem si : alleSeiten) {
			alle.add(si.getLabel());
		}

		Iterator<String> iterator = alle.iterator();
		while (iterator.hasNext()) {
			String elem = ((String) iterator.next());
			if (elem != null && elem.contains(pref) || "".equals(pref)) {
				// if ((elem != null && elem.toLowerCase().indexOf(pref.toLowerCase()) == 0) || "".equals(pref)) {
				result.add(elem);
			}
		}
		return result;
	}

	public boolean getIsNotRootElement() {
		if (myDocStruct != null) {
			if (myDocStruct.getParent() == null) {
				return false;
			}
		}
		return true;
	}

}
