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

package org.kitodo.production.forms;

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.catalogue.CataloguePlugin;
import org.goobi.production.plugin.catalogue.Hit;
import org.goobi.production.plugin.catalogue.QueryBuilder;
import org.jdom.JDOMException;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.UGHException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.ConfigProject;
import org.kitodo.config.DigitalCollection;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessCreationException;
import org.kitodo.exceptions.UghHelperException;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.AdditionalField;
import org.kitodo.production.helper.BeanHelper;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.SelectItemList;
import org.kitodo.production.helper.UghHelper;
import org.kitodo.production.helper.WikiFieldHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyLogicalDocStructTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.metadata.copier.CopierData;
import org.kitodo.production.metadata.copier.DataCopier;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.thread.TaskScriptThread;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

@Named("ProzesskopieForm")
@SessionScoped
public class ProzesskopieForm implements Serializable {
    private static final Logger logger = LogManager.getLogger(ProzesskopieForm.class);
    private static final long serialVersionUID = -4512865679353743L;
    protected static final String ERROR_READ = "errorReading";
    private static final String OPAC_CONFIG = "configurationOPAC";
    private static final String BOUND_BOOK = "boundbook";
    private static final String FIRST_CHILD = "firstchild";
    private static final String LIST_OF_CREATORS = "ListOfCreators";
    private transient MetsService metsService = ServiceManager.getMetsService();

    private int activeTabId = 0;

    /**
     * Get activeTabId.
     *
     * @return value of activeTabId
     */
    public int getActiveTabId() {
        return activeTabId;
    }

    /**
     * Set activeTabId.
     *
     * @param activeTabId
     *            as int
     */
    public void setActiveTabId(int activeTabId) {
        this.activeTabId = activeTabId;
    }

    /**
     * The class SelectableHit represents a hit on the hit list that shows up if
     * a catalogue search yielded more than one result. We need an inner class
     * for this because Faces is strictly object oriented and the always
     * argument-less actions can only be executed relatively to the list entry
     * in question this way if they are concerning elements that are rendered by
     * iterating along a list.
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    public class SelectableHit {
        /**
         * The field hit holds the hit to be rendered as a list entry.
         */
        private final Hit hit;

        /**
         * The field error holds an error message to be rendered as a list entry
         * in case that retrieving the hit failed within the plug-in used for
         * catalogue access.
         */
        private final String error;

        /**
         * Selectable hit constructor. Creates a new SelectableHit object with a
         * hit to show.
         *
         * @param hit
         *            Hit to show
         */
        public SelectableHit(Hit hit) {
            this.hit = hit;
            error = null;
        }

        /**
         * Selectable hit constructor. Creates a new SelectableHit object with
         * an error message to show.
         *
         * @param error
         *            error message
         */
        public SelectableHit(String error) {
            hit = null;
            this.error = error;
        }

        /**
         * The function getBibliographicCitation() returns a summary of this hit
         * in bibliographic citation style as HTML as read-only property
         * “bibliographicCitation”.
         *
         * @return a summary of this hit in bibliographic citation style as HTML
         */
        public String getBibliographicCitation() {
            return hit.getBibliographicCitation();
        }

        /**
         * The function getErrorMessage() returns an error if that had occurred
         * when trying to retrieve that hit from the catalogue as read-only
         * property “errorMessage”.
         *
         * @return an error message to be rendered as a list entry
         */
        public String getErrorMessage() {
            return error;
        }

        /**
         * The function isError() returns whether an error occurred when trying
         * to retrieve that hit from the catalogue as read-only property
         * “error”.
         *
         * @return whether an error occurred when retrieving that hit
         */
        public boolean isError() {
            return hit == null;
        }

