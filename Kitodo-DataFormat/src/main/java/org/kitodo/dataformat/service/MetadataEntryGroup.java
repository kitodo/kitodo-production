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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;

public class MetadataEntryGroup extends Metadata implements MetadataGroupXmlElementAccessInterface {

    Collection<MetadataAccessInterface> metadata = new ArrayList<>();

    public MetadataEntryGroup(MdSec domain, MetadataGroupType metadataGroup) {
        super.domain = domain;
        super.type = metadataGroup.getName();
        this.metadata = Stream
                .concat(metadataGroup.getMetadata().parallelStream().map(y -> new MetadataEntry(null, y)),
                    metadataGroup.getMetadataGroup().parallelStream().map(y -> new MetadataEntryGroup(null, y)))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        return metadata;
    }

    MetadataGroupType toMetadataGroup() {
        MetadataGroupType metadataGroup = new MetadataGroupType();
        metadataGroup.setName(super.type);
        metadata.parallelStream().filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast)
                .map(MetadataEntry::toMetadata).forEach(metadataGroup.getMetadata()::add);
        metadata.parallelStream().filter(MetadataEntryGroup.class::isInstance).map(MetadataEntryGroup.class::cast)
                .map(MetadataEntryGroup::toMetadataGroup).forEach(metadataGroup.getMetadataGroup()::add);
        return metadataGroup;
    }
}
