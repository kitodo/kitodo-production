package org.kitodo.dataformat.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.FileType.FLocat;

public class MediaFile implements FLocatXmlElementAccessInterface {

    URI uri;

    public MediaFile(FLocat fLocat) {
        try {
            uri = new URI(fLocat.getHref());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void setUri(URI href) {
        this.uri = href;
    }

}
