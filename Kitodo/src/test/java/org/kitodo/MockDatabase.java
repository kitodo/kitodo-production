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

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.kitodo.data.database.beans.*;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryType;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.IndexRestClient;
import org.kitodo.services.ServiceManager;

/**
 * Insert data to test database.
 */
public class MockDatabase {

    private static final ServiceManager serviceManager = new ServiceManager();

    public static void insertProcessesFull() throws DAOException, IOException, ResponseException {
        if (serviceManager.getBatchService().find(1) == null) {
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
            insertTasks();
            insertHistory();
        }
    }

    private static void insertBatches() throws DAOException, IOException, ResponseException {
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

    public static void insertDockets() throws DAOException, IOException, ResponseException {
        Docket firstDocket = new Docket();
        firstDocket.setTitle("default");
        firstDocket.setFile("docket.xsl");
        serviceManager.getDocketService().save(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setTitle("second");
        secondDocket.setFile("docket.xsl");
        serviceManager.getDocketService().save(secondDocket);
    }

    private static void insertHistory() throws DAOException, IOException, ResponseException {
        History firstHistory = new History();
        Process firstProcess = serviceManager.getProcessService().find(1);
        firstHistory.setNumericValue(2.0);
        firstHistory.setStringValue("History");
        firstHistory.setHistoryType(HistoryType.color);
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

    private static void insertProcesses() throws DAOException, IOException, ResponseException {
        Process firstProcess = new Process();
        firstProcess.setTitle("First process");
        firstProcess.setOutputName("Test");
        firstProcess.setWikiField("wiki");
        LocalDate localDate = new LocalDate(2016, 10, 20);
        firstProcess.setCreationDate(localDate.toDate());

        List<Batch> batches = new ArrayList<>();
        Batch firstBatch = serviceManager.getBatchService().find(1);
        Batch secondBatch = serviceManager.getBatchService().find(3);
        List<Process> processes = new ArrayList<>();
        processes.add(firstProcess);
        firstBatch.setProcesses(processes);
        secondBatch.setProcesses(processes);
        batches.add(firstBatch);
        batches.add(secondBatch);
        firstProcess.setBatches(batches);

        firstProcess.setDocket(serviceManager.getDocketService().find(1));
        firstProcess.setProject(serviceManager.getProjectService().find(1));
        firstProcess.setRuleset(serviceManager.getRulesetService().find(1));
        serviceManager.getProcessService().save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("Second process");
        secondProcess.setOutputName("Testowy");
        secondProcess.setWikiField("field");
        localDate = new LocalDate(2017, 1, 20);
        secondProcess.setCreationDate(localDate.toDate());
        secondProcess.setDocket(serviceManager.getDocketService().find(1));
        secondProcess.setProject(serviceManager.getProjectService().find(1));
        secondProcess.setRuleset(serviceManager.getRulesetService().find(1));
        serviceManager.getProcessService().save(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setTitle("Second process");
        thirdProcess.setOutputName("Unreachable");
        thirdProcess.setWikiField("problem");
        localDate = new LocalDate(2017, 2, 10);
        thirdProcess.setCreationDate(localDate.toDate());
        thirdProcess.setDocket(serviceManager.getDocketService().find(1));
        thirdProcess.setProject(serviceManager.getProjectService().find(1));
        thirdProcess.setRuleset(serviceManager.getRulesetService().find(1));
        serviceManager.getProcessService().save(thirdProcess);
    }

    private static void insertProcessProperties() throws DAOException, IOException, ResponseException {
        ProcessProperty firstProcessProperty = new ProcessProperty();
        Process process = serviceManager.getProcessService().find(1);
        firstProcessProperty.setTitle("First Property");
        firstProcessProperty.setValue("first value");
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setType(PropertyType.general);
        firstProcessProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstProcessProperty.setCreationDate(localDate.toDate());
        firstProcessProperty.setContainer(1);
        firstProcessProperty.setProcess(process);
        serviceManager.getProcessPropertyService().save(firstProcessProperty);

        ProcessProperty secondProcessProperty = new ProcessProperty();
        secondProcessProperty.setTitle("secondProperty");
        secondProcessProperty.setValue("second");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setType(PropertyType.CommandLink);
        secondProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondProcessProperty.setCreationDate(localDate.toDate());
        secondProcessProperty.setContainer(2);
        secondProcessProperty.setProcess(process);
        serviceManager.getProcessPropertyService().save(secondProcessProperty);

        process.getProperties().add(firstProcessProperty);
        process.getProperties().add(secondProcessProperty);
        serviceManager.getProcessService().save(process);
    }

    private static void insertProjects() throws DAOException, IOException, ResponseException {
        User firstUser = serviceManager.getUserService().find(1);
        User secondUser = serviceManager.getUserService().find(2);

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

    private static void insertProjectFileGroups() throws DAOException, IOException, ResponseException {
        Project project = serviceManager.getProjectService().find(1);

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

    public static void insertRulesets() throws DAOException, IOException, ResponseException {
        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_slubdd.xml");
        firstRuleset.setOrderMetadataByRuleset(false);
        serviceManager.getRulesetService().save(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("SLUBHH");
        secondRuleset.setFile("ruleset_slubhh.xml");
        secondRuleset.setOrderMetadataByRuleset(false);
        serviceManager.getRulesetService().save(secondRuleset);
    }

    private static void insertTasks() throws DAOException, IOException, ResponseException {
        Task firstTask = new Task();
        Process firstProcess = serviceManager.getProcessService().find(1);
        UserGroup userGroup = serviceManager.getUserGroupService().find(1);
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
        User firstUser = serviceManager.getUserService().find(1);
        firstTask.setProcessingUser(firstUser);
        firstTask.setProcessingStatusEnum(TaskStatus.OPEN);
        firstTask.setProcess(firstProcess);
        firstTask.setUsers(serviceManager.getUserService().findAll());
        firstTask.getUserGroups().add(userGroup);
        firstProcess.getTasks().add(firstTask);
        serviceManager.getProcessService().save(firstProcess);
        firstUser.getProcessingTasks().add(firstTask);
        serviceManager.getUserService().save(firstUser);

        Process secondProcess = serviceManager.getProcessService().find(2);
        User blockedUser = serviceManager.getUserService().find(3);
        User secondUser = serviceManager.getUserService().find(2);

        Task secondTask = new Task();
        secondTask.setTitle("Blocking");
        secondTask = serviceManager.getTaskService().setCorrectionStep(secondTask);
        secondTask.setOrdering(2);
        secondTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016, 9, 25);
        secondTask.setProcessingBegin(localDate.toDate());
        secondTask.setProcessingUser(blockedUser);
        secondTask.setProcessingStatusEnum(TaskStatus.OPEN);
        secondTask.setProcess(secondProcess);
        secondTask.getUsers().add(blockedUser);
        secondTask.getUsers().add(secondUser);
        secondTask.getUserGroups().add(userGroup);
        secondTask.setScriptName1("scriptName");
        secondTask.setTypeAutomaticScriptPath("../type/automatic/script/path");
        secondTask.setScriptName2("secondScriptName");
        secondTask.setTypeAutomaticScriptPath2("../type/automatic/script/path2");
        secondTask.setScriptName3("thirdScriptName");
        secondTask.setTypeAutomaticScriptPath3("../type/automatic/script/path3");

        Task thirdTask = new Task();
        thirdTask.setTitle("Testing and Blocking");
        thirdTask.setOrdering(3);
        thirdTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2017, 1, 25);
        thirdTask.setProcessingBegin(localDate.toDate());
        thirdTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        thirdTask.setProcess(secondProcess);
        thirdTask.getUsers().add(secondUser);

        Task fourthTask = new Task();
        fourthTask.setTitle("Progress");
        fourthTask.setOrdering(4);
        fourthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fourthTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017, 1, 29);
        fourthTask.setProcessingBegin(localDate.toDate());
        fourthTask.setProcessingStatusEnum(TaskStatus.INWORK);
        fourthTask.setProcessingUser(serviceManager.getUserService().find(2));
        fourthTask.setProcess(secondProcess);
        fourthTask.setUsers(serviceManager.getUserService().findAll());

        secondProcess.getTasks().add(secondTask);
        secondProcess.getTasks().add(thirdTask);
        secondProcess.getTasks().add(fourthTask);
        System.out.println("tasks2 " + secondProcess.getTasks().size());
        serviceManager.getProcessService().save(secondProcess);

        blockedUser.getProcessingTasks().add(secondTask);
        blockedUser.getTasks().add(secondTask);
        secondUser.getTasks().add(secondTask);
        secondUser.getTasks().add(thirdTask);
        serviceManager.getUserService().save(blockedUser);
        serviceManager.getUserService().save(secondUser);

        userGroup.getTasks().add(firstTask);
        userGroup.getTasks().add(secondTask);
        serviceManager.getUserGroupService().save(userGroup);
    }

    private static void insertTemplates() throws DAOException, IOException, ResponseException {
        Process process = serviceManager.getProcessService().find(1);

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

    private static void insertTemplateProperties() throws DAOException, IOException, ResponseException {
        Template template = serviceManager.getTemplateService().find(1);

        TemplateProperty firstTemplateProperty = new TemplateProperty();
        firstTemplateProperty.setTitle("first title");
        firstTemplateProperty.setValue("first value");
        firstTemplateProperty.setObligatory(true);
        firstTemplateProperty.setType(PropertyType.general);
        firstTemplateProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstTemplateProperty.setCreationDate(localDate.toDate());
        firstTemplateProperty.setContainer(1);
        firstTemplateProperty.setTemplate(template);
        serviceManager.getTemplatePropertyService().save(firstTemplateProperty);

        TemplateProperty secondTemplateProperty = new TemplateProperty();
        secondTemplateProperty.setTitle("template");
        secondTemplateProperty.setValue("second");
        secondTemplateProperty.setObligatory(false);
        secondTemplateProperty.setType(PropertyType.CommandLink);
        secondTemplateProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondTemplateProperty.setCreationDate(localDate.toDate());
        secondTemplateProperty.setContainer(2);
        secondTemplateProperty.setTemplate(template);
        serviceManager.getTemplatePropertyService().save(secondTemplateProperty);

        template.getProperties().add(firstTemplateProperty);
        template.getProperties().add(secondTemplateProperty);
        serviceManager.getTemplateService().save(template);
    }

    private static void insertUsers() throws DAOException, IOException, ResponseException {
        User firstUser = new User();
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("kowal");
        firstUser.setPassword("test");
        firstUser.setLdapLogin("kowalLDP");
        firstUser.setTableSize(20);
        firstUser.setCss("/css/fancy.css");
        serviceManager.getUserService().save(firstUser);

        User secondUser = new User();
        secondUser.setName("Adam");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("nowak");
        secondUser.setLdapLogin("nowakLDP");
        secondUser.setSessionTimeout(9000);
        secondUser.setLdapGroup(serviceManager.getLdapGroupService().find(1));
        serviceManager.getUserService().save(secondUser);

        User thirdUser = new User();
        thirdUser.setName("Anna");
        thirdUser.setSurname("Dora");
        thirdUser.setLogin("dora");
        thirdUser.setLdapLogin("doraLDP");
        thirdUser.setActive(false);
        serviceManager.getUserService().save(thirdUser);
    }

    private static void insertUserGroups() throws DAOException, IOException, ResponseException {
        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setTitle("Admin");
        firstUserGroup.setPermission(1);
        List<User> users = new ArrayList<>();
        User user = serviceManager.getUserService().find(1);
        List<UserGroup> userGroups = new ArrayList<>();
        userGroups.add(firstUserGroup);
        user.setUserGroups(userGroups);
        users.add(serviceManager.getUserService().find(1));
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

    public static void insertUserProperties() throws DAOException, IOException, ResponseException {
        User user = serviceManager.getUserService().find(1);

        UserProperty firstUserProperty = new UserProperty();
        firstUserProperty.setTitle("First Property");
        firstUserProperty.setValue("first value");
        firstUserProperty.setObligatory(true);
        firstUserProperty.setType(PropertyType.general);
        firstUserProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 14);
        firstUserProperty.setCreationDate(localDate.toDate());
        firstUserProperty.setContainer(1);
        firstUserProperty.setUser(user);
        serviceManager.getUserPropertyService().save(firstUserProperty);

        UserProperty secondUserProperty = new UserProperty();
        secondUserProperty.setTitle("secondProperty");
        secondUserProperty.setValue("second");
        secondUserProperty.setObligatory(false);
        secondUserProperty.setType(PropertyType.CommandLink);
        secondUserProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 15);
        secondUserProperty.setCreationDate(localDate.toDate());
        secondUserProperty.setContainer(2);
        secondUserProperty.setUser(user);
        serviceManager.getUserPropertyService().save(secondUserProperty);

        user.getProperties().add(firstUserProperty);
        user.getProperties().add(secondUserProperty);
        serviceManager.getUserService().save(user);
    }

    private static void insertWorkpieces() throws DAOException, IOException, ResponseException {
        Process process = serviceManager.getProcessService().find(1);

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

    private static void insertWorkpieceProperties() throws DAOException, IOException, ResponseException {
        Workpiece workpiece = serviceManager.getWorkpieceService().find(1);

        WorkpieceProperty firstWorkpieceProperty = new WorkpieceProperty();
        firstWorkpieceProperty.setTitle("First Property");
        firstWorkpieceProperty.setValue("first value");
        firstWorkpieceProperty.setObligatory(true);
        firstWorkpieceProperty.setType(PropertyType.general);
        firstWorkpieceProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017, 1, 13);
        firstWorkpieceProperty.setCreationDate(localDate.toDate());
        firstWorkpieceProperty.setContainer(1);
        firstWorkpieceProperty.setWorkpiece(workpiece);
        serviceManager.getWorkpiecePropertyService().save(firstWorkpieceProperty);

        WorkpieceProperty secondWorkpieceProperty = new WorkpieceProperty();
        secondWorkpieceProperty.setTitle("workpiece");
        secondWorkpieceProperty.setValue("second");
        secondWorkpieceProperty.setObligatory(false);
        secondWorkpieceProperty.setType(PropertyType.CommandLink);
        secondWorkpieceProperty.setChoice("chosen");
        localDate = new LocalDate(2017, 1, 14);
        secondWorkpieceProperty.setCreationDate(localDate.toDate());
        secondWorkpieceProperty.setContainer(2);
        secondWorkpieceProperty.setWorkpiece(workpiece);
        serviceManager.getWorkpiecePropertyService().save(secondWorkpieceProperty);

        workpiece.getProperties().add(firstWorkpieceProperty);
        workpiece.getProperties().add(secondWorkpieceProperty);
        serviceManager.getWorkpieceService().save(workpiece);
    }

    // TODO: find out why this method doesn't clean database after every test's
    // class
    public static void cleanDatabase() {
        Session session = Helper.getHibernateSession();
        session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        session.createQuery("DELETE FROM History WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM User WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Process WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Project WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Workpiece WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Batch WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM LdapGroup WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM User WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Docket WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM ProcessProperty WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM ProjectFileGroup WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Ruleset WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Task WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM Template WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM TemplateProperty WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM UserGroup WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM UserProperty WHERE id !=null").executeUpdate();
        session.createQuery("DELETE FROM WorkpieceProperty WHERE id !=null").executeUpdate();
        // session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    public static void cleanIndex() throws IOException, ResponseException {
        IndexRestClient restClient = new IndexRestClient();
        restClient.initiateClient();
        restClient.deleteIndex();
    }
}
