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
     * Creates a new gallery stripe.
     *
     * @param panel
     *            gallery panel this gallery stripe belongs to
     * @param structure
     *            structure this gallery stripe is related to
     */
    GalleryStripe(GalleryPanel panel, LogicalDivision structure) {
        this.structure = structure;
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
}
