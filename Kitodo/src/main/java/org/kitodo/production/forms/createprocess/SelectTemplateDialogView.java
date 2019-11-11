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
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@ViewScoped
@Named("SelectTemplateDialogView")
public class SelectTemplateDialogView implements Serializable {

    private static final Logger logger = LogManager.getLogger(SelectTemplateDialogView.class);
    private int selectedTemplateId;
    private ProjectDTO project;
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";

    /**
     * Get project.
     *
     * @return value of project
     */
    public ProjectDTO getProject() {
        return project;
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
     * Get templates.
     *
     * @return value of templates
     */
    public List<Template> getProjectTemplates() {
        try {
            Project project = ServiceManager.getProjectService().getById(this.project.getId());
            return project.getTemplates();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROJECT.getTranslationSingular(),
                    this.project.getId()}, logger, e);
        }
        return Collections.emptyList();
    }

    /**
     * Navigate to 'createProcess' page if given ID 'templateId' is > 0.
     * Show template selection dialog if 'templateId' is 0 and more than one template is configured for given project.
     * Display error message if no template is configured for given project.
     *
     * @param projectId id of project for which a new process is created.
     * @param templateId id of template from which new process is created for Project with given ID.
     */
    public void createProcessForProject(int projectId, int templateId) {
        try {
            this.project = ServiceManager.getProjectService().findById(projectId);
            if (templateId > 0 || this.project.getTemplates().size() == 1) {
                TemplateDTO template;
                if (templateId > 0) {
                    template = ServiceManager.getTemplateService().findById(templateId);
                } else {
                    template = this.project.getTemplates().get(0);
                }
                try {
                    FacesContext context = FacesContext.getCurrentInstance();
                    String path = context.getExternalContext().getRequestContextPath() + CREATE_PROCESS_PATH
                            + "&templateId=" + template.getId() + "&projectId=" + projectId
                            + "&referrer=" + context.getViewRoot().getViewId();
                    context.getExternalContext().redirect(path);
                } catch (IOException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage());
                }
            } else if (project.getTemplates().size() > 1) {
                PrimeFaces.current().ajax().update("projectsTabView:selectTemplateDialog");
                PrimeFaces.current().executeScript("PF('selectTemplateDialog').show();");
            } else {
                Helper.setErrorMessage("noTemplatesConfigured");
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e);
        }
    }
}
