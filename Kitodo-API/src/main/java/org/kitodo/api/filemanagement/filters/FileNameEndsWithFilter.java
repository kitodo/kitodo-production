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
 * Implementation of FileNameFilter for checking the end of files.
 */
public class FileNameEndsWithFilter implements FilenameFilter {

    private String end;

    /**
     * Filter files by given file end.
     *
     * @param end
     *            file name should end with this String
     * @throws IllegalArgumentException
     *             it is thrown in case parameter is null or empty String
     */
    public FileNameEndsWithFilter(String end) {
        if (end == null || end.isEmpty()) {
            throw new IllegalArgumentException("No filter or empty filter for file end is given.");
        }
        this.end = end;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(end);
    }
}