        /**
         * The function selectClick() is called if the user clicks on a
         * catalogue hit summary in order to import it into Production.
         */
        public void selectClick() {
            try {
                importHit(hit);
            } catch (PreferencesException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_READ, new Object[] {"OPAC" }, logger, e);
            } finally {
                hitlistPage = -1;
            }
        }
    }

    private static final String DIRECTORY_SUFFIX = ConfigCore.getParameterOrDefaultValue(ParameterCore.DIRECTORY_SUFFIX);
    private String addToWikiField = "";
    private String atstsl = "";
    private Integer guessedImages = 0;
    private Process processForChoice;

    /**
     * The field hitlist holds some reference to the hitlist retrieved from a
     * library catalogue. The internals of this object are subject to the plugin
     * implementation and are not to be accessed directly.
     */
    private Object hitlist;

    /**
     * The field hitlistPage holds the zero-based index of the page of the
     * hitlist currently showing. A negative value means that the hitlist is
     * hidden, otherwise it is showing the respective page.
     */
    private long hitlistPage = -1;
    /**
     * The field hits holds the number of hits in the hitlist last retrieved
     * from a library catalogue.
     */
    private long hits;

    /**
     * The field importCatalogue holds the catalogue plugin used to access the
     * library catalogue.
     */
    private transient CataloguePlugin importCatalogue;

    private FileformatInterface rdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private List<String> workflowConditions = new ArrayList<>();
    private static final String REDIRECT_PATH = "/pages/{0}?" + "faces-redirect=true";
    private String processListPath = MessageFormat.format(REDIRECT_PATH, "processes");
    private String processFromTemplatePath = MessageFormat.format(REDIRECT_PATH, "processFromTemplate");

    protected String docType;
    protected Template template = new Template();
    protected Process prozessKopie = new Process();
    protected Project project;
    protected boolean useOpac;
    protected boolean useTemplates;
    protected transient List<AdditionalField> additionalFields;
    protected transient Map<String, Boolean> standardFields;
    protected String tifDefinition;
    protected String titleDefinition;
    protected String tifHeaderImageDescription = "";
    protected String tifHeaderDocumentName = "";
    protected transient List<String> digitalCollections;
    protected transient List<String> possibleDigitalCollection;

    protected static final String INCOMPLETE_DATA = "errorDataIncomplete";

    /**
     * Prepare template and project for which new process will be created.
     *
     * @param templateId
     *            id of template to query from database
     * @param projectId
     *            id of project to query from database
     *
     * @return path to page with form
     */
    public String prepare(int templateId, int projectId) {
        atstsl = "";
        try {
            this.template = ServiceManager.getTemplateService().getById(templateId);
            this.project = ServiceManager.getProjectService().getById(projectId);
        } catch (DAOException e) {
            Helper.setErrorMessage("Template with id " + templateId + " or project with id " + projectId + " not found.", logger, e);
            return null;
        }

        if (ServiceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            ServiceManager.getTaskService().setUpErrorMessagesForUnreachableTasks(this.template.getTasks());
            return null;
        }

        clearValues();
        readProjectConfigs();
        this.rdf = null;
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setTemplate(this.template);
        this.prozessKopie.setProject(this.project);
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.prozessKopie.setDocket(this.template.getDocket());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.template, this.prozessKopie);

        initializePossibleDigitalCollections();

        return processFromTemplatePath;
    }

    /**
     * Get Process templates.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProcessesForChoiceList() {
        return SelectItemList.getProcessesForChoiceList();
    }

    /**
     * The function evaluateOpac() is executed if a user clicks the command link
     * to start a catalogue search. It performs the search and loads the hit if
     * it is unique. Otherwise, it will cause a hit list to show up for the user
     * to select a hit.
     */
    public void evaluateOpac() {
        long timeout = CataloguePlugin.getTimeout();
        clearValues();
        RequestContext.getCurrentInstance().update("hitlistForm");
        try {
            readProjectConfigs();
            if (pluginAvailableFor(opacKatalog)) {
                String query = QueryBuilder.restrictToField(opacSuchfeld, opacSuchbegriff);
                query = QueryBuilder.appendAll(query, ConfigOpac.getRestrictionsForCatalogue(opacKatalog));

                hitlist = importCatalogue.find(query, timeout);
                hits = importCatalogue.getNumberOfHits(hitlist, timeout);

                String message = MessageFormat.format(Helper.getTranslation("newProcess.catalogueSearch.results"),
                    hits);

                switch ((int) Math.min(hits, Integer.MAX_VALUE)) {
                    case 0:
                        Helper.setErrorMessage(message);
                        break;
                    case 1:
                        importHit(importCatalogue.getHit(hitlist, 0, timeout));
                        Helper.setMessage(message);
                        break;
                    default:
                        hitlistPage = 0; // show first page of hitlist
                        Helper.setMessage(message);
                        RequestContext.getCurrentInstance().execute("PF('hitlistDialog').show()");
                        break;
                }
            } else {
                Helper.setErrorMessage("ERROR: No suitable plugin available for OPAC '" + opacKatalog + "'");
            }
        } catch (FileNotFoundException | PreferencesException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"OPAC " + opacKatalog }, logger, e);
        }
    }

    /**
     * Read project configs for display in GUI.
     */
    protected void readProjectConfigs() {
        ConfigProject cp;
        try {
            cp = new ConfigProject(this.project.getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        this.docType = cp.getDocType();
        this.useOpac = cp.isUseOpac();
        this.useTemplates = cp.isUseTemplates();
        if (this.opacKatalog.equals("")) {
            this.opacKatalog = cp.getOpacCatalog();
        }

        this.tifDefinition = cp.getTifDefinition();
        this.titleDefinition = cp.getTitleDefinition();

        this.standardFields.putAll(cp.getHiddenFields());
        this.additionalFields = cp.getAdditionalFields();
    }

    /**
     * The function pluginAvailableFor(catalogue) verifies that a plugin
     * suitable for accessing the library catalogue identified by the given
     * String is available in the global variable importCatalogue. If
     * importCatalogue is empty or the current plugin doesn’t support the given
     * catalogue, the function will try to load a suitable plugin. Upon success
     * the preferences and the catalogue to use will be configured in the
     * plugin, otherwise an error message will be set to be shown.
     *
     * @param catalogue
     *            identifier string for the catalogue that the plugin shall
     *            support
     * @return whether a plugin is available in the global variable
     *         importCatalogue
     */
    private boolean pluginAvailableFor(String catalogue) {
        if (importCatalogue == null || !importCatalogue.supportsCatalogue(catalogue)) {
            importCatalogue = PluginLoader.getCataloguePluginForCatalogue(catalogue);
        }
        if (importCatalogue == null) {
            Helper.setErrorMessage("NoCataloguePluginForCatalogue", catalogue);
            return false;
        } else {
            importCatalogue
                    .setPreferences(ServiceManager.getRulesetService().getPreferences(prozessKopie.getRuleset()));
            importCatalogue.useCatalogue(catalogue);
            return true;
        }
    }

    /**
     * Reset all configuration properties and fields.
     */
    protected void clearValues() {
        if (this.opacKatalog == null) {
            this.opacKatalog = "";
        }
        this.standardFields = new HashMap<>();
        this.standardFields.put("collections", true);
        this.standardFields.put("doctype", true);
        this.standardFields.put("regelsatz", true);
        this.standardFields.put("images", true);
        this.additionalFields = new ArrayList<>();
        this.tifHeaderDocumentName = "";
        this.tifHeaderImageDescription = "";
    }

    /**
     * The method importHit() loads a hit into the display.
     *
     * @param hit
     *            Hit to load
     */
    protected void importHit(Hit hit) throws PreferencesException {
        rdf = hit.getFileformat();
        docType = hit.getDocType();
        fillFieldsFromMetadataFile();
        applyCopyingRules(new CopierData(rdf, this.template));
        atstsl = createAtstsl(hit.getTitle(), hit.getAuthors());
        setActiveTabId(0);
    }

    /**
     * Creates a DataCopier with the given configuration, lets it process the
     * given data and wraps any errors to display in the front end.
     *
     * @param data
     *            data to process
     */
    private void applyCopyingRules(CopierData data) {
        String rules = ConfigCore.getParameter(ParameterCore.COPY_DATA_ON_CATALOGUE_QUERY);
        if (Objects.nonNull(rules)) {
            try {
                new DataCopier(rules).process(data);
            } catch (ConfigurationException e) {
                Helper.setErrorMessage("dataCopier.syntaxError", logger, e);
            }
        }
    }

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei
     * füllen.
     */
    private void fillFieldsFromMetadataFile() throws PreferencesException {
        if (this.rdf != null) {
            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    proceedField(field);
                    if (field.getValue() != null && !field.getValue().equals("")) {
                        field.setValue(field.getValue().replace("&amp;", "&"));
                    }
                }
            }
        }
    }

    private void proceedField(AdditionalField field) throws PreferencesException {
        LegacyDocStructHelperInterface docStruct = getDocStruct(field);
        try {
            if (field.getMetadata().equals(LIST_OF_CREATORS)) {
                field.setValue(getAuthors(docStruct.getAllPersons()));
            } else {
                // evaluate the content in normal fields
                LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(
                    ServiceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                    field.getMetadata());
                LegacyMetadataHelper md = UghHelper.getMetadata(docStruct, mdt);
                if (md != null) {
                    field.setValue(md.getValue());
                    md.setStringValue(field.getValue().replace("&amp;", "&"));
                }
            }
        } catch (UghHelperException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private LegacyDocStructHelperInterface getDocStruct(AdditionalField field) throws PreferencesException {
        LegacyMetsModsDigitalDocumentHelper digitalDocument = this.rdf.getDigitalDocument();
        LegacyDocStructHelperInterface docStruct = digitalDocument.getLogicalDocStruct();
        if (field.getDocstruct().equals(FIRST_CHILD)) {
            docStruct = digitalDocument.getLogicalDocStruct().getAllChildren().get(0);
        }
        if (field.getDocstruct().equals(BOUND_BOOK)) {
            docStruct = digitalDocument.getPhysicalDocStruct();
        }
        return docStruct;
    }

    /**
     * Get together authors' names.
     *
     * @param persons
     *            list of persons
     * @return authors' names as String
     */
    protected String getAuthors(List<PersonInterface> persons) {
        StringBuilder authors = new StringBuilder();

        if (persons != null) {
            for (PersonInterface p : persons) {
                authors.append(p.getLastName());
                if (StringUtils.isNotBlank(p.getFirstName())) {
                    authors.append(", ");
                    authors.append(p.getFirstName());
                }
                authors.append("; ");
            }
            if (authors.toString().endsWith("; ")) {
                authors.setLength(authors.length() - 2);
            }
        }

        return authors.toString();
    }

    /**
     * Auswahl des Prozesses auswerten.
     */
    public String templateAuswahlAuswerten() {
        if (ServiceManager.getProcessService().getWorkpiecesSize(this.processForChoice) > 0) {
            for (Property workpieceProperty : this.processForChoice.getWorkpieces()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitle().equals(workpieceProperty.getTitle())) {
                        field.setValue(workpieceProperty.getValue());
                    }
                    if (workpieceProperty.getTitle().equals("DocType")) {
                        docType = workpieceProperty.getValue();
                    }
                }
            }
        }

        if (ServiceManager.getProcessService().getTemplatesSize(this.processForChoice) > 0) {
            for (Property templateProperty : this.processForChoice.getTemplates()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitle().equals(templateProperty.getTitle())) {
                        field.setValue(templateProperty.getValue());
                    }
                }
            }
        }

        if (ServiceManager.getProcessService().getPropertiesSize(this.processForChoice) > 0) {
            for (Property processProperty : this.processForChoice.getProperties()) {
                if (processProperty.getTitle().equals("digitalCollection")) {
                    digitalCollections.add(processProperty.getValue());
                }
            }
        }

        try {
            this.rdf = ServiceManager.getProcessService().readMetadataAsTemplateFile(this.processForChoice);
        } catch (ReadException | PreferencesException | IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"template-metadata" }, logger, e);
        }

        removeCollectionsForChildren();
        return null;
    }

    /**
     * If there is a first child, the collections are for it.
     */
    private void removeCollectionsForChildren() {
        try {
            LegacyDocStructHelperInterface colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct, this.prozessKopie);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct, this.prozessKopie);
        } catch (PreferencesException e) {
            Helper.setErrorMessage("Error on creating process", logger, e);
        } catch (RuntimeException e) {
            logger.debug("das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden", e);
        }
    }

    private boolean isContentValid(boolean criticiseEmptyTitle) {
        boolean valid = true;

        if (criticiseEmptyTitle) {
            valid = isProcessTitleCorrect(this.prozessKopie);
        }

        /*
         * Prüfung der standard-Eingaben, die angegeben werden müssen
         */
        /* keine Collektion ausgewählt */
        if (this.standardFields.get("collections") && getDigitalCollections().isEmpty()) {
            valid = false;
            Helper.setErrorMessage(INCOMPLETE_DATA, "processCreationErrorNoCollection");
        }

        /*
         * Prüfung der additional-Eingaben, die angegeben werden müssen
         */
        for (AdditionalField field : this.additionalFields) {
            if ((field.getValue() == null || field.getValue().equals("")) && field.isRequired()
                    && field.getShowDependingOnDoctype() && (StringUtils.isBlank(field.getValue()))) {
                valid = false;
                Helper.setErrorMessage(INCOMPLETE_DATA,
                    " " + field.getTitle() + " " + Helper.getTranslation("processCreationErrorFieldIsEmpty"));

            }
        }
        return valid;
    }

    protected boolean isProcessTitleCorrect(Process process) {
        boolean valid = true;

        if (process.getTitle() == null || process.getTitle().equals("")) {
            valid = false;
            Helper.setErrorMessage(INCOMPLETE_DATA, "processTitleEmpty");
        }

        String validateRegEx = ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_PROCESS_TITLE_REGEX);
        if (!process.getTitle().matches(validateRegEx)) {
            valid = false;
            Helper.setErrorMessage("processTitleInvalid", new Object[] {validateRegEx});
        }

        if (process.getTitle() != null) {
            valid = isProcessTitleAvailable(process.getTitle());
        }
        return valid;
    }

    /**
     * Checks if process title is available. If yes, return true, if no, return
     * false.
     *
     * @param title
     *            of process
     * @return boolean
     */
    protected boolean isProcessTitleAvailable(String title) {
        long amount;
        try {
            amount = ServiceManager.getProcessService().findNumberOfProcessesWithTitle(title);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation("process") }, logger, e);
            return false;
        }
        if (amount > 0) {
            Helper.setErrorMessage(Helper.getTranslation(INCOMPLETE_DATA)
                    + Helper.getTranslation("processTitleAlreadyInUse"));
            return false;
        }
        return true;
    }

    /**
     * Anlegen des Prozesses und save der Metadaten.
     */
    public String createNewProcess() {
        if (!isContentValid(true)) {
            return null;
        }
        addProperties();
        updateTasks(this.prozessKopie);

        try {
            this.prozessKopie.setSortHelperImages(this.guessedImages);
            ServiceManager.getProcessService().save(this.prozessKopie);
            ServiceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            Helper.setErrorMessage("errorCreating", new Object[] {Helper.getTranslation("process") }, logger, e);
            return null;
        }

        String baseProcessDirectory = ServiceManager.getProcessService().getProcessDataDirectory(this.prozessKopie)
                .toString();
        boolean successful = false;
        try {
            successful = ServiceManager.getFileService().createMetaDirectory(URI.create(""), baseProcessDirectory);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        if (!successful) {
            String message = "Metadata directory: " + baseProcessDirectory + " in path: "
                    + ConfigCore.getKitodoDataDirectory() + " was not created!";
            logger.error(message);
            Helper.setErrorMessage(message);
            return null;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage
         * stattfand, dann jetzt eine anlegen
         */
        if (this.rdf == null) {
            createNewFileformat();
        }

        /*
         * wenn eine RDF-Konfiguration vorhanden ist (z.B. aus dem Opac-Import,
         * oder frisch angelegt), dann diese ergänzen
         */
        try {
            if (this.rdf != null) {
                insertLogicalDocStruct();

                for (AdditionalField field : this.additionalFields) {
                    if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                        processAdditionalField(field);
                    }
                }

                updateMetadata();
                insertCollections();
                insertImagePath();
            }

            // Create configured directories
            ServiceManager.getProcessService().createProcessDirs(this.prozessKopie);
            ServiceManager.getProcessService().readMetadataFile(this.prozessKopie);

            startTaskScriptThreads();

        } catch (IOException | PreferencesException | WriteException | ReadException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return processListPath;
    }

    private void processAdditionalField(AdditionalField field) throws PreferencesException {
        // which DocStruct
        LegacyDocStructHelperInterface tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
        LegacyDocStructHelperInterface tempChild = null;
        String fieldDocStruct = field.getDocstruct();
        if (fieldDocStruct.equals(FIRST_CHILD)) {
            try {
                tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
            } catch (RuntimeException e) {
                Helper.setErrorMessage(
                    e.getMessage() + " The first child below the top structure could not be determined!", logger, e);
            }
        }
        // if topstruct and first child should get the metadata
        if (!fieldDocStruct.equals(FIRST_CHILD) && fieldDocStruct.contains(FIRST_CHILD)) {
            try {
                tempChild = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
            } catch (RuntimeException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
        if (fieldDocStruct.equals(BOUND_BOOK)) {
            tempStruct = this.rdf.getDigitalDocument().getPhysicalDocStruct();
        }
        // which Metadata
        try {
            // except for the authors, take all additional into the metadata
            if (!field.getMetadata().equals(LIST_OF_CREATORS)) {
                PrefsInterface prefs = ServiceManager.getRulesetService()
                        .getPreferences(this.prozessKopie.getRuleset());
                LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(prefs, field.getMetadata());
                LegacyMetadataHelper metadata = UghHelper.getMetadata(tempStruct, mdt);
                if (Objects.nonNull(metadata)) {
                    metadata.setStringValue(field.getValue());
                }
                // if the topstruct and the first child should be given the
                // value
                if (Objects.nonNull(tempChild)) {
                    metadata = UghHelper.getMetadata(tempChild, mdt);
                    if (Objects.nonNull(metadata)) {
                        metadata.setStringValue(field.getValue());
                    }
                }
            }
        } catch (UghHelperException | RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * There must be at least one non-anchor level doc struct, if missing,
     * insert logical doc structures until you reach it.
     */
    private void insertLogicalDocStruct() {
        LegacyDocStructHelperInterface populizer = null;
        try {
            populizer = rdf.getDigitalDocument().getLogicalDocStruct();
            if (populizer.getAnchorClass() != null && populizer.getAllChildren() == null) {
                PrefsInterface ruleset = ServiceManager.getRulesetService().getPreferences(prozessKopie.getRuleset());
                LegacyLogicalDocStructTypeHelper docStructType = populizer.getDocStructType();
                while (docStructType.getAnchorClass() != null) {
                    populizer = populizer.createChild(docStructType.getAllAllowedDocStructTypes().get(0),
                        rdf.getDigitalDocument(), ruleset);
                }
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            String name = populizer != null && populizer.getDocStructType() != null
                    ? populizer.getDocStructType().getName()
                    : null;
            Helper.setErrorMessage("DocStrctType: " + name + " is configured as anchor but has no allowedchildtype.",
                logger, e);
        } catch (UGHException catchAll) {
            Helper.setErrorMessage(catchAll.getLocalizedMessage(), logger, catchAll);
        }
    }

    private void insertCollections() throws PreferencesException {
        LegacyDocStructHelperInterface colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
        if (Objects.nonNull(colStruct) && Objects.nonNull(colStruct.getAllChildren())
                && !colStruct.getAllChildren().isEmpty()) {
            try {
                addCollections(colStruct);
                // falls ein erstes Kind vorhanden ist, sind die Collectionen
                // dafür
                colStruct = colStruct.getAllChildren().get(0);
                addCollections(colStruct);
            } catch (RuntimeException e) {
                Helper.setErrorMessage("The first child below the top structure could not be determined!", logger, e);
            }
        }
    }

    /**
     * Insert image path and delete any existing ones first.
     */
    private void insertImagePath() throws IOException, PreferencesException, WriteException {
        LegacyMetsModsDigitalDocumentHelper digitalDocument = this.rdf.getDigitalDocument();
        try {
            LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(this.prozessKopie, "pathimagefiles");
            List<? extends LegacyMetadataHelper> allImagePaths = digitalDocument.getPhysicalDocStruct()
                    .getAllMetadataByType(mdt);
            if (Objects.nonNull(allImagePaths)) {
                for (LegacyMetadataHelper metadata : allImagePaths) {
                    digitalDocument.getPhysicalDocStruct().getAllMetadata().remove(metadata);
                }
            }
            LegacyMetadataHelper newMetadata = new LegacyMetadataHelper(mdt);
            String path = ServiceManager.getFileService().getImagesDirectory(this.prozessKopie)
                    + this.prozessKopie.getTitle().trim() + "_" + DIRECTORY_SUFFIX;
            if (SystemUtils.IS_OS_WINDOWS) {
                newMetadata.setStringValue("file:/" + path);
            } else {
                newMetadata.setStringValue("file://" + path);
            }
            digitalDocument.getPhysicalDocStruct().addMetadata(newMetadata);

            // write Rdf file
            ServiceManager.getFileService().writeMetadataFile(this.rdf, this.prozessKopie);
        } catch (DocStructHasNoTypeException e) {
            Helper.setErrorMessage("DocStructHasNoTypeException", logger, e);
        } catch (UghHelperException e) {
            Helper.setErrorMessage("UghHelperException", logger, e);
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setErrorMessage("MetadataTypeNotAllowedException", logger, e);
        }
    }

    protected void updateTasks(Process process) {
        for (Task task : process.getTasks()) {
            // always save date and user for each step
            task.setProcessingTime(process.getCreationDate());
            task.setEditTypeEnum(TaskEditType.AUTOMATIC);
            // only if its done, set edit start and end date
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                task.setProcessingBegin(process.getCreationDate());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date date = new Date();
                task.setProcessingTime(date);
                task.setProcessingEnd(date);
            }
        }
    }

    /**
     * Metadata inheritance and enrichment.
     */
    private void updateMetadata() throws PreferencesException {
        if (ConfigCore.getBooleanParameter(ParameterCore.USE_METADATA_ENRICHMENT)) {
            LegacyDocStructHelperInterface enricher = rdf.getDigitalDocument().getLogicalDocStruct();
            Map<String, Map<String, LegacyMetadataHelper>> higherLevelMetadata = new HashMap<>();
            while (enricher.getAllChildren() != null) {
                // save higher level metadata for lower enrichment
                List<LegacyMetadataHelper> allMetadata = enricher.getAllMetadata();
                if (allMetadata == null) {
                    allMetadata = Collections.emptyList();
                }
                iterateOverAllMetadata(higherLevelMetadata, allMetadata);

                // enrich children with inherited metadata
                for (LegacyDocStructHelperInterface nextChild : enricher.getAllChildren()) {
                    enricher = nextChild;
                    iterateOverHigherLevelMetadata(enricher, higherLevelMetadata);
                }
            }
        }
    }

    private void iterateOverAllMetadata(Map<String, Map<String, LegacyMetadataHelper>> higherLevelMetadata,
            List<LegacyMetadataHelper> allMetadata) {
        for (LegacyMetadataHelper available : allMetadata) {
            String availableKey = available.getMetadataType().getName();
            String availableValue = available.getValue();
            Map<String, LegacyMetadataHelper> availableMetadata = higherLevelMetadata.containsKey(availableKey)
                    ? higherLevelMetadata.get(availableKey)
                    : new HashMap<>();
            if (!availableMetadata.containsKey(availableValue)) {
                availableMetadata.put(availableValue, available);
            }
            higherLevelMetadata.put(availableKey, availableMetadata);
        }
    }

    private void iterateOverHigherLevelMetadata(LegacyDocStructHelperInterface enricher,
            Map<String, Map<String, LegacyMetadataHelper>> higherLevelMetadata) {
        for (Entry<String, Map<String, LegacyMetadataHelper>> availableHigherMetadata : higherLevelMetadata.entrySet()) {
            String enrichable = availableHigherMetadata.getKey();
            boolean addable = false;
            List<LegacyMetadataTypeHelper> addableTypesNotNull = enricher.getAddableMetadataTypes();
            if (Objects.isNull(addableTypesNotNull)) {
                addableTypesNotNull = Collections.emptyList();
            }
            for (LegacyMetadataTypeHelper addableMetadata : addableTypesNotNull) {
                if (addableMetadata.getName().equals(enrichable)) {
                    addable = true;
                    break;
                }
            }
            if (!addable) {
                continue;
            }
            there: for (Entry<String, LegacyMetadataHelper> higherElement : availableHigherMetadata.getValue()
                    .entrySet()) {
                List<LegacyMetadataHelper> amNotNull = enricher.getAllMetadata();
                if (Objects.isNull(amNotNull)) {
                    amNotNull = Collections.emptyList();
                }
                for (LegacyMetadataHelper existentMetadata : amNotNull) {
                    if (existentMetadata.getMetadataType().getName().equals(enrichable)
                            && existentMetadata.getValue().equals(higherElement.getKey())) {
                        continue there;
                    }
                }
                try {
                    enricher.addMetadata(higherElement.getValue());
                } catch (UGHException e) {
                    Helper.setErrorMessage("errorAdding", new Object[] {Helper.getTranslation("metadata") }, logger, e);
                }
            }
        }
    }

    private void startTaskScriptThreads() {
        /* damit die Sortierung stimmt nochmal einlesen */
        ServiceManager.getProcessService().refresh(this.prozessKopie);

        List<Task> tasks = this.prozessKopie.getTasks();
        for (Task task : tasks) {
            if (task.getProcessingStatus() == 1 && task.isTypeAutomatic()) {
                TaskScriptThread thread = new TaskScriptThread(task);
                thread.start();
            }
        }
    }

    private void addCollections(LegacyDocStructHelperInterface colStruct) {
        for (String s : this.digitalCollections) {
            try {
                LegacyMetadataHelper md = new LegacyMetadataHelper(UghHelper.getMetadataType(
                    ServiceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                    "singleDigCollection"));
                md.setStringValue(s);
                md.setDocStruct(colStruct);
                colStruct.addMetadata(md);
            } catch (UghHelperException | DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                Helper.setErrorMessage(e.getMessage(), logger, e);
            }
        }
    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen.
     */
    protected void removeCollections(LegacyDocStructHelperInterface colStruct, Process process) {
        try {
            LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(
                ServiceManager.getRulesetService().getPreferences(process.getRuleset()), "singleDigCollection");
            ArrayList<LegacyMetadataHelper> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt));
            for (LegacyMetadataHelper md : myCollections) {
                colStruct.removeMetadata(md);
            }
        } catch (UghHelperException | DocStructHasNoTypeException e) {
            Helper.setErrorMessage(e.getMessage(), logger, e);
        }
    }

    /**
     * Creates a new file format. When a new process is created, an empty METS
     * file must be created for it.
     */
    public void createNewFileformat() {
        RulesetManagementInterface ruleset = ((LegacyPrefsHelper) ServiceManager.getRulesetService()
                .getPreferences(this.prozessKopie.getRuleset())).getRuleset();
        try {
            MetsXmlElementAccessInterface workpiece = metsService.createMetsXmlElementAccess();
            DivXmlElementAccessInterface structure = workpiece.getStructMap();
            ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(this.docType);
            if (configOpacDoctype != null) {
                // monograph
                if (!configOpacDoctype.isPeriodical() && !configOpacDoctype.isMultiVolume()) {
                    workpiece.getStructMap().setType(configOpacDoctype.getRulesetType());
                    this.rdf = new LegacyMetsModsDigitalDocumentHelper(ruleset, workpiece);
                } else if (configOpacDoctype.isPeriodical()) {
                    // journal
                    structure.setType("Periodical");
                    addChild(structure, "PeriodicalVolume");
                    this.rdf = new LegacyMetsModsDigitalDocumentHelper(ruleset, workpiece);
                } else if (configOpacDoctype.isMultiVolume()) {
                    // volume of a multi-volume publication
                    structure.setType("MultiVolumeWork");
                    addChild(structure, "Volume");
                    this.rdf = new LegacyMetsModsDigitalDocumentHelper(ruleset, workpiece);
                }
            }
            if (this.docType.equals("volumerun")) {
                structure.setType("VolumeRun");
                addChild(structure, "Record");
                this.rdf = new LegacyMetsModsDigitalDocumentHelper(ruleset, workpiece);
            }
        } catch (FileNotFoundException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
        }
    }

    /**
     * Adds a child node to a part of the logical structure tree.
     * 
     * @param structure
     *            tree to add to
     * @param type
     *            type of child to create
     */
    private void addChild(DivXmlElementAccessInterface structure, String type) {
        DivXmlElementAccessInterface volume = metsService.createDivXmlElementAccess();
        volume.setType(type);
        structure.getChildren().add(volume);
    }

    private void addProperties() {
        addAdditionalFields(this.additionalFields, this.prozessKopie);

        for (String col : digitalCollections) {
            BeanHelper.addPropertyForProcess(this.prozessKopie, "digitalCollection", col);
        }

        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
            this.tifHeaderImageDescription);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        BeanHelper.addPropertyForProcess(this.prozessKopie, "Template", this.template.getTitle());
        BeanHelper.addPropertyForProcess(this.prozessKopie, "TemplateID", String.valueOf(this.template.getId()));
    }

    protected void addAdditionalFields(List<AdditionalField> additionalFields, Process process) {
        for (AdditionalField field : additionalFields) {
            if (field.getShowDependingOnDoctype()) {
                if (field.getFrom().equals("werk")) {
                    BeanHelper.addPropertyForWorkpiece(process, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("vorlage")) {
                    BeanHelper.addPropertyForTemplate(process, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("prozess")) {
                    BeanHelper.addPropertyForProcess(process, field.getTitle(), field.getValue());
                }
            }
        }
    }

    public String getDocType() {
        return this.docType;
    }

    /**
     * Set document type.
     *
     * @param docType
     *            String
     */
    public void setDocType(String docType) {
        if (!this.docType.equals(docType)) {
            this.docType = docType;
            if (rdf != null) {

                FileformatInterface tmp = rdf;

                createNewFileformat();
                try {
                    if (rdf.getDigitalDocument().getLogicalDocStruct()
                            .equals(tmp.getDigitalDocument().getLogicalDocStruct())) {
                        rdf = tmp;
                    } else {
                        LegacyDocStructHelperInterface oldLogicalDocstruct = tmp.getDigitalDocument().getLogicalDocStruct();
                        LegacyDocStructHelperInterface newLogicalDocstruct = rdf.getDigitalDocument().getLogicalDocStruct();
                        // both have no children
                        if (oldLogicalDocstruct.getAllChildren() == null
                                && newLogicalDocstruct.getAllChildren() == null) {
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                        } else if (oldLogicalDocstruct.getAllChildren() != null
                                && newLogicalDocstruct.getAllChildren() == null) {
                            // old has a child, new has no child
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.getAllChildren().get(0), newLogicalDocstruct);
                        } else if (oldLogicalDocstruct.getAllChildren() == null
                                && newLogicalDocstruct.getAllChildren() != null) {
                            // new has a child, but old not
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.copy(true, false),
                                newLogicalDocstruct.getAllChildren().get(0));
                        } else if (oldLogicalDocstruct.getAllChildren() != null
                                && newLogicalDocstruct.getAllChildren() != null) {
                            // both have children
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.getAllChildren().get(0),
                                newLogicalDocstruct.getAllChildren().get(0));
                        }
                    }
                } catch (PreferencesException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
                try {
                    fillFieldsFromMetadataFile();
                } catch (PreferencesException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
    }

    private void copyMetadata(LegacyDocStructHelperInterface oldDocStruct, LegacyDocStructHelperInterface newDocStruct) {

        if (oldDocStruct.getAllMetadata() != null) {
            for (LegacyMetadataHelper md : oldDocStruct.getAllMetadata()) {
                try {
                    newDocStruct.addMetadata(md);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
        if (oldDocStruct.getAllPersons() != null) {
            for (PersonInterface p : oldDocStruct.getAllPersons()) {
                try {
                    newDocStruct.addPerson(p);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
    }

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * The function getProzessVorlageTitel() returns some kind of identifier for
     * this ProzesskopieForm. The title of the process template that a process
     * will be created from can be considered with some reason to be some good
     * identifier for the ProzesskopieForm, too.
     *
     * @return a human-readable identifier for this object
     */
    public String getProzessVorlageTitel() {
        return this.template != null ? this.template.getTitle() : null;
    }

    /**
     * Set template.
     *
     * @param template
     *            as Template object
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Get process for choice list.
     *
     * @return process for choice list
     */
    public Process getProcessForChoice() {
        return this.processForChoice;
    }

    /**
     * Set process for choice list.
     *
     * @param processForChoice as Process object
     */
    public void setProcessForChoice(Process processForChoice) {
        this.processForChoice = processForChoice;
    }

    public List<AdditionalField> getAdditionalFields() {
        return this.additionalFields;
    }

    /**
     * The method getVisibleAdditionalFields returns a list of visible
     * additional fields.
     *
     * @return list of AdditionalField
     */
    public List<AdditionalField> getVisibleAdditionalFields() {
        return this.getAdditionalFields().stream().filter(AdditionalField::getShowDependingOnDoctype)
                .collect(Collectors.toList());
    }

    /**
     * The method setAdditionalField() sets the value of an AdditionalField held
     * by a ProzesskopieForm object.
     *
     * @param key
     *            the title of the AdditionalField whose value shall be modified
     * @param value
     *            the new value for the AdditionalField
     * @param strict
     *            throw a RuntimeException if the field is unknown
     * @throws ProcessCreationException
     *             in case that no field with a matching title was found in the
     *             ProzesskopieForm object
     */
    public void setAdditionalField(String key, String value, boolean strict) {
        boolean unknownField = true;
        for (AdditionalField field : additionalFields) {
            if (key.equals(field.getTitle())) {
                field.setValue(value);
                unknownField = false;
            }
        }
        if (unknownField && strict) {
            throw new ProcessCreationException(
                    "Couldn’t set “" + key + "” to “" + value + "”: No such field in record.");
        }
    }

    public void setAdditionalFields(List<AdditionalField> additionalFields) {
        this.additionalFields = additionalFields;
    }

    /**
     * This is needed for GUI, render multiple select only if this is false if
     * this is true use the only choice.
     *
     * @return true or false
     */
    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);
    }

    /**
     * Get possible digital collections if single choice.
     *
     * @return possible digital collections if single choice
     */
    public String getDigitalCollectionIfSingleChoice() {
        List<String> pdc = getPossibleDigitalCollections();
        if (pdc.size() == 1) {
            return pdc.get(0);
        } else {
            return null;
        }
    }

    public List<String> getPossibleDigitalCollections() {
        return this.possibleDigitalCollection;
    }

    protected void initializePossibleDigitalCollections() {
        try {
            DigitalCollection.possibleDigitalCollectionsForProcess(this.prozessKopie.getProject());
        } catch (JDOMException | IOException e) {
            Helper.setErrorMessage("Error while parsing digital collections", logger, e);
        }

        this.possibleDigitalCollection = DigitalCollection.getPossibleDigitalCollection();
        this.digitalCollections = DigitalCollection.getDigitalCollections();

        // if only one collection is possible take it directly
        if (isSingleChoiceCollection()) {
            this.digitalCollections.add(getDigitalCollectionIfSingleChoice());
        }
    }

    /**
     * Get all OPAC catalogues.
     *
     * @return list of catalogues
     */
    public List<String> getAllOpacCatalogues() {
        try {
            return ConfigOpac.getAllCatalogueTitles();
        } catch (RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all document types.
     *
     * @return list of ConfigOpacDoctype objects
     */
    public List<ConfigOpacDoctype> getAllDoctypes() {
        try {
            return ConfigOpac.getAllDoctypes();
        } catch (RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Changed, so that on first request list gets set if there is only one
     * choice.
     *
     * @return list of digital collections
     */
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    public Map<String, Boolean> getStandardFields() {
        return this.standardFields;
    }

    public boolean isUseOpac() {
        return this.useOpac;
    }

    public boolean isUseTemplates() {
        return this.useTemplates;
    }

    public String getTifHeaderDocumentName() {
        return this.tifHeaderDocumentName;
    }

    public void setTifHeaderDocumentName(String tifHeaderDocumentName) {
        this.tifHeaderDocumentName = tifHeaderDocumentName;
    }

    public String getTifHeaderImageDescription() {
        return this.tifHeaderImageDescription;
    }

    public void setTifHeaderImageDescription(String tifHeaderImageDescription) {
        this.tifHeaderImageDescription = tifHeaderImageDescription;
    }

    public Process getProzessKopie() {
        return this.prozessKopie;
    }

    public void setProzessKopie(Process prozessKopie) {
        this.prozessKopie = prozessKopie;
    }

    public String getOpacSuchfeld() {
        return this.opacSuchfeld;
    }

    public void setOpacSuchfeld(String opacSuchfeld) {
        this.opacSuchfeld = opacSuchfeld;
    }

    public String getOpacKatalog() {
        return this.opacKatalog;
    }

    public void setOpacKatalog(String opacKatalog) {
        this.opacKatalog = opacKatalog;
    }

    public String getOpacSuchbegriff() {
        return this.opacSuchbegriff;
    }

    public void setOpacSuchbegriff(String opacSuchbegriff) {
        this.opacSuchbegriff = opacSuchbegriff;
    }

    /*
     * Helper
     */

    /**
     * Prozesstitel und andere Details generieren.
     */
    public void calculateProcessTitle() {
        generateTitle(null);
        Ajax.update("editForm:processFromTemplateTabView:processDataTab");
    }

    /**
     * Generate title.
     *
     * @param genericFields
     *            Map of Strings
     * @return String
     */
    public String generateTitle(Map<String, String> genericFields) {
        String currentAuthors = "";
        String currentTitle = "";
        int counter = 0;
        for (AdditionalField field : this.additionalFields) {
            if (field.getAutogenerated() && field.getValue().isEmpty()) {
                field.setValue(String.valueOf(System.currentTimeMillis() + counter));
                counter++;
            }
            if (field.getMetadata() != null && field.getMetadata().equals("TitleDocMain")
                    && currentTitle.length() == 0) {
                currentTitle = field.getValue();
            } else if (field.getMetadata() != null && field.getMetadata().equals(LIST_OF_CREATORS)
                    && currentAuthors.length() == 0) {
                currentAuthors = field.getValue();
            }

        }
        StringBuilder newTitle = new StringBuilder();

        StringTokenizer tokenizer = new StringTokenizer(titleDefinition, "+");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
             * übernehmen
             */
            if (token.startsWith("'") && token.endsWith("'")) {
                newTitle.append(token, 1, token.length() - 1);
            } else if (token.startsWith("#")) {
                // resolve strings beginning with # from generic fields
                if (genericFields != null) {
                    String genericValue = genericFields.get(token);
                    if (genericValue != null) {
                        newTitle.append(genericValue);
                    }
                }
            } else {
                newTitle.append(evaluateAdditionalFieldsForTitle(currentTitle, currentAuthors, token));
            }
        }

        if (newTitle.toString().endsWith("_")) {
            newTitle.setLength(newTitle.length() - 1);
        }
        // remove non-ascii characters for the sake of TIFF header limits
        String filteredTitle = newTitle.toString().replaceAll("[^\\p{ASCII}]", "");
        prozessKopie.setTitle(filteredTitle);
        calculateTiffHeader();
        return filteredTitle;
    }

    private String evaluateAdditionalFieldsForTitle(String currentTitle, String currentAuthors, String token) {
        StringBuilder newTitle = new StringBuilder();

        for (AdditionalField additionalField : this.additionalFields) {
            /*
             * if it is the ATS or TSL field, then use the calculated atstsl if
             * it does not already exist
             */
            if ((additionalField.getTitle().equals("ATS") || additionalField.getTitle().equals("TSL"))
                    && additionalField.getShowDependingOnDoctype()
                    && (additionalField.getValue() == null || additionalField.getValue().equals(""))) {
                if (atstsl == null || atstsl.length() == 0) {
                    atstsl = createAtstsl(currentTitle, currentAuthors);
                }
                additionalField.setValue(this.atstsl);
            }

            // add the content to the title
            if (additionalField.getTitle().equals(token) && additionalField.getShowDependingOnDoctype()
                    && additionalField.getValue() != null) {
                newTitle.append(calculateProcessTitleCheck(additionalField.getTitle(), additionalField.getValue()));
            }
        }
        return newTitle.toString();
    }

    private String calculateProcessTitleCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        // Bandnummer
        if (inFeldName.equals("Bandnummer") || inFeldName.equals("Volume number")) {
            try {
                int bandint = Integer.parseInt(inFeldWert);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                rueckgabe = df.format(bandint);
            } catch (NumberFormatException e) {
                if (inFeldName.equals("Bandnummer")) {
                    Helper.setErrorMessage(Helper.getTranslation(INCOMPLETE_DATA) + "Bandnummer ist keine gültige Zahl",
                        logger, e);
                } else {
                    Helper.setErrorMessage(
                        Helper.getTranslation(INCOMPLETE_DATA) + "Volume number is not a valid number", logger, e);
                }
            }
            if (rueckgabe != null && rueckgabe.length() < 4) {
                rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
            }
        }

        return rueckgabe;
    }

    /**
     * Calculate tiff header.
     */
    public void calculateTiffHeader() {
        // possible replacements
        this.tifDefinition = this.tifDefinition.replaceAll("\\[\\[", "<");
        this.tifDefinition = this.tifDefinition.replaceAll("\\]\\]", ">");

        // Documentname ist im allgemeinen = Prozesstitel
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        StringBuilder tifHeaderImageDescriptionBuilder = new StringBuilder();
        // image description
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        // jetzt den Tiffheader parsen
        String title = "";
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
             * übernehmen
             */
            if (token.startsWith("'") && token.endsWith("'") && token.length() > 2) {
                tifHeaderImageDescriptionBuilder.append(token, 1, token.length() - 1);
            } else if (token.equals("$Doctype")) {
                /* wenn der Doctype angegeben werden soll */
                try {
                    tifHeaderImageDescriptionBuilder.append(ConfigOpac.getDoctypeByName(this.docType).getTifHeaderType());
                } catch (FileNotFoundException | RuntimeException e) {
                    Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
                }
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField additionalField : this.additionalFields) {
                    if (additionalField.getTitle().equals("Titel") || additionalField.getTitle().equals("Title")
                            && additionalField.getValue() != null && !additionalField.getValue().equals("")) {
                        title = additionalField.getValue();
                    }
                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten
                     * atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((additionalField.getTitle().equals("ATS") || additionalField.getTitle().equals("TSL"))
                            && additionalField.getShowDependingOnDoctype()
                            && (additionalField.getValue() == null || additionalField.getValue().equals(""))) {
                        additionalField.setValue(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (additionalField.getTitle().equals(token) && additionalField.getShowDependingOnDoctype()
                            && additionalField.getValue() != null) {
                        tifHeaderImageDescriptionBuilder.append(calculateProcessTitleCheck(additionalField.getTitle(),
                            additionalField.getValue()));
                    }

                }
            }
        }
        this.tifHeaderImageDescription = tifHeaderImageDescriptionBuilder.toString();
        reduceLengthOfTifHeaderImageDescription(title);
    }

    /**
     * Reduce to 255 characters.
     */
    private void reduceLengthOfTifHeaderImageDescription(String title) {
        int length = this.tifHeaderImageDescription.length();
        if (length > 255) {
            try {
                int toCut = length - 255;
                String newTitle = title.substring(0, title.length() - toCut);
                this.tifHeaderImageDescription = this.tifHeaderImageDescription.replace(title, newTitle);
            } catch (IndexOutOfBoundsException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    /**
     * Set images guessed.
     *
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
     * Get images guessed.
     *
     * @return the imagesGuessed
     */
    public Integer getImagesGuessed() {
        return this.guessedImages;
    }

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
        if (addToWikiField != null && !addToWikiField.equals("")) {
            User user = ServiceManager.getUserService().getAuthenticatedUser();
            String message = this.addToWikiField + " (" + ServiceManager.getUserService().getFullName(user) + ")";
            this.prozessKopie
                    .setWikiField(WikiFieldHelper.getWikiMessage(prozessKopie.getWikiField(), "info", message));
        }
    }

    /**
     * Create Atstsl.
     *
     * @param title
     *            String
     * @param author
     *            String
     * @return String
     */
    public static String createAtstsl(String title, String author) {
        StringBuilder result = new StringBuilder(8);
        if (author != null && author.trim().length() > 0) {
            result.append(getPartString(author, 4));
            result.append(getPartString(title, 4));
        } else {
            StringTokenizer titleWords = new StringTokenizer(title);
            int wordNo = 1;
            while (titleWords.hasMoreTokens() && wordNo < 5) {
                String word = titleWords.nextToken();
                switch (wordNo) {
                    case 1:
                        result.append(getPartString(word, 4));
                        break;
                    case 2:
                    case 3:
                        result.append(getPartString(word, 2));
                        break;
                    case 4:
                        result.append(getPartString(word, 1));
                        break;
                    default:
                        assert false : wordNo;
                }
                wordNo++;
            }
        }
        return result.toString().replaceAll("[\\W]", ""); // delete umlauts etc.
    }

    private static String getPartString(String word, int length) {
        return word.length() > length ? word.substring(0, length) : word;
    }

    /**
     * The function getHitlist returns the hits for the currently showing page
     * of the hitlist as read-only property "hitlist".
     *
     * @return a list of hits to render in the hitlist
     */
    public List<SelectableHit> getHitlist() {
        if (hitlistPage < 0) {
            return Collections.emptyList();
        }
        int pageSize = getPageSize();
        List<SelectableHit> result = new ArrayList<>(pageSize);
        long firstHit = hitlistPage * pageSize;
        long lastHit = Math.min(firstHit + pageSize - 1, hits - 1);
        for (long index = firstHit; index <= lastHit; index++) {
            try {
                Hit hit = importCatalogue.getHit(hitlist, index, CataloguePlugin.getTimeout());
                result.add(new SelectableHit(hit));
            } catch (RuntimeException e) {
                result.add(new SelectableHit(e.getMessage()));
            }
        }
        return result;
    }

    /**
     * The function getNumberOfHits() returns the number of hits on the hit list
     * as read-only property "numberOfHits".
     *
     * @return the number of hits on the hit list
     */
    public long getNumberOfHits() {
        return hits;
    }

    /**
     * The function getPageSize() retrieves the desired number of hits on one
     * page of the hit list from the configuration.
     *
     * @return desired number of hits on one page of the hit list from the
     *         configuration
     */
    private int getPageSize() {
        return ConfigCore.getIntParameterOrDefaultValue(ParameterCore.HITLIST_PAGE_SIZE);
    }

    /**
     * The function isFirstPage() returns whether the currently showing page of
     * the hitlist is the first page of it as read-only property "firstPage".
     *
     * @return whether the currently showing page of the hitlist is the first
     *         one
     */
    public boolean isFirstPage() {
        return hitlistPage == 0;
    }

    /**
     * The function getHitlistShowing returns whether the hitlist shall be
     * rendered or not as read-only property "hitlistShowing".
     *
     * @return whether the hitlist is to be shown or not
     */
    public boolean isHitlistShowing() {
        return hitlistPage >= 0;
    }

    /**
     * The function isLastPage() returns whether the currently showing page of
     * the hitlist is the last page of it as read-only property "lastPage".
     *
     * @return whether the currently showing page of the hitlist is the last one
     */
    public boolean isLastPage() {
        return (hitlistPage + 1) * getPageSize() > hits - 1;
    }

    /**
     * The function nextPageClick() is executed if the user clicks the action
     * link to flip one page forward in the hit list.
     */
    public void nextPageClick() {
        hitlistPage++;
    }

    /**
     * The function previousPageClick() is executed if the user clicks the
     * action link to flip one page backwards in the hit list.
     */
    public void previousPageClick() {
        hitlistPage--;
    }

    /**
     * The function isCalendarButtonShowing tells whether the calendar button
     * shall show up or not as read-only property "calendarButtonShowing".
     *
     * @return whether the calendar button shall show
     */
    public boolean isCalendarButtonShowing() {
        try {
            return ConfigOpac.getDoctypeByName(docType).isNewspaper();
        } catch (NullPointerException e) {
            // may occur if user continues to interact with the page across a
            // restart of the servlet container
            return false;
        } catch (FileNotFoundException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
            return false;
        }
    }

    /**
     * Returns the representation of the file holding the document metadata in
     * memory.
     *
     * @return the metadata file in memory
     */
    public FileformatInterface getFileformat() {
        return rdf;
    }

    /**
     * Get workflow conditions.
     *
     * @return value of workflowConditions
     */
    public List<String> getWorkflowConditions() {
        return workflowConditions;
    }

    /**
     * Set workflow conditions.
     *
     * @param workflowConditions
     *            as List of Strings
     */
    public void setWorkflowConditions(List<String> workflowConditions) {
        this.workflowConditions = workflowConditions;
    }
}
