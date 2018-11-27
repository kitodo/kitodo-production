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

package org.kitodo.api.dataformat.mets;

import java.util.Map.Entry;
import java.util.Set;

/**
 * Interface for a service that handles access to the {@code <mets:file>}
 * element.
 *
 * <p>
 * A file is an abstractly intended media unit that can have different variants
 * (METS idiom: uses). Other than you might think, a file is not a file on the
 * datastore, but a file can have different variants, and these variants then
 * each have a file (METS idiom: F locat) on the datastore.
 */
public interface FileXmlElementAccessInterface {

    /**
     * Returns all services for accessing the variants of the media unit
     * together with each one service for accessing the associated computer
     * file.
     *
     * @return all use services with their associated F locat services
     */
    Set<? extends Entry<? extends UseXmlAttributeAccessInterface, ? extends FLocatXmlElementAccessInterface>> getAllUsesWithFLocats();

    /**
     * Returns a service to access a computer file that represents a variant of
     * this media device.
     *
     * @param use
     *            requested variant of the media unit
     * @return service to access the F locat
     */
    FLocatXmlElementAccessInterface getFLocatForUse(UseXmlAttributeAccessInterface use);

    /**
     * Returns the ordinal number for this media unit.
     *
     * @return the ordinal number
     */
    int getOrder();

    /**
     * Returns a humanized version of the ordinal number for this media unit.
     * This does not necessarily have to be in a clearly recognizable connection
     * with the technical ordinal number.
     *
     * @return the order label
     */
    String getOrderlabel();

    /**
     * Adds a service to access a variant of the media device along with a
     * service to access the associated computer file to the media unit.
     *
     * @param use
     *            service for accessing a variant of the media unit
     * @param fLocat
     *            service to access the computer file representing the specified
     *            variant of the media device
     */
    void putFLocatForUse(UseXmlAttributeAccessInterface use, FLocatXmlElementAccessInterface fLocat);

    /**
     * Removes a service to use a variant media device and thus also the service
     * provided for this variant to access F locat associated with this variant.
     *
     * @param use
     *            service for accessing a variant of the media unit to remove
     */
    void removeFLocatForUse(UseXmlAttributeAccessInterface use);

    /**
     * Sets the ordinal number for this media unit.
     *
     * @param order
     *            ordinal number to set
     */
    void setOrder(int order);

    /**
     * Sets the humanized version of the ordinal number for this media unit.
     *
     * @param orderlabel
     *            orderlabel to set
     */
    void setOrderlabel(String orderlabel);
}
