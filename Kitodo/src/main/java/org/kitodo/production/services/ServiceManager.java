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
import org.kitodo.production.services.data.*;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.production.services.dataeditor.RulesetManagementService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.image.ImageService;
import org.kitodo.production.services.index.IndexingService;
import org.kitodo.production.services.migration.MigrationService;
import org.kitodo.production.services.schema.SchemaService;
import org.kitodo.production.services.security.SecurityAccessService;
import org.kitodo.production.services.security.SessionService;
import org.kitodo.production.services.validation.FileStructureValidationService;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;
import org.kitodo.production.services.validation.MetadataValidationService;

public class ServiceManager {

    private static AuthorityService authorityService;
    private static BatchService batchService;
    private static ClientService clientService;
    private static CommandService commandService;
    private static CommentService commentService;
    private static DataEditorService dataEditorService;
    private static DocketService docketService;
    private static FileService fileService;
    private static FileStructureValidationService fileStructureValidationService;
    private static FilterService filterService;
    private static FolderService folderService;
    private static ImageService imageService;
    private static ImportService importService;
    private static IndexingService indexingService;
    private static LdapGroupService ldapGroupService;
    private static LdapServerService ldapServerService;
    private static ListColumnService listColumnService;
    private static LongTermPreservationValidationService longTermPreservationValidationService;
    private static MetadataValidationService metadataValidationService;
    private static MassImportService massImportService;
    private static MetsService metsService;
    private static MigrationService migrationService;
    private static PropertyService propertyService;
    private static ProcessService processService;
    private static ProjectService projectService;
    private static RoleService roleService;
    private static RulesetService rulesetService;
    private static RulesetManagementService rulesetManagementService;
    private static SchemaService schemaService;
    private static SecurityAccessService securityAccessService;
    private static SessionService sessionService;
    private static TaskService taskService;
    private static TemplateService templateService;
    private static UserService userService;
    private static WorkflowService workflowService;
    private static WorkflowConditionService workflowConditionService;

    /**
     * Private constructor.
     */
    private ServiceManager() {
    }

    private static void initializeAuthorizationService() {
        if (Objects.isNull(authorityService)) {
            authorityService = AuthorityService.getInstance();
        }
    }

    private static void initializeBatchService() {
        if (Objects.isNull(batchService)) {
            batchService = BatchService.getInstance();
        }
    }

    private static void initializeClientService() {
        if (Objects.isNull(clientService)) {
            clientService = ClientService.getInstance();
        }
    }

    private static void initializeDataEditorService() {
        if (Objects.isNull(dataEditorService)) {
            dataEditorService = new DataEditorService();
        }
    }

    private static void initializeDocketService() {
        if (Objects.isNull(docketService)) {
            docketService = DocketService.getInstance();
        }
    }

    private static void initializeFilterService() {
        if (Objects.isNull(filterService)) {
            filterService = FilterService.getInstance();
        }
    }

    private static void initializeImageService() {
        if (Objects.isNull(imageService)) {
            imageService = ImageService.getInstance();
        }
    }

    private static void initializeImportService() {
        if (Objects.isNull(importService)) {
            importService = ImportService.getInstance();
        }
    }

    private static void initializeLdapGroupService() {
        if (Objects.isNull(ldapGroupService)) {
            ldapGroupService = new LdapGroupService();
        }
    }

    private static void initializeLdapServerService() {
        if (Objects.isNull(ldapServerService)) {
            ldapServerService = LdapServerService.getInstance();
        }
    }

    private static void initializeMetsService() {
        if (Objects.isNull(metsService)) {
            metsService = MetsService.getInstance();
        }
    }

    private static void initializeMassImportService() {
        if (Objects.isNull(massImportService)) {
            massImportService = MassImportService.getInstance();
        }
    }

    private static void initializePropertyService() {
        if (Objects.isNull(propertyService)) {
            propertyService = PropertyService.getInstance();
        }
    }

    private static void initializeProcessService() {
        if (Objects.isNull(processService)) {
            processService = ProcessService.getInstance();
        }
    }

    private static void initializeFolderService() {
        if (Objects.isNull(folderService)) {
            folderService = new FolderService();
        }
    }

