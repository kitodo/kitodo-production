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

import de.sub.goobi.helper.exceptions.UghHelperException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStruct;
import org.kitodo.api.ugh.Metadata;
import org.kitodo.api.ugh.MetadataType;
import org.kitodo.api.ugh.Prefs;
import org.kitodo.api.ugh.UghImplementation;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class UghHelper {
    private static final Logger logger = LogManager.getLogger(UghHelper.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * MetadataType aus Preferences eines Prozesses ermitteln.
     *
     * @param inProzess
     *            Process object
     * @param inName
     *            String
     * @return MetadataType
     */
    public static MetadataType getMetadataType(Process inProzess, String inName) throws UghHelperException {
        Prefs myPrefs = serviceManager.getRulesetService().getPreferences(inProzess.getRuleset());
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
    public static MetadataType getMetadataType(Prefs inPrefs, String inName) throws UghHelperException {
        MetadataType mdt = inPrefs.getMetadataTypeByName(inName);
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
    public static Metadata getMetadata(DocStruct inStruct, MetadataType inMetadataType) {
        if (inStruct != null && inMetadataType != null) {
            List<? extends Metadata> all = inStruct.getAllMetadataByType(inMetadataType);
            if (all.size() == 0) {
                try {
                    Metadata md = UghImplementation.INSTANCE.createMetadata(inMetadataType);
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
