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

public class TemplateProperty extends Property {

    /** The templateId */
    private Integer templateId;

    /** Gets the templateId.
     * @return The templateId.
     */
    public Integer getTemplateId() {
        return templateId;
    }

    /** Sets the templateId.
     * @param templateId The templateId.
     */
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }
}
