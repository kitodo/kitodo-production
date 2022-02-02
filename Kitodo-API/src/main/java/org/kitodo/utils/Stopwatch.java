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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;

public class Stopwatch {

    private static final String MESSAGE_SUFFIX = " {} {}objects {}took {} ms";

    private String message;
    private int size;
    private String typeInfo = "";
    private String idInfo = "";
    private long startTime;

    /**
     * Creates a new stopwatch. The processed objects are evaluated in order to
     * prepare the details of the log message. The creation time is saved and later
     * used to measure the duration.
     * 
     * @param <T>           type of objects
     * @param <S>           supertype of objects holding a function to get the ID of
     *                      the objects
     * @param messagePrefix description of action measured
     * @param objects       objects
     * @param getIdFunction function to get the ID of the objects
     */
    public <T extends S, S> Stopwatch(String messagePrefix, Collection<T> objects, Function<S, Object> getIdFunction) {
        message = messagePrefix.concat(MESSAGE_SUFFIX);
        setFields(objects, getIdFunction);
        startTime = System.nanoTime();
    }

    /**
     * Creates a new stopwatch.
     * 
     * @param messagePrefix description of action measured
     */
    public Stopwatch(String messagePrefix) {
        message = messagePrefix.concat(MESSAGE_SUFFIX);
        startTime = System.nanoTime();
    }

    private final <T extends S, S> void setFields(Collection<T> objects, Function<S, Object> getIdFunction) {
        size = objects.size();
        Iterator<T> iterator = objects.iterator();
        if (iterator.hasNext()) {
            T firstObject = iterator.next();
            typeInfo = firstObject.getClass().getSimpleName().concat(" ");
            if (Objects.nonNull(getIdFunction)) {
                String firstId = Objects.toString(getIdFunction.apply(firstObject));
                if (iterator.hasNext()) {
                    T lastObject = null;
                    while (iterator.hasNext()) {
                        lastObject = iterator.next();
                    }
                    String lastId = Objects.toString(getIdFunction.apply(lastObject));
                    idInfo = "(IDs " + firstId + ".." + lastId + ") ";
                } else {
                    idInfo = "(ID " + firstId + ") ";
                }
            }
        }
    }

    /**
     * Print measured time to log.
     * 
     * @param logger        logger to write to
     */
    public void log(Logger logger) {
        long timeSpent = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        logger.info(message, size, typeInfo, idInfo, timeSpent);
    }

    /**
     * Print measured time to log.
     * 
     * @param <T>           type of objects
     * @param <S>           supertype of objects holding a function to get the ID of
     *                      the objects
     * @param objects       objects
     * @param getIdFunction function to get the ID of the objects
     * @param logger        logger to write to
     */
    public <T extends S, S> void log(Collection<T> objects, Function<S, Object> getIdFunction, Logger logger) {
        long timeSpent = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        setFields(objects, getIdFunction);
        logger.info(message, size, typeInfo, idInfo, timeSpent);
    }
}
