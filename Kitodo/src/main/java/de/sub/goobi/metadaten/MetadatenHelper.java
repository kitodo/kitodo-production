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

package de.sub.goobi.metadaten;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;

public class MetadatenHelper implements Comparator<Object> {
    private static final Logger logger = LogManager.getLogger(MetadatenHelper.class);
    private static final int PAGENUMBER_FIRST = 0;
    private static final int PAGENUMBER_LAST = 1;
    private static final String METADATA_NOT_ALLOWED = "metadataNotAllowed";
    private static ServiceManager serviceManager = new ServiceManager();
    private PrefsInterface prefs;
    private DigitalDocumentInterface digitalDocument;

    public MetadatenHelper(PrefsInterface inPrefs, DigitalDocumentInterface inDocument) {
        this.prefs = inPrefs;
        this.digitalDocument = inDocument;
    }

    /**
     * Getter for final value PAGENUMBER_FIRST.
     *
     * @return PAGENUMBER_FIRST
     */
    public static int getPageNumberFirst() {
        return PAGENUMBER_FIRST;
    }

    /**
     * Getter for final value PAGENUMBER_LAST.
     *
     * @return PAGENUMBER_LAST
     */
    public static int getPageNumberLast() {
        return PAGENUMBER_LAST;
    }

    /**
     * Change current document structure.
     * {@code inOldDocstruct.getType().getName()} shall become
     * {@code inNewType}.
     *
     * @param inOldDocstruct
     *            DocStruct object
     * @param inNewType
     *            String
     * @return DocStruct object
     */
    public DocStructInterface changeCurrentDocstructType(DocStructInterface inOldDocstruct, String inNewType)
            throws MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

        DocStructTypeInterface dst = this.prefs.getDocStrctTypeByName(inNewType);
        DocStructInterface newDocstruct = this.digitalDocument.createDocStruct(dst);

        // add all metadata
        if (Objects.nonNull(inOldDocstruct.getAllMetadata())) {
            for (MetadataInterface old : inOldDocstruct.getAllMetadata()) {
                if (Objects.nonNull(newDocstruct.getPossibleMetadataTypes())
                        && !newDocstruct.getPossibleMetadataTypes().isEmpty()) {
                    boolean match = isFoundMatchForMetadata(newDocstruct, old);
                    if (!match) {
                        try {
                            newDocstruct.addMetadata(old);
                        } catch (RuntimeException e) {
                            Helper.setErrorMessage(METADATA_NOT_ALLOWED,
                                new Object[] {Helper.getTranslation("metadata"), old.getMetadataType().getName(),
                                              newDocstruct.getDocStructType().getName() },
                                logger, e);
                            return inOldDocstruct;
                        }
                    } else {
                        newDocstruct.addMetadata(old);
                    }
                } else {
                    Helper.setFehlerMeldung(
                        Helper.getTranslation(METADATA_NOT_ALLOWED, Arrays.asList(Helper.getTranslation("metadata"),
                            old.getMetadataType().getName(), newDocstruct.getDocStructType().getName())));
                    return inOldDocstruct;
                }
            }
        }

        // add all persons
        if (Objects.nonNull(inOldDocstruct.getAllPersons())) {
            for (PersonInterface old : inOldDocstruct.getAllPersons()) {
                if (Objects.nonNull(newDocstruct.getPossibleMetadataTypes())
                        && !newDocstruct.getPossibleMetadataTypes().isEmpty()) {
                    boolean match = isFoundMatchForMetadata(newDocstruct, old);
                    if (!match) {
                        Helper.setFehlerMeldung(
                                Helper.getTranslation(METADATA_NOT_ALLOWED, Arrays.asList(Helper.getTranslation("person"),
                                        old.getMetadataType().getName(), newDocstruct.getDocStructType().getName())));
                    } else {
                        newDocstruct.addPerson(old);
                    }
                } else {
                    Helper.setFehlerMeldung(
                            Helper.getTranslation(METADATA_NOT_ALLOWED, Arrays.asList(Helper.getTranslation("person"),
                                    old.getMetadataType().getName(), newDocstruct.getDocStructType().getName())));
                    return inOldDocstruct;
                }
            }
        }

        // add all pages
        if (inOldDocstruct.getAllToReferences() != null) {
            for (ReferenceInterface reference : inOldDocstruct.getAllToReferences()) {
                newDocstruct.addReferenceTo(reference.getTarget(), reference.getType());
            }
        }

