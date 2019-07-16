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
import java.util.HashMap;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;

/**
 * Bean for locking the metadata.
 */
public class MetadataLock implements Serializable {
    private static HashMap<Integer, HashMap<String, String>> locks = new HashMap<>();
    private static final String USER = "Benutzer";
    private static final String LIFE_SIGN = "Lebenszeichen";
    /*
     * Time within which the user must act to keep his lock (30 min)
     */
    private static final long LOCKING_TIME = ConfigCore.getLongParameterOrDefaultValue(ParameterCore.METS_EDITOR_LOCKING_TIME);

    /**
     * Unlock metadata of a particular process again.
     */
    public void setFree(int prozessID) {
        locks.remove(prozessID);
    }

    /**
     * Lock metadata of a specific process for a user.
     */
    public void setLocked(int prozessID, String benutzerID) {
        HashMap<String, String> map = new HashMap<>();
        map.put(USER, benutzerID);
        map.put(LIFE_SIGN, String.valueOf(System.currentTimeMillis()));
        locks.put(prozessID, map);
    }

    /**
     * Check if certain metadata is still locked by other users.
     */
    public static boolean isLocked(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* if the process is not in the hash map, it is not locked */
        if (temp == null) {
            return false;
        } else {
            /* if it is in the hash map, the time must be checked */
            long lifeSign = Long.parseLong(temp.get(LIFE_SIGN));
            return lifeSign >= System.currentTimeMillis() - LOCKING_TIME;
        }
    }

    /**
     * Java doc.
     *
     * @param inBenutzerID
     *            Integer
     */
    public void alleBenutzerSperrungenAufheben(Integer inBenutzerID) {
        String inBenutzerString = String.valueOf(inBenutzerID.intValue());
        HashMap<Integer, HashMap<String, String>> temp = new HashMap<>(locks);
        for (Integer key : temp.keySet()) {
            HashMap<String, String> intern = locks.get(key);
            if (intern.get(USER).equals(inBenutzerString)) {
                locks.remove(key);
            }
        }
    }

    /**
     * Return a user who has locked metadata.
     */
    public String getLockUser(int processID) {
        String lockUser = "-1";
        HashMap<String, String> temp = locks.get(processID);
        /*
         * if the process is not in the hash map, there is no user
         */
        if (temp != null) {
            lockUser = temp.get(USER);
        }
        return lockUser;
    }

    /**
     * Remove lock for process.
     *
     * @param processID
     *            Id of process to unlock
     */
    public static void unlockProcess(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* if the process is in the hash map, take it out there */
        if (temp != null) {
            locks.remove(processID);
        }
    }

    /**
     * Return seconds since the metadata was last edited.
     */
    public long getLockSeconds(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* if the process is not in the hash map, there is no time */
        if (temp == null) {
            return 0;
        } else {
            return (System.currentTimeMillis() - Long.parseLong(temp.get(LIFE_SIGN))) / 1000;
        }
    }
}
