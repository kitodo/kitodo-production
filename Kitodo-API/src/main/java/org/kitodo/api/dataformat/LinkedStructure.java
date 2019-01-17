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
import java.util.Collection;
import java.util.List;

import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;

/**
 * A link to a structure in a different {@code Workpiece}.
 */
class LinkedStructure implements ExistingOrLinkedStructure {
    /**
     * The label of the linked structure. The label is saved in the linked file.
     */
    private String label;

    /**
     * The order is a number that serves to place other structures together with
     * them in the correct order in the same parental structure. After a link
     * has been made, the order value no longer affects the order. The order is
     * saved in the file that contains the link.
     */
    private BigInteger order;

    /**
     * The type of structure, for example, book, chapter, page. Although the
     * data type of this variable is a string, it is recommended to use a
     * controlled vocabulary. If the generated METS files are to be used with
     * the DFG Viewer, the list of possible structure types is defined. The type
     * is saved in the linked file.
     * 
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    private String type;

    /**
     * The URI of the linked process. The URI is saved in the file that contains
     * the link.
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

    @Override
    public List<AreaXmlElementAccessInterface> getAreas() {
        throw new UnsupportedOperationException("areas is not supported by linked structure");
    }

    @Override
    public List<DivXmlElementAccessInterface> getChildren() {
        throw new UnsupportedOperationException("children is not supported by linked structure");
    }

    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        throw new UnsupportedOperationException("metadata is not supported by linked structure");
    }

    @Override
    public String getOrderlabel() {
        throw new UnsupportedOperationException("orderlabel is not supported by linked structure");
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setOrderlabel(String orderlabel) {
        throw new UnsupportedOperationException("orderlabel is not supported by linked structure");
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
