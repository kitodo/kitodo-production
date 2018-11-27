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

import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;
import static org.kitodo.data.database.beans.Batch.Type.NEWSPAPER;
import static org.kitodo.data.database.beans.Batch.Type.SERIAL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.h2.tools.Server;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.LocalDate;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.LinkingMode;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.PasswordEncryption;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.security.password.SecurityPasswordEncoder;
import org.kitodo.services.ServiceManager;

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
    private static final Logger logger = LogManager.getLogger(MockDatabase.class);
    private static final ServiceManager serviceManager = new ServiceManager();
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

    @SuppressWarnings("unchecked")
    public static void startNode() throws Exception {
        startNodeWithoutMapping();
        indexRestClient.createIndex(readMapping());
    }

    @SuppressWarnings("unchecked")
    public static void startNodeWithoutMapping() throws Exception {
        String nodeName = Helper.generateRandomString(6);
        final String port = ConfigMain.getParameter("elasticsearch.port", "9205");

        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        indexRestClient = initializeIndexRestClient();

        Map settingsMap = prepareNodeSettings(port, nodeName);
        Settings settings = Settings.builder().put(settingsMap).build();

        removeOldDataDirectories("target/" + nodeName);

        if (node != null) {
            stopNode();
        }
        node = new ExtendedNode(settings, Collections.singleton(Netty4Plugin.class));
        node.start();
    }

    public static void stopNode() throws Exception {
        indexRestClient.deleteIndex();
        node.close();
        node = null;
    }

    public static void setUpAwaitility() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Duration.TWO_SECONDS);
    }

    public static void insertProcessesFull() throws DAOException, DataException {
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

    public static void insertProcessesForWorkflowFull() throws DAOException, DataException {
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
    }

    public static void insertRolesFull() throws DAOException, DataException {
        insertAuthorities();
        insertClients();
        insertLdapGroups();
        insertRoles();
        insertUsers();
    }

    public static void insertForAuthenticationTesting() throws DAOException, DataException {
        insertAuthorities();
        insertLdapGroups();
        insertClients();
        insertRoles();
        insertUsers();
        insertProjects();
    }

    private static class ExtendedNode extends Node {
        ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        return restClient;
    }

    private static String readMapping() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classloader.getResourceAsStream("mapping.json")) {
            String mapping = IOUtils.toString(inputStream, "UTF-8");
            try (JsonReader jsonReader = Json.createReader(new StringReader(mapping))) {
                JsonObject jsonObject = jsonReader.readObject();
                return jsonObject.toString();
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

    @SuppressWarnings("unchecked")
    private static Map prepareNodeSettings(String httpPort, String nodeName) {
        Map settingsMap = new HashMap();
        settingsMap.put("node.name", nodeName);
        // create all data directories under Maven build directory
        settingsMap.put("path.conf", "target");
        settingsMap.put("path.data", "target");
        settingsMap.put("path.logs", "target");
        settingsMap.put("path.home", "target");
        // set ports used by Elastic Search to something different than default
        settingsMap.put("http.type", "netty4");
        settingsMap.put("http.port", httpPort);
        settingsMap.put("transport.tcp.port", HTTP_TRANSPORT_PORT);
        settingsMap.put("transport.type", "netty4");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");
        return settingsMap;
    }

    private static void insertAuthorities() throws DataException {
        List<Authority> authorities = new ArrayList<>();

        // Client
        authorities.add(new Authority("viewAllClients" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("viewClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("deleteClient" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("addClient" + GLOBAL_ASSIGNABLE));

        authorities.add(new Authority("viewClient" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editClient" + CLIENT_ASSIGNABLE));

        authorities.add(new Authority("viewIndex" + GLOBAL_ASSIGNABLE));
        authorities.add(new Authority("editIndex" + GLOBAL_ASSIGNABLE));

        //Role
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

        // Process
        authorities.add(new Authority("viewProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("viewAllProcesses" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("editProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("deleteProcess" + CLIENT_ASSIGNABLE));
        authorities.add(new Authority("addProcess" + CLIENT_ASSIGNABLE));

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

        for (Authority authority : authorities) {
            serviceManager.getAuthorityService().save(authority);
        }
    }

    private static void insertBatches() throws DAOException, DataException {
        Batch firstBatch = new Batch();
        firstBatch.setTitle("First batch");
        firstBatch.setType(LOGISTIC);
        firstBatch.getProcesses().add(serviceManager.getProcessService().getById(1));
        serviceManager.getBatchService().save(firstBatch);

        Batch secondBatch = new Batch();
        secondBatch.setTitle("Second batch");
        secondBatch.setType(LOGISTIC);
        serviceManager.getBatchService().save(secondBatch);

        Batch thirdBatch = new Batch();
        thirdBatch.setTitle("Third batch");
        thirdBatch.setType(NEWSPAPER);
        thirdBatch.getProcesses().add(serviceManager.getProcessService().getById(1));
        thirdBatch.getProcesses().add(serviceManager.getProcessService().getById(2));
        serviceManager.getBatchService().save(thirdBatch);

        Batch fourthBatch = new Batch();
        fourthBatch.setType(SERIAL);
        serviceManager.getBatchService().save(fourthBatch);
    }

    public static void insertDockets() throws DAOException, DataException {
        Client client = serviceManager.getClientService().getById(1);

        Docket firstDocket = new Docket();
        firstDocket.setTitle("default");
        firstDocket.setFile("docket.xsl");
        firstDocket.setClient(client);
        serviceManager.getDocketService().save(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second");
        secondDocket.setFile("MetsModsGoobi_to_MetsKitodo.xsl");
        secondDocket.setClient(client);
        serviceManager.getDocketService().save(secondDocket);

        Docket thirdDocket = new Docket();
        thirdDocket.setTitle("third");
        thirdDocket.setFile("third_docket.xsl");
        thirdDocket.setClient(serviceManager.getClientService().getById(2));
        serviceManager.getDocketService().save(thirdDocket);

        Docket fourthDocket = new Docket();
        fourthDocket.setTitle("tester");
        fourthDocket.setFile("docket.xsl");
        fourthDocket.setClient(client);
        serviceManager.getDocketService().save(fourthDocket);
    }

    private static void insertLdapServers() throws DAOException {
        LdapServer ldapServer = new LdapServer();
        ldapServer.setTitle("FirstLdapServer");
        ldapServer.setManagerLogin("LdapManager");
        ldapServer.setManagerPassword("LdapManagerPasswort");
        ldapServer.setUrl("LdapUrl");
        ldapServer.setPasswordEncryptionEnum(PasswordEncryption.SHA);
        ldapServer.setUseSsl(false);

        serviceManager.getLdapServerService().saveToDatabase(ldapServer);
    }

    public static void insertLdapGroups() throws DAOException {

        insertLdapServers();

        LdapGroup firstLdapGroup = new LdapGroup();
        firstLdapGroup.setTitle("LG");
        firstLdapGroup.setHomeDirectory("..//test_directory/");
        firstLdapGroup.setDescription("Test LDAP group");
        firstLdapGroup.setDisplayName("Name");
        firstLdapGroup.setLdapServer(serviceManager.getLdapServerService().getById(1));

        serviceManager.getLdapGroupService().saveToDatabase(firstLdapGroup);
    }

    private static void insertProcesses() throws DAOException, DataException {
        Project project = serviceManager.getProjectService().getById(1);
        Template template = serviceManager.getTemplateService().getById(1);

        Process firstProcess = new Process();
        firstProcess.setTitle("First process");
        firstProcess.setWikiField("field");
        LocalDate localDate = new LocalDate(2017, 1, 20);
        firstProcess.setCreationDate(localDate.toDate());
        firstProcess.setSortHelperImages(30);
        firstProcess.setInChoiceListShown(true);
        firstProcess.setDocket(serviceManager.getDocketService().getById(1));
        firstProcess.setProject(project);
        firstProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        firstProcess.setTemplate(template);
        firstProcess.setSortHelperStatus("100000000");
        serviceManager.getProcessService().save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("Second process");
        secondProcess.setWikiField("problem");
        localDate = new LocalDate(2017, 2, 10);
        secondProcess.setCreationDate(localDate.toDate());
        secondProcess.setDocket(serviceManager.getDocketService().getById(1));
        secondProcess.setProject(project);
        secondProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        secondProcess.setTemplate(template);
        serviceManager.getProcessService().save(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setTitle("DBConnectionTest");
        serviceManager.getProcessService().save(thirdProcess);
    }

    private static void insertTemplates() throws DAOException, DataException {
        Project project = serviceManager.getProjectService().getById(1);

        Template firstTemplate = new Template();
        firstTemplate.setTitle("First template");
        firstTemplate.setWikiField("wiki");
        LocalDate localDate = new LocalDate(2016, 10, 20);
        firstTemplate.setCreationDate(localDate.toDate());
        firstTemplate.setInChoiceListShown(true);
        firstTemplate.setDocket(serviceManager.getDocketService().getById(2));
        firstTemplate.getProjects().add(project);
        firstTemplate.setRuleset(serviceManager.getRulesetService().getById(2));
        serviceManager.getTemplateService().save(firstTemplate);

        Project thirdProject = serviceManager.getProjectService().getById(3);
        Template secondTemplate = new Template();
        secondTemplate.setTitle("Second template");
        secondTemplate.setWikiField("works");
        localDate = new LocalDate(2017, 2, 10);
        secondTemplate.setCreationDate(localDate.toDate());
        secondTemplate.setDocket(serviceManager.getDocketService().getById(1));
        secondTemplate.getProjects().add(thirdProject);
        thirdProject.getTemplates().add(secondTemplate);
        secondTemplate.setRuleset(serviceManager.getRulesetService().getById(1));
        secondTemplate.setInChoiceListShown(true);
        serviceManager.getTemplateService().save(secondTemplate);

        thirdProject = serviceManager.getProjectService().getById(3);
        Template thirdTemplate = new Template();
        thirdTemplate.setTitle("Third template");
        thirdTemplate.setWikiField("problem");
        localDate = new LocalDate(2018, 2, 10);
        thirdTemplate.setCreationDate(localDate.toDate());
        thirdTemplate.setDocket(serviceManager.getDocketService().getById(1));
        thirdTemplate.getProjects().add(thirdProject);
        thirdProject.getTemplates().add(thirdTemplate);
        thirdTemplate.setRuleset(serviceManager.getRulesetService().getById(1));
        thirdTemplate.setInChoiceListShown(true);
        serviceManager.getTemplateService().save(thirdTemplate);
    }

    private static void insertProcessProperties() throws DAOException, DataException {
        Process firstProcess = serviceManager.getProcessService().getById(1);

        Property firstProcessProperty = new Property();
        firstProcessProperty.setTitle("Process Property");
        firstProcessProperty.setValue("first value");
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setType(PropertyType.STRING);
        firstProcessProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstProcessProperty.setCreationDate(localDate.toDate());
        firstProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(firstProcessProperty);

        Property secondProcessProperty = new Property();
        secondProcessProperty.setTitle("Korrektur notwendig");
        secondProcessProperty.setValue("second value");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setType(PropertyType.MESSAGE_ERROR);
        secondProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondProcessProperty.setCreationDate(localDate.toDate());
        secondProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(secondProcessProperty);

        Property thirdProcessProperty = new Property();
        thirdProcessProperty.setTitle("Korrektur notwendig");
        thirdProcessProperty.setValue("fix it");
        thirdProcessProperty.setObligatory(false);
        thirdProcessProperty.setType(PropertyType.MESSAGE_ERROR);
        thirdProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 7, 15);
        thirdProcessProperty.setCreationDate(localDate.toDate());
        thirdProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(thirdProcessProperty);

        Process secondProcess = serviceManager.getProcessService().getById(2);
        Property fourthProcessProperty = new Property();
        fourthProcessProperty.setTitle("Korrektur notwendig");
        fourthProcessProperty.setValue("improved ids");
        fourthProcessProperty.setObligatory(false);
        fourthProcessProperty.setType(PropertyType.MESSAGE_ERROR);
        fourthProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 7, 15);
        fourthProcessProperty.setCreationDate(localDate.toDate());
        fourthProcessProperty.getProcesses().add(secondProcess);
        serviceManager.getPropertyService().save(fourthProcessProperty);

        firstProcess.getProperties().add(firstProcessProperty);
        firstProcess.getProperties().add(secondProcessProperty);
        firstProcess.getProperties().add(thirdProcessProperty);
        serviceManager.getProcessService().save(firstProcess);

        secondProcess.getProperties().add(fourthProcessProperty);
        serviceManager.getProcessService().save(secondProcess);

    }

    private static void insertProcessPropertiesForWorkflow() throws DAOException, DataException {
        Process firstProcess = serviceManager.getProcessService().getById(1);

        Property firstProcessProperty = new Property();
        firstProcessProperty.setTitle("Process Property");
        firstProcessProperty.setValue("first value");
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setType(PropertyType.STRING);
        firstProcessProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstProcessProperty.setCreationDate(localDate.toDate());
        firstProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(firstProcessProperty);

        Property secondProcessProperty = new Property();
        secondProcessProperty.setTitle("Korrektur notwendig");
        secondProcessProperty.setValue("second value");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setType(PropertyType.MESSAGE_ERROR);
        secondProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondProcessProperty.setCreationDate(localDate.toDate());
        secondProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(secondProcessProperty);

        Property thirdProcessProperty = new Property();
        thirdProcessProperty.setTitle("Korrektur notwendig");
        thirdProcessProperty.setValue("fix it");
        thirdProcessProperty.setObligatory(false);
        thirdProcessProperty.setType(PropertyType.MESSAGE_ERROR);
        thirdProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 7, 15);
        thirdProcessProperty.setCreationDate(localDate.toDate());
        thirdProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(thirdProcessProperty);
    }

    public static void insertClients() throws DataException {
        Client client = new Client();
        client.setName("First client");
        serviceManager.getClientService().save(client);

        Client secondClient = new Client();
        secondClient.setName("Second client");
        serviceManager.getClientService().save(secondClient);

        Client thirdClient = new Client();
        thirdClient.setName("Not used client");
        serviceManager.getClientService().save(thirdClient);
    }

    private static void insertProjects() throws DAOException, DataException {
        User firstUser = serviceManager.getUserService().getById(1);
        User secondUser = serviceManager.getUserService().getById(2);

        Project firstProject = new Project();
        firstProject.setTitle("First project");
        firstProject.setUseDmsImport(true);
        LocalDate localDate = new LocalDate(2016, 10, 20);
        firstProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017, 10, 20);
        firstProject.setEndDate(localDate.toDate());
        firstProject.setNumberOfPages(30);
        firstProject.setNumberOfVolumes(2);
        firstProject.setFileFormatInternal("Mets");
        firstProject.setFileFormatDmsExport("Mets");
        firstProject.setMetsRightsOwner("Test Owner");
        firstProject.getUsers().add(firstUser);
        firstProject.getUsers().add(secondUser);
        Client client = serviceManager.getClientService().getById(1);
        client.getProjects().add(firstProject);
        firstProject.setClient(client);
        serviceManager.getProjectService().save(firstProject);

        Project secondProject = new Project();
        secondProject.setTitle("Second project");
        secondProject.setUseDmsImport(false);
        localDate = new LocalDate(2016, 11, 10);
        secondProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017, 9, 15);
        secondProject.setEndDate(localDate.toDate());
        secondProject.setNumberOfPages(80);
        secondProject.setNumberOfVolumes(4);
        secondProject.getUsers().add(firstUser);
        client.getProjects().add(secondProject);
        secondProject.setClient(client);
        serviceManager.getProjectService().save(secondProject);

        firstUser.getProjects().add(firstProject);
        firstUser.getProjects().add(secondProject);
        secondUser.getProjects().add(firstProject);
        serviceManager.getUserService().save(firstUser);

        Project thirdProject = new Project();
        thirdProject.setTitle("Inactive project");
        localDate = new LocalDate(2014, 11, 10);
        thirdProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2016, 9, 15);
        thirdProject.setEndDate(localDate.toDate());
        thirdProject.setNumberOfPages(160);
        thirdProject.setNumberOfVolumes(5);
        thirdProject.setActive(false);
        User thirdUser = serviceManager.getUserService().getById(3);
        thirdProject.getUsers().add(secondUser);
        thirdProject.getUsers().add(thirdUser);
        Client secondClient = serviceManager.getClientService().getById(2);
        thirdProject.setClient(secondClient);
        serviceManager.getProjectService().save(thirdProject);

        secondUser.getProjects().add(thirdProject);
        thirdUser.getProjects().add(thirdProject);
        serviceManager.getUserService().save(secondUser);
        serviceManager.getUserService().save(thirdUser);
    }

    private static void insertFolders() throws DAOException, DataException {
        Project project = serviceManager.getProjectService().getById(1);

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
        fourthFolder.setMimeType("text/xml");
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

        project.getFolders().add(firstFolder);
        project.getFolders().add(secondFolder);
        project.getFolders().add(thirdFolder);
        project.getFolders().add(fourthFolder);
        project.getFolders().add(fifthFolder);

        serviceManager.getProjectService().save(project);
    }

    public static void insertRulesets() throws DAOException, DataException {
        Client client = serviceManager.getClientService().getById(1);

        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_test.xml");
        firstRuleset.setOrderMetadataByRuleset(false);
        firstRuleset.setClient(client);
        serviceManager.getRulesetService().save(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("SLUBHH");
        secondRuleset.setFile("ruleset_slubhh.xml");
        secondRuleset.setOrderMetadataByRuleset(false);
        secondRuleset.setClient(client);
        serviceManager.getRulesetService().save(secondRuleset);

        Client secondClient = serviceManager.getClientService().getById(2);

        Ruleset thirdRuleset = new Ruleset();
        thirdRuleset.setTitle("SLUBBB");
        thirdRuleset.setFile("ruleset_slubbb.xml");
        thirdRuleset.setOrderMetadataByRuleset(false);
        thirdRuleset.setClient(secondClient);
        serviceManager.getRulesetService().save(thirdRuleset);
    }

    private static void insertTasks() throws DAOException, DataException {
        Template firstTemplate = serviceManager.getTemplateService().getById(1);
        Role role = serviceManager.getRoleService().getById(1);

        List<Task> templateTasks = new ArrayList<>(getTasks());
        for (Task task : templateTasks) {
            task.setTemplate(firstTemplate);
            task.getRoles().add(role);
            role.getTasks().add(task);
            serviceManager.getTaskService().save(task);
        }

        Process firstProcess = serviceManager.getProcessService().getById(1);
        User firstUser = serviceManager.getUserService().getById(1);
        User secondUser = serviceManager.getUserService().getById(2);
        User blockedUser = serviceManager.getUserService().getById(3);

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
            serviceManager.getTaskService().save(task);
        }

        serviceManager.getUserService().save(firstUser);
        serviceManager.getUserService().save(secondUser);
        serviceManager.getUserService().save(blockedUser);

        Process secondProcess = serviceManager.getProcessService().getById(2);

        Task eleventhTask = new Task();
        eleventhTask.setTitle("Additional");
        eleventhTask.setOrdering(1);
        eleventhTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        LocalDate localDate = new LocalDate(2016, 9, 25);
        eleventhTask.setProcessingBegin(localDate.toDate());
        eleventhTask.setProcessingUser(firstUser);
        eleventhTask.setProcessingStatusEnum(TaskStatus.DONE);
        eleventhTask.setProcess(secondProcess);
        eleventhTask.setScriptName("scriptName");
        eleventhTask.setScriptPath("../type/automatic/script/path");
        eleventhTask.getRoles().add(role);
        role.getTasks().add(eleventhTask);
        serviceManager.getTaskService().save(eleventhTask);
        firstUser.getProcessingTasks().add(eleventhTask);

        Task twelfthTask = new Task();
        twelfthTask.setTitle("Processed and Some");
        twelfthTask.setOrdering(2);
        twelfthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016, 10, 25);
        twelfthTask.setProcessingBegin(localDate.toDate());
        twelfthTask.setProcessingUser(firstUser);
        twelfthTask.setProcessingStatusEnum(TaskStatus.INWORK);
        twelfthTask.setProcess(secondProcess);
        twelfthTask.getRoles().add(role);
        role.getTasks().add(twelfthTask);
        serviceManager.getTaskService().save(twelfthTask);
        firstUser.getProcessingTasks().add(twelfthTask);
        serviceManager.getUserService().save(firstUser);

        Task thirteenTask = new Task();
        thirteenTask.setTitle("Next Open");
        thirteenTask.setOrdering(3);
        thirteenTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016, 10, 25);
        thirteenTask.setProcessingBegin(localDate.toDate());
        thirteenTask.setProcessingStatusEnum(TaskStatus.OPEN);
        thirteenTask.setProcess(secondProcess);
        thirteenTask.getRoles().add(role);
        role.getTasks().add(thirteenTask);
        serviceManager.getTaskService().save(thirteenTask);

        serviceManager.getRoleService().save(role);
    }

    private static List<Task> getTasks() {
        Task firstTask = new Task();
        firstTask.setTitle("Finished");
        firstTask.setPriority(1);
        firstTask.setOrdering(1);
        firstTask.setEditTypeEnum(TaskEditType.ADMIN);
        LocalDate localDate = new LocalDate(2016, 8, 20);
        firstTask.setProcessingBegin(localDate.toDate());
        localDate = new LocalDate(2016, 9, 24);
        firstTask.setProcessingTime(localDate.toDate());
        localDate = new LocalDate(2016, 9, 24);
        firstTask.setProcessingEnd(localDate.toDate());
        firstTask.setProcessingStatusEnum(TaskStatus.DONE);

        Task secondTask = new Task();
        secondTask.setTitle("Blocking");
        secondTask.setOrdering(2);
        secondTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016, 9, 25);
        secondTask.setProcessingBegin(localDate.toDate());
        localDate = new LocalDate(2016, 11, 25);
        secondTask.setProcessingEnd(localDate.toDate());
        secondTask.setProcessingStatusEnum(TaskStatus.DONE);
        secondTask.setScriptName("scriptName");
        secondTask.setScriptPath("../type/automatic/script/path");

        Task thirdTask = new Task();
        thirdTask.setTitle("Progress");
        thirdTask.setOrdering(3);
        thirdTask.setPriority(10);
        thirdTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        thirdTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017, 1, 25);
        thirdTask.setProcessingBegin(localDate.toDate());
        thirdTask.setProcessingStatusEnum(TaskStatus.INWORK);

        Task fourthTask = new Task();
        fourthTask.setTitle("Open");
        fourthTask.setOrdering(4);
        fourthTask.setPriority(10);
        fourthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fourthTask.setProcessingStatusEnum(TaskStatus.OPEN);

        Task fifthTask = new Task();
        fifthTask.setTitle("Locked");
        fifthTask.setOrdering(5);
        fifthTask.setPriority(10);
        fifthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fifthTask.setTypeImagesWrite(true);
        fifthTask.setProcessingStatusEnum(TaskStatus.LOCKED);

        return Arrays.asList(firstTask, secondTask, thirdTask, fourthTask, fifthTask);
    }

    private static void insertTemplateProperties() throws DAOException, DataException {
        Process template = serviceManager.getProcessService().getById(1);

        Property firstTemplateProperty = new Property();
        firstTemplateProperty.setTitle("firstTemplate title");
        firstTemplateProperty.setValue("first value");
        firstTemplateProperty.setObligatory(true);
        firstTemplateProperty.setType(PropertyType.STRING);
        firstTemplateProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstTemplateProperty.setCreationDate(localDate.toDate());
        firstTemplateProperty.getTemplates().add(template);
        serviceManager.getPropertyService().save(firstTemplateProperty);

        Property secondTemplateProperty = new Property();
        secondTemplateProperty.setTitle("template");
        secondTemplateProperty.setValue("second");
        secondTemplateProperty.setObligatory(false);
        secondTemplateProperty.setType(PropertyType.STRING);
        secondTemplateProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondTemplateProperty.setCreationDate(localDate.toDate());
        secondTemplateProperty.getTemplates().add(template);
        serviceManager.getPropertyService().save(secondTemplateProperty);

        template.getTemplates().add(firstTemplateProperty);
        template.getTemplates().add(secondTemplateProperty);
        serviceManager.getProcessService().save(template);
    }

    private static void insertUsers() throws DAOException, DataException {
        SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
        Client firstClient = serviceManager.getClientService().getById(1);
        Client secondClient = serviceManager.getClientService().getById(2);

        Role adminRole = serviceManager.getRoleService().getById(1);
        Role generalRole = serviceManager.getRoleService().getById(2);
        Role projectRoleForFirstClient = serviceManager.getRoleService().getById(3);
        Role projectRoleForSecondClient = serviceManager.getRoleService().getById(4);
        Role withoutAuthoritiesRole = serviceManager.getRoleService().getById(5);

        User firstUser = new User();
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("kowal");
        firstUser.setPassword(passwordEncoder.encrypt("test"));
        firstUser.setLdapLogin("kowalLDP");
        firstUser.setLocation("Dresden");
        firstUser.setTableSize(20);
        firstUser.setLanguage("de");
        firstUser.setMetadataLanguage("de");
        firstUser.getRoles().add(adminRole);
        firstUser.getRoles().add(generalRole);
        firstUser.getClients().add(firstClient);
        serviceManager.getUserService().save(firstUser);

        User secondUser = new User();
        secondUser.setName("Adam");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("nowak");
        secondUser.setPassword(passwordEncoder.encrypt("test"));
        secondUser.setLdapLogin("nowakLDP");
        secondUser.setLocation("Dresden");
        secondUser.setLanguage("de");
        secondUser.setLdapGroup(serviceManager.getLdapGroupService().getById(1));
        secondUser.getRoles().add(projectRoleForFirstClient);
        secondUser.getRoles().add(projectRoleForSecondClient);
        secondUser.getClients().add(firstClient);
        secondUser.getClients().add(secondClient);
        serviceManager.getUserService().save(secondUser);

        User thirdUser = new User();
        thirdUser.setName("Anna");
        thirdUser.setSurname("Dora");
        thirdUser.setLogin("dora");
        thirdUser.setLdapLogin("doraLDP");
        thirdUser.setLocation("Leipzig");
        thirdUser.setLanguage("de");
        thirdUser.setActive(false);
        thirdUser.getRoles().add(adminRole);
        serviceManager.getUserService().save(thirdUser);

        User fourthUser = new User();
        fourthUser.setName("Max");
        fourthUser.setSurname("Mustermann");
        fourthUser.setLogin("mmustermann");
        fourthUser.setPassword(passwordEncoder.encrypt("test"));
        fourthUser.setLdapLogin("mmustermann");
        fourthUser.setLocation("Dresden");
        fourthUser.setTableSize(20);
        fourthUser.setLanguage("de");
        fourthUser.getRoles().add(withoutAuthoritiesRole);
        serviceManager.getUserService().save(fourthUser);

        User fifthUser = new User();
        fifthUser.setName("Last");
        fifthUser.setSurname("User");
        fifthUser.setLogin("user");
        fifthUser.setPassword(passwordEncoder.encrypt("test"));
        fifthUser.setLdapLogin("user");
        fifthUser.setLocation("Dresden");
        fifthUser.setTableSize(20);
        fifthUser.setLanguage("de");
        serviceManager.getUserService().save(fifthUser);
    }

    private static void insertRoles() throws DAOException, DataException {
        List<Authority> allAuthorities = serviceManager.getAuthorityService().getAll();
        Client client = serviceManager.getClientService().getById(1);

        Role firstRole = new Role();
        firstRole.setTitle("Admin");
        firstRole.setClient(client);

        // insert administration authorities
        for (int i = 0; i < 34; i++) {
            firstRole.getAuthorities().add(allAuthorities.get(i));
        }

        serviceManager.getRoleService().save(firstRole);

        Role secondRole = new Role();
        secondRole.setTitle("General");
        secondRole.setClient(client);

        // insert general authorities
        for (int i = 34; i < allAuthorities.size(); i++) {
            secondRole.getAuthorities().add(allAuthorities.get(i));
        }

        serviceManager.getRoleService().save(secondRole);

        Role thirdRole = new Role();
        thirdRole.setTitle("Random for first");
        thirdRole.setClient(client);

        // insert authorities for view on projects page
        List<Authority> userAuthoritiesForFirst = new ArrayList<>();
        userAuthoritiesForFirst.add(serviceManager.getAuthorityService().getByTitle("viewProject" + CLIENT_ASSIGNABLE));
        userAuthoritiesForFirst.add(serviceManager.getAuthorityService().getByTitle("viewAllProjects" + CLIENT_ASSIGNABLE));
        thirdRole.setAuthorities(userAuthoritiesForFirst);

        serviceManager.getRoleService().save(thirdRole);

        Role fourthRole = new Role();
        fourthRole.setTitle("Random for second");
        fourthRole.setClient(serviceManager.getClientService().getById(2));

        // insert authorities for view on projects page
        List<Authority> userAuthoritiesForSecond = new ArrayList<>();
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewProject" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewAllProjects" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewTemplate" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewAllTemplates" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewWorkflow" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewAllWorkflows" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewDocket" + CLIENT_ASSIGNABLE));
        userAuthoritiesForSecond.add(serviceManager.getAuthorityService().getByTitle("viewAllDockets" + CLIENT_ASSIGNABLE));
        fourthRole.setAuthorities(userAuthoritiesForSecond);

        serviceManager.getRoleService().save(fourthRole);

        Role fifthUserGroup = new Role();
        fifthUserGroup.setTitle("Without authorities");
        fifthUserGroup.setClient(client);
        serviceManager.getRoleService().save(fifthUserGroup);
    }

    private static void insertUserFilters() throws DAOException, DataException {
        User user = serviceManager.getUserService().getById(1);

        Filter firstUserFilter = new Filter();
        firstUserFilter.setValue("\"id:1\"");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstUserFilter.setCreationDate(localDate.toDate());
        firstUserFilter.setUser(user);
        serviceManager.getFilterService().save(firstUserFilter);

        Filter secondUserFilter = new Filter();
        secondUserFilter.setValue("\"id:2\"");
        localDate = new LocalDate(2017, 1, 15);
        secondUserFilter.setCreationDate(localDate.toDate());
        secondUserFilter.setUser(user);
        serviceManager.getFilterService().save(secondUserFilter);

        user.getFilters().add(firstUserFilter);
        user.getFilters().add(secondUserFilter);
        serviceManager.getUserService().save(user);
    }

    private static void insertWorkpieceProperties() throws DAOException, DataException {
        Process workpiece = serviceManager.getProcessService().getById(1);

        Property firstWorkpieceProperty = new Property();
        firstWorkpieceProperty.setTitle("FirstWorkpiece Property");
        firstWorkpieceProperty.setValue("first value");
        firstWorkpieceProperty.setObligatory(true);
        firstWorkpieceProperty.setType(PropertyType.STRING);
        firstWorkpieceProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 13);
        firstWorkpieceProperty.setCreationDate(localDate.toDate());
        firstWorkpieceProperty.getWorkpieces().add(workpiece);
        serviceManager.getPropertyService().save(firstWorkpieceProperty);

        Property secondWorkpieceProperty = new Property();
        secondWorkpieceProperty.setTitle("workpiece");
        secondWorkpieceProperty.setValue("second");
        secondWorkpieceProperty.setObligatory(false);
        secondWorkpieceProperty.setType(PropertyType.STRING);
        secondWorkpieceProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 14);
        secondWorkpieceProperty.setCreationDate(localDate.toDate());
        secondWorkpieceProperty.getWorkpieces().add(workpiece);
        serviceManager.getPropertyService().save(secondWorkpieceProperty);

        workpiece.getWorkpieces().add(firstWorkpieceProperty);
        workpiece.getWorkpieces().add(secondWorkpieceProperty);
        serviceManager.getProcessService().save(workpiece);
    }

    public static void insertWorkflows() throws DAOException, DataException {
        Workflow firstWorkflow = new Workflow("say-hello", "test");
        firstWorkflow.setActive(true);
        firstWorkflow.setReady(true);
        firstWorkflow.setClient(serviceManager.getClientService().getById(1));
        serviceManager.getWorkflowService().save(firstWorkflow);

        Workflow secondWorkflow = new Workflow("gateway", "gateway");
        secondWorkflow.setReady(false);
        secondWorkflow.setClient(serviceManager.getClientService().getById(2));
        serviceManager.getWorkflowService().save(secondWorkflow);
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
        List tableResult = session.createNativeQuery(query).getResultList();
        for (Object table : tableResult) {
            tables.add((String) table);
        }

        for (String table : tables) {
            session.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
        }

        Set<String> sequences = new HashSet<>();
        query = "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'";
        List sequencesResult = session.createNativeQuery(query).getResultList();
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
        serviceManager.getClientService().save(client);
        removableObjectIDs.put(ObjectType.CLIENT.name(), client.getId());

        Client assignableClient = serviceManager.getClientService().getById(1);

        Docket docket = new Docket();
        docket.setTitle("Removable docket");
        docket.setClient(assignableClient);
        serviceManager.getDocketService().save(docket);
        removableObjectIDs.put(ObjectType.DOCKET.name(), docket.getId());

        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("Removable ruleset");
        ruleset.setClient(assignableClient);
        serviceManager.getRulesetService().save(ruleset);
        removableObjectIDs.put(ObjectType.RULESET.name(), ruleset.getId());

        User user = new User();
        user.setName("Removable user");
        user.getClients().add(assignableClient);
        serviceManager.getUserService().save(user);
        removableObjectIDs.put(ObjectType.USER.name(), user.getId());

        Role role = new Role();
        role.setTitle("Removable role");
        role.setClient(assignableClient);
        serviceManager.getRoleService().save(role);
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

}
