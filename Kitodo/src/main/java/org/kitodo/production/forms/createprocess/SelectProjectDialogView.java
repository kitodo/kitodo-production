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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@ViewScoped
@Named("SelectProjectDialogView")
public class SelectProjectDialogView implements Serializable {

    private static final Logger logger = LogManager.getLogger(SelectProjectDialogView.class);
    private int selectedProjectId = 0;
    private TemplateDTO templateDTO;
    protected static final String ERROR_LOADING_ONE = "errorLoadingOne";
    private static final String CREATE_PROCESS_PATH = "/pages/processFromTemplate.jsf?faces-redirect=true";

    /**
     * Get template.
     *
     * @return value of templateDTO
     */
    public TemplateDTO getTemplate() {
        return templateDTO;
    }

    /**
     * Set templateDTO.
     *
     * @param templateDTO as org.kitodo.production.dto.TemplateDTO
     */
    public void setTemplateDTO(TemplateDTO templateDTO) {
        this.templateDTO = templateDTO;
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
     * @param projectId as int
     */
    public void setSelectedProjectId(int projectId) {
        this.selectedProjectId = projectId;
    }

    /**
     * Get projects using current template.
     *
     * @return list of projects using the current template.
     */
    public List<Project> getTemplateProjects() {
        try {
            Template template = ServiceManager.getTemplateService().getById(this.templateDTO.getId());
            return template.getProjects().stream().sorted(Comparator.comparing(Project::getTitle))
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TEMPLATE.getTranslationSingular(),
                    this.templateDTO.getId()}, logger, e);
        }
        return Collections.emptyList();
    }

    /**
     * Navigate to 'createProcess' page if 'selectedProjectId' is > 0.
     * Show project selection dialog if 'selectedProjectId' is 0 and more the current template is used in more than one
     * project.
     * Display error message if the current template is not used in any project.
     */
    public void createProcessFromTemplate() {
        if (this.templateDTO.getProjects().size() == 1) {
            this.selectedProjectId = this.templateDTO.getProjects().get(0).getId();
        }
        if (this.selectedProjectId > 0) {
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                String path = context.getExternalContext().getRequestContextPath() + CREATE_PROCESS_PATH
                        + "&templateId=" + this.templateDTO.getId() + "&projectId=" + this.selectedProjectId
                        + "&referrer=" + context.getViewRoot().getViewId();
                context.getExternalContext().redirect(path);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage());
            }
        } else if (templateDTO.getProjects().size() > 1) {
            PrimeFaces.current().ajax().update("selectProjectDialog");
            PrimeFaces.current().executeScript("PF('selectProjectDialog').show();");
        } else {
            Helper.setErrorMessage("noProjectsConfigured");
        }
    }
}
