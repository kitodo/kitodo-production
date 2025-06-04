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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.services.command.CommandService;
import org.kitodo.production.services.command.KitodoScriptService;
import org.kitodo.production.services.data.AuthorityService;
import org.kitodo.production.services.data.BatchService;
import org.kitodo.production.services.data.ClientService;
import org.kitodo.production.services.data.CommentService;
import org.kitodo.production.services.data.DataEditorSettingService;
import org.kitodo.production.services.data.DocketService;
import org.kitodo.production.services.data.FilterService;
import org.kitodo.production.services.data.FolderService;
import org.kitodo.production.services.data.ImportConfigurationService;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.LdapGroupService;
import org.kitodo.production.services.data.LdapServerService;
import org.kitodo.production.services.data.ListColumnService;
import org.kitodo.production.services.data.MappingFileService;
import org.kitodo.production.services.data.MassImportService;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.ProjectService;
import org.kitodo.production.services.data.PropertyService;
import org.kitodo.production.services.data.RoleService;
import org.kitodo.production.services.data.RulesetService;
import org.kitodo.production.services.data.SearchFieldService;
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
import org.kitodo.production.services.index.IndexingService;
import org.kitodo.production.services.migration.MigrationService;
import org.kitodo.production.services.ocr.OcrdWorkflowService;
import org.kitodo.production.services.schema.SchemaService;
import org.kitodo.production.services.security.SecurityAccessService;
import org.kitodo.production.services.security.SessionService;
import org.kitodo.production.services.validation.FileStructureValidationService;
import org.kitodo.production.services.validation.LongTermPreservationValidationService;
import org.kitodo.production.services.validation.MetadataValidationService;

public class ServiceManager {
    private static final Logger logger = LogManager.getLogger(ServiceManager.class);

