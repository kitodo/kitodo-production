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

package org.kitodo.production.services.file;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.model.Subfolder;

/**
 * This class contains a method that can be used to generate multiple subfolder
 * objects with a single method call.
 * 
 * @see Subfolder
 */
public class SubfolderFactoryService {

    /**
     * Convenience function to create a bunch of subfolders in one.
     * 
     * @param process
     *            the process this subfolder belongs to
     * @param folders
     *            The general metrics of the kinds of subfolders to create
     * @return subfolders
     */
    public static List<Subfolder> createAll(Process process, Collection<Folder> folders) {
        return folders.parallelStream().map(folder -> new Subfolder(process, folder)).collect(Collectors.toList());
    }

}
