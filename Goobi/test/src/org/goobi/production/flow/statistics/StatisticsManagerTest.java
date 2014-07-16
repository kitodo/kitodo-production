/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.flow.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.jfree.data.general.DefaultValueDataset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.schlichtherle.io.File;
import de.sub.goobi.config.ConfigMain;

public class StatisticsManagerTest {
	static StatisticsManager testManager;
	static StatisticsManager testManager2;
	static Locale locale = new Locale("GERMAN");
	static IDataSource testFilter = new UserDefinedFilter("stepdone:5");
	private static String tempPath;

	@BeforeClass
	public static void setUp() {
		File f = new File("pages/imagesTemp");
		tempPath = f.getAbsolutePath() + File.separator;

		testManager = new StatisticsManager(StatisticsMode.THROUGHPUT, testFilter, locale);
		testManager2 = new StatisticsManager(StatisticsMode.PRODUCTION, testFilter, locale);

		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.set(2009, 01, 01, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(2009, 03, 31, 0, 0, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		TimeUnit sourceTimeUnit = TimeUnit.months;
		CalculationUnit targetCalculationUnit = CalculationUnit.volumes;
		ResultOutput targetResultOutput = ResultOutput.chart;
		testManager.setSourceDateFrom(cal1.getTime());
		testManager.setShowAverage(false);
		testManager.setSourceDateTo(cal2.getTime());
		testManager.setSourceNumberOfTimeUnitsAsString("3");
		testManager.setSourceTimeUnit(sourceTimeUnit);
		testManager.setTargetTimeUnit(sourceTimeUnit);
		testManager.setTargetCalculationUnit(targetCalculationUnit);
		testManager.setTargetResultOutput(targetResultOutput);
	}

	@AfterClass
	public static void tearDown() {
		File dir = new File(tempPath);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(java.io.File dir, String name) {
				return (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
			}
		};	
		String[] data = dir.list(filter);
		File file;
		if (data == null || data.length == 0) {
			return;
		}
	    for (int i = 0; i < data.length; i++) {
	    	file = new File(tempPath + data[i]);
	    	boolean success = file.delete();
	    	if (!success) {
	    		throw new IllegalArgumentException("Delete: deletion failed");
	    	}
	    }    	
	}

	@Test
	public void testStatisticsManager() {
		StatisticsManager testProjects = new StatisticsManager(StatisticsMode.PROJECTS, testFilter, locale);
		StatisticsManager testStorage = new StatisticsManager(StatisticsMode.STORAGE, testFilter, locale);
		assertEquals(StatisticsMode.THROUGHPUT, testManager.getStatisticMode());
		assertEquals(StatisticsMode.PRODUCTION, testManager2.getStatisticMode());
		assertEquals(StatisticsMode.PROJECTS, testProjects.getStatisticMode());
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

	@Ignore("Crashing") 
	@Test
	public final void testCalculate() {
		ConfigMain.setImagesPath(tempPath);
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
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.set(2009, 01, 01, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(2009, 03, 31, 0, 0, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		assertEquals(cal1.getTime(), testManager.getSourceDateFrom());
		assertNotSame(cal2.getTime(), testManager.getSourceDateFrom());
	}

	@Test
	public final void testSetSourceDateFrom() {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2009, 01, 02, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		testManager.setSourceDateFrom(cal1.getTime());
		assertEquals(cal1.getTime(), testManager.getSourceDateFrom());
	}

	@Test
	public final void testGetSourceDateTo() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.set(2009, 01, 01, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(2009, 03, 31, 0, 0, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		assertNotSame(cal1.getTime(), testManager.getSourceDateTo());
		assertEquals(cal2.getTime(), testManager.getSourceDateTo());
	}

	@Test
	public final void testSetSourceDateTo() {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2009, 04, 01);
		testManager.setSourceDateTo(cal1.getTime());
		assertEquals(cal1.getTime(), testManager.getSourceDateTo());
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
		ConfigMain.setImagesPath(tempPath);
		testManager.getRenderingElements();
	}

	@Test
	public final void testGetLocale() {
		assertEquals(new Locale("GERMAN"), StatisticsManager.getLocale());

	}
}