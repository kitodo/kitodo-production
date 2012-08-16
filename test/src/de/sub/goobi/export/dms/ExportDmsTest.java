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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ExportDmsTest {

	private TestAppender testAppender;

	@Before
	public void setup() {
		testAppender = new TestAppender();
		BasicConfigurator.configure(testAppender);
	}

	@Test
	public void shoudlRaiseWarningIfOcrDirectoryDoesNotExist() throws IOException, SwapException, DAOException, InterruptedException {
		ExportDms fixture = new ExportDms();

		fixture.exportContentOfOcrDirectory(new File("/foo/bar"), new File("userHome"), "");
		assertWarning("OCR directory /foo/bar does not exists.");
	}

	private void assertWarning(String message) {
		assertEquals("Expecting WARN log level", Level.WARN, testAppender.getLastEvent().getLevel());
		assertEquals("Unexected log message", message, testAppender.getLastEvent().getMessage());
	}

}
