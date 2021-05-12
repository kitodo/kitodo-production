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

package org.kitodo.production.metadata;

public enum InsertionPosition {
    BEFORE_CURRENT_ELEMENT,
    AFTER_CURRENT_ELEMENT,
    FIRST_CHILD_OF_CURRENT_ELEMENT,
    LAST_CHILD_OF_CURRENT_ELEMENT,
    CURRENT_POSITION,
    PARENT_OF_CURRENT_ELEMENT
}
