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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
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
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.constants.Parameters;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.CataloguePlugin.Hit;
import org.goobi.production.plugin.CataloguePlugin.QueryBuilder;
import org.jdom.JDOMException;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.UGHException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.omnifaces.util.Ajax;

@Named("ProzesskopieForm")
@SessionScoped
public class ProzesskopieForm implements Serializable {
    private static final Logger logger = LogManager.getLogger(ProzesskopieForm.class);
    private static final long serialVersionUID = -4512865679353743L;
    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * The class SelectableHit represents a hit on the hit list that shows up if
     * a catalogue search yielded more than one result. We need an inner class
     * for this because Faces is striclty object oriented and the always
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
         *
         * @return always "", indicating to Faces to stay on that page
         */
        public String selectClick() {
            try {
                importHit(hit);
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error on reading opac ", e);
            } finally {
                hitlistPage = -1;
            }
            return null;
        }
    }

    /**
     * The constant DEFAULT_HITLIST_PAGE_SIZE holds the fallback number of hits
     * to show per page on the hit list if the user conducted a catalogue search
     * that yielded more than one result, if none is configured in the
     * Production configuration file.
     */
    private static final int DEFAULT_HITLIST_PAGE_SIZE = 10;

    public static final String DIRECTORY_SUFFIX = "_tif";

    static final String NAVI_FIRST_PAGE = "/pages/NewProcess/Page1";

    private String addToWikiField = "";
    private List<AdditionalField> additionalFields;
    private String atstsl = "";
    private List<String> digitalCollections;
    private String docType;
    private Integer guessedImages = 0;

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
    private CataloguePlugin importCatalogue;

    private FileformatInterface rdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private List<String> possibleDigitalCollection;
    private Process prozessVorlage = new Process();
    private Process prozessKopie = new Process();
    private boolean useOpac;
    private boolean useTemplates;
    private Integer auswahl;
    private HashMap<String, Boolean> standardFields;
    private String tifHeaderImageDescription = "";
    private String tifHeaderDocumentName = "";

    private static final String TEMPLATE_ROOT = "/pages/";
    private static final String PROCESS_FROM_TEMPLATE_PATH = TEMPLATE_ROOT + "processFromTemplate";
    private static final String PROCESS_FROM_TEMPLATE_PATH_OLD = TEMPLATE_ROOT + "ProzessverwaltungAlle";

    private static final String PROCESS_PATH = TEMPLATE_ROOT + "processes";
    private static final String PROCESS_PATH_OLD = TEMPLATE_ROOT + "/NewProcess/Page3";

    static final String REDIRECT_PARAMETER = "faces-redirect=true";

    /**
     * Prepare.
     *
     * @return empty String
     */
    public String prepare(int id) {
        atstsl = "";
        try {
            this.prozessVorlage = serviceManager.getProcessService().getById(id);
        } catch (DAOException e) {
            logger.error(e.getMessage());
            Helper.setFehlerMeldung("Process " + id + " not found.");
            return null;
        }
        Helper.getHibernateSession().refresh(this.prozessVorlage);
        if (serviceManager.getProcessService().getContainsUnreachableSteps(this.prozessVorlage)) {
            if (this.prozessVorlage.getTasks().size() == 0) {
                Helper.setFehlerMeldung("noStepsInWorkflow");
            }
            for (Task s : this.prozessVorlage.getTasks()) {
                if (serviceManager.getTaskService().getUserGroupsSize(s) == 0
                        && serviceManager.getTaskService().getUsersSize(s) == 0) {
                    List<String> param = new ArrayList<>();
                    param.add(s.getTitle());
                    Helper.setFehlerMeldung(Helper.getTranslation("noUserInStep", param));
                }
            }
            return null;
        }

        clearValues();
        readProjectConfigs();
        this.rdf = null;
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setTemplate(false);
        this.prozessKopie.setInChoiceListShown(false);
        this.prozessKopie.setProject(this.prozessVorlage.getProject());
        this.prozessKopie.setRuleset(this.prozessVorlage.getRuleset());
        this.prozessKopie.setDocket(this.prozessVorlage.getDocket());
        this.digitalCollections = new ArrayList<>();

        /*
         * Kopie der Prozessvorlage anlegen
         */
        BeanHelper.copyTasks(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyScanTemplates(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyWorkpieces(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyProperties(this.prozessVorlage, this.prozessKopie);

        initializePossibleDigitalCollections();

        return redirectToProcessFromTemplateEdit();
    }

    private void readProjectConfigs() {
        // projektabhängig die richtigen Felder in der Gui anzeigen
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        this.docType = cp.getParamString("createNewProcess.defaultdoctype",
            ConfigOpac.getAllDoctypes().get(0).getTitle());
        this.useOpac = cp.getParamBoolean("createNewProcess.opac[@use]");
        this.useTemplates = cp.getParamBoolean("createNewProcess.templates[@use]");
        if (this.opacKatalog.equals("")) {
            this.opacKatalog = cp.getParamString("createNewProcess.opac.catalogue");
        }

        // die auszublendenden Standard-Felder ermitteln
        for (String t : cp.getParamList("createNewProcess.itemlist.hide")) {
            this.standardFields.put(t, false);
        }

        // die einzublendenen (zusätzlichen) Eigenschaften ermitteln
        int count = cp.getParamList("createNewProcess.itemlist.item").size();
        for (int i = 0; i < count; i++) {
            AdditionalField fa = new AdditionalField(this);
            fa.setFrom(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@from]"));
            fa.setTitle(cp.getParamString("createNewProcess.itemlist.item(" + i + ")"));
            fa.setRequired(cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@required]"));
            fa.setIsdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isdoctype]"));
            fa.setIsnotdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isnotdoctype]"));
            // attributes added 30.3.09
            String test = (cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initStart]"));
            fa.setInitStart(test);

            fa.setInitEnd(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initEnd]"));

            /*
             * Bindung an ein Metadatum eines Docstructs
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
             * prüfen, ob das aktuelle Item eine Auswahlliste werden soll
             */
            int selectItemCount = cp.getParamList("createNewProcess.itemlist.item(" + i + ").select").size();
            /* Children durchlaufen und SelectItems erzeugen */
            if (selectItemCount > 0) {
                fa.setSelectList(new ArrayList<>());
            }
            for (int j = 0; j < selectItemCount; j++) {
                String svalue = cp
                        .getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")[@label]");
                String sid = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")");
                fa.getSelectList().add(new SelectItem(sid, svalue, null));
            }
            this.additionalFields.add(fa);
        }
    }

    /**
     * Get Process templates.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProzessTemplates() {
        List<Process> processes;
        // TODO Change to check the corresponding authority
        if (serviceManager.getSecurityAccessService().isAdmin()) {
            processes = serviceManager.getProcessService().getProcessTemplates();
        } else {
            User currentUser = null;
            try {
                currentUser = serviceManager.getUserService().getAuthenticatedUser();
            } catch (DAOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
            ArrayList<Integer> projectIds = new ArrayList<>();

            for (Project project : currentUser.getProjects()) {
                projectIds.add(project.getId());
            }
            processes = serviceManager.getProcessService().getProcessTemplatesForUser(projectIds);
        }

        List<SelectItem> processTemplates = new ArrayList<>();
        for (Process process : processes) {
            processTemplates.add(new SelectItem(process.getId(), process.getTitle(), null));
        }
        return processTemplates;
    }

    /**
     * The function evaluateOpac() is executed if a user clicks the command link
     * to start a catalogue search. It performs the search and loads the hit if
     * it is unique. Otherwise, it will cause a hit list to show up for the user
     * to select a hit.
     *
     * @return always "", telling JSF to stay on that page
     */
    public String evaluateOpac() {
        long timeout = CataloguePlugin.getTimeout();
        try {
            clearValues();
            readProjectConfigs();
            if (!pluginAvailableFor(opacKatalog)) {
                return null;
            }

            String query = QueryBuilder.restrictToField(opacSuchfeld, opacSuchbegriff);
            query = QueryBuilder.appendAll(query, ConfigOpac.getRestrictionsForCatalogue(opacKatalog));

            hitlist = importCatalogue.find(query, timeout);
            hits = importCatalogue.getNumberOfHits(hitlist, timeout);

            switch ((int) Math.min(hits, Integer.MAX_VALUE)) {
                case 0:
                    Helper.setFehlerMeldung("No hit found", "");
                    break;
                case 1:
                    importHit(importCatalogue.getHit(hitlist, 0, timeout));
                    break;
                default:
                    hitlistPage = 0; // show first page of hitlist
                    break;
            }
            return null;
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on reading opac ", e);
            return null;
        }
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
     * @return whether a plugin is available in the global varibale
     *         importCatalogue
     */
    private boolean pluginAvailableFor(String catalogue) {
        if (importCatalogue == null || !importCatalogue.supportsCatalogue(catalogue)) {
            importCatalogue = PluginLoader.getCataloguePluginForCatalogue(catalogue);
        }
        if (importCatalogue == null) {
            Helper.setFehlerMeldung("NoCataloguePluginForCatalogue", catalogue);
            return false;
        } else {
            importCatalogue
                    .setPreferences(serviceManager.getRulesetService().getPreferences(prozessKopie.getRuleset()));
            importCatalogue.useCatalogue(catalogue);
            return true;
        }
    }

    /**
     * alle Konfigurationseigenschaften und Felder zurücksetzen.
     */
    private void clearValues() {
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
        applyCopyingRules(new CopierData(rdf, prozessVorlage));
        atstsl = createAtstsl(hit.getTitle(), hit.getAuthors());
    }

    /**
     * Creates a DataCopier with the given configuration, lets it process the
     * given data and wraps any errors to display in the front end.
     *
     * @param data
     *            data to process
     */
    private void applyCopyingRules(CopierData data) {
        String rules = ConfigCore.getParameter("copyData.onCatalogueQuery");
        if (rules != null && !rules.equals("- keine Konfiguration gefunden -")) {
            try {
                new DataCopier(rules).process(data);
            } catch (ConfigurationException e) {
                Helper.setFehlerMeldung("dataCopier.syntaxError", e.getMessage());
            } catch (RuntimeException exception) {
                if (RuntimeException.class.equals(exception.getClass())) {
                    Helper.setFehlerMeldung("dataCopier.runtimeException", exception.getMessage());
                } else {
                    throw exception;
                }
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
                    /* welches Docstruct */
                    DocStructInterface myTempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            myTempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            logger.error(e);
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        myTempStruct = this.rdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (field.getMetadata().equals("ListOfCreators")) {
                            /* bei Autoren die Namen zusammenstellen */
                            StringBuilder authors = new StringBuilder();
                            if (myTempStruct.getAllPersons() != null) {
                                for (PersonInterface p : myTempStruct.getAllPersons()) {
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
                            field.setValue(authors.toString());
                        } else {
                            /* bei normalen Feldern die Inhalte auswerten */
                            MetadataTypeInterface mdt = UghHelper.getMetadataType(
                                serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                field.getMetadata());
                            MetadataInterface md = UghHelper.getMetadata(myTempStruct, mdt);
                            if (md != null) {
                                field.setValue(md.getValue());
                                md.setStringValue(field.getValue().replace("&amp;", "&"));
                            }
                        }
                    } catch (UghHelperException e) {
                        logger.error(e);
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                    if (field.getValue() != null && !field.getValue().equals("")) {
                        field.setValue(field.getValue().replace("&amp;", "&"));
                    }
                } // end if ughbinding
            } // end for
        } // end if myrdf==null
    }

    /**
     * Auswahl des Prozesses auswerten.
     */
    public String templateAuswahlAuswerten() throws DAOException {
        /* den ausgewählten Prozess laden */
        Process tempProzess = serviceManager.getProcessService().getById(this.auswahl);
        if (serviceManager.getProcessService().getWorkpiecesSize(tempProzess) > 0) {
            for (Property workpieceProperty : tempProzess.getWorkpieces()) {
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

        if (serviceManager.getProcessService().getTemplatesSize(tempProzess) > 0) {
            for (Property templateProperty : tempProzess.getTemplates()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitle().equals(templateProperty.getTitle())) {
                        field.setValue(templateProperty.getValue());
                    }
                }
            }
        }

        if (serviceManager.getProcessService().getPropertiesSize(tempProzess) > 0) {
            for (Property processProperty : tempProzess.getProperties()) {
                if (processProperty.getTitle().equals("digitalCollection")) {
                    digitalCollections.add(processProperty.getValue());
                }
            }
        }
        try {
            this.rdf = serviceManager.getProcessService().readMetadataAsTemplateFile(tempProzess);
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on reading template-metadata ", e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            DocStructInterface colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Error on creating process", e);
            logger.error("Error on creating process", e);
        } catch (RuntimeException e) {
            /*
             * das Firstchild unterhalb des Topstructs konnte nicht ermittelt
             * werden
             */
        }

        return null;
    }

    /**
     * Validierung der Eingaben.
     *
     * @return sind Fehler bei den Eingaben vorhanden?
     */
    boolean isContentValid() {
        return isContentValid(true);
    }

    boolean isContentValid(boolean criticiseEmptyTitle) {
        boolean valide = true;

        if (criticiseEmptyTitle) {

            /*
             * grundsätzlich den Vorgangstitel prüfen
             */
            /* kein Titel */
            if (this.prozessKopie.getTitle() == null || this.prozessKopie.getTitle().equals("")) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                        + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigCore.getParameter("validateProzessTitelRegex", "[\\w-]*");
            if (!this.prozessKopie.getTitle().matches(validateRegEx)) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
            }

            /* prüfen, ob der Prozesstitel schon verwendet wurde */
            if (this.prozessKopie.getTitle() != null) {
                long amount = 0;
                try {
                    amount = serviceManager.getProcessService()
                            .findNumberOfProcessesWithTitle(this.prozessKopie.getTitle());
                } catch (DataException e) {
                    Helper.setFehlerMeldung("Error on reading process information", e.getMessage());
                    valide = false;
                }
                if (amount > 0) {
                    valide = false;
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten:")
                            + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
                }
            }

        }

        /*
         * Prüfung der standard-Eingaben, die angegeben werden müssen
         */
        /* keine Collektion ausgewählt */
        if (this.standardFields.get("collections") && getDigitalCollections().size() == 0) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                    + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        /*
         * Prüfung der additional-Eingaben, die angegeben werden müssen
         */
        for (AdditionalField field : this.additionalFields) {
            if ((field.getValue() == null || field.getValue().equals("")) && field.isRequired()
                    && field.getShowDependingOnDoctype() && (StringUtils.isBlank(field.getValue()))) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitle() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));

            }
        }
        return valide;
    }

    public String goToPageOne() {
        return NAVI_FIRST_PAGE;
    }

    // TODO: why do we need page two?
    /**
     * Go to page 2.
     *
     * @return page
     */
    public String goToPageTwo() {
        if (!isContentValid()) {
            return NAVI_FIRST_PAGE;
        } else {
            return "/pages/NewProcess/Page2";
        }
    }

    /**
     * Anlegen des Prozesses und save der Metadaten.
     */
    public String createNewProcess() throws ReadException, IOException, PreferencesException, WriteException {

        // evict set up id to null
        Helper.getHibernateSession().evict(this.prozessKopie);
        if (!isContentValid()) {
            return NAVI_FIRST_PAGE;
        }
        addProperties();

        updateTasks();

        try {
            this.prozessKopie.setSortHelperImages(this.guessedImages);
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            Helper.setErrorMessage("errorCreating", new Object[] {Helper.getTranslation("prozess") }, logger, e);
            return null;
        }

        String baseProcessDirectory = serviceManager.getProcessService().getProcessDataDirectory(this.prozessKopie)
                .toString();
        boolean successful = serviceManager.getFileService().createMetaDirectory(URI.create(""), baseProcessDirectory);
        if (!successful) {
            String message = "Metadata directory: " + baseProcessDirectory + "in path:"
                    + ConfigCore.getKitodoDataDirectory() + " was not created!";
            logger.error(message);
            Helper.setFehlerMeldung(message);
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
        if (this.rdf != null) {

            // there must be at least one non-anchor level doc struct
            // if missing, insert logical doc structs until you reach it
            DocStructInterface populizer = null;
            try {
                populizer = rdf.getDigitalDocument().getLogicalDocStruct();
                if (populizer.getAnchorClass() != null && populizer.getAllChildren() == null) {
                    PrefsInterface ruleset = serviceManager.getRulesetService()
                            .getPreferences(prozessKopie.getRuleset());
                    while (populizer.getDocStructType().getAnchorClass() != null) {
                        populizer = populizer.createChild(
                            populizer.getDocStructType().getAllAllowedDocStructTypes().get(0), rdf.getDigitalDocument(),
                            ruleset);
                    }
                }
            } catch (NullPointerException | IndexOutOfBoundsException e) { // if
                // getAllAllowedDocStructTypes()
                // returns null
                Helper.setFehlerMeldung("DocStrctType is configured as anchor but has no allowedchildtype.",
                    populizer != null && populizer.getDocStructType() != null ? populizer.getDocStructType().getName()
                            : null);
            } catch (UGHException catchAll) {
                Helper.setErrorMessage(catchAll.getMessage(), logger, catchAll);
            }

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    /* welches Docstruct */
                    DocStructInterface tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
                    DocStructInterface tempChild = null;
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            Helper.setErrorMessage(
                                e.getMessage() + " The first child below the top structure could not be determined!",
                                logger, e);
                        }
                    }
                    /*
                     * falls topstruct und firstchild das Metadatum bekommen
                     * sollen
                     */
                    if (!field.getDocstruct().equals("firstchild") && field.getDocstruct().contains("firstchild")) {
                        try {
                            tempChild = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        tempStruct = this.rdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        /*
                         * bis auf die Autoren alle additionals in die Metadaten
                         * übernehmen
                         */
                        if (!field.getMetadata().equals("ListOfCreators")) {
                            MetadataTypeInterface mdt = UghHelper.getMetadataType(
                                serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                field.getMetadata());
                            MetadataInterface metadata = UghHelper.getMetadata(tempStruct, mdt);
                            if (metadata != null) {
                                metadata.setStringValue(field.getValue());
                            }
                            /*
                             * wenn dem Topstruct und dem Firstchild der Wert
                             * gegeben werden soll
                             */
                            if (tempChild != null) {
                                metadata = UghHelper.getMetadata(tempChild, mdt);
                                if (metadata != null) {
                                    metadata.setStringValue(field.getValue());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                    }
                }
            }

            updateMetadata();

            /*
             * Collectionen hinzufügen
             */
            DocStructInterface colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
            if (Objects.nonNull(colStruct) && Objects.nonNull(colStruct.getAllChildren())
                    && colStruct.getAllChildren().size() > 0) {
                try {
                    addCollections(colStruct);
                    /*
                     * falls ein erstes Kind vorhanden ist, sind die Collectionen dafür
                     */
                    colStruct = colStruct.getAllChildren().get(0);
                    addCollections(colStruct);
                } catch (RuntimeException e) {
                    Helper.setErrorMessage(
                        e.getMessage() + " The first child below the top structure could not be determined!", logger,
                        e);
                }
            }

            /*
             * Imagepfad hinzufügen (evtl. vorhandene zunächst löschen)
             */
            try {
                MetadataTypeInterface mdt = UghHelper.getMetadataType(this.prozessKopie, "pathimagefiles");
                List<? extends MetadataInterface> allImagePaths = this.rdf.getDigitalDocument().getPhysicalDocStruct()
                        .getAllMetadataByType(mdt);
                if (allImagePaths != null && allImagePaths.size() > 0) {
                    for (MetadataInterface metadata : allImagePaths) {
                        this.rdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(metadata);
                    }
                }
                MetadataInterface newMetadata = UghImplementation.INSTANCE.createMetadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newMetadata.setStringValue(
                        "file:/" + serviceManager.getFileService().getImagesDirectory(this.prozessKopie)
                                + this.prozessKopie.getTitle().trim() + DIRECTORY_SUFFIX);
                } else {
                    newMetadata.setStringValue(
                        "file://" + serviceManager.getFileService().getImagesDirectory(this.prozessKopie)
                                + this.prozessKopie.getTitle().trim() + DIRECTORY_SUFFIX);
                }
                this.rdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newMetadata);

                /* Rdf-File schreiben */
                serviceManager.getFileService().writeMetadataFile(this.rdf, this.prozessKopie);

                /*
                 * soll der Prozess als Vorlage verwendet werden?
                 */
                if (this.useTemplates && this.prozessKopie.isInChoiceListShown()) {
                    serviceManager.getFileService().writeMetadataAsTemplateFile(this.rdf, this.prozessKopie);
                }
            } catch (DocStructHasNoTypeException e) {
                Helper.setErrorMessage("DocStructHasNoTypeException", logger, e);
            } catch (UghHelperException e) {
                Helper.setErrorMessage("UghHelperException", logger, e);
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setErrorMessage("MetadataTypeNotAllowedException", logger, e);
            }

        }

        // Create configured directories
        serviceManager.getProcessService().createProcessDirs(this.prozessKopie);

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        startTaskScriptThreads();

        return this.redirectToProcessesAfterSave();
    }

    private void updateTasks() {
        for (Task task : this.prozessKopie.getTasks()) {
            // always save date and user for each step
            task.setProcessingTime(this.prozessKopie.getCreationDate());
            task.setEditTypeEnum(TaskEditType.AUTOMATIC);
            User user = Helper.getCurrentUser();
            if (user != null) {
                task.setProcessingUser(user);
            }

            // only if its done, set edit start and end date
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                task.setProcessingBegin(this.prozessKopie.getCreationDate());
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
        if (ConfigCore.getBooleanParameter(Parameters.USE_METADATA_ENRICHMENT, false)) {
            DocStructInterface enricher = rdf.getDigitalDocument().getLogicalDocStruct();
            Map<String, Map<String, MetadataInterface>> higherLevelMetadata = new HashMap<>();
            while (enricher.getAllChildren() != null) {
                // save higher level metadata for lower enrichment
                List<MetadataInterface> allMetadata = enricher.getAllMetadata();
                if (allMetadata == null) {
                    allMetadata = Collections.emptyList();
                }
                for (MetadataInterface available : allMetadata) {
                    Map<String, MetadataInterface> availableMetadata = higherLevelMetadata
                            .containsKey(available.getMetadataType().getName())
                                    ? higherLevelMetadata.get(available.getMetadataType().getName())
                                    : new HashMap<>();
                    if (!availableMetadata.containsKey(available.getValue())) {
                        availableMetadata.put(available.getValue(), available);
                    }
                    higherLevelMetadata.put(available.getMetadataType().getName(), availableMetadata);
                }

                // enrich children with inherited metadata
                for (DocStructInterface nextChild : enricher.getAllChildren()) {
                    enricher = nextChild;
                    for (Entry<String, Map<String, MetadataInterface>> availableHigherMetadata : higherLevelMetadata
                            .entrySet()) {
                        String enrichable = availableHigherMetadata.getKey();
                        boolean addable = false;
                        List<MetadataTypeInterface> addableTypesNotNull = enricher.getAddableMetadataTypes();
                        if (addableTypesNotNull == null) {
                            addableTypesNotNull = Collections.emptyList();
                        }
                        for (MetadataTypeInterface addableMetadata : addableTypesNotNull) {
                            if (addableMetadata.getName().equals(enrichable)) {
                                addable = true;
                                break;
                            }
                        }
                        if (!addable) {
                            continue;
                        }
                        there: for (Entry<String, MetadataInterface> higherElement : availableHigherMetadata.getValue()
                                .entrySet()) {
                            List<MetadataInterface> amNotNull = enricher.getAllMetadata();
                            if (amNotNull == null) {
                                amNotNull = Collections.emptyList();
                            }
                            for (MetadataInterface existentMetadata : amNotNull) {
                                if (existentMetadata.getMetadataType().getName().equals(enrichable)
                                        && existentMetadata.getValue().equals(higherElement.getKey())) {
                                    continue there;
                                }
                            }
                            try {
                                enricher.addMetadata(higherElement.getValue());
                            } catch (UGHException didNotWork) {
                                Helper.setErrorMessage("errorAdding",
                                    new Object[] {Helper.getTranslation("metadata") }, logger, didNotWork);
                            }
                        }
                    }
                }
            }
        }
    }

    private void startTaskScriptThreads() {
        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);

        List<Task> tasks = this.prozessKopie.getTasks();
        for (Task task : tasks) {
            if (task.getProcessingStatus() == 1 && task.isTypeAutomatic()) {
                TaskScriptThread thread = new TaskScriptThread(task);
                thread.start();
            }
        }
    }

    private void addCollections(DocStructInterface colStruct) {
        for (String s : this.digitalCollections) {
            try {
                MetadataInterface md = UghImplementation.INSTANCE.createMetadata(UghHelper.getMetadataType(
                    serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
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
    private void removeCollections(DocStructInterface colStruct) {
        try {
            MetadataTypeInterface mdt = UghHelper.getMetadataType(
                serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                "singleDigCollection");
            ArrayList<MetadataInterface> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt));
            if (myCollections.size() > 0) {
                for (MetadataInterface md : myCollections) {
                    colStruct.removeMetadata(md);
                }
            }
        } catch (UghHelperException | DocStructHasNoTypeException e) {
            Helper.setErrorMessage(e.getMessage(), logger, e);
        }
    }

    /**
     * Create new file format.
     */
    public void createNewFileformat() {
        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());
        try {
            DigitalDocumentInterface dd = UghImplementation.INSTANCE.createDigitalDocument();
            FileformatInterface ff = UghImplementation.INSTANCE.createXStream(myPrefs);
            ff.setDigitalDocument(dd);
            // add BoundBook
            DocStructTypeInterface dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStructInterface dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(this.docType);

            if (configOpacDoctype != null) {
                // Monographie
                if (!configOpacDoctype.isPeriodical() && !configOpacDoctype.isMultiVolume()) {
                    DocStructTypeInterface dsty = myPrefs.getDocStrctTypeByName(configOpacDoctype.getRulesetType());
                    DocStructInterface ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);
                    this.rdf = ff;
                } else if (configOpacDoctype.isPeriodical()) {
                    // Zeitschrift
                    DocStructTypeInterface dsty = myPrefs.getDocStrctTypeByName("Periodical");
                    DocStructInterface ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);

                    DocStructTypeInterface dstyvolume = myPrefs.getDocStrctTypeByName("PeriodicalVolume");
                    DocStructInterface dsvolume = dd.createDocStruct(dstyvolume);
                    ds.addChild(dsvolume);
                    this.rdf = ff;
                } else if (configOpacDoctype.isMultiVolume()) {
                    // MultivolumeBand
                    DocStructTypeInterface dsty = myPrefs.getDocStrctTypeByName("MultiVolumeWork");
                    DocStructInterface ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);

                    DocStructTypeInterface dstyvolume = myPrefs.getDocStrctTypeByName("Volume");
                    DocStructInterface dsvolume = dd.createDocStruct(dstyvolume);
                    ds.addChild(dsvolume);
                    this.rdf = ff;
                }
            } else {
                // TODO: what should happen if configOpacDoctype is null?
            }

            if (this.docType.equals("volumerun")) {
                DocStructTypeInterface dsty = myPrefs.getDocStrctTypeByName("VolumeRun");
                DocStructInterface ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);

                DocStructTypeInterface dstyvolume = myPrefs.getDocStrctTypeByName("Record");
                DocStructInterface dsvolume = dd.createDocStruct(dstyvolume);
                ds.addChild(dsvolume);
                this.rdf = ff;
            }
        } catch (TypeNotAllowedAsChildException | PreferencesException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        } catch (FileNotFoundException e) {
            Helper.setErrorMessage("Error while reading von opac-config", logger, e);
        }
    }

    private void addProperties() {
        for (AdditionalField field : this.additionalFields) {
            if (field.getShowDependingOnDoctype()) {
                if (field.getFrom().equals("werk")) {
                    BeanHelper.addPropertyForWorkpiece(this.prozessKopie, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("vorlage")) {
                    BeanHelper.addPropertyForTemplate(this.prozessKopie, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("prozess")) {
                    BeanHelper.addPropertyForProcess(this.prozessKopie, field.getTitle(), field.getValue());
                }
            }
        }

        for (String col : digitalCollections) {
            BeanHelper.addPropertyForProcess(this.prozessKopie, "digitalCollection", col);
        }

        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
            this.tifHeaderImageDescription);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        BeanHelper.addPropertyForProcess(this.prozessKopie, "Template", prozessVorlage.getTitle());
        BeanHelper.addPropertyForProcess(this.prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
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
                        DocStructInterface oldLogicalDocstruct = tmp.getDigitalDocument().getLogicalDocStruct();
                        DocStructInterface newLogicalDocstruct = rdf.getDigitalDocument().getLogicalDocStruct();
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

    private void copyMetadata(DocStructInterface oldDocStruct, DocStructInterface newDocStruct) {

        if (oldDocStruct.getAllMetadata() != null) {
            for (MetadataInterface md : oldDocStruct.getAllMetadata()) {
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

    public Process getProzessVorlage() {
        return this.prozessVorlage;
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
        return prozessVorlage != null ? prozessVorlage.getTitle() : null;
    }

    public void setProzessVorlage(Process prozessVorlage) {
        this.prozessVorlage = prozessVorlage;
    }

    public Integer getAuswahl() {
        return this.auswahl;
    }

    public void setAuswahl(Integer auswahl) {
        this.auswahl = auswahl;
    }

    public List<AdditionalField> getAdditionalFields() {
        return this.additionalFields;
    }

    /**
     * The method getVisibleAdditionalFields returns a list of visible additional fields
     * @return list of AdditionalField
     */
    public List<AdditionalField> getVisibleAdditionalFields() {
        return this.getAdditionalFields().stream().filter(af -> af.getShowDependingOnDoctype()).collect(Collectors.toList());
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
     * @throws RuntimeException
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
            throw new RuntimeException("Couldn’t set “" + key + "” to “" + value + "”: No such field in record.");
        }
    }

    public void setAdditionalFields(List<AdditionalField> additionalFields) {
        this.additionalFields = additionalFields;
    }

    /*
     * this is needed for GUI, render multiple select only if this is false if
     * this is true use the only choice
     *
     * @author Wulf
     */
    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

    }

    /**
     * This is needed for GUI, render multiple select only if this is false if
     * isSingleChoiceCollection is true use this choice.
     *
     * @author Wulf
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

    @SuppressWarnings("unchecked")
    private void initializePossibleDigitalCollections() {
        try {
            DigitalCollections.possibleDigitalCollectionsForProcess(this.prozessKopie);
        } catch (JDOMException | IOException e) {
            Helper.setErrorMessage("Error while parsing digital collections", logger, e);
        }

        this.possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        this.digitalCollections = DigitalCollections.getDigitalCollections();

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
        } catch (Exception e) {
            logger.error("Error while reading von opac-config", e);
            Helper.setFehlerMeldung("Error while reading von opac-config", e.getMessage());
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
        } catch (Exception e) {
            logger.error("Error while reading von opac-config", e);
            Helper.setFehlerMeldung("Error while reading von opac-config", e.getMessage());
            return new ArrayList<>();
        }
    }

    /*
     * changed, so that on first request list gets set if there is only one
     * choice
     */
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    public HashMap<String, Boolean> getStandardFields() {
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
        try {
            generateTitle(null);
            Ajax.update("editForm");
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Generate title.
     *
     * @param genericFields
     *            Map of Strings
     * @return String
     */
    public String generateTitle(Map<String, String> genericFields) throws IOException {
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
            } else if (field.getMetadata() != null && field.getMetadata().equals("ListOfCreators")
                    && currentAuthors.length() == 0) {
                currentAuthors = field.getValue();
            }

        }
        StringBuilder newTitle = new StringBuilder();
        String titeldefinition = "";
        ConfigProjects cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());

        int count = cp.getParamList("createNewProcess.itemlist.processtitle").size();
        for (int i = 0; i < count; i++) {
            String titel = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")");
            String isdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isdoctype]");
            String isnotdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isnotdoctype]");

            if (titel == null) {
                titel = "";
            }
            if (isdoctype == null) {
                isdoctype = "";
            }
            if (isnotdoctype == null) {
                isnotdoctype = "";
            }

            /* wenn nix angegeben wurde, dann anzeigen */
            if (isdoctype.equals("") && isnotdoctype.equals("")) {
                titeldefinition = titel;
                break;
            }

            /* wenn beides angegeben wurde */
            if (!isdoctype.equals("") && !isnotdoctype.equals("")
                    && StringUtils.containsIgnoreCase(isdoctype, this.docType)
                    && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }

            /* wenn nur pflicht angegeben wurde */
            if (isnotdoctype.equals("") && StringUtils.containsIgnoreCase(isdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
            /* wenn nur "darf nicht" angegeben wurde */
            if (isdoctype.equals("") && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(titeldefinition, "+");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
             * übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'")) {
                newTitle.append(myString.substring(1, myString.length() - 1));
            } else if (myString.startsWith("#")) {
                /*
                 * resolve strings beginning with # from generic fields
                 */
                if (genericFields != null) {
                    String genericValue = genericFields.get(myString);
                    if (genericValue != null) {
                        newTitle.append(genericValue);
                    }
                }
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField additionalField : this.additionalFields) {
                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten
                     * atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((additionalField.getTitle().equals("ATS") || additionalField.getTitle().equals("TSL"))
                            && additionalField.getShowDependingOnDoctype()
                            && (additionalField.getValue() == null || additionalField.getValue().equals(""))) {
                        if (atstsl == null || atstsl.length() == 0) {
                            atstsl = createAtstsl(currentTitle, currentAuthors);
                        }
                        additionalField.setValue(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (additionalField.getTitle().equals(myString) && additionalField.getShowDependingOnDoctype()
                            && additionalField.getValue() != null) {
                        newTitle.append(
                            calculateProcessTitleCheck(additionalField.getTitle(), additionalField.getValue()));
                    }
                }
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

    private String calculateProcessTitleCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        /*
         * Bandnummer
         */
        if (inFeldName.equals("Bandnummer") || inFeldName.equals("Volume number")) {
            try {
                int bandint = Integer.parseInt(inFeldWert);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                rueckgabe = df.format(bandint);
            } catch (NumberFormatException e) {
                if (inFeldName.equals("Bandnummer")) {
                    Helper.setErrorMessage(
                        Helper.getTranslation("UngueltigeDaten: ") + "Bandnummer ist keine gültige Zahl", logger, e);
                } else {
                    Helper.setErrorMessage(
                        Helper.getTranslation("UngueltigeDaten: ") + "Volume number is not a valid number", logger, e);
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
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }
        String tifDefinition = cp.getParamString("tifheader." + this.docType, "intranda");

        // possible replacements
        tifDefinition = tifDefinition.replaceAll("\\[\\[", "<");
        tifDefinition = tifDefinition.replaceAll("\\]\\]", ">");

        // Documentname ist im allgemeinen = Prozesstitel
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        this.tifHeaderImageDescription = "";
        // image description
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        // jetzt den Tiffheader parsen
        String title = "";
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
             * übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2) {
                this.tifHeaderImageDescription += myString.substring(1, myString.length() - 1);
            } else if (myString.equals("$Doctype")) {
                /* wenn der Doctype angegeben werden soll */
                try {
                    this.tifHeaderImageDescription += ConfigOpac.getDoctypeByName(this.docType).getTifHeaderType();
                } catch (Exception e) {
                    logger.error("Error while reading von opac-config", e);
                    Helper.setFehlerMeldung("Error while reading von opac-config", e.getMessage());
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
                    if (additionalField.getTitle().equals(myString) && additionalField.getShowDependingOnDoctype()
                            && additionalField.getValue() != null) {
                        this.tifHeaderImageDescription += calculateProcessTitleCheck(additionalField.getTitle(),
                            additionalField.getValue());
                    }

                }
            }
            // reduce to 255 character
        }
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
     * Downloads a docket for the process.
     *
     * @return the navigation-strign
     */
    public String downloadDocket() {
        try {
            serviceManager.getProcessService().downloadDocket(this.prozessKopie);
        } catch (IOException e) {
            Helper.setErrorMessage("errorCreating", new Object[] {Helper.getTranslation("docket") }, logger, e);
        }
        return "";
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
        this.prozessKopie.setWikiField(prozessVorlage.getWikiField());
        this.addToWikiField = addToWikiField;
        if (addToWikiField != null && !addToWikiField.equals("")) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            String message = this.addToWikiField + " (" + serviceManager.getUserService().getFullName(user) + ")";
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
            result.append(author.length() > 4 ? author.substring(0, 4) : author);
            result.append(title.length() > 4 ? title.substring(0, 4) : title);
        } else {
            StringTokenizer titleWords = new StringTokenizer(title);
            int wordNo = 1;
            while (titleWords.hasMoreTokens() && wordNo < 5) {
                String word = titleWords.nextToken();
                switch (wordNo) {
                    case 1:
                        result.append(word.length() > 4 ? word.substring(0, 4) : word);
                        break;
                    case 2:
                    case 3:
                        result.append(word.length() > 2 ? word.substring(0, 2) : word);
                        break;
                    case 4:
                        result.append(word.length() > 1 ? word.substring(0, 1) : word);
                        break;
                    default:
                        assert false : wordNo;
                }
                wordNo++;
            }
        }
        return result.toString().replaceAll("[\\W]", ""); // delete umlauts etc.
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
        return ConfigCore.getIntParameter(Parameters.HITLIST_PAGE_SIZE, DEFAULT_HITLIST_PAGE_SIZE);
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
            Helper.setErrorMessage("Error while reading von opac-config", logger, e);
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

    // TODO:
    // replace calls to this function with "/pages/processFromTemplate" once we have completely
    // switched to the new frontend pages
    private String redirectToProcessFromTemplateEdit() {
        try {
            String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referer.substring(referer.lastIndexOf("/") + 1);
            if (!callerViewId.isEmpty()
                    && (callerViewId.contains("projects.jsf"))) {
                return PROCESS_FROM_TEMPLATE_PATH + "?" + REDIRECT_PARAMETER;
            } else {
                return PROCESS_FROM_TEMPLATE_PATH_OLD + "?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when "ProzesskopieForm" is
            // used from it's integration test
            // class "ProzesskopieFormIT", where no "FacesContext" is available!
            return PROCESS_FROM_TEMPLATE_PATH_OLD + "?" + REDIRECT_PARAMETER;
        }
    }

    // TODO:
    // replace calls to this function with "/pages/processFromTemplate" once we have completely
    // switched to the new frontend pages
    private String redirectToProcessesAfterSave() {
        try {
            String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referer.substring(referer.lastIndexOf("/") + 1);
            if (!callerViewId.isEmpty()
                    && (callerViewId.contains("processFromTemplate.jsf"))) {
                return PROCESS_PATH + "?" + REDIRECT_PARAMETER;
            } else {
                return PROCESS_PATH_OLD + "?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when "ProzesskopieForm" is
            // used from it's integration test
            // class "ProzesskopieFormIT", where no "FacesContext" is available!
            return PROCESS_PATH_OLD + "?" + REDIRECT_PARAMETER;
        }
    }
}
