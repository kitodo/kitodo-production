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

package org.kitodo.dataaccess;

/**
 * A named node, which has an identifier, but may be reference-only.
 */
public interface IdentifiableNode extends NodeType {
    /**
     * Returns the name URI of this node.
     *
     * @return the name of this node
     */
    String getIdentifier();
}
