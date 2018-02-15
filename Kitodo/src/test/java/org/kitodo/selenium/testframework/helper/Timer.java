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

package org.kitodo.selenium.testframework.helper;

public class Timer {

    private Long startTime;
    private Long stopTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() throws IllegalAccessException {
        if (startTime != null) {
            stopTime = System.currentTimeMillis();
        } else {
            throw new IllegalAccessException("Timer is not running");
        }
    }

    public float getElapsedTimeAfterStartSec() {
        return (System.currentTimeMillis() - startTime) / 1000F;
    }

    public float getElapsedTimeSec() {
        return (stopTime - startTime) / 1000F;
    }
}
