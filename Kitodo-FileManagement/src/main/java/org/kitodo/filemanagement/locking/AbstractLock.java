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

import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.net.URI;
import java.nio.file.AccessDeniedException;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * An abstract superclass for the lock classes that implement the different lock
 * types.
 */
abstract class AbstractLock {
    /**
     * Creates a lock depending on its type.
     *
     * @param uri
     *            URI to be locked
     * @param lockType
     *            which lock should be generated
     * @param owner
     *            who is the owner of the lock
     * @param streamManagement
     *            the stream management
     * @param immutableReadFileManagement
     *            the immutable read file management
     * @return the created lock
     * @throws UncheckedIOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    static AbstractLock createLock(URI uri, LockingMode lockType, String owner, StreamManagement streamManagement,
            ImmutableReadFileManagement immutableReadFileManagement) {

        switch (lockType) {
            case EXCLUSIVE: {
                return new ExclusiveLock(owner, uri, streamManagement, immutableReadFileManagement);
            }
            case IMMUTABLE_READ: {
                return new ImmutableReadLock(owner, uri, immutableReadFileManagement);
            }
            case UPGRADEABLE_READ: {
                return new UpgradeableReadLock(owner, uri, false);
            }
            case UPGRADE_WRITE_ONCE: {
                return new UpgradeableReadLock(owner, uri, true);
            }
            default: {
                throw new IllegalStateException("complete switch");
            }
        }
    }

    /**
     * The URI this lock is on.
     */
    protected final URI uri;

    /**
     * User who owns this lock.
     */
    private final String user;

    /**
     * Constructor for an abstract lock. For use by subclasses.
     *
     * @param user
     *            user who owns this lock
     * @param uri
     *            locked URI
     */
    protected AbstractLock(String user, URI uri) {
        this.user = user;
        this.uri = uri;
    }

    /**
     * Here, the implementing class must return the enumerated constant that
     * corresponds to the lock.
     *
     * @return the enumerated constant that corresponds to the lock
     */
    abstract LockingMode getLockingMode();

    /**
     * Returns the name of the user to whom this lock belongs if the specified
     * other lock can not be granted because this user already has a conflicting
     * lock. Otherwise, null is returned.
     *
     * @param other
     *            which lock should be granted additionally
     * @return username at conflict, otherwise {@code null}
     */
    String hasConflictingUserFor(LockingMode other) {
        return !isCombinableWith(other) ? user : null;
    }

    /**
     * Fails if the user cannot prove the necessary authorization to write. This
     * method is called immediately before write access to the URI is
     * established and serves as protection barrier against invalid access. If
     * the code uses the interface according to its contract, this method should
     * never fail and just finish quietly.
     *
     * @throws AccessDeniedException
     *             If the user does not have appropriate permission or the
     *             permission does not allow write access.
     * @throws ProtocolException
     *             if the file had to be first read in again, but this step was
     *             skipped on the protocol. This error can occur with the
     *             UPGRADE_WRITE_ONCE lock because its protocol form requires
     *             that the file must first be read in again and the input
     *             stream must be closed after the lock has been upgraded.
     */
    abstract void isAllowingToWrite() throws AccessDeniedException, ProtocolException;

    /**
     * Here, the implementation class must specify whether it can be combined
     * with the requested other lock.
     *
     * @param other
     *            the requested other lock
     * @return whether the locks can be combined
     */
    abstract boolean isCombinableWith(LockingMode other);
}
