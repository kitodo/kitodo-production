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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
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
        ServiceManager.getTemplateService().save(template);

        task.setTemplate(template);
        template.getTasks().add(task);
        ServiceManager.getTemplateService().save(template);
        ServiceManager.getTaskService().save(task);

        Folder local = ServiceManager.getFolderService().getById(6);
        local.setMimeType("image/jpeg");
        ServiceManager.getFolderService().save(local);

        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
    }

    @BeforeAll
    public static void setScriptPermission() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission
                    .setExecutePermission(new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META)));
        }
    }

    @BeforeEach
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

        int processesBefore = ServiceManager.getProcessService().count().intValue();

        // initialize
        underTest.run(0);
        assertEquals(43, underTest.totalActions, "should require 43 actions");

        // validate: correct processes
        underTest.run(1);
        assertEquals("p1_valid", underTest.validatingImportingProcess.directoryName, "should have validated p1_valid");
        ImportingProcess p1_valid = underTest.validatingImportingProcess;
        assertTrue(p1_valid.errors.isEmpty(), "should have validated without errors");

        underTest.run(2);
        assertEquals("p1c1_valid", underTest.validatingImportingProcess.directoryName, "should have validated p1c1_valid");
        ImportingProcess p1c1_valid = underTest.validatingImportingProcess;
        assertTrue(p1c1_valid.errors.isEmpty(), "should have validated without errors");

        underTest.run(3);
        assertEquals("p1c2_valid", underTest.validatingImportingProcess.directoryName, "should have validated p1c2_valid");
        ImportingProcess p1c2_valid = underTest.validatingImportingProcess;
        assertTrue(p1c2_valid.errors.isEmpty(), "should have validated without errors");

        assertTrue(p1_valid.isCorrect(), "p1_valid should be correct");
        assertTrue(p1c1_valid.isCorrect(), "p1c1_valid should be correct");
        assertTrue(p1c2_valid.isCorrect(), "p1c2_valid should be correct");

        // validate: error case 'parent is missing a child'
        underTest.run(4);
        assertEquals("p2_parentMissingAChild", underTest.validatingImportingProcess.directoryName, "should have validated p2_parentMissingAChild");
        ImportingProcess p2_parentMissingAChild = underTest.validatingImportingProcess;
        assertFalse(p2_parentMissingAChild.errors.isEmpty(), "should have validated with error");

        underTest.run(5);
        assertEquals("p2c1_valid", underTest.validatingImportingProcess.directoryName, "should have validated p2c1_valid");
        ImportingProcess p2c1_valid = underTest.validatingImportingProcess;
        assertTrue(p2c1_valid.errors.isEmpty(), "should have validated without errors");

        assertFalse(p2_parentMissingAChild.isCorrect(), "p1_valid should not be correct");
        assertFalse(p2c1_valid.isCorrect(), "p1c1_valid should not be correct, due to problem in parent case");

        // validate: error case 'parent not valid'
        underTest.run(6);
        assertEquals("p3_not-valid", underTest.validatingImportingProcess.directoryName, "should have validated p3_not-valid");
        ImportingProcess p3_notValid = underTest.validatingImportingProcess;
        assertFalse(p3_notValid.errors.isEmpty(), "should have validated with error");

        underTest.run(7);
        assertEquals("p3c1_valid", underTest.validatingImportingProcess.directoryName, "should have validated p3c1_valid");
        ImportingProcess p3c1_valid = underTest.validatingImportingProcess;
        assertTrue(p3c1_valid.errors.isEmpty(), "should have validated without errors");

        assertFalse(p3_notValid.isCorrect(), "p1_valid should not be correct");
        assertFalse(p3c1_valid.isCorrect(), "p1c1_valid should not be correct, due to problem in parent case");

        // validate: error case 'child not valid'
        underTest.run(8);
        assertEquals("p4_valid_butChildIsNot", underTest.validatingImportingProcess.directoryName, "should have validated p4_valid_butChildIsNot");
        ImportingProcess p4_valid_butChildIsNot = underTest.validatingImportingProcess;
        assertTrue(p4_valid_butChildIsNot.errors.isEmpty(), "should have validated without errors");

        underTest.run(9);
        assertEquals("p4c1_not-valid", underTest.validatingImportingProcess.directoryName, "should have validated p4c1_not-valid");
        ImportingProcess p4c1_notValid = underTest.validatingImportingProcess;
        assertFalse(p4c1_notValid.errors.isEmpty(), "should have validated with error");

        assertFalse(p4_valid_butChildIsNot.isCorrect(), "p1_valid should not be correct, due to problem in child case");
        assertFalse(p4c1_notValid.isCorrect(), "p1c1_valid should not be correct");

        // copy files and create database entry

        // p1c1_valid (OK)
        firstProcessId = processesBefore + 1;
        Path processPath = Paths.get("src/test/resources/metadata", Integer.toString(firstProcessId));
        Path imagesPath = processPath.resolve("images");
        Path mediaPath = imagesPath.resolve("17_123_0001_media");
        Path imageOne = mediaPath.resolve("00000001.jpg");
        Path metaXml = processPath.resolve("meta.xml");

        underTest.run(10);
        assertEquals(Long.valueOf(firstProcessId), ServiceManager.getProcessService().count(), "should have created 1st process,");
        underTest.run(11);
        assertTrue(Files.isDirectory(processPath), "should have created process directory");
        underTest.run(12);
        underTest.run(13);
        underTest.run(14);
        underTest.run(15);

        assertTrue(Files.isDirectory(imagesPath), "should have created images directory");
        assertTrue(Files.isDirectory(mediaPath), "should have created media directory");
        assertTrue(Files.exists(imageOne), "should have copied media file");
        assertTrue(Files.exists(metaXml), "should have written meta.xml file");
        assertTrue(Files.readString(metaXml, UTF_8).contains("xlink:href=\"images/17_123_0001_media/00000001.jpg\""), "should have added image to meta.xml file");

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
        assertTrue(Files.readString(metaXml, UTF_8).contains("xlink:href=\"images/17_123_0002_media/00000001.jpg\""), "should have added image to meta.xml file");

        // p2c1_valid (error, broken parent)
        underTest.run(22);
        assertTrue(Files.isDirectory(Paths.get("src/test/resources/errors/p2c1_valid")), "should have error directory");
        underTest.run(23);
        assertTrue(Files.exists(Paths.get("src/test/resources/errors/p2c1_valid/Errors.txt")), "should have written Errors.txt file");
        assertTrue(Files.readString(Paths.get("src/test/resources/errors/p2c1_valid/Errors.txt"), UTF_8)
                .contains("errors in related process(es): p2_parentMissingAChild"), "should have written error message");
        underTest.run(24);
        assertTrue(Files.exists(Paths.get("src/test/resources/errors/p2c1_valid/meta.xml")), "should have copied meta.xml file");

        // p3c1_valid (error, broken parent)
        underTest.run(25);
        underTest.run(26);
        assertTrue(Files.readString(Paths.get("src/test/resources/errors/p3c1_valid/Errors.txt"), UTF_8)
                .contains("errors in related process(es): p3_not-valid"), "should have written error message");
        underTest.run(27);
        assertTrue(Files.exists(Paths.get("src/test/resources/errors/p3c1_valid/meta.xml")), "should have copied meta.xml file");

        // p4c1_not-valid (error, not valid)
        underTest.run(28);
        underTest.run(29);
        assertTrue(Files.readString(Paths.get("src/test/resources/errors/p4c1_not-valid/Errors.txt"), UTF_8)
                .contains("Validation error"), "should have written error message");
        underTest.run(30);

        // p1_valid (OK)
        thirdProcessId = processesBefore + 3;
        processPath = Paths.get("src/test/resources/metadata", Integer.toString(thirdProcessId));
        metaXml = processPath.resolve("meta.xml");

        underTest.run(31);
        assertEquals(processesBefore + 3, (long) ServiceManager.getProcessService().count(), "should have created 3rd process,");
        underTest.run(32);
        assertTrue(Files.isDirectory(processPath), "should have created process directory");
        underTest.run(33);
        String thirdMetaXml = Files.readString(metaXml, UTF_8);
        assertThat("should have added correct child links to meta.xml file", thirdMetaXml,
            containsString("xlink:href=\"database://?process.id=" + firstProcessId + "\""));
        assertThat("should have added correct child links to meta.xml file", thirdMetaXml,
            containsString("xlink:href=\"database://?process.id=" + secondProcessId + "\""));

        Process parent = ServiceManager.getProcessService().getById(6);
        assertEquals(2, parent.getChildren().size(), "parent should have 2 children");
        assertEquals(parent, ServiceManager.getProcessService().getById(firstProcessId).getParent(), "child (ID " + firstProcessId + ") should have the correct parent");
        assertEquals(parent, ServiceManager.getProcessService().getById(secondProcessId).getParent(), "child (ID " + secondProcessId + ") should have the correct parent");

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
        assertEquals(processesBefore + 3, (long) ServiceManager.getProcessService().count(), "Should import 3 processes,");
        assertEquals(6, ERRORS_DIR_PATH.toFile().list().length, "Should not import 6 processes,");
    }

    @AfterEach
    public void deleteCreatedFiles() throws Exception {
        ProcessTestUtils.removeTestProcess(firstProcessId);
        ProcessTestUtils.removeTestProcess(secondProcessId);
        ProcessTestUtils.removeTestProcess(thirdProcessId);
        TreeDeleter.deltree(ERRORS_DIR_PATH);
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }
}
