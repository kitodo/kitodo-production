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

package org.kitodo.production.workflow.model;

import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

public class Updater {

    private Template template;

    /**
     * Public constructor.
     *
     * @param template
     *      for which processes need to be updated
     */
    public Updater(Template template) {
        this.template = template;
    }

    /**
     * Update all processes assigned to given template.
     */
    public void updateProcessesAssignedToTemplate() throws DataException {
        List<Process> processes = this.template.getProcesses();

        for (Process process : processes) {
            ServiceManager.getProcessService().save(process);
        }
    }

    /**
     * Get template.
     *
     * @return Template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set template.
     *
     * @param template
     *        which was updated
     */
    public void setTemplate(Template template) {
        this.template = template;
    }
}
