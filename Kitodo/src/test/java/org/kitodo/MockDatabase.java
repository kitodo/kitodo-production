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

import static java.util.Arrays.asList;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;
import static org.kitodo.data.database.beans.Batch.Type.NEWSPAPER;
import static org.kitodo.data.database.beans.Batch.Type.SERIAL;

import de.sub.goobi.helper.Helper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kitodo.config.ConfigMain;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * Insert data to test database.
 */
public class MockDatabase {

    private static Node node;
    private static IndexRestClient indexRestClient;
    private static String testIndexName;
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final Logger logger = LogManager.getLogger(MockDatabase.class);
    private static final ServiceManager serviceManager = new ServiceManager();
    private static Server tcpServer;

    public static void startDatabaseServer() throws SQLException {
        tcpServer = Server.createTcpServer().start();
    }

    public static void stopDatabaseServer() throws SQLException {
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
        String nodeName = randomString(6);
        final String port = ConfigMain.getParameter("elasticsearch.port", "9205");

        testIndexName = ConfigMain.getParameter("elasticsearch.index", "testindex");
        indexRestClient = initializeIndexRestClient();

        Map settingsMap = prepareNodeSettings(port, HTTP_TRANSPORT_PORT, nodeName);
        Settings settings = Settings.builder().put(settingsMap).build();

        removeOldDataDirectories("target/" + nodeName);

        if (node != null) {
            stopNode();
        }
        node = new ExtendedNode(settings, asList(Netty4Plugin.class));
        node.start();
    }

    public static void stopNode() throws Exception {
        indexRestClient.deleteIndex();
        node.close();
        node = null;
    }

    public static void insertProcessesFull() throws DAOException, DataException {
        insertBatches();
        insertDockets();
        insertRulesets();
        insertLdapGroups();
        insertUsers();
        insertUserGroups();
        insertProjects();
        insertProjectFileGroups();
        insertProcesses();
        insertProcessProperties();
        insertWorkpieces();
        insertWorkpieceProperties();
        insertTemplates();
        insertTemplateProperties();
        insertUserFilters();
        insertTasks();
        insertHistory();
    }

    public static void insertUserGroupsFull() throws DAOException, DataException {
        insertLdapGroups();
        insertUsers();
        insertUserGroups();
    }

    private static class ExtendedNode extends Node {
        public ExtendedNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
    }

