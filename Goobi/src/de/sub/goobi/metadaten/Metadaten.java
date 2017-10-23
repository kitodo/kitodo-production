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

package de.sub.goobi.metadaten;

import de.sub.goobi.forms.ProzesskopieForm;
import org.goobi.io.SafeFile;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.goobi.api.display.Modes;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.helper.ConfigDispayRules;
import org.goobi.production.constants.Parameters;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.CataloguePlugin.QueryBuilder;
import org.kitodo.production.lugh.AuthorityFileUtil;
import org.kitodo.production.lugh.ld.*;
import org.kitodo.production.lugh.pagination.*;

import com.hp.hpl.jena.shared.JenaException;
import com.sharkysoft.util.UnreachableCodeException;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.*;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import de.sub.goobi.helper.Transliteration;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.ProzessDAO;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
public class Metadaten {
    private static final Logger logger = Logger.getLogger(Metadaten.class);
    MetadatenImagesHelper imagehelper;
    MetadatenHelper metahelper;
    private boolean treeReloaden = false;
    String ocrResult = "";
    private Fileformat gdzfile;
    private DocStruct myDocStruct;
    private DocStruct tempStrukturelement;
    private List<MetadatumImpl> myMetadaten = new LinkedList<MetadatumImpl>();
    private List<MetaPerson> myPersonen = new LinkedList<MetaPerson>();
    private MetadatumImpl curMetadatum;
    private MetaPerson curPerson;
    private DigitalDocument mydocument;
    private Prozess myProzess;
    private Prefs myPrefs;
    private String myBenutzerID;
    private String tempTyp;
    private String tempWert;
    private String tempPersonRecord;
    private String tempPersonVorname;
    private String tempPersonNachname;
    private String tempPersonRolle;
    private String currentTifFolder;
    private List<String> allTifFolders;
    /* Variablen für die Zuweisung der Seiten zu Strukturelementen */
    private String alleSeitenAuswahl_ersteSeite;
    private String alleSeitenAuswahl_letzteSeite;
    private String[] alleSeitenAuswahl;
    private String[] structSeitenAuswahl;
    private SelectItem alleSeiten[];
    private MetadatumImpl alleSeitenNeu[];
    private ArrayList<MetadatumImpl> tempMetadatumList = new ArrayList<MetadatumImpl>();
    private MetadatumImpl selectedMetadatum;
    private String currentRepresentativePage = "";

    private String paginierungWert;
    private int paginierungAbSeiteOderMarkierung;

    /**
     * Code value for {@link PaginatorType}.
     */
    private String paginierungArt;

    /**
     * Code value for {@link PaginatorMode}.
     */
    private int paginierungSeitenProImage = 1;
    private boolean fictitious = false;

    private SelectItem structSeiten[];
    private MetadatumImpl structSeitenNeu[];
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
    private final MetadatenSperrung sperrung = new MetadatenSperrung();
    private boolean nurLesenModus;
    private String neuesElementWohin = "1";
    private boolean modusStrukturelementVerschieben = false;
    private String additionalOpacPpns;
    private String opacSuchfeld = "12";
    private String opacKatalog;

    private String ajaxSeiteStart = "";
    private String ajaxSeiteEnde = "";
    private String pagesStart = "";
    private String pagesEnd = "";
    private HashMap<String, Boolean> treeProperties;
    private final ReentrantLock xmlReadingLock = new ReentrantLock();
    private FileManipulation fileManipulation = null;
    private boolean addMetadataGroupMode = false;

    private RenderableMetadataGroup newMetadataGroup;
    private boolean addServeralStructuralElementsMode = false;
    private int elementsCount = 1;
    private String addMetaDataType;
    private String addMetaDataValue;

    /**
     * Konstruktor ================================================================
     */
    public Metadaten() {
        this.treeProperties = new HashMap<String, Boolean>();
        this.treeProperties.put("showtreelevel", Boolean.FALSE);
        this.treeProperties.put("showtitle", Boolean.FALSE);
        this.treeProperties.put("fullexpanded", Boolean.TRUE);
        this.treeProperties.put("showfirstpagenumber", Boolean.FALSE);
        this.treeProperties.put("showpagesasajax", Boolean.TRUE);
    }

