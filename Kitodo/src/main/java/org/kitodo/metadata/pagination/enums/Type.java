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

public enum Type {
    ARABIC(Helper.getTranslation("arabic")),
    ROMAN(Helper.getTranslation("roman")),
    UNCOUNTED(Helper.getTranslation("uncounted")),
    FREETEXT(Helper.getTranslation("paginationFreetext"));

    private String label;

    Type(String label) {
        this.label = label;
    }

    /**
     * Gets label of paginator type.
     *
     * @return The label of paginator type.
     */
    public String getLabel() {
        return label;
    }
}
