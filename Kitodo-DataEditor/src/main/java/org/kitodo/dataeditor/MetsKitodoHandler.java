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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.DateTime;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;

/**
 * General utilities for handling of generated mets-kitodo class content.
 */
class MetsKitodoHandler {

    /**
     * Gets the first object of the specified type from a given object list of JAXB
     * elements.
     *
     * @param objects
     *            The list of objects.
     * @param type
     *            The type of object to return.
     * @return The first object that corresponds to the given type.
     */
    static <T> T getFirstGenericTypeFromJaxbObjectList(List<Object> objects, Class<T> type) {
        if (XmlUtils.objectListContainsType(objects, type)) {
            for (Object object : objects) {
                if (object instanceof JAXBElement) {
                    JAXBElement jaxbElement = (JAXBElement) object;
                    if (type.isInstance(jaxbElement.getValue())) {
                        return (type.cast(jaxbElement.getValue()));
                    }
                }
            }
        }
        throw new NoSuchElementException("No " + type.getName() + " objects found");
    }



    /**
     * Gets an optional list of objects which holds the xml data of an mets objets
     * mdSec element.
     * 
     * @param mdSecType
     *            The mdSec element.
     * @return A list of objects wraped in Optional class.
     */
    static Optional<List<Object>> getXmlDataOfMdSec(MdSecType mdSecType) {
        // Wrapping null-checks at getter-chain into Optional<T>.class
        return Optional.ofNullable(mdSecType).map(MdSecType::getMdWrap).map(MdSecType.MdWrap::getXmlData)
                .map(MdSecType.MdWrap.XmlData::getAny);
    }

    /**
     * Gets the xml metadata of the specified mdSec element of an Mets object.
     * 
     * @param mets
     *            The Mets object.
     * @param index
     *            The index of the mdSec element.
     * @return The list of objects which contains the xml data.
     */
    static List<Object> getXmlDataOfMetsByMdSecIndex(Mets mets, int index) {
        if (mets.getDmdSec().size() > index) {
            Optional<List<Object>> xmlData = getXmlDataOfMdSec(mets.getDmdSec().get(index));
            if (xmlData.isPresent()) {
                return xmlData.get();
            }
            throw new NoSuchElementException("MdSec element with index: " + index + " does not have data");
        }
        throw new NoSuchElementException("MdSec element with index: " + index + " does not exist");
    }

    /**
     * Adds a note to the first {@code agent} element in mets header. Does nothing
     * if no {@code agent} element exists.
     * 
     * @param noteMessage
     *            The note message.
     * @param mets
     *            The Mets object.
     * @return The Mets object with added note.
     */
    public static Mets addNoteToMetsHeader(String noteMessage, Mets mets) {
        List<MetsType.MetsHdr.Agent> agents = mets.getMetsHdr().getAgent();
        if (agents.size() > 0) {
            mets.getMetsHdr().getAgent().get(0).getNote().add(noteMessage);
        }
        return mets;
    }
}
