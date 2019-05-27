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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class ProcessListComparatorTest {
    @Test
    public void testCompareLists() {
        TreeSet<Integer> left = new TreeSet<>();
        left.addAll(Arrays.asList(13, 21, 30, 29, 12, 6, 28, 27, 5, 1, 16, 2));

        TreeSet<Integer> right = new TreeSet<>();
        right.addAll(Arrays.asList(25, 24, 5, 1, 8, 26, 9, 29, 27, 16, 22, 6));

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(12, result.size());

        for (int i : new int[] {30, 21, 2, 13, 12, 28 }) {
            Assert.assertTrue(result.get(i));
        }

        for (int i : new int[] {26, 25, 24, 9, 8, 22 }) {
            Assert.assertFalse(result.get(i));
        }
    }

    @Test
    public void testCompareListsWithEmptyLeftList() {
        TreeSet<Integer> left = new TreeSet<>();

        TreeSet<Integer> right = new TreeSet<>();
        right.addAll(Arrays.asList(8, 14, 7, 12, 13, 4));

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(6, result.size());

        for (int i : new int[] {4, 7, 8, 12, 14, 13 }) {
            Assert.assertFalse(result.get(i));
        }
    }

    @Test
    public void testCompareListsWithEmptyRightList() {
        TreeSet<Integer> left = new TreeSet<>();
        left.addAll(Arrays.asList(5, 12, 1, 16, 3, 11));

        TreeSet<Integer> right = new TreeSet<>();

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(6, result.size());

        for (int i : new int[] {11, 12, 3, 16, 5, 1 }) {
            Assert.assertTrue(result.get(i));
        }
    }

    @Test
    public void testCompareListsWithBothListsEmpty() {
        TreeSet<Integer> left = new TreeSet<>();
        TreeSet<Integer> right = new TreeSet<>();

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testCompareListsWithBothListsIdentical() {
        TreeSet<Integer> input = new TreeSet<>();
        input.addAll(Arrays.asList(12, 19, 11, 9, 13, 8, 2, 7));

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(input, input);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testCompareListsWithNoEntriesWhichAreInBothLists() {
        TreeSet<Integer> left = new TreeSet<>();
        left.addAll(Arrays.asList(14, 13, 9, 11, 8, 28, 19, 24));

        TreeSet<Integer> right = new TreeSet<>();
        right.addAll(Arrays.asList(26, 27, 10, 18, 12, 29, 5, 22));

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(16, result.size());

        for (int i : new int[] {19, 14, 24, 9, 28, 11, 8, 13 }) {
            Assert.assertTrue(result.get(i));
        }

        for (int i : new int[] {22, 18, 12, 27, 10, 5, 26, 29 }) {
            Assert.assertFalse(result.get(i));
        }
    }

    @Test
    public void testCompareListsWithDifferentListSizes() {
        TreeSet<Integer> left = new TreeSet<>();
        left.addAll(Arrays.asList(11, 15, 4, 9, 21, 8, 1, 5, 14, 3, 22, 12));

        TreeSet<Integer> right = new TreeSet<>();
        right.addAll(Arrays.asList(4, 10, 5, 6, 8, 9, 15));

        Map<Integer, Boolean> result = ProcessListComparator.compareLists(left, right);

        Assert.assertEquals(9, result.size());

        for (int i : new int[] {12, 1, 21, 22, 3, 14, 11 }) {
            Assert.assertTrue(result.get(i));
        }

        for (int i : new int[] {6, 10 }) {
            Assert.assertFalse(result.get(i));
        }
    }
}
