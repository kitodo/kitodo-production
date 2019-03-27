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

package org.kitodo.production.process.field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdditionalFieldTest {

    private AdditionalField additionalField = new AdditionalField("monograph");

    @Test
    public void shouldShowDependingOnDoctypeWhenBothAreEmpty() {
        assertTrue(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldShowDependingOnDoctypeWhenItIs() {
        additionalField.setIsDocType("monograph");

        assertTrue(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldShowDependingOnDoctypeWhenItIsNot() {
        additionalField.setIsDocType("multivolum");

        assertFalse(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldNotShowDependingOnDoctypeWhenItIs() {
        additionalField.setIsDocType("multivolume");

        assertFalse(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldNotShowDependingOnDoctypeWhenItIsNot() {
        additionalField.setIsNotDoctype("monograph");

        assertFalse(additionalField.showDependingOnDoctype());
    }
}
