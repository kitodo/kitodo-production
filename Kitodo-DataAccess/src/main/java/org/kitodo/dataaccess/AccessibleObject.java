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

import java.nio.BufferOverflowException;
import java.util.NoSuchElementException;

/**
 * A node or a literal, which is accessible and provides access methods.
 */
public interface AccessibleObject extends ObjectType {
    /**
     * Returns the semantic web class type of the implementing object.
     *
     * @return the semantic web class type of the implementing object
     * @throws NoSuchElementException
     *             if the object is a node which has no RDF.TYPE literal
     *             attached
     * @throws BufferOverflowException
     *             if the object is a node which has more than one RDF.TYPE
     *             literal attached
     */
    String getType();

    /**
     * Returns whether this node has all the data from the conditions node. It
     * may have a different address, and it may have <em>more</em> data as well.
     * Should return true if the condition is null.
     *
     * @param condition
     *            a node which may be a subset of the information contained in
     *            this node
     * @return whether this node fulfils the set of conditions
     */
    boolean matches(ObjectType condition);
}
