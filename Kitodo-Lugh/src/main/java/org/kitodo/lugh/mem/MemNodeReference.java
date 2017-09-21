/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.lugh.mem;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.lugh.NodeReference;

/**
 * A reference to a named linked data node.
 *
 * @author Matthias Ronge
 */
public class MemNodeReference implements NodeReference {

    private final String identifier;

    /**
     * Creates a new NodeReference.
     *
     * @param url
     *            referenced URL
     */
    public MemNodeReference(String url) {
        identifier = url;
    }

    /**
     * Compares two node references for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NodeReference other = (NodeReference) obj;
        if (identifier == null) {
            if (other.getIdentifier() != null) {
                return false;
            }
        } else if (!identifier.equals(other.getIdentifier())) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns a hash code value for this node reference.
     *
     * @return a has code for this instance
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (identifier == null ? 0 : identifier.hashCode());
        return result;
    }

    /**
     * Converts this NodeReference to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model) {
        return model.createResource(identifier);
    }

    /**
     * Returns a version of this node reference which, in a debugger, will
     * symbolically represent it.
     */
    @Override
    public String toString() {
        return '↗' + identifier;
    }
}
