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
@Named("SelectProjectDialogView")
public class SelectProjectDialogView implements Serializable {

    private static final Logger logger = LogManager.getLogger(SelectProjectDialogView.class);
    private int selectedProjectId;
    private TemplateDTO templateDTO;
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    private final static String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";

    /**
     * Get template.
     *
     * @return value of templateDTO
     */
    public TemplateDTO getTemplate() {
        return templateDTO;
    }

    /**
     * Get selectedProjectId.
     *
     * @return value of selectedProjectId
     */
    public int getSelectedProjectId() {
        return selectedProjectId;
    }

    /**
     * Set selectedProjectId.
     *
     * @param id as int
     */
    public void setSelectedProjectId(int id) {
        this.selectedProjectId = id;
    }

    /**
     * Get projects using current template.
     *
     * @return list of projects using the current template.
     */
    public List<Project> getTemplateProjects() {
        try {
            Template template = ServiceManager.getTemplateService().getById(this.templateDTO.getId());
            return template.getProjects();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TEMPLATE.getTranslationSingular(),
                    this.templateDTO.getId()}, logger, e);
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
    public void createProcessForTemplate(int projectId, int templateId) {
        try {
            this.templateDTO = ServiceManager.getTemplateService().findById(templateId);
            if (projectId > 0 || this.templateDTO.getProjects().size() == 1) {
                ProjectDTO projectDTO;
                if (projectId > 0) {
                    projectDTO = ServiceManager.getProjectService().findById(projectId);
                } else {
                    projectDTO = this.templateDTO.getProjects().get(0);
                }
                try {
                    FacesContext context = FacesContext.getCurrentInstance();
                    String path = context.getExternalContext().getRequestContextPath() + CREATE_PROCESS_PATH
                            + "&templateId=" + templateId + "&projectId=" + projectDTO.getId()
                            + "&referrer=" + context.getViewRoot().getViewId();
                    context.getExternalContext().redirect(path);
                } catch (IOException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage());
                }
            } else if (templateDTO.getProjects().size() > 1) {
                PrimeFaces.current().ajax().update("projectsTabView:selectProjectDialog");
                PrimeFaces.current().executeScript("PF('selectProjectDialog').show();");
            } else {
                Helper.setErrorMessage("noProjectsConfigured");
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e);
        }
    }
}
