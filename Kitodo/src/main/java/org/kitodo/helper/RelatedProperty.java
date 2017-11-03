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

package org.kitodo.helper;

import java.util.List;

public class RelatedProperty {

    private Integer id;
    private List<String> values;

    /**
     * Get id.
     * 
     * @return id ss Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set id.
     * 
     * @param id
     *            as Integer
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get values.
     * 
     * @return value as List of Strings
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Set values.
     * 
     * @param values
     *            as List of Strings
     */
    public void setValues(List<String> values) {
        this.values = values;
    }
}
