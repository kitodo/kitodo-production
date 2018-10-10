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

import java.net.ProtocolException;
import java.net.URI;
import java.nio.file.AccessDeniedException;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * A read lock that may, under certain circumstances, receive a one-time upgrade
 * to write.
 */
class UpgradeableReadLock extends AbstractLock {
    /**
     * This lock is so complicated that it has been implemented as a state
     * machine. This variable contains the current state.
     */
    private UpgradeableReadLockState state;

    /**
     * Creates a new upgradeable read lock.
     *
     * @param owner
     *            future owner of the lock
     * @param uri
     *            locked URI
     * @param write
     *            whether the lock is currently in write mode
     */
    UpgradeableReadLock(String owner, URI uri, boolean write) {
        super(owner, uri);
        state = write ? UpgradeableReadLockState.WRITING : UpgradeableReadLockState.READ;
    }

    @Override
    LockingMode getLockingMode() {
        return state.toLockingMode();
    }

    @Override
    void isAllowingToWrite() throws AccessDeniedException, ProtocolException {
        if (state.equals(UpgradeableReadLockState.READ)) {
            throw new AccessDeniedException(uri.toString(), null,
                    "It is forbidden to open an output stream via an upgradeable read lock in read mode.");
        } else if (!state.equals(UpgradeableReadLockState.EXPECTING_WRITE)) {
            throw new ProtocolException(
                    uri.toString() + ": Contract violated. You must reread the file before you can write it.");
        }
    }

    @Override
    boolean isCombinableWith(LockingMode other) {
        return state.isCombinableWith(other);
    }

    /**
     * Tells the upgradeable read lock that reading over it has begun. If the
     * lock has been upgraded and has waited for the file to be read in again,
     * it switches to the condition that the file can now be written.
     */
    void noteReadingStarts() {
        if (state.equals(UpgradeableReadLockState.EXPECTING_REREAD)) {
            state = UpgradeableReadLockState.EXPECTING_WRITE;
        }
    }

    /**
     * Tells the upgradeable read lock that writing over it has finished.
     */
    void noteWritingEnds() {
        state = UpgradeableReadLockState.READ;
    }

    /**
     * Tells the upgradeable read lock that writing over it has begun.
     */
    void noteWritingStarts() {
        state = UpgradeableReadLockState.WRITING;
    }

    /**
     * Starts the upgrade procedure.
     */
    void upgradeForAOneTimeWrite() {
        if (state.equals(UpgradeableReadLockState.READ)) {
            state = UpgradeableReadLockState.EXPECTING_REREAD;
        }
    }
}
