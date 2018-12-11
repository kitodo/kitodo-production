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

package org.kitodo.helper;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;

public class WikiFieldHelper {

    private static final Logger logger = LogManager.getLogger(WikiFieldHelper.class);

    private static final String TAG_ERROR = "<font color=\"#FF0000\">";
    private static final String TAG_WARN = "<font color=\"#FF6600\">";
    private static final String TAG_INFO = "<font color=\"#0033CC\">";
    private static final String TAG_DEBUG = "<font color=\"#CCCCCC\">";
    private static final String TAG_USER = "<font color=\"#006600\">";
    private static final String TAG_END = "</font>";

    private static final String BREAK = "<br/>";

    private WikiFieldHelper() {

    }

    /**
     * Get wiki messages.
     *
     * @param process
     *            Process object
     * @param currentWikiFieldContent
     *            String
     * @param type
     *            String
     * @param value
     *            String
     * @return String
     */
    public static String getWikiMessage(Process process, String currentWikiFieldContent, String type, String value) {
        String message = "";
        if (currentWikiFieldContent != null && currentWikiFieldContent.length() > 0) {
            message += currentWikiFieldContent;
            message += BREAK;
        }

        message += addMatchingTagMessage(type);

        String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date());
        String processName = "";
        if (Objects.nonNull(process)) {
            processName = "processname: " + process.getTitle() + ", message: ";
        }
        logger.info("{} {} {}", timestamp, processName, value);
        message = message + timestamp + ": " + value + TAG_END;
        return message;
    }

    /**
     * Get wiki messages.
     *
     * @param currentWikiFieldContent
     *            String
     * @param type
     *            String
     * @param value
     *            String
     * @return String
     */
    public static String getWikiMessage(String currentWikiFieldContent, String type, String value) {
        return WikiFieldHelper.getWikiMessage(null, currentWikiFieldContent, type, value);
    }

    private static String addMatchingTagMessage(String type) {
        String message;

        switch (type) {
            case "error":
                message = TAG_ERROR;
                break;
            case "debug":
                message = TAG_DEBUG;
                break;
            case "user":
                message = TAG_USER;
                break;
            case "warn":
                message = TAG_WARN;
                break;
            default:
                message = TAG_INFO;
                break;
        }

        return message;
    }
}
