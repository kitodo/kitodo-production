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

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataEditor;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class StructurePanel implements Serializable {

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
            Set<MediaUnit> mediaUnitsShowingOnTheStructureTree = result.getRight().parallelStream()
                    .map(View::getMediaUnit).collect(Collectors.toSet());
            this.physicalTree = buildMediaTree(dataEditor.getWorkpiece().getMediaUnit(),
                mediaUnitsShowingOnTheStructureTree);
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

        DefaultTreeNode result = new DefaultTreeNode();
        result.setExpanded(true);
        Collection<View> viewsShowingOnAChild = buildStructureTreeRecursively(structure, result);
        return Pair.of(Collections.singletonList(result), viewsShowingOnAChild);
    }

    private Collection<View> buildStructureTreeRecursively(IncludedStructuralElement structure, TreeNode result) {

        StructuralElementViewInterface divisionView = dataEditor.getRuleset().getStructuralElementView(
            structure.getType(), dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        /*
         * Creating the tree node by handing over the parent node automatically
         * appends it to the parent as a child. That’s the logic of the JSF
         * framework. So you do not have to add the result anywhere.
         */
        DefaultTreeNode parent = new DefaultTreeNode(
                new StructureTreeNode(this, divisionView.getLabel(), divisionView.isUndefined(), false, structure),
                result);
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
     * Creates the media tree.
     *
     * @param mediaRoot
     *            root of media units to show on the tree
     * @param mediaUnitsShowingOnTheStructureTree
     *            media units already showing on the structure tree. In mixed
     *            mode, only media units not yet linked anywhere will show in
     *            the second tree as unlinked media.
     *
     * @return the media tree
     */
    private DefaultTreeNode buildMediaTree(MediaUnit mediaRoot,
            Collection<MediaUnit> mediaUnitsShowingOnTheStructureTree) {
        DefaultTreeNode rootTreeNode = new DefaultTreeNode();
        rootTreeNode.setExpanded(true);

        /*
         * Creating the tree node by handing over the parent node automatically
         * appends it to the parent as a child. That's the logic of the JSF
         * framework. So you do not have to add the result anywhere.
         */
        /*DefaultTreeNode mediaTreeRoot = new DefaultTreeNode(new StructureTreeNode(this,
                Helper.getTranslation(separateMedia ? "dataEditor.mediaTree" : "dataEditor.unlinkedMediaTree"), false,
                false, mediaRoot.getChildren()), rootTreeNode);
        mediaTreeRoot.setExpanded(true);
        */

        buildMediaTreeRecursively(mediaRoot, rootTreeNode);

        /*
        String page = Helper.getTranslation("page").concat(" ");
        boolean isEmpty = true;
        for (MediaUnit mediaUnit : mediaUnits) {
            if (Boolean.TRUE.equals(separateMedia) || !mediaUnitsShowingOnTheStructureTree.contains(mediaUnit)) {
                new DefaultTreeNode(
                        new StructureTreeNode(this, page.concat(mediaUnit.getOrderlabel()), false, false, mediaUnit),
                        mediaTreeRoot).setExpanded(true);
                isEmpty = false;
            }
        }
        return !isEmpty ? rootTreeNode : null;
        */
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

    void treeLogicalSelect() {
        /*
         * The newly selected element has already been set in 'selectedLogicalNode' by
         * JSF at this point.
         */
        try {
            dataEditor.switchStructure();
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

    private void updatePhysicalNodeSelection(GalleryMediaContent galleryMediaContent) {
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

    private void updateLogicalNodeSelection(GalleryMediaContent galleryMediaContent) {
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
