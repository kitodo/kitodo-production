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

package org.kitodo.enums;

public enum ObjectMode {
    PROCESS("process"),
    PROCESSPROPERTY("processProperty"),
    PROJECT("project"),
    TASK("task"),
    TEMPLATE("template"),
    TEMPLATEPROPERTY("templateProperty"),
    WORKPIECE("workpiece"),
    WORKPIECEPROPERTY("workpieceProperty"),
    NONE("");

    private String mode;

    ObjectMode(String mode) {
        this.mode = mode;
    }

    /**
     * Get mode for object.
     * 
     * @return mode as String
     */
    public String getMode() {
        return mode;
    }
}
