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
import org.kitodo.data.database.beans.Property;
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
     * Add property for process.
     *
     * @param process
     *            object
     * @param title
     *            String
     * @param value
     *            String
     */
    public static void addPropertyForProcess(Process process, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getProcesses().add(process);
        List<Property> properties = process.getProperties();
        properties.add(property);
    }

    /**
     * Add property for template.
     *
     * @param process
     *            object
     * @param title
     *            String
     * @param value
     *            String
     */
    public static void addPropertyForTemplate(Process process, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getTemplates().add(process);
        List<Property> properties = process.getTemplates();
        properties.add(property);
    }

    /**
     * Add property for workpiece.
     *
     * @param process
     *            object
     * @param title
     *            String
     * @param value
     *            String
     */
    public static void addPropertyForWorkpiece(Process process, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getWorkpieces().add(process);
        List<Property> properties = process.getWorkpieces();
        properties.add(property);
    }

    /**
     * Copy property for process.
     *
     * @param process
     *            object
     * @param property
     *            origin property
     */
    public static void copyPropertyForProcess(Process process, Property property) {
        if (ProcessValidator.existsProperty(process.getProperties(), property)) {
            return;
        }

        Property processProperty = insertDataToProperty(property);
        processProperty.getProcesses().add(process);
        List<Property> properties = process.getProperties();
        properties.add(processProperty);
    }

    /**
     * Copy property for template.
     *
     * @param template
     *            object
     * @param property
     *            origin property
     */
    public static void copyPropertyForTemplate(Process template, Property property) {
        if (ProcessValidator.existsProperty(template.getTemplates(), property)) {
            return;
        }

        Property templateProperty = insertDataToProperty(property);
        templateProperty.getTemplates().add(template);
        List<Property> properties = template.getTemplates();
        properties.add(templateProperty);
    }

    /**
     * Add property for workpiece.
     *
     * @param workpiece
     *            object
     * @param property
     *            origin property
     */
    public static void copyPropertyForWorkpiece(Process workpiece, Property property) {
        if (ProcessValidator.existsProperty(workpiece.getWorkpieces(), property)) {
            return;
        }

        Property workpieceProperty = insertDataToProperty(property);
        workpieceProperty.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getWorkpieces();
        properties.add(workpieceProperty);
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

    private static Property insertDataToProperty(Property property) {
        Property newProperty = new Property();
        newProperty.setTitle(property.getTitle());
        newProperty.setValue(property.getValue());
        newProperty.setChoice(property.getChoice());
        newProperty.setDataType(property.getDataType());
        return newProperty;
    }
}
