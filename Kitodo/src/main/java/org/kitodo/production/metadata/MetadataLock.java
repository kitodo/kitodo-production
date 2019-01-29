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

import org.kitodo.production.config.ConfigCore;
import org.kitodo.production.config.enums.ParameterCore;

/**
 * Bean für die Sperrung der Metadaten.
 */
public class MetadataLock implements Serializable {
    private static final long serialVersionUID = -8248209179063050307L;
    private static HashMap<Integer, HashMap<String, String>> locks = new HashMap<>();
    private static final String USER = "Benutzer";
    private static final String LIFE_SIGN = "Lebenszeichen";
    /*
     * Zeit, innerhalb der der Benutzer handeln muss, um seine Sperrung zu
     * behalten (30 min)
     */
    private static final long LOCKING_TIME = ConfigCore.getLongParameterOrDefaultValue(ParameterCore.METS_EDITOR_LOCKING_TIME);

    /**
     * Metadaten eines bestimmten Prozesses wieder freigeben.
     */
    public void setFree(int prozessID) {
        locks.remove(prozessID);
    }

    /**
     * Metadaten eines bestimmten Prozesses für einen Benutzer sperren.
     */
    public void setLocked(int prozessID, String benutzerID) {
        HashMap<String, String> map = new HashMap<>();
        map.put(USER, benutzerID);
        map.put(LIFE_SIGN, String.valueOf(System.currentTimeMillis()));
        locks.put(prozessID, map);
    }

    /**
     * prüfen, ob bestimmte Metadaten noch durch anderen Benutzer gesperrt sind.
     */
    public static boolean isLocked(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* wenn der Prozess nicht in der Hashpmap ist, ist er nicht gesperrt */
        if (temp == null) {
            return false;
        } else {
            /* wenn er in der Hashmap ist, muss die Zeit geprüft werden */
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
     * Benutzer zurückgeben, der Metadaten gesperrt hat.
     */
    public String getLockUser(int processID) {
        String result = "-1";
        HashMap<String, String> temp = locks.get(processID);
        /*
         * wenn der Prozess nicht in der Hashpmap ist, gibt es keinen Benutzer
         */
        if (temp != null) {
            result = temp.get(USER);
        }
        return result;
    }

    /**
     * Remove lock for process.
     *
     * @param processID
     *            Id of process to unlock
     */
    public static void unlockProcess(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* wenn der Prozess in der Hashpmap ist, dort rausnehmen */
        if (temp != null) {
            locks.remove(processID);
        }
    }

    /**
     * Sekunden zurückgeben, seit der letzten Bearbeitung der Metadaten.
     */
    public long getLockSeconds(int processID) {
        HashMap<String, String> temp = locks.get(processID);
        /* wenn der Prozess nicht in der Hashmap ist, gibt es keine Zeit */
        if (temp == null) {
            return 0;
        } else {
            return (System.currentTimeMillis() - Long.parseLong(temp.get(LIFE_SIGN))) / 1000;
        }
    }
}
