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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * A list that is aware of and enforces the order of its members prescribed by
 * the ordinal number of the media units.
 */
class OrderAwareList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Determines whether the order is ensured when Get is called. During the
     * order check, this value is set to false to avoid an infinite loop.
     */
    private boolean ensureOrderOnGet = true;

    private Function<T, Integer> orderGetter;

    public OrderAwareList(Function<T, Integer> orderGetter) {
        this.orderGetter = orderGetter;
    }

    /**
     * Sorts the list by the media unit’s order value and can handle duplicate
     * occurrences of the same number (unlike a TreeMap). The order of list
     * items is changed only when necessary. The order of list members with the
     * same ordinal number is not affected.
     * 
     * <p>
     * The mathematical complexity of this algorithm is linear (n) if the order
     * is already correct, which is already given in the vast majority of
     * invocations of the method.
     */
    private void ensureOrder() {
        ensureOrderOnGet = false;
        int i = 0;
        while (i < super.size() - 2) {
            if (orderGetter.apply(super.get(i)) <= orderGetter.apply(super.get(i + 1))) {
                i++;
            } else {
                Collections.swap(this, i, i + 1);
                if (i > 0) {
                    i--;
                }
            }
        }
        ensureOrderOnGet = true;
    }

    @Override
    public Iterator<T> iterator() {
        ensureOrder();
        return super.iterator();
    }

    @Override
    public Object[] toArray() {
        ensureOrder();
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        ensureOrder();
        return super.toArray(a);
    }

    @Override
    public boolean add(T e) {
        boolean result = super.add(e);
        ensureOrder();
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(element);
        ensureOrder();
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);
        ensureOrder();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = super.addAll(c);
        ensureOrder();
        return result;
    }

    @Override
    public T get(int index) {
        if (ensureOrderOnGet) {
            ensureOrder();
        }
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        T result = super.set(index, element);
        ensureOrder();
        return result;
    }

    @Override
    public int indexOf(Object o) {
        ensureOrder();
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        ensureOrder();
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        ensureOrder();
        return super.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ensureOrder();
        return super.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        ensureOrder();
        return super.subList(fromIndex, toIndex);
    }
}
