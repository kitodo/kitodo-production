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
     * Some magic numbers that are used in the METS XML file representation of
     * this structure to describe relations between XML elements. They need to
     * be stored because some scatty third-party scripts rely on them not being
     * changed anymore once assigned.
     */
    private final String metsReferrerId;

    /**
     * References computer file.
     */
    private final URI uri;

    /**
     * Public constructor for creating a new media file reference. This
     * constructor can be used with the service manager to create a new instance
     * of media file.
     */
    FLocatXmlElementAccess(URI uri) {
        metsReferrerId = UUID.randomUUID().toString();
        this.uri = uri;
    }

    /**
     * Constructor for creating a new media file reference from METS F locat.
     * 
     * @param file
     *            File to create a new media file reference from
     */
    FLocatXmlElementAccess(FileType file) {
        metsReferrerId = file.getID();
        try {
            uri = new URI(file.getFLocat().get(0).getHref());
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
        file.setID(metsReferrerId);
        file.setMIMETYPE(mimeType);
        FLocat fLocat = new FLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(uri.toString());
        file.getFLocat().add(fLocat);
        return file;
    }
}
