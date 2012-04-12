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

package org.goobi.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

import static junit.framework.Assert.fail;

public class BackupFileRotationTest {

	private static File testFile;
	public static final String FILE_BACKUP_FILE_ROTATION_TEST_XML = "./File-BackupFileRotationTest.xml";

	@Test
	public void testFoo() throws Exception {
		fail();
	}

	@Before
	public void setUp() throws Exception {
		testFile = new File(FILE_BACKUP_FILE_ROTATION_TEST_XML);
		FileWriter writer = new FileWriter(testFile);
		writer.write("TEST");
		writer.close();
	}

	@After
	public void tearDown() throws Exception {
		testFile.delete();
	}
}
