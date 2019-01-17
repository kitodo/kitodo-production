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

import java.math.BigInteger;
import java.net.URI;

/**
 * A link to a structure in a different {@code Workpiece}.
 */
class LinkedStructure implements ExistingOrLinkedStructure {
    /**
     * The label of the linked structure.
     */
    private String label;

    /**
     * The order is a number that serves to place other structures together with
     * them in the correct order in the same parental structure. After a link
     * has been made, the order value no longer affects the order.
     */
    private BigInteger order;

    /**
     * The URI of the linked process.
     */
    private URI uri;

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Returns the order value of the linked structure.
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

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the order value of the linked structure.
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