    private static String randomString(int lenght) {
        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(lenght);
        for (int i = 0; i < lenght; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    private static IndexRestClient initializeIndexRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(testIndexName);
        return restClient;
    }

    private static String readMapping() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classloader.getResourceAsStream("mapping.json")) {
            String mapping = IOUtils.toString(inputStream, "UTF-8");
            Object object = parser.parse(mapping);
            jsonObject = (JSONObject) object;
        } catch (IOException | ParseException e) {
            logger.error(e);
        }
        return jsonObject.toJSONString();
    }

    private static void removeOldDataDirectories(String dataDirectory) throws Exception {
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(dataDir.toPath());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map prepareNodeSettings(String httpPort, String httpTransportPort, String nodeName) {
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
        settingsMap.put("transport.tcp.port", httpTransportPort);
        settingsMap.put("transport.type", "netty4");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");
        return settingsMap;
    }

    private static void insertBatches() throws DataException {
        Batch firstBatch = new Batch();
        firstBatch.setTitle("First batch");
        firstBatch.setType(LOGISTIC);
        serviceManager.getBatchService().save(firstBatch);

        Batch secondBatch = new Batch();
        secondBatch.setTitle("Second batch");
        secondBatch.setType(LOGISTIC);
        serviceManager.getBatchService().save(secondBatch);

        Batch thirdBatch = new Batch();
        thirdBatch.setTitle("Third batch");
        thirdBatch.setType(NEWSPAPER);
        serviceManager.getBatchService().save(thirdBatch);

        Batch fourthBatch = new Batch();
        fourthBatch.setType(SERIAL);
        serviceManager.getBatchService().save(fourthBatch);
    }

    public static void insertDockets() throws DataException {
        Docket firstDocket = new Docket();
        firstDocket.setTitle("default");
        firstDocket.setFile("docket.xsl");
        serviceManager.getDocketService().save(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second");
        secondDocket.setFile("docket.xsl");
        serviceManager.getDocketService().save(secondDocket);
    }

    private static void insertHistory() throws DAOException, DataException {
        History firstHistory = new History();
        Process firstProcess = serviceManager.getProcessService().getById(1);
        firstHistory.setNumericValue(2.0);
        firstHistory.setStringValue("History");
        firstHistory.setHistoryType(HistoryTypeEnum.color);
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstHistory.setDate(localDate.toDate());
        firstHistory.setProcess(firstProcess);
        serviceManager.getHistoryService().save(firstHistory);
        firstProcess.getHistory().add(firstHistory);
        serviceManager.getProcessService().save(firstProcess);
    }

    public static void insertLdapGroups() throws DAOException {
        LdapGroup firstLdapGroup = new LdapGroup();
        firstLdapGroup.setTitle("LG");
        firstLdapGroup.setHomeDirectory("..//test_directory/");
        firstLdapGroup.setDescription("Test LDAP group");
        firstLdapGroup.setDisplayName("Name");
        serviceManager.getLdapGroupService().save(firstLdapGroup);
    }

    private static void insertProcesses() throws DAOException, DataException {
        Project project = serviceManager.getProjectService().getById(1);

        Process firstProcess = new Process();
        firstProcess.setTitle("First process");
        firstProcess.setOutputName("Test");
        firstProcess.setWikiField("wiki");
        LocalDate localDate = new LocalDate(2016, 10, 20);
        firstProcess.setCreationDate(localDate.toDate());
        firstProcess.setSortHelperImages(20);
        List<Batch> batches = new ArrayList<>();
        Batch firstBatch = serviceManager.getBatchService().getById(1);
        Batch secondBatch = serviceManager.getBatchService().getById(3);
        List<Process> processes = new ArrayList<>();
        processes.add(firstProcess);
        firstBatch.setProcesses(processes);
        secondBatch.setProcesses(processes);
        batches.add(firstBatch);
        batches.add(secondBatch);
        firstProcess.setBatches(batches);
        firstProcess.setTemplate(true);
        firstProcess.setInChoiceListShown(true);
        firstProcess.setDocket(serviceManager.getDocketService().getById(1));
        firstProcess.setProject(project);
        firstProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        serviceManager.getProcessService().save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("Second process");
        secondProcess.setOutputName("Testowy");
        secondProcess.setWikiField("field");
        localDate = new LocalDate(2017, 1, 20);
        secondProcess.setCreationDate(localDate.toDate());
        secondProcess.setInChoiceListShown(true);
        secondProcess.setSortHelperImages(30);
        secondProcess.setDocket(serviceManager.getDocketService().getById(1));
        secondProcess.setProject(project);
        secondProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        secondProcess.setSortHelperStatus("100000000");
        serviceManager.getProcessService().save(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setTitle("Second process");
        thirdProcess.setOutputName("Unreachable");
        thirdProcess.setWikiField("problem");
        localDate = new LocalDate(2017, 2, 10);
        thirdProcess.setCreationDate(localDate.toDate());
        thirdProcess.setInChoiceListShown(true);
        thirdProcess.setDocket(serviceManager.getDocketService().getById(1));
        thirdProcess.setProject(project);
        thirdProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        serviceManager.getProcessService().save(thirdProcess);

        Process fourthProcess = new Process();
        fourthProcess.setTitle("DBConnectionTest");
        fourthProcess.setInChoiceListShown(true);
        serviceManager.getProcessService().save(fourthProcess);

        project.getProcesses().add(firstProcess);
        project.getProcesses().add(secondProcess);
        project.getProcesses().add(thirdProcess);

        serviceManager.getProjectService().save(project);

        Project thirdProject = serviceManager.getProjectService().getById(3);
        Process fifthProcess = new Process();
        fifthProcess.setTitle("Fifth process");
        fifthProcess.setOutputName("Unreachable");
        fifthProcess.setWikiField("problem");
        localDate = new LocalDate(2017, 2, 10);
        fifthProcess.setCreationDate(localDate.toDate());
        fifthProcess.setDocket(serviceManager.getDocketService().getById(1));
        fifthProcess.setProject(thirdProject);
        fifthProcess.setRuleset(serviceManager.getRulesetService().getById(1));
        fifthProcess.setTemplate(true);
        fifthProcess.setInChoiceListShown(true);
        serviceManager.getProcessService().save(fifthProcess);
        serviceManager.getProjectService().save(thirdProject);
    }

    private static void insertProcessProperties() throws DAOException, DataException {
        Process firstProcess = serviceManager.getProcessService().getById(1);

        Property firstProcessProperty = new Property();
        firstProcessProperty.setTitle("Process Property");
        firstProcessProperty.setValue("first value");
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setType(PropertyType.general);
        firstProcessProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstProcessProperty.setCreationDate(localDate.toDate());
        firstProcessProperty.setContainer(1);
        firstProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(firstProcessProperty);

        Property secondProcessProperty = new Property();
        secondProcessProperty.setTitle("Korrektur notwendig");
        secondProcessProperty.setValue("second value");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setType(PropertyType.CommandLink);
        secondProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondProcessProperty.setCreationDate(localDate.toDate());
        secondProcessProperty.setContainer(2);
        secondProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(secondProcessProperty);

        Property thirdProcessProperty = new Property();
        thirdProcessProperty.setTitle("Korrektur notwendig");
        thirdProcessProperty.setValue("fix it");
        thirdProcessProperty.setObligatory(false);
        thirdProcessProperty.setType(PropertyType.CommandLink);
        thirdProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 7, 15);
        thirdProcessProperty.setCreationDate(localDate.toDate());
        thirdProcessProperty.setContainer(2);
        thirdProcessProperty.getProcesses().add(firstProcess);
        serviceManager.getPropertyService().save(thirdProcessProperty);

        Process secondProcess = serviceManager.getProcessService().getById(2);
        Property fourthProcessProperty = new Property();
        fourthProcessProperty.setTitle("Korrektur notwendig");
        fourthProcessProperty.setValue("improved ids");
        fourthProcessProperty.setObligatory(false);
        fourthProcessProperty.setType(PropertyType.CommandLink);
        fourthProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 7, 15);
        fourthProcessProperty.setCreationDate(localDate.toDate());
        fourthProcessProperty.setContainer(2);
        fourthProcessProperty.getProcesses().add(secondProcess);
        serviceManager.getPropertyService().save(fourthProcessProperty);

        firstProcess.getProperties().add(firstProcessProperty);
        firstProcess.getProperties().add(secondProcessProperty);
        firstProcess.getProperties().add(thirdProcessProperty);
        serviceManager.getProcessService().save(firstProcess);

        secondProcess.getProperties().add(fourthProcessProperty);
        serviceManager.getProcessService().save(secondProcess);

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
        firstProject.getUsers().add(firstUser);
        firstProject.getUsers().add(secondUser);
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
        serviceManager.getProjectService().save(secondProject);

        firstUser.getProjects().add(firstProject);
        firstUser.getProjects().add(secondProject);
        secondUser.getProjects().add(firstProject);
        serviceManager.getUserService().save(firstUser);
        serviceManager.getUserService().save(secondUser);

        Project thirdProject = new Project();
        thirdProject.setTitle("Archived project");
        localDate = new LocalDate(2014, 11, 10);
        thirdProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2016, 9, 15);
        thirdProject.setEndDate(localDate.toDate());
        thirdProject.setNumberOfPages(160);
        thirdProject.setNumberOfVolumes(5);
        thirdProject.setProjectIsArchived(true);
        serviceManager.getProjectService().save(thirdProject);
    }

    private static void insertProjectFileGroups() throws DAOException, DataException {
        Project project = serviceManager.getProjectService().getById(1);

        ProjectFileGroup firstProjectFileGroup = new ProjectFileGroup();
        firstProjectFileGroup.setName("MAX");
        firstProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstProjectFileGroup.setMimeType("image/jpeg");
        firstProjectFileGroup.setSuffix("jpg");
        firstProjectFileGroup.setPreviewImage(false);
        firstProjectFileGroup.setProject(project);
        serviceManager.getProjectFileGroupService().save(firstProjectFileGroup);

        ProjectFileGroup secondProjectFileGroup = new ProjectFileGroup();
        secondProjectFileGroup.setName("DEFAULT");
        secondProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondProjectFileGroup.setMimeType("image/jpeg");
        secondProjectFileGroup.setSuffix("jpg");
        secondProjectFileGroup.setPreviewImage(false);
        secondProjectFileGroup.setProject(project);
        serviceManager.getProjectFileGroupService().save(secondProjectFileGroup);

        ProjectFileGroup thirdProjectFileGroup = new ProjectFileGroup();
        thirdProjectFileGroup.setName("THUMBS");
        thirdProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdProjectFileGroup.setMimeType("image/jpeg");
        thirdProjectFileGroup.setSuffix("jpg");
        thirdProjectFileGroup.setPreviewImage(false);
        thirdProjectFileGroup.setProject(project);
        serviceManager.getProjectFileGroupService().save(thirdProjectFileGroup);

        ProjectFileGroup fourthProjectFileGroup = new ProjectFileGroup();
        fourthProjectFileGroup.setName("FULLTEXT");
        fourthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthProjectFileGroup.setMimeType("text/xml");
        fourthProjectFileGroup.setSuffix("xml");
        fourthProjectFileGroup.setPreviewImage(false);
        fourthProjectFileGroup.setProject(project);
        serviceManager.getProjectFileGroupService().save(fourthProjectFileGroup);

        ProjectFileGroup fifthProjectFileGroup = new ProjectFileGroup();
        fifthProjectFileGroup.setName("DOWNLOAD");
        fifthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthProjectFileGroup.setMimeType("application/pdf");
        fifthProjectFileGroup.setSuffix("pdf");
        fifthProjectFileGroup.setPreviewImage(false);
        fifthProjectFileGroup.setProject(project);
        serviceManager.getProjectFileGroupService().save(fifthProjectFileGroup);

        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);

        serviceManager.getProjectService().save(project);
    }

    public static void insertRulesets() throws DataException {
        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_test.xml");
        firstRuleset.setOrderMetadataByRuleset(false);
        serviceManager.getRulesetService().save(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("SLUBHH");
        secondRuleset.setFile("ruleset_slubhh.xml");
        secondRuleset.setOrderMetadataByRuleset(false);
        serviceManager.getRulesetService().save(secondRuleset);
    }

    private static void insertTasks() throws DAOException, DataException {
        Task firstTask = new Task();
        Process firstProcess = serviceManager.getProcessService().getById(1);
        UserGroup userGroup = serviceManager.getUserGroupService().getById(1);
        firstTask.setTitle("Testing");
        firstTask.setPriority(1);
        firstTask.setOrdering(1);
        firstTask.setEditTypeEnum(TaskEditType.ADMIN);
        LocalDate localDate = new LocalDate(2016, 10, 20);
        firstTask.setProcessingBegin(localDate.toDate());
        localDate = new LocalDate(2016, 12, 24);
        firstTask.setProcessingTime(localDate.toDate());
        localDate = new LocalDate(2016, 12, 24);
        firstTask.setProcessingEnd(localDate.toDate());
        User firstUser = serviceManager.getUserService().getById(1);
        firstTask.setProcessingUser(firstUser);
        firstTask.setProcessingStatusEnum(TaskStatus.OPEN);
        firstTask.setProcess(firstProcess);
        firstTask.setUsers(serviceManager.getUserService().getAll());
        firstTask.getUserGroups().add(userGroup);
        serviceManager.getTaskService().save(firstTask);
        firstProcess.getTasks().add(firstTask);
        serviceManager.getProcessService().save(firstProcess);
        firstUser.getProcessingTasks().add(firstTask);
        serviceManager.getUserService().save(firstUser);

        Process secondProcess = serviceManager.getProcessService().getById(2);
        User blockedUser = serviceManager.getUserService().getById(3);
        User secondUser = serviceManager.getUserService().getById(2);

        Task secondTask = new Task();
        secondTask.setTitle("Blocking");
        secondTask = serviceManager.getTaskService().setCorrectionStep(secondTask);
        secondTask.setOrdering(1);
        secondTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016, 9, 25);
        secondTask.setProcessingBegin(localDate.toDate());
        secondTask.setProcessingUser(blockedUser);
        secondTask.setProcessingStatusEnum(TaskStatus.OPEN);
        secondTask.setProcess(secondProcess);
        secondTask.getUsers().add(blockedUser);
        secondTask.getUsers().add(secondUser);
        secondTask.getUserGroups().add(userGroup);
        secondTask.setScriptName("scriptName");
        secondTask.setScriptPath("../type/automatic/script/path");
        serviceManager.getTaskService().save(secondTask);

        Task thirdTask = new Task();
        thirdTask.setTitle("Testing and Blocking");
        thirdTask.setOrdering(2);
        thirdTask.setPriority(10);
        thirdTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2017, 1, 25);
        thirdTask.setProcessingBegin(localDate.toDate());
        thirdTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        thirdTask.setProcess(secondProcess);
        thirdTask.getUsers().add(secondUser);
        serviceManager.getTaskService().save(thirdTask);

        Task fourthTask = new Task();
        fourthTask.setTitle("Progress");
        fourthTask.setOrdering(3);
        fourthTask.setPriority(10);
        fourthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fourthTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017, 1, 29);
        fourthTask.setProcessingBegin(localDate.toDate());
        fourthTask.setProcessingStatusEnum(TaskStatus.INWORK);
        fourthTask.setProcessingUser(secondUser);
        fourthTask.setProcess(secondProcess);
        fourthTask.setUsers(serviceManager.getUserService().getAll());
        serviceManager.getTaskService().save(fourthTask);

        secondProcess.getTasks().add(secondTask);
        secondProcess.getTasks().add(thirdTask);
        secondProcess.getTasks().add(fourthTask);
        serviceManager.getProcessService().save(secondProcess);

        secondUser.getProcessingTasks().add(fourthTask);
        blockedUser.getProcessingTasks().add(secondTask);
        blockedUser.getTasks().add(secondTask);
        secondUser.getTasks().add(secondTask);
        secondUser.getTasks().add(thirdTask);
        serviceManager.getUserService().save(blockedUser);
        serviceManager.getUserService().save(secondUser);

        userGroup.getTasks().add(firstTask);
        userGroup.getTasks().add(secondTask);
        serviceManager.getUserGroupService().save(userGroup);

        Process fifthProcess = serviceManager.getProcessService().getById(5);

        Task fifthTask = new Task();
        fifthTask.setTitle("Closed");
        fifthTask.setOrdering(1);
        fifthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fifthTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017, 6, 27);
        fifthTask.setProcessingBegin(localDate.toDate());
        fifthTask.setProcessingStatusEnum(TaskStatus.DONE);
        fifthTask.setProcessingUser(secondUser);
        fifthTask.setProcess(fifthProcess);
        fifthTask.setUsers(serviceManager.getUserService().getAll());
        serviceManager.getTaskService().save(fifthTask);

        Task sixthTask = new Task();
        sixthTask.setTitle("Progress");
        sixthTask.setOrdering(2);
        sixthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        sixthTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017, 7, 27);
        sixthTask.setProcessingBegin(localDate.toDate());
        sixthTask.setProcessingStatusEnum(TaskStatus.INWORK);
        sixthTask.setProcessingUser(secondUser);
        sixthTask.setProcess(fifthProcess);
        sixthTask.setUsers(serviceManager.getUserService().getAll());
        serviceManager.getTaskService().save(sixthTask);

        fifthProcess.getTasks().add(fifthTask);
        fifthProcess.getTasks().add(sixthTask);
        serviceManager.getProcessService().save(fifthProcess);

        secondUser.getProcessingTasks().add(fifthTask);
        secondUser.getProcessingTasks().add(sixthTask);
        serviceManager.getUserService().save(secondUser);
    }

    private static void insertTemplates() throws DAOException, DataException {
        Process process = serviceManager.getProcessService().getById(1);
        process.setTemplate(true);

        Template firstTemplate = new Template();
        firstTemplate.setProcess(process);
        firstTemplate.setOrigin("test");
        serviceManager.getTemplateService().save(firstTemplate);

        Template secondTemplate = new Template();
        secondTemplate.setProcess(process);
        secondTemplate.setOrigin("addition");
        serviceManager.getTemplateService().save(secondTemplate);

        process.getTemplates().add(firstTemplate);
        process.getTemplates().add(secondTemplate);
        serviceManager.getProcessService().save(process);
    }

    private static void insertTemplateProperties() throws DAOException, DataException {
        Template template = serviceManager.getTemplateService().getById(1);

        Property firstTemplateProperty = new Property();
        firstTemplateProperty.setTitle("firstTemplate title");
        firstTemplateProperty.setValue("first value");
        firstTemplateProperty.setObligatory(true);
        firstTemplateProperty.setType(PropertyType.general);
        firstTemplateProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstTemplateProperty.setCreationDate(localDate.toDate());
        firstTemplateProperty.setContainer(1);
        firstTemplateProperty.getTemplates().add(template);
        serviceManager.getPropertyService().save(firstTemplateProperty);

        Property secondTemplateProperty = new Property();
        secondTemplateProperty.setTitle("template");
        secondTemplateProperty.setValue("second");
        secondTemplateProperty.setObligatory(false);
        secondTemplateProperty.setType(PropertyType.CommandLink);
        secondTemplateProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondTemplateProperty.setCreationDate(localDate.toDate());
        secondTemplateProperty.setContainer(2);
        secondTemplateProperty.getTemplates().add(template);
        serviceManager.getPropertyService().save(secondTemplateProperty);

        template.getProperties().add(firstTemplateProperty);
        template.getProperties().add(secondTemplateProperty);
        serviceManager.getTemplateService().save(template);
    }

    private static void insertUsers() throws DAOException, DataException {
        User firstUser = new User();
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("kowal");
        firstUser.setPasswordDecrypted("test");
        firstUser.setLdapLogin("kowalLDP");
        firstUser.setLocation("Dresden");
        firstUser.setTableSize(20);
        firstUser.setCss("/css/fancy.css");
        serviceManager.getUserService().save(firstUser);

        User secondUser = new User();
        secondUser.setName("Adam");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("nowak");
        secondUser.setLdapLogin("nowakLDP");
        secondUser.setLocation("Dresden");
        secondUser.setSessionTimeout(9000);
        secondUser.setLdapGroup(serviceManager.getLdapGroupService().getById(1));
        serviceManager.getUserService().save(secondUser);

        User thirdUser = new User();
        thirdUser.setName("Anna");
        thirdUser.setSurname("Dora");
        thirdUser.setLogin("dora");
        thirdUser.setLdapLogin("doraLDP");
        thirdUser.setLocation("Leipzig");
        thirdUser.setActive(false);
        serviceManager.getUserService().save(thirdUser);
    }

    private static void insertUserGroups() throws DAOException, DataException {
        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setTitle("Admin");
        firstUserGroup.setPermission(1);
        List<User> users = new ArrayList<>();
        User firstUser = serviceManager.getUserService().getById(1);
        User secondUser = serviceManager.getUserService().getById(2);
        List<UserGroup> userGroups = new ArrayList<>();
        userGroups.add(firstUserGroup);
        firstUser.setUserGroups(userGroups);
        secondUser.setUserGroups(userGroups);
        users.add(firstUser);
        users.add(secondUser);
        firstUserGroup.setUsers(users);
        serviceManager.getUserGroupService().save(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setTitle("Random");
        secondUserGroup.setPermission(2);
        serviceManager.getUserGroupService().save(secondUserGroup);

        UserGroup thirdUserGroup = new UserGroup();
        thirdUserGroup.setTitle("Without permission");
        serviceManager.getUserGroupService().save(thirdUserGroup);
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

    private static void insertWorkpieces() throws DAOException, DataException {
        Process process = serviceManager.getProcessService().getById(1);

        Workpiece firstWorkpiece = new Workpiece();
        firstWorkpiece.setProcess(process);
        serviceManager.getWorkpieceService().save(firstWorkpiece);

        Workpiece secondWorkpiece = new Workpiece();
        secondWorkpiece.setProcess(process);
        serviceManager.getWorkpieceService().save(secondWorkpiece);

        process.getWorkpieces().add(firstWorkpiece);
        process.getWorkpieces().add(secondWorkpiece);
        serviceManager.getProcessService().save(process);
    }

    private static void insertWorkpieceProperties() throws DAOException, DataException {
        Workpiece workpiece = serviceManager.getWorkpieceService().getById(1);

        Property firstWorkpieceProperty = new Property();
        firstWorkpieceProperty.setTitle("FirstWorkpiece Property");
        firstWorkpieceProperty.setValue("first value");
        firstWorkpieceProperty.setObligatory(true);
        firstWorkpieceProperty.setType(PropertyType.general);
        firstWorkpieceProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 13);
        firstWorkpieceProperty.setCreationDate(localDate.toDate());
        firstWorkpieceProperty.setContainer(1);
        firstWorkpieceProperty.getWorkpieces().add(workpiece);
        serviceManager.getPropertyService().save(firstWorkpieceProperty);

        Property secondWorkpieceProperty = new Property();
        secondWorkpieceProperty.setTitle("workpiece");
        secondWorkpieceProperty.setValue("second");
        secondWorkpieceProperty.setObligatory(false);
        secondWorkpieceProperty.setType(PropertyType.CommandLink);
        secondWorkpieceProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 14);
        secondWorkpieceProperty.setCreationDate(localDate.toDate());
        secondWorkpieceProperty.setContainer(2);
        secondWorkpieceProperty.getWorkpieces().add(workpiece);
        serviceManager.getPropertyService().save(secondWorkpieceProperty);

        workpiece.getProperties().add(firstWorkpieceProperty);
        workpiece.getProperties().add(secondWorkpieceProperty);
        serviceManager.getWorkpieceService().save(workpiece);
    }

    /**
     * Clean database after class. Truncate all tables, reset id sequences and
     * clear session.
     */
    public static void cleanDatabase() {
        Session session = Helper.getHibernateSession();
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
        transaction.commit();
        session.clear();
    }
}
