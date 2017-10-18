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

package org.kitodo.dataaccess.storage.memory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.kitodo.dataaccess.NodeReference;

/**
 * A reference to a named linked data node.
 */
public class MemoryNodeReference implements NodeReference {

    private final String identifier;

    /**
     * Creates a new NodeReference.
     *
     * @param identifier
     *            referenced URL
     */
    public MemoryNodeReference(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier must not be null.");
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier must not be empty.");
        }
        assert URI_SCHEME.matcher(identifier).find() : "Identifier isn't a valid URI: " + identifier;
        this.identifier = identifier;
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
        MemoryNodeReference other = (MemoryNodeReference) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

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

    @Override
    public RDFNode toRDFNode(Model model, Boolean unused) {
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
