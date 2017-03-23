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

package org.kitodo.services;

import org.kitodo.services.data.BatchService;
import org.kitodo.services.data.DocketService;
import org.kitodo.services.data.HistoryService;
import org.kitodo.services.data.LdapGroupService;
import org.kitodo.services.data.ProcessPropertyService;
import org.kitodo.services.data.ProcessService;
import org.kitodo.services.data.ProjectFileGroupService;
import org.kitodo.services.data.ProjectService;
import org.kitodo.services.data.RulesetService;
import org.kitodo.services.data.TaskService;
import org.kitodo.services.data.TemplatePropertyService;
import org.kitodo.services.data.TemplateService;
import org.kitodo.services.data.UserGroupService;
import org.kitodo.services.data.UserPropertyService;
import org.kitodo.services.data.UserService;
import org.kitodo.services.data.WorkpiecePropertyService;
import org.kitodo.services.data.WorkpieceService;

public class ServiceManager {

    private BatchService batchService;
    private DocketService docketService;
    private HistoryService historyService;
    private LdapGroupService ldapGroupService;
    private ProcessPropertyService processPropertyService;
    private ProcessService processService;
    private ProjectFileGroupService projectFileGroupService;
    private ProjectService projectService;
    private RulesetService rulesetService;
    private TaskService taskService;
    private TemplatePropertyService templatePropertyService;
    private TemplateService templateService;
    private UserGroupService userGroupService;
    private UserPropertyService userPropertyService;
    private UserService userService;
    private WorkpiecePropertyService workpiecePropertyService;
    private WorkpieceService workpieceService;

    private void initializeBatchService() {
        if (batchService == null) {
            batchService = new BatchService();
        }
    }

    private void initializeDocketService() {
        if (docketService == null) {
            docketService = new DocketService();
        }
    }

    private void initializeHistoryService() {
        if (historyService == null) {
            historyService = new HistoryService();
        }
    }

    private void initializeLdapGroupService() {
        if (ldapGroupService == null) {
            ldapGroupService = new LdapGroupService();
        }
    }

    private void initializeProcessPropertyService() {
        if (processPropertyService == null) {
            processPropertyService = new ProcessPropertyService();
        }
    }

    private void initializeProcessService() {
        if (processService == null) {
            processService = new ProcessService();
        }
    }

    private void initializeProjectFileGroupService() {
        if (projectFileGroupService == null) {
            projectFileGroupService = new ProjectFileGroupService();
        }
    }

    private void initializeProjectService() {
        if (projectService == null) {
            projectService = new ProjectService();
        }
    }

    private void initializeRulesetService() {
        if (rulesetService == null) {
            rulesetService = new RulesetService();
        }
    }

    private void initializeTaskService() {
        if (taskService == null) {
            taskService = new TaskService();
        }
    }

    private void initializeTemplatePropertyService() {
        if (templatePropertyService == null) {
            templatePropertyService = new TemplatePropertyService();
        }
    }

    private void initializeTemplateService() {
        if (templateService == null) {
            templateService = new TemplateService();
        }
    }

    private void initializeUserGroupService() {
        if (userGroupService == null) {
            userGroupService = new UserGroupService();
        }
    }

    private void initializeUserPropertyService() {
        if (userPropertyService == null) {
            userPropertyService = new UserPropertyService();
        }
    }

    private void initializeUserService() {
        if (userService == null) {
            userService = new UserService();
        }
    }

    private void initializeWorkpiecePropertyService() {
        if (workpiecePropertyService == null) {
            workpiecePropertyService = new WorkpiecePropertyService();
        }
    }

    private void initializeWorkpieceService() {
        if (workpieceService == null) {
            workpieceService = new WorkpieceService();
        }
    }

    /**
     * Initialize BatchService if it is not yet initialized and next return it.
     *
     * @return BatchService object
     */
    public BatchService getBatchService() {
        initializeBatchService();
        return batchService;
    }

    /**
     * Initialize DocketService if it is not yet initialized and next return it.
     *
     * @return DocketService object
     */
    public DocketService getDocketService() {
        initializeDocketService();
        return docketService;
    }

    /**
     * Initialize HistoryService if it is not yet initialized and next return it.
     *
     * @return HistoryService object
     */
    public HistoryService getHistoryService() {
        initializeHistoryService();
        return historyService;
    }

    /**
     * Initialize LdapGroupService if it is not yet initialized and next return it.
     *
     * @return LdapGroupService object
     */
    public LdapGroupService getLdapGroupService() {
        initializeLdapGroupService();
        return ldapGroupService;
    }

    /**
     * Initialize ProcessPropertyService if it is not yet initialized and next return it.
     *
     * @return ProcessPropertyService object
     */
    public ProcessPropertyService getProcessPropertyService() {
        initializeProcessPropertyService();
        return processPropertyService;
    }

    /**
     * Initialize ProcessService if it is not yet initialized and next return it.
     *
     * @return ProcessService object
     */
    public ProcessService getProcessService() {
        initializeProcessService();
        return processService;
    }

    /**
     * Initialize ProjectFileGroupService if it is not yet initialized and next return it.
     *
     * @return ProjectFileGroupService object
     */
    public ProjectFileGroupService getProjectFileGroupService() {
        initializeProjectFileGroupService();
        return projectFileGroupService;
    }

    /**
     * Initialize ProjectService if it is not yet initialized and next return it.
     *
     * @return ProjectService object
     */
    public ProjectService getProjectService() {
        initializeProjectService();
        return projectService;
    }

    /**
     * Initialize RulesetService if it is not yet initialized and next return it.
     *
     * @return RulesetService object
     */
    public RulesetService getRulesetService() {
        initializeRulesetService();
        return rulesetService;
    }

    /**
     * Initialize TaskService if it is not yet initialized and next return it.
     *
     * @return TaskService object
     */
    public TaskService getTaskService() {
        initializeTaskService();
        return taskService;
    }

    /**
     * Initialize TemplatePropertyService if it is not yet initialized and next return it.
     *
     * @return TemplatePropertyService object
     */
    public TemplatePropertyService getTemplatePropertyService() {
        initializeTemplatePropertyService();
        return templatePropertyService;
    }

    /**
     * Initialize TemplateService if it is not yet initialized and next return it.
     *
     * @return TemplateService object
     */
    public TemplateService getTemplateService() {
        initializeTemplateService();
        return templateService;
    }

    /**
     * Initialize UserGroupService if it is not yet initialized and next return it.
     *
     * @return UserGroupService object
     */
    public UserGroupService getUserGroupService() {
        initializeUserGroupService();
        return userGroupService;
    }

    /**
     * Initialize UserPropertyService if it is not yet initialized and next return it.
     *
     * @return UserPropertyService object
     */
    public UserPropertyService getUserPropertyService() {
        initializeUserPropertyService();
        return userPropertyService;
    }

    /**
     * Initialize UserService if it is not yet initialized and next return it.
     *
     * @return UserService object
     */
    public UserService getUserService() {
        initializeUserService();
        return userService;
    }

    /**
     * Initialize WorkpiecePropertyService if it is not yet initialized and next return it.
     *
     * @return WorkpiecePropertyService object
     */
    public WorkpiecePropertyService getWorkpiecePropertyService() {
        initializeWorkpiecePropertyService();
        return workpiecePropertyService;
    }

    /**
     * Initialize WorkpieceService if it is not yet initialized and next return it.
     *
     * @return WorkpieceService object
     */
    public WorkpieceService getWorkpieceService() {
        initializeWorkpieceService();
        return workpieceService;
    }
}
