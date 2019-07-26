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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Parent;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.pagination.Paginator;
import org.kitodo.production.services.ServiceManager;

/**
 * This class contains some methods to handle metadata (semi) automatically.
 */
public class MetadataEditor {
    /**
     * Separator for specifying an insertion position.
     */
    public static final String INSERTION_POSITION_SEPARATOR = ",";

    /**
     * LOCTYPE used for internal links.
     */
    public static final String INTERNAL_LOCTYPE = "Kitodo.Production";

    /**
     * Connects two processes by means of a link. The link is sorted as a linked
     * included structural element in a included structural element of the
     * parent process. The order is based on the order number specified by the
     * user. This method does not create a link between the two processes in the
     * database, this must and can only happen when saving.
     *
     * @param parentIncludedStructuralElement
     *            document included structural element of the parent process in
     *            which the link is to be added
     * @param order
     *            at which point the link is to be ordered. The value is
     *            interpreted relative to the order values of other links in the
     *            included structural element.
     * @param childProcessId
     *            Database ID of the child process to be linked
     */
    public static void addLink(IncludedStructuralElement parentIncludedStructuralElement, BigInteger order,
            int childProcessId) {

        LinkedMetsResource link = new LinkedMetsResource();
        link.setLoctype(INTERNAL_LOCTYPE);
        link.setOrder(order);
        URI uri = ServiceManager.getProcessService().getProcessURI(childProcessId);
        link.setUri(uri);
        IncludedStructuralElement includedStructuralElement = new IncludedStructuralElement();
        includedStructuralElement.setLink(link);

        List<IncludedStructuralElement> children = parentIncludedStructuralElement.getChildren();
        int position = getPositionForOrder(children, order);

        children.add(position, includedStructuralElement);
    }

