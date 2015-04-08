/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.helper;

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

	private final ArrayList<K> keys = new ArrayList<K>();
	private final ArrayList<V> values = new ArrayList<V>();

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
		for (int i = 0; i < keyListSize; i++)
			result |= values.add(value);
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
