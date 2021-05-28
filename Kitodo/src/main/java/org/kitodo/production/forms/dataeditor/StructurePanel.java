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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.UnknownTreeNodeDataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class StructurePanel implements Serializable {
    private static final Logger logger = LogManager.getLogger(StructurePanel.class);

    private final DataEditorForm dataEditor;

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
    private HashMap<MediaUnit, Boolean> previousExpansionStatesPhysicalTree;

    /**
     * List of all mediaUnits assigned to multiple IncludedStructuralElements.
     */
    private List<MediaUnit> severalAssignments = new LinkedList<>();

    /**
     * Variable used to set the correct order value when building the logical and physical trees from the PrimeFaces tree.
     */
    private int order = 1;

    /**
     * Active tabs in StructurePanel's accordion.
     */
    private String activeTabs;

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
        if (!getSelectedStructure().isPresent()) {
            /*
             * No element is selected or the selected element is not a structure
             * but, for example, a media unit.
             */
            return;
        }
        IncludedStructuralElement selectedStructure = getSelectedStructure().get();
        LinkedList<IncludedStructuralElement> ancestors = MetadataEditor.getAncestorsOfStructure(selectedStructure, structure);
        if (ancestors.isEmpty()) {
            // The selected element is the root node of the tree.
            return;
        }
        IncludedStructuralElement parent = ancestors.getLast();

        Collection<View> subViews = new ArrayList<>();
        getAllSubViews(selectedStructure, subViews);

        List<View> multipleViews = subViews.stream().filter(v -> v.getMediaUnit().getIncludedStructuralElements().size() > 1)
                .collect(Collectors.toList());
        for (View view : multipleViews) {
            dataEditor.unassignView(selectedStructure, view, selectedStructure.getViews().getLast().equals(view));
            if (view.getMediaUnit().getIncludedStructuralElements().size() <= 1) {
                severalAssignments.remove(view.getMediaUnit());
            }
        }
        subViews.removeAll(multipleViews);

        parent.getViews().addAll(subViews);
        parent.getViews().sort(Comparator.comparingInt(v -> v.getMediaUnit().getOrder()));

        parent.getChildren().remove(selectedStructure);
        show();
        dataEditor.getGalleryPanel().updateStripes();
    }

    private void getAllSubViews(IncludedStructuralElement selectedStructure, Collection<View> views) {
        if (Objects.nonNull(selectedStructure.getViews())) {
            views.addAll(selectedStructure.getViews());
        }
        for (IncludedStructuralElement child : selectedStructure.getChildren()) {
            getAllSubViews(child, views);
        }
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
            previouslySelectedPhysicalNode.setSelected(false);
            show(true);
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

    void preserve() throws UnknownTreeNodeDataException {
        if (isSeparateMedia()) {
            this.preserveLogical();
            this.preservePhysical();
        } else {
            preserveLogicalAndPhysical();
        }
    }

    /**
     * Updates the live structure of the workpiece with the current members of
     * the structure tree in their given order. The live structure of the
     * workpiece which is stored in the root element of the structure tree.
     */
    private void preserveLogical() {
        if (!this.logicalTree.getChildren().isEmpty()) {
            preserveLogicalRecursive(this.logicalTree.getChildren().get(logicalTree.getChildCount() - 1));
            this.dataEditor.checkForChanges();
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
            this.dataEditor.checkForChanges();
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
            String logicalRowKey = null;
            if (Objects.nonNull(selectedLogicalNode)) {
                logicalRowKey = selectedLogicalNode.getRowKey();
            }
            String physicalRowKey = null;
            if (Objects.nonNull(selectedPhysicalNode)) {
                physicalRowKey = selectedPhysicalNode.getRowKey();
            }
            TreeNode keepSelectedLogicalNode = selectedLogicalNode;
            TreeNode keepSelectedPhysicalNode = selectedPhysicalNode;
            show();
            selectedLogicalNode = keepSelectedLogicalNode;
            selectedPhysicalNode = keepSelectedPhysicalNode;
            if (Objects.nonNull(logicalRowKey)) {
                restoreSelection(logicalRowKey, this.logicalTree);
            }
            if (Objects.nonNull(physicalRowKey)) {
                restoreSelection(physicalRowKey, this.physicalTree);
            }
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

        this.previousExpansionStatesLogicalTree = getLogicalTreeNodeExpansionStates(this.logicalTree);
        this.logicalTree = buildStructureTree();
        updateLogicalNodeExpansionStates(this.logicalTree, this.previousExpansionStatesLogicalTree);

        this.previousExpansionStatesPhysicalTree = getPhysicalTreeNodeExpansionStates(this.physicalTree);
        this.physicalTree = buildMediaTree(dataEditor.getWorkpiece().getMediaUnit());
        updatePhysicalNodeExpansionStates(this.physicalTree, this.previousExpansionStatesPhysicalTree);

        this.selectedLogicalNode = logicalTree.getChildren().get(logicalTree.getChildCount() - 1);
        this.selectedPhysicalNode = physicalTree.getChildren().get(0);
        this.previouslySelectedLogicalNode = selectedLogicalNode;
        this.previouslySelectedPhysicalNode = selectedPhysicalNode;
        this.dataEditor.checkForChanges();
    }

    private void restoreSelection(String rowKey, TreeNode parentNode) {
        for (TreeNode childNode : parentNode.getChildren()) {
            if (Objects.nonNull(childNode) && rowKey.equals(childNode.getRowKey())) {
                childNode.setSelected(true);
                break;
            } else {
                childNode.setSelected(false);
                restoreSelection(rowKey, childNode);
            }
        }
    }

    /**
     * Creates the structure tree. If hierarchical links exist upwards, they are
     * displayed above the tree as separate trees.
     *
     * @return the structure tree(s) and the collection of views displayed in
     *         the tree
     */
    private DefaultTreeNode buildStructureTree() {
        DefaultTreeNode invisibleRootNode = new DefaultTreeNode();
        invisibleRootNode.setExpanded(true);
        addParentLinksRecursive(dataEditor.getProcess(), invisibleRootNode);
        buildStructureTreeRecursively(structure, invisibleRootNode);
        return invisibleRootNode;
    }

    private Collection<View> buildStructureTreeRecursively(IncludedStructuralElement structure, TreeNode result) {
        StructureTreeNode node;
        if (Objects.isNull(structure.getLink())) {
            StructuralElementViewInterface divisionView = dataEditor.getRulesetManagement().getStructuralElementView(
                structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            node = new StructureTreeNode(divisionView.getLabel(),
                    divisionView.isUndefined() && Objects.nonNull(structure.getType()), false, structure);
        } else {
            node = new StructureTreeNode(structure.getLink().getUri().toString(), true, true, structure);
            for (Process child : dataEditor.getCurrentChildren()) {
                try {
                    String type = ServiceManager.getProcessService().getBaseType(child.getId());
                    if (child.getId() == ServiceManager.getProcessService()
                            .processIdFromUri(structure.getLink().getUri())) {
                        StructuralElementViewInterface view = dataEditor.getRulesetManagement().getStructuralElementView(
                            type, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                        node = new StructureTreeNode(view.getLabel(), view.isUndefined(), true, structure);
                    }
                } catch (DataException e) {
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
        if (logicalNodeStateUnknown(this.previousExpansionStatesLogicalTree, parent)) {
            parent.setExpanded(true);
        }

        Set<View> viewsShowingOnAChild = new HashSet<>();
        if (this.isSeparateMedia()) {
            for (IncludedStructuralElement child : structure.getChildren()) {
                viewsShowingOnAChild.addAll(buildStructureTreeRecursively(child, parent));
            }
        } else {
            orderChildrenAndViews(new ArrayList<>(structure.getChildren()), new ArrayList<>(structure.getViews()), parent,
                    viewsShowingOnAChild);
        }
        return viewsShowingOnAChild;
    }

    /**
     * This method appends IncludedStructuralElement children and assigned views while considering the ORDER attribute to create the
     * combined tree with the correct order of logical and physical elements.
     * @param children List of IncludedStructuralElements which are children of the element represented by the DefaultTreeNode parent
     * @param views List of Views assigned to the element represented by the DefaultTreeNode parent
     * @param parent DefaultTreeNode representing the logical element where all new elements should be appended
     * @param viewsShowingOnAChild Collection of Views displayed in the combined tree
     */
    private void orderChildrenAndViews(List<IncludedStructuralElement> children, List<View> views, DefaultTreeNode parent,
                                       Set<View> viewsShowingOnAChild) {
        List<IncludedStructuralElement> temporaryChildren = new ArrayList<>(children);
        List<View> temporaryViews = new ArrayList<>(views);
        temporaryChildren.removeAll(Collections.singletonList(null));
        temporaryViews.removeAll(Collections.singletonList(null));
        while (temporaryChildren.size() > 0 || temporaryViews.size() > 0) {
            IncludedStructuralElement temporaryChild = null;
            View temporaryView = null;

            if (temporaryChildren.size() > 0) {
                temporaryChild = temporaryChildren.get(0);
            }
            if (temporaryViews.size() > 0) {
                temporaryView = temporaryViews.get(0);
            }

            if (Objects.isNull(temporaryChild) && Objects.isNull(temporaryView)) {
                break;
            }

            if (Objects.nonNull(temporaryChild) && Objects.isNull(temporaryView)
                    || Objects.nonNull(temporaryChild) && temporaryChild.getOrder() <= temporaryView.getMediaUnit().getOrder()) {
                viewsShowingOnAChild.addAll(buildStructureTreeRecursively(temporaryChild, parent));
                temporaryChildren.remove(0);
            } else {
                if (!viewsShowingOnAChild.contains(temporaryView)) {
                    addTreeNode(buildViewLabel(temporaryView), false, false, temporaryView, parent);
                    viewsShowingOnAChild.add(temporaryView);
                }
                temporaryViews.remove(0);
            }
        }
    }

    private String buildViewLabel(View view) {
        String order = view.getMediaUnit().getOrder() + " : ";
        if (Objects.nonNull(view.getMediaUnit().getOrderlabel())) {
            return order + view.getMediaUnit().getOrderlabel();
        } else {
            return order + "uncounted";
        }
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
        StructuralElementViewInterface structuralElementView = dataEditor.getRulesetManagement().getStructuralElementView(type,
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
        if (dataObject instanceof MediaUnit && physicalNodeStateUnknown(this.previousExpansionStatesPhysicalTree, node)
                || dataObject instanceof IncludedStructuralElement
                && logicalNodeStateUnknown(this.previousExpansionStatesLogicalTree, node)) {
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
     * @param tree
     *            list of structure trees, in this list the parent links are
     *            inserted on top, therefore LinkedList
     */
    private void addParentLinksRecursive(Process child, DefaultTreeNode tree) {
        Process parent = child.getParent();
        // Termination condition of recursion, if the process has no parent
        if (Objects.isNull(parent)) {
            return;
        }
        // Process parent link of the parent recursively
        addParentLinksRecursive(parent, tree);
        URI uri = ServiceManager.getProcessService().getMetadataFileUri(parent);
        try {
            IncludedStructuralElement rootElement = ServiceManager.getMetsService().loadWorkpiece(uri).getRootElement();
            List<IncludedStructuralElement> includedStructuralElementList
                    = MetadataEditor.determineIncludedStructuralElementPathToChild(rootElement, child.getId());
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
                        parentNode.setExpanded(true);
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
        if (physicalNodeStateUnknown(this.previousExpansionStatesPhysicalTree, rootTreeNode)) {
            rootTreeNode.setExpanded(true);
        }
        buildMediaTreeRecursively(mediaRoot, rootTreeNode);
        return rootTreeNode;
    }

    private void buildMediaTreeRecursively(MediaUnit mediaUnit, DefaultTreeNode parentTreeNode) {
        StructuralElementViewInterface divisionView = dataEditor.getRulesetManagement().getStructuralElementView(
                mediaUnit.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        DefaultTreeNode treeNode = addTreeNode(Objects.equals(mediaUnit.getType(), MediaUnit.TYPE_PAGE)
                        ? divisionView.getLabel().concat(" " + mediaUnit.getOrderlabel()) : divisionView.getLabel(),
                false, false, mediaUnit, parentTreeNode);
        if (physicalNodeStateUnknown(this.previousExpansionStatesPhysicalTree, treeNode)) {
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
            dataEditor.switchStructure(event.getTreeNode().getData(), true);
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

    void updateNodeSelection(GalleryMediaContent galleryMediaContent, IncludedStructuralElement structure) {
        this.updateLogicalNodeSelection(galleryMediaContent, structure);
        this.updatePhysicalNodeSelection(galleryMediaContent);
    }

    private void updatePhysicalNodeSelection(TreeNode treeNode) {
        if (this.isSeparateMedia()) {
            if (Objects.nonNull(previouslySelectedPhysicalNode)) {
                previouslySelectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(selectedPhysicalNode) && !selectedPhysicalNode.equals(treeNode)) {
                selectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(physicalTree) && Objects.nonNull(treeNode)) {
                setSelectedPhysicalNode(treeNode);
                this.dataEditor.getMetadataPanel().showPhysical(this.dataEditor.getSelectedMediaUnit());
            }
        }
    }

    void updatePhysicalNodeSelection(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(physicalTree)) {
            TreeNode selectedTreeNode = updatePhysicalNodeSelectionRecursive(galleryMediaContent, physicalTree);
            updatePhysicalNodeSelection(selectedTreeNode);
        }
    }

    void updateLogicalNodeSelection(GalleryMediaContent galleryMediaContent, IncludedStructuralElement structure) {
        if (Objects.nonNull(previouslySelectedLogicalNode)) {
            previouslySelectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(selectedLogicalNode)) {
            selectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(this.logicalTree)) {
            if (Objects.isNull(structure)) {
                GalleryStripe matchingGalleryStripe = this.dataEditor.getGalleryPanel().getLogicalStructureOfMedia(galleryMediaContent);
                if (Objects.nonNull(matchingGalleryStripe)) {
                    structure = matchingGalleryStripe.getStructure();
                }
            }
            if (Objects.nonNull(structure)) {
                TreeNode selectedTreeNode;
                if (this.isSeparateMedia()) {
                    selectedTreeNode = updateLogicalNodeSelectionRecursive(structure, logicalTree);
                } else {
                    selectedTreeNode = updatePhysSelectionInLogTreeRecursive(galleryMediaContent.getView().getMediaUnit(), structure,
                            logicalTree);
                }
                if (Objects.nonNull(selectedTreeNode)) {
                    setSelectedLogicalNode(selectedTreeNode);
                } else {
                    Helper.setErrorMessage("Unable to update node selection in logical structure!");
                }
            }
        }
    }

    void updateLogicalNodeSelection(IncludedStructuralElement includedStructuralElement) {
        if (Objects.nonNull(previouslySelectedLogicalNode)) {
            previouslySelectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(selectedLogicalNode)) {
            selectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(logicalTree)) {
            TreeNode selectedTreeNode = updateLogicalNodeSelectionRecursive(includedStructuralElement, logicalTree);
            if (Objects.nonNull(selectedTreeNode)) {
                setSelectedLogicalNode(selectedTreeNode);
                try {
                    dataEditor.switchStructure(selectedTreeNode.getData(), false);
                } catch (NoSuchMetadataFieldException e) {
                    logger.error(e.getLocalizedMessage());
                }
            } else {
                Helper.setErrorMessage("Unable to update node selection in logical structure!");
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

    private TreeNode updatePhysicalNodeSelectionRecursive(GalleryMediaContent galleryMediaContent, TreeNode treeNode) {
        if (Objects.isNull(galleryMediaContent)) {
            return null;
        }
        TreeNode matchingTreeNode = null;
        for (TreeNode currentTreeNode : treeNode.getChildren()) {
            if (currentTreeNode.getChildCount() < 1 && treeNodeMatchesGalleryMediaContent(galleryMediaContent, currentTreeNode)) {
                currentTreeNode.setSelected(true);
                matchingTreeNode = currentTreeNode;
            } else {
                currentTreeNode.setSelected(false);
                matchingTreeNode = updatePhysicalNodeSelectionRecursive(galleryMediaContent, currentTreeNode);
            }
            if (Objects.nonNull(matchingTreeNode)) {
                break;
            }
        }
        return matchingTreeNode;
    }

    private TreeNode updatePhysSelectionInLogTreeRecursive(MediaUnit selectedMediaUnit, IncludedStructuralElement parentElement,
                                                           TreeNode treeNode) {
        TreeNode matchingTreeNode = null;
        for (TreeNode currentTreeNode : treeNode.getChildren()) {
            if (treeNode.getData() instanceof StructureTreeNode
                    && Objects.nonNull(((StructureTreeNode) treeNode.getData()).getDataObject())
                    && ((StructureTreeNode) treeNode.getData()).getDataObject().equals(parentElement)
                    && currentTreeNode.getData() instanceof StructureTreeNode
                    && ((StructureTreeNode) currentTreeNode.getData()).getDataObject() instanceof View
                    && ((View) ((StructureTreeNode) currentTreeNode.getData()).getDataObject()).getMediaUnit().equals(selectedMediaUnit)) {
                currentTreeNode.setSelected(true);
                matchingTreeNode = currentTreeNode;
            } else {
                currentTreeNode.setSelected(false);
                matchingTreeNode = updatePhysSelectionInLogTreeRecursive(selectedMediaUnit, parentElement, currentTreeNode);
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
        } catch (Exception exception) {
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
    IncludedStructuralElement getPageStructure(View view, IncludedStructuralElement parent) {
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
    private void movePageNode(TreeDragDropEvent event, StructureTreeNode dropNode, StructureTreeNode dragNode) throws Exception {
        TreeNode dragParent = event.getDragNode().getParent();
        if (dragParent.getData() instanceof StructureTreeNode) {
            StructureTreeNode dragParentTreeNode = (StructureTreeNode) dragParent.getData();
            if (dragParentTreeNode.getDataObject() instanceof IncludedStructuralElement) {
                // FIXME waiting for PrimeFaces' tree drop index bug to be fixed.
                // Until fixed dropping nodes onto other nodes will produce random drop indices.
                preserveLogicalAndPhysical();
                show();
                expandNode(event.getDropNode());
                this.dataEditor.getGalleryPanel().updateStripes();
                return;
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
     * Change the order of the MediaUnits in the workpiece.
     * When structure is saved to METS this is represented by the order of DIV elements in the physical structMap.
     * @param toElement logical element where to which the MediaUnits are assigned
     * @param elementsToBeMoved List of MediaUnits which are moved
     * @param insertionIndex index at which the MediaUnits are added to the existing List of MediaUnits.
     *                       The value -1 represents the end of the list.
     */
    void reorderMediaUnits(IncludedStructuralElement toElement,
                           List<Pair<View, IncludedStructuralElement>> elementsToBeMoved,
                           int insertionIndex) {
        int physicalInsertionIndex;
        List<MediaUnit> mediaUnitsToBeMoved = elementsToBeMoved.stream()
                .map(e -> e.getLeft().getMediaUnit())
                .collect(Collectors.toList());

        if (insertionIndex > toElement.getViews().size()) {
            Helper.setErrorMessage("Unsupported drag'n'drop operation: Insertion index exceeds list.");
            insertionIndex = -1;
        }

        if (insertionIndex < 0 || toElement.getViews().size() == 0) {
            // no insertion position was specified or the element does not contain any pages yet
            physicalInsertionIndex = toElement.getOrder() - 1;
        } else {
            // if 'insertionIndex' equals the size of the list, it means we want to append the moved pages _behind_ the media unit of
            // the last view in the list of views of the 'toElement'
            if (insertionIndex == toElement.getViews().size()) {
                physicalInsertionIndex = toElement.getViews().getLast().getMediaUnit().getOrder();
            } else if (insertionIndex == 0) {
                // insert at first position directly after logical element
                physicalInsertionIndex = toElement.getOrder() - 1;
            } else {
                // insert at given index
                physicalInsertionIndex = toElement.getViews().get(insertionIndex).getMediaUnit().getOrder() - 1;
            }
        }

        if (physicalInsertionIndex > mediaUnitsToBeMoved.stream()
                .map(MediaUnit::getOrder)
                .collect(Collectors.summarizingInt(Integer::intValue))
                .getMin() - 1) {
            int finalInsertionIndex = physicalInsertionIndex;
            physicalInsertionIndex -= (int) mediaUnitsToBeMoved.stream().filter(m -> m.getOrder() - 1 < finalInsertionIndex).count();
        }
        dataEditor.getWorkpiece().getMediaUnit().getChildren().removeAll(mediaUnitsToBeMoved);
        int numberOfChildren = dataEditor.getWorkpiece().getMediaUnit().getChildren().size();
        if (physicalInsertionIndex < numberOfChildren) {
            dataEditor.getWorkpiece().getMediaUnit().getChildren().addAll(physicalInsertionIndex, mediaUnitsToBeMoved);
        } else {
            dataEditor.getWorkpiece().getMediaUnit().getChildren().addAll(mediaUnitsToBeMoved);
            if (physicalInsertionIndex > numberOfChildren) {
                Helper.setErrorMessage("Could not append media at correct position. Index exceeded list.");
            }
        }
    }

    /**
     * Change order fields of physical elements. When saved to METS this is represented by the physical structMap divs' "ORDER" attribute.
     * @param toElement Logical element to which the physical elements are assigned. The physical elements' order follows the order of the
     *                  logical elements.
     * @param elementsToBeMoved List of physical elements to be moved
     */
    void changePhysicalOrderFields(IncludedStructuralElement toElement, List<Pair<View, IncludedStructuralElement>> elementsToBeMoved) {
        ServiceManager.getFileService().renumberMediaUnits(dataEditor.getWorkpiece(), false);
    }

    /**
     * Change the order attribute of the logical elements that are affected by pages around them being moved.
     * @param toElement logical element the pages will be assigned to
     * @param elementsToBeMoved physical elements which are moved
     */
    void changeLogicalOrderFields(IncludedStructuralElement toElement, List<Pair<View, IncludedStructuralElement>> elementsToBeMoved,
                                  int insertionIndex) {
        HashMap<Integer, List<IncludedStructuralElement>> logicalElementsByOrder = new HashMap<>();
        for (IncludedStructuralElement logicalElement : dataEditor.getWorkpiece().getAllIncludedStructuralElements()) {
            if (logicalElementsByOrder.containsKey(logicalElement.getOrder()))  {
                logicalElementsByOrder.get(logicalElement.getOrder()).add(logicalElement);
            } else {
                logicalElementsByOrder.put(logicalElement.getOrder(), new LinkedList<>(Collections.singletonList(logicalElement)));
            }
        }

        // Order values of moved pages and target element. Logical elements located between these Order values are affected.
        List<Integer> ordersAffectedByMove = getOrdersAffectedByMove(elementsToBeMoved, toElement);

        /* The new Order value for the logical elements can be calculated quite simple:
        The Order values of elements located before the target element have to be modified by -i - 1.
        The Order values of elements located after the target element have to be modified by the size of ordersAffectedByMove - i.
        (ordersAffectedByMove equals to the number of moved pages + the target element.)
         */

        for (Map.Entry<Integer, List<IncludedStructuralElement>> entry : logicalElementsByOrder.entrySet()) {
            for (int i = 0; i < ordersAffectedByMove.size() - 1; i++) {
                if (ordersAffectedByMove.get(i) < entry.getKey() && entry.getKey() < ordersAffectedByMove.get(i + 1)) {
                    if (ordersAffectedByMove.get(i) < toElement.getOrder()) {
                        updateOrder(entry.getValue(), -i - 1);
                    } else if (ordersAffectedByMove.get(i) > toElement.getOrder()) {
                        updateOrder(entry.getValue(), ordersAffectedByMove.size() - i);
                    }
                }
            }
            // check if elements exist with the same order like toElement (the toElememt itself might be affected as well)
            if (entry.getKey() == toElement.getOrder()) {
                List<IncludedStructuralElement> beforeToElement = entry.getValue().subList(0, entry.getValue().indexOf(toElement) + 1);
                List<IncludedStructuralElement> afterToElement = entry.getValue().subList(entry.getValue().indexOf(toElement) + 1,
                        entry.getValue().size());
                /* toElement at index 0 means we're in an edge case: toElement is the first order which is affected (no pages with smaller
                order affected) and its order will not change, nor will other elements with the same order before it. */
                if (ordersAffectedByMove.indexOf(toElement.getOrder()) > 0) {
                    updateOrder(beforeToElement, -ordersAffectedByMove.indexOf(entry.getKey()));
                }
                /* Order of elements directly after toElement (with same order) only have to be update if the pages are inserted at the
                first position. If they are inserted after any pages, the order of elements in afterToElement will not change. */
                if (insertionIndex == 0) {
                    updateOrder(afterToElement, elementsToBeMoved.size() - ordersAffectedByMove.indexOf(toElement.getOrder()));
                }
            }
        }
    }

    private List<Integer> getOrdersAffectedByMove(List<Pair<View, IncludedStructuralElement>> views, IncludedStructuralElement toElement) {
        Set<Integer> ordersAffectedByMove = views.stream()
                .map(e -> e.getLeft().getMediaUnit().getOrder())
                .collect(Collectors.toSet());
        ordersAffectedByMove.add(toElement.getOrder());
        return ordersAffectedByMove.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    private void updateOrder(List<IncludedStructuralElement> elementsToBeUpdated, int delta) {
        for (IncludedStructuralElement element : elementsToBeUpdated) {
            element.setOrder(element.getOrder() + delta);
        }
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

    private void checkLogicalDragDrop(StructureTreeNode dragNode, StructureTreeNode dropNode) throws Exception {

        IncludedStructuralElement dragStructure = (IncludedStructuralElement) dragNode.getDataObject();
        IncludedStructuralElement dropStructure = (IncludedStructuralElement) dropNode.getDataObject();

        StructuralElementViewInterface divisionView = dataEditor.getRulesetManagement().getStructuralElementView(
                dropStructure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());

        LinkedList<IncludedStructuralElement> dragParents;
        if (divisionView.getAllowedSubstructuralElements().containsKey(dragStructure.getType())) {
            dragParents = MetadataEditor.getAncestorsOfStructure(dragStructure,
                    dataEditor.getWorkpiece().getRootElement());
            if (!dragParents.isEmpty()) {
                IncludedStructuralElement parentStructure = dragParents.get(dragParents.size() - 1);
                if (parentStructure.getChildren().contains(dragStructure)) {
                    if (isSeparateMedia()) {
                        preserveLogical();
                    } else {
                        preserveLogicalAndPhysical();
                    }
                    this.dataEditor.getGalleryPanel().updateStripes();
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

        StructuralElementViewInterface divisionView = dataEditor.getRulesetManagement().getStructuralElementView(
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

    private void preserveLogicalAndPhysical() throws UnknownTreeNodeDataException {
        if (!this.logicalTree.getChildren().isEmpty()) {
            order = 1;
            for (MediaUnit mediaUnit : dataEditor.getWorkpiece().getMediaUnit().getChildren()) {
                mediaUnit.getIncludedStructuralElements().clear();
            }
            dataEditor.getWorkpiece().getMediaUnit().getChildren().clear();
            preserveLogicalAndPhysicalRecursive(this.logicalTree.getChildren().get(logicalTree.getChildCount() - 1));
        }
    }

    private IncludedStructuralElement preserveLogicalAndPhysicalRecursive(TreeNode treeNode) throws UnknownTreeNodeDataException {
        StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
        if (Objects.isNull(structureTreeNode) || !(structureTreeNode.getDataObject() instanceof IncludedStructuralElement)) {
            return null;
        }
        IncludedStructuralElement structure = (IncludedStructuralElement) structureTreeNode.getDataObject();
        structure.setOrder(order);
        structure.getViews().clear();
        structure.getChildren().clear();
        for (TreeNode child : treeNode.getChildren()) {
            if (!(child.getData() instanceof StructureTreeNode)) {
                throw new UnknownTreeNodeDataException(child.getData().getClass().getCanonicalName());
            }
            if (((StructureTreeNode) child.getData()).getDataObject() instanceof IncludedStructuralElement) {
                IncludedStructuralElement possibleChildStructure = preserveLogicalAndPhysicalRecursive(child);
                if (Objects.nonNull(possibleChildStructure)) {
                    structure.getChildren().add(possibleChildStructure);
                }
            } else if (((StructureTreeNode) child.getData()).getDataObject() instanceof View) {
                View view = (View) ((StructureTreeNode) child.getData()).getDataObject();
                structure.getViews().add(view);
                if (!dataEditor.getWorkpiece().getAllMediaUnits().contains(view.getMediaUnit())) {
                    view.getMediaUnit().setOrder(order);
                    dataEditor.getWorkpiece().getMediaUnit().getChildren().add(view.getMediaUnit());
                    order++;
                }
                if (!view.getMediaUnit().getIncludedStructuralElements().contains(structure)) {
                    view.getMediaUnit().getIncludedStructuralElements().add(structure);
                }
            }
        }
        return structure;
    }

    /**
     * Check and return whether the metadata of a process should be displayed in separate logical and physical
     * structure trees or in one unified structure tree.
     *
     * @return
     *          whether metadata structure should be displayed in separate structure trees or not
     */
    public boolean isSeparateMedia() {
        return this.dataEditor.getProcess().getTemplate().getWorkflow().isSeparateStructure();
    }

    private void expandNode(TreeNode node) {
        if (Objects.nonNull(node)) {
            node.setExpanded(true);
            expandNode(node.getParent());
        }
    }

    private HashMap<IncludedStructuralElement, Boolean> getLogicalTreeNodeExpansionStates(DefaultTreeNode tree) {
        if (Objects.nonNull(tree) && tree.getChildCount() == 1) {
            TreeNode treeRoot = tree.getChildren().get(0);
            IncludedStructuralElement structuralElement = getTreeNodeStructuralElement(treeRoot);
            if (Objects.nonNull(structuralElement)) {
                return getLogicalTreeNodeExpansionStatesRecursively(treeRoot, new HashMap<>());
            }
        }
        return new HashMap<>();
    }

    private HashMap<IncludedStructuralElement, Boolean> getLogicalTreeNodeExpansionStatesRecursively(TreeNode treeNode,
            HashMap<IncludedStructuralElement, Boolean> expansionStates) {
        if (Objects.nonNull(treeNode)) {
            IncludedStructuralElement structureData = getTreeNodeStructuralElement(treeNode);
            if (Objects.nonNull(structureData)) {
                expansionStates.put(structureData, treeNode.isExpanded());
                for (TreeNode childNode : treeNode.getChildren()) {
                    expansionStates.putAll(getLogicalTreeNodeExpansionStatesRecursively(childNode, expansionStates));
                }
            }
        }
        return expansionStates;
    }

    private HashMap<MediaUnit, Boolean> getPhysicalTreeNodeExpansionStates(DefaultTreeNode tree) {
        if (Objects.nonNull(tree) && tree.getChildCount() == 1) {
            TreeNode treeRoot = tree.getChildren().get(0);
            MediaUnit mediaUnit = getTreeNodeMediaUnit(treeRoot);
            if (Objects.nonNull(mediaUnit)) {
                return getPhysicalTreeNodeExpansionStatesRecursively(treeRoot, new HashMap<>());
            }
        }
        return new HashMap<>();
    }

    private HashMap<MediaUnit, Boolean> getPhysicalTreeNodeExpansionStatesRecursively(TreeNode treeNode,
            HashMap<MediaUnit, Boolean> expansionStates) {
        if (Objects.nonNull(treeNode)) {
            MediaUnit mediaUnit = getTreeNodeMediaUnit(treeNode);
            if (Objects.nonNull(mediaUnit)) {
                expansionStates.put(mediaUnit, treeNode.isExpanded());
                for (TreeNode childNode : treeNode.getChildren()) {
                    expansionStates.putAll(getPhysicalTreeNodeExpansionStatesRecursively(childNode, expansionStates));
                }
            }
        }
        return expansionStates;
    }

    private void updateLogicalNodeExpansionStates(DefaultTreeNode tree, HashMap<IncludedStructuralElement, Boolean> expansionStates) {
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

    private void updatePhysicalNodeExpansionStates(DefaultTreeNode tree, HashMap<MediaUnit, Boolean> expansionStates) {
        if (Objects.nonNull(tree) && Objects.nonNull(expansionStates) && !expansionStates.isEmpty()) {
            updatePhysicalNodeExpansionStatesRecursively(tree, expansionStates);
        }
    }

    private void updatePhysicalNodeExpansionStatesRecursively(TreeNode treeNode, HashMap<MediaUnit, Boolean> expansionStates) {
        MediaUnit mediaUnit = getTreeNodeMediaUnit(treeNode);
        if (Objects.nonNull(mediaUnit) && expansionStates.containsKey(mediaUnit)) {
            treeNode.setExpanded(expansionStates.get(mediaUnit));
        }
        for (TreeNode childNode : treeNode.getChildren()) {
            updatePhysicalNodeExpansionStatesRecursively(childNode, expansionStates);
        }
    }

    private boolean logicalNodeStateUnknown(HashMap<IncludedStructuralElement, Boolean> expansionStates, TreeNode treeNode) {
        IncludedStructuralElement element = getTreeNodeStructuralElement(treeNode);
        return !Objects.nonNull(expansionStates) || (Objects.nonNull(element) && !expansionStates.containsKey(element));
    }

    private boolean physicalNodeStateUnknown(HashMap<MediaUnit, Boolean> expanionStates, TreeNode treeNode) {
        MediaUnit mediaUnit = getTreeNodeMediaUnit(treeNode);
        return Objects.isNull(expanionStates) || (Objects.nonNull(mediaUnit) && !expanionStates.containsKey(mediaUnit));
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

    private MediaUnit getTreeNodeMediaUnit(TreeNode treeNode) {
        if (treeNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) treeNode.getData();
            if (structureTreeNode.getDataObject() instanceof MediaUnit) {
                return (MediaUnit) structureTreeNode.getDataObject();
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
     * Get activeTabs.
     *
     * @return value of activeTabs
     */
    public String getActiveTabs() {
        return activeTabs;
    }

    /**
     * Set activeTabs.
     *
     * @param activeTabs as java.lang.String
     */
    public void setActiveTabs(String activeTabs) {
        this.activeTabs = activeTabs;
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
                List<TreeNode> viewSiblings = selectedLogicalNode.getParent().getChildren();
                // check for selected node's positions and siblings after selected node's parent
                if (viewSiblings.indexOf(selectedLogicalNode) == viewSiblings.size() - 1
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
            View viewToAssign = new View();
            viewToAssign.setMediaUnit(view.getMediaUnit());
            List<TreeNode> logicalNodeSiblings = selectedLogicalNode.getParent().getParent().getChildren();
            int logicalNodeIndex = logicalNodeSiblings.indexOf(selectedLogicalNode.getParent());
            TreeNode nextSibling = logicalNodeSiblings.get(logicalNodeIndex + 1);
            StructureTreeNode structureTreeNodeSibling = (StructureTreeNode) nextSibling.getData();
            IncludedStructuralElement includedStructuralElement = (IncludedStructuralElement) structureTreeNodeSibling.getDataObject();
            dataEditor.assignView(includedStructuralElement, viewToAssign, 0);
            severalAssignments.add(viewToAssign.getMediaUnit());
            show();
            dataEditor.getSelectedMedia().clear();
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
                    show();
                    dataEditor.getGalleryPanel().updateStripes();
                }
            }
        }
    }
}
