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

import org.kitodo.services.command.CommandService;
import org.kitodo.services.data.AuthorityService;
import org.kitodo.services.data.BatchService;
import org.kitodo.services.data.ClientService;
import org.kitodo.services.data.DocketService;
import org.kitodo.services.data.FilterService;
import org.kitodo.services.data.LdapGroupService;
import org.kitodo.services.data.LdapServerService;
import org.kitodo.services.data.ProcessService;
import org.kitodo.services.data.ProjectFileGroupService;
import org.kitodo.services.data.ProjectService;
import org.kitodo.services.data.PropertyService;
import org.kitodo.services.data.RulesetService;
import org.kitodo.services.data.TaskService;
import org.kitodo.services.data.TemplateService;
import org.kitodo.services.data.UserGroupClientAuthorityRelationService;
import org.kitodo.services.data.UserGroupProjectAuthorityRelationService;
import org.kitodo.services.data.UserGroupService;
import org.kitodo.services.data.UserService;
import org.kitodo.services.file.FileService;
import org.kitodo.services.schema.SchemaService;
import org.kitodo.services.security.SecurityAccessService;
import org.kitodo.services.security.SessionService;
import org.kitodo.services.validation.FileStructureValidationService;
import org.kitodo.services.validation.LongTimePreservationValidationService;
import org.kitodo.services.validation.MetadataValidationService;
import org.kitodo.services.workflow.WorkflowService;

public class ServiceManager {

    private AuthorityService authorityService;
    private BatchService batchService;
    private ClientService clientService;
    private DocketService docketService;
    private FilterService filterService;
    private LdapGroupService ldapGroupService;
    private LdapServerService ldapServerService;
    private PropertyService propertyService;
    private ProcessService processService;
    private ProjectFileGroupService projectFileGroupService;
    private ProjectService projectService;
    private RulesetService rulesetService;
    private TaskService taskService;
    private TemplateService templateService;
    private UserGroupService userGroupService;
    private UserGroupClientAuthorityRelationService userGroupClientAuthorityRelationService;
    private UserGroupProjectAuthorityRelationService userGroupProjectAuthorityRelationService;
    private UserService userService;
    private org.kitodo.services.data.WorkflowService workflowDataService;
    private FileService fileService;
    private CommandService commandService;
    private SchemaService schemaService;
    private SecurityAccessService securityAccessService;
    private FileStructureValidationService fileStructureValidationService;
    private LongTimePreservationValidationService longTimePreservationValidationService;
    private MetadataValidationService metadataValidationService;
    private SessionService sessionService;
    private WorkflowService workflowService;

    private void initializeAuthorizationService() {
        if (authorityService == null) {
            authorityService = AuthorityService.getInstance();
        }
    }

    private void initializeBatchService() {
        if (batchService == null) {
            batchService = BatchService.getInstance();
        }
    }

    private void initializeClientService() {
        if (clientService == null) {
            clientService = ClientService.getInstance();
        }
    }

    private void initializeDocketService() {
        if (docketService == null) {
            docketService = DocketService.getInstance();
        }
    }

    private void initializeFilterService() {
        if (filterService == null) {
            filterService = FilterService.getInstance();
        }
    }

    private void initializeLdapGroupService() {
        if (ldapGroupService == null) {
            ldapGroupService = new LdapGroupService();
        }
    }

    private void initializeLdapServerService() {
        if (ldapServerService == null) {
            ldapServerService = LdapServerService.getInstance();
        }
    }

    private void initializePropertyService() {
        if (propertyService == null) {
            propertyService = PropertyService.getInstance();
        }
    }

    private void initializeProcessService() {
        if (processService == null) {
            processService = ProcessService.getInstance();
        }
    }

    private void initializeProjectFileGroupService() {
        if (projectFileGroupService == null) {
            projectFileGroupService = new ProjectFileGroupService();
        }
    }

    private void initializeProjectService() {
        if (projectService == null) {
            projectService = ProjectService.getInstance();
        }
    }

    private void initializeRulesetService() {
        if (rulesetService == null) {
            rulesetService = RulesetService.getInstance();
        }
    }

