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

package org.kitodo.dataeditor.entities;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataeditor.enums.PositionOfNewDiv;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class LogicalStructMapType extends StructMapType {

    private static final Logger logger = LogManager.getLogger(LogicalStructMapType.class);

    public LogicalStructMapType(StructMapType structMapType) {
        super.div = structMapType.getDiv();
        super.id = structMapType.getID();
        super.label = structMapType.getLABEL();
        super.type = structMapType.getTYPE();
    }

    /**
     * Moves a given DivType object by removing it from logical structMap and
     * inserting it as a child of a new parent div.
     *
     * @param movedDiv
     *            The DivType object which has to be moved.
     * @param parentDiv
     *            The DivType object which is the new parent div.
     * @param index
     *            The index position where the moved div needs to be inserted.
     */
    public void moveDivToDivAtIndex(DivType movedDiv, DivType parentDiv, int index) {
        removeDiv(movedDiv);
        parentDiv.getDiv().add(index, movedDiv);
        generateIdsForDivs();
    }

    /**
     * Removed the given DivType object from current logical structMap.
     *
     * @param divToRemove
     *            The DivType object which should be removed.
     */
    public void removeDiv(DivType divToRemove) {
        removeDivFromStructMap(divToRemove);
        generateIdsForDivs();
    }

    /**
     * Adds a new DivType object which specified type and position to the given
     * DivType object.
     *
     * @param presentDiv
     *            The DivType object to which the new DivType should be added to.
     * @param type
     *            The type of the DivType object.
     * @param position
     *            The position in relation to the given DivType object.
     */
    public void addNewDiv(DivType presentDiv, String type, PositionOfNewDiv position) {
        addNewLogicalDivToExistingDiv(presentDiv, type, position);
        generateIdsForDivs();
    }

    /**
     * Adds a new logical div to an existing div of a structMap.
     *
     * @param existingDiv
     *            The existing div.
     * @param type
     *            The type of the new div.
     * @param position
     *            The position of the new added div.
     */
    private void addNewLogicalDivToExistingDiv(DivType existingDiv, String type,
                                               PositionOfNewDiv position) {
        MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
        DivType newDiv = objectFactory.createDivType();
        newDiv.setTYPE(type);
        switch (position) {
            case LAST_CHILD_OF_ELEMENT:
                addDivToDivAsLastChild(existingDiv, newDiv);
                break;
            case FIRST_CHILD_OF_ELEMENT:
                addDivToDivAsFirstChild(existingDiv, newDiv);
                break;
            case BEFOR_ELEMENT:
                addDivBeforeExistingDiv(existingDiv, newDiv);
                break;
            case AFTER_ELEMENT:
                addDivAfterExistingDiv(existingDiv, newDiv);
                break;
            default:
                throw new IllegalArgumentException("Position of new div element is not supported");
        }
    }

    private void addDivToDivAsLastChild(DivType existingDiv, DivType newDiv) {
        existingDiv.getDiv().add(newDiv);
    }

    private void addDivToDivAsFirstChild(DivType existingDiv, DivType newDiv) {
        existingDiv.getDiv().add(0, newDiv);
    }

    private void addDivBeforeExistingDiv(DivType existingDiv, DivType newDiv) {
        DivType parentDiv = getParentDivOfDiv(existingDiv);
        int count = parentDiv.getDiv().size();
        for (int i = 0; i < count; i++) {
            if (Objects.equals(parentDiv.getDiv().get(i).getID(), existingDiv.getID())) {
                parentDiv.getDiv().add(i, newDiv);
                return;
            }
        }
    }

    private void addDivAfterExistingDiv(DivType existingDiv, DivType newDiv) {
        DivType parentDiv = getParentDivOfDiv(existingDiv);
        int count = parentDiv.getDiv().size();
        for (int i = 0; i < count; i++) {
            if (Objects.equals(parentDiv.getDiv().get(i).getID(), existingDiv.getID())) {
                parentDiv.getDiv().add(i + 1, newDiv);
                return;
            }
        }
    }

    private DivType getParentDivOfDiv(DivType div) {
        DivType currentParentDiv = this.getDiv();
        if (currentParentDiv.getDiv().contains(div)) {
            return currentParentDiv;
        }
        return getParentDivOfChildDivFromDivList(div, currentParentDiv.getDiv());
    }

    private DivType getParentDivOfChildDivFromDivList(DivType childDiv, List<DivType> divTypeList) {
        if (childDiv.getID().contains("ROOT")) {
            throw new UnsupportedOperationException("Root element can not have a parent!");
        }
        for (DivType div : divTypeList) {
            if (div.getDiv().contains(childDiv)) {
                return div;
            }
            if (!div.getDiv().isEmpty()) {
                try {
                    return getParentDivOfChildDivFromDivList(childDiv, div.getDiv());
                } catch (NoSuchElementException e) {
                    // this method is calling recursive its self for handling a complex structure of nested divs
                    // we need to catch the below exception internally that the for loop can run farther
                    logger.debug("Div element with Id " + div.getID() + " does not contain div element with Id: " + childDiv.getID());
                }
            }
        }
        throw new NoSuchElementException("Child div element not found");
    }

    /**
     * Generating and setting of ids of all div elements in given StructMapType
     * object.
     *
     */
    private void generateIdsForDivs() {
        if (Objects.nonNull(super.getDiv())) {
            List<DivType> divTypes = super.getDiv().getDiv();
            if (!divTypes.isEmpty()) {
                int index = 1;
                setIdsOfDivTypes(divTypes, "LOG_", index);
            }
        }
    }

    private int setIdsOfDivTypes(List<DivType> divTypes, String prefix, int startingIndex) {
        for (DivType div : divTypes) {
            div.setID(prefix + String.format("%04d", startingIndex));
            startingIndex++;
            if (!div.getDiv().isEmpty()) {
                startingIndex = setIdsOfDivTypes(div.getDiv(), prefix, startingIndex);
            }
        }
        return startingIndex;
    }

    /**
     * Removed a div element from a structMap. Does nothing if the given div does
     * not exist at structMap.
     *
     * @param divToRemove
     *            The divType object to remove.
     */
    public void removeDivFromStructMap(DivType divToRemove) {
        removeDivFromDivList(divToRemove, this.getDiv().getDiv());
    }

    private void removeDivFromDivList(DivType divToRemove, List<DivType> divList) {
        if (!divList.remove(divToRemove)) {
            for (DivType div : divList) {
                if (!div.getDiv().isEmpty()) {
                    removeDivFromDivList(divToRemove, div.getDiv());
                }
            }
        }
    }
}
