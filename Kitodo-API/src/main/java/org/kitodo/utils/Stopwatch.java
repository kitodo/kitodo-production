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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;

public class Stopwatch {

    private Class<?> executor;
    private Object object;
    private String functionName;
    private String[] args;
    private long start;

    /**
     * Creates a new stopwatch.
     * 
     * @param object
     *            object for which the time should be taken
     * @param functionName
     *            function whose runtime is measured
     * @param args
     *            function arguments, optional. Specify them in pairs: string
     *            name, string value, string name, string value, etc.
     */
    public Stopwatch(Object object, String functionName, String... args) {
        this.executor = object.getClass();
        this.object = object;
        this.functionName = functionName;
        this.args = args;
        this.start = System.nanoTime();
    }

    /**
     * Creates a new stopwatch.
     * 
     * @param executor
     *            object class for which the time should be taken
     * @param object
     *            processed object
     * @param functionName
     *            function whose runtime is measured
     * @param args
     *            function arguments, optional. Specify them in pairs: string
     *            name, string value, string name, string value, etc.
     */
    public Stopwatch(Class<?> executor, Object object, String functionName, String... args) {
        this.executor = executor;
        this.object = object;
        this.functionName = functionName;
        this.args = args;
        this.start = System.nanoTime();
    }

    /**
     * Take the time and leave it in the log.
     */
    public void stop() {
        long millis = MILLISECONDS.convert(System.nanoTime() - this.start, NANOSECONDS);
        if (millis > 0) {
            StringBuilder args = new StringBuilder();
            if (this.args != null) {
                short mater = 0;
                for (String arg : this.args) {
                    switch (mater) {
                        case 1:
                            args = args.append(": ");
                            break;
                        case 2:
                            args = args.append(", ");
                            mater = 0;
                            break;
                        default:
                    }
                    int argLength = Objects.nonNull(arg) ? arg.length() : 0;
                    for (int pos = 0; pos < argLength;) {
                        int codePoint = arg.codePointAt(pos);
                        if (codePoint >= ' ' && codePoint <= '~' || codePoint >= '¡' && codePoint <= 'ÿ'
                                || Character.isLetterOrDigit(codePoint)) {
                            args.appendCodePoint(codePoint);
                        } else if (Character.isWhitespace(codePoint)) {
                            args.append(' ');
                        } else {
                            args.append(65533);
                        }
                        pos += Character.charCount(codePoint);
                    }
                    mater++;
                }
            }
            String objectName = Objects.toString(object);
            LogManager.getLogger(executor).trace("{}: {}({}) took {} ms",
                    objectName.contains("@") ? object.getClass().getSimpleName() : objectName,
                    functionName, args, millis);
        }
    }

    public <T> T stop(T t) {
        stop();
        return t;
    }
}
