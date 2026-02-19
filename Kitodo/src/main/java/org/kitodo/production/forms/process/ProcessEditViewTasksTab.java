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

package org.kitodo.production.forms.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.utils.Stopwatch;
import org.xml.sax.SAXException;


@Named("ProcessEditViewTasksTab")
@ViewScoped
public class ProcessEditViewTasksTab extends BaseTabEditView<Process> {

    private static final Logger logger = LogManager.getLogger(ProcessEditViewTasksTab.class);

    private Process process;

    private final transient ProcessService processService = ServiceManager.getProcessService();
    private final transient WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    /**
     * Return the process currently being edited.
     * 
     * @return the process currently being edited
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Get translation of task status title.
     *
     * @param taskStatusTitle
     *            'statusDone', 'statusLocked' and so on
     * @return translated message for given task status title
     */
    public String getTaskStatusTitle(String taskStatusTitle) {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatusTitle", "taskStatusTitle", taskStatusTitle);
        return stopwatch.stop(Helper.getTranslation(taskStatusTitle));
    }

    /**
     * Get diagram image for current template.
     *
     * @return diagram image file
     */
    public InputStream getTasksDiagram() {
        Stopwatch stopwatch = new Stopwatch(this, "getTasksDiagram");
        Workflow workflow = this.process.getTemplate().getWorkflow();
        if (Objects.nonNull(workflow)) {
            return ServiceManager.getTemplateService().getTasksDiagram(workflow.getTitle());
        }
        return stopwatch.stop(ServiceManager.getTemplateService().getTasksDiagram(""));
    }

    /**
     * Load process that is currently being edited.
     * 
     * @param process the process currently being edited
     */
    public void load(Process process) {
        this.process = process;
    }

    /**
     * Task status up.
     *
     * @throws DAOException
     *          when setting up task status via WorkflowControllerService fails
     * @throws IOException
     *          when setting up task status via WorkflowControllerService fails
     * @throws SAXException
     *          when setting up task status via WorkflowControllerService fails
     * @throws FileStructureValidationException
     *          when setting up task status via WorkflowControllerService fails
     */
    public void setTaskStatusUp(Task task) throws DAOException, IOException, SAXException, FileStructureValidationException {
        final Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusUp");
        workflowControllerService.setTaskStatusUp(task);
        processService.refresh(this.process);
        ProcessService.deleteSymlinksFromUserHomes(task);
        refreshParent();
        stopwatch.stop();
    }

    /**
     * Task status down.
     */
    public void setTaskStatusDown(Task task) {
        final Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusDown");
        workflowControllerService.setTaskStatusDown(task);
        ProcessService.deleteSymlinksFromUserHomes(task);
        refreshParent();
        stopwatch.stop();
    }

    /**
     * Remove task.
     */
    public void removeTask(Task task) {
        final Stopwatch stopwatch = new Stopwatch(this, "removeTask");
        this.process.getTasks().remove(task);

        List<Role> roles = task.getRoles();
        for (Role role : roles) {
            role.getTasks().remove(task);
        }
        ProcessService.deleteSymlinksFromUserHomes(task);
        stopwatch.stop();
    }


    private void refreshParent() {
        try {
            if (Objects.nonNull(process.getParent())) {
                this.process.setParent(ServiceManager.getProcessService().getById(process.getParent().getId()));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.PROCESS.getTranslationSingular(), process.getParent().getId() }, logger, e);
        }
    }
    
}
