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

/**
 * A meta-data entry. A meta-data entry consists of a key and a value. The key
 * is stored in the superclass {@link Metadata}. There is also the domain, that
 * is an indication in which container the meta-data entry is stored.
 */
public class MetadataEntry extends Metadata {
    /**
     * The value of the meta-data entry.
     */
    private String value;

    /**
     * Returns the value of the meta-data entry.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the meta-data entry.
     * 
     * @param value
     *            value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
