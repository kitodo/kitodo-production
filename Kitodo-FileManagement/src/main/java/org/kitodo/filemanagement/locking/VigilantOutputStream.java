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
import java.io.OutputStream;
import java.net.URI;

/**
 * A vigilant output stream is an output stream that observes the underlying
 * stream and notifies its clients when the stream has dried up.
 */
class VigilantOutputStream extends OutputStream implements StreamGuard {
    /**
     * The immutable read file management to be notified.
     */
    private final ImmutableReadFileManagement immutableReadFileManagement;

    /**
     * The input stream to be watched.
     */
    private final OutputStream outputStream;

    private final GrantedAccess permissions;

    /**
     * The stream management to be notified.
     */
    private final StreamManagement streamManagement;

    /**
     * The upgradeable read lock to be notified.
     */
    private final UpgradeableReadLock upgradeableReadLock;

    /**
     * Where the stream writes.
     */
    private final URI uri;

    /**
     * Generates a vigilant output stream.
     * 
     * @param outputStream
     *            the input stream to be monitored
     * @param uri
     *            URI of the input stream
     * @param streamManagement
     *            the stream management to be notified
     * @param immutableReadFileManagement
     *            the immutable read file management to be notified
     * @param upgradeableReadLock
     *            the upgradeable read lock to be notified
     * @param permissions
     *            the authorization object
     */
    VigilantOutputStream(OutputStream outputStream, URI uri, StreamManagement streamManagement,
            ImmutableReadFileManagement immutableReadFileManagement, UpgradeableReadLock upgradeableReadLock,
            GrantedAccess permissions) {
        this.outputStream = outputStream;
        this.uri = uri;
        this.streamManagement = streamManagement;
        this.immutableReadFileManagement = immutableReadFileManagement;
        this.upgradeableReadLock = upgradeableReadLock;
        this.permissions = permissions;
    }

    /**
     * Closes the underlying output stream and notifies all affected parties.
     * When closing the stream guard, the underlying output stream is first
     * closed. Then, the immutable read file management is notified that the
     * content of the underlying URI has changed and that a new copy of the file
     * must be created for future users asking for an immutable read file. The
     * stream management is notified that it can fire the stream guard. If an
     * upgradeable read lock has been parameterized, it will be notified so that
     * it can adjust its state.
     */
    @Override
    public void close() throws IOException {
        outputStream.close();
        super.close();
        immutableReadFileManagement.markAsChanged(uri);
        streamManagement.processClosedStream(this);
        if (upgradeableReadLock != null) {
            upgradeableReadLock.noteWritingEnds();
        }
        permissions.closeYouselfIfYouShould();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public boolean isMonitoringAnOutputStream() {
        return true;
    }

    /**
     * A method to write an integer to a stream of numbers. With this method,
     * the accessing objects access the underlying data stream.
     */
    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }
}
