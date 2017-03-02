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

package de.sub.goobi.metadaten;

/**
 * The four different ways nodes can be added to the logical structure tree.
 */
enum TreeInsertionMode {
    BEFORE_ELEMENT, AFTER_ELEMENT, AS_FIRST_CHILD, AS_LAST_CHILD;

    /**
     * Returns the mode by its integer identifier.
     *
     * @param i
     *            identifier
     * @return the corresponding tree insertion mode
     * @throws IllegalArgumentException
     *             if the int string isn’t an int, or the enum type has no
     *             constant with that number
     */
    public static TreeInsertionMode fromIntString(String i) {
        switch (Integer.valueOf(i)) {
        case 1:
            return BEFORE_ELEMENT;
        case 2:
            return AFTER_ELEMENT;
        case 3:
            return AS_FIRST_CHILD;
        case 4:
            return AS_LAST_CHILD;
        default:
            throw new IllegalArgumentException("For string: " + i);
        }
    }
}
