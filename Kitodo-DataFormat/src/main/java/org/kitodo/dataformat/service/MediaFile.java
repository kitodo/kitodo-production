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

import java.net.URI;
import java.net.URISyntaxException;

import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.FileType.FLocat;

public class MediaFile implements FLocatXmlElementAccessInterface {

    URI uri;

    public MediaFile() {
    }

    MediaFile(FLocat fLocat) {
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

    FileType toFile(String fileId, String mimeType) {
        FileType file = new FileType();
        file.setID(fileId);
        file.setMIMETYPE(mimeType);
        FLocat fLocat = new FLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(uri.toString());
        file.getFLocat().add(fLocat);
        return file;
    }

}
