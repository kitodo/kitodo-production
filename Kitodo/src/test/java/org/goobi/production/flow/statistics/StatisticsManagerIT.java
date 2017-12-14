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

package org.goobi.production.flow.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import de.sub.goobi.config.ConfigCore;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.jfree.data.general.DefaultValueDataset;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.services.ServiceManager;

public class StatisticsManagerIT {
    private static StatisticsManager testManager;
    private static StatisticsManager testManager2;
    private static Locale locale = new Locale("GERMAN");
    private static List testFilter = new ArrayList();
    private static URI tempPath;
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * Performs computationally expensive setup shared several tests. This
     * compromises the independence of the tests, bit is a necessary
     * optimization here.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        testFilter = serviceManager.getProcessService().findAll(null);
        tempPath = URI.create("pages/imagesTemp/");
        testManager = new StatisticsManager(StatisticsMode.THROUGHPUT, testFilter, locale);
        testManager2 = new StatisticsManager(StatisticsMode.PRODUCTION, testFilter, locale);
    }

    /**
     * Creates objects that several tests need.
     */
    @Before
    public void initTestManager() {
        Calendar calendarOne = Calendar.getInstance();
        Calendar calendarTwo = Calendar.getInstance();
        calendarOne.set(2009, 01, 01, 0, 0, 0);
        calendarOne.set(Calendar.MILLISECOND, 0);
        calendarTwo.set(2009, 03, 31, 0, 0, 0);
        calendarTwo.set(Calendar.MILLISECOND, 0);
        TimeUnit sourceTimeUnit = TimeUnit.months;
        CalculationUnit targetCalculationUnit = CalculationUnit.volumes;
        ResultOutput targetResultOutput = ResultOutput.chart;
        testManager.setSourceDateFrom(calendarOne.getTime());
        testManager.setShowAverage(false);
        testManager.setSourceDateTo(calendarTwo.getTime());
        testManager.setSourceNumberOfTimeUnitsAsString("3");
        testManager.setSourceTimeUnit(sourceTimeUnit);
        testManager.setTargetTimeUnit(sourceTimeUnit);
        testManager.setTargetCalculationUnit(targetCalculationUnit);
        testManager.setTargetResultOutput(targetResultOutput);
    }

    /**
     * Releases expensive external resources allocated in {@code setUp()}.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
            }
        };
        ArrayList<URI> data = serviceManager.getFileService().getSubUris(filter, tempPath);
        if (data.size() == 0) {
            return;
        }
        for (URI aData : data) {
            URI tempData = URI.create(tempPath.toString() + "/" + aData);
            boolean success = serviceManager.getFileService().delete(tempData);
            if (!success) {
                throw new IllegalArgumentException("Delete: deletion failed");
            }
        }
    }

    @Test
    public void testStatisticsManager() {
        StatisticsManager testProjects = new StatisticsManager(StatisticsMode.PROJECTS, testFilter, locale);
        assertEquals(StatisticsMode.THROUGHPUT, testManager.getStatisticMode());
        assertEquals(StatisticsMode.PRODUCTION, testManager2.getStatisticMode());
        assertEquals(StatisticsMode.PROJECTS, testProjects.getStatisticMode());
        StatisticsManager testStorage = new StatisticsManager(StatisticsMode.STORAGE, testFilter, locale);
        assertEquals(StatisticsMode.STORAGE, testStorage.getStatisticMode());
        assertNotSame(testManager, testManager2);
    }

    @Test
    public final void testGetJfreeDataset() {
        assertEquals(new DefaultValueDataset(), testManager.getJfreeDataset());
    }

    @Test
    public final void testGetStatisticMode() {
        assertEquals(StatisticsMode.THROUGHPUT, testManager.getStatisticMode());
        assertNotSame(StatisticsMode.PRODUCTION, testManager.getStatisticMode());
    }

    @Ignore("org.hibernate.exception.SQLGrammarException: could not prepare statement - incorrect for H2 database")
    @Test
    public final void testCalculate() {
        ConfigCore.setImagesPath(tempPath);
        testManager.calculate();
    }

    @Test
    public final void testGetAllTimeUnits() {
        List<TimeUnit> timeUnit = testManager.getAllTimeUnits();
        assertEquals(Arrays.asList(TimeUnit.values()), timeUnit);
    }

    @Test
    public final void testGetAllCalculationUnits() {
        List<CalculationUnit> calc = testManager.getAllCalculationUnits();
        assertEquals(Arrays.asList(CalculationUnit.values()), calc);
    }

    @Test
    public final void testGetAllResultOutputs() {
        List<ResultOutput> resultOutputs = testManager.getAllResultOutputs();
        assertEquals(Arrays.asList(ResultOutput.values()), resultOutputs);
    }

    @Test
    public final void testGetSourceDateFrom() {
        Calendar calendarOne = Calendar.getInstance();
        Calendar calendarTwo = Calendar.getInstance();
        calendarOne.set(2009, 01, 01, 0, 0, 0);
        calendarOne.set(Calendar.MILLISECOND, 0);
        calendarTwo.set(2009, 03, 31, 0, 0, 0);
        calendarTwo.set(Calendar.MILLISECOND, 0);
        assertEquals(calendarOne.getTime(), testManager.getSourceDateFrom());
        assertNotSame(calendarTwo.getTime(), testManager.getSourceDateFrom());
    }

    @Test
    public final void testSetSourceDateFrom() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2009, 01, 02, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        testManager.setSourceDateFrom(calendar.getTime());
        assertEquals(calendar.getTime(), testManager.getSourceDateFrom());
    }

    @Test
    public final void testGetSourceDateTo() {
        Calendar calendarOne = Calendar.getInstance();
        Calendar calendarTwo = Calendar.getInstance();
        calendarOne.set(2009, 01, 01, 0, 0, 0);
        calendarOne.set(Calendar.MILLISECOND, 0);
        calendarTwo.set(2009, 03, 31, 0, 0, 0);
        calendarTwo.set(Calendar.MILLISECOND, 0);
        assertNotSame(calendarOne.getTime(), testManager.getSourceDateTo());
        assertEquals(calendarTwo.getTime(), testManager.getSourceDateTo());
    }

    @Test
    public final void testSetSourceDateTo() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2009, 04, 01);
        testManager.setSourceDateTo(calendar.getTime());
        assertEquals(calendar.getTime(), testManager.getSourceDateTo());
    }

    @Test
    public final void testGetSourceNumberOfTimeUnitsAsString() {
        testManager.setSourceNumberOfTimeUnitsAsString("1");
        assertEquals("1", testManager.getSourceNumberOfTimeUnitsAsString());
    }

    @Test
    public final void testSetSourceNumberOfTimeUnitsAsString() {
        testManager.setSourceNumberOfTimeUnitsAsString("1");
        assertEquals("1", testManager.getSourceNumberOfTimeUnitsAsString());
    }

    @Test
    public final void testGetSourceTimeUnit() {
        testManager.setSourceTimeUnit(TimeUnit.months);
        assertEquals(TimeUnit.months, testManager.getSourceTimeUnit());
    }

    @Test
    public final void testSetSourceTimeUnit() {
        testManager.setSourceTimeUnit(TimeUnit.months);
        assertEquals(TimeUnit.months, testManager.getSourceTimeUnit());
    }

    @Test
    public final void testGetTargetTimeUnit() {
        testManager.setTargetTimeUnit(TimeUnit.months);
        assertEquals(TimeUnit.months, testManager.getTargetTimeUnit());
    }

    @Test
    public final void testSetTargetTimeUnit() {
        testManager.setTargetTimeUnit(TimeUnit.months);
        assertEquals(TimeUnit.months, testManager.getTargetTimeUnit());
    }

    @Test
    public final void testGetTargetResultOutput() {
        testManager.setTargetResultOutput(ResultOutput.chart);
        assertEquals(ResultOutput.chart, testManager.getTargetResultOutput());
    }

    @Test
    public final void testSetTargetResultOutput() {
        testManager.setTargetResultOutput(ResultOutput.chart);
        assertEquals(ResultOutput.chart, testManager.getTargetResultOutput());
    }

    @Test
    public final void testGetTargetCalculationUnit() {
        testManager.setTargetCalculationUnit(CalculationUnit.volumes);
        assertEquals(CalculationUnit.volumes, testManager.getTargetCalculationUnit());
    }

    @Test
    public final void testSetTargetCalculationUnit() {
        testManager.setTargetCalculationUnit(CalculationUnit.volumes);
        assertEquals(CalculationUnit.volumes, testManager.getTargetCalculationUnit());
    }

    @Test
    public final void testIsShowAverage() {
        testManager.setShowAverage(true);
        assertTrue(testManager.isShowAverage());
    }

    @Test
    public final void testSetShowAverage() {
        testManager.setShowAverage(true);
        assertTrue(testManager.isShowAverage());
    }

    @Test
    public final void testGetRenderingElements() {
        ConfigCore.setImagesPath(tempPath);
        testManager.getRenderingElements();
    }

    @Test
    public final void testGetLocale() {
        assertEquals(new Locale("GERMAN"), StatisticsManager.getLocale());
    }
}
