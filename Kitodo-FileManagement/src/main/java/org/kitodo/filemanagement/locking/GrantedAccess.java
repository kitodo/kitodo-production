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
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;

/**
 * Access privileges granted to a user in one or more consecutive requests. This
 * object does not necessarily reflect all the access permissions of a user, but
 * only those from the queries that have led to this object or with which it has
 * been extended.
 */
public class GrantedAccess extends LockResult {
    /**
     * The lock management.
     */
    private final LockManagement lockManagement;

    /**
     * The locks granted to the user in this action. The user can still hold
     * more locks that are managed in other lock objects. If close is called on
     * this object, these locks are reset (not all). If a user requests multiple
     * locks on the same file in multiple places, the lock will not be returned
     * until all lock objects have been closed.
     */
    private final Map<URI, AbstractLock> locks;

    /**
     * Whether the access closes automatically after a stream granted has been
     * closed. This one-time use is an aid and should not be the default way.
     */
    private boolean selfClosing = false;

    /**
     * Which user owns this access.
     */
    private final String user;

    /**
     * Creates a new granted lock object with the locks granted to the user.
     *
     * @param user
     *            which user owns this access
     * @param locks
     *            the locks granted
     * @param lockManagement
     *            the lock management
     *
     */
    GrantedAccess(String user, Map<URI, AbstractLock> locks, LockManagement lockManagement) {
        this.user = user;
        this.locks = locks;
        this.lockManagement = lockManagement;
    }

    /**
     * Unlocks all locks. The method is inherited from the AutoCloseable
     * interface, which means that calling tryLock() on the
     * FileManagementInterface can be done in a try-with-resources statement.
     *
     * @throws IllegalStateException
     *             if a lock cannot be returned because there are still streams
     *             through this lock open
     */
    @Override
    public void close() {
        lockManagement.close(user, locks);
    }

    /**
     * Releases a single lock.
     *
     * @param uri
     *            file whose lock is to be released
     * @throws IllegalStateException
     *             if the lock can not be released, there is still a current
     *             flowing through the lock
     */
    @Override
    public void close(URI uri) {
        lockManagement.close(this, uri);
    }

    /**
     * Allows the access to close itself. It depends on whether the access is
     * set to self-closing, if something happens.
     */
    void closeYouselfIfYouShould() {
        if (selfClosing) {
            close();
        }
    }

    /**
     * In the last step, deletes an access from an access model after the access
     * has been deregistered by the lock management.
     *
     * @param uri
     *            URI of access to be deleted from the access model.
     */
    void forgetAccessTo(URI uri) {
        locks.remove(uri);
    }

    /**
     * This class only manages successfully granted locks. Because of this, this
     * method always returns the empty map.
     */
    @Override
    public Map<URI, Collection<String>> getConflicts() {
        return Collections.emptyMap();
    }

    /**
     * Returns the lock that is stored in the access model for a URI.
     *
     * @param uri
     *            URI for which the lock is deposited
     * @return the lock for the URI
     */
    AbstractLock getLock(URI uri) {
        return locks.get(uri);
    }

    /**
     * Returns the appropriate enumerated constant of the API model for a lock
     * deposited in the access model.
     *
     * @param uri
     *            URI for which the lock is deposited
     * @return the locking mode constant for the lock
     */
    LockingMode getLockingMode(URI uri) {
        return locks.get(uri).getLockingMode();
    }

    /**
     * Returns the user who has been granted these locks.
     *
     * @return the user
     */
    String getUser() {
        return user;
    }

    /**
     * Sets the access to self-closing, ie it is closed automatically when
     * closing a stream passed over it. This is helpful for one-way shares,
     * because you do not have to worry about closing the access.
     */
    public void setSelfClosing() {
        selfClosing = true;
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
    @Override
    public Map<URI, Collection<String>> tryLock(Map<URI, LockingMode> requests) throws IOException {
        LockResult result = lockManagement.tryLock(user, this, requests);
        if (result instanceof GrantedAccess) {
            locks.putAll(((GrantedAccess) result).locks);
        }
        return result.getConflicts();
    }
}
