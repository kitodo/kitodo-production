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

package org.kitodo.filemanagement.locking;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enables you to create a map in a stream in a way that allows the map entries
 * to be created in parallel first and then merged into a map.
 * 
 * @param <K>
 *            type of the future map key
 * @param <V>
 *            type of the future map value
 */
public class FutureMapEntry<K, V> {
    /**
     * Key of the future map entry.
     */
    private final K key;

    /**
     * Value of the future map entry.
     */
    private final V value;

    /**
     * Creates a new entry for the future map.
     * 
     * @param key
     *            key of the future map entry
     * @param value
     *            value of the future map entry
     */
    public FutureMapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of the future map entry.
     * 
     * @return the key of the future map entry
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of the future map entry.
     * 
     * @return the value of the future map entry
     */
    public V getValue() {
        return value;
    }

    /**
     * Captures a flow of map entries into a map.
     *
     * @param entryStream
     *            stream of conflicts map entries (future map entries)
     * @return a map containing the pairs
     */
    public static <K, V> Map<K, V> toMap(Stream<FutureMapEntry<K, V>> entryStream) {
        return entryStream.collect(Collectors.toMap(FutureMapEntry::getKey, FutureMapEntry::getValue));
    }
}
