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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import de.sub.goobi.helper.Transliteration;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.api.display.Modes;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.helper.ConfigDispayRules;
import org.goobi.production.constants.Parameters;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.CataloguePlugin.QueryBuilder;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.IsDirectoryFilter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

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
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
public class Metadaten {
    private static final Logger logger = LogManager.getLogger(Metadaten.class);
    MetadatenImagesHelper imageHelper;
    MetadatenHelper metaHelper;
    private boolean treeReloaded = false;
    String ocrResult = "";
    private Fileformat gdzfile;
    private DocStruct docStruct;
    private DocStruct tempStrukturelement;
    private List<MetadatumImpl> myMetadaten = new LinkedList<>();
    private List<MetaPerson> metaPersonList = new LinkedList<>();
    private MetadatumImpl curMetadatum;
    private MetaPerson curPerson;
    private DigitalDocument digitalDocument;
    private Process process;
    private Prefs myPrefs;
    private String userId;
    private String tempTyp;
    private String tempValue;
    private String tempPersonRecord;
    private String tempPersonVorname;
    private String tempPersonNachname;
    private String tempPersonRolle;
    private URI currentTifFolder;
    private List<URI> allTifFolders;
    /* Variablen für die Zuweisung der Seiten zu Strukturelementen */
    private String allPagesSelectionFirstPage;
    private String allPagesSelectionLastPage;
    private String[] allPagesSelection;
    private String[] structSeitenAuswahl;
    private String[] allPages;
    private MetadatumImpl[] allPagesNew;
    private ArrayList<MetadatumImpl> tempMetadatumList = new ArrayList<>();
    private MetadatumImpl selectedMetadatum;
    private String currentRepresentativePage = "";

    private String paginationValue;
    private int paginationFromPageOrMark;
    private String paginationType;
    private int paginationPagesProImage = 1; // 1=normale Paginierung, 2=zwei
    // Spalten auf einem Image,
    // 3=nur jede zweite Seite hat
    // Seitennummer
    private boolean fictitious = false;

    private SelectItem[] structSeiten;
    private MetadatumImpl[] structSeitenNeu;
    private DocStruct logicalTopstruct;

    private boolean modeAdd = false;
    private boolean modeAddPerson = false;
    private boolean modeMoveStructureElement = false;
    private boolean modeOnlyRead;
    private String modeView = "Metadaten";
    private TreeNodeStruct3 treeNodeStruct;
    private URI image;

    private int imageNumber = 0;
    private int lastImage = 0;
    private int imageCounter = 0;
    private int imageSize = 30;
    private int imageRotation = 0;

    private boolean displayImage = true;
    private boolean imageToStructuralElement = false;
    private String imageNumberToGo = "";
    private String addFirstDocStructType;
    private String addSecondDocStructType;
    private String result = "Main";
    private final MetadatenSperrung sperrung = new MetadatenSperrung();
    private String neuesElementWohin = "1";
    private String additionalOpacPpns;
    private String opacSearchField = "12";
    private String opacCatalog;

    private String ajaxPageStart = "";
    private String ajaxPageEnd = "";
    private String pagesStart = "";
    private String pagesEnd = "";
    private HashMap<String, Boolean> treeProperties;
    private final ReentrantLock xmlReadingLock = new ReentrantLock();
    private FileManipulation fileManipulation = null;
    private boolean addMetadataGroupMode = false;
    private RenderableMetadataGroup newMetadataGroup;
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();
    private Paginator paginator = new Paginator();
    private TreeNode selectedTreeNode;

    /**
     * Konstruktor.
     */
    public Metadaten() {
        this.treeProperties = new HashMap<>();
        this.treeProperties.put("showtreelevel", Boolean.FALSE);
        this.treeProperties.put("showtitle", Boolean.FALSE);
        this.treeProperties.put("fullexpanded", Boolean.TRUE);
        this.treeProperties.put("showfirstpagenumber", Boolean.FALSE);
        this.treeProperties.put("showpagesasajax", Boolean.TRUE);
    }

