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
import java.io.InputStream;
import java.net.URI;

/**
 * A vigilant input stream is a stream that observes the underlying input stream
 * and notifies its client when the stream has dried up.
 */
class VigilantInputStream extends InputStream implements StreamGuard {
    /**
     * The input stream to be watched.
     */
    private final InputStream inputStream;

    private final GrantedAccess permissions;

    /**
     * The managing authority to be notified.
     */
    private final StreamManagement streamManagement;

    /**
     * URI, which is read by means of the input stream.
     */
    private final URI uri;

    /**
     * Generates a vigilant input stream.
     * 
     * @param uri
     *            URI of the input stream
     * @param inputStream
     *            the input stream to be monitored
     * @param streamManagement
     *            the administrative instance to be notified
     * @param permissions
     *            the authorization object
     */
    VigilantInputStream(URI uri, InputStream inputStream, StreamManagement streamManagement,
            GrantedAccess permissions) {
        this.uri = uri;
        this.inputStream = inputStream;
        this.streamManagement = streamManagement;
        this.permissions = permissions;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        super.close();
        streamManagement.processClosedStream(this);
        permissions.closeYouselfIfYouShould();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public boolean isMonitoringAnOutputStream() {
        return false;
    }

    /**
     * A method to read an integer from a stream of numbers. With this method,
     * the accessing objects access the underlying data stream.
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

}
