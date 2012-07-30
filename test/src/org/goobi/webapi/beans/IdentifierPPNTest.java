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

package org.goobi.webapi.beans;

import junit.framework.Assert;
import org.junit.Test;

public class IdentifierPPNTest {

	@Test
	public void validPpnShouldValidateAsCorrect() {
		String identifier = "32578597X";

		Assert.assertTrue("Given PPN " + identifier + " should be correct.", IdentifierPPN.isValid(identifier));
	}

	@Test
	public void lowerCaseValidPpnShouldBeCorrect() {
		String identifier = "32578597x";

		Assert.assertTrue("Given lowercase PPN " + identifier + " should be correct.", IdentifierPPN.isValid(identifier));

	}

	@Test
	public void invalidPpnShouldBeFalse() {
		String identifier = "32578597A";

		Assert.assertFalse("Given PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
	}

	@Test
	public void emptyStringValueShouldBeFalse() {
		String identifier = "";

		Assert.assertFalse("Empty string value should be invalid.", IdentifierPPN.isValid(identifier));
	}

	@Test
	public void nullValueValidatedAsFalse() {
		String identifier = null;

		Assert.assertFalse("Null value should be invalid.", IdentifierPPN.isValid(identifier));
	}

	@Test
	public void toShortPpnShouldNotBeCorrect() {
		String identifier = "123";

		Assert.assertFalse("To short PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
	}

	@Test
	public void toLongPpnShouldNotBeCorrect() {
		String identifier = "1234567890";

		Assert.assertFalse("To long PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
	}
}
