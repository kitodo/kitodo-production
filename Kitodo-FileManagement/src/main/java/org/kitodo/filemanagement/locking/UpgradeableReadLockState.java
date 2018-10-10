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

import java.util.EnumMap;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * The possible states of an upgradeable read lock. This type of lock is so
 * complicated that it has been implemented as a state machine. And yet, it is
 * not that complicated for a state machine, at least. The state machine has
 * four states that change in a circular fashion. It starts in the read state.
 * If an upgrade write once was granted, the state changes to expecting reread.
 * As soon as an input stream is opened on the file, the lock changes to the
 * third state reread. If the input stream is closed, it goes into the fourth
 * state write once. As soon as an output stream is opened it falls back to the
 * original state read.
 *
 * <p>
 * Figure:
 *
 * <pre>
 *  /======\      upgrade was granted     /------------------\
 *  | READ | ---------------------------> | EXPECTING_REREAD |
 *  \======/                              \------------------/
 *      ^                                          |
 *      | output stream                            | input stream
 *      | was closed                               | was opened
 *      |                                          v
 * /---------\   output stream was opened  /-----------------\
 * | WRITING | <-------------------------- | EXPECTING_WRITE |
 * \---------/                             \-----------------/
 * </pre>
 */
enum UpgradeableReadLockState {
    /**
     * If an upgrade write once has been granted, the file must first be read in
     * again. In this state, the lock waits for an input stream to be opened to
     * re-read the file.
     */
    EXPECTING_REREAD(true, false),

    /**
     * If an upgrade write once has been granted, the file must first be read in
     * again. In this state, the lock waits for the input stream to re-read the
     * file to be closed again.
     */
    EXPECTING_WRITE(true, false),

    /**
     * By default, this lock just allows reading.
     */
    READ(true, true),

    /**
     * In this state, after successfully reloading the file, the lock waits for
     * the file to be written.
     */
    WRITING(false, false);

    /**
     * This variable stores which locks can be combined with the lock in the
     * respective state of the lock.
     */
    private final EnumMap<LockingMode, Boolean> combinationMatrix = new EnumMap<>(LockingMode.class);

    /**
     * Creates the upgradeable read lock state members.
     *
     * @param canImmutableRead
     *            whether other users can be granted immutable read locks in
     *            this state
     * @param canUpgradeWriteOnce
     *            whether other users can be granted upgrade write once in this
     *            state
     */
    UpgradeableReadLockState(boolean canImmutableRead, boolean canUpgradeWriteOnce) {
        combinationMatrix.put(LockingMode.EXCLUSIVE, false);
        combinationMatrix.put(LockingMode.IMMUTABLE_READ, canImmutableRead);
        combinationMatrix.put(LockingMode.UPGRADE_WRITE_ONCE, canUpgradeWriteOnce);
        combinationMatrix.put(LockingMode.UPGRADEABLE_READ, true);
    }

    /**
     * Returns whether another lock can be combined with an upgradeable read
     * lock in this state.
     *
     * @param other
     *            the other lock for which the question is asked
     * @return whether another lock can be combined
     */
    boolean isCombinableWith(LockingMode other) {
        return combinationMatrix.get(other);
    }

    /**
     * Converts the current state of the lock into the associated identification
     * constant from the API.
     *
     * @return the state identification constant
     */
    LockingMode toLockingMode() {
        return equals(READ) ? LockingMode.UPGRADEABLE_READ : LockingMode.UPGRADE_WRITE_ONCE;
    }
}
