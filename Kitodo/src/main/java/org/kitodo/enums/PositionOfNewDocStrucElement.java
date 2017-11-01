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

package org.kitodo.enums;

import de.sub.goobi.helper.Helper;

public enum PositionOfNewDocStrucElement {
    BEFOR_CURRENT_ELEMENT(Helper.getTranslation("vorDasAktuelleElement")),
    AFTER_CURRENT_ELEMENT(Helper.getTranslation("hinterDasAktuelleElement")),
    FIRST_CHILD_OF_CURRENT_ELEMENT(Helper.getTranslation("alsErstesKindDesAktuellenElements")),
    LAST_CHILD_OF_CURRENT_ELEMENT(Helper.getTranslation("alsLetztesKindDesAktuellenElements"));

    private String label;

    PositionOfNewDocStrucElement(String label) {
        this.label = label;
    }

    /**
     * Gets label of paginator mode.
     *
     * @return The label of paginator mode.
     */
    public String getLabel() {
        return label;
    }

}
