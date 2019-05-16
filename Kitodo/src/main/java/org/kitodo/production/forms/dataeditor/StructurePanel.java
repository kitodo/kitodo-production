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
import java.util.Collection;
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
    private DefaultTreeNode logicalTree;

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
        logicalTree = null;
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
        this.selectedPhysicalNode = selectedPhysicalNode;
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
     * Get logicalTree.
     *
     * @return value of logicalTree
     */
    public DefaultTreeNode getLogicalTree() {
        return logicalTree;
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
        if (!logicalTree.getChildren().isEmpty()) {
            preserveLogicalRecursive(logicalTree.getChildren().get(0));
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
     */
    public void show() {
        this.structure = dataEditor.getWorkpiece().getRootElement();
        Pair<List<DefaultTreeNode>, Collection<View>> result = buildStructureTree();
        this.logicalTree = result.getLeft().get(result.getLeft().size() - 1); // TODO size() - 1 might be dangerous
        if (separateMedia != null) {
            this.physicalTree = buildMediaTree(dataEditor.getWorkpiece().getMediaUnit());
        }
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
    private Pair<List<DefaultTreeNode>, Collection<View>> buildStructureTree() {
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
            for (Process child : dataEditor.getProcess().getChildren()) {
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
                    new DefaultTreeNode(new StructureTreeNode(this, page.concat(view.getMediaUnit().getOrderlabel()),
                            false, false, view), parent).setExpanded(true);
                    viewsShowingOnAChild.add(view);
                }
            }
        }
        return viewsShowingOnAChild;
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
        if (Objects.nonNull(parent)) {
            URI uri = ServiceManager.getProcessService().getMetadataFileUri(parent);
            DefaultTreeNode tree = new DefaultTreeNode();
            tree.setExpanded(true);
            try {
                List<IncludedStructuralElement> includedStructuralElementList = determineIncludedStructuralElementPathToChildRecursive(
                    ServiceManager.getMetsService().loadWorkpiece(uri).getRootElement(), child.getId());
                DefaultTreeNode parentNode = tree;
                if (Objects.isNull(includedStructuralElementList)) {
                    new DefaultTreeNode(new StructureTreeNode(this, parent.getTitle(), true, true, parent), tree);
                } else {
                    for (IncludedStructuralElement includedStructuralElement : includedStructuralElementList) {
                        StructuralElementViewInterface structuralElementView = dataEditor.getRuleset()
                                .getStructuralElementView(includedStructuralElement.getType(),
                                    dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
                        parentNode = new DefaultTreeNode(
                                new StructureTreeNode(this, structuralElementView.getLabel(), false, true, null),
                                parentNode);
                        parentNode.setExpanded(true);
                    }
                }
            } catch (IOException e) {
                Helper.setErrorMessage("metadataReadError", e.getMessage(), logger, e);
                new DefaultTreeNode(new StructureTreeNode(this, parent.getTitle(), true, true, parent), tree);
            }
            result.addFirst(tree);
            addParentLinksRecursive(parent, result);
        }
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
    private LinkedList<IncludedStructuralElement> determineIncludedStructuralElementPathToChildRecursive(
            IncludedStructuralElement includedStructuralElement, int number) {

        if (!Objects.isNull(includedStructuralElement.getLink())) {
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
            LinkedList<IncludedStructuralElement> includedStructuralElementList = determineIncludedStructuralElementPathToChildRecursive(
                includedStructuralElementChild, number);
            if (Objects.nonNull(includedStructuralElementList)) {
                includedStructuralElementList.addFirst(includedStructuralElementChild);
                return includedStructuralElementList;
            }
        }
        return null;
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
        String displayLabel = mediaUnit.getType(); // TODO translate type with ruleset
        DefaultTreeNode treeNode = new DefaultTreeNode(new StructureTreeNode(this,
                displayLabel, false, false, mediaUnit), parentTreeNode);
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
        } catch (NoSuchMetadataFieldException | InvalidMetadataValueException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
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
        } catch (NoSuchMetadataFieldException | InvalidMetadataValueException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            selectedPhysicalNode = previouslySelectedPhysicalNode;
        }
    }

    void updateNodeSelection(GalleryMediaContent galleryMediaContent) {
        this.updateLogicalNodeSelection(galleryMediaContent);
        this.updatePhysicalNodeSelection(galleryMediaContent);
    }

    void updatePhysicalNodeSelection(GalleryMediaContent galleryMediaContent) {
        if (this.separateMedia) {
            if (Objects.nonNull(previouslySelectedPhysicalNode)) {
                previouslySelectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(selectedPhysicalNode)) {
                selectedPhysicalNode.setSelected(false);
            }
            if (Objects.nonNull(physicalTree)) {
                TreeNode selectedTreeNode = updateNodeSelectionRecursive(galleryMediaContent, physicalTree);
                if (Objects.nonNull(selectedTreeNode)) {
                    setSelectedPhysicalNode(selectedTreeNode);
                } else {
                    Helper.setErrorMessage("Unable to update Node selection in physical structure!");
                }
            }
        }
    }

    void updateLogicalNodeSelection(GalleryMediaContent galleryMediaContent) {
        if (Objects.nonNull(previouslySelectedLogicalNode)) {
            previouslySelectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(selectedLogicalNode)) {
            selectedLogicalNode.setSelected(false);
        }
        if (Objects.nonNull(logicalTree)) {
            GalleryStripe matchingGalleryStripe = this.dataEditor.getGalleryPanel().getLogicalStructureOfMedia(galleryMediaContent);
            if (Objects.nonNull(matchingGalleryStripe) && Objects.nonNull(matchingGalleryStripe.getStructure())) {
                if (this.separateMedia) {
                    TreeNode selectedLogicalTreeNode =
                            updateLogicalNodeSelectionRecursive(matchingGalleryStripe.getStructure(), logicalTree);
                    if (Objects.nonNull(selectedLogicalTreeNode)) {
                        setSelectedLogicalNode(selectedLogicalTreeNode);
                    } else {
                        Helper.setErrorMessage("Unable to update node selection in logical structure!");
                    }
                } else {
                    TreeNode selectedTreeNode = updateNodeSelectionRecursive(galleryMediaContent, logicalTree);
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
            Helper.setErrorMessage("Unable to move structure element!");
            return;
        }
        StructureTreeNode dropNode = (StructureTreeNode) dropNodeObject;
        StructureTreeNode dragNode = (StructureTreeNode) dragNodeObject;

        if (dropNode.getDataObject() instanceof IncludedStructuralElement
                && dragNode.getDataObject() instanceof IncludedStructuralElement) {
            checkLogicalDragDrop((IncludedStructuralElement) dragNode.getDataObject(),
                    (IncludedStructuralElement) dropNode.getDataObject());
        } else if ((dropNode.getDataObject()) instanceof MediaUnit && dropNode.getDataObject() instanceof MediaUnit) {
            checkPhysicalDragDrop((MediaUnit) dragNode.getDataObject(), (MediaUnit) dropNode.getDataObject());
        } else {
            Helper.setErrorMessage("Unable to move structure element!");
        }
    }

    private void checkLogicalDragDrop(IncludedStructuralElement dragStructure,
                                      IncludedStructuralElement dropStructure) {
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                dropStructure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());

        LinkedList<IncludedStructuralElement> dragParents;
        if (divisionView.getAllowedSubstructuralElements().containsKey(dragStructure.getType())) {
            dragParents = MetadataEditor.getAncestorsOfStructure(dragStructure, dataEditor.getWorkpiece().getRootElement());
            if (!dragParents.isEmpty()) {
                IncludedStructuralElement parentStructure = dragParents.get(dragParents.size() - 1);
                if (parentStructure.getChildren().contains(dragStructure)) {
                    preserveLogical();
                    return;
                } else {
                    Helper.setErrorMessage("Parents of structure " + dragStructure.getType()
                            + " do not contain structure!");
                }
            } else {
                Helper.setErrorMessage("No parents of structure " + dragStructure.getType() + " found!");
            }
        } else {
            Helper.setErrorMessage("Structure of type '" + dragStructure.getType()
                    + "' NOT allowed as child of structure of type '" + dropStructure.getType() + "'! ");
        }
        show();
    }

    private void checkPhysicalDragDrop(MediaUnit dragUnit, MediaUnit dropUnit) {
        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
                dropUnit.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());

        LinkedList<MediaUnit> dragParents;
        if (divisionView.getAllowedSubstructuralElements().containsKey(dragUnit.getType())) {
            dragParents = MetadataEditor.getAncestorsOfMediaUnit(dragUnit, dataEditor.getWorkpiece().getMediaUnit());
            if (dragParents.isEmpty()) {
                Helper.setErrorMessage("No parents of media unit " + dragUnit.getType() + " found!");
            } else {
                MediaUnit parentUnit = dragParents.get(dragParents.size() - 1);
                if (parentUnit.getChildren().contains(dragUnit)) {
                    preservePhysical();
                    return;
                } else {
                    Helper.setErrorMessage("Parents of media unit " + dragUnit.getType() + " do not contain media "
                            + "unit!");
                }
            }
        } else {
            Helper.setErrorMessage("Media unit of type '" + dragUnit.getType()
                    + "' NOT allowed as child of media unit of type '" + dropUnit.getType() + "!");
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
