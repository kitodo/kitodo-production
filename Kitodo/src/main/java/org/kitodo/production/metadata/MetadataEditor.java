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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
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
     * logical division in a logical division of the parent process. The order
     * is based on the order number specified by the user. This method does not
     * create a link between the two processes in the database, this must and
     * can only happen when saving.
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
        LogicalDivision logicalDivision = workpiece.getLogicalStructure();
        for (int index = 0; index < indices.size(); index++) {
            if (index < indices.size() - 1) {
                logicalDivision = logicalDivision.getChildren().get(Integer.parseInt(indices.get(index)));
            } else {
                addLink(logicalDivision, Integer.parseInt(indices.get(index)), childProcessId);
            }
        }
        ServiceManager.getFileService().createBackupFile(process);
        ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFileUri);
    }

    /**
     * Connects two processes by means of a link. The link is sorted as a linked
     * logical division in a logical division of the parent process. The order
     * is based on the order number specified by the user. This method does not
     * create a link between the two processes in the database, this must and
     * can only happen when saving.
     *
     * @param parentLogicalDivision
     *            document logical division of the parent process in which the
     *            link is to be added
     * @param childProcessId
     *            Database ID of the child process to be linked
     */
    public static void addLink(LogicalDivision parentLogicalDivision, int childProcessId) {
        addLink(parentLogicalDivision, -1, childProcessId);
    }

    private static void addLink(LogicalDivision parentLogicalDivision, int index, int childProcessId) {

        LinkedMetsResource link = new LinkedMetsResource();
        link.setLoctype(INTERNAL_LOCTYPE);
        URI uri = ServiceManager.getProcessService().getProcessURI(childProcessId);
        link.setUri(uri);
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setLink(link);
        List<LogicalDivision> children = parentLogicalDivision.getChildren();
        if (index < 0) {
            children.add(logicalDivision);
        } else {
            children.add(index, logicalDivision);
        }
    }

    /**
     * Remove link to process with ID 'childProcessId' from workpiece of Process
     * 'parentProcess'.
     *
     * @param parentProcess
     *            Process from which link is removed
     * @param childProcessId
     *            ID of process whose link will be remove from workpiece of
     *            parent process
     * @throws IOException
     *             thrown if meta.xml could not be loaded
     */
    public static void removeLink(Process parentProcess, int childProcessId) throws IOException {
        URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(parentProcess);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);
        if (removeLinkRecursive(workpiece.getLogicalStructure(), childProcessId)) {
            ServiceManager.getFileService().createBackupFile(parentProcess);
            ServiceManager.getMetsService().saveWorkpiece(workpiece, metadataFileUri);
        } else {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("link") });
        }
    }

    private static boolean removeLinkRecursive(LogicalDivision element, int childId) {
        LogicalDivision parentElement = null;
        LogicalDivision linkElement = null;
        for (LogicalDivision structuralElement : element.getChildren()) {
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
        // no need to check if 'linkElement' is Null since it is set in the same
        // place as 'parentElement'!
        if (Objects.nonNull(parentElement)) {
            parentElement.getChildren().remove(linkElement);
            return true;
        }
        return false;
    }

    private static void addMultipleStructuresWithMetadataEntry(int number, String type, Workpiece workpiece, LogicalDivision structure,
            InsertionPosition position, String metadataKey, String metadataValue) {
        for (int i = 0; i < number; i++) {
            LogicalDivision newStructure = addLogicalDivision(type, workpiece, structure, position,
                Collections.emptyList());
            if (Objects.isNull(newStructure) || metadataKey.isEmpty()) {
                continue;
            }
            if (!metadataKey.isEmpty()) {
                MetadataEntry metadataEntry = new MetadataEntry();
                metadataEntry.setKey(metadataKey);
                metadataEntry.setValue(metadataValue + " " + (number - i));
                newStructure.getMetadata().add(metadataEntry);
            }
        }
    }

    private static void addMultipleStructuresWithMetadataGroup(int number, String type, Workpiece workpiece, LogicalDivision structure,
            InsertionPosition position, String metadataKey) {

        for (int i = 0; i < number; i++) {
            LogicalDivision newStructure = addLogicalDivision(type, workpiece, structure, position,
                Collections.emptyList());
            if (Objects.isNull(newStructure) || metadataKey == null || metadataKey.isEmpty()) {
                continue;
            }
            MetadataGroup metadataGroup = new MetadataGroup();
            metadataGroup.setKey(metadataKey);
            newStructure.getMetadata().add(metadataGroup);
        }
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
     * @param metadataView
     *            view on the metadata to be added
     * @param metadataValue
     *            value of the first metadata entry
     */
    public static void addMultipleStructuresWithMetadata(int number, String type, Workpiece workpiece, LogicalDivision structure,
            InsertionPosition position, MetadataViewInterface metadataView, String metadataValue) {

        String metadataKey = metadataView.getId();
        
        if (metadataView.isComplex()) {
            addMultipleStructuresWithMetadataGroup(number, type, workpiece, structure, position, metadataKey);
        } else {
            addMultipleStructuresWithMetadataEntry(number, type, workpiece, structure, position, metadataKey, metadataValue);
        }
    }
    
    /**
     * Creates a given number of new structures and inserts them into the
     * workpiece. The insertion position is given relative to an existing
     * structure.
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
     */
    public static void addMultipleStructures(int number, String type, Workpiece workpiece, LogicalDivision structure,
            InsertionPosition position) {
        for (int i = 0; i < number; i++) {
            LogicalDivision newStructure = addLogicalDivision(type, workpiece, structure, position,
                Collections.emptyList());
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
     * @param logicalDivision
     *            structure relative to which the new structure is to be
     *            inserted
     * @param position
     *            relative insertion position
     * @param viewsToAdd
     *            views to be assigned to the structure
     * @return the newly created structure
     */
    public static LogicalDivision addLogicalDivision(String type, Workpiece workpiece, LogicalDivision logicalDivision,
            InsertionPosition position, List<View> viewsToAdd) {
        LinkedList<LogicalDivision> parents = getAncestorsOfLogicalDivision(logicalDivision,
            workpiece.getLogicalStructure());
        List<LogicalDivision> siblings = new LinkedList<>();
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
        LogicalDivision newStructure = new LogicalDivision();
        newStructure.setType(type);

        handlePosition(workpiece, logicalDivision, position, viewsToAdd, parents, siblings, newStructure);

        if (Objects.nonNull(viewsToAdd) && !viewsToAdd.isEmpty()) {
            handleViewsToAdd(viewsToAdd, newStructure);
        }
        return newStructure;
    }

    private static void handlePosition(Workpiece workpiece, LogicalDivision logicalDivision, InsertionPosition position,
                                       List<View> viewsToAdd, LinkedList<LogicalDivision> parents,
                                       List<LogicalDivision> siblings, LogicalDivision newStructure) {
        switch (position) {
            case AFTER_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(logicalDivision) + 1, newStructure);
                break;
            case BEFORE_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(logicalDivision), newStructure);
                break;
            case CURRENT_POSITION:
                OptionalInt minOrder = viewsToAdd.stream().mapToInt(v -> v.getPhysicalDivision().getOrder()).min();
                if (minOrder.isPresent()) {
                    int structureOrder = minOrder.getAsInt();
                    // new structure ORDER must be set to same min ORDER value of contained physical divisions
                    newStructure.setOrder(structureOrder);
                    List<Integer> siblingOrderValues = Stream.concat(logicalDivision.getChildren().stream()
                            .map(Division::getOrder), Stream.of(structureOrder)).sorted().collect(Collectors.toList());

                    // new order must be set at correct location between existing siblings
                    logicalDivision.getChildren().add(siblingOrderValues.lastIndexOf(structureOrder), newStructure);
                }
                break;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                logicalDivision.getChildren().add(0, newStructure);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                logicalDivision.getChildren().add(newStructure);
                break;
            case PARENT_OF_CURRENT_ELEMENT:
                newStructure.getChildren().add(logicalDivision);
                if (parents.isEmpty()) {
                    workpiece.setLogicalStructure(newStructure);
                } else {
                    siblings.set(siblings.indexOf(logicalDivision), newStructure);
                }
                break;
            default:
                throw new IllegalStateException("complete switch");
        }
    }

    private static void handleViewsToAdd(List<View> viewsToAdd, LogicalDivision newStructure) {
        for (View viewToAdd : viewsToAdd) {
            List<LogicalDivision> logicalDivisions = viewToAdd.getPhysicalDivision().getLogicalDivisions();
            for (LogicalDivision elementToUnassign : logicalDivisions) {
                elementToUnassign.getViews().remove(viewToAdd);
            }
            logicalDivisions.clear();
            logicalDivisions.add(newStructure);
        }
        newStructure.getViews().addAll(viewsToAdd);
    }

    /**
     * Create a new PhysicalDivision and insert it into the passed workpiece. The position of insertion
     * is determined by the passed parent and position.
     * @param type type of new PhysicalDivision
     * @param workpiece workpiece from which the root physical division is retrieved
     * @param parent parent of the new PhysicalDivision
     * @param position position relative to the parent element
     */
    public static PhysicalDivision addPhysicalDivision(String type, Workpiece workpiece, PhysicalDivision parent,
            InsertionPosition position) {
        LinkedList<PhysicalDivision> grandparents = getAncestorsOfPhysicalDivision(parent, workpiece.getPhysicalStructure());
        List<PhysicalDivision> siblings = new LinkedList<>();
        if (grandparents.isEmpty()) {
            if (position.equals(InsertionPosition.AFTER_CURRENT_ELEMENT)
                    || position.equals(InsertionPosition.BEFORE_CURRENT_ELEMENT)) {
                Helper.setErrorMessage("No parent found for currently selected physical "
                        + "division to which new physical division can be appended!");
            }
        } else {
            siblings = grandparents.getLast().getChildren();
        }
        PhysicalDivision newPhysicalDivision = new PhysicalDivision();
        newPhysicalDivision.setType(type);
        switch (position) {
            case AFTER_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(parent) + 1, newPhysicalDivision);
                break;
            case BEFORE_CURRENT_ELEMENT:
                siblings.add(siblings.indexOf(parent), newPhysicalDivision);
                break;
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                parent.getChildren().add(0, newPhysicalDivision);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                parent.getChildren().add(newPhysicalDivision);
                break;
            default:
                throw new IllegalStateException("Used InsertionPosition not allowed.");
        }
        return newPhysicalDivision;
    }

    /**
     * Assigns all views of all children to the specified included structural
     * element.
     *
     * @param structure
     *            structure to add all views of all children to
     */
    public static void assignViewsFromChildren(LogicalDivision structure) {
        Collection<View> viewsToAdd = getViewsFromChildrenRecursive(structure);
        Collection<View> assignedViews = structure.getViews();
        viewsToAdd.removeAll(assignedViews);
        List<View> sortedViews = Stream.concat(assignedViews.stream(), viewsToAdd.stream())
                .sorted(Comparator.comparing(view -> view.getPhysicalDivision().getOrder()))
                .collect(Collectors.toList());
        assignedViews.clear();
        assignedViews.addAll(sortedViews);
    }

    private static Collection<View> getViewsFromChildrenRecursive(LogicalDivision structure) {
        Set<View> viewsFromChildren = new LinkedHashSet<>(structure.getViews());
        for (LogicalDivision child : structure.getChildren()) {
            viewsFromChildren.addAll(getViewsFromChildrenRecursive(child));
        }
        return viewsFromChildren;
    }

    /**
     * Creates a view on a physical division that is not further restricted; that is,
     * the entire physical division is displayed.
     *
     * @param physicalDivision
     *            physical division on which a view is to be formed
     * @return the created physical division
     */
    public static View createUnrestrictedViewOn(PhysicalDivision physicalDivision) {
        View unrestrictedView = new View();
        unrestrictedView.setPhysicalDivision(physicalDivision);
        return unrestrictedView;
    }

    /**
     * Determines the path to the logical division of the child. For each level
     * of the logical structure, the recursion is run through once, that is for
     * a newspaper year process tree times (year, month, day).
     *
     * @param logicalDivision
     *            logical division of the level stage of recursion (starting
     *            from the top)
     * @param number
     *            number of the record of the process of the child
     *
     */
    public static List<LogicalDivision> determineLogicalDivisionPathToChild(
            LogicalDivision logicalDivision, int number) {

        if (Objects.nonNull(logicalDivision.getLink())) {
            try {
                if (ServiceManager.getProcessService()
                        .processIdFromUri(logicalDivision.getLink().getUri()) == number) {
                    LinkedList<LogicalDivision> linkedLogicalDivisions = new LinkedList<>();
                    linkedLogicalDivisions.add(logicalDivision);
                    return linkedLogicalDivisions;
                }
            } catch (IllegalArgumentException | ClassCastException | SecurityException e) {
                logger.catching(Level.TRACE, e);
            }
        }
        for (LogicalDivision logicalDivisionChild : logicalDivision.getChildren()) {
            List<LogicalDivision> logicalDivisionList = determineLogicalDivisionPathToChild(
                logicalDivisionChild, number);
            if (!logicalDivisionList.isEmpty()) {
                logicalDivisionList.add(0, logicalDivision);
                return logicalDivisionList;
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
    public static LinkedList<LogicalDivision> getAncestorsOfLogicalDivision(LogicalDivision searched,
                                                                                LogicalDivision position) {
        return getAncestorsRecursive(searched, position, null)
                .stream()
                .map(parent -> (LogicalDivision) parent)
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
    public static LinkedList<PhysicalDivision> getAncestorsOfPhysicalDivision(PhysicalDivision searched, PhysicalDivision position) {
        return getAncestorsRecursive(searched, position, null)
                .stream()
                .map(parent -> (PhysicalDivision) parent)
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
     * @param logicalDivision
     *            logical division from whose metadata the value is
     *            to be retrieved
     * @param key
     *            key of the metadata to be determined
     * @return the value of the metadata entry, otherwise {@code null}
     */
    public static String getMetadataValue(LogicalDivision logicalDivision, String key) {
        for (Metadata metadata : logicalDivision.getMetadata()) {
            if (metadata.getKey().equals(key) && metadata instanceof MetadataEntry) {
                return ((MetadataEntry) metadata).getValue();
            }
        }
        return null;
    }

    /**
     * Get the first view the given PhysicalDivision is assigned to.
     * @param physicalDivision PhysicalDivision to get the view for
     * @return View or null if no View was found
     */
    public static View getFirstViewForPhysicalDivision(PhysicalDivision physicalDivision) {
        List<LogicalDivision> logicalDivisions = physicalDivision.getLogicalDivisions();
        if (!logicalDivisions.isEmpty() && Objects.nonNull(logicalDivisions.get(0))) {
            for (View view : logicalDivisions.get(0).getViews()) {
                if (Objects.nonNull(view) && Objects.equals(view.getPhysicalDivision(), physicalDivision)) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * Reads the simple metadata from a logical division defined by
     * the simple metadata view interface, including {@code mets:div} metadata.
     *
     * @param division
     *            logical division from which the metadata should be
     *            read
     * @param simpleMetadataView
     *            simple metadata view interface which formally describes the
     *            methadata to be read
     * @return metadata which corresponds to the formal description
     */
    public static List<String> readSimpleMetadataValues(LogicalDivision division,
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
     * @param logicalDivision
     *            logical division to remove metadata from
     * @param key
     *            key of metadata to remove
     */
    public static void removeAllMetadata(LogicalDivision logicalDivision, String key) {
        logicalDivision.getMetadata().removeIf(metadata -> key.equals(metadata.getKey()));
    }

    /**
     * Writes a metadata entry as defined by the ruleset. The ruleset allows the
     * domain {@code mets:div}. If a metadata entry must be written with the
     * {@code mets:div} domain, this must set the internal value on the object
     * model. Otherwise, however, a metadata entry is written in metadata area.
     *
     * @param division
     *            logical division at which the metadata entry is to
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
    public static void writeMetadataEntry(Division<?> division,
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