    /**
     * die Anzeige der Details ändern (z.B. nur die Metadaten anzeigen, oder nur
     * die Paginierungssequenzen).
     *
     * @return Navigationsanweisung "null" als String (also gleiche Seite
     *         reloaden)
     */
    public String changeView() {
        this.modeView = Helper.getRequestParameter("Ansicht");
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * Add.
     */
    public void add() {
        this.modeAdd = true;
        Modes.setBindState(BindState.create);
        getMetadatum().setValue("");
    }

    /**
     * Add person.
     *
     * @return String
     */
    public String addPerson() {
        this.modeAddPerson = true;
        this.tempPersonNachname = "";
        this.tempPersonRecord = ConfigCore.getParameter(Parameters.AUTHORITY_DEFAULT, "");
        this.tempPersonVorname = "";
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * cancel.
     *
     * @return String
     */
    public String cancel() {
        this.modeAdd = false;
        this.modeAddPerson = false;
        Modes.setBindState(BindState.edit);
        getMetadatum().setValue("");
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * Save metadata to Xml file.
     */
    public void saveMetadataToXml() {
        calculateMetadataAndImages();
        cleanupMetadata();
        if (storeMetadata()) {
            Helper.setMeldung("XML saved");
        }
    }

    /**
     * Save metadata to Xml file.
     */
    public String saveMetadataToXmlAndGoToProcessPage() {
        calculateMetadataAndImages();
        cleanupMetadata();
        if (storeMetadata()) {
            return "/pages/ProzessverwaltungAlle?faces-redirect=true";
        } else {
            Helper.setMeldung("XML could not be saved");
            return "";
        }
    }

    /**
     * Copy.
     *
     */
    public void copy() {
        Metadata md;
        try {
            md = new Metadata(this.curMetadatum.getMd().getType());

            md.setValue(this.curMetadatum.getMd().getValue());
            this.docStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setFehlerMeldung(e.getMessage());
            logger.error("Error at Metadata copy (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Copy person.
     *
     * @return String
     */
    public String copyPerson() {
        Person per;
        try {
            per = new Person(this.myPrefs.getMetadataTypeByName(this.curPerson.getP().getRole()));
            per.setFirstname(this.curPerson.getP().getFirstname());
            per.setLastname(this.curPerson.getP().getLastname());
            per.setRole(this.curPerson.getP().getRole());

            this.docStruct.addPerson(per);
        } catch (IncompletePersonObjectException e) {
            logger.error("Fehler beim copy von Personen (IncompletePersonObjectException): " + e.getMessage());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim copy von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        saveMetadataAsBean(this.docStruct);
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * Change current document structure.
     *
     * @return String
     */
    public void changeCurrentDocstructType() {

        if (this.docStruct != null && this.tempTyp != null) {
            try {
                DocStruct result = this.metaHelper.changeCurrentDocstructType(this.docStruct, this.tempTyp);
                saveMetadataAsBean(result);
                readMetadataAsFirstTree();
            } catch (DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (DocStructHasNoTypeException): ",
                        e.getMessage());
                logger.error("Error while changing DocStructTypes (DocStructHasNoTypeException): " + e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (MetadataTypeNotAllowedException): ",
                        e.getMessage());
                logger.error(
                        "Error while changing DocStructTypes (MetadataTypeNotAllowedException): " + e.getMessage());
            } catch (TypeNotAllowedAsChildException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedAsChildException): ",
                        e.getMessage());
                logger.error("Error while changing DocStructTypes (TypeNotAllowedAsChildException): " + e.getMessage());
            } catch (TypeNotAllowedForParentException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes (TypeNotAllowedForParentException): ",
                        e.getMessage());
                logger.error(
                        "Error while changing DocStructTypes (TypeNotAllowedForParentException): " + e.getMessage());
            }
        }
    }

    /**
     * Save.
     *
     */
    public void save() {
        try {
            Metadata md = new Metadata(this.myPrefs.getMetadataTypeByName(this.tempTyp));
            md.setValue(this.selectedMetadatum.getValue());

            this.docStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        /*
         * wenn TitleDocMain, dann gleich Sortiertitel mit gleichem Inhalt
         * anlegen
         */
        if (this.tempTyp.equals("TitleDocMain") && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
            try {
                Metadata secondMetadata = new Metadata(this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                secondMetadata.setValue(this.selectedMetadatum.getValue());
                this.docStruct.addMetadata(secondMetadata);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }

        this.modeAdd = false;
        Modes.setBindState(BindState.edit);
        this.selectedMetadatum.setValue("");
        this.tempValue = "";
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Save person.
     *
     * @return String
     */
    public String savePerson() {
        try {
            Person per = new Person(this.myPrefs.getMetadataTypeByName(this.tempPersonRolle));
            per.setFirstname(this.tempPersonVorname);
            per.setLastname(this.tempPersonNachname);
            per.setRole(this.tempPersonRolle);
            String[] authorityFile = parseAuthorityFileArgs(tempPersonRecord);
            per.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
            this.docStruct.addPerson(per);
        } catch (IncompletePersonObjectException e) {
            Helper.setFehlerMeldung("Incomplete data for person", "");

            return "";
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setFehlerMeldung("Person is for this structure not allowed", "");
            return "";
        }
        this.modeAddPerson = false;
        saveMetadataAsBean(this.docStruct);
        if (!updateBlocked()) {
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
     * @param valueURI
     *            URI in an authority file
     * @return a String[] with authority, authorityURI and valueURI
     */
    static String[] parseAuthorityFileArgs(String valueURI) {
        String authority = null;
        String authorityURI = null;
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
                    authority = ConfigCore
                            .getParameter(Parameters.AUTHORITY_ID_FROM_URI.replaceFirst("\\{0\\}", authorityURI), null);
                }
            }
        }
        return new String[] {authority, authorityURI, valueURI };
    }

    /**
     * Delete.
     *
     */
    public void delete() {
        this.docStruct.removeMetadata(this.curMetadatum.getMd());
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Delete person.
     *
     * @return String
     */
    public String deletePerson() {
        this.docStruct.removePerson(this.curPerson.getP());
        saveMetadataAsBean(this.docStruct);
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * die noch erlaubten Rollen zurückgeben.
     */
    public ArrayList<SelectItem> getAddableRollen() {
        return this.metaHelper.getAddablePersonRoles(this.docStruct, "");
    }

    /**
     * Get size of roles.
     *
     * @return size
     */
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

    /**
     * Get size of metadata.
     *
     * @return size
     */
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
     * die noch erlaubten Metadaten zurückgeben.
     */
    public ArrayList<SelectItem> getAddableMetadataTypes() {
        ArrayList<SelectItem> selectItems = new ArrayList<>();
        /*
         * zuerst mal alle addierbaren Metadatentypen ermitteln
         */
        List<MetadataType> types = this.docStruct.getAddableMetadataTypes();
        if (types == null) {
            return selectItems;
        }

        /*
         * alle Metadatentypen, die keine Person sind, oder mit einem
         * Unterstrich anfangen rausnehmen
         */
        for (MetadataType mdt : new ArrayList<>(types)) {
            if (mdt.getIsPerson()) {
                types.remove(mdt);
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortType("MetadatenTypen");
        Collections.sort(types, c);

        int counter = types.size();

        for (MetadataType mdt : types) {
            selectItems.add(new SelectItem(mdt.getName(), this.metaHelper.getMetadatatypeLanguage(mdt)));
            try {
                Metadata md = new Metadata(mdt);
                MetadatumImpl mdum = new MetadatumImpl(md, counter, this.myPrefs, this.process);
                counter++;
                this.tempMetadatumList.add(mdum);

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        return selectItems;
    }

    public ArrayList<MetadatumImpl> getTempMetadatumList() {
        return this.tempMetadatumList;
    }

    public void setTempMetadatumList(ArrayList<MetadatumImpl> tempMetadatumList) {
        this.tempMetadatumList = tempMetadatumList;
    }

    /**
     * die MetadatenTypen zurückgeben.
     */
    public SelectItem[] getMetadatenTypen() {
        /*
         * zuerst mal die addierbaren Metadatentypen ermitteln
         */
        List<MetadataType> types = this.docStruct.getAddableMetadataTypes();

        if (types == null) {
            return new SelectItem[0];
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortType("MetadatenTypen");
        Collections.sort(types, c);

        /*
         * nun ein Array mit der richtigen Größe anlegen
         */
        int zaehler = types.size();
        SelectItem[] myTypen = new SelectItem[zaehler];

        /*
         * und anschliessend alle Elemente in das Array packen
         */
        zaehler = 0;
        for (MetadataType mdt : types) {
            myTypen[zaehler] = new SelectItem(mdt.getName(), this.metaHelper.getMetadatatypeLanguage(mdt));
            zaehler++;
        }

        /*
         * alle Typen, die einen Unterstrich haben nochmal rausschmeissen
         */
        SelectItem[] typesWithoutUnderscore = new SelectItem[zaehler];
        for (int i = 0; i < zaehler; i++) {
            typesWithoutUnderscore[i] = myTypen[i];
        }
        return typesWithoutUnderscore;
    }

    /*
     * Metadaten lesen und schreiben
     */

    /**
     * Metadaten Einlesen.
     *
     */
    public String readXml() {
        String redirect = "";
        if (xmlReadingLock.tryLock()) {
            try {
                readXmlAndBuildTree();
            } catch (RuntimeException rte) {
                throw rte;
            } finally {
                xmlReadingLock.unlock();
            }
        } else {
            Helper.setFehlerMeldung("metadatenEditorThreadLock");
            return redirect;
        }
        redirect = "/pages/metadataEditor?faces-redirect=true";
        return redirect;
    }

    private void readXmlAndBuildTree() {

        /*
         * re-reading the config for display rules
         */
        ConfigDispayRules.getInstance().refresh();

        Modes.setBindState(BindState.edit);
        try {
            Integer id = Integer.valueOf(Helper.getRequestParameter("ProzesseID"));
            this.process = serviceManager.getProcessService().getById(id);
        } catch (NumberFormatException | DAOException e1) {
            Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
        }
        this.userId = Helper.getRequestParameter("BenutzerID");
        this.allPagesSelectionFirstPage = "";
        this.allPagesSelectionLastPage = "";
        this.result = Helper.getRequestParameter("zurueck");
        String onlyRead = Helper.getRequestParameter("nurLesen");
        if (onlyRead != null) {
            this.modeOnlyRead = onlyRead.equals("true");
        }
        this.neuesElementWohin = "1";
        this.treeNodeStruct = null;
        try {
            readXmlStart();
        } catch (ReadException e) {
            Helper.setFehlerMeldung(e.getMessage());
        } catch (PreferencesException | IOException e) {
            Helper.setFehlerMeldung("error while loading metadata" + e.getMessage());
        }

        expandTree();
        this.sperrung.setLocked(this.process.getId(), this.userId);

    }

    /**
     * Metadaten Einlesen.
     */

    public void readXmlStart() throws ReadException, IOException, PreferencesException {
        currentRepresentativePage = "";
        this.myPrefs = serviceManager.getRulesetService().getPreferences(this.process.getRuleset());
        this.modeView = "Metadaten";
        this.modeAdd = false;
        this.modeAddPerson = false;
        this.modeMoveStructureElement = false;
        // TODO: Make file pattern configurable
        this.image = null;
        this.imageNumber = 1;
        this.imageRotation = 0;
        this.currentTifFolder = null;
        readAllTifFolders();

        /*
         * Dokument einlesen
         */
        this.gdzfile = serviceManager.getProcessService().readMetadataFile(this.process);
        this.digitalDocument = this.gdzfile.getDigitalDocument();
        this.digitalDocument.addAllContentFiles();
        this.metaHelper = new MetadatenHelper(this.myPrefs, this.digitalDocument);
        this.imageHelper = new MetadatenImagesHelper(this.myPrefs, this.digitalDocument);

        /*
         * Das Hauptelement ermitteln
         */

        // TODO: think something up, how to handle a not matching ruleset
        // causing logicalDocstruct to be null
        this.logicalTopstruct = this.digitalDocument.getLogicalDocStruct();

        // this exception needs some serious feedback because data is corrupted
        if (this.logicalTopstruct == null) {
            throw new ReadException(Helper.getTranslation("metaDataError"));
        }

        identifyImage(1);
        retrieveAllImages();
        if (ConfigCore.getBooleanParameter(Parameters.WITH_AUTOMATIC_PAGINATION, true)
                && (this.digitalDocument.getPhysicalDocStruct() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren().size() == 0)) {
            try {
                createPagination();
            } catch (TypeNotAllowedForParentException e) {
                logger.error(e);
            }
        }

        if (this.digitalDocument.getPhysicalDocStruct().getAllMetadata() != null
                && this.digitalDocument.getPhysicalDocStruct().getAllMetadata().size() > 0) {
            for (Metadata md : this.digitalDocument.getPhysicalDocStruct().getAllMetadata()) {
                if (md.getType().getName().equals("_representative")) {
                    try {
                        Integer value = Integer.valueOf(md.getValue());
                        currentRepresentativePage = String.valueOf(value - 1);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }

        createDefaultValues(this.logicalTopstruct);
        saveMetadataAsBean(this.logicalTopstruct);
        readMetadataAsFirstTree();

    }

    private void createDefaultValues(DocStruct element) {
        if (ConfigCore.getBooleanParameter("MetsEditorEnableDefaultInitialisation", true)) {
            saveMetadataAsBean(element);
            if (element.getAllChildren() != null && element.getAllChildren().size() > 0) {
                for (DocStruct ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }

    private void calculateMetadataAndImages() {

        /*
         * für den Prozess nochmal die Metadaten durchlaufen und die Daten
         * speichern
         */
        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        this.process
                .setSortHelperDocstructs(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.DOCSTRUCT));
        this.process.setSortHelperMetadata(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.METADATA));
        try {
            this.process.setSortHelperImages(fileService
                    .getNumberOfFiles(serviceManager.getProcessService().getImagesOrigDirectory(true, this.process)));
            serviceManager.getProcessService().save(this.process);
        } catch (DataException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
        } catch (IOException e) {
            Helper.setFehlerMeldung("error while counting current images", e);
            logger.error(e);
        }
    }

    private void cleanupMetadata() {
        /*
         * vor dem save alle ungenutzen Docstructs rauswerfen
         */
        this.metaHelper.deleteAllUnusedElements(this.digitalDocument.getLogicalDocStruct());

        if (currentRepresentativePage != null && currentRepresentativePage.length() > 0) {
            boolean match = false;
            if (this.digitalDocument.getPhysicalDocStruct() != null
                    && this.digitalDocument.getPhysicalDocStruct().getAllMetadata() != null
                    && this.digitalDocument.getPhysicalDocStruct().getAllMetadata().size() > 0) {
                for (Metadata md : this.digitalDocument.getPhysicalDocStruct().getAllMetadata()) {
                    if (md.getType().getName().equals("_representative")) {
                        Integer value = Integer.valueOf(currentRepresentativePage);
                        md.setValue(String.valueOf(value + 1));
                        match = true;
                    }
                }
            }
            if (!match) {
                MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
                try {
                    Metadata md = new Metadata(mdt);
                    Integer value = Integer.valueOf(currentRepresentativePage);
                    md.setValue(String.valueOf(value + 1));
                    this.digitalDocument.getPhysicalDocStruct().addMetadata(md);
                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }

            }
        }
    }

    private boolean storeMetadata() {
        boolean result = true;
        try {
            fileService.writeMetadataFile(this.gdzfile, this.process);
        } catch (Exception e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
            result = false;

        }
        return result;
    }

    /**
     * Metadaten Schreiben.
     */
    public String writeXml() {

        calculateMetadataAndImages();

        cleanupMetadata();

        storeMetadata() ;


        disableReturn();
        return this.result;
    }

    /**
     * Check for representative.
     *
     * @return boolean
     */
    public boolean isCheckForRepresentative() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");

        return mdt != null;
    }

    /**
     * vom aktuellen Strukturelement alle Metadaten einlesen.
     *
     * @param inStrukturelement
     *            DocStruct object
     */

    private void saveMetadataAsBean(DocStruct inStrukturelement) {
        this.docStruct = inStrukturelement;
        LinkedList<MetadatumImpl> lsMeta = new LinkedList<>();
        LinkedList<MetaPerson> lsPers = new LinkedList<>();

        /*
         * alle Metadaten und die DefaultDisplay-Werte anzeigen
         */
        List<? extends Metadata> tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(inStrukturelement,
                (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"), false, this.process);
        if (tempMetadata != null) {
            for (Metadata metadata : tempMetadata) {
                MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.process);
                meta.getSelectedItem();
                lsMeta.add(meta);
            }
        }

        /*
         * alle Personen und die DefaultDisplay-Werte ermitteln
         */
        tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(inStrukturelement,
                (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"), true, this.process);
        if (tempMetadata != null) {
            for (Metadata metadata : tempMetadata) {
                lsPers.add(new MetaPerson((Person) metadata, 0, this.myPrefs, inStrukturelement));
            }
        }

        this.myMetadaten = lsMeta;
        this.metaPersonList = lsPers;

        /*
         * die zugehörigen Seiten ermitteln
         */
        determinePagesStructure(this.docStruct);
    }

    /*
     * Treeview
     */

    @SuppressWarnings("rawtypes")
    private void readMetadataAsFirstTree() {
        HashMap map;
        TreeNodeStruct3 nodes;
        List<DocStruct> status = new ArrayList<>();

        /*
         * den Ausklapp-Zustand aller Knoten erfassen
         */
        if (this.treeNodeStruct != null) {
            for (HashMap childrenList : this.treeNodeStruct.getChildrenAsList()) {
                map = childrenList;
                nodes = (TreeNodeStruct3) map.get("node");
                if (nodes.isExpanded()) {
                    status.add(nodes.getStruct());
                }
            }
        }

        if (this.logicalTopstruct == null) {

        }
        /*
         * Die Struktur als Tree3 aufbereiten
         */
        String label = this.logicalTopstruct.getType()
                .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
        if (label == null) {
            label = this.logicalTopstruct.getType().getName();
        }

        this.treeNodeStruct = new TreeNodeStruct3(label, this.logicalTopstruct);
        readMetadataAsSecondTree(this.logicalTopstruct, this.treeNodeStruct);

        /*
         * den Ausklappzustand nach dem neu-Einlesen wieder herstellen
         */
        for (HashMap childrenList : this.treeNodeStruct.getChildrenAsListAlle()) {
            map = childrenList;
            nodes = (TreeNodeStruct3) map.get("node");
            // Ausklappstatus wiederherstellen
            if (status.contains(nodes.getStruct())) {
                nodes.setExpanded(true);
            }
            // Selection wiederherstellen
            if (this.docStruct == nodes.getStruct()) {
                nodes.setSelected(true);
            }
        }

    }

    /**
     * Metadaten in Tree3 ausgeben.
     *
     * @param inStrukturelement
     *            DocStruct object
     * @param upperNode
     *            TreeNodeStruct3 object
     */
    private void readMetadataAsSecondTree(DocStruct inStrukturelement, TreeNodeStruct3 upperNode) {
        upperNode.setMainTitle(determineMetadata(inStrukturelement, "TitleDocMain"));
        upperNode.setZblNummer(determineMetadata(inStrukturelement, "ZBLIdentifier"));
        upperNode.setZblSeiten(determineMetadata(inStrukturelement, "ZBLPageNumber"));
        upperNode.setPpnDigital(determineMetadata(inStrukturelement, "IdentifierDigital"));
        upperNode.setFirstImage(this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberFirst()));
        upperNode.setLastImage(this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberLast()));
        // wenn es ein Heft ist, die Issue-Number mit anzeigen
        if (inStrukturelement.getType().getName().equals("PeriodicalIssue")) {
            upperNode.setDescription(
                    upperNode.getDescription() + " " + determineMetadata(inStrukturelement, "CurrentNo"));
        }

        // wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
        if (inStrukturelement.getType().getName().equals("Periodical")
                || inStrukturelement.getType().getName().equals("PeriodicalVolume")) {
            upperNode.setExpanded(true);
        }

        /*
         * vom aktuellen Strukturelement alle Kinder in den Tree packen
         */
        List<DocStruct> meineListe = inStrukturelement.getAllChildren();
        if (meineListe != null) {
            /* es gibt Kinder-Strukturelemente */
            for (DocStruct kind : meineListe) {
                String label = kind.getType().getNameByLanguage(
                        (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
                if (label == null) {
                    label = kind.getType().getName();
                }
                TreeNodeStruct3 tns = new TreeNodeStruct3(label, kind);
                upperNode.addChild(tns);
                readMetadataAsSecondTree(kind, tns);
            }
        }
    }

    /**
     * Metadaten gezielt zurückgeben.
     *
     * @param inStrukturelement
     *            DocStruct object
     * @param inTyp
     *            String
     */
    private String determineMetadata(DocStruct inStrukturelement, String inTyp) {
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

    public String getMetadataByElementAndType(DocStruct inStrukturelement, String inTyp) {
        String result = "";
        List<Metadata> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                if (md.getType().getName().equals(inTyp)) {
                    result += (md.getValue() == null ? "" : md.getValue()) + " ";
                }
            }
        }
        return result.trim();
    }

    public String getImageRangeByElement(DocStruct inStrukturelement) {
        String firstImage = this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberFirst());
        String lastImage = this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberLast());

        return firstImage + " - " + lastImage;
    }

    /**
     * Set my structure element.
     *
     * @param inStruct
     *            DocStruct
     */
    @SuppressWarnings("rawtypes")
    public void setMyStrukturelement(DocStruct inStruct) {
        this.modeAdd = false;
        this.modeAddPerson = false;
        Modes.setBindState(BindState.edit);
        saveMetadataAsBean(inStruct);

        /*
         * die Selektion kenntlich machen
         */
        for (HashMap childrenList : this.treeNodeStruct.getChildrenAsListAlle()) {
            TreeNodeStruct3 nodes = (TreeNodeStruct3) childrenList.get("node");
            // Selection wiederherstellen
            if (this.docStruct == nodes.getStruct()) {
                nodes.setSelected(true);
            } else {
                nodes.setSelected(false);
            }
        }

        updateBlocked();
    }

    /**
     * Knoten nach oben schieben.
     */
    public void nodeUp() {
        try {
            this.metaHelper.knotUp(this.docStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        readMetadataAsFirstTree();
    }

    /**
     * Knoten nach unten schieben.
     */
    public void nodeDown() {
        try {
            this.metaHelper.setNodeDown(this.docStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        readMetadataAsFirstTree();
    }

    /**
     * Knoten zu einer anderen Stelle.
     */
    public String moveNode() throws TypeNotAllowedAsChildException {
        this.docStruct.getParent().removeChild(this.docStruct);
        this.tempStrukturelement.addChild(this.docStruct);
        readMetadataAsFirstTree();
        logger.debug(this.modeMoveStructureElement);
        this.neuesElementWohin = "1";
        return "Metadaten3links";
    }

    /**
     * Knoten nach oben schieben.
     */
    public void deleteNode() {
        if (this.docStruct != null && this.docStruct.getParent() != null) {
            DocStruct tempParent = this.docStruct.getParent();
            this.docStruct.getParent().removeChild(this.docStruct);
            this.docStruct = tempParent;
        }
        // den Tree neu einlesen
        readMetadataAsFirstTree();
    }

    /**
     * Knoten hinzufügen.=
     */
    public void addNode() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {

        /*
         * prüfen, wohin das Strukturelement gepackt werden soll, anschliessend
         * entscheiden, welches Strukturelement gewählt wird und abschliessend
         * richtig einfügen
         */

        DocStruct ds = null;
        /*
         * vor das aktuelle Element
         */
        if (this.neuesElementWohin.equals("1")) {
            if (this.addFirstDocStructType == null || this.addFirstDocStructType.equals("")) {
                return;
            }
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(this.addFirstDocStructType);
            ds = this.digitalDocument.createDocStruct(dst);
            if (this.docStruct == null) {
                return;
            }
            DocStruct parent = this.docStruct.getParent();
            if (parent == null) {
                logger.debug("das gewählte Element kann den Vater nicht ermitteln");
            }
            List<DocStruct> alleDS = new ArrayList<>();

            /* alle Elemente des Parents durchlaufen */
            for (DocStruct docStruct : parent.getAllChildren()) {
                /* wenn das aktuelle Element das gesuchte ist */
                if (docStruct == this.docStruct) {
                    alleDS.add(ds);
                }
                alleDS.add(docStruct);
            }

            /* anschliessend alle Childs entfernen */
            for (DocStruct docStruct : alleDS) {
                parent.removeChild(docStruct);
            }

            /* anschliessend die neue Childliste anlegen */
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        /*
         * hinter das aktuelle Element
         */
        if (this.neuesElementWohin.equals("2")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(this.addFirstDocStructType);
            ds = this.digitalDocument.createDocStruct(dst);
            DocStruct parent = this.docStruct.getParent();
            if (parent == null) {
                logger.debug("das gewählte Element kann den Vater nicht ermitteln");
                return;
            }
            List<DocStruct> alleDS = new ArrayList<>();

            /* alle Elemente des Parents durchlaufen */
            for (DocStruct docStruct : parent.getAllChildren()) {
                alleDS.add(docStruct);
                /* wenn das aktuelle Element das gesuchte ist */
                if (docStruct == this.docStruct) {
                    alleDS.add(ds);
                }
            }

            /* anschliessend alle Childs entfernen */
            for (DocStruct docStruct : alleDS) {
                parent.removeChild(docStruct);
            }

            /* anschliessend die neue Childliste anlegen */
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        /*
         * als erstes Child
         */
        if (this.neuesElementWohin.equals("3")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(this.addSecondDocStructType);
            ds = this.digitalDocument.createDocStruct(dst);
            DocStruct parent = this.docStruct;
            if (parent == null) {
                logger.debug("das gewählte Element kann den Vater nicht ermitteln");
                return;
            }
            List<DocStruct> alleDS = new ArrayList<>();
            alleDS.add(ds);

            if (parent.getAllChildren() != null && parent.getAllChildren().size() != 0) {
                alleDS.addAll(parent.getAllChildren());
                parent.getAllChildren().retainAll(new ArrayList<>());
            }

            /* anschliessend die neue Childliste anlegen */
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        /*
         * als letztes Child
         */
        if (this.neuesElementWohin.equals("4")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(this.addSecondDocStructType);
            ds = this.digitalDocument.createDocStruct(dst);
            this.docStruct.addChild(ds);
        }

        if (!this.pagesStart.equals("") && !this.pagesEnd.equals("")) {
            DocStruct temp = this.docStruct;
            this.docStruct = ds;
            this.ajaxPageStart = this.pagesStart;
            this.ajaxPageEnd = this.pagesEnd;
            ajaxSeitenStartUndEndeSetzen();
            this.docStruct = temp;
        }

        readMetadataAsFirstTree();
    }

    /**
     * mögliche Docstructs als Kind zurückgeben.
     */
    public SelectItem[] getAddableDocStructTypenAlsKind() {
        return this.metaHelper.getAddableDocStructTypen(this.docStruct, false);
    }

    /**
     * mögliche Docstructs als Nachbar zurückgeben.
     */
    public SelectItem[] getAddableDocStructTypenAlsNachbar() {
        return this.metaHelper.getAddableDocStructTypen(this.docStruct, true);
    }

    /*
     * Strukturdaten: Seiten
     */

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images.
     */
    public String createPagination() throws TypeNotAllowedForParentException, IOException {
        this.imageHelper.createPagination(this.process, this.currentTifFolder);
        retrieveAllImages();

        // added new
        DocStruct log = this.digitalDocument.getLogicalDocStruct();
        while (log.getType().getAnchorClass() != null && log.getAllChildren() != null
                && log.getAllChildren().size() > 0) {
            log = log.getAllChildren().get(0);
        }
        if (log.getType().getAnchorClass() != null) {
            return "";
        }

        if (log.getAllChildren() != null) {
            for (DocStruct child : log.getAllChildren()) {
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
        return "";
    }

    /**
     * alle Seiten ermitteln.
     */
    public void retrieveAllImages() {
        DigitalDocument document = null;
        try {
            document = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMeldung(null, "Can not get DigitalDocument: ", e.getMessage());
            return;
        }

        List<DocStruct> meineListe = document.getPhysicalDocStruct().getAllChildren();
        if (meineListe == null) {
            this.allPages = null;
            return;
        }
        int zaehler = meineListe.size();
        this.allPages = new String[zaehler];
        this.allPagesNew = new MetadatumImpl[zaehler];
        zaehler = 0;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        for (DocStruct mySeitenDocStruct : meineListe) {
            List<? extends Metadata> mySeitenDocStructMetadaten = mySeitenDocStruct.getAllMetadataByType(mdt);
            for (Metadata meineSeite : mySeitenDocStructMetadaten) {
                this.allPagesNew[zaehler] = new MetadatumImpl(meineSeite, zaehler, this.myPrefs, this.process);
                this.allPages[zaehler] = determineMetadata(meineSeite.getDocStruct(), "physPageNumber").trim() + ": " + meineSeite.getValue();
            }
            zaehler++;
        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln.
     */
    private void determinePagesStructure(DocStruct inStrukturelement) {
        if (inStrukturelement == null) {
            return;
        }
        List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
        int zaehler = 0;
        int imageNr = 0;
        if (listReferenzen != null) {
            /*
             * Referenzen sortieren
             */
            Collections.sort(listReferenzen, new Comparator<Reference>() {
                @Override
                public int compare(final Reference firstObject, final Reference secondObject) {
                    Integer firstPage = 0;
                    Integer secondPage = 0;

                    MetadataType mdt = Metadaten.this.myPrefs.getMetadataTypeByName("physPageNumber");
                    List<? extends Metadata> listMetadaten = firstObject.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        Metadata meineSeite = listMetadaten.get(0);
                        firstPage = Integer.parseInt(meineSeite.getValue());
                    }
                    listMetadaten = secondObject.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        Metadata meineSeite = listMetadaten.get(0);
                        secondPage = Integer.parseInt(meineSeite.getValue());
                    }
                    return firstPage.compareTo(secondPage);
                }
            });

            /* die Größe der Arrays festlegen */
            this.structSeiten = new SelectItem[listReferenzen.size()];
            this.structSeitenNeu = new MetadatumImpl[listReferenzen.size()];

            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (Reference ref : listReferenzen) {
                DocStruct target = ref.getTarget();
                determineSecondPagesStructure(target, zaehler);
                if (imageNr == 0) {
                    imageNr = determineThirdPagesStructure(target);
                }
                zaehler++;
            }

        }

        /*
         * Wenn eine Verknüpfung zwischen Strukturelement und Bildern sein soll,
         * das richtige Bild anzeigen
         */
        if (this.imageToStructuralElement) {
            identifyImage(imageNr - this.imageNumber);
        }
    }

    public void getfirstPageOfElement(DocStruct structureElement) {
//        structureElement.getAllMetadata().get(0).
//        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
//        System.out.println(mdt.getNum());
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln 2.
     */
    private void determineSecondPagesStructure(DocStruct inStrukturelement, int inZaehler) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return;
        }
        for (Metadata meineSeite : listMetadaten) {
            this.structSeitenNeu[inZaehler] = new MetadatumImpl(meineSeite, inZaehler, this.myPrefs, this.process);
            this.structSeiten[inZaehler] = new SelectItem(String.valueOf(inZaehler),
                    determineMetadata(meineSeite.getDocStruct(), "physPageNumber").trim() + ": "
                            + meineSeite.getValue());
        }
    }

    /**
     * noch für Testzweck zum direkten öffnen der richtigen Startseite 3.
     */
    private int determineThirdPagesStructure(DocStruct inStrukturelement) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return 0;
        }
        int result = 0;
        for (Metadata page : listMetadaten) {
            result = Integer.parseInt(page.getValue());
        }
        return result;
    }

    /**
     * Gets paginator instance.
     *
     * @return The paginator instance.
     */
    public Paginator getPaginator() {
        return paginator;
    }

    /**
     * Sets paginator instance.
     *
     * @param paginator The paginator instance.
     */
    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    /**
     * die Paginierung ändern.
     */
    public String changePagination() {

        int[] pageSelection = new int[allPagesSelection.length];
        for (int i = 0; i < allPagesSelection.length; i++) {
            pageSelection[i] = Integer.parseInt(allPagesSelection[i].split(":")[0]) - 1;
        }

        try {
            paginator.setPageSelection(pageSelection);
            paginator.setPagesToPaginate(allPagesNew);
            paginator.setFictitious(fictitious);
            paginator.setPaginationSeparator(paginationSeparators.getObject().getSeparatorString());
            paginator.setPaginationStartValue(paginationValue);
            paginator.run();
        } catch (IllegalArgumentException iae) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", iae.getMessage());
        }

        /*
         * zum Schluss nochmal alle Seiten neu einlesen
         */
        allPagesSelection = null;
        retrieveAllImages();
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }

        return null;
    }

    /**
     * alle Knoten des Baums expanden oder collapsen.
     */
    public String expandTree() {
        this.treeNodeStruct.expandNodes(this.treeProperties.get("fullexpanded"));
        return "Metadaten3links";
    }

    /**
     * scrollImage.
     *
     * @return String
     */
    public String scrollImage() {
        String parameter = Helper.getRequestParameter("Anzahl");
        if (parameter.equals("")) {
            parameter = "0";
        }
        int tempint = Integer.parseInt(parameter);
        identifyImage(tempint);
        return "";
    }

    /**
     * Rotate left.
     */
    public void rotateLeft() {
        if (this.imageRotation < 90) {
            this.imageRotation = 360;
        }
        this.imageRotation = (this.imageRotation - 90) % 360;
        identifyImage(this.imageNumber);
    }

    public void rotateRight() {
        this.imageRotation = (this.imageRotation + 90) % 360;
        identifyImage(this.imageNumber);
    }

    /**
     * goToCurrentImageNumber.
     */
    public void goToCurrentImageNumber() {
        identifyImage(this.imageNumber);
    }

    /**
     * Changes image number and visualization to 1.
     */
    public void goToFirstImage() {
        this.imageNumber = 1;
        goToCurrentImageNumber();
    }

    /**
     * Changes image number and visualization to last image.
     */
    public void goToLastImage() {
        this.imageNumber = this.lastImage;
        goToCurrentImageNumber();
    }

    /**
     * Get image.
     *
     * @return String
     */
    public String getBild() {
        /* Session ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        return ConfigCore.getTempImagesPath() + session.getId() + "_" + this.imageCounter + ".png";
    }

    public List<URI> getAllTifFolders() {
        return this.allTifFolders;
    }

    /**
     * Read all tif folders.
     */
    public void readAllTifFolders() throws IOException {
        this.allTifFolders = new ArrayList<>();

        /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
        FilenameFilter filterDirectory = new IsDirectoryFilter();
        ArrayList<URI> subUris = fileService.getSubUrisForProcess(filterDirectory, this.process,
                ProcessSubType.IMAGE, "");
        this.allTifFolders.addAll(subUris);

        if (ConfigCore.getParameter("MetsEditorDefaultSuffix", null) != null) {
            String suffix = ConfigCore.getParameter("MetsEditorDefaultSuffix");
            for (URI directoryUri : this.allTifFolders) {
                if (directoryUri.toString().endsWith(suffix) || directoryUri.toString().endsWith(suffix + "/")) {
                    this.currentTifFolder = directoryUri;
                    break;
                }
            }
        }

        if (!this.allTifFolders.contains(this.currentTifFolder)) {
            this.currentTifFolder = serviceManager.getProcessService().getImagesTifDirectory(true, this.process);
        }
    }

    /**
     * identifyImage.
     *
     * @param pageNumber
     *            int
     */
    public void identifyImage(int pageNumber) {
        /*
         * wenn die Bilder nicht angezeigt werden, brauchen wir auch das Bild
         * nicht neu umrechnen
         */
        logger.trace("start identifyImage 1");
        if (!this.displayImage) {
            logger.trace("end identifyImage 1");
            return;
        }
        logger.trace("ocr identifyImage");
        this.ocrResult = "";

        logger.trace("dataList");
        List<URI> dataList = this.imageHelper.getImageFiles(digitalDocument.getPhysicalDocStruct());
        logger.trace("dataList 2");
        if (ConfigCore.getBooleanParameter(Parameters.WITH_AUTOMATIC_PAGINATION, true)
                && (dataList == null || dataList.isEmpty())) {
            try {
                createPagination();
                dataList = this.imageHelper.getImageFiles(digitalDocument.getPhysicalDocStruct());
            } catch (IOException | TypeNotAllowedForParentException e) {
                logger.error(e);
            }
        }
        if (dataList != null && dataList.size() > 0) {
            logger.trace("dataList not null");
            this.lastImage = dataList.size();
            logger.trace("myBildLetztes");
            if (this.image == null) {
                this.image = dataList.get(0);
            }
            if (this.currentTifFolder != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("currentTifFolder: " + this.currentTifFolder);
                }
                dataList = this.imageHelper.getImageFiles(this.process, this.currentTifFolder);
                if (dataList == null) {
                    return;
                }
            }

            if (dataList.size() >= pageNumber) {
                this.image = dataList.get(pageNumber - 1);
            } else {
                Helper.setFehlerMeldung("Image file for page " + pageNumber + " not found in metadata folder: " + this.currentTifFolder);
                this.image = null;
            }

            this.imageNumber = pageNumber;

            URI pagesDirectory = ConfigCore.getTempImagesPathAsCompleteDirectory();

            this.imageCounter++;

            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentPngFile = session.getId() + "_" + this.imageCounter + ".png";
            logger.trace("facescontext");

            File temporaryTifFile = null;
            try {
                temporaryTifFile = File.createTempFile("tempTif_",".tif");
            } catch (IOException e) {
                logger.error(e);
            }
            /* das neue Bild zuweisen */
            if (this.image != null) {
                try {
                    URI tifFile = this.currentTifFolder.resolve(this.image);
                    if (logger.isTraceEnabled()) {
                        logger.trace("tiffconverterpfad: " + tifFile);
                    }
                    if (!fileService.fileExist(tifFile)) {
                        tifFile = serviceManager.getProcessService()
                                .getImagesTifDirectory(true, this.process).resolve(this.image);
                        Helper.setFehlerMeldung("formularOrdner:TifFolders", "",
                                "image " + this.image + " does not exist in folder " + this.currentTifFolder
                                        + ", using image from " + new File(serviceManager.getProcessService()
                                        .getImagesTifDirectory(true, this.process)).getName());
                    }

                    //Copy tif-file to temporay folder
                    InputStream tifFileInputStream = fileService.read(tifFile);
                    if (temporaryTifFile != null) {
                        FileUtils.copyInputStreamToFile(tifFileInputStream,temporaryTifFile);
                        this.imageHelper.scaleFile(temporaryTifFile.toURI(), pagesDirectory.resolve(currentPngFile), this.imageSize,
                                this.imageRotation);
                        logger.trace("scaleFile");
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("could not getById image folder", e);
                    logger.error(e);
                } finally {
                    if (temporaryTifFile != null) {
                        try {
                            if (!fileService.delete(temporaryTifFile.toURI())) {
                                logger.error("Error while deleting temporary tif file: " + temporaryTifFile.getAbsolutePath());
                            }
                            //not working
                        } catch (IOException e) {
                            logger.error("Error while deleting temporary tif file: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if current image is the correct (actually wanted) image from the
     * list.
     * 
     * @param dataList
     *            list of all images
     * @param i
     *            iterator
     * @return true or false
     */
    private boolean isCurrentImageCorrectImage(List<URI> dataList, int i) {
        if (logger.isTraceEnabled()) {
            logger.trace("myBild: " + this.image);
        }
        String index = fileService.getFileName(dataList.get(i));
        if (logger.isTraceEnabled()) {
            logger.trace("index: " + index);
        }
        String picture = fileService.getFileName(this.image);
        return index.equals(picture);
    }

    private void checkImage() {
        /* wenn bisher noch kein Bild da ist, das erste nehmen */
        boolean exists = false;
        try {
            if (this.currentTifFolder != null && this.image != null) {
                exists = fileService.fileExist(fileService.getImagesDirectory(this.process)
                        .resolve(this.currentTifFolder + "/" + this.image));
            }
        } catch (Exception e) {
            this.imageNumber = -1;
            logger.error(e);
        }
        /* wenn das Bild nicht existiert, den Status ändern */
        if (!exists) {
            this.imageNumber = -1;
        }
    }

    /*
     * Sperrung der Metadaten aktualisieren oder prüfen
     */

    private boolean updateBlocked() {
        /*
         * wenn die Sperrung noch aktiv ist und auch für den aktuellen Nutzer
         * gilt, Sperrung aktualisieren
         */
        if (MetadatenSperrung.isLocked(this.process.getId())
                && this.sperrung.getLockBenutzer(this.process.getId()).equals(this.userId)) {
            this.sperrung.setLocked(this.process.getId(), this.userId);
            return true;
        } else {
            return false;
        }
    }

    private void disableReturn() {
        if (MetadatenSperrung.isLocked(this.process.getId())
                && this.sperrung.getLockBenutzer(this.process.getId()).equals(this.userId)) {
            this.sperrung.setFree(this.process.getId());
        }
    }

    /*
     * Navigationsanweisungen
     */

    /**
     * zurück zur Startseite, Metadaten vorher freigeben.
     */
    public String goMain() {
        disableReturn();
        return "newMain";
    }

    /**
     * zurück gehen.
     */
    public String goZurueck() {
        disableReturn();
        return this.result;
    }

    /**
     * Transliteration bestimmter Felder.
     */
    public String transliterate() {
        Metadata md = this.curMetadatum.getMd();

        /*
         * wenn es ein russischer Titel ist, dessen Transliterierungen anzeigen
         */
        if (md.getType().getName().equals("RUSMainTitle")) {
            Transliteration trans = new Transliteration();

            try {
                MetadataType mdt = this.myPrefs.getMetadataTypeByName("MainTitleTransliterated");
                Metadata mdDin = new Metadata(mdt);
                Metadata mdIso = new Metadata(mdt);
                mdDin.setValue(trans.transliterateDIN(md.getValue()));
                mdIso.setValue(trans.transliterateISO(md.getValue()));

                this.docStruct.addMetadata(mdDin);
                this.docStruct.addMetadata(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): "
                        + e.getMessage());
            }
        }
        saveMetadataAsBean(this.docStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /**
     * Transliteration person.
     *
     * @return String
     */
    public String transliteratePerson() {
        Person md = this.curPerson.getP();

        /*
         * wenn es ein russischer Autor ist, dessen Transliterierungen anlegen
         */
        if (md.getRole().equals("Author")) {
            Transliteration trans = new Transliteration();
            try {
                MetadataType metadataTypeDIN = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedDIN");
                MetadataType metadataTypeISO = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedISO");
                Person mdDin = new Person(metadataTypeDIN);
                Person mdIso = new Person(metadataTypeISO);

                mdDin.setFirstname(trans.transliterateDIN(md.getFirstname()));
                mdDin.setLastname(trans.transliterateDIN(md.getLastname()));
                mdIso.setFirstname(trans.transliterateISO(md.getFirstname()));
                mdIso.setLastname(trans.transliterateISO(md.getLastname()));
                mdDin.setRole("AuthorTransliteratedDIN");
                mdIso.setRole("AuthorTransliteratedISO");

                this.docStruct.addPerson(mdDin);
                this.docStruct.addPerson(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): "
                        + e.getMessage());
            }
        }
        saveMetadataAsBean(this.docStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return "";
    }

    /*
     * aus einer Liste von PPNs Strukturelemente aus dem Opac ## holen und dem
     * aktuellen Strukturelement unterordnen
     */

    /**
     * mehrere PPNs aus dem Opac abfragen und dem aktuellen Strukturelement
     * unterordnen.
     */
    public String addAdditionalOpacPpns() {
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                Fileformat addrdf = CataloguePlugin.getFirstHit(opacCatalog,
                        QueryBuilder.restrictToField(opacSearchField, tok), myPrefs);
                if (addrdf != null) {
                    this.docStruct.addChild(addrdf.getDigitalDocument().getLogicalDocStruct());
                    readMetadataAsFirstTree();
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return "Metadaten3links";
    }

    /**
     * eine PPN aus dem Opac abfragen und dessen Metadaten dem aktuellen
     * Strukturelement zuweisen.
     */
    public String addMetadaFromOpacPpn() {
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                Fileformat addrdf = CataloguePlugin.getFirstHit(opacCatalog,
                        QueryBuilder.restrictToField(opacSearchField, tok), myPrefs);
                if (addrdf != null) {

                    /* die Liste aller erlaubten Metadatenelemente erstellen */
                    List<String> erlaubte = new ArrayList<>();
                    for (MetadataType metadataType : this.docStruct.getAddableMetadataTypes()) {
                        erlaubte.add(metadataType.getName());
                    }

                    /*
                     * wenn der Metadatentyp in der Liste der erlaubten Typen,
                     * dann hinzufügen
                     */
                    for (Metadata metadata : addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadata()) {
                        if (erlaubte.contains(metadata.getType().getName())) {
                            this.docStruct.addMetadata(metadata);
                        }
                    }

                    for (Person person : addrdf.getDigitalDocument().getLogicalDocStruct().getAllPersons()) {
                        if (erlaubte.contains(person.getType().getName())) {
                            this.docStruct.addPerson(person);
                        }
                    }

                    readMetadataAsFirstTree();
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        saveMetadataAsBean(this.docStruct);
        this.modeView = "Metadaten";
        return "";
    }

    /**
     * Metadatenvalidierung.
     */
    public void validate() {
        serviceManager.getMetadataValidationService().validate(this.gdzfile, this.myPrefs, this.process);
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Auswahl der Seiten über Ajax.
     */
    public String getAjaxPageStart() {
        return this.ajaxPageStart;
    }

    public void setAjaxPageStart(String ajaxPageStart) {
        this.ajaxPageStart = ajaxPageStart;
    }

    public String getAjaxPageEnd() {
        return this.ajaxPageEnd;
    }

    public void setAjaxPageEnd(String ajaxPageEnd) {
        this.ajaxPageEnd = ajaxPageEnd;
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

    /**
     * Current start page.
     */
    public void currentStartpage() {
        for (String selectItem : this.allPages) {
            if (selectItem.equals(String.valueOf(this.pageNumber))) {
                this.pagesStart = selectItem;
            }
        }
    }

    /**
     * Current end page.
     */
    public void currentEndpage() {
        for (String selectItem : this.allPages) {
            if (selectItem.equals(String.valueOf(this.pageNumber))) {
                this.pagesEnd = selectItem;
            }
        }
    }

    private int pageNumber = 0;

    private SelectOne<Separator> paginationSeparators = new SelectOne<>(
            Separator.factory(ConfigCore.getParameter(Parameters.PAGE_SEPARATORS, "\" \"")));

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Get all Ajax pages.
     *
     * @param prefix
     *            String
     * @return list of Strings
     */
    public List<String> getAjaxAlleSeiten(String prefix) {
        logger.debug("Ajax-Liste abgefragt");
        List<String> li = new ArrayList<>();
        if (this.allPages != null && this.allPages.length > 0) {
            for (String selectItem : this.allPages) {
                if (selectItem.contains(prefix)) {
                    li.add(selectItem);
                }
            }
        }
        return li;
    }

    /**
     * die Seiten über die Ajax-Felder festlegen.
     */
    public void ajaxSeitenStartUndEndeSetzen() {
        boolean startseiteOk = false;
        boolean endseiteOk = false;

        /*
         * alle Seiten durchlaufen und prüfen, ob die eingestellte Seite
         * überhaupt existiert
         */
        for (String selectItem : this.allPages) {
            if (selectItem.equals(this.ajaxPageStart)) {
                startseiteOk = true;
                this.allPagesSelectionFirstPage = selectItem;
            }
            if (selectItem.equals(this.ajaxPageEnd)) {
                endseiteOk = true;
                this.allPagesSelectionLastPage = selectItem;
            }
        }

        /* wenn die Seiten ok sind */
        if (startseiteOk && endseiteOk) {
            setPageStartAndEnd();
        } else {
            Helper.setFehlerMeldung("Selected image(s) unavailable");
        }
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public String setPageStartAndEnd() {
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        int anzahlAuswahl = Integer.parseInt(this.allPagesSelectionLastPage)
                - Integer.parseInt(this.allPagesSelectionFirstPage) + 1;
        if (anzahlAuswahl > 0) {
            /* alle bisher zugewiesenen Seiten entfernen */
            this.docStruct.getAllToReferences().clear();
            int zaehler = 0;
            while (zaehler < anzahlAuswahl) {
                this.docStruct.addReferenceTo(
                        this.allPagesNew[Integer.parseInt(this.allPagesSelectionFirstPage) + zaehler].getMd()
                                .getDocStruct(),
                        "logical_physical");
                zaehler++;
            }
        }
        determinePagesStructure(this.docStruct);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public String takePagesFromChildren() {
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }

        /* alle Kinder des aktuellen DocStructs durchlaufen */
        this.docStruct.getAllReferences("to").removeAll(this.docStruct.getAllReferences("to"));
        if (this.docStruct.getAllChildren() != null) {
            for (DocStruct child : this.docStruct.getAllChildren()) {
                List<Reference> childRefs = child.getAllReferences("to");
                for (Reference toAdd : childRefs) {
                    boolean match = false;
                    for (Reference ref : this.docStruct.getAllReferences("to")) {
                        if (ref.getTarget().equals(toAdd.getTarget())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        this.docStruct.getAllReferences("to").add(toAdd);
                    }

                }
            }
        }
        determinePagesStructure(this.docStruct);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public String imageShowFirstPage() {
        image = null;
        identifyImage(1);
        return "";
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public String imageShowLastPage() {
        this.displayImage = true;
        if (this.treeProperties.get("showpagesasajax")) {
            for (String selectItem : this.allPages) {
                if (selectItem.equals(this.ajaxPageEnd)) {
                    this.allPagesSelectionLastPage = selectItem;
                    break;
                }
            }
        }
        try {
            int pageNumber = Integer.parseInt(this.allPagesSelectionLastPage) - this.imageNumber + 1;
            identifyImage(pageNumber);
        } catch (Exception e) {
            logger.error(e);
        }
        return "";
    }

    /**
     * ausgewählte Seiten dem aktuellen Strukturelement hinzufügen.
     */
    public String addPages() {
        /* alle markierten Seiten durchlaufen */
        for (String page : this.allPagesSelection) {
            int aktuelleID = Integer.parseInt(page.split(":")[0]);
            boolean schonEnthalten = false;

            /*
             * wenn schon References vorhanden, prüfen, ob schon enthalten, erst
             * dann zuweisen
             */
            if (this.docStruct.getAllToReferences("logical_physical") != null) {
                for (Reference reference : this.docStruct.getAllToReferences("logical_physical")) {
                    if (reference.getTarget() == this.allPagesNew[aktuelleID - 1].getMd().getDocStruct()) {
                        schonEnthalten = true;
                        break;
                    }
                }
            }

            if (!schonEnthalten) {
                this.docStruct.addReferenceTo(this.allPagesNew[aktuelleID - 1].getMd().getDocStruct(),
                        "logical_physical");
            }
        }
        determinePagesStructure(this.docStruct);
        this.allPagesSelection = null;
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return null;
    }

    /**
     * ausgewählte Seiten aus dem Strukturelement entfernen.
     */
    public String removePages() {
        for (String structurePage : this.structSeitenAuswahl) {
            int aktuelleID = Integer.parseInt(structurePage);
            this.docStruct.removeReferenceTo(this.structSeitenNeu[aktuelleID].getMd().getDocStruct());
        }
        determinePagesStructure(this.docStruct);
        this.structSeitenAuswahl = null;
        if (!updateBlocked()) {
            return "SperrungAbgelaufen";
        }
        return null;
    }

    /**
     * OCR.
     */
    public boolean isShowOcrButton() {
        return ConfigCore.getBooleanParameter("showOcrButton");
    }

    /**
     * Show OCR result.
     */
    public void showOcrResult() {
        String myOcrUrl = getOcrBasisUrl(this.imageNumber);
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

    /**
     * Get OCR address.
     *
     * @return String
     */
    public String getOcrAcdress() {
        int startseite = -1;
        int endseite = -1;
        if (this.structSeiten != null) {
            for (SelectItem selectItem : this.structSeiten) {
                int temp = Integer.parseInt(selectItem.getLabel().substring(0, selectItem.getLabel().indexOf(":")));
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
        String url = ConfigCore.getParameter("ocrUrl");
        VariableReplacer replacer = new VariableReplacer(this.digitalDocument, this.myPrefs, this.process, null);
        url = replacer.replace(url);
        url += "/&imgrange=" + seiten[0];
        if (seiten.length > 1) {
            url += "-" + seiten[1];
        }
        return url;
    }

    public int getImageNumber() {
        return this.imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    public int getLastImage() {
        return this.lastImage;
    }

    public int getImageSize() {
        return this.imageSize;
    }

    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * Get temporal type.
     *
     * @return String
     */
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

    /**
     * Set temporal type.
     *
     * @param tempTyp
     *            String
     */
    public void setTempTyp(String tempTyp) {
//        DocStructType mdt = this.myPrefs.getDocStrctTypeByName(tempTyp);
//        try {
//            Metadata md = new Metadata(mdt);
//            this.selectedMetadatum = new MetadatumImpl(md, this.myMetadaten.size() + 1, this.myPrefs, this.process);
//        } catch (MetadataTypeNotAllowedException e) {
//            logger.error(e.getMessage());
//        }
        this.tempTyp = tempTyp;
    }


    /**
     * Get metadata.
     *
     * @return MetadatumImpl object
     */
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

    public String getTempValue() {
        return this.tempValue;
    }

    public void setTempValue(String tempValue) {
        this.tempValue = tempValue;
    }

    public boolean isModeAdd() {
        return this.modeAdd;
    }

    public void setModeAdd(boolean modeAdd) {
        this.modeAdd = modeAdd;
    }

    public boolean isModeAddPerson() {
        return this.modeAddPerson;
    }

    public void setModeAddPerson(boolean modeAddPerson) {
        this.modeAddPerson = modeAddPerson;
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

    public String[] getAllPagesSelection() {
        return this.allPagesSelection;
    }

    public void setAllPagesSelection(String[] allPagesSelection) {
        this.allPagesSelection = allPagesSelection;
    }

    public String[] getAllPages() {
        return this.allPages;
    }

    /**
     * Get structure site.
     *
     * @return SelectItem object
     */
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

    public Process getProcess() {
        return this.process;
    }

    public String getModeView() {
        return this.modeView;
    }

    public void setModeView(String modeView) {
        this.modeView = modeView;
    }

    public String getPaginationValue() {
        return this.paginationValue;
    }

    public void setPaginationValue(String paginationValue) {
        this.paginationValue = paginationValue;
    }

    public int getPaginationFromPageOrMark() {
        return this.paginationFromPageOrMark;
    }

    public void setPaginationFromPageOrMark(int paginationFromPageOrMark) {
        this.paginationFromPageOrMark = paginationFromPageOrMark;
    }

    public String getPaginationType() {
        return this.paginationType;
    }

    public void setPaginationType(String paginationType) {
        this.paginationType = paginationType;
    }

    public boolean isDisplayImage() {
        return this.displayImage;
    }

    /**
     * Show picture.
     */
    public void showImage() {
        this.displayImage = !this.displayImage;
        if (this.displayImage) {
            try {
                identifyImage(this.imageNumber);
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while generating image", e.getMessage());
                logger.error(e);
            }
        }
    }

    public String getAddFirstDocStructType() {
        return this.addFirstDocStructType;
    }

    public void setAddFirstDocStructType(String addFirstDocStructType) {
        this.addFirstDocStructType = addFirstDocStructType;
    }

    public String getAddSecondDocStructType() {
        return this.addSecondDocStructType;
    }

    public void setAddSecondDocStructType(String addSecondDocStructType) {
        this.addSecondDocStructType = addSecondDocStructType;
    }

    public String getImageNumberToGo() {
        return "";
    }

    public void setImageNumberToGo(String imageNumberToGo) {
        this.imageNumberToGo = imageNumberToGo;
    }

    public boolean isModeOnlyRead() {
        return this.modeOnlyRead;
    }

    public void setModeOnlyRead(boolean modeOnlyRead) {
        this.modeOnlyRead = modeOnlyRead;
    }

    public boolean isImageToStructuralElement() {
        return this.imageToStructuralElement;
    }

    public void setImageToStructuralElement(boolean imageToStructuralElement) {
        this.imageToStructuralElement = imageToStructuralElement;
    }

    /**
     * Get new element.
     *
     * @return String
     */
    public String getNeuesElementWohin() {
        if (this.neuesElementWohin == null || this.neuesElementWohin.isEmpty()) {
            this.neuesElementWohin = "1";
        }
        return this.neuesElementWohin;
    }

    /**
     * Set new element.
     *
     * @param inNeuesElementWohin
     *            String
     */
    public void setNeuesElementWohin(String inNeuesElementWohin) {
        if (inNeuesElementWohin == null || inNeuesElementWohin.equals("")) {
            this.neuesElementWohin = "1";
        } else {
            this.neuesElementWohin = inNeuesElementWohin;
        }
    }

    public String getAllPagesSelectionFirstPage() {
        return this.allPagesSelectionFirstPage;
    }

    public void setAllPagesSelectionFirstPagee(String allPagesSelectionFirstPage) {
        this.allPagesSelectionFirstPage = allPagesSelectionFirstPage;
    }

    public String getAllPagesSelectionLastPage() {
        return this.allPagesSelectionLastPage;
    }

    public void setAllPagesSelectionLastPage(String allPagesSelectionLastPage) {
        this.allPagesSelectionLastPage = allPagesSelectionLastPage;
    }

    /**
     * Get structure tree 3.
     *
     * @return list of HashMaps
     */
    public List<HashMap<String, Object>> getStrukturBaum3() {
        if (this.treeNodeStruct != null) {
            return this.treeNodeStruct.getChildrenAsList();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the current selected TreeNode.
     *
     * @return The TreeNode.
     */
    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    /**
     * Sets the selecetd TreeNode.
     *
     * @param selectedTreeNode
     *          The TreeNode.
     */
    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    /**
     * Sets MyStrukturelement on selection of TreeNode.
     *
     * @param event
     *          The NoteSelectEvent.
     */
    public void onNodeSelect(NodeSelectEvent event) {
        setMyStrukturelement((DocStruct) event.getTreeNode().getData());
    }

    /**
     * Gets logicalTopstruct of digital document as TreeNode structure.
     *
     * @return
     *          The TreeNote.
     */
    public TreeNode getTreeNodes() {
        TreeNode root = new DefaultTreeNode("root", null);
        List<DocStruct> children = this.logicalTopstruct.getAllChildren();
        TreeNode visibleRoot = new DefaultTreeNode(this.logicalTopstruct, root);
        if (children != null) {
            visibleRoot.getChildren().add(convertDocstructToPrimeFacesTreeNode(children, visibleRoot));
        }
        return setExpandingAll(root,true);
    }

    private TreeNode convertDocstructToPrimeFacesTreeNode(List<DocStruct> elements, TreeNode parentTreeNode) {
        TreeNode treeNode = null;

        for (DocStruct element : elements) {
            List<DocStruct> children = element.getAllChildren();
            treeNode = new DefaultTreeNode(element, parentTreeNode);
            if (children != null) {
                convertDocstructToPrimeFacesTreeNode(children, treeNode);
            }
        }
        return treeNode;
    }

    private TreeNode setExpandingAll(TreeNode node, boolean expanded) {
        for (TreeNode child : node.getChildren()) {
            setExpandingAll(child, expanded);
        }
        node.setExpanded(expanded);

        return node;
    }

    /**
     * Get all structure trees 3.
     *
     * @return list of HashMaps
     */
    public List<HashMap<String, Object>> getStrukturBaum3Alle() {
        if (this.treeNodeStruct != null) {
            return this.treeNodeStruct.getChildrenAsListAlle();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isModeMoveStructureElement() {
        return this.modeMoveStructureElement;
    }

    /**
     * Method.
     *
     * @param modeMoveStructureElement
     *            boolean
     */
    public void setModeMoveStructureElement(boolean modeMoveStructureElement) {
        this.modeMoveStructureElement = modeMoveStructureElement;

        // wenn der Verschiebevorgang gestartet werden soll, dann in allen
        // DocStructs prüfen
        // ob das aktuelle Strukturelement dort eingefügt werden darf
        if (this.modeMoveStructureElement) {
            runThroughTree(this.treeNodeStruct);
        }
    }

    @SuppressWarnings("rawtypes")
    private void runThroughTree(TreeNodeStruct3 inTreeStruct) {
        DocStruct temp = inTreeStruct.getStruct();
        if (inTreeStruct.getStruct() == this.docStruct) {
            inTreeStruct.setSelected(true);
        } else {
            inTreeStruct.setSelected(false);
        }

        // alle erlaubten Typen durchlaufen
        for (String allAllowedDocStructTypes : temp.getType().getAllAllowedDocStructTypes()) {
            if (this.docStruct.getType().getName().equals(allAllowedDocStructTypes)) {
                inTreeStruct.setEinfuegenErlaubt(true);
                break;
            }
        }

        for (de.sub.goobi.helper.TreeNode treeNode : inTreeStruct.getChildren()) {
            TreeNodeStruct3 kind = (TreeNodeStruct3) treeNode;
            runThroughTree(kind);
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

    public List<MetaPerson> getMetaPersonList() {
        return this.metaPersonList;
    }

    public void setMetaPersonList(List<MetaPerson> metaPersonList) {
        this.metaPersonList = metaPersonList;
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

    public boolean isTreeReloaded() {
        return this.treeReloaded;
    }

    public void setTreeReloaded(boolean treeReloaded) {
        this.treeReloaded = treeReloaded;
    }

    public HashMap<String, Boolean> getTreeProperties() {
        return this.treeProperties;
    }

    public void setTreeProperties(HashMap<String, Boolean> treeProperties) {
        this.treeProperties = treeProperties;
    }

    public String getOpacCatalog() {
        return this.opacCatalog;
    }

    public void setOpacCatalog(String opacCatalog) {
        this.opacCatalog = opacCatalog;
    }

    public String getOpacSearchField() {
        return this.opacSearchField;
    }

    public void setOpacSearchField(String opacSearchField) {
        this.opacSearchField = opacSearchField;
    }

    public int getPaginationPagesProImage() {
        return this.paginationPagesProImage;
    }

    public void setPaginationPagesProImage(int paginationPagesProImage) {
        this.paginationPagesProImage = paginationPagesProImage;
    }

    public URI getCurrentTifFolder() {
        return this.currentTifFolder;
    }

    public void setCurrentTifFolder(URI currentTifFolder) {
        this.currentTifFolder = currentTifFolder;
    }

    /**
     * Autocomplete.
     *
     * @param suggest
     *            Object
     * @return list of Strings
     */
    public List<String> autocomplete(Object suggest) {
        String pref = (String) suggest;
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> all = new ArrayList<>();
        for (String si : this.allPages) {
            all.add(si);
        }

        for (String element : all) {
            if (element != null && element.contains(pref) || "".equals(pref)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Get is not root element.
     *
     * @return boolean
     */
    public boolean getIsNotRootElement() {
        if (this.docStruct != null) {
            if (this.docStruct.getParent() == null) {
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

        firstpage.setImageName(otherFile);
        secondpage.setImageName(firstFile);
    }

    /**
     * Move selected pages up.
     */
    public void moveSeltectedPagesUp() {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
        List<String> pageNoList = Arrays.asList(allPagesSelection);
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
        List<String> newSelectionList = new ArrayList<>();
        for (Integer pageIndex : selectedPages) {
            DocStruct firstpage = allPages.get(pageIndex - 1);
            DocStruct secondpage = allPages.get(pageIndex);
            switchFileNames(firstpage, secondpage);
            newSelectionList.add(String.valueOf(pageIndex - 1));
        }

        allPagesSelection = newSelectionList.toArray(new String[newSelectionList.size()]);

        retrieveAllImages();
        identifyImage(1);
    }

    /**
     * Move selected pages down.
     */
    public void moveSelectedPagesDown() {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = Arrays.asList(allPagesSelection);
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            if (currentPhysicalPageNo + 1 == this.allPages.length) {
                break;
            }
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        List<String> newSelectionList = new ArrayList<>();
        for (Integer pageIndex : selectedPages) {
            DocStruct firstPage = allPages.get(pageIndex + 1);
            DocStruct secondPage = allPages.get(pageIndex);
            switchFileNames(firstPage, secondPage);
            newSelectionList.add(String.valueOf(pageIndex + 1));
        }

        allPagesSelection = newSelectionList.toArray(new String[newSelectionList.size()]);
        retrieveAllImages();
        identifyImage(1);
    }

    /**
     * Delete selected pages.
     */
    public void deleteSelectedPages() throws IOException {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = Arrays.asList(allPagesSelection);
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
            String imageName = pageToRemove.getImageName();

            removeImage(imageName);
            digitalDocument.getFileSet().removeFile(pageToRemove.getAllContentFiles().get(0));
            digitalDocument.getPhysicalDocStruct().removeChild(pageToRemove);
            List<Reference> refs = new ArrayList<>(pageToRemove.getAllFromReferences());
            for (ugh.dl.Reference ref : refs) {
                ref.getSource().removeReferenceTo(pageToRemove);
            }

        }

        allPagesSelection = null;
        if (digitalDocument.getPhysicalDocStruct().getAllChildren() != null) {
            lastImage = digitalDocument.getPhysicalDocStruct().getAllChildren().size();
        } else {
            lastImage = 0;
        }

        allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();

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
        if (selectedPages.contains(imageNumber - 1)) {

            imageShowFirstPage();
        } else {
            identifyImage(1);
        }
    }

    /**
     * Reorder pagination.
     */
    public void reOrderPagination() throws IOException {
        URI imageDirectory;
        imageDirectory = fileService.getImagesDirectory(process);
        if (imageDirectory.equals("")) {
            Helper.setFehlerMeldung("ErrorMetsEditorImageRenaming");
            return;
        }

        List<URI> oldfilenames = new ArrayList<>();
        for (DocStruct page : digitalDocument.getPhysicalDocStruct().getAllChildren()) {
            oldfilenames.add(URI.create(page.getImageName()));
        }

        for (URI imagename : oldfilenames) {
            for (URI folder : allTifFolders) {
                URI filename = imageDirectory.resolve(folder).resolve(imagename);
                String newFileName = filename + "_bak";
                fileService.renameFile(filename, newFileName);
            }
            URI ocrFolder = fileService.getProcessSubTypeURI(process, ProcessSubType.OCR, null);
            if (fileService.fileExist(ocrFolder)) {
                ArrayList<URI> allOcrFolder = fileService.getSubUris(ocrFolder);
                for (URI folder : allOcrFolder) {
                    URI filename = folder.resolve(imagename);
                    String newFileName = filename + "_bak";
                    fileService.renameFile(filename, newFileName);
                }
            }

            int counter = 1;
            for (URI oldImagename : oldfilenames) {
                String newfilenamePrefix = generateFileName(counter);
                for (URI folder : allTifFolders) {
                    URI fileToSort = imageDirectory.resolve(folder).resolve(oldImagename);
                    String fileExtension = Metadaten
                            .getFileExtension(fileService.getFileName(fileToSort).replace("_bak", ""));
                    URI tempFileName = imageDirectory.resolve(folder)
                            .resolve(fileService.getFileName(fileToSort) + "_bak");
                    String sortedName = newfilenamePrefix + fileExtension.toLowerCase();
                    fileService.renameFile(tempFileName, sortedName);
                    digitalDocument.getPhysicalDocStruct().getAllChildren().get(counter - 1).setImageName(sortedName);
                }
                try {
                    URI ocr = fileService.getProcessSubTypeURI(process, ProcessSubType.OCR, null);
                    if (fileService.fileExist(ocr)) {
                        ArrayList<URI> allOcrFolder = fileService.getSubUris(ocr);
                        for (URI folder : allOcrFolder) {
                            URI fileToSort = folder.resolve(imagename);
                            String fileExtension = Metadaten
                                    .getFileExtension(fileService.getFileName(fileToSort).replace("_bak", ""));
                            URI tempFileName = fileToSort.resolve("_bak");
                            String sortedName = newfilenamePrefix + fileExtension.toLowerCase();
                            fileService.renameFile(tempFileName, sortedName);
                        }
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
                counter++;
            }
            retrieveAllImages();

            identifyImage(1);
        }
    }

    private void removeImage(String fileToDelete) throws IOException {
        // TODO check what happens with .tar.gz
        String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf("."));
        for (URI folder : allTifFolders) {
            ArrayList<URI> filesInFolder = fileService
                    .getSubUris(fileService.getImagesDirectory(process).resolve(folder));
            for (URI currentFile : filesInFolder) {
                String filename = fileService.getFileName(currentFile);
                String filenamePrefix = filename.replace(getFileExtension(filename), "");
                if (filenamePrefix.equals(fileToDeletePrefix)) {
                    fileService.delete(currentFile);
                }
            }
        }

        URI ocr = serviceManager.getFileService().getOcrDirectory(process);
        if (fileService.fileExist(ocr)) {
            ArrayList<URI> folder = fileService.getSubUris(ocr);
            for (URI dir : folder) {
                if (fileService.isDirectory(dir) && fileService.getSubUris(dir).size() > 0) {
                    ArrayList<URI> filesInFolder = fileService.getSubUris(dir);
                    for (URI currentFile : filesInFolder) {
                        String filename = fileService.getFileName(currentFile);
                        String filenamePrefix = filename.substring(0, filename.lastIndexOf("."));
                        if (filenamePrefix.equals(fileToDeletePrefix)) {
                            fileService.delete(currentFile);
                        }
                    }
                }
            }
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

    /**
     * Get file manipulation.
     *
     * @return FileManipulation object
     */
    public FileManipulation getFileManipulation() {
        if (fileManipulation == null) {
            fileManipulation = new FileManipulation(this);
        }
        return fileManipulation;
    }

    public void setFileManipulation(FileManipulation fileManipulation) {
        this.fileManipulation = fileManipulation;
    }

    public DigitalDocument getDigitalDocument() {
        return digitalDocument;
    }

    public void setDigitalDocument(DigitalDocument digitalDocument) {
        this.digitalDocument = digitalDocument;
    }

    /**
     * Get file extension.
     *
     * @param filename
     *            String
     * @return String
     */
    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public Boolean getDisplayFileManipulation() {
        return ConfigCore.getBooleanParameter("MetsEditorDisplayFileManipulation", false);
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
            docStruct.addMetadataGroup(newMetadataGroup.toMetadataGroup());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        return showMetadata();
    }

    /**
     * Checks whether a given meta-data group type is available for adding. This
     * can be used by a RenderableMetadataGroup to getById out whether it can be
     * copied or not.
     *
     * @param type
     *            meta-data group type to look for
     * @return whether the type is available to add
     */
    boolean canCreate(MetadataGroupType type) {
        List<MetadataGroupType> addableTypes = docStruct.getAddableMetadataGroupTypes();
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
        List<MetadataGroup> records = docStruct.getAllMetadataGroups();
        if (records == null) {
            return Collections.emptyList();
        }
        List<RenderableMetadataGroup> result = new ArrayList<>(records.size());
        String language = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}");
        String projectName = process.getProject().getTitle();
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
        String language = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}");
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
        return docStruct.getAddableMetadataGroupTypes() != null;
    }

    /**
     * Deletes the metadata group
     *
     * @param metadataGroup
     *            metadata group to delete.
     */
    void removeMetadataGroupFromCurrentDocStruct(MetadataGroup metadataGroup) {
        docStruct.removeMetadataGroup(metadataGroup);
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
        newMetadataGroup = new RenderableMetadataGroup(master, docStruct.getAddableMetadataGroupTypes());
        modeAdd = false;
        modeAddPerson = false;
        addMetadataGroupMode = true;
        return !updateBlocked() ? "SperrungAbgelaufen" : "";
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
            newMetadataGroup = new RenderableMetadataGroup(docStruct.getAddableMetadataGroupTypes(),
                    process.getProject().getTitle());
        } catch (ConfigurationException e) {
            Helper.setFehlerMeldung("Form_configuration_mismatch", e.getMessage());
            logger.error(e.getMessage());
            return "";
        }
        modeAdd = false;
        modeAddPerson = false;
        addMetadataGroupMode = true;
        return !updateBlocked() ? "SperrungAbgelaufen" : "";
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
        modeAdd = false;
        modeAddPerson = false;
        addMetadataGroupMode = false;
        newMetadataGroup = null;
        return !updateBlocked() ? "SperrungAbgelaufen" : "";
    }

    /**
     * Returns the ID string of the currently selected separator.
     *
     * @return the ID of the selected separator
     */
    public String getPaginationSeparator() {
        return paginationSeparators.getSelected();
    }

    /**
     * Sets the currently selected separator by its ID.
     *
     * @param selected
     *            the ID of the separator to select
     */
    public void setPaginationSeparator(String selected) {
        paginationSeparators.setSelected(selected);
    }

    /**
     * Returns the List of separators the user can select from. Each element of
     * the list must be an object that implements the two functions
     * {@code String getId()} and {@code String getLabel()}.
     *
     * @return the List of separators
     */
    public Collection<Separator> getPaginationSeparators() {
        return paginationSeparators.getItems();
    }
}
