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

package org.kitodo.production.lugh.ld;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A named linked data node whose contents are available.
 *
 * @author Matthias Ronge
 */
public class NamedNode extends Node implements IdentifiableNode {

    private static final String MISSING_IDENTIFIER = "Identifier must not be null.";

    private final String identifier;

    /**
     * Creates a new named node.
     *
     * @param identifier
     *            the name URI of this node
     */
    public NamedNode(String identifier) {
        if (identifier == null) {
            throw new NullPointerException(MISSING_IDENTIFIER);
        }
        this.identifier = identifier;
    }

    /**
     * Creates a named resource.
     *
     * @param model
     *            model to create the resource in
     * @return the created resource
     */
    @Override
    protected Resource createRDFSubject(Model model) {
        return model.createResource(identifier);
    }

    /**
     * Compares two named nodes for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NamedNode other = (NamedNode) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the name URI of this node.
     *
     * @return the name of this node
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns a hash value of this object.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + (identifier == null ? 0 : identifier.hashCode());
        return result;
    }
}
