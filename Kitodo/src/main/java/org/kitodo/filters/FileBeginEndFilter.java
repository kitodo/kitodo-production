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

package org.kitodo.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Implementation of FileNameFilter for checking the begin and the end of files.
 */
public class FileBeginEndFilter implements FilenameFilter {

    private String begin;
    private String end;

    /**
     * Filter files by given begin and end of file.
     *
     * @param end
     *            String
     */
    public FileBeginEndFilter(String begin, String end) {
        if (begin == null || end == null) {
            throw new IllegalArgumentException("No filter for file begin or end given.");
        }
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean accept(File dir, String name) {
        return (name.endsWith(end) && name.startsWith(begin));
    }
}
