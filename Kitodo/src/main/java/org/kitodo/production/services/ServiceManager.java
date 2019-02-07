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

package org.kitodo.production.services;

import java.util.Objects;

import org.kitodo.production.services.command.CommandService;
import org.kitodo.production.services.data.AuthorityService;
import org.kitodo.production.services.data.BatchService;
import org.kitodo.production.services.data.ClientService;
import org.kitodo.production.services.data.DocketService;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.FolderService;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.LdapGroupService;
import org.kitodo.production.services.data.LdapServerService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.ProjectService;
import org.kitodo.production.services.data.PropertyService;
import org.kitodo.production.services.data.RoleService;
import org.kitodo.production.services.data.RulesetService;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.data.TemplateService;
import org.kitodo.production.services.data.UserService;
import org.kitodo.production.services.data.WorkflowConditionService;
import org.kitodo.production.services.data.WorkflowService;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.production.services.dataeditor.RulesetManagementService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.image.ImageService;
import org.kitodo.production.services.schema.SchemaService;
import org.kitodo.production.services.security.SecurityAccessService;
import org.kitodo.production.services.security.SessionService;
import org.kitodo.production.services.validation.FileStructureValidationService;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;
import org.kitodo.production.services.validation.MetadataValidationService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

public class ServiceManager {

    private static AuthorityService authorityService;
    private static BatchService batchService;
    private static ClientService clientService;
    private static DataEditorService dataEditorService;
    private static DocketService docketService;
    private static FilterService filterService;
    private static ImageService imageService;
    private static ImportService importService;
    private static LdapGroupService ldapGroupService;
    private static LdapServerService ldapServerService;
    private static MetsService metsService;
    private static PropertyService propertyService;
    private static ProcessService processService;
    private static FolderService folderService;
    private static ProjectService projectService;
    private static RulesetService rulesetService;
    private static TaskService taskService;
    private static TemplateService templateService;
    private static RoleService roleService;
    private static UserService userService;
    private static WorkflowService workflowService;
    private static WorkflowConditionService workflowConditionService;
    private static FileService fileService;
    private static CommandService commandService;
    private static SchemaService schemaService;
    private static SecurityAccessService securityAccessService;
    private static FileStructureValidationService fileStructureValidationService;
    private static LongTermPreservationValidationService longTermPreservationValidationService;
    private static MetadataValidationService metadataValidationService;
    private static RulesetManagementService rulesetManagementService;
    private static SessionService sessionService;
    private static WorkflowControllerService workflowControllerService;

    /**
     * Private constructor.
     */
    private ServiceManager() {
    }

    private static void initializeAuthorizationService() {
        if (authorityService == null) {
            authorityService = AuthorityService.getInstance();
        }
    }

    private static void initializeBatchService() {
        if (batchService == null) {
            batchService = BatchService.getInstance();
        }
    }

    private static void initializeClientService() {
        if (clientService == null) {
            clientService = ClientService.getInstance();
        }
    }

    private static void initializeDataEditorService() {
        if (dataEditorService == null) {
            dataEditorService = new DataEditorService();
        }
    }

    private static void initializeDocketService() {
        if (docketService == null) {
            docketService = DocketService.getInstance();
        }
    }

    private static void initializeFilterService() {
        if (filterService == null) {
            filterService = FilterService.getInstance();
        }
    }

    private static void initializeImageService() {
        if (imageService == null) {
            imageService = ImageService.getInstance();
        }
    }

    private static void initializeImportService() {
        if (Objects.isNull(importService)) {
            importService = ImportService.getInstance();
        }
    }

    private static void initializeLdapGroupService() {
        if (ldapGroupService == null) {
            ldapGroupService = new LdapGroupService();
        }
    }

    private static void initializeLdapServerService() {
        if (ldapServerService == null) {
            ldapServerService = LdapServerService.getInstance();
        }
    }

    private static void initializeMetsService() {
        if (metsService == null) {
            metsService = MetsService.getInstance();
        }
    }

    private static void initializePropertyService() {
        if (propertyService == null) {
            propertyService = PropertyService.getInstance();
        }
    }

    private static void initializeProcessService() {
        if (processService == null) {
            processService = ProcessService.getInstance();
        }
    }

    private static void initializeFolderService() {
        if (folderService == null) {
            folderService = new FolderService();
        }
    }

    private static void initializeProjectService() {
        if (projectService == null) {
            projectService = ProjectService.getInstance();
        }
    }

    private static void initializeRulesetService() {
        if (rulesetService == null) {
            rulesetService = RulesetService.getInstance();
        }
    }

    private static void initializeSessionService() {
        if (sessionService == null) {
            sessionService = SessionService.getInstance();
        }
    }

