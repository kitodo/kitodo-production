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

import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.primefaces.event.NodeSelectEvent;

public class StructureTreeNode implements Serializable {

    private final Object dataObject;
    private final String label;
    private final boolean linked;
    private final StructurePanel structurePanel;
    private final boolean undefined;

    StructureTreeNode(StructurePanel structurePanel, String label, boolean undefined, boolean linked,
            Object dataObject) {
        this.structurePanel = structurePanel;
        this.label = label;
        this.undefined = undefined;
        this.linked = linked;
        this.dataObject = dataObject;
    }

    /**
     * Return dataObject.
     *
     * @return dataObject
     */
    public Object getDataObject() {
        return dataObject;
    }

    /**
     * Return label.
     *
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return linked.
     *
     * @return linked
     */
    public boolean isLinked() {
        return linked;
    }

    /**
     * Return undefined.
     *
     * @return undefined
     */
    public boolean isUndefined() {
        return undefined;
    }

    /**
     * Return label of dataObject if dataObject is instance of MediaUnit or IncludedStructuralElement.
     *
     * @return label
     */
    public String getOrderLabel() {
        if (this.dataObject instanceof MediaUnit) {
            return ((MediaUnit) this.dataObject).getOrderlabel();
        } else if (this.dataObject instanceof IncludedStructuralElement) {
            return ((IncludedStructuralElement) this.dataObject).getOrderlabel();
        } else {
            return "";
        }
    }

    /**
     * Callback function triggered when a node is selected in the logical structure tree.
     *
     * @param event
     *            NodeSelectEvent triggered by logical node being selected
     */
    public void treeLogicalSelect(NodeSelectEvent event) {
        structurePanel.treeLogicalSelect(event.getTreeNode().getData());
    }

    /**
     * Callback function triggered when a node is selected in the physical structure tree.
     *
     * @param event
     *            NodeSelectEvent triggered by logical node being selected
     */
    public void treePhysicalSelect(NodeSelectEvent event) {
        structurePanel.treePhysicalSelect();
    }

}
