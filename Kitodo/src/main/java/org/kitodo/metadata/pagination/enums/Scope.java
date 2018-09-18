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

package org.kitodo.metadata.pagination.enums;

import org.kitodo.helper.Helper;

/**
 * Sets new labels to a given set of pages.
 */

public enum Scope {
    FROMFIRST(Helper.getTranslation("abDerErstenMarkiertenSeite")),
    SELECTED(Helper.getTranslation("nurDieMarkiertenSeiten"));

    private String label;

    Scope(String label) {
        this.label = label;
    }

    /**
     * Gets label of paginator scope.
     *
     * @return The label of paginator scope.
     */
    public String getLabel() {
        return label;
    }
}
