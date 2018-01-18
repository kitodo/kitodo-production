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

package org.kitodo.data.database.helper;

import java.util.Collection;

/**
 * Collection of simple utility methods.
 * 
 * @author <a href="mailto:nick@systemmobile.com">Nick Heudecker</a>
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public final class Util {

    private Util() {
    }

    /**
     * Calculates the optimal initial capacity for a HashMap or HashSet instance
     * that is to be populated with the given collection and isn’t intended to
     * grow any further. TODO: find way to replace it if we really need it
     *
     * @param collection
     *            collection whose size shall be used to determine the initial
     *            capacity for a HashMap
     * @return the appropriate capacity
     */
    public static int hashCapacityFor(Collection<?> collection) {
        return (int) Math.ceil(collection.size() / 0.75);
    }
}
