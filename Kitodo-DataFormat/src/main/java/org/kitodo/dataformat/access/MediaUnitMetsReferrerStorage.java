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

package org.kitodo.dataformat.access;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.kitodo.api.dataformat.MediaUnit;

/**
 * A memory brick to preserve the METS IDs of the media unit div and the media
 * files.
 */
class MediaUnitMetsReferrerStorage extends MediaUnit {
    /**
     * Saves the METS identifier for the division.
     */
    private String metsDivReferrerId;

    /**
     * Save the METS identifiers for the files.
     */
    private Map<URI, String> metsFileReferrerIds = new HashMap<>();

    /**
     * Passthrough function for reading the file identifier to the URI.
     * 
     * @param fLocatXmlElementAccess
     *            Access object on f locat
     * @return Access object on f locat
     */
    FLocatXmlElementAccess storeFileId(FLocatXmlElementAccess fLocatXmlElementAccess) {
        metsFileReferrerIds.put(fLocatXmlElementAccess.getUri(), fLocatXmlElementAccess.getFileId());
        return fLocatXmlElementAccess;
    }

    /**
     * Returns the ID of div, or if unknown, creates a new one.
     * 
     * @return the ID of div
     */
    String getDivId() {
        return metsDivReferrerId != null ? metsDivReferrerId : UUID.randomUUID().toString();
    }

    /**
     * Set the ID of div.
     * 
     * @param id
     *            ID of div to set
     */
    void setDivId(String id) {
        this.metsDivReferrerId = id;
    }

    /**
     * Returns the ID of file.
     * 
     * @param key
     *            URI whose ID to return
     * @return the ID of file
     */
    String getFileId(URI key) {
        return metsFileReferrerIds.get(key);
    }
}
