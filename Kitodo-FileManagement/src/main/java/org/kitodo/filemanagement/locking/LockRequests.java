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
import java.util.Map;
import java.util.stream.Collectors;

import org.kitodo.api.filemanagement.LockingMode;

/**
 * Capsules a user request for locks.
 */
class LockRequests {
    /**
     * Which locks the user would like.
     */
    private final Map<URI, LockingMode> locks;

    /**
     * Which user requests the locks.
     */
    private final String user;

    /**
     * Create a new request object.
     *
     * @param user
     *            user making the request
     * @param locks
     *            which locks he or she would like
     */
    LockRequests(String user, Map<URI, LockingMode> locks) {
        this.user = user;
        this.locks = locks;
    }

    /**
     * Compares two lock objects.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LockRequests other = (LockRequests) obj;
        if (locks == null) {
            if (other.locks != null) {
                return false;
            }
        } else if (!locks.equals(other.locks)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the desired locks specified in the request as a nicely formatted
     * string.
     *
     * @return the locks as a string
     */
    String formatLocksAsString() {
        return locks.entrySet().parallelStream()
                .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString())
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the desired locks specified in the request.
     *
     * @return the locks
     */
    Map<URI, LockingMode> getLocks() {
        return locks;
    }

    /**
     * Returns the name of the requesting user.
     *
     * @return the user
     */
    String getUser() {
        return user;
    }

    /**
     * Computes a pseudorandom number to distribute instances of this class in a
     * hash data structure as widely as possible.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (locks == null ? 0 : locks.hashCode());
        result = prime * result + (user == null ? 0 : user.hashCode());
        return result;
    }

    /**
     * Formats the content of the object into a string for display in the
     * debugger.
     */
    @Override
    public String toString() {
        return "LockRequests [user=" + user + ", locks=" + formatLocksAsString() + "]";
    }
}
