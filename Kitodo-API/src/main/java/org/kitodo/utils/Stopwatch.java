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

import org.apache.logging.log4j.LogManager;

public class Stopwatch {

    private Class<?> executor;
    private Object object;
    private String functionName;
    private String[] args;
    private long start;

    public Stopwatch(Object object, String functionName, String... args) {
        this.executor = object.getClass();
        this.object = object;
        this.functionName = functionName;
        this.args = args;
        this.start = System.nanoTime();
    }

    public Stopwatch(Class<?> executor, Object object, String functionName, String... args) {
        this.executor = executor;
        this.object = object;
        this.functionName = functionName;
        this.args = args;
        this.start = System.nanoTime();
    }

    public void stop() {
        long millis = MILLISECONDS.convert(System.nanoTime() - this.start, NANOSECONDS);
        String args = "";
        if (this.args != null) {
            short mater = 0;
            for (String arg : this.args) {
                switch (mater) {
                    case 1:
                        args = args.concat(": ");
                        break;
                    case 2:
                        args = args.concat(", ");
                        mater = 0;
                    default:
                }
                args = args.concat(arg);
                mater++;
            }
        }
        String objectName = object.toString();
        LogManager.getLogger(executor).trace("{}: {}({}) took {} ms", objectName.contains("@") ? object.getClass()
                .getSimpleName() : objectName, functionName, args, millis);
    }

    public <T> T stop(T t) {
        stop();
        return t;
    }
}
