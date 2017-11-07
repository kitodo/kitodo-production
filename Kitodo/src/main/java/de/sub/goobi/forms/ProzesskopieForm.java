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
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.copier.CopierData;
import de.sub.goobi.metadaten.copier.DataCopier;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.File;
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
import java.util.StringTokenizer;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.constants.FileNames;
import org.goobi.production.constants.Parameters;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.CataloguePlugin.Hit;
import org.goobi.production.plugin.CataloguePlugin.QueryBuilder;
import org.goobi.production.plugin.PluginLoader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;

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
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.XStream;

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

    private Fileformat rdf;
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

        return NAVI_FIRST_PAGE;
    }

    private void readProjectConfigs() {
        /*
         * projektabhängig die richtigen Felder in der Gui anzeigen
         */
        ConfigProjects cp = null;
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

        /*
         * die auszublendenden Standard-Felder ermitteln
         */
        for (String t : cp.getParamList("createNewProcess.itemlist.hide")) {
            this.standardFields.put(t, false);
        }

        /*
         * die einzublendenen (zusätzlichen) Eigenschaften ermitteln
         */
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
        List<SelectItem> processTemplates = new ArrayList<>();

        /* Einschränkung auf bestimmte Projekte, wenn kein Admin */
        LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        List<Process> processes = serviceManager.getProcessService().getProcessTemplates();
        if (loginForm != null) {
            User currentUser = loginForm.getMyBenutzer();
            try {
                currentUser = serviceManager.getUserService().getById(loginForm.getMyBenutzer().getId());
            } catch (DAOException e) {
                logger.error(e);
            }
            if (currentUser != null) {
                /*
                 * wenn die maximale Berechtigung nicht Admin ist, dann nur
                 * bestimmte
                 */
                if (loginForm.getMaximaleBerechtigung() > 1) {
                    ArrayList<Integer> projectIds = new ArrayList<>();
                    for (Project project : currentUser.getProjects()) {
                        projectIds.add(project.getId());
                    }
                    if (projectIds.size() > 0) {
                        processes = serviceManager.getProcessService().getProcessTemplatesForUser(projectIds);
                    }
                }
            }
        }

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
                    DocStruct myTempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            myTempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren()
                                    .get(0);
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
                                for (Person p : myTempStruct.getAllPersons()) {
                                    authors.append(p.getLastname());
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        authors.append(", ");
                                        authors.append(p.getFirstname());
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
                            MetadataType mdt = UghHelper.getMetadataType(
                                    serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                    field.getMetadata());
                            Metadata md = UghHelper.getMetadata(myTempStruct, mdt);
                            if (md != null) {
                                field.setValue(md.getValue());
                                md.setValue(field.getValue().replace("&amp;", "&"));
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
            /* erstes Werkstück durchlaufen */
            Workpiece werk = tempProzess.getWorkpieces().get(0);
            for (Property workpieceProperty : werk.getProperties()) {
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
            /* erste Vorlage durchlaufen */
            Template vor = tempProzess.getTemplates().get(0);
            for (Property templateProperty : vor.getProperties()) {
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
            DocStruct colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
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
    public String createNewProcess()
            throws ReadException, IOException, PreferencesException, WriteException {
        Helper.getHibernateSession().evict(this.prozessKopie);

        this.prozessKopie.setId(null);
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
            logger.error(e);
            logger.error("error on save: ", e);
            return null;
        }

        String baseProcessDirectory = serviceManager.getProcessService().getProcessDataDirectory(this.prozessKopie).toString();
        boolean successful = serviceManager.getFileService().createMetaDirectory(URI.create(""), baseProcessDirectory);
        if (!successful) {
            String message = "Metadata directory: " + baseProcessDirectory + "in path:"
                    +  ConfigCore.getKitodoDataDirectory() + " was not created!";
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
            DocStruct populizer = null;
            try {
                populizer = rdf.getDigitalDocument().getLogicalDocStruct();
                if (populizer.getAnchorClass() != null && populizer.getAllChildren() == null) {
                    Prefs ruleset = serviceManager.getRulesetService().getPreferences(prozessKopie.getRuleset());
                    while (populizer.getType().getAnchorClass() != null) {
                        populizer = populizer.createChild(populizer.getType().getAllAllowedDocStructTypes().get(0),
                                rdf.getDigitalDocument(), ruleset);
                    }
                }
            } catch (NullPointerException | IndexOutOfBoundsException e) { // if
                // getAllAllowedDocStructTypes()
                // returns null
                Helper.setFehlerMeldung("DocStrctType is configured as anchor but has no allowedchildtype.",
                        populizer != null && populizer.getType() != null ? populizer.getType().getName() : null);
            } catch (UGHException catchAll) {
                Helper.setFehlerMeldung(catchAll.getMessage());
            }

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    /* welches Docstruct */
                    DocStruct tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
                    DocStruct tempChild = null;
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            tempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct().getAllChildren()
                                    .get(0);
                        } catch (RuntimeException e) {
                            logger.error(e.getMessage() + " The first child below the top structure could not be determined!");
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
                            logger.error(e);
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
                            MetadataType mdt = UghHelper.getMetadataType(
                                    serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                    field.getMetadata());
                            Metadata metadata = UghHelper.getMetadata(tempStruct, mdt);
                            if (metadata != null) {
                                metadata.setValue(field.getValue());
                            }
                            /*
                             * wenn dem Topstruct und dem Firstchild der Wert
                             * gegeben werden soll
                             */
                            if (tempChild != null) {
                                metadata = UghHelper.getMetadata(tempChild, mdt);
                                if (metadata != null) {
                                    metadata.setValue(field.getValue());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Helper.setFehlerMeldung(e);
                    }
                }
            }

            updateMetadata();

            /*
             * Collectionen hinzufügen
             */
            DocStruct colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
            try {
                addCollections(colStruct);
                /*
                 * falls ein erstes Kind vorhanden ist, sind die Collectionen
                 * dafür
                 */
                colStruct = colStruct.getAllChildren().get(0);
                addCollections(colStruct);
            } catch (RuntimeException e) {
                logger.error(e.getMessage() + " The first child below the top structure could not be determined!");
            }

            /*
             * Imagepfad hinzufügen (evtl. vorhandene zunächst löschen)
             */
            try {
                MetadataType mdt = UghHelper.getMetadataType(this.prozessKopie, "pathimagefiles");
                List<? extends Metadata> allImagePaths = this.rdf.getDigitalDocument().getPhysicalDocStruct()
                        .getAllMetadataByType(mdt);
                if (allImagePaths != null && allImagePaths.size() > 0) {
                    for (Metadata metadata : allImagePaths) {
                        this.rdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(metadata);
                    }
                }
                Metadata newMetadata = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newMetadata.setValue("file:/" + serviceManager.getFileService().getImagesDirectory(this.prozessKopie)
                            + this.prozessKopie.getTitle().trim() + DIRECTORY_SUFFIX);
                } else {
                    newMetadata.setValue("file://" + serviceManager.getFileService().getImagesDirectory(this.prozessKopie)
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

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
                logger.error("creation of new process throws an error: ", e);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("UghHelperException", e.getMessage());
                logger.error("creation of new process throws an error: ", e);
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());
                logger.error("creation of new process throws an error: ", e);
            }

        }

        // Create configured directories
        serviceManager.getProcessService().createProcessDirs(this.prozessKopie);

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProcess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
            return null;
        } else {
            try {
                serviceManager.getProcessService().save(this.prozessKopie);
            } catch (DataException e) {
                logger.error("error on save: ", e);
                return null;
            }
        }

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        startTaskScriptThreads();

        return "/pages/NewProcess/Page3";
    }

    private void updateTasks() {
        for (Task task : this.prozessKopie.getTasks()) {
            /*
             * always save date and user for each step
             */
            task.setProcessingTime(this.prozessKopie.getCreationDate());
            task.setEditTypeEnum(TaskEditType.AUTOMATIC);
            LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
            if (loginForm != null) {
                task.setProcessingUser(loginForm.getMyBenutzer());
            }

            /*
             * only if its done, set edit start and end date
             */
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
            DocStruct enricher = rdf.getDigitalDocument().getLogicalDocStruct();
            Map<String, Map<String, Metadata>> higherLevelMetadata = new HashMap<>();
            while (enricher.getAllChildren() != null) {
                // save higher level metadata for lower enrichment
                List<Metadata> allMetadata = enricher.getAllMetadata();
                if (allMetadata == null) {
                    allMetadata = Collections.emptyList();
                }
                for (Metadata available : allMetadata) {
                    Map<String, Metadata> availableMetadata = higherLevelMetadata
                            .containsKey(available.getType().getName())
                            ? higherLevelMetadata.get(available.getType().getName()) : new HashMap<>();
                    if (!availableMetadata.containsKey(available.getValue())) {
                        availableMetadata.put(available.getValue(), available);
                    }
                    higherLevelMetadata.put(available.getType().getName(), availableMetadata);
                }

                // enrich children with inherited metadata
                for (DocStruct nextChild : enricher.getAllChildren()) {
                    enricher = nextChild;
                    for (Entry<String, Map<String, Metadata>> availableHigherMetadata : higherLevelMetadata
                            .entrySet()) {
                        String enrichable = availableHigherMetadata.getKey();
                        boolean addable = false;
                        List<MetadataType> addableTypesNotNull = enricher.getAddableMetadataTypes();
                        if (addableTypesNotNull == null) {
                            addableTypesNotNull = Collections.emptyList();
                        }
                        for (MetadataType addableMetadata : addableTypesNotNull) {
                            if (addableMetadata.getName().equals(enrichable)) {
                                addable = true;
                                break;
                            }
                        }
                        if (!addable) {
                            continue;
                        }
                        there: for (Entry<String, Metadata> higherElement : availableHigherMetadata.getValue()
                                .entrySet()) {
                            List<Metadata> amNotNull = enricher.getAllMetadata();
                            if (amNotNull == null) {
                                amNotNull = Collections.emptyList();
                            }
                            for (Metadata existentMetadata : amNotNull) {
                                if (existentMetadata.getType().getName().equals(enrichable)
                                        && existentMetadata.getValue().equals(higherElement.getKey())) {
                                    continue there;
                                }
                            }
                            try {
                                enricher.addMetadata(higherElement.getValue());
                            } catch (UGHException didNotWork) {
                                logger.info(didNotWork);
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

    private void addCollections(DocStruct colStruct) {
        for (String s : this.digitalCollections) {
            try {
                Metadata md = new Metadata(UghHelper.getMetadataType(
                        serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                        "singleDigCollection"));
                md.setValue(s);
                md.setDocStruct(colStruct);
                colStruct.addMetadata(md);
            } catch (UghHelperException | DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
            }
        }
    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen.
     */
    private void removeCollections(DocStruct colStruct) {
        try {
            MetadataType mdt = UghHelper.getMetadataType(
                    serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                    "singleDigCollection");
            ArrayList<Metadata> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt));
            if (myCollections.size() > 0) {
                for (Metadata md : myCollections) {
                    colStruct.removeMetadata(md);
                }
            }
        } catch (UghHelperException | DocStructHasNoTypeException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            logger.error(e);
        }
    }

    /**
     * Create new file format.
     */
    public void createNewFileformat() {
        Prefs myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());
        try {
            DigitalDocument dd = new DigitalDocument();
            Fileformat ff = new XStream(myPrefs);
            ff.setDigitalDocument(dd);
            /* BoundBook hinzufügen */
            DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            /* Monographie */
            if (!ConfigOpac.getDoctypeByName(this.docType).isPeriodical()
                    && !ConfigOpac.getDoctypeByName(this.docType).isMultiVolume()) {
                DocStructType dsty = myPrefs
                        .getDocStrctTypeByName(ConfigOpac.getDoctypeByName(this.docType).getRulesetType());
                DocStruct ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);
                this.rdf = ff;
            } else if (ConfigOpac.getDoctypeByName(this.docType).isPeriodical()) {
                /* Zeitschrift */
                DocStructType dsty = myPrefs.getDocStrctTypeByName("Periodical");
                DocStruct ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);

                DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("PeriodicalVolume");
                DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                ds.addChild(dsvolume);
                this.rdf = ff;
            } else if (ConfigOpac.getDoctypeByName(this.docType).isMultiVolume()) {
                /* MultivolumeBand */
                DocStructType dsty = myPrefs.getDocStrctTypeByName("MultiVolumeWork");
                DocStruct ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);

                DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Volume");
                DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                ds.addChild(dsvolume);
                this.rdf = ff;
            }
            if (this.docType.equals("volumerun")) {
                DocStructType dsty = myPrefs.getDocStrctTypeByName("VolumeRun");
                DocStruct ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);

                DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Record");
                DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                ds.addChild(dsvolume);
                this.rdf = ff;
            }

        } catch (TypeNotAllowedForParentException | TypeNotAllowedAsChildException | PreferencesException e) {
            logger.error(e);
        } catch (FileNotFoundException e) {
            logger.error("Error while reading von opac-config", e);
            Helper.setFehlerMeldung("Error while reading von opac-config", e.getMessage());
        }
    }

    private void addProperties() {
        /*
         * Vorlageneigenschaften initialisieren
         */
        Template vor;
        if (serviceManager.getProcessService().getTemplatesSize(this.prozessKopie) > 0) {
            vor = this.prozessKopie.getTemplates().get(0);
        } else {
            vor = new Template();
            vor.setProcess(this.prozessKopie);
            List<Template> vorlagen = new ArrayList<>();
            vorlagen.add(vor);
            this.prozessKopie.setTemplates(vorlagen);
        }

        /*
         * Werkstückeigenschaften initialisieren
         */
        Workpiece werk;
        if (serviceManager.getProcessService().getWorkpiecesSize(this.prozessKopie) > 0) {
            werk = this.prozessKopie.getWorkpieces().get(0);
        } else {
            werk = new Workpiece();
            werk.setProcess(this.prozessKopie);
            List<Workpiece> werkstuecke = new ArrayList<>();
            werkstuecke.add(werk);
            this.prozessKopie.setWorkpieces(werkstuecke);
        }

        for (AdditionalField field : this.additionalFields) {
            if (field.getShowDependingOnDoctype()) {
                if (field.getFrom().equals("werk")) {
                    BeanHelper.addProperty(werk, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("vorlage")) {
                    BeanHelper.addProperty(vor, field.getTitle(), field.getValue());
                }
                if (field.getFrom().equals("prozess")) {
                    BeanHelper.addProperty(this.prozessKopie, field.getTitle(), field.getValue());
                }
            }
        }

        for (String col : digitalCollections) {
            BeanHelper.addProperty(prozessKopie, "digitalCollection", col);
        }
        /* Doctype */
        BeanHelper.addProperty(werk, "DocType", this.docType);
        /* Tiffheader */
        BeanHelper.addProperty(werk, "TifHeaderImagedescription", this.tifHeaderImageDescription);
        BeanHelper.addProperty(werk, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        BeanHelper.addProperty(prozessKopie, "Template", prozessVorlage.getTitle());
        BeanHelper.addProperty(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
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

                Fileformat tmp = rdf;

                createNewFileformat();
                try {
                    if (rdf.getDigitalDocument().getLogicalDocStruct()
                            .equals(tmp.getDigitalDocument().getLogicalDocStruct())) {
                        rdf = tmp;
                    } else {
                        DocStruct oldLogicalDocstruct = tmp.getDigitalDocument().getLogicalDocStruct();
                        DocStruct newLogicalDocstruct = rdf.getDigitalDocument().getLogicalDocStruct();
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
                    logger.error(e);
                }
                try {
                    fillFieldsFromMetadataFile();
                } catch (PreferencesException e) {
                    logger.error(e);
                }
            }
        }
    }

    private void copyMetadata(DocStruct oldDocStruct, DocStruct newDocStruct) {

        if (oldDocStruct.getAllMetadata() != null) {
            for (Metadata md : oldDocStruct.getAllMetadata()) {
                try {
                    newDocStruct.addMetadata(md);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    logger.error(e);
                }
            }
        }
        if (oldDocStruct.getAllPersons() != null) {
            for (Person p : oldDocStruct.getAllPersons()) {
                try {
                    newDocStruct.addPerson(p);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    logger.error(e);
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
    public void setAdditionalField(String key, String value, boolean strict) throws RuntimeException {
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
        this.possibleDigitalCollection = new ArrayList<>();
        ArrayList<String> defaultCollections = new ArrayList<>();

        String filename = FilenameUtils.concat(ConfigCore.getKitodoConfigDirectory(),
                FileNames.DIGITAL_COLLECTIONS_FILE);
        if (!(new File(filename).exists())) {
            Helper.setFehlerMeldung("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<>();
        try {
            /* Datei einlesen und Root ermitteln */
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(filename));
            Element root = doc.getRootElement();
            /* alle Projekte durchlaufen */
            List<Element> projekte = root.getChildren();
            for (Element project : projekte) {
                // collect default collections
                if (project.getName().equals("default")) {
                    List<Element> myCols = project.getChildren("DigitalCollection");
                    for (Element digitalCollection : myCols) {
                        if (digitalCollection.getAttribute("default") != null
                                && digitalCollection.getAttributeValue("default").equalsIgnoreCase("true")) {
                            digitalCollections.add(digitalCollection.getText());
                        }
                        defaultCollections.add(digitalCollection.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = project.getChildren("name");
                    for (Element projectName : projektnamen) {
                        // all all collections to list
                        if (projectName.getText().equalsIgnoreCase(this.prozessKopie.getProject().getTitle())) {
                            List<Element> myCols = project.getChildren("DigitalCollection");
                            for (Element digitalCollection : myCols) {
                                if (digitalCollection.getAttribute("default") != null
                                        && digitalCollection.getAttributeValue("default").equalsIgnoreCase("true")) {
                                    digitalCollections.add(digitalCollection.getText());
                                }
                                this.possibleDigitalCollection.add(digitalCollection.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e1) {
            logger.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollection.size() == 0) {
            this.possibleDigitalCollection = defaultCollections;
        }

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
        } catch (Throwable t) {
            logger.error("Error while reading von opac-config", t);
            Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
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
        } catch (Throwable t) {
            logger.error("Error while reading von opac-config", t);
            Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
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
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
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
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("UngueltigeDaten: ") + "Bandnummer ist keine gültige Zahl");
                } else {
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("UngueltigeDaten: ") + "Volume number is not a valid number");
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
        String tifDefinition = "";
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }
        tifDefinition = cp.getParamString("tifheader." + this.docType, "intranda");

        /*
         * evtuelle Ersetzungen
         */
        tifDefinition = tifDefinition.replaceAll("\\[\\[", "<");
        tifDefinition = tifDefinition.replaceAll("\\]\\]", ">");

        /*
         * Documentname ist im allgemeinen = Prozesstitel
         */
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        this.tifHeaderImageDescription = "";
        /*
         * Imagedescription
         */
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
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
                } catch (Throwable t) {
                    logger.error("Error while reading von opac-config", t);
                    Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
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
                logger.error(e);
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
            logger.error("Excetion thrown, when creating the docket", e.getMessage());
            // TODO: Handle exceptions in Frontend
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
            logger.error("Error while reading von opac-config", e);
            Helper.setFehlerMeldung("Error while reading von opac-config", e.getMessage());
            return false;
        }
    }

    /**
     * Returns the representation of the file holding the document metadata in
     * memory.
     *
     * @return the metadata file in memory
     */
    public Fileformat getFileformat() {
        return rdf;
    }
}
