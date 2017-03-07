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

import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.helper.Helper;
import org.hibernate.Session;
import org.joda.time.LocalDate;
import org.kitodo.data.database.beans.*;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryType;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.*;

import static org.kitodo.data.database.beans.Batch.Type.*;

/**
 * Insert data to test database.
 */
public class MockDatabase {

    public static void insertProcessesFull() throws DAOException {
        BatchService batchService = new BatchService();
        if(batchService.find(1) == null) {
            System.out.println(batchService.find(1));
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

    private static void insertBatches() throws DAOException {
        BatchService batchService = new BatchService();

        Batch firstBatch = new Batch();
        firstBatch.setTitle("First batch");
        firstBatch.setType(LOGISTIC);
        batchService.save(firstBatch);

        Batch secondBatch = new Batch();
        secondBatch.setTitle("Second batch");
        secondBatch.setType(LOGISTIC);
        batchService.save(secondBatch);

        Batch thirdBatch = new Batch();
        thirdBatch.setTitle("Third batch");
        thirdBatch.setType(NEWSPAPER);
        batchService.save(thirdBatch);

        Batch fourthBatch = new Batch();
        fourthBatch.setType(SERIAL);
        batchService.save(fourthBatch);
    }

    public static void insertDockets() throws DAOException {
        DocketService docketService = new DocketService();

        Docket firstDocket = new Docket();
        firstDocket.setName("default");
        firstDocket.setFile("docket.xsl");
        docketService.save(firstDocket);

        Docket secondDocket = new Docket();
        secondDocket.setName("second");
        secondDocket.setFile("docket.xsl");
        docketService.save(secondDocket);
    }

    private static void insertHistory() throws DAOException {
        HistoryService historyService = new HistoryService();
        ProcessService processService = new ProcessService();

        History firstHistory = new History();
        Process firstProcess = processService.find(1);
        firstHistory.setNumericValue(2.0);
        firstHistory.setStringValue("History");
        firstHistory.setHistoryType(HistoryType.color);
        LocalDate localDate = new LocalDate(2017,1,14);
        firstHistory.setDate(localDate.toDate());
        firstHistory.setProcess(firstProcess);
        historyService.save(firstHistory);
        firstProcess.getHistory().add(firstHistory);
        processService.save(firstProcess);
    }

    public static void insertLdapGroups() throws DAOException {
        LdapGroupService ldapGroupService = new LdapGroupService();

        LdapGroup firstLdapGroup = new LdapGroup();
        firstLdapGroup.setTitle("LG");
        firstLdapGroup.setHomeDirectory("..//test_directory/");
        firstLdapGroup.setDescription("Test LDAP group");
        firstLdapGroup.setDisplayName("Name");
        ldapGroupService.save(firstLdapGroup);
    }

    private static void insertProcesses() throws DAOException {
        BatchService batchService = new BatchService();
        DocketService docketService = new DocketService();
        ProcessService processService = new ProcessService();
        ProjectService projectService = new ProjectService();
        RulesetService rulesetService = new RulesetService();

        Process firstProcess = new Process();
        firstProcess.setTitle("First process");
        firstProcess.setOutputName("Test");
        firstProcess.setWikiField("wiki");
        LocalDate localDate = new LocalDate(2016,10,20);
        firstProcess.setCreationDate(localDate.toDate());

        List<Batch> batches = new ArrayList<>();
        Batch firstBatch = batchService.find(1);
        Batch secondBatch = batchService.find(3);
        List<Process> processes = new ArrayList<>();
        processes.add(firstProcess);
        firstBatch.setProcesses(processes);
        secondBatch.setProcesses(processes);
        batches.add(firstBatch);
        batches.add(secondBatch);
        firstProcess.setBatches(batches);

        firstProcess.setDocket(docketService.find(1));
        firstProcess.setProject(projectService.find(1));
        firstProcess.setRuleset(rulesetService.find(1));
        processService.save(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setTitle("Second process");
        secondProcess.setOutputName("Testowy");
        secondProcess.setWikiField("field");
        localDate = new LocalDate(2017,1,20);
        secondProcess.setCreationDate(localDate.toDate());
        secondProcess.setDocket(docketService.find(1));
        secondProcess.setProject(projectService.find(1));
        secondProcess.setRuleset(rulesetService.find(1));
        processService.save(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setTitle("Second process");
        thirdProcess.setOutputName("Unreachable");
        thirdProcess.setWikiField("problem");
        localDate = new LocalDate(2017,2,10);
        thirdProcess.setCreationDate(localDate.toDate());
        thirdProcess.setDocket(docketService.find(1));
        thirdProcess.setProject(projectService.find(1));
        thirdProcess.setRuleset(rulesetService.find(1));
        processService.save(thirdProcess);
    }

    private static void insertProcessProperties() throws DAOException {
        ProcessService processService = new ProcessService();
        ProcessPropertyService processPropertyService = new ProcessPropertyService();

        ProcessProperty firstProcessProperty = new ProcessProperty();
        Process process = processService.find(1);
        firstProcessProperty.setTitle("First Property");
        firstProcessProperty.setValue("first value");
        firstProcessProperty.setObligatory(true);
        firstProcessProperty.setType(PropertyType.general);
        firstProcessProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017,1,14);
        firstProcessProperty.setCreationDate(localDate.toDate());
        firstProcessProperty.setContainer(1);
        firstProcessProperty.setProcess(process);
        processPropertyService.save(firstProcessProperty);

        ProcessProperty secondProcessProperty = new ProcessProperty();
        secondProcessProperty.setTitle("secondProperty");
        secondProcessProperty.setValue("second");
        secondProcessProperty.setObligatory(false);
        secondProcessProperty.setType(PropertyType.CommandLink);
        secondProcessProperty.setChoice("chosen");
        localDate = new LocalDate(2017,1,15);
        secondProcessProperty.setCreationDate(localDate.toDate());
        secondProcessProperty.setContainer(2);
        secondProcessProperty.setProcess(process);
        processPropertyService.save(secondProcessProperty);

        process.getProperties().add(firstProcessProperty);
        process.getProperties().add(secondProcessProperty);
        processService.save(process);
    }

    private static void insertProjects() throws DAOException {
        ProjectService projectService = new ProjectService();
        UserService userService = new UserService();

        User firstUser = userService.find(1);
        User secondUser = userService.find(2);

        Project firstProject = new Project();
        firstProject.setTitle("First project");
        firstProject.setUseDmsImport(true);
        LocalDate localDate = new LocalDate(2016,10,20);
        firstProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017,10,20);
        firstProject.setEndDate(localDate.toDate());
        firstProject.setNumberOfPages(30);
        firstProject.setNumberOfVolumes(2);
        firstProject.getUsers().add(firstUser);
        firstProject.getUsers().add(secondUser);
        projectService.save(firstProject);

        Project secondProject = new Project();
        secondProject.setTitle("Second project");
        secondProject.setUseDmsImport(false);
        localDate = new LocalDate(2016,11,10);
        secondProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2017,9,15);
        secondProject.setEndDate(localDate.toDate());
        secondProject.setNumberOfPages(80);
        secondProject.setNumberOfVolumes(4);
        secondProject.getUsers().add(firstUser);
        projectService.save(secondProject);

        firstUser.getProjects().add(firstProject);
        firstUser.getProjects().add(secondProject);
        secondUser.getProjects().add(firstProject);
        userService.save(firstUser);
        userService.save(secondUser);

        Project thirdProject = new Project();
        thirdProject.setTitle("Archived project");
        localDate = new LocalDate(2014,11,10);
        thirdProject.setStartDate(localDate.toDate());
        localDate = new LocalDate(2016,9,15);
        thirdProject.setEndDate(localDate.toDate());
        thirdProject.setNumberOfPages(160);
        thirdProject.setNumberOfVolumes(5);
        thirdProject.setProjectIsArchived(true);
        projectService.save(thirdProject);
    }

    private static void insertProjectFileGroups() throws DAOException {
        ProjectService projectService = new ProjectService();
        ProjectFileGroupService projectFileGroupService = new ProjectFileGroupService();

        Project project = projectService.find(1);

        ProjectFileGroup firstProjectFileGroup = new ProjectFileGroup();
        firstProjectFileGroup.setName("MAX");
        firstProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstProjectFileGroup.setMimeType("image/jpeg");
        firstProjectFileGroup.setSuffix("jpg");
        firstProjectFileGroup.setPreviewImage(false);
        firstProjectFileGroup.setProject(project);
        projectFileGroupService.save(firstProjectFileGroup);

        ProjectFileGroup secondProjectFileGroup = new ProjectFileGroup();
        secondProjectFileGroup.setName("DEFAULT");
        secondProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondProjectFileGroup.setMimeType("image/jpeg");
        secondProjectFileGroup.setSuffix("jpg");
        secondProjectFileGroup.setPreviewImage(false);
        secondProjectFileGroup.setProject(project);
        projectFileGroupService.save(secondProjectFileGroup);

        ProjectFileGroup thirdProjectFileGroup = new ProjectFileGroup();
        thirdProjectFileGroup.setName("THUMBS");
        thirdProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/");
        thirdProjectFileGroup.setMimeType("image/jpeg");
        thirdProjectFileGroup.setSuffix("jpg");
        thirdProjectFileGroup.setPreviewImage(false);
        thirdProjectFileGroup.setProject(project);
        projectFileGroupService.save(thirdProjectFileGroup);

        ProjectFileGroup fourthProjectFileGroup = new ProjectFileGroup();
        fourthProjectFileGroup.setName("FULLTEXT");
        fourthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/");
        fourthProjectFileGroup.setMimeType("text/xml");
        fourthProjectFileGroup.setSuffix("xml");
        fourthProjectFileGroup.setPreviewImage(false);
        fourthProjectFileGroup.setProject(project);
        projectFileGroupService.save(fourthProjectFileGroup);

        ProjectFileGroup fifthProjectFileGroup = new ProjectFileGroup();
        fifthProjectFileGroup.setName("DOWNLOAD");
        fifthProjectFileGroup.setPath("http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/");
        fifthProjectFileGroup.setMimeType("application/pdf");
        fifthProjectFileGroup.setSuffix("pdf");
        fifthProjectFileGroup.setPreviewImage(false);
        fifthProjectFileGroup.setProject(project);
        projectFileGroupService.save(fifthProjectFileGroup);

        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);
        project.getProjectFileGroups().add(fifthProjectFileGroup);

        projectService.save(project);
    }

