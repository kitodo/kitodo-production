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

import org.kitodo.dataformat.metsmodskitodo.ExtensionDefinition;
import org.kitodo.dataformat.metsmodskitodo.KitodoType;
import org.kitodo.dataformat.metsmodskitodo.ModsDefinition;

/**
 * General utilities for handling of generated mets-mods-kitodo class content.
 */
class MetsModsKitodoUtils {

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

    /**
     * Gets the KitodoType object of a given ModsDefinition object.
     * 
     * @param modsDefinition
     *            The modsDefinition object.
     * @return The KitodoType object.
     */
    static KitodoType getKitodoTypeFromModsDefinition(ModsDefinition modsDefinition) {
        Optional<List<Object>> extensionData = Optional.ofNullable(modsDefinition).map(ModsDefinition::getModsGroup);

        if (extensionData.isPresent()) {
            ExtensionDefinition extensionDefinition = getFirstGenericTypeFromObjectList(extensionData.get(),
                ExtensionDefinition.class);
            return getKitodoTypeFromExtensionDefinition(extensionDefinition);
        }
        throw new NoSuchElementException("ModsDefinition does not have MODS-extension-elements");
    }

    private static KitodoType getKitodoTypeFromExtensionDefinition(ExtensionDefinition extensionDefinition) {
        Optional<List<Object>> kitodoData = Optional.ofNullable(extensionDefinition)
                .map(ExtensionDefinition::getContent);

        if (kitodoData.isPresent()) {
            return getFirstGenericTypeFromObjectList(kitodoData.get(), KitodoType.class);
        }
        throw new NoSuchElementException("ExtensionDefinition does not have Kitodo-elements");
    }
}
