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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

/**
 * This class contains some methods to handle metadata (semi) automatically.
 */
public class MetadataEditor {
    private static final Logger logger = LogManager.getLogger(MetadataEditor.class);

    /**
     * Separator for specifying an insertion position.
     */
    public static final String INSERTION_POSITION_SEPARATOR = ",";

    /**
     * LOCTYPE used for internal links.
     */
    private static final String INTERNAL_LOCTYPE = "Kitodo.Production";

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
                        .get(Integer.parseInt(indices.get(index)));
            } else {
                addLink(includedStructuralElement, Integer.parseInt(indices.get(index)), childProcessId);
            }
        }
        ServiceManager.getFileService().createBackupFile(process);
        ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFileUri);
    }

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
     * @param childProcessId
     *            Database ID of the child process to be linked
     */
    public static void addLink(IncludedStructuralElement parentIncludedStructuralElement, int childProcessId) {
        addLink(parentIncludedStructuralElement, -1, childProcessId);
    }

    private static void addLink(IncludedStructuralElement parentIncludedStructuralElement, int index,
            int childProcessId) {

        LinkedMetsResource link = new LinkedMetsResource();
        link.setLoctype(INTERNAL_LOCTYPE);
        URI uri = ServiceManager.getProcessService().getProcessURI(childProcessId);
        link.setUri(uri);
        IncludedStructuralElement includedStructuralElement = new IncludedStructuralElement();
        includedStructuralElement.setLink(link);
        List<IncludedStructuralElement> children = parentIncludedStructuralElement.getChildren();
        if (index < 0) {
            children.add(includedStructuralElement);
        } else {
            children.add(index, includedStructuralElement);
        }
    }

    /**
     * Remove link to process with ID 'childProcessId' from workpiece of Process 'parentProcess'.
     *
     * @param parentProcess Process from which link is removed
     * @param childProcessId ID of process whose link will be remove from workpiece of parent process
     * @throws IOException thrown if meta.xml could not be loaded
     */
    public static void removeLink(Process parentProcess, int childProcessId) throws IOException {
        URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(parentProcess);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);
        if (removeLinkRecursive(workpiece.getRootElement(), childProcessId)) {
            ServiceManager.getFileService().createBackupFile(parentProcess);
            ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFileUri);
        } else {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("link") });
        }
    }

    private static boolean removeLinkRecursive(IncludedStructuralElement element, int childId) {
        IncludedStructuralElement parentElement = null;
        IncludedStructuralElement linkElement = null;
        for (IncludedStructuralElement structuralElement : element.getChildren()) {
            if (Objects.nonNull(structuralElement.getLink()) && Objects.nonNull(structuralElement.getLink().getUri())
                    && structuralElement.getLink().getUri().toString().endsWith("process.id=" + childId)) {
                parentElement = element;
                linkElement = structuralElement;
                break;
            } else {
                if (removeLinkRecursive(structuralElement, childId)) {
                    return true;
                }
            }
        }
        // no need to check if 'linkElement' is Null since it is set in the same place as 'parentElement'!
        if (Objects.nonNull(parentElement)) {
            parentElement.getChildren().remove(linkElement);
            return true;
        }
        return false;
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

        for (int i = 0; i < number; i++) {
            IncludedStructuralElement newStructure = addStructure(type, workpiece, structure, position, Collections.emptyList());
            if (Objects.isNull(newStructure)) {
                continue;
            }
            MetadataEntry metadataEntry = new MetadataEntry();
            metadataEntry.setKey(metadataKey);
            metadataEntry.setValue(metadataValue + " " + (number - i));
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
        switch (position) {
            case AFTER_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(structure) + 1, newStructure);
                break;
            case BEFORE_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(structure), newStructure);
                break;
            case CURRENT_POSITION:
                OptionalInt minOrder = viewsToAdd.stream().mapToInt(v -> v.getMediaUnit().getOrder()).min();
                if (minOrder.isPresent()) {
                    int structureOrder = minOrder.getAsInt();
                    // new structure ORDER must be set to same min ORDER value of contained media units
                    newStructure.setOrder(structureOrder);
                    List<Integer> siblingOrderValues = Stream.concat(structure.getChildren().stream()
                            .map(Division::getOrder), Stream.of(structureOrder)).sorted().collect(Collectors.toList());

                    // new order must be set at correction location between existing siblings
                    structure.getChildren().add(siblingOrderValues.indexOf(structureOrder), newStructure);
                }
                break;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                structure.getChildren().add(0, newStructure);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                structure.getChildren().add(newStructure);
                break;
            case PARENT_OF_CURRENT_ELEMENT:
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
        if (Objects.nonNull(viewsToAdd) && !viewsToAdd.isEmpty()) {
            for (View viewToAdd : viewsToAdd) {
                List<IncludedStructuralElement> includedStructuralElements = viewToAdd.getMediaUnit().getIncludedStructuralElements();
                for (IncludedStructuralElement elementToUnassign : includedStructuralElements) {
                    elementToUnassign.getViews().remove(viewToAdd);
                }
                includedStructuralElements.clear();
                includedStructuralElements.add(newStructure);
            }
            newStructure.getViews().addAll(viewsToAdd);
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
     * Determines the path to the included structural element of the child. For
     * each level of the root element, the recursion is run through once, that
     * is for a newspaper year process tree times (year, month, day).
     *
     * @param includedStructuralElement
     *            included structural element of the level stage of recursion
     *            (starting from the top)
     * @param number
     *            number of the record of the process of the child
     *
     */
    public static List<IncludedStructuralElement> determineIncludedStructuralElementPathToChild(
            IncludedStructuralElement includedStructuralElement, int number) {

        if (Objects.nonNull(includedStructuralElement.getLink())) {
            try {
                if (ServiceManager.getProcessService()
                        .processIdFromUri(includedStructuralElement.getLink().getUri()) == number) {
                    LinkedList<IncludedStructuralElement> linkedIncludedStructuralElements = new LinkedList<>();
                    linkedIncludedStructuralElements.add(includedStructuralElement);
                    return linkedIncludedStructuralElements;
                }
            } catch (IllegalArgumentException | ClassCastException | SecurityException e) {
                logger.catching(Level.TRACE, e);
            }
        }
        for (IncludedStructuralElement includedStructuralElementChild : includedStructuralElement.getChildren()) {
            List<IncludedStructuralElement> includedStructuralElementList = determineIncludedStructuralElementPathToChild(
                includedStructuralElementChild, number);
            if (!includedStructuralElementList.isEmpty()) {
                includedStructuralElementList.add(0, includedStructuralElement);
                return includedStructuralElementList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Transforms a {@code Domain} specifying object from the ruleset into an
     * {@code MdSec} specifier for metadata in internal data format. Note that
     * there is no {@code MdSec} for {@code Domain.METS_DIV}; that has to be
     * treated differently.
     *
     * @param domain
     *            domain to transform
     * @return {@code MdSec} is returned
     * @throws IllegalArgumentException
     *             if the {@code Domain} is {@code mets:div}
     */
    public static MdSec domainToMdSec(Domain domain) {
        switch (domain) {
            case DESCRIPTION:
                return MdSec.DMD_SEC;
            case DIGITAL_PROVENANCE:
                return MdSec.DIGIPROV_MD;
            case RIGHTS:
                return MdSec.RIGHTS_MD;
            case SOURCE:
                return MdSec.SOURCE_MD;
            case TECHNICAL:
                return MdSec.TECH_MD;
            default:
                throw new IllegalArgumentException(domain.name());
        }
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

    private static <T extends Division<T>> LinkedList<Division<T>> getAncestorsRecursive(Division<T> searched,
            Division<T> position, Division<T> parent) {

        if (position.equals(searched)) {
            if (Objects.isNull(parent)) {
                return new LinkedList<>();
            }
            LinkedList<Division<T>> ancestors = new LinkedList<>();
            ancestors.add(parent);
            return ancestors;
        }
        for (Division<T> child : position.getChildren()) {
            LinkedList<Division<T>> maybeFound = getAncestorsRecursive(searched, child, position);
            if (!maybeFound.isEmpty()) {
                if (Objects.nonNull(parent)) {
                    maybeFound.addFirst(parent);
                }
                return maybeFound;
            }
        }
        return new LinkedList<>();
    }

    /**
     * Returns the value of the specified metadata entry.
     *
     * @param includedStructuralElement
     *            included structural element from whose metadata the value is
     *            to be retrieved
     * @param key
     *            key of the metadata to be determined
     * @return the value of the metadata entry, otherwise {@code null}
     */
    public static String getMetadataValue(IncludedStructuralElement includedStructuralElement, String key) {
        for (Metadata metadata : includedStructuralElement.getMetadata()) {
            if (metadata.getKey().equals(key) && metadata instanceof MetadataEntry) {
                return ((MetadataEntry) metadata).getValue();
            }
        }
        return null;
    }

    /**
     * Get the first view the given MediaUnit is assigned to.
     * @param mediaUnit MediaUnit to get the view for
     * @return View or null if no View was found
     */
    public static View getFirstViewForMediaUnit(MediaUnit mediaUnit) {
        List<IncludedStructuralElement> includedStructuralElements = mediaUnit.getIncludedStructuralElements();
        if (!includedStructuralElements.isEmpty() && Objects.nonNull(includedStructuralElements.get(0))) {
            for (View view : includedStructuralElements.get(0).getViews()) {
                if (Objects.nonNull(view) && Objects.equals(view.getMediaUnit(), mediaUnit)) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * Reads the simple metadata from an included structural element defined by
     * the simple metadata view interface, including {@code mets:div} metadata.
     *
     * @param division
     *            included structural element from which the metadata should be
     *            read
     * @param simpleMetadataView
     *            simple metadata view interface which formally describes the
     *            methadata to be read
     * @return metadata which corresponds to the formal description
     */
    public static List<String> readSimpleMetadataValues(IncludedStructuralElement division,
            SimpleMetadataViewInterface simpleMetadataView) {
        Domain domain = simpleMetadataView.getDomain().orElse(Domain.DESCRIPTION);
        if (domain.equals(Domain.METS_DIV)) {
            switch (simpleMetadataView.getId().toLowerCase()) {
                case "label":
                    return Collections.singletonList(division.getLabel());
                case "orderlabel":
                    return Collections.singletonList(division.getOrderlabel());
                case "type":
                    return Collections.singletonList(division.getType());
                default:
                    throw new IllegalArgumentException(division.getClass().getSimpleName() + " has no field '"
                            + simpleMetadataView.getId() + "'.");
            }
        } else {
            return division.getMetadata().parallelStream()
                    .filter(metadata -> metadata.getKey().equals(simpleMetadataView.getId()))
                    .filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast).map(MetadataEntry::getValue)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Removes all metadata of the given key from the given included structural
     * element.
     *
     * @param includedStructuralElement
     *            included structural element to remove metadata from
     * @param key
     *            key of metadata to remove
     */
    public static void removeAllMetadata(IncludedStructuralElement includedStructuralElement, String key) {
        includedStructuralElement.getMetadata().removeIf(metadata -> key.equals(metadata.getKey()));
    }

    /**
     * Writes a metadata entry as defined by the ruleset. The ruleset allows the
     * domain {@code mets:div}. If a metadata entry must be written with the
     * {@code mets:div} domain, this must set the internal value on the object
     * model. Otherwise, however, a metadata entry is written in metadata area.
     *
     * @param division
     *            included structural element at which the metadata entry is to
     *            be written
     * @param simpleMetadataView
     *            properties of the metadata entry as defined in the ruleset
     * @param value
     *            worth writing
     * @throws IllegalArgumentException
     *             when trying to write a value to the structure, there is no
     *             field for it. This is when the domain is {@code mets:div},
     *             and the value is different from either {@code label} or
     *             {@code orderlabel}.
     */
    public static void writeMetadataEntry(IncludedStructuralElement division,
            SimpleMetadataViewInterface simpleMetadataView, String value) {

        Domain domain = simpleMetadataView.getDomain().orElse(Domain.DESCRIPTION);
        if (domain.equals(Domain.METS_DIV)) {
            switch (simpleMetadataView.getId().toLowerCase()) {
                case "label":
                    division.setLabel(value);
                    break;
                case "orderlabel":
                    division.setOrderlabel(value);
                    break;
                case "type":
                    throw new IllegalArgumentException(
                            "'" + simpleMetadataView.getId() + "' is reserved for the key ID.");
                default:
                    throw new IllegalArgumentException(division.getClass().getSimpleName() + " has no field '"
                            + simpleMetadataView.getId() + "'.");
            }
        } else {
            MetadataEntry metadata = new MetadataEntry();
            metadata.setKey(simpleMetadataView.getId());
            metadata.setDomain(domainToMdSec(domain));
            metadata.setValue(value);
            division.getMetadata().add(metadata);
        }
    }
}
