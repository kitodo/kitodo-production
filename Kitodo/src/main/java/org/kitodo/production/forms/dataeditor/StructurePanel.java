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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.View;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.services.ServiceManager;
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

    /**
     * Whether the media shall be shown separately in a second tree. If false,
     * the media—if linked—will be merged shown within the structure tree.
     */
    private Boolean separateMedia = Boolean.FALSE;

    private IncludedStructuralElement structure;

    /**
     * The logical structure tree of the edited document.
     */
    private LinkedList<DefaultTreeNode> logicalTrees;

    /**
     * The physical structure tree of the edited document.
     */
    private DefaultTreeNode physicalTree = null;

    /**
     * Creates a new structure panel.
     *
     * @param dataEditor
     *            the master meta-data editor
     */
    StructurePanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Clear content.
     */
    public void clear() {
        logicalTrees = null;
        physicalTree = null;
        selectedLogicalNode = null;
        selectedPhysicalNode = null;
        previouslySelectedLogicalNode = null;
        previouslySelectedPhysicalNode = null;
        structure = null;
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
        parent.getChildren().remove(selectedStructure.get());
        show();
        dataEditor.getGalleryPanel().updateStripes();
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
    public void selectMediaUnit(MediaUnit mediaUnit) {
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
     * Get parentTrees.
     *
     * @return value of parentTrees
     */
    public List<DefaultTreeNode> getParentTrees() {
        return logicalTrees.subList(0, logicalTrees.size() - 1);
    }

    /**
     * Get logicalTree.
     *
     * @return value of logicalTree
     */
    public DefaultTreeNode getLogicalTree() {
        return logicalTrees.getLast();
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
        if (!logicalTrees.getLast().getChildren().isEmpty()) {
            preserveLogicalRecursive(logicalTrees.getLast().getChildren().get(0));
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
        this.logicalTrees = result.getLeft();
        if (separateMedia != null) {
            this.physicalTree = buildMediaTree(dataEditor.getWorkpiece().getMediaUnit());
        }
        this.selectedLogicalNode = logicalTrees.getLast().getChildren().get(0);
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
        main.setExpanded(true);
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
            node = new StructureTreeNode(this, divisionView.getLabel(), divisionView.isUndefined(), false, structure);
        } else {
            node = new StructureTreeNode(this, structure.getLink().getUri().toString(), true, true, structure);
            for (Process child : dataEditor.getCurrentChildren()) {
                try {
                    String type = ServiceManager.getProcessService().getBaseType(child);
                    if (child.getId() == ServiceManager.getProcessService()
                            .processIdFromUri(structure.getLink().getUri())) {
                        StructuralElementViewInterface view = dataEditor.getRuleset().getStructuralElementView(
                            type, dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                        node = new StructureTreeNode(this, view.getLabel(), view.isUndefined(), true, structure);
                    }
                } catch (IOException e) {
                    Helper.setErrorMessage("metadataReadError", e.getMessage(), logger, e);
                    node = new StructureTreeNode(this, child.getTitle(), true, true, child);
                }
            }
        }
        /*
         * Creating the tree node by handing over the parent node automatically
         * appends it to the parent as a child. That’s the logic of the JSF
         * framework. So you do not have to add the result anywhere.
         */
        DefaultTreeNode parent = new DefaultTreeNode(node, result);
        parent.setExpanded(true);

        Set<View> viewsShowingOnAChild = new HashSet<>();
        for (IncludedStructuralElement child : structure.getChildren()) {
            viewsShowingOnAChild.addAll(buildStructureTreeRecursively(child, parent));
        }

        if (Boolean.FALSE.equals(separateMedia)) {
            String page = Helper.getTranslation("page").concat(" ");
            for (View view : structure.getViews()) {
                if (!viewsShowingOnAChild.contains(view)
                        && Objects.nonNull(view.getMediaUnit())
                        && Objects.nonNull(view.getMediaUnit().getOrderlabel())) {
                    addTreeNode(page.concat(view.getMediaUnit().getOrderlabel()), false, false, view, parent);
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
        DefaultTreeNode node = new DefaultTreeNode(new StructureTreeNode(this, label, undefined, linked, dataObject),
                parent);
        node.setExpanded(true);
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
        tree.setExpanded(true);
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
             * Error case: The meta-data file of the parent process cannot be
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
        rootTreeNode.setExpanded(true);
        buildMediaTreeRecursively(mediaRoot, rootTreeNode);
        return rootTreeNode;
    }

    private void buildMediaTreeRecursively(MediaUnit mediaUnit, DefaultTreeNode parentTreeNode) {
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                mediaUnit.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        DefaultTreeNode treeNode = addTreeNode(divisionView.getLabel(), false, false, mediaUnit, parentTreeNode);
        treeNode.setExpanded(true);
        if (Objects.nonNull(mediaUnit.getChildren())) {
            for (MediaUnit child : mediaUnit.getChildren()) {
                buildMediaTreeRecursively(child, treeNode);
            }
        }
    }

    void treeLogicalSelect(Object treeNodeData) {
        /*
         * The newly selected element has already been set in 'selectedLogicalNode' by
         * JSF at this point.
         */
        try {
            dataEditor.switchStructure(treeNodeData);
            previouslySelectedLogicalNode = selectedLogicalNode;
        } catch (NoSuchMetadataFieldException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            selectedLogicalNode = previouslySelectedLogicalNode;
        }
    }

    void treePhysicalSelect() {
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

    void updatePhysicalNodeSelection(TreeNode treeNode) {
        if (this.separateMedia) {
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
        if (Objects.nonNull(logicalTrees)) {
            GalleryStripe matchingGalleryStripe = this.dataEditor.getGalleryPanel().getLogicalStructureOfMedia(galleryMediaContent);
            if (Objects.nonNull(matchingGalleryStripe) && Objects.nonNull(matchingGalleryStripe.getStructure())) {
                if (this.separateMedia) {
                    TreeNode selectedLogicalTreeNode =
                            updateLogicalNodeSelectionRecursive(matchingGalleryStripe.getStructure(),
                                logicalTrees.getLast());
                    if (Objects.nonNull(selectedLogicalTreeNode)) {
                        setSelectedLogicalNode(selectedLogicalTreeNode);
                    } else {
                        Helper.setErrorMessage("Unable to update node selection in logical structure!");
                    }
                } else {
                    TreeNode selectedTreeNode = updateNodeSelectionRecursive(galleryMediaContent,
                        logicalTrees.getLast());
                    if (Objects.nonNull(selectedTreeNode)) {
                        setSelectedLogicalNode(selectedTreeNode);
                    } else {
                        Helper.setErrorMessage("Unable to update node selection in logical structure!");
                    }
                }
            }
        }
    }

    private TreeNode updateLogicalNodeSelectionRecursive(IncludedStructuralElement structure, TreeNode treeNode) {
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
            if (Objects.nonNull(mediaUnit) && mediaUnit.getMediaFiles().size() > 0) {
                Map<MediaVariant, URI> mediaVariants = mediaUnit.getMediaFiles();
                return mediaVariants.values().contains(galleryMediaContent.getPreviewUri());
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
     * Callback function triggered on TreeDragDropEvent. Checks whether performed drag'n'drop action is allowed
     * considering ruleset restrictions on structure hierarchy. In case some ruleset rules were violated by the action
     * displays a corresponding error message to the user and reverts tree to prior state.
     *
     * @param event TreeDragDropEvent
     *              event triggering this callback function
     */
    public void onDragDrop(TreeDragDropEvent event) {

        TreeNode dragTreeNode = event.getDragNode();
        TreeNode dropTreeNode = event.getDropNode();

        Object dragNodeObject = dragTreeNode.getData();
        Object dropNodeObject = dropTreeNode.getData();

        if (!(dragNodeObject instanceof StructureTreeNode) || !(dropNodeObject instanceof StructureTreeNode)) {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.unableToMoveError"));
            return;
        }
        StructureTreeNode dropNode = (StructureTreeNode) dropNodeObject;
        StructureTreeNode dragNode = (StructureTreeNode) dragNodeObject;


        try {
            if (dragNode.getDataObject() instanceof IncludedStructuralElement
                    && dropNode.getDataObject() instanceof IncludedStructuralElement) {
                checkLogicalDragDrop(dragNode, dropNode);
            } else if (dragNode.getDataObject() instanceof MediaUnit
                    && dropNode.getDataObject() instanceof MediaUnit) {
                checkPhysicalDragDrop(dragNode, dropNode);
            } else {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.dragnDropError", Arrays.asList(
                        dragNode.getLabel(), dropNode.getLabel())));
            }
        } catch (ClassCastException exception) {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.unableToMoveError"));
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

    public boolean isSeparateMedia() {
        return this.separateMedia;
    }

    public void setSeparateMedia(boolean separateMedia) {
        this.separateMedia = separateMedia;
    }
}
