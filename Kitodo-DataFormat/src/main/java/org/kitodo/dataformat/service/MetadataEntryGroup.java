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

package org.kitodo.dataformat.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;

public class MetadataEntryGroup extends Metadata implements MetadataGroupXmlElementAccessInterface {

    private Collection<MetadataAccessInterface> metadata = new HashSet<>();

    MetadataEntryGroup(MdSec domain, MetadataGroupType metadataGroup) {
        super.domain = domain;
        super.type = metadataGroup.getName();
        this.metadata = Stream
                .concat(metadataGroup.getMetadata().parallelStream().map(y -> new MetadataEntry(null, y)),
                    metadataGroup.getMetadataGroup().parallelStream().map(y -> new MetadataEntryGroup(null, y)))
                .collect(Collectors.toSet());
    }

    public MetadataEntryGroup() {
    }

    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        return metadata;
    }

    MetadataGroupType toMetadataGroup() {
        MetadataGroupType metadataGroup = new MetadataGroupType();
        metadataGroup.setName(super.type);
        for (MetadataAccessInterface entry : metadata) {
            if (entry instanceof MetadataEntry) {
                metadataGroup.getMetadata().add(((MetadataEntry) entry).toMetadata());
            } else if (entry instanceof MetadataEntryGroup) {
                metadataGroup.getMetadataGroup().add(((MetadataEntryGroup) entry).toMetadataGroup());
            }
        }
        return metadataGroup;
    }
}
