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

package org.kitodo.helper;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.UghHelperException;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;

public class UghHelper {
    private static final Logger logger = LogManager.getLogger(UghHelper.class);

    /**
     * Private constructor to hide the implicit public one.
     */
    private UghHelper() {

    }

    /**
     * MetadataType aus Preferences eines Prozesses ermitteln.
     *
     * @param inProzess
     *            Process object
     * @param inName
     *            String
     * @return MetadataType
     */
    public static MetadataTypeInterface getMetadataType(Process inProzess, String inName) throws UghHelperException {
        PrefsInterface myPrefs = ServiceManager.getRulesetService().getPreferences(inProzess.getRuleset());
        return getMetadataType(myPrefs, inName);
    }

    /**
     * MetadataType aus Preferences ermitteln.
     *
     * @param inPrefs
     *            Prefs object
     * @param inName
     *            String
     * @return MetadataType
     */
    public static MetadataTypeInterface getMetadataType(PrefsInterface inPrefs, String inName) throws UghHelperException {
        MetadataTypeInterface mdt = inPrefs.getMetadataTypeByName(inName);
        if (mdt == null) {
            throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
        }
        return mdt;
    }

    /**
     * Metadata eines Docstructs ermitteln.
     *
     * @param inStruct
     *            DocStruct object
     * @param inMetadataType
     *            MetadataType object
     * @return Metadata
     */
    public static MetadataInterface getMetadata(DocStructInterface inStruct, MetadataTypeInterface inMetadataType) {
        if (inStruct != null && inMetadataType != null) {
            List<? extends MetadataInterface> all = inStruct.getAllMetadataByType(inMetadataType);
            if (all.isEmpty()) {
                try {
                    MetadataInterface md = UghImplementation.INSTANCE.createMetadata(inMetadataType);
                    md.setDocStruct(inStruct);
                    inStruct.addMetadata(md);
                    return md;
                } catch (MetadataTypeNotAllowedException e) {
                    logger.debug(e.getMessage());
                    return null;
                }
            } else {
                return all.get(0);
            }
        }
        return null;
    }

}
