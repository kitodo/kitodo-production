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

package org.kitodo.production.forms.dataeditor;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class StructurePanel implements Serializable {
    private static final Logger logger = LogManager.getLogger(StructurePanel.class);

    private DataEditorForm dataEditor;

    /**
     * If changing the tree node fails, we need this value to undo the user’s
     * select action.
     */
    private TreeNode previouslySelectedLogicalNode;

    /**
     * If changing the tree node fails, we need this value to undo the user’s
     * select action.
     */
    private TreeNode previouslySelectedPhysicalNode;

    private TreeNode selectedLogicalNode;

    private TreeNode selectedPhysicalNode;

    private IncludedStructuralElement structure;

    /**
     * The logical structure tree of the edited document.
     */
    private DefaultTreeNode logicalTree = null;

    /**
     * The physical structure tree of the edited document.
     */
    private DefaultTreeNode physicalTree = null;

    /**
     * HashMap containing the current expansion states of all TreeNodes in the logical structure tree.
     */
    private HashMap<IncludedStructuralElement, Boolean> previousExpansionStatesLogicalTree;

    /**
     * HashMap containing the current expansion states of all TreeNodes in the physical structure tree.
     */
    private HashMap<IncludedStructuralElement, Boolean> previousExpansionStatesPhysicalTree;

    /**
     * List of all mediaUnits assigned to multiple IncludedStructuralElements.
     */
    private List<MediaUnit> severalAssignments = new LinkedList<>();

    /**
     * Creates a new structure panel.
     *
     * @param dataEditor
     *            the master metadata editor
     */
    StructurePanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Clear content.
     */
    public void clear() {
        logicalTree = null;
        physicalTree = null;
        selectedLogicalNode = null;
        selectedPhysicalNode = null;
        previouslySelectedLogicalNode = null;
        previouslySelectedPhysicalNode = null;
        structure = null;
        severalAssignments = new LinkedList<>();
    }

    void deleteSelectedStructure() {
        Optional<IncludedStructuralElement> selectedStructure = getSelectedStructure();
        if (!selectedStructure.isPresent()) {
            /*
             * No element is selected or the selected element is not a structure
             * but, for example, a media unit.
             */
            return;
        }
        LinkedList<IncludedStructuralElement> ancestors = MetadataEditor.getAncestorsOfStructure(selectedStructure.get(), structure);
        if (ancestors.isEmpty()) {
            // The selected element is the root node of the tree.
            return;
        }
        IncludedStructuralElement parent = ancestors.getLast();

        Collection<View> subViews = new ArrayList<>();
        subViews = getAllSubViews(selectedStructure.get(), subViews);
        parent.getViews().addAll(subViews);

        parent.getChildren().remove(selectedStructure.get());
        show();
        dataEditor.getGalleryPanel().updateStripes();
    }

    private Collection<View> getAllSubViews(IncludedStructuralElement selectedStructure, Collection<View> views) {
        if (Objects.nonNull(selectedStructure.getViews())) {
            views.addAll(selectedStructure.getViews());
        }
        for (IncludedStructuralElement child : selectedStructure.getChildren()) {
            getAllSubViews(child, views);
        }
        return views;
    }

    void deleteSelectedMediaUnit() {
        Optional<MediaUnit> selectedMediaUnit = getSelectedMediaUnit();
        if (!selectedMediaUnit.isPresent()) {
            return;
        }
        LinkedList<MediaUnit> ancestors = MetadataEditor.getAncestorsOfMediaUnit(selectedMediaUnit.get(),
                dataEditor.getWorkpiece().getMediaUnit());
        if (ancestors.isEmpty()) {
            // The selected element is the root node of the tree.
            return;
        }
        MediaUnit parent = ancestors.getLast();
        parent.getChildren().remove(selectedMediaUnit.get());
        show();
    }

    /**
     * Get selected logical TreeNode.
     *
     * @return value of selectedLogicalNode
     */
    public TreeNode getSelectedLogicalNode() {
        return selectedLogicalNode;
    }

    /**
     * Set selected logical TreeNode.
     *
     * @param selected
     *          TreeNode that will be selected
     */
    public void setSelectedLogicalNode(TreeNode selected) {
        if (Objects.nonNull(selected)) {
            this.selectedLogicalNode = selected;
            expandNode(selected.getParent());
        }
    }

    /**
     * Get selectedPhysicalNode.
     *
     * @return value of selectedPhysicalNode
     */
    public TreeNode getSelectedPhysicalNode() {
        return selectedPhysicalNode;
    }

    /**
     * Set selectedPhysicalNode.
     *
     * @param selectedPhysicalNode as org.primefaces.model.TreeNode
     */
    public void setSelectedPhysicalNode(TreeNode selectedPhysicalNode) {
        if (Objects.nonNull(selectedPhysicalNode)) {
            this.selectedPhysicalNode = selectedPhysicalNode;
            expandNode(selectedPhysicalNode.getParent());
        }
    }

    Optional<IncludedStructuralElement> getSelectedStructure() {
        StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
        Object dataObject = structureTreeNode.getDataObject();
        return Optional.ofNullable(dataObject instanceof IncludedStructuralElement ? (IncludedStructuralElement) dataObject : null);
    }

    Optional<MediaUnit> getSelectedMediaUnit() {
        StructureTreeNode structureTreeNode = (StructureTreeNode) selectedPhysicalNode.getData();
        Object dataObject = structureTreeNode.getDataObject();
        return Optional.ofNullable(dataObject instanceof MediaUnit ? (MediaUnit) dataObject : null);
    }

    /**
     * Select given MediaUnit in physical structure tree.
     *
     * @param mediaUnit
     *          MediaUnit to be selected in physical structure tree
     */
    void selectMediaUnit(MediaUnit mediaUnit) {
        TreeNode matchingTreeNode = getMatchingTreeNode(getPhysicalTree(), mediaUnit);
        if (Objects.nonNull(matchingTreeNode)) {
            updatePhysicalNodeSelection(matchingTreeNode);
            matchingTreeNode.setSelected(true);
        }
    }

    private TreeNode getMatchingTreeNode(TreeNode parent, MediaUnit mediaUnit) {
        TreeNode matchingTreeNode = null;
        for (TreeNode treeNode : parent.getChildren()) {
            if (Objects.nonNull(treeNode) && treeNode.getData() instanceof StructureTreeNode) {
                StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
                if (structureTreeNode.getDataObject() instanceof MediaUnit) {
                    MediaUnit currentMediaUnit = (MediaUnit) structureTreeNode.getDataObject();
                    if (Objects.nonNull(currentMediaUnit.getDivId())
                            && currentMediaUnit.getDivId().equals(mediaUnit.getDivId())) {
                        matchingTreeNode = treeNode;
                        break;
                    } else {
                        matchingTreeNode = getMatchingTreeNode(treeNode, mediaUnit);
                        if (Objects.nonNull(matchingTreeNode)) {
                            break;
                        }
                    }
                }
            }
        }
        return matchingTreeNode;
    }

    /**
     * Get logicalTree.
     *
     * @return value of logicalTree
     */
    public DefaultTreeNode getLogicalTree() {
        return this.logicalTree;
    }

    /**
     * Get physicalTree.
     *
     * @return value of physicalTree
     */
    public DefaultTreeNode getPhysicalTree() {
        return physicalTree;
    }

    void preserve() {
        this.preserveLogical();
        this.preservePhysical();
    }

    /**
     * Updates the live structure of the workpiece with the current members of
     * the structure tree in their given order. The live structure of the
     * workpiece which is stored in the root element of the structure tree.
     */
    private void preserveLogical() {
        if (!this.logicalTree.getChildren().isEmpty()) {
            preserveLogicalRecursive(this.logicalTree.getChildren().get(0));
        }
    }

    /**
     * Updates the live structure of a structure tree node and returns it, to
     * provide for updating the parent. If the tree node contains children which
     * aren’t structures, {@code null} is returned to skip them on the level
     * above.
     */
    private static IncludedStructuralElement preserveLogicalRecursive(TreeNode treeNode) {
        StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
        if (Objects.isNull(structureTreeNode) || !(structureTreeNode.getDataObject() instanceof IncludedStructuralElement)) {
            return null;
        }
        IncludedStructuralElement structure = (IncludedStructuralElement) structureTreeNode.getDataObject();

        List<IncludedStructuralElement> childrenLive = structure.getChildren();
        childrenLive.clear();
        for (TreeNode child : treeNode.getChildren()) {
            IncludedStructuralElement maybeChildStructure = preserveLogicalRecursive(child);
            if (Objects.nonNull(maybeChildStructure)) {
                childrenLive.add(maybeChildStructure);
            }
        }
        return structure;
    }

    private void preservePhysical() {
        if (!physicalTree.getChildren().isEmpty()) {
            preservePhysicalRecursive(physicalTree.getChildren().get(0));
        }
    }

    private static MediaUnit preservePhysicalRecursive(TreeNode treeNode) {
        StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
        if (Objects.isNull(structureTreeNode) || !(structureTreeNode.getDataObject() instanceof MediaUnit)) {
            return null;
        }
        MediaUnit mediaUnit = (MediaUnit) structureTreeNode.getDataObject();

        List<MediaUnit> childrenLive = mediaUnit.getChildren();
        childrenLive.clear();
        for (TreeNode child : treeNode.getChildren()) {
            MediaUnit possibleChildMediaUnit = preservePhysicalRecursive(child);
            if (Objects.nonNull(possibleChildMediaUnit)) {
                childrenLive.add(possibleChildMediaUnit);
            }
        }
        return mediaUnit;
    }

    /**
     * Loads the tree(s) into the panel and sets the selected element to the
     * root element of the structure tree.
     *
     * @param keepSelection
     *            if true, keeps the currently selected node(s)
     */
    public void show(boolean keepSelection) {
        if (keepSelection) {
            TreeNode keepSelectedLogicalNode = selectedLogicalNode;
            TreeNode keepSelectedPhysicalNode = selectedPhysicalNode;
            show();
            selectedLogicalNode = keepSelectedLogicalNode;
            selectedPhysicalNode = keepSelectedPhysicalNode;
        } else {
            show();
        }
    }

    /**
     * Loads the tree(s) into the panel and sets the selected element to the
     * root element of the structure tree.
     */
    public void show() {
        this.structure = dataEditor.getWorkpiece().getRootElement();
        Pair<LinkedList<DefaultTreeNode>, Collection<View>> result = buildStructureTree();

        this.previousExpansionStatesLogicalTree = getTreeNodeExpansionStates(this.logicalTree);
        this.logicalTree = result.getLeft().getLast();
        updateNodeExpansionStates(this.logicalTree, this.previousExpansionStatesLogicalTree);

        this.previousExpansionStatesPhysicalTree = getTreeNodeExpansionStates(this.getPhysicalTree());
        this.physicalTree = buildMediaTree(dataEditor.getWorkpiece().getMediaUnit());
        updateNodeExpansionStates(this.getPhysicalTree(), this.previousExpansionStatesPhysicalTree);

        this.selectedLogicalNode = logicalTree.getChildren().get(0);
        this.selectedPhysicalNode = physicalTree.getChildren().get(0);
        this.previouslySelectedLogicalNode = selectedLogicalNode;
        this.previouslySelectedPhysicalNode = selectedPhysicalNode;
    }

    /**
     * Creates the structure tree. If hierarchical links exist upwards, they are
     * displayed above the tree as separate trees.
     *
     * @return the structure tree(s) and the collection of views displayed in
     *         the tree
     */
    private Pair<LinkedList<DefaultTreeNode>, Collection<View>> buildStructureTree() {
        LinkedList<DefaultTreeNode> result = new LinkedList<>();

        DefaultTreeNode main = new DefaultTreeNode();
        if (nodeStateUnknown(this.previousExpansionStatesLogicalTree, main)) {
            main.setExpanded(true);
        }
        Collection<View> viewsShowingOnAChild = buildStructureTreeRecursively(structure, main);
        result.add(main);
        addParentLinksRecursive(dataEditor.getProcess(), result);
        return Pair.of(result, viewsShowingOnAChild);
    }

    private Collection<View> buildStructureTreeRecursively(IncludedStructuralElement structure, TreeNode result) {
        StructureTreeNode node;
        if (Objects.isNull(structure.getLink())) {
            StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            node = new StructureTreeNode(divisionView.getLabel(), divisionView.isUndefined(), false, structure);
        } else {
            node = new StructureTreeNode(structure.getLink().getUri().toString(), true, true, structure);
            for (Process child : dataEditor.getCurrentChildren()) {
                try {
                    String type = ServiceManager.getProcessService().getBaseType(child);
                    if (child.getId() == ServiceManager.getProcessService()
                            .processIdFromUri(structure.getLink().getUri())) {
                        StructuralElementViewInterface view = dataEditor.getRuleset().getStructuralElementView(
                            type, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                        node = new StructureTreeNode(view.getLabel(), view.isUndefined(), true, structure);
                    }
                } catch (IOException e) {
                    Helper.setErrorMessage("metadataReadError", e.getMessage(), logger, e);
                    node = new StructureTreeNode(child.getTitle(), true, true, child);
                }
            }
        }
        /*
         * Creating the tree node by handing over the parent node automatically
         * appends it to the parent as a child. That’s the logic of the JSF
         * framework. So you do not have to add the result anywhere.
         */
        DefaultTreeNode parent = new DefaultTreeNode(node, result);
        if (nodeStateUnknown(this.previousExpansionStatesLogicalTree, parent)) {
            parent.setExpanded(true);
        }

        Set<View> viewsShowingOnAChild = new HashSet<>();
        for (IncludedStructuralElement child : structure.getChildren()) {
            viewsShowingOnAChild.addAll(buildStructureTreeRecursively(child, parent));
        }

        if (Boolean.FALSE.equals(this.isSeparateMedia())) {
            String page = Helper.getTranslation("page").concat(" ");
            for (View view : structure.getViews()) {
                if (!viewsShowingOnAChild.contains(view) && Objects.nonNull(view.getMediaUnit())) {
                    if (Objects.nonNull(view.getMediaUnit().getOrderlabel())) {
                        addTreeNode(page.concat(view.getMediaUnit().getOrderlabel()), false, false, view, parent);
                    } else {
                        addTreeNode(page, false, false, view, parent);
                    }

                    viewsShowingOnAChild.add(view);
                }
            }
        }
        return viewsShowingOnAChild;
    }

    /**
     * Adds a tree node to the given parent node. The tree node is set to
     * ‘expanded’.
     *
     * @param type
     *            the internal name of the type of node, to be resolved through
     *            the rule set
     * @param linked
     *            whether the node is a link. If so, it will be marked with a
     *            link symbol.
     * @param dataObject
     *            the internal object represented by the node
     * @param parent
     *            parent node to which the new node is to be added
     * @return the generated node so that you can add children to it
     */
    private DefaultTreeNode addTreeNode(String type, boolean linked, Object dataObject, DefaultTreeNode parent) {
        StructuralElementViewInterface structuralElementView = dataEditor.getRuleset().getStructuralElementView(type,
            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        return addTreeNode(structuralElementView.getLabel(), structuralElementView.isUndefined(), linked, dataObject,
            parent);
    }

    /**
     * Adds a tree node to the given parent node. The tree node is set to
     * ‘expanded’.
     *
     * @param label
     *            Label of the tree node displayed to the user
     * @param undefined
     *            whether the given type in the rule set is undefined. If so,
     *            the node is highlighted in color and marked with a warning
     *            symbol.
     * @param linked
     *            whether the node is a link. If so, it will be marked with a
     *            link symbol.
     * @param dataObject
     *            the internal object represented by the node
     * @param parent
     *            parent node to which the new node is to be added
     * @return the generated node so that you can add children to it
     */
    private DefaultTreeNode addTreeNode(String label, boolean undefined, boolean linked, Object dataObject,
            DefaultTreeNode parent) {
        DefaultTreeNode node = new DefaultTreeNode(new StructureTreeNode(label, undefined, linked, dataObject),
                parent);
        if (dataObject instanceof MediaUnit && nodeStateUnknown(this.previousExpansionStatesPhysicalTree, node)
                || dataObject instanceof IncludedStructuralElement
                && nodeStateUnknown(this.previousExpansionStatesLogicalTree, node)) {
            node.setExpanded(true);
        }
        return node;
    }

    /**
     * Recursively adds the parent processes in the display. For each parent
     * process, the recursion is run through once, that is for a newspaper issue
     * twice (annual process, overall process). If this fails (child is not
     * found in the parent process, or I/O error), instead only a link is added
     * to the process and the warning sign is activated.
     *
     * @param child
     *            child process, calling recursion
     * @param result
     *            list of structure trees, in this list the parent links are
     *            inserted on top, therefore LinkedList
     */
    private void addParentLinksRecursive(Process child, LinkedList<DefaultTreeNode> result) {
        Process parent = child.getParent();
        // Termination condition of recursion, if the process has no parent
        if (Objects.isNull(parent)) {
            return;
        }
        URI uri = ServiceManager.getProcessService().getMetadataFileUri(parent);
        DefaultTreeNode tree = new DefaultTreeNode();
        if (nodeStateUnknown(this.previousExpansionStatesLogicalTree, tree)) {
            tree.setExpanded(true);
        }
        try {
            IncludedStructuralElement rootElement = ServiceManager.getMetsService().loadWorkpiece(uri).getRootElement();
            List<IncludedStructuralElement> includedStructuralElementList
                    = determineIncludedStructuralElementPathToChildRecursive(rootElement, child.getId());
            DefaultTreeNode parentNode = tree;
            if (includedStructuralElementList.isEmpty()) {
                /*
                 * Error case: The child is not linked in the parent process.
                 * Show the process title of the parent process and a warning
                 * sign.
                 */
                addTreeNode(parent.getTitle(), true, true, parent, tree);
            } else {
                /*
                 * Default case: Show the path through the parent process to the
                 * linked child
                 */
                for (IncludedStructuralElement includedStructuralElement : includedStructuralElementList) {
                    if (Objects.isNull(includedStructuralElement.getType())) {
                        break;
                    } else {
                        parentNode = addTreeNode(includedStructuralElement.getType(), true, null, parentNode);
                    }
                }
            }
        } catch (IOException e) {
            /*
             * Error case: The metadata file of the parent process cannot be
             * loaded. Show the process title of the parent process and the
             * warning sign.
             */
            Helper.setErrorMessage("metadataReadError", e.getMessage(), logger, e);
            addTreeNode(parent.getTitle(), true, true, parent, tree);
        }
        // Insert the link representation above the existing tree.
        result.addFirst(tree);
        // Process parent link of the parent recursively.
        addParentLinksRecursive(parent, result);
    }

    /**
     * Recursively determines the path to the included structural element of the
     * child. For each level of the root element, the recursion is run through
     * once, that is for a newspaper year process tree times (year, month, day).
     *
     * @param includedStructuralElement
     *            included structural element of the level stage of recursion
     *            (starting from the top)
     * @param number
     *            number of the record of the process of the child
     *
     */
    private static List<IncludedStructuralElement> determineIncludedStructuralElementPathToChildRecursive(
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
            List<IncludedStructuralElement> includedStructuralElementList = determineIncludedStructuralElementPathToChildRecursive(
                includedStructuralElementChild, number);
            if (!includedStructuralElementList.isEmpty()) {
                includedStructuralElementList.add(0, includedStructuralElement);
                return includedStructuralElementList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Creates the media tree.
     *
     * @param mediaRoot
     *            root of media units to show on the tree
     * @return the media tree
     */
    private DefaultTreeNode buildMediaTree(MediaUnit mediaRoot) {
        DefaultTreeNode rootTreeNode = new DefaultTreeNode();
        if (nodeStateUnknown(this.previousExpansionStatesPhysicalTree, rootTreeNode)) {
            rootTreeNode.setExpanded(true);
        }
        buildMediaTreeRecursively(mediaRoot, rootTreeNode);
        return rootTreeNode;
    }

    private void buildMediaTreeRecursively(MediaUnit mediaUnit, DefaultTreeNode parentTreeNode) {
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                mediaUnit.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        DefaultTreeNode treeNode = addTreeNode("page".equals(mediaUnit.getType())
                        ? divisionView.getLabel().concat(" " + mediaUnit.getOrderlabel()) : divisionView.getLabel(),
                false, false, mediaUnit, parentTreeNode);
        if (nodeStateUnknown(this.previousExpansionStatesPhysicalTree, treeNode)) {
            treeNode.setExpanded(true);
        }
        if (Objects.nonNull(mediaUnit.getChildren())) {
            for (MediaUnit child : mediaUnit.getChildren()) {
                buildMediaTreeRecursively(child, treeNode);
            }
        }
    }

    /**
     * Callback function triggered when a node is selected in the logical structure tree.
     *
     * @param event
     *            NodeSelectEvent triggered by logical node being selected
     */
    public void treeLogicalSelect(NodeSelectEvent event) {
        /*
         * The newly selected element has already been set in 'selectedLogicalNode' by
         * JSF at this point.
         */
        try {
            dataEditor.switchStructure(event.getTreeNode().getData());
            previouslySelectedLogicalNode = selectedLogicalNode;
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            selectedLogicalNode = previouslySelectedLogicalNode;
        }
    }

    /**
     * Callback function triggered when a node is selected in the physical structure tree.
     *
     * @param event
     *            NodeSelectEvent triggered by logical node being selected
     */
    public void treePhysicalSelect(NodeSelectEvent event) {
        /*
         * The newly selected element has already been set in 'selectedPhysicalNode' by
         * JSF at this point.
         */
        try {
            dataEditor.switchMediaUnit();
            previouslySelectedPhysicalNode = selectedPhysicalNode;
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            selectedPhysicalNode = previouslySelectedPhysicalNode;
        }
    }

    void updateNodeSelection(GalleryMediaContent galleryMediaContent) {
        this.updateLogicalNodeSelection(galleryMediaContent);
        this.updatePhysicalNodeSelection(galleryMediaContent);
    }

    private void updatePhysicalNodeSelection(TreeNode treeNode) {
        if (this.isSeparateMedia()) {
            if (Objects.nonNull(previouslySelectedPhysicalNode)) {
                previouslySelectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(selectedPhysicalNode)) {
                selectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(physicalTree)) {
                if (Objects.nonNull(treeNode)) {
                    setSelectedPhysicalNode(treeNode);
                    this.dataEditor.getMetadataPanel().showPhysical(this.dataEditor.getSelectedMediaUnit());
                } else {
                    Helper.setErrorMessage("Unable to update Node selection in physical structure!");
                }
            }
        }
    }

    void updatePhysicalNodeSelection(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(physicalTree)) {
            TreeNode selectedTreeNode = updateNodeSelectionRecursive(galleryMediaContent, physicalTree);
            updatePhysicalNodeSelection(selectedTreeNode);
        }
    }

    void updateLogicalNodeSelection(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(previouslySelectedLogicalNode)) {
            previouslySelectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(selectedLogicalNode)) {
            selectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(this.logicalTree)) {
            GalleryStripe matchingGalleryStripe = this.dataEditor.getGalleryPanel().getLogicalStructureOfMedia(galleryMediaContent);
            if (Objects.nonNull(matchingGalleryStripe) && Objects.nonNull(matchingGalleryStripe.getStructure())) {
                if (this.isSeparateMedia()) {
                    TreeNode selectedLogicalTreeNode =
                            updateLogicalNodeSelectionRecursive(matchingGalleryStripe.getStructure(),
                                logicalTree);
                    if (Objects.nonNull(selectedLogicalTreeNode)) {
                        setSelectedLogicalNode(selectedLogicalTreeNode);
                    } else {
                        Helper.setErrorMessage("Unable to update node selection in logical structure!");
                    }
                } else {
                    TreeNode selectedTreeNode = updateNodeSelectionRecursive(galleryMediaContent,
                        logicalTree);
                    if (Objects.nonNull(selectedTreeNode)) {
                        setSelectedLogicalNode(selectedTreeNode);
                    } else {
                        Helper.setErrorMessage("Unable to update node selection in logical structure!");
                    }
                }
            }
        }
    }

    /**
     * Update the node selection in logical tree.
     * @param structure the IncludedStructuralElement to be selected as a TreeNode
     * @param treeNode the logical structure tree
     * @return the TreeNode that will be selected
     */
    public TreeNode updateLogicalNodeSelectionRecursive(IncludedStructuralElement structure, TreeNode treeNode) {
        TreeNode matchingTreeNode = null;
        for (TreeNode currentTreeNode : treeNode.getChildren()) {
            if (treeNodeMatchesStructure(structure, currentTreeNode)) {
                currentTreeNode.setSelected(true);
                matchingTreeNode = currentTreeNode;
            } else {
                matchingTreeNode = updateLogicalNodeSelectionRecursive(structure, currentTreeNode);
            }
            if (Objects.nonNull(matchingTreeNode)) {
                break;
            }
        }
        return matchingTreeNode;
    }

    private TreeNode updateNodeSelectionRecursive(GalleryMediaContent galleryMediaContent, TreeNode treeNode) {
        TreeNode matchingTreeNode = null;
        for (TreeNode currentTreeNode : treeNode.getChildren()) {
            if (currentTreeNode.getChildCount() < 1 && treeNodeMatchesGalleryMediaContent(galleryMediaContent, currentTreeNode)) {
                currentTreeNode.setSelected(true);
                matchingTreeNode = currentTreeNode;
            } else {
                currentTreeNode.setSelected(false);
                matchingTreeNode = updateNodeSelectionRecursive(galleryMediaContent, currentTreeNode);
            }
            if (Objects.nonNull(matchingTreeNode)) {
                break;
            }
        }
        return matchingTreeNode;
    }

    private boolean treeNodeMatchesGalleryMediaContent(GalleryMediaContent galleryMediaContent, TreeNode treeNode) {
        if (treeNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
            MediaUnit mediaUnit = null;
            if (structureTreeNode.getDataObject() instanceof MediaUnit) {
                mediaUnit = (MediaUnit) structureTreeNode.getDataObject();
            } else if (structureTreeNode.getDataObject() instanceof View) {
                View view = (View) structureTreeNode.getDataObject();
                mediaUnit = view.getMediaUnit();
            }
            if (Objects.nonNull(mediaUnit) && Objects.nonNull(galleryMediaContent.getView())) {
                return Objects.equals(mediaUnit, galleryMediaContent.getView().getMediaUnit());
            }
        }
        return false;
    }

    private boolean treeNodeMatchesStructure(IncludedStructuralElement structure, TreeNode treeNode) {
        if (Objects.nonNull(treeNode) && treeNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
            if (structureTreeNode.getDataObject() instanceof IncludedStructuralElement) {
                return Objects.equals(structureTreeNode.getDataObject(), structure);
            }
        }
        return false;
    }

    /**
     * Callback function triggered on NodeCollapseEvent. Sets the 'expanded' flag of the corresponding tree node to
     * 'false' because this is not done automatically by PrimeFaces on a NodeCollapseEvent.
     *
     * @param event
     *          the NodeCollapseEvent triggered in the corresponding structure tree
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.getTreeNode())) {
            event.getTreeNode().setExpanded(false);
        }
    }

    /**
     * Callback function triggered on NodeExpandEvent. Sets the 'expanded' flag of the corresponding tree node to
     * 'true' because this is not done automatically by PrimeFaces on a NodeExpandEvent.
     *
     * @param event
     *          the NodeExpandEvent triggered in the corresponding structure tree
     */
    public void onNodeExpand(NodeExpandEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.getTreeNode())) {
            event.getTreeNode().setExpanded(true);
        }
    }

    /**
     * Callback function triggered on TreeDragDropEvent. Checks whether performed drag'n'drop action is allowed
     * considering ruleset restrictions on structure hierarchy. In case some ruleset rules were violated by the action
     * displays a corresponding error message to the user and reverts tree to prior state.
     *
     * @param event TreeDragDropEvent
     *              event triggering this callback function
     */
    public void onDragDrop(TreeDragDropEvent event) {

        Object dragNodeObject = event.getDragNode().getData();
        Object dropNodeObject = event.getDropNode().getData();

        expandNode(event.getDropNode());

        try {
            StructureTreeNode dropNode = (StructureTreeNode) dropNodeObject;
            StructureTreeNode dragNode = (StructureTreeNode) dragNodeObject;
            if (dragNode.getDataObject() instanceof IncludedStructuralElement
                    && dropNode.getDataObject() instanceof IncludedStructuralElement) {
                checkLogicalDragDrop(dragNode, dropNode);
            } else if (dragNode.getDataObject() instanceof MediaUnit
                    && dropNode.getDataObject() instanceof MediaUnit) {
                checkPhysicalDragDrop(dragNode, dropNode);
            } else if (dragNode.getDataObject() instanceof View
                     && dropNode.getDataObject() instanceof IncludedStructuralElement) {
                movePageNode(event, dropNode, dragNode);
            } else {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.dragnDropError", Arrays.asList(
                        dragNode.getLabel(), dropNode.getLabel())));
                show();
            }
        } catch (ClassCastException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    /**
     * Determine the IncludedStructuralElement to which the given View is assigned.
     *
     * @param view
     *          View for which the IncludedStructuralElement is determined
     * @return the IncludedStructuralElement to which the given View is assigned
     */
    private IncludedStructuralElement getPageStructure(View view, IncludedStructuralElement parent) {
        IncludedStructuralElement resultElement = null;
        for (IncludedStructuralElement child : parent.getChildren()) {
            if (child.getViews().contains(view)) {
                resultElement = child;
            } else {
                resultElement =  getPageStructure(view, child);
            }
            if (Objects.nonNull(resultElement)) {
                break;
            }
        }
        return resultElement;
    }

    /**
     * Move page encapsulated in given StructureTreeNode 'dragNode' to Structural Element encapsulated in given
     * StructureTreeNode 'dropNode' at index encoded in given TreeDragDropEvent 'event'.
     *
     * @param event
     *          TreeDragDropEvent triggering 'movePageNode'
     * @param dropNode
     *          StructureTreeNode containing the Structural Element to which the page is moved
     * @param dragNode
     *          StructureTreeNode containing the View/Page that is moved
     */
    private void movePageNode(TreeDragDropEvent event, StructureTreeNode dropNode, StructureTreeNode dragNode) {
        TreeNode dragParent = event.getDragNode().getParent();
        if (dragParent.getData() instanceof StructureTreeNode) {
            StructureTreeNode dragParentTreeNode = (StructureTreeNode) dragParent.getData();
            if (dragParentTreeNode.getDataObject() instanceof IncludedStructuralElement) {
                View view = (View) dragNode.getDataObject();
                IncludedStructuralElement previousParent;
                if (dataEditor.getWorkpiece().getRootElement().getViews().contains(view)) {
                    previousParent = dataEditor.getWorkpiece().getRootElement();
                } else {
                    previousParent = getPageStructure(view, dataEditor.getWorkpiece().getRootElement());
                }
                if (Objects.nonNull(previousParent)) {
                    IncludedStructuralElement element = (IncludedStructuralElement) dropNode.getDataObject();
                    // TODO once PrimeFaces' tree drop index bug is fixed pass index where the pages should be inserted
                    moveViews(element, Collections.singletonList(new ImmutablePair<>(view, previousParent)), -1);
                    expandNode(event.getDropNode());
                    this.dataEditor.getGalleryPanel().updateStripes();
                    return;
                } else {
                    Helper.setErrorMessage(Helper.getTranslation("dataEditor.noParentsError",
                            Collections.singletonList(dragNode.getLabel())));
                }
            } else {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.dragnDropError", Arrays.asList(
                        dragNode.getLabel(), dropNode.getLabel())));
            }
        } else {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.dragnDropError", Arrays.asList(
                    dragNode.getLabel(), dropNode.getLabel())));
        }
        show();
    }

    /**
     * Move List of elements 'elementsToBeMoved' from IncludedStructuralElement in each Pair to IncludedStructuralElement
     * 'toElement'.
     *
     * @param toElement
     *          IncludedStructuralElement to which View is moved
     * @param elementsToBeMoved
     *          List of elements to be moved as Pairs of View and IncludedStructuralElement they are attached to
     * @param insertionIndex
     *          Index where views will be inserted into toElement's views
     */
    void moveViews(IncludedStructuralElement toElement,
                   List<Pair<View, IncludedStructuralElement>> elementsToBeMoved,
                   int insertionIndex) {
        List<View> views = elementsToBeMoved.stream()
                .map(Pair::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (insertionIndex < 0 || insertionIndex == toElement.getViews().size()) {
            toElement.getViews().addAll(views);
        } else {
            toElement.getViews().addAll(insertionIndex, views);
        }

        for (Pair<View, IncludedStructuralElement> elementToBeMoved : elementsToBeMoved) {
            boolean removeLastOccurrenceOfView = toElement.equals(elementToBeMoved.getValue())
                    && insertionIndex < elementToBeMoved.getValue().getViews().lastIndexOf(elementToBeMoved.getKey());
            dataEditor.unassignView(elementToBeMoved.getValue(), elementToBeMoved.getKey(), removeLastOccurrenceOfView);
            elementToBeMoved.getKey().getMediaUnit().getIncludedStructuralElements().add(toElement);
        }
    }

    private void checkLogicalDragDrop(StructureTreeNode dragNode, StructureTreeNode dropNode) {

        IncludedStructuralElement dragStructure = (IncludedStructuralElement) dragNode.getDataObject();
        IncludedStructuralElement dropStructure = (IncludedStructuralElement) dropNode.getDataObject();

        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                dropStructure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());

        LinkedList<IncludedStructuralElement> dragParents;
        if (divisionView.getAllowedSubstructuralElements().containsKey(dragStructure.getType())) {
            dragParents = MetadataEditor.getAncestorsOfStructure(dragStructure,
                    dataEditor.getWorkpiece().getRootElement());
            if (!dragParents.isEmpty()) {
                IncludedStructuralElement parentStructure = dragParents.get(dragParents.size() - 1);
                if (parentStructure.getChildren().contains(dragStructure)) {
                    preserveLogical();
                    this.dataEditor.getGalleryPanel().updateStripes();
                    return;
                } else {
                    Helper.setErrorMessage(Helper.getTranslation("dataEditor.childNotContainedError",
                            Collections.singletonList(dragNode.getLabel())));
                }
            } else {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.noParentsError",
                        Collections.singletonList(dragNode.getLabel())));
            }
        } else {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.forbiddenChildElement",
                    Arrays.asList(dragNode.getLabel(), dropNode.getLabel())));
        }
        show();
    }

    private void checkPhysicalDragDrop(StructureTreeNode dragNode, StructureTreeNode dropNode) {

        MediaUnit dragUnit = (MediaUnit) dragNode.getDataObject();
        MediaUnit dropUnit = (MediaUnit) dropNode.getDataObject();

        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                dropUnit.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());

        LinkedList<MediaUnit> dragParents;
        if (divisionView.getAllowedSubstructuralElements().containsKey(dragUnit.getType())) {
            dragParents = MetadataEditor.getAncestorsOfMediaUnit(dragUnit, dataEditor.getWorkpiece().getMediaUnit());
            if (dragParents.isEmpty()) {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.noParentsError",
                        Collections.singletonList(dragNode.getLabel())));
            } else {
                MediaUnit parentUnit = dragParents.get(dragParents.size() - 1);
                if (parentUnit.getChildren().contains(dragUnit)) {
                    preservePhysical();
                    return;
                } else {
                    Helper.setErrorMessage(Helper.getTranslation("dataEditor.childNotContainedError",
                            Collections.singletonList(dragUnit.getType())));
                }
            }
        } else {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.forbiddenChildElement",
                    Arrays.asList(dragNode.getLabel(), dropNode.getLabel())));
        }
        show();
    }

    /**
     * Check and return whether the metadata of a process should be displayed in separate logical and physical
     * structure trees or in one unified structure tree.
     * Returns true if the current task in the metadata editor is
     * - non null
     * - assigned to the current user
     * - type metadata
     * - separate structure flag = true
     *
     * @return
     *          whether metadata structure should be displayed in separate structure trees or not
     */
    public boolean isSeparateMedia() {
        if (Objects.nonNull(this.dataEditor.getCurrentTask())
                && this.dataEditor.getCurrentTask().isTypeMetadata()
                && this.dataEditor.getCurrentTask().isSeparateStructure()) {
            SecurityUserDetails authenticatedUser = ServiceManager.getUserService().getAuthenticatedUser();
            if (Objects.nonNull(authenticatedUser)) {
                int userID = authenticatedUser.getId();
                return Objects.nonNull(this.dataEditor.getCurrentTask().getProcessingUser())
                        && Objects.equals(this.dataEditor.getCurrentTask().getProcessingUser().getId(), userID);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void expandNode(TreeNode node) {
        if (Objects.nonNull(node)) {
            node.setExpanded(true);
            expandNode(node.getParent());
        }
    }

    private HashMap<IncludedStructuralElement, Boolean> getTreeNodeExpansionStates(DefaultTreeNode tree) {
        if (Objects.nonNull(tree) && tree.getChildCount() == 1) {
            TreeNode treeRoot = tree.getChildren().get(0);
            IncludedStructuralElement structuralElement = getTreeNodeStructuralElement(treeRoot);
            if (Objects.nonNull(structuralElement)) {
                return getTreeNodeExpansionStatesRecursively(treeRoot, new HashMap<>());
            }
        }
        return new HashMap<>();
    }

    private HashMap<IncludedStructuralElement, Boolean> getTreeNodeExpansionStatesRecursively(TreeNode treeNode,
            HashMap<IncludedStructuralElement, Boolean> expansionStates) {
        if (Objects.nonNull(treeNode)) {
            IncludedStructuralElement structureData = getTreeNodeStructuralElement(treeNode);
            if (Objects.nonNull(structureData)) {
                expansionStates.put(structureData, treeNode.isExpanded());
                for (TreeNode childNode : treeNode.getChildren()) {
                    expansionStates.putAll(getTreeNodeExpansionStatesRecursively(childNode, expansionStates));
                }
            }
        }
        return expansionStates;
    }

    private void updateNodeExpansionStates(DefaultTreeNode tree, HashMap<IncludedStructuralElement, Boolean> expansionStates) {
        if (Objects.nonNull(tree) && Objects.nonNull(expansionStates) && !expansionStates.isEmpty()) {
            updateNodeExpansionStatesRecursively(tree, expansionStates);
        }
    }

    private void updateNodeExpansionStatesRecursively(TreeNode treeNode, HashMap<IncludedStructuralElement, Boolean> expansionStates) {
        IncludedStructuralElement element = getTreeNodeStructuralElement(treeNode);
        if (Objects.nonNull(element) && expansionStates.containsKey(element)) {
            treeNode.setExpanded(expansionStates.get(element));
        }
        for (TreeNode childNode : treeNode.getChildren()) {
            updateNodeExpansionStatesRecursively(childNode, expansionStates);
        }
    }

    private boolean nodeStateUnknown(HashMap<IncludedStructuralElement, Boolean> expansionStates, TreeNode treeNode) {
        IncludedStructuralElement element = getTreeNodeStructuralElement(treeNode);
        return !Objects.nonNull(expansionStates) || (Objects.nonNull(element) && !expansionStates.containsKey(element));
    }

    private IncludedStructuralElement getTreeNodeStructuralElement(TreeNode treeNode) {
        if (treeNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
            if (structureTreeNode.getDataObject() instanceof IncludedStructuralElement) {
                return (IncludedStructuralElement) structureTreeNode.getDataObject();
            }
        }
        return null;
    }

    /**
     * Get List of MediaUnits assigned to multiple IncludedStructuralElements.
     *
     * @return value of severalAssignments
     */
    List<MediaUnit> getSeveralAssignments() {
        return severalAssignments;
    }


    /**
     * Get the index of this StructureTreeNode's MediaUnit out of all MediaUnits
     * which are assigned to more than one IncludedStructuralElement.
     *
     * @param treeNode object to find the index for
     * @return index of the StructureTreeNode's MediaUnit if present in the List of several assignments, or -1 if not present in the list.
     */
    public int getMultipleAssignmentsIndex(StructureTreeNode treeNode) {
        if (treeNode.getDataObject() instanceof View
                && Objects.nonNull(((View) treeNode.getDataObject()).getMediaUnit())) {
            return severalAssignments.indexOf(((View) treeNode.getDataObject()).getMediaUnit());
        }
        return -1;
    }

    /**
     * Check if the selected Node's MediaUnit is assigned to several IncludedStructuralElements.
     *
     * @return {@code true} when the MediaUnit is assigned to more than one logical element
     */
    public boolean isAssignedSeveralTimes() {
        if (Objects.nonNull(selectedLogicalNode) && selectedLogicalNode.getData() instanceof  StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
            if (structureTreeNode.getDataObject() instanceof View) {
                View view = (View) structureTreeNode.getDataObject();
                return view.getMediaUnit().getIncludedStructuralElements().size() > 1;
            }
        }
        return false;
    }

    /**
     * Check if the selected Node's MediaUnit can be assigned to the next logical element in addition to the current assignment.
     * @return {@code true} if the MediaUnit can be assigned to the next IncludedStructuralElement
     */
    public boolean isAssignableSeveralTimes() {
        if (Objects.nonNull(selectedLogicalNode) && selectedLogicalNode.getData() instanceof  StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
            if (structureTreeNode.getDataObject() instanceof View) {
                List<TreeNode> logicalNodeSiblings = selectedLogicalNode.getParent().getParent().getChildren();
                int logicalNodeIndex = logicalNodeSiblings.indexOf(selectedLogicalNode.getParent());
                List<TreeNode> viewSiblbings = selectedLogicalNode.getParent().getChildren();
                // check for selected node's positions and siblings after selected node's parent
                if (viewSiblbings.indexOf(selectedLogicalNode) == viewSiblbings.size() - 1
                        && logicalNodeSiblings.size() > logicalNodeIndex + 1) {
                    TreeNode nextSibling = logicalNodeSiblings.get(logicalNodeIndex + 1);
                    if (nextSibling.getData() instanceof StructureTreeNode) {
                        StructureTreeNode structureTreeNodeSibling = (StructureTreeNode) nextSibling.getData();
                        return structureTreeNodeSibling.getDataObject() instanceof IncludedStructuralElement;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Assign selected Node's MediaUnit to the next IncludedStructuralElement.
     */
    public void assign() {
        if (isAssignableSeveralTimes()) {
            View view = (View) ((StructureTreeNode) selectedLogicalNode.getData()).getDataObject();
            List<TreeNode> logicalNodeSiblings = selectedLogicalNode.getParent().getParent().getChildren();
            int logicalNodeIndex = logicalNodeSiblings.indexOf(selectedLogicalNode.getParent());
            TreeNode nextSibling = logicalNodeSiblings.get(logicalNodeIndex + 1);
            StructureTreeNode structureTreeNodeSibling = (StructureTreeNode) nextSibling.getData();
            IncludedStructuralElement includedStructuralElement = (IncludedStructuralElement) structureTreeNodeSibling.getDataObject();
            dataEditor.assignView(includedStructuralElement, view);
            severalAssignments.add(view.getMediaUnit());
            preserveLogical();
            show();
            dataEditor.getGalleryPanel().updateStripes();
        }
    }

    /**
     * Unassign the selected Node's MediaUnit from the IncludedStructuralElement parent at the selected position.
     * This does not remove it from other IncludedStructuralElements.
     */
    public void unassign() {
        if (isAssignedSeveralTimes()) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
            View view = (View) structureTreeNode.getDataObject();
            if (selectedLogicalNode.getParent().getData() instanceof StructureTreeNode) {
                StructureTreeNode structureTreeNodeParent = (StructureTreeNode) selectedLogicalNode.getParent().getData();
                if (structureTreeNodeParent.getDataObject() instanceof IncludedStructuralElement) {
                    IncludedStructuralElement includedStructuralElement =
                            (IncludedStructuralElement) structureTreeNodeParent.getDataObject();
                    dataEditor.unassignView(includedStructuralElement, view, false);
                    if (view.getMediaUnit().getIncludedStructuralElements().size() <= 1) {
                        severalAssignments.remove(view.getMediaUnit());
                    }
                    preserveLogical();
                    show();
                    dataEditor.getGalleryPanel().updateStripes();
                }
            }
        }
    }
}
