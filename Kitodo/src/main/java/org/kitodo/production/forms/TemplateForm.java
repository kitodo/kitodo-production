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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.SelectItemList;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.workflow.model.Converter;

@Named("TemplateForm")
@SessionScoped
public class TemplateForm extends TemplateBaseForm {

    private static final long serialVersionUID = 2890900843176821176L;
    private static final Logger logger = LogManager.getLogger(TemplateForm.class);
    private Template template;
    private List<Project> assignedProjects = new ArrayList<>();
    private Task task;
    private boolean showInactiveTemplates = false;
    private final String templateListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private final String templateEditPath = MessageFormat.format(REDIRECT_PATH, "templateEdit");

    /**
     * Constructor.
     */
    public TemplateForm() {
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getTemplateService()));
    }

    /**
     * Check if inactive templates should be shown.
     *
     * @return true or false
     */
    public boolean isShowInactiveTemplates() {
        return this.showInactiveTemplates;
    }

    /**
     * Set if inactive templates should be shown.
     *
     * @param showInactiveTemplates
     *            true or false
     */
    public void setShowInactiveTemplates(boolean showInactiveTemplates) {
        this.showInactiveTemplates = showInactiveTemplates;
        ServiceManager.getTemplateService().setShowInactiveTemplates(showInactiveTemplates);
    }

    /**
     * Create new template.
     *
     * @return page
     */
    public String newTemplate() {
        this.template = new Template();
        this.template.setTitle("");
        this.assignedProjects.clear();
        return templateEditPath + "&id=" + (Objects.isNull(this.template.getId()) ? 0 : this.template.getId());
    }

    /**
     * Duplicate the selected template.
     *
     * @param itemId
     *            ID of the template to duplicate
     * @return page address; either redirect to the edit template page or return
     *         'null' if the template could not be retrieved, which will prompt
     *         JSF to remain on the same page and reuse the bean.
     */
    public String duplicate(Integer itemId) {
        try {
            Template baseTemplate = ServiceManager.getTemplateService().getById(itemId);
            this.template = ServiceManager.getTemplateService().duplicateTemplate(baseTemplate);
            this.assignedProjects.clear();
            this.assignedProjects.addAll(template.getProjects());
            return templateEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Save template and redirect to list view.
     *
     * @return url to list view
     */
    public String save() {
        if (Objects.nonNull(this.template.getTitle()) && !this.template.getTitle().isEmpty()) {
            try {
                if (this.template.getTasks().isEmpty()) {
                    Converter converter = new Converter(this.template.getWorkflow().getTitle());
                    converter.convertWorkflowToTemplate(this.template);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage("errorDiagramConvert", new Object[] {this.template.getWorkflow().getTitle() }, logger, e);
                return this.stayOnCurrentPage;
            } catch (IOException e) {
                Helper.setErrorMessage("errorDiagramFile", new Object[] {this.template.getWorkflow().getTitle() }, logger, e);
                return this.stayOnCurrentPage;
            } catch (WorkflowException e) {
                Helper.setErrorMessage("errorDiagramTask", new Object[] {this.template.getWorkflow().getTitle() }, logger, e);
                return this.stayOnCurrentPage;
            }

            this.template.getProjects().clear();
            this.template.getProjects().addAll(assignedProjects);

            try {
                ServiceManager.getTemplateService().save(this.template);
            } catch (DataException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() },
                    logger, e);
                return this.stayOnCurrentPage;
            }
        } else {
            Helper.setErrorMessage("titleEmpty");
            return this.stayOnCurrentPage;
        }
        return templateListPath;
    }

    /**
     * Remove template if there is no assigned processes.
     */
    public void delete() {
        if (!this.template.getProcesses().isEmpty()) {
            Helper.setErrorMessage("processAssignedError");
        } else {
            try {
                ServiceManager.getTemplateService().remove(this.template);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to templateEdit view
     */
    public String saveTaskAndRedirect() {
        saveTask(this.task, this.template, ObjectType.TEMPLATE.getTranslationSingular(),
            ServiceManager.getTemplateService());
        return templateEditPath + "&id=" + (Objects.isNull(this.template.getId()) ? 0 : this.template.getId());
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram() {
        if (Objects.nonNull(this.template.getWorkflow())) {
            return ServiceManager.getTemplateService().getTasksDiagram(this.template.getWorkflow().getTitle());
        }
        return ServiceManager.getTemplateService().getTasksDiagram("");
    }

    /**
     * Get list of dockets for select list.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getDockets() {
        return SelectItemList.getDockets(ServiceManager.getDocketService().getAllForSelectedClient());
    }

    /**
     * Get list of projects for select list.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProjects() {
        return SelectItemList.getProjects(ServiceManager.getProjectService().getAllForSelectedClient());
    }

    /**
     * Get list of rulesets for select list.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getRulesets() {
        return SelectItemList.getRulesets(ServiceManager.getRulesetService().getAllForSelectedClient());
    }

    /**
     * Get list of workflows for select list.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getWorkflows() {
        return SelectItemList.getWorkflows(ServiceManager.getWorkflowService().getAvailableWorkflows());
    }

    /**
     * Set template by id.
     *
     * @param id
     *            of template to set
     */
    public void setTemplateById(int id) {
        try {
            setTemplate(ServiceManager.getTemplateService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TEMPLATE.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Method being used as viewAction for template edit form. If the given
     * parameter 'id' is '0', the form for creating a new template will be
     * displayed.
     *
     * @param id
     *            of the template to load
     */
    public void loadTemplate(int id) {
        try {
            if (id != 0) {
                setTemplate(ServiceManager.getTemplateService().getById(id));
            } else {
                newTemplate();
            }
            setSaveDisabled(false);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TEMPLATE.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Method being used as viewAction for task form.
     *
     * @param id
     *            of the task to load
     */
    public void loadTask(int id) {
        try {
            if (id != 0) {
                setTask(ServiceManager.getTaskService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set template.
     *
     * @param template
     *            as Template
     */
    public void setTemplate(Template template) {
        this.template = template;
        this.assignedProjects.clear();
        this.assignedProjects.addAll(template.getProjects());
    }

    /**
     * Get assignedProjects.
     *
     * @return value of assigned projects
     */
    public List<Project> getAssignedProjects() {
        return assignedProjects;
    }

    /**
     * Set assignedProjects.
     *
     * @param assignedProjects as assignedProjects
     */
    public void setAssignedProjects(ArrayList<Project> assignedProjects) {
        this.assignedProjects = assignedProjects;
    }

    /**
     * Get task.
     *
     * @return value of task
     */
    public Task getTask() {
        return task;
    }

    /**
     * Set task.
     *
     * @param task
     *            as Task
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Get list of switch objects for all folders whose contents can be
     * generated.
     *
     * @return list of FolderProcessingSwitch objects or empty list
     */
    public List<FolderProcessingSwitch> getGeneratableFolderSwitches() {
        Stream<Project> projectsStream = template.getProjects().stream();
        Stream<Folder> generatableFolders = TaskService.generatableFoldersFromProjects(projectsStream);
        return getSwitches(generatableFolders, task.getContentFolders());
    }

    /**
     * Convert the stream of folders to a list of switch objects.
     * 
     * @param folders
     *            folders for which generation or validation can be switched on
     *            or off
     * @param activated
     *            Folders for which generation or validation is switched on.
     *            This list must be modifiable and connected to the database so
     *            that changes made by the switches are persisted when the
     *            template is stored in the database.
     * @return a list of switch objects to be rendered by JSF
     */
    private List<FolderProcessingSwitch> getSwitches(Stream<Folder> folders, List<Folder> activated) {
        Stream<FolderProcessingSwitch> validatorSwitches = folders
                .map(folder -> new FolderProcessingSwitch(folder, activated));
        return validatorSwitches.collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get list of switch objects for all folders whose contents can be
     * generated.
     *
     * @return list of FolderProcessingSwitch objects or empty list
     */
    public List<FolderProcessingSwitch> getValidatableFolderSwitches() {
        Stream<Folder> validatableFolders = template.getProjects().stream()
                .flatMap(project -> project.getFolders().stream());
        return getSwitches(validatableFolders, task.getValidationFolders());
    }
}
