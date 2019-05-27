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

import java.util.Map;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class HelperComparatorTest {
    @Test
    public void testCompareLists() {
        TreeSet<Integer> left = new TreeSet<>();
        left.add(13);
        left.add(21);
        left.add(30);
        left.add(29);
        left.add(12);
        left.add(6);
        left.add(28);
        left.add(27);
        left.add(5);
        left.add(1);
        left.add(16);
        left.add(2);

        TreeSet<Integer> right = new TreeSet<>();
        right.add(25);
        right.add(24);
        right.add(5);
        right.add(1);
        right.add(8);
        right.add(26);
        right.add(9);
        right.add(29);
        right.add(27);
        right.add(16);
        right.add(22);
        right.add(6);

        Map<Integer, Boolean> result = HelperComparator.compareLists(left, right);

        Assert.assertEquals(12, result.size());

        Assert.assertTrue(result.get(30));
        Assert.assertTrue(result.get(21));
        Assert.assertTrue(result.get(2));
        Assert.assertTrue(result.get(13));
        Assert.assertTrue(result.get(12));
        Assert.assertTrue(result.get(28));

        Assert.assertFalse(result.get(26));
        Assert.assertFalse(result.get(25));
        Assert.assertFalse(result.get(24));
        Assert.assertFalse(result.get(9));
        Assert.assertFalse(result.get(8));
        Assert.assertFalse(result.get(22));
    }
}
