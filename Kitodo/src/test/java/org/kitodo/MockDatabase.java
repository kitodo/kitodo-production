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

package org.kitodo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.h2.tools.Server;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.database.enums.PasswordEncryption;
import org.kitodo.data.database.enums.PropertyType;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.data.elasticsearch.KitodoRestClient;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.model.Converter;

/**
 * Insert data to test database.
 */
public class MockDatabase {

    private static Node node;
    private static IndexRestClient indexRestClient;
    private static String testIndexName;
    private static final String GLOBAL_ASSIGNABLE = "_globalAssignable";
    private static final String CLIENT_ASSIGNABLE = "_clientAssignable";
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final String TARGET = "target";
    private static final String CHOICE = "choice";
    private static final String TEST = "test";
    private static final String FIRST_VALUE = "first value";
    private static final Logger logger = LogManager.getLogger(MockDatabase.class);
    private static Server tcpServer;
    private static HashMap<String, Integer> removableObjectIDs;

    public static void startDatabaseServer() throws SQLException {
        tcpServer = Server.createTcpServer().start();
    }

    public static void stopDatabaseServer() {
        if (tcpServer.isRunning(true)) {
            tcpServer.shutdown();
        }
    }

    public static void startNode() throws Exception {
        startNodeWithoutMapping();
        for (String mappingType : KitodoRestClient.MAPPING_TYPES) {
            indexRestClient.createIndex(readMapping(mappingType), mappingType);
        }
        indexRestClient.setRefreshInterval();
    }

    public static void startNodeWithoutMapping() throws Exception {
        String nodeName = Helper.generateRandomString(6);
        final String port = ConfigMain.getParameter("elasticsearch.port", "9205");

        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        indexRestClient = initializeIndexRestClient();

        Settings settings = prepareNodeSettings(port, nodeName);

        removeOldDataDirectories("target/" + nodeName);

        if (node != null) {
            stopNode();
        }
        Supplier<String> nodeNameSupplier = () -> nodeName;
        node = new ExtendedNode(settings, Collections.singleton(Netty4Plugin.class), nodeNameSupplier);
        node.start();
    }

    public static void stopNode() throws Exception {
        indexRestClient.deleteAllIndexes();
        node.close();
        node = null;
    }

