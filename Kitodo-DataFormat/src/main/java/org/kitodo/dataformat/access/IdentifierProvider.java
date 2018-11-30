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

import java.util.Iterator;

/**
 * Provides unique identifiers for elements in XML files.
 */
class IdentifierProvider implements Iterator<String> {
    int i = 1;

    /**
     * Returns if there is another identifier. Since there is always a larger
     * integer than the previous one, this is not checked at all, but the
     * interface requires this method.
     * 
     * @return whether there is a larger integer than the previous one
     */
    @Override
    public boolean hasNext() {
        return i > 0;
    }

    /**
     * Returns a new identifier.
     * 
     * @return a new identifier
     */
    @Override
    public String next() {
        return Integer.toString(i++);
    }

}
