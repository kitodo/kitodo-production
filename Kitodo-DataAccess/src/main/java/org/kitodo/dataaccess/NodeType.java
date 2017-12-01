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
 * Summary interface for all variants of nodes. This includes
 * {@link NodeReference}s, anonymous {@link Node}s and
 * {@link IdentifiableNode}s. Conceptually, they form the complementary set to
 * all {@link Literal}s, that is “all {@link ObjectType}s that are not
 * literals”. Functionally, these classes do not have much in common. That is
 * why this interface is empty. However, it is used for filtering in
 * {@link Result}.
 */
public interface NodeType extends ObjectType {
}
