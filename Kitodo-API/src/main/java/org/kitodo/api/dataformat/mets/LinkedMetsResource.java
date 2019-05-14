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

import java.math.BigInteger;
import java.net.URI;

/**
 * Data about a linked METS resource.
 */
public class LinkedMetsResource {
    /**
     * The loctype of the linked METS resource.
     */
    private String loctype;

    /**
     * The order of the linked METS resource.
     */
    private BigInteger order;

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
     * Returns the order of the linked METS resource.
     *
     * @return the order
     */
    public BigInteger getOrder() {
        return order;
    }

    /**
     * Sets the order of the linked METS resource.
     *
     * @param order
     *            order to set
     */
    public void setOrder(BigInteger order) {
        this.order = order;
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
}
