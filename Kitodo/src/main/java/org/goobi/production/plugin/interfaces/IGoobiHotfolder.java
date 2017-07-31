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

package org.goobi.production.plugin.interfaces;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.List;

public interface IGoobiHotfolder {

    /**
     * Get URI's list with current files.
     * 
     * @return a list with all xml files in GoobiHotfolder
     */

    List<URI> getCurrentFiles();

    /**
     * Get URI's list with files for exact String name.
     * 
     * @param name
     *            of file
     * @return a list with all file names containing the name in GoobiHotfolder
     */

    List<URI> getFilesByName(String name);

    /**
     * Get URI's list with files' names for exact filter.
     * 
     * @param filter
     *            as FilenameFilter
     * @return a list with all file names matching the filter
     */

    List<URI> getFileNamesByFilter(FilenameFilter filter);

    /**
     * Get URI's list with files for exact filter.
     * 
     * @param filter
     *            as FilenameFilter
     * @return a list with all file matching the filter
     */

    List<URI> getFilesByFilter(FilenameFilter filter);

    /**
     * Get folder as String.
     * 
     * @return hotfolder as string
     */
    String getFolderAsString();

    /**
     * Get folder as File.
     * 
     * @return hotfolder as file
     */
    File getFolderAsFile();

    /**
     * Get folder as URI.
     * 
     * @return hotfolder as URI
     */
    URI getFolderAsUri();

}
