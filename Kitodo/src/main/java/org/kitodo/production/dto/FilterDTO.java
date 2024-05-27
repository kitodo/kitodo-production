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

package org.kitodo.production.dto;

import org.kitodo.data.interfaces.FilterInterface;

/**
 * Filter DTO object.
 */
public class FilterDTO extends BaseDTO implements FilterInterface {

    private String value;

    /**
     * Get value.
     * @return value as String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value as String
     */
    public void setValue(String value) {
        this.value = value;
    }
}
