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

package org.kitodo.production.process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.services.ServiceManager;

public class ProcessGenerator {

    private Process generatedProcess;
    private Project project;
    private Template template;

    /**
     * Get generatedProcess.
     *
     * @return value of generatedProcess
     */
    public Process getGeneratedProcess() {
        return generatedProcess;
    }

    /**
     * Get project.
     *
     * @return value of project
     */
    public Project getProject() {
        return project;
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
     * Generate new process for given project and template.
     *
     * @param templateId
     *            id of template to query from database
     * @param projectId
     *            id of project to query from database
     *
     * @return true if process was generated, otherwise false
     */
    public boolean generateProcess(int templateId, int projectId) throws ProcessGenerationException {
        try {
            this.template = ServiceManager.getTemplateService().getById(templateId);
            this.project = ServiceManager.getProjectService().getById(projectId);
        } catch (DAOException e) {
            throw new ProcessGenerationException(
                    "Template with id " + templateId + " or project with id " + projectId + " not found.", e);
        }

        if (ServiceManager.getTemplateService().containsUnreachableTasks(this.template.getTasks())) {
            ServiceManager.getTaskService().setUpErrorMessagesForUnreachableTasks(this.template.getTasks());
            return false;
        }

        this.generatedProcess = new Process();
        this.generatedProcess.setTitle("");
        this.generatedProcess.setTemplate(this.template);
        this.generatedProcess.setProject(this.project);
        this.generatedProcess.setRuleset(this.template.getRuleset());
        this.generatedProcess.setDocket(this.template.getDocket());

        ProcessGenerator.copyTasks(this.template, this.generatedProcess);

        return true;
    }

    /**
     * Copy tasks from process' template to process.
     *
     * @param processTemplate
     *            template object
     * @param processCopy
     *            new object
     */
    public static void copyTasks(Template processTemplate, Process processCopy) {
        List<Task> tasks = new ArrayList<>();

        for (Task templateTask : processTemplate.getTasks()) {
            Task task = new Task(templateTask);
            task.setProcess(processCopy);
            tasks.add(task);
        }

        tasks.sort(Comparator.comparing(Task::getOrdering).thenComparing(Task::getTitle));
        processCopy.setTasks(tasks);
    }
}
