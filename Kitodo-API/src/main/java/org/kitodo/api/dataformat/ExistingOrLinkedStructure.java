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

package org.kitodo.dataformat.access;

/**
 * A common interface for {@code Structure}s and {@code LinkedStructure}s.
 * 
 * @see Structure
 */
public interface ExistingOrLinkedStructure {
    /**
     * Returns the label of this structure.
     * 
     * @return the label
     */
    String getLabel();

    /**
     * Returns the type of this structure.
     * 
     * @return the type
     */
    String getType();

    /**
     * Returns whether this structure is a link to a structure in another
     * workpiece.
     * 
     * @return whether this structure is a link
     */
    boolean isLinked();

    /**
     * Sets the label of this structure.
     * 
     * @param label
     *            label to set
     */
    void setLabel(String label);

    /**
     * Sets the type of this structure.
     * 
     * @param type
     *            type to set
     */
    void setType(String type);
}
