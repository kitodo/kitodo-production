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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;

import org.apache.log4j.BasicConfigurator;

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
	}

	@Test(expected = java.io.FileNotFoundException.class)
	public void RenamingOfNonExistingFileShouldThrowFileNotFoundException () throws IOException {
		String oldFileName = "test.xml";
		String newFileName = "test.new.xml";

		FilesystemHelper.renameFile(oldFileName, newFileName);
	}

}
