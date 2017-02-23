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

package org.kitodo.api.filemanagement;

import java.io.File;
import java.nio.file.Path;

public interface FileManagementInterface {

    /**
     * Retrieves a File from a given path.
     * @param path The path, to get the File from.
     * @return The retrieved file.
     */
    File retrieve(Path path);

    /**
     * Saves a given File to a given Path.
     *
     * @param file the file to be saved.
     * @param path the path, to save the file to.
     * @return true, if saving was successfull, false if an error occured.
     */
    boolean save(File file, Path path);

}