    public static void setUpAwaitility() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Durations.ONE_MINUTE);
    }

    public static void insertProcessesFull() throws Exception {
        insertRolesFull();
        insertDockets();
        insertRulesets();
        insertProjects();
        insertFolders();
        insertTemplates();
        insertProcesses();
        insertBatches();
        insertProcessProperties();
        insertWorkpieceProperties();
        insertTemplateProperties();
        insertUserFilters();
        insertTasks();
        insertWorkflows();
        insertRemovableObjects();
    }

    public static void insertProcessesForWorkflowFull() throws Exception {
        insertRolesFull();
        insertDockets();
        insertRulesets();
        insertProjects();
        insertFolders();
        insertTemplates();
        insertProcesses();
        insertBatches();
        insertProcessPropertiesForWorkflow();
        insertWorkpieceProperties();
        insertTemplateProperties();
        insertUserFilters();
        insertTasks();
        insertDataForParallelTasks();
        insertDataForScriptParallelTasks();
    }

    public static void insertRolesFull() throws DAOException {
        insertAuthorities();
        insertClients();
        insertLdapGroups();
        insertRoles();
        insertUsers();
    }

    public static void insertForAuthenticationTesting() throws Exception {
        insertAuthorities();
        insertLdapGroups();
        insertClients();
        insertRoles();
        insertUsers();
        insertProjects();
    }

    /**
     * Prepare database for tests.
     * @throws Exception when preparation fails
     */
    public static void insertForDataEditorTesting() throws Exception {
        insertRolesFull();
        insertDockets();
        insertRulesets();
        insertProjects();
        insertTemplates();
        insertDataEditorSettings();
    }

    private static class ExtendedNode extends Node {
        ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins,
                     Supplier<String> nodeNameSupplier) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, Collections.emptyMap(),
                    Paths.get("target"), nodeNameSupplier), classpathPlugins, false);
        }
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndexBase(testIndexName);
        return restClient;
    }

    private static String readMapping(String mappingType) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classloader.getResourceAsStream("elasticsearch_mappings/" + mappingType + ".json")) {
            if (Objects.nonNull(inputStream)) {
                String mapping = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                try (JsonReader jsonReader = Json.createReader(new StringReader(mapping))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    return jsonObject.toString();
                }
            } else {
                return "";
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(dataDir.toPath());
        }
    }

    private static Settings prepareNodeSettings(String httpPort, String nodeName) {
        return Settings.builder().put("node.name", nodeName)
                .put("path.data", TARGET)
                .put("path.logs", TARGET)
                .put("path.home", TARGET)
                .put("http.type", "netty4")
                .put("http.port", httpPort)
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("transport.type", "netty4")
                .put("action.auto_create_index", "false").build();
    }

    public static void insertAuthorities() throws DAOException {
        List<Authority> authorities = new ArrayList<>();

        // Client
        authorities.add(new Authority("viewAllClients" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("deleteClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("addClient" + GLOBAL_ASSIGNABLE));

        authorities.add(new Authority("viewClient" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editClient" + CLIENT_ASSIGNABLE));

        // System page
        authorities.add(new Authority("viewIndex" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editIndex" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewTaskManager" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewTerms" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewMigration" + GLOBAL_ASSIGNABLE));

        // Role
        authorities.add(new Authority("viewAllRoles" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewRole" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("addRole" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editRole" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("deleteRole" + GLOBAL_ASSIGNABLE));

        authorities.add(new Authority("viewRole" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllRoles" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editRole" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteRole" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addRole" + CLIENT_ASSIGNABLE));

        // User
        authorities.add(new Authority("viewAllUsers" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewUser" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("addUser" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editUser" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("deleteUser" + GLOBAL_ASSIGNABLE));

        authorities.add(new Authority("viewUser" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllUsers" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editUser" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteUser" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addUser" + CLIENT_ASSIGNABLE));

        // LDAP Group
        authorities.add(new Authority("viewAllLdapGroups" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewLdapGroup" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("addLdapGroup" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editLdapGroup" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("deleteLdapGroup" + GLOBAL_ASSIGNABLE));

        // Project
        authorities.add(new Authority("viewProject" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllProjects" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProject" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteProject" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addProject" + CLIENT_ASSIGNABLE));

        // Template
        authorities.add(new Authority("viewTemplate" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllTemplates" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editTemplate" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteTemplate" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addTemplate" + CLIENT_ASSIGNABLE));

        // Workflow
        authorities.add(new Authority("viewWorkflow" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllWorkflows" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editWorkflow" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteWorkflow" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addWorkflow" + CLIENT_ASSIGNABLE));

        // Docket
        authorities.add(new Authority("viewDocket" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllDockets" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editDocket" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteDocket" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addDocket" + CLIENT_ASSIGNABLE));

        // Ruleset
        authorities.add(new Authority("viewRuleset" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllRulesets" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editRuleset" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteRuleset" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addRuleset" + CLIENT_ASSIGNABLE));

        // ImportConfigurations
        authorities.add(new Authority("addImportConfiguration" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editImportConfiguration" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewImportConfiguration" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllImportConfigurations" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteImportConfiguration" + CLIENT_ASSIGNABLE));

        // MappingFiles
        authorities.add(new Authority("addMappingFile" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editMappingFile" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewMappingFile" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllMappingFiles" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteMappingFile" + CLIENT_ASSIGNABLE));

        // Process
        authorities.add(new Authority("viewProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllProcesses" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("exportProcess" + CLIENT_ASSIGNABLE));

        authorities.add(new Authority("editProcessMetaData" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProcessStructureData" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProcessPagination" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProcessImages" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewProcessMetaData" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewProcessStructureData" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewProcessPagination" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewProcessImages" + CLIENT_ASSIGNABLE));

        // Batch
        authorities.add(new Authority("viewBatch" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllBatches" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editBatch" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteBatch" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addBatch" + CLIENT_ASSIGNABLE));

        // Task
        authorities.add(new Authority("viewTask" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllTasks" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editTask" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteTask" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addTask" + CLIENT_ASSIGNABLE));

        // Database statistics
        authorities.add(new Authority("viewDatabaseStatistic" + GLOBAL_ASSIGNABLE));

        for (Authority authority : authorities) {
            ServiceManager.getAuthorityService().saveToDatabase(authority);
        }
    }

    private static void insertListColumns() throws DAOException {
        List<ListColumn> listColumns = new ArrayList<>();

        listColumns.add(new ListColumn("project.title"));
        listColumns.add(new ListColumn("project.metsRightsOwner"));
        listColumns.add(new ListColumn("project.active"));

        listColumns.add(new ListColumn("template.title"));
        listColumns.add(new ListColumn("template.ruleset"));

        listColumns.add(new ListColumn("workflow.title"));
        listColumns.add(new ListColumn("workflow.filename"));
        listColumns.add(new ListColumn("workflow.status"));

        listColumns.add(new ListColumn("docket.title"));
        listColumns.add(new ListColumn("docket.filename"));

        listColumns.add(new ListColumn("ruleset.title"));
        listColumns.add(new ListColumn("ruleset.filename"));
        listColumns.add(new ListColumn("ruleset.sorting"));

        listColumns.add(new ListColumn("task.title"));
        listColumns.add(new ListColumn("task.process"));
        listColumns.add(new ListColumn("task.project"));
        listColumns.add(new ListColumn("task.state"));

        listColumns.add(new ListColumn("process.title"));
        listColumns.add(new ListColumn("process.status"));
        listColumns.add(new ListColumn("process.project"));

        listColumns.add(new ListColumn("user.username"));
        listColumns.add(new ListColumn("user.location"));
        listColumns.add(new ListColumn("user.roles"));
        listColumns.add(new ListColumn("user.clients"));
        listColumns.add(new ListColumn("user.projects"));
        listColumns.add(new ListColumn("user.active"));

        listColumns.add(new ListColumn("role.role"));
        listColumns.add(new ListColumn("role.client"));

        listColumns.add(new ListColumn("client.client"));

        listColumns.add(new ListColumn("ldapgroup.ldapgroup"));
        listColumns.add(new ListColumn("ldapgroup.home_directory"));
        listColumns.add(new ListColumn("ldapgroup.gidNumber"));

        for (ListColumn listColumn : listColumns) {
            ServiceManager.getListColumnService().saveToDatabase(listColumn);
        }
    }

    private static void insertBatches() throws DAOException, DataException {
        Batch firstBatch = new Batch();
        firstBatch.setTitle("First batch");
        firstBatch.getProcesses().add(ServiceManager.getProcessService().getById(1));
        ServiceManager.getBatchService().save(firstBatch);

        Batch secondBatch = new Batch();
        secondBatch.setTitle("Second batch");
        ServiceManager.getBatchService().save(secondBatch);

        Batch thirdBatch = new Batch();
        thirdBatch.setTitle("Third batch");
        thirdBatch.getProcesses().add(ServiceManager.getProcessService().getById(1));
        thirdBatch.getProcesses().add(ServiceManager.getProcessService().getById(2));
        ServiceManager.getBatchService().save(thirdBatch);

        Batch fourthBatch = new Batch();
        ServiceManager.getBatchService().save(fourthBatch);
    }

    private static void insertDataEditorSettings() throws DAOException {
        DataEditorSetting firstSetting = new DataEditorSetting();
        firstSetting.setUserId(1);
        firstSetting.setTaskId(1);
        firstSetting.setStructureWidth(0.2f);
        firstSetting.setMetadataWidth(0.4f);
        firstSetting.setGalleryWidth(0.4f);
        ServiceManager.getDataEditorSettingService().saveToDatabase(firstSetting);

        DataEditorSetting secondSetting = new DataEditorSetting();
        secondSetting.setUserId(1);
        secondSetting.setTaskId(2);
        secondSetting.setStructureWidth(0f);
        secondSetting.setMetadataWidth(0.5f);
        secondSetting.setGalleryWidth(0.5f);
        ServiceManager.getDataEditorSettingService().saveToDatabase(secondSetting);

        DataEditorSetting thirdSetting = new DataEditorSetting();
        thirdSetting.setUserId(1);
        thirdSetting.setTaskId(4);
        thirdSetting.setStructureWidth(1f);
        thirdSetting.setMetadataWidth(0f);
        thirdSetting.setGalleryWidth(0f);
        ServiceManager.getDataEditorSettingService().saveToDatabase(thirdSetting);
    }

    public static void insertDockets() throws DAOException, DataException {
        Client client = ServiceManager.getClientService().getById(1);

        Docket firstDocket = new Docket();
        firstDocket.setTitle("default");
        firstDocket.setFile("docket.xsl");
        firstDocket.setClient(client);
        ServiceManager.getDocketService().save(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second");
        secondDocket.setFile("MetsModsGoobi_to_MetsKitodo.xsl");
        secondDocket.setClient(client);
        ServiceManager.getDocketService().save(secondDocket);

        Docket thirdDocket = new Docket();
        thirdDocket.setTitle("third");
        thirdDocket.setFile("third_docket.xsl");
        thirdDocket.setClient(ServiceManager.getClientService().getById(2));
        ServiceManager.getDocketService().save(thirdDocket);

        Docket fourthDocket = new Docket();
        fourthDocket.setTitle("tester");
        fourthDocket.setFile("docket.xsl");
        fourthDocket.setClient(client);
        ServiceManager.getDocketService().save(fourthDocket);
    }

    private static void insertLdapServers() throws DAOException {
        LdapServer ldapServer = new LdapServer();
        ldapServer.setTitle("FirstLdapServer");
        ldapServer.setManagerLogin("LdapManager");
        ldapServer.setManagerPassword("LdapManagerPasswort");
        ldapServer.setUrl("LdapUrl");
        ldapServer.setPasswordEncryption(PasswordEncryption.SHA);
        ldapServer.setUseSsl(false);

        ServiceManager.getLdapServerService().saveToDatabase(ldapServer);
    }

    public static void insertLdapGroups() throws DAOException {

        insertLdapServers();

        LdapGroup firstLdapGroup = new LdapGroup();
        firstLdapGroup.setTitle("LG");
        firstLdapGroup.setHomeDirectory("..//test_directory/");
        firstLdapGroup.setDescription("Test LDAP group");
        firstLdapGroup.setDisplayName("Name");
        firstLdapGroup.setLdapServer(ServiceManager.getLdapServerService().getById(1));

        ServiceManager.getLdapGroupService().saveToDatabase(firstLdapGroup);
    }

    private static void insertProcesses() throws DAOException, DataException {
        Project projectOne = ServiceManager.getProjectService().getById(1);
        Template template = ServiceManager.getTemplateService().getById(1);

        Process firstProcess = new Process();
        firstProcess.setTitle("First process");
        firstProcess.setWikiField("field");
        LocalDate localDate = LocalDate.of(2017, 1, 20);
        firstProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstProcess.setSortHelperImages(30);
        firstProcess.setInChoiceListShown(true);
        firstProcess.setDocket(ServiceManager.getDocketService().getById(1));
        firstProcess.setProject(projectOne);
        firstProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        firstProcess.setTemplate(template);
        ServiceManager.getProcessService().save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("Second process");
        secondProcess.setWikiField("problem");
        localDate = LocalDate.of(2017, 2, 10);
        secondProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondProcess.setDocket(ServiceManager.getDocketService().getById(1));
        secondProcess.setProject(projectOne);
        secondProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        secondProcess.setTemplate(template);
        ServiceManager.getProcessService().save(secondProcess);

        Project projectTwo = ServiceManager.getProjectService().getById(2);
        Process thirdProcess = new Process();
        thirdProcess.setTitle("DBConnectionTest");
        thirdProcess.setSortHelperStatus("100000000");
        thirdProcess.setProject(projectTwo);
        ServiceManager.getProcessService().save(thirdProcess);
    }

    public static void insertProcessesForHierarchyTests() throws DAOException, DataException {
        Process fourthProcess = new Process();
        fourthProcess.setProject(ServiceManager.getProjectService().getById(1));
        fourthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        fourthProcess.setTitle("HierarchyParent");
        ServiceManager.getProcessService().save(fourthProcess);

        Process fifthProcess = new Process();
        fifthProcess.setTitle("HierarchChildToKeep");
        fourthProcess.getChildren().add(fifthProcess);
        fifthProcess.setParent(fourthProcess);
        fifthProcess.setProject(ServiceManager.getProjectService().getById(1));
        fifthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        ServiceManager.getProcessService().save(fifthProcess);

        Process sixthProcess = new Process();
        sixthProcess.setTitle("HierarchChildToRemove");
        fourthProcess.getChildren().add(sixthProcess);
        sixthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        sixthProcess.setProject(ServiceManager.getProjectService().getById(1));
        sixthProcess.setParent(fourthProcess);
        ServiceManager.getProcessService().save(sixthProcess);

        Process seventhProcess = new Process();
        seventhProcess.setTitle("HierarchChildToAdd");
        ServiceManager.getProcessService().save(seventhProcess);
    }

    public static void removeProcessesForHierarchyTests() throws DataException {
        ServiceManager.getProcessService().remove(5);
        ServiceManager.getProcessService().remove(6);
        ServiceManager.getProcessService().remove(7);
        ServiceManager.getProcessService().remove(4);
    }


    public static void insertProcessForCalendarHierarchyTests() throws DAOException, DataException {
        Ruleset fivthRuleset = new Ruleset();
        fivthRuleset.setTitle("Newspaper");
        fivthRuleset.setFile("newspaper.xml");
        fivthRuleset.setOrderMetadataByRuleset(false);
        fivthRuleset.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getRulesetService().save(fivthRuleset);

        /*
         * The folders up to and including number 9 in the metadata folder are
         * already in use, so here we insert placeholder processes so that the
         * newspaper’s overall process gets the number 10.
         */
        for (int processNumber = 4; processNumber <= 9; processNumber++) {
            Process nthProcess = new Process();
            nthProcess.setTitle("Placeholder process number ".concat(Integer.toString(processNumber)));
            ServiceManager.getProcessService().save(nthProcess);
        }

        Process tenthProcess = new Process();
        tenthProcess.setProject(ServiceManager.getProjectService().getById(1));
        tenthProcess.setTemplate(ServiceManager.getTemplateService().getById(1));
        tenthProcess.setRuleset(ServiceManager.getRulesetService().getById(5));
        tenthProcess.setTitle("NewspaperOverallProcess");
        ServiceManager.getProcessService().save(tenthProcess);
    }

    private static void insertTemplates() throws DAOException, DataException {
        Project project = ServiceManager.getProjectService().getById(1);

        Template firstTemplate = new Template();
        firstTemplate.setTitle("First template");
        LocalDate localDate = LocalDate.of(2016, 10, 20);
        firstTemplate.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstTemplate.setClient(project.getClient());
        firstTemplate.setDocket(ServiceManager.getDocketService().getById(2));
        firstTemplate.getProjects().add(project);
        firstTemplate.setRuleset(ServiceManager.getRulesetService().getById(1));
        ServiceManager.getTemplateService().save(firstTemplate, true);

        Project thirdProject = ServiceManager.getProjectService().getById(3);
        Template secondTemplate = new Template();
        secondTemplate.setTitle("Second template");
        localDate = LocalDate.of(2017, 2, 10);
        secondTemplate.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondTemplate.setDocket(ServiceManager.getDocketService().getById(1));
        secondTemplate.setClient(thirdProject.getClient());
        secondTemplate.getProjects().add(thirdProject);
        thirdProject.getTemplates().add(secondTemplate);
        secondTemplate.setRuleset(ServiceManager.getRulesetService().getById(2));
        ServiceManager.getTemplateService().save(secondTemplate, true);

        thirdProject = ServiceManager.getProjectService().getById(3);
        Template thirdTemplate = new Template();
        thirdTemplate.setTitle("Third template");
        localDate = LocalDate.of(2018, 2, 10);
        thirdTemplate.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirdTemplate.setClient(thirdProject.getClient());
        thirdTemplate.setDocket(ServiceManager.getDocketService().getById(1));
        thirdTemplate.getProjects().add(thirdProject);
        thirdProject.getTemplates().add(thirdTemplate);
        thirdTemplate.setRuleset(ServiceManager.getRulesetService().getById(1));
        ServiceManager.getTemplateService().save(thirdTemplate, true);

        Template fourthTemplate = new Template();
        fourthTemplate.setTitle("Fourth template");
        localDate = LocalDate.of(2016, 10, 20);
        fourthTemplate.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        fourthTemplate.setClient(project.getClient());
        fourthTemplate.setDocket(ServiceManager.getDocketService().getById(2));
        fourthTemplate.getProjects().add(project);
        fourthTemplate.getProjects().add(thirdProject);
        fourthTemplate.setRuleset(ServiceManager.getRulesetService().getById(2));
        ServiceManager.getTemplateService().save(fourthTemplate, true);
    }

    private static void insertProcessProperties() throws DAOException, DataException {
        Process firstProcess = ServiceManager.getProcessService().getById(1);

        Property firstProcessProperty = new Property();
        firstProcessProperty.setTitle("Process Property");
        firstProcessProperty.setValue(FIRST_VALUE);
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setDataType(PropertyType.STRING);
        firstProcessProperty.setChoice(CHOICE);
        LocalDate localDate = LocalDate.of(2017, 1, 14);
        firstProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(firstProcessProperty);

        Property secondProcessProperty = new Property();
        secondProcessProperty.setTitle("Korrektur notwendig");
        secondProcessProperty.setValue("second value");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setDataType(PropertyType.MESSAGE_ERROR);
        secondProcessProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 1, 15);
        secondProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(secondProcessProperty);

        Property thirdProcessProperty = new Property();
        thirdProcessProperty.setTitle("Korrektur notwendig");
        thirdProcessProperty.setValue("fix it");
        thirdProcessProperty.setObligatory(false);
        thirdProcessProperty.setDataType(PropertyType.MESSAGE_ERROR);
        thirdProcessProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 7, 15);
        thirdProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirdProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(thirdProcessProperty);

        Process secondProcess = ServiceManager.getProcessService().getById(2);
        Property fourthProcessProperty = new Property();
        fourthProcessProperty.setTitle("Korrektur notwendig");
        fourthProcessProperty.setValue("improved ids");
        fourthProcessProperty.setObligatory(false);
        fourthProcessProperty.setDataType(PropertyType.MESSAGE_ERROR);
        fourthProcessProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 7, 15);
        fourthProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        fourthProcessProperty.getProcesses().add(secondProcess);
        ServiceManager.getPropertyService().saveToDatabase(fourthProcessProperty);

        firstProcess.getProperties().add(firstProcessProperty);
        firstProcess.getProperties().add(secondProcessProperty);
        firstProcess.getProperties().add(thirdProcessProperty);
        ServiceManager.getProcessService().save(firstProcess);

        secondProcess.getProperties().add(fourthProcessProperty);
        ServiceManager.getProcessService().save(secondProcess);

    }

    private static void insertProcessPropertiesForWorkflow() throws DAOException, DataException {
        Process firstProcess = ServiceManager.getProcessService().getById(1);

        Property firstProcessProperty = new Property();
        firstProcessProperty.setTitle("Process Property");
        firstProcessProperty.setValue(FIRST_VALUE);
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setDataType(PropertyType.STRING);
        firstProcessProperty.setChoice(CHOICE);
        LocalDate localDate = LocalDate.of(2017, 1, 14);
        firstProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(firstProcessProperty);

        Property secondProcessProperty = new Property();
        secondProcessProperty.setTitle("Korrektur notwendig");
        secondProcessProperty.setValue("second value");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setDataType(PropertyType.MESSAGE_ERROR);
        secondProcessProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 1, 15);
        secondProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(secondProcessProperty);

        Property thirdProcessProperty = new Property();
        thirdProcessProperty.setTitle("Korrektur notwendig");
        thirdProcessProperty.setValue("fix it");
        thirdProcessProperty.setObligatory(false);
        thirdProcessProperty.setDataType(PropertyType.MESSAGE_ERROR);
        thirdProcessProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 7, 15);
        thirdProcessProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirdProcessProperty.getProcesses().add(firstProcess);
        ServiceManager.getPropertyService().saveToDatabase(thirdProcessProperty);

        firstProcess.getProperties().add(firstProcessProperty);
        firstProcess.getProperties().add(secondProcessProperty);
        firstProcess.getProperties().add(thirdProcessProperty);
        ServiceManager.getProcessService().save(firstProcess);
    }

    public static void insertClients() throws DAOException {
        insertListColumns();

        Client client = new Client();
        client.setName("First client");
        client = ServiceManager.getClientService().addStandardListColumns(client);
        ServiceManager.getClientService().saveToDatabase(client);

        Client secondClient = new Client();
        secondClient.setName("Second client");
        secondClient = ServiceManager.getClientService().addStandardListColumns(secondClient);
        ServiceManager.getClientService().saveToDatabase(secondClient);

        Client thirdClient = new Client();
        thirdClient.setName("Not used client");
        thirdClient = ServiceManager.getClientService().addStandardListColumns(thirdClient);
        ServiceManager.getClientService().saveToDatabase(thirdClient);
    }

    private static void insertProjects() throws Exception {
        User firstUser = ServiceManager.getUserService().getById(1);
        User secondUser = ServiceManager.getUserService().getById(2);
        User sixthUser = ServiceManager.getUserService().getById(6);

        Client client = ServiceManager.getClientService().getById(1);

        Project firstProject = new Project();
        firstProject.setTitle("First project");
        LocalDate localDate = LocalDate.of(2016, 10, 20);
        firstProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2017, 10, 20);
        firstProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstProject.setNumberOfPages(30);
        firstProject.setNumberOfVolumes(2);
        firstProject.setMetsRightsOwner("Test Owner");
        firstProject.getUsers().add(firstUser);
        firstProject.getUsers().add(secondUser);
        firstProject.getUsers().add(sixthUser);
        firstProject.setClient(client);
        ServiceManager.getProjectService().save(firstProject);

        Project secondProject = new Project();
        secondProject.setTitle("Second project");
        localDate = LocalDate.of(2016, 11, 10);
        secondProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2017, 9, 15);
        secondProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondProject.setNumberOfPages(80);
        secondProject.setNumberOfVolumes(4);
        secondProject.getUsers().add(firstUser);
        secondProject.setClient(client);
        ServiceManager.getProjectService().save(secondProject);

        firstUser.getProjects().add(firstProject);
        firstUser.getProjects().add(secondProject);
        secondUser.getProjects().add(firstProject);
        ServiceManager.getUserService().saveToDatabase(firstUser);
        ServiceManager.getProjectService().saveToIndex(secondProject, true);

        Project thirdProject = new Project();
        thirdProject.setTitle("Inactive project");
        localDate = LocalDate.of(2014, 11, 10);
        thirdProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2016, 9, 15);
        thirdProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirdProject.setNumberOfPages(160);
        thirdProject.setNumberOfVolumes(5);
        thirdProject.setActive(false);
        User thirdUser = ServiceManager.getUserService().getById(3);
        thirdProject.getUsers().add(secondUser);
        thirdProject.getUsers().add(thirdUser);
        Client secondClient = ServiceManager.getClientService().getById(2);
        thirdProject.setClient(secondClient);
        ServiceManager.getProjectService().save(thirdProject);

        secondUser.getProjects().add(thirdProject);
        thirdUser.getProjects().add(thirdProject);
        sixthUser.getProjects().add(firstProject);
        ServiceManager.getUserService().saveToDatabase(secondUser);
        ServiceManager.getUserService().saveToDatabase(thirdUser);
        ServiceManager.getUserService().saveToDatabase(sixthUser);
    }

    private static void insertFolders() throws DAOException, DataException {
        Project project = ServiceManager.getProjectService().getById(1);

        Folder firstFolder = new Folder();
        firstFolder.setFileGroup("MAX");
        firstFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstFolder.setMimeType("image/jpeg");
        firstFolder.setPath("jpgs/max");
        firstFolder.setCopyFolder(true);
        firstFolder.setCreateFolder(true);
        firstFolder.setDerivative(1.0);
        firstFolder.setLinkingMode(LinkingMode.ALL);

        Folder secondFolder = new Folder();
        secondFolder.setFileGroup("DEFAULT");
        secondFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondFolder.setMimeType("image/jpeg");
        secondFolder.setPath("jpgs/default");
        secondFolder.setCopyFolder(true);
        secondFolder.setCreateFolder(true);
        secondFolder.setDerivative(0.8);
        secondFolder.setLinkingMode(LinkingMode.ALL);

        Folder thirdFolder = new Folder();
        thirdFolder.setFileGroup("THUMBS");
        thirdFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdFolder.setMimeType("image/jpeg");
        thirdFolder.setPath("jpgs/thumbs");
        thirdFolder.setCopyFolder(true);
        thirdFolder.setCreateFolder(true);
        thirdFolder.setImageSize(150);
        thirdFolder.setLinkingMode(LinkingMode.ALL);

        Folder fourthFolder = new Folder();
        fourthFolder.setFileGroup("FULLTEXT");
        fourthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthFolder.setMimeType("application/alto+xml");
        fourthFolder.setPath("ocr/alto");
        fourthFolder.setCopyFolder(true);
        fourthFolder.setCreateFolder(true);
        fourthFolder.setLinkingMode(LinkingMode.ALL);

        Folder fifthFolder = new Folder();
        fifthFolder.setFileGroup("DOWNLOAD");
        fifthFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthFolder.setMimeType("application/pdf");
        fifthFolder.setPath("pdf");
        fifthFolder.setCopyFolder(true);
        fifthFolder.setCreateFolder(true);
        fifthFolder.setLinkingMode(LinkingMode.ALL);

        firstFolder.setProject(project);
        project.getFolders().add(firstFolder);

        secondFolder.setProject(project);
        project.getFolders().add(secondFolder);

        thirdFolder.setProject(project);
        project.getFolders().add(thirdFolder);

        fourthFolder.setProject(project);
        project.getFolders().add(fourthFolder);

        fifthFolder.setProject(project);
        project.getFolders().add(fifthFolder);

        ServiceManager.getProjectService().save(project);
    }

    public static void insertRulesets() throws DAOException, DataException {
        Client client = ServiceManager.getClientService().getById(1);

        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_test.xml");
        firstRuleset.setOrderMetadataByRuleset(false);
        firstRuleset.setClient(client);
        ServiceManager.getRulesetService().save(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("SUBHH");
        secondRuleset.setFile("ruleset_subhh.xml");
        secondRuleset.setOrderMetadataByRuleset(false);
        secondRuleset.setClient(client);
        ServiceManager.getRulesetService().save(secondRuleset);

        Client secondClient = ServiceManager.getClientService().getById(2);

        Ruleset thirdRuleset = new Ruleset();
        thirdRuleset.setTitle("SUBBB");
        thirdRuleset.setFile("ruleset_subbb.xml");
        thirdRuleset.setOrderMetadataByRuleset(false);
        thirdRuleset.setClient(secondClient);
        ServiceManager.getRulesetService().save(thirdRuleset);
    }

    private static void insertTasks() throws Exception {
        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        Role role = ServiceManager.getRoleService().getById(1);

        List<Task> templateTasks = new ArrayList<>(getTasks());
        for (Task task : templateTasks) {
            task.setTemplate(firstTemplate);
            task.getRoles().add(role);
            role.getTasks().add(task);
            ServiceManager.getTaskService().save(task);
        }

        Process firstProcess = ServiceManager.getProcessService().getById(1);
        User firstUser = ServiceManager.getUserService().getById(1);
        User secondUser = ServiceManager.getUserService().getById(2);
        User blockedUser = ServiceManager.getUserService().getById(3);

        List<Task> processTasks = new ArrayList<>(getTasks());
        for (int i = 0; i < processTasks.size(); i++) {
            Task task = processTasks.get(i);
            task.setProcess(firstProcess);
            task.getRoles().add(role);
            role.getTasks().add(task);
            if (i == 0) {
                task.setProcessingUser(firstUser);
                firstUser.getProcessingTasks().add(task);
            } else if (i == 1) {
                task.setTitle("Closed");
                task.setProcessingUser(blockedUser);
                blockedUser.getProcessingTasks().add(task);
            } else {
                task.setProcessingUser(secondUser);
                secondUser.getProcessingTasks().add(task);
            }
            ServiceManager.getTaskService().save(task, true);
        }

        ServiceManager.getUserService().saveToDatabase(firstUser);
        ServiceManager.getUserService().saveToDatabase(secondUser);
        ServiceManager.getUserService().saveToDatabase(blockedUser);

        Process secondProcess = ServiceManager.getProcessService().getById(2);

        Task eleventhTask = new Task();
        eleventhTask.setTitle("Additional");
        eleventhTask.setOrdering(1);
        eleventhTask.setEditType(TaskEditType.MANUAL_SINGLE);
        LocalDate localDate = LocalDate.of(2016, 9, 25);
        eleventhTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        eleventhTask.setProcessingUser(firstUser);
        eleventhTask.setProcessingStatus(TaskStatus.DONE);
        eleventhTask.setProcess(secondProcess);
        eleventhTask.setScriptName("scriptName");
        eleventhTask.setScriptPath("../type/automatic/script/path");
        eleventhTask.getRoles().add(role);
        role.getTasks().add(eleventhTask);
        ServiceManager.getTaskService().save(eleventhTask, true);
        firstUser.getProcessingTasks().add(eleventhTask);
        ServiceManager.getTaskService().saveToIndex(eleventhTask, true);

        Task twelfthTask = new Task();
        twelfthTask.setTitle("Processed and Some");
        twelfthTask.setOrdering(2);
        twelfthTask.setEditType(TaskEditType.MANUAL_SINGLE);
        localDate = LocalDate.of(2016, 10, 25);
        twelfthTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        twelfthTask.setProcessingUser(firstUser);
        twelfthTask.setProcessingStatus(TaskStatus.INWORK);
        twelfthTask.setProcess(secondProcess);
        twelfthTask.getRoles().add(role);
        role.getTasks().add(twelfthTask);
        ServiceManager.getTaskService().save(twelfthTask, true);
        firstUser.getProcessingTasks().add(twelfthTask);
        ServiceManager.getUserService().saveToDatabase(firstUser);
        ServiceManager.getTaskService().saveToIndex(twelfthTask, true);

        Task thirteenTask = new Task();
        thirteenTask.setTitle("Next Open");
        thirteenTask.setOrdering(3);
        thirteenTask.setEditType(TaskEditType.MANUAL_SINGLE);
        localDate = LocalDate.of(2016, 10, 25);
        thirteenTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirteenTask.setProcessingStatus(TaskStatus.OPEN);
        thirteenTask.setProcess(secondProcess);
        thirteenTask.getRoles().add(role);
        role.getTasks().add(thirteenTask);
        ServiceManager.getTaskService().save(thirteenTask, true);

        ServiceManager.getRoleService().saveToDatabase(role);
    }

    private static List<Task> getTasks() {
        Task firstTask = new Task();
        firstTask.setTitle("Finished");
        firstTask.setRepeatOnCorrection(false);
        firstTask.setOrdering(1);
        firstTask.setEditType(TaskEditType.ADMIN);
        LocalDate localDate = LocalDate.of(2016, 8, 20);
        firstTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2016, 9, 24);
        firstTask.setProcessingTime(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2016, 9, 24);
        firstTask.setProcessingEnd(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstTask.setProcessingStatus(TaskStatus.DONE);

        Task secondTask = new Task();
        secondTask.setTitle("Blocking");
        secondTask.setOrdering(2);
        secondTask.setEditType(TaskEditType.MANUAL_SINGLE);
        localDate = LocalDate.of(2016, 9, 25);
        secondTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        localDate = LocalDate.of(2016, 11, 25);
        secondTask.setProcessingEnd(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondTask.setProcessingStatus(TaskStatus.DONE);
        secondTask.setScriptName("scriptName");
        secondTask.setScriptPath("../type/automatic/script/path");

        Task thirdTask = new Task();
        thirdTask.setTitle("Progress");
        thirdTask.setOrdering(3);
        thirdTask.setRepeatOnCorrection(true);
        thirdTask.setEditType(TaskEditType.MANUAL_SINGLE);
        thirdTask.setTypeImagesWrite(true);
        localDate = LocalDate.of(2017, 1, 25);
        thirdTask.setProcessingBegin(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        thirdTask.setProcessingStatus(TaskStatus.INWORK);

        Task fourthTask = new Task();
        fourthTask.setTitle("Open");
        fourthTask.setOrdering(4);
        fourthTask.setRepeatOnCorrection(true);
        fourthTask.setEditType(TaskEditType.MANUAL_SINGLE);
        fourthTask.setProcessingStatus(TaskStatus.OPEN);

        Task fifthTask = new Task();
        fifthTask.setTitle("Locked");
        fifthTask.setOrdering(5);
        fifthTask.setRepeatOnCorrection(true);
        fifthTask.setEditType(TaskEditType.MANUAL_SINGLE);
        fifthTask.setTypeImagesWrite(true);
        fifthTask.setProcessingStatus(TaskStatus.LOCKED);

        return Arrays.asList(firstTask, secondTask, thirdTask, fourthTask, fifthTask);
    }

    private static void insertTemplateProperties() throws DAOException, DataException {
        Process template = ServiceManager.getProcessService().getById(1);

        Property firstTemplateProperty = new Property();
        firstTemplateProperty.setTitle("firstTemplate title");
        firstTemplateProperty.setValue(FIRST_VALUE);
        firstTemplateProperty.setObligatory(true);
        firstTemplateProperty.setDataType(PropertyType.STRING);
        firstTemplateProperty.setChoice(CHOICE);
        LocalDate localDate = LocalDate.of(2017, 1, 14);
        firstTemplateProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstTemplateProperty.getTemplates().add(template);
        ServiceManager.getPropertyService().saveToDatabase(firstTemplateProperty);

        Property secondTemplateProperty = new Property();
        secondTemplateProperty.setTitle("template");
        secondTemplateProperty.setValue("second");
        secondTemplateProperty.setObligatory(false);
        secondTemplateProperty.setDataType(PropertyType.STRING);
        secondTemplateProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 1, 15);
        secondTemplateProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondTemplateProperty.getTemplates().add(template);
        ServiceManager.getPropertyService().saveToDatabase(secondTemplateProperty);

        template.getTemplates().add(firstTemplateProperty);
        template.getTemplates().add(secondTemplateProperty);
        ServiceManager.getProcessService().save(template);
    }

    private static void insertUsers() throws DAOException {
        SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
        Client firstClient = ServiceManager.getClientService().getById(1);
        Client secondClient = ServiceManager.getClientService().getById(2);

        Role adminRole = ServiceManager.getRoleService().getById(1);
        Role generalRole = ServiceManager.getRoleService().getById(2);
        Role projectRoleForFirstClient = ServiceManager.getRoleService().getById(3);
        Role projectRoleForSecondClient = ServiceManager.getRoleService().getById(4);
        Role withoutAuthoritiesRole = ServiceManager.getRoleService().getById(5);
        Role metadataRole = ServiceManager.getRoleService().getById(6);
        Role databaseRole = ServiceManager.getRoleService().getById(7);

        User firstUser = new User();
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("kowal");
        firstUser.setPassword(passwordEncoder.encrypt(TEST));
        firstUser.setLdapLogin("kowalLDP");
        firstUser.setLocation("Dresden");
        firstUser.setTableSize(20);
        firstUser.setLanguage("de");
        firstUser.setMetadataLanguage("de");
        firstUser.getRoles().add(adminRole);
        firstUser.getRoles().add(generalRole);
        firstUser.getRoles().add(databaseRole);
        firstUser.getClients().add(firstClient);
        ServiceManager.getUserService().saveToDatabase(firstUser);

        User secondUser = new User();
        secondUser.setName("Adam");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("nowak");
        secondUser.setPassword(passwordEncoder.encrypt(TEST));
        secondUser.setLdapLogin("nowakLDP");
        secondUser.setLocation("Dresden");
        secondUser.setLanguage("de");
        secondUser.setLdapGroup(ServiceManager.getLdapGroupService().getById(1));
        secondUser.getRoles().add(projectRoleForFirstClient);
        secondUser.getRoles().add(projectRoleForSecondClient);
        secondUser.getClients().add(firstClient);
        secondUser.getClients().add(secondClient);
        ServiceManager.getUserService().saveToDatabase(secondUser);

        User thirdUser = new User();
        thirdUser.setName("Anna");
        thirdUser.setSurname("Dora");
        thirdUser.setLogin("dora");
        thirdUser.setLdapLogin("doraLDP");
        thirdUser.setLocation("Leipzig");
        thirdUser.setLanguage("de");
        thirdUser.setActive(false);
        thirdUser.getRoles().add(adminRole);
        ServiceManager.getUserService().saveToDatabase(thirdUser);

        User fourthUser = new User();
        fourthUser.setName("Max");
        fourthUser.setSurname("Mustermann");
        fourthUser.setLogin("mmustermann");
        fourthUser.setPassword(passwordEncoder.encrypt(TEST));
        fourthUser.setLdapLogin("mmustermann");
        fourthUser.setLocation("Dresden");
        fourthUser.setTableSize(20);
        fourthUser.setLanguage("de");
        fourthUser.getRoles().add(withoutAuthoritiesRole);
        ServiceManager.getUserService().saveToDatabase(fourthUser);

        User fifthUser = new User();
        fifthUser.setName("Last");
        fifthUser.setSurname("User");
        fifthUser.setLogin("user");
        fifthUser.setPassword(passwordEncoder.encrypt(TEST));
        fifthUser.setLdapLogin("user");
        fifthUser.setLocation("Dresden");
        fifthUser.setTableSize(20);
        fifthUser.setLanguage("de");
        ServiceManager.getUserService().saveToDatabase(fifthUser);

        User sixthUser = new User();
        sixthUser.setName("Very last");
        sixthUser.setSurname("User");
        sixthUser.setLogin("verylast");
        sixthUser.setPassword(passwordEncoder.encrypt(TEST));
        sixthUser.getClients().add(firstClient);
        sixthUser.getRoles().add(metadataRole);
        sixthUser.setMetadataLanguage("de");
        ServiceManager.getUserService().saveToDatabase(sixthUser);
    }

    private static void insertRoles() throws DAOException {
        List<Authority> allAuthorities = ServiceManager.getAuthorityService().getAll();
        Client client = ServiceManager.getClientService().getById(1);

        Role firstRole = new Role();
        firstRole.setTitle("Admin");
        firstRole.setClient(client);

        // insert administration authorities
        for (int i = 0; i < 34; i++) {
            firstRole.getAuthorities().add(allAuthorities.get(i));
        }

        ServiceManager.getRoleService().saveToDatabase(firstRole);

        Role secondRole = new Role();
        secondRole.setTitle("General");
        secondRole.setClient(client);

        // insert general authorities
        for (int i = 34; i < allAuthorities.size(); i++) {
            secondRole.getAuthorities().add(allAuthorities.get(i));
        }

        ServiceManager.getRoleService().saveToDatabase(secondRole);

        Role thirdRole = new Role();
        thirdRole.setTitle("Random for first");
        thirdRole.setClient(client);

        // insert authorities for view on projects page
        List<Authority> userAuthoritiesForFirst = new ArrayList<>();
        userAuthoritiesForFirst.add(ServiceManager.getAuthorityService().getByTitle("viewProject" + CLIENT_ASSIGNABLE));
        userAuthoritiesForFirst.add(ServiceManager.getAuthorityService().getByTitle("viewAllProjects" + CLIENT_ASSIGNABLE));
        thirdRole.setAuthorities(userAuthoritiesForFirst);

        ServiceManager.getRoleService().saveToDatabase(thirdRole);

        Role fourthRole = new Role();
        fourthRole.setTitle("Random for second");
        fourthRole.setClient(ServiceManager.getClientService().getById(2));

        // insert authorities for view on projects page
        List<Authority> userAuthoritiesForSecond = new ArrayList<>();
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewProject" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewAllProjects" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewTemplate" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewAllTemplates" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewWorkflow" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewAllWorkflows" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewDocket" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(ServiceManager.getAuthorityService().getByTitle("viewAllDockets" + CLIENT_ASSIGNABLE));
        fourthRole.setAuthorities(userAuthoritiesForSecond);

        ServiceManager.getRoleService().saveToDatabase(fourthRole);

        Role fifthUserGroup = new Role();
        fifthUserGroup.setTitle("Without authorities");
        fifthUserGroup.setClient(client);
        ServiceManager.getRoleService().saveToDatabase(fifthUserGroup);

        Role sixthRole = new Role();
        sixthRole.setTitle("With partial metadata editor authorities");
        sixthRole.setClient(client);

        // insert authorities to view metadata and gallery in metadata editor, but not structure data
        List<Authority> userMetadataAuthorities = new ArrayList<>();
        userMetadataAuthorities.add(ServiceManager.getAuthorityService().getByTitle("viewAllProcesses" + CLIENT_ASSIGNABLE));
        userMetadataAuthorities.add(ServiceManager.getAuthorityService().getByTitle("viewProcessImages" + CLIENT_ASSIGNABLE));
        userMetadataAuthorities.add(ServiceManager.getAuthorityService().getByTitle("viewProcessMetaData" + CLIENT_ASSIGNABLE));
        sixthRole.setAuthorities(userMetadataAuthorities);

        ServiceManager.getRoleService().saveToDatabase(sixthRole);

        // insert database authority
        Role databaseStatisticsRole = new Role();
        databaseStatisticsRole.setTitle("Database management role");
        databaseStatisticsRole.setClient(client);

        List<Authority> databaseStatisticAuthorities = new ArrayList<>();
        databaseStatisticAuthorities.add(ServiceManager.getAuthorityService().getByTitle("viewDatabaseStatistic" + GLOBAL_ASSIGNABLE));
        databaseStatisticsRole.setAuthorities(databaseStatisticAuthorities);

        ServiceManager.getRoleService().saveToDatabase(databaseStatisticsRole);
    }

    private static void insertUserFilters() throws DAOException, DataException {
        User user = ServiceManager.getUserService().getById(1);

        Filter firstUserFilter = new Filter();
        firstUserFilter.setValue("\"id:1\"");
        LocalDate localDate = LocalDate.of(2017, 1, 14);
        firstUserFilter.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstUserFilter.setUser(user);
        ServiceManager.getFilterService().save(firstUserFilter);

        Filter secondUserFilter = new Filter();
        secondUserFilter.setValue("\"id:2\"");
        localDate = LocalDate.of(2017, 1, 15);
        secondUserFilter.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondUserFilter.setUser(user);
        ServiceManager.getFilterService().save(secondUserFilter);

        user.getFilters().add(firstUserFilter);
        user.getFilters().add(secondUserFilter);
        ServiceManager.getUserService().saveToDatabase(user);
    }

    private static void insertWorkpieceProperties() throws DAOException, DataException {
        Process workpiece = ServiceManager.getProcessService().getById(1);

        Property firstWorkpieceProperty = new Property();
        firstWorkpieceProperty.setTitle("FirstWorkpiece Property");
        firstWorkpieceProperty.setValue(FIRST_VALUE);
        firstWorkpieceProperty.setObligatory(true);
        firstWorkpieceProperty.setDataType(PropertyType.STRING);
        firstWorkpieceProperty.setChoice(CHOICE);
        LocalDate localDate = LocalDate.of(2017, 1, 13);
        firstWorkpieceProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstWorkpieceProperty.getWorkpieces().add(workpiece);
        ServiceManager.getPropertyService().saveToDatabase(firstWorkpieceProperty);

        Property secondWorkpieceProperty = new Property();
        secondWorkpieceProperty.setTitle("workpiece");
        secondWorkpieceProperty.setValue("second");
        secondWorkpieceProperty.setObligatory(false);
        secondWorkpieceProperty.setDataType(PropertyType.STRING);
        secondWorkpieceProperty.setChoice("chosen");
        localDate = LocalDate.of(2017, 1, 14);
        secondWorkpieceProperty.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        secondWorkpieceProperty.getWorkpieces().add(workpiece);
        ServiceManager.getPropertyService().saveToDatabase(secondWorkpieceProperty);

        workpiece.getWorkpieces().add(firstWorkpieceProperty);
        workpiece.getWorkpieces().add(secondWorkpieceProperty);
        ServiceManager.getProcessService().save(workpiece);
    }

    public static void insertWorkflows() throws DAOException, DataException {
        Workflow firstWorkflow = new Workflow(TEST);
        firstWorkflow.setStatus(WorkflowStatus.ACTIVE);
        firstWorkflow.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getWorkflowService().save(firstWorkflow);
        Template template = ServiceManager.getTemplateService().getById(1);
        template.setWorkflow(firstWorkflow);
        ServiceManager.getTemplateService().save(template);

        Workflow secondWorkflow = new Workflow(TEST);
        secondWorkflow.setStatus(WorkflowStatus.DRAFT);
        secondWorkflow.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getWorkflowService().save(secondWorkflow);

        Workflow thirdWorkflow = new Workflow("gateway");
        thirdWorkflow.setStatus(WorkflowStatus.DRAFT);
        thirdWorkflow.setClient(ServiceManager.getClientService().getById(2));
        ServiceManager.getWorkflowService().save(thirdWorkflow);
    }

    public static void insertMappingFiles() throws DAOException {
        // add MODS to Kitodo mapping file
        MappingFile mappingFileModsToKitodo = new MappingFile();
        mappingFileModsToKitodo.setFile("mods2kitodo.xsl");
        mappingFileModsToKitodo.setTitle("MODS to Kitodo mapping");
        mappingFileModsToKitodo.setInputMetadataFormat(MetadataFormat.MODS.name());
        mappingFileModsToKitodo.setOutputMetadataFormat(MetadataFormat.KITODO.name());
        ServiceManager.getMappingFileService().saveToDatabase(mappingFileModsToKitodo);

        // add PICA to Kitodo mapping file
        MappingFile mappingFilePicaToKitodo = new MappingFile();
        mappingFilePicaToKitodo.setFile("pica2kitodo.xsl");
        mappingFilePicaToKitodo.setTitle("PICA to Kitodo mapping");
        mappingFilePicaToKitodo.setInputMetadataFormat(MetadataFormat.PICA.name());
        mappingFilePicaToKitodo.setOutputMetadataFormat(MetadataFormat.KITODO.name());
        ServiceManager.getMappingFileService().saveToDatabase(mappingFilePicaToKitodo);
    }

    public static void insertImportConfigurations() throws DAOException, DataException {

        // add GBV import configuration, including id and default search fields
        ImportConfiguration gbvConfiguration = new ImportConfiguration();
        gbvConfiguration.setTitle("GBV");
        gbvConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.name());
        gbvConfiguration.setInterfaceType(SearchInterfaceType.SRU.name());
        gbvConfiguration.setSruVersion("1.2");
        gbvConfiguration.setSruRecordSchema("mods");

        SearchField ppnField = new SearchField();
        ppnField.setValue("pica.ppn");
        ppnField.setLabel("PPN");
        ppnField.setDisplayed(true);
        ppnField.setImportConfiguration(gbvConfiguration);

        gbvConfiguration.setSearchFields(Collections.singletonList(ppnField));
        gbvConfiguration.setIdSearchField(gbvConfiguration.getSearchFields().get(0));
        gbvConfiguration.setDefaultSearchField(gbvConfiguration.getSearchFields().get(0));
        ServiceManager.getImportConfigurationService().saveToDatabase(gbvConfiguration);

        // add Kalliope import configuration, including id search field
        ImportConfiguration kalliopeConfiguration = new ImportConfiguration();
        kalliopeConfiguration.setTitle("Kalliope");
        kalliopeConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.name());
        kalliopeConfiguration.setInterfaceType(SearchInterfaceType.SRU.name());
        kalliopeConfiguration.setSruVersion("1.2");
        kalliopeConfiguration.setSruRecordSchema("mods");
        kalliopeConfiguration.setHost("localhost");
        kalliopeConfiguration.setScheme("http");
        kalliopeConfiguration.setPath("/sru");
        kalliopeConfiguration.setPort(8888);
        kalliopeConfiguration.setPrestructuredImport(false);
        kalliopeConfiguration.setReturnFormat(FileFormat.XML.name());
        kalliopeConfiguration.setMetadataFormat(MetadataFormat.MODS.name());
        kalliopeConfiguration.setIdentifierMetadata("CatalogIDDigital");
        kalliopeConfiguration.setMappingFiles(Collections.singletonList(ServiceManager.getMappingFileService()
                .getById(1)));

        SearchField idSearchFieldKalliope = new SearchField();
        idSearchFieldKalliope.setValue("ead.id");
        idSearchFieldKalliope.setLabel("Identifier");
        idSearchFieldKalliope.setImportConfiguration(kalliopeConfiguration);

        kalliopeConfiguration.setSearchFields(Collections.singletonList(idSearchFieldKalliope));
        ServiceManager.getImportConfigurationService().saveToDatabase(kalliopeConfiguration);

        kalliopeConfiguration.setIdSearchField(kalliopeConfiguration.getSearchFields().get(0));
        ServiceManager.getImportConfigurationService().saveToDatabase(kalliopeConfiguration);

        // add K10Plus import configuration, including id search field
        ImportConfiguration k10plusConfiguration = new ImportConfiguration();
        k10plusConfiguration.setTitle("K10Plus");
        k10plusConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.name());
        k10plusConfiguration.setInterfaceType(SearchInterfaceType.SRU.name());
        k10plusConfiguration.setSruVersion("1.1");
        k10plusConfiguration.setSruRecordSchema("picaxml");
        k10plusConfiguration.setHost("localhost");
        k10plusConfiguration.setScheme("http");
        k10plusConfiguration.setPath("/sru");
        k10plusConfiguration.setPort(8888);
        k10plusConfiguration.setPrestructuredImport(false);
        k10plusConfiguration.setReturnFormat(FileFormat.XML.name());
        k10plusConfiguration.setMetadataFormat(MetadataFormat.PICA.name());
        k10plusConfiguration.setMappingFiles(Collections.singletonList(ServiceManager.getMappingFileService()
                .getById(2)));

        SearchField idSearchFieldK10Plus = new SearchField();
        idSearchFieldK10Plus.setValue("pica.ppn");
        idSearchFieldK10Plus.setLabel("PPN");
        idSearchFieldK10Plus.setImportConfiguration(k10plusConfiguration);

        k10plusConfiguration.setSearchFields(Collections.singletonList(idSearchFieldK10Plus));
        ServiceManager.getImportConfigurationService().saveToDatabase(k10plusConfiguration);

        k10plusConfiguration.setIdSearchField(k10plusConfiguration.getSearchFields().get(0));
        ServiceManager.getImportConfigurationService().saveToDatabase(k10plusConfiguration);

        for (Project project : ServiceManager.getProjectService().getAll()) {
            project.setDefaultImportConfiguration(k10plusConfiguration);
            ServiceManager.getProjectService().save(project);
        }
    }

    private static void insertDataForParallelTasks() throws DAOException, DataException, IOException, WorkflowException {
        Client client = ServiceManager.getClientService().getById(1);

        Workflow workflow = new Workflow("gateway-test1");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setClient(client);
        ServiceManager.getWorkflowService().save(workflow);

        Project project = ServiceManager.getProjectService().getById(1);

        Converter converter = new Converter("gateway-test1");

        Template template = new Template();
        template.setTitle("Parallel Template");
        converter.convertWorkflowToTemplate(template);
        template.setClient(client);
        template.setDocket(ServiceManager.getDocketService().getById(1));
        template.setRuleset(ServiceManager.getRulesetService().getById(1));
        template.setWorkflow(workflow);
        template.getProjects().add(project);
        ServiceManager.getTemplateService().save(template);

        Process firstProcess = new Process();
        firstProcess.setTitle("Parallel");
        firstProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, firstProcess);
        firstProcess.getTasks().get(0).setProcessingStatus(TaskStatus.INWORK);
        firstProcess.getTasks().get(0).setProcessingUser(ServiceManager.getUserService().getById(1));
        firstProcess.getTasks().get(1).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(2).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        firstProcess.setProject(project);
        firstProcess.setDocket(template.getDocket());
        firstProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("ParallelInWork");
        secondProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, secondProcess);
        secondProcess.getTasks().get(0).setProcessingStatus(TaskStatus.DONE);
        secondProcess.getTasks().get(1).setProcessingStatus(TaskStatus.INWORK);
        secondProcess.getTasks().get(2).setProcessingStatus(TaskStatus.LOCKED);
        secondProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        secondProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        secondProcess.setProject(project);
        secondProcess.setDocket(template.getDocket());
        secondProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setTitle("ParallelInWorkWithBlocking");
        thirdProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, thirdProcess);
        thirdProcess.getTasks().get(0).setProcessingStatus(TaskStatus.DONE);
        thirdProcess.getTasks().get(1).setProcessingStatus(TaskStatus.INWORK);
        thirdProcess.getTasks().get(2).setProcessingStatus(TaskStatus.INWORK);
        thirdProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        thirdProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        thirdProcess.setProject(project);
        thirdProcess.setDocket(template.getDocket());
        thirdProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(thirdProcess);

        Process fourthProcess = new Process();
        fourthProcess.setTitle("ParallelInWorkWithNonBlocking");
        fourthProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, fourthProcess);
        fourthProcess.getTasks().get(0).setProcessingStatus(TaskStatus.DONE);
        fourthProcess.getTasks().get(1).setProcessingStatus(TaskStatus.INWORK);
        fourthProcess.getTasks().get(2).setProcessingStatus(TaskStatus.INWORK);
        fourthProcess.getTasks().get(2).setConcurrent(true);
        fourthProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        fourthProcess.getTasks().get(3).setConcurrent(true);
        fourthProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        fourthProcess.setProject(project);
        fourthProcess.setDocket(template.getDocket());
        fourthProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(fourthProcess);

        Process fifthProcess = new Process();
        fifthProcess.setTitle("ParallelAlmostFinished");
        secondProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, fifthProcess);
        fifthProcess.getTasks().get(0).setProcessingStatus(TaskStatus.DONE);
        fifthProcess.getTasks().get(1).setProcessingStatus(TaskStatus.DONE);
        fifthProcess.getTasks().get(2).setProcessingStatus(TaskStatus.DONE);
        fifthProcess.getTasks().get(3).setProcessingStatus(TaskStatus.INWORK);
        fifthProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        fifthProcess.setProject(project);
        fifthProcess.setDocket(template.getDocket());
        fifthProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(fifthProcess);

        Process sixthProcess = new Process();
        sixthProcess.setTitle("ParallelInWorkWithNonBlockingToAssign");
        sixthProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, sixthProcess);
        sixthProcess.getTasks().get(0).setProcessingStatus(TaskStatus.INWORK);
        sixthProcess.getTasks().get(1).setProcessingStatus(TaskStatus.LOCKED);
        sixthProcess.getTasks().get(1).setConcurrent(true);
        sixthProcess.getTasks().get(2).setProcessingStatus(TaskStatus.LOCKED);
        sixthProcess.getTasks().get(2).setConcurrent(true);
        sixthProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        sixthProcess.getTasks().get(3).setConcurrent(true);
        sixthProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        sixthProcess.setProject(project);
        sixthProcess.setDocket(template.getDocket());
        sixthProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(sixthProcess);
    }

    private static void insertDataForScriptParallelTasks() throws DAOException, DataException, IOException, WorkflowException {
        Workflow workflow = new Workflow("gateway-test5");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setClient(ServiceManager.getClientService().getById(1));
        ServiceManager.getWorkflowService().save(workflow);

        Project project = ServiceManager.getProjectService().getById(1);

        Converter converter = new Converter("gateway-test5");

        Template template = new Template();
        template.setTitle("Parallel Script Template");
        converter.convertWorkflowToTemplate(template);
        template.setDocket(ServiceManager.getDocketService().getById(1));
        template.setRuleset(ServiceManager.getRulesetService().getById(1));
        template.setWorkflow(workflow);
        template.getProjects().add(project);
        ServiceManager.getTemplateService().save(template);

        Process firstProcess = new Process();
        firstProcess.setTitle("Script");
        firstProcess.setTemplate(template);

        ProcessGenerator.copyTasks(template, firstProcess);
        firstProcess.getTasks().get(0).setProcessingStatus(TaskStatus.INWORK);
        firstProcess.getTasks().get(0).setProcessingUser(ServiceManager.getUserService().getById(1));
        firstProcess.getTasks().get(1).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(2).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(3).setProcessingStatus(TaskStatus.LOCKED);
        firstProcess.getTasks().get(4).setProcessingStatus(TaskStatus.LOCKED);

        firstProcess.setProject(project);
        firstProcess.setDocket(template.getDocket());
        firstProcess.setRuleset(template.getRuleset());
        ServiceManager.getProcessService().save(firstProcess);
    }

    /**
     * Clean database after class. Truncate all tables, reset id sequences and clear
     * session.
     */
    public static void cleanDatabase() {
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        Set<String> tables = new HashSet<>();
        String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_SCHEMA='PUBLIC'";
        List<?> tableResult = session.createNativeQuery(query).getResultList();
        for (Object table : tableResult) {
            tables.add((String) table);
        }

        for (String table : tables) {
            session.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
        }

        Set<String> sequences = new HashSet<>();
        query = "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'";
        List<?> sequencesResult = session.createNativeQuery(query).getResultList();
        for (Object test : sequencesResult) {
            sequences.add((String) test);
        }

        for (String sequence : sequences) {
            session.createNativeQuery("ALTER SEQUENCE " + sequence + " RESTART WITH 1").executeUpdate();
        }

        session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        session.clear();
        transaction.commit();
    }

    private static void insertRemovableObjects() throws DataException, DAOException {
        removableObjectIDs = new HashMap<>();

        Client client = new Client();
        client.setName("Removable client");
        ServiceManager.getClientService().saveToDatabase(client);
        removableObjectIDs.put(ObjectType.CLIENT.name(), client.getId());

        Client assignableClient = ServiceManager.getClientService().getById(1);

        Docket docket = new Docket();
        docket.setTitle("Removable docket");
        docket.setClient(assignableClient);
        ServiceManager.getDocketService().save(docket);
        removableObjectIDs.put(ObjectType.DOCKET.name(), docket.getId());

        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("Removable ruleset");
        ruleset.setClient(assignableClient);
        ServiceManager.getRulesetService().save(ruleset);
        removableObjectIDs.put(ObjectType.RULESET.name(), ruleset.getId());

        User user = new User();
        user.setName("Removable user");
        user.getClients().add(assignableClient);
        ServiceManager.getUserService().saveToDatabase(user);
        removableObjectIDs.put(ObjectType.USER.name(), user.getId());

        Role role = new Role();
        role.setTitle("Removable role");
        role.setClient(assignableClient);
        ServiceManager.getRoleService().saveToDatabase(role);
        removableObjectIDs.put(ObjectType.ROLE.name(), role.getId());

    }

    /**
     * Return HashMap containing ObjectTypes as keys and Integers denoting IDs of
     * removable database objects as values.
     *
     * @return HashMap containing IDs of removable instances of ObjectsTypes
     */
    public static HashMap<String, Integer> getRemovableObjectIDs() {
        if (removableObjectIDs.isEmpty()) {
            try {
                insertRemovableObjects();
            } catch (DataException | DAOException e) {
                logger.error("Unable to save removable objects to test database!");
            }
        }
        return removableObjectIDs;
    }

    /**
     * Return Kalliope ImportConfiguration.
     *
     * @return Kalliope ImportConfiguration
     * @throws DAOException thrown if Kalliope ImportConfiguration cannot be loaded from database
     */
    public static ImportConfiguration getKalliopeImportConfiguration() throws DAOException {
        return ServiceManager.getImportConfigurationService().getById(2);
    }

    /**
     * Return K10Plus ImportConfiguration
     *
     * @return K10Plus ImportConfiguration
     * @throws DAOException thrown if K10Plus ImportConfiguration cannot be loaded from database
     */
    public static ImportConfiguration getK10PlusImportConfiguration() throws DAOException {
        return ServiceManager.getImportConfigurationService().getById(3);
    }
}
