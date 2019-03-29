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

package org.kitodo.api.dataformat;

import java.math.BigInteger;
import java.net.URI;

/**
 * A link to a structural element in a different {@code Workpiece}.
 */
public class LinkedStructuralElement extends StructuralElement {

    /**
     * The order is a number that serves to place other structural elements together with
     * them in the correct order in the same parental structural element. After a link
     * has been made, the order value no longer affects the order. The order is
     * saved in the file that contains the link.
     */
    private BigInteger order;

    /**
     * The URI of the linked process. The URI is saved in the file that contains
     * the link.
     */
    private URI uri;


    /**
     * Returns the order value of the linked structural element.
     *
     * @return the order value
     */
    public BigInteger getOrder() {
        return order;
    }

    /**
     * Returns the URI of the linked {@code Workpiece}.
     *
     * @return the URI of the linked {@code Workpiece}
     * @see Workpiece
     */
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean isLinked() {
        return true;
    }


    /**
     * Sets the order value of the linked structural element.
     *
     * @param order
     *            order value to set
     */
    public void setOrder(BigInteger order) {
        this.order = order;
    }

    /**
     * Sets the URI of the linked {@code Workpiece}.
     *
     * @param uri
     *            URI to set
     * @see Workpiece
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }
}
