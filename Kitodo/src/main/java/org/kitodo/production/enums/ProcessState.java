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

package org.kitodo.production.enums;

public enum ProcessState {

    /**
     * Use this enum for completed processes.
     */
    COMPLETED("100000000000"),

    /**
     * Do not use this enum except you need to stay compatible with Kitodo.Production 2.x values.
     */
    COMPLETED20("100000000");

    private final String value;

    private ProcessState(String value) {
        this.value = value;
    }

    /**
     * Get the value of used enum entry.
     *
     * @return value of used enum entry
     */
    public String getValue() {
        return value;
    }
}
