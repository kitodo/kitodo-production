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

package org.kitodo.production.forms.task;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.controller.SecurityAccessController;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseEditView;
import org.kitodo.production.forms.process.ProcessEditView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.utils.Stopwatch;

@Named("TaskEditView")
@ViewScoped
public class TaskEditView extends BaseEditView {

    private static final Logger logger = LogManager.getLogger(TaskEditView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "taskEdit");
    
    private Task task;

    private String referrer = DEFAULT_LINK;

    /**
     * Return view path to edit a task.
     * 
     * @param task the task to be edited
     * @return the view path
     */
    public static String getViewPath(Task task) {
        return VIEW_PATH + "&id=" + task.getId();
    }

    /**
     * Get task object.
     *
     * @return Task object
     */
    public Task getTask() {
        Stopwatch stopwatch = new Stopwatch(this, "getTask");
        return stopwatch.stop(this.task);
    }

    /**
     * Get task statuses for select list.
     *
     * @return array of task statuses
     */
    public TaskStatus[] getTaskStatuses() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatuses");
        return stopwatch.stop(TaskStatus.values());
    }

    /**
     * Method being used as viewAction for task edit form.
     */
    public void load(int id) {
        Stopwatch stopwatch = new Stopwatch(this, "loadTask");
        SecurityAccessController securityAccessController = new SecurityAccessController();
        try {
            if (!securityAccessController.hasAuthorityToEditTask(id)) {
                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.redirect(DEFAULT_LINK);
            }
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                    logger, e);
        }
        try {
            if (id != 0) {
                this.task = ServiceManager.getTaskService().getById(id);
                this.task.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Save task and redirect to processEdit view.
     *
     * @return url to processEdit view
     */
    public String saveTaskAndRedirect() {
        Stopwatch stopwatch = new Stopwatch(this, "saveTaskAndRedirect");
        save();
        try {
            Process process = ServiceManager.getProcessService().getById(this.task.getProcess().getId());
            ServiceManager.getProcessService().save(process);
            return stopwatch.stop(getReferrerViewPath());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.PROCESS.getTranslationSingular()},
                    logger, e);
        }
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Remove role from the task.
     *
     * @return stay on the same page
     */
    public String deleteRole() {
        Stopwatch stopwatch = new Stopwatch(this, "deleteRole");
        String idParameter = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(idParameter)) {
            try {
                int roleId = Integer.parseInt(idParameter);
                this.task.getRoles().removeIf(role -> role.getId().equals(roleId));
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Add role to the task.
     *
     * @return stay on the same page
     */
    public String addRole() {
        Stopwatch stopwatch = new Stopwatch(this, "addRole");
        String idParameter = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(idParameter)) {
            int roleId = 0;
            try {
                roleId = Integer.parseInt(idParameter);
                Role role = ServiceManager.getRoleService().getById(roleId);

                if (!this.task.getRoles().contains(role)) {
                    this.task.getRoles().add(role);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.ROLE.getTranslationSingular(), roleId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Set referring view which will be returned when the user clicks "save" or
     * "cancel" on the task edit page.
     *
     * @param referrer
     *            the referring view
     */
    public void setReferrerFromTemplate(String referrer) {
        Stopwatch stopwatch = new Stopwatch(this, "setReferrerFromTemplate", "referer", referrer);
        if (referrer.equals("tasks") || referrer.equals("processEdit")) {
            this.referrer = referrer;
        } else {
            this.referrer = DEFAULT_LINK;
        }
        stopwatch.stop();
    }

    /**
     * Return the view path to the referring view, which can be either the desktop view, task list or process edit page.
     * 
     * @return the view path
     */
    public String getReferrerViewPath() {
        if (this.referrer.equals("tasks")) {
            return TaskListView.getViewPath() + "&" + getReferrerListOptions();
        } else if (this.referrer.equals("processEdit")) {
            return ProcessEditView.VIEW_PATH + "&id=" + this.task.getProcess().getId();
        }
        return "desktop";
    }

    /** 
     * Save current task.
    */
    private void save() {
        try {
            ServiceManager.getTaskService().save(this.task);
            ServiceManager.getTaskService().evict(this.task);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
    }
}