    /**
     * Connects two processes by means of a link. The link is sorted as a linked
     * included structural element in a included structural element of the
     * parent process. The order is based on the order number specified by the
     * user. This method does not create a link between the two processes in the
     * database, this must and can only happen when saving.
     *
     * @param process
     *            the parent process in which the link is to be added
     * @param insertionPosition
     *            at which point the link is to be inserted
     * @param childProcessId
     *            Database ID of the child process to be linked
     * @throws IOException
     *             if the METS file cannot be read or written
     */
    public static void addLink(Process process, String insertionPosition, int childProcessId) throws IOException {
        URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(process);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);
        List<String> indices = Arrays.asList(insertionPosition.split(Pattern.quote(INSERTION_POSITION_SEPARATOR)));
        IncludedStructuralElement includedStructuralElement = workpiece.getRootElement();
        for (int index = 0; index < indices.size(); index++) {
            if (index < indices.size() - 1) {
                includedStructuralElement = includedStructuralElement.getChildren()
                        .get(Integer.valueOf(indices.get(index)));
            } else {
                LinkedMetsResource link = new LinkedMetsResource();
                link.setLoctype(INTERNAL_LOCTYPE);
                URI childProcessUri = ServiceManager.getProcessService().getProcessURI(childProcessId);
                link.setUri(childProcessUri);
                IncludedStructuralElement includedStructuralElementToAdd = new IncludedStructuralElement();
                includedStructuralElementToAdd.setLink(link);
                includedStructuralElement.getChildren().add(Integer.valueOf(indices.get(index)),
                    includedStructuralElementToAdd);
            }
        }
        ServiceManager.getFileService().createBackupFile(process);
        ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFileUri);
    }

    private static int getPositionForOrder(List<IncludedStructuralElement> children, BigInteger order) {
        for (int i = 0; i < children.size(); i++) {
            LinkedMetsResource otherLink = children.get(i).getLink();
            if (Objects.nonNull(otherLink) && Objects.nonNull(otherLink.getOrder())
                    && (Objects.isNull(order) || otherLink.getOrder().compareTo(order) > 0)) {
                return i;
            }
        }
        return children.size();
    }

    /**
     * Creates a given number of new structures and inserts them into the
     * workpiece. The insertion position is given relative to an existing
     * structure. In addition, you can specify metadata, which is assigned to
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
     *            key of the metadata to create
     * @param metadataValue
     *            value of the first metadata entry
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
        LinkedList<IncludedStructuralElement> parents = getAncestorsOfStructure(structure, workpiece.getRootElement());
        List<IncludedStructuralElement> siblings = new LinkedList<>();
        if (parents.isEmpty()) {
            if (position.equals(InsertionPosition.AFTER_CURRENT_ELEMENT)
                    || position.equals(InsertionPosition.BEFORE_CURRENT_ELEMENT)
                    || position.equals(InsertionPosition.PARENT_OF_CURRENT_ELEMENT)) {
                Helper.setErrorMessage("No parent found for currently selected structure to which new structure can be appended!");
                return null;
            }
        } else {
            siblings = parents.getLast().getChildren();
        }
        IncludedStructuralElement newStructure = new IncludedStructuralElement();
        newStructure.setType(type);
        LinkedList<IncludedStructuralElement> structuresToAddViews = new LinkedList<>(parents);
        switch (position) {
            case AFTER_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(structure) + 1, newStructure);
                break;
            case BEFORE_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(structure), newStructure);
                break;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                structuresToAddViews.add(structure);
                structure.getChildren().add(0, newStructure);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                structuresToAddViews.add(structure);
                structure.getChildren().add(newStructure);
                break;
            case PARENT_OF_CURRENT_ELEMENT:
                structuresToAddViews.removeLast();
                newStructure.getChildren().add(structure);
                if (parents.isEmpty()) {
                    workpiece.setRootElement(newStructure);
                } else {
                    siblings.set(siblings.indexOf(structure), newStructure);
                }
                break;
            default:
                throw new IllegalStateException("complete switch");
        }
        for (IncludedStructuralElement structureToAddViews : structuresToAddViews) {
            structureToAddViews.getViews().addAll(viewsToAdd);
        }
        return newStructure;
    }

    /**
     * Create a new MediaUnit and insert it into the passed workpiece. The position of insertion
     * is determined by the passed parent and position.
     * @param type type of new MediaUnit
     * @param workpiece workpiece from which the root media unit is retrieved
     * @param parent parent of the new MediaUnit
     * @param position position relative to the parent element
     */
    public static MediaUnit addMediaUnit(String type, Workpiece workpiece, MediaUnit parent, InsertionPosition position) {
        LinkedList<MediaUnit> grandparents = getAncestorsOfMediaUnit(parent, workpiece.getMediaUnit());
        List<MediaUnit> siblings = new LinkedList<>();
        if (grandparents.isEmpty()) {
            if (position.equals(InsertionPosition.AFTER_CURRENT_ELEMENT)
                    || position.equals(InsertionPosition.BEFORE_CURRENT_ELEMENT)) {
                Helper.setErrorMessage("No parent found for currently selected media unit to which new media unit can be appended!");
            }
        } else {
            siblings = grandparents.getLast().getChildren();
        }
        MediaUnit newMediaUnit = new MediaUnit();
        newMediaUnit.setType(type);
        switch (position) {
            case AFTER_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(parent) + 1, newMediaUnit);
                break;
            case BEFORE_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(parent), newMediaUnit);
                break;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                parent.getChildren().add(0, newMediaUnit);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                parent.getChildren().add(newMediaUnit);
                break;
            default:
                throw new IllegalStateException("Used InsertionPosition not allowed.");
        }
        return newMediaUnit;
    }

    /**
     * Assigns all views of all children to the specified included structural
     * element.
     *
     * @param structure
     *            structure to add all views of all children to
     */
    public static void assignViewsFromChildren(IncludedStructuralElement structure) {
        Collection<View> viewsToAdd = getViewsFromChildrenRecursive(structure);
        Collection<View> assignedViews = structure.getViews();
        viewsToAdd.removeAll(assignedViews);
        assignedViews.addAll(viewsToAdd);
    }

    private static Collection<View> getViewsFromChildrenRecursive(IncludedStructuralElement structure) {
        List<View> viewsFromChildren = new ArrayList<>(structure.getViews());
        for (IncludedStructuralElement child : structure.getChildren()) {
            viewsFromChildren.addAll(getViewsFromChildrenRecursive(child));
        }
        return viewsFromChildren;
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
        View unrestrictedView = new View();
        unrestrictedView.setMediaUnit(mediaUnit);
        return unrestrictedView;
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
        return getAncestorsRecursive(searched, position, null)
                .stream()
                .map(parent -> (IncludedStructuralElement) parent)
                .collect(Collectors.toCollection(LinkedList::new));
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
    public static LinkedList<MediaUnit> getAncestorsOfMediaUnit(MediaUnit searched, MediaUnit position) {
        return getAncestorsRecursive(searched, position, null)
                .stream()
                .map(parent -> (MediaUnit) parent)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static <T> LinkedList<Parent<T>> getAncestorsRecursive(Parent<T> searched, Parent<T> position, Parent<T> parent) {
        if (position.equals(searched)) {
            if (Objects.isNull(parent)) {
                return new LinkedList<>();
            }
            LinkedList<Parent<T>> ancestors = new LinkedList<>();
            ancestors.add(parent);
            return ancestors;

        }
        for (T child : position.getChildren()) {
            LinkedList<Parent<T>> maybeFound = getAncestorsRecursive(searched, (Parent<T>)child, position);
            if (!maybeFound.isEmpty()) {
                if (Objects.nonNull(parent)) {
                    maybeFound.addFirst(parent);
                }
                return maybeFound;
            }
        }
        return new LinkedList<>();
    }
}