    private void initializeSessionService() {
        if (sessionService == null) {
            sessionService = SessionService.getInstance();
        }
    }

    private void initializeSecurityAccessService() {
        if (securityAccessService == null) {
            securityAccessService = SecurityAccessService.getInstance();
        }
    }

    private void initializeTaskService() {
        if (taskService == null) {
            taskService = TaskService.getInstance();
        }
    }

    private void initializeTemplateService() {
        if (templateService == null) {
            templateService = TemplateService.getInstance();
        }
    }

    private void initializeUserGroupService() {
        if (userGroupService == null) {
            userGroupService = UserGroupService.getInstance();
        }
    }

    private void initializeUserGroupClientAuthorityRelationService() {
        if (userGroupClientAuthorityRelationService == null) {
            userGroupClientAuthorityRelationService = UserGroupClientAuthorityRelationService.getInstance();
        }
    }

    private void initializeUserGroupProjectAuthorityRelationService() {
        if (userGroupProjectAuthorityRelationService == null) {
            userGroupProjectAuthorityRelationService = UserGroupProjectAuthorityRelationService.getInstance();
        }
    }

    private void initializeUserService() {
        if (userService == null) {
            userService = UserService.getInstance();
        }
    }

    private void initializeWorkflowDataService() {
        if (workflowDataService == null) {
            workflowDataService = new org.kitodo.services.data.WorkflowService();
        }
    }

    private void initializeFileService() {
        if (fileService == null) {
            fileService = new FileService();
        }
    }

    private void initializeCommandService() {
        if (commandService == null) {
            commandService = new CommandService();
        }
    }

    private void initializeSchemaService() {
        if (schemaService == null) {
            schemaService = new SchemaService();
        }
    }

    private void initializeFileStructureValidationService() {
        if (fileStructureValidationService == null) {
            fileStructureValidationService = new FileStructureValidationService();
        }
    }

    private void initializeLongTimePreservationValidationService() {
        if (longTimePreservationValidationService == null) {
            longTimePreservationValidationService = new LongTimePreservationValidationService();
        }
    }

    private void initializeMetadataValidationService() {
        if (metadataValidationService == null) {
            metadataValidationService = new MetadataValidationService();
        }
    }

    private void initializeWorkflowService() {
        if (workflowService == null) {
            workflowService = new WorkflowService();
        }
    }

