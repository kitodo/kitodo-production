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

package org.kitodo.forms;

import de.sub.goobi.forms.BaseForm;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.json.JsonException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;


@Named("DesktopForm")
@ViewScoped
public class DesktopForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(DesktopForm.class);
    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * Default constructor.
     */
    public DesktopForm() {
        super();
    }

    /**
     * Get values of ObjectType enum.
     *
     * @return array containing values of ObjectType enum
     */
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
            return serviceManager.getTaskService().findAll("{\"title\":\"asc\" }", 0, 10, new HashMap());
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.TASK.getTranslationPlural() }, logger, e);
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
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROCESS.getTranslationPlural() }, logger, e);
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
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() }, logger, e);
            return new ArrayList();
        }
    }

    /**
     * Get project of process.
     *
     * @param processDTO
     *          process whose project is returned
     * @return project of the given process
     */
    public ProjectDTO getProject(ProcessDTO processDTO) {
        try {
            return serviceManager.getProcessService().findById(processDTO.getId()).getProject();
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROJECT.getTranslationSingular() }, logger, e);
            return null;
        }
    }

    /**
     * Get number of elements of given type 'objectType' in index.
     *
     * @param objectType
     *          type of elements
     * @return number of elements
     */
    public long getNumberOfElements(ObjectType objectType) {
        try {
            switch (objectType) {
                case NONE:
                    return 0L;
                case TASK:
                    return serviceManager.getTaskService().count();
                case USER:
                    return serviceManager.getUserService().count();
                case BATCH:
                    return serviceManager.getBatchService().count();
                case CLIENT:
                    return serviceManager.getClientService().count();
                case DOCKET:
                    return serviceManager.getDocketService().count();
                case FILTER:
                    return serviceManager.getFilterService().count();
                case PROCESS:
                    return serviceManager.getProcessService().count();
                case PROJECT:
                    return serviceManager.getProjectService().count();
                case RULESET:
                    return serviceManager.getRulesetService().count();
                case AUTHORITY:
                    return serviceManager.getAuthorityService().count();
                case PROPERTY:
                    return serviceManager.getPropertyService().count();
                case TEMPLATE:
                    return serviceManager.getTemplateService().count();
                case USER_GROUP:
                    return serviceManager.getUserGroupService().count();
                default:
                    return 0L;
            }

        } catch (DataException | JsonException e) {
            Helper.setErrorMessage("Unable to load number of elements", logger, e);
        }
        return 0L;
    }
}
