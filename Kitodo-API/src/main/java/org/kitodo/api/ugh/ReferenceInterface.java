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

package org.kitodo.api.ugh;

/**
 * A {@code Reference} object represents a single reference. A reference links
 * two different structure entities in a non-hierarchical way. A reference may
 * even link structure entities from different structures.
 *
 * <p>
 * The most common use of a {@code References} object is the linking between a
 * logical structure entity (such as a chapter) and a physical structure entity
 * (like a page).
 *
 * <p>
 * References are always storing the source and target of the link. Besides
 * these two {@code DocStruct} elements a type can be stored, to give
 * information about the link.
 *
 * <p>
 * Usually, {@code References} objects need not be created manually, but are
 * used internally, when creating links. You should use the appropriate methods
 * of the {@code DocStruct} class to set linking between {@code DocStruct}
 * objects.
 */
public interface ReferenceInterface {
    /**
     * Returns the source of the reference.
     * 
     * @return the source
     */
    DocStructInterface getSource();

    /**
     * Returns the target of the reference.
     * 
     * @return the target
     */
    DocStructInterface getTarget();

    /**
     * Returns the type of the reference.
     * 
     * @return the type
     */
    String getType();
}
