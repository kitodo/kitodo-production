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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

public class MetadatenHelper implements Comparator<Object> {
    private static final Logger logger = LogManager.getLogger(MetadatenHelper.class);
    public static final int PAGENUMBER_FIRST = 0;
    public static final int PAGENUMBER_LAST = 1;

    private Prefs myPrefs;
    private DigitalDocument mydocument;

    public MetadatenHelper(Prefs inPrefs, DigitalDocument inDocument) {
        this.myPrefs = inPrefs;
        this.mydocument = inDocument;
    }

    /**
     * Change current document structure.
     *
     * @param inOldDocstruct
     *            DocStruct object
     * @param inNewType
     *            String
     * @return DocStruct object
     */
    public DocStruct changeCurrentDocstructType(DocStruct inOldDocstruct, String inNewType)
            throws DocStructHasNoTypeException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException,
            TypeNotAllowedForParentException {
        // inOldDocstruct.getType().getName()
        // + " soll werden zu " + inNewType);
        DocStructType dst = this.myPrefs.getDocStrctTypeByName(inNewType);
        DocStruct newDocstruct = this.mydocument.createDocStruct(dst);
        /*
         * alle Metadaten hinzufügen
         */
        if (inOldDocstruct.getAllMetadata() != null && inOldDocstruct.getAllMetadata().size() > 0) {
            for (Metadata old : inOldDocstruct.getAllMetadata()) {
                boolean match = false;

                if (newDocstruct.getPossibleMetadataTypes() != null
                        && newDocstruct.getPossibleMetadataTypes().size() > 0) {
                    for (MetadataType mt : newDocstruct.getPossibleMetadataTypes()) {
                        if (mt.getName().equals(old.getType().getName())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        try {
                            newDocstruct.addMetadata(old);
                        } catch (Exception e) {
                            Helper.setFehlerMeldung("Metadata " + old.getType().getName()
                                    + " is not allowed in new element " + newDocstruct.getType().getName());
                            return inOldDocstruct;
                        }
                    } else {
                        newDocstruct.addMetadata(old);
                    }
                } else {
                    Helper.setFehlerMeldung("Metadata " + old.getType().getName() + " is not allowed in new element "
                            + newDocstruct.getType().getName());
                    return inOldDocstruct;
                }
            }
        }
        /*
         * alle Personen hinzufügen
         */
        if (inOldDocstruct.getAllPersons() != null && inOldDocstruct.getAllPersons().size() > 0) {
            for (Person old : inOldDocstruct.getAllPersons()) {
                boolean match = false;
                if (newDocstruct.getPossibleMetadataTypes() != null
                        && newDocstruct.getPossibleMetadataTypes().size() > 0) {
                    for (MetadataType mt : newDocstruct.getPossibleMetadataTypes()) {
                        if (mt.getName().equals(old.getType().getName())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        Helper.setFehlerMeldung("Person " + old.getType().getName() + " is not allowed in new element "
                                + newDocstruct.getType().getName());
                    } else {
                        newDocstruct.addPerson(old);
                    }
                } else {
                    Helper.setFehlerMeldung("Person " + old.getType().getName() + " is not allowed in new element "
                            + newDocstruct.getType().getName());
                    return inOldDocstruct;
                }
            }
        }
        /*
         * alle Seiten hinzufügen
         */
        if (inOldDocstruct.getAllToReferences() != null) {
            // TODO: get rid of Iterators, use a for Loop instead
            for (Iterator<Reference> iterator = inOldDocstruct.getAllToReferences().iterator(); iterator.hasNext();) {
                Reference p = iterator.next();
                newDocstruct.addReferenceTo(p.getTarget(), p.getType());
            }
        }

        /*
         * alle Docstruct-Children hinzufügen
         */
        if (inOldDocstruct.getAllChildren() != null && inOldDocstruct.getAllChildren().size() > 0) {
            for (DocStruct old : inOldDocstruct.getAllChildren()) {
                if (newDocstruct.getType().getAllAllowedDocStructTypes() != null
                        && newDocstruct.getType().getAllAllowedDocStructTypes().size() > 0) {

                    if (!newDocstruct.getType().getAllAllowedDocStructTypes().contains(old.getType().getName())) {
                        Helper.setFehlerMeldung("Child element " + old.getType().getName()
                                + " is not allowed in new element " + newDocstruct.getType().getName());
                        return inOldDocstruct;
                    } else {
                        newDocstruct.addChild(old);
                    }
                } else {
                    Helper.setFehlerMeldung("Child element " + old.getType().getName()
                            + " is not allowed in new element " + newDocstruct.getType().getName());
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
        // TODO: get rid of Iterators, use a for Loop instead
        for (Iterator<DocStruct> iter = newDocstruct.getParent().getAllChildren().iterator(); iter.hasNext(); i++) {
            if (iter.next() == inOldDocstruct) {
                break;
            }
        }
        for (int j = newDocstruct.getParent().getAllChildren().size() - i; j > 0; j--) {
            KnotenUp(newDocstruct);
        }

        /*
         * altes Docstruct vom Parent entfernen und neues als aktuelles nehmen
         */
        inOldDocstruct.getParent().removeChild(inOldDocstruct);
        return newDocstruct;
    }

    /**
     * Move around the document structure tree.
     *
     * @param inStruct
     *            DocStruct object
     */
    public void KnotenUp(DocStruct inStruct) throws TypeNotAllowedAsChildException {
        DocStruct parent = inStruct.getParent();
        if (parent == null) {
            return;
        }
        List<DocStruct> alleDS = null;

        /* das erste Element kann man nicht nach oben schieben */
        if (parent.getAllChildren().get(0) == inStruct) {
            return;
        }

        /* alle Elemente des Parents durchlaufen */
        for (DocStruct tempDS : parent.getAllChildren()) {
            /*
             * wenn das folgende Element das zu verschiebende ist dabei die
             * Exception auffangen, falls es kein nächstes Kind gibt
             */
            try {
                if (parent.getNextChild(tempDS) == inStruct) {
                    alleDS = new ArrayList<DocStruct>();
                }
            } catch (Exception e) {
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
            for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
                parent.removeChild(iter.next());
            }

            /* anschliessend die Childliste korrigieren */
            // parent.addChild(myStrukturelement);
            for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
                parent.addChild(iter.next());
            }
        }
    }

    /**
     * Move around the document structure tree.
     *
     * @param inStruct
     *            DocStruct object
     */
    public void setNodeDown(DocStruct inStruct) throws TypeNotAllowedAsChildException {
        DocStruct parent = inStruct.getParent();
        if (parent == null) {
            return;
        }
        List<DocStruct> alleDS = new ArrayList<DocStruct>();

        /* alle Elemente des Parents durchlaufen */
        // TODO: get rid of Iterators, use a for Loop instead
        for (Iterator<DocStruct> iter = parent.getAllChildren().iterator(); iter.hasNext();) {
            DocStruct tempDS = iter.next();

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
        for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
            parent.removeChild(iter.next());
        }

        /* anschliessend die neue Childliste anlegen */
        for (Iterator<DocStruct> iter = alleDS.iterator(); iter.hasNext();) {
            parent.addChild(iter.next());
        }
    }

    /**
     * die MetadatenTypen zurückgeben.
     */
    public SelectItem[] getAddableDocStructTypen(DocStruct inStruct, boolean checkTypesFromParent) {
        /*
         * zuerst mal die addierbaren Metadatentypen ermitteln
         */
        List<String> types;
        SelectItem myTypes[] = new SelectItem[0];

        try {
            if (!checkTypesFromParent) {
                types = inStruct.getType().getAllAllowedDocStructTypes();
            } else {
                types = inStruct.getParent().getType().getAllAllowedDocStructTypes();
            }
        } catch (RuntimeException e) {
            return myTypes;
        }

        if (types == null) {
            return myTypes;
        }

        List<DocStructType> newTypes = new ArrayList<DocStructType>();
        for (String tempTitel : types) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(tempTitel);
            if (dst != null) {
                newTypes.add(dst);
            } else {
                Helper.setMeldung(null, "Regelsatz-Fehler: ", " DocstructType " + tempTitel + " nicht definiert");
                logger.error("getAddableDocStructTypen() - Regelsatz-Fehler: DocstructType " + tempTitel
                        + " nicht definiert");
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("DocStructTypen");
        // TODO: Uses generics, if possible
        Collections.sort(newTypes, c);

        /*
         * nun ein Array mit der richtigen Größe anlegen
         */
        int zaehler = newTypes.size();
        myTypes = new SelectItem[zaehler];

        /*
         * und anschliessend alle Elemente in das Array packen
         */
        zaehler = 0;
        Iterator<DocStructType> it = newTypes.iterator();
        while (it.hasNext()) {
            DocStructType dst = it.next();
            String label = dst
                    .getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
            if (label == null) {
                label = dst.getName();
            }
            myTypes[zaehler] = new SelectItem(dst.getName(), label);
            zaehler++;
        }
        return myTypes;
    }

    /**
     * alle unbenutzen Metadaten des Docstruct löschen, Unterelemente rekursiv
     * aufrufen.
     */
    public void deleteAllUnusedElements(DocStruct inStruct) {
        inStruct.deleteUnusedPersonsAndMetadata();
        if (inStruct.getAllChildren() != null && inStruct.getAllChildren().size() > 0) {
            // TODO: get rid of Iterators, use a for Loop instead
            for (Iterator<DocStruct> it = inStruct.getAllChildren().iterator(); it.hasNext();) {
                DocStruct ds = it.next();
                deleteAllUnusedElements(ds);
            }
        }
    }

    /**
     * die erste Imagenummer zurückgeben.
     */
    // FIXME: alphanumerisch
    public String getImageNumber(DocStruct inStrukturelement, int inPageNumber) {
        String rueckgabe = "";

        if (inStrukturelement == null) {
            return "";
        }
        List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
        if (listReferenzen != null && listReferenzen.size() > 0) {
            /*
             * Referenzen sortieren
             */
            Collections.sort(listReferenzen, new Comparator<Reference>() {
                @Override
                public int compare(final Reference firstObject, final Reference secondObject) {
                    final Reference firstReference = firstObject;
                    final Reference secondReference = secondObject;
                    Integer firstPage = 0;
                    Integer secondPage = 0;
                    final MetadataType mdt = MetadatenHelper.this.myPrefs.getMetadataTypeByName("physPageNumber");
                    List<? extends Metadata> listMetadaten = firstReference.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        final Metadata meineSeite = listMetadaten.get(0);
                        firstPage = Integer.parseInt(meineSeite.getValue());
                    }
                    listMetadaten = secondReference.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        final Metadata meineSeite = listMetadaten.get(0);
                        secondPage = Integer.parseInt(meineSeite.getValue());
                    }
                    return firstPage.compareTo(secondPage);
                }
            });

            MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            List<? extends Metadata> listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (listSeiten != null && listSeiten.size() > 0) {
                Metadata meineSeite = listSeiten.get(0);
                rueckgabe += meineSeite.getValue();
            }
            mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
            listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (listSeiten != null && listSeiten.size() > 0) {
                Metadata meineSeite = listSeiten.get(0);
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
    public List<? extends Metadata> getMetadataInclDefaultDisplay(DocStruct inStruct, String inLanguage,
            boolean inIsPerson, Process inProzess) {
        List<MetadataType> displayMetadataTypes = inStruct.getDisplayMetadataTypes();
        /* sofern Default-Metadaten vorhanden sind, diese ggf. ergänzen */
        if (displayMetadataTypes != null) {
            for (MetadataType mdt : displayMetadataTypes) {
                // check, if mdt is already in the allMDs Metadata list, if not
                // - add it
                if (!(inStruct.getAllMetadataByType(mdt) != null && inStruct.getAllMetadataByType(mdt).size() != 0)) {
                    try {
                        if (mdt.getIsPerson()) {
                            Person p = new Person(mdt);
                            p.setRole(mdt.getName());
                            inStruct.addPerson(p);
                        } else {
                            Metadata md = new Metadata(mdt);
                            inStruct.addMetadata(md); // add this new metadata
                            // element
                        }
                    } catch (DocStructHasNoTypeException e) {
                        continue;
                    } catch (MetadataTypeNotAllowedException e) {
                        continue;
                    }
                }
            }
        }

        /*
         * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch
         * sortieren
         */
        if (inIsPerson) {
            List<Person> persons = inStruct.getAllPersons();
            if (persons != null && !inProzess.getRuleset().isOrderMetadataByRuleset()) {
                Collections.sort(persons, new MetadataComparator(inLanguage));
            }
            return persons;
        } else {
            List<Metadata> metadata = inStruct.getAllMetadata();
            if (metadata != null && !inProzess.getRuleset().isOrderMetadataByRuleset()) {
                Collections.sort(metadata, new MetadataComparator(inLanguage));
            }
            return getAllVisibleMetadataHack(inStruct);

        }
    }

    /** TODO: Replace it, after Maven is kicked :). */
    private List<Metadata> getAllVisibleMetadataHack(DocStruct inStruct) {

        // Start with the list of all metadata.
        List<Metadata> result = new LinkedList<Metadata>();

        // Iterate over all metadata.
        if (inStruct.getAllMetadata() != null) {
            for (Metadata md : inStruct.getAllMetadata()) {
                // If the metadata has some value and it does not start with the
                // HIDDEN_METADATA_CHAR, add it to the result list.
                if (!md.getType().getName().startsWith("_")) {
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
    public static String getMetaFileType(String file) throws IOException {
        /*
         * Typen und Suchbegriffe festlegen
         */
        HashMap<String, String> types = new HashMap<String, String>();
        types.put("metsmods", "ugh.fileformats.mets.MetsModsImportExport".toLowerCase());
        types.put("mets", "www.loc.gov/METS/".toLowerCase());
        types.put("rdf", "<RDF:RDF ".toLowerCase());
        types.put("xstream", "<ugh.dl.DigitalDocument>".toLowerCase());

        try (InputStreamReader input = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader bufRead = new BufferedReader(input);) {
            char[] buffer = new char[200];
            while ((bufRead.read(buffer)) >= 0) {
                String temp = new String(buffer).toLowerCase();
                Iterator<Entry<String, String>> i = types.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<String, String> entry = i.next();
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
    public String getMetadatatypeLanguage(MetadataType inMdt) {
        String label = inMdt
                .getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadataLanguage}"));
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
        private String language = "de";

        public MetadataComparator(String inLanguage) {
            this.language = inLanguage;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public int compare(Object firstObject, Object secondObject) {
            Metadata firstMetadata = (Metadata) firstObject;
            Metadata secondMetadata = (Metadata) secondObject;
            if (firstMetadata == null) {
                return -1;
            }
            if (secondMetadata == null) {
                return 1;
            }
            String firstName = "";
            String secondName = "";
            try {
                MetadataType firstMetadataType = firstMetadata.getType();
                MetadataType secondMetadataType = secondMetadata.getType();
                firstName = firstMetadataType.getNameByLanguage(this.language);
                secondName = secondMetadataType.getNameByLanguage(this.language);
            } catch (java.lang.NullPointerException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Language " + language + " for metadata " + firstMetadata.getType() + " or "
                            + secondMetadata.getType() + " is missing in ruleset");
                }
                return 0;
            }
            if (firstName == null || firstName.length() == 0) {
                firstName = firstMetadata.getType().getName();
                if (firstName == null) {
                    return -1;
                }
            }
            if (secondName == null || secondName.length() == 0) {
                secondName = secondMetadata.getType().getName();
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
    public ArrayList<SelectItem> getAddablePersonRoles(DocStruct myDocStruct, String inRoleName) {
        ArrayList<SelectItem> myList = new ArrayList<SelectItem>();
        /*
         * zuerst mal alle addierbaren Metadatentypen ermitteln
         */
        List<MetadataType> types = myDocStruct.getPossibleMetadataTypes();
        if (types == null) {
            types = new ArrayList<MetadataType>();
        }
        if (inRoleName != null && inRoleName.length() > 0) {
            boolean addRole = true;
            for (MetadataType mdt : types) {
                if (mdt.getName().equals(inRoleName)) {
                    addRole = false;
                }
            }

            if (addRole) {
                types.add(this.myPrefs.getMetadataTypeByName(inRoleName));
            }
        }
        /*
         * alle Metadatentypen, die keine Person sind, oder mit einem
         * Unterstrich anfangen rausnehmen
         */
        for (MetadataType mdt : new ArrayList<MetadataType>(types)) {
            if (!mdt.getIsPerson()) {
                types.remove(mdt);
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        for (MetadataType mdt : types) {
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
        firstString = firstString.substring(0, firstString.lastIndexOf("."));
        secondString = secondString.substring(0, secondString.lastIndexOf("."));

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
