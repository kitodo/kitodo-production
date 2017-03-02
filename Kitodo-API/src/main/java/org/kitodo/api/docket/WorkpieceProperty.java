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

package org.kitodo.api.docket;

public class WorkpieceProperty extends Property {

    /** The workpieceId. */
    private Integer workpieceId;

    /** Gets the workpieceId. */
    public Integer getWorkpieceId() {
        return workpieceId;
    }

    /** Sets the workpieceId. */
    public void setWorkpieceId(Integer workpieceId) {
        this.workpieceId = workpieceId;
    }
}
