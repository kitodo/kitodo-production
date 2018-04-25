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

import javax.xml.bind.JAXBElement;

/**
 * General utilities for handling of generated mets-kitodo class content.
 */
class MetsKitodoUtils {

    /**
     * Gets the first object of the specified type from a given object list.
     * 
     * @param objects
     *            The list of objects.
     * @param type
     *            The type of object to return.
     * @return The first object that corresponds to the given type.
     */
    static <T> T getFirstGenericTypeFromObjectList(List<Object> objects, Class<T> type) {
        for (Object object : objects) {
            if (object instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) object;
                if (type.isInstance(jaxbElement.getValue())) {
                    return (type.cast(jaxbElement.getValue()));
                }
            }
            if (type.isInstance(object)) {
                return (type.cast(object));
            }
        }
        throw new NoSuchElementException("No " + type.getName() + " objects found");
    }
}
