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
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;

/**
 * This class is used to return conflicts if the lock was unsuccessful.
 */
class DeniedAccess extends LockResult {
    /**
     * The conflicts to be returned.
     */
    private final Map<URI, Collection<String>> conflicts;

    /**
     * Generates a new result object in case locks could not be granted.
     *
     * @param conflicts
     *            why did not work
     */
    public DeniedAccess(Map<URI, Collection<String>> conflicts) {
        if (conflicts.isEmpty()) {
            throw new IllegalArgumentException("the conflict map should not be empty");
        }
        this.conflicts = conflicts;
    }

    /**
     * If no locks have been granted, nothing can be reset. This method is
     * automatically called when exiting the try-with-resources statement and
     * should therefore not throw an exception.
     */
    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void close(URI uri) {
        throw new UnsupportedOperationException(
                "The lock on " + uri + " cannot be reset because it was not granted in this request.");
    }

    @Override
    public Map<URI, Collection<String>> getConflicts() {
        return conflicts;
    }

    @Override
    public String toString() {
        return "Denied Access, conflict(s): " + conflicts.entrySet().parallelStream()
                .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString())
                .collect(Collectors.joining(", "));
    }

    @Override
    public Map<URI, Collection<String>> tryLock(Map<URI, LockingMode> requests) {
        throw new UnsupportedOperationException(
                "The previously requested locks could not be granted, so you can not add any.");
    }

}
