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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * Defines an interface to manage allocated locks or obtain information about
 * conflict originators in case of error.
 */
public interface LockResultProtocol extends AutoCloseable {
    /**
     * Calling close() unlocks all locks. The method is inherited from the
     * AutoCloseable interface, which means that calling tryLock() on the
     * FileManagementInterface can be done in a try-with-resources statement.
     * 
     * @throws IllegalStateException
     *             if a lock cannot be returned because there are still streams
     *             through this lock open
     */
    @Override
    void close();

    /**
     * Releases a single lock.
     * 
     * @param uri
     *            file whose lock is to be released
     * @throws IllegalStateException
     *             if a lock cannot be returned because there are still streams
     *             through this lock open
     */
    void close(URI uri);

    /**
     * If the request for locks failed, this method tells which users or
     * processes are holding locks on files that caused the request to fail. If
     * successful, the map is empty.
     * 
     * @return which users hold locks on which files
     */
    Map<URI, Collection<String>> getConflicts();

    /**
     * Convenience method to check if the locking was successful.
     * 
     * @return if the locking was successful
     */
    default boolean isSuccessful() {
        return getConflicts().isEmpty();
    }

    /**
     * Tries to request more locks. The method can be used to request
     * UPGRADE_WRITE_ONCE as well as to lock completely new files. If
     * successful, the locks will be added to the current LockingResult and the
     * returned map will be empty. Failure returns a map that can tell which
     * users or processes are holding locks on the requested files.
     * 
     * @param requests
     *            the locks to request
     * @return map telling which users or processes are holding locks on files
     *         that caused the request to fail, empty on success
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    Map<URI, Collection<String>> tryLock(Map<URI, LockingMode> requests) throws IOException;
}