    public static void insertRulesets() throws DAOException {
        RulesetService rulesetService = new RulesetService();

        Ruleset firstRuleset = new Ruleset();
        firstRuleset.setTitle("SLUBDD");
        firstRuleset.setFile("ruleset_slubdd.xml");
        firstRuleset.setOrderMetadataByRuleset(false);
        rulesetService.save(firstRuleset);

        Ruleset secondRuleset = new Ruleset();
        secondRuleset.setTitle("SLUBHH");
        secondRuleset.setFile("ruleset_slubhh.xml");
        secondRuleset.setOrderMetadataByRuleset(false);
        rulesetService.save(secondRuleset);
    }

    private static void insertTasks() throws DAOException {
        ProcessService processService = new ProcessService();
        TaskService taskService = new TaskService();
        UserService userService = new UserService();
        UserGroupService userGroupService = new UserGroupService();

        Task firstTask = new Task();
        Process firstProcess = processService.find(1);
        UserGroup userGroup = userGroupService.find(1);
        firstTask.setTitle("Testing");
        firstTask.setPriority(1);
        firstTask.setOrdering(1);
        firstTask.setEditTypeEnum(TaskEditType.ADMIN);
        LocalDate localDate = new LocalDate(2016,10,20);
        firstTask.setProcessingBegin(localDate.toDate());
        localDate = new LocalDate(2016,12,24);
        firstTask.setProcessingEnd(localDate.toDate());
        User firstUser = userService.find(1);
        firstTask.setProcessingUser(firstUser);
        firstTask.setProcessingStatusEnum(TaskStatus.OPEN);
        firstTask.setProcess(firstProcess);
        firstTask.setUsers(userService.findAll());
        firstTask.getUserGroups().add(userGroup);
        firstProcess.getTasks().add(firstTask);
        processService.save(firstProcess);
        firstUser.getProcessingTasks().add(firstTask);
        userService.save(firstUser);

        Process secondProcess = processService.find(2);
        User blockedUser = userService.find(3);
        User secondUser = userService.find(2);

        Task secondTask = new Task();
        secondTask.setTitle("Blocking");
        secondTask = taskService.setCorrectionStep(secondTask);
        secondTask.setOrdering(2);
        secondTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        localDate = new LocalDate(2016,9,25);
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
        localDate = new LocalDate(2017,1,25);
        thirdTask.setProcessingBegin(localDate.toDate());
        thirdTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        thirdTask.setProcess(secondProcess);
        thirdTask.getUsers().add(secondUser);

        Task fourthTask = new Task();
        fourthTask.setTitle("Progress");
        fourthTask.setOrdering(4);
        fourthTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        fourthTask.setTypeImagesWrite(true);
        localDate = new LocalDate(2017,1,29);
        fourthTask.setProcessingBegin(localDate.toDate());
        fourthTask.setProcessingStatusEnum(TaskStatus.INWORK);
        fourthTask.setProcessingUser(userService.find(2));
        fourthTask.setProcess(secondProcess);
        fourthTask.setUsers(userService.findAll());

        secondProcess.getTasks().add(secondTask);
        secondProcess.getTasks().add(thirdTask);
        secondProcess.getTasks().add(fourthTask);
        System.out.println("tasks2 " + secondProcess.getTasks().size());
        processService.save(secondProcess);

        blockedUser.getProcessingTasks().add(secondTask);
        blockedUser.getTasks().add(secondTask);
        secondUser.getTasks().add(secondTask);
        secondUser.getTasks().add(thirdTask);
        userService.save(blockedUser);
        userService.save(secondUser);

        userGroup.getTasks().add(firstTask);
        userGroup.getTasks().add(secondTask);
        userGroupService.save(userGroup);
    }

