package org.kitodo.dataformat.service;

import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetadataType;

public class Description extends CommonDescription implements MetadataXmlElementAccessInterface {

    private String value;

    public Description(MdSec domain, MetadataType metadataType) {
        super.domain = domain;
        super.type = metadataType.getName();
        this.value = metadataType.getValue();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
