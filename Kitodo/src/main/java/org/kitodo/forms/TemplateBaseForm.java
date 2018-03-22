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
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class TemplateBaseForm extends BasisForm {

    private static final long serialVersionUID = 6566567843176821176L;
    private static final Logger logger = LogManager.getLogger(TemplateForm.class);
    private boolean showClosedProcesses = false;
    private boolean showInactiveProjects = false;
    private transient ServiceManager serviceManager = new ServiceManager();

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
     * Add user group to task.
     *
     * @param task
     *            to add user group
     */
    public void addUserGroup(Task task) {
        Integer userGroupId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            UserGroup userGroup = serviceManager.getUserGroupService().getById(userGroupId);
            for (UserGroup taskUserGroup : task.getUserGroups()) {
                if (taskUserGroup.equals(userGroup)) {
                    return;
                }
            }
            task.getUserGroups().add(userGroup);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
        }
    }

    /**
     * Add user to task.
     *
     * @param task
     *            to add user
     */
    public void addUser(Task task) {
        Integer userId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            User user = serviceManager.getUserService().getById(userId);
            for (User taskUser : task.getUsers()) {
                if (taskUser.equals(user)) {
                    return;
                }
            }
            task.getUsers().add(user);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
        }
    }

    /**
     * Remove user from task.
     *
     * @param task
     *            for delete user
     */
    public void deleteUser(Task task) {
        Integer userId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            User user = serviceManager.getUserService().getById(userId);
            task.getUsers().remove(user);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
        }
    }

    /**
     * Remove user group from task.
     *
     * @param task
     *            for delete user group
     */
    public void deleteUserGroup(Task task) {
        Integer userGroupId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            UserGroup userGroup = serviceManager.getUserGroupService().getById(userGroupId);
            task.getUserGroups().remove(userGroup);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
        }
    }

    /**
     * Set ordering for task up.
     *
     * @param tasks
     *            list of all task assigned to process/template
     * @param task
     *            task for change ordering
     */
    public void setOrderingUp(List<Task> tasks, Task task) {
        Integer ordering = task.getOrdering() - 1;
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering().equals(ordering)) {
                tempTask.setOrdering(ordering + 1);
            }
        }
        task.setOrdering(ordering);
    }

    /**
     * Set ordering for task down.
     *
     * @param tasks
     *            list of all task assigned to process/template
     * @param task
     *            task for change ordering
     */
    public void setOrderingDown(List<Task> tasks, Task task) {
        Integer ordering = task.getOrdering() + 1;
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering().equals(ordering)) {
                tempTask.setOrdering(ordering - 1);
            }
        }
        task.setOrdering(ordering);
    }

    /**
     * Get list of projects.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getProjectsSelectList() {
        List<SelectItem> projects = new ArrayList<>();
        List<Project> temp = serviceManager.getProjectService().getByQuery("from Project ORDER BY title");
        for (Project project : temp) {
            projects.add(new SelectItem(project.getId(), project.getTitle(), null));
        }
        return projects;
    }

    /**
     * Get selected project.
     *
     * @return Integer
     */
    protected Integer getProjectSelect(Project project) {
        if (Objects.nonNull(project)) {
            return project.getId();
        } else {
            return 0;
        }
    }

    /**
     * Save task.
     * 
     * @param task
     *            to save
     */
    protected void saveTask(Task task) {
        task.setEditTypeEnum(TaskEditType.ADMIN);
        task.setProcessingTime(new Date());
        User user = getUser();
        serviceManager.getTaskService().replaceProcessingUser(task, user);

        try {
            serviceManager.getTaskService().save(task);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("arbeitsschritt") }, logger, e);
        }
    }

    protected void reload(BaseBean baseBean, String message) {
        if (Objects.nonNull(baseBean) && Objects.nonNull(baseBean.getId())) {
            try {
                Helper.getHibernateSession().refresh(baseBean);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation(message) },
                            logger, e);
                }
            }
        }
    }

    protected String sortList() {
        String sort = SortBuilders.fieldSort("title").order(SortOrder.ASC).toString();
        if (this.sortierung.equals("titelAsc")) {
            sort += "," + SortBuilders.fieldSort("title").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("titelDesc")) {
            sort += "," + SortBuilders.fieldSort("title").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("batchAsc")) {
            sort += ", " + SortBuilders.fieldSort("batches.id").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("batchDesc")) {
            sort += ", " + SortBuilders.fieldSort("batches.id").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("projektAsc")) {
            sort += ", " + SortBuilders.fieldSort("project").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("projektDesc")) {
            sort += ", " + SortBuilders.fieldSort("project").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("vorgangsdatumAsc")) {
            sort += "," + SortBuilders.fieldSort("creationDate").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("vorgangsdatumDesc")) {
            sort += "," + SortBuilders.fieldSort("creationDate").order(SortOrder.DESC).toString();
        }
        if (this.sortierung.equals("fortschrittAsc")) {
            sort += "," + SortBuilders.fieldSort("sortHelperStatus").order(SortOrder.ASC).toString();
        }
        if (this.sortierung.equals("fortschrittDesc")) {
            sort += "," + SortBuilders.fieldSort("sortHelperStatus").order(SortOrder.DESC).toString();
        }
        return sort;
    }
}
