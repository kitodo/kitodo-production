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
import org.kitodo.api.validation.metadata.MetadataValidationInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;

public class MetadataValidationService {

    List<DocStruct> docStructsOhneSeiten;
    Process myProcess;
    boolean autoSave = false;
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
        Prefs myPrefs = serviceManager.getRulesetService().getPreferences(process.getRuleset());
        /*
         * Fileformat einlesen
         */
        Fileformat gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(process);
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataReadError") + process.getTitle(), e.getMessage());
            return false;
        }
        return validate(gdzfile, myPrefs, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param inPrefs
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(Fileformat gdzfile, Prefs inPrefs, Process process) {
        String metadataLanguage = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}");
        this.myProcess = process;
        boolean ergebnis = true;

        DigitalDocument dd = null;
        try {
            dd = gdzfile.getDigitalDocument();
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataDigitalDocumentError") + process.getTitle(),
                    e.getMessage());
            return false;
        }

        DocStruct logical = dd.getLogicalDocStruct();
        if (logical.getAllIdentifierMetadata() != null && logical.getAllIdentifierMetadata().size() > 0) {
            Metadata identifierTopStruct = logical.getAllIdentifierMetadata().get(0);
            try {
                if (!identifierTopStruct.getValue()
                        .replaceAll(ConfigCore.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
                    List<String> parameter = new ArrayList<>();
                    parameter.add(identifierTopStruct.getType().getNameByLanguage(metadataLanguage));
                    parameter.add(logical.getType().getNameByLanguage(metadataLanguage));

                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierCharacter", parameter));

                    ergebnis = false;
                }
                DocStruct firstChild = logical.getAllChildren().get(0);
                Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
                if (identifierTopStruct.getValue() != null && !identifierTopStruct.getValue().isEmpty()
                        && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
                    List<String> parameter = new ArrayList<>();
                    parameter.add(identifierTopStruct.getType().getName());
                    parameter.add(logical.getType().getName());
                    parameter.add(firstChild.getType().getName());
                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierSame", parameter));
                    ergebnis = false;
                }
                if (!identifierFirstChild.getValue()
                        .replaceAll(ConfigCore.getParameter("validateIdentifierRegex", "[\\w|-]"), "").equals("")) {
                    List<String> parameter = new ArrayList<>();
                    parameter.add(identifierFirstChild.getType().getNameByLanguage(metadataLanguage));
                    parameter.add(firstChild.getType().getNameByLanguage(metadataLanguage));
                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierCharacter", parameter));
                    ergebnis = false;
                }
            } catch (Exception e) {
                // no firstChild or no identifier
            }
        } else {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataMissingIdentifier"));
            ergebnis = false;
        }
        /*
         * PathImagesFiles prüfen
         */
        if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), inPrefs)) {
            ergebnis = false;
        }

        /*
         * auf Docstructs ohne Seiten prüfen
         */
        DocStruct logicalTop = dd.getLogicalDocStruct();
        this.docStructsOhneSeiten = new ArrayList<>();
        if (logicalTop == null) {
            Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationError"));
            ergebnis = false;
        } else {
            this.checkDocStructsOhneSeiten(logicalTop);
        }

        if (this.docStructsOhneSeiten.size() != 0) {
            for (DocStruct docStructWithoutPages : this.docStructsOhneSeiten) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationStructure")
                        + docStructWithoutPages.getType().getNameByLanguage(metadataLanguage));
            }
            ergebnis = false;
        }

        /*
         * uf Seiten ohne Docstructs prüfen
         */
        List<String> seitenOhneDocstructs = null;
        try {
            seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
        } catch (PreferencesException e1) {
            Helper.setFehlerMeldung("[" + process.getTitle() + "] Can not check pages without docstructs: ");
            ergebnis = false;
        }
        if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
            for (String pageWithoutDocStruct : seitenOhneDocstructs) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataPaginationPages"),
                        pageWithoutDocStruct);
            }
            ergebnis = false;
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
            ergebnis = false;
        }

        /*
         * auf Details in den Metadaten prüfen, die in der Konfiguration
         * angegeben wurden
         */
        List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<>(),
                inPrefs, metadataLanguage);
        if (configuredList.size() != 0) {
            for (String configured : configuredList) {
                Helper.setFehlerMeldung(process.getTitle() + ": " + Helper.getTranslation("MetadataInvalidData"), configured);
            }
            ergebnis = false;
        }

        MetadatenImagesHelper mih = new MetadatenImagesHelper(inPrefs, dd);
        try {
            if (!mih.checkIfImagesValid(process.getTitle(),
                    serviceManager.getProcessService().getImagesTifDirectory(true, process))) {
                ergebnis = false;
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(process.getTitle() + ": ", e);
            ergebnis = false;
        }

        try {
            List<URI> images = mih.getDataFiles(myProcess);
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
            ergebnis = false;
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
        return ergebnis;
    }

    private boolean isValidPathImageFiles(DocStruct phys, Prefs myPrefs) {
        try {
            MetadataType mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
            List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(mdt);
            if (alleMetadaten != null && alleMetadaten.size() > 0) {
                return true;
            } else {
                Helper.setFehlerMeldung(this.myProcess.getTitle() + ": " + "Can not verify, image path is not set", "");
                return false;
            }
        } catch (UghHelperException e) {
            Helper.setFehlerMeldung(this.myProcess.getTitle() + ": " + "Verify aborted, error: ", e.getMessage());
            return false;
        }
    }

    private void checkDocStructsOhneSeiten(DocStruct inStruct) {
        if (inStruct.getAllToReferences().size() == 0 && inStruct.getType().getAnchorClass() == null) {
            this.docStructsOhneSeiten.add(inStruct);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                checkDocStructsOhneSeiten(child);
            }
        }
    }

    private List<String> checkSeitenOhneDocstructs(Fileformat inRdf) throws PreferencesException {
        List<String> rueckgabe = new ArrayList<>();
        DocStruct boundbook = inRdf.getDigitalDocument().getPhysicalDocStruct();
        /* wenn boundbook null ist */
        if (boundbook == null || boundbook.getAllChildren() == null) {
            return rueckgabe;
        }

        /* alle Seiten durchlaufen und prüfen ob References existieren */
        for (DocStruct docStruct : boundbook.getAllChildren()) {
            List<Reference> refs = docStruct.getAllFromReferences();
            String physical = "";
            String logical = "";
            if (refs.size() == 0) {

                for (Metadata metadata : docStruct.getAllMetadata()) {
                    if (metadata.getType().getName().equals("logicalPageNumber")) {
                        logical = " (" + metadata.getValue() + ")";
                    }
                    if (metadata.getType().getName().equals("physPageNumber")) {
                        physical = metadata.getValue();
                    }
                }
                rueckgabe.add(physical + logical);
            }
        }
        return rueckgabe;
    }

    private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList, String language) {
        DocStructType dst = inStruct.getType();
        List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataType mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends Metadata> ll = inStruct.getAllMetadataByType(mdt);
            int real = 0;
            // if (ll.size() > 0) {
            real = ll.size();

            if ((number.equals("1m") || number.equals("+")) && real == 1
                    && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {

                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataIsEmpty"));
            }
            /* jetzt die Typen prüfen */
            if (number.equals("1m") && real != 1) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotOneElement") + " " + real
                        + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("1o") && real > 1) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataToManyElements") + " " + real + " "
                        + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("+") && real == 0) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotEnoughElements"));
            }
        }
        // }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                checkMandatoryValues(child, inList, language);
            }
        }
        return inList;
    }

    /**
     * individuelle konfigurierbare projektspezifische Validierung der
     * Metadaten.
     */
    private List<String> checkConfiguredValidationValues(DocStruct inStruct, ArrayList<String> inFehlerList,
                                                         Prefs inPrefs, String language) {
        /*
         * Konfiguration öffnen und die Validierungsdetails auslesen
         */
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.myProcess.getProject().getTitle());
        } catch (IOException e) {
            Helper.setFehlerMeldung("[" + this.myProcess.getTitle() + "] " + "IOException", e.getMessage());
            return inFehlerList;
        }
        int count = cp.getParamList("validate.metadata").size();
        for (int i = 0; i < count; i++) {

            /* Attribute auswerten */
            String propMetadatatype = cp.getParamString("validate.metadata(" + i + ")[@metadata]");
            String propDoctype = cp.getParamString("validate.metadata(" + i + ")[@docstruct]");
            String propStartswith = cp.getParamString("validate.metadata(" + i + ")[@startswith]");
            String propEndswith = cp.getParamString("validate.metadata(" + i + ")[@endswith]");
            String propCreateElementFrom = cp.getParamString("validate.metadata(" + i + ")[@createelementfrom]");
            DocStruct myStruct = inStruct;
            MetadataType mdt = null;
            try {
                mdt = UghHelper.getMetadataType(inPrefs, propMetadatatype);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("[" + this.myProcess.getTitle() + "] " + "Metadatatype does not exist: ",
                        propMetadatatype);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann
             * dieses jetzt (sofern vorhanden) übernehmen
             */
            if (propDoctype != null && propDoctype.equals("firstchild")) {
                if (myStruct.getAllChildren() != null && myStruct.getAllChildren().size() > 0) {
                    myStruct = myStruct.getAllChildren().get(0);
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
                    ArrayList<MetadataType> listOfFromMdts = new ArrayList<>();
                    StringTokenizer tokenizer = new StringTokenizer(propCreateElementFrom, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        try {
                            MetadataType emdete = UghHelper.getMetadataType(inPrefs, tok);
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
                        checkCreateElementFrom(inFehlerList, listOfFromMdts, myStruct, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(inFehlerList, propStartswith, propEndswith, myStruct, mdt, language);
                }
            }
        }
        return inFehlerList;
    }

    /**
     * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum
     * erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
     * vorhanden.
     */
    private void checkCreateElementFrom(ArrayList<String> inFehlerList, ArrayList<MetadataType> inListOfFromMdts,
                                        DocStruct myStruct, MetadataType mdt, String language) {

        /*
         * existiert das zu erzeugende Metadatum schon, dann überspringen,
         * ansonsten alle Daten zusammensammeln und in das neue Element
         * schreiben
         */
        List<? extends Metadata> createMetadaten = myStruct.getAllMetadataByType(mdt);
        if (createMetadaten == null || createMetadaten.size() == 0) {
            try {
                Metadata createdElement = new Metadata(mdt);
                StringBuffer myValue = new StringBuffer();
                /*
                 * alle anzufügenden Metadaten durchlaufen und an das Element
                 * anhängen
                 */
                for (MetadataType mdttemp : inListOfFromMdts) {

                    List<Person> fromElemente = myStruct.getAllPersons();
                    if (fromElemente != null && fromElemente.size() > 0) {
                        /*
                         * wenn Personen vorhanden sind (z.B. Illustrator), dann
                         * diese durchlaufen
                         */
                        for (Person p : fromElemente) {
                            if (p.getRole() == null) {
                                Helper.setFehlerMeldung("[" + this.myProcess.getTitle() + " "
                                        + myStruct.getType().getNameByLanguage(language) + "] "
                                        + Helper.getTranslation("MetadataPersonWithoutRole"));
                                break;
                            } else {
                                if (p.getRole().equals(mdttemp.getName())) {
                                    if (myValue.length() > 0) {
                                        myValue.append("; ");
                                    }
                                    myValue.append(p.getLastname());
                                    myValue.append(", ");
                                    myValue.append(p.getFirstname());
                                }
                            }
                        }
                    }
                }

                if (myValue.length() > 0) {
                    createdElement.setValue(myValue.toString());

                    myStruct.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                logger.error(e);
            }

        }

        /*
         * alle Kinder durchlaufen
         */
        List<DocStruct> children = myStruct.getAllChildren();
        if (children != null && children.size() > 0) {
            for (DocStruct aChildren : children) {
                checkCreateElementFrom(inFehlerList, inListOfFromMdts, aChildren, mdt, language);
            }
        }
    }

    /**
     * Metadatum soll mit bestimmten String beginnen oder enden.
     */
    private void checkStartsEndsWith(List<String> inFehlerList, String propStartswith, String propEndswith,
                                     DocStruct myStruct, MetadataType mdt, String language) {
        /* startswith oder endswith */
        List<? extends Metadata> alleMetadaten = myStruct.getAllMetadataByType(mdt);
        if (alleMetadaten != null && alleMetadaten.size() > 0) {
            for (Metadata md : alleMetadaten) {
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
     * automatisch speichern lassen, wenn Änderungen nötig waren.
     */
    public boolean isAutoSave() {
        return this.autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    private MetadataValidationInterface getValidationModule() {
        KitodoServiceLoader<MetadataValidationInterface> loader = new KitodoServiceLoader<>(
                MetadataValidationInterface.class, ConfigCore.getParameter("moduleFolder"));
        return loader.loadModule();
    }
}
