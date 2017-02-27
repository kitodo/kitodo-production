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

package de.sub.goobi.helper;

import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class UghHelper {
    private static final Logger logger = Logger.getLogger(UghHelper.class);

    /**
     * MetadataType aus Preferences eines Prozesses ermitteln
     *
     * @param inProzess
     * @param inName
     * @return MetadataType
     * @throws UghHelperException
     */
    public static MetadataType getMetadataType(Prozess inProzess, String inName) throws UghHelperException {
        Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
        return getMetadataType(myPrefs, inName);
    }

    /**
     * MetadataType aus Preferences ermitteln
     *
     * @param inPrefs
     * @param inName
     * @return MetadataType
     * @throws UghHelperException
     */
    public static MetadataType getMetadataType(Prefs inPrefs, String inName) throws UghHelperException {
        MetadataType mdt = inPrefs.getMetadataTypeByName(inName);
        if (mdt == null) {
            throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
        }
        return mdt;
    }

    /**
     * Metadata eines Docstructs ermitteln
     *
     * @param inStruct
     * @param inMetadataType
     * @return Metadata
     */
    public static Metadata getMetadata(DocStruct inStruct, MetadataType inMetadataType) {
        if (inStruct != null && inMetadataType != null) {
            List<? extends Metadata> all = inStruct.getAllMetadataByType(inMetadataType);
            if (all.size() == 0) {
                try {
                    Metadata md = new Metadata(inMetadataType);
                    md.setDocStruct(inStruct);
                    inStruct.addMetadata(md);

                    return md;
                } catch (MetadataTypeNotAllowedException e) {
                    logger.debug(e.getMessage());
                    return null;
                }
            }
            if (all.size() != 0) {
                return all.get(0);
            } else {
                return null;
            }
        }
        return null;
    }

}
