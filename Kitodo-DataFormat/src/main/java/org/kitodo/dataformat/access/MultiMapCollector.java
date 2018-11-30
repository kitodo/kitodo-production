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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collects an object stream to a multi-map. A multi-map is a
 * {@code Map<K, Set<V>>}, which can store several objects under one key.
 */
class MultiMapCollector<T, K, V> implements Collector<T, Map<K, Set<V>>, Map<K, Set<V>>> {

    private final Function<T, K> keyAccessor;
    private final Function<T, V> valueAccessor;

    public MultiMapCollector(Function<T, K> keyAccessor, Function<T, V> valueAccessor) {
        this.keyAccessor = keyAccessor;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public BiConsumer<Map<K, Set<V>>, T> accumulator() {
        return (result, element) -> {
            K key = keyAccessor.apply(element);
            if (!result.containsKey(key)) {
                result.put(key, new HashSet<>());
            }
            result.get(key).add(valueAccessor.apply(element));
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>(Arrays.asList(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH));
    }

    @Override
    public BinaryOperator<Map<K, Set<V>>> combiner() {
        return (one, other) -> {
            other.entrySet().parallelStream().forEachOrdered(entry -> {
                if (one.containsKey(entry.getKey())) {
                    one.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    one.put(entry.getKey(), entry.getValue());
                }
            });
            return one;
        };
    }

    @Override
    public Function<Map<K, Set<V>>, Map<K, Set<V>>> finisher() {
        return Function.identity();
    }

    @Override
    public Supplier<Map<K, Set<V>>> supplier() {
        return () -> new HashMap<K, Set<V>>();
    }
}
