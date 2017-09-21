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

package org.kitodo.lugh;

/**
 * A named linked data node whose contents are available.
 *
 * @author Matthias Ronge
 */
public interface NamedNode extends IdentifiableNode, Node {

    /**
     * Compares two named nodes for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Returns the name URI of this node.
     *
     * @return the name of this node
     */
    @Override
    public String getIdentifier();

    /**
     * Returns a hash value of this object.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode();
}
