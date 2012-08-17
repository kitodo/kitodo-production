/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.export.dms;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.goobi.log4j.TestAppender;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.goobi.junit.AssertFileSystem.assertDirectoryIsEmpty;
import static org.goobi.junit.AssertFileSystem.assertFileExists;
import static org.junit.Assert.assertEquals;

public class ExportDmsTest {
	private static final String DUMMY_ATS = "test123";

	private final static String DIRECTORY_PREFIX = UUID.randomUUID().toString() + "-";
	private final static String DESTINATION_DIRECTORY = DIRECTORY_PREFIX + "destination";
	private final static String SOURCE_DIRECTORY = DIRECTORY_PREFIX + "source";

	private final static File destinationDirectory = new File(DESTINATION_DIRECTORY);
	private final static File sourceDirectory = new File(SOURCE_DIRECTORY);

	private TestAppender testAppender;

	@BeforeClass
	public static void createDirectories() {
		destinationDirectory.mkdir();
		sourceDirectory.mkdir();
	}

	@Before
	public void setupTestAppender() {
		testAppender = new TestAppender();
		BasicConfigurator.configure(testAppender);
	}

	@AfterClass
	public static void removeDirectories() {
		deleteRecursive(destinationDirectory);
		deleteRecursive(sourceDirectory);
	}

	@Test
	public void shoudlRaiseWarningIfOcrDirectoryDoesNotExist() throws IOException, SwapException, DAOException, InterruptedException {
		ExportDms fixture = new ExportDms();

		fixture.exportContentOfOcrDirectory(new File("/foo/bar"), new File("userHome"), "");

		assertWarning("OCR directory /foo/bar does not exists.");
	}

	@Test
	public void shouldDoNothingWithEmptySourceDirectory() throws IOException, SwapException, DAOException, InterruptedException {
		ExportDms fixture = new ExportDms();

		fixture.exportContentOfOcrDirectory(sourceDirectory, destinationDirectory, "");

		assertDirectoryIsEmpty("Destination directory should be empty.", destinationDirectory);
	}

	@Test
	public void shouldDoNothingIfSubSourceDirectoryIsEmpty() throws IOException, SwapException, DAOException, InterruptedException {
		ExportDms fixture = new ExportDms();

		fixture.exportContentOfOcrDirectory(sourceDirectory, destinationDirectory, DUMMY_ATS);

		assertDirectoryIsEmpty("Destination directory should be empty.", destinationDirectory);
	}

	@Test
	public void dummyFileShouldEndUpInDestinationDirectory() throws IOException, SwapException, DAOException, InterruptedException {
		String dummyDestinationFilePath = DESTINATION_DIRECTORY + File.separator + DUMMY_ATS
				+ "_xml" + File.separator + "dummy.xml";

		String sourceSubDirectoryName = SOURCE_DIRECTORY + File.separator + "test_xml";

		String dummySourceFilePath = sourceSubDirectoryName + File.separator + "dummy.xml";

		File dummySourceFile = new File(dummySourceFilePath);
		File sourceSubDirectory = new File(sourceSubDirectoryName);
		File dummyDestinationFile = new File(dummyDestinationFilePath);

		sourceSubDirectory.mkdir();
		dummySourceFile.createNewFile();

		ExportDms fixture = new ExportDms();
		fixture.exportContentOfOcrDirectory(sourceDirectory, destinationDirectory, DUMMY_ATS);

		assertFileExists(dummyDestinationFile.getAbsolutePath());
	}

	private void assertWarning(String message) {
		assertEquals("Expecting WARN log level", Level.WARN, testAppender.getLastEvent().getLevel());
		assertEquals("Unexected log message", message, testAppender.getLastEvent().getMessage());
	}

	private static void deleteRecursive(File f) {
		if (f.exists()) {
			if (f.isDirectory()) {
				for (File sf : f.listFiles()) {
					deleteRecursive(sf);
				}
			}
			f.delete();
		}
	}

}
