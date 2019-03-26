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

    @Test
    public void shouldShowDependingOnDoctypeWhenBothAreEmpty() {
        AdditionalField additionalField = new AdditionalField("monograph");

        assertTrue(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldShowDependingOnDoctypeWhenItIs() {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setIsDocType("monograph");

        assertTrue(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldShowDependingOnDoctypeWhenItIsNot() {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setIsDocType("multivolum");

        assertFalse(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldNotShowDependingOnDoctypeWhenItIs() {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setIsDocType("multivolume");

        assertFalse(additionalField.showDependingOnDoctype());
    }

    @Test
    public void shouldNotShowDependingOnDoctypeWhenItIsNot() {
        AdditionalField additionalField = new AdditionalField("monograph");
        additionalField.setIsNotDoctype("monograph");

        assertFalse(additionalField.showDependingOnDoctype());
    }
}
