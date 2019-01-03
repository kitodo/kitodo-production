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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.FileType.FLocat;

/**
 * A media file is a reference to a computer file on the data store. Since it is
 * referenced by URI, it can also be in the world wide web.
 */
public class FLocatXmlElementAccess {
    /**
     * References computer file.
     */
    private final URI uri;

    FLocatXmlElementAccess(URI uri) {
        this.uri = uri;
    }

    /**
     * Constructor for creating a new media file reference from METS F locat.
     * 
     * @param fLocat
     *            F locat to create a new media file reference from
     */
    FLocatXmlElementAccess(FLocat fLocat) {
        try {
            uri = new URI(fLocat.getHref());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Creates a new METS {@code <file>} element with this media file reference
     * in it.
     * 
     * @param mimeType
     *            possible Internet MIME type of a computer file that can be
     *            obtained when the URI is downloaded
     * @return a METS {@code <file>} element
     */
    FileType toFile(String mimeType) {
        FileType file = new FileType();
        file.setID(UUID.randomUUID().toString());
        file.setMIMETYPE(mimeType);
        FLocat fLocat = new FLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(uri.toString());
        file.getFLocat().add(fLocat);
        return file;
    }
}
