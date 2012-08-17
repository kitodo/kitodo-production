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

package org.goobi.junit;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AssertFileSystem {

	public static void assertLastModifiedDate(String fileName, long expectedLastModifiedDate) {
		long currentLastModifiedDate = getLastModifiedFileDate(fileName);
		Assert.assertEquals("Last modified date of file " + fileName + " differ:", expectedLastModifiedDate, currentLastModifiedDate);
	}

	public static long getLastModifiedFileDate(String fileName) {
		File testFile = new File(fileName);
		return testFile.lastModified();
	}

	public static void assertFileHasContent(String fileName, String expectedContent) throws IOException {
		File testFile = new File(fileName);
		FileReader reader = new FileReader(testFile);
		BufferedReader br = new BufferedReader(reader);
		String content = br.readLine();
		br.close();
		reader.close();
		Assert.assertEquals("File " + fileName + " does not contain expected content:", expectedContent, content);
	}

	public static void assertFileExists(String fileName) {
		File newFile = new File(fileName);
		if (!newFile.exists()) {
			Assert.fail("File " + fileName + " does not exist.");
		}
	}

	public static void assertFileNotExists(String fileName) {
		File newFile = new File(fileName);
		if (newFile.exists()) {
			Assert.fail("File " + fileName + " should not exist.");
		}
	}

	public static void assertDirectoryIsEmpty(String message, File directory) {
		if (! directory.isDirectory()) {
		    Assert.fail(directory.getAbsolutePath() + " is not a directory!");
		}

		Assert.assertEquals(message, 0, directory.list().length);
	}

}
