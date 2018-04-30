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
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;

/**
 * General utilities for handling of generated mets-kitodo class content.
 */
class MetsKitodoUtils {

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
        if (jaxbObjectListContainsType(objects, type)) {
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
     * Checks if a List of Jaxb-Object elements contain objects of given type.
     *
     * @param objects
     *            The list of objects.
     * @param type
     *            The type of object to check.
     * @return {@code true} if the list of Jaxb-Object elements contain objects of
     *         given type. {@code false} if not.
     */
    static <T> boolean jaxbObjectListContainsType(List<Object> objects, Class<T> type) {
        for (Object object : objects) {
            if (object instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) object;
                if (type.isInstance(jaxbElement.getValue())) {
                    return true;
                }
            }
        }
        return false;
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
     * Checks if the specified mdSec element of an mets object contains any metdata.
     * 
     * @param mets
     *            The Mets object.
     * @param index
     *            The mdSec element index.
     * @return {@code true} if the specified mdSec element contains any metadata.
     *         {@code false} if not or if the mdSec element with the specified index
     *         does not exist.
     */
    static boolean metsContainsMetadataAtMdSecIndex(Mets mets, int index) {
        if (mets.getDmdSec().size() > index) {
            Optional<List<Object>> xmlData = getXmlDataOfMdSec(mets.getDmdSec().get(index));
            return xmlData.isPresent();
        }
        return false;
    }

    /**
     * Gets the xml metadata of the specified mets objects mdSec element.
     * 
     * @param mets
     *            The Mets object.
     * @param index
     *            The mdSec element index.
     * @return
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
}
