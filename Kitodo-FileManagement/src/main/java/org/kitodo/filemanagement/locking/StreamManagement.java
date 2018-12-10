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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stream Management manages the input and output streams and interacts with
 * lock management. The stream management belongs to the lock management and
 * should only be instantiated by the lock management. Basically, you could have
 * programmed everything together into a class, but then it would have become
 * very large, hence the division.
 */
class StreamManagement {
    /**
     * Stores which streams are currently open.
     */
    private final Map<URI, StreamsPerURI> openStreams = new HashMap<>();

    /**
     * Returns whether the stream management knows an open output stream for the
     * specified URI.
     *
     * @param uriOfStream
     *            URI to which the question relates
     * @return whether there is an open output stream for the URI
     */
    boolean isKnowingAnOpenOutputStreamTo(URI uriOfStream) {
        StreamsPerURI streamsPerURI = openStreams.get(uriOfStream);
        return Objects.nonNull(streamsPerURI) && streamsPerURI.isContainingAnOutputStreamGuard();
    }

    /**
     * Returns whether the stream management knows an open stream for the
     * specified URI.
     *
     * @param uriOfStream
     *            URI to which the question relates
     * @return whether there is an open stream for the URI
     */
    boolean isKnowingAnOpenStreamTo(URI uriOfStream) {
        return openStreams.containsKey(uriOfStream);
    }

    /**
     * Processes the information that a stream has been closed.
     *
     * @param streamGuard
     *            stream guard who has watched the closing of the stream
     */
    void processClosedStream(StreamGuard streamGuard) {
        URI uriOfStream = streamGuard.getURI();
        StreamsPerURI streamsPerURI = openStreams.get(uriOfStream);
        if (Objects.nonNull(streamsPerURI)) {
            streamsPerURI.remove(streamGuard);
            if (streamsPerURI.isEmpty()) {
                openStreams.remove(uriOfStream);
            }
        }
    }

    /**
     * To register a stream guard in stream management.
     *
     * @param streamGuard
     *            to be notified stream guard
     */
    void registerStreamGuard(StreamGuard streamGuard) {
        URI uriOfStream = streamGuard.getURI();
        StreamsPerURI streamsPerURI = openStreams.get(uriOfStream);
        if (Objects.isNull(streamsPerURI)) {
            streamsPerURI = new StreamsPerURI();
            openStreams.put(uriOfStream, streamsPerURI);
        }
        streamsPerURI.add(streamGuard);
    }
}
