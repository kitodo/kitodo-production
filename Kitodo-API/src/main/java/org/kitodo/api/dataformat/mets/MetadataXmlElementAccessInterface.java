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

/**
 * Interface for a service that handles access to the {@code <kitodo:metadata>}
 * element. Meta-data is stored as a collection of key-value pairs. Access to
 * the key is possible via the parent interface, this interface grants access to
 * the value.
 */
public interface MetadataXmlElementAccessInterface extends MetadataAccessInterface {
    /**
     * Returns the value of the meta-data entry.
     *
     * @return the value
     */
    String getValue();

    /**
     * Sets the value of the meta-data entry.
     *
     * @param value
     *            value to be set
     */
    void setValue(String value);
}
