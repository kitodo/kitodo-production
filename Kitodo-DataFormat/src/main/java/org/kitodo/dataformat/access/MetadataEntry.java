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

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataType;

/**
 * A meta-data entry. This is probably the most important Java class of all
 * Production. Production serves to capture meta-data about a digital
 * representation of a cultural work. Everything finally boils down to this
 * class, which represents a single meta-data entry. A meta-data entry consists
 * of a key and a value. The key is stored in the superclass {@link Metadata}.
 * Then there is the domain, that is the container within the METS file, in
 * which the meta-data entry is stored. There is nothing more to say about it.
 * The most important things are always very simple.
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