    private static void initializeProjectService() {
        if (Objects.isNull(projectService)) {
            projectService = ProjectService.getInstance();
        }
    }

    private static void initializeRulesetService() {
        if (Objects.isNull(rulesetService)) {
            rulesetService = RulesetService.getInstance();
        }
    }

    private static void initializeSessionService() {
        if (Objects.isNull(sessionService)) {
            sessionService = SessionService.getInstance();
        }
    }

    private static void initializeSecurityAccessService() {
        if (Objects.isNull(securityAccessService)) {
            securityAccessService = SecurityAccessService.getInstance();
        }
    }

    private static void initializeTaskService() {
        if (Objects.isNull(taskService)) {
            taskService = TaskService.getInstance();
        }
    }

    private static void initializeTemplateService() {
        if (Objects.isNull(templateService)) {
            templateService = TemplateService.getInstance();
        }
    }

    private static void initializeRoleService() {
        if (Objects.isNull(roleService)) {
            roleService = RoleService.getInstance();
        }
    }

    private static void initializeUserService() {
        if (Objects.isNull(userService)) {
            userService = UserService.getInstance();
        }
    }

    private static void initializeWorkflowService() {
        if (Objects.isNull(workflowService)) {
            workflowService = WorkflowService.getInstance();
        }
    }

    private static void initializeWorkflowConditionService() {
        if (Objects.isNull(workflowConditionService)) {
            workflowConditionService = WorkflowConditionService.getInstance();
        }
    }

    private static void initializeFileService() {
        if (Objects.isNull(fileService)) {
            fileService = new FileService();
        }
    }

    private static void initializeCommandService() {
        if (Objects.isNull(commandService)) {
            commandService = new CommandService();
        }
    }

    private static void initializeSchemaService() {
        if (Objects.isNull(schemaService)) {
            schemaService = new SchemaService();
        }
    }

    private static void initializeFileStructureValidationService() {
        if (Objects.isNull(fileStructureValidationService)) {
            fileStructureValidationService = new FileStructureValidationService();
        }
    }

    private static void initializeLongTermPreservationValidationService() {
        if (Objects.isNull(longTermPreservationValidationService)) {
            longTermPreservationValidationService = new LongTermPreservationValidationService();
        }
    }

    private static void initializeMetadataValidationService() {
        if (Objects.isNull(metadataValidationService)) {
            metadataValidationService = new MetadataValidationService();
        }
    }

    private static void initializeMigrationService() {
        if (Objects.isNull(migrationService)) {
            migrationService = new MigrationService();
        }
    }

    private static void initializeRulesetManagementService() {
        if (Objects.isNull(rulesetManagementService)) {
            rulesetManagementService = RulesetManagementService.getInstance();
        }
    }

    private static void initializeListColumnService() {
        if (Objects.isNull(listColumnService)) {
            listColumnService = ListColumnService.getInstance();
        }
    }

    private static void initializeCommentService() {
        if (Objects.isNull(commentService)) {
            commentService = CommentService.getInstance();
        }
    }

    private static void initializeIndexingService() {
        if (Objects.isNull(indexingService)) {
            indexingService = IndexingService.getInstance();
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
     * Initialize MassImportService if it is not yet initialized and next return it.
     *
     * @return MassImportService object
     */
    public static MassImportService getMassImportService() {
        initializeMassImportService();
        return massImportService;
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
     * Initialize MigrationService if it is not yet initialized and
     * next return it.
     *
     * @return MigrationService object
     */
    public static MigrationService getMigrationService() {
        initializeMigrationService();
        return migrationService;
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
     * Initialize ListColumnService if it is not yet initialized and return it.
     *
     * @return ColumnService object
     */
    public static ListColumnService getListColumnService() {
        initializeListColumnService();
        return listColumnService;
    }

    /**
     * Initialize CommentService if it is not yet initialized and return it.
     *
     * @return CommentService object
     */
    public static CommentService getCommentService() {
        initializeCommentService();
        return commentService;
    }

    /**
     * Initialize IndexingService if it is not yet initialize and return it.
     *
     * @return IndexingService object
     */
    public static IndexingService getIndexingService() {
        initializeIndexingService();
        return indexingService;
    }
}
