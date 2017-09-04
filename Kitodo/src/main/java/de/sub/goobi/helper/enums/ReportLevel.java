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

package de.sub.goobi.helper.enums;

/**
 * These are the possible states for output to “activeMQ.results.topic”.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public enum ReportLevel {
    FATAL,
    ERROR,
    WARN,
    INFO,
    SUCCESS,
    DEBUG,
    VERBOSE,
    LUDICROUS;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
