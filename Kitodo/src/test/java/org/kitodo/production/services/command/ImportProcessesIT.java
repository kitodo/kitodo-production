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

package org.kitodo.production.services.command;

// abbreviations
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// base Java
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

// open source code
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;

public class ImportProcessesIT {
    private static final Path ERRORS_DIR_PATH = Paths.get("src/test/resources/errors");

    private int firstProcessId, secondProcessId, thirdProcessId;
    private static Template template;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        // add ruleset
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("ImportProcessesIT");
        ruleset.setFile("ImportProcessesIT.xml");
        ruleset.setOrderMetadataByRuleset(true);
        Client clientOne = ServiceManager.getClientService().getById(1);
        ruleset.setClient(clientOne);
        ServiceManager.getRulesetService().save(ruleset);

        Task task = new Task();
        task.getRoles().add(ServiceManager.getRoleService().getById(1));
        ServiceManager.getTaskService().save(task);

        template = new Template();
        template.setTitle("Import processes template");
        LocalDate localDate = LocalDate.of(2023, 8, 9);
        template.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        template.setClient(clientOne);
        template.setDocket(ServiceManager.getDocketService().getById(1));
        template.getProjects().add(ServiceManager.getProjectService().getById(1));
        template.setRuleset(ruleset);
        ServiceManager.getTemplateService().save(template, true);

        task.setTemplate(template);
        template.getTasks().add(task);
        ServiceManager.getTemplateService().save(template, true);
        ServiceManager.getTaskService().save(task);