        // add all Docstruct children
        if (Objects.nonNull(inOldDocstruct.getAllChildren())) {
            for (DocStructInterface old : inOldDocstruct.getAllChildren()) {
                if (Objects.nonNull(newDocstruct.getDocStructType().getAllAllowedDocStructTypes())
                        && !newDocstruct.getDocStructType().getAllAllowedDocStructTypes().isEmpty()) {

                    if (!newDocstruct.getDocStructType().getAllAllowedDocStructTypes()
                            .contains(old.getDocStructType().getName())) {
                        Helper.setFehlerMeldung(
                                Helper.getTranslation(METADATA_NOT_ALLOWED, Arrays.asList(Helper.getTranslation("childElement"),
                                        old.getDocStructType().getName(), newDocstruct.getDocStructType().getName())));
                        return inOldDocstruct;
                    } else {
                        newDocstruct.addChild(old);
                    }
                } else {
                    Helper.setFehlerMeldung(
                            Helper.getTranslation(METADATA_NOT_ALLOWED, Arrays.asList(Helper.getTranslation("childElement"),
                                    old.getDocStructType().getName(), newDocstruct.getDocStructType().getName())));
                    return inOldDocstruct;
                }
            }
        }
        /*
         * neues Docstruct zum Parent hinzufügen und an die gleiche Stelle
         * schieben, wie den Vorg?nger
         */
        inOldDocstruct.getParent().addChild(newDocstruct);
        int i = 1;
        List<DocStructInterface> children = newDocstruct.getParent().getAllChildren();
        for (DocStructInterface child : children) {
            if (child == inOldDocstruct) {
                break;
            }
        }
        for (int j = newDocstruct.getParent().getAllChildren().size() - i; j > 0; j--) {
            moveNodeUp(newDocstruct);
        }

