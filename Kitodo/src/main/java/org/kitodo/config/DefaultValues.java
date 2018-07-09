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

package org.kitodo.config;

import java.util.concurrent.TimeUnit;

/**
 * Hard-coded default values.
 */
public class DefaultValues {

    public static final long ACTIVE_MQ_RESULTS_TTL = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
    public static final long CATALOGUE_TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);
    public static final String DIRECTORY_PREFIX = "orig";
    public static final String DIRECTORY_SUFFIX = "tif";
    public static final String DONE_DIRECTORY_NAME = "fertig/";
    public static final int ELASTICSEARCH_BATCH = 1000;
    public static final String ERR_LINK_TO_PAGE = "./desktop.jsf";
    public static final int HITLIST_PAGE_SIZE = 10;
    public static final int KITODO_CONTENT_SERVER_TIMEOUT = (int) TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
    public static final String IMAGE_PREFIX = "\\d{8}";
    public static final String IMAGE_SORTING = Parameters.IMAGE_SORTING_VALUE_NUMBER;
    public static final String ISSUE_COLOURS = "#CC0000;#0000AA;#33FF00;#FF9900;#5555FF;#006600;#AAAAFF;#000055;#0000FF;#FFFF00;#000000";
    public static final String LANGUAGE_DEFAULT = "de";
    public static final String LANGUAGE_FORCE_DEFAULT = "de";
    public static final String LOCAL_MESSAGES = "/usr/local/kitodo/messages/";
    public static final long METS_EDITOR_LOCKING_TIME = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);
    public static final String PAGE_SEPARATORS = "\" \"";
    public static final long TASK_MANAGER_INSPECTION_INTERVAL_MILLIS = TimeUnit.MILLISECONDS.convert(2,
        TimeUnit.SECONDS);
    public static final int TASK_MANAGER_KEEP_FAILED = 10;
    public static final long TASK_MANAGER_KEEP_FAILED_MINS = TimeUnit.MINUTES.convert(4, TimeUnit.HOURS);
    public static final int TASK_MANAGER_KEEP_SUCCESSFUL = 3;
    public static final long TASK_MANAGER_KEEP_SUCCESSFUL_MINS = TimeUnit.MINUTES.convert(20, TimeUnit.MINUTES);
    public static final String TEMPFOLDER = "/usr/local/kitodo/temp/";
    public static final String VALIDATE_IDENTIFIER_REGEX = "[\\w|-]";
    public static final String VALIDATE_PROCESS_TITLE_REGEX = "[\\w-]*";

}
