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

package org.kitodo.dataeditor.entities;

import org.kitodo.dataeditor.JaxbXmlUtils;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;


public class DmdSec extends MdSecType {

    /**
     * Constructor to copy the data from parent class.
     *
     * @param mdSecType
     *            The MdSecType object.
     */
    public DmdSec(MdSecType mdSecType) {
        if (!mdSecType.getADMID().isEmpty()) {
            super.admid = mdSecType.getADMID();
        }
        super.created = mdSecType.getCREATED();
        super.groupid = mdSecType.getGROUPID();
        super.id = mdSecType.getID();
        super.mdRef = mdSecType.getMdRef();
        super.mdWrap = mdSecType.getMdWrap();
        super.status = mdSecType.getSTATUS();
    }

    public KitodoType getKitodoType() {
        return JaxbXmlUtils.getKitodoTypeOfDmdSecElement(this);
    }
}
