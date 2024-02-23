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

package org.kitodo.api.filemanagement.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Implementation of FileNameFilter for checking the begin and the end of files.
 */
public class FileNameEndsAndDoesNotBeginWithFilter implements FilenameFilter {

    private String notBegin;
    private String end;

    /**
     * Filter files by given begin and end of file.
     *
     * @param notBegin
     *            file name should not start from this String
     * @param end
     *            file name should end with this String
     * @throws IllegalArgumentException
     *             it is thrown in case one or both parameters are null or empty
     *             Strings
     */
    public FileNameEndsAndDoesNotBeginWithFilter(String notBegin, String end) {
        if (notBegin == null || notBegin.isEmpty() || end == null || end.isEmpty()) {
            throw new IllegalArgumentException("No filter or empty filter for file begin or end is given.");
        }
        this.notBegin = notBegin;
        this.end = end;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(end) && !name.startsWith(notBegin);
    }
}
