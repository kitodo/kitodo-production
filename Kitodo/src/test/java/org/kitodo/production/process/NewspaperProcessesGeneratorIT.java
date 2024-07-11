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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.NewspaperCourse;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.production.helper.tasks.GeneratesNewspaperProcessesThread;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.test.utils.ProcessTestUtils;

public class NewspaperProcessesGeneratorIT {
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final MetsService metsService = ServiceManager.getMetsService();

    private static final String firstProcess = "First process";
    private static final File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
    private static int newspaperTestProcessId = -1;
    private static int rulesetId = -1;
    private static final String NEWSPAPER_TEST_METADATA_FILE = "testmetaNewspaper.xml";
    private static final String NEWSPAPER_TEST_PROCESS_TITLE = "NewspaperOverallProcess";

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
        MockDatabase.insertFoldersForSecondProject();
        MockDatabase.setUpAwaitility();
        rulesetId = MockDatabase.insertRuleset("Newspaper", "newspaper.xml", 1);
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
     * Create newspaper test process and copy corresponding meta.xml file.
     * @throws DAOException when inserting test or dummy processes fails
     * @throws DataException when inserting test or dummy processes fails
     * @throws IOException when copying test metadata file fails
     */
    @Before
    public void prepareNewspaperProcess() throws DAOException, DataException, IOException {
        newspaperTestProcessId = MockDatabase.insertTestProcess(NEWSPAPER_TEST_PROCESS_TITLE, 1, 1, rulesetId);
        ProcessTestUtils.copyTestFiles(newspaperTestProcessId, NEWSPAPER_TEST_METADATA_FILE);
    }

    /**
     * Remove newspaper test processes.
     * @throws DAOException when removing dummy processes from database fails
     * @throws DataException when deleting newspaper test processes fails
     * @throws IOException when deleting metadata test files fails
     */
    @After
    public void cleanupNewspaperProcess() throws DAOException, DataException, IOException {
        if (newspaperTestProcessId > 0) {
            deleteProcessHierarchy(ServiceManager.getProcessService().getById(newspaperTestProcessId));
        }
    }

    private static void deleteProcessHierarchy(Process process) throws DAOException, DataException, IOException {
        for (Process childProcess : process.getChildren()) {
            deleteProcessHierarchy(childProcess);
        }
        ProcessService.deleteProcess(process.getId());
    }

    /**
     * Perform the test. A long-running task is simulated: Progress and number
     * of steps are queried and the next step is performed.
     */
    @Test
    public void shouldGenerateNewspaperProcesses() throws Exception {
        Process completeEdition = ServiceManager.getProcessService().getById(newspaperTestProcessId);
        Course course = NewspaperCourse.getCourse();
        course.splitInto(Granularity.DAYS);
        NewspaperProcessesGenerator underTest = new NewspaperProcessesGenerator(completeEdition, course);
        while (underTest.getProgress() < underTest.getNumberOfSteps()) {
            underTest.nextStep();
        }
        int maxId = getChildProcessWithLargestId(completeEdition, 0);
        Assert.assertEquals("The newspaper processes generator has not been completed!", underTest.getNumberOfSteps(),
            underTest.getProgress());
        Assert.assertEquals("Process title missing in newspaper's meta.xml", "NewspaperOverallProcess",
            readProcessTitleFromMetadata(newspaperTestProcessId, false));
        Assert.assertEquals("Process title missing in year's meta.xml", "NewspaperOverallProcess_1703",
            readProcessTitleFromMetadata(newspaperTestProcessId + 1, false));
        Assert.assertEquals("Process title missing in issue's meta.xml", "NewspaperOverallProcess_17050127",
            readProcessTitleFromMetadata(maxId, true));
    }

    private int getChildProcessWithLargestId(Process process, int maxId) {
        maxId = Math.max(maxId, process.getId());
        for (Process childProcess : process.getChildren()) {
            maxId = getChildProcessWithLargestId(childProcess, maxId);
        }
        return maxId;
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
        Process seasonProcess = ServiceManager.getProcessService().getById(newspaperTestProcessId);
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
                Assert.assertEquals("Bad season-year in " + seasonProcess + ": " + twoYears,
                        Integer.parseInt(years.get(0)) + 1, Integer.parseInt(years.get(1)));

                // more tests
                monthChecksOfShouldGenerateSeasonProcesses(seasonProcess, workpiece, twoYears, years);
                dayChecksOfShouldGenerateSeasonProcesses(seasonProcess, workpiece);
            }
        }
    }

    @Test
    public void shouldNotGenerateDuplicateProcessTitle() throws DAOException, DataException {
        Process completeEdition = ServiceManager.getProcessService().getById(newspaperTestProcessId);
        Course course = NewspaperCourse.getDuplicatedCourse();
        course.splitInto(Granularity.DAYS);
        GeneratesNewspaperProcessesThread generatesNewspaperProcessesThread = new GeneratesNewspaperProcessesThread(completeEdition, course);
        generatesNewspaperProcessesThread.start();
        DataException dataException = assertThrows(DataException.class,
                () -> ServiceManager.getProcessService().findById(11));
        Assert.assertEquals("Process should not have been created",
            "org.kitodo.data.database.exceptions.DAOException: Process 11 cannot be found in database",
            dataException.getMessage());
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
}
