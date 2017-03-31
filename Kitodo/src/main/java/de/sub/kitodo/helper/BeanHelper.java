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

package de.sub.kitodo.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.beans.WorkpieceProperty;
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
        ProcessProperty property = new ProcessProperty();
        property.setTitle(title);
        property.setValue(value);
        property.setProcess(process);
        List<ProcessProperty> properties = serviceManager.getProcessService().getPropertiesInitialized(process);
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
        TemplateProperty property = new TemplateProperty();
        property.setTitle(title);
        property.setValue(value);
        property.setTemplate(template);
        List<TemplateProperty> properties = template.getProperties();
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
        WorkpieceProperty property = new WorkpieceProperty();
        property.setTitle(title);
        property.setValue(value);
        property.setWorkpiece(workpiece);
        List<WorkpieceProperty> properties = workpiece.getProperties();
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

            /*
             * Details des Schritts
             */
            Task taskNew = new Task();
            taskNew.setTypeAutomatic(task.isTypeAutomatic());
            taskNew.setScriptName1(task.getScriptName1());
            taskNew.setScriptName2(task.getScriptName2());
            taskNew.setScriptName3(task.getScriptName3());
            taskNew.setScriptName4(task.getScriptName4());
            taskNew.setScriptName5(task.getScriptName5());

            taskNew.setTypeAutomaticScriptPath(task.getTypeAutomaticScriptPath());
            taskNew.setTypeAutomaticScriptPath2(task.getTypeAutomaticScriptPath2());
            taskNew.setTypeAutomaticScriptPath3(task.getTypeAutomaticScriptPath3());
            taskNew.setTypeAutomaticScriptPath4(task.getTypeAutomaticScriptPath4());
            taskNew.setTypeAutomaticScriptPath5(task.getTypeAutomaticScriptPath5());
            taskNew.setBatchStep(task.getBatchStep());
            taskNew.setTypeScriptStep(task.getTypeScriptStep());
            taskNew.setTypeAcceptClose(task.isTypeAcceptClose());
            taskNew.setTypeAcceptModule(task.isTypeAcceptModule());
            taskNew.setTypeAcceptModuleAndClose(task.isTypeAcceptModuleAndClose());
            taskNew.setTypeCloseVerify(task.isTypeCloseVerify());
            taskNew.setTypeModuleName(task.getTypeModuleName());
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

            taskNew.setStepPlugin(task.getStepPlugin());
            taskNew.setValidationPlugin(task.getValidationPlugin());

            /*
             * Benutzer übernehmen
             */
            List<User> myUsers = new ArrayList<>();
            for (User userNew : task.getUsers()) {
                myUsers.add(userNew);
            }
            taskNew.setUsers(myUsers);

            /*
             * Benutzergruppen übernehmen
             */
            List<UserGroup> myUserGroups = new ArrayList<>();
            for (UserGroup userGroupNew : task.getUserGroups()) {
                myUserGroups.add(userGroupNew);
            }
            taskNew.setUserGroups(myUserGroups);

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
            List<WorkpieceProperty> myProperties = new ArrayList<>();
            for (Iterator<WorkpieceProperty> iterator = workpiece.getProperties().iterator(); iterator.hasNext();) {
                WorkpieceProperty property = iterator.next();
                WorkpieceProperty propertyNew = new WorkpieceProperty();
                propertyNew.setObligatory(property.isObligatory());
                propertyNew.setType(property.getType());
                propertyNew.setTitle(property.getTitle());
                propertyNew.setValue(property.getValue());
                propertyNew.setWorkpiece(workpieceNew);
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
        List<ProcessProperty> myProperties = new ArrayList<>();
        for (Iterator<ProcessProperty> iterator = processTemplate.getProperties().iterator(); iterator.hasNext();) {
            ProcessProperty property = iterator.next();
            ProcessProperty propertyNew = new ProcessProperty();
            propertyNew.setObligatory(property.isObligatory());
            propertyNew.setType(property.getType());
            propertyNew.setTitle(property.getTitle());
            propertyNew.setValue(property.getValue());
            propertyNew.setProcess(processCopy);
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
            List<TemplateProperty> myProperties = new ArrayList<>();
            for (Iterator<TemplateProperty> iterator = template.getProperties().iterator(); iterator.hasNext();) {
                TemplateProperty property = iterator.next();
                TemplateProperty propertyNew = new TemplateProperty();
                propertyNew.setObligatory(property.isObligatory());
                propertyNew.setType(property.getType());
                propertyNew.setTitle(property.getTitle());
                propertyNew.setValue(property.getValue());
                propertyNew.setTemplate(templateNew);
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
            for (WorkpieceProperty property : myWorkpiece.getProperties()) {
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
            for (TemplateProperty property : myTemplate.getProperties()) {
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
            for (WorkpieceProperty property : myWorkpiece.getProperties()) {
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
            for (TemplateProperty property : myTemplate.getProperties()) {
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
            for (WorkpieceProperty property : myWorkpiece.getProperties()) {
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
            for (TemplateProperty property : myTemplate.getProperties()) {
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
            for (WorkpieceProperty property : myWorkpiece.getProperties()) {
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
            for (TemplateProperty property : myTemplate.getProperties()) {
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