        /*
         * altes Docstruct vom Parent entfernen und neues als aktuelles nehmen
         */
        inOldDocstruct.getParent().removeChild(inOldDocstruct);
        return newDocstruct;
    }

    private boolean isFoundMatchForMetadata(DocStructInterface newDocStruct, MetadataInterface old) {
        for (MetadataTypeInterface metadataType : newDocStruct.getPossibleMetadataTypes()) {
            if (metadataType.getName().equals(old.getMetadataType().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Move around the document structure tree.
     *
     * @param inStruct
     *            DocStruct object
     */
    public void moveNodeUp(DocStructInterface inStruct) throws TypeNotAllowedAsChildException {
        DocStructInterface parent = inStruct.getParent();
        if (parent == null) {
            return;
        }
        List<DocStructInterface> alleDS = null;

        /* das erste Element kann man nicht nach oben schieben */
        if (parent.getAllChildren().get(0) == inStruct) {
            return;
        }

        /* alle Elemente des Parents durchlaufen */
        for (DocStructInterface tempDS : parent.getAllChildren()) {
            /*
             * wenn das folgende Element das zu verschiebende ist dabei die
             * Exception auffangen, falls es kein nächstes Kind gibt
             */
            try {
                if (parent.getNextChild(tempDS) == inStruct) {
                    alleDS = new ArrayList<>();
                }
            } catch (IndexOutOfBoundsException e) {
                logger.error(e.getMessage(), e);
            }

            /*
             * nachdem der Vorg?nger gefunden wurde, werden alle anderen
             * Elemente aus der Child-Liste entfernt und separat gesammelt
             */
            if (alleDS != null && tempDS != inStruct) {
                alleDS.add(tempDS);
            }
        }

        if (alleDS != null) {
            /* anschliessend die Childs entfernen */
            for (DocStructInterface child : alleDS) {
                parent.removeChild(child);
            }

            /* anschliessend die Childliste korrigieren */
            // parent.addChild(myStrukturelement);
            for (DocStructInterface child : alleDS) {
                parent.addChild(child);
            }
        }
    }

    /**
     * Move around the document structure tree.
     *
     * @param inStruct
     *            DocStruct object
     */
    public void moveNodeDown(DocStructInterface inStruct) throws TypeNotAllowedAsChildException {
        DocStructInterface parent = inStruct.getParent();
        if (parent == null) {
            return;
        }
        List<DocStructInterface> alleDS = new ArrayList<>();

        /* alle Elemente des Parents durchlaufen */
        for (Iterator<DocStructInterface> iter = parent.getAllChildren().iterator(); iter.hasNext();) {
            DocStructInterface tempDS = iter.next();

            /* wenn das aktuelle Element das zu verschiebende ist */
            if (tempDS != inStruct) {
                alleDS.add(tempDS);
            } else {
                if (iter.hasNext()) {
                    alleDS.add(iter.next());
                }
                alleDS.add(inStruct);
            }
        }

        /* anschliessend alle Children entfernen */
        for (DocStructInterface child : alleDS) {
            parent.removeChild(child);
        }

        /* anschliessend die neue Childliste anlegen */
        for (DocStructInterface child : alleDS) {
            parent.addChild(child);
        }
    }

    /**
     * die MetadatenTypen zurückgeben.
     */
    public SelectItem[] getAddableDocStructTypen(DocStructInterface inStruct, boolean checkTypesFromParent) {
        /*
         * zuerst mal die addierbaren Metadatentypen ermitteln
         */
        List<String> types;
        SelectItem[] myTypes = new SelectItem[0];

        try {
            if (!checkTypesFromParent) {
                types = inStruct.getDocStructType().getAllAllowedDocStructTypes();
            } else {
                types = inStruct.getParent().getDocStructType().getAllAllowedDocStructTypes();
            }
        } catch (RuntimeException e) {
            return myTypes;
        }

        if (types == null) {
            return myTypes;
        }

        List<DocStructTypeInterface> newTypes = new ArrayList<>();
        for (String tempTitel : types) {
            DocStructTypeInterface dst = this.prefs.getDocStrctTypeByName(tempTitel);
            if (dst != null) {
                newTypes.add(dst);
            } else {
                Helper.setMeldung(null, "Regelsatz-Fehler: ", " DocstructType " + tempTitel + " nicht definiert");
                logger.error(
                    "getAddableDocStructTypen() - Regelsatz-Fehler: DocstructType " + tempTitel + " nicht definiert");
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortType("DocStructTypen");
        // TODO: Uses generics, if possible
        Collections.sort(newTypes, c);

        // nun ein Array mit der richtigen Größe anlegen
        int zaehler = newTypes.size();
        myTypes = new SelectItem[zaehler];

        // und anschliessend alle Elemente in das Array packen
        zaehler = 0;
        String language = serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        for (DocStructTypeInterface docStructType : newTypes) {
            String label = docStructType.getNameByLanguage(language);
            if (label == null) {
                label = docStructType.getName();
            }
            myTypes[zaehler] = new SelectItem(docStructType.getName(), label);
            zaehler++;
        }
        return myTypes;
    }

    /**
     * alle unbenutzen Metadaten des Docstruct löschen, Unterelemente rekursiv
     * aufrufen.
     */
    public void deleteAllUnusedElements(DocStructInterface inStruct) {
        inStruct.deleteUnusedPersonsAndMetadata();
        if (Objects.nonNull(inStruct.getAllChildren())) {
            for (DocStructInterface child : inStruct.getAllChildren()) {
                deleteAllUnusedElements(child);
            }
        }
    }

    /**
     * die erste Imagenummer zurückgeben.
     */
    // FIXME: alphanumerisch
    public String getImageNumber(DocStructInterface inStrukturelement, int inPageNumber) {
        String rueckgabe = "";

        if (inStrukturelement == null) {
            return "";
        }
        List<ReferenceInterface> listReferenzen = inStrukturelement.getAllReferences("to");
        if (Objects.nonNull(listReferenzen) && !listReferenzen.isEmpty()) {
            Collections.sort(listReferenzen, new Comparator<ReferenceInterface>() {
                @Override
                public int compare(final ReferenceInterface firstObject, final ReferenceInterface secondObject) {
                    Integer firstPage = 0;
                    Integer secondPage = 0;
                    final MetadataTypeInterface mdt = MetadatenHelper.this.prefs
                            .getMetadataTypeByName("physPageNumber");
                    List<? extends MetadataInterface> listMetadaten = firstObject.getTarget().getAllMetadataByType(mdt);
                    if (Objects.nonNull(listMetadaten) && !listMetadaten.isEmpty()) {
                        final MetadataInterface meineSeite = listMetadaten.get(0);
                        firstPage = Integer.parseInt(meineSeite.getValue());
                    }
                    listMetadaten = secondObject.getTarget().getAllMetadataByType(mdt);
                    if (Objects.nonNull(listMetadaten) && !listMetadaten.isEmpty()) {
                        final MetadataInterface meineSeite = listMetadaten.get(0);
                        secondPage = Integer.parseInt(meineSeite.getValue());
                    }
                    return firstPage.compareTo(secondPage);
                }
            });

            MetadataTypeInterface mdt = this.prefs.getMetadataTypeByName("physPageNumber");
            List<? extends MetadataInterface> listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (Objects.nonNull(listSeiten) && !listSeiten.isEmpty()) {
                MetadataInterface meineSeite = listSeiten.get(0);
                rueckgabe += meineSeite.getValue();
            }
            mdt = this.prefs.getMetadataTypeByName("logicalPageNumber");
            listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (Objects.nonNull(listSeiten) && !listSeiten.isEmpty()) {
                MetadataInterface meineSeite = listSeiten.get(0);
                rueckgabe += ":" + meineSeite.getValue();
            }
        }
        return rueckgabe;
    }

    /**
     * vom übergebenen DocStruct alle Metadaten ermitteln und um die fehlenden
     * DefaultDisplay-Metadaten ergänzen.
     */
    @SuppressWarnings("deprecation")
    public List<? extends MetadataInterface> getMetadataInclDefaultDisplay(DocStructInterface inStruct,
            String inLanguage, boolean inIsPerson, Process inProzess) {
        List<MetadataTypeInterface> displayMetadataTypes = inStruct.getDisplayMetadataTypes();
        /* sofern Default-Metadaten vorhanden sind, diese ggf. ergänzen */
        if (displayMetadataTypes != null) {
            for (MetadataTypeInterface mdt : displayMetadataTypes) {
                // check, if mdt is already in the allMDs Metadata list, if not
                // - add it
                if (!(inStruct.getAllMetadataByType(mdt) != null && !inStruct.getAllMetadataByType(mdt).isEmpty())) {
                    try {
                        if (mdt.isPerson()) {
                            PersonInterface p = UghImplementation.INSTANCE.createPerson(mdt);
                            p.setRole(mdt.getName());
                            inStruct.addPerson(p);
                        } else {
                            MetadataInterface md = UghImplementation.INSTANCE.createMetadata(mdt);
                            inStruct.addMetadata(md); // add this new metadata
                            // element
                        }
                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        /*
         * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch
         * sortieren
         */
        if (inIsPerson) {
            List<PersonInterface> person = inStruct.getAllPersons();
            if (person != null && !inProzess.getRuleset().isOrderMetadataByRuleset()) {
                Collections.sort(person, new MetadataComparator(inLanguage));
            }
            return person;
        } else {
            List<MetadataInterface> metadata = inStruct.getAllMetadata();
            if (metadata != null && !inProzess.getRuleset().isOrderMetadataByRuleset()) {
                Collections.sort(metadata, new MetadataComparator(inLanguage));
            }
            return getAllVisibleMetadataHack(inStruct);

        }
    }

    /** TODO: Replace it, after Maven is kicked :). */
    private List<MetadataInterface> getAllVisibleMetadataHack(DocStructInterface inStruct) {

        // Start with the list of all metadata.
        List<MetadataInterface> result = new LinkedList<>();

        // Iterate over all metadata.
        if (inStruct.getAllMetadata() != null) {
            for (MetadataInterface md : inStruct.getAllMetadata()) {
                // If the metadata has some value and it does not start with the
                // HIDDEN_METADATA_CHAR, add it to the result list.
                if (!md.getMetadataType().getName().startsWith("_")) {
                    result.add(md);
                }
            }
        }
        if (result.isEmpty()) {
            result = null;
        }
        return result;
    }

    /**
     * prüfen, ob es sich hier um eine rdf- oder um eine mets-Datei handelt.
     */
    public static String getMetaFileType(URI file) throws IOException {
        /*
         * Typen und Suchbegriffe festlegen
         */
        HashMap<String, String> types = new HashMap<>();
        types.put("metsmods", "ugh.fileformats.mets.MetsModsImportExport".toLowerCase());
        types.put("mets", "www.loc.gov/METS/".toLowerCase());
        types.put("rdf", "<RDF:RDF ".toLowerCase());
        types.put("xstream", "<ugh.dl.DigitalDocument>".toLowerCase());

        try (InputStreamReader input = new InputStreamReader(serviceManager.getFileService().read((file)),
                StandardCharsets.UTF_8); BufferedReader bufRead = new BufferedReader(input)) {
            char[] buffer = new char[200];
            while (bufRead.read(buffer) >= 0) {
                String temp = new String(buffer).toLowerCase();
                for (Entry<String, String> entry : types.entrySet()) {
                    if (temp.contains(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }
        return "-";
    }

    /**
     * Get Metadata type language.
     *
     * @param inMdt
     *            MetadataType object
     * @return localized Title of metadata type
     */
    public String getMetadatatypeLanguage(MetadataTypeInterface inMdt) {
        String label = inMdt.getLanguage(serviceManager.getUserService().getAuthenticatedUser().getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
        }
        return label;
    }

    /**
     * Comparator für die Metadaten.
     */
    // TODO: Uses generics, if possible
    public static class MetadataComparator implements Comparator<Object> {
        private String language;

        public MetadataComparator(String inLanguage) {
            this.language = inLanguage;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public int compare(Object firstObject, Object secondObject) {
            MetadataInterface firstMetadata = (MetadataInterface) firstObject;
            MetadataInterface secondMetadata = (MetadataInterface) secondObject;
            if (firstMetadata == null) {
                return -1;
            }
            if (secondMetadata == null) {
                return 1;
            }
            String firstName;
            String secondName;
            try {
                MetadataTypeInterface firstMetadataType = firstMetadata.getMetadataType();
                MetadataTypeInterface secondMetadataType = secondMetadata.getMetadataType();
                firstName = firstMetadataType.getNameByLanguage(this.language);
                secondName = secondMetadataType.getNameByLanguage(this.language);
            } catch (java.lang.NullPointerException e) {
                logger.debug("Language {} for metadata {} or {} is missing in ruleset", this.language,
                    firstMetadata.getMetadataType(), secondMetadata.getMetadataType());
                return 0;
            }
            if (firstName == null || firstName.length() == 0) {
                firstName = firstMetadata.getMetadataType().getName();
                if (firstName == null) {
                    return -1;
                }
            }
            if (secondName == null || secondName.length() == 0) {
                secondName = secondMetadata.getMetadataType().getName();
                if (secondName == null) {
                    return 1;
                }
            }

            return firstName.compareToIgnoreCase(secondName);
        }
    }

    /**
     * Alle Rollen ermitteln, die für das übergebene Strukturelement erlaubt
     * sind.
     *
     * @param myDocStruct
     *            DocStruct object
     * @param inRoleName
     *            der aktuellen Person, damit diese ggf. in die Liste mit
     *            übernommen wird
     */
    public List<SelectItem> getAddablePersonRoles(DocStructInterface myDocStruct, String inRoleName) {
        ArrayList<SelectItem> myList = new ArrayList<>();
        /*
         * zuerst mal alle addierbaren Metadatentypen ermitteln
         */
        List<MetadataTypeInterface> types = myDocStruct.getPossibleMetadataTypes();
        if (types == null) {
            types = new ArrayList<>();
        }
        if (inRoleName != null && inRoleName.length() > 0) {
            boolean addRole = true;
            for (MetadataTypeInterface mdt : types) {
                if (mdt.getName().equals(inRoleName)) {
                    addRole = false;
                }
            }

            if (addRole) {
                types.add(this.prefs.getMetadataTypeByName(inRoleName));
            }
        }
        /*
         * alle Metadatentypen, die keine Person sind, oder mit einem
         * Unterstrich anfangen rausnehmen
         */
        for (MetadataTypeInterface mdt : new ArrayList<>(types)) {
            if (!mdt.isPerson()) {
                types.remove(mdt);
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortType("MetadatenTypen");
        Collections.sort(types, c);

        for (MetadataTypeInterface mdt : types) {
            myList.add(new SelectItem(mdt.getName(), getMetadatatypeLanguage(mdt)));
        }
        return myList;
    }

    @Override
    public int compare(Object firstObject, Object secondObject) {
        String imageSorting = ConfigCore.getParameter("ImageSorting", "number");
        String firstString = (String) firstObject;
        String secondString = (String) secondObject;
        // comparing only prefixes of files:
        firstString = firstString.substring(0, firstString.lastIndexOf('.'));
        secondString = secondString.substring(0, secondString.lastIndexOf('.'));

        if (imageSorting.equalsIgnoreCase("number")) {
            try {
                Integer firstIterator = Integer.valueOf(firstString);
                Integer secondIterator = Integer.valueOf(secondString);
                return firstIterator.compareTo(secondIterator);
            } catch (NumberFormatException e) {
                return firstString.compareToIgnoreCase(secondString);
            }
        } else if (imageSorting.equalsIgnoreCase("alphanumeric")) {
            return firstString.compareToIgnoreCase(secondString);
        } else {
            return firstString.compareToIgnoreCase(secondString);
        }
    }

}
