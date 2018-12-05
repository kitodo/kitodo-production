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

package org.kitodo.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;

public class BeanHelper {

    /**
     * Private constructor to hide the implicit public one.
     */
    private BeanHelper() {

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
     */
    public static void copyTasks(Template processTemplate, Process processCopy) {
        List<Task> tasks = new ArrayList<>();

        for (Task templateTask : processTemplate.getTasks()) {
            Task task = new Task(templateTask);
            task.setProcess(processCopy);
            tasks.add(task);
        }

        adjustTaskOrdering(tasks);
        processCopy.setTasks(tasks);
    }

    /**
     * First order tasks by ids read from templates and next assign correct
     * ordering.
     * 
     * @param tasks
     *            as List of Task objects
     */
    private static void adjustTaskOrdering(List<Task> tasks) {
        tasks.sort(Comparator.comparing(Task::getOrdering).thenComparing(Task::getTitle));

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
