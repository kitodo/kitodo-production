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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.MdSec;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;

/**
 * A group of metadata entries. A group of metadata entries is like a table
 * with different metadata entries, which can be groups again. This allows any
 * nesting depths to be achieved.
 */
public class MetadataGroupXmlElementAccess extends MetadataXmlElementsAccess {

    private final MetadataGroup metadataGroup;

    /**
     * Constructor for a new, empty metadata entries group. This constructor
     * can be used with the module loader to create a new metadata entries
     * group.
     */
    public MetadataGroupXmlElementAccess() {
        metadataGroup = new MetadataGroup();
    }

    /**
     * Constructor for a metadata entry group gained from METS.
     *
     * @param domain
     *            domain of the METS document where the metadata was read
     * @param xmlMetadataGroup
     *            {@code <kitodo:metadataGroup>} XML element
     */
    MetadataGroupXmlElementAccess(MdSec domain, MetadataGroupType xmlMetadataGroup) {
        this();
        metadataGroup.setDomain(domain);
        metadataGroup.setKey(xmlMetadataGroup.getName());
        metadataGroup.getMetadata()
                .addAll(Stream.concat(
                    xmlMetadataGroup.getMetadata().parallelStream()
                            .map(kitodoMetadata -> new MetadataXmlElementAccess(null, kitodoMetadata)
                                    .getMetadataEntry()),
                    xmlMetadataGroup.getMetadataGroup().parallelStream()
                            .map(kitodoMetadataGroup -> new MetadataGroupXmlElementAccess(null,
                                    kitodoMetadataGroup).metadataGroup))
                        .collect(Collectors.toSet()));
    }

    MetadataGroupXmlElementAccess(MetadataGroup metadataEntriesGroup) {
        this.metadataGroup = metadataEntriesGroup;
    }

    MetadataGroup getMetadataGroup() {
        return metadataGroup;
    }

    /**
     * Generates a {@code <kitodo:metadataGroup>} XML element from this group.
     *
     * @return a {@code <kitodo:metadataGroup>} XML element
     */
    MetadataGroupType toXMLMetadataGroup() {
        MetadataGroupType xmlMetadataGroup = new MetadataGroupType();
        xmlMetadataGroup.setName(metadataGroup.getKey());
        for (org.kitodo.api.Metadata entry : metadataGroup.getMetadata()) {
            if (entry instanceof MetadataEntry) {
                xmlMetadataGroup.getMetadata().add(new MetadataXmlElementAccess((MetadataEntry) entry).toMetadata());
            } else if (entry instanceof MetadataGroup) {
                xmlMetadataGroup.getMetadataGroup()
                        .add(new MetadataGroupXmlElementAccess((MetadataGroup) entry).toXMLMetadataGroup());
            }
        }
        return xmlMetadataGroup;
    }
}
