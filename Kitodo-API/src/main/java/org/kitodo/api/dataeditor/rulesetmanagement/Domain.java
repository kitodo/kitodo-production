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

package org.kitodo.api.dataeditor.rulesetmanagement;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The domain determines in which of the six available containers the metadata
 * entry is stored. Thus, metadata can be roughly classified according to
 * semantic spheres.
 */
@XmlEnum(String.class)
public enum Domain {
    /**
     * Metadata in this domain is primarily descriptive of the digital
     * representation of the cultural work. For obvious reasons, this is in part
     * identical to the description of the physical template from which the
     * digital representation was created by digital scanning techniques. But
     * this is really (primarily) about the digital representation, while
     * information on the actual cultural work is recorded under {@code SOURCE}.
     */
    @XmlEnumValue("description")
    DESCRIPTION,

    /**
     * Metadata in this domain describes the history of the digital
     * representation of the cultural work. Here, for example, information about
     * conversion processes can be made, for example, if an originally used
     * storage format was converted into another.
     */
    @XmlEnumValue("digitalProvenance")
    DIGITAL_PROVENANCE,

    /**
     * Metadata in this domain describes permissions and legal restrictions
     * regarding the use of the digital resource.
     */
    @XmlEnumValue("rights")
    RIGHTS,

    /**
     * Metadata in this domain describes the physical, digitized cultural work.
     * A whole range of information here can be identical to the description
     * domain, for example the title and the creator. But it can also be
     * metadata, which refers exclusively to the physical work, such as the
     * exact location, materials or weight.
     */
    @XmlEnumValue("source")
    SOURCE,

    /**
     * Metadata in this domain is for technical purposes, such as references to
     * servers in the world's spider web, or billing information.
     */
    @XmlEnumValue("technical")
    TECHNICAL,

    /**
     * Metadata in this domain is stored outside the actual metadata container
     * directly in the description format. This area may only accept certain
     * fields specified by the format.
     */
    @XmlEnumValue("mets:div")
    METS_DIV
}
