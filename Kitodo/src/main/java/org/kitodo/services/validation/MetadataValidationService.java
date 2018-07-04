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

package org.kitodo.services.validation;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.api.validation.metadata.MetadataValidationInterface;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Process;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;

public class MetadataValidationService {

    private List<DocStructInterface> docStructsOhneSeiten;
    private Process process;
    private boolean autoSave = false;
    private static final Logger logger = LogManager.getLogger(MetadataValidationService.class);
    private static final String VALIDATE_METADATA = "validate.metadata";
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Validate.
     *
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(Process process) {
        PrefsInterface prefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        FileformatInterface gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(process);
        } catch (PreferencesException | IOException | ReadException | RuntimeException e) {
            Helper.setErrorMessage("metadataReadError", new Object[] {process.getTitle() }, logger, e);
            return false;
        }
        return validate(gdzfile, prefs, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param prefs
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(FileformatInterface gdzfile, PrefsInterface prefs, Process process) {
        String metadataLanguage = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        this.process = process;
        boolean result = true;

        DigitalDocumentInterface dd;
        try {
            dd = gdzfile.getDigitalDocument();
        } catch (PreferencesException | RuntimeException e) {
            Helper.setErrorMessage(Helper.getTranslation("metadataDigitalDocumentError") + process.getTitle(), logger,
                e);
            return false;
        }

        DocStructInterface logical = dd.getLogicalDocStruct();
        List<MetadataInterface> allIdentifierMetadata = logical.getAllIdentifierMetadata();
        if (Objects.nonNull(allIdentifierMetadata) && !allIdentifierMetadata.isEmpty()) {
            MetadataInterface identifierTopStruct = allIdentifierMetadata.get(0);

            if (isMetadataValueReplaced(logical, identifierTopStruct, metadataLanguage)) {
                result = false;
            }

            DocStructInterface firstChild = logical.getAllChildren().get(0);
            List<MetadataInterface> allChildIdentifierMetadata = firstChild.getAllIdentifierMetadata();
            if (Objects.nonNull(allChildIdentifierMetadata) && !allChildIdentifierMetadata.isEmpty()) {
                MetadataInterface identifierFirstChild = allChildIdentifierMetadata.get(0);
                if (identifierTopStruct.getValue() != null && !identifierTopStruct.getValue().isEmpty()
                        && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
                    Object[] parameter = new Object[] {identifierTopStruct.getMetadataType().getName(),
                                                       logical.getDocStructType().getName(),
                                                       firstChild.getDocStructType().getName() };
                    Helper.setErrorMessage("invalidIdentifierSame", parameter);
                    result = false;
                }

                if (isMetadataValueReplaced(firstChild, identifierFirstChild, metadataLanguage)) {
                    result = false;
                }
            } else {
                logger.info("no firstChild or no identifier");
            }
        } else {
            Helper.setErrorMessage("metadataMissingIdentifier");
            result = false;
        }

        if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), prefs)) {
            result = false;
        }

        /*
         * auf Docstructs ohne Seiten prüfen
         */
        DocStructInterface logicalTop = dd.getLogicalDocStruct();
        this.docStructsOhneSeiten = new ArrayList<>();
        if (logicalTop == null) {
            Helper.setErrorMessage(process.getTitle(), "metadataPaginationError");
            result = false;
        } else {
            checkDocStructsOhneSeiten(logicalTop);
        }

        if (!this.docStructsOhneSeiten.isEmpty()) {
            for (DocStructInterface docStructWithoutPages : this.docStructsOhneSeiten) {
                Helper.setErrorMessage(process.getTitle(), Helper.getTranslation("metadataPaginationStructure")
                        + docStructWithoutPages.getDocStructType().getNameByLanguage(metadataLanguage));
            }
            result = false;
        }

        /*
         * uf Seiten ohne Docstructs prüfen
         */
        List<String> seitenOhneDocstructs = null;
        try {
            seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
        } catch (PreferencesException e) {
            Helper.setErrorMessage("[" + process.getTitle() + "] Can not check pages without docstructs: ", logger, e);
            result = false;
        }
        if (isStringListIncorrect(seitenOhneDocstructs, "metadataPaginationPages")) {
            result = false;
        }

        /*
         * auf mandatory Values der Metadaten prüfen
         */
        List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<>(),
            metadataLanguage);
        if (isStringListIncorrect(mandatoryList, "metadataMandatoryElement")) {
            result = false;
        }

        /*
         * auf Details in den Metadaten prüfen, die in der Konfiguration
         * angegeben wurden
         */
        List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<>(),
            prefs, metadataLanguage);
        if (isStringListIncorrect(configuredList, "metadataInvalidData")) {
            result = false;
        }

        MetadatenImagesHelper mih = new MetadatenImagesHelper(prefs, dd);
        try {
            if (!mih.checkIfImagesValid(process.getTitle(),
                serviceManager.getProcessService().getImagesTifDirectory(true, process))) {
                result = false;
            }
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage(process.getTitle() + ": ", logger, e);
            result = false;
        }

        try {
            List<URI> images = mih.getDataFiles(this.process);
            int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
            int sizeOfImages = images.size();
            if (sizeOfPagination != sizeOfImages) {
                Helper.setErrorMessage("imagePaginationError", new Object[] {sizeOfPagination, sizeOfImages });
                return false;
            }
        } catch (InvalidImagesException e) {
            Helper.setErrorMessage(process.getTitle() + ": ", logger, e);
            result = false;
        }

        saveMetadataFile(gdzfile, process);

        return result;
    }

    private boolean isStringListIncorrect(List<String> strings, String messageTitle) {
        boolean incorrect = false;
        if (Objects.nonNull(strings)) {
            for (String string : strings) {
                Helper.setErrorMessage(process.getTitle() + ": " + Helper.getTranslation(messageTitle), string);
            }
            incorrect = true;
        }
        return incorrect;
    }

    private boolean isMetadataValueReplaced(DocStructInterface docStruct, MetadataInterface metadata,
            String metadataLanguage) {

        if (!metadata.getValue().replaceAll(ConfigCore.getParameter(Parameters.VALIDATE_IDENTIFIER_REGEX,
            Parameters.DefaultValues.VALIDATE_IDENTIFIER_REGEX), "").equals("")) {
            Object[] parameters = new Object[] {metadata.getMetadataType().getNameByLanguage(metadataLanguage),
                                                docStruct.getDocStructType().getNameByLanguage(metadataLanguage) };
            Helper.setErrorMessage("invalidIdentifierCharacter", parameters);
            return true;
        }
        return false;
    }

    private boolean isValidPathImageFiles(DocStructInterface phys, PrefsInterface myPrefs) {
        try {
            MetadataTypeInterface mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
            List<? extends MetadataInterface> allMetadata = phys.getAllMetadataByType(mdt);
            if (Objects.nonNull(allMetadata) && !allMetadata.isEmpty()) {
                return true;
            } else {
                Helper.setErrorMessage(this.process.getTitle(), "Can not verify, image path is not set");
                return false;
            }
        } catch (UghHelperException e) {
            Helper.setErrorMessage(this.process.getTitle() + ": " + "Verify aborted, error: ", e.getMessage(), logger,
                e);
            return false;
        }
    }

    private void checkDocStructsOhneSeiten(DocStructInterface docStruct) {
        if (docStruct.getAllToReferences().isEmpty() && docStruct.getDocStructType().getAnchorClass() == null) {
            this.docStructsOhneSeiten.add(docStruct);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStruct.getAllChildren() != null) {
            for (DocStructInterface child : docStruct.getAllChildren()) {
                checkDocStructsOhneSeiten(child);
            }
        }
    }

    private List<String> checkSeitenOhneDocstructs(FileformatInterface inRdf) throws PreferencesException {
        List<String> result = new ArrayList<>();
        DocStructInterface boundBook = inRdf.getDigitalDocument().getPhysicalDocStruct();
        // if boundBook is null
        if (boundBook == null || boundBook.getAllChildren() == null) {
            return result;
        }

        /* alle Seiten durchlaufen und prüfen ob References existieren */
        for (DocStructInterface docStruct : boundBook.getAllChildren()) {
            List<ReferenceInterface> refs = docStruct.getAllFromReferences();
            if (refs.isEmpty()) {
                result.add(collectLogicalAndPhysicalStructure(docStruct));
            }
        }
        return result;
    }

    private String collectLogicalAndPhysicalStructure(DocStructInterface docStruct) {
        String physical = "";
        String logical = "";

        for (MetadataInterface metadata : docStruct.getAllMetadata()) {
            if (metadata.getMetadataType().getName().equals("logicalPageNumber")) {
                logical = " (" + metadata.getValue() + ")";
            }
            if (metadata.getMetadataType().getName().equals("physPageNumber")) {
                physical = metadata.getValue();
            }
        }

        return physical + logical;
    }

    private List<String> checkMandatoryValues(DocStructInterface docStruct, ArrayList<String> list, String language) {
        DocStructTypeInterface dst = docStruct.getDocStructType();
        List<MetadataTypeInterface> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataTypeInterface mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends MetadataInterface> ll = docStruct.getAllMetadataByType(mdt);
            int real = ll.size();
            // if (ll.size() > 0) {

            if ((number.equals("1m") || number.equals("+")) && real == 1
                    && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {

                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("metadataIsEmpty"));
            }
            // check types
            if (number.equals("1m") && real != 1) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("metadataNotOneElement") + " " + real
                        + Helper.getTranslation("metadataTimes"));
            }
            if (number.equals("1o") && real > 1) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("metadataToManyElements") + " " + real + " "
                        + Helper.getTranslation("metadataTimes"));
            }
            if (number.equals("+") && real == 0) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("metadataNotEnoughElements"));
            }
        }
        // }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStruct.getAllChildren() != null) {
            for (DocStructInterface child : docStruct.getAllChildren()) {
                checkMandatoryValues(child, list, language);
            }
        }
        return list;
    }

    /**
     * individuelle konfigurierbare projektspezifische Validierung der
     * Metadaten.
     */
    private List<String> checkConfiguredValidationValues(DocStructInterface docStruct, ArrayList<String> errorList,
            PrefsInterface prefs, String language) {
        // open configuration and read the validation details
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.process.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(process.getTitle(), logger, e);
            return errorList;
        }
        int count = cp.getParamList(VALIDATE_METADATA).size();
        for (int i = 0; i < count; i++) {
            // evaluate attributes
            String propMetadatatype = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@metadata]");
            String propDoctype = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@docstruct]");
            String propStartswith = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@startswith]");
            String propEndswith = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@endswith]");
            String propCreateElementFrom = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@createelementfrom]");
            MetadataTypeInterface mdt = null;
            try {
                mdt = UghHelper.getMetadataType(prefs, propMetadatatype);
            } catch (UghHelperException e) {
                Helper.setErrorMessage("[" + this.process.getTitle() + "] " + "Metadatatype does not exist: ",
                    propMetadatatype, logger, e);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann
             * dieses jetzt (sofern vorhanden) übernehmen
             */
            if (propDoctype != null && propDoctype.equals("firstchild")) {
                if (Objects.nonNull(docStruct.getAllChildren()) && !docStruct.getAllChildren().isEmpty()) {
                    docStruct = docStruct.getAllChildren().get(0);
                } else {
                    continue;
                }
            }

            /*
             * wenn der MetadatenTyp existiert, dann jetzt die nötige Aktion
             * überprüfen
             */
            if (mdt != null) {
                /* ein CreatorsAllOrigin soll erzeugt werden */
                if (propCreateElementFrom != null) {
                    List<MetadataTypeInterface> listOfFromMdts = prepareMetadataTypes(prefs, propCreateElementFrom);
                    if (!listOfFromMdts.isEmpty()) {
                        checkCreateElementFrom(listOfFromMdts, docStruct, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(errorList, propStartswith, propEndswith, docStruct, mdt, language);
                }
            }
        }
        return errorList;
    }

    private List<MetadataTypeInterface> prepareMetadataTypes(PrefsInterface prefs, String propCreateElementFrom) {
        List<MetadataTypeInterface> metadataTypes = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(propCreateElementFrom, "|");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            try {
                MetadataTypeInterface emdete = UghHelper.getMetadataType(prefs, token);
                metadataTypes.add(emdete);
            } catch (UghHelperException e) {
                /*
                 * if the compilation does not exist for creatorsAllOrigin as
                 * the metadata type, fetch exception and do not elaborate on it
                 */
            }
        }
        return metadataTypes;
    }

    private void saveMetadataFile(FileformatInterface gdzfile, Process process) {
        try {
            if (this.autoSave) {
                serviceManager.getFileService().writeMetadataFile(gdzfile, process);
            }
        } catch (PreferencesException | WriteException | IOException | RuntimeException e) {
            Helper.setErrorMessage("Error while writing metadata: ", process.getTitle(), logger, e);
        }
    }

    /**
     * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum
     * erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
     * vorhanden.
     */
    private void checkCreateElementFrom(List<MetadataTypeInterface> metadataTypes, DocStructInterface docStruct,
            MetadataTypeInterface mdt, String language) {
        /*
         * existiert das zu erzeugende Metadatum schon, dann überspringen,
         * ansonsten alle Daten zusammensammeln und in das neue Element
         * schreiben
         */
        List<? extends MetadataInterface> createMetadata = docStruct.getAllMetadataByType(mdt);
        if (createMetadata == null || createMetadata.isEmpty()) {
            try {
                MetadataInterface createdElement = UghImplementation.INSTANCE.createMetadata(mdt);
                String value = "";
                // go through all the metadata to append and append to the
                // element
                for (MetadataTypeInterface metadataType : metadataTypes) {
                    List<PersonInterface> fromElemente = docStruct.getAllPersons();
                    if (Objects.nonNull(fromElemente) && !fromElemente.isEmpty()) {
                        value = iterateOverPersons(fromElemente, docStruct, language, metadataType);
                    }
                }

                if (!value.isEmpty()) {
                    createdElement.setStringValue(value);
                    docStruct.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // go through all children
        List<DocStructInterface> children = docStruct.getAllChildren();
        if (Objects.nonNull(children)) {
            for (DocStructInterface child : children) {
                checkCreateElementFrom(metadataTypes, child, mdt, language);
            }
        }
    }

    private String iterateOverPersons(List<PersonInterface> persons, DocStructInterface docStruct, String language,
            MetadataTypeInterface metadataType) {
        StringBuilder value = new StringBuilder();
        for (PersonInterface p : persons) {
            if (p.getRole() == null) {
                Helper.setErrorMessage(
                    "[" + this.process.getTitle() + " " + docStruct.getDocStructType().getNameByLanguage(language)
                            + "] " + Helper.getTranslation("metadataPersonWithoutRole"));
                break;
            } else {
                if (p.getRole().equals(metadataType.getName())) {
                    if (value.length() > 0) {
                        value.append("; ");
                    }
                    value.append(p.getLastName());
                    value.append(", ");
                    value.append(p.getFirstName());
                }
            }
        }
        return value.toString();
    }

    /**
     * Metadata should start or end with a certain string.
     *
     * @param errorList
     *            list of errors
     * @param propStartsWith
     *            check if starts with this String
     * @param propEndsWith
     *            check if ends with this String
     * @param myStruct
     *            DocStruct
     * @param mdt
     *            MetadataType
     * @param language
     *            as String
     */
    private void checkStartsEndsWith(List<String> errorList, String propStartsWith, String propEndsWith,
            DocStructInterface myStruct, MetadataTypeInterface mdt, String language) {
        // starts with or ends with
        List<? extends MetadataInterface> allMetadataByType = myStruct.getAllMetadataByType(mdt);
        if (Objects.nonNull(allMetadataByType)) {
            for (MetadataInterface md : allMetadataByType) {
                /* prüfen, ob es mit korrekten Werten beginnt */
                if (propStartsWith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propStartsWith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().startsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        errorList.add(md.getMetadataType().getNameByLanguage(language) + " "
                                + Helper.getTranslation("metadataWithValue") + " " + md.getValue() + " "
                                + Helper.getTranslation("metadataDoesNotStartWith") + " " + propStartsWith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setStringValue(new StringTokenizer(propStartsWith, "|").nextToken() + md.getValue());
                    }
                }
                /* prüfen, ob es mit korrekten Werten endet */
                if (propEndsWith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propEndsWith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().endsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        errorList.add(md.getMetadataType().getNameByLanguage(language) + " "
                                + Helper.getTranslation("metadataWithValue") + " " + md.getValue() + " "
                                + Helper.getTranslation("metadataDoesNotEndWith") + " " + propEndsWith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setStringValue(md.getValue() + new StringTokenizer(propEndsWith, "|").nextToken());
                    }
                }
            }
        }
    }

    /**
     * Check if automatic save is allowed.
     *
     * @return true or false
     */
    public boolean isAutoSave() {
        return this.autoSave;
    }

    /**
     * Set if automatic save is allowed.
     *
     * @param autoSave
     *            true or false
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    private MetadataValidationInterface getValidationModule() {
        KitodoServiceLoader<MetadataValidationInterface> loader = new KitodoServiceLoader<>(
                MetadataValidationInterface.class);
        return loader.loadModule();
    }
}
