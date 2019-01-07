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

package org.kitodo.production.metadata;

import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.IsDirectoryFilter;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.IncompletePersonObjectException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.UGHException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
<<<<<<< HEAD:Kitodo/src/main/java/org/kitodo/production/metadata/MetadataProcessor.java
import org.kitodo.production.enums.PositionOfNewDocStrucElement;
import org.kitodo.production.enums.SortType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.HelperComparator;
import org.kitodo.production.helper.XmlArticleCounter;
import org.kitodo.production.helper.XmlArticleCounter.CountType;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.helper.metadata.ImageHelper;
import org.kitodo.production.helper.metadata.MetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyLogicalDocStructTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.metadata.display.Modes;
import org.kitodo.production.metadata.display.enums.BindState;
import org.kitodo.production.metadata.display.helper.ConfigDisplayRules;
import org.kitodo.production.metadata.elements.renderable.RenderableMetadataGroup;
import org.kitodo.production.metadata.elements.selectable.SelectOne;
import org.kitodo.production.metadata.elements.selectable.Separator;
import org.kitodo.production.metadata.pagination.Paginator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.workflow.Problem;
=======
import org.kitodo.helper.metadata.LegacyLogicalDocStructTypeHelper;
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;
import org.kitodo.helper.metadata.MetadataHelper;
import org.kitodo.metadata.display.Modes;
import org.kitodo.metadata.display.enums.BindState;
import org.kitodo.metadata.display.helper.ConfigDisplayRules;
import org.kitodo.metadata.elements.renderable.RenderableMetadataGroup;
import org.kitodo.metadata.elements.selectable.SelectOne;
import org.kitodo.metadata.elements.selectable.Separator;
import org.kitodo.metadata.pagination.Paginator;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.Problem;
>>>>>>> Remove references to MetadataTypeInterface [not compilable]:Kitodo/src/main/java/org/kitodo/metadata/MetadataProcessor.java
import org.primefaces.PrimeFaces;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen
 * Eigenschaften und erlaubt die Bearbeitung der Schrittdetails.
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
public class MetadataProcessor {
    private static final Logger logger = LogManager.getLogger(MetadataProcessor.class);

    private ImageHelper imageHelper;
    private MetadataHelper metaHelper;
    private FileformatInterface gdzfile;
    private LegacyDocStructHelperInterface docStruct;
    private List<MetadataImpl> myMetadaten = new LinkedList<>();
    private List<MetaPerson> metaPersonList = new LinkedList<>();
    private MetadataImpl currentMetadata;
    private MetaPerson curPerson;
    private DigitalDocumentInterface digitalDocument;
    private Process process;
    private PrefsInterface myPrefs;
    private String userId;
    private String tempTyp;
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
    private MetadataImpl[] allPagesNew;
    private List<MetadataImpl> tempMetadataList = new ArrayList<>();
    private MetadataImpl selectedMetadata;
    private String currentRepresentativePage = "";
    private boolean showPagination = false;

    private boolean showNewComment = false;
    private boolean correctionComment = false;
    private String addToWikiField;
    protected User user;
    private Problem problem = new Problem();

    private String viewMode = "list";
    private String currentImage = "";

    private String paginationValue;
    // Spalten auf einem Image,
    // 3=nur jede zweite Seite hat
    // Seitennummer
    private boolean fictitious = false;
    private SelectItem[] structSeiten;
    private MetadataImpl[] structurePageNew;
    private LegacyDocStructHelperInterface logicalTopstruct;
    private TreeNodeStruct3 treeNodeStruct;
    private URI image;
    private int imageNumber = 0;
    private int imageCounter = 0;
    private int imageSize = 30;
    private int imageRotation = 0;
    private boolean displayImage = true;
    private boolean imageToStructuralElement = false;
    private final MetadataLock metadataLock = new MetadataLock();
    private String ajaxPageStart = "";
    private String ajaxPageEnd = "";
    private String pagesStart = "";
    private String pagesEnd = "";
    private Map<String, Boolean> treeProperties;
    private final ReentrantLock xmlReadingLock = new ReentrantLock();
    private final FileService fileService = ServiceManager.getFileService();
    private Paginator paginator = new Paginator();
    private TreeNode selectedTreeNode;
    private PositionOfNewDocStrucElement positionOfNewDocStrucElement = PositionOfNewDocStrucElement.AFTER_CURRENT_ELEMENT;
    private int metadataElementsToAdd = 1;
    private String addMetaDataType;
    private String addMetaDataValue;
    private boolean addServeralStructuralElementsMode = false;
    private static final String BLOCK_EXPIRED = "SperrungAbgelaufen";

    private URI imagesFolderURI = ConfigCore.getTempImagesPathAsCompleteDirectory();
    private Path imageFolderPath = Paths.get(imagesFolderURI);
    private File imageFolderFile = imageFolderPath.toFile();
    private String imagesFolder = imageFolderFile.toString().replace("pages", "images").replace("imagesTemp", "");
    private String fullsizePath = "";
    private String thumbnailPath = "";
    private String subfolderName = "";
    private static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    private static final String FULLSIZE_FOLDER_NAME = "fullsize";
    private int numberOfConvertedImages = 0;
    private int numberOfImagesToAdd = 0;
    private List<String> metadataEditorComponents = Arrays.asList("structureTreeForm:structureWrapperPanel",
        "structureTreeForm:paginationWrapperPanel", "metadataWrapperPanel", "commentWrapperPanel",
        "galleryWrapperPanel");
    private String referringView = "desktop";

    /**
     * Public constructor.
     */
    public MetadataProcessor() {
        this.treeProperties = new HashMap<>();
        this.treeProperties.put("showtreelevel", Boolean.FALSE);
        this.treeProperties.put("showtitle", Boolean.FALSE);
        this.treeProperties.put("fullexpanded", Boolean.TRUE);
        this.treeProperties.put("showfirstpagenumber", Boolean.FALSE);
        this.treeProperties.put("showpagesasajax", Boolean.TRUE);
    }

    /**
     * Add.
     */
    public void add() {
        Modes.setBindState(BindState.CREATE);
        getMetadata().setValue("");
    }

    /**
     * Add person.
     */
    public void addPerson() {
        this.tempPersonNachname = "";
        this.tempPersonRecord = ConfigCore.getParameter(ParameterCore.AUTHORITY_DEFAULT, "");
        this.tempPersonVorname = "";
    }

    /**
     * cancel.
     */
    public void cancel() {
        Modes.setBindState(BindState.EDIT);
        getMetadata().setValue("");
    }

