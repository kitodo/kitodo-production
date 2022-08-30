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

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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

    @BeforeClass
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
        Assert.assertNull("should not yet have parent", issueOne.getParent());
        Process issueTwo = processService.getById(2);
        Assert.assertNull("should not yet have parent", issueTwo.getParent());
        Assert.assertEquals("should not yet have created year process", 0,
            processService.findByTitle("NewsMiTe_1850").size());
        Assert.assertEquals("should not yet have created overall process", 0,
            processService.findByTitle("NewsMiTe").size());

        NewspaperMigrationTask underTest = new NewspaperMigrationTask(batchService.getById(5));
        underTest.start();
        Assert.assertTrue("should be running", underTest.isAlive());
        underTest.join();
        Assert.assertFalse("should have finished", underTest.isAlive());
        Assert.assertEquals("should have completed", 100, underTest.getProgress());

        Workpiece workpiece = ServiceManager.getMetsService()
                .loadWorkpiece(processService.getMetadataFileUri(issueOne));
        LogicalDivision logicalStructure = workpiece.getLogicalStructure();
        Assert.assertEquals("should have modified METS file", "NewspaperMonth", logicalStructure.getType());
        Assert.assertEquals("should have added date for month", "1850-03", logicalStructure.getOrderlabel());
        Assert.assertEquals("should have added date for day", "1850-03-12",
            logicalStructure.getChildren().get(0).getOrderlabel());

        Process newspaperProcess = processService.getById(4);
        processService.save(newspaperProcess);
        Process yearProcess = processService.getById(5);
        processService.save(yearProcess);
        Assert.assertEquals("should have created year process", 1, processService.findByTitle("NewsMiTe-1850").size());
        Assert.assertEquals("should have created overall process", 1, processService.findByTitle("NewsMiTe", true, true).size());
        Assert.assertTrue("should have added link from newspaper process to year process",
            newspaperProcess.getChildren().contains(yearProcess));
        List<Process> linksInYear = yearProcess.getChildren();
        Assert.assertTrue("should have added links from year process to issues",
            linksInYear.contains(issueOne) && linksInYear.contains(issueTwo));
    }

    @AfterClass
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
