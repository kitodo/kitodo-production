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
import org.kitodo.api.ugh.UghImplementation;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.validation.metadata.MetadataValidationInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;

public class MetadataValidationService {

    private List<DocStructInterface> docStructsOhneSeiten;
    private Process process;
    private boolean autoSave = false;
    private static final Logger logger = LogManager.getLogger(MetadataValidationService.class);
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Validate.
     *
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(Process process) {
        PrefsInterface prefsInterface = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        /*
         * Fileformat einlesen
         */
        FileformatInterface gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(process);
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataReadError") + process.getTitle(), e.getMessage());
            return false;
        }
        return validate(gdzfile, prefsInterface, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param prefsInterface
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(FileformatInterface gdzfile, PrefsInterface prefsInterface, Process process) {
        String metadataLanguage = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}");
        this.process = process;
        boolean result = true;

        DigitalDocumentInterface dd;
        try {
            dd = gdzfile.getDigitalDocument();
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataDigitalDocumentError") + process.getTitle(),
                e.getMessage());
            return false;
        }

        DocStructInterface logical = dd.getLogicalDocStruct();
        List<MetadataInterface> allIdentifierMetadata = logical.getAllIdentifierMetadata();
        if (allIdentifierMetadata != null && allIdentifierMetadata.size() > 0) {
            MetadataInterface identifierTopStruct = allIdentifierMetadata.get(0);

            result = checkIfMetadataValueNotReplaced(logical, identifierTopStruct, metadataLanguage);

            DocStructInterface firstChild = logical.getAllChildren().get(0);
            List<MetadataInterface> allChildIdentifierMetadata = firstChild.getAllIdentifierMetadata();
            if (allChildIdentifierMetadata != null && allChildIdentifierMetadata.size() > 0) {
                MetadataInterface identifierFirstChild = allChildIdentifierMetadata.get(0);
                if (identifierTopStruct.getValue() != null && !identifierTopStruct.getValue().isEmpty()
                        && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
                    List<String> parameter = new ArrayList<>();
                    parameter.add(identifierTopStruct.getType().getName());
                    parameter.add(logical.getType().getName());
                    parameter.add(firstChild.getType().getName());
                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierSame", parameter));
                    result = false;
                }

                result = checkIfMetadataValueNotReplaced(firstChild, identifierFirstChild, metadataLanguage);
            } else {
                logger.info("no firstChild or no identifier");
            }
        } else {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataMissingIdentifier"));
            result = false;
        }
        /*
         * PathImagesFiles prüfen
         */
        if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), prefsInterface)) {
            result = false;
        }

        /*
         * auf Docstructs ohne Seiten prüfen
         */
        DocStructInterface logicalTop = dd.getLogicalDocStruct();
        this.docStructsOhneSeiten = new ArrayList<>();
        if (logicalTop == null) {
            Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationError"));
            result = false;
        } else {
            this.checkDocStructsOhneSeiten(logicalTop);
        }

        if (this.docStructsOhneSeiten.size() != 0) {
            for (DocStructInterface docStructWithoutPages : this.docStructsOhneSeiten) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationStructure")
                        + docStructWithoutPages.getType().getNameByLanguage(metadataLanguage));
            }
            result = false;
        }

        /*
         * uf Seiten ohne Docstructs prüfen
         */
        List<String> seitenOhneDocstructs = null;
        try {
            seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
        } catch (PreferencesException e1) {
            Helper.setFehlerMeldung("[" + process.getTitle() + "] Can not check pages without docstructs: ");
            result = false;
        }
        if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
            for (String pageWithoutDocStruct : seitenOhneDocstructs) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationPages"),
                    pageWithoutDocStruct);
            }
            result = false;
        }

        /*
         * auf mandatory Values der Metadaten prüfen
         */
        List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<>(),
            metadataLanguage);
        if (mandatoryList.size() != 0) {
            for (String mandatory : mandatoryList) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataMandatoryElement"),
                    mandatory);
            }
            result = false;
        }

        /*
         * auf Details in den Metadaten prüfen, die in der Konfiguration
         * angegeben wurden
         */
        List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<>(),
            prefsInterface, metadataLanguage);
        if (configuredList.size() != 0) {
            for (String configured : configuredList) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataInvalidData"),
                    configured);
            }
            result = false;
        }

        MetadatenImagesHelper mih = new MetadatenImagesHelper(prefsInterface, dd);
        try {
            if (!mih.checkIfImagesValid(process.getTitle(),
                serviceManager.getProcessService().getImagesTifDirectory(true, process))) {
                result = false;
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(process.getTitle() + ": ", e);
            result = false;
        }

        try {
            List<URI> images = mih.getDataFiles(this.process);
            int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
            int sizeOfImages = images.size();
            if (sizeOfPagination != sizeOfImages) {
                List<String> param = new ArrayList<>();
                param.add(String.valueOf(sizeOfPagination));
                param.add(String.valueOf(sizeOfImages));
                Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
                return false;
            }
        } catch (InvalidImagesException e1) {
            Helper.setFehlerMeldung(process.getTitle() + ": ", e1);
            result = false;
        }

        /*
         * Metadaten ggf. zum Schluss speichern
         */
        try {
            if (this.autoSave) {
                serviceManager.getFileService().writeMetadataFile(gdzfile, process);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error while writing metadata: " + process.getTitle(), e);
        }
        return result;
    }

    private boolean checkIfMetadataValueNotReplaced(DocStructInterface docStructInterface, MetadataInterface metadataInterface, String metadataLanguage) {
        if (!metadataInterface.getValue().replaceAll(ConfigCore.getParameter("validateIdentifierRegex", "[\\w|-]"), "")
                .equals("")) {
            List<String> parameter = new ArrayList<>();
            parameter.add(metadataInterface.getType().getNameByLanguage(metadataLanguage));
            parameter.add(docStructInterface.getType().getNameByLanguage(metadataLanguage));
            Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierCharacter", parameter));
            return false;
        }
        return true;
    }

    private boolean isValidPathImageFiles(DocStructInterface phys, PrefsInterface myPrefs) {
        try {
            MetadataTypeInterface mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
            List<? extends MetadataInterface> allMetadata = phys.getAllMetadataByType(mdt);
            if (allMetadata != null && allMetadata.size() > 0) {
                return true;
            } else {
                Helper.setFehlerMeldung(this.process.getTitle() + ": " + "Can not verify, image path is not set", "");
                return false;
            }
        } catch (UghHelperException e) {
            Helper.setFehlerMeldung(this.process.getTitle() + ": " + "Verify aborted, error: ", e.getMessage());
            return false;
        }
    }

    private void checkDocStructsOhneSeiten(DocStructInterface docStructInterface) {
        if (docStructInterface.getAllToReferences().size() == 0 && docStructInterface.getType().getAnchorClass() == null) {
            this.docStructsOhneSeiten.add(docStructInterface);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStructInterface.getAllChildren() != null) {
            for (DocStructInterface child : docStructInterface.getAllChildren()) {
                checkDocStructsOhneSeiten(child);
            }
        }
    }

    private List<String> checkSeitenOhneDocstructs(FileformatInterface inRdf) throws PreferencesException {
        List<String> result = new ArrayList<>();
        DocStructInterface boundBook = inRdf.getDigitalDocument().getPhysicalDocStruct();
        /* wenn boundBook null ist */
        if (boundBook == null || boundBook.getAllChildren() == null) {
            return result;
        }

        /* alle Seiten durchlaufen und prüfen ob References existieren */
        for (DocStructInterface docStructInterface : boundBook.getAllChildren()) {
            List<ReferenceInterface> refs = docStructInterface.getAllFromReferences();
            String physical = "";
            String logical = "";
            if (refs.size() == 0) {

                for (MetadataInterface metadataInterface : docStructInterface.getAllMetadata()) {
                    if (metadataInterface.getType().getName().equals("logicalPageNumber")) {
                        logical = " (" + metadataInterface.getValue() + ")";
                    }
                    if (metadataInterface.getType().getName().equals("physPageNumber")) {
                        physical = metadataInterface.getValue();
                    }
                }
                result.add(physical + logical);
            }
        }
        return result;
    }

    private List<String> checkMandatoryValues(DocStructInterface docStructInterface, ArrayList<String> list, String language) {
        DocStructTypeInterface dst = docStructInterface.getType();
        List<MetadataTypeInterface> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataTypeInterface mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends MetadataInterface> ll = docStructInterface.getAllMetadataByType(mdt);
            int real = ll.size();
            // if (ll.size() > 0) {

            if ((number.equals("1m") || number.equals("+")) && real == 1
                    && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {

                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataIsEmpty"));
            }
            /* jetzt die Typen prüfen */
            if (number.equals("1m") && real != 1) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotOneElement") + " " + real
                        + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("1o") && real > 1) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataToManyElements") + " " + real + " "
                        + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("+") && real == 0) {
                list.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotEnoughElements"));
            }
        }
        // }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStructInterface.getAllChildren() != null) {
            for (DocStructInterface child : docStructInterface.getAllChildren()) {
                checkMandatoryValues(child, list, language);
            }
        }
        return list;
    }

    /**
     * individuelle konfigurierbare projektspezifische Validierung der
     * Metadaten.
     */
    private List<String> checkConfiguredValidationValues(DocStructInterface docStructInterface, ArrayList<String> errorList, PrefsInterface prefsInterface,
            String language) {
        /*
         * Konfiguration öffnen und die Validierungsdetails auslesen
         */
        ConfigProjects cp;
        try {
            cp = new ConfigProjects(this.process.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("[" + this.process.getTitle() + "] " + "IOException", e.getMessage());
            return errorList;
        }
        int count = cp.getParamList("validate.metadata").size();
        for (int i = 0; i < count; i++) {

            /* Attribute auswerten */
            String propMetadatatype = cp.getParamString("validate.metadata(" + i + ")[@metadata]");
            String propDoctype = cp.getParamString("validate.metadata(" + i + ")[@docstruct]");
            String propStartswith = cp.getParamString("validate.metadata(" + i + ")[@startswith]");
            String propEndswith = cp.getParamString("validate.metadata(" + i + ")[@endswith]");
            String propCreateElementFrom = cp.getParamString("validate.metadata(" + i + ")[@createelementfrom]");
            MetadataTypeInterface mdt = null;
            try {
                mdt = UghHelper.getMetadataType(prefsInterface, propMetadatatype);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("[" + this.process.getTitle() + "] " + "Metadatatype does not exist: ",
                    propMetadatatype);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann
             * dieses jetzt (sofern vorhanden) übernehmen
             */
            if (propDoctype != null && propDoctype.equals("firstchild")) {
                if (docStructInterface.getAllChildren() != null && docStructInterface.getAllChildren().size() > 0) {
                    docStructInterface = docStructInterface.getAllChildren().get(0);
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
                    ArrayList<MetadataTypeInterface> listOfFromMdts = new ArrayList<>();
                    StringTokenizer tokenizer = new StringTokenizer(propCreateElementFrom, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        try {
                            MetadataTypeInterface emdete = UghHelper.getMetadataType(prefsInterface, tok);
                            listOfFromMdts.add(emdete);
                        } catch (UghHelperException e) {
                            /*
                             * wenn die zusammenzustellenden Personen für
                             * CreatorsAllOrigin als Metadatatyp nicht
                             * existieren, Exception abfangen und nicht weiter
                             * drauf eingehen
                             */
                        }
                    }
                    if (listOfFromMdts.size() > 0) {
                        checkCreateElementFrom(listOfFromMdts, docStructInterface, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(errorList, propStartswith, propEndswith, docStructInterface, mdt, language);
                }
            }
        }
        return errorList;
    }

    /**
     * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum
     * erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
     * vorhanden.
     */
    private void checkCreateElementFrom(ArrayList<MetadataTypeInterface> metadataTypeInterfaces, DocStructInterface docStructInterface, MetadataTypeInterface mdt,
            String language) {

        /*
         * existiert das zu erzeugende Metadatum schon, dann überspringen,
         * ansonsten alle Daten zusammensammeln und in das neue Element
         * schreiben
         */
        List<? extends MetadataInterface> createMetadaten = docStructInterface.getAllMetadataByType(mdt);
        if (createMetadaten == null || createMetadaten.size() == 0) {
            try {
                MetadataInterface createdElement = UghImplementation.INSTANCE.createMetadata(mdt);
                StringBuilder value = new StringBuilder();
                /*
                 * alle anzufügenden Metadaten durchlaufen und an das Element
                 * anhängen
                 */
                for (MetadataTypeInterface mdttemp : metadataTypeInterfaces) {

                    List<PersonInterface> fromElemente = docStructInterface.getAllPersons();
                    if (fromElemente != null && fromElemente.size() > 0) {
                        /*
                         * wenn Personen vorhanden sind (z.B. Illustrator), dann
                         * diese durchlaufen
                         */
                        for (PersonInterface p : fromElemente) {
                            if (p.getRole() == null) {
                                Helper.setFehlerMeldung("[" + this.process.getTitle() + " "
                                        + docStructInterface.getType().getNameByLanguage(language) + "] "
                                        + Helper.getTranslation("MetadataPersonWithoutRole"));
                                break;
                            } else {
                                if (p.getRole().equals(mdttemp.getName())) {
                                    if (value.length() > 0) {
                                        value.append("; ");
                                    }
                                    value.append(p.getLastname());
                                    value.append(", ");
                                    value.append(p.getFirstname());
                                }
                            }
                        }
                    }
                }

                if (value.length() > 0) {
                    createdElement.setValue(value.toString());
                    docStructInterface.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                logger.error(e);
            }
        }

        /*
         * alle Kinder durchlaufen
         */
        List<DocStructInterface> children = docStructInterface.getAllChildren();
        if (children != null && children.size() > 0) {
            for (DocStructInterface child : children) {
                checkCreateElementFrom(metadataTypeInterfaces, child, mdt, language);
            }
        }
    }

    /**
     * Metadatum soll mit bestimmten String beginnen oder enden.
     */
    private void checkStartsEndsWith(List<String> inFehlerList, String propStartswith, String propEndswith,
            DocStructInterface myStruct, MetadataTypeInterface mdt, String language) {
        /* startswith oder endswith */
        List<? extends MetadataInterface> alleMetadaten = myStruct.getAllMetadataByType(mdt);
        if (alleMetadaten != null && alleMetadaten.size() > 0) {
            for (MetadataInterface md : alleMetadaten) {
                /* prüfen, ob es mit korrekten Werten beginnt */
                if (propStartswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propStartswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().startsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        inFehlerList.add(md.getType().getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataWithValue") + " " + md.getValue() + " "
                                + Helper.getTranslation("MetadataDoesNotStartWith") + " " + propStartswith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(new StringTokenizer(propStartswith, "|").nextToken() + md.getValue());
                    }
                }
                /* prüfen, ob es mit korrekten Werten endet */
                if (propEndswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propEndswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().endsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        inFehlerList.add(md.getType().getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataWithValue") + " " + md.getValue() + " "
                                + Helper.getTranslation("MetadataDoesNotEndWith") + " " + propEndswith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(md.getValue() + new StringTokenizer(propEndswith, "|").nextToken());
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
                MetadataValidationInterface.class, ConfigCore.getParameter("moduleFolder"));
        return loader.loadModule();
    }
}
