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

package org.kitodo.production.helper.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.enums.SortType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.HelperComparator;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyLogicalDocStructTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyReferenceHelper;
import org.kitodo.production.metadata.comparator.MetadataComparator;
import org.kitodo.production.services.ServiceManager;

public class MetadataHelper {
    private static final Logger logger = LogManager.getLogger(MetadataHelper.class);
    private static final int PAGENUMBER_FIRST = 0;
    private static final int PAGENUMBER_LAST = 1;
    private LegacyPrefsHelper prefs;
    private LegacyMetsModsDigitalDocumentHelper digitalDocument;

    public MetadataHelper(LegacyPrefsHelper inPrefs, LegacyMetsModsDigitalDocumentHelper inDocument) {
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
     * die MetadatenTypen zurückgeben.
     */
    public SelectItem[] getAddableDocStructTypen(LegacyDocStructHelperInterface inStruct, boolean checkTypesFromParent) {
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

        List<LegacyLogicalDocStructTypeHelper> newTypes = new ArrayList<>();
        for (String tempTitel : types) {
            LegacyLogicalDocStructTypeHelper dst = this.prefs.getDocStrctTypeByName(tempTitel);
            if (dst != null) {
                newTypes.add(dst);
            } else {
                Helper.setMessage("Regelsatz-Fehler: ", " DocstructType " + tempTitel + " nicht definiert");
                logger.error(
                    "getAddableDocStructTypen() - Regelsatz-Fehler: DocstructType " + tempTitel + " nicht definiert");
            }
        }

        /*
         * die Metadatentypen sortieren
         */
        HelperComparator c = new HelperComparator();
        c.setSortType(SortType.DOC_STRUCT_TYPE);
        newTypes.sort(c);

        // nun ein Array mit der richtigen Größe anlegen
        int zaehler = newTypes.size();
        myTypes = new SelectItem[zaehler];

        // und anschliessend alle Elemente in das Array packen
        zaehler = 0;
        String language = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        for (LegacyLogicalDocStructTypeHelper docStructType : newTypes) {
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
    public void deleteAllUnusedElements(LegacyDocStructHelperInterface inStruct) {
        inStruct.deleteUnusedPersonsAndMetadata();
        if (Objects.nonNull(inStruct.getAllChildren())) {
            for (LegacyDocStructHelperInterface child : inStruct.getAllChildren()) {
                deleteAllUnusedElements(child);
            }
        }
    }

    /**
     * die erste Imagenummer zurückgeben.
     */
    // FIXME: alphanumerisch
    public String getImageNumber(LegacyDocStructHelperInterface inStrukturelement, int inPageNumber) {
        String result = "";

        if (inStrukturelement == null) {
            return "";
        }
        List<LegacyReferenceHelper> references = inStrukturelement.getAllReferences("to");
        if (Objects.nonNull(references) && !references.isEmpty()) {
            references.sort(new ReferencesSortHelper(prefs));
            result = getMetadataPageNumber("physPageNumber", inPageNumber, references);
            String pageNumber = getMetadataPageNumber("logicalPageNumber", inPageNumber, references);
            if (!pageNumber.isEmpty()) {
                result += ":" + pageNumber;
            }
        }
        return result;
    }

    private String getMetadataPageNumber(String metadataType, int inPageNumber,
            List<LegacyReferenceHelper> references) {
        LegacyMetadataTypeHelper metadataTypeHelper = this.prefs.getMetadataTypeByName(metadataType);
        List<? extends LegacyMetadataHelper> pages = references.get(0).getTarget()
                .getAllMetadataByType(metadataTypeHelper);
        if (inPageNumber == PAGENUMBER_LAST) {
            pages = references.get(references.size() - 1).getTarget().getAllMetadataByType(metadataTypeHelper);
        }
        if (Objects.nonNull(pages) && !pages.isEmpty()) {
            LegacyMetadataHelper meineSeite = pages.get(0);
            return meineSeite.getValue();
        }
        return "";
    }

    /**
     * Determine all meta-data of the transferred DocStruct and complete the
     * missing DefaultDisplay meta-data.
     */
    public List<LegacyMetadataHelper> getMetadataInclDefaultDisplay(LegacyDocStructHelperInterface inStruct,
            String inLanguage, boolean inIsPerson, Process inProzess) {

        supplementDefaultMetadata(inStruct, inStruct.getDisplayMetadataTypes());

        /*
         * if you do not want to sort by ruleset, sort alphabetically here
         */
        if (!inProzess.getRuleset().isOrderMetadataByRuleset()) {
            List<LegacyMetadataHelper> metadata = inStruct.getAllMetadata();
            if (metadata != null) {
                metadata.sort(new MetadataComparator(inLanguage));
            }
        }

        return getAllVisibleMetadataHack(inStruct);
    }

    /**
     * If default metadata exist, supplement it if necessary.
     */
    private void supplementDefaultMetadata(LegacyDocStructHelperInterface inStruct,
            List<LegacyMetadataTypeHelper> displayMetadataTypes) {
        if (displayMetadataTypes != null) {
            for (LegacyMetadataTypeHelper mdt : displayMetadataTypes) {
                /*
                 * check, if mdt is already in the allMDs Metadata list, if not
                 * - add it
                 */
                if (!(inStruct.getAllMetadataByType(mdt) != null && !inStruct.getAllMetadataByType(mdt).isEmpty())) {
                    if (mdt.isPerson()) {
                        throw new UnsupportedOperationException("Dead code pending removal");
                    } else {
                        LegacyMetadataHelper md = new LegacyMetadataHelper(mdt);
                        inStruct.addMetadata(md);
                    }
                }
            }
        }
    }

    /** TODO: Replace it, after Maven is kicked :). */
    private List<LegacyMetadataHelper> getAllVisibleMetadataHack(LegacyDocStructHelperInterface inStruct) {

        // Start with the list of all metadata.
        List<LegacyMetadataHelper> result = new LinkedList<>();

        // Iterate over all metadata.
        if (inStruct.getAllMetadata() != null) {
            for (LegacyMetadataHelper md : inStruct.getAllMetadata()) {
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

        try (InputStreamReader input = new InputStreamReader(ServiceManager.getFileService().read((file)),
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
    public String getMetadatatypeLanguage(LegacyMetadataTypeHelper inMdt) {
        String label = inMdt.getLanguage(ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
        }
        return label;
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
    public List<SelectItem> getAddablePersonRoles(LegacyDocStructHelperInterface myDocStruct, String inRoleName) {
        ArrayList<SelectItem> myList = new ArrayList<>();
        /*
         * zuerst mal alle addierbaren Metadatentypen ermitteln
         */
        List<LegacyMetadataTypeHelper> types = myDocStruct.getPossibleMetadataTypes();
        if (types == null) {
            types = new ArrayList<>();
        }
        if (inRoleName != null && inRoleName.length() > 0) {
            boolean addRole = true;
            for (LegacyMetadataTypeHelper mdt : types) {
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
        for (LegacyMetadataTypeHelper mdt : new ArrayList<>(types)) {
            if (!mdt.isPerson()) {
                types.remove(mdt);
            }
        }

        HelperComparator c = new HelperComparator();
        c.setSortType(SortType.METADATA_TYPE);
        types.sort(c);

        for (LegacyMetadataTypeHelper mdt : types) {
            myList.add(new SelectItem(mdt.getName(), getMetadatatypeLanguage(mdt)));
        }
        return myList;
    }

    public LegacyPrefsHelper getPrefs() {
        return this.prefs;
    }
}
