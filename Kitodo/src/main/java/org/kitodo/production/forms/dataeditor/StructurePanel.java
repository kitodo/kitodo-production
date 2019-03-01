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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class StructurePanel {

    private DataEditorForm dataEditor;

    /**
     * The logical structure tree of the edited document.
     */
    private DefaultTreeNode structureTree;

    /**
     * List of structure trees to be displayed. This list contains a tree for
     * each linked logical parent, then the logical tree, and last, in mixed
     * mode, a tree with all the unlinked media, or in separate mode, the
     * physical tree.
     */
    private final List<DefaultTreeNode> trees = new ArrayList<>();

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
     * Whether the media shall be shown separately in a second tree. If false,
     * the media—if linked—will be merged shown within the structure tree.
     */
    private Boolean separateMedia = Boolean.FALSE;

    private TreeNode selectedNode;

    /**
     * If changing the tree node fails, we need this value to undo the user’s
     * select action.
     */
    private TreeNode previouslySelectedNode;

    /**
     * Creates the media tree.
     *
     * @param mediaUnits
     *            media units to show on the tree
     * @param mediaUnitsShowingOnTheStructureTree
     *            media units already showing on the structure tree. In mixed
     *            mode, only media units not yet linked anywhere will show in
     *            the second tree as unlinked media.
     *
     * @return the media tree
     */
    private DefaultTreeNode buildMediaTree(List<MediaUnit> mediaUnits,
            Collection<MediaUnit> mediaUnitsShowingOnTheStructureTree) {
        DefaultTreeNode result = new DefaultTreeNode();
        result.setExpanded(true);

        /*
         * Creating the tree node by handing over the parent node automatically
         * appends it to the parent as a child. That's the logic of the JSF
         * framework. So you do not have to add the result anywhere.
         */
        DefaultTreeNode mediaTreeRoot = new DefaultTreeNode(new StructureTreeNode(this,
                Helper.getTranslation(separateMedia ? "dataEditor.mediaTree" : "dataEditor.unlikedMediaTree"), false,
                false, mediaUnits), result);
        mediaTreeRoot.setExpanded(true);

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
        return !isEmpty ? result : null;
    }

    /**
     * Creates the structure tree. If hierarchical links exist upwards, they are
     * displayed above the tree as separate trees.
     *
     * @param structure
     *            the structure from which to create the JSF tree
     * @return the structure tree(s) and the collection of views displayed in
     *         the tree
     */
    private Pair<List<DefaultTreeNode>, Collection<View>> buildStructureTree(Structure structure) {

        DefaultTreeNode result = new DefaultTreeNode();
        result.setExpanded(true);
        Collection<View> viewsShowingOnAChild = buildStructureTreeRecursively(structure, result);
        return Pair.of(Arrays.asList(result), viewsShowingOnAChild);
    }

    Structure getSelectedStructure() {
        StructureTreeNode structureTreeNode = (StructureTreeNode) selectedNode.getData();
        Object dataObject = structureTreeNode.getDataObject();
        return dataObject instanceof Structure ? (Structure) dataObject : null;
    }

    private Collection<View> buildStructureTreeRecursively(Structure structure, TreeNode result) {

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
        for (Structure child : structure.getChildren()) {
            viewsShowingOnAChild.addAll(buildStructureTreeRecursively(child, parent));
        }

        if (Boolean.FALSE.equals(separateMedia)) {
            String page = Helper.getTranslation("page").concat(" ");
            for (View view : structure.getViews()) {
                if (!viewsShowingOnAChild.contains(view)) {
                    new DefaultTreeNode(new StructureTreeNode(this, page.concat(view.getMediaUnit().getOrderlabel()),
                            false, false, view), parent).setExpanded(true);
                    viewsShowingOnAChild.add(view);
                }
            }
        }
        return viewsShowingOnAChild;
    }

    public List<DefaultTreeNode> getTrees() {
        return trees;
    }

    /**
     * Loads the tree(s) into the panel and sets the selected element to the
     * root element of the structure tree.
     *
     * @param workpiece
     *            workpiece to load
     */
    void show(Workpiece workpiece) {
        trees.clear();
        Pair<List<DefaultTreeNode>, Collection<View>> result = buildStructureTree(workpiece.getStructure());
        trees.addAll(result.getLeft());
        if (separateMedia != null) {
            Set<MediaUnit> mediaUnitsShowingOnTheStructureTree = result.getRight().parallelStream()
                    .map(View::getMediaUnit).collect(Collectors.toSet());
            DefaultTreeNode mediaTree = buildMediaTree(workpiece.getMediaUnits(), mediaUnitsShowingOnTheStructureTree);
            if (mediaTree != null) {
                trees.add(mediaTree);
            }
        }
        this.structureTree = trees.get(result.getLeft().size() - 1);
        this.selectedNode = structureTree.getChildren().get(0);
        this.previouslySelectedNode = selectedNode;
    }

    void treeElementSelect() {
        /*
         * The newly selected element has already been set in 'selectedNode' by
         * JSF at this point.
         */
        try {
            dataEditor.switchTheMetadataPanelTo(getSelectedStructure());
            previouslySelectedNode = selectedNode;
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            selectedNode = previouslySelectedNode;
        }
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selected) {
        if (Objects.nonNull(selected)) {
            this.selectedNode = selected;
        }
    }
}
