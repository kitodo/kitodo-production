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

package org.kitodo.dataeditor.handlers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.kitodo.dataeditor.JaxbXmlUtils;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MetsKitodoMdSecHandler {

    /**
     * Returns the KitodoType object of an MdSecType object.
     * 
     * @param dmdSecElement
     *            The DmdSecElement as MdSecType object.
     * @return The KitodoType object.
     */
    public static KitodoType getKitodoTypeOfDmdSecElement(MdSecType dmdSecElement) {
        Optional<List<Object>> xmlDataOfMdSec = getXmlDataOfMdSec(dmdSecElement);
        if (xmlDataOfMdSec.isPresent()) {
            return getFirstGenericTypeFromJaxbObjectList(xmlDataOfMdSec.get(), KitodoType.class);
        }
        throw new NoSuchElementException("DmdSec element with id " + dmdSecElement.getID() + " does not have xml data");
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
    public static List<Object> getXmlDataOfMetsByMdSecIndex(Mets mets, int index) {
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
     * Gets an optional list of objects which holds the xml data of an mets objets
     * mdSec element.
     *
     * @param mdSecType
     *            The mdSec element.
     * @return A list of objects wraped in Optional class.
     */
    public static Optional<List<Object>> getXmlDataOfMdSec(MdSecType mdSecType) {
        // Wrapping null-checks at getter-chain into Optional<T>.class
        return Optional.ofNullable(mdSecType).map(MdSecType::getMdWrap).map(MdSecType.MdWrap::getXmlData)
                .map(MdSecType.MdWrap.XmlData::getAny);
    }

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
    private static <T> T getFirstGenericTypeFromJaxbObjectList(List<Object> objects, Class<T> type) {
        if (JaxbXmlUtils.objectListContainsType(objects, type)) {
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
}
