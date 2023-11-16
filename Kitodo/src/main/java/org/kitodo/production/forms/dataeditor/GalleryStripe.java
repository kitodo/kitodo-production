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
import java.util.List;

import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;

/**
 * One media stripe in the structured gallery view.
 */
public class GalleryStripe {
    /**
     * Label of the stripe.
     */
    private final String label;

    /**
     * Medias in this stripe.
     */
    private final List<GalleryMediaContent> medias = new ArrayList<>();

    /**
     * Structure this gallery stripe is related to.
     */
    private final LogicalDivision structure;

    /**
     * Stores the primefaces tree node id of the corresponding tree node of the logical structure 
     * tree. This id can be used in the user interface to identify which gallery stripe corresponds 
     * to which tree node in the logical structure tree.
     * 
     * <p>It consists of a sequence of numbers separated by underscore, e.g. "0_1_4". Each number
     * describes the position of a child amongst its siblings at that level. For example, "0_1_4" 
     * references the node that is reached when moving from root node to leaf node using the first 
     * child, then the second child, and then the fifth child.</p>
     * 
     * <p>The root node itself is never referenced, as it is not visualized anyway.</p>
     */
    private final String logicalTreeNodeId;

    /**
     * Creates a new gallery stripe.
     *
     * @param panel
     *            gallery panel this gallery stripe belongs to
     * @param structure
     *            structure this gallery stripe is related to
     */
    GalleryStripe(GalleryPanel panel, LogicalDivision structure, String logicalTreeNodeId) {
        this.structure = structure;
        this.logicalTreeNodeId = logicalTreeNodeId;
        this.label = getLabel(panel, structure);
    }

    /**
     * Returns the label for the stripe, which is the label of the structure.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the translated label for a structure.
     *
     * @param panel
     *            panel providing the ruleset and its configuration settings
     * @param structure
     *            structure whose label is to return
     * @return the label
     */
    private String getLabel(GalleryPanel panel, LogicalDivision structure) {
        RulesetManagementInterface ruleset = panel.getRuleset();
        StructuralElementViewInterface divisionView = ruleset.getStructuralElementView(structure.getType(),
            panel.getAcquisitionStage(), panel.getPriorityList());
        return divisionView.getLabel();
    }

    /**
     * Returns the medias of the stripe.
     *
     * @return the medias
     */
    public List<GalleryMediaContent> getMedias() {
        return medias;
    }

    /**
     * Returns the structure of the stripe.
     *
     * @return structure
     */
    public LogicalDivision getStructure() {
        return structure;
    }

    /**
     * Returns the logical tree node id corresponding to this stripe.
     * @return the logical tree node id
     */
    public String getLogicalTreeNodeId() {
        return logicalTreeNodeId;
    }
}
