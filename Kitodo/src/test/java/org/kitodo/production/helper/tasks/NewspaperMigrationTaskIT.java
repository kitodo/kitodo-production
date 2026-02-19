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

package org.kitodo.production.helper.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.BatchService;
import org.kitodo.production.services.data.ProcessService;

public class NewspaperMigrationTaskIT {

    private static final File METADATA_DIRECTORY = new File("src/test/resources/metadata");
    private static final File TEST_DATAFILES_DIRECTORY = new File("src/test/resources/NewspaperMigrationTaskIT");
    private static final File ORIGINAL_METADATA_TEMPORARY_LOCATION = new File("metadata.orig");

    private static final BatchService batchService = ServiceManager.getBatchService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

    @BeforeAll
    public static void prepareDatabase() throws Exception {

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }

        moveOriginMetadataDirectoryAside();
        FileUtils.copyDirectory(TEST_DATAFILES_DIRECTORY, METADATA_DIRECTORY);
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        addNewspaperDatabase();
        User user = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
    }

    private static void addNewspaperDatabase() throws Exception {
        Process processOne = processService.getById(1);
        Ruleset ruleset = processOne.getRuleset();
        ruleset.setFile("ruleset_newspaper-migration-test.xml");
        ServiceManager.getRulesetService().save(ruleset);
        Process processTwo = processService.getById(2);
        Batch newspaperBatch = new Batch();
        newspaperBatch.setTitle("Newspaper migration test");
        newspaperBatch.getProcesses().add(processOne);
        newspaperBatch.getProcesses().add(processTwo);
        ServiceManager.getBatchService().save(newspaperBatch);
        processOne.setTitle("NewsMiTe_18500312");
        processOne.getBatches().add(newspaperBatch);
        processService.save(processOne);
        processTwo.setTitle("NewsMiTe_18501105");
        processTwo.getBatches().add(newspaperBatch);
        processService.save(processTwo);
    }

    private static void moveOriginMetadataDirectoryAside() throws Exception {
        if (ORIGINAL_METADATA_TEMPORARY_LOCATION.exists()) {
            TreeDeleter.deltree(METADATA_DIRECTORY);
        } else {
            METADATA_DIRECTORY.renameTo(ORIGINAL_METADATA_TEMPORARY_LOCATION);
        }
    }

    @Test
    public void testNewspaperMigrationTask() throws Exception {

        Process issueOne = processService.getById(1);
        assertNull(issueOne.getParent(), "should not yet have parent");
        Process issueTwo = processService.getById(2);
        assertNull(issueTwo.getParent(), "should not yet have parent");
        assertEquals(0, processService.findByTitle("NewsMiTe_1850").size(), "should not yet have created year process");
        assertEquals(0, processService.findByTitle("NewsMiTe").size(), "should not yet have created overall process");

        NewspaperMigrationTask underTest = new NewspaperMigrationTask(batchService.getById(5));
        underTest.start();
        assertTrue(underTest.isAlive(), "should be running");
        underTest.join();
        assertFalse(underTest.isAlive(), "should have finished");
        assertEquals(100, underTest.getProgress(), "should have completed");

        Workpiece workpiece = ServiceManager.getMetsService()
                .loadWorkpiece(processService.getMetadataFileUri(issueOne));
        LogicalDivision logicalStructure = workpiece.getLogicalStructure();
        assertEquals("NewspaperMonth", logicalStructure.getType(), "should have modified METS file");
        assertEquals("1850-03", logicalStructure.getOrderlabel(), "should have added date for month");
        assertEquals("1850-03-12", logicalStructure.getChildren().getFirst().getOrderlabel(), "should have added date for day");

        Process newspaperProcess = processService.getById(4);
        processService.save(newspaperProcess);
        Process yearProcess = processService.getById(5);
        processService.save(yearProcess);
        assertEquals(1, processService.findByTitle("NewsMiTe-1850").size(), "should have created year process");
        assertEquals(1, processService.findByTitle("NewsMiTe").size(), "should have created overall process");
        assertTrue(newspaperProcess.getChildren().contains(yearProcess), "should have added link from newspaper process to year process");
        List<Process> linksInYear = yearProcess.getChildren();
        assertTrue(linksInYear.contains(issueOne) && linksInYear.contains(issueTwo), "should have added links from year process to issues");
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        restoreMetadataDirectoryContents();
        SecurityTestUtils.cleanSecurityContext();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
    }

    private static void restoreMetadataDirectoryContents() throws Exception {
        if (ORIGINAL_METADATA_TEMPORARY_LOCATION.exists()) {
            if (METADATA_DIRECTORY.exists()) {
                TreeDeleter.deltree(METADATA_DIRECTORY);
            }
            ORIGINAL_METADATA_TEMPORARY_LOCATION.renameTo(METADATA_DIRECTORY);
        }
    }
}
