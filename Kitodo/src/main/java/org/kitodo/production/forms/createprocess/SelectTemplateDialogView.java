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

package org.kitodo.production.forms.createprocess;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@ViewScoped
@Named("SelectTemplateDialogView")
public class SelectTemplateDialogView implements Serializable {

    private int selectedTemplateId = 0;
    private Project project;
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";
    private static final String MASSIMPORT_PATH = "/pages/massImport.jsf?faces-redirect=true";
    private String redirectPath;

    /**
     * Get project.
     *
     * @return value of project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Set project.
     *
     * @param project as org.kitodo.production.dto.Project
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get selectedTemplateId.
     *
     * @return value of selectedTemplateId
     */
    public int getSelectedTemplateId() {
        return selectedTemplateId;
    }

    /**
     * Set selectedTemplateId.
     *
     * @param selectedTemplateId as org.kitodo.production.dto.Template
     */
    public void setSelectedTemplateId(int selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    /**
     * check for templates with create process path.
     */
    public void createProcessForProject() {
        redirectPath = CREATE_PROCESS_PATH;
        checkForTemplates();
    }

    /**
     * check for templates with massimport path.
     */
    public void openMassImportForProject() {
        redirectPath = MASSIMPORT_PATH;
        checkForTemplates();
    }

    /**
     * Navigate to redirectPath page if 'selectedTemplateId' is > 0.
     * Show template selection dialog if 'selectedTemplateId' is 0 and more than one template is configured for
     * current project.
     * Display error message if no template is configured for current project.
     */
    public void checkForTemplates() {
        try {
            this.project = ServiceManager.getProjectService().getById(this.project.getId());
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
        List<Template> availableTemplates = this.project.getActiveTemplates();
        if (availableTemplates.size() == 1) {
            this.selectedTemplateId = availableTemplates.getFirst().getId();
        }
        if (this.selectedTemplateId > 0) {
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                String path = context.getExternalContext().getRequestContextPath() + redirectPath
                        + "&templateId=" + this.selectedTemplateId + "&projectId=" + this.project.getId()
                        + "&referrer=" + context.getViewRoot().getViewId();
                context.getExternalContext().redirect(path);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage());
            }
        } else if (availableTemplates.size() > 1) {
            PrimeFaces.current().ajax().update("selectTemplateDialog");
            PrimeFaces.current().executeScript("PF('selectTemplateDialog').show();");
        } else {
            Helper.setErrorMessage("noTemplatesConfigured");
        }
    }
}
