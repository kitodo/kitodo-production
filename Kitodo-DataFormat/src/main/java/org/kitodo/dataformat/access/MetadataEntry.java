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

package org.kitodo.dataformat.access;

import org.kitodo.api.MdSec;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataType;

/**
 * A meta-data entry. A meta-data entry consists of a key and a value. The key
 * is stored in the superclass {@link Metadata}. There is also the domain, that
 * is an indication in which container within the METS file the meta-data entry
 * is stored.
 */
public class MetadataEntry extends Metadata implements MetadataXmlElementAccessInterface {
    /**
     * The value of the meta-data entry.
     */
    private String value;

    /**
     * Public constructor for creating a new meta-data entry. This constructor
     * can be used with the service loader to create a new meta-data entry.
     */
    public MetadataEntry() {
    }

    /**
     * Constructor for producing a meta-data entry from METS/Kitodo format.
     * 
     * @param domain
     *            at which place the entry was found
     * @param metadataType
     *            Kitodo meta-data object (input)
     */
    MetadataEntry(MdSec domain, MetadataType metadataType) {
        super.domain = domain;
        super.type = metadataType.getName();
        this.value = metadataType.getValue();
    }

    /**
     * Returns the value of the meta-data entry.
     * 
     * @return the value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the meta-data entry.
     * 
     * @param value
     *            value to set
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Creates a Kitodo XML element from the meta-data entry.
     * 
     * @return a Kitodo XML element
     */
    MetadataType toMetadata() {
        MetadataType metadataType = new MetadataType();
        metadataType.setName(super.type);
        metadataType.setValue(this.value);
        return metadataType;
    }
}