    private static void insertTemplates() throws DAOException {
        ProcessService processService = new ProcessService();
        TemplateService templateService = new TemplateService();

        Process process = processService.find(1);

        Template firstTemplate = new Template();
        firstTemplate.setProcess(process);
        firstTemplate.setOrigin("test");
        templateService.save(firstTemplate);

        Template secondTemplate = new Template();
        secondTemplate.setProcess(process);
        secondTemplate.setOrigin("addition");
        templateService.save(secondTemplate);

        process.getTemplates().add(firstTemplate);
        process.getTemplates().add(secondTemplate);
        processService.save(process);
    }

    private static void insertTemplateProperties() throws DAOException {
        TemplateService templateService = new TemplateService();
        TemplatePropertyService templatePropertyService = new TemplatePropertyService();

        Template template = templateService.find(1);

        TemplateProperty firstTemplateProperty = new TemplateProperty();
        firstTemplateProperty.setTitle("first title");
        firstTemplateProperty.setValue("first value");
        firstTemplateProperty.setObligatory(true);
        firstTemplateProperty.setType(PropertyType.general);
        firstTemplateProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017,1,14);
        firstTemplateProperty.setCreationDate(localDate.toDate());
        firstTemplateProperty.setContainer(1);
        firstTemplateProperty.setTemplate(template);
        templatePropertyService.save(firstTemplateProperty);

