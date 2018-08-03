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
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.importer.ImportObject;
import org.jdom.JDOMException;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;

public class CopyProcess extends ProzesskopieForm {

    private static final Logger logger = LogManager.getLogger(CopyProcess.class);
    private FileformatInterface myRdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private Template template = new Template();
    private Process prozessKopie = new Process();
    private Project project;
    /* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
    private boolean useOpac;
    private boolean useTemplates;
    private URI metadataFile;
    private HashMap<String, Boolean> standardFields;
    private List<AdditionalField> additionalFields;
    private List<String> digitalCollections;
    private StringBuilder tifHeaderImageDescription = new StringBuilder();
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
    // TODO: why this not used ImportObject here?
    public String prepare(ImportObject io) {
        if (serviceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            return "";
        }

        clearValues();
        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.template.getRuleset());
        try {
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e.getMessage(), e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setProject(this.template.getProject());
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.prozessKopie.setDocket(this.template.getDocket());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.template, this.prozessKopie);

        return this.naviFirstPage;
    }

    @Override
    public String prepare(int templateId, int projectId) {
        try {
            this.template = serviceManager.getTemplateService().getById(templateId);
            this.project = serviceManager.getProjectService().getById(projectId);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return null;
        }
        if (serviceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            for (Task s : this.template.getTasks()) {
                if (serviceManager.getTaskService().getUserGroupsSize(s) == 0
                        && serviceManager.getTaskService().getUsersSize(s) == 0) {
                    Helper.setErrorMessage("Kein Benutzer festgelegt für: ", s.getTitle());
                }
            }
            return "";
        }

        clearValues();
        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.template.getRuleset());
        try {
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e.getMessage(), e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setProject(this.template.getProject());
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.template, this.prozessKopie);

        initializePossibleDigitalCollections();

        return this.naviFirstPage;
    }

    private void readProjectConfigs() {
        // depending on the project configuration display the correct fields in
        // the GUI
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.template.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        this.docType = cp.getParamString(CREATE_NEW_PROCESS + ".defaultdoctype",
            ConfigOpac.getAllDoctypes().get(0).getTitle());
        this.useOpac = cp.getParamBoolean(CREATE_NEW_PROCESS + ".opac[@use]");
        this.useTemplates = cp.getParamBoolean(CREATE_NEW_PROCESS + ".templates[@use]");
        this.naviFirstPage = "NewProcess/Page1";
        if (this.opacKatalog.equals("")) {
            this.opacKatalog = cp.getParamString(CREATE_NEW_PROCESS + ".opac.catalogue");
        }

        /*
         * die auszublendenden Standard-Felder ermitteln
         */
        for (String t : cp.getParamList(ITEM_LIST + ".hide")) {
            this.standardFields.put(t, false);
        }

        /*
         * die einzublendenen (zusätzlichen) Eigenschaften ermitteln
         */
        int count = cp.getParamList(ITEM_LIST_ITEM).size();
        for (int i = 0; i < count; i++) {
            AdditionalField fa = new AdditionalField(this);
            fa.setFrom(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@from]"));
            fa.setTitle(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")"));
            fa.setRequired(cp.getParamBoolean(ITEM_LIST_ITEM + "(" + i + ")[@required]"));
            fa.setIsdoctype(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@isdoctype]"));
            fa.setIsnotdoctype(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@isnotdoctype]"));

            // attributes added 30.3.09
            String test = (cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@initStart]"));
            fa.setInitStart(test);

            fa.setInitEnd(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@initEnd]"));

            /*
             * Bindung an ein Metadatum eines Docstructs
             */
            if (cp.getParamBoolean(ITEM_LIST_ITEM + "(" + i + ")[@ughbinding]")) {
                fa.setUghbinding(true);
                fa.setDocstruct(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@docstruct]"));
                fa.setMetadata(cp.getParamString(ITEM_LIST_ITEM + "(" + i + ")[@metadata]"));
            }

