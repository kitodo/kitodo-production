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

package de.sub.goobi.helper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;

import static junit.framework.Assert.fail;

import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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