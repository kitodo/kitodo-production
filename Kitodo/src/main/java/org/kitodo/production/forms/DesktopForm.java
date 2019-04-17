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

package org.kitodo.production.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.json.JsonException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;

@Named("DesktopForm")
@ViewScoped
public class DesktopForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(DesktopForm.class);
    private static final String SORT_TITLE = "title";

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
    public List<ObjectType> getObjectTypes() {
        ArrayList<ObjectType> objectTypes = new ArrayList<>();
        objectTypes.add(ObjectType.TASK);
        objectTypes.add(ObjectType.USER);
        objectTypes.add(ObjectType.PROCESS);
        objectTypes.add(ObjectType.DOCKET);
        objectTypes.add(ObjectType.PROJECT);
        objectTypes.add(ObjectType.RULESET);
        objectTypes.add(ObjectType.TEMPLATE);
        objectTypes.add(ObjectType.ROLE);
        objectTypes.add(ObjectType.WORKFLOW);
        return objectTypes;
    }

    /**
     * Get tasks.
     *
     * @return task list
     */
    public List getTasks() {
        try {
            if (ServiceManager.getSecurityAccessService().hasAuthorityToViewTaskList()) {
                return ServiceManager.getTaskService().loadData(0, 10, SORT_TITLE, SortOrder.ASCENDING, new HashMap());
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
            if (ServiceManager.getSecurityAccessService().hasAuthorityToViewProcessList()) {
                return ServiceManager.getProcessService().loadData(0, 10,SORT_TITLE, SortOrder.ASCENDING, null);
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
            if (ServiceManager.getSecurityAccessService().hasAuthorityToViewProjectList()) {
                return ServiceManager.getProjectService().loadData(0, 10, SORT_TITLE, SortOrder.ASCENDING, null);
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
                case TASK:
                    return ServiceManager.getTaskService().countResults(null);
                case USER:
                    return ServiceManager.getUserService().countResults(null);
                case DOCKET:
                    return ServiceManager.getDocketService().countResults(null);
                case PROCESS:
                    return ServiceManager.getProcessService().countResults(null);
                case PROJECT:
                    return ServiceManager.getProjectService().countResults(null);
                case RULESET:
                    return ServiceManager.getRulesetService().countResults(null);
                case TEMPLATE:
                    return ServiceManager.getTemplateService().countResults(null);
                case ROLE:
                    return ServiceManager.getRoleService().countResults(null);
                case WORKFLOW:
                    return ServiceManager.getWorkflowService().countResults(null);
                default:
                    return 0L;
            }

        } catch (DAOException | DataException | JsonException e) {
            Helper.setErrorMessage("Unable to load number of elements", logger, e);
        }
        return 0L;
    }
}
