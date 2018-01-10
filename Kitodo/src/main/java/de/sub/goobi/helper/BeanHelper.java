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
    public static void copyTasks(Process processTemplate, Process processCopy) {
        List<Task> myTasks = new ArrayList<>();
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

            // set up the users
            List<User> users = new ArrayList<>(task.getUsers());
            taskNew.setUsers(users);

            // set up user's groups
            List<UserGroup> userGroups = new ArrayList<>(task.getUserGroups());
            taskNew.setUserGroups(userGroups);

            // save task
            myTasks.add(taskNew);
        }
        processCopy.setTasks(myTasks);
    }

    /**
     * Copy workpieces from process' template to process.
     *
     * @param processTemplate
     *            template object
     * @param processCopy
     *            new object
     */
    public static void copyWorkpieces(Process processTemplate, Process processCopy) {
        List<Property> workpieceProperties = new ArrayList<>();
        for (Property workpieceProperty : processTemplate.getWorkpieces()) {
            Property propertyNew = new Property();
            propertyNew.setObligatory(workpieceProperty.isObligatory());
            propertyNew.setType(workpieceProperty.getType());
            propertyNew.setTitle(workpieceProperty.getTitle());
            propertyNew.setValue(workpieceProperty.getValue());
            propertyNew.getWorkpieces().add(processCopy);
            workpieceProperties.add(propertyNew);
        }
        processCopy.setWorkpieces(workpieceProperties);
    }

    /**
     * Copy properties from process' template to process.
     *
     * @param processTemplate
     *            template object
     * @param processCopy
     *            new object
     */
    public static void copyProperties(Process processTemplate, Process processCopy) {
        List<Property> myProperties = new ArrayList<>();
        for (Property templateProperty : processTemplate.getProperties()) {
            Property propertyNew = new Property();
            propertyNew.setObligatory(templateProperty.isObligatory());
            propertyNew.setType(templateProperty.getType());
            propertyNew.setTitle(templateProperty.getTitle());
            propertyNew.setValue(templateProperty.getValue());
            propertyNew.getProcesses().add(processCopy);
            myProperties.add(propertyNew);
        }
        // TODO read property configuration
        processCopy.setProperties(myProperties);
    }

    /**
     * Copy scan templates from process' template to process.
     *
     * @param processTemplate
     *            template object
     * @param processCopy
     *            new object
     */
    public static void copyScanTemplates(Process processTemplate, Process processCopy) {
        List<Property> templateProperties = new ArrayList<>();
        for (Property templateProperty : processTemplate.getTemplates()) {
            Property propertyNew = new Property();
            propertyNew.setObligatory(templateProperty.isObligatory());
            propertyNew.setType(templateProperty.getType());
            propertyNew.setTitle(templateProperty.getTitle());
            propertyNew.setValue(templateProperty.getValue());
            propertyNew.getTemplates().add(processCopy);
            templateProperties.add(propertyNew);
        }
        processCopy.setTemplates(templateProperties);
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

    /**
     * Determine scan template property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @return property String
     */
    public static String determineScanTemplateProperty(Process process, String inputProperty) {
        String propertyString = "";
        for (Property templateProperty : process.getTemplates()) {
            if (templateProperty.getTitle().equals(inputProperty)) {
                propertyString = templateProperty.getValue();
            }
        }
        return propertyString;
    }

    /**
     * Change workpiece property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void changeWorkpieceProperty(Process process, String inputProperty, String inputValue) {
        for (Property workpieceProperty : process.getWorkpieces()) {
            if (workpieceProperty.getTitle().equals(inputProperty)) {
                workpieceProperty.setValue(inputValue);
            }
        }
    }

    /**
     * Change scan template property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void changeScanTemplateProperty(Process process, String inputProperty, String inputValue) {
        for (Property templateProperty : process.getTemplates()) {
            if (templateProperty.getTitle().equals(inputProperty)) {
                templateProperty.setValue(inputValue);
            }
        }
    }

    /**
     * Remove workpiece property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void removeWorkpieceProperty(Process process, String inputProperty, String inputValue) {
        for (Property workpieceProperty : process.getWorkpieces()) {
            if (workpieceProperty.getTitle().equals(inputProperty) && workpieceProperty.getValue().equals(inputValue)) {
                process.getWorkpieces().remove(workpieceProperty);
            }
        }
    }

    /**
     * Remove scan template property.
     *
     * @param process
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void removeScanTemplateProperty(Process process, String inputProperty, String inputValue) {
        for (Property templateProperty : process.getTemplates()) {
            if (templateProperty.getTitle().equals(inputProperty) && templateProperty.getValue().equals(inputValue)) {
                process.getTemplates().remove(templateProperty);
            }
        }
    }

    /**
     * Remove double workpiece property.
     *
     * @param process
     *            process object
     */
    public static void removeDoubleWorkpieceProperty(Process process) {
        for (Property workpieceProperty : process.getWorkpieces()) {
            List<String> singleProperty = new ArrayList<>();
            /* prüfen, ob die Eigenschaft doppelt, wenn ja, löschen */
            if (singleProperty.contains(workpieceProperty.getTitle() + "|" + workpieceProperty.getValue())) {
                process.getWorkpieces().remove(workpieceProperty);
            } else {
                singleProperty.add(workpieceProperty.getTitle() + "|" + workpieceProperty.getValue());
            }

        }
    }

    /**
     * Remove double scan template property.
     *
     * @param process
     *            process object
     */
    public static void removeDoubleScanTemplateProperty(Process process) {
        for (Property templateProperty : process.getTemplates()) {
            List<String> singleProperty = new ArrayList<>();
            /* prüfen, ob die Eigenschaft doppelt, wenn ja, löschen */
            if (singleProperty.contains(templateProperty.getTitle() + "|" + templateProperty.getValue())) {
                process.getTemplates().remove(templateProperty);
            } else {
                singleProperty.add(templateProperty.getTitle() + "|" + templateProperty.getValue());
            }
        }
    }
}
