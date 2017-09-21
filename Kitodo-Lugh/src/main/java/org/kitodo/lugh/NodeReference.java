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
 * A reference to a named linked data node.
 *
 * @author Matthias Ronge
 */
public interface NodeReference extends IdentifiableNode, NodeType {

    /**
     * Compares two node references for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a hash code value for this node reference.
     *
     * @return a has code for this instance
     */
    @Override
    public int hashCode();
}
