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

package org.kitodo.dataeditor.handlers;

import java.util.Objects;
import java.util.Optional;

import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class MetsKitodoStructMapHandler {

    private MetsKitodoStructMapHandler() {
    }

    /**
     * Returns the StructMap element of the given type from given mets object.
     *
     * @param mets
     *            The mets object from which the StructMap element should be
     *            returned.
     * @param type
     *            The type of the StructMap element which should be returned, e.g.
     *            "PHYSICAL" or "LOGICAL".
     * @return The StructMapType object.
     */
    public static Optional<StructMapType> getMetsStructMapByType(Mets mets, String type) {
        for (StructMapType structMap : mets.getStructMap()) {
            if (Objects.equals(type, structMap.getTYPE())) {
                return Optional.of(structMap);
            }
        }
        return Optional.empty();
    }


}
