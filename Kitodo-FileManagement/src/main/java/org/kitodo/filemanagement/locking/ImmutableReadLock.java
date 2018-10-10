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

package org.kitodo.filemanagement.locking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * An immutable read lock.
 */
class ImmutableReadLock extends AbstractLock {
    /**
     * URI of the immutable read copy held in this lock.
     */
    private URI immutableReadCopyURI;

    /**
     * Creates a new immutable read lock.
     *
     * @param owner
     *            future owner of the lock
     * @param uri
     *            URI for which the lock was granted
     * @param immutableReadFileManagement
     *            the immutable read file management, which provides the
     *            immutable read file
     * @throws UncheckedIOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    ImmutableReadLock(String owner, URI uri, ImmutableReadFileManagement immutableReadFileManagement) {
        super(owner, uri);
        try {
            immutableReadCopyURI = immutableReadFileManagement.getImmutableReadCopy(owner, uri);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the URI of the immutable read file.
     * 
     * @return the URI of the immutable read file
     */
    URI getImmutableReadCopyURI() {
        return immutableReadCopyURI;
    }

    @Override
    LockingMode getLockingMode() {
        return LockingMode.IMMUTABLE_READ;
    }

    @Override
    void isAllowingToWrite() throws AccessDeniedException {
        throw new AccessDeniedException(uri.toString(), null,
                "It is forbidden to open an output stream via an immutable read lock.");
    }

    /**
     * An immutable read lock can be combined with all types of locks at any
     * time. That’s the ingenious thing about it. Because it basically does not
     * refer to the URI at all, but always starts from the copy. But the
     * question has to be asked here, so here is the answer.
     *
     * @return always {@code true}
     */
    @Override
    boolean isCombinableWith(LockingMode other) {
        return true;
    }

}
