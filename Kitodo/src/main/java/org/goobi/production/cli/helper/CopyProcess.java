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
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.ProzesskopieForm;
import org.kitodo.production.helper.BeanHelper;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyLogicalDocStructHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.process.ProcessValidator;
import org.kitodo.production.process.field.AdditionalField;
import org.kitodo.production.services.ServiceManager;

public class CopyProcess extends ProzesskopieForm {

    private static final Logger logger = LogManager.getLogger(CopyProcess.class);
    private transient LegacyMetsModsDigitalDocumentHelper myRdf;
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
        readPreferences();
        this.prozessKopie = new Process();
        this.prozessKopie.setTitle("");
        this.prozessKopie.setProject(this.project);
        this.prozessKopie.setRuleset(this.template.getRuleset());
        this.prozessKopie.setDocket(this.template.getDocket());
        this.digitalCollections = new ArrayList<>();

        ProcessGenerator.copyTasks(this.template, this.prozessKopie);

        return this.naviFirstPage;
    }

    @Override
    public String prepare(int templateId, int projectId) {
        ProcessGenerator processGenerator = new ProcessGenerator();
        try {
            boolean generated = processGenerator.generateProcess(templateId, projectId);

            if (generated) {
                this.prozessKopie = processGenerator.getGeneratedProcess();
                this.project = processGenerator.getProject();
                this.template = processGenerator.getTemplate();

                clearValues();
                readPreferences();
                this.digitalCollections = new ArrayList<>();
                initializePossibleDigitalCollections();

                return this.naviFirstPage;
            }
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getMessage(), logger, e);
        }
        return null;
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
            this.myRdf = new LegacyMetsModsDigitalDocumentHelper(myPrefs.getRuleset());
            this.myRdf.read(this.metadataFile.getPath());
            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getDocStructType().getName();

            fillFieldsFromMetadataFile(this.myRdf);
            fillFieldsFromConfig();
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"Opac-Ergebnisses" }, logger, e);
        }
    }

    private void readPreferences() {
        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(this.template.getRuleset());
        try {
            this.myRdf = new LegacyMetsModsDigitalDocumentHelper(prefs.getRuleset());
            this.myRdf.read(this.metadataFile.getPath());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei
     * füllen.
     */
    private void fillFieldsFromMetadataFile(LegacyMetsModsDigitalDocumentHelper myRdf) {
        if (Objects.nonNull(myRdf)) {
            for (AdditionalField field : this.additionalFields) {
                if (field.isUghBinding() && field.showDependingOnDoctype()) {
                    LegacyDocStructHelperInterface myTempStruct = getDocstructForMetadataFile(myRdf, field);
                    try {
                        setMetadataForMetadataFile(field, myTempStruct);
                    } catch (IllegalArgumentException e) {
                        Helper.setErrorMessage(e.getMessage(), logger, e);
                    }
                }
            }
        }
    }

    private LegacyDocStructHelperInterface getDocstructForMetadataFile(LegacyMetsModsDigitalDocumentHelper myRdf,
            AdditionalField field) {
        LegacyDocStructHelperInterface myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
        if (field.getDocStruct().equals("firstchild")) {
            try {
                myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (field.getDocStruct().equals("boundbook")) {
            myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
        }
        return myTempStruct;
    }

    private void setMetadataForMetadataFile(AdditionalField field, LegacyDocStructHelperInterface myTempStruct) {
        if (field.getMetadata().equals("ListOfCreators")) {
            throw new UnsupportedOperationException("Dead code pending removal");
        } else {
            /* evaluate the content in normal fields */
            LegacyMetadataTypeHelper mdt = LegacyPrefsHelper.getMetadataType(
                ServiceManager.getRulesetService().getPreferences(this.prozessKopie.getRuleset()), field.getMetadata());
            LegacyMetadataHelper md = LegacyLogicalDocStructHelper.getMetadata(myTempStruct, mdt);
            if (Objects.nonNull(md)) {
                field.setValue(md.getValue());
            }
        }
    }

    private void fillFieldsFromConfig() {
        for (AdditionalField field : this.additionalFields) {
            if (!field.isUghBinding() && field.showDependingOnDoctype() && Objects.nonNull(field.getSelectList())
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
    public String evaluateTemplateSelection() {
        readTemplateSelection();

        try {
            this.myRdf = ServiceManager.getProcessService().readMetadataAsTemplateFile(this.processForChoice);
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {"Template-Metadaten" }, logger, e);
        }

        removeCollectionsForChildren(this.myRdf, this.prozessKopie);

        return "";
    }

    @Override
    protected void readTemplateSelection() {
        readTemplateWorkpieces(this.additionalFields, this.processForChoice);
        readTemplateTemplates(this.additionalFields, this.processForChoice);
    }

    /**
     * Test title correction.
     *
     * @return true if title is correct, false otherwise
     */
    public boolean testTitle() {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.MASS_IMPORT_UNIQUE_TITLE)) {
            return ProcessValidator.isProcessTitleCorrect(this.prozessKopie.getTitle());
        }
        return true;
    }

    /**
     * Create Process.
     *
     * @param io
     *            import object
     * @return Process object
     */
    public Process createProcess(ImportObject io) throws IOException {
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
        if (Objects.isNull(this.myRdf)) {
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

        LegacyMetsModsDigitalDocumentHelper ff;
        try {
            ff = new LegacyMetsModsDigitalDocumentHelper(myPrefs.getRuleset());
            ff.read(this.metadataFile.getPath());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addProperties(ImportObject io) {
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "DocType", this.docType);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderImagedescription",
                this.tifHeaderImageDescription);
        BeanHelper.addPropertyForWorkpiece(this.prozessKopie, "TifHeaderDocumentname", this.tifHeaderDocumentName);

        if (Objects.isNull(io)) {
            addAdditionalFields(this.additionalFields, this.prozessKopie);
        } else {
            for (Property processProperty : io.getProcessProperties()) {
                addPropertyForProcess(this.prozessKopie, processProperty);
            }
            for (Property workpieceProperty : io.getWorkProperties()) {
                addPropertyForWorkpiece(this.prozessKopie, workpieceProperty);
            }

            for (Property templateProperty : io.getTemplateProperties()) {
                addPropertyForTemplate(this.prozessKopie, templateProperty);
            }
            BeanHelper.addPropertyForProcess(this.prozessKopie, "Template", this.template.getTitle());
            BeanHelper.addPropertyForProcess(this.prozessKopie, "TemplateID", String.valueOf(this.template.getId()));
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
        return getPossibleDigitalCollections().size() == 1;

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
                appendDataFromAdditionalFields(token, newTitle);
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
            if (Objects.nonNull(result) && result.length() < 4) {
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
            String token = tokenizer.nextToken();
            // if the string begins and ends with ', then take over the content
            if (token.startsWith("'") && token.endsWith("'") && token.length() > 2) {
                tifHeaderImageDescriptionBuilder.append(token, 1, token.length() - 1);
            } else if (token.equals("$Doctype")) {
                tifHeaderImageDescriptionBuilder.append(this.docType);
            } else {
                appendDataFromAdditionalFields(token, tifHeaderImageDescriptionBuilder);
            }
        }
        this.tifHeaderImageDescription = tifHeaderImageDescriptionBuilder.toString();
    }

    /**
     * Evaluate the token as field name.
     * 
     * @param token
     *            as String
     * @param stringBuilder
     *            as StringBuilder
     */
    private void appendDataFromAdditionalFields(String token, StringBuilder stringBuilder) {
        for (AdditionalField additionalField : this.additionalFields) {
            /*
             * if it is the ATS or TSL field, then use the calculated atstsl if it does not
             * already exist
             */
            String title = additionalField.getTitle();
            String value = additionalField.getValue();
            if ((title.equals("ATS") || title.equals("TSL")) && additionalField.showDependingOnDoctype()
                    && (Objects.isNull(value) || value.isEmpty())) {
                additionalField.setValue(CopyProcess.atstsl);
            }

            // add the content to the title
            if (title.equals(token) && additionalField.showDependingOnDoctype() && Objects.nonNull(value)) {
                stringBuilder.append(calcProcessTitleCheck(title, value));
            }
        }
    }

    private void addPropertyForTemplate(Process template, Property property) {
        if (ProcessValidator.existsProperty(template.getTemplates(), property)) {
            return;
        }

        Property templateProperty = insertDataToProperty(property);
        templateProperty.getTemplates().add(template);
        List<Property> properties = template.getTemplates();
        properties.add(templateProperty);
    }

    private void addPropertyForProcess(Process process, Property property) {
        if (ProcessValidator.existsProperty(process.getProperties(), property)) {
            return;
        }

        Property processProperty = insertDataToProperty(property);
        processProperty.getProcesses().add(process);
        List<Property> properties = process.getProperties();
        properties.add(processProperty);
    }

    private void addPropertyForWorkpiece(Process workpiece, Property property) {
        if (ProcessValidator.existsProperty(workpiece.getWorkpieces(), property)) {
            return;
        }

        Property workpieceProperty = insertDataToProperty(property);
        workpieceProperty.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getWorkpieces();
        properties.add(workpieceProperty);
    }

    private Property insertDataToProperty(Property property) {
        Property newProperty = new Property();
        newProperty.setTitle(property.getTitle());
        newProperty.setValue(property.getValue());
        newProperty.setChoice(property.getChoice());
        newProperty.setDataType(property.getDataType());
        return newProperty;
    }

    public void setMetadataFile(URI mdFile) {
        this.metadataFile = mdFile;
    }

    public URI getMetadataFile() {
        return this.metadataFile;
    }
}
