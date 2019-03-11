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
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
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
    public static void addMultipleStructures(int number, String type, Workpiece workpiece, Structure structure,
            InsertionPosition position, String metadataKey, String metadataValue) {

        Paginator metadataValues = new Paginator(metadataValue);
        for (int i = 1; i < number; i++) {
            Structure newStructure = addStructure(type, workpiece, structure, position, Collections.emptyList());
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
    public static Structure addStructure(String type, Workpiece workpiece, Structure structure,
            InsertionPosition position, List<View> viewsToAdd) {

        LinkedList<Structure> parents = getAncestorsOfStructureRecursive(structure, workpiece.getStructure(), null);
        Structure newStructure = new Structure();
        newStructure.setType(type);
        List<Structure> siblings = parents.getLast().getChildren();
        LinkedList<Structure> structuresToAddViews = new LinkedList<>(parents);
        switch (position) {
            case AFTER_CURRENT_ELEMENT: {
                int index = siblings.indexOf(structure) + 1;
                siblings.add(index, newStructure);
                break;
            }
            case BEFOR_CURRENT_ELEMENT: {
                int index = siblings.indexOf(structure);
                siblings.add(index, newStructure);
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
                    workpiece.setStructure(newStructure);
                } else {
                    int index = siblings.indexOf(structure);
                    siblings.set(index, newStructure);
                }
                break;
            }
            default:
                throw new IllegalStateException("complete switch");
        }
        for (Structure structuree : structuresToAddViews) {
            structuree.getViews().addAll(viewsToAdd);
        }
        return newStructure;
    }

    public static void assignViewsFromChildren(Structure structure) {
        structure.getViews().addAll(getViewsFromChildrenRecursive(structure));
    }

    private static Collection<View> getViewsFromChildrenRecursive(Structure structure) {
        List<View> result = new ArrayList<>();
        result.addAll(structure.getViews());
        for (Structure child : structure.getChildren()) {
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
    public static LinkedList<Structure> getAncestorsOfStructure(Structure searched, Structure position) {
        return getAncestorsOfStructureRecursive(searched, position, null);
    }

    private static LinkedList<Structure> getAncestorsOfStructureRecursive(Structure searched, Structure position,
            Structure parent) {
        if (position.equals(searched)) {
            if (Objects.isNull(parent)) {
                return new LinkedList<>();
            }
            LinkedList<Structure> result = new LinkedList<>();
            result.add(parent);
            return result;

        }
        for (Structure child : position.getChildren()) {
            LinkedList<Structure> maybeFound = getAncestorsOfStructureRecursive(searched, child, position);
            if (!maybeFound.isEmpty()) {
                maybeFound.addFirst(parent);
                return maybeFound;
            }
        }
        return new LinkedList<>();
    }

    public static void moveView(View view, Structure from, Structure to) {
        from.getViews().remove(view);
        to.getViews().add(view);
    }
}
