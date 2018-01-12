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
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.FileNames;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.importer.ImportObject;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.UghImplementation;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class CopyProcess extends ProzesskopieForm {

    private static final Logger logger = LogManager.getLogger(CopyProcess.class);
    private FileformatInterface myRdf;
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
    private String naviFirstPage;
    private Integer auswahl;
    private String docType;
    // TODO: check use of atstsl. Why is it never modified?
    private static final String atstsl = "";
    private List<String> possibleDigitalCollection;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Prepare import object.
     *
     * @param io
     *            import object
     * @return page or empty String
     */
    public String prepare(ImportObject io) {
        if (serviceManager.getProcessService().getContainsUnreachableSteps(this.prozessVorlage)) {
            return "";
        }

        clearValues();
        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessVorlage.getRuleset());
        try {
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());
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

        /*
         * Kopie der Prozessvorlage anlegen
         */
        BeanHelper.copyTasks(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyScanTemplates(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyWorkpieces(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyProperties(this.prozessVorlage, this.prozessKopie);

        return this.naviFirstPage;
    }

    @Override
    public String prepare(int id) {
        try {
            this.prozessVorlage = serviceManager.getProcessService().getById(id);
        } catch (DAOException e) {
            logger.error(e.getMessage());
            return null;
        }
        if (serviceManager.getProcessService().getContainsUnreachableSteps(this.prozessVorlage)) {
            for (Task s : this.prozessVorlage.getTasks()) {
                if (serviceManager.getTaskService().getUserGroupsSize(s) == 0
                        && serviceManager.getTaskService().getUsersSize(s) == 0) {
                    Helper.setFehlerMeldung("Kein Benutzer festgelegt für: ", s.getTitle());
                }
            }
            return "";
        }

        clearValues();
        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessVorlage.getRuleset());
        try {
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setTemplate(false);
        this.prozessKopie.setInChoiceListShown(false);
        this.prozessKopie.setProject(this.prozessVorlage.getProject());
        this.prozessKopie.setRuleset(this.prozessVorlage.getRuleset());
        this.digitalCollections = new ArrayList<>();

        /*
         * Kopie der Prozessvorlage anlegen
         */
        BeanHelper.copyTasks(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyScanTemplates(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyWorkpieces(this.prozessVorlage, this.prozessKopie);
        BeanHelper.copyProperties(this.prozessVorlage, this.prozessKopie);

        initializePossibleDigitalCollections();

        return this.naviFirstPage;
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
        this.naviFirstPage = "NewProcess/Page1";
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
    @Override
    public String evaluateOpac() {
        clearValues();
        readProjectConfigs();
        try {
            PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessVorlage.getRuleset());
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

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
    private void fillFieldsFromMetadataFile(FileformatInterface myRdf) throws PreferencesException {
        if (myRdf != null) {

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    /* welches Docstruct */

                    DocStructInterface myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            logger.error(e);
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (field.getMetadata().equals("ListOfCreators")) {
                            /* bei Autoren die Namen zusammenstellen */
                            StringBuilder authors = new StringBuilder();
                            if (myTempStruct.getAllPersons() != null) {
                                for (PersonInterface p : myTempStruct.getAllPersons()) {
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
                            MetadataTypeInterface mdt = UghHelper.getMetadataType(
                                serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                field.getMetadata());
                            MetadataInterface md = UghHelper.getMetadata(myTempStruct, mdt);
                            if (md != null) {
                                field.setValue(md.getValue());
                            }
                        }
                    } catch (UghHelperException e) {
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                } // end if ughbinding
            } // end for
        } // end if myrdf==null
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
    @Override
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

        try {
            this.myRdf = serviceManager.getProcessService().readMetadataAsTemplateFile(tempProzess);
        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen der Template-Metadaten ", e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            DocStructInterface colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Fehler beim Anlegen des Vorgangs", e);
            logger.error("Fehler beim Anlegen des Vorgangs", e);
        } catch (RuntimeException e) {
            /*
             * das Firstchild unterhalb des Topstructs konnte nicht ermittelt
             * werden
             */
        }

        return "";
    }

    /**
     * Validierung der Eingaben.
     *
     * @return sind Fehler bei den Eingaben vorhanden?
     */
    private boolean isContentValid() {
        /*
         * Vorbedingungen prüfen
         */
        boolean valide = true;

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
            Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
        }

        if (this.prozessKopie.getTitle() != null) {
            valide = isProcessTitleAvailable(this.prozessKopie.getTitle());
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
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype()
                    && (StringUtils.isBlank(field.getValue()))) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitle() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }
        }
        return valide;
    }

    @Override
    public String goToPageOne() {
        return this.naviFirstPage;
    }

    @Override
    public String goToPageTwo() {
        if (!isContentValid()) {
            return this.naviFirstPage;
        } else {
            return "NewProcess/Page2";
        }
    }

    /**
     * Test title.
     *
     * @return boolean
     */
    public boolean testTitle() {
        boolean valide = true;

        if (ConfigCore.getBooleanParameter("MassImportUniqueTitle", true)) {
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
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            if (this.prozessKopie.getTitle() != null) {
                valide = isProcessTitleAvailable(this.prozessKopie.getTitle());
            }
        }
        return valide;
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
        if (this.myRdf == null) {
            createNewFileformat();
        }

        serviceManager.getFileService().writeMetadataFile(this.myRdf, this.prozessKopie);

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

        this.prozessKopie.setId(null);
        addProperties(io);
        prepareTasksForProcess();

        if (!io.getBatches().isEmpty()) {
            this.prozessKopie.getBatches().addAll(io.getBatches());
        }
        try {
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            e.printStackTrace();
            logger.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage
         * stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        serviceManager.getFileService().writeMetadataFile(this.myRdf, this.prozessKopie);

        if (!addProcessToHistory()) {
            return this.prozessKopie;
        }

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);
        return this.prozessKopie;
    }

    private void prepareTasksForProcess() {
        for (Task task : this.prozessKopie.getTasks()) {
            /*
             * always save date and user for each task
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
            Helper.setFehlerMeldung(e.getMessage(), "");
            e.printStackTrace();
        }
    }

    @Override
    public void createNewFileformat() {

        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());

        FileformatInterface ff;
        try {
            ff = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            ff.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e);
        }
    }

    private void addProperties(ImportObject io) {
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

        /*
         * jetzt alle zusätzlichen Felder durchlaufen und die Werte hinzufügen
         */
        if (io == null) {
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
            /* Doctype */
            BeanHelper.addProperty(werk, "DocType", this.docType);
            /* Tiffheader */
            BeanHelper.addProperty(werk, "TifHeaderImagedescription", this.tifHeaderImageDescription.toString());
            BeanHelper.addProperty(werk, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        } else {
            BeanHelper.addProperty(werk, "DocType", this.docType);
            /* Tiffheader */
            BeanHelper.addProperty(werk, "TifHeaderImagedescription", this.tifHeaderImageDescription.toString());
            BeanHelper.addProperty(werk, "TifHeaderDocumentname", this.tifHeaderDocumentName);

            for (Property processProperty : io.getProcessProperties()) {
                addProperty(this.prozessKopie, processProperty);
            }
            for (Property workpieceProperty : io.getWorkProperties()) {
                addProperty(werk, workpieceProperty);
            }

            for (Property templateProperty : io.getTemplateProperties()) {
                addProperty(vor, templateProperty);
            }
            BeanHelper.addProperty(prozessKopie, "Template", prozessVorlage.getTitle());
            BeanHelper.addProperty(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
        }
    }

    @Override
    public String getDocType() {
        return this.docType;
    }

    @Override
    public void setDocType(String docType) {
        this.docType = docType;
    }

    @Override
    public Process getProzessVorlage() {
        return this.prozessVorlage;
    }

    @Override
    public void setProzessVorlage(Process prozessVorlage) {
        this.prozessVorlage = prozessVorlage;
    }

    @Override
    public Integer getAuswahl() {
        return this.auswahl;
    }

    @Override
    public void setAuswahl(Integer auswahl) {
        this.auswahl = auswahl;
    }

    @Override
    public List<AdditionalField> getAdditionalFields() {
        return this.additionalFields;
    }

    /**
     * this is needed for GUI, render multiple select only if this is false if
     * this is true use the only choice.
     *
     * @author Wulf
     */
    @Override
    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

    }

    /**
     * this is needed for GUI, render multiple select only if this is false if
     * isSingleChoiceCollection is true use this choice.
     *
     * @author Wulf
     */
    @Override
    public String getDigitalCollectionIfSingleChoice() {
        List<String> pdc = getPossibleDigitalCollections();
        if (pdc.size() == 1) {
            return pdc.get(0);
        } else {
            return null;
        }
    }

    @Override
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

    @Override
    public List<String> getAllOpacCatalogues() {
        return ConfigOpac.getAllCatalogueTitles();
    }

    @Override
    public List<ConfigOpacDoctype> getAllDoctypes() {
        return ConfigOpac.getAllDoctypes();
    }

    /*
     * changed, so that on first request list gets set if there is only one
     * choice
     */
    @Override
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    @Override
    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    @Override
    public HashMap<String, Boolean> getStandardFields() {
        return this.standardFields;
    }

    @Override
    public boolean isUseOpac() {
        return this.useOpac;
    }

    @Override
    public boolean isUseTemplates() {
        return this.useTemplates;
    }

    @Override
    public String getTifHeaderDocumentName() {
        return this.tifHeaderDocumentName;
    }

    @Override
    public void setTifHeaderDocumentName(String tifHeaderDocumentName) {
        this.tifHeaderDocumentName = tifHeaderDocumentName;
    }

    @Override
    public String getTifHeaderImageDescription() {
        return this.tifHeaderImageDescription.toString();
    }

    @Override
    public void setTifHeaderImageDescription(String tifHeaderImageDescription) {
        this.tifHeaderImageDescription = new StringBuilder(tifHeaderImageDescription);
    }

    @Override
    public Process getProzessKopie() {
        return this.prozessKopie;
    }

    @Override
    public void setProzessKopie(Process prozessKopie) {
        this.prozessKopie = prozessKopie;
    }

    @Override
    public String getOpacSuchfeld() {
        return this.opacSuchfeld;
    }

    @Override
    public void setOpacSuchfeld(String opacSuchfeld) {
        this.opacSuchfeld = opacSuchfeld;
    }

    @Override
    public String getOpacKatalog() {
        return this.opacKatalog;
    }

    @Override
    public void setOpacKatalog(String opacKatalog) {
        this.opacKatalog = opacKatalog;
    }

    @Override
    public String getOpacSuchbegriff() {
        return this.opacSuchbegriff;
    }

    @Override
    public void setOpacSuchbegriff(String opacSuchbegriff) {
        this.opacSuchbegriff = opacSuchbegriff;
    }

    /*
     * Helper
     */

    /**
     * Prozesstitel und andere Details generieren.
     */
    @Override
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

    @Override
    public void calculateTiffHeader() {
        String tifDefinition;
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        tifDefinition = cp.getParamString("tifheader." + this.docType.toLowerCase(), "blabla");

        /*
         * evtuelle Ersetzungen
         */
        tifDefinition = tifDefinition.replaceAll("\\[\\[", "<");
        tifDefinition = tifDefinition.replaceAll("\\]\\]", ">");

        /*
         * Documentname ist im allgemeinen = Prozesstitel
         */
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        this.tifHeaderImageDescription = new StringBuilder("");
        /*
         * Imagedescription
         */
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String string = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so
             * übernehmen
             */
            if (string.startsWith("'") && string.endsWith("'") && string.length() > 2) {
                this.tifHeaderImageDescription.append(string.substring(1, string.length() - 1));
            } else if (string.equals("$Doctype")) {

                this.tifHeaderImageDescription.append(this.docType);
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
                    if (additionalField.getTitle().equals(string) && additionalField.getShowDependingOnDoctype()
                            && additionalField.getValue() != null) {
                        this.tifHeaderImageDescription
                                .append(calcProcessTitleCheck(additionalField.getTitle(), additionalField.getValue()));
                    }
                }
            }
        }
    }

    private void addProperty(Template template, Property property) {
        if (!verifyProperty(template.getProperties(), property)) {
            return;
        }

        Property templateProperty = insertDataToProperty(property);
        templateProperty.getTemplates().add(template);
        List<Property> properties = template.getProperties();
        if (properties != null) {
            properties.add(templateProperty);
        }
    }

    private void addProperty(Process process, Property property) {
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

    private void addProperty(Workpiece workpiece, Property property) {
        if (!verifyProperty(workpiece.getProperties(), property)) {
            return;
        }

        Property workpieceProperty = insertDataToProperty(property);
        workpieceProperty.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getProperties();
        if (properties != null) {
            properties.add(workpieceProperty);
        }
    }

    private boolean verifyProperty(List<Property> properties, Property property) {
        if (property.getContainer() == 0) {
            for (Property tempProperty : properties) {
                if (tempProperty.getTitle().equals(property.getTitle()) && tempProperty.getContainer() > 0) {
                    tempProperty.setValue(property.getValue());
                    return false;
                }
            }
        }
        return true;
    }

    private Property insertDataToProperty(Property property) {
        Property newProperty = new Property();
        newProperty.setTitle(property.getTitle());
        newProperty.setValue(property.getValue());
        newProperty.setChoice(property.getChoice());
        newProperty.setContainer(property.getContainer());
        newProperty.setType(property.getType());
        return newProperty;
    }

    public void setMetadataFile(URI mdFile) {
        this.metadataFile = mdFile;
    }

    public URI getMetadataFile() {
        return this.metadataFile;
    }
}
