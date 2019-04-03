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

package org.kitodo.production.services.dataformat;

import java.net.URI;

import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.filemanagement.LockResult;

/**
 * An open workpiece contains the data of a workpiece, and in addition open
 * locks, which are needed for subsequent saving of the workpiece. Please note
 * that an open workpiece must be {@code close()}d after finishing.
 */
public class OpenWorkpiece implements AutoCloseable {
    /**
     * The locks that the user holds.
     */
    private LockResult lockResult;

    /**
     * The data of the workpiece.
     */
    private final Workpiece workpiece;

    /**
     * The URI where the workpiece is saved as a file.
     */
    private URI uri;

    /**
     * Creates a new open work piece with an empty workpiece and without any
     * locks.
     */
    public OpenWorkpiece() {
        this.workpiece = new Workpiece();
    }

    /**
     * Creates a new open workpiece with a workpiece, with its URI and open
     * locks.
     *
     * @param lockResult
     *            Result of the lock request
     * @param uri
     *            URI of the workpiece
     * @param workpiece
     *            data of the workpiece
     */
    OpenWorkpiece(LockResult lockResult, URI uri, Workpiece workpiece) {
        this.lockResult = lockResult;
        this.uri = uri;
        this.workpiece = workpiece;
    }

    /**
     * Releases all locks. The open workpiece implements the AutoCloseable
     * interface. So it can be used in a try-with-resource expression. Closing
     * causes the release of all locks and is therefore important.
     */
    @Override
    public void close() {
        lockResult.close();
    }

    /**
     * Returns the lock results.
     * 
     * @return the lock results
     */
    LockResult getLockResult() {
        return lockResult;
    }

    /**
     * Sets a lock result. The method is used when an open workpiece with an
     * empty workpiece and without locks is saved the first time.
     *
     * @param lockResult
     *            lock result to set
     */
    void setLockResult(LockResult lockResult) {
        this.lockResult = lockResult;
    }

    /**
     * Returns the URI of the workpiece. This is the URI for which there is an
     * exclusive open lock.
     * 
     * @return the URI of the workpiece
     */
    URI getUri() {
        return uri;
    }

    /**
     * Sets the URI of the workpiece. The method is used when a workpiece is
     * saved to a different URI.
     * 
     * @param uri
     *            URI of the workpiece to set.
     */
    void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the data of the workpiece.
     * 
     * @return the workpiece
     */
    public Workpiece getWorkpiece() {
        return workpiece;
    }

}
