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

public enum LockingMode {
    /**
     * The user can read and write the file as often as he likes. Other users do
     * not have access to the file in time. Precondition is that the file
     * currently has no other user in use.
     */
    EXCLUSIVE,

    /**
     * The user can read the file as often as he likes. The user can not request
     * UPGRADE_WRITE_ONCE for this file. Other users can edit the file in the
     * meantime. For this purpose, a temporary copy has to be kept in the
     * background for this user, which shows the old state. The precondition is
     * that the file is currently being written by no other user.
     */
    IMMUTABLE_READ,

    /**
     * The user can read the file any number of times and can try to promote to
     * UPGRADE_WRITE_ONCE. Other users can read the file in the meantime but can
     * not write it. Precondition is that the file is currently being written by
     * no other user.
     */
    UPGRADEABLE_READ,

    /**
     * The user can read the file as often as he likes and write once. Once it
     * has written it and the OutputStream has been closed, the lock is
     * automatically reverted to UPGRADEABLE_READ. The precondition is that the
     * file is currently not in use by another user.
     */
    UPGRADE_WRITE_ONCE;
}
