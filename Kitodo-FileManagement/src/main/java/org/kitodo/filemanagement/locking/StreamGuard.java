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

/**
 * A stream guard observes the underlying stream and notifies its client when
 * the stream has been closed.
 */
interface StreamGuard {
    /**
     * Here, the implementing class must return the URI of the monitored stream.
     *
     * @return the URI of the monitored stream
     */
    URI getURI();

    /**
     * Here, the implementing class must return whether it is monitoring an
     * output stream (or an input stream).
     *
     * @return true, when an output stream is being monitored
     */
    boolean isMonitoringAnOutputStream();
}
