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

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.helper.Helper;
import org.primefaces.PrimeFaces;

@ViewScoped
@Named("SelectTemplateDialogView")
public class SelectTemplateDialogView implements Serializable {

    private int selectedTemplateId = 0;
    private ProjectDTO project;
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";
    private static final String MASSIMPORT_PATH = "/pages/massImport.jsf?faces-redirect=true";
    private String redirectPath;

    /**
     * Get project.
     *
     * @return value of project
     */
    public ProjectDTO getProject() {
        return project;
    }

    /**
     * Set project.
     *
     * @param project as org.kitodo.production.dto.ProjectDTO
     */
    public void setProject(ProjectDTO project) {
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
     * @param selectedTemplateId as org.kitodo.production.dto.TemplateDTO
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
        List<TemplateDTO> availableTemplates = this.project.getTemplates();
        if (availableTemplates.size() == 1) {
            this.selectedTemplateId = availableTemplates.get(0).getId();
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