    private static void initializeSecurityAccessService() {
        if (securityAccessService == null) {
            securityAccessService = SecurityAccessService.getInstance();
        }
    }

    private static void initializeTaskService() {
        if (taskService == null) {
            taskService = TaskService.getInstance();
        }
    }

    private static void initializeTemplateService() {
        if (templateService == null) {
            templateService = TemplateService.getInstance();
        }
    }

    private static void initializeRoleService() {
        if (roleService == null) {
            roleService = RoleService.getInstance();
        }
    }

    private static void initializeUserService() {
        if (userService == null) {
            userService = UserService.getInstance();
        }
    }

    private static void initializeWorkflowService() {
        if (workflowService == null) {
            workflowService = WorkflowService.getInstance();
        }
    }

    private static void initializeWorkflowConditionService() {
        if (Objects.isNull(workflowConditionService)) {
            workflowConditionService = WorkflowConditionService.getInstance();
        }
    }

    private static void initializeFileService() {
        if (fileService == null) {
            fileService = new FileService();
        }
    }

    private static void initializeCommandService() {
        if (commandService == null) {
            commandService = new CommandService();
        }
    }

    private static void initializeSchemaService() {
        if (schemaService == null) {
            schemaService = new SchemaService();
        }
    }

    private static void initializeFileStructureValidationService() {
        if (fileStructureValidationService == null) {
            fileStructureValidationService = new FileStructureValidationService();
        }
    }

    private static void initializeLongTermPreservationValidationService() {
        if (longTermPreservationValidationService == null) {
            longTermPreservationValidationService = new LongTermPreservationValidationService();
        }
    }

    private static void initializeMetadataValidationService() {
        if (metadataValidationService == null) {
            metadataValidationService = new MetadataValidationService();
        }
    }

    private static void initializeRulesetManagementService() {
        if (rulesetManagementService == null) {
            rulesetManagementService = RulesetManagementService.getInstance();
        }
    }

    private static void initializeWorkflowControllerService() {
        if (workflowControllerService == null) {
            workflowControllerService = WorkflowControllerService.getInstance();
        }
    }

    /**
     * Initialize AuthorityService if it is not yet initialized and next return
     * it.
     *
     * @return AuthorityService object
     */
    public static AuthorityService getAuthorityService() {
        initializeAuthorizationService();
        return authorityService;
    }

    /**
     * Initialize BatchService if it is not yet initialized and next return it.
     *
     * @return BatchService object
     */
    public static BatchService getBatchService() {
        initializeBatchService();
        return batchService;
    }

    /**
     * Initialize ClientService if it is not yet initialized and next return it.
     *
     * @return ClientService object
     */
    public static ClientService getClientService() {
        initializeClientService();
        return clientService;
    }

    /**
     * Initialize DataEditorService if it is not yet initialized and next return
     * it.
     *
     * @return DataEditorService object
     */
    public static DataEditorService getDataEditorService() {
        initializeDataEditorService();
        return dataEditorService;
    }

    /**
     * Initialize DocketService if it is not yet initialized and next return it.
     *
     * @return DocketService object
     */
    public static DocketService getDocketService() {
        initializeDocketService();
        return docketService;
    }

    /**
     * Initialize FilterService if it is not yet initialized and next return it.
     *
     * @return FilterService object
     */
    public static FilterService getFilterService() {
        initializeFilterService();
        return filterService;
    }

    /**
     * Initialize ImportService if it is not yet initialized and return it.
     * @return ImportService object
     */
    public static ImportService getImportService() {
        initializeImportService();
        return importService;
    }

    /**
     * Initialize LdapGroupService if it is not yet initialized and next return
     * it.
     *
     * @return LdapGroupService object
     */
    public static LdapGroupService getLdapGroupService() {
        initializeLdapGroupService();
        return ldapGroupService;
    }

    /**
     * Initialize LdapServerService if it is not yet initialized and next return
     * it.
     *
     * @return LdapServerService object
     */
    public static LdapServerService getLdapServerService() {
        initializeLdapServerService();
        return ldapServerService;
    }

    /**
     * Initialize ImageService if it is not yet initialized and next return
     * it.
     *
     * @return ImageService object
     */
    public static ImageService getImageService() {
        initializeImageService();
        return imageService;
    }

    /**
     * Initialize MetsService if it is not yet initialized and next return it.
     *
     * @return MetsService object
     */
    public static MetsService getMetsService() {
        initializeMetsService();
        return metsService;
    }

    /**
     * Initialize PropertyService if it is not yet initialized and next return
     * it.
     *
     * @return PropertyService object
     */
    public static PropertyService getPropertyService() {
        initializePropertyService();
        return propertyService;
    }

