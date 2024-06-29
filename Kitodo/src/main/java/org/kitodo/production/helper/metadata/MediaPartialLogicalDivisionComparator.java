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

package org.kitodo.production.helper.metadata;

import java.util.Comparator;
import java.util.Objects;

import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;

public class MediaPartialLogicalDivisionComparator implements Comparator<LogicalDivision> {

    @Override
    public int compare(LogicalDivision logicalDivisionA, LogicalDivision logicalDivisionB) {
        if (!logicalDivisionA.getViews().isEmpty() && !logicalDivisionB.getViews().isEmpty()) {
            View viewA = logicalDivisionA.getViews().getFirst();
            View viewB = logicalDivisionB.getViews().getFirst();
            if (Objects.nonNull(viewA) && Objects.nonNull(viewB)) {
                PhysicalDivision physicalDivisionA = viewA.getPhysicalDivision();
                PhysicalDivision physicalDivisionB = viewB.getPhysicalDivision();
                if (physicalDivisionA.hasMediaPartial() && physicalDivisionB.hasMediaPartial()) {
                    return physicalDivisionA.getMediaPartial().getBegin()
                            .compareTo(physicalDivisionB.getMediaPartial().getBegin());
                }
            }
        }
        return Integer.compare(logicalDivisionA.getOrder(), logicalDivisionB.getOrder());
    }

}
