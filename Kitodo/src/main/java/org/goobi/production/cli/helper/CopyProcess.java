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

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.importer.ImportObject;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.UghHelperException;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
import org.kitodo.production.forms.ProzesskopieForm;
import org.kitodo.production.helper.AdditionalField;
import org.kitodo.production.helper.BeanHelper;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.UghHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;

public class CopyProcess extends ProzesskopieForm {

    private static final Logger logger = LogManager.getLogger(CopyProcess.class);
    private transient FileformatInterface myRdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private URI metadataFile;
    private String naviFirstPage;
    private Process processForChoice;
    // TODO: check use of atstsl. Why is it never modified?
    private static final String atstsl = "";

    /**
     * Prepare import object.
     *
     * @param io
     *            import object
     * @return page or empty String
     */
    // TODO: why this not used ImportObject here?
    public String prepare(ImportObject io) {
        if (ServiceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            return "";
        }

        clearValues();
        LegacyPrefsHelper myPrefs = ServiceManager.getRulesetService().getPreferences(this.template.getRuleset());
        try {
            this.myRdf = new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) myPrefs).getRuleset());
            this.myRdf.read(this.metadataFile.getPath());
        } catch (ReadException e) {
            logger.error(e.getMessage(), e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setProject(this.project);
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.prozessKopie.setDocket(this.template.getDocket());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.template, this.prozessKopie);

        return this.naviFirstPage;
    }

    @Override
    public String prepare(int templateId, int projectId) {
        try {
            this.template = ServiceManager.getTemplateService().getById(templateId);
            this.project = ServiceManager.getProjectService().getById(projectId);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return null;
        }
        if (ServiceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            for (Task s : this.template.getTasks()) {
                if (ServiceManager.getTaskService().getRolesSize(s) == 0) {
                    Helper.setErrorMessage("Kein Benutzer festgelegt für: ", s.getTitle());
                }
            }
            return "";
        }

        clearValues();
        LegacyPrefsHelper myPrefs = ServiceManager.getRulesetService().getPreferences(this.template.getRuleset());
        try {
            this.myRdf = new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) myPrefs).getRuleset());
            this.myRdf.read(this.metadataFile.getPath());
        } catch (ReadException e) {
            logger.error(e.getMessage(), e);
        }
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setProject(this.project);
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.digitalCollections = new ArrayList<>();

        BeanHelper.copyTasks(this.template, this.prozessKopie);

        initializePossibleDigitalCollections();

        return this.naviFirstPage;
    }

    /**
     * OpacAnfrage.
     */
    @Override
    public void evaluateOpac() {
        clearValues();
        readProjectConfigs();
        try {
            LegacyPrefsHelper myPrefs = ServiceManager.getRulesetService().getPreferences(this.template.getRuleset());
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) myPrefs).getRuleset());
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

                    LegacyDocStructHelperInterface myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
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
                            LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(
                                ServiceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()),
                                field.getMetadata());
                            LegacyMetadataHelper md = UghHelper.getMetadata(myTempStruct, mdt);
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
     * Auswahl des Prozesses auswerten.
     */
    @Override
    public String templateAuswahlAuswerten() {
        if (ServiceManager.getProcessService().getWorkpiecesSize(this.processForChoice) > 0) {
            for (Property workpieceProperty : this.processForChoice.getWorkpieces()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitle().equals(workpieceProperty.getTitle())) {
                        field.setValue(workpieceProperty.getValue());
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

        try {
            this.myRdf = ServiceManager.getProcessService().readMetadataAsTemplateFile(this.processForChoice);
        } catch (ReadException | PreferencesException | IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"Template-Metadaten" }, logger, e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            LegacyDocStructHelperInterface colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
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
     * Test title.
     *
     * @return boolean
     */
    public boolean testTitle() {
        boolean valid = true;

        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.MASS_IMPORT_UNIQUE_TITLE)) {
            valid = isProcessTitleCorrect(this.prozessKopie);
        }
        return valid;
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

        addProperties(io);
        updateTasks(this.prozessKopie);

        if (!io.getBatches().isEmpty()) {
            this.prozessKopie.getBatches().addAll(io.getBatches());
        }
        try {
            ServiceManager.getProcessService().save(this.prozessKopie);
            ServiceManager.getProcessService().refresh(this.prozessKopie);
        } catch (DataException e) {
            logger.error("errorSaving", new Object[] {Helper.getTranslation("process") }, e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage
         * stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        ServiceManager.getFileService().writeMetadataFile(this.myRdf, this.prozessKopie);
        ServiceManager.getProcessService().readMetadataFile(this.prozessKopie);

        /* damit die Sortierung stimmt nochmal einlesen */
        ServiceManager.getProcessService().refresh(this.prozessKopie);
        return this.prozessKopie;
    }

    @Override
    public void createNewFileformat() {

        LegacyPrefsHelper myPrefs = ServiceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset());

        FileformatInterface ff;
        try {
            ff = new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) myPrefs).getRuleset());
            ff.read(this.metadataFile.getPath());
        } catch (ReadException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addProperties(ImportObject io) {
        if (io == null) {
            addAdditionalFields(this.additionalFields, this.prozessKopie);

            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
                this.tifHeaderImageDescription);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);
        } else {
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
            BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
                this.tifHeaderImageDescription);
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
    public Process getProcessForChoice() {
        return this.processForChoice;
    }

    @Override
    public void setProcessForChoice(Process processForChoice) {
        this.processForChoice = processForChoice;
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

    @Override
    public List<String> getAllOpacCatalogues() {
        return ConfigOpac.getAllCatalogueTitles();
    }

    @Override
    public List<ConfigOpacDoctype> getAllDoctypes() {
        return ConfigOpac.getAllDoctypes();
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
        // possible replacements
        this.tifDefinition = this.tifDefinition.replaceAll("\\[\\[", "<");
        this.tifDefinition = this.tifDefinition.replaceAll("\\]\\]", ">");

        /*
         * Documentname ist im allgemeinen = Prozesstitel
         */
        this.tifHeaderDocumentName = this.prozessKopie.getTitle();
        StringBuilder tifHeaderImageDescriptionBuilder = new StringBuilder();

        // image description
        StringTokenizer tokenizer = new StringTokenizer(this.tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String string = tokenizer.nextToken();
            // if the string begins and ends with ', then take over the content
            if (string.startsWith("'") && string.endsWith("'") && string.length() > 2) {
                tifHeaderImageDescriptionBuilder.append(string, 1, string.length() - 1);
            } else if (string.equals("$Doctype")) {
                tifHeaderImageDescriptionBuilder.append(this.docType);
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
                        tifHeaderImageDescriptionBuilder.append(calcProcessTitleCheck(additionalField.getTitle(), additionalField.getValue()));
                    }
                }
            }
        }
        this.tifHeaderImageDescription = tifHeaderImageDescriptionBuilder.toString();
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
