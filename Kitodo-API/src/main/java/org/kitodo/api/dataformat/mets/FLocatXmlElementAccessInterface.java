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

package org.kitodo.api.dataformat.mets;

import java.net.URI;

/**
 * Interface for a service that handles access to the {@code <mets:FLocat>}
 * element.
 *
 * <p>
 * To make it easy: A F locat is a binary file on your computer's hard drive, or
 * anywhere on the Internet that can be referenced by a URL. Why was not that
 * just called file? Because the name file was already taken for the
 * superordinate concept of an abstract imaginary media unit. This media unit
 * has several possible uses and each use is stored in a computer file called F
 * locat.
 */
public interface FLocatXmlElementAccessInterface {
    /**
     * Returns the URI for accessing the F locat (computer file).
     *
     * @return URI of the F locat
     */
    URI getUri();

    /**
     * Sets the URI to access the F locat (computer file).
     *
     * @param href
     *            URI of the F locat to set
     */
    void setUri(URI href);
}
