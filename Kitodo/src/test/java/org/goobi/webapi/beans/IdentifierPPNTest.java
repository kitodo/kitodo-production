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

        Assert.assertTrue("Given lowercase PPN " + identifier + " should be correct.",
            IdentifierPPN.isValid(identifier));

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
