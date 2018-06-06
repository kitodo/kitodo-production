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
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;

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
import java.util.Map;
import java.util.Objects;
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
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.goobi.production.constants.Parameters;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.CataloguePlugin.QueryBuilder;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.IsDirectoryFilter;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.IncompletePersonObjectException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.UGHException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.PositionOfNewDocStrucElement;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
public class Metadaten {
    private static final Logger logger = LogManager.getLogger(Metadaten.class);
    private MetadatenImagesHelper imageHelper;
    private MetadatenHelper metaHelper;
    private boolean treeReloaded = false;
    private String ocrResult = "";
    private FileformatInterface gdzfile;
    private DocStructInterface docStruct;
    private DocStructInterface tempStrukturelement;
    private List<MetadatumImpl> myMetadaten = new LinkedList<>();
    private List<MetaPerson> metaPersonList = new LinkedList<>();
    private MetadatumImpl curMetadatum;
    private MetaPerson curPerson;
    private DigitalDocumentInterface digitalDocument;
    private Process process;
    private PrefsInterface myPrefs;
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
    private DocStructInterface logicalTopstruct;
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
    private Map<String, Boolean> treeProperties;
    private final ReentrantLock xmlReadingLock = new ReentrantLock();
    private FileManipulation fileManipulation = null;
    private boolean addMetadataGroupMode = false;
    private RenderableMetadataGroup newMetadataGroup;
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();
    private Paginator paginator = new Paginator();
    private TreeNode selectedTreeNode;
    private PositionOfNewDocStrucElement positionOfNewDocStrucElement = PositionOfNewDocStrucElement.AFTER_CURRENT_ELEMENT;
    private int metadataElementsToAdd = 1;
    private String addMetaDataType;
    private String addMetaDataValue;
    private boolean addServeralStructuralElementsMode = false;
    private static final String BLOCK_EXPIRED = "SperrungAbgelaufen";

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
            return BLOCK_EXPIRED;
        }
        return "";
    }

    /**
     * Add.
     */
    public void add() {
        this.modeAdd = true;
        Modes.setBindState(BindState.CREATE);
        getMetadatum().setValue("");
    }

    /**
     * Add person.
     */
    public void addPerson() {
        this.modeAddPerson = true;
        this.tempPersonNachname = "";
        this.tempPersonRecord = ConfigCore.getParameter(Parameters.AUTHORITY_DEFAULT, "");
        this.tempPersonVorname = "";
    }

    /**
     * cancel.
     */
    public void cancel() {
        this.modeAdd = false;
        this.modeAddPerson = false;
        Modes.setBindState(BindState.EDIT);
        getMetadatum().setValue("");
    }

    /**
     * Save metadata to Xml file.
     */
    public void saveMetadataToXml() {
        calculateMetadataAndImages();
        cleanupMetadata();
        if (storeMetadata()) {
            Helper.setMessage("XML saved");
        }
    }

    /**
     * Save metadata to Xml file.
     */
    public String saveMetadataToXmlAndGoToProcessPage() {
        calculateMetadataAndImages();
        cleanupMetadata();
        if (storeMetadata()) {
            return "/pages/processes?faces-redirect=true";
        } else {
            Helper.setMessage("XML could not be saved");
            return "";
        }
    }

    /**
     * Copy.
     *
     */
    public void copy() {
        MetadataInterface md;
        try {
            md = UghImplementation.INSTANCE.createMetadata(this.curMetadatum.getMd().getMetadataType());

            md.setStringValue(this.curMetadatum.getMd().getValue());
            this.docStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setErrorMessage(e.getMessage());
            logger.error("Error at Metadata copy (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Copy person.
     *
     */
    public void copyPerson() {
        PersonInterface per;
        try {
            per = UghImplementation.INSTANCE
                    .createPerson(this.myPrefs.getMetadataTypeByName(this.curPerson.getP().getRole()));
            per.setFirstName(this.curPerson.getP().getFirstName());
            per.setLastName(this.curPerson.getP().getLastName());
            per.setRole(this.curPerson.getP().getRole());

            this.docStruct.addPerson(per);
        } catch (IncompletePersonObjectException e) {
            logger.error("Fehler beim copy von Personen (IncompletePersonObjectException): " + e.getMessage());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim copy von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Change current document structure.
     *
     */
    public void changeCurrentDocstructType() {

        if (this.docStruct != null && this.tempTyp != null) {
            try {
                DocStructInterface result = this.metaHelper.changeCurrentDocstructType(this.docStruct, this.tempTyp);
                saveMetadataAsBean(result);
                readMetadataAsFirstTree();
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException | TypeNotAllowedAsChildException e) {
                Helper.setErrorMessage("Error while changing DocStructTypes (" + e.getClass().getSimpleName() + "): ",
                    logger, e);
            }
        }
    }

    /**
     * Save.
     */
    public void save() {
        try {
            MetadataInterface md = UghImplementation.INSTANCE
                    .createMetadata(this.myPrefs.getMetadataTypeByName(this.tempTyp));
            md.setStringValue(this.selectedMetadatum.getValue());

            this.docStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        // if TitleDocMain, then create equal sort titles with the same content
        if (this.tempTyp.equals("TitleDocMain") && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
            try {
                MetadataInterface secondMetadata = UghImplementation.INSTANCE
                        .createMetadata(this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                secondMetadata.setStringValue(this.selectedMetadatum.getValue());
                this.docStruct.addMetadata(secondMetadata);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }

        this.modeAdd = false;
        Modes.setBindState(BindState.EDIT);
        this.selectedMetadatum.setValue("");
        this.tempValue = "";
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Save person.
     */
    public void savePerson() {
        try {
            PersonInterface per = UghImplementation.INSTANCE
                    .createPerson(this.myPrefs.getMetadataTypeByName(this.tempPersonRolle));
            per.setFirstName(this.tempPersonVorname);
            per.setLastName(this.tempPersonNachname);
            per.setRole(this.tempPersonRolle);
            String[] authorityFile = parseAuthorityFileArgs(tempPersonRecord);
            per.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
            this.docStruct.addPerson(per);
            this.modeAddPerson = false;
            saveMetadataAsBean(this.docStruct);
        } catch (IncompletePersonObjectException e) {
            Helper.setErrorMessage("Incomplete data for person", logger, e);
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setErrorMessage("Person is for this structure not allowed", logger, e);
        }
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
                throw new IncompletePersonObjectException("uriMalformed");
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
     */
    public void deletePerson() {
        this.docStruct.removePerson(this.curPerson.getP());
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Get allowed roles.
     *
     * @return list of allowed roles as SelectItems
     */
    public List<SelectItem> getAddableRollen() {
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
     * Gets addeble metadata types.
     *
     * @return The metadata types.
     */
    public List<SelectItem> getAddableMetadataTypes() {
        return getAddableMetadataTypes(docStruct, tempMetadatumList);
    }

    /**
     * Gets addable metadata types from tempTyp.
     *
     * @return The addable metadata types from tempTyp.
     */
    public List<SelectItem> getAddableMetadataTypesFromTempType() {
        DocStructTypeInterface dst = this.myPrefs.getDocStrctTypeByName(this.tempTyp);
        DocStructInterface ds = this.digitalDocument.createDocStruct(dst);

        return getAddableMetadataTypes(ds, this.tempMetadatumList);
    }

    private ArrayList<SelectItem> getAddableMetadataTypes(DocStructInterface myDocStruct,
            ArrayList<MetadatumImpl> tempMetadatumList) {
        ArrayList<SelectItem> selectItems = new ArrayList<>();

        // determine all addable metadata types
        List<MetadataTypeInterface> types = myDocStruct.getAddableMetadataTypes();
        if (types == null) {
            return selectItems;
        }

        // alle Metadatentypen, die keine Person sind, oder mit einem
        // Unterstrich anfangen rausnehmen
        for (MetadataTypeInterface mdt : new ArrayList<>(types)) {
            if (mdt.isPerson()) {
                types.remove(mdt);
            }
        }

        // sort the metadata types
        HelperComparator c = new HelperComparator();
        c.setSortType("MetadatenTypen");
        Collections.sort(types, c);

        int counter = types.size();

        for (MetadataTypeInterface mdt : types) {
            selectItems.add(new SelectItem(mdt.getName(), this.metaHelper.getMetadatatypeLanguage(mdt)));
            try {
                MetadataInterface md = UghImplementation.INSTANCE.createMetadata(mdt);
                MetadatumImpl mdum = new MetadatumImpl(md, counter, this.myPrefs, this.process);
                counter++;
                if (tempMetadatumList != null) {
                    tempMetadatumList.add(mdum);
                }

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        return selectItems;
    }

    /**
     * die MetadatenTypen zurückgeben.
     */
    public SelectItem[] getMetadatenTypen() {
        /*
         * zuerst mal die addierbaren Metadatentypen ermitteln
         */
        List<MetadataTypeInterface> types = this.docStruct.getAddableMetadataTypes();

        if (types == null) {
            return new SelectItem[0];
        }

        // sort the metadata types
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
        for (MetadataTypeInterface mdt : types) {
            myTypen[zaehler] = new SelectItem(mdt.getName(), this.metaHelper.getMetadatatypeLanguage(mdt));
            zaehler++;
        }

        /*
         * alle Typen, die einen Unterstrich haben nochmal rausschmeissen
         */
        SelectItem[] typesWithoutUnderscore = new SelectItem[zaehler];
        System.arraycopy(myTypen, 0, typesWithoutUnderscore, 0, zaehler);
        return typesWithoutUnderscore;
    }

    /**
     * Metadaten Einlesen.
     *
     */
    public String readXml() {
        String redirect = "";
        if (xmlReadingLock.tryLock()) {
            try {
                readXmlAndBuildTree();
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            } finally {
                xmlReadingLock.unlock();
            }
        } else {
            Helper.setErrorMessage("metadataEditorThreadLock");
            return redirect;
        }
        redirect = "/pages/metadataEditor?faces-redirect=true";
        return redirect;
    }

    private void readXmlAndBuildTree() {
        // re-reading the config for display rules
        ConfigDisplayRules.getInstance().refresh();

        Modes.setBindState(BindState.EDIT);
        try {
            Integer id = Integer.valueOf(Helper.getRequestParameter("ProzesseID"));
            this.process = serviceManager.getProcessService().getById(id);
        } catch (NumberFormatException | DAOException e) {
            Helper.setErrorMessage("error while loading process data" + e.getMessage(), logger, e);
        }
        this.userId = Helper.getRequestParameter("BenutzerID");
        this.allPagesSelectionFirstPage = "";
        this.allPagesSelectionLastPage = "";
        this.result = Helper.getRequestParameter("zurueck");
        this.neuesElementWohin = "1";
        this.treeNodeStruct = null;
        try {
            readXmlStart();
        } catch (ReadException e) {
            Helper.setErrorMessage(e.getMessage(), logger, e);
        } catch (PreferencesException | IOException e) {
            Helper.setErrorMessage("error while loading metadata" + e.getMessage(), logger, e);
        }

        expandTree();
        this.sperrung.setLocked(this.process.getId(), this.userId);
    }

    /**
     * Read metadata.
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
            throw new ReadException(Helper.getTranslation("metadataError"));
        }

        identifyImage(1);
        retrieveAllImages();
        if (ConfigCore.getBooleanParameter(Parameters.WITH_AUTOMATIC_PAGINATION, true)
                && (this.digitalDocument.getPhysicalDocStruct() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren().isEmpty())) {
            createPagination();
        }

        List<MetadataInterface> allMetadata = this.digitalDocument.getPhysicalDocStruct().getAllMetadata();
        if (Objects.nonNull(allMetadata)) {
            for (MetadataInterface md : allMetadata) {
                if (md.getMetadataType().getName().equals("_representative")) {
                    try {
                        Integer value = Integer.valueOf(md.getValue());
                        currentRepresentativePage = String.valueOf(value - 1);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        createDefaultValues(this.logicalTopstruct);
        saveMetadataAsBean(this.logicalTopstruct);
        readMetadataAsFirstTree();

    }

    private void createDefaultValues(DocStructInterface element) {
        if (ConfigCore.getBooleanParameter("MetsEditorEnableDefaultInitialisation", true)) {
            saveMetadataAsBean(element);
            List allChildren = element.getAllChildren();
            if (Objects.nonNull(allChildren)) {
                for (DocStructInterface ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }

    private void calculateMetadataAndImages() {
        // go again through the metadata for the process and save the data
        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        this.process
                .setSortHelperDocstructs(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.DOCSTRUCT));
        this.process.setSortHelperMetadata(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.METADATA));
        try {
            this.process.setSortHelperImages(fileService
                    .getNumberOfFiles(serviceManager.getProcessService().getImagesOrigDirectory(true, this.process)));
            serviceManager.getProcessService().save(this.process);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("process") }, logger, e);
        } catch (IOException e) {
            Helper.setErrorMessage("error while counting current images", logger, e);
        }
    }

    private void cleanupMetadata() {
        // before save remove all unused docstructs
        this.metaHelper.deleteAllUnusedElements(this.digitalDocument.getLogicalDocStruct());

        if (currentRepresentativePage != null && currentRepresentativePage.length() > 0) {
            boolean match = false;
            DocStructInterface physicalDocStruct = this.digitalDocument.getPhysicalDocStruct();
            if (Objects.nonNull(physicalDocStruct) && Objects.nonNull(physicalDocStruct.getAllMetadata())) {
                for (MetadataInterface md : this.digitalDocument.getPhysicalDocStruct().getAllMetadata()) {
                    if (md.getMetadataType().getName().equals("_representative")) {
                        Integer value = Integer.valueOf(currentRepresentativePage);
                        md.setStringValue(String.valueOf(value + 1));
                        match = true;
                    }
                }
            }
            if (!match) {
                MetadataTypeInterface mdt = myPrefs.getMetadataTypeByName("_representative");
                addMetadataToPhysicalDocStruct(mdt);
            }
        }
    }

    private void addMetadataToPhysicalDocStruct(MetadataTypeInterface mdt) {
        try {
            MetadataInterface md = UghImplementation.INSTANCE.createMetadata(mdt);
            Integer value = Integer.valueOf(currentRepresentativePage);
            md.setStringValue(String.valueOf(value + 1));
            this.digitalDocument.getPhysicalDocStruct().addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean storeMetadata() {
        boolean result = true;
        try {
            fileService.writeMetadataFile(this.gdzfile, this.process);
        } catch (PreferencesException | WriteException | IOException | RuntimeException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("metadata") }, logger, e);
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
        storeMetadata();
        disableReturn();
        return this.result;
    }

    /**
     * Check for representative.
     *
     * @return boolean
     */
    public boolean isCheckForRepresentative() {
        MetadataTypeInterface mdt = myPrefs.getMetadataTypeByName("_representative");

        return mdt != null;
    }

    /**
     * vom aktuellen Strukturelement alle Metadaten einlesen.
     *
     * @param inStrukturelement
     *            DocStruct object
     */
    private void saveMetadataAsBean(DocStructInterface inStrukturelement) {
        this.docStruct = inStrukturelement;
        LinkedList<MetadatumImpl> lsMeta = new LinkedList<>();
        LinkedList<MetaPerson> lsPers = new LinkedList<>();

        /*
         * alle Metadaten und die DefaultDisplay-Werte anzeigen
         */
        List<? extends MetadataInterface> tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(
            inStrukturelement, serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage(), false, this.process);
        if (tempMetadata != null) {
            for (MetadataInterface metadata : tempMetadata) {
                MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.process);
                meta.getSelectedItem();
                lsMeta.add(meta);
            }
        }

        /*
         * alle Personen und die DefaultDisplay-Werte ermitteln
         */
        tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(inStrukturelement,
            serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage(), true, this.process);
        if (tempMetadata != null) {
            for (MetadataInterface metadata : tempMetadata) {
                lsPers.add(new MetaPerson((PersonInterface) metadata, 0, this.myPrefs, inStrukturelement));
            }
        }

        this.myMetadaten = lsMeta;
        this.metaPersonList = lsPers;

        determinePagesStructure(this.docStruct);
    }

    /*
     * Treeview
     */

    @SuppressWarnings("rawtypes")
    private void readMetadataAsFirstTree() {
        HashMap map;
        TreeNodeStruct3 nodes;
        List<DocStructInterface> status = new ArrayList<>();

        // capture the pop-up state of all nodes
        if (this.treeNodeStruct != null) {
            for (HashMap childrenList : this.treeNodeStruct.getChildrenAsList()) {
                map = childrenList;
                nodes = (TreeNodeStruct3) map.get("node");
                if (nodes.isExpanded()) {
                    status.add(nodes.getStruct());
                }
            }
        }

        /*
         * Die Struktur als Tree3 aufbereiten
         */
        String label = this.logicalTopstruct.getDocStructType()
                .getNameByLanguage(serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage());
        if (label == null) {
            label = this.logicalTopstruct.getDocStructType().getName();
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
    private void readMetadataAsSecondTree(DocStructInterface inStrukturelement, TreeNodeStruct3 upperNode) {
        upperNode.setMainTitle(determineMetadata(inStrukturelement, "TitleDocMain"));
        upperNode.setZblNummer(determineMetadata(inStrukturelement, "ZBLIdentifier"));
        upperNode.setZblSeiten(determineMetadata(inStrukturelement, "ZBLPageNumber"));
        upperNode.setPpnDigital(determineMetadata(inStrukturelement, "IdentifierDigital"));
        upperNode
                .setFirstImage(this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberFirst()));
        upperNode.setLastImage(this.metaHelper.getImageNumber(inStrukturelement, MetadatenHelper.getPageNumberLast()));
        // wenn es ein Heft ist, die Issue-Number mit anzeigen
        if (inStrukturelement.getDocStructType().getName().equals("PeriodicalIssue")) {
            upperNode.setDescription(
                upperNode.getDescription() + " " + determineMetadata(inStrukturelement, "CurrentNo"));
        }

        // wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
        if (inStrukturelement.getDocStructType().getName().equals("Periodical")
                || inStrukturelement.getDocStructType().getName().equals("PeriodicalVolume")) {
            upperNode.setExpanded(true);
        }

        // vom aktuellen Strukturelement alle Kinder in den Tree packen
        List<DocStructInterface> children = inStrukturelement.getAllChildren();
        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        if (children != null) {
            // es gibt Kinder-Strukturelemente
            for (DocStructInterface child : children) {
                String label = child.getDocStructType().getNameByLanguage(language);
                if (label == null) {
                    label = child.getDocStructType().getName();
                }
                TreeNodeStruct3 tns = new TreeNodeStruct3(label, child);
                upperNode.addChild(tns);
                readMetadataAsSecondTree(child, tns);
            }
        }
    }

    /**
     * Metadaten gezielt zurückgeben.
     *
     * @param inStrukturelement
     *            DocStruct object
     * @param type
     *            String
     */
    private String determineMetadata(DocStructInterface inStrukturelement, String type) {
        StringBuilder result = new StringBuilder();
        List<MetadataInterface> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (MetadataInterface md : allMDs) {
                if (md.getMetadataType().getName().equals(type)) {
                    result.append(md.getValue() == null ? "" : md.getValue());
                    result.append(" ");
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Gets metadata value of specific type of an DocStruct element.
     *
     * @param docStructElement
     *            The DocStruct element.
     * @param type
     *            The metadata typ.
     * @return The metadata value.
     */
    public String getMetadataByElementAndType(DocStructInterface docStructElement, String type) {
        return determineMetadata(docStructElement, type);
    }

    /**
     * Gets the image range of a specific DocStruct element.
     *
     * @param docStructElement
     *            The DocStruct element.
     * @return The image range (image number - page namber)
     */
    public String getImageRangeByElement(DocStructInterface docStructElement) {
        String firstImage = this.metaHelper.getImageNumber(docStructElement, MetadatenHelper.getPageNumberFirst());
        String lastImage = this.metaHelper.getImageNumber(docStructElement, MetadatenHelper.getPageNumberLast());

        return firstImage + " - " + lastImage;
    }

    /**
     * Set my structure element.
     *
     * @param inStruct
     *            DocStruct
     */
    @SuppressWarnings("rawtypes")
    public void setMyStrukturelement(DocStructInterface inStruct) {
        this.modeAdd = false;
        this.modeAddPerson = false;
        Modes.setBindState(BindState.EDIT);
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
            this.metaHelper.moveNodeUp(this.docStruct);
        } catch (TypeNotAllowedAsChildException e) {
            logger.debug("Fehler beim Verschieben des Knotens: {}", e.getMessage());
        }
        readMetadataAsFirstTree();
    }

    /**
     * Knoten nach unten schieben.
     */
    public void nodeDown() {
        try {
            this.metaHelper.moveNodeDown(this.docStruct);
        } catch (TypeNotAllowedAsChildException e) {
            logger.debug("Fehler beim Verschieben des Knotens: {}", e.getMessage());
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
            DocStructInterface tempParent = this.docStruct.getParent();
            this.docStruct.getParent().removeChild(this.docStruct);
            this.docStruct = tempParent;
        }
        // den Tree neu einlesen
        readMetadataAsFirstTree();
    }

    /**
     * Gets position of new inserted DocStruc elements.
     *
     * @return The position of new inserted DocStruc elements.
     */
    public PositionOfNewDocStrucElement getPositionOfNewDocStrucElement() {
        return this.positionOfNewDocStrucElement;
    }

    /**
     * Sets position of new inserted DocStruc elements.
     *
     * @param positionOfNewDocStrucElement
     *            The position of new inserted DocStruc elements.
     */
    public void setPositionOfNewDocStrucElement(PositionOfNewDocStrucElement positionOfNewDocStrucElement) {
        this.positionOfNewDocStrucElement = positionOfNewDocStrucElement;
    }

    /**
     * Gets all possible positions of new DocStruct elements.
     *
     * @return The positions of new DocStruct elements.
     */
    public PositionOfNewDocStrucElement[] getPositionsOfNewDocStrucElement() {
        return PositionOfNewDocStrucElement.values();
    }

    /**
     * Adds a single new DocStruct element to the current DocStruct tree and
     * sets the specified pages.
     */
    public void addSingleNodeWithPages() {

        DocStructInterface docStruct = null;
        DocStructTypeInterface docStructType = this.myPrefs.getDocStrctTypeByName(this.tempTyp);

        try {
            docStruct = addNode(this.docStruct, this.digitalDocument, docStructType, this.positionOfNewDocStrucElement,
                1, null, null);

        } catch (UGHException e) {
            logger.error(e.getMessage());
        }

        if (!this.pagesStart.equals("") && !this.pagesEnd.equals("")) {
            DocStructInterface temp = this.docStruct;
            this.docStruct = docStruct;
            this.ajaxPageStart = this.pagesStart;
            this.ajaxPageEnd = this.pagesEnd;
            ajaxSeitenStartUndEndeSetzen();
            this.docStruct = temp;
        }
        readMetadataAsFirstTree();
    }

    /**
     * Adds a several new DocStruct elements to the current DocStruct tree and
     * sets specified metadata.
     */
    public void addSeveralNodesWithMetadata() {
        DocStructInterface ds;

        DocStructTypeInterface docStructType = this.myPrefs.getDocStrctTypeByName(this.tempTyp);
        try {
            ds = addNode(this.docStruct, this.digitalDocument, docStructType, this.positionOfNewDocStrucElement,
                this.metadataElementsToAdd, this.addMetaDataType, this.addMetaDataValue);
        } catch (UGHException e) {
            logger.error(e.getMessage());
        }
        readMetadataAsFirstTree();
    }

    private void addNewDocStructToExistingDocStruct(DocStructInterface existingDocStruct,
            DocStructInterface newDocStruct, int index) throws TypeNotAllowedAsChildException {

        if (existingDocStruct.isDocStructTypeAllowedAsChild(newDocStruct.getDocStructType())) {
            existingDocStruct.addChild(index, newDocStruct);
        } else {
            throw new TypeNotAllowedAsChildException(newDocStruct.getDocStructType() + " ot allowed as child of "
                    + existingDocStruct.getDocStructType());
        }
    }

    private DocStructInterface addNode(DocStructInterface docStruct, DigitalDocumentInterface digitalDocument,
            DocStructTypeInterface docStructType, PositionOfNewDocStrucElement positionOfNewDocStrucElement,
            int quantity, String metadataType, String value)
            throws MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

        ArrayList<DocStructInterface> createdElements = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {

            DocStructInterface createdElement = digitalDocument.createDocStruct(docStructType);
            if (docStructType != null && value != null && metadataType != null) {
                createdElement.addMetadata(metadataType, value);
            }
            createdElements.add(createdElement);
        }

        if (positionOfNewDocStrucElement.equals(PositionOfNewDocStrucElement.LAST_CHILD_OF_CURRENT_ELEMENT)) {
            for (DocStructInterface element : createdElements) {
                docStruct.addChild(element);
            }
        } else {
            DocStructInterface edited = positionOfNewDocStrucElement.equals(
                PositionOfNewDocStrucElement.FIRST_CHILD_OF_CURRENT_ELEMENT) ? docStruct : docStruct.getParent();
            if (edited == null) {
                logger.debug("The selected element cannot investigate the father.");
            } else {
                List<DocStructInterface> childrenBefore = edited.getAllChildren();
                if (childrenBefore == null) {
                    for (DocStructInterface element : createdElements) {
                        edited.addChild(element);
                    }
                } else {
                    // Build a new list of children for the edited element
                    List<DocStructInterface> newChildren = new ArrayList<>(childrenBefore.size() + 1);
                    if (positionOfNewDocStrucElement
                            .equals(PositionOfNewDocStrucElement.FIRST_CHILD_OF_CURRENT_ELEMENT)) {
                        newChildren.addAll(createdElements);
                    }
                    for (DocStructInterface child : childrenBefore) {
                        if (child == docStruct && positionOfNewDocStrucElement
                                .equals(PositionOfNewDocStrucElement.BEFOR_CURRENT_ELEMENT)) {
                            newChildren.addAll(createdElements);
                        }
                        newChildren.add(child);
                        if (child == docStruct && positionOfNewDocStrucElement
                                .equals(PositionOfNewDocStrucElement.AFTER_CURRENT_ELEMENT)) {
                            newChildren.addAll(createdElements);
                        }
                    }

                    // Remove the existing children
                    for (DocStructInterface child : newChildren) {
                        edited.removeChild(child);
                    }

                    // Set the new children on the edited element
                    for (DocStructInterface child : newChildren) {
                        edited.addChild(child);
                    }
                }
            }
        }
        return createdElements.iterator().next();
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

    /**
     * Returns possible DocStruct types which can be added to current DocStruct.
     *
     * @return The DocStruct types.
     */
    public SelectItem[] getAddableDocStructTypes() {
        switch (positionOfNewDocStrucElement) {
            case BEFOR_CURRENT_ELEMENT:
            case AFTER_CURRENT_ELEMENT:
                return this.metaHelper.getAddableDocStructTypen(this.docStruct, true);

            case FIRST_CHILD_OF_CURRENT_ELEMENT:
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                return this.metaHelper.getAddableDocStructTypen(this.docStruct, false);

            default:
                logger.error("Invalid positionOfNewDocStrucElement");
                return new SelectItem[0];
        }
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images.
     */
    public String createPagination() throws IOException {
        this.imageHelper.createPagination(this.process, this.currentTifFolder);
        retrieveAllImages();

        // added new
        DocStructInterface log = this.digitalDocument.getLogicalDocStruct();
        while (log.getDocStructType().getAnchorClass() != null && log.getAllChildren() != null
                && !log.getAllChildren().isEmpty()) {
            log = log.getAllChildren().get(0);
        }
        if (log.getDocStructType().getAnchorClass() != null) {
            return "";
        }

        if (log.getAllChildren() != null) {
            for (DocStructInterface child : log.getAllChildren()) {
                List<ReferenceInterface> childRefs = child.getAllReferences("to");
                for (ReferenceInterface toAdd : childRefs) {
                    boolean match = false;
                    for (ReferenceInterface ref : log.getAllReferences("to")) {
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
        DigitalDocumentInterface document;
        try {
            document = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMessage(null, "Can not get DigitalDocument: ", e.getMessage());
            return;
        }

        List<DocStructInterface> meineListe = document.getPhysicalDocStruct().getAllChildren();
        if (meineListe == null) {
            this.allPages = null;
            return;
        }
        int zaehler = meineListe.size();
        this.allPages = new String[zaehler];
        this.allPagesNew = new MetadatumImpl[zaehler];
        zaehler = 0;
        MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        for (DocStructInterface mySeitenDocStruct : meineListe) {
            List<? extends MetadataInterface> mySeitenDocStructMetadaten = mySeitenDocStruct.getAllMetadataByType(mdt);
            for (MetadataInterface page : mySeitenDocStructMetadaten) {
                this.allPagesNew[zaehler] = new MetadatumImpl(page, zaehler, this.myPrefs, this.process);
                this.allPages[zaehler] = determineMetadata(page.getDocStruct(), "physPageNumber").trim() + ": "
                        + page.getValue();
            }
            zaehler++;
        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln.
     */
    private void determinePagesStructure(DocStructInterface inStrukturelement) {
        if (inStrukturelement == null) {
            return;
        }
        List<ReferenceInterface> listReferenzen = inStrukturelement.getAllReferences("to");
        int zaehler = 0;
        int imageNr = 0;
        if (listReferenzen != null) {
            /*
             * Referenzen sortieren
             */
            Collections.sort(listReferenzen, new Comparator<ReferenceInterface>() {
                @Override
                public int compare(final ReferenceInterface firstObject, final ReferenceInterface secondObject) {
                    Integer firstPage = 0;
                    Integer secondPage = 0;

                    MetadataTypeInterface mdt = Metadaten.this.myPrefs.getMetadataTypeByName("physPageNumber");
                    List<? extends MetadataInterface> listMetadaten = firstObject.getTarget().getAllMetadataByType(mdt);
                    if (Objects.nonNull(listMetadaten) && !listMetadaten.isEmpty()) {
                        MetadataInterface meineSeite = listMetadaten.get(0);
                        firstPage = Integer.parseInt(meineSeite.getValue());
                    }
                    listMetadaten = secondObject.getTarget().getAllMetadataByType(mdt);
                    if (Objects.nonNull(listMetadaten) && !listMetadaten.isEmpty()) {
                        MetadataInterface meineSeite = listMetadaten.get(0);
                        secondPage = Integer.parseInt(meineSeite.getValue());
                    }
                    return firstPage.compareTo(secondPage);
                }
            });

            /* die Größe der Arrays festlegen */
            this.structSeiten = new SelectItem[listReferenzen.size()];
            this.structSeitenNeu = new MetadatumImpl[listReferenzen.size()];

            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (ReferenceInterface ref : listReferenzen) {
                DocStructInterface target = ref.getTarget();
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

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln 2.
     */
    private void determineSecondPagesStructure(DocStructInterface inStrukturelement, int inZaehler) {
        MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends MetadataInterface> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.isEmpty()) {
            return;
        }
        for (MetadataInterface meineSeite : listMetadaten) {
            this.structSeitenNeu[inZaehler] = new MetadatumImpl(meineSeite, inZaehler, this.myPrefs, this.process);
            this.structSeiten[inZaehler] = new SelectItem(String.valueOf(inZaehler),
                    determineMetadata(meineSeite.getDocStruct(), "physPageNumber") + ": " + meineSeite.getValue());
        }
    }

    /**
     * noch für Testzweck zum direkten öffnen der richtigen Startseite 3.
     */
    private int determineThirdPagesStructure(DocStructInterface inStrukturelement) {
        MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        List<? extends MetadataInterface> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.isEmpty()) {
            return 0;
        }
        int result = 0;
        for (MetadataInterface page : listMetadaten) {
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
     * @param paginator
     *            The paginator instance.
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
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage("fehlerBeimEinlesen", logger, e);
        }

        /*
         * zum Schluss nochmal alle Seiten neu einlesen
         */
        allPagesSelection = null;
        retrieveAllImages();
        if (!updateBlocked()) {
            return BLOCK_EXPIRED;
        }

        return null;
    }

    /**
     * alle Knoten des Baums expanden oder collapsen.
     */
    public String expandTree() {
        if (this.treeNodeStruct != null) {
            this.treeNodeStruct.expandNodes(this.treeProperties.get("fullexpanded"));
        }
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
        return ConfigCore.IMAGES_TEMP + session.getId() + "_" + this.imageCounter + ".png";
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
        List<URI> subUris = fileService.getSubUrisForProcess(filterDirectory, this.process, ProcessSubType.IMAGE,
            "");
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
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (dataList != null && !dataList.isEmpty()) {
            logger.trace("dataList not null");
            this.lastImage = dataList.size();
            logger.trace("myBildLetztes");
            if (this.image == null) {
                this.image = dataList.get(0);
            }
            if (this.currentTifFolder != null) {
                logger.trace("currentTifFolder: {}", this.currentTifFolder);
                dataList = this.imageHelper.getImageFiles(this.currentTifFolder);
                if (dataList == null) {
                    return;
                }
            }

            if (dataList.size() >= pageNumber) {
                this.image = dataList.get(pageNumber - 1);
            } else {
                Helper.setErrorMessage(
                    "Image file for page " + pageNumber + " not found in metadata folder: " + this.currentTifFolder);
                this.image = null;
            }

            this.imageNumber = pageNumber;

            URI pagesDirectory = ConfigCore.getTempImagesPathAsCompleteDirectory();

            this.imageCounter++;

            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentPngFile = session.getId() + "_" + this.imageCounter + ".png";
            logger.trace("facescontext");

            assignNewImage(pagesDirectory, currentPngFile);
        }
    }

    private void assignNewImage(URI pagesDirectory, String currentPngFile) {
        File temporaryTifFile = null;
        try {
            temporaryTifFile = File.createTempFile("tempTif_", ".tif");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        if (this.image != null) {
            try {
                URI tifFile = this.currentTifFolder.resolve(this.image);
                logger.trace("tiffconverterpfad: {}", tifFile);
                if (!fileService.fileExist(tifFile)) {
                    tifFile = serviceManager.getProcessService().getImagesTifDirectory(true, this.process)
                            .resolve(this.image);
                    Helper.setErrorMessage("formularOrdner:TifFolders", "",
                        "image " + this.image + " does not exist in folder " + this.currentTifFolder
                                + ", using image from "
                                + new File(serviceManager.getProcessService().getImagesTifDirectory(true, this.process))
                                        .getName());
                }

                // Copy tif-file to temporary folder
                try (InputStream tifFileInputStream = fileService.read(tifFile)) {
                    if (temporaryTifFile != null) {
                        FileUtils.copyInputStreamToFile(tifFileInputStream, temporaryTifFile);
                        this.imageHelper.scaleFile(temporaryTifFile.toURI(), pagesDirectory.resolve(currentPngFile),
                            this.imageSize, this.imageRotation);
                        logger.trace("scaleFile");
                    }
                }
            } catch (IOException | ImageManipulatorException | ImageManagerException | RuntimeException e) {
                Helper.setErrorMessage("could not getById image folder", logger, e);
            } finally {
                if (temporaryTifFile != null) {
                    try {
                        if (!fileService.delete(temporaryTifFile.toURI())) {
                            logger.error(
                                "Error while deleting temporary tif file: " + temporaryTifFile.getAbsolutePath());
                        }
                        // not working
                    } catch (IOException e) {
                        logger.error("Error while deleting temporary tif file: " + e.getMessage());
                    }
                }
            }
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
        MetadataInterface md = this.curMetadatum.getMd();

        /*
         * wenn es ein russischer Titel ist, dessen Transliterierungen anzeigen
         */
        if (md.getMetadataType().getName().equals("RUSMainTitle")) {
            Transliteration trans = new Transliteration();

            try {
                MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("MainTitleTransliterated");
                MetadataInterface mdDin = UghImplementation.INSTANCE.createMetadata(mdt);
                MetadataInterface mdIso = UghImplementation.INSTANCE.createMetadata(mdt);
                mdDin.setStringValue(trans.transliterateDIN(md.getValue()));
                mdIso.setStringValue(trans.transliterateISO(md.getValue()));

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
            return BLOCK_EXPIRED;
        }
        return "";
    }

    /**
     * Transliteration person.
     *
     * @return String
     */
    public String transliteratePerson() {
        PersonInterface md = this.curPerson.getP();

        /*
         * wenn es ein russischer Autor ist, dessen Transliterierungen anlegen
         */
        if (md.getRole().equals("Author")) {
            Transliteration trans = new Transliteration();
            try {
                MetadataTypeInterface metadataTypeDIN = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedDIN");
                MetadataTypeInterface metadataTypeISO = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedISO");
                PersonInterface mdDin = UghImplementation.INSTANCE.createPerson(metadataTypeDIN);
                PersonInterface mdIso = UghImplementation.INSTANCE.createPerson(metadataTypeISO);

                mdDin.setFirstName(trans.transliterateDIN(md.getFirstName()));
                mdDin.setLastName(trans.transliterateDIN(md.getLastName()));
                mdIso.setFirstName(trans.transliterateISO(md.getFirstName()));
                mdIso.setLastName(trans.transliterateISO(md.getLastName()));
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
            return BLOCK_EXPIRED;
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
                FileformatInterface addRdf = CataloguePlugin.getFirstHit(opacCatalog,
                    QueryBuilder.restrictToField(opacSearchField, tok), myPrefs);
                if (addRdf != null) {
                    this.docStruct.addChild(addRdf.getDigitalDocument().getLogicalDocStruct());
                    readMetadataAsFirstTree();
                } else {
                    Helper.setMessage(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (TypeNotAllowedAsChildException | PreferencesException | RuntimeException e) {
                logger.error(e.getMessage(), e);
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
                FileformatInterface addRdf = CataloguePlugin.getFirstHit(opacCatalog,
                    QueryBuilder.restrictToField(opacSearchField, tok), myPrefs);
                if (addRdf != null) {
                    addMetadataToDocStruct(addRdf);
                    readMetadataAsFirstTree();
                } else {
                    Helper.setMessage(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (MetadataTypeNotAllowedException | PreferencesException | RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
        }
        saveMetadataAsBean(this.docStruct);
        this.modeView = "Metadaten";
        return "";
    }

    private void addMetadataToDocStruct(FileformatInterface addRdf)
            throws MetadataTypeNotAllowedException, PreferencesException {
        // create the list of all allowed metadata elements
        List<MetadataTypeInterface> addableMetadataTypes = this.docStruct.getAddableMetadataTypes();
        List<String> allowed = new ArrayList<>();
        if (Objects.nonNull(addableMetadataTypes)) {
            for (MetadataTypeInterface metadataType : addableMetadataTypes) {
                allowed.add(metadataType.getName());
            }
        }

        // if the metadata type in the list of allowed types, then add
        for (MetadataInterface metadata : addRdf.getDigitalDocument().getLogicalDocStruct().getAllMetadata()) {
            if (allowed.contains(metadata.getMetadataType().getName())) {
                this.docStruct.addMetadata(metadata);
            }
        }

        for (PersonInterface person : addRdf.getDigitalDocument().getLogicalDocStruct().getAllPersons()) {
            if (allowed.contains(person.getMetadataType().getName())) {
                this.docStruct.addPerson(person);
            }
        }
    }

    /**
     * Metadata validation.
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
        boolean startPageOk = false;
        boolean endPageOk = false;

        // go through all the pages and check if the page you have set exists
        for (String selectItem : this.allPages) {
            if (selectItem.equals(this.ajaxPageStart)) {
                startPageOk = true;
                this.allPagesSelectionFirstPage = selectItem;
            }
            if (selectItem.equals(this.ajaxPageEnd)) {
                endPageOk = true;
                this.allPagesSelectionLastPage = selectItem;
            }
        }

        // if pages are ok
        if (startPageOk && endPageOk) {
            setPageStartAndEnd();
        } else {
            Helper.setErrorMessage("Selected image(s) unavailable");
        }
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public void setPageStartAndEnd() {
        int startPage = Integer.parseInt(this.allPagesSelectionFirstPage.split(":")[0]) - 1;
        int lastPage = Integer.parseInt(this.allPagesSelectionLastPage.split(":")[0]) - 1;

        int selectionCount = lastPage - startPage + 1;
        if (selectionCount > 0) {
            /* alle bisher zugewiesenen Seiten entfernen */
            this.docStruct.getAllToReferences().clear();
            int zaehler = 0;
            while (zaehler < selectionCount) {
                this.docStruct.addReferenceTo(this.allPagesNew[startPage + zaehler].getMd().getDocStruct(),
                    "logical_physical");
                zaehler++;
            }
        } else {
            Helper.setErrorMessage("Last page before first page is not allowed");
        }
        determinePagesStructure(this.docStruct);

    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     */
    public String takePagesFromChildren() {
        if (!updateBlocked()) {
            return BLOCK_EXPIRED;
        }

        // go through all the children of the current DocStruct
        this.docStruct.getAllReferences("to").removeAll(this.docStruct.getAllReferences("to"));
        if (this.docStruct.getAllChildren() != null) {
            for (DocStructInterface child : this.docStruct.getAllChildren()) {
                List<ReferenceInterface> childRefs = child.getAllReferences("to");
                for (ReferenceInterface toAdd : childRefs) {
                    boolean match = isFoundMatchForReference(toAdd);
                    if (!match) {
                        this.docStruct.getAllReferences("to").add(toAdd);
                    }

                }
            }
        }
        determinePagesStructure(this.docStruct);
        return null;
    }

    private boolean isFoundMatchForReference(ReferenceInterface toAdd) {
        for (ReferenceInterface ref : this.docStruct.getAllReferences("to")) {
            if (ref.getTarget().equals(toAdd.getTarget())) {
                return true;
            }
        }
        return false;
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
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * ausgewählte Seiten dem aktuellen Strukturelement hinzufügen.
     */
    public String addPages() {
        // go through all selected pages
        for (String page : this.allPagesSelection) {
            int currentId = Integer.parseInt(page.split(":")[0]);
            boolean schonEnthalten = false;

            /*
             * wenn schon References vorhanden, prüfen, ob schon enthalten, erst
             * dann zuweisen
             */
            if (this.docStruct.getAllToReferences("logical_physical") != null) {
                for (ReferenceInterface reference : this.docStruct.getAllToReferences("logical_physical")) {
                    if (reference.getTarget() == this.allPagesNew[currentId - 1].getMd().getDocStruct()) {
                        schonEnthalten = true;
                        break;
                    }
                }
            }

            if (!schonEnthalten) {
                this.docStruct.addReferenceTo(this.allPagesNew[currentId - 1].getMd().getDocStruct(),
                    "logical_physical");
            }
        }
        determinePagesStructure(this.docStruct);
        this.allPagesSelection = null;
        if (!updateBlocked()) {
            return BLOCK_EXPIRED;
        }
        return null;
    }

    /**
     * ausgewählte Seiten aus dem Strukturelement entfernen.
     */
    public String removePages() {
        for (String structurePage : this.structSeitenAuswahl) {
            int currentId = Integer.parseInt(structurePage);
            this.docStruct.removeReferenceTo(this.structSeitenNeu[currentId].getMd().getDocStruct());
        }
        determinePagesStructure(this.docStruct);
        this.structSeitenAuswahl = null;
        if (!updateBlocked()) {
            return BLOCK_EXPIRED;
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
     * Set OCR result.
     *
     * @param ocrResult
     *            as String
     */
    public void setOcrResult(String ocrResult) {
        this.ocrResult = ocrResult;
    }

    /**
     * Get OCR address.
     *
     * @return String
     */
    public String getOcrAcdress() {
        int startPage = -1;
        int endPage = -1;
        if (this.structSeiten != null) {
            for (SelectItem selectItem : this.structSeiten) {
                int temp = Integer.parseInt(selectItem.getLabel().substring(0, selectItem.getLabel().indexOf(':')));
                if (startPage == -1 || startPage > temp) {
                    startPage = temp;
                }
                if (endPage == -1 || endPage < temp) {
                    endPage = temp;
                }
            }
        }
        return getOcrBasisUrl(startPage, endPage);
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
        return this.selectedMetadatum.getMd().getMetadataType().getName();
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
            } catch (RuntimeException e) {
                Helper.setErrorMessage("Error while generating image", logger, e);
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

    public void setAllPagesSelectionFirstPage(String allPagesSelectionFirstPage) {
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
     *            The TreeNode.
     */
    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    /**
     * Sets MyStrukturelement on selection of TreeNode.
     *
     * @param event
     *            The NoteSelectEvent.
     */
    public void onNodeSelect(NodeSelectEvent event) {
        setMyStrukturelement((DocStructInterface) event.getTreeNode().getData());
    }

    /**
     * Gets logicalTopstruct of digital document as full expanded TreeNode
     * structure.
     *
     * @return The TreeNote.
     */
    public TreeNode getTreeNodes() {
        TreeNode root = new DefaultTreeNode("root", null);
        List<DocStructInterface> children = logicalTopstruct != null ? this.logicalTopstruct.getAllChildren() : null;
        TreeNode visibleRoot = new DefaultTreeNode(this.logicalTopstruct, root);
        if (this.selectedTreeNode == null) {
            visibleRoot.setSelected(true);
        } else {
            if (this.selectedTreeNode.equals(visibleRoot)) {
                visibleRoot.setSelected(true);
            }
        }

        if (children != null) {
            visibleRoot.getChildren().add(convertDocstructToPrimeFacesTreeNode(children, visibleRoot));
        }
        return setExpandingAll(root, true);
    }

    private TreeNode convertDocstructToPrimeFacesTreeNode(List<DocStructInterface> elements, TreeNode parentTreeNode) {
        TreeNode treeNode = null;

        for (DocStructInterface element : elements) {

            treeNode = new DefaultTreeNode(element, parentTreeNode);
            if (this.selectedTreeNode != null && this.selectedTreeNode.getData().equals(element)) {
                treeNode.setSelected(true);
            }
            List<DocStructInterface> children = element.getAllChildren();
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
     * Handles the TreeDragDropEvent of DocStruct tree.
     *
     * @param event
     *            The TreeDragDropEvent.
     */
    public void onNodeDragDrop(TreeDragDropEvent event) {

        int dropIndex = event.getDropIndex();

        DocStructInterface dropDocStruct = (DocStructInterface) event.getDropNode().getData();
        DocStructInterface dragDocStruct = (DocStructInterface) event.getDragNode().getData();

        if (event.getDropNode().getParent().getData().equals("root")) {
            Helper.setErrorMessage("Only one root element allowed");
        } else {

            if (dropDocStruct.isDocStructTypeAllowedAsChild(dragDocStruct.getDocStructType())) {
                this.docStruct = dragDocStruct;
                this.docStruct.getParent().removeChild(dragDocStruct);

                try {
                    addNewDocStructToExistingDocStruct(dropDocStruct, dragDocStruct, dropIndex);
                } catch (TypeNotAllowedAsChildException e) {
                    Helper.setErrorMessage(e.getMessage(), logger, e);
                }
            } else {
                Helper.setErrorMessage(
                    dragDocStruct.getDocStructType() + " not allowed as child of " + dropDocStruct.getDocStructType());
            }
        }
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
        DocStructInterface temp = inTreeStruct.getStruct();
        if (inTreeStruct.getStruct() == this.docStruct) {
            inTreeStruct.setSelected(true);
        } else {
            inTreeStruct.setSelected(false);
        }

        // alle erlaubten Typen durchlaufen
        for (String allAllowedDocStructTypes : temp.getDocStructType().getAllAllowedDocStructTypes()) {
            if (this.docStruct.getDocStructType().getName().equals(allAllowedDocStructTypes)) {
                inTreeStruct.setEinfuegenErlaubt(true);
                break;
            }
        }

        for (de.sub.goobi.helper.TreeNode treeNode : inTreeStruct.getChildren()) {
            TreeNodeStruct3 kind = (TreeNodeStruct3) treeNode;
            runThroughTree(kind);
        }
    }

    public void setTempStrukturelement(DocStructInterface tempStrukturelement) {
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

    public Map<String, Boolean> getTreeProperties() {
        return this.treeProperties;
    }

    public void setTreeProperties(Map<String, Boolean> treeProperties) {
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
        ArrayList<String> all = new ArrayList<>(Arrays.asList(this.allPages));

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
        return !(Objects.nonNull(this.docStruct) && Objects.isNull(this.docStruct.getParent()));
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

    private void switchFileNames(DocStructInterface firstpage, DocStructInterface secondpage) {
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
        List<DocStructInterface> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
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
            DocStructInterface firstpage = allPages.get(pageIndex - 1);
            DocStructInterface secondpage = allPages.get(pageIndex);
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
        List<DocStructInterface> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
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
            DocStructInterface firstPage = allPages.get(pageIndex + 1);
            DocStructInterface secondPage = allPages.get(pageIndex);
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
        List<DocStructInterface> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = Arrays.asList(allPagesSelection);
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }

        removeReferenceToSelectedPages(selectedPages, allPages);

        allPagesSelection = null;
        if (digitalDocument.getPhysicalDocStruct().getAllChildren() != null) {
            lastImage = digitalDocument.getPhysicalDocStruct().getAllChildren().size();
        } else {
            lastImage = 0;
        }

        allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();

        int currentPhysicalOrder = 1;
        if (allPages != null) {
            MetadataTypeInterface mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            for (DocStructInterface page : allPages) {
                List<? extends MetadataInterface> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.isEmpty()) {
                    currentPhysicalOrder++;
                    break;
                }
                for (MetadataInterface pageNo : pageNoMetadata) {
                    pageNo.setStringValue(String.valueOf(currentPhysicalOrder));
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

    private void removeReferenceToSelectedPages(List<Integer> selectedPages, List<DocStructInterface> allPages)
            throws IOException {
        for (Integer pageIndex : selectedPages) {
            DocStructInterface pageToRemove = allPages.get(pageIndex);
            String imageName = pageToRemove.getImageName();
            removeImage(imageName);
            digitalDocument.getFileSet().removeFile(pageToRemove.getAllContentFiles().get(0));
            digitalDocument.getPhysicalDocStruct().removeChild(pageToRemove);
            List<ReferenceInterface> refs = new ArrayList<>(pageToRemove.getAllFromReferences());
            for (ReferenceInterface ref : refs) {
                ref.getSource().removeReferenceTo(pageToRemove);
            }
        }
    }

    /**
     * Reorder pagination.
     */
    public void reOrderPagination() throws IOException {
        final String bak = "_bak";
        URI imageDirectory;
        imageDirectory = fileService.getImagesDirectory(process);
        if (imageDirectory.getRawPath().equals("")) {
            Helper.setErrorMessage("errorMetsEditorImageRenaming");
            return;
        }

        List<URI> oldFileNames = new ArrayList<>();
        for (DocStructInterface page : digitalDocument.getPhysicalDocStruct().getAllChildren()) {
            oldFileNames.add(URI.create(page.getImageName()));
        }

        for (URI imagename : oldFileNames) {
            for (URI folder : allTifFolders) {
                URI filename = imageDirectory.resolve(folder).resolve(imagename);
                String newFileName = filename + bak;
                fileService.renameFile(filename, newFileName);
            }
            URI ocrFolder = fileService.getProcessSubTypeURI(process, ProcessSubType.OCR, null);
            if (fileService.fileExist(ocrFolder)) {
                List<URI> allOcrFolder = fileService.getSubUris(ocrFolder);
                for (URI folder : allOcrFolder) {
                    URI filename = folder.resolve(imagename);
                    String newFileName = filename + bak;
                    fileService.renameFile(filename, newFileName);
                }
            }

            int counter = 1;
            for (URI oldImageName : oldFileNames) {
                String newfilenamePrefix = generateFileName(counter);
                for (URI folder : allTifFolders) {
                    URI fileToSort = imageDirectory.resolve(folder).resolve(oldImageName);
                    String fileExtension = Metadaten
                            .getFileExtension(fileService.getFileName(fileToSort).replace(bak, ""));
                    URI tempFileName = imageDirectory.resolve(folder)
                            .resolve(fileService.getFileName(fileToSort) + bak);
                    String sortedName = newfilenamePrefix + fileExtension.toLowerCase();
                    fileService.renameFile(tempFileName, sortedName);
                    digitalDocument.getPhysicalDocStruct().getAllChildren().get(counter - 1).setImageName(sortedName);
                }
                try {
                    URI ocr = fileService.getProcessSubTypeURI(process, ProcessSubType.OCR, null);
                    if (fileService.fileExist(ocr)) {
                        List<URI> allOcrFolder = fileService.getSubUris(ocr);
                        for (URI folder : allOcrFolder) {
                            URI fileToSort = folder.resolve(imagename);
                            String fileExtension = Metadaten
                                    .getFileExtension(fileService.getFileName(fileToSort).replace(bak, ""));
                            URI tempFileName = fileToSort.resolve(bak);
                            String sortedName = newfilenamePrefix + fileExtension.toLowerCase();
                            fileService.renameFile(tempFileName, sortedName);
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                counter++;
            }
            retrieveAllImages();

            identifyImage(1);
        }
    }

    private void removeImage(String fileToDelete) throws IOException {
        // TODO check what happens with .tar.gz
        String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf('.'));

        for (URI folder : allTifFolders) {
            removeFiles(fileService.getImagesDirectory(process).resolve(folder), fileToDeletePrefix);
        }

        URI ocr = serviceManager.getFileService().getOcrDirectory(process);
        if (fileService.fileExist(ocr)) {
            List<URI> folder = fileService.getSubUris(ocr);
            for (URI dir : folder) {
                if (fileService.isDirectory(dir) && !fileService.getSubUris(dir).isEmpty()) {
                    removeFiles(dir, fileToDeletePrefix);
                }
            }
        }
    }

    private void removeFiles(URI directory, String fileToDeletePrefix) throws IOException {
        List<URI> filesInFolder = fileService.getSubUris(directory);
        for (URI currentFile : filesInFolder) {
            String fileName = fileService.getFileName(currentFile);
            if (fileName.equals(fileToDeletePrefix)) {
                fileService.delete(currentFile);
            }
        }
    }

    private static String generateFileName(int counter) {
        String fileName;
        if (counter >= 10000000) {
            fileName = "" + counter;
        } else if (counter >= 1000000) {
            fileName = "0" + counter;
        } else if (counter >= 100000) {
            fileName = "00" + counter;
        } else if (counter >= 10000) {
            fileName = "000" + counter;
        } else if (counter >= 1000) {
            fileName = "0000" + counter;
        } else if (counter >= 100) {
            fileName = "00000" + counter;
        } else if (counter >= 10) {
            fileName = "000000" + counter;
        } else {
            fileName = "0000000" + counter;
        }
        return fileName;
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

    public DigitalDocumentInterface getDigitalDocument() {
        return digitalDocument;
    }

    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
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
    public String addMetadataGroup() {
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
    boolean canCreate(MetadataGroupTypeInterface type) {
        List<MetadataGroupTypeInterface> addableTypes = docStruct.getAddableMetadataGroupTypes();
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
        List<MetadataGroupInterface> records = docStruct.getAllMetadataGroups();
        if (records == null) {
            return Collections.emptyList();
        }
        List<RenderableMetadataGroup> result = new ArrayList<>(records.size());
        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        String projectName = process.getProject().getTitle();
        for (MetadataGroupInterface record : records) {
            result.add(new RenderableMetadataGroup(record, this, language, projectName));
        }
        return result;
    }

    /**
     * Returns a backing bean object to display the form to create a new
     * metadata group.
     *
     * @return a bean to create a new metadata group.
     */
    public RenderableMetadataGroup getNewMetadataGroup() {
        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
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
    void removeMetadataGroupFromCurrentDocStruct(MetadataGroupInterface metadataGroup) {
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
        return !updateBlocked() ? BLOCK_EXPIRED : "";
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
            Helper.setErrorMessage("formConfigurationMismatch", logger, e);
            return "";
        }
        modeAdd = false;
        modeAddPerson = false;
        addMetadataGroupMode = true;
        return !updateBlocked() ? BLOCK_EXPIRED : "";
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
        return !updateBlocked() ? BLOCK_EXPIRED : "";
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

    /**
     * Gets all metadata elements which are added to new Docstruc elements.
     *
     * @return The all metadata elements which are added to new Docstruc
     *         elements.
     */
    public int getMetadataElementsToAdd() {
        return metadataElementsToAdd;
    }

    /**
     * Sets all metadata elements which are added to new Docstruc elements.
     *
     * @param metadataElementsToAdd
     *            The all metadata elements which are added to new Docstruc
     *            elements.
     */
    public void setMetadataElementsToAdd(int metadataElementsToAdd) {
        this.metadataElementsToAdd = metadataElementsToAdd;
    }

    /**
     * Gets the metadata typ which is added to new Docstruc elements.
     *
     * @return The metadata typ which is added to new Docstruc elements.
     */
    public String getAddMetaDataType() {
        return addMetaDataType;
    }

    /**
     * Sets the metadata typ which is added to new Docstruc elements.
     *
     * @param addMetaDataType
     *            The metadata typ which is added to new Docstruc elements.
     */
    public void setAddMetaDataType(String addMetaDataType) {
        this.addMetaDataType = addMetaDataType;
    }

    /**
     * Sets the metadata value which is added to new Docstruc elements.
     *
     * @return The metadata value which is added to new Docstruc elements.
     */
    public String getAddMetaDataValue() {
        return addMetaDataValue;
    }

    /**
     * Gets the metadata value which is added to new Docstruc elements.
     *
     * @param addMetaDataValue
     *            The metadata value which is added to new Docstruc elements.
     */
    public void setAddMetaDataValue(String addMetaDataValue) {
        this.addMetaDataValue = addMetaDataValue;
    }

    /**
     * Returns <code>true</code> if adding-serveral-DocStruc-elements-mode is
     * active.
     *
     * @return <code>true</code> if adding-serveral-DocStruc-elements-mode is
     *         active.
     */
    public boolean isAddServeralStructuralElementsMode() {
        return addServeralStructuralElementsMode;
    }

    /**
     * Sets the adding-serveral-DocStruc-elements-mode.
     *
     * @param addServeralStructuralElementsMode
     *            <code>true</code> if adding-serveral-DocStruc-elements-mode
     *            should be active.
     */
    public void setAddServeralStructuralElementsMode(boolean addServeralStructuralElementsMode) {
        this.addServeralStructuralElementsMode = addServeralStructuralElementsMode;
    }
}
