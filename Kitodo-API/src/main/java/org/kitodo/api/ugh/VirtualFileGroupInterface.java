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

package org.kitodo.api.ugh;

/**
 * A {@code VirtualFileGroup} contains all file groups needed for the METS/MODS
 * export writer.
 */
public interface VirtualFileGroupInterface {

    /**
     * Sets the file suffix of the virtual file group.
     *
     * @param suffix
     *            file suffix to set
     */
    void setFileSuffix(String suffix);

    /**
     * Sets the internet MIME type of the virtual file group.
     *
     * @param mimeType
     *            the MIME type
     */
    void setMimetype(String mimeType);

    /**
     * Sets the name of the virtual file group.
     *
     * @param name
     *            name to set
     */
    void setName(String name);

    /**
     * Sets the ordinary of the virtual file group.
     *
     * @param ordinary
     *            ordinary to set
     */
    void setOrdinary(boolean ordinary);

    /**
     * Sets the path to files of the virtual file group.
     *
     * @param path
     *            path to set
     */
    void setPathToFiles(String path);
}
