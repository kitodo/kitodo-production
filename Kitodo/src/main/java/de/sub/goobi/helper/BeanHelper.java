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
import java.util.List;

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
     */
    public static void copyTasks(Template processTemplate, Process processCopy) {
        List<Task> tasks = new ArrayList<>();
        for (Task task : processTemplate.getTasks()) {

            Task taskNew = new Task();
            taskNew.setTypeAutomatic(task.isTypeAutomatic());
            taskNew.setScriptName(task.getScriptName());
            taskNew.setScriptPath(task.getScriptPath());
            taskNew.setBatchStep(task.isBatchStep());
            taskNew.setTypeAcceptClose(task.isTypeAcceptClose());
            taskNew.setTypeCloseVerify(task.isTypeCloseVerify());
            taskNew.setTypeExportDMS(task.isTypeExportDMS());
            taskNew.setTypeExportRussian(task.isTypeExportRussian());
            taskNew.setTypeImagesRead(task.isTypeImagesRead());
            taskNew.setTypeImagesWrite(task.isTypeImagesWrite());
            taskNew.setTypeImportFileUpload(task.isTypeImportFileUpload());
            taskNew.setTypeMetadata(task.isTypeMetadata());
            taskNew.setPriority(task.getPriority());
            taskNew.setProcessingStatusEnum(task.getProcessingStatusEnum());
            taskNew.setOrdering(task.getOrdering());
            taskNew.setTitle(task.getTitle());
            taskNew.setHomeDirectory(task.getHomeDirectory());
            taskNew.setProcess(processCopy);

            // set up the users - necessary to create new ArrayList in other case session problem!
            ArrayList<User> users = new ArrayList<>(task.getUsers());
            taskNew.setUsers(users);

            // set up user's groups - necessary to create new ArrayList in other case session problem!
            ArrayList<UserGroup> userGroups = new ArrayList<>(task.getUserGroups());
            taskNew.setUserGroups(userGroups);

            // save task
            tasks.add(taskNew);
        }
        processCopy.setTasks(tasks);
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
