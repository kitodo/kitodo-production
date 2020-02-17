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

package org.kitodo.production.forms;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("MassImportForm")
@SessionScoped
public class MassImportForm extends BaseForm {

    private int projectId;
    private int templateId;

    public void prepareMassImport(int templateId, int projectId) {
        this.projectId = projectId;
        this.templateId = templateId;
    }

    /**
     * Get projectId.
     *
     * @return value of projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Set projectId.
     *
     * @param projectId
     *            as int
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Get templateId.
     *
     * @return value of templateId
     */
    public int getTemplateId() {
        return templateId;
    }

    /**
     * Set templateId.
     *
     * @param templateId
     *            as int
     */
    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }
}
