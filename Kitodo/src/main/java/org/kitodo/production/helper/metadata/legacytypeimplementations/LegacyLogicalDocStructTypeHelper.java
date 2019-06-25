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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;

/**
 * Connects a legacy doc struct type from the logical map to a division view.
 * This is a soldering class to keep legacy code operational which is about to
 * be removed. Do not use this class.
 */
public class LegacyLogicalDocStructTypeHelper {

    /**
     * The division view accessed via this soldering class.
     */
    private StructuralElementViewInterface divisionView;

    @Deprecated
    public LegacyLogicalDocStructTypeHelper(StructuralElementViewInterface divisionView) {
        this.divisionView = divisionView;
    }

    @Deprecated
    public String getName() {
        return divisionView.getId();
    }

    @Deprecated
    public String getNameByLanguage(String language) {
        return divisionView.getLabel();
    }
}
