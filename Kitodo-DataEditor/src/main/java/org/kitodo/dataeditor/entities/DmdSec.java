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