    /**
     * die Anzeige der Details ändern (z.B. nur die Metadaten anzeigen, oder nur die Paginierungssequenzen)
     *
     * @return Navigationsanweisung "null" als String (also gleiche Seite reloaden)
     */
    public String AnsichtAendern() {
        this.modusAnsicht = Helper.getRequestParameter("Ansicht");
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String Hinzufuegen() {
        this.modusHinzufuegen = true;
        Modes.setBindState(BindState.create);
        getMetadatum().setValue("");
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String HinzufuegenPerson() {
        this.modusHinzufuegenPerson = true;
        this.tempPersonNachname = "";
        this.tempPersonRecord = ConfigMain.getParameter(Parameters.AUTHORITY_DEFAULT, "");
        this.tempPersonVorname = "";
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String Abbrechen() {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        Modes.setBindState(BindState.edit);
        getMetadatum().setValue("");
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String Reload() {
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        } else {
            calculateMetadataAndImages();
            cleanupMetadata();
            // ignoring result of store operation
            storeMetadata();
            return "";
        }
    }

    public String Kopieren() {
        Metadata md;
        try {
            md = new Metadata(this.curMetadatum.getMd().getType());

            md.setValue(this.curMetadatum.getMd().getValue());
            this.myDocStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String KopierenPerson() {
        Person per;
        try {
            per = new Person(this.myPrefs.getMetadataTypeByName(this.curPerson.getP().getRole()));
            per.setFirstname(this.curPerson.getP().getFirstname());
            per.setLastname(this.curPerson.getP().getLastname());
            per.setRole(this.curPerson.getP().getRole());

            this.myDocStruct.addPerson(per);
        } catch (IncompletePersonObjectException e) {
            logger.error("Fehler beim Kopieren von Personen (IncompletePersonObjectException): " + e.getMessage());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim Kopieren von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String ChangeCurrentDocstructType() {

        if (this.myDocStruct != null && this.tempWert != null) {
            try {
                DocStruct rueckgabe = this.metahelper.ChangeCurrentDocstructType(this.myDocStruct, this.tempWert);
                MetadatenalsBeanSpeichern(rueckgabe);
                MetadatenalsTree3Einlesen1();
            } catch (DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (DocStructHasNoTypeException): ", e.getMessage());
                logger.error("Error while changing DocStructTypes (DocStructHasNoTypeException): " + e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (MetadataTypeNotAllowedException): ", e.getMessage());
                logger.error("Error while changing DocStructTypes (MetadataTypeNotAllowedException): " + e.getMessage());
            } catch (TypeNotAllowedAsChildException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedAsChildException): ", e.getMessage());
                logger.error("Error while changing DocStructTypes (TypeNotAllowedAsChildException): " + e.getMessage());
            } catch (TypeNotAllowedForParentException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedForParentException): ", e.getMessage());
                logger.error("Error while changing DocStructTypes (TypeNotAllowedForParentException): " + e.getMessage());
            }
        }
        return "Metadaten3links";
    }

    public String Speichern() {
        try {
            Metadata md = new Metadata(this.myPrefs.getMetadataTypeByName(this.tempTyp));
            md.setValue(this.selectedMetadatum.getValue());

            this.myDocStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        /*
         * wenn TitleDocMain, dann gleich Sortiertitel mit gleichem Inhalt anlegen
         */
        if (this.tempTyp.equals("TitleDocMain") && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
            try {
                Metadata md2 = new Metadata(this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                md2.setValue(this.selectedMetadatum.getValue());
                this.myDocStruct.addMetadata(md2);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }

        this.modusHinzufuegen = false;
        Modes.setBindState(BindState.edit);
        this.selectedMetadatum.setValue("");
        this.tempWert = "";
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String loadRightFrame() {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        Modes.setBindState(BindState.edit);
        return "Metadaten2rechts";
    }

    public String SpeichernPerson() {
        try {
            Person per = new Person(this.myPrefs.getMetadataTypeByName(this.tempPersonRolle));
            per.setFirstname(this.tempPersonVorname);
            per.setLastname(this.tempPersonNachname);
            per.setRole(this.tempPersonRolle);
            String[] authorityFile = parseAuthorityFileArgs(tempPersonRecord);
            per.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
            this.myDocStruct.addPerson(per);
        } catch (IncompletePersonObjectException e) {
            Helper.setFehlerMeldung("Incomplete data for person", "");

            return "";
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setFehlerMeldung("Person is for this structure not allowed", "");
            return "";
        }
        this.modusHinzufuegenPerson = false;
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * The function parseAuthorityFileArgs() parses a valueURI (i.e.
     * “http://d-nb.info/gnd/117034592”) and returns the three arguments
     * authority, authorityURI and valueURI required to call
     * {@link ugh.dl.Metadata#setAutorityFile(String, String, String)}. The
     * authorityURI may end in # or / otherwise. The authority’s name id must be
     * configured in the main configuration file like referencing the
     * authorityURI (remember to escape colons):
     *
     * <code>authority.http\://d-nb.info/gnd/.id=gnd</code>
     *
     * @param an
     *            URI in an authority file
     * @return a String[] with authority, authorityURI and valueURI
     */
    static String[] parseAuthorityFileArgs(String valueURI) {
        String authority = null, authorityURI = null;
        if (valueURI != null && !valueURI.isEmpty()) {
            int boundary = valueURI.indexOf('#');
            if (boundary == -1) {
                boundary = valueURI.lastIndexOf('/');
            }
            if (boundary == -1) {
                throw new IncompletePersonObjectException("URI_malformed");
            } else {
                authorityURI = valueURI.substring(0, boundary + 1);
                if (!authorityURI.equals(valueURI)) {
                    authority = new Parameters(ConfigMain.toMap()).getNamespacePrefixes(false, true).get(authorityURI);
                }
            }
        }
        return new String[] { authority, authorityURI, valueURI };
    }

    public String Loeschen() {
        this.myDocStruct.removeMetadata(this.curMetadatum.getMd());
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String LoeschenPerson() {
        this.myDocStruct.removePerson(this.curPerson.getP());
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * die noch erlaubten Rollen zurückgeben ================================================================
     */
    public ArrayList<SelectItem> getAddableRollen() {
        return this.metahelper.getAddablePersonRoles(this.myDocStruct, "");
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
        return getAddableMetadataTypes(myDocStruct, tempMetadatumList);
    }

    private ArrayList<SelectItem> getAddableMetadataTypes(DocStruct myDocStruct, ArrayList<MetadatumImpl> tempMetadatumList) {
        ArrayList<SelectItem> myList = new ArrayList<SelectItem>();
        /*
         * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = myDocStruct.getAddableMetadataTypes();
        if (types == null) {
            return myList;
        }

        /*
         * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
         */
        for (MetadataType mdt : new ArrayList<MetadataType>(types)) {
            if (mdt.getIsPerson()) {
                types.remove(mdt);
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        int counter = types.size();

        for (MetadataType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), this.metahelper.getMetadatatypeLanguage(mdt)));
            try {
                Metadata md = new Metadata(mdt);
                MetadatumImpl mdum = new MetadatumImpl(md, counter, this.myPrefs, this.myProzess);
                counter++;
                if (tempMetadatumList != null) {
                    tempMetadatumList.add(mdum);
                }

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        return myList;
    }

    public ArrayList<MetadatumImpl> getTempMetadatumList() {
        return this.tempMetadatumList;
    }

    public void setTempMetadatumList(ArrayList<MetadatumImpl> tempMetadatumList) {
        this.tempMetadatumList = tempMetadatumList;
    }

    /**
     * die MetadatenTypen zurückgeben ================================================================
     */
    public SelectItem[] getMetadatenTypen() {
        /*
         * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = this.myDocStruct.getAddableMetadataTypes();

        if (types == null) {
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

            myTypen[zaehler] = new SelectItem(mdt.getName(), this.metahelper.getMetadatatypeLanguage(mdt));
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
        String result = "";
        if (xmlReadingLock.tryLock()) {
            try {
                result = readXmlAndBuildTree();
            } catch (RuntimeException rte) {
                throw rte;
            } finally {
                xmlReadingLock.unlock();
            }
        } else {
            Helper.setFehlerMeldung("metadatenEditorThreadLock");
        }

        return result;
    }

    private String readXmlAndBuildTree() {



        /*
         * re-reading the config for display rules
         */
        ConfigDispayRules.getInstance().refresh();

        Modes.setBindState(BindState.edit);
        try {
            Integer id = Integer.valueOf(Helper.getRequestParameter("ProzesseID"));
            this.myProzess = new ProzessDAO().get(id);
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
            return Helper.getRequestParameter("zurueck");
        } catch (DAOException e1) {
            Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
            return Helper.getRequestParameter("zurueck");
        }
        this.myBenutzerID = Helper.getRequestParameter("BenutzerID");
        this.alleSeitenAuswahl_ersteSeite = "";
        this.alleSeitenAuswahl_letzteSeite = "";
        this.zurueck = Helper.getRequestParameter("zurueck");
        this.nurLesenModus = Helper.getRequestParameter("nurLesen").equals("true") ? true : false;
        this.neuesElementWohin = "1";
        this.tree3 = null;
        try {
            XMLlesenStart();
        } catch (DAOException | IOException | InterruptedException | PreferencesException | ReadException |
                 SwapException | WriteException e) {
            logger.error(e);
            Helper.setFehlerMeldung("metadataCorrupt", e);
            return Helper.getRequestParameter("zurueck");
        }

        TreeExpand();
        this.sperrung.setLocked(this.myProzess.getId().intValue(), this.myBenutzerID);
        return "Metadaten";
    }

    /**
     * Metadaten Einlesen
     *
     * @throws ReadException
     * @throws InterruptedException
     * @throws IOException
     * @throws PreferencesException ============================================================ == ==
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */

    public String XMLlesenStart() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
            WriteException {
        currentRepresentativePage = "";
        this.myPrefs = this.myProzess.getRegelsatz().getPreferences();
        this.modusAnsicht = "Metadaten";
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        this.modusStrukturelementVerschieben = false;
        // TODO: Make file pattern configurable
        this.myBild = null;
        this.myBildNummer = 1;
        this.myImageRotation = 0;
        this.currentTifFolder = null;
        readAllTifFolders();

        /*
         * -------------------------------- Dokument einlesen --------------------------------
         */
        this.gdzfile = this.myProzess.readMetadataFile();
        this.mydocument = this.gdzfile.getDigitalDocument();
        this.mydocument.addAllContentFiles();
        this.metahelper = new MetadatenHelper(this.myPrefs, this.mydocument);
        this.imagehelper = new MetadatenImagesHelper(this.myPrefs, this.mydocument);

        /*
         * -------------------------------- Das Hauptelement ermitteln --------------------------------
         */

        // TODO: think something up, how to handle a not matching ruleset
        // causing logicalDocstruct to be null
        this.logicalTopstruct = this.mydocument.getLogicalDocStruct();

        // this exception needs some serious feedback because data is corrupted
        if (this.logicalTopstruct == null) {
            throw new ReadException(Helper.getTranslation("metaDataError"));
        }

        BildErmitteln(0);
        retrieveAllImages();
        if (ConfigMain.getBooleanParameter(Parameters.WITH_AUTOMATIC_PAGINATION, true) && (this.mydocument.getPhysicalDocStruct() == null || this.mydocument.getPhysicalDocStruct().getAllChildren() == null
                || this.mydocument.getPhysicalDocStruct().getAllChildren().size() == 0)) {
            try {
                createPagination(false);
            } catch (TypeNotAllowedForParentException e) {

            }
        }

        if (this.mydocument.getPhysicalDocStruct().getAllMetadata() != null && this.mydocument.getPhysicalDocStruct().getAllMetadata().size() > 0) {
            for (Metadata md : this.mydocument.getPhysicalDocStruct().getAllMetadata()) {
                if (md.getType().getName().equals("_representative")) {
                    try {
                    Integer value = Integer.valueOf(md.getValue());
                    currentRepresentativePage = String.valueOf(value-1);
                    } catch (Exception e) {

                    }
                }
            }
        }

        createDefaultValues(this.logicalTopstruct);
        MetadatenalsBeanSpeichern(this.logicalTopstruct);
        MetadatenalsTree3Einlesen1();

        if (!this.nurLesenModus) {
            // inserted to make Paginierung the starting view
            this.modusAnsicht = "Paginierung";
        }
        return "Metadaten";
    }

    private void createDefaultValues(DocStruct element) {
        if (ConfigMain.getBooleanParameter("MetsEditorEnableDefaultInitialisation", true)) {
            MetadatenalsBeanSpeichern(element);
            if (element.getAllChildren() != null && element.getAllChildren().size() > 0) {
                for (DocStruct ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }



    private void calculateMetadataAndImages() {

        /*
         * für den Prozess nochmal die Metadaten durchlaufen und die Daten speichern
         */
        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        this.myProzess.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.DOCSTRUCT));
        this.myProzess.setSortHelperMetadata(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.METADATA));
        try {
            this.myProzess.setSortHelperImages(FileUtils.getNumberOfFiles(new SafeFile(this.myProzess.getImagesOrigDirectory(true))));
            new ProzessDAO().save(this.myProzess);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
        } catch (Exception e) {
            Helper.setFehlerMeldung("error while counting current images", e);
            logger.error(e);
        }
    }

    private void cleanupMetadata() {
        /*
         * --------------------- vor dem Speichern alle ungenutzen Docstructs rauswerfen -------------------
         */
        this.metahelper.deleteAllUnusedElements(this.mydocument.getLogicalDocStruct());

        if (currentRepresentativePage != null && currentRepresentativePage.length() > 0) {
            boolean match = false;
            if (this.mydocument.getPhysicalDocStruct() != null && this.mydocument.getPhysicalDocStruct().getAllMetadata() != null
                    && this.mydocument.getPhysicalDocStruct().getAllMetadata().size() > 0) {
                for (Metadata md : this.mydocument.getPhysicalDocStruct().getAllMetadata()) {
                    if (md.getType().getName().equals("_representative")) {
                        Integer value = Integer.valueOf(currentRepresentativePage);
                        md.setValue(String.valueOf(value +1));
                        match = true;
                    }
                }
            }
            if (!match) {
                MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
                try {
                    Metadata md = new Metadata(mdt);
                    Integer value = Integer.valueOf(currentRepresentativePage);
                    md.setValue(String.valueOf(value +1));
                    this.mydocument.getPhysicalDocStruct().addMetadata(md);
                } catch (MetadataTypeNotAllowedException e) {

                }

            }
        }
    }


    private boolean storeMetadata() {
        boolean result = true;
        try {
            this.myProzess.writeMetadataFile(this.gdzfile);
        } catch (Exception e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
            result = false;

        }
        return result;
    }


    /**
     * Metadaten Schreiben
     *
     * @throws InterruptedException
     * @throws IOException
     *             ============================================================ == ==
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     * @throws PreferencesException
     */
    public String XMLschreiben() {

        calculateMetadataAndImages();

        cleanupMetadata();

        if (!storeMetadata()) {
            return "Metadaten";
        }

        SperrungAufheben();
        return this.zurueck;
    }

    public boolean isCheckForRepresentative() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
        if (mdt != null) {
            return true;
        }
        return false;
    }

    /**
     * vom aktuellen Strukturelement alle Metadaten einlesen
     *
     * @param inStrukturelement ============================================================== ==
     */

    private void MetadatenalsBeanSpeichern(DocStruct inStrukturelement) {
        this.myDocStruct = inStrukturelement;
        LinkedList<MetadatumImpl> lsMeta = new LinkedList<MetadatumImpl>();
        LinkedList<MetaPerson> lsPers = new LinkedList<MetaPerson>();

        /*
         * -------------------------------- alle Metadaten und die DefaultDisplay-Werte anzeigen --------------------------------
         */
        List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(inStrukturelement,
                (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"), false, this.myProzess);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.myProzess);
                meta.getSelectedItem();
                lsMeta.add(meta);
            }
        }


        /*
         * -------------------------------- alle Personen und die DefaultDisplay-Werte ermitteln --------------------------------
         */
        myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(inStrukturelement,
                (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"), true, this.myProzess);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                lsPers.add(new MetaPerson((Person) metadata, 0, this.myPrefs, inStrukturelement));
            }
        }

        this.myMetadaten = lsMeta;
        this.myPersonen = lsPers;

        /*
         * -------------------------------- die zugehörigen Seiten ermitteln --------------------------------
         */
        StructSeitenErmitteln(this.myDocStruct);
    }

    /*
     * ##################################################### ##################################################### ## ## Treeview ##
     * ##################################################### ####################################################
     */

    @SuppressWarnings("rawtypes")
    private String MetadatenalsTree3Einlesen1() {
        HashMap map;
        TreeNodeStruct3 knoten;
        List<DocStruct> status = new ArrayList<DocStruct>();

        /*
         * -------------------------------- den Ausklapp-Zustand aller Knoten erfassen --------------------------------
         */
        if (this.tree3 != null) {
            for (Iterator iter = this.tree3.getChildrenAsList().iterator(); iter.hasNext();) {
                map = (HashMap) iter.next();
                knoten = (TreeNodeStruct3) map.get("node");
                if (knoten.isExpanded()) {
                    status.add(knoten.getStruct());
                }
            }
        }

        if (this.logicalTopstruct == null) {
            return "Metadaten3links";
        }
        /*
         * -------------------------------- Die Struktur als Tree3 aufbereiten --------------------------------
         */
        String label = this.logicalTopstruct.getType().getNameByLanguage(
                (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
        if (label == null) {
            label = this.logicalTopstruct.getType().getName();
        }

        this.tree3 = new TreeNodeStruct3(label, this.logicalTopstruct);
        MetadatenalsTree3Einlesen2(this.logicalTopstruct, this.tree3);

        /*
         * -------------------------------- den Ausklappzustand nach dem neu-Einlesen wieder herstellen --------------------------------
         */
        for (Iterator iter = this.tree3.getChildrenAsListAlle().iterator(); iter.hasNext();) {
            map = (HashMap) iter.next();
            knoten = (TreeNodeStruct3) map.get("node");
            // Ausklappstatus wiederherstellen
            if (status.contains(knoten.getStruct())) {
                knoten.setExpanded(true);
            }
            // Selection wiederherstellen
            if (this.myDocStruct == knoten.getStruct()) {
                knoten.setSelected(true);
            }
        }

        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "Metadaten3links";
    }

    /**
     * Metadaten in Tree3 ausgeben
     *
     * @param inStrukturelement ============================================================== ==
     */
    private void MetadatenalsTree3Einlesen2(DocStruct inStrukturelement, TreeNodeStruct3 OberKnoten) {
        OberKnoten.setMainTitle(MetadatenErmitteln(inStrukturelement, "TitleDocMain"));
        OberKnoten.setZblNummer(MetadatenErmitteln(inStrukturelement, "ZBLIdentifier"));
        OberKnoten.setZblSeiten(MetadatenErmitteln(inStrukturelement, "ZBLPageNumber"));
        OberKnoten.setPpnDigital(MetadatenErmitteln(inStrukturelement, "IdentifierDigital"));
        OberKnoten.setFirstImage(this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_FIRST));
        OberKnoten.setLastImage(this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_LAST));
        // wenn es ein Heft ist, die Issue-Number mit anzeigen
        if (inStrukturelement.getType().getName().equals("PeriodicalIssue")) {
            OberKnoten.setDescription(OberKnoten.getDescription() + " " + MetadatenErmitteln(inStrukturelement, "CurrentNo"));
        }

        // wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
        if (inStrukturelement.getType().getName().equals("Periodical") || inStrukturelement.getType().getName().equals("PeriodicalVolume")) {
            OberKnoten.setExpanded(true);
        }

        /*
         * -------------------------------- vom aktuellen Strukturelement alle Kinder in den Tree packen --------------------------------
         */
        List<DocStruct> meineListe = inStrukturelement.getAllChildren();
        if (meineListe != null) {
            /* es gibt Kinder-Strukturelemente */
            for (DocStruct kind : meineListe) {
                String label = kind.getType().getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
                if (label == null) {
                    label = kind.getType().getName();
                }
                TreeNodeStruct3 tns = new TreeNodeStruct3(label, kind);
                OberKnoten.addChild(tns);
                MetadatenalsTree3Einlesen2(kind, tns);
            }
        }
    }

    /**
     * Metadaten gezielt zurückgeben
     *
     * @param inStrukturelement ============================================================== ==
     */
    private String MetadatenErmitteln(DocStruct inStrukturelement, String inTyp) {
        String rueckgabe = "";
        List<Metadata> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                if (md.getType().getName().equals(inTyp)) {
                    rueckgabe += (md.getValue() == null ? "" : md.getValue()) + " ";
                }
            }
        }
        return rueckgabe.trim();
    }



    @SuppressWarnings("rawtypes")
    public void setMyStrukturelement(DocStruct inStruct) {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        Modes.setBindState(BindState.edit);
        MetadatenalsBeanSpeichern(inStruct);

        /*
         * -------------------------------- die Selektion kenntlich machen --------------------------------
         */
        for (Iterator iter = this.tree3.getChildrenAsListAlle().iterator(); iter.hasNext();) {

            HashMap map = (HashMap) iter.next();
            TreeNodeStruct3 knoten = (TreeNodeStruct3) map.get("node");
            // Selection wiederherstellen
            if (this.myDocStruct == knoten.getStruct()) {
                knoten.setSelected(true);
            } else {
                knoten.setSelected(false);
            }
        }

        SperrungAktualisieren();
    }

    /**
     * Knoten nach oben schieben ================================================================
     */
    public String KnotenUp() {
        try {
            this.metahelper.KnotenUp(this.myDocStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if(logger.isDebugEnabled()){
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        return MetadatenalsTree3Einlesen1();
    }

    /**
     * Knoten nach unten schieben ================================================================
     */
    public String KnotenDown() {
        try {
            this.metahelper.KnotenDown(this.myDocStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if(logger.isDebugEnabled()){
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        return MetadatenalsTree3Einlesen1();
    }

    /**
     * Knoten zu einer anderen Stelle
     *
     * @throws TypeNotAllowedAsChildException ============================================================ == ==
     */
    public String KnotenVerschieben() throws TypeNotAllowedAsChildException {
        this.myDocStruct.getParent().removeChild(this.myDocStruct);
        this.tempStrukturelement.addChild(this.myDocStruct);
        MetadatenalsTree3Einlesen1();
        logger.debug(this.modusStrukturelementVerschieben);
        this.neuesElementWohin = "1";
        return "Metadaten3links";
    }

    /**
     * Knoten nach oben schieben
     *
     * @throws IOException ============================================================ == ==
     */
    public String KnotenDelete() throws IOException {
        if (this.myDocStruct != null && this.myDocStruct.getParent() != null) {
            DocStruct tempParent = this.myDocStruct.getParent();
            this.myDocStruct.getParent().removeChild(this.myDocStruct);
            this.myDocStruct = tempParent;
        }
        // den Tree neu einlesen
        return MetadatenalsTree3Einlesen1();
    }

    /**
     * Adds structural elements to the structural tree.
     *
     * @return ID of the page to navigate to
     */
    public String addNodesClick() {
        TreeInsertionMode mode = TreeInsertionMode.fromIntString(neuesElementWohin);
        DocStructType type;
        switch (mode) {
        case BEFORE_ELEMENT:
            type = myPrefs.getDocStrctTypeByName(addDocStructType1);
            break;
        case AFTER_ELEMENT:
            type = myPrefs.getDocStrctTypeByName(addDocStructType1);
            break;
        case AS_FIRST_CHILD:
            type = myPrefs.getDocStrctTypeByName(addDocStructType2);
            break;
        case AS_LAST_CHILD:
            type = myPrefs.getDocStrctTypeByName(addDocStructType2);
            break;
        default:
            throw new UnreachableCodeException("complete switch");
        }
        int quantity = addServeralStructuralElementsMode ? elementsCount : 1;
        if (type != null) {
            DocStruct ds = addNodes(myDocStruct, mydocument, type, mode, quantity, addMetaDataType, addMetaDataValue);
            if (!addServeralStructuralElementsMode && ds != null) {
                DocStruct temp = this.myDocStruct;
                this.myDocStruct = ds;
                this.ajaxSeiteStart = this.pagesStart;
                this.ajaxSeiteEnde = this.pagesEnd;
                AjaxSeitenStartUndEndeSetzen();
                this.myDocStruct = temp;
            }
        }
        MetadatenalsTree3Einlesen1();
        return SperrungAktualisieren() ? "Metadaten3links" : "SperrungAbgelaufen";
    }

    /**
     * Adds nodes to the document structure tree.
     *
     * @param selection
     *            structural element currently selected
     * @param factory
     *            digital document able to create structural elements
     * @param type
     *            type of structural element to create
     * @param mode
     *            insert location relative to the selection
     * @param quantity
     *            number of elements to create
     * @param field
     *            meta-data field to be added to the child
     * @param value
     *            value to be written in the meta-data field
     * @return the first created element
     */
    private static DocStruct addNodes(DocStruct selection, DigitalDocument factory, DocStructType type,
            TreeInsertionMode mode, int quantity, String field, String value) {
        try {
            Paginator enumeratingLabel = field != null && !field.isEmpty() && value != null && !value.isEmpty() ? new Paginator(value) : null;
            ArrayList<DocStruct> createdElements = new ArrayList<>(quantity);
            for (int i = 0; i < quantity; i++) {
                DocStruct createdElement = factory.createDocStruct(type);
                if (enumeratingLabel != null) {
                    createdElement.addMetadata(field, enumeratingLabel.next());
                }
                createdElements.add(createdElement);
            }
            if (mode.equals(TreeInsertionMode.AS_LAST_CHILD)) {
                for (DocStruct element : createdElements) {
                    selection.addChild(element);
                }
            } else {
                DocStruct edited = mode.equals(TreeInsertionMode.AS_FIRST_CHILD) ? selection
                        : selection.getParent();
                if (edited == null) {
                    logger.debug("The selected element cannot investigate the father.");
                    return null;
                }

                List<DocStruct> childrenBefore = edited.getAllChildren();
                if (childrenBefore == null) {
                    for (DocStruct element : createdElements) {
                        edited.addChild(element);
                    }
                } else {
                    // Build a new list of children for the edited element
                    List<DocStruct> newChildren = new ArrayList<>(childrenBefore.size() + 1);
                    if (mode.equals(TreeInsertionMode.AS_FIRST_CHILD)) {
                        for (DocStruct createdElement : createdElements) {
                            newChildren.add(createdElement);
                        }
                    }
                    for (DocStruct child : childrenBefore) {
                        if (child == selection && mode.equals(TreeInsertionMode.BEFORE_ELEMENT)) {
                            for (DocStruct element : createdElements) {
                                newChildren.add(element);
                            }
                        }
                        newChildren.add(child);
                        if (child == selection && mode.equals(TreeInsertionMode.AFTER_ELEMENT)) {
                            for (DocStruct element : createdElements) {
                                newChildren.add(element);
                            }
                        }
                    }

                    // Remove the existing children
                    for (DocStruct child : newChildren) {
                        edited.removeChild(child);
                    }

                    // Set the new children on the edited element
                    for (DocStruct child : newChildren) {
                        edited.addChild(child);
                    }
                }
            }
            return createdElements.iterator().next();
        } catch (UGHException iek) {
            throw new RuntimeException(iek.getMessage(), iek);
        }
    }

    /**
     * mögliche Docstructs als Kind zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsKind() {
        return this.metahelper.getAddableDocStructTypen(this.myDocStruct, false);
    }

    /**
     * mögliche Docstructs als Nachbar zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsNachbar() {
        return this.metahelper.getAddableDocStructTypen(this.myDocStruct, true);
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
        createPagination(true);
        return "";
    }

    private void createPagination(boolean determineImage) throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException, DAOException {
        this.imagehelper.createPagination(this.myProzess, this.currentTifFolder);
        retrieveAllImages();

        // added new
        DocStruct log = this.mydocument.getLogicalDocStruct();
        while (log.getType().getAnchorClass() != null && log.getAllChildren() != null
                && log.getAllChildren().size() > 0) {
            log = log.getAllChildren().get(0);
        }
        if (log.getType().getAnchorClass() != null) {
            return;
        }

        if (log.getAllChildren() != null) {
            for (Iterator<DocStruct> iter = log.getAllChildren().iterator(); iter.hasNext();) {
                DocStruct child = iter.next();
                List<Reference> childRefs = child.getAllReferences("to");
                for (Reference toAdd : childRefs) {
                    boolean match = false;
                    for (Reference ref : log.getAllReferences("to")) {
                        if (ref.getTarget().equals(toAdd.getTarget())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        log.getAllReferences("to").add(toAdd);
                    }

                }
            }
        }

        if (determineImage) {
            BildErmitteln(0);
        }
    }

    /**
     * alle Seiten ermitteln ================================================================
     */
    public void retrieveAllImages() {
        DigitalDocument mydocument = null;
        try {
            mydocument = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMeldung(null, "Cannot get DigitalDocument: ", e.getMessage());
            return;
        }

        List<DocStruct> meineListe = mydocument.getPhysicalDocStruct().getAllChildren();
        if (meineListe == null) {
            this.alleSeiten = null;
            return;
        }
        int zaehler = meineListe.size();
        this.alleSeiten = new SelectItem[zaehler];
        this.alleSeitenNeu = new MetadatumImpl[zaehler];
        zaehler = 0;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        for (DocStruct mySeitenDocStruct : meineListe) {
            List<? extends Metadata> mySeitenDocStructMetadaten = mySeitenDocStruct.getAllMetadataByType(mdt);
            for (Metadata meineSeite : mySeitenDocStructMetadaten) {
                this.alleSeitenNeu[zaehler] = new MetadatumImpl(meineSeite, zaehler, this.myPrefs, this.myProzess);
                this.alleSeiten[zaehler] = new SelectItem(String.valueOf(zaehler),
                    MetadatenErmitteln(meineSeite.getDocStruct(), "physPageNumber").trim() + ": " + meineSeite.getValue());
            }
            zaehler++;
        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln ================================================================
     */
    private void StructSeitenErmitteln(DocStruct inStrukturelement) {
        if (inStrukturelement == null) {
            return;
        }
        List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
        int zaehler = 0;
        int imageNr = 0;
        if (listReferenzen != null) {
            /*
             * -------------------------------- Referenzen sortieren --------------------------------
             */
            Collections.sort(listReferenzen, new Comparator<Reference>() {
                @Override
                public int compare(final Reference o1, final Reference o2) {
                    final Reference r1 = o1;
                    final Reference r2 = o2;
                    Integer page1 = 0;
                    Integer page2 = 0;

                    MetadataType mdt = Metadaten.this.myPrefs.getMetadataTypeByName("physPageNumber");
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
            this.structSeiten = new SelectItem[listReferenzen.size()];
            this.structSeitenNeu = new MetadatumImpl[listReferenzen.size()];

            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (Reference ref : listReferenzen) {
                DocStruct target = ref.getTarget();
                StructSeitenErmitteln2(target, zaehler);
                if (imageNr == 0) {
                    imageNr = StructSeitenErmitteln3(target);
                }
                zaehler++;
            }

        }

        /*
         * Wenn eine Verknüpfung zwischen Strukturelement und Bildern sein soll, das richtige Bild anzeigen
         */
        if (this.bildZuStrukturelement) {
            BildErmitteln(imageNr - this.myBildNummer);
        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln 2 ================================================================
     */
    private void StructSeitenErmitteln2(DocStruct inStrukturelement, int inZaehler) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return;
        }
        for (Metadata meineSeite : listMetadaten) {
            this.structSeitenNeu[inZaehler] = new MetadatumImpl(meineSeite, inZaehler, this.myPrefs, this.myProzess);
            this.structSeiten[inZaehler] = new SelectItem(String.valueOf(inZaehler), MetadatenErmitteln(meineSeite.getDocStruct(), "physPageNumber")
                    .trim() + ": " + meineSeite.getValue());
        }
    }

    /**
     * noch für Testzweck zum direkten öffnen der richtigen Startseite 3 ================================================================
     */
    private int StructSeitenErmitteln3(DocStruct inStrukturelement) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return 0;
        }
        int rueckgabe = 0;
        for (Metadata meineSeite : listMetadaten) {
            rueckgabe = Integer.parseInt(meineSeite.getValue());
        }
        return rueckgabe;
    }

    /**
     * Changes the pagination.
     *
     * @return always {@code null}, indicating to JSF to stay on the same page
     */
    public String Paginierung() {
        final int FROMFIRST = 1;

        Paginator paginator;
        try {
            PaginatorMode mode = PaginatorMode.valueOf(paginierungSeitenProImage);
            PaginatorType type = PaginatorType.valueOf(Integer.parseInt(paginierungArt));
            String initializer = type.format(mode, paginierungWert, fictitious, paginierungSeparators.getObject().getSeparatorString());
            paginator = new Paginator(initializer);
        } catch (NumberFormatException nfe) {
            return null;
        }

        // assert selection is not null

        if (alleSeitenAuswahl == null || alleSeitenAuswahl.length == 0) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", "No pages selected for pagination.");
        } else {

            // apply pagination sequence

            try {
                if (paginierungAbSeiteOderMarkierung == FROMFIRST) {
                    // apply from first selected
                    for (int pageNum = Integer.parseInt(alleSeitenAuswahl[0]);
                            pageNum < alleSeitenNeu.length; pageNum++) {
                        alleSeitenNeu[pageNum].setWert(paginator.next());
                    }
                } else {
                    // apply to selected
                    for (String selected : alleSeitenAuswahl) {
                        alleSeitenNeu[Integer.parseInt(selected)].setWert(paginator.next());
                    }
                }
            } catch (IllegalArgumentException iae) {
                Helper.setFehlerMeldung("fehlerBeimEinlesen", iae.getMessage());
            }
        }

        /*
         * -------------------------------- zum Schluss nochmal alle Seiten neu einlesen --------------------------------
         */
        alleSeitenAuswahl = null;
        retrieveAllImages();
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }

        return null;
    }

    /**
     * alle Knoten des Baums expanden oder collapsen ================================================================
     */
    public String TreeExpand() {
        this.tree3.expandNodes(this.treeProperties.get("fullexpanded"));
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
        if (parameter.equals("")) {
            parameter = "0";
        }
        int tempint = Integer.parseInt(parameter);
        BildErmitteln(tempint);
        return "";
    }

    public void rotateLeft() {
        if (this.myImageRotation < 90) {
            this.myImageRotation = 360;
        }
        this.myImageRotation = (this.myImageRotation - 90) % 360;
        BildErmitteln(0);
    }

    public void rotateRight() {
        this.myImageRotation = (this.myImageRotation + 90) % 360;
        BildErmitteln(0);
    }

    public String BildGeheZu() {
        int eingabe;
        try {
            eingabe = Integer.parseInt(this.bildNummerGeheZu);
        } catch (Exception e) {
            eingabe = this.myBildNummer;
        }

        BildErmitteln(eingabe - this.myBildNummer);
        return "";
    }

    public String BildZoomPlus() {
        this.myBildGroesse += 10;
        BildErmitteln(0);
        return "";
    }

    public String BildZoomMinus() {
        if (this.myBildGroesse > 10) {
            this.myBildGroesse -= 10;
        }
        BildErmitteln(0);
        return "";
    }

    public String getBild() {
        BildPruefen();
        /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        return ConfigMain.getTempImagesPath() + session.getId() + "_" + this.myBildCounter + ".png";
    }

    public List<String> getAllTifFolders() {
        return this.allTifFolders;
    }

    public void readAllTifFolders() throws IOException, InterruptedException, SwapException, DAOException {
        this.allTifFolders = new ArrayList<String>();
        SafeFile dir = new SafeFile(this.myProzess.getImagesDirectory());

        /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
        // TODO: Remove this, we have several implementions of this, use an
        // existing one.
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File indir, String name) {
                return (new SafeFile(indir + File.separator + name).isDirectory());
            }
        };

        String[] verzeichnisse = dir.list(filterVerz);
        for (int i = 0; i < verzeichnisse.length; i++) {
            this.allTifFolders.add(verzeichnisse[i]);
        }

        if (ConfigMain.getParameter("MetsEditorDefaultSuffix", null) != null) {
            String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix");
            for (String directory : this.allTifFolders) {
                if (directory.endsWith(suffix)) {
                    this.currentTifFolder = directory;
                    break;
                }
            }
        }

        if (!this.allTifFolders.contains(this.currentTifFolder)) {
            this.currentTifFolder = new SafeFile(this.myProzess.getImagesTifDirectory(true)).getName();
        }
    }

     public void BildErmitteln(int welches) {
            /*
             * wenn die Bilder nicht angezeigt werden, brauchen wir auch das Bild nicht neu umrechnen
             */
            logger.trace("start BildErmitteln 1");
            if (!this.bildAnzeigen) {
                logger.trace("end BildErmitteln 1");
                return;
            }
            logger.trace("ocr BildErmitteln");
            this.ocrResult = "";

            logger.trace("dataList");
            List<String> dataList = this.imagehelper.getImageFiles(mydocument.getPhysicalDocStruct());
            logger.trace("dataList 2");
            if (ConfigMain.getBooleanParameter(Parameters.WITH_AUTOMATIC_PAGINATION, true) && (dataList == null || dataList.isEmpty())) {
                try {
                    createPagination(false);
                    dataList = this.imagehelper.getImageFiles(mydocument.getPhysicalDocStruct());
                } catch (TypeNotAllowedForParentException e) {
                    logger.error(e);
                } catch (SwapException e) {
                    logger.error(e);
                } catch (DAOException e) {
                    logger.error(e);
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
            if (dataList != null && dataList.size() > 0) {
                logger.trace("dataList not null");
                this.myBildLetztes = dataList.size();
                logger.trace("myBildLetztes");
                for (int i = 0; i < dataList.size(); i++) {
                    if(logger.isTraceEnabled()){
                        logger.trace("file: " + i);
                    }
                    if (this.myBild == null) {
                        this.myBild = dataList.get(0);
                    }
                    if(logger.isTraceEnabled()){
                        logger.trace("myBild: " + this.myBild);
                    }
                    String index = dataList.get(i).substring(0, dataList.get(i).lastIndexOf("."));
                    if(logger.isTraceEnabled()){
                        logger.trace("index: " + index);
                    }
                    String myPicture = this.myBild.substring(0, this.myBild.lastIndexOf("."));
                    if(logger.isTraceEnabled()){
                        logger.trace("myPicture: " + myPicture);
                    }
                    /* wenn das aktuelle Bild gefunden ist, das neue ermitteln */
                    if (index.equals(myPicture)) {
                        logger.trace("index == myPicture");
                        int pos = i + welches;
                        if(logger.isTraceEnabled()){
                            logger.trace("pos: " + pos);
                        }
                        /* aber keine Indexes ausserhalb des Array erlauben */
                        if (pos < 0) {
                            pos = 0;
                        }
                        if (pos > dataList.size() - 1) {
                            pos = dataList.size() - 1;
                        }
                        if (this.currentTifFolder != null) {
                            if(logger.isTraceEnabled()){
                                logger.trace("currentTifFolder: " + this.currentTifFolder);
                            }
                            try {
                                dataList = this.imagehelper.getImageFiles(this.myProzess, this.currentTifFolder);
                                if (dataList == null) {
                                    return;
                                }
                            } catch (InvalidImagesException e1) {
                                logger.trace("dataList error");
                                logger.error("Images could not be read", e1);
                                Helper.setFehlerMeldung("images could not be read", e1);
                            }
                        }
                        /* das aktuelle tif erfassen */
                        if (dataList.size() > pos) {
                            this.myBild = dataList.get(pos);
                        } else {
                            this.myBild = dataList.get(dataList.size() - 1);
                        }
                        logger.trace("found myBild");
                        /* die korrekte Seitenzahl anzeigen */
                        this.myBildNummer = pos + 1;
                        if(logger.isTraceEnabled()){
                            logger.trace("myBildNummer: " + this.myBildNummer);
                        }
                        /* Pages-Verzeichnis ermitteln */
                        String myPfad = ConfigMain.getTempImagesPathAsCompleteDirectory();
                        if(logger.isTraceEnabled()){
                            logger.trace("myPfad: " + myPfad);
                        }
                        /*
                         * den Counter für die Bild-ID auf einen neuen Wert setzen, damit nichts gecacht wird
                         */
                        this.myBildCounter++;
                        if(logger.isTraceEnabled()){
                            logger.trace("myBildCounter: " + this.myBildCounter);
                        }

                        /* Session ermitteln */
                        FacesContext context = FacesContext.getCurrentInstance();
                        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                        String mySession = session.getId() + "_" + this.myBildCounter + ".png";
                        logger.trace("facescontext");

                        /* das neue Bild zuweisen */
                        try {
                            String tiffconverterpfad = this.myProzess.getImagesDirectory() + this.currentTifFolder + File.separator + this.myBild;
                            if(logger.isTraceEnabled()){
                                logger.trace("tiffconverterpfad: " + tiffconverterpfad);
                            }
                            if (!new SafeFile(tiffconverterpfad).exists()) {
                                tiffconverterpfad = this.myProzess.getImagesTifDirectory(true) + this.myBild;
                                Helper.setFehlerMeldung("formularOrdner:TifFolders", "", "image " + this.myBild + " does not exist in folder "
                                        + this.currentTifFolder + ", using image from " + new SafeFile(this.myProzess.getImagesTifDirectory(true)).getName());
                            }
                            this.imagehelper.scaleFile(tiffconverterpfad, myPfad + mySession, this.myBildGroesse, this.myImageRotation);
                            logger.trace("scaleFile");
                        } catch (Exception e) {
                            Helper.setFehlerMeldung("could not find image folder", e);
                            logger.error(e);
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
            if (this.currentTifFolder != null && this.myBild != null) {
                exists = (new SafeFile(this.myProzess.getImagesDirectory() + this.currentTifFolder + File.separator + this.myBild)).exists();
            }
        } catch (Exception e) {
            this.myBildNummer = -1;
            logger.error(e);
        }
        /* wenn das Bild nicht existiert, den Status ändern */
        if (!exists) {
            this.myBildNummer = -1;
        }
    }

    /**
     * Checks for meta-data lock, and, on success, updates the lock time.
     *
     * @return true, if the user holds a lock on this meta-data record
     */
    private boolean SperrungAktualisieren() {
        Integer processId;
        if (this.sperrung == null || this.myProzess == null || (processId = this.myProzess.getId()) == null) {
            return false;
        }
        if (MetadatenSperrung.isLocked(processId)) {
            String userHoldingLock = this.sperrung.getLockBenutzer(processId);
            if (userHoldingLock != null && userHoldingLock.equals(this.myBenutzerID)) {
                this.sperrung.setLocked(processId, this.myBenutzerID);
                return true;
            }
        }
        return false;
    }

    /**
     * Release meta-data lock, if any.
     */
    private void SperrungAufheben() {
        Integer processId;
        if (this.sperrung == null || this.myProzess == null || (processId = this.myProzess.getId()) == null) {
            return;
        }
        if (MetadatenSperrung.isLocked(processId)) {
            String userHoldingLock = this.sperrung.getLockBenutzer(processId);
            if (userHoldingLock != null && userHoldingLock.equals(this.myBenutzerID)) {
                this.sperrung.setFree(processId);
            }
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
        return this.zurueck;
    }

    /*
     * ##################################################### ##################################################### ## ## Transliteration bestimmter
     * Felder ## ##################################################### ####################################################
     */

    public String Transliterieren() {
        Metadata md = this.curMetadatum.getMd();

        /*
         * -------------------------------- wenn es ein russischer Titel ist, dessen Transliterierungen anzeigen --------------------------------
         */
        if (md.getType().getName().equals("RUSMainTitle")) {
            Transliteration trans = new Transliteration();

            try {
                MetadataType mdt = this.myPrefs.getMetadataTypeByName("MainTitleTransliterated");
                Metadata mdDin = new Metadata(mdt);
                Metadata mdIso = new Metadata(mdt);
                mdDin.setValue(trans.transliterate_din(md.getValue()));
                mdIso.setValue(trans.transliterate_iso(md.getValue()));

                this.myDocStruct.addMetadata(mdDin);
                this.myDocStruct.addMetadata(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    public String TransliterierenPerson() {
        Person md = this.curPerson.getP();

        /*
         * -------------------------------- wenn es ein russischer Autor ist, dessen Transliterierungen anlegen --------------------------------
         */
        if (md.getRole().equals("Author")) {
            Transliteration trans = new Transliteration();
            try {
                MetadataType mdtDin = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedDIN");
                MetadataType mdtIso = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedISO");
                Person mdDin = new Person(mdtDin);
                Person mdIso = new Person(mdtIso);

                mdDin.setFirstname(trans.transliterate_din(md.getFirstname()));
                mdDin.setLastname(trans.transliterate_din(md.getLastname()));
                mdIso.setFirstname(trans.transliterate_iso(md.getFirstname()));
                mdIso.setLastname(trans.transliterate_iso(md.getLastname()));
                mdDin.setRole("AuthorTransliteratedDIN");
                mdIso.setRole("AuthorTransliteratedISO");

                this.myDocStruct.addPerson(mdDin);
                this.myDocStruct.addPerson(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
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
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                Fileformat addrdf = CataloguePlugin.getFirstHit(opacKatalog,
                        QueryBuilder.restrictToField(opacSuchfeld, tok), myPrefs);
                if (addrdf != null) {
                    this.myDocStruct.addChild(addrdf.getDigitalDocument().getLogicalDocStruct());
                    MetadatenalsTree3Einlesen1();
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
            }
        }
        return "Metadaten3links";
    }

    /**
     * eine PPN aus dem Opac abfragen und dessen Metadaten dem aktuellen Strukturelement zuweisen
     * ================================================================
     */
    public String AddMetadaFromOpacPpn() {
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                Fileformat addrdf = CataloguePlugin.getFirstHit(opacKatalog,
                        QueryBuilder.restrictToField(opacSuchfeld, tok), myPrefs);
                if (addrdf != null) {

                    /* die Liste aller erlaubten Metadatenelemente erstellen */
                    List<String> erlaubte = new ArrayList<String>();
                    for (Iterator<MetadataType> it = this.myDocStruct.getAddableMetadataTypes().iterator(); it.hasNext();) {
                        MetadataType mt = it.next();
                        erlaubte.add(mt.getName());
                    }

                    /*
                     * wenn der Metadatentyp in der Liste der erlaubten Typen, dann hinzufügen
                     */
                    for (Iterator<Metadata> it = addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadata().iterator(); it.hasNext();) {
                        Metadata m = it.next();
                        if (erlaubte.contains(m.getType().getName())) {
                            this.myDocStruct.addMetadata(m);
                        }
                    }

                    for (Iterator<Person> it = addrdf.getDigitalDocument().getLogicalDocStruct().getAllPersons().iterator(); it.hasNext();) {
                        Person m = it.next();
                        if (erlaubte.contains(m.getType().getName())) {
                            this.myDocStruct.addPerson(m);
                        }
                    }

                    MetadatenalsTree3Einlesen1();
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {

            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        this.modusAnsicht = "Metadaten";
        return "";
    }

    /*
     * ##################################################### ##################################################### ## ## Metadatenvalidierung ##
     * ##################################################### ####################################################
     */

    public void Validate() {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        mv.validate(this.gdzfile, this.myPrefs, this.myProzess);
        MetadatenalsBeanSpeichern(this.myDocStruct);
    }

    /*
     * ##################################################### ##################################################### ## ## Auswahl der Seiten über Ajax
     * ## ##################################################### ####################################################
     */

    public String getAjaxSeiteStart() {
        return this.ajaxSeiteStart;
    }

    public void setAjaxSeiteStart(String ajaxSeiteStart) {
        this.ajaxSeiteStart = ajaxSeiteStart;
    }

    public String getAjaxSeiteEnde() {
        return this.ajaxSeiteEnde;
    }

    public void setAjaxSeiteEnde(String ajaxSeiteEnde) {
        this.ajaxSeiteEnde = ajaxSeiteEnde;
    }

    public String getPagesEnd() {
        return this.pagesEnd;
    }

    public String getPagesStart() {
        return this.pagesStart;
    }

    public void setPagesEnd(String pagesEnd) {
        this.pagesEnd = pagesEnd;
    }

    public void setPagesStart(String pagesStart) {
        this.pagesStart = pagesStart;
    }

    public void CurrentStartpage() {
        for (int i = 0; i < this.alleSeiten.length; i++) {
            SelectItem si = this.alleSeiten[i];
            if (si.getValue().equals(String.valueOf(this.pageNumber))) {
                this.pagesStart = si.getLabel();
            }
        }
    }

    public void CurrentEndpage() {
        for (int i = 0; i < this.alleSeiten.length; i++) {
            SelectItem si = this.alleSeiten[i];
            if (si.getValue().equals(String.valueOf(this.pageNumber))) {
                this.pagesEnd = si.getLabel();
            }
        }
    }

    private int pageNumber = 0;

    private SelectOne<Separator> paginierungSeparators = new SelectOne<Separator>(Separator.factory(
            ConfigMain.getParameter(Parameters.PAGE_SEPARATORS, "\" \"")));

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber - 1;

    }

    public List<String> getAjaxAlleSeiten(String prefix) {
        logger.debug("Ajax-Liste abgefragt");
        List<String> li = new ArrayList<String>();
        if (this.alleSeiten != null && this.alleSeiten.length > 0) {
            for (int i = 0; i < this.alleSeiten.length; i++) {
                SelectItem si = this.alleSeiten[i];
                if (si.getLabel().contains(prefix)) {
                    li.add(si.getLabel());
                }
            }
        }
        return li;
    }

    /**
     * die Seiten über die Ajax-Felder festlegen ================================================================
     */
    public void AjaxSeitenStartUndEndeSetzen() {
        if (this.alleSeiten == null) {
            return;
        }

        boolean startseiteOk = false;
        boolean endseiteOk = false;

        /*
         * alle Seiten durchlaufen und prüfen, ob die eingestellte Seite überhaupt existiert
         */
        for (int i = 0; i < this.alleSeiten.length; i++) {
            SelectItem si = this.alleSeiten[i];
            if (si.getLabel().equals(this.ajaxSeiteStart)) {
                startseiteOk = true;
                this.alleSeitenAuswahl_ersteSeite = (String) si.getValue();
            }
            if (si.getLabel().equals(this.ajaxSeiteEnde)) {
                endseiteOk = true;
                this.alleSeitenAuswahl_letzteSeite = (String) si.getValue();
            }
        }

        /* wenn die Seiten ok sind */
        if (startseiteOk && endseiteOk) {
            SeitenStartUndEndeSetzen();
        } else {
            Helper.setFehlerMeldung("Selected image(s) unavailable");
        }
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String SeitenStartUndEndeSetzen() {
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        int anzahlAuswahl = Integer.parseInt(this.alleSeitenAuswahl_letzteSeite) - Integer.parseInt(this.alleSeitenAuswahl_ersteSeite) + 1;
        if (anzahlAuswahl > 0) {
            /* alle bisher zugewiesenen Seiten entfernen */
            this.myDocStruct.getAllToReferences().clear();
            int zaehler = 0;
            while (zaehler < anzahlAuswahl) {
                this.myDocStruct.addReferenceTo(this.alleSeitenNeu[Integer.parseInt(this.alleSeitenAuswahl_ersteSeite) + zaehler].getMd()
                        .getDocStruct(), "logical_physical");
                zaehler++;
            }
        }
        StructSeitenErmitteln(this.myDocStruct);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */

    public String SeitenVonChildrenUebernehmen() {
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }

        /* alle Kinder des aktuellen DocStructs durchlaufen */
        this.myDocStruct.getAllReferences("to").removeAll(this.myDocStruct.getAllReferences("to"));
        if (this.myDocStruct.getAllChildren() != null) {
            for (Iterator<DocStruct> iter = this.myDocStruct.getAllChildren().iterator(); iter.hasNext();) {
                DocStruct child = iter.next();
                List<Reference> childRefs = child.getAllReferences("to");
                for (Reference toAdd : childRefs) {
                    boolean match = false;
                    for (Reference ref : this.myDocStruct.getAllReferences("to")) {
                        if (ref.getTarget().equals(toAdd.getTarget())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        this.myDocStruct.getAllReferences("to").add(toAdd);
                    }

                }
            }
        }
        StructSeitenErmitteln(this.myDocStruct);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String BildErsteSeiteAnzeigen() {
        myBild = null;
        BildErmitteln(0);
        return "";
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String BildLetzteSeiteAnzeigen() {
        this.bildAnzeigen = true;
        if (this.treeProperties.get("showpagesasajax")) {
            for (int i = 0; i < this.alleSeiten.length; i++) {
                SelectItem si = this.alleSeiten[i];
                if (si.getLabel().equals(this.ajaxSeiteEnde)) {
                    this.alleSeitenAuswahl_letzteSeite = (String) si.getValue();
                    break;
                }
            }
        }
        try {
            int pageNumber = Integer.parseInt(this.alleSeitenAuswahl_letzteSeite) - this.myBildNummer + 1;
            BildErmitteln(pageNumber);
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * ausgewählte Seiten dem aktuellen Strukturelement hinzufügen ================================================================
     */
    public String SeitenHinzu() {
        /* alle markierten Seiten durchlaufen */
        for (int i = 0; i < this.alleSeitenAuswahl.length; i++) {
            int aktuelleID = Integer.parseInt(this.alleSeitenAuswahl[i]);

            boolean schonEnthalten = false;

            /*
             * wenn schon References vorhanden, prüfen, ob schon enthalten, erst dann zuweisen
             */
            if (this.myDocStruct.getAllToReferences("logical_physical") != null) {
                for (Iterator<Reference> iter = this.myDocStruct.getAllToReferences("logical_physical").iterator(); iter.hasNext();) {
                    Reference obj = iter.next();
                    if (obj.getTarget() == this.alleSeitenNeu[aktuelleID].getMd().getDocStruct()) {
                        schonEnthalten = true;
                        break;
                    }
                }
            }

            if (!schonEnthalten) {
                this.myDocStruct.addReferenceTo(this.alleSeitenNeu[aktuelleID].getMd().getDocStruct(), "logical_physical");
            }
        }
        StructSeitenErmitteln(this.myDocStruct);
        this.alleSeitenAuswahl = null;
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
        return null;
    }

    /**
     * ausgewählte Seiten aus dem Strukturelement entfernen ================================================================
     */
    public String SeitenWeg() {
        for (int i = 0; i < this.structSeitenAuswahl.length; i++) {
            int aktuelleID = Integer.parseInt(this.structSeitenAuswahl[i]);
            this.myDocStruct.removeReferenceTo(this.structSeitenNeu[aktuelleID].getMd().getDocStruct());
        }
        StructSeitenErmitteln(this.myDocStruct);
        this.structSeitenAuswahl = null;
        if (!SperrungAktualisieren()) {
            return "SperrungAbgelaufen";
        }
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
        String myOcrUrl = getOcrBasisUrl(this.myBildNummer);
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(myOcrUrl);
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                this.ocrResult = "HttpStatus nicht ok";
                return;
            }
            this.ocrResult = method.getResponseBodyAsString();
        } catch (HttpException e) {
            this.ocrResult = "Fatal protocol violation: " + e.getMessage();
        } catch (IOException e) {
            this.ocrResult = "Fatal transport error: " + e.getMessage();
        } finally {
            method.releaseConnection();
        }
    }

    public String getOcrResult() {
        return this.ocrResult;
    }

    public String getOcrAcdress() {
        int startseite = -1;
        int endseite = -1;
        if (this.structSeiten != null) {
            for (int i = 0; i < this.structSeiten.length; i++) {
                SelectItem si = this.structSeiten[i];
                int temp = Integer.parseInt(si.getLabel().substring(0, si.getLabel().indexOf(":")));
                if (startseite == -1 || startseite > temp) {
                    startseite = temp;
                }
                if (endseite == -1 || endseite < temp) {
                    endseite = temp;
                }
            }
        }
        return getOcrBasisUrl(startseite, endseite);
    }

    private String getOcrBasisUrl(int... seiten) {
        String url = ConfigMain.getParameter("ocrUrl");
        VariableReplacer replacer = new VariableReplacer(this.mydocument, this.myPrefs, this.myProzess, null);
        url = replacer.replace(url);
        url += "/&imgrange=" + seiten[0];
        if (seiten.length > 1) {
            url += "-" + seiten[1];
        }
        return url;
    }

    /*
     * ##################################################### ##################################################### ## ## Getter und Setter ##
     * ##################################################### ####################################################
     */

    public int getBildNummer() {
        return this.myBildNummer;
    }

    public void setBildNummer(int inBild) {
    }

    public int getBildLetztes() {
        return this.myBildLetztes;
    }

    public int getBildGroesse() {
        return this.myBildGroesse;
    }

    public void setBildGroesse(int myBildGroesse) {
        this.myBildGroesse = myBildGroesse;
    }

    public String getTempTyp() {
        if (this.selectedMetadatum == null) {
            getAddableMetadataTypes();
            this.selectedMetadatum = this.tempMetadatumList.get(0);
        }
        return this.selectedMetadatum.getMd().getType().getName();
    }

    public MetadatumImpl getSelectedMetadatum() {
        return this.selectedMetadatum;
    }

    public void setSelectedMetadatum(MetadatumImpl newMeta) {
        this.selectedMetadatum = newMeta;
    }

    public void setTempTyp(String tempTyp) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(tempTyp);
        try {
            Metadata md = new Metadata(mdt);
            this.selectedMetadatum = new MetadatumImpl(md, this.myMetadaten.size() + 1, this.myPrefs, this.myProzess);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e.getMessage());
        }
        this.tempTyp = tempTyp;
    }

    public MetadatumImpl getMetadatum() {

        if (this.selectedMetadatum == null) {
            getAddableMetadataTypes();
            this.selectedMetadatum = this.tempMetadatumList.get(0);
        }
        return this.selectedMetadatum;
    }

    public void setMetadatum(MetadatumImpl meta) {
        this.selectedMetadatum = meta;
    }

    public String getOutputType() {
        return this.selectedMetadatum.getOutputType();
    }

    public String getTempWert() {
        return this.tempWert;
    }

    public void setTempWert(String tempWert) {
        this.tempWert = tempWert;
    }

    public boolean isModusHinzufuegen() {
        return this.modusHinzufuegen;
    }

    public void setModusHinzufuegen(boolean modusHinzufuegen) {
        this.modusHinzufuegen = modusHinzufuegen;
    }

    public boolean isModusHinzufuegenPerson() {
        return this.modusHinzufuegenPerson;
    }

    public void setModusHinzufuegenPerson(boolean modusHinzufuegenPerson) {
        this.modusHinzufuegenPerson = modusHinzufuegenPerson;
    }

    public String getTempPersonNachname() {
        return this.tempPersonNachname;
    }

    public void setTempPersonNachname(String tempPersonNachname) {
        this.tempPersonNachname = tempPersonNachname;
    }

    public String getTempPersonRecord() {
        return tempPersonRecord;
    }

    public void setTempPersonRecord(String tempPersonRecord) {
        this.tempPersonRecord = tempPersonRecord;
    }

    public String getTempPersonRolle() {
        return this.tempPersonRolle;
    }

    public void setTempPersonRolle(String tempPersonRolle) {
        this.tempPersonRolle = tempPersonRolle;
    }

    public String getTempPersonVorname() {
        return this.tempPersonVorname;
    }

    public void setTempPersonVorname(String tempPersonVorname) {
        this.tempPersonVorname = tempPersonVorname;
    }

    public String[] getAlleSeitenAuswahl() {
        return this.alleSeitenAuswahl;
    }

    public void setAlleSeitenAuswahl(String[] alleSeitenAuswahl) {
        this.alleSeitenAuswahl = alleSeitenAuswahl;
    }

    public SelectItem[] getAlleSeiten() {
        return this.alleSeiten;
    }

    public SelectItem[] getStructSeiten() {
        if (this.structSeiten.length > 0 && this.structSeiten[0] == null) {
            return new SelectItem[0];
        } else {
            return this.structSeiten;
        }
    }

    public String[] getStructSeitenAuswahl() {
        return this.structSeitenAuswahl;
    }

    public void setStructSeitenAuswahl(String[] structSeitenAuswahl) {
        this.structSeitenAuswahl = structSeitenAuswahl;
    }

    public Prozess getMyProzess() {
        return this.myProzess;
    }

    public String getModusAnsicht() {
        return this.modusAnsicht;
    }

    public void setModusAnsicht(String modusAnsicht) {
        this.modusAnsicht = modusAnsicht;
    }

    public String getPaginierungWert() {
        return this.paginierungWert;
    }

    public void setPaginierungWert(String paginierungWert) {
        this.paginierungWert = paginierungWert;
    }

    public int getPaginierungAbSeiteOderMarkierung() {
        return this.paginierungAbSeiteOderMarkierung;
    }

    public void setPaginierungAbSeiteOderMarkierung(int paginierungAbSeiteOderMarkierung) {
        this.paginierungAbSeiteOderMarkierung = paginierungAbSeiteOderMarkierung;
    }

    public String getPaginierungArt() {
        return this.paginierungArt;
    }

    public void setPaginierungArt(String paginierungArt) {
        this.paginierungArt = paginierungArt;
    }

    public boolean isBildAnzeigen() {
        return this.bildAnzeigen;
    }

    public void BildAnzeigen() {
        this.bildAnzeigen = !this.bildAnzeigen;
        if (this.bildAnzeigen) {
            try {
                BildErmitteln(0);
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while generating image", e.getMessage());
                logger.error(e);
            }
        }
    }

    public String getAddDocStructType1() {
        return this.addDocStructType1;
    }

    public void setAddDocStructType1(String addDocStructType1) {
        this.addDocStructType1 = addDocStructType1;
    }

    public String getAddDocStructType2() {
        return this.addDocStructType2;
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
        return this.nurLesenModus;
    }

    public void setNurLesenModus(boolean nurLesenModus) {
        this.nurLesenModus = nurLesenModus;
    }

    public boolean isBildZuStrukturelement() {
        return this.bildZuStrukturelement;
    }

    public void setBildZuStrukturelement(boolean bildZuStrukturelement) {
        this.bildZuStrukturelement = bildZuStrukturelement;
    }

    public String getNeuesElementWohin() {
        if (this.neuesElementWohin == null || this.neuesElementWohin.isEmpty()) {
            this.neuesElementWohin = "1";
        }
        return this.neuesElementWohin;
    }

    public void setNeuesElementWohin(String inNeuesElementWohin) {
        if (inNeuesElementWohin == null || inNeuesElementWohin.equals("")) {
            this.neuesElementWohin = "1";
        } else {
            this.neuesElementWohin = inNeuesElementWohin;
        }
    }

    public String getAlleSeitenAuswahl_ersteSeite() {
        return this.alleSeitenAuswahl_ersteSeite;
    }

    public void setAlleSeitenAuswahl_ersteSeite(String alleSeitenAuswahl_ersteSeite) {
        this.alleSeitenAuswahl_ersteSeite = alleSeitenAuswahl_ersteSeite;
    }

    public String getAlleSeitenAuswahl_letzteSeite() {
        return this.alleSeitenAuswahl_letzteSeite;
    }

    public void setAlleSeitenAuswahl_letzteSeite(String alleSeitenAuswahl_letzteSeite) {
        this.alleSeitenAuswahl_letzteSeite = alleSeitenAuswahl_letzteSeite;
    }

    public List<HashMap<String, Object>> getStrukturBaum3() {
        if (this.tree3 != null) {
            return this.tree3.getChildrenAsList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<HashMap<String, Object>> getStrukturBaum3Alle() {
        if (this.tree3 != null) {
            return this.tree3.getChildrenAsListAlle();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isModusStrukturelementVerschieben() {
        return this.modusStrukturelementVerschieben;
    }

    public void setModusStrukturelementVerschieben(boolean modusStrukturelementVerschieben) {
        this.modusStrukturelementVerschieben = modusStrukturelementVerschieben;

        // wenn der Verschiebevorgang gestartet werden soll, dann in allen
        // DocStructs prüfen
        // ob das aktuelle Strukturelement dort eingefügt werden darf
        if (this.modusStrukturelementVerschieben) {
            TreeDurchlaufen(this.tree3);
        }
    }

    @SuppressWarnings("rawtypes")
    private void TreeDurchlaufen(TreeNodeStruct3 inTreeStruct) {
        DocStruct temp = inTreeStruct.getStruct();
        if (inTreeStruct.getStruct() == this.myDocStruct) {
            inTreeStruct.setSelected(true);
        } else {
            inTreeStruct.setSelected(false);
        }

        // alle erlaubten Typen durchlaufen
        for (Iterator<String> iter = temp.getType().getAllAllowedDocStructTypes().iterator(); iter.hasNext();) {
            String dst = iter.next();
            if (this.myDocStruct.getType().getName().equals(dst)) {
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

    public List<MetadatumImpl> getMyMetadaten() {
        return this.myMetadaten;
    }

    public void setMyMetadaten(List<MetadatumImpl> myMetadaten) {
        this.myMetadaten = myMetadaten;
    }

    public List<MetaPerson> getMyPersonen() {
        return this.myPersonen;
    }

    public void setMyPersonen(List<MetaPerson> myPersonen) {
        this.myPersonen = myPersonen;
    }

    public MetadatumImpl getCurMetadatum() {
        return this.curMetadatum;
    }

    public void setCurMetadatum(MetadatumImpl curMetadatum) {
        this.curMetadatum = curMetadatum;
    }

    public MetaPerson getCurPerson() {
        return this.curPerson;
    }

    public void setCurPerson(MetaPerson curPerson) {
        this.curPerson = curPerson;
    }

    public String getAdditionalOpacPpns() {
        return this.additionalOpacPpns;
    }

    public void setAdditionalOpacPpns(String additionalOpacPpns) {
        this.additionalOpacPpns = additionalOpacPpns;
    }

    public boolean isTreeReloaden() {
        return this.treeReloaden;
    }

    public void setTreeReloaden(boolean treeReloaden) {
        this.treeReloaden = treeReloaden;
    }

    public HashMap<String, Boolean> getTreeProperties() {
        return this.treeProperties;
    }

    public void setTreeProperties(HashMap<String, Boolean> treeProperties) {
        this.treeProperties = treeProperties;
    }

    public String getOpacKatalog() {
        return this.opacKatalog;
    }

    public void setOpacKatalog(String opacKatalog) {
        this.opacKatalog = opacKatalog;
    }

    public String getOpacSuchfeld() {
        return this.opacSuchfeld;
    }

    public void setOpacSuchfeld(String opacSuchfeld) {
        this.opacSuchfeld = opacSuchfeld;
    }

    public int getPaginierungSeitenProImage() {
        return this.paginierungSeitenProImage;
    }

    public void setPaginierungSeitenProImage(int paginierungSeitenProImage) {
        this.paginierungSeitenProImage = paginierungSeitenProImage;
    }

    public String getCurrentTifFolder() {
        return this.currentTifFolder;
    }

    public void setCurrentTifFolder(String currentTifFolder) {
        this.currentTifFolder = currentTifFolder;
    }

    public List<String> autocomplete(Object suggest) {
        String pref = (String) suggest;
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> alle = new ArrayList<String>();
        for (SelectItem si : this.alleSeiten) {
            alle.add(si.getLabel());
        }

        Iterator<String> iterator = alle.iterator();
        while (iterator.hasNext()) {
            String elem = iterator.next();
            if (elem != null && elem.contains(pref) || "".equals(pref)) {
                result.add(elem);
            }
        }
        return result;
    }

    public boolean getIsNotRootElement() {
        if (this.myDocStruct != null) {
            if (this.myDocStruct.getParent() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean getFictitious() {
        return fictitious;
    }

    public void setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
    }

    public String getCurrentRepresentativePage() {
        return currentRepresentativePage;
    }

    public void setCurrentRepresentativePage(String currentRepresentativePage) {
        this.currentRepresentativePage = currentRepresentativePage;
    }

    private void switchFileNames(DocStruct firstpage, DocStruct secondpage) {
        String firstFile = firstpage.getImageName();
        String otherFile = secondpage.getImageName();

        try {
            File first = new File(myProzess.getImagesTifDirectory(true), otherFile);
            File other = new File(myProzess.getImagesTifDirectory(true), firstFile);
            firstpage.setImageName(first.toURI().toString());
            secondpage.setImageName(other.toURI().toString());
        } catch (InterruptedException | SwapException | DAOException | IOException e) {
            logger.error("Could not determinate image directory!", e);
        }
    }

    public void moveSeltectedPagesUp() {
        List<Integer> selectedPages = new ArrayList<Integer>();
        List<DocStruct> allPages = mydocument.getPhysicalDocStruct().getAllChildren();
        List<String> pageNoList = Arrays.asList(alleSeitenAuswahl);
        for (String order : pageNoList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            if (currentPhysicalPageNo == 0) {
                break;
            }
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        List<String> newSelectionList = new ArrayList<String>();
        for (Integer pageIndex : selectedPages) {
            DocStruct firstpage = allPages.get(pageIndex - 1);
            DocStruct secondpage = allPages.get(pageIndex);
            switchFileNames(firstpage, secondpage);
            newSelectionList.add(String.valueOf(pageIndex - 1));
        }

        alleSeitenAuswahl = newSelectionList.toArray(new String[newSelectionList.size()]);

        retrieveAllImages();
        BildErmitteln(0);
    }

    public void moveSeltectedPagesDown() {
        List<Integer> selectedPages = new ArrayList<Integer>();
        List<DocStruct> allPages = mydocument.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = Arrays.asList(alleSeitenAuswahl);
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            if (currentPhysicalPageNo + 1 == alleSeiten.length) {
                break;
            }
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        List<String> newSelectionList = new ArrayList<String>();
        for (Integer pageIndex : selectedPages) {
            DocStruct firstpage = allPages.get(pageIndex + 1);
            DocStruct secondpage = allPages.get(pageIndex);
            switchFileNames(firstpage, secondpage);
            newSelectionList.add(String.valueOf(pageIndex + 1));
        }

        alleSeitenAuswahl = newSelectionList.toArray(new String[newSelectionList.size()]);
        retrieveAllImages();
        BildErmitteln(0);
    }

    public void deleteSeltectedPages() {
        List<Integer> selectedPages = new ArrayList<Integer>();
        List<DocStruct> allPages = mydocument.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = Arrays.asList(alleSeitenAuswahl);
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }

        for (Integer pageIndex : selectedPages) {

            DocStruct pageToRemove = allPages.get(pageIndex);
            String imagename = pageToRemove.getImageName();

            removeImage(imagename);
            mydocument.getFileSet().removeFile(pageToRemove.getAllContentFiles().get(0));

            mydocument.getPhysicalDocStruct().removeChild(pageToRemove);
            List<Reference> refs = new ArrayList<Reference>(pageToRemove.getAllFromReferences());
            for (ugh.dl.Reference ref : refs) {
                ref.getSource().removeReferenceTo(pageToRemove);
            }

        }

        alleSeitenAuswahl = null;
        if (mydocument.getPhysicalDocStruct().getAllChildren() != null) {
            myBildLetztes = mydocument.getPhysicalDocStruct().getAllChildren().size();
        } else {
            myBildLetztes = 0;
        }

        allPages = mydocument.getPhysicalDocStruct().getAllChildren();

        int currentPhysicalOrder = 1;
        if (allPages != null) {
            MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            for (DocStruct page : allPages) {
                List<? extends Metadata> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.size() == 0) {
                    currentPhysicalOrder++;
                    break;
                }
                for (Metadata pageNo : pageNoMetadata) {
                    pageNo.setValue(String.valueOf(currentPhysicalOrder));
                }
                currentPhysicalOrder++;
            }
        }
        retrieveAllImages();

        // current image was deleted, load first image
        if (selectedPages.contains(myBildNummer - 1)) {

            BildErsteSeiteAnzeigen();
        } else {
            BildErmitteln(0);
        }
    }

    public void reOrderPagination() {
        String imageDirectory = "";
        try {
            imageDirectory = myProzess.getImagesDirectory();
        } catch (SwapException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);

        }
        if (imageDirectory.equals("")) {
            Helper.setFehlerMeldung("ErrorMetsEditorImageRenaming");
            return;
        }

        List<String> oldfilenames = new ArrayList<String>();
        for (DocStruct page : mydocument.getPhysicalDocStruct().getAllChildren()) {
            oldfilenames.add(page.getImageName());
        }

        for (String imagename : oldfilenames) {
            for (String folder : allTifFolders) {
                SafeFile filename = new SafeFile(imageDirectory + folder, imagename);
                SafeFile newFileName = new SafeFile(imageDirectory + folder, imagename + "_bak");
                filename.renameTo(newFileName);
            }

            try {
                SafeFile ocr = new SafeFile(myProzess.getOcrDirectory());
                if (ocr.exists()) {
                    SafeFile[] allOcrFolder = ocr.listFiles();
                    for (SafeFile folder : allOcrFolder) {
                        SafeFile filename = new SafeFile(folder, imagename);
                        SafeFile newFileName = new SafeFile(folder, imagename + "_bak");
                        filename.renameTo(newFileName);
                    }
                }
            } catch (SwapException e) {
                logger.error(e);
            } catch (DAOException e) {
                logger.error(e);
            } catch (IOException e) {
                logger.error(e);
            } catch (InterruptedException e) {
                logger.error(e);
            }

        }
        int counter = 1;
        for (String imagename : oldfilenames) {
            String newfilenamePrefix = generateFileName(counter);
            for (String folder : allTifFolders) {
                SafeFile fileToSort = new SafeFile(imageDirectory + folder, imagename);
                String fileExtension = Metadaten.getFileExtension(fileToSort.getName().replace("_bak", ""));
                SafeFile tempFileName = new SafeFile(imageDirectory + folder, fileToSort.getName() + "_bak");
                SafeFile sortedName = new SafeFile(imageDirectory + folder, newfilenamePrefix + fileExtension.toLowerCase());
                tempFileName.renameTo(sortedName);
                mydocument.getPhysicalDocStruct().getAllChildren().get(counter - 1).setImageName(sortedName.toURI().toString());
            }
            try {
                SafeFile ocr = new SafeFile(myProzess.getOcrDirectory());
                if (ocr.exists()) {
                    SafeFile[] allOcrFolder = ocr.listFiles();
                    for (SafeFile folder : allOcrFolder) {
                        SafeFile fileToSort = new SafeFile(folder, imagename);
                        String fileExtension = Metadaten.getFileExtension(fileToSort.getName().replace("_bak", ""));
                        SafeFile tempFileName = new SafeFile(folder, fileToSort.getName() + "_bak");
                        SafeFile sortedName = new SafeFile(folder, newfilenamePrefix + fileExtension.toLowerCase());
                        tempFileName.renameTo(sortedName);
                    }
                }
            } catch (SwapException e) {
                logger.error(e);
            } catch (DAOException e) {
                logger.error(e);
            } catch (IOException e) {
                logger.error(e);
            } catch (InterruptedException e) {
                logger.error(e);
            }
            counter++;
        }
        retrieveAllImages();

        BildErmitteln(0);
    }

    private void removeImage(String fileToDelete) {
        try {
            // TODO check what happens with .tar.gz
            String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf("."));
            for (String folder : allTifFolders) {
                SafeFile[] filesInFolder = new SafeFile(myProzess.getImagesDirectory() + folder).listFiles();
                for (SafeFile currentFile : filesInFolder) {
                    String filename = currentFile.getName();
                    String filenamePrefix = filename.replace(getFileExtension(filename), "");
                    if (filenamePrefix.equals(fileToDeletePrefix)) {
                        currentFile.delete();
                    }
                }
            }

            SafeFile ocr = new SafeFile(myProzess.getOcrDirectory());
            if (ocr.exists()) {
                SafeFile[] folder = ocr.listFiles();
                for (SafeFile dir : folder) {
                    if (dir.isDirectory() && dir.list().length > 0) {
                        SafeFile[] filesInFolder = dir.listFiles();
                        for (SafeFile currentFile : filesInFolder) {
                            String filename = currentFile.getName();
                            String filenamePrefix = filename.substring(0, filename.lastIndexOf("."));
                            if (filenamePrefix.equals(fileToDeletePrefix)) {
                                currentFile.delete();
                            }
                        }
                    }
                }
            }
        } catch (SwapException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }

    }

    private static String generateFileName(int counter) {
        String filename = "";
        if (counter >= 10000000) {
            filename = "" + counter;
        } else if (counter >= 1000000) {
            filename = "0" + counter;
        } else if (counter >= 100000) {
            filename = "00" + counter;
        } else if (counter >= 10000) {
            filename = "000" + counter;
        } else if (counter >= 1000) {
            filename = "0000" + counter;
        } else if (counter >= 100) {
            filename = "00000" + counter;
        } else if (counter >= 10) {
            filename = "000000" + counter;
        } else {
            filename = "0000000" + counter;
        }
        return filename;
    }

    public FileManipulation getFileManipulation() {
        if (fileManipulation == null) {
            fileManipulation = new FileManipulation(this);
        }
        return fileManipulation;
    }

    public void setFileManipulation(FileManipulation fileManipulation) {
        this.fileManipulation = fileManipulation;
    }

    public DigitalDocument getDocument() {
        return mydocument;
    }

    public void setDocument(DigitalDocument document) {
        this.mydocument = document;
    }

    public static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex);
    }


    public Boolean getDisplayFileManipulation() {
        return ConfigMain.getBooleanParameter("MetsEditorDisplayFileManipulation", false);
    }

    /**
     * Saves the input from the subform to create a new metadata group in the
     * currently selected docStruct and then toggles the form to show the page
     * “Metadata”.
     *
     * @return "" to indicate JSF not to navigate anywhere or
     *         "SperrungAbgelaufen" to make JSF show the message that the lock
     *         time is up and the user must leave the editor and open it anew
     */
    public String addMetadataGroup() throws DocStructHasNoTypeException {
        try {
            myDocStruct.addMetadataGroup(newMetadataGroup.toMetadataGroup());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        return showMetadata();
    }

    /**
     * Checks whether a given meta-data group type is available for adding. This
     * can be used by a RenderableMetadataGroup to find out whether it can be
     * copied or not.
     *
     * @param type
     *            meta-data group type to look for
     * @return whether the type is available to add
     */
    boolean canCreate(MetadataGroupType type) {
        List<MetadataGroupType> addableTypes = myDocStruct.getAddableMetadataGroupTypes();
        if (addableTypes == null) {
            addableTypes = Collections.emptyList();
        }
        return addableTypes.contains(type);
    }

    /**
     * Returns a list with backing beans for all metadata groups available for
     * the structural element under edit.
     *
     * @return backing beans for the metadata groups of the current element
     * @throws ConfigurationException
     *             if a single value metadata field is configured to show a
     *             multi-select input
     */
    public List<RenderableMetadataGroup> getMyGroups() throws ConfigurationException {
        List<MetadataGroup> records;
        if (myDocStruct == null || (records = myDocStruct.getAllMetadataGroups()) == null) {
            return Collections.emptyList();
        }
        List<RenderableMetadataGroup> result = new ArrayList<RenderableMetadataGroup>(records.size());
        String language = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}");
        String projectName = myProzess.getProjekt().getTitel();
        for (MetadataGroup record : records) {
            result.add(new RenderableMetadataGroup(record, this, language, projectName));
        }
        return result;
    }

    /**
     * Returns a backing bean object to display the form to create a new
     * metadata group.
     *
     * @return a bean to create a new metadata group
     */
    public RenderableMetadataGroup getNewMetadataGroup() {
        String language = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}");
        newMetadataGroup.setLanguage(language);
        return newMetadataGroup;
    }

    /**
     * Returns whether the metadata editor is showing the subpage to add a new
     * metadata group.
     *
     * @return whether the page to add a new metadata group shows
     */
    public boolean isAddMetadataGroupMode() {
        return this.addMetadataGroupMode;
    }

    /**
     * Returns whether the metadata editor is showing a link to open the subpage
     * to add a new metadata group.
     *
     * @return whether the link to add a new metadata group shows
     */
    public boolean isAddNewMetadataGroupLinkShowing() {
        return myDocStruct != null && myDocStruct.getAddableMetadataGroupTypes() != null;
    }

    /**
     * Deletes the metadata group
     *
     * @param metadataGroup
     *            metadata group to delete.
     */
    void removeMetadataGroupFromCurrentDocStruct(MetadataGroup metadataGroup) {
        myDocStruct.removeMetadataGroup(metadataGroup);
    }

    /**
     * Toggles the form to show the subpage to add a new metadata group. The
     * form is prepared with the values from the metadata group that the copy
     * mode was called from.
     *
     * @return "" to indicate JSF not to navigate anywhere or
     *         "SperrungAbgelaufen" to make JSF show the message that the lock
     *         time is up and the user must leave the editor and open it anew
     */
    String showAddMetadataGroupAsCopy(RenderableMetadataGroup master) {
        newMetadataGroup = new RenderableMetadataGroup(master, myDocStruct.getAddableMetadataGroupTypes());
        modusHinzufuegen = false;
        modusHinzufuegenPerson = false;
        addMetadataGroupMode = true;
        return !SperrungAktualisieren() ? "SperrungAbgelaufen" : "";
    }

    /**
     * Toggles the form to show the subpage to add a new metadata group.
     *
     * @return "" to indicate JSF not to navigate anywhere or
     *         "SperrungAbgelaufen" to make JSF show the message that the lock
     *         time is up and the user must leave the editor and open it anew
     */
    public String showAddNewMetadataGroup() {
        try {
            newMetadataGroup = new RenderableMetadataGroup(myDocStruct.getAddableMetadataGroupTypes(), myProzess
                    .getProjekt().getTitel());
        } catch (ConfigurationException e) {
            Helper.setFehlerMeldung("Form_configuration_mismatch", e.getMessage());
            logger.error(e.getMessage());
            return "";
        }
        modusHinzufuegen = false;
        modusHinzufuegenPerson = false;
        addMetadataGroupMode = true;
        return !SperrungAktualisieren() ? "SperrungAbgelaufen" : "";
    }

    /**
     * Leaves the subpage to add a new metadata group without saving any input
     * and toggles the form to show the page “Metadata”.
     *
     * @return "" to indicate JSF not to navigate anywhere or
     *         "SperrungAbgelaufen" to make JSF show the message that the lock
     *         time is up and the user must leave the editor and open it anew
     */
    public String showMetadata() {
        modusHinzufuegen = false;
        modusHinzufuegenPerson = false;
        addMetadataGroupMode = false;
        newMetadataGroup = null;
        return !SperrungAktualisieren() ? "SperrungAbgelaufen" : "";
    }

    /**
     * Returns the ID string of the currently selected separator.
     *
     * @return the ID of the selected separator
     */
    public String getPaginierungSeparator() {
        return paginierungSeparators.getSelected();
    }

    /**
     * Sets the currently selected separator by its ID.
     *
     * @param selected
     *            the ID of the separator to select
     */
    public void setPaginierungSeparator(String selected) {
        paginierungSeparators.setSelected(selected);
    }

    /**
     * Returns the List of separators the user can select from. Each element of
     * the list must be an object that implements the two functions
     * {@code String getId()} and {@code String getLabel()}.
     *
     * @return the List of separators
     */
    public Collection<Separator> getPaginierungSeparators() {
        return paginierungSeparators.getItems();
    }

    /**
     * Updates a meta-data group with the data retrieved from an authority
     * database.
     *
     * @param metaDataGroup
     *            meta-data group to update
     */
    public void updateMetadataGroupFromAuthorityFile(MetadataGroup metaDataGroup) {
        try {
            Parameters parameters = new Parameters(ConfigMain.toMap());
            Map<String, String> namespaces = parameters.getNamespacePrefixes(true, true);
            String recordURI = AuthorityFileUtil.getRecordURI(metaDataGroup);
            Node record = AuthorityFileUtil.downloadAuthorityRecord(recordURI);

            String debugFolderPath = ConfigMain.getParameter("debugFolder", null);
            if (debugFolderPath != null) {
                File debugFolder = new File(debugFolderPath);
                if (debugFolder.exists() && debugFolder.canWrite()) {
                    SerializationFormat.TURTLE.write(record, namespaces, new File(debugFolder, "authorityResult.ttl"));
                }
            }

            Map<String, String> mapping = parameters.getAuthorityMapping();
            for (Metadata metaDatum : metaDataGroup.getMetadataList()) {
                if (metaDatum.getType() != null) {
                    if (mapping.containsKey(metaDatum.getType().getName())) {
                        String gpath = mapping.get(metaDatum.getType().getName());
                        Result result = record.find(new GraphPath(gpath, namespaces));
                        metaDatum.setValue(result.strings(" ; ", true));
                    }
                }
            }
        } catch (IOException | JenaException | LinkedDataException | URISyntaxException e) {
            Helper.setFehlerMeldung("getNormDataRecordFailed",
                e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()
            );
        }
    }

    /**
     * Returns the configuration parameter {@code advancedPaginationEnabled}.
     * This is used for conditional JSP rendering.
     *
     * @return configuration parameter {@code advancedPaginationEnabled}
     */
    public boolean isAdvancedPaginationEnabled() {
        return ConfigMain.getBooleanParameter("advancedPaginationEnabled", true);
    }

    /**
     * Returns whether the mode to add several structural elements is enabled.
     * If so, the dialog to add structural elements is rendered differently.
     *
     * @return whether the mode to add several structural elements is enabled
     */
    public boolean isAddServeralStructuralElementsMode() {
        return addServeralStructuralElementsMode;
    }

    /**
     * Toggles the mode to add several structural elements on/off.
     *
     * @return the empty string, telling JSF to remain on that page
     */
    public String ToggleAddServeralStructuralElementsMode() {
        addServeralStructuralElementsMode = !addServeralStructuralElementsMode;
        return "";
    }

    /**
     * Returns the text to be shown in the ‘count’ text field.
     *
     * @return value for ‘count’
     */
    public String getElementsCount() {
        return Integer.toString(elementsCount);
    }

    /**
     * Sets the number of structural elements to create from the text input
     * received from the user.
     *
     * @param elementsCount
     *            text input for ‘count’
     */
    public void setElementsCount(String elementsCount) {
        if (elementsCount.isEmpty()) {
            this.elementsCount = 1;
        } else try{
            this.elementsCount = Math.abs(Integer.valueOf(elementsCount.trim()));
        }catch(NumberFormatException e){
            Helper.setFehlerMeldung("nan", e.getMessage());
            this.elementsCount = 1;
        }
    }

    /**
     * Returns the object that shall be selected in the drop-down box to select
     * a meta-data type to add.
     *
     * @return selected meta-data type to add
     */
    public Object getAddMetaDataType() {
        return addMetaDataType;
    }

    /**
     * To set the object the user selected in the drop-down box to select a
     * meta-data type to add.
     *
     * @param addMetaDataType
     *            selected meta-data type to add
     */
    public void setAddMetaDataType(Object addMetaDataType) {
        this.addMetaDataType = (String) addMetaDataType;
    }

    /**
     * Returns the text to be shown in the text field to add as meta-datum.
     *
     * @return value for meta-datum
     */
    public String getAddMetaDataValue() {
        return addMetaDataValue;
    }

    /**
     * To set the text the user entered in the text field to add as meta-datum.
     *
     * @param addMetaDataValue
     *            value for meta-datum
     */
    public void setAddMetaDataValue(String addMetaDataValue) {
        this.addMetaDataValue = addMetaDataValue;
    }

    /**
     * Returns the elements available in the drop-down box to select a meta-data
     * type to add.
     *
     * @return selected meta-data type to add
     * @throws TypeNotAllowedForParentException
     */
    public ArrayList<SelectItem> getAddableMetaDataTypes() throws TypeNotAllowedForParentException {
        ArrayList<SelectItem> result = new ArrayList<>();

        // an element to disable adding meta-data
        result.add(new SelectItem((Object) "", Helper.getTranslation("keineNutzung")));

        /* If neuesElementWohin is string "1" or "2", the available meta-data
         * elements depend on the selection in the drop-down element to add a
         * structural instance as neighbour (addDocStructType1), if it is "3" or
         * "4", they depend on the selection in the drop-down element to add a
         * structural instance as child (addDocStructType2). */

        int code = neuesElementWohin.codePointAt(0);
        String docStructType = (code & 1) != (code & 2) >>> 1 ? addDocStructType1 : addDocStructType2;

        /* To obtain the list of addable meta-data types, a new structure level
         * of the determined type, without any meta-data, is assumed. */

        DocStructType type = myPrefs.getDocStrctTypeByName(docStructType);
        if (type != null) {
            DocStruct empty = mydocument.createDocStruct(type);
            result.addAll(getAddableMetadataTypes(empty, null));
        }
        return result;
    }

    /**
     * @see @link{de.sub.goobi.forms.ProzesskopieForm.getSearchFields}
     *
     * @return A map containing the search fields for the currently selected OPAC
     */
    public HashMap<String, String> getSearchFields() {
        if (opacKatalog == null || opacKatalog.isEmpty()) {
            ProzesskopieForm pkf = new ProzesskopieForm();
            List<String> allOpacCatalogues = pkf.getAllOpacCatalogues();
            if (!allOpacCatalogues.isEmpty()) {
                opacKatalog = allOpacCatalogues.get(0);
            }
        }
        return ProzesskopieForm.getSearchFieldsForCatalogue(opacKatalog);
    }

}
