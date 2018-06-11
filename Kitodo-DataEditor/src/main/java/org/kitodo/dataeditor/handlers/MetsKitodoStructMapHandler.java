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

package org.kitodo.dataeditor.handlers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.kitodo.config.Config;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataeditor.enums.PositionOfNewDiv;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.StructMapType;
import org.kitodo.exceptions.NotImplementedException;

public class MetsKitodoStructMapHandler {

    private static final MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    private MetsKitodoStructMapHandler() {
    }

    /**
     * Returns the StructMap element of the given type from given mets object.
     * 
     * @param mets
     *            The mets object from which the StructMap element should be
     *            returned.
     * @param type
     *            The type of the StructMap element which should be returned, e.g.
     *            "PHYSICAL" or "LOGICAL".
     * @return The StructMapType object.
     */
    public static StructMapType getMetsStructMapByType(Mets mets, String type) {
        for (StructMapType structMap : mets.getStructMap()) {
            if (Objects.equals(type, structMap.getTYPE())) {
                return structMap;
            }
        }
        throw new NoSuchElementException("StructMap element of type " + type + " does not exist");
    }

    /**
     * Reads the FileSec of mets object and inserts corresponding physical
     * structMap.
     * 
     * @param mets
     *            The mets object.
     */
    public static void fillPhysicalStructMapByMetsFileSec(Mets mets) {
        DivType rootDiv = objectFactory.createRootDivTypeForPhysicalStructMap();
        rootDiv.getDiv()
                .addAll(getDivTypesByFileTypes(MetsKitodoFileSecHandler.getLocalFileGroupOfMets(mets).getFile()));
        StructMapType physicalStructMap = getMetsStructMapByType(mets, "PHYSICAL");
        physicalStructMap.setDiv(rootDiv);
    }

    private static List<DivType> getDivTypesByFileTypes(List<FileType> fileTypes) {
        List<DivType> divTypes = new ArrayList<>();
        int counter = 1;
        for (FileType file : fileTypes) {
            DivType div = objectFactory.createDivType();
            div.setID("PHYS_" + String.format("%04d", counter));
            div.setORDER(BigInteger.valueOf(counter));
            div.setORDERLABEL(Config.getParameter("MetsEditorDefaultPagination"));
            div.setTYPE(getPhysicalDivTypeByFileType(file));
            DivType.Fptr divTypeFptr = objectFactory.createDivTypeFptr();
            divTypeFptr.setFILEID(file);
            div.getFptr().add(divTypeFptr);

            divTypes.add(div);

            counter++;
        }
        return divTypes;
    }

    private static String getPhysicalDivTypeByFileType(FileType file) {
        if (file.getMIMETYPE().contains("image")) {
            return "page";
        }
        if (file.getMIMETYPE().contains("audio")) {
            return "track";
        }
        return "other";
    }

    public static void addNewLogicalDivToDivOfStructMap(DivType presentDiv, String type, StructMapType structMap,
            PositionOfNewDiv position) {
        DivType newDiv = objectFactory.createDivType();
        DivType parentDiv;
        int count;
        newDiv.setTYPE(type);
        switch (position) {
            case LAST_CHILD_OF_ELEMENT:
                addDivToDivAsLastChild(presentDiv, newDiv);
                break;
            case FIRST_CHILD_OF_ELEMENT:
                addDivToDivAsFirstChild(presentDiv, newDiv);
                break;
            case BEFOR_ELEMENT:
                parentDiv = getParentDivOfDivByStructMap(presentDiv, structMap);
                count = parentDiv.getDiv().size();
                for (int i = 0; i < count; i++) {
                    if (Objects.equals(parentDiv.getDiv().get(i).getID(), presentDiv.getID())) {
                        parentDiv.getDiv().add(i, newDiv);
                        break;
                    }
                }
                break;
            case AFTER_ELEMENT:
                parentDiv = getParentDivOfDivByStructMap(presentDiv, structMap);
                count = parentDiv.getDiv().size();
                for (int i = 0; i < count; i++) {
                    if (Objects.equals(parentDiv.getDiv().get(i).getID(), presentDiv.getID())) {
                        parentDiv.getDiv().add(i + 1, newDiv);
                        break;
                    }
                }
                break;
            default:
                throw new NotImplementedException("Position of new div element is not implemented");
        }
    }

    private static void addDivToDivAsLastChild(DivType presentDiv, DivType newDiv) {
        presentDiv.getDiv().add(newDiv);
    }

    private static void addDivToDivAsFirstChild(DivType presentDiv, DivType newDiv) {
        presentDiv.getDiv().add(0, newDiv);
    }

    private static DivType getParentDivOfDivByStructMap(DivType div, StructMapType structMap) {
        DivType currentParentDiv = structMap.getDiv();
        if (divTypeListContainsDiv(currentParentDiv.getDiv(), div)) {
            return currentParentDiv;
        }
        return getParentDivOfDivByDivList(div, currentParentDiv.getDiv());
    }

    private static boolean divTypeListContainsDiv(List<DivType> divTypeList, DivType div) {
        for (DivType divInList : divTypeList) {
            if (Objects.equals(divInList.getID(), div.getID())) {
                return true;
            }
        }
        return false;
    }

    private static DivType getParentDivOfDivByDivList(DivType div, List<DivType> divTypeList) {
        for (DivType divInList : divTypeList) {
            DivType currentParentDiv = divInList;
            if (divTypeListContainsDiv(currentParentDiv.getDiv(), div)) {
                return currentParentDiv;
            }
            try {
                return getParentDivOfDivByDivList(div, currentParentDiv.getDiv());
            } catch (NoSuchElementException e) {
                // we do nothing here
                // this method is calling its self so we need to catch the exception that
                // the for loop can run farther
            }
        }
        throw new NoSuchElementException("Parent div element not found");
    }

    /**
     * Generating and setting of ids of all div elements in given StructMapType
     * object.
     * 
     * @param structMap
     *            The StructMapType object.
     */
    public static void generateIdsForLogicalStructMapElements(StructMapType structMap) {
        if (Objects.nonNull(structMap.getDiv())) {
            List<DivType> divTypes = structMap.getDiv().getDiv();
            if (!divTypes.isEmpty()) {
                int index = 1;
                setIdsOfDivTypes(divTypes, "LOG_", index);
            }
        }
    }

    private static int setIdsOfDivTypes(List<DivType> divTypes, String prefix, int startingIndex) {
        for (DivType div : divTypes) {
            div.setID(prefix + String.format("%04d", startingIndex));
            startingIndex++;
            if (!div.getDiv().isEmpty()) {
                startingIndex = setIdsOfDivTypes(div.getDiv(), prefix, startingIndex);
            }
        }
        return startingIndex;
    }
}
