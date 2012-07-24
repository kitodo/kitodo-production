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

package de.sub.goobi.helper;

import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FilesystemHelperTest {

	@BeforeClass
	public static void oneTimeSetUp() {
		BasicConfigurator.configure();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		deleteFile("old.xml");
		deleteFile("new.xml");
	}

	@Test(expected = java.io.FileNotFoundException.class)
	public void RenamingOfNonExistingFileShouldThrowFileNotFoundException () throws IOException {
		String oldFileName = "old.xml";
		String newFileName = "new.xml";

		FilesystemHelper.renameFile(oldFileName, newFileName);
	}

	@Test
	public void shouldRenameAFile()
		throws IOException {
		createFile("old.xml");
		FilesystemHelper.renameFile("old.xml", "new.xml");
		assertFileExists("new.xml");
		assertFileNotExists("old.xml");
	}

	@Test
	public void nothingHappensIfSourceFilenameIsNotSet()
		throws IOException {
		FilesystemHelper.renameFile(null, "new.xml");
		assertFileNotExists("new.xml");
	}
	
	@Test
	public void nothingHappensIfTargetFilenameIsNotSet()
		throws IOException {
		createFile("old.xml");
		FilesystemHelper.renameFile("old.xml", null);
		assertFileNotExists("new.xml");
	}

	private void assertFileExists(String fileName) {
		File newFile = new File(fileName);
		if (!newFile.exists()) {
			fail("File " + fileName + " does not exist.");
		}
	}

	private void assertFileNotExists(String fileName) {
		File newFile = new File(fileName);
		if (newFile.exists()) {
			fail("File " + fileName + " should not exist.");
		}
	}

	private void createFile(String fileName) throws IOException {
		File testFile = new File(fileName);
		FileWriter writer = new FileWriter(testFile);
		writer.close();
	}

	private void deleteFile(String fileName) {
		File testFile = new File(fileName);
		testFile.delete();
	}
}
