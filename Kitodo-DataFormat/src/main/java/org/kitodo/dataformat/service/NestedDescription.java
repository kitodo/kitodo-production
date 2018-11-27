package org.kitodo.dataformat.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;

public class NestedDescription extends CommonDescription implements MetadataGroupXmlElementAccessInterface {

    Collection<CommonDescription> description = new ArrayList<>();

    public NestedDescription(MdSec domain, MetadataGroupType metadataGroup) {
        super.domain = domain;
        super.type = metadataGroup.getName();
        this.description = Stream
                .concat(metadataGroup.getMetadata().parallelStream().map(y -> new Description(null, y)),
                    metadataGroup.getMetadataGroup().parallelStream().map(y -> new NestedDescription(null, y)))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends MetadataAccessInterface> getMetadata() {
        return description;
    }
}
