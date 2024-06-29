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

package org.kitodo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kitodo.api.dataformat.PhysicalDivision;

public class MediaUtilTest {

    /**
     * Test detection of mime type.
     */
    @Test
    public void testMimeTypeDetection() {
        assertTrue(MediaUtil.isAudio("audio/mp3"));
        assertFalse(MediaUtil.isVideo("image/jpeg"));

        assertTrue(MediaUtil.isImage("image/jpeg"));
        assertFalse(MediaUtil.isImage("video/mp4"));

        assertTrue(MediaUtil.isVideo("video/mp4"));
        assertFalse(MediaUtil.isVideo("image/jpeg"));

        assertTrue(MediaUtil.isAudioOrVideo("audio/mp3"));
        assertTrue(MediaUtil.isAudioOrVideo("video/mp4"));
        assertFalse(MediaUtil.isAudioOrVideo("image/jpeg"));
    }

    /**
     * Test getting the type of the {@link org.kitodo.api.dataformat.PhysicalDivision}.
     */
    @Test
    public void testGettingPhysicalDivisionTypeByMimeType() {
        assertEquals(PhysicalDivision.TYPE_PAGE, MediaUtil.getPhysicalDivisionTypeOfMimeType("image/jpeg"));
        assertEquals(PhysicalDivision.TYPE_TRACK, MediaUtil.getPhysicalDivisionTypeOfMimeType("audio/mp3"));
        assertEquals(PhysicalDivision.TYPE_TRACK, MediaUtil.getPhysicalDivisionTypeOfMimeType("video/mp4"));
        assertEquals(PhysicalDivision.TYPE_OTHER, MediaUtil.getPhysicalDivisionTypeOfMimeType("application/pdf"));
    }
}
