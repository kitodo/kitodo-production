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

import java.util.Collection;
import java.util.LinkedList;

/**
 * A container for streams flowing over a URI.
 */
class StreamsPerURI {
    /**
     * The input streams that flow over the URI.
     */
    private final Collection<StreamGuard> inputStreamGuards = new LinkedList<>();

    /**
     * The output streams that flow over the URI.
     */
    private final Collection<StreamGuard> outputStreamGuards = new LinkedList<>();

    /**
     * Adds a new stream guard to the administrative unit.
     *
     * @param streamGuard
     *            stram guard to add
     */
    void add(StreamGuard streamGuard) {
        if (streamGuard.isMonitoringAnOutputStream()) {
            outputStreamGuards.add(streamGuard);
        } else {
            inputStreamGuards.add(streamGuard);
        }

    }

    /**
     * Returns whether there is an output stream guard here.
     *
     * @return whether there is an output stream guard
     */
    boolean isContainingAnOutputStreamGuard() {
        return !outputStreamGuards.isEmpty();
    }

    /**
     * Returns whether there is no stream guard here.
     *
     * @return whether there is no stream guard
     */
    boolean isEmpty() {
        return inputStreamGuards.isEmpty() && outputStreamGuards.isEmpty();
    }

    /**
     * Dismisses a stream guard from the administrative unit.
     *
     * @param streamGuard
     *            the stream guard to be dismissed
     */
    void remove(StreamGuard streamGuard) {
        if (streamGuard.isMonitoringAnOutputStream()) {
            outputStreamGuards.remove(streamGuard);
        } else {
            inputStreamGuards.remove(streamGuard);
        }
    }
}
