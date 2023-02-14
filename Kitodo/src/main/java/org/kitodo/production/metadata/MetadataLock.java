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

package org.kitodo.production.metadata;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;

/**
 * Bean for locking the metadata.
 */
@Named("MetadataLock")
@ApplicationScoped
public class MetadataLock implements Serializable {
    private static final ConcurrentHashMap<Integer, User> locks = new ConcurrentHashMap<>();

    /**
     * Unlock metadata of a particular process again.
     */
    public static void setFree(int prozessID) {
        locks.remove(prozessID);
    }

    /**
     * Lock metadata of a specific process for a user.
     */
    public static void setLocked(int prozessID, User user) {
        locks.put(prozessID, user);
    }

    /**
     * Check if certain metadata is still locked by other users.
     */
    public static boolean isLocked(int processID) {
        User user = locks.get(processID);
        /* if the process is not in the hash map, it is not locked */
        if (user == null) {
            return false;
        } else {
            /* if it is in the hash map, the user must be checked */
            return !user.equals(ServiceManager.getUserService().getCurrentUser());
        }
    }

    /**
     * Java doc.
     *
     * @param inUsername
     *            String
     */
    public static void setAllUserLocksFree(String inUsername) {
        for (Iterator<Entry<Integer, User>> intern = locks.entrySet().iterator(); intern.hasNext();) {
            if (intern.next().getValue().getLogin().equals(inUsername)) {
                intern.remove();
            }
        }
    }

    /**
     * Return a user who has locked metadata.
     */
    public static User getLockUser(int processID) {
        return locks.get(processID);
    }
}
