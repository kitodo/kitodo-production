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

package org.kitodo.forms;

import de.sub.goobi.forms.BasisForm;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

@Named("TemplateForm")
@SessionScoped
public class TemplateForm extends BasisForm {

    private static final long serialVersionUID = 2890900843176821176L;
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor.
     */
    public TemplateForm() {
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getTemplateService()));
    }

    /**
     * Check if closed processes should be shown.
     *
     * @return true or false
     */
    public boolean isShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    /**
     * Set if closed processes should be shown.
     *
     * @param showClosedProcesses
     *            true or false
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    /**
     * Check if inactive projects should be shown.
     *
     * @return true or false
     */
    public boolean isShowInactiveProjects() {
        return this.showInactiveProjects;
    }

    /**
     * Set if inactive projects should be shown.
     *
     * @param showInactiveProjects
     *            true or false
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }
}
