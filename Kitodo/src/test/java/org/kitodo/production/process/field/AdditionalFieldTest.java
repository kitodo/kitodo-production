/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.process.field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
