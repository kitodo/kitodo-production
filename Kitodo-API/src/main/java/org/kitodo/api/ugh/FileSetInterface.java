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
 * A {@code FileSet} contains all content files which belong to a digital
 * document. The class provides methods to add or remove content file objects.
 * Each content file object can only be added once. Same is true for the file
 * set.
 *
 * <p>
 * Beside grouping content files, a file set can store meta-data. This meta-data
 * is valid for the all content files. In opposite to the document structure objects,
 * there is no validation when adding meta-data objects to a file set. A file set
 * can contain any and as many meta-data as desired.
 */
public interface FileSetInterface {
    /**
     * Adds a content file object to the file set, if it is not yet existing.
     *
     * @return always {@code true}. The result value is never used.
     */
    boolean addFile(ContentFileInterface contentFile);

    /**
     * Adds a virtual file group.
     * 
     * @param virtualFileGroup
     *            virtual file group to add
     */
    void addVirtualFileGroup(VirtualFileGroupInterface virtualFileGroup);

    /**
     * Returns an iterable over all content files of this file set.
     * 
     * @return an iterable over all content files of this file set
     */
    Iterable<ContentFileInterface> getAllFiles();

    /**
     * Removes a content file from the file set.
     * 
     * @return always {@code true}. The result value is never used.
     */
    boolean removeFile(ContentFileInterface contentFileInterface);

}
