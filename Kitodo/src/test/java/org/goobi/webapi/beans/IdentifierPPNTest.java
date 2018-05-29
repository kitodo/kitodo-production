/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.webapi.beans;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdentifierPPNTest {

    @Test
    public void validPpnShouldValidateAsCorrect() {
        String identifier = "32578597X";

        assertTrue("Given PPN " + identifier + " should be correct.", IdentifierPPN.isValid(identifier));
    }

    @Test
    public void lowerCaseValidPpnShouldBeCorrect() {
        String identifier = "32578597x";

        assertTrue("Given lowercase PPN " + identifier + " should be correct.", IdentifierPPN.isValid(identifier));

    }

    @Test
    public void invalidPpnShouldBeFalse() {
        String identifier = "32578597A";

        assertFalse("Given PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
    }

    @Test
    public void emptyStringValueShouldBeFalse() {
        String identifier = "";

        assertFalse("Empty string value should be invalid.", IdentifierPPN.isValid(identifier));
    }

    @Test
    public void nullValueValidatedAsFalse() {
        String identifier = null;

        assertFalse("Null value should be invalid.", IdentifierPPN.isValid(identifier));
    }

    @Test
    public void toShortPpnShouldNotBeCorrect() {
        String identifier = "123";

        assertFalse("To short PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
    }

    @Test
    public void toLongPpnShouldNotBeCorrect() {
        String identifier = "1234567890";

        assertFalse("To long PPN " + identifier + " should be invalid.", IdentifierPPN.isValid(identifier));
    }
}
