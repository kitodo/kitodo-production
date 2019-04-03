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

package org.kitodo.production.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.pagination.Paginator;

/**
 * This class contains some methods to handle meta-data (semi) automatically.
 */
public class MetadataEditor {
    /**
     * Creates a given number of new structures and inserts them into the
     * workpiece. The insertion position is given relative to an existing
     * structure. In addition, you can specify meta-data, which is assigned to
     * the structures consecutively with a counter.
     *
     * @param number
     *            number of structures to create
     * @param type
     *            type of new structure
     * @param workpiece
     *            workpiece to which the new structure is to be added
     * @param structure
     *            structure relative to which the new structure is to be
     *            inserted
     * @param position
     *            relative insertion position
     * @param metadataKey
     *            key of the meta-data to create
     * @param metadataValue
     *            value of the first meta-data entry
     */
    public static void addMultipleStructures(int number, String type, Workpiece workpiece, IncludedStructuralElement structure,
            InsertionPosition position, String metadataKey, String metadataValue) {

        Paginator metadataValues = new Paginator(metadataValue);
        for (int i = 1; i < number; i++) {
            IncludedStructuralElement newStructure = addStructure(type, workpiece, structure, position, Collections.emptyList());
            if (Objects.isNull(newStructure)) {
                continue;
            }
            MetadataEntry metadataEntry = new MetadataEntry();
            metadataEntry.setKey(metadataKey);
            metadataEntry.setValue(metadataValues.next());
            newStructure.getMetadata().add(metadataEntry);
        }
    }

    /**
     * Creates a new structure and inserts it into a workpiece. The insertion
     * position is determined by the specified structure and mode. The given
     * views are assigned to the structure and all its parent structures.
     *
     * @param type
     *            type of new structure
     * @param workpiece
     *            workpiece to which the new structure is to be added
     * @param structure
     *            structure relative to which the new structure is to be
     *            inserted
     * @param position
     *            relative insertion position
     * @param viewsToAdd
     *            views to be assigned to the structure
     * @return the newly created structure
     */
    public static IncludedStructuralElement addStructure(String type, Workpiece workpiece, IncludedStructuralElement structure,
            InsertionPosition position, List<View> viewsToAdd) {
        LinkedList<IncludedStructuralElement> parents = getAncestorsOfStructureRecursive(structure, workpiece.getRootElement(), null);
        if (parents.isEmpty()) {
            if ((position.equals(InsertionPosition.AFTER_CURRENT_ELEMENT)
                    || position.equals(InsertionPosition.BEFOR_CURRENT_ELEMENT))) {
                Helper.setErrorMessage("No parent found for currently selected structure to which new structure can be appended!");
                return null;
            }
        }
        IncludedStructuralElement newStructure = new IncludedStructuralElement();
        newStructure.setType(type);
        LinkedList<IncludedStructuralElement> structuresToAddViews = new LinkedList<>(parents);
        List<IncludedStructuralElement> siblings = parents.getLast().getChildren();
        switch (position) {
            case AFTER_CURRENT_ELEMENT: {
                siblings.add(siblings.indexOf(structure) + 1, newStructure);
                break;
            }
            case BEFOR_CURRENT_ELEMENT: {
                siblings.add(siblings.indexOf(structure), newStructure);
                break;
            }
            case FIRST_CHILD_OF_CURRENT_ELEMENT: {
                structuresToAddViews.add(structure);
                structure.getChildren().add(0, newStructure);
                break;
            }
            case LAST_CHILD_OF_CURRENT_ELEMENT: {
                structuresToAddViews.add(structure);
                structure.getChildren().add(newStructure);
                break;
            }
            case PARENT_OF_CURRENT_ELEMENT: {
                structuresToAddViews.removeLast();
                newStructure.getChildren().add(structure);
                if (parents.isEmpty()) {
                    workpiece.setRootElement(newStructure);
                } else {
                    siblings.set(siblings.indexOf(structure), newStructure);
                }
                break;
            }
            default:
                throw new IllegalStateException("complete switch");
        }
        for (IncludedStructuralElement structuree : structuresToAddViews) {
            structuree.getViews().addAll(viewsToAdd);
        }
        return newStructure;
    }

    public static void assignViewsFromChildren(IncludedStructuralElement structure) {
        structure.getViews().addAll(getViewsFromChildrenRecursive(structure));
    }

    private static Collection<View> getViewsFromChildrenRecursive(IncludedStructuralElement structure) {
        List<View> result = new ArrayList<>(structure.getViews());
        for (IncludedStructuralElement child : structure.getChildren()) {
            result.addAll(getViewsFromChildrenRecursive(child));
        }
        return result;
    }

    /**
     * Creates a view on a media unit that is not further restricted; that is,
     * the entire media unit is displayed.
     *
     * @param mediaUnit
     *            media unit on which a view is to be formed
     * @return the created media unit
     */
    public static View createUnrestrictedViewOn(MediaUnit mediaUnit) {
        View result = new View();
        result.setMediaUnit(mediaUnit);
        return result;
    }

    /**
     * Determines the ancestors of a tree node.
     *
     * @param searched
     *            node whose ancestor nodes are to be found
     * @param position
     *            node to be searched recursively
     * @return the parent nodes (maybe empty)
     */
    public static LinkedList<IncludedStructuralElement> getAncestorsOfStructure(IncludedStructuralElement searched,
                                                                                IncludedStructuralElement position) {
        return getAncestorsOfStructureRecursive(searched, position, null);
    }

    private static LinkedList<IncludedStructuralElement> getAncestorsOfStructureRecursive(IncludedStructuralElement searched, IncludedStructuralElement position,
                                                                                          IncludedStructuralElement parent) {
        if (position.equals(searched)) {
            if (Objects.isNull(parent)) {
                return new LinkedList<>();
            }
            LinkedList<IncludedStructuralElement> result = new LinkedList<>();
            result.add(parent);
            return result;

        }
        for (IncludedStructuralElement child : position.getChildren()) {
            LinkedList<IncludedStructuralElement> maybeFound = getAncestorsOfStructureRecursive(searched, child, position);
            if (!maybeFound.isEmpty()) {
                if (Objects.nonNull(parent)) {
                    maybeFound.addFirst(parent);
                }
                return maybeFound;
            }
        }
        return new LinkedList<>();
    }

    public static void moveView(View view, IncludedStructuralElement from, IncludedStructuralElement to) {
        from.getViews().remove(view);
        to.getViews().add(view);
    }
}