    /**
     * Initialize AuthorityService if it is not yet initialized and next return it.
     *
     * @return AuthorityService object
     */
    public AuthorityService getAuthorityService() {
        initializeAuthorizationService();
        return authorityService;
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
     * Initialize ClientService if it is not yet initialized and next return it.
     *
     * @return ClientService object
     */
    public ClientService getClientService() {
        initializeClientService();
        return clientService;
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
     * Initialize FilterService if it is not yet initialized and next return it.
     *
     * @return FilterService object
     */
    public FilterService getFilterService() {
        initializeFilterService();
        return filterService;
    }

    /**
     * Initialize LdapGroupService if it is not yet initialized and next return
     * it.
     *
     * @return LdapGroupService object
     */
    public LdapGroupService getLdapGroupService() {
        initializeLdapGroupService();
        return ldapGroupService;
    }

    /**
     * Initialize LdapServerService if it is not yet initialized and next return
     * it.
     *
     * @return LdapServerService object
     */
    public LdapServerService getLdapServerService() {
        initializeLdapServerService();
        return ldapServerService;
    }

    /**
     * Initialize PropertyService if it is not yet initialized and next return
     * it.
     *
     * @return PropertyService object
     */
    public PropertyService getPropertyService() {
        initializePropertyService();
        return propertyService;
    }

    /**
     * Initialize ProcessService if it is not yet initialized and next return
     * it.
     *
     * @return ProcessService object
     */
    public ProcessService getProcessService() {
        initializeProcessService();
        return processService;
    }

    /**
     * Initialize ProjectFileGroupService if it is not yet initialized and next
     * return it.
     *
     * @return ProjectFileGroupService object
     */
    public ProjectFileGroupService getProjectFileGroupService() {
        initializeProjectFileGroupService();
        return projectFileGroupService;
    }

    /**
     * Initialize ProjectService if it is not yet initialized and next return
     * it.
     *
     * @return ProjectService object
     */
    public ProjectService getProjectService() {
        initializeProjectService();
        return projectService;
    }

    /**
     * Initialize RulesetService if it is not yet initialized and next return
     * it.
     *
     * @return RulesetService object
     */
    public RulesetService getRulesetService() {
        initializeRulesetService();
        return rulesetService;
    }

    /**
     * Initialize SessionService if it is not yet initialized and next return
     * it.
     *
     * @return SessionService object
     */
    public SessionService getSessionService() {
        initializeSessionService();
        return sessionService;
    }

    /**
     * Initialize SecurityAccessService if it is not yet initialized and next return
     * it.
     *
     * @return SecurityAccessService object
     */
    public SecurityAccessService getSecurityAccessService() {
        initializeSecurityAccessService();
        return securityAccessService;
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
     * Initialize TemplateService if it is not yet initialized and next return it.
     *
     * @return TemplateService object
     */
    public TemplateService getTemplateService() {
        initializeTemplateService();
        return templateService;
    }

    /**
     * Initialize UserGroupService if it is not yet initialized and next return
     * it.
     *
     * @return UserGroupService object
     */
    public UserGroupService getUserGroupService() {
        initializeUserGroupService();
        return userGroupService;
    }

    /**
     * Initialize UserGroupClientAuthorityRelationService if it is not yet
     * initialized and next return it.
     *
     * @return UserGroupClientAuthorityRelationService object
     */
    public UserGroupClientAuthorityRelationService getUserGroupClientAuthorityRelationService() {
        initializeUserGroupClientAuthorityRelationService();
        return userGroupClientAuthorityRelationService;
    }

    /**
     * Initialize UserGroupProjectAuthorityRelationService if it is not yet
     * initialized and next return it.
     *
     * @return UserGroupProjectAuthorityRelationService object
     */
    public UserGroupProjectAuthorityRelationService getUserGroupProjectAuthorityRelationService() {
        initializeUserGroupProjectAuthorityRelationService();
        return userGroupProjectAuthorityRelationService;
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
     * Initialize WorkflowService if it is not yet initialized and next return it.
     *
     * @return WorkflowService object
     */
    public org.kitodo.services.data.WorkflowService getWorkflowDataService() {
        initializeWorkflowDataService();
        return workflowDataService;
    }

    /**
     * Initialize FileService if it is not yet initialized and next return it.
     *
     * @return FileService object
     */
    public FileService getFileService() {
        initializeFileService();
        return fileService;
    }

    /**
     * Initialize CommandService if it is not yet initialized and next return it.
     *
     * @return CommandService object
     */
    public CommandService getCommandService() {
        initializeCommandService();
        return commandService;
    }

    /**
     * Initialize SchemaService if it is not yet initialized and next return it.
     *
     * @return SchemaService object
     */
    public SchemaService getSchemaService() {
        initializeSchemaService();
        return schemaService;
    }

    /**
     * Initialize FileStructureValidationService if it is not yet initialized and next return it.
     *
     * @return FileStructureValidationService object
     */
    public FileStructureValidationService getFileStructureValidationService() {
        initializeFileStructureValidationService();
        return fileStructureValidationService;
    }

    /**
     * Initialize LongTimePreservationValidationService if it is not yet initialized and next return it.
     *
     * @return LongTimePreservationValidationService object
     */
    public LongTimePreservationValidationService getLongTimePreservationValidationService() {
        initializeLongTimePreservationValidationService();
        return longTimePreservationValidationService;
    }

    /**
     * Initialize MetadataValidationService if it is not yet initialized and next return it.
     *
     * @return MetadataValidationService object
     */
    public MetadataValidationService getMetadataValidationService() {
        initializeMetadataValidationService();
        return metadataValidationService;
    }

    /**
     * Initialize WorkflowService if it is not yet initialized and next return it.
     *
     * @return WorkflowService object
     */
    public WorkflowService getWorkflowService() {
        initializeWorkflowService();
        return workflowService;
    }
}
