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

package org.goobi.production.cli.helper;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;

public class WikiFieldHelper {

    private static final Logger logger = Logger.getLogger(WikiFieldHelper.class);


    private static final String TAG_ERROR = "<font color=\"#FF0000\">";
    private static final String TAG_WARN = "<font color=\"#FF6600\">";
    private static final String TAG_INFO = "<font color=\"#0033CC\">";
    private static final String TAG_DEBUG = "<font color=\"#CCCCCC\">";
    private static final String TAG_USER = "<font color=\"#006600\">";
    private static final String ENDTAG = "</font>";

    private static final String BREAK = "<br/>";

    public static String getWikiMessage(Prozess p, String currentWikifieldcontent, String type, String value) {
        String message = "";
        if (currentWikifieldcontent != null && currentWikifieldcontent.length() > 0) {
            message += currentWikifieldcontent;
            message += BREAK;
        }

        if (type.equals("error")) {
            message += TAG_ERROR;
        } else if (type.equals("debug")) {
            message += TAG_DEBUG;
        } else if (type.equals("user")) {
            message += TAG_USER;
        } else if (type.equals("warn")) {
            message += TAG_WARN;
        }else {
            message += TAG_INFO;
        }

        String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date());
        String processname = "";
        if (p != null) {
            processname = "processname: " + p.getTitel() + ", message: ";
        }
        if(logger.isInfoEnabled()){
            logger.info(timestamp + " " + processname + " " + value );
        }
        message = message + timestamp + ": " + value + ENDTAG;
        return message;
    }


    public static String getWikiMessage(String currentWikifieldcontent, String type, String value) {
        String message = "";
        if (currentWikifieldcontent != null && currentWikifieldcontent.length() > 0) {
            message += currentWikifieldcontent;
            message += BREAK;
        }

        if (type.equals("error")) {
            message += TAG_ERROR;
        } else if (type.equals("debug")) {
            message += TAG_DEBUG;
        } else if (type.equals("user")) {
            message += TAG_USER;
        } else if (type.equals("warn")) {
            message += TAG_WARN;
        }else {
            message += TAG_INFO;
        }

        String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date());

        message = message + timestamp + ": " + value + ENDTAG;
        return message;
    }
}
