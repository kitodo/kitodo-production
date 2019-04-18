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
 * Implementation of FileNameFilter for checking if file is a directory.
 */
public class IsDirectoryFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory();
    }
}