            /*
             * prüfen, ob das aktuelle Item eine Auswahlliste werden soll
             */
            int selectItemCount = cp.getParamList(ITEM_LIST_ITEM + "(" + i + ").select").size();
            /* Children durchlaufen und SelectItems erzeugen */
            if (selectItemCount > 0) {
                fa.setSelectList(new ArrayList<>());
            }
            for (int j = 0; j < selectItemCount; j++) {
                String svalue = cp.getParamString(ITEM_LIST_ITEM + "(" + i + ").select(" + j + ")[@label]");
                String sid = cp.getParamString(ITEM_LIST_ITEM + "(" + i + ").select(" + j + ")");
                fa.getSelectList().add(new SelectItem(sid, svalue, null));
            }
            this.additionalFields.add(fa);
        }
    }

    /**
     * OpacAnfrage.
     */
    @Override
    public void evaluateOpac() {
        clearValues();
        readProjectConfigs();
        try {
            PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.template.getRuleset());
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            this.myRdf.read(this.metadataFile.getPath());

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getDocStructType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

            fillFieldsFromConfig();

        } catch (PreferencesException | ReadException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"Opac-Ergebnisses" }, logger, e);
        }
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
                            logger.error(e.getMessage(), e);
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (field.getMetadata().equals("ListOfCreators")) {
                            field.setValue(getAuthors(myTempStruct.getAllPersons()));
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
                        Helper.setErrorMessage(e.getMessage(), logger, e);
                    }
                }
            }
        }
    }

    private void fillFieldsFromConfig() {
        for (AdditionalField field : this.additionalFields) {
            if (!field.isUghbinding() && field.getShowDependingOnDoctype() && Objects.nonNull(field.getSelectList())
                    && !field.getSelectList().isEmpty()) {
                field.setValue((String) field.getSelectList().get(0).getValue());
            }
        }
        calculateTiffHeader();

    }

    /**
     * Reset all configuration properties and fields.
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
        this.tifHeaderImageDescription = new StringBuilder();
    }

    /**
     * Auswahl des Prozesses auswerten.
     */
    @Override
    public String templateAuswahlAuswerten() throws DAOException {
        /* den ausgewählten Prozess laden */
        Process tempProzess = serviceManager.getProcessService().getById(this.auswahl);
        if (serviceManager.getProcessService().getWorkpiecesSize(tempProzess) > 0) {
            for (Property workpieceProperty : tempProzess.getWorkpieces()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitle().equals(workpieceProperty.getTitle())) {
                        field.setValue(workpieceProperty.getValue());
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

        try {
            this.myRdf = serviceManager.getProcessService().readMetadataAsTemplateFile(tempProzess);
        } catch (ReadException | PreferencesException | IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"Template-Metadaten" }, logger, e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            DocStructInterface colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct, this.prozessKopie);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct, this.prozessKopie);
        } catch (PreferencesException e) {
            Helper.setErrorMessage("Fehler beim Anlegen des Vorgangs", logger, e);
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
        boolean valid = isProcessTitleCorrect(this.prozessKopie);

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
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype()
                    && (StringUtils.isBlank(field.getValue()))) {
                valid = false;
                Helper.setErrorMessage(INCOMPLETE_DATA,
                    field.getTitle() + " " + Helper.getTranslation("processCreationErrorFieldIsEmpty"));
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

        if (ConfigCore.getBooleanParameter(Parameters.MASS_IMPORT_UNIQUE_TITLE, true)) {
            valid = isProcessTitleCorrect(this.prozessKopie);
        }
        return valid;
    }

    /**
     * Anlegen des Prozesses und save der Metadaten.
     */
    public Process neuenProzessAnlegen() throws ReadException, IOException, PreferencesException, WriteException {
        serviceManager.getProcessService().evict(this.prozessKopie);

        this.prozessKopie.setId(null);

        addProperties(null);
        updateTasks(this.prozessKopie);

        try {
            serviceManager.getProcessService().save(this.prozessKopie);
            serviceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
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
            Helper.setErrorMessage(message);
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

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        serviceManager.getProcessService().refresh(this.prozessKopie);
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
        serviceManager.getProcessService().evict(this.prozessKopie);

        this.prozessKopie.setId(null);
        addProperties(io);
        updateTasks(this.prozessKopie);

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
        if (this.myRdf == null) {
            createNewFileformat();
        }

        serviceManager.getFileService().writeMetadataFile(this.myRdf, this.prozessKopie);

        serviceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        serviceManager.getProcessService().refresh(this.prozessKopie);
        return this.prozessKopie;
    }

    @Override
    public void createNewFileformat() {

        PrefsInterface myPrefs = serviceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());

        FileformatInterface ff;
        try {
            ff = UghImplementation.INSTANCE.createMetsMods(myPrefs);
            ff.read(this.metadataFile.getPath());
        } catch (PreferencesException | ReadException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addProperties(ImportObject io) {
        if (io == null) {
            addAdditionalFields(this.additionalFields, this.prozessKopie);

            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
                this.tifHeaderImageDescription.toString());
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        } else {
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
                this.tifHeaderImageDescription.toString());
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
            BeanHelper.addPropertyForProcess(prozessKopie, "Template", this.template.getTitle());
            BeanHelper.addPropertyForProcess(prozessKopie, "TemplateID", String.valueOf(this.template.getId()));
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
    public Template getTemplate() {
        return this.template;
    }

    @Override
    public void setTemplate(Template template) {
        this.template = template;
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
    public Map<String, Boolean> getStandardFields() {
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

    /**
     * Generate process titles and other details.
     */
    @Override
    public void calculateProcessTitle() {
        StringBuilder newTitle = new StringBuilder();
        String titleDefinition;
        try {
            titleDefinition = getTitleDefinition(this.template.getProject().getTitle(), this.docType);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(titleDefinition, "+");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // if the string begins and ends with ', then take over the content
            if (token.startsWith("'") && token.endsWith("'")) {
                newTitle.append(token, 1, token.length() - 1);
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
                        additionalField.setValue(CopyProcess.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (additionalField.getTitle().equals(token) && additionalField.getShowDependingOnDoctype()
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

    private String calcProcessTitleCheck(String fieldName, String fieldValue) {
        String result = fieldValue;

        if (fieldName.equals("Bandnummer")) {
            try {
                int bandInt = Integer.parseInt(fieldValue);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                result = df.format(bandInt);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(INCOMPLETE_DATA, "Bandnummer ist keine gültige Zahl", logger, e);
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
            cp = new ConfigProjects(this.template.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        tifDefinition = cp.getParamString("tifheader." + this.docType.toLowerCase(), "blabla");

        // possible replacements
        tifDefinition = tifDefinition.replaceAll("\\[\\[", "<");
        tifDefinition = tifDefinition.replaceAll("\\]\\]", ">");

        /*
         * Documentname ist im allgemeinen = Prozesstitel
         */
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        this.tifHeaderImageDescription = new StringBuilder();

        // image description
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String string = tokenizer.nextToken();
            // if the string begins and ends with ', then take over the content
            if (string.startsWith("'") && string.endsWith("'") && string.length() > 2) {
                this.tifHeaderImageDescription.append(string, 1, string.length() - 1);
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
                        additionalField.setValue(CopyProcess.atstsl);
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
}
