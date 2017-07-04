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
 * Implementation of FileNameFilter for checking the end of files.
 */
public class FileEndFilter implements FilenameFilter {

    private String end;

    /**
     * Filter files by given file end.
     *
     * @param end
     *            String
     */
    public FileEndFilter(String end) {
        if (end == null) {
            throw new IllegalArgumentException("No filter for file end given.");
        }
        this.end = end;
    }

    @Override
    public boolean accept(File dir, String name) {
        return (name.endsWith(end));
    }
}
