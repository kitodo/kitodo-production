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

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;

public class StructureTreeNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object dataObject;
    private final String label;
    private final boolean linked;
    private final StructurePanel structurePanel;
    private final boolean undefined;

    public StructureTreeNode(StructurePanel structurePanel, String label, boolean undefined, boolean linked,
            Object dataObject) {
        this.structurePanel = structurePanel;
        this.label = label;
        this.undefined = undefined;
        this.linked = linked;
        this.dataObject = dataObject;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLinked() {
        return linked;
    }

    public boolean isUndefined() {
        return undefined;
    }

    /**
     * TODO add javaDoc.
     * 
     * @param event
     *            TreeDragDropEvent triggered by logical node being dropped
     */
    public void treeElementDragDrop(TreeDragDropEvent event) {

    }

    /**
     * TODO add javaDoc.
     * 
     * @param event
     *            NodeSelectEvent triggered by logical node being selected
     */
    public void treeElementSelect(NodeSelectEvent event) {
        structurePanel.treeElementSelect();
    }

}
