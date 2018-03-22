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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

@Named("TemplateForm")
@SessionScoped
public class TemplateForm extends TemplateBaseForm {

    private static final long serialVersionUID = 2890900843176821176L;
    private static final Logger logger = LogManager.getLogger(TemplateForm.class);
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private Template template = new Template();
    private Task task = new Task();
    private String title;
    private List<TemplateDTO> templateDTOS;
    private transient ServiceManager serviceManager = new ServiceManager();

    private String templateListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String templateEditPath = MessageFormat.format(REDIRECT_PATH, "templateEdit");
    private String taskEditPath = MessageFormat.format(REDIRECT_PATH, "taskEdit");

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

    /**
     * This method initializes the template list without any filter whenever the bean
     * is constructed.
     */
    @PostConstruct
    public void initializeTemplateList() {
        setFilter("");
    }


    /**
     * Create new template.
     *
     * @return page
     */
    public String newTemplate() {
        this.template = new Template();
        this.template.setTitle("");
        return templateEditPath + "&id=" + (Objects.isNull(this.template.getId()) ? 0 : this.template.getId());
    }

    /**
     * Filter templates.
     *
     * @return page
     */
    public String filterTemplates() {
        try {
            if (this.filter.equals("")) {
                filterTemplatesWithoutFilter();
            } else {
                filterTemplatesWithFilter();
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return templateListPath;
    }

    /**
     * Add UserGroup.
     *
     * @return empty String
     */
    public String addUserGroup() {
        addUserGroup(this.task);
        return null;
    }

    /**
     * Add User.
     *
     * @return empty String
     */
    public String addUser() {
        addUser(this.task);
        return null;
    }

    /**
     * Remove User.
     *
     * @return empty String
     */
    public String deleteUser() {
        deleteUser(this.task);
        return null;
    }

    /**
     * Remove UserGroup.
     *
     * @return empty String
     */
    public String deleteUserGroup() {
        deleteUserGroup(this.task);
        return null;
    }

    /**
     * Set ordering up.
     *
     * @return String
     */
    public String setOrderingUp() {
        setOrderingUp(this.template.getTasks(), this.task);
        return save();
    }

    /**
     * Set ordering down.
     *
     * @return String
     */
    public String setOrderingDown() {
        setOrderingDown(this.template.getTasks(), this.task);
        return save();
    }

    /**
     * Save template.
     *
     * @return null
     */
    public String save() {
        if (this.template != null && this.template.getTitle() != null) {
            if (!this.template.getTitle().equals(this.title) && this.title != null
                    && !renameAfterProcessTitleChanged()) {
                return null;
            }

            try {
                serviceManager.getTemplateService().save(this.template);
            } catch (DataException e) {
                Helper.setErrorMessage("fehlerNichtSpeicherbar", logger, e);
            }
        } else {
            Helper.setFehlerMeldung("titleEmpty");
        }
        return reload();
    }

    /**
     * Save template and redirect to list view.
     *
     * @return url to list view
     */
    public String saveAndRedirect() {
        save();
        return templateListPath;
    }

    /**
     * New task.
     */
    public String newTask() {
        this.task = new Task();
        this.task.setTemplate(this.template);
        this.template.getTasks().add(this.task);
        return taskEditPath;
    }

    /**
     * Save task.
     */
    public void saveTask() {
        saveTask(this.task);
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        saveTask();
        return taskEditPath;
    }

    private void filterTemplatesWithFilter() throws DataException {
        BoolQueryBuilder query = serviceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS,
                false, false);
        if (!this.showClosedProcesses) {
            query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
        }
        if (!this.showInactiveProjects) {
            query.must(serviceManager.getProcessService().getQueryProjectActive(true));
        }
        templateDTOS = serviceManager.getTemplateService().findByQuery(query, sortList(), false);
    }

    private void filterTemplatesWithoutFilter() throws DataException {
        if (!this.showInactiveProjects) {
            templateDTOS = serviceManager.getTemplateService().findTemplatesOfActiveProjects(sortList());
        } else {
            templateDTOS = serviceManager.getTemplateService().findAll(sortList());
        }
    }

    private boolean renameAfterProcessTitleChanged() {
        String validateRegEx = ConfigCore.getParameter("validateProzessTitelRegex", "[\\w-]*");
        if (!this.title.matches(validateRegEx)) {
            Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
            return false;
        } else {
            this.template.setTitle(this.title);
        }
        return true;
    }

    /**
     * Reload.
     *
     * @return String
     */
    public String reload() {
        reload(this.task, "arbeitsschritt");
        reload(this.template, "template");
        return null;
    }

    /**
     * Get selected project.
     *
     * @return Integer
     */
    public Integer getProjectSelect() {
        return getProjectSelect(this.template.getProject());
    }

    /**
     * Set selected project.
     *
     * @param projectSelect
     *            Integer
     */
    public void setProjectSelect(Integer projectSelect) {
        if (projectSelect != 0) {
            try {
                this.template.setProject(serviceManager.getProjectService().getById(projectSelect));
            } catch (DAOException e) {
                Helper.setErrorMessage("Error assigning project", logger, e);
            }
        }
    }

    /**
     * Method being used as viewAction for template edit form. If the given parameter
     * 'id' is '0', the form for creating a new template will be displayed.
     *
     * @param id
     *            of the template to load
     */
    public void loadTemplate(int id) {
        try {
            if (id != 0) {
                setTemplate(this.serviceManager.getTemplateService().getById(id));
            } else {
                newTemplate();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("template"), id }, logger, e);
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
                setTask(this.serviceManager.getTaskService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("arbeitsschritt"), id },
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
     * @param template as Template
     */
    public void setTemplate(Template template) {
        this.template = template;
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
     * @param task as Task
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title as String
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
