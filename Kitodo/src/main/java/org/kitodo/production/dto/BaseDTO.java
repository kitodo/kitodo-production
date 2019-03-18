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

import java.io.Serializable;

public abstract class BaseDTO implements Serializable {

    private Integer id;

    /**
     * Get id.
     *
     * @return id as Integer
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
}
