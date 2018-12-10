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

import java.net.URI;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * Implements an exclusive lock. An exclusive lock allows full access to the
 * file. At the same time, no other users can use the file. An exception to this
 * is the unchangeable read-only option, where the user has to read a copy that
 * is really always there, unless the copy has to be created first.
 */
class ExclusiveLock extends AbstractLock {

    /**
     * The immutable read file management.
     */
    private final ImmutableReadFileManagement immutableReadFileManagement;

    /**
     * The stream management.
     */
    private final StreamManagement streamManagement;

    /**
     * The locked URI.
     */
    private final URI uri;

    /**
     * Creates a new exclusive lock.
     *
     * @param owner
     *            future owner of the lock
     * @param uri
     *            locked URI
     * @param streamManagement
     *            the stream management
     * @param immutableReadFileManagement
     *            the immutable read file management
     */
    ExclusiveLock(String owner, URI uri, StreamManagement streamManagement,
            ImmutableReadFileManagement immutableReadFileManagement) {
        super(owner, uri);
        this.uri = uri;
        this.streamManagement = streamManagement;
        this.immutableReadFileManagement = immutableReadFileManagement;
    }

    @Override
    LockingMode getLockingMode() {
        return LockingMode.EXCLUSIVE;
    }

    @Override
    void isAllowingToWrite() {
        // An exclusive lock always allows writing. Here is nothing to do.
    }

    /**
     * An exclusive lock cannot be combined with any other lock than with
     * immutable read, which is almost always, unless the URI is being written
     * <i>and</i> the immutable read file management does not already have a
     * copy of the URI.
     */
    @Override
    boolean isCombinableWith(LockingMode other) {
        return other.equals(LockingMode.IMMUTABLE_READ) && (!streamManagement.isKnowingAnOpenOutputStreamTo(uri)
                || immutableReadFileManagement.isHavingACopyOf(uri));
    }
}
