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

package org.kitodo.dataformat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DataFormatVersionProviderTest {

    private final DataFormatVersionProvider versionProvider = new DataFormatVersionProvider();

    @Test
    public void getDataFormatVersionTest() {
        assertEquals("1.0", versionProvider.getDataFormatVersion(), "Data format version was not correct");
    }
}
