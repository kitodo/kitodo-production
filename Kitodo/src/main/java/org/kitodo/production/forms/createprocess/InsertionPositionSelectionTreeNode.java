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

package org.kitodo.production.forms.createprocess;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * A node in the tree to select the insertion position for the child to create.
 */
public class InsertionPositionSelectionTreeNode extends DefaultTreeNode {

    private int itemIndex;
    private String label;
    private boolean possibleInsertionPosition;

    /**
     * Creates a new node for a radio button.
     *
     * @param parent
     *            parent node in the tree view
     * @param itemIndex
     *            index of the radio button
     */
    InsertionPositionSelectionTreeNode(TreeNode parent, int itemIndex) {
        super(null, parent);
        this.itemIndex = itemIndex;
        this.possibleInsertionPosition = true;
        super.setData(this);
    }

    /**
     * Creates a node for an element of the hierarchical document root node.
     *
     * @param parent
     *            parent node in the tree view
     * @param label
     *            label for the included structural element
     */
    InsertionPositionSelectionTreeNode(TreeNode parent, String label) {
        super(null, parent);
        this.label = label;
        this.possibleInsertionPosition = false;
        super.setData(this);
        setExpanded(true);
    }

    /**
     * Returns the index for the radio button.
     *
     * @return the index for the radio button
     */
    public int getItemIndex() {
        return itemIndex;
    }

    /**
     * Returns the label for an existing included structural element.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns if this is a possible insertion position.
     *
     * @return if this is a possible insertion position
     */
    public boolean isPossibleInsertionPosition() {
        return possibleInsertionPosition;
    }
}
