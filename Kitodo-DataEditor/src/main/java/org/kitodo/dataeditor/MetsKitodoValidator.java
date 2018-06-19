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

import java.util.List;
import java.util.Optional;

import org.kitodo.dataeditor.handlers.MetsKitodoMdSecHandler;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.Mets;

/**
 * This class provides methods for checking or validating Mets objects.
 */
class MetsKitodoValidator {

    /**
     * Checks if the first metadata element of a Mets object is an instance of KitodoType.
     *
     * @param mets
     *            The Mets object.
     * @return True if the first metadata element of given Mets object is an instance
     *         of KitodoType
     */
    static boolean checkMetsKitodoFormatOfMets(Mets mets) {
        return JaxbXmlUtils.objectListContainsType(MetsKitodoMdSecHandler.getXmlDataOfMetsByMdSecIndex(mets, 0),
            KitodoType.class);
    }

    /**
     * Checks if the specified mdSec element of an mets object contains any metadata.
     *
     * @param mets
     *            The Mets object.
     * @param index
     *            The index of the mdSec element.
     * @return {@code true} if the specified mdSec element contains any metadata.
     *         {@code false} if not or if the mdSec element with the specified index
     *         does not exist.
     */
    static boolean metsContainsMetadataAtDmdSecIndex(Mets mets, int index) {
        if (mets.getDmdSec().size() > index) {
            Optional<List<Object>> xmlData = MetsKitodoMdSecHandler.getXmlDataOfMdSec(mets.getDmdSec().get(index));
            return xmlData.isPresent();
        }
        return false;
    }
}
