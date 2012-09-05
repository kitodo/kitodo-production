/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *	   - http://gdz.sub.uni-goettingen.de
 *	   - http://www.goobi.org
 *	   - http://launchpad.net/goobi-production
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

package de.sub.goobi.metadaten;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class MetadatenTest {

	@Test(expected = NullPointerException.class)
	public void throwsNullPointerExceptionWhenCalledWithoutInitializedHelper() {
		Metadaten md = new Metadaten();
		md.XMLlesen();
	}

	@Test
	public void xmlReadingLockIsNotAquiredAfterCreation() {
		Metadaten md = new Metadaten();
		assertFalse("XML Reading Lock is already aquired but shouldn't be.",
			md.xmlReadingLock.isLocked());
	}

	@Test
	public void returnsEmptyStringIfXmlReadingLockIsAlreadyAquiredByOtherThread()
	throws InterruptedException {
		final Metadaten md = new Metadaten();
		final MutableString result = new MutableString();
		Thread t = new Thread() {
			public void run() {
				result.set(md.XMLlesen());
			}
		};
		
		md.xmlReadingLock.lock();
		t.start();
		t.join();

		assertEquals("Should return empty string is XML Reading Lock is aquired.", result.get(), "");
	}

	@Test
	public void freesXmlReadingLockIfExceptionHappens()
	throws InterruptedException {
		final Metadaten md = new Metadaten();
		try {
			md.XMLlesen();
		} catch (NullPointerException npe) {
			assertFalse("Should free XML Reading Lock.", md.xmlReadingLock.isLocked());
		}
	}

	private class MutableString {
		private String s;
		public void set(String val) { s = val; }
		public String get() { return s; }
	}

}
