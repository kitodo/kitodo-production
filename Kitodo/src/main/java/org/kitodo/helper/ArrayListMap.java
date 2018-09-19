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

package org.kitodo.helper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The class ArrayListMap implements a simple table with two columns which are
 * realized as ArrayLists. All modifying operations on the ArrayListMap are
 * implemented in a way that they modify both columns in the same way, thus
 * granting that they will always have the same length.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class ArrayListMap<K, V> {

    private final ArrayList<K> keys = new ArrayList<>();
    private final ArrayList<V> values = new ArrayList<>();

    /**
     * The function addAll() appends all of the elements in the specified
     * collection to the end of the keys list, in the order that they are
     * returned by the specified collection's Iterator. The same quantity of
     * elements with the given value will be added to the values list.
     *
     * @param keyList
     *            a list of keys which map to a unique value
     * @param value
     *            a value the keys map to
     * @return whether one of the lists was changed
     */
    public boolean addAll(Collection<? extends K> keyList, V value) {
        boolean result = keys.addAll(keyList);
        int keyListSize = keyList.size();
        values.ensureCapacity(values.size() + keyListSize);
        for (int i = 0; i < keyListSize; i++) {
            result |= values.add(value);
        }
        return result;
    }

    /**
     * Returns the element at the specified position in the key list.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in the key list
     */
    public K getKey(int index) {
        return keys.get(index);
    }

    /**
     * Returns the element at the specified position in the value list.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in the value list
     */
    public V getValue(int index) {
        return values.get(index);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return keys.size();
    }

}
