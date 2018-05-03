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

package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;

public class BeanHelper {

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
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }

    /**
     * Add property for template.
     *
     * @param template
     *            object
     * @param title
     *            String
     * @param value
     *            String
     */
    public static void addPropertyForTemplate(Process template, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getTemplates().add(template);
        List<Property> properties = template.getTemplates();
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }

    /**
     * Add property for workpiece.
     *
     * @param workpiece
     *            object
     * @param title
     *            String
     * @param value
     *            String
     */
    public static void addPropertyForWorkpiece(Process workpiece, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getWorkpieces();
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }

    /**
     * Copy tasks from process' template to process.
     *
     * @param processTemplate
     *            template object
     * @param processCopy
     *            new object
     * @param workflowConditions
     *            list of string values for workflow gateways, if list is null or
     *            empty only tasks with default workflow conditions will be inserted
     *            to newly created process
     */
    public static void copyTasks(Template processTemplate, Process processCopy, List<String> workflowConditions) {
        List<Task> tasks = new ArrayList<>();
        for (Task templateTask : processTemplate.getTasks()) {
            String taskWorkflowCondition = templateTask.getWorkflowCondition();
            if (Objects.isNull(workflowConditions) || workflowConditions.isEmpty()) {
                // tasks created before workflow functionality was introduced has null value
                if (Objects.isNull(taskWorkflowCondition) || taskWorkflowCondition.contains("default")) {
                    Task task = getCopiedTask(templateTask);
                    task.setProcess(processCopy);
                    tasks.add(task);
                }
            } else {
                for (String workflowCondition : workflowConditions) {
                    if (taskWorkflowCondition.contains("default")) {
                        Task task = getCopiedTask(templateTask);
                        task.setProcess(processCopy);
                        tasks.add(task);
                    } else if (taskWorkflowCondition.contains(workflowCondition)) {
                        Task task = getCopiedTask(templateTask);
                        task.setProcess(processCopy);
                        tasks.add(task);
                    }
                }
            }
        }
        adjustTaskOrdering(tasks);
        processCopy.setTasks(tasks);
    }

    private static Task getCopiedTask(Task templateTask) {
        Task task = new Task();
        task.setTypeAutomatic(templateTask.isTypeAutomatic());
        task.setScriptName(templateTask.getScriptName());
        task.setScriptPath(templateTask.getScriptPath());
        task.setBatchStep(templateTask.isBatchStep());
        task.setTypeAcceptClose(templateTask.isTypeAcceptClose());
        task.setTypeCloseVerify(templateTask.isTypeCloseVerify());
        task.setTypeExportDMS(templateTask.isTypeExportDMS());
        task.setTypeExportRussian(templateTask.isTypeExportRussian());
        task.setTypeImagesRead(templateTask.isTypeImagesRead());
        task.setTypeImagesWrite(templateTask.isTypeImagesWrite());
        task.setTypeImportFileUpload(templateTask.isTypeImportFileUpload());
        task.setTypeMetadata(templateTask.isTypeMetadata());
        task.setPriority(templateTask.getPriority());
        task.setProcessingStatusEnum(templateTask.getProcessingStatusEnum());
        task.setOrdering(templateTask.getOrdering());
        task.setTitle(templateTask.getTitle());
        task.setHomeDirectory(templateTask.getHomeDirectory());

        // necessary to create new ArrayList in other case session problem!
        ArrayList<User> users = new ArrayList<>(templateTask.getUsers());
        task.setUsers(users);

        // necessary to create new ArrayList in other case session problem!
        ArrayList<UserGroup> userGroups = new ArrayList<>(templateTask.getUserGroups());
        task.setUserGroups(userGroups);

        return task;
    }

    /**
     * First order tasks by ids read from templates and next assign correct
     * ordering.
     * 
     * @param tasks
     *            as List of Task objects
     */
    private static void adjustTaskOrdering(List<Task> tasks) {
        tasks.sort(Comparator.comparing(Task::getOrdering));

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setOrdering(i + 1);
        }
    }

    /**
     * Determine workpiece property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @return property String
     */
    public static String determineWorkpieceProperty(Process process, String inputProperty) {
        String propertyString = "";
        for (Property workpieceProperty : process.getWorkpieces()) {
            if (workpieceProperty.getTitle().equals(inputProperty)) {
                propertyString = workpieceProperty.getValue();
            }

        }
        return propertyString;
    }
}