    private static AuthorityService authorityService;
    private static BatchService batchService;
    private static ClientService clientService;
    private static CommandService commandService;
    private static CommentService commentService;
    private static DataEditorService dataEditorService;
    private static DataEditorSettingService dataEditorSettingService;
    private static DocketService docketService;
    private static FileService fileService;
    private static FileStructureValidationService fileStructureValidationService;
    private static FilterService filterService;
    private static FolderService folderService;
    private static ImageService imageService;
    private static ImportService importService;
    private static IndexingService indexingService;
    private static KitodoScriptService kitodoScriptService;
    private static LdapGroupService ldapGroupService;
    private static LdapServerService ldapServerService;
    private static ListColumnService listColumnService;
    private static LongTermPreservationValidationService longTermPreservationValidationService;
    private static MetadataValidationService metadataValidationService;
    private static MappingFileService mappingFileService;
    private static MassImportService massImportService;
    private static MetsService metsService;
    private static MigrationService migrationService;
    private static ImportConfigurationService importConfigurationService;
    private static OcrdWorkflowService ocrdWorkflowService;
    private static PropertyService propertyService;
    private static ProcessService processService;
    private static ProjectService projectService;
    private static RoleService roleService;
    private static RulesetService rulesetService;
    private static RulesetManagementService rulesetManagementService;
    private static SchemaService schemaService;
    private static SearchFieldService searchFieldService;
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
            logServiceInitialization();
            authorityService = AuthorityService.getInstance();
        }
    }

    private static void initializeBatchService() {
        if (Objects.isNull(batchService)) {
            logServiceInitialization();
            batchService = BatchService.getInstance();
        }
    }

    private static void initializeClientService() {
        if (Objects.isNull(clientService)) {
            logServiceInitialization();
            clientService = ClientService.getInstance();
        }
    }

    private static void initializeDataEditorService() {
        if (Objects.isNull(dataEditorService)) {
            logServiceInitialization();
            dataEditorService = new DataEditorService();
        }
    }

    private static void initializeDocketService() {
        if (Objects.isNull(docketService)) {
            logServiceInitialization();
            docketService = DocketService.getInstance();
        }
    }

    private static void initializeFilterService() {
        if (Objects.isNull(filterService)) {
            logServiceInitialization();
            filterService = FilterService.getInstance();
        }
    }

    private static void initializeKitodoScriptService() {
        if (Objects.isNull(kitodoScriptService)) {
            logServiceInitialization();
            kitodoScriptService = KitodoScriptService.getInstance();
        }
    }

    private static void initializeImageService() {
        if (Objects.isNull(imageService)) {
            logServiceInitialization();
            imageService = ImageService.getInstance();
        }
    }

    private static void initializeImportService() {
        if (Objects.isNull(importService)) {
            logServiceInitialization();
            importService = ImportService.getInstance();
        }
    }

    private static void initializeLdapGroupService() {
        if (Objects.isNull(ldapGroupService)) {
            logServiceInitialization();
            ldapGroupService = new LdapGroupService();
        }
    }

    private static void initializeLdapServerService() {
        if (Objects.isNull(ldapServerService)) {
            logServiceInitialization();
            ldapServerService = LdapServerService.getInstance();
        }
    }

    private static void initializeMetsService() {
        if (Objects.isNull(metsService)) {
            logServiceInitialization();
            metsService = MetsService.getInstance();
        }
    }

    private static void initializeMassImportService() {
        if (Objects.isNull(massImportService)) {
            logServiceInitialization();
            massImportService = MassImportService.getInstance();
        }
    }

    private static void initializePropertyService() {
        if (Objects.isNull(propertyService)) {
            logServiceInitialization();
            propertyService = PropertyService.getInstance();
        }
    }

    private static void initializeProcessService() {
        if (Objects.isNull(processService)) {
            logServiceInitialization();
            processService = ProcessService.getInstance();
        }
    }

    private static void initializeFolderService() {
        if (Objects.isNull(folderService)) {
            logServiceInitialization();
            folderService = new FolderService();
        }
    }

    private static void initializeOcrdWorkflowService() {
        if (Objects.isNull(ocrdWorkflowService)) {
            logServiceInitialization();
            ocrdWorkflowService = OcrdWorkflowService.getInstance();
        }
    }

    private static void initializeProjectService() {
        if (Objects.isNull(projectService)) {
            logServiceInitialization();
            projectService = ProjectService.getInstance();
        }
    }

    private static void initializeRulesetService() {
        if (Objects.isNull(rulesetService)) {
            logServiceInitialization();
            rulesetService = RulesetService.getInstance();
        }
    }

    private static void initializeSessionService() {
        if (Objects.isNull(sessionService)) {
            logServiceInitialization();
            sessionService = SessionService.getInstance();
        }
    }

    private static void initializeSecurityAccessService() {
        if (Objects.isNull(securityAccessService)) {
            logServiceInitialization();
            securityAccessService = SecurityAccessService.getInstance();
        }
    }

    private static void initializeTaskService() {
        if (Objects.isNull(taskService)) {
            logServiceInitialization();
            taskService = TaskService.getInstance();
        }
    }

    private static void initializeTemplateService() {
        if (Objects.isNull(templateService)) {
            logServiceInitialization();
            templateService = TemplateService.getInstance();
        }
    }

    private static void initializeRoleService() {
        if (Objects.isNull(roleService)) {
            logServiceInitialization();
            roleService = RoleService.getInstance();
        }
    }

    private static void initializeUserService() {
        if (Objects.isNull(userService)) {
            logServiceInitialization();
            userService = UserService.getInstance();
        }
    }

    private static void initializeWorkflowService() {
        if (Objects.isNull(workflowService)) {
            logServiceInitialization();
            workflowService = WorkflowService.getInstance();
        }
    }

    private static void initializeWorkflowConditionService() {
        if (Objects.isNull(workflowConditionService)) {
            logServiceInitialization();
            workflowConditionService = WorkflowConditionService.getInstance();
        }
    }

    private static void initializeFileService() {
        if (Objects.isNull(fileService)) {
            logServiceInitialization();
            fileService = new FileService();
        }
    }

    private static void initializeCommandService() {
        if (Objects.isNull(commandService)) {
            logServiceInitialization();
            commandService = new CommandService();
        }
    }

    private static void initializeSchemaService() {
        if (Objects.isNull(schemaService)) {
            logServiceInitialization();
            schemaService = new SchemaService();
        }
    }

    private static void initializeFileStructureValidationService() {
        if (Objects.isNull(fileStructureValidationService)) {
            logServiceInitialization();
            fileStructureValidationService = new FileStructureValidationService();
        }
    }

    private static void initializeLongTermPreservationValidationService() {
        if (Objects.isNull(longTermPreservationValidationService)) {
            logServiceInitialization();
            longTermPreservationValidationService = new LongTermPreservationValidationService();
        }
    }

    private static void initializeMetadataValidationService() {
        if (Objects.isNull(metadataValidationService)) {
            logServiceInitialization();
            metadataValidationService = new MetadataValidationService();
        }
    }

    private static void initializeMigrationService() {
        if (Objects.isNull(migrationService)) {
            logServiceInitialization();
            migrationService = new MigrationService();
        }
    }

    private static void initializeRulesetManagementService() {
        if (Objects.isNull(rulesetManagementService)) {
            logServiceInitialization();
            rulesetManagementService = RulesetManagementService.getInstance();
        }
    }

    private static void initializeListColumnService() {
        if (Objects.isNull(listColumnService)) {
            logServiceInitialization();
            listColumnService = ListColumnService.getInstance();
        }
    }

    private static void initializeCommentService() {
        if (Objects.isNull(commentService)) {
            logServiceInitialization();
            commentService = CommentService.getInstance();
        }
    }

    private static void initializeIndexingService() {
        if (Objects.isNull(indexingService)) {
            logServiceInitialization();
            indexingService = IndexingService.getInstance();
        }
    }

    private static void initializeDataEditorSettingService() {
        if (Objects.isNull(dataEditorSettingService)) {
            logServiceInitialization();
            dataEditorSettingService = DataEditorSettingService.getInstance();
        }
    }

    private static void initializeOpacConfigurationService() {
        if (Objects.isNull(importConfigurationService)) {
            logServiceInitialization();
            importConfigurationService = ImportConfigurationService.getInstance();
        }
    }

    private static void initializeSearchFieldService() {
        if (Objects.isNull(searchFieldService)) {
            logServiceInitialization();
            searchFieldService = SearchFieldService.getInstance();
        }
    }

    private static void initializeMappingFileService() {
        if (Objects.isNull(mappingFileService)) {
            logServiceInitialization();
            mappingFileService = MappingFileService.getInstance();
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
        logServiceDelivery();
        return authorityService;
    }

    /**
     * Initialize BatchService if it is not yet initialized and next return it.
     *
     * @return BatchService object
     */
    public static BatchService getBatchService() {
        initializeBatchService();
        logServiceDelivery();
        return batchService;
    }

    /**
     * Initialize ClientService if it is not yet initialized and next return it.
     *
     * @return ClientService object
     */
    public static ClientService getClientService() {
        initializeClientService();
        logServiceDelivery();
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
        logServiceDelivery();
        return dataEditorService;
    }

    /**
     * Initialize DocketService if it is not yet initialized and next return it.
     *
     * @return DocketService object
     */
    public static DocketService getDocketService() {
        initializeDocketService();
        logServiceDelivery();
        return docketService;
    }

    /**
     * Initialize FilterService if it is not yet initialized and next return it.
     *
     * @return FilterService object
     */
    public static FilterService getFilterService() {
        initializeFilterService();
        logServiceDelivery();
        return filterService;
    }

    /**
     * Initialize ImportService if it is not yet initialized and return it.
     * 
     * @return ImportService object
     */
    public static ImportService getImportService() {
        initializeImportService();
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
        return imageService;
    }

    /**
     * Initialize MetsService if it is not yet initialized and next return it.
     *
     * @return MetsService object
     */
    public static MetsService getMetsService() {
        initializeMetsService();
        logServiceDelivery();
        return metsService;
    }

    /**
     * Initialize MassImportService if it is not yet initialized and next return it.
     *
     * @return MassImportService object
     */
    public static MassImportService getMassImportService() {
        initializeMassImportService();
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
        return processService;
    }

    /**
     * Initialize FolderService if it is not yet initialized and next return it.
     *
     * @return FolderService object
     */
    public static FolderService getFolderService() {
        initializeFolderService();
        logServiceDelivery();
        return folderService;
    }

    /**
     * Initialize OcrdWorkflowService if it is not yet initialized and next return
     * it.
     *
     * @return OcrdWorkflowService object
     */
    public static OcrdWorkflowService getOcrdWorkflowService() {
        initializeOcrdWorkflowService();
        logServiceDelivery();
        return ocrdWorkflowService;
    }

    /**
     * Initialize ProjectService if it is not yet initialized and next return
     * it.
     *
     * @return ProjectService object
     */
    public static ProjectService getProjectService() {
        initializeProjectService();
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
        return securityAccessService;
    }

    /**
     * Initialize TaskService if it is not yet initialized and next return it.
     *
     * @return TaskService object
     */
    public static TaskService getTaskService() {
        initializeTaskService();
        logServiceDelivery();
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
        logServiceDelivery();
        return templateService;
    }

    /**
     * Initialize RoleService if it is not yet initialized and next return it.
     *
     * @return RoleService object
     */
    public static RoleService getRoleService() {
        initializeRoleService();
        logServiceDelivery();
        return roleService;
    }

    /**
     * Initialize UserService if it is not yet initialized and next return it.
     *
     * @return UserService object
     */
    public static UserService getUserService() {
        initializeUserService();
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
        return workflowConditionService;
    }

    /**
     * Initialize FileService if it is not yet initialized and next return it.
     *
     * @return FileService object
     */
    public static FileService getFileService() {
        initializeFileService();
        logServiceDelivery();
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
        logServiceDelivery();
        return commandService;
    }

    /**
     * Initialize SchemaService if it is not yet initialized and next return it.
     *
     * @return SchemaService object
     */
    public static SchemaService getSchemaService() {
        initializeSchemaService();
        logServiceDelivery();
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
        logServiceDelivery();
        return fileStructureValidationService;
    }

    /**
     * Initialize KitodoScriptService if it is not yet initialized and next
     * return it.
     *
     * @return KitodoScriptService object
     */
    public static KitodoScriptService getKitodoScriptService() {
        initializeKitodoScriptService();
        logServiceDelivery();
        return kitodoScriptService;
    }

    /**
     * Initialize LongTermPreservationValidationService if it is not yet
     * initialized and next return it.
     *
     * @return LongTermPreservationValidationService object
     */
    public static LongTermPreservationValidationService getLongTermPreservationValidationService() {
        initializeLongTermPreservationValidationService();
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
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
        logServiceDelivery();
        return rulesetManagementService;
    }

    /**
     * Initialize ListColumnService if it is not yet initialized and return it.
     *
     * @return ColumnService object
     */
    public static ListColumnService getListColumnService() {
        initializeListColumnService();
        logServiceDelivery();
        return listColumnService;
    }

    /**
     * Initialize CommentService if it is not yet initialized and return it.
     *
     * @return CommentService object
     */
    public static CommentService getCommentService() {
        initializeCommentService();
        logServiceDelivery();
        return commentService;
    }

    /**
     * Returns a service instance for indexing.
     *
     * @return an indexing service
     */
    public static IndexingService getIndexingService() {
        initializeIndexingService();
        logServiceDelivery();
        return indexingService;
    }

    /**
     * Get dataEditorSettingService.
     *
     * @return value of dataEditorSettingService
     */
    public static DataEditorSettingService getDataEditorSettingService() {
        initializeDataEditorSettingService();
        logServiceDelivery();
        return dataEditorSettingService;
    }

    /**
     * Get importConfigurationService.
     *
     * @return value of importConfigurationService
     */
    public static ImportConfigurationService getImportConfigurationService() {
        initializeOpacConfigurationService();
        logServiceDelivery();
        return importConfigurationService;
    }

    /**
     * Get searchFieldService.
     *
     * @return value of searchFieldService
     */
    public static SearchFieldService getSearchFieldService() {
        initializeSearchFieldService();
        logServiceDelivery();
        return searchFieldService;
    }

    /**
     * Get mappingFileService.
     *
     * @return value of mappingFileService
     */
    public static MappingFileService getMappingFileService() {
        initializeMappingFileService();
        logServiceDelivery();
        return mappingFileService;
    }

    private static void logServiceInitialization() {
        if (logger.isTraceEnabled()) {
            logger.trace(new RuntimeException().getStackTrace()[1].getMethodName()
                .replace("initialize", "Initializing "));
        }
    }

    private static void logServiceDelivery() {
        if (logger.isTraceEnabled()) {
            StackTraceElement[] callStack = new Exception().getStackTrace();
            String serviceName = callStack[1].getMethodName().substring(3);
            String fullClassName = callStack[2].getClassName();
            int lastDotIndex = fullClassName.lastIndexOf('.');
            String requestingClass = fullClassName.substring(lastDotIndex + 1);
            logger.trace(String.format("Providing %s to %s", serviceName, requestingClass));
        }
    }
}
