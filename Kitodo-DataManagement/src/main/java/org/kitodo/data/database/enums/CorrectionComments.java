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

package org.kitodo.data.database.enums;

/**
 * Enum for the status of a Process' CorrectionComments.
 */
public enum CorrectionComments {

    /**
     * Process has no correction comments at all.
     */
    NO_CORRECTION_COMMENTS(0),

    /**
     * Process has no open correction comments.
     */
    NO_OPEN_CORRECTION_COMMENTS(1),

    /**
     * Process has open correction comments.
     */
    OPEN_CORRECTION_COMMENTS(2);

    CorrectionComments(int value) {
        this.value = value;
    }

    private final int value;

    public int getValue() {
        return value;
    }
}
