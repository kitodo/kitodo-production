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

package org.kitodo.production.forms.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kitodo.production.helper.Helper;

/**
 * This exception can be thrown to indicate that locking files has failed.
 */
public class LockingConflictsException extends IOException {
    /**
     * The map of conflicts.
     */
    private Map<URI, Collection<String>> conflicts;

    /**
     * Creates a new locking conflicts exception.
     * 
     * @param conflicts
     *            the map of conflicts
     */
    public LockingConflictsException(Map<URI, Collection<String>> conflicts) {
        this.conflicts = conflicts;
    }

    @Override
    public String getMessage() {
        int numberOfConflicts = conflicts.size();
        if (numberOfConflicts == 1) {
            String conflictingUri = conflicts.entrySet().iterator().next().getKey().toString();
            return conflictingUri + " could not be locked because of conflicting locks by " + conflictingUsers() + '.';
        } else {
            return numberOfConflicts + " files could not be locked because of conflicting locks by "
                    + conflictingUsers() + '.';
        }
    }

    /**
     * Unifies the conflicting users and returns them as a string.
     * 
     * @return the conflicting users, unified
     */
    private String conflictingUsers() {
        Set<String> elements = new HashSet<>();
        for (Entry<URI, Collection<String>> conflict : conflicts.entrySet()) {
            elements.addAll(conflict.getValue());
        }
        return String.join(" & ", elements);
    }

    @Override
    public String getLocalizedMessage() {
        int numberOfConflicts = conflicts.size();
        if (numberOfConflicts == 1) {
            String conflictingUri = conflicts.entrySet().iterator().next().getKey().toString();
            return Helper.getTranslation("dataEditor.lockingConflict",
                Arrays.asList(conflictingUri, conflictingUsers()));
        } else {
            return Helper.getTranslation("dataEditor.lockingConflicts",
                Arrays.asList(Integer.toString(numberOfConflicts), conflictingUsers()));
        }
    }
}
