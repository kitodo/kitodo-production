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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

/**
 * Connects a legacy reference to a physical division. This is a soldering class to
 * keep legacy code operational which is about to be removed. Do not use this
 * class.
 */
public class LegacyReferenceHelper {

    /**
     * The soldering class containing the physical division accessed via this soldering
     * class.
     */
    private LegacyInnerPhysicalDocStructHelper target;

    @Deprecated
    public LegacyReferenceHelper(LegacyInnerPhysicalDocStructHelper target) {
        this.target = target;
    }
}
