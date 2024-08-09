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

import org.kitodo.api.dataformat.PhysicalDivision;

/**
 * Represents the only existing legacy doc struct type from the physical map
 * named “page”. This is a soldering class to keep legacy code operational which
 * is about to be removed. Do not use this class.
 */
public class LegacyInnerPhysicalDocStructTypePageHelper extends LegacyLogicalDocStructTypeHelper {

    /**
     * The sole doc struct type instance “page”.
     */
    @Deprecated
    public static final LegacyLogicalDocStructTypeHelper INSTANCE = new LegacyInnerPhysicalDocStructTypePageHelper();

    private LegacyInnerPhysicalDocStructTypePageHelper() {
        super(null);
    }

    @Override
    @Deprecated
    public String getName() {
        return PhysicalDivision.TYPE_PAGE;
    }

    @Override
    @Deprecated
    public String getNameByLanguage(String language) {
        switch (language) {
            case "de":
                return "Seite";
            default:
                return "Page";
        }
    }
}
