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

package org.goobi.io;

import java.io.File;
import java.io.FilenameFilter;

public class FileListFilter implements FilenameFilter {

    private String name;

	/**
	 * Filter file list.
     *
	 * @param name String
	 */
    public FileListFilter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("No filter name given.");
        }
        this.name = name;
    }

    @Override
    public boolean accept(File directory, String filename) {
        return filename.matches(name);
    }
}