    /**
     * Initialize ProcessService if it is not yet initialized and next return
     * it.
     *
     * @return ProcessService object
     */
    public static ProcessService getProcessService() {
        initializeProcessService();
        return processService;
    }

    /**
     * Initialize FolderService if it is not yet initialized and next return it.
     *
     * @return FolderService object
     */
    public static FolderService getFolderService() {
        initializeFolderService();
        return folderService;
    }

    /**
     * Initialize ProjectService if it is not yet initialized and next return
     * it.
     *
     * @return ProjectService object
     */
    public static ProjectService getProjectService() {
        initializeProjectService();
        return projectService;
    }

    /**
     * Initialize RulesetService if it is not yet initialized and next return
     * it.
     *
     * @return RulesetService object
     */
    public static RulesetService getRulesetService() {
        initializeRulesetService();
        return rulesetService;
    }

    /**
     * Initialize SessionService if it is not yet initialized and next return
     * it.
     *
     * @return SessionService object
     */
    public static SessionService getSessionService() {
        initializeSessionService();
        return sessionService;
    }

    /**
     * Initialize SecurityAccessService if it is not yet initialized and next
     * return it.
     *
     * @return SecurityAccessService object
     */
    public static SecurityAccessService getSecurityAccessService() {
        initializeSecurityAccessService();
        return securityAccessService;
    }

    /**
     * Initialize TaskService if it is not yet initialized and next return it.
     *
     * @return TaskService object
     */
    public static TaskService getTaskService() {
        initializeTaskService();
        return taskService;
    }

    /**
     * Initialize TemplateService if it is not yet initialized and next return
     * it.
     *
     * @return TemplateService object
     */
    public static TemplateService getTemplateService() {
        initializeTemplateService();
        return templateService;
    }

    /**
     * Initialize RoleService if it is not yet initialized and next return it.
     *
     * @return RoleService object
     */
    public static RoleService getRoleService() {
        initializeRoleService();
        return roleService;
    }

    /**
     * Initialize UserService if it is not yet initialized and next return it.
     *
     * @return UserService object
     */
    public static UserService getUserService() {
        initializeUserService();
        return userService;
    }

    /**
     * Initialize WorkflowService if it is not yet initialized and next return
     * it.
     *
     * @return WorkflowService object
     */
    public static WorkflowService getWorkflowService() {
        initializeWorkflowService();
        return workflowService;
    }

    /**
     * Initialize WorkflowConditionService if it is not yet initialized and next return
     * it.
     *
     * @return WorkflowConditionService object
     */
    public static WorkflowConditionService getWorkflowConditionService() {
        initializeWorkflowConditionService();
        return workflowConditionService;
    }

    /**
     * Initialize FileService if it is not yet initialized and next return it.
     *
     * @return FileService object
     */
    public static FileService getFileService() {
        initializeFileService();
        return fileService;
    }

    /**
     * Initialize CommandService if it is not yet initialized and next return
     * it.
     *
     * @return CommandService object
     */
    public static CommandService getCommandService() {
        initializeCommandService();
        return commandService;
    }

    /**
     * Initialize SchemaService if it is not yet initialized and next return it.
     *
     * @return SchemaService object
     */
    public static SchemaService getSchemaService() {
        initializeSchemaService();
        return schemaService;
    }

    /**
     * Initialize FileStructureValidationService if it is not yet initialized
     * and next return it.
     *
     * @return FileStructureValidationService object
     */
    public static FileStructureValidationService getFileStructureValidationService() {
        initializeFileStructureValidationService();
        return fileStructureValidationService;
    }

    /**
     * Initialize LongTermPreservationValidationService if it is not yet
     * initialized and next return it.
     *
     * @return LongTermPreservationValidationService object
     */
    public static LongTermPreservationValidationService getLongTermPreservationValidationService() {
        initializeLongTermPreservationValidationService();
        return longTermPreservationValidationService;
    }

    /**
     * Initialize MetadataValidationService if it is not yet initialized and
     * next return it.
     *
     * @return MetadataValidationService object
     */
    public static MetadataValidationService getMetadataValidationService() {
        initializeMetadataValidationService();
        return metadataValidationService;
    }

    /**
     * Initialize RulesetManagementService if it is not yet initialized and next
     * return it.
     *
     * @return RulesetService object
     */
    public static RulesetManagementService getRulesetManagementService() {
        initializeRulesetManagementService();
        return rulesetManagementService;
    }

    /**
     * Initialize WorkflowControllerService if it is not yet initialized and
     * next return it.
     *
     * @return WorkflowControllerService object
     */
    public static WorkflowControllerService getWorkflowControllerService() {
        initializeWorkflowControllerService();
        return workflowControllerService;
    }
}
