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

package org.goobi.production.cli.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.FileNames;
import org.goobi.production.constants.Parameters;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.importer.ImportObject;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
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
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.XStream;

public class CopyProcess {

    private static final Logger logger = LogManager.getLogger(CopyProcess.class);
    private Fileformat rdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private Process prozessVorlage = new Process();
    private Process prozessKopie = new Process();
    /* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
    private boolean useOpac;
    private boolean useTemplates;
    private URI metadataFile;
    private HashMap<String, Boolean> standardFields;
    private List<AdditionalField> additionalFields;
    private List<String> digitalCollections;
    private StringBuilder tifHeaderImageDescription = new StringBuilder("");
    private String tifHeaderDocumentName = "";
    private Integer auswahl;
    private Integer guessedImages = 0;
    private String docType;
    // TODO: check use of atstsl. Why is it never modified?
    private static String atstsl = "";
    private List<String> possibleDigitalCollection;
    private static final String DIRECTORY_SUFFIX = "_tif";
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Prepare import object.
     *
     * @param io
     *            import object
     * @return page or empty String
     */
    public boolean prepare(ImportObject io) {
        if (serviceManager.getProcessService().getContainsUnreachableSteps(this.prozessVorlage)) {
            if (serviceManager.getProcessService().getContainsUnreachableSteps(this.prozessVorlage)) {
                if (this.prozessVorlage.getTasks().size() == 0) {
                    Helper.setFehlerMeldung("noStepsInWorkflow");
                }
                for (Task task : this.prozessVorlage.getTasks()) {
                    if (serviceManager.getTaskService().getUserGroupsSize(task) == 0
                            && serviceManager.getTaskService().getUsersSize(task) == 0) {
                        Helper.setFehlerMeldung(Helper.getTranslation("noUserInStep", task.getTitle()));
                    }
                }
                return false;
            }
        }

        clearValues();
        readProjectConfigs();
        Prefs prefs = serviceManager.getRulesetService().getPreferences(this.prozessVorlage.getRuleset());
        try {
            this.rdf = new MetsMods(prefs);
            this.rdf.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setTemplate(false);
        this.prozessKopie.setInChoiceListShown(false);
        this.prozessKopie.setProject(this.prozessVorlage.getProject());
        this.prozessKopie.setRuleset(this.prozessVorlage.getRuleset());
        this.prozessKopie.setDocket(this.prozessVorlage.getDocket());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyScanTemplates(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyWorkpieces(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyProperties(this.prozessVorlage, this.prozessKopie);

        initializePossibleDigitalCollections();

        return true;
    }

    private void readProjectConfigs() {
        /*
         * projektabhängig die richtigen Felder in der Gui anzeigen
         */
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
     * OpacAnfrage.
     */
    public String evaluateOpac() {
        clearValues();
        readProjectConfigs();
        try {
            Prefs myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessVorlage.getRuleset());
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.rdf = new MetsMods(myPrefs);
            this.rdf.read(this.metadataFile.getPath());

            this.docType = this.rdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile();

            fillFieldsFromConfig();

        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
            e.printStackTrace();
        }
        return "";
    }

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei
     * füllen.
     */
    public void fillFieldsFromMetadataFile() throws PreferencesException {
        if (this.rdf != null) {

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    /* welches Docstruct */

                    DocStruct myTempStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
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
                                for (Person p : myTempStruct.getAllPersons()) {
                                    authors.append(p.getLastname());
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        authors.append(", ");
                                        authors.append(p.getFirstname());
                                    }
                                    authors.append("; ");
                                }
                                if (authors.toString().endsWith("; ")) {
                                    authors.substring(0, authors.length() - 2);
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
                            }
                        }
                    } catch (UghHelperException e) {
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                }
            }
        }
    }

    private void fillFieldsFromConfig() {
        for (AdditionalField field : this.additionalFields) {
            if (!field.isUghbinding() && field.getShowDependingOnDoctype()) {
                if (field.getSelectList() != null && field.getSelectList().size() > 0) {
                    field.setValue((String) field.getSelectList().get(0).getValue());
                }

            }
        }
        calculateTiffHeader();
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
        this.additionalFields = new ArrayList<>();
        this.tifHeaderDocumentName = "";
        this.tifHeaderImageDescription = new StringBuilder("");
    }

    /**
     * Auswahl des Prozesses auswerten.
     */
    public String evaluateSelectedTemplate() throws DAOException {
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
            DocStruct colStruct = this.rdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Error on creating process", e);
            logger.error("Error on creating process", e);
        } catch (RuntimeException e) {
            // the first child below the topstruct could not be determined
        }

        return null;
    }

    /**
     * Validation of the input Process.
     *
     * @param criticiseEmptyTitle true if check also for title
     * @return true if copied Process is correct, else false
     */
    public boolean isContentValid(boolean criticiseEmptyTitle) {
        boolean valid = true;

        if (criticiseEmptyTitle) {
            if (this.prozessKopie.getTitle() == null || this.prozessKopie.getTitle().equals("")) {
                valid = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                        + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigCore.getParameter("validateProzessTitelRegex", "[\\w-]*");
            if (!this.prozessKopie.getTitle().matches(validateRegEx)) {
                valid = false;
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            if (this.prozessKopie.getTitle() != null) {
                valid = isProcessTitleAvailable(this.prozessKopie.getTitle());
            }
        }

        // check the standard inputs that must be specified - no collections
        if (this.standardFields.get("collections") && getDigitalCollections().size() == 0) {
            valid = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                    + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        // check the additional inputs that must be specified
        for (AdditionalField field : this.additionalFields) {
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype()
                    && (StringUtils.isBlank(field.getValue()))) {
                valid = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitle() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }
        }
        return valid;
    }

    /**
     * Test title.
     *
     * @return boolean
     */
    public boolean testTitle() {
        boolean valid = true;

        if (ConfigCore.getBooleanParameter("MassImportUniqueTitle", true)) {

            if (this.prozessKopie.getTitle() == null || this.prozessKopie.getTitle().equals("")) {
                valid = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                        + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigCore.getParameter("validateProzessTitelRegex", "[\\w-]*");
            if (!this.prozessKopie.getTitle().matches(validateRegEx)) {
                valid = false;
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            if (this.prozessKopie.getTitle() != null) {
                valid = isProcessTitleAvailable(this.prozessKopie.getTitle());
            }
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
    private boolean isProcessTitleAvailable(String title) {
        long amount;
        try {
            amount = serviceManager.getProcessService().findNumberOfProcessesWithTitle(title);
        } catch (DataException e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
            return false;
        }
        if (amount > 0) {
            Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten:")
                    + Helper.getTranslation("ProcessCreationErrorTitleAlreadyInUse"));
            return false;
        }
        return true;
    }

    /**
     * Anlegen des Prozesses und save der Metadaten.
     */

    public Process neuenProzessAnlegen() throws ReadException, IOException, PreferencesException, WriteException {
        Helper.getHibernateSession().evict(this.prozessKopie);

        this.prozessKopie.setId(null);

        addProperties(null);
        prepareTasksForProcess();

        try {
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            e.printStackTrace();
            logger.error("error on save: ", e);
            return this.prozessKopie;
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

        serviceManager.getFileService().writeMetadataFile(this.rdf, this.prozessKopie);

        if (!addProcessToHistory()) {
            return this.prozessKopie;
        }

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);
        return this.prozessKopie;

    }

    /**
     * Create Process.
     *
     * @param io
     *            import object
     * @return Process object
     */
    public Process createProcess(ImportObject io)
            throws ReadException, IOException, PreferencesException, WriteException {
        Helper.getHibernateSession().evict(this.prozessKopie);

        addProperties(io);
        prepareTasksForProcess();

        if (!io.getBatches().isEmpty()) {
            this.prozessKopie.getBatches().addAll(io.getBatches());
        }
        try {
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            logger.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage
         * stattfand, dann jetzt eine anlegen
         */
        if (this.rdf == null) {
            createNewFileformat();
        }

        serviceManager.getFileService().writeMetadataFile(this.rdf, this.prozessKopie);

        if (!addProcessToHistory()) {
            return this.prozessKopie;
        }

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);
        return this.prozessKopie;
    }

    public boolean createNewProcess()
            throws ReadException, IOException, PreferencesException, WriteException {

        //evict set up id to null
        Helper.getHibernateSession().evict(this.prozessKopie);
        if (!isContentValid(true)) {
            return false;
        }
        addProperties(null);
        prepareTasksForProcess();

        try {
            this.prozessKopie.setSortHelperImages(this.guessedImages);
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            logger.error("error on save: ", e);
            return false;
        }

        String baseProcessDirectory = serviceManager.getProcessService().getProcessDataDirectory(this.prozessKopie).toString();
        boolean successful = serviceManager.getFileService().createMetaDirectory(URI.create(""), baseProcessDirectory);
        if (!successful) {
            String message = "Metadata directory: " + baseProcessDirectory + "in path:"
                    +  ConfigCore.getKitodoDataDirectory() + " was not created!";
            logger.error(message);
            Helper.setFehlerMeldung(message);
            return false;
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
            return false;
        } else {
            try {
                serviceManager.getProcessService().save(this.prozessKopie);
            } catch (DataException e) {
                logger.error("error on save: ", e);
                return false;
            }
        }

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        startTaskScriptThreads();

        return true;
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
                    for (Map.Entry<String, Map<String, Metadata>> availableHigherMetadata : higherLevelMetadata
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
                        there: for (Map.Entry<String, Metadata> higherElement : availableHigherMetadata.getValue()
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

    private void prepareTasksForProcess() {
        for (Task task : this.prozessKopie.getTasks()) {
            /*
             * always save date and user for each task
             */
            task.setProcessingTime(this.prozessKopie.getCreationDate());
            task.setEditTypeEnum(TaskEditType.AUTOMATIC);
            User user = Helper.getCurrentUser();
            if (user != null) {
                task.setProcessingUser(user);
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

    private boolean addProcessToHistory() {
        if (!HistoryAnalyserJob.updateHistoryForProcess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                serviceManager.getProcessService().save(this.prozessKopie);
            } catch (DataException e) {
                e.printStackTrace();
                logger.error("error on save: ", e);
                return false;
            }
        }
        return true;
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
            } catch (UghHelperException | DocStructHasNoTypeException | ugh.exceptions.MetadataTypeNotAllowedException e) {
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

    /*public void createNewFileformat() {

        Prefs myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());

        Fileformat ff;
        try {
            ff = new MetsMods(myPrefs);
            ff.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e);
        }
    }*/

    /**
     * Create new file format.
     */
    public void createNewFileformat() {
        Prefs myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());
        try {
            DigitalDocument dd = new DigitalDocument();
            Fileformat ff = new XStream(myPrefs);
            ff.setDigitalDocument(dd);
            // add BoundBook
            DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(this.docType);

            if (configOpacDoctype != null) {
                // Monographie
                if (!configOpacDoctype.isPeriodical() && !configOpacDoctype.isMultiVolume()) {
                    DocStructType dsty = myPrefs.getDocStrctTypeByName(configOpacDoctype.getRulesetType());
                    DocStruct ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);
                    this.rdf = ff;
                } else if (configOpacDoctype.isPeriodical()) {
                    // Zeitschrift
                    DocStructType dsty = myPrefs.getDocStrctTypeByName("Periodical");
                    DocStruct ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);

                    DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("PeriodicalVolume");
                    DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                    ds.addChild(dsvolume);
                    this.rdf = ff;
                } else if (configOpacDoctype.isMultiVolume()) {
                    // MultivolumeBand
                    DocStructType dsty = myPrefs.getDocStrctTypeByName("MultiVolumeWork");
                    DocStruct ds = dd.createDocStruct(dsty);
                    dd.setLogicalDocStruct(ds);

                    DocStructType dstyvolume = myPrefs.getDocStrctTypeByName("Volume");
                    DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                    ds.addChild(dsvolume);
                    this.rdf = ff;
                }
            } else {
                // TODO: what should happen if configOpacDoctype is null?
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

    private void addProperties(ImportObject io) {
        if (io == null) {
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

            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription", this.tifHeaderImageDescription.toString());
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        } else {
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription", this.tifHeaderImageDescription.toString());
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);

            for (Property processProperty : io.getProcessProperties()) {
                addPropertyForProcess(this.prozessKopie, processProperty);
            }
            for (Property workpieceProperty : io.getWorkProperties()) {
                addPropertyForWorkpiece(this.prozessKopie, workpieceProperty);
            }

            for (Property templateProperty : io.getTemplateProperties()) {
                addPropertyForTemplate(this.prozessKopie, templateProperty);
            }
        }

        BeanHelper.addPropertyForProcess(prozessKopie, "Template", prozessVorlage.getTitle());
        BeanHelper.addPropertyForProcess(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
    }

    public String getDocType() {
        return this.docType;
    }

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
     * this is needed for GUI, render multiple select only if this is false if
     * this is true use the only choice.
     */
    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

    }

    /**
     * this is needed for GUI, render multiple select only if this is false if
     * isSingleChoiceCollection is true use this choice.
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
        String filename = ConfigCore.getKitodoConfigDirectory() + FileNames.DIGITAL_COLLECTIONS_FILE;
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

    public List<String> getAllOpacCatalogues() {
        return ConfigOpac.getAllCatalogueTitles();
    }

    public List<ConfigOpacDoctype> getAllDoctypes() {
        return ConfigOpac.getAllDoctypes();
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
        return this.tifHeaderImageDescription.toString();
    }

    public void setTifHeaderImageDescription(String tifHeaderImageDescription) {
        this.tifHeaderImageDescription = new StringBuilder(tifHeaderImageDescription);
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
    @SuppressWarnings("rawtypes")
    public void calculateProcessTitle() {
        StringBuilder newTitle = new StringBuilder();
        String titeldefinition = "";
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        int count = cp.getParamList("createNewProcess.itemlist.processtitle").size();
        for (int i = 0; i < count; i++) {
            String title = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")");
            String isDocType = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isdoctype]");
            String isNotDocType = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isnotdoctype]");

            title = processNullValues(title);
            isDocType = processNullValues(isDocType);
            isNotDocType = processNullValues(isNotDocType);

            /* wenn nix angegeben wurde, dann anzeigen */
            if (isDocType.equals("") && isNotDocType.equals("")) {
                titeldefinition = title;
                break;
            }

            /* wenn beides angegeben wurde */
            if (!isDocType.equals("") && !isNotDocType.equals("")
                    && StringUtils.containsIgnoreCase(isDocType, this.docType)
                    && !StringUtils.containsIgnoreCase(isNotDocType, this.docType)) {
                titeldefinition = title;
                break;
            }

            /* wenn nur pflicht angegeben wurde */
            if (isNotDocType.equals("") && StringUtils.containsIgnoreCase(isDocType, this.docType)) {
                titeldefinition = title;
                break;
            }
            /* wenn nur "darf nicht" angegeben wurde */
            if (isDocType.equals("") && !StringUtils.containsIgnoreCase(isNotDocType, this.docType)) {
                titeldefinition = title;
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
            if (myString.startsWith("'") && myString.endsWith("'")) {
                newTitle.append(myString.substring(1, myString.length() - 1));
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
                        additionalField.setValue(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (additionalField.getTitle().equals(myString) && additionalField.getShowDependingOnDoctype()
                            && additionalField.getValue() != null) {
                        newTitle.append(calcProcessTitleCheck(additionalField.getTitle(), additionalField.getValue()));
                    }
                }
            }
        }

        if (newTitle.toString().endsWith("_")) {
            newTitle.substring(0, newTitle.length() - 1);
        }
        this.prozessKopie.setTitle(newTitle.toString());
        calculateTiffHeader();
    }

    private String processNullValues(String value) {
        if (value == null) {
            value = "";
        }
        return value;
    }

    private String calcProcessTitleCheck(String fieldName, String fieldvalue) {
        String result = fieldvalue;

        /*
         * Bandnummer
         */
        if (fieldName.equals("Bandnummer")) {
            try {
                int bandInt = Integer.parseInt(fieldvalue);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                result = df.format(bandInt);
            } catch (NumberFormatException e) {
                Helper.setFehlerMeldung("Ungültige Daten: ", "Bandnummer ist keine gültige Zahl");
            }
            if (result != null && result.length() < 4) {
                result = "0000".substring(result.length()) + result;
            }
        }
        return result;
    }

    /**
     * Calculate tiff header.
     */
    public void calculateTiffHeader() {
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }
        String tifDefinition = cp.getParamString("tifheader." + this.docType, "intranda");

        // possible replacements
        tifDefinition = tifDefinition.replaceAll("\\[\\[", "<");
        tifDefinition = tifDefinition.replaceAll("\\]\\]", ">");

        // Documentname ist im allgemeinen = Prozesstitel
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        this.tifHeaderImageDescription = new StringBuilder("");
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
                this.tifHeaderImageDescription.append(myString.substring(1, myString.length() - 1));
            } else if (myString.equals("$Doctype")) {
                /* wenn der Doctype angegeben werden soll */
                try {
                    this.tifHeaderImageDescription.append(ConfigOpac.getDoctypeByName(this.docType).getTifHeaderType());
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
                        this.tifHeaderImageDescription.append(calculateProcessTitleCheck(additionalField.getTitle(),
                                additionalField.getValue()));
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
                this.tifHeaderImageDescription = new StringBuilder(this.tifHeaderImageDescription.toString().replace(title, newTitle));
            } catch (IndexOutOfBoundsException e) {
                logger.error(e);
            }
        }
    }


    private void addPropertyForTemplate(Process template, Property property) {
        if (!verifyProperty(template.getTemplates(), property)) {
            return;
        }

        Property templateProperty = insertDataToProperty(property);
        templateProperty.getTemplates().add(template);
        List<Property> properties = template.getTemplates();
        if (properties != null) {
            properties.add(templateProperty);
        }
    }

    private void addPropertyForProcess(Process process, Property property) {
        if (!verifyProperty(process.getProperties(), property)) {
            return;
        }

        Property processProperty = insertDataToProperty(property);
        processProperty.getProcesses().add(process);
        List<Property> properties = process.getProperties();
        if (properties != null) {
            properties.add(processProperty);
        }
    }

    private void addPropertyForWorkpiece(Process workpiece, Property property) {
        if (!verifyProperty(workpiece.getWorkpieces(), property)) {
            return;
        }

        Property workpieceProperty = insertDataToProperty(property);
        workpieceProperty.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getWorkpieces();
        if (properties != null) {
            properties.add(workpieceProperty);
        }
    }

    private boolean verifyProperty(List<Property> properties, Property property) {
        for (Property tempProperty : properties) {
            if (tempProperty.getTitle().equals(property.getTitle())) {
                tempProperty.setValue(property.getValue());
                return false;
            }
        }
        return true;
    }

    private Property insertDataToProperty(Property property) {
        Property newProperty = new Property();
        newProperty.setTitle(property.getTitle());
        newProperty.setValue(property.getValue());
        newProperty.setChoice(property.getChoice());
        newProperty.setType(property.getType());
        return newProperty;
    }

    public void setMetadataFile(URI mdFile) {
        this.metadataFile = mdFile;
    }

    public URI getMetadataFile() {
        return this.metadataFile;
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

    /* Create Atstsl.
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
}
