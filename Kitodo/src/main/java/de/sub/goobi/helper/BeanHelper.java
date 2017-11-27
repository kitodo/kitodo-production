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
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.services.ServiceManager;

public class BeanHelper {

    private static final ServiceManager serviceManager = new ServiceManager();

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
    public static void addProperty(Process process, String title, String value) {
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
    public static void addProperty(Template template, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getTemplates().add(template);
        List<Property> properties = template.getProperties();
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
    public static void addProperty(Workpiece workpiece, String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        property.getWorkpieces().add(workpiece);
        List<Property> properties = workpiece.getProperties();
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
            taskNew.setTypeScriptStep(task.getTypeScriptStep());
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

            /*
             * Benutzer übernehmen
             */
            List<User> users = new ArrayList<>();
            users.addAll(task.getUsers());
            taskNew.setUsers(users);

            /*
             * Benutzergruppen übernehmen
             */
            List<UserGroup> userGroups = new ArrayList<>();
            userGroups.addAll(task.getUserGroups());
            taskNew.setUserGroups(userGroups);

            /* Schritt speichern */
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
        List<Workpiece> myWorkpieces = new ArrayList<>();
        for (Workpiece workpiece : processTemplate.getWorkpieces()) {
            /*
             * Details des Werkstücks
             */
            Workpiece workpieceNew = new Workpiece();
            workpieceNew.setProcess(processCopy);

            /*
             * Eigenschaften des Schritts
             */
            List<Property> myProperties = new ArrayList<>();
            for (Property workpieceProperty : workpiece.getProperties()) {
                Property propertyNew = new Property();
                propertyNew.setObligatory(workpieceProperty.isObligatory());
                propertyNew.setType(workpieceProperty.getType());
                propertyNew.setTitle(workpieceProperty.getTitle());
                propertyNew.setValue(workpieceProperty.getValue());
                propertyNew.getWorkpieces().add(workpieceNew);
                myProperties.add(propertyNew);
            }
            workpieceNew.setProperties(myProperties);

            /* Schritt speichern */
            myWorkpieces.add(workpieceNew);
        }
        processCopy.setWorkpieces(myWorkpieces);
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
        List<Template> myTemplates = new ArrayList<>();
        for (Template template : processTemplate.getTemplates()) {
            /*
             * Details der Vorlage
             */
            Template templateNew = new Template();
            templateNew.setOrigin(template.getOrigin());
            templateNew.setProcess(processCopy);

            /*
             * Eigenschaften des Schritts
             */
            List<Property> myProperties = new ArrayList<>();
            for (Property templateProperty : template.getProperties()) {
                Property propertyNew = new Property();
                propertyNew.setObligatory(templateProperty.isObligatory());
                propertyNew.setType(templateProperty.getType());
                propertyNew.setTitle(templateProperty.getTitle());
                propertyNew.setValue(templateProperty.getValue());
                propertyNew.getTemplates().add(templateNew);
                myProperties.add(propertyNew);
            }
            templateNew.setProperties(myProperties);

            /* Schritt speichern */
            myTemplates.add(templateNew);
        }
        processCopy.setTemplates(myTemplates);
    }

    /**
     * Determine workpiece property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @return property String
     */
    public static String determineWorkpieceProperty(Process myProcess, String inputProperty) {
        String propertyString = "";
        for (Workpiece myWorkpiece : myProcess.getWorkpieces()) {
            for (Property property : myWorkpiece.getProperties()) {
                if (property.getTitle().equals(inputProperty)) {
                    propertyString = property.getValue();
                }
            }
        }
        return propertyString;
    }

    /**
     * Determine scan template property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @return property String
     */
    public static String determineScanTemplateProperty(Process myProcess, String inputProperty) {
        String propertyString = "";
        for (Template myTemplate : myProcess.getTemplates()) {
            for (Property property : myTemplate.getProperties()) {
                if (property.getTitle().equals(inputProperty)) {
                    propertyString = property.getValue();
                }
            }
        }
        return propertyString;
    }

    /**
     * Change workpiece property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void changeWorkpieceProperty(Process myProcess, String inputProperty, String inputValue) {
        for (Workpiece myWorkpiece : myProcess.getWorkpieces()) {
            for (Property property : myWorkpiece.getProperties()) {
                if (property.getTitle().equals(inputProperty)) {
                    property.setValue(inputValue);
                }
            }
        }
    }

    /**
     * Change scan template property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void changeScanTemplateProperty(Process myProcess, String inputProperty, String inputValue) {
        for (Template myTemplate : myProcess.getTemplates()) {
            for (Property property : myTemplate.getProperties()) {
                if (property.getTitle().equals(inputProperty)) {
                    property.setValue(inputValue);
                }
            }
        }
    }

    /**
     * Remove workpiece property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void removeWorkpieceProperty(Process myProcess, String inputProperty, String inputValue) {
        for (Workpiece myWorkpiece : myProcess.getWorkpieces()) {
            for (Property property : myWorkpiece.getProperties()) {
                if (property.getTitle().equals(inputProperty) && property.getValue().equals(inputValue)) {
                    myWorkpiece.getProperties().remove(property);
                }
            }
        }
    }

    /**
     * Remove scan template property.
     *
     * @param myProcess
     *            process object
     * @param inputProperty
     *            input property
     * @param inputValue
     *            input value
     */
    public static void removeScanTemplateProperty(Process myProcess, String inputProperty, String inputValue) {
        for (Template myTemplate : myProcess.getTemplates()) {
            for (Property property : myTemplate.getProperties()) {
                if (property.getTitle().equals(inputProperty) && property.getValue().equals(inputValue)) {
                    myTemplate.getProperties().remove(property);
                }
            }
        }
    }

    /**
     * Remove double workpiece property.
     *
     * @param myProcess
     *            process object
     */
    public static void removeDoubleWorkpieceProperty(Process myProcess) {
        for (Workpiece myWorkpiece : myProcess.getWorkpieces()) {
            List<String> einzelstuecke = new ArrayList<>();
            for (Property property : myWorkpiece.getProperties()) {
                /* prüfen, ob die Eigenschaft doppelt, wenn ja, löschen */
                if (einzelstuecke.contains(property.getTitle() + "|" + property.getValue())) {
                    myWorkpiece.getProperties().remove(property);
                } else {
                    einzelstuecke.add(property.getTitle() + "|" + property.getValue());
                }
            }
        }
    }

    /**
     * Remove double scan template property.
     *
     * @param myProcess
     *            process object
     */
    public static void removeDoubleScanTemplateProperty(Process myProcess) {
        for (Template myTemplate : myProcess.getTemplates()) {
            List<String> einzelstuecke = new ArrayList<>();
            for (Property property : myTemplate.getProperties()) {
                /* prüfen, ob die Eigenschaft doppelt, wenn ja, löschen */
                if (einzelstuecke.contains(property.getTitle() + "|" + property.getValue())) {
                    myTemplate.getProperties().remove(property);
                } else {
                    einzelstuecke.add(property.getTitle() + "|" + property.getValue());
                }
            }
        }
    }
}
