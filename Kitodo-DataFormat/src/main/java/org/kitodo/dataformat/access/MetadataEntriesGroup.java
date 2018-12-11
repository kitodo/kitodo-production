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

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;

/**
 * A group of meta-data entries. A group of meta-data entries is like a table
 * with different meta-data entries, which can be groups again. This allows any
 * nesting depths to be achieved.
 */
public class MetadataEntriesGroup extends Metadata implements MetadataGroupXmlElementAccessInterface {
    /**
     * The meta-data in this group.
     */
    private Collection<MetadataAccessInterface> metadata = new HashSet<>();

    /**
     * Constructor for a new, empty meta-data entries group. This constructor
     * can be used with the module loader to create a new meta-data entries
     * group.
     */
    public MetadataEntriesGroup() {
    }

    /**
     * Constructor for a meta-data entry group gained from METS.
     * 
     * @param domain
     *            domain of the METS document where the metadata was read
     * @param metadataGroup
     *            {@code <kitodo:metadataGroup>} XML element
     */
    MetadataEntriesGroup(MdSec domain, MetadataGroupType metadataGroup) {
        super.domain = domain;
        super.type = metadataGroup.getName();
        this.metadata = Stream.concat(
            metadataGroup.getMetadata().parallelStream().map(kitodoMetadata -> new MetadataEntry(null, kitodoMetadata)),
            metadataGroup.getMetadataGroup().parallelStream()
                    .map(kitodoMetadataGroup -> new MetadataEntriesGroup(null, kitodoMetadataGroup)))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the meta-data of this group.
     * 
     * @return the meta-data of this group
     */
    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        return metadata;
    }

    /**
     * Generates a {@code <kitodo:metadataGroup>} XML element from this group.
     * 
     * @return a {@code <kitodo:metadataGroup>} XML element
     */
    MetadataGroupType toMetadataGroup() {
        MetadataGroupType metadataGroup = new MetadataGroupType();
        metadataGroup.setName(super.type);
        for (MetadataAccessInterface entry : metadata) {
            if (entry instanceof MetadataEntry) {
                metadataGroup.getMetadata().add(((MetadataEntry) entry).toMetadata());
            } else if (entry instanceof MetadataEntriesGroup) {
                metadataGroup.getMetadataGroup().add(((MetadataEntriesGroup) entry).toMetadataGroup());
            }
        }
        return metadataGroup;
    }
}
