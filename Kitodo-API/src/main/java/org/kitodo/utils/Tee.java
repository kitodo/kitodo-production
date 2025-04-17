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

package org.kitodo.utils;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A tee for object streams, allowing objects to be diverted midstream for
 * secondary use.
 * 
 * @param <T>
 *            type of stream objects
 * @see "https://stackoverflow.com/q/73470158"
 */
public class Tee<T> implements Predicate<T> {
    private final Consumer<? super T> consumer;

    public Tee(Consumer<? super T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean test(T t) {
        consumer.accept(t);
        return true;
    }
}
