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

package org.kitodo.production.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class ProcessListComparator {
    /**
     * Compares two ordered lists and returns the differences.
     *
     * @param left
     *            the one list
     * @param right
     *            the other list
     * @return a map with the differences, mapped as T on boolean, where
     *         {@code true} if it appears in the first list and not in the
     *         second, {@code false} if it appears in the second list and not in
     *         the first.
     */
    public static Map<Integer, Boolean> compareLists(TreeSet<Integer> left, TreeSet<Integer> right) {
        final Map<Integer, Boolean> result = new HashMap<>();
        Iterator<Integer> leftIterator = left.iterator();
        Iterator<Integer> rightIterator = right.iterator();
        boolean nextLeft = true;
        boolean nextRight = true;
        Integer currentLeft = null;
        Integer currentRight = null;
        while (nextLeft || nextRight) {
            if (nextLeft) {
                currentLeft = leftIterator.hasNext() ? leftIterator.next() : null;
                nextLeft = false;
            }
            if (nextRight) {
                currentRight = rightIterator.hasNext() ? rightIterator.next() : null;
                nextRight = false;
            }
            boolean comparable = Objects.nonNull(currentLeft) && Objects.nonNull(currentRight);
            if (Objects.nonNull(currentLeft) && Objects.isNull(currentRight)
                    || comparable && currentLeft < currentRight) {
                result.put(currentLeft, Boolean.TRUE);
                nextLeft = true;
            } else if (Objects.isNull(currentLeft) && Objects.nonNull(currentRight)
                    || comparable && currentLeft > currentRight) {
                result.put(currentRight, Boolean.FALSE);
                nextRight = true;
            } else if (comparable) {
                nextLeft = true;
                nextRight = true;
            }
        }
        return result;
    }
}
