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

package de.sub.goobi.forms;

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;


@Named("DesktopForm")
@RequestScoped
public class DesktopForm extends BasisForm {
    private static final Logger logger = LogManager.getLogger(DesktopForm.class);
    private transient ServiceManager serviceManager = new ServiceManager();

    public DesktopForm() {
        super();
    }

    public ObjectType[] getObjectTypes() {
        return ObjectType.values();
    }

    /**
     * Get tasks.
     *
     * @return task list
     */
    public List getTasks() {
        try {
            return serviceManager.getTaskService().findAll("{\"title\":\"asc\" }", 0, 10);
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("tasks") }, logger, e);
            return new ArrayList();
        }
    }

    /**
     * Get processes.
     *
     * @return process list
     */
    public List getProcesses() {
        try {
            return serviceManager.getProcessService().findAll("{\"title\":\"asc\" }", 0, 10);
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("processes") }, logger, e);
            return new ArrayList();
        }
    }

    /**
     * Get projects.
     *
     * @return project list
     */
    public List getProjects() {
        try {
            return serviceManager.getProjectService().findAll("{\"title\":\"asc\" }", 0, 10);
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("projects") }, logger, e);
            return new ArrayList();
        }
    }

    /**
     * Get number of elements of given type 'objectType' in index.
     *
     * @param objectType
     *          type of elements
     * @return number of elements
     */
    public int getNumberOfElements(ObjectType objectType) {
        try {
            switch (objectType) {
                case NONE:
                    return 0;
                case TASK:
                    return serviceManager.getTaskService().findAll().size();
                case USER:
                    return serviceManager.getUserService().findAll().size();
                case BATCH:
                    return serviceManager.getBatchService().findAll().size();
                case CLIENT:
                    return serviceManager.getClientService().findAll().size();
                case DOCKET:
                    return serviceManager.getDocketService().findAll().size();
                case FILTER:
                    return serviceManager.getFilterService().findAll().size();
                case PROCESS:
                    return serviceManager.getProcessService().findAll().size();
                case PROJECT:
                    return serviceManager.getProjectService().findAll().size();
                case RULESET:
                    return serviceManager.getRulesetService().findAll().size();
                case AUTHORITY:
                    return serviceManager.getAuthorityService().findAll().size();
                case PROPERTY:
                    return serviceManager.getPropertyService().findAll().size();
                case TEMPLATE:
                    return serviceManager.getTemplateService().findAll().size();
                case USERGROUP:
                    return serviceManager.getUserGroupService().findAll().size();
                default:
                    return 0;
            }

        } catch (DataException e) {
            Helper.setErrorMessage("Unable to load number of elements", logger, e);
        }
        return 0;
    }
}