    /**
     * Save metadata to Xml file.
     */
    public String saveMetadataToXmlAndGoToProcessPage() {
        calculateMetadataAndImages();
        cleanupMetadata();
        if (storeMetadata()) {
            return referringView;
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
        LegacyMetadataHelper md;
        try {
            md = new LegacyMetadataHelper(this.currentMetadata.getMd().getMetadataType());

            md.setStringValue(this.currentMetadata.getMd().getValue());
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
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    /**
     * Save.
     */
    public void save() {
        try {
            LegacyMetadataHelper md = new LegacyMetadataHelper(this.myPrefs.getMetadataTypeByName(this.tempTyp));
            md.setStringValue(this.selectedMetadata.getValue());

            this.docStruct.addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        // if TitleDocMain, then create equal sort titles with the same content
        if (this.tempTyp.equals("TitleDocMain") && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
            try {
                LegacyMetadataHelper secondMetadata = new LegacyMetadataHelper(
                        this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                secondMetadata.setStringValue(this.selectedMetadata.getValue());
                this.docStruct.addMetadata(secondMetadata);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }

        Modes.setBindState(BindState.EDIT);
        this.selectedMetadata.setValue("");
        saveMetadataAsBean(this.docStruct);
    }

    /**
     * Save person.
     */
    public void savePerson() {
        throw new UnsupportedOperationException("Dead code pending removal");
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
    public static String[] parseAuthorityFileArgs(String valueURI) {
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
                    authority = ConfigCore.getParameter(
                        ParameterCore.AUTHORITY_ID_FROM_URI.getName().replaceFirst("\\{0\\}", authorityURI), null);
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
        this.docStruct.removeMetadata(this.currentMetadata.getMd());
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
     * Gets addable metadata types.
     *
     * @return The metadata types.
     */
    public List<SelectItem> getAddableMetadataTypes() {
        return getAddableMetadataTypes(docStruct, this.tempMetadataList);
    }

    private ArrayList<SelectItem> getAddableMetadataTypes(LegacyDocStructHelperInterface myDocStruct,
            List<MetadataImpl> tempMetadataList) {
        ArrayList<SelectItem> selectItems = new ArrayList<>();

        // determine all addable metadata types
        List<LegacyMetadataTypeHelper> types = myDocStruct.getAddableMetadataTypes();
        if (types == null) {
            return selectItems;
        }

        // alle Metadatentypen, die keine Person sind, oder mit einem
        // Unterstrich anfangen rausnehmen
        for (LegacyMetadataTypeHelper mdt : new ArrayList<>(types)) {
            if (mdt.isPerson()) {
                types.remove(mdt);
            }
        }

        // sort the metadata types
        HelperComparator c = new HelperComparator();
        c.setSortType(SortType.METADATA_TYPE);
        types.sort(c);

        int counter = types.size();

        for (LegacyMetadataTypeHelper mdt : types) {
            selectItems.add(new SelectItem(mdt.getName(), this.metaHelper.getMetadatatypeLanguage(mdt)));
            LegacyMetadataHelper md = new LegacyMetadataHelper(mdt);
            MetadataImpl mdum = new MetadataImpl(md, counter, this.myPrefs, this.process);
            counter++;
            if (tempMetadataList != null) {
                tempMetadataList.add(mdum);
            }
        }
        return selectItems;
    }

    /**
     * Gets addable metadata types from tempTyp.
     *
     * @return The addable metadata types from tempTyp.
     */
    public List<SelectItem> getAddableMetadataTypesFromTempType() {
        LegacyLogicalDocStructTypeHelper dst = this.myPrefs.getDocStrctTypeByName(this.tempTyp);
        LegacyDocStructHelperInterface ds = this.digitalDocument.createDocStruct(dst);

        return getAddableMetadataTypes(ds, this.tempMetadataList);
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
            this.process = ServiceManager.getProcessService().getById(id);
        } catch (NumberFormatException | DAOException e) {
            Helper.setErrorMessage("error while loading process data" + e.getMessage(), logger, e);
        }
        this.userId = Helper.getRequestParameter("BenutzerID");
        this.allPagesSelectionFirstPage = "";
        this.allPagesSelectionLastPage = "";
        this.treeNodeStruct = null;
        try {
            readXmlStart();
        } catch (ReadException e) {
            Helper.setErrorMessage(e.getMessage(), logger, e);
        } catch (PreferencesException | IOException e) {
            Helper.setErrorMessage("error while loading metadata" + e.getMessage(), logger, e);
        }

        expandTree();
        this.metadataLock.setLocked(this.process.getId(), this.userId);
    }

    /**
     * Read metadata.
     */
    public void readXmlStart() throws ReadException, IOException, PreferencesException {
        currentRepresentativePage = "";
        this.myPrefs = ServiceManager.getRulesetService().getPreferences(this.process.getRuleset());
        // TODO: Make file pattern configurable
        this.image = null;
        this.imageNumber = 1;
        this.imageRotation = 0;
        this.currentTifFolder = null;
        readAllTifFolders();

        /*
         * Dokument einlesen
         */
        this.gdzfile = ServiceManager.getProcessService().readMetadataFile(this.process);
        this.digitalDocument = this.gdzfile.getDigitalDocument();
        this.digitalDocument.addAllContentFiles();
        this.metaHelper = new MetadataHelper(this.myPrefs, this.digitalDocument);
        this.imageHelper = new ImageHelper(this.myPrefs, this.digitalDocument);

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

        retrieveAllImages();
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.WITH_AUTOMATIC_PAGINATION)
                && (this.digitalDocument.getPhysicalDocStruct() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren() == null
                        || this.digitalDocument.getPhysicalDocStruct().getAllChildren().isEmpty())) {
            createPagination();
        }

        List<LegacyMetadataHelper> allMetadata = this.digitalDocument.getPhysicalDocStruct().getAllMetadata();
        if (Objects.nonNull(allMetadata)) {
            for (LegacyMetadataHelper md : allMetadata) {
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

    private void createDefaultValues(LegacyDocStructHelperInterface element) {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.METS_EDITOR_ENABLE_DEFAULT_INITIALISATION)) {
            saveMetadataAsBean(element);
            List allChildren = element.getAllChildren();
            if (Objects.nonNull(allChildren)) {
                for (LegacyDocStructHelperInterface ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }

    private void calculateMetadataAndImages() {
        // go again through the metadata for the process and save the data
        XmlArticleCounter counter = new XmlArticleCounter();

        this.process
                .setSortHelperDocstructs(counter.getNumberOfUghElements(this.logicalTopstruct, CountType.DOCSTRUCT));
        this.process.setSortHelperMetadata(counter.getNumberOfUghElements(this.logicalTopstruct, CountType.METADATA));
        try {
            this.process.setSortHelperImages(fileService
                    .getNumberOfFiles(ServiceManager.getProcessService().getImagesOriginDirectory(true, this.process)));
            ServiceManager.getProcessService().save(this.process);
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
            LegacyDocStructHelperInterface physicalDocStruct = this.digitalDocument.getPhysicalDocStruct();
            if (Objects.nonNull(physicalDocStruct) && Objects.nonNull(physicalDocStruct.getAllMetadata())) {
                for (LegacyMetadataHelper md : this.digitalDocument.getPhysicalDocStruct().getAllMetadata()) {
                    if (md.getMetadataType().getName().equals("_representative")) {
                        Integer value = Integer.valueOf(currentRepresentativePage);
                        md.setStringValue(String.valueOf(value + 1));
                        match = true;
                    }
                }
            }
            if (!match) {
                LegacyMetadataTypeHelper mdt = myPrefs.getMetadataTypeByName("_representative");
                addMetadataToPhysicalDocStruct(mdt);
            }
        }
    }

    private void addMetadataToPhysicalDocStruct(LegacyMetadataTypeHelper mdt) {
        try {
            LegacyMetadataHelper md = new LegacyMetadataHelper(mdt);
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
     * vom aktuellen Strukturelement alle Metadaten einlesen.
     *
     * @param inStrukturelement
     *            DocStruct object
     */
    private void saveMetadataAsBean(LegacyDocStructHelperInterface inStrukturelement) {
        this.docStruct = inStrukturelement;
        LinkedList<MetadataImpl> lsMeta = new LinkedList<>();
        LinkedList<MetaPerson> lsPers = new LinkedList<>();

        /*
         * alle Metadaten und die DefaultDisplay-Werte anzeigen
         */
        List<? extends LegacyMetadataHelper> tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(
            inStrukturelement, ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage(), false,
            this.process);
        if (tempMetadata != null) {
            for (LegacyMetadataHelper metadata : tempMetadata) {
                MetadataImpl meta = new MetadataImpl(metadata, 0, this.myPrefs, this.process);
                meta.getSelectedItem();
                lsMeta.add(meta);
            }
        }

        /*
         * alle Personen und die DefaultDisplay-Werte ermitteln
         */
        tempMetadata = this.metaHelper.getMetadataInclDefaultDisplay(inStrukturelement,
            ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage(), true, this.process);
        if (tempMetadata != null) {
            for (LegacyMetadataHelper metadata : tempMetadata) {
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
        List<LegacyDocStructHelperInterface> status = new ArrayList<>();

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
                .getNameByLanguage(ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage());
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
    private void readMetadataAsSecondTree(LegacyDocStructHelperInterface inStrukturelement, TreeNodeStruct3 upperNode) {
        upperNode.setMainTitle(determineMetadata(inStrukturelement, "TitleDocMain"));
        upperNode.setZblNummer(determineMetadata(inStrukturelement, "ZBLIdentifier"));
        upperNode.setZblSeiten(determineMetadata(inStrukturelement, "ZBLPageNumber"));
        upperNode.setPpnDigital(determineMetadata(inStrukturelement, "IdentifierDigital"));
        upperNode.setFirstImage(this.metaHelper.getImageNumber(inStrukturelement, MetadataHelper.getPageNumberFirst()));
        upperNode.setLastImage(this.metaHelper.getImageNumber(inStrukturelement, MetadataHelper.getPageNumberLast()));
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
        List<LegacyDocStructHelperInterface> children = inStrukturelement.getAllChildren();
        String language = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        if (children != null) {
            // es gibt Kinder-Strukturelemente
            for (LegacyDocStructHelperInterface child : children) {
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
    private String determineMetadata(LegacyDocStructHelperInterface inStrukturelement, String type) {
        StringBuilder result = new StringBuilder();
        List<LegacyMetadataHelper> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (LegacyMetadataHelper md : allMDs) {
                if (md.getMetadataType().getName().equals(type)) {
                    result.append(md.getValue() == null ? "" : md.getValue());
                    result.append(" ");
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Set my structure element.
     *
     * @param inStruct
     *            DocStruct
     */
    @SuppressWarnings("rawtypes")
    public void setMyStrukturelement(LegacyDocStructHelperInterface inStruct) {
        Modes.setBindState(BindState.EDIT);
        saveMetadataAsBean(inStruct);

        /*
         * die Selektion kenntlich machen
         */
        for (HashMap childrenList : this.treeNodeStruct.getChildrenAsListAlle()) {
            TreeNodeStruct3 nodes = (TreeNodeStruct3) childrenList.get("node");
            // restore Selection
            nodes.setSelected(this.docStruct == nodes.getStruct());
        }

        updateBlocked();
    }

    /**
     * Knoten nach oben schieben.
     */
    public void deleteNode() {
        if (this.docStruct != null && this.docStruct.getParent() != null) {
            LegacyDocStructHelperInterface tempParent = this.docStruct.getParent();
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
        LegacyDocStructHelperInterface docStruct = null;
        LegacyLogicalDocStructTypeHelper docStructType = this.myPrefs.getDocStrctTypeByName(this.tempTyp);

        try {
            docStruct = addNode(this.docStruct, this.digitalDocument, docStructType, this.positionOfNewDocStrucElement,
                1, null, null);

        } catch (UGHException e) {
            logger.error(e.getMessage());
        }

        if (!this.pagesStart.equals("") && !this.pagesEnd.equals("")) {

            this.ajaxPageStart = this.pagesStart;
            this.ajaxPageEnd = this.pagesEnd;
            setFirstAndLastPageViaAjax(docStruct);

        }
        readMetadataAsFirstTree();
    }

    /**
     * Adds a several new DocStruct elements to the current DocStruct tree and
     * sets specified metadata.
     */
    public void addSeveralNodesWithMetadata() {
        LegacyDocStructHelperInterface ds;

        LegacyLogicalDocStructTypeHelper docStructType = this.myPrefs.getDocStrctTypeByName(this.tempTyp);
        try {
            ds = addNode(this.docStruct, this.digitalDocument, docStructType, this.positionOfNewDocStrucElement,
                this.metadataElementsToAdd, this.addMetaDataType, this.addMetaDataValue);
        } catch (UGHException e) {
            logger.error(e.getMessage());
        }
        readMetadataAsFirstTree();
    }

    private void addNewDocStructToExistingDocStruct(LegacyDocStructHelperInterface existingDocStruct,
            LegacyDocStructHelperInterface newDocStruct, int index) throws TypeNotAllowedAsChildException {

        if (existingDocStruct.isDocStructTypeAllowedAsChild(newDocStruct.getDocStructType())) {
            if (existingDocStruct.getAllChildren().size() < index) {
                index--;
            }
            existingDocStruct.addChild(index, newDocStruct);
        } else {
            throw new TypeNotAllowedAsChildException(newDocStruct.getDocStructType() + " ot allowed as child of "
                    + existingDocStruct.getDocStructType());
        }
    }

    private LegacyDocStructHelperInterface addNode(LegacyDocStructHelperInterface docStruct, DigitalDocumentInterface digitalDocument,
            LegacyLogicalDocStructTypeHelper docStructType, PositionOfNewDocStrucElement positionOfNewDocStrucElement,
            int quantity, String metadataType, String value)
            throws MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

        ArrayList<LegacyDocStructHelperInterface> createdElements = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {

            LegacyDocStructHelperInterface createdElement = digitalDocument.createDocStruct(docStructType);
            if (docStructType != null && value != null && metadataType != null) {
                createdElement.addMetadata(metadataType, value);
            }
            createdElements.add(createdElement);
        }

        if (positionOfNewDocStrucElement.equals(PositionOfNewDocStrucElement.LAST_CHILD_OF_CURRENT_ELEMENT)) {
            for (LegacyDocStructHelperInterface element : createdElements) {
                docStruct.addChild(element);
            }
        } else {
            LegacyDocStructHelperInterface edited = positionOfNewDocStrucElement.equals(
                PositionOfNewDocStrucElement.FIRST_CHILD_OF_CURRENT_ELEMENT) ? docStruct : docStruct.getParent();
            if (edited == null) {
                logger.debug("The selected element cannot investigate the father.");
            } else {
                List<LegacyDocStructHelperInterface> childrenBefore = edited.getAllChildren();
                if (childrenBefore == null) {
                    for (LegacyDocStructHelperInterface element : createdElements) {
                        edited.addChild(element);
                    }
                } else {
                    // Build a new list of children for the edited element
                    List<LegacyDocStructHelperInterface> newChildren = new ArrayList<>(childrenBefore.size() + 1);
                    if (positionOfNewDocStrucElement
                            .equals(PositionOfNewDocStrucElement.FIRST_CHILD_OF_CURRENT_ELEMENT)) {
                        newChildren.addAll(createdElements);
                    }
                    for (LegacyDocStructHelperInterface child : childrenBefore) {
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
                    for (LegacyDocStructHelperInterface child : newChildren) {
                        edited.removeChild(child);
                    }

                    // Set the new children on the edited element
                    for (LegacyDocStructHelperInterface child : newChildren) {
                        edited.addChild(child);
                    }
                }
            }
        }
        return createdElements.iterator().next();
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
    public void createPagination() throws IOException {
        this.imageHelper.createPagination(this.process, this.currentTifFolder);
        retrieveAllImages();

        // added new
        LegacyDocStructHelperInterface log = this.digitalDocument.getLogicalDocStruct();
        while (log.getDocStructType().getAnchorClass() != null && log.getAllChildren() != null
                && !log.getAllChildren().isEmpty()) {
            log = log.getAllChildren().get(0);
        }
        if (log.getDocStructType().getAnchorClass() != null) {
            return;
        }

        if (log.getAllChildren() != null) {
            for (LegacyDocStructHelperInterface child : log.getAllChildren()) {
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
    }

    /**
     * alle Seiten ermitteln.
     */
    public void retrieveAllImages() {
        DigitalDocumentInterface document;
        try {
            document = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMessage("Can not get DigitalDocument: ", e.getMessage());
            return;
        }

        List<LegacyDocStructHelperInterface> meineListe = document.getPhysicalDocStruct().getAllChildren();
        if (meineListe == null) {
            this.allPages = null;
            return;
        }
        int zaehler = meineListe.size();
        this.allPages = new String[zaehler];
        this.allPagesNew = new MetadataImpl[zaehler];
        zaehler = 0;
        LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        for (LegacyDocStructHelperInterface mySeitenDocStruct : meineListe) {
            List<? extends LegacyMetadataHelper> mySeitenDocStructMetadaten = mySeitenDocStruct.getAllMetadataByType(mdt);
            for (LegacyMetadataHelper page : mySeitenDocStructMetadaten) {
                this.allPagesNew[zaehler] = new MetadataImpl(page, zaehler, this.myPrefs, this.process);
                this.allPages[zaehler] = determineMetadata(page.getDocStruct(), "physPageNumber").trim() + ": "
                        + page.getValue();
            }
            zaehler++;
        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln.
     */
    private void determinePagesStructure(LegacyDocStructHelperInterface inStrukturelement) {
        if (inStrukturelement == null) {
            return;
        }
        List<ReferenceInterface> references = inStrukturelement.getAllReferences("to");
        int zaehler = 0;
        int imageNr = 0;
        if (references != null) {
            references.sort((firstObject, secondObject) -> {
                Integer firstPage = 0;
                int secondPage = 0;

                LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
                List<? extends LegacyMetadataHelper> listMetadata = firstObject.getTarget().getAllMetadataByType(mdt);
                if (Objects.nonNull(listMetadata) && !listMetadata.isEmpty()) {
                    LegacyMetadataHelper page = listMetadata.get(0);
                    firstPage = Integer.parseInt(page.getValue());
                }
                listMetadata = secondObject.getTarget().getAllMetadataByType(mdt);
                if (Objects.nonNull(listMetadata) && !listMetadata.isEmpty()) {
                    LegacyMetadataHelper page = listMetadata.get(0);
                    secondPage = Integer.parseInt(page.getValue());
                }
                return firstPage.compareTo(secondPage);
            });

            /* die Größe der Arrays festlegen */
            this.structSeiten = new SelectItem[references.size()];
            this.structurePageNew = new MetadataImpl[references.size()];

            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (ReferenceInterface ref : references) {
                LegacyDocStructHelperInterface target = ref.getTarget();
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
    private void determineSecondPagesStructure(LegacyDocStructHelperInterface inStrukturelement, int inZaehler) {
        LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends LegacyMetadataHelper> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.isEmpty()) {
            return;
        }
        for (LegacyMetadataHelper meineSeite : listMetadaten) {
            this.structurePageNew[inZaehler] = new MetadataImpl(meineSeite, inZaehler, this.myPrefs, this.process);
            this.structSeiten[inZaehler] = new SelectItem(String.valueOf(inZaehler),
                    determineMetadata(meineSeite.getDocStruct(), "physPageNumber") + ": " + meineSeite.getValue());
        }
    }

    /**
     * noch für Testzweck zum direkten öffnen der richtigen Startseite 3.
     */
    private int determineThirdPagesStructure(LegacyDocStructHelperInterface inStrukturelement) {
        LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        List<? extends LegacyMetadataHelper> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.isEmpty()) {
            return 0;
        }
        int result = 0;
        for (LegacyMetadataHelper page : listMetadaten) {
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
        List<URI> subUris = fileService.getSubUrisForProcess(filterDirectory, this.process, ProcessSubType.IMAGE, "");
        this.allTifFolders.addAll(subUris);

        Optional<String> suffix = ConfigCore.getOptionalString(ParameterCore.METS_EDITOR_DEFAULT_SUFFIX);
        if (suffix.isPresent()) {
            for (URI directoryUri : this.allTifFolders) {
                if (directoryUri.toString().endsWith(suffix.get())
                        || directoryUri.toString().endsWith(suffix.get().concat("/"))) {
                    this.currentTifFolder = directoryUri;
                    break;
                }
            }
        }

        if (!this.allTifFolders.contains(this.currentTifFolder)) {
            this.currentTifFolder = ServiceManager.getProcessService().getImagesTifDirectory(true, this.process.getId(),
                this.process.getTitle(), this.process.getProcessBaseUri());
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

        logger.trace("dataList");
        List<URI> dataList = this.imageHelper.getImageFiles(digitalDocument.getPhysicalDocStruct());
        logger.trace("dataList 2");
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.WITH_AUTOMATIC_PAGINATION)
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
                    tifFile = ServiceManager.getProcessService().getImagesTifDirectory(true, this.process.getId(),
                        this.process.getTitle(), this.process.getProcessBaseUri()).resolve(this.image);
                    Helper.setErrorMessage("formularOrdner:TifFolders", "",
                        "image " + this.image + " does not exist in folder " + this.currentTifFolder
                                + ", using image from "
                                + new File(ServiceManager.getProcessService().getImagesTifDirectory(true,
                                    this.process.getId(), this.process.getTitle(), this.process.getProcessBaseUri()))
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
        if (MetadataLock.isLocked(this.process.getId())
                && this.metadataLock.getLockUser(this.process.getId()).equals(this.userId)) {
            this.metadataLock.setLocked(this.process.getId(), this.userId);
            return true;
        } else {
            return false;
        }
    }

    /*
     * Navigationsanweisungen
     */

    /*
     * aus einer Liste von PPNs Strukturelemente aus dem Opac ## holen und dem
     * aktuellen Strukturelement unterordnen
     */

    /**
     * Metadata validation.
     */
    public void validate() {
        ServiceManager.getMetadataValidationService().validate(this.gdzfile, this.myPrefs, this.process);
        saveMetadataAsBean(this.docStruct);
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

    private SelectOne<Separator> paginationSeparators = new SelectOne<>(
            Separator.factory(ConfigCore.getParameterOrDefaultValue(ParameterCore.PAGE_SEPARATORS)));

    /**
     * Set the first and the last page via AJAX request.
     * 
     * @param docStruct
     *            the doc structure for which the pages are set
     */
    public void setFirstAndLastPageViaAjax(LegacyDocStructHelperInterface docStruct) {
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
            setPageStartAndEnd(docStruct);
        } else {
            Helper.setErrorMessage("Selected image(s) unavailable");
        }
    }

    public void setPageStartAndEnd() {
        setPageStartAndEnd(this.docStruct);
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen.
     * 
     * @param docStruct
     *            the doc structure for which the pages are set
     */
    public void setPageStartAndEnd(LegacyDocStructHelperInterface docStruct) {
        int startPage = Integer.parseInt(this.allPagesSelectionFirstPage.split(":")[0]) - 1;
        int lastPage = Integer.parseInt(this.allPagesSelectionLastPage.split(":")[0]) - 1;

        int selectionCount = lastPage - startPage + 1;
        if (selectionCount > 0) {

            /* alle bisher zugewiesenen Seiten entfernen */
            docStruct.getAllToReferences().clear();
            int zaehler = 0;
            while (zaehler < selectionCount) {
                docStruct.addReferenceTo(this.allPagesNew[startPage + zaehler].getMd().getDocStruct(),
                    "logical_physical");
                zaehler++;
            }
        } else {
            Helper.setErrorMessage("Last page before first page is not allowed");
        }
        determinePagesStructure(docStruct);

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
            for (LegacyDocStructHelperInterface child : this.docStruct.getAllChildren()) {
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
            this.docStruct.removeReferenceTo(this.structurePageNew[currentId].getMd().getDocStruct());
        }
        determinePagesStructure(this.docStruct);
        this.structSeitenAuswahl = null;
        if (!updateBlocked()) {
            return BLOCK_EXPIRED;
        }
        return null;
    }

    /**
     * Get temporal type.
     *
     * @return String
     */
    public String getTempTyp() {
        if (this.selectedMetadata == null) {
            getAddableMetadataTypes();
            if (!this.tempMetadataList.isEmpty()) {
                this.selectedMetadata = this.tempMetadataList.get(0);
            } else {
                return "";
            }
        }
        return this.selectedMetadata.getMd().getMetadataType().getName();
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
     * @return MetadataImpl object
     */
    public MetadataImpl getMetadata() {

        if (this.selectedMetadata == null) {
            getAddableMetadataTypes();
            if (!this.tempMetadataList.isEmpty()) {
                this.selectedMetadata = this.tempMetadataList.get(0);
            }
        }
        return this.selectedMetadata;
    }

    public void setMetadatum(MetadataImpl meta) {
        this.selectedMetadata = meta;
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

    public String getPaginationValue() {
        return this.paginationValue;
    }

    public void setPaginationValue(String paginationValue) {
        this.paginationValue = paginationValue;
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
        setMyStrukturelement((LegacyDocStructHelperInterface) event.getTreeNode().getData());
    }

    /**
     * Gets logicalTopstruct of digital document as full expanded TreeNode
     * structure.
     *
     * @return The TreeNote.
     */
    public TreeNode getTreeNodes() {
        TreeNode root = new DefaultTreeNode("root", null);
        List<LegacyDocStructHelperInterface> children = logicalTopstruct != null ? this.logicalTopstruct.getAllChildren() : null;
        TreeNode visibleRoot = new DefaultTreeNode(this.logicalTopstruct, root);
        if (this.selectedTreeNode == null) {
            visibleRoot.setSelected(true);
        } else {
            if (this.selectedTreeNode.equals(visibleRoot)) {
                visibleRoot.setSelected(true);
            }
        }

        if (children != null) {
            Optional<TreeNode> optionalPrimeFacesTreeNode = convertDocstructToPrimeFacesTreeNode(children, visibleRoot);
            if (optionalPrimeFacesTreeNode.isPresent()) {
                visibleRoot.getChildren().add(optionalPrimeFacesTreeNode.get());
            }
        }
        return setExpandingAll(root, true);
    }

    private Optional<TreeNode> convertDocstructToPrimeFacesTreeNode(List<LegacyDocStructHelperInterface> elements,
            TreeNode parentTreeNode) {
        TreeNode treeNode = null;

        for (LegacyDocStructHelperInterface element : elements) {

            treeNode = new DefaultTreeNode(element, parentTreeNode);
            if (this.selectedTreeNode != null && Objects.equals(this.selectedTreeNode.getData(), element)) {
                treeNode.setSelected(true);
            }
            List<LegacyDocStructHelperInterface> children = element.getAllChildren();
            List<LegacyDocStructHelperInterface> pages = getPageReferencesToDocStruct(element);
            if (children != null) {
                if (Objects.nonNull(pages) && !pages.isEmpty()) {
                    children.addAll(pages);
                }
                convertDocstructToPrimeFacesTreeNode(children, treeNode);
            } else if (Objects.nonNull(pages) && !pages.isEmpty()) {
                convertDocstructToPrimeFacesTreeNode(pages, treeNode);
            }
        }
        return Optional.ofNullable(treeNode);
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
        if (event.getDropNode().getData().equals("root")) {
            Helper.setErrorMessage("Only one root element allowed");
        } else  {
            LegacyDocStructHelperInterface dropDocStruct = (LegacyDocStructHelperInterface) event.getDropNode().getData();
            LegacyDocStructHelperInterface dragDocStruct = (LegacyDocStructHelperInterface) event.getDragNode().getData();

            if (Objects.equals(dragDocStruct.getDocStructType().getName(), "page")) {
                String pyhsicalPageNumber = String.valueOf(getPhysicalPageNumber(dragDocStruct));
                this.docStruct = dropDocStruct;
                this.allPagesSelection = new String[1];
                this.allPagesSelection[0] = pyhsicalPageNumber;
                addPages();
                // TODO We need to implement also the removing of the draged node from the old
                // parent
                return;
            }

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

    public List<MetadataImpl> getMyMetadaten() {
        return this.myMetadaten;
    }

    public void setMyMetadaten(List<MetadataImpl> myMetadaten) {
        this.myMetadaten = myMetadaten;
    }

    public List<MetaPerson> getMetaPersonList() {
        return this.metaPersonList;
    }

    public void setMetaPersonList(List<MetaPerson> metaPersonList) {
        this.metaPersonList = metaPersonList;
    }

    public MetadataImpl getCurrentMetadata() {
        return this.currentMetadata;
    }

    public void setCurrentMetadata(MetadataImpl currentMetadata) {
        this.currentMetadata = currentMetadata;
    }

    public MetaPerson getCurPerson() {
        return this.curPerson;
    }

    public void setCurPerson(MetaPerson curPerson) {
        this.curPerson = curPerson;
    }

    public URI getCurrentTifFolder() {
        return this.currentTifFolder;
    }

    public void setCurrentTifFolder(URI currentTifFolder) {
        this.currentTifFolder = currentTifFolder;
    }

    public boolean getFictitious() {
        return fictitious;
    }

    public void setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
    }

    /**
     * Delete selected pages.
     */
    void deleteSelectedPages() throws IOException {
        List<Integer> selectedPages = new ArrayList<>();
        List<String> pagesList = Arrays.asList(allPagesSelection);
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }

        List<LegacyDocStructHelperInterface> allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();

        removeReferenceToSelectedPages(selectedPages, allPages);

        allPagesSelection = null;

        allPages = digitalDocument.getPhysicalDocStruct().getAllChildren();

        int currentPhysicalOrder = 1;
        if (allPages != null) {
            LegacyMetadataTypeHelper mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            for (LegacyDocStructHelperInterface page : allPages) {
                List<? extends LegacyMetadataHelper> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.isEmpty()) {
                    currentPhysicalOrder++;
                    break;
                }
                for (LegacyMetadataHelper pageNo : pageNoMetadata) {
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

    private void removeReferenceToSelectedPages(List<Integer> selectedPages, List<LegacyDocStructHelperInterface> allPages)
            throws IOException {
        for (Integer pageIndex : selectedPages) {
            LegacyDocStructHelperInterface pageToRemove = allPages.get(pageIndex);
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

    private void removeImage(String fileToDelete) throws IOException {
        // TODO check what happens with .tar.gz
        String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf('.'));

        for (URI folder : allTifFolders) {
            removeFiles(fileService.getImagesDirectory(process).resolve(folder), fileToDeletePrefix);
        }

        URI ocr = fileService.getOcrDirectory(process);
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

    /**
     * Checks whether a given meta-data group type is available for adding. This
     * can be used by a RenderableMetadataGroup to getById out whether it can be
     * copied or not.
     *
     * @param type
     *            meta-data group type to look for
     * @return whether the type is available to add
     */
    public boolean canCreate(MetadataGroupTypeInterface type) {
        List<MetadataGroupTypeInterface> addableTypes = docStruct.getAddableMetadataGroupTypes();
        if (addableTypes == null) {
            addableTypes = Collections.emptyList();
        }
        return addableTypes.contains(type);
    }

    /**
     * Deletes the metadata group.
     *
     * @param metadataGroup
     *            metadata group to delete.
     */
    public void removeMetadataGroupFromCurrentDocStruct(MetadataGroupInterface metadataGroup) {
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
    public String showAddMetadataGroupAsCopy(RenderableMetadataGroup master) {
        return !updateBlocked() ? BLOCK_EXPIRED : "";
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

    /**
     * Get showPagination.
     *
     * @return value of showPagination
     */
    public boolean isShowPagination() {
        return showPagination;
    }

    /**
     * Set showPagination.
     *
     * @param showPagination
     *            as boolean
     */
    public void setShowPagination(boolean showPagination) {
        this.showPagination = showPagination;
    }

    public boolean isShowNewComment() {
        return showNewComment;
    }

    public void setShowNewComment(boolean showNewComment) {
        this.showNewComment = showNewComment;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    public String getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(String currentImage) {
        this.currentImage = currentImage;
    }

    /**
     * Gets numberOfImagesToAdd.
     *
     * @return The numberOfImagesToAdd.
     */
    public int getNumberOfImagesToAdd() {
        return numberOfImagesToAdd;
    }

    /**
     * Sets numberOfImagesToAdd.
     *
     * @param numberOfImagesToAdd
     *            The numberOfImagesToAdd.
     */
    public void setNumberOfImagesToAdd(int numberOfImagesToAdd) {
        this.numberOfImagesToAdd = numberOfImagesToAdd;
    }

    /**
     * Return index of currently selected page.
     * 
     * @return current page index
     */
    public int getPageIndex() {
        if (!this.getImages().isEmpty() && this.getImages().contains(this.currentImage)) {
            return this.getImages().indexOf(this.currentImage) + 1;
        } else {
            return 0;
        }
    }

    /**
     * Creates dummy images for current process and paginates by configured standard setting.
     */
    public void addNewImagesAndPaginate() {
        try {
            fileService.createDummyImagesForProcess(this.process, this.numberOfImagesToAdd);
            createPagination();
            this.digitalDocument = this.gdzfile.getDigitalDocument();
            this.digitalDocument.addAllContentFiles();
            readAllTifFolders();
        } catch (IOException | PreferencesException | URISyntaxException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Gets the logical page number from a paginated docstruct.
     * 
     * @param docStruct
     *            The DocStruct opject.
     * @return The logical page number.
     */
    public String getLogicalPageNumber(LegacyDocStructHelperInterface docStruct) {
        for (String page : allPages) {
            int physicalPageNumber = getPhysicalPageNumber(docStruct);
            if (page.startsWith(String.valueOf(physicalPageNumber))) {
                return getLogicalPageNumberOfPaginatedImage(page);
            }
        }
        return "";
    }

    private String getLogicalPageNumberOfPaginatedImage(String paginationText) {
        paginationText = paginationText.replace(" ", "");
        return paginationText.split(":")[1];
    }

    /**
     * Convert the TIFF images of the current process to PNG images for the metadata web frontend and
     * copy them to them to the webapps/images/[processID]/fullsize/ folder.
     */
    private void convertImages() {
        if (Objects.nonNull(this.currentTifFolder)) {
            try {
                ensureDirectoryExists(Paths.get(fullsizePath));

                // first, convert tiff images to pngs
                for (URI tiffPath : this.imageHelper.getImageFiles(this.currentTifFolder)) {
                    String targetPath = fullsizePath + FilenameUtils.removeExtension(tiffPath.toString()) + ".png";

                    File fullsizeFile = new File(targetPath);
                    if (fullsizeFile.exists()) {
                        continue;
                    }

                    URI tiffURI = Paths
                            .get(ConfigCore.getKitodoDataDirectory() + this.currentTifFolder + tiffPath.toString())
                            .toUri();

                    logger.info("Reading {}…", tiffURI);
                    BufferedImage inputImage = ImageIO.read(tiffURI.toURL());

                    logger.info("Writing {}…", targetPath);
                    ImageIO.write(inputImage, "png", new File(targetPath));
                    numberOfConvertedImages++;
                    // FIXME: this call to the update function does not work!
                    updateComponent(metadataEditorComponents);
                }

                // then, create thumbnails from the converted images
                generateThumbnails();

                updateComponent(metadataEditorComponents);
            } catch (MalformedURLException e) {
                Helper.setErrorMessage("ERROR: URL malformed!", logger, e);
            } catch (IOException e) {
                Helper.setErrorMessage("ERROR: IOException!", logger, e);
            }
        }
    }

    private void generateThumbnails() {
        updateImagesFolder();
        if (!thumbnailsExist()) {
            URI fullsizeFolderURI = Paths.get(fullsizePath).toUri();
            try (Stream<Path> imagePaths = Files.list(Paths.get(fullsizeFolderURI))) {
                logger.info("Creating thumbnails from {} to {}", fullsizePath, thumbnailPath);
                Thumbnails.of(
                    (File[]) imagePaths.filter(path -> path.toFile().isFile()).filter(path -> path.toFile().canRead())
                            .filter(path -> path.toString().endsWith(".png")).map(Path::toFile).toArray(File[]::new))
                        .size(60, 100).outputFormat("png")
                        .toFiles(new File(thumbnailPath), Rename.PREFIX_DOT_THUMBNAIL);
                logger.info("Thumbnails completed in {}", thumbnailPath);
            } catch (IOException e) {
                logger.error("ERROR: IOException thrown while creating thumbnails: " + e.getLocalizedMessage());
            } catch (IllegalArgumentException e) {
                logger.error("ERROR: IllegalArgumentException thrown while creating thumbnails: " + e.getMessage());
            }
        }
    }

    private boolean thumbnailsExist() {
        Path thumbnailDirectory = Paths.get(thumbnailPath);
        ensureDirectoryExists(thumbnailDirectory);
        File thumbnailFile;
        File imageFile;
        for (String image : getImages()) {
            imageFile = new File(image);
            String thumbnailFilepath = thumbnailPath + File.separator + imageFile.getName();
            thumbnailFile = new File(thumbnailFilepath);
            if (!thumbnailFile.exists()) {
                return false;
            }
        }
        return true;
    }

    private void ensureDirectoryExists(Path directory) {
        if (!directory.toFile().exists() || !directory.toFile().isDirectory()) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                logger.error("ERROR: IOException thrown while trying to create directory '" + directory + ": "
                        + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Return the path to the thumbnail for the page with the given path 'image'.
     * 
     * @param image
     *            path to image file whose thumbnail is returned
     * @return thumbnail for given image
     */
    public String getThumbnail(String image) {
        File imageFile = new File(image);
        String filename = imageFile.getName();
        return image.replace(FULLSIZE_FOLDER_NAME, THUMBNAIL_FOLDER_NAME).replace(filename, "thumbnail." + filename);
    }

    private void resetTemporaryFolders() {

        try {
            FileUtils.deleteDirectory(new File(fullsizePath));
        } catch (IOException e) {
            logger.error("ERROR: unable to delete directory '" + fullsizePath
                    + "' containing fullsize PNG copies of TIFF images (" + "reason: " + e.getLocalizedMessage() + ")");
        }

        try {
            FileUtils.deleteDirectory(new File(thumbnailPath));
        } catch (IOException e) {
            logger.error("ERROR: unable to delete directory '" + thumbnailPath
                    + "' containing PNG thumbnails of TIFF images (" + "reason: " + e.getLocalizedMessage() + ")");
        }
    }

    /**
     * Generate PNG copies and thumbnails of all images in currently selected TIF folder.
     */
    public void generatePNGs() {
        this.numberOfConvertedImages = 0;
        // resetTemporaryFolders();
        updateComponent(Collections.singletonList("convertTIFFDialog"));
        convertImages();
    }

    /**
     * Get number of TIFF images converted to PNG.
     * 
     * @return number of converted TIFF images
     */
    public int getNumberOfConvertedImages() {
        return numberOfConvertedImages;
    }

    /**
     * Get number of all images in current TIFF folder.
     * 
     * @return number of images in current TIFF folder
     */
    public int getNumberOfImagesInCurrentTifFolder() {
        return this.imageHelper.getImageFiles(this.currentTifFolder).size();
    }

    /**
     * Return all structure elements.
     * 
     * @return list of all structure elements
     */
    public List<LegacyDocStructHelperInterface> getAllStructureElements() {
        return getStructureElements(this.logicalTopstruct);
    }

    private List<LegacyDocStructHelperInterface> getStructureElements(LegacyDocStructHelperInterface docStruct) {
        List<LegacyDocStructHelperInterface> docStructElements = new LinkedList<>();
        if (Objects.nonNull(docStruct)) {
            docStructElements.add(docStruct);
            if (Objects.nonNull(docStruct.getAllChildren())) {
                for (LegacyDocStructHelperInterface element : docStruct.getAllChildren()) {
                    if (Objects.nonNull(element)) {
                        if (Objects.isNull(element.getAllChildren()) || element.getAllChildren().isEmpty()) {
                            docStructElements.add(element);
                        } else {
                            docStructElements.addAll(getStructureElements(element));
                        }
                    }
                }
            }
        }
        return docStructElements;
    }

    /**
     * Event listener for drag drop event.
     * 
     * @param dragDropEvent
     *            the event that triggers this listener
     */
    public void onPageDrop(DragDropEvent dragDropEvent) {
        String dragId = dragDropEvent.getDragId();
        String dropId = dragDropEvent.getDropId();

        String[] dragIDComponents = dragId.split(":");
        String[] dropIDComponents = dropId.split(":");

        int sourceStructureElementIndex;
        int pageIndex;

        int targetStructureElementIndex = Integer.parseInt(dropIDComponents[2]);
        LegacyDocStructHelperInterface targetDocStruct = getAllStructureElements().get(targetStructureElementIndex);

        if (dragIDComponents[1].equals("structuredPages")) {
            sourceStructureElementIndex = Integer.parseInt(dragIDComponents[2]);
            pageIndex = Integer.parseInt(dragIDComponents[4]);

            LegacyDocStructHelperInterface sourceDocStruct = getAllStructureElements().get(sourceStructureElementIndex);

            List<String> docStructPages = getPagesAssignedToDocStruct(sourceDocStruct);

            String pagePath = docStructPages.get(pageIndex);

            if (Objects.nonNull(sourceDocStruct.getAllToReferences("logical_physical"))) {
                for (ReferenceInterface reference : sourceDocStruct.getAllToReferences("logical_physical")) {

                    if (FilenameUtils.getBaseName(pagePath)
                            .equals(FilenameUtils.removeExtension(reference.getTarget().getImageName()))) {
                        // Remove page reference from source doc struct
                        sourceDocStruct.removeReferenceTo(reference.getTarget());

                        // Add page reference to target doc struct
                        targetDocStruct.addReferenceTo(reference.getTarget(), "logical_physical");

                        determinePagesStructure(sourceDocStruct);
                        determinePagesStructure(targetDocStruct);

                        break;
                    }
                }
            }
        }
    }

    /**
     * Retrieve and return list of all DocStructInferface instances referencing the
     * given DocStructInterfaces 'docStruct'.
     * 
     * @param docStruct
     *            the DocStructInterface for which the references are determined
     * @return list of DocStructInterface instances referencing the given
     *         DocStructInterface docStruct
     */
    public List<LegacyDocStructHelperInterface> getPageReferencesToDocStruct(LegacyDocStructHelperInterface docStruct) {
        List<LegacyDocStructHelperInterface> pageReferenceDocStructs = new LinkedList<>();
        List<ReferenceInterface> pageReferences = docStruct.getAllReferences("to");

        for (ReferenceInterface pageReferenceInterface : pageReferences) {
            pageReferenceDocStructs.add(pageReferenceInterface.getTarget());
        }

        return pageReferenceDocStructs;
    }

    /**
     * Retrieve and return list of file paths for pages assigned to given
     * DocStructInterface 'docStruct'.
     * 
     * @param docStruct
     *            DocStructInterfaces for which page file paths are returned.
     * @return list of file paths of pages assigned to given DocStructInterface
     *         'docStruct'.
     */
    @SuppressWarnings("unchecked")
    private List<String> getPagesAssignedToDocStruct(LegacyDocStructHelperInterface docStruct) {
        List<String> assignedPages = new LinkedList<>();
        List<ReferenceInterface> pageReferences = docStruct.getAllReferences("to");
        PrefsInterface prefsInterface = this.metaHelper.getPrefs();
        LegacyMetadataTypeHelper mdt = prefsInterface.getMetadataTypeByName("physPageNumber");

        List<String> allImages = getImages();

        if (!allImages.isEmpty()) {
            for (ReferenceInterface pageReferenceInterface : pageReferences) {
                LegacyDocStructHelperInterface LegacyDocStructHelperInterface = pageReferenceInterface.getTarget();
                List<LegacyMetadataHelper> allMetadata = (List<LegacyMetadataHelper>) LegacyDocStructHelperInterface.getAllMetadataByType(mdt);
                for (LegacyMetadataHelper LegacyMetadataHelper : allMetadata) {
                    assignedPages.add(allImages.get(Integer.parseInt(LegacyMetadataHelper.getValue()) - 1));
                }
            }
        } else {
            logger.error("ERROR: empty list of image file paths!");
        }
        return assignedPages;
    }

    /**
     * Retrieve file path to png image for given DocStructInterface 'pageDocStruct'
     * representing a single scanned page image.
     * 
     * @param pageDocStruct
     *            DocStructInterface for which the corresponding file path to the
     *            png copy is returned.
     * @return file path to the png image of the given DocStructInterface
     *         'pageDoctStruct'.
     */
    public String getPageImageFilePath(LegacyDocStructHelperInterface pageDocStruct) {
        final String errorMessage = "IMAGE_PATH_NOT_FOUND";
        PrefsInterface prefsInterface = this.metaHelper.getPrefs();
        LegacyMetadataTypeHelper mdt = prefsInterface.getMetadataTypeByName("physPageNumber");
        List<String> allImages = getImages();
        List<? extends LegacyMetadataHelper> allMetadata = pageDocStruct.getAllMetadataByType(mdt);

        int imageIndex;

        switch (allMetadata.size()) {
            case 0:
                logger.error("ERROR: metadata of type 'physPageNumber' not found in given page doc struct!");
                return errorMessage;
            case 1:
                imageIndex = Integer.parseInt(allMetadata.get(0).getValue()) - 1;
                if (!allImages.isEmpty() && allImages.size() > imageIndex) {
                    return allImages.get(imageIndex);
                } else {
                    logger.error("ERROR: empty or broken list of image file paths!");
                    return errorMessage;
                }
            default:
                logger.error("WARNING: number of 'physPageNumber' metadata values in given page doc struct is "
                        + allMetadata.size() + " (1 expected)!");
                return errorMessage;
        }
    }

    /**
     * Get list of image file paths for current process.
     *
     * @return List of images.
     */
    public List<String> getImages() {
        updateImagesFolder();
        List<String> imagePaths = new LinkedList<>();
        Path pngDir = Paths.get(fullsizePath);
        ensureDirectoryExists(pngDir);
        try (Stream<Path> streamPaths = Files.list(pngDir)) {
            imagePaths = streamPaths.filter(path -> path.toFile().isFile()).filter(path -> path.toFile().canRead())
                    .filter(path -> path.toString().endsWith(".png")).sorted().map(Path::getFileName)
                    .map(Path::toString).map(filename -> "/images/" + this.process.getId() + "/" + this.subfolderName
                            + "/" + FULLSIZE_FOLDER_NAME + "/" + filename)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return imagePaths;
    }

    /**
     * Determines whether the list of images exists.
     *
     * @return boolean being true when the list contains at least one image
     */
    public boolean isImageListExistent() {
        return !getImages().isEmpty();
    }

    /**
     * Retrieve and return physical page number of given DocStructInterface
     * 'pageDocStruct'.
     * 
     * @param pageDocStruct
     *            DocStructInterface whose physical page number is returned.
     * @return physical page number of given DocStructInterface pageDocStruct
     */
    public int getPhysicalPageNumber(LegacyDocStructHelperInterface pageDocStruct) {
        try {
            return Integer.parseInt(determineMetadata(pageDocStruct, "physPageNumber"));
        } catch (NullPointerException e) {
            return -1;
        }
    }

    /**
     * Checks and returns whether access is granted to the image with the given
     * filepath "imagePath".
     * 
     * @param imagePath
     *            the filepath of the image for which access rights are checked
     * @return true if access is granted and false otherwise
     */
    public boolean isAccessGranted(String imagePath) {
        String imageName = FilenameUtils.getName(imagePath);
        String filePath;
        if (FilenameUtils.getPath(imagePath).endsWith(FULLSIZE_FOLDER_NAME + "/")) {
            filePath = fullsizePath + imageName;
        } else if (FilenameUtils.getPath(imagePath).endsWith(THUMBNAIL_FOLDER_NAME + "/")) {
            filePath = thumbnailPath + imageName;
        } else {
            logger.error("ERROR: Image path '" + imagePath + "' is invalid!");
            return false;
        }
        File image = new File(filePath);
        return image.canRead();
    }

    /**
     * Update the image folder.
     */
    public void updateImagesFolder() {
        String uriSeparator = "/";
        String[] pathParts = this.currentTifFolder.toString().split(uriSeparator);
        if (pathParts.length > 0) {
            subfolderName = pathParts[pathParts.length - 1];
            fullsizePath = imagesFolder + this.process.getId() + File.separator + subfolderName + File.separator
                    + FULLSIZE_FOLDER_NAME + File.separator;
            thumbnailPath = imagesFolder + this.process.getId() + File.separator + subfolderName + File.separator
                    + THUMBNAIL_FOLDER_NAME + File.separator;
        } else {
            logger.error("ERROR: splitting '" + this.currentTifFolder + "' at '" + File.separator
                    + "' resulted in an empty array!");
        }
    }

    private void updateComponent(List<String> componentIDs) {
        PrimeFaces primeFaces = PrimeFaces.current();
        if (primeFaces.isAjaxRequest()) {
            primeFaces.ajax().update(componentIDs);
        }
    }

    /**
     * Get current user.
     *
     * @return User
     */
    public User getCurrentUser() {
        if (this.user == null) {
            this.user = ServiceManager.getUserService().getAuthenticatedUser();
        }
        return this.user;
    }

    /**
     * Get wiki field.
     *
     * @return values for wiki field
     */
    public String[] getWikiField() {
        refreshProcess(this.process);
        String wiki = getProcess().getWikiField();
        if (!wiki.isEmpty()) {
            wiki = wiki.replace("</p>", "");
            String[] comments = wiki.split("<p>");
            if (comments[0].isEmpty()) {
                List<String> list = new ArrayList<>(Arrays.asList(comments));
                list.remove(list.get(0));
                comments = list.toArray(new String[list.size()]);
            }
            return comments;
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Get add to wiki field.
     *
     * @return values for add to wiki field
     */
    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    /**
     * Set add to wiki field.
     *
     * @param addToWikiField
     *            String
     */
    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    /**
     * Add to wiki field.
     */
    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            String comment = ServiceManager.getUserService().getFullName(getCurrentUser()) + ": " + this.addToWikiField;
            ServiceManager.getProcessService().addToWikiField(comment, this.process);
            this.addToWikiField = "";
            try {
                ServiceManager.getProcessService().save(process);
                refreshProcess(process);
            } catch (DataException e) {
                Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation("wikiField") }, logger, e);
            }
        }
        setShowNewComment(false);
    }

    /**
     * Get correction comment.
     *
     * @return value of correction comment
     */
    public boolean isCorrectionComment() {
        return correctionComment;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Set correction comment.
     *
     * @param correctionComment
     *            as boolean
     */
    public void setCorrectionComment(boolean correctionComment) {
        this.correctionComment = correctionComment;
    }

    /**
     * Get problem.
     *
     * @return Problem object
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Set problem.
     *
     * @param problem
     *            object
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    /**
     * Correction message to previous Tasks.
     */
    public List<Task> getPreviousStepsForProblemReporting() {
        refreshProcess(this.process);
        return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
            ServiceManager.getProcessService().getCurrentTask(this.process).getOrdering(), this.process.getId());
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    /**
     * Report the problem.
     *
     *
     */
    public void reportProblem() {
        List<Task> taskList = new ArrayList<>();
        taskList.add(ServiceManager.getProcessService().getCurrentTask(this.process));
        BatchTaskHelper batchStepHelper = new BatchTaskHelper(taskList);
        batchStepHelper.setProblem(getProblem());
        batchStepHelper.reportProblemForSingle();
        refreshProcess(this.process);
        setShowNewComment(false);
        setCorrectionComment(false);
        setProblem(new Problem());
    }

    /**
     * Solve the problem.
     */
    public void solveProblem(String comment) {
        BatchTaskHelper batchStepHelper = new BatchTaskHelper();
        batchStepHelper.solveProblemForSingle(ServiceManager.getProcessService().getCurrentTask(this.process));
        refreshProcess(this.process);
        String wikiField = getProcess().getWikiField();
        wikiField = wikiField.replace(comment.trim(), comment.trim().replace("Red K", "Orange K "));
        ServiceManager.getProcessService().setWikiField(wikiField, this.process);
        try {
            ServiceManager.getProcessService().save(process);
        } catch (DataException e) {
            Helper.setErrorMessage("correctionSolveProblem", logger, e);
        }
        refreshProcess(this.process);
    }

    /**
     * refresh the process in the session.
     *
     * @param process
     *            Object process to refresh
     */
    public void refreshProcess(Process process) {
        try {
            if (process.getId() != 0) {
                ServiceManager.getProcessService().refresh(process);
                setProcess(ServiceManager.getProcessService().getById(process.getId()));
            }

        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find process with ID " + process.getId(), logger, e);
        }
    }

    public void setReferringView(String referringView) {
        this.referringView = referringView;
    }

    public String getReferringView() {
        return this.referringView;
    }

}
