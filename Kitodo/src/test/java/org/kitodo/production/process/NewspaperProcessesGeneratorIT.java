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

package org.kitodo.production.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.NewspaperCourse;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

public class NewspaperProcessesGeneratorIT {
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";
    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    /**
     * The test environment is being set up.
     *
     * @throws Exception
     *             if that does not work
     */
    @BeforeClass
    public static void setUp() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        FileLoader.createConfigProjectsFileForCalendarHierarchyTests();
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessForCalendarHierarchyTests();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        Awaitility.await().untilTrue(new AtomicBoolean(Objects.nonNull(processService.findByTitle(firstProcess))));
    }

    /**
     * The test environment is cleaned up and the database is closed.
     */
    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().delete();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
    }

    /**
     * Perform the test. A long-running task is simulated: Progress and number
     * of steps are queried and the next step is performed.
     */
    @Test
    public void shouldGenerateNewspaperProcesses() throws Exception {
        // create backup of meta data file as this file is modified inside test
        File metaFile = new File("src/test/resources/metadata/10/meta.xml");
        File backupFile = new File("src/test/resources/metadata/10/meta.xml.1");
        FileUtils.copyFile(metaFile, backupFile);

        Process completeEdition = ServiceManager.getProcessService().getById(10);
        Course course = NewspaperCourse.getCourse();
        course.splitInto(Granularity.DAYS);
        NewspaperProcessesGenerator underTest = new NewspaperProcessesGenerator(completeEdition, course);
        while (underTest.getProgress() < underTest.getNumberOfSteps()) {
            underTest.nextStep();
        }
        Assert.assertEquals("The newspaper processes generator has not been completed!", underTest.getNumberOfSteps(),
            underTest.getProgress());

        // restore backuped meta data file
        FileUtils.deleteQuietly(metaFile);
        FileUtils.moveFile(backupFile, metaFile);
        cleanUp();
    }

    /**
     * To clean up after the end of the test. All metadata directories >10 will
     * be deleted.
     */
    private static void cleanUp() throws IOException {
        Path dirProcesses = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_PROCESSES));
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirProcesses);
        for (Path path : directoryStream) {
            String fileName = path.getFileName().toString();
            if (fileName.matches("\\d+") && Integer.valueOf(fileName) > 10) {
                TreeDeleter.deltree(path);
            }
        }
    }
}
