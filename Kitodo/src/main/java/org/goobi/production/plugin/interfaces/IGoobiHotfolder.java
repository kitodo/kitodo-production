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
	 * @return a list with all xml files in GoobiHotfolder
	 */

	public List<File> getCurrentFiles();

	/**
	 * @param name add description
	 * @return a list with all filenames containing the name in GoobiHotfolder
	 */

	public List<String> getFilesByName(String name);

	/**
	 * @param filter add description
	 * @return a list with all filenames matching the filter
	 */

	public List<String> getFileNamesByFilter(FilenameFilter filter);

	/**
	 * @param filter add description
	 * @return a list with all file matching the filter
	 */

	public List<File> getFilesByFilter(FilenameFilter filter);

	/**
	 * @return hotfolder as string
	 */
	public String getFolderAsString();

	/**
	 * @return hotfolder as file
	 */
	public File getFolderAsFile();

	/**
	 * @return hotfolder as URI
	 */
	public URI getFolderAsUri();

}
