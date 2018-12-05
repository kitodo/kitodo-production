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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.json.JsonException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;

@Named("DesktopForm")
@ViewScoped
public class DesktopForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(DesktopForm.class);
    private static final String SORT_TITLE_ASC = "{\"title\":\"asc\" }";

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
            if (serviceManager.getSecurityAccessService().hasAuthorityToViewTaskList()) {
                return serviceManager.getTaskService().findAll(SORT_TITLE_ASC, 0, 10, new HashMap());
            }
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.TASK.getTranslationPlural() }, logger,
                e);
        }
        return new ArrayList();
    }

    /**
     * Get processes.
     *
     * @return process list
     */
    public List getProcesses() {
        try {
            if (serviceManager.getSecurityAccessService().hasAuthorityToViewProcessList()) {
                return serviceManager.getProcessService().findAll(SORT_TITLE_ASC, 0, 10, null);
            }
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROCESS.getTranslationPlural() },
                logger, e);
        }
        return new ArrayList();
    }

    /**
     * Get projects.
     *
     * @return project list
     */
    public List getProjects() {
        try {
            if (serviceManager.getSecurityAccessService().hasAuthorityToViewProjectList()) {
                return serviceManager.getProjectService().findAll(SORT_TITLE_ASC, 0, 10, null);
            }
        } catch (DataException | JsonException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
        }
        return new ArrayList();
    }

    /**
     * Get number of elements of given type 'objectType' in index.
     *
     * @param objectType
     *            type of elements
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
                case ROLE:
                    return serviceManager.getRoleService().count();
                default:
                    return 0L;
            }

        } catch (DataException | JsonException e) {
            Helper.setErrorMessage("Unable to load number of elements", logger, e);
        }
        return 0L;
    }
}
