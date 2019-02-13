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

package org.kitodo.api.dataformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * A list that is enforces the order of its members.
 */
class SortedList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Possibility to get an order number for T.
     */
    private Function<T, Integer> orderGetter;

    /**
     * Constructor for a sorted list.
     * 
     * @param orderGetter
     *            possibility to get the order number for an element
     */
    public SortedList(Function<T, Integer> orderGetter) {
        this.orderGetter = orderGetter;
    }

    @Override
    public T get(int index) {
        sort();
        return super.get(index);
    }

    @Override
    public int lastIndexOf(Object object) {
        sort();
        return super.lastIndexOf(object);
    }

    @Override
    public ListIterator<T> listIterator() {
        sort();
        return super.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        sort();
        return super.listIterator(index);
    }

    @Override
    public int indexOf(Object object) {
        sort();
        return super.indexOf(object);
    }

    @Override
    public Iterator<T> iterator() {
        sort();
        return super.iterator();
    }

    /**
     * Sorts the list by the order value. The order of list items is changed
     * only when necessary. The order of list members with the same ordinal
     * number is not affected.
     */
    private void sort() {
        Collections.sort(this, (one, another) -> orderGetter.apply(one).compareTo(orderGetter.apply(another)));
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        sort();
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        sort();
        return super.toArray();
    }

    @Override
    public <U> U[] toArray(U[] array) {
        sort();
        return super.toArray(array);
    }
}
