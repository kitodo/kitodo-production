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

    public BatchService getBatchService() {
        initializeBatchService();
        return batchService;
    }

    public DocketService getDocketService() {
        initializeDocketService();
        return docketService;
    }

    public HistoryService getHistoryService() {
        initializeHistoryService();
        return historyService;
    }

    public LdapGroupService getLdapGroupService() {
        initializeLdapGroupService();
        return ldapGroupService;
    }

    public ProcessPropertyService getProcessPropertyService() {
        initializeProcessPropertyService();
        return processPropertyService;
    }

    public ProcessService getProcessService() {
        initializeProcessService();
        return processService;
    }

    public ProjectFileGroupService getProjectFileGroupService() {
        initializeProjectFileGroupService();
        return projectFileGroupService;
    }

    public ProjectService getProjectService() {
        initializeProjectService();
        return projectService;
    }

    public RulesetService getRulesetService() {
        initializeRulesetService();
        return rulesetService;
    }

    public TaskService getTaskService() {
        initializeTaskService();
        return taskService;
    }

    public TemplatePropertyService getTemplatePropertyService() {
        initializeTemplatePropertyService();
        return templatePropertyService;
    }

    public TemplateService getTemplateService() {
        initializeTemplateService();
        return templateService;
    }

    public UserGroupService getUserGroupService() {
        initializeUserGroupService();
        return userGroupService;
    }

    public UserPropertyService getUserPropertyService() {
        initializeUserPropertyService();
        return userPropertyService;
    }

    public UserService getUserService() {
        initializeUserService();
        return userService;
    }

    public WorkpiecePropertyService getWorkpiecePropertyService() {
        initializeWorkpiecePropertyService();
        return workpiecePropertyService;
    }

    public WorkpieceService getWorkpieceService() {
        initializeWorkpieceService();
        return workpieceService;
    }
}