        TemplateProperty secondTemplateProperty = new TemplateProperty();
        secondTemplateProperty.setTitle("template");
        secondTemplateProperty.setValue("second");
        secondTemplateProperty.setObligatory(false);
        secondTemplateProperty.setType(PropertyType.CommandLink);
        secondTemplateProperty.setChoice("chosen");
        localDate = new LocalDate(2017,1,15);
        secondTemplateProperty.setCreationDate(localDate.toDate());
        secondTemplateProperty.setContainer(2);
        secondTemplateProperty.setTemplate(template);
        templatePropertyService.save(secondTemplateProperty);

        template.getProperties().add(firstTemplateProperty);
        template.getProperties().add(secondTemplateProperty);
        templateService.save(template);
    }

    private static void insertUsers() throws DAOException {
        LdapGroupService ldapGroupService = new LdapGroupService();
        UserService userService = new UserService();

        User firstUser = new User();
        firstUser.setName("Jan");
        firstUser.setSurname("Kowalski");
        firstUser.setLogin("kowal");
        firstUser.setPassword("test");
        firstUser.setLdapLogin("kowalLDP");
        firstUser.setTableSize(20);
        firstUser.setCss("/css/fancy.css");
        userService.save(firstUser);

        User secondUser = new User();
        secondUser.setName("Adam");
        secondUser.setSurname("Nowak");
        secondUser.setLogin("nowak");
        secondUser.setLdapLogin("nowakLDP");
        secondUser.setSessionTimeout(9000);
        secondUser.setLdapGroup(ldapGroupService.find(1));
        userService.save(secondUser);

        User thirdUser = new User();
        thirdUser.setName("Anna");
        thirdUser.setSurname("Dora");
        thirdUser.setLogin("dora");
        thirdUser.setLdapLogin("doraLDP");
        thirdUser.setActive(false);
        userService.save(thirdUser);
    }

    private static void insertUserGroups() throws DAOException {
        UserService userService = new UserService();
        UserGroupService userGroupService = new UserGroupService();

        UserGroup firstUserGroup = new UserGroup();
        firstUserGroup.setTitle("Admin");
        firstUserGroup.setPermission(1);
        List<User> users = new ArrayList<>();
        User user = userService.find(1);
        List<UserGroup> userGroups = new ArrayList<>();
        userGroups.add(firstUserGroup);
        user.setUserGroups(userGroups);
        users.add(userService.find(1));
        firstUserGroup.setUsers(users);
        userGroupService.save(firstUserGroup);

        UserGroup secondUserGroup = new UserGroup();
        secondUserGroup.setTitle("Random");
        secondUserGroup.setPermission(2);
        userGroupService.save(secondUserGroup);

        UserGroup thirdUserGroup = new UserGroup();
        thirdUserGroup.setTitle("Without permission");
        userGroupService.save(thirdUserGroup);
    }

    public static void insertUserProperties() throws DAOException {
        UserService userService = new UserService();
        UserPropertyService userPropertyService = new UserPropertyService();

        User user = userService.find(1);

        UserProperty firstUserProperty = new UserProperty();
        firstUserProperty.setTitle("First Property");
        firstUserProperty.setValue("first value");
        firstUserProperty.setObligatory(true);
        firstUserProperty.setType(PropertyType.general);
        firstUserProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017,1,14);
        firstUserProperty.setCreationDate(localDate.toDate());
        firstUserProperty.setContainer(1);
        firstUserProperty.setUser(user);
        userPropertyService.save(firstUserProperty);

        UserProperty secondUserProperty = new UserProperty();
        secondUserProperty.setTitle("secondProperty");
        secondUserProperty.setValue("second");
        secondUserProperty.setObligatory(false);
        secondUserProperty.setType(PropertyType.CommandLink);
        secondUserProperty.setChoice("chosen");
        localDate = new LocalDate(2017,1,15);
        secondUserProperty.setCreationDate(localDate.toDate());
        secondUserProperty.setContainer(2);
        secondUserProperty.setUser(user);
        userPropertyService.save(secondUserProperty);

        user.getProperties().add(firstUserProperty);
        user.getProperties().add(secondUserProperty);
        userService.save(user);
    }

    public static void insertWorkpieces() throws DAOException {
        ProcessService processService = new ProcessService();
        WorkpieceService workpieceService = new WorkpieceService();

        Process process = processService.find(1);

        Workpiece firstWorkpiece = new Workpiece();
        firstWorkpiece.setProcess(process);
        workpieceService.save(firstWorkpiece);

        Workpiece secondWorkpiece = new Workpiece();
        secondWorkpiece.setProcess(process);
        workpieceService.save(secondWorkpiece);

        process.getWorkpieces().add(firstWorkpiece);
        process.getWorkpieces().add(secondWorkpiece);
        processService.save(process);
    }

    public static void insertWorkpieceProperties() throws DAOException {
        WorkpieceService workpieceService = new WorkpieceService();
        WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();

        Workpiece workpiece = workpieceService.find(1);

        WorkpieceProperty firstWorkpieceProperty = new WorkpieceProperty();
        firstWorkpieceProperty.setTitle("First Property");
        firstWorkpieceProperty.setValue("first value");
        firstWorkpieceProperty.setObligatory(true);
        firstWorkpieceProperty.setType(PropertyType.general);
        firstWorkpieceProperty.setChoice("choice");
        LocalDate localDate = new LocalDate(2017,1,13);
        firstWorkpieceProperty.setCreationDate(localDate.toDate());
        firstWorkpieceProperty.setContainer(1);
        firstWorkpieceProperty.setWorkpiece(workpiece);
        workpiecePropertyService.save(firstWorkpieceProperty);

        WorkpieceProperty secondWorkpieceProperty = new WorkpieceProperty();
        secondWorkpieceProperty.setTitle("workpiece");
        secondWorkpieceProperty.setValue("second");
        secondWorkpieceProperty.setObligatory(false);
        secondWorkpieceProperty.setType(PropertyType.CommandLink);
        secondWorkpieceProperty.setChoice("chosen");
        localDate = new LocalDate(2017,1,14);
        secondWorkpieceProperty.setCreationDate(localDate.toDate());
        secondWorkpieceProperty.setContainer(2);
        secondWorkpieceProperty.setWorkpiece(workpiece);
        workpiecePropertyService.save(secondWorkpieceProperty);

        workpiece.getProperties().add(firstWorkpieceProperty);
        workpiece.getProperties().add(secondWorkpieceProperty);
        workpieceService.save(workpiece);
    }

    //TODO: find out why this method doesn't clean database after every test's class
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
        //session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
