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

import javax.faces.model.SelectItem;

/**
 * Backing bean for a JSF select item with an image.
 */
public class IllustratedSelectItem extends SelectItem {

    private String image;

    /**
     * Constructor.
     *
     * @param value
     *          value of IllustratedSelectItem
     * @param label
     *          label of IllustratedSelectItem
     * @param image
     *          image of IllustratedSelectItem
     */
    IllustratedSelectItem(Object value, String label, String image) {
        super(value, label);
        this.image = image;
    }

    /**
     * Return image.
     *
     * @return image
     */
    public String getImage() {
        return image;
    }
}