        Folder local = ServiceManager.getFolderService().getById(6);
        local.setMimeType("image/jpeg");
        ServiceManager.getFolderService().saveToDatabase(local);

        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
    }

    @BeforeClass
    public static void setScriptPermission() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission
                    .setExecutePermission(new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META)));
        }
    }

    @Before
    public void createOutputDirectories() throws Exception {
        Files.createDirectories(ERRORS_DIR_PATH);
    }

    /**
     * Tests the target behavior specified.
     */
    @Test
    public void shouldImport() throws Exception {
        // create test object
        String indir = "src/test/resources/ImportProcessesIT";
        String projectId = "1";
        String templateId = template.getId().toString();
        String errors = "src/test/resources/errors";
        ImportProcesses underTest = new ImportProcesses(indir, projectId, templateId, errors);

        int processesBefore = ServiceManager.getProcessService().countDatabaseRows().intValue();

        // initialize
        underTest.run(0);
        assertEquals("should require 43 actions", 43, underTest.totalActions);

        // validate: correct processes
        underTest.run(1);
        assertEquals("should have validated p1_valid", "p1_valid", underTest.validatingImportingProcess.directoryName);
        ImportingProcess p1_valid = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p1_valid.errors.isEmpty());

        underTest.run(2);
        assertEquals("should have validated p1c1_valid", "p1c1_valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p1c1_valid = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p1c1_valid.errors.isEmpty());

        underTest.run(3);
        assertEquals("should have validated p1c2_valid", "p1c2_valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p1c2_valid = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p1c2_valid.errors.isEmpty());

        assertTrue("p1_valid should be correct", p1_valid.isCorrect());
        assertTrue("p1c1_valid should be correct", p1c1_valid.isCorrect());
        assertTrue("p1c2_valid should be correct", p1c2_valid.isCorrect());

        // validate: error case 'parent is missing a child'
        underTest.run(4);
        assertEquals("should have validated p2_parentMissingAChild", "p2_parentMissingAChild",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p2_parentMissingAChild = underTest.validatingImportingProcess;
        assertFalse("should have validated with error", p2_parentMissingAChild.errors.isEmpty());

        underTest.run(5);
        assertEquals("should have validated p2c1_valid", "p2c1_valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p2c1_valid = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p2c1_valid.errors.isEmpty());

        assertFalse("p1_valid should not be correct", p2_parentMissingAChild.isCorrect());
        assertFalse("p1c1_valid should not be correct, due to problem in parent case", p2c1_valid.isCorrect());

        // validate: error case 'parent not valid'
        underTest.run(6);
        assertEquals("should have validated p3_not-valid", "p3_not-valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p3_notValid = underTest.validatingImportingProcess;
        assertFalse("should have validated with error", p3_notValid.errors.isEmpty());

        underTest.run(7);
        assertEquals("should have validated p3c1_valid", "p3c1_valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p3c1_valid = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p3c1_valid.errors.isEmpty());

        assertFalse("p1_valid should not be correct", p3_notValid.isCorrect());
        assertFalse("p1c1_valid should not be correct, due to problem in parent case", p3c1_valid.isCorrect());

        // validate: error case 'child not valid'
        underTest.run(8);
        assertEquals("should have validated p4_valid_butChildIsNot", "p4_valid_butChildIsNot",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p4_valid_butChildIsNot = underTest.validatingImportingProcess;
        assertTrue("should have validated without errors", p4_valid_butChildIsNot.errors.isEmpty());

        underTest.run(9);
        assertEquals("should have validated p4c1_not-valid", "p4c1_not-valid",
            underTest.validatingImportingProcess.directoryName);
        ImportingProcess p4c1_notValid = underTest.validatingImportingProcess;
        assertFalse("should have validated with error", p4c1_notValid.errors.isEmpty());

        assertFalse("p1_valid should not be correct, due to problem in child case", p4_valid_butChildIsNot.isCorrect());
        assertFalse("p1c1_valid should not be correct", p4c1_notValid.isCorrect());

        // copy files and create database entry

        // p1c1_valid (OK)
        firstProcessId = processesBefore + 1;
        Path processPath = Paths.get("src/test/resources/metadata", Integer.toString(firstProcessId));
        Path imagesPath = processPath.resolve("images");
        Path mediaPath = imagesPath.resolve("17_123_0001_media");
        Path imageOne = mediaPath.resolve("00000001.jpg");
        Path metaXml = processPath.resolve("meta.xml");

        underTest.run(10);
        assertEquals("should have created 1st process,", Long.valueOf(firstProcessId),
            ServiceManager.getProcessService().countDatabaseRows());
        underTest.run(11);
        assertTrue("should have created process directory", Files.isDirectory(processPath));
        underTest.run(12);
        underTest.run(13);
        underTest.run(14);
        underTest.run(15);

        assertTrue("should have created images directory", Files.isDirectory(imagesPath));
        assertTrue("should have created media directory", Files.isDirectory(mediaPath));
        assertTrue("should have copied media file", Files.exists(imageOne));
        assertTrue("should have written meta.xml file", Files.exists(metaXml));
        assertTrue("should have added image to meta.xml file",
            Files.readString(metaXml, UTF_8).contains("xlink:href=\"images/17_123_0001_media/00000001.jpg\""));

        // p1c2_valid (OK)
        secondProcessId = processesBefore + 2;
        processPath = Paths.get("src/test/resources/metadata", Integer.toString(secondProcessId));
        metaXml = processPath.resolve("meta.xml");

        underTest.run(16);
        underTest.run(17);
        underTest.run(18);
        underTest.run(19);
        underTest.run(20);
        underTest.run(21);
        assertTrue("should have added image to meta.xml file",
            Files.readString(metaXml, UTF_8).contains("xlink:href=\"images/17_123_0002_media/00000001.jpg\""));

        // p2c1_valid (error, broken parent)
        underTest.run(22);
        assertTrue("should have error directory", Files.isDirectory(Paths.get("src/test/resources/errors/p2c1_valid")));
        underTest.run(23);
        assertTrue("should have written Errors.txt file",
            Files.exists(Paths.get("src/test/resources/errors/p2c1_valid/Errors.txt")));
        assertTrue("should have written error message",
            Files.readString(Paths.get("src/test/resources/errors/p2c1_valid/Errors.txt"), UTF_8)
                    .contains("errors in related process(es): p2_parentMissingAChild"));
        underTest.run(24);
        assertTrue("should have copied meta.xml file",
            Files.exists(Paths.get("src/test/resources/errors/p2c1_valid/meta.xml")));

        // p3c1_valid (error, broken parent)
        underTest.run(25);
        underTest.run(26);
        assertTrue("should have written error message",
            Files.readString(Paths.get("src/test/resources/errors/p3c1_valid/Errors.txt"), UTF_8)
                    .contains("errors in related process(es): p3_not-valid"));
        underTest.run(27);
        assertTrue("should have copied meta.xml file",
            Files.exists(Paths.get("src/test/resources/errors/p3c1_valid/meta.xml")));

        // p4c1_not-valid (error, not valid)
        underTest.run(28);
        underTest.run(29);
        assertTrue("should have written error message",
            Files.readString(Paths.get("src/test/resources/errors/p4c1_not-valid/Errors.txt"), UTF_8)
                    .contains("Validation error"));
        underTest.run(30);

        // p1_valid (OK)
        thirdProcessId = processesBefore + 3;
        processPath = Paths.get("src/test/resources/metadata", Integer.toString(thirdProcessId));
        metaXml = processPath.resolve("meta.xml");

        underTest.run(31);
        assertEquals("should have created 3rd process,", processesBefore + 3,
            (long) ServiceManager.getProcessService().countDatabaseRows());
        underTest.run(32);
        assertTrue("should have created process directory", Files.isDirectory(processPath));
        underTest.run(33);
        String thirdMetaXml = Files.readString(metaXml, UTF_8);
        assertThat("should have added correct child links to meta.xml file", thirdMetaXml,
            containsString("xlink:href=\"database://?process.id=" + firstProcessId + "\""));
        assertThat("should have added correct child links to meta.xml file", thirdMetaXml,
            containsString("xlink:href=\"database://?process.id=" + secondProcessId + "\""));

        Process parent = ServiceManager.getProcessService().getById(6);
        assertEquals("parent should have 2 children", 2, parent.getChildren().size());
        assertEquals("child (ID " + firstProcessId + ") should have the correct parent", parent,
            ServiceManager.getProcessService().getById(firstProcessId).getParent());
        assertEquals("child (ID " + secondProcessId + ") should have the correct parent", parent,
            ServiceManager.getProcessService().getById(secondProcessId).getParent());

        // p2_parentMissingAChild (error)
        underTest.run(34);
        underTest.run(35);
        underTest.run(36);

        // p3_not-valid (error)
        underTest.run(37);
        underTest.run(38);
        underTest.run(39);

        // p4_valid_butChildIsNot (error)
        underTest.run(40);
        underTest.run(41);
        underTest.run(42);

        // import results
        assertEquals("Should import 3 processes,", processesBefore + 3,
            (long) ServiceManager.getProcessService().countDatabaseRows());
        assertEquals("Should not import 6 processes,", 6, ERRORS_DIR_PATH.toFile().list().length);
    }

    @After
    public void deleteCreatedFiles() throws Exception {
        ProcessTestUtils.removeTestProcess(firstProcessId);
        ProcessTestUtils.removeTestProcess(secondProcessId);
        ProcessTestUtils.removeTestProcess(thirdProcessId);
        TreeDeleter.deltree(ERRORS_DIR_PATH);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }
}
