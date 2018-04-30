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

package org.kitodo.dataeditor;

import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.Mets;

class MetsKitodoValidator {

    /**
     * Validates the mets-kitodo-format of a given Mets object by checking if the
     * first metadate element is an instance of KitodoType.
     * 
     * @param mets
     *            The Mets object.
     * @return True if the first metadate element of given Mets object is an instance
     *         of KitodoType
     */
    static boolean checkValidMetsKitodoFormat(Mets mets) {
        return MetsKitodoUtils.jaxbObjectListContainsType(MetsKitodoUtils.getXmlDataOfMetsByMdSecIndex(mets, 0),
            KitodoType.class);
    }
}
