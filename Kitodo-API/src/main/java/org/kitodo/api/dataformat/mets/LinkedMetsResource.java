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
import java.util.Objects;

/**
 * Data about a linked METS resource.
 */
public class LinkedMetsResource {
    /**
     * The loctype of the linked METS resource.
     */
    private String loctype;

    /**
     * The URI of the linked METS resource.
     */
    private URI uri;

    /**
     * Returns the loctype of the linked METS resource.
     *
     * @return the loctype
     */
    public String getLoctype() {
        return loctype;
    }

    /**
     * Sets the loctype of the linked METS resource.
     *
     * @param loctype
     *            loctype to set
     */
    public void setLoctype(String loctype) {
        this.loctype = loctype;
    }

    /**
     * Sets the URI of the linked METS resource.
     *
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the URI of the linked METS resource.
     *
     * @param uri
     *            URI to set
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkedMetsResource that = (LinkedMetsResource) o;
        return Objects.equals(loctype, that.loctype)
                && Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((loctype == null) ? 0 : loctype.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }
}
