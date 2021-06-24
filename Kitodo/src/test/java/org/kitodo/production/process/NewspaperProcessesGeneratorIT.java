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
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.NewspaperCourse;
import org.kitodo.SecurityTestUtils;
import org.kitodo.TreeDeleter;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;

public class NewspaperProcessesGeneratorIT {
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final MetsService metsService = ServiceManager.getMetsService();

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
    @Before
    public void setUp() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        FileLoader.createConfigProjectsFileForCalendarHierarchyTests();
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessForCalendarHierarchyTests();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        Awaitility.await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
    }

    /**
     * The test environment is cleaned up and the database is closed.
     */
    @After
    public void tearDown() throws Exception {
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
        Assert.assertEquals("Process title missing in newspaper's meta.xml", "NewspaperOverallProcess",
            readProcessTitleFromMetadata(10, false));
        Assert.assertEquals("Process title missing in year's meta.xml", "NewspaperOverallProcess_1703",
            readProcessTitleFromMetadata(11, false));
        Assert.assertEquals("Process title missing in issue's meta.xml", "NewspaperOverallProcess_17050127",
            readProcessTitleFromMetadata(28, true));

        // restore backuped meta data file
        FileUtils.deleteQuietly(metaFile);
        FileUtils.moveFile(backupFile, metaFile);
        cleanUp();
    }

    /*
     * @param issue
     *            In the overall process and in the annual processes (both
     *            {@code false}), the process title is saved in the root
     *            element. In the issue process ({@code true}), it is in the
     *            issue, which is two levels below the logical structure.
     */
    private String readProcessTitleFromMetadata(int processId, boolean issue) throws DAOException, IOException {
        LogicalDivision logicalStructure = metsService
                .loadWorkpiece(processService.getMetadataFileUri(processService.getById(processId))).getLogicalStructure();
        LogicalDivision logicalDivision = issue
                ? logicalStructure.getChildren().get(0).getChildren().get(0)
                : logicalStructure;
        return logicalDivision.getMetadata().parallelStream()
                .filter(metadata -> metadata.getKey().equals("ProcessTitle")).map(MetadataEntry.class::cast)
                .map(MetadataEntry::getValue).collect(Collectors.joining(" ; "));
    }

    /**
     * Tests whether the newspaper generator correctly creates processes with a
     * postponed start of the year.
     */
    @Test
    public void shouldGenerateSeasonProcesses() throws Exception {
        // create backup of meta data file as this file is modified inside test
        File metaFile = new File("src/test/resources/metadata/10/meta.xml");
        File backupFile = new File("src/test/resources/metadata/10/meta.xml.1");
        FileUtils.copyFile(metaFile, backupFile);

        // Set base type in metadata/10/meta.xml to "Season"
        Process seasonProcess = ServiceManager.getProcessService().getById(10);
        URI seasonUri = processService.getMetadataFileUri(seasonProcess);
        Workpiece seasonMets = metsService.loadWorkpiece(seasonUri);
        seasonMets.getLogicalStructure().setType("Season");
        metsService.saveWorkpiece(seasonMets, seasonUri);

        Course course = NewspaperCourse.getCourse();
        course.setYearStart(MonthDay.of(7, 1));
        course.splitInto(Granularity.DAYS);

        NewspaperProcessesGenerator underTest = new NewspaperProcessesGenerator(seasonProcess, course);
        while (underTest.getProgress() < underTest.getNumberOfSteps()) {
            underTest.nextStep();
        }
        Assert.assertEquals("The newspaper processes generator has not been completed!", underTest.getNumberOfSteps(),
            underTest.getProgress());

        // check season-year processes
        for (Process process : processService.getAll()) {
            if (Objects.nonNull(process.getParent()) && !process.getChildren().isEmpty()) {
                URI saisonYearProcessMetadataUri = processService.getMetadataFileUri(process);
                Workpiece workpiece = metsService.loadWorkpiece(saisonYearProcessMetadataUri);

                /*
                 * Year identifier must be two consecutive integer years
                 * separated by '/'.
                 */
                String twoYears = workpiece.getLogicalStructure().getOrderlabel();
                List<String> years = Arrays.asList(twoYears.split("/", 2));
                Assert.assertTrue("Bad season-year in " + seasonProcess + ": " + twoYears,
                    Integer.parseInt(years.get(0)) + 1 == Integer.parseInt(years.get(1)));

                // more tests
                monthChecksOfShouldGenerateSeasonProcesses(seasonProcess, workpiece, twoYears, years);
                dayChecksOfShouldGenerateSeasonProcesses(seasonProcess, workpiece);
            }
        }

        // restore backed-up meta data file
        FileUtils.deleteQuietly(metaFile);
        FileUtils.moveFile(backupFile, metaFile);
        cleanUp();
    }

    private void dayChecksOfShouldGenerateSeasonProcesses(Process seasonProcess, Workpiece seasonYearWorkpiece) {
        // all days must be inside their month
        for (LogicalDivision monthLogicalDivision : seasonYearWorkpiece.getLogicalStructure()
                .getChildren()) {
            String monthValue = monthLogicalDivision.getOrderlabel();
            for (LogicalDivision dayLogicalDivision : monthLogicalDivision
                    .getChildren()) {
                String dayValue = dayLogicalDivision.getOrderlabel();
                Assert.assertTrue(
                    "Error in " + seasonProcess + ": " + dayValue + " misplaced in month " + monthValue + '!',
                    dayValue.startsWith(monthValue));
            }
        }

        // days must be ordered ascending
        for (LogicalDivision monthLogicalDivision : seasonYearWorkpiece.getLogicalStructure()
                .getChildren()) {
            String previousDayValue = null;
            for (LogicalDivision dayLogicalDivision : monthLogicalDivision
                    .getChildren()) {
                String dayValue = dayLogicalDivision.getOrderlabel();
                if (Objects.nonNull(previousDayValue)) {
                    Assert.assertTrue("Bad order of days in " + seasonProcess + ": " + dayValue + " should be before "
                            + previousDayValue + ", but isn’t!",
                        dayValue.compareTo(previousDayValue) > 0);
                }
                previousDayValue = dayValue;
            }
        }
    }

    private void monthChecksOfShouldGenerateSeasonProcesses(Process seasonProcess, Workpiece seasonYearWorkpiece,
            String twoYears, List<String> years) {
        // all months must be in the timespan
        for (LogicalDivision monthLogicalDivision : seasonYearWorkpiece.getLogicalStructure()
                .getChildren()) {
            String monthValue = monthLogicalDivision.getOrderlabel();
            List<String> monthValueFields = Arrays.asList(monthValue.split("-", 2));
            int monthNumberOfMonth = Integer.parseInt(monthValueFields.get(1));
            if (monthValueFields.get(0).equals(years.get(0))) {
                Assert.assertTrue("Error in " + seasonProcess + ": Found misplaced month " + monthValue
                        + ", should not be in year " + twoYears + ", starting by the 1st of July!",
                    monthNumberOfMonth >= 7);
            } else if (monthValueFields.get(0).equals(years.get(1))) {
                Assert.assertTrue("Error in " + seasonProcess + ": Found misplaced month " + monthValue
                        + ", should not be in year " + twoYears + ", starting by the 1st of July!",
                    monthNumberOfMonth < 7);
            } else {
                Assert.fail(
                    "Error in " + seasonProcess + ": Month " + monthValue + " is not in years " + twoYears + '!');
            }
        }

        // months must be ordered ascending
        String previousMonthValue = null;
        for (LogicalDivision monthLogicalDivision : seasonYearWorkpiece.getLogicalStructure()
                .getChildren()) {
            String monthValue = monthLogicalDivision.getOrderlabel();
            if (Objects.nonNull(previousMonthValue)) {
                Assert.assertTrue("Bad order of months in " + seasonProcess + ": " + monthValue + " should be before "
                        + previousMonthValue + ", but isn’t!",
                    monthValue.compareTo(previousMonthValue) > 0);
            }
            previousMonthValue = monthValue;
        }
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
