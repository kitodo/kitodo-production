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

package org.kitodo.api;

public class MetadataEntry extends Metadata {

    // The value of the metadata.
    private String value;

    /**
     * Get the value of the metadataentry.
     *
     * @return The value of the metadata.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value of the metadataentry.
     *
     * @param value
     *            The value of the metadata.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
