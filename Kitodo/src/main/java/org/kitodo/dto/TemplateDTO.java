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

package org.kitodo.dto;

public class TemplateDTO extends BaseTemplateDTO {

    private boolean containsUnreachableSteps;

    /**
     * Get information if process contains unreachable tasks.
     *
     * @return true or false
     */
    public boolean isContainsUnreachableSteps() {
        return containsUnreachableSteps;
    }

    /**
     * Set information if process contains unreachable tasks.
     *
     * @param containsUnreachableSteps
     *            as boolean
     */
    public void setContainsUnreachableSteps(boolean containsUnreachableSteps) {
        this.containsUnreachableSteps = containsUnreachableSteps;
    }
}
