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

package org.kitodo.exceptions;

import java.util.Objects;

/**
 * Provides static functions for monitoring method parameters. If not, a verbose
 * exception is thrown, which can make debugging easier.
 * 
 * @see "https://www.heise.de/-9241426"
 */
public class Guard {

    private Guard() {
        // do not instantiate
    }

    /**
     * Monitors a parameter for a matching type, otherwise throws an
     * {@code IllegalArgumentException}. Returns the object cast, so as not to
     * require a new {@code instanceof} check to satisfy the compiler.
     * 
     * @param <T>
     *            target type to cast to
     * @param name
     *            name of the parameter to be monitored
     * @param value
     *            value of the parameter to be monitored
     * @param castClass
     *            target class to cast to
     * @return the cast object
     * @throws IllegalArgumentException
     *             if the cast is inadmissible
     */
    @SuppressWarnings("unchecked")
    public static <T> T canCast(String name, Object value, Class<T> castClass) {
        Class<? extends Object> valueClass = value.getClass();
        if (castClass.isAssignableFrom(valueClass)) {
            return (T) value;
        } else {
            String message = valueClass.getName() + " '" + name + "' is not a (subclass of) " + castClass.getName();
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Monitors an integer parameter for being in a specified range, otherwise
     * throws an {@code IllegalArgumentException}.
     * 
     * @param name
     *            name of the parameter to be monitored
     * @param value
     *            value of the parameter to be monitored
     * @param min
     *            smallest permissible value
     * @param max
     *            highest allowable value
     * @throws IllegalArgumentException
     *             if the parameter is out of range
     */
    public static void isInRange(String name, long value, long min, long max) {
        if (value < min || value > max) {
            String message = '\'' + name + "' out of range: " + value + " not in [" + min + ".." + max + "]";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Monitors an integer parameter for being not {@code null}, otherwise
     * throws an {@code IllegalArgumentException}.
     * 
     * @param name
     *            name of the parameter to be monitored
     * @param value
     *            value of the parameter to be monitored
     * @throws IllegalArgumentException
     *             if the parameter is {@code null}
     */
    public static void isNotNull(String name, Object value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException('\'' + name + "' must not be null");
        }
    }

    /**
     * Monitors a floating-point parameter for being positive, otherwise throws
     * an {@code IllegalArgumentException}.
     * 
     * @param name
     *            name of the parameter to be monitored
     * @param value
     *            value of the parameter to be monitored
     * @throws IllegalArgumentException
     *             if the parameter is not positive
     */
    public static void isPositive(String name, double value) {
        if (Double.isNaN(value) || value <= 0.0) {
            String message = '\'' + name + "' out of range: " + value + " not > 0";
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Monitors an integer parameter for being positive, otherwise throws an
     * {@code IllegalArgumentException}.
     * 
     * @param name
     *            name of the parameter to be monitored
     * @param value
     *            value of the parameter to be monitored
     * @throws IllegalArgumentException
     *             if the parameter is not positive
     */
    public static void isPositive(String name, long value) {
        if (value <= 0) {
            String message = '\'' + name + "' out of range: " + value + " not > 0";
            throw new IllegalArgumentException(message);
        }
    }
}
