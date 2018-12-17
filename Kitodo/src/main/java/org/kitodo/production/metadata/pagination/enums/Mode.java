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

package org.kitodo.production.metadata.pagination.enums;

import org.kitodo.production.helper.Helper;

/**
 * Sets new labels to a given set of pages.
 */

public enum Mode {
    PAGES(Helper.getTranslation("pageCount"), "paginierung_seite.svg"),
    COLUMNS(Helper.getTranslation("columnCount"), "paginierung_spalte.svg"),
    FOLIATION(Helper.getTranslation("blattzaehlung"), "paginierung_blatt.svg"),
    RECTOVERSO_FOLIATION(Helper.getTranslation("blattzaehlungrectoverso"), "paginierung_blatt_rectoverso.svg"),
    RECTOVERSO(Helper.getTranslation("pageCountRectoVerso"), "paginierung_seite_rectoverso.svg"),
    DOUBLE_PAGES(Helper.getTranslation("pageCountDouble"), "paginierung_doppelseite.svg");

    private String label;
    private String image;

    Mode(String label, String image) {
        this.label = label;
        this.image = image;
    }

    /**
     * Gets label of paginator mode.
     *
     * @return The label of paginator mode.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets image of paginator mode for displaying at frontend.
     *
     * @return The label of paginator mode.
     */
    public String getImage() {
        return image;
    }
}
