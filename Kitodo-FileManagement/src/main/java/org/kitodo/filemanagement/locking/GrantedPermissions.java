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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * For each URI, an instance of this class will list which permissions have been
 * granted to whom.
 */
class GrantedPermissions {
    /**
     * Which user holds which locks on the object.
     */
    private final Map<String, Collection<AbstractLock>> locks = new ConcurrentHashMap<>();

    /**
     * URI for which this object manages the granted locks.
     */
    private final URI uri;

    /**
     * Creates a new container for granted permissions.
     *
     * @param uri
     *            URI for which the permissions were granted
     */
    GrantedPermissions(URI uri) {
        this.uri = uri;
    }

    /**
     * Adds a granted lock. This method must assume that checkLockability has
     * previously checked that the lock may be granted and that since then this
     * object has not been modified by another thread.
     *
     * @param user
     *            user who wants the lock
     * @param which
     *            which lock is desired
     * @param streamManagement
     *            the stream management
     * @param immutableReadFileManagement
     *            the immutable read file management
     * @param rights
     *            previously granted permissions. These are needed in the case
     *            to grant an upgrade write once to an existing upgradeable read
     *            lock.
     * @return the newly created lock
     * @throws UncheckedIOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    AbstractLock addAGrantedLock(String user, LockingMode which, StreamManagement streamManagement,
            ImmutableReadFileManagement immutableReadFileManagement, GrantedAccess rights) {

        locks.computeIfAbsent(user, create -> new LinkedList<>());
        Collection<AbstractLock> locksForUser = locks.get(user);
        AbstractLock lock;
        if (Objects.nonNull(rights) && which.equals(LockingMode.UPGRADE_WRITE_ONCE)
                && rights.getLock(uri) instanceof UpgradeableReadLock) {

            lock = rights.getLock(uri);
            ((UpgradeableReadLock) lock).upgradeForAOneTimeWrite();
        } else {
            lock = AbstractLock.createLock(uri, which, user, streamManagement, immutableReadFileManagement);
        }
        locksForUser.add(lock);
        return lock;
    }

    /**
     * Checks if the user only claims to have a lock that he actually has.
     *
     * @param user
     *            user to be examined
     * @param lock
     *            claimed lock
     * @param write
     *            whether to check if write access is required
     * @throws AccessDeniedException
     *             if the user has tried to cheat
     * @throws ProtocolException
     *             if the file had to be first read in again, but this step was
     *             skipped on the protocol. This error can occur with the
     *             UPGRADE_WRITE_ONCE lock because its protocol form requires
     *             that the file must first be read in again and the input
     *             stream must be closed after the lock has been upgraded.
     */
    void checkAuthorization(String user, AbstractLock lock, boolean write)
            throws AccessDeniedException, ProtocolException {
        Collection<AbstractLock> userLocks = locks.get(user);
        if (userLocks == null || !userLocks.contains(lock)) {
            throw new AccessDeniedException(user + " claims to have a privilege to " + uri
                    + " he does not have at all. Whatever he did, it was wrong.");
        }
        if (write) {
            lock.isAllowingToWrite();
        }
    }

    /**
     * Checks whether the lock may be granted.
     *
     * @param user
     *            user who wants the lock
     * @param requestedLock
     *            which lock is desired
     * @return a collection of the names of all users who hold locks that
     *         conflict with the desired lock
     */
    Collection<String> checkLockability(String user, LockingMode requestedLock) {
        Stream<Entry<String, Collection<AbstractLock>>> locksByOtherUsers = locks.entrySet().parallelStream()
                .filter(entry -> !user.equals(entry.getKey()));
        Stream<String> conflictingUsers = locksByOtherUsers.flatMap(entry -> entry.getValue().parallelStream())
                .map(lock -> lock.hasConflictingUserFor(requestedLock)).filter(Objects::nonNull);
        return conflictingUsers.collect(Collectors.toSet());
    }

    /**
     * Returns whether the permissions object no longer contains any
     * permissions.
     * 
     * @return whether there aren’t any permissions
     */
    boolean isEmpty() {
        return locks.isEmpty();
    }

    /**
     * Removes a lock stored for a user. If the user no longer holds any locks
     * thereafter, the management object for the user is also deleted.
     * 
     * @param user
     *            user to which belongs the lock
     * @param lock
     *            the lock to be removed
     */
    void remove(String user, AbstractLock lock) {
        Collection<AbstractLock> userLocks = locks.get(user);
        userLocks.remove(lock);
        if (userLocks.isEmpty()) {
            locks.remove(user);
        }
    }
}
